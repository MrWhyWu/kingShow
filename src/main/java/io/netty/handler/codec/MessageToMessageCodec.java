/*   1:    */ package io.netty.handler.codec;
/*   2:    */ 
/*   3:    */ import io.netty.channel.ChannelDuplexHandler;
/*   4:    */ import io.netty.channel.ChannelHandlerContext;
/*   5:    */ import io.netty.channel.ChannelPromise;
/*   6:    */ import io.netty.util.internal.TypeParameterMatcher;
/*   7:    */ import java.util.List;
/*   8:    */ 
/*   9:    */ public abstract class MessageToMessageCodec<INBOUND_IN, OUTBOUND_IN>
/*  10:    */   extends ChannelDuplexHandler
/*  11:    */ {
/*  12: 57 */   private final MessageToMessageEncoder<Object> encoder = new MessageToMessageEncoder()
/*  13:    */   {
/*  14:    */     public boolean acceptOutboundMessage(Object msg)
/*  15:    */       throws Exception
/*  16:    */     {
/*  17: 61 */       return MessageToMessageCodec.this.acceptOutboundMessage(msg);
/*  18:    */     }
/*  19:    */     
/*  20:    */     protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out)
/*  21:    */       throws Exception
/*  22:    */     {
/*  23: 67 */       MessageToMessageCodec.this.encode(ctx, msg, out);
/*  24:    */     }
/*  25:    */   };
/*  26: 71 */   private final MessageToMessageDecoder<Object> decoder = new MessageToMessageDecoder()
/*  27:    */   {
/*  28:    */     public boolean acceptInboundMessage(Object msg)
/*  29:    */       throws Exception
/*  30:    */     {
/*  31: 75 */       return MessageToMessageCodec.this.acceptInboundMessage(msg);
/*  32:    */     }
/*  33:    */     
/*  34:    */     protected void decode(ChannelHandlerContext ctx, Object msg, List<Object> out)
/*  35:    */       throws Exception
/*  36:    */     {
/*  37: 81 */       MessageToMessageCodec.this.decode(ctx, msg, out);
/*  38:    */     }
/*  39:    */   };
/*  40:    */   private final TypeParameterMatcher inboundMsgMatcher;
/*  41:    */   private final TypeParameterMatcher outboundMsgMatcher;
/*  42:    */   
/*  43:    */   protected MessageToMessageCodec()
/*  44:    */   {
/*  45: 93 */     this.inboundMsgMatcher = TypeParameterMatcher.find(this, MessageToMessageCodec.class, "INBOUND_IN");
/*  46: 94 */     this.outboundMsgMatcher = TypeParameterMatcher.find(this, MessageToMessageCodec.class, "OUTBOUND_IN");
/*  47:    */   }
/*  48:    */   
/*  49:    */   protected MessageToMessageCodec(Class<? extends INBOUND_IN> inboundMessageType, Class<? extends OUTBOUND_IN> outboundMessageType)
/*  50:    */   {
/*  51:105 */     this.inboundMsgMatcher = TypeParameterMatcher.get(inboundMessageType);
/*  52:106 */     this.outboundMsgMatcher = TypeParameterMatcher.get(outboundMessageType);
/*  53:    */   }
/*  54:    */   
/*  55:    */   public void channelRead(ChannelHandlerContext ctx, Object msg)
/*  56:    */     throws Exception
/*  57:    */   {
/*  58:111 */     this.decoder.channelRead(ctx, msg);
/*  59:    */   }
/*  60:    */   
/*  61:    */   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
/*  62:    */     throws Exception
/*  63:    */   {
/*  64:116 */     this.encoder.write(ctx, msg, promise);
/*  65:    */   }
/*  66:    */   
/*  67:    */   public boolean acceptInboundMessage(Object msg)
/*  68:    */     throws Exception
/*  69:    */   {
/*  70:125 */     return this.inboundMsgMatcher.match(msg);
/*  71:    */   }
/*  72:    */   
/*  73:    */   public boolean acceptOutboundMessage(Object msg)
/*  74:    */     throws Exception
/*  75:    */   {
/*  76:134 */     return this.outboundMsgMatcher.match(msg);
/*  77:    */   }
/*  78:    */   
/*  79:    */   protected abstract void encode(ChannelHandlerContext paramChannelHandlerContext, OUTBOUND_IN paramOUTBOUND_IN, List<Object> paramList)
/*  80:    */     throws Exception;
/*  81:    */   
/*  82:    */   protected abstract void decode(ChannelHandlerContext paramChannelHandlerContext, INBOUND_IN paramINBOUND_IN, List<Object> paramList)
/*  83:    */     throws Exception;
/*  84:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.MessageToMessageCodec
 * JD-Core Version:    0.7.0.1
 */