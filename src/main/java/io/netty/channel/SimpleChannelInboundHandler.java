/*   1:    */ package io.netty.channel;
/*   2:    */ 
/*   3:    */ import io.netty.util.ReferenceCountUtil;
/*   4:    */ import io.netty.util.internal.TypeParameterMatcher;
/*   5:    */ 
/*   6:    */ public abstract class SimpleChannelInboundHandler<I>
/*   7:    */   extends ChannelInboundHandlerAdapter
/*   8:    */ {
/*   9:    */   private final TypeParameterMatcher matcher;
/*  10:    */   private final boolean autoRelease;
/*  11:    */   
/*  12:    */   protected SimpleChannelInboundHandler()
/*  13:    */   {
/*  14: 57 */     this(true);
/*  15:    */   }
/*  16:    */   
/*  17:    */   protected SimpleChannelInboundHandler(boolean autoRelease)
/*  18:    */   {
/*  19: 67 */     this.matcher = TypeParameterMatcher.find(this, SimpleChannelInboundHandler.class, "I");
/*  20: 68 */     this.autoRelease = autoRelease;
/*  21:    */   }
/*  22:    */   
/*  23:    */   protected SimpleChannelInboundHandler(Class<? extends I> inboundMessageType)
/*  24:    */   {
/*  25: 75 */     this(inboundMessageType, true);
/*  26:    */   }
/*  27:    */   
/*  28:    */   protected SimpleChannelInboundHandler(Class<? extends I> inboundMessageType, boolean autoRelease)
/*  29:    */   {
/*  30: 86 */     this.matcher = TypeParameterMatcher.get(inboundMessageType);
/*  31: 87 */     this.autoRelease = autoRelease;
/*  32:    */   }
/*  33:    */   
/*  34:    */   public boolean acceptInboundMessage(Object msg)
/*  35:    */     throws Exception
/*  36:    */   {
/*  37: 95 */     return this.matcher.match(msg);
/*  38:    */   }
/*  39:    */   
/*  40:    */   public void channelRead(ChannelHandlerContext ctx, Object msg)
/*  41:    */     throws Exception
/*  42:    */   {
/*  43:100 */     boolean release = true;
/*  44:    */     try
/*  45:    */     {
/*  46:102 */       if (acceptInboundMessage(msg))
/*  47:    */       {
/*  48:104 */         I imsg = msg;
/*  49:105 */         channelRead0(ctx, imsg);
/*  50:    */       }
/*  51:    */       else
/*  52:    */       {
/*  53:107 */         release = false;
/*  54:108 */         ctx.fireChannelRead(msg);
/*  55:    */       }
/*  56:    */     }
/*  57:    */     finally
/*  58:    */     {
/*  59:111 */       if ((this.autoRelease) && (release)) {
/*  60:112 */         ReferenceCountUtil.release(msg);
/*  61:    */       }
/*  62:    */     }
/*  63:    */   }
/*  64:    */   
/*  65:    */   protected abstract void channelRead0(ChannelHandlerContext paramChannelHandlerContext, I paramI)
/*  66:    */     throws Exception;
/*  67:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.SimpleChannelInboundHandler
 * JD-Core Version:    0.7.0.1
 */