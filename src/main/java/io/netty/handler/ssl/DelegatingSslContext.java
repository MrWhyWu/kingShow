/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.util.internal.ObjectUtil;
/*   5:    */ import java.util.List;
/*   6:    */ import javax.net.ssl.SSLEngine;
/*   7:    */ import javax.net.ssl.SSLSessionContext;
/*   8:    */ 
/*   9:    */ public abstract class DelegatingSslContext
/*  10:    */   extends SslContext
/*  11:    */ {
/*  12:    */   private final SslContext ctx;
/*  13:    */   
/*  14:    */   protected DelegatingSslContext(SslContext ctx)
/*  15:    */   {
/*  16: 33 */     this.ctx = ((SslContext)ObjectUtil.checkNotNull(ctx, "ctx"));
/*  17:    */   }
/*  18:    */   
/*  19:    */   public final boolean isClient()
/*  20:    */   {
/*  21: 38 */     return this.ctx.isClient();
/*  22:    */   }
/*  23:    */   
/*  24:    */   public final List<String> cipherSuites()
/*  25:    */   {
/*  26: 43 */     return this.ctx.cipherSuites();
/*  27:    */   }
/*  28:    */   
/*  29:    */   public final long sessionCacheSize()
/*  30:    */   {
/*  31: 48 */     return this.ctx.sessionCacheSize();
/*  32:    */   }
/*  33:    */   
/*  34:    */   public final long sessionTimeout()
/*  35:    */   {
/*  36: 53 */     return this.ctx.sessionTimeout();
/*  37:    */   }
/*  38:    */   
/*  39:    */   public final ApplicationProtocolNegotiator applicationProtocolNegotiator()
/*  40:    */   {
/*  41: 58 */     return this.ctx.applicationProtocolNegotiator();
/*  42:    */   }
/*  43:    */   
/*  44:    */   public final SSLEngine newEngine(ByteBufAllocator alloc)
/*  45:    */   {
/*  46: 63 */     SSLEngine engine = this.ctx.newEngine(alloc);
/*  47: 64 */     initEngine(engine);
/*  48: 65 */     return engine;
/*  49:    */   }
/*  50:    */   
/*  51:    */   public final SSLEngine newEngine(ByteBufAllocator alloc, String peerHost, int peerPort)
/*  52:    */   {
/*  53: 70 */     SSLEngine engine = this.ctx.newEngine(alloc, peerHost, peerPort);
/*  54: 71 */     initEngine(engine);
/*  55: 72 */     return engine;
/*  56:    */   }
/*  57:    */   
/*  58:    */   protected final SslHandler newHandler(ByteBufAllocator alloc, boolean startTls)
/*  59:    */   {
/*  60: 77 */     SslHandler handler = this.ctx.newHandler(alloc, startTls);
/*  61: 78 */     initHandler(handler);
/*  62: 79 */     return handler;
/*  63:    */   }
/*  64:    */   
/*  65:    */   protected final SslHandler newHandler(ByteBufAllocator alloc, String peerHost, int peerPort, boolean startTls)
/*  66:    */   {
/*  67: 84 */     SslHandler handler = this.ctx.newHandler(alloc, peerHost, peerPort, startTls);
/*  68: 85 */     initHandler(handler);
/*  69: 86 */     return handler;
/*  70:    */   }
/*  71:    */   
/*  72:    */   public final SSLSessionContext sessionContext()
/*  73:    */   {
/*  74: 91 */     return this.ctx.sessionContext();
/*  75:    */   }
/*  76:    */   
/*  77:    */   protected abstract void initEngine(SSLEngine paramSSLEngine);
/*  78:    */   
/*  79:    */   protected void initHandler(SslHandler handler)
/*  80:    */   {
/*  81:104 */     initEngine(handler.engine());
/*  82:    */   }
/*  83:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.DelegatingSslContext
 * JD-Core Version:    0.7.0.1
 */