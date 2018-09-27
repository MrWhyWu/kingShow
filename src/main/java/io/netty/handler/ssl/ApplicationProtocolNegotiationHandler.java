/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import io.netty.channel.ChannelHandlerContext;
/*   4:    */ import io.netty.channel.ChannelInboundHandlerAdapter;
/*   5:    */ import io.netty.channel.ChannelPipeline;
/*   6:    */ import io.netty.util.internal.ObjectUtil;
/*   7:    */ import io.netty.util.internal.logging.InternalLogger;
/*   8:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   9:    */ 
/*  10:    */ public abstract class ApplicationProtocolNegotiationHandler
/*  11:    */   extends ChannelInboundHandlerAdapter
/*  12:    */ {
/*  13: 65 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(ApplicationProtocolNegotiationHandler.class);
/*  14:    */   private final String fallbackProtocol;
/*  15:    */   
/*  16:    */   protected ApplicationProtocolNegotiationHandler(String fallbackProtocol)
/*  17:    */   {
/*  18: 76 */     this.fallbackProtocol = ((String)ObjectUtil.checkNotNull(fallbackProtocol, "fallbackProtocol"));
/*  19:    */   }
/*  20:    */   
/*  21:    */   public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
/*  22:    */     throws Exception
/*  23:    */   {
/*  24: 81 */     if ((evt instanceof SslHandshakeCompletionEvent))
/*  25:    */     {
/*  26: 82 */       ctx.pipeline().remove(this);
/*  27:    */       
/*  28: 84 */       SslHandshakeCompletionEvent handshakeEvent = (SslHandshakeCompletionEvent)evt;
/*  29: 85 */       if (handshakeEvent.isSuccess())
/*  30:    */       {
/*  31: 86 */         SslHandler sslHandler = (SslHandler)ctx.pipeline().get(SslHandler.class);
/*  32: 87 */         if (sslHandler == null) {
/*  33: 88 */           throw new IllegalStateException("cannot find a SslHandler in the pipeline (required for application-level protocol negotiation)");
/*  34:    */         }
/*  35: 91 */         String protocol = sslHandler.applicationProtocol();
/*  36: 92 */         configurePipeline(ctx, protocol != null ? protocol : this.fallbackProtocol);
/*  37:    */       }
/*  38:    */       else
/*  39:    */       {
/*  40: 94 */         handshakeFailure(ctx, handshakeEvent.cause());
/*  41:    */       }
/*  42:    */     }
/*  43: 98 */     ctx.fireUserEventTriggered(evt);
/*  44:    */   }
/*  45:    */   
/*  46:    */   protected abstract void configurePipeline(ChannelHandlerContext paramChannelHandlerContext, String paramString)
/*  47:    */     throws Exception;
/*  48:    */   
/*  49:    */   protected void handshakeFailure(ChannelHandlerContext ctx, Throwable cause)
/*  50:    */     throws Exception
/*  51:    */   {
/*  52:115 */     logger.warn("{} TLS handshake failed:", ctx.channel(), cause);
/*  53:116 */     ctx.close();
/*  54:    */   }
/*  55:    */   
/*  56:    */   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
/*  57:    */     throws Exception
/*  58:    */   {
/*  59:121 */     logger.warn("{} Failed to select the application-level protocol:", ctx.channel(), cause);
/*  60:122 */     ctx.close();
/*  61:    */   }
/*  62:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.ApplicationProtocolNegotiationHandler
 * JD-Core Version:    0.7.0.1
 */