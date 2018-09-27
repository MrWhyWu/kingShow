/*   1:    */ package io.netty.handler.codec;
/*   2:    */ 
/*   3:    */ import io.netty.channel.ChannelHandlerContext;
/*   4:    */ import io.netty.channel.ChannelInboundHandlerAdapter;
/*   5:    */ import io.netty.util.ReferenceCountUtil;
/*   6:    */ import io.netty.util.internal.TypeParameterMatcher;
/*   7:    */ import java.util.List;
/*   8:    */ 
/*   9:    */ public abstract class MessageToMessageDecoder<I>
/*  10:    */   extends ChannelInboundHandlerAdapter
/*  11:    */ {
/*  12:    */   private final TypeParameterMatcher matcher;
/*  13:    */   
/*  14:    */   protected MessageToMessageDecoder()
/*  15:    */   {
/*  16: 60 */     this.matcher = TypeParameterMatcher.find(this, MessageToMessageDecoder.class, "I");
/*  17:    */   }
/*  18:    */   
/*  19:    */   protected MessageToMessageDecoder(Class<? extends I> inboundMessageType)
/*  20:    */   {
/*  21: 69 */     this.matcher = TypeParameterMatcher.get(inboundMessageType);
/*  22:    */   }
/*  23:    */   
/*  24:    */   public boolean acceptInboundMessage(Object msg)
/*  25:    */     throws Exception
/*  26:    */   {
/*  27: 77 */     return this.matcher.match(msg);
/*  28:    */   }
/*  29:    */   
/*  30:    */   public void channelRead(ChannelHandlerContext ctx, Object msg)
/*  31:    */     throws Exception
/*  32:    */   {
/*  33: 82 */     CodecOutputList out = CodecOutputList.newInstance();
/*  34:    */     try
/*  35:    */     {
/*  36: 84 */       if (acceptInboundMessage(msg))
/*  37:    */       {
/*  38: 86 */         I cast = msg;
/*  39:    */         try
/*  40:    */         {
/*  41: 88 */           decode(ctx, cast, out);
/*  42:    */         }
/*  43:    */         finally
/*  44:    */         {
/*  45: 90 */           ReferenceCountUtil.release(cast);
/*  46:    */         }
/*  47:    */       }
/*  48:    */       else
/*  49:    */       {
/*  50: 93 */         out.add(msg);
/*  51:    */       }
/*  52:    */     }
/*  53:    */     catch (DecoderException e)
/*  54:    */     {
/*  55:    */       int size;
/*  56:    */       int i;
/*  57: 96 */       throw e;
/*  58:    */     }
/*  59:    */     catch (Exception e)
/*  60:    */     {
/*  61: 98 */       throw new DecoderException(e);
/*  62:    */     }
/*  63:    */     finally
/*  64:    */     {
/*  65:100 */       int size = out.size();
/*  66:101 */       for (int i = 0; i < size; i++) {
/*  67:102 */         ctx.fireChannelRead(out.getUnsafe(i));
/*  68:    */       }
/*  69:104 */       out.recycle();
/*  70:    */     }
/*  71:    */   }
/*  72:    */   
/*  73:    */   protected abstract void decode(ChannelHandlerContext paramChannelHandlerContext, I paramI, List<Object> paramList)
/*  74:    */     throws Exception;
/*  75:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.MessageToMessageDecoder
 * JD-Core Version:    0.7.0.1
 */