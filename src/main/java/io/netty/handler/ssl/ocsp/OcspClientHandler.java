/*  1:   */ package io.netty.handler.ssl.ocsp;
/*  2:   */ 
/*  3:   */ import io.netty.channel.ChannelHandlerContext;
/*  4:   */ import io.netty.channel.ChannelInboundHandlerAdapter;
/*  5:   */ import io.netty.channel.ChannelPipeline;
/*  6:   */ import io.netty.handler.ssl.ReferenceCountedOpenSslEngine;
/*  7:   */ import io.netty.handler.ssl.SslHandshakeCompletionEvent;
/*  8:   */ import io.netty.util.internal.ObjectUtil;
/*  9:   */ import io.netty.util.internal.ThrowableUtil;
/* 10:   */ import javax.net.ssl.SSLHandshakeException;
/* 11:   */ 
/* 12:   */ public abstract class OcspClientHandler
/* 13:   */   extends ChannelInboundHandlerAdapter
/* 14:   */ {
/* 15:38 */   private static final SSLHandshakeException OCSP_VERIFICATION_EXCEPTION = (SSLHandshakeException)ThrowableUtil.unknownStackTrace(new SSLHandshakeException("Bad OCSP response"), OcspClientHandler.class, "verify(...)");
/* 16:   */   private final ReferenceCountedOpenSslEngine engine;
/* 17:   */   
/* 18:   */   protected OcspClientHandler(ReferenceCountedOpenSslEngine engine)
/* 19:   */   {
/* 20:44 */     this.engine = ((ReferenceCountedOpenSslEngine)ObjectUtil.checkNotNull(engine, "engine"));
/* 21:   */   }
/* 22:   */   
/* 23:   */   protected abstract boolean verify(ChannelHandlerContext paramChannelHandlerContext, ReferenceCountedOpenSslEngine paramReferenceCountedOpenSslEngine)
/* 24:   */     throws Exception;
/* 25:   */   
/* 26:   */   public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
/* 27:   */     throws Exception
/* 28:   */   {
/* 29:54 */     if ((evt instanceof SslHandshakeCompletionEvent))
/* 30:   */     {
/* 31:55 */       ctx.pipeline().remove(this);
/* 32:   */       
/* 33:57 */       SslHandshakeCompletionEvent event = (SslHandshakeCompletionEvent)evt;
/* 34:58 */       if ((event.isSuccess()) && (!verify(ctx, this.engine))) {
/* 35:59 */         throw OCSP_VERIFICATION_EXCEPTION;
/* 36:   */       }
/* 37:   */     }
/* 38:63 */     ctx.fireUserEventTriggered(evt);
/* 39:   */   }
/* 40:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.ocsp.OcspClientHandler
 * JD-Core Version:    0.7.0.1
 */