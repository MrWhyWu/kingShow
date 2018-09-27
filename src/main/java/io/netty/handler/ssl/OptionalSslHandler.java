/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.ChannelHandler;
/*   5:    */ import io.netty.channel.ChannelHandlerContext;
/*   6:    */ import io.netty.channel.ChannelPipeline;
/*   7:    */ import io.netty.handler.codec.ByteToMessageDecoder;
/*   8:    */ import io.netty.util.ReferenceCountUtil;
/*   9:    */ import io.netty.util.internal.ObjectUtil;
/*  10:    */ import java.util.List;
/*  11:    */ 
/*  12:    */ public class OptionalSslHandler
/*  13:    */   extends ByteToMessageDecoder
/*  14:    */ {
/*  15:    */   private final SslContext sslContext;
/*  16:    */   
/*  17:    */   public OptionalSslHandler(SslContext sslContext)
/*  18:    */   {
/*  19: 39 */     this.sslContext = ((SslContext)ObjectUtil.checkNotNull(sslContext, "sslContext"));
/*  20:    */   }
/*  21:    */   
/*  22:    */   protected void decode(ChannelHandlerContext context, ByteBuf in, List<Object> out)
/*  23:    */     throws Exception
/*  24:    */   {
/*  25: 44 */     if (in.readableBytes() < 5) {
/*  26: 45 */       return;
/*  27:    */     }
/*  28: 47 */     if (SslHandler.isEncrypted(in)) {
/*  29: 48 */       handleSsl(context);
/*  30:    */     } else {
/*  31: 50 */       handleNonSsl(context);
/*  32:    */     }
/*  33:    */   }
/*  34:    */   
/*  35:    */   private void handleSsl(ChannelHandlerContext context)
/*  36:    */   {
/*  37: 55 */     SslHandler sslHandler = null;
/*  38:    */     try
/*  39:    */     {
/*  40: 57 */       sslHandler = newSslHandler(context, this.sslContext);
/*  41: 58 */       context.pipeline().replace(this, newSslHandlerName(), sslHandler);
/*  42: 59 */       sslHandler = null;
/*  43: 63 */       if (sslHandler != null) {
/*  44: 64 */         ReferenceCountUtil.safeRelease(sslHandler.engine());
/*  45:    */       }
/*  46:    */     }
/*  47:    */     finally
/*  48:    */     {
/*  49: 63 */       if (sslHandler != null) {
/*  50: 64 */         ReferenceCountUtil.safeRelease(sslHandler.engine());
/*  51:    */       }
/*  52:    */     }
/*  53:    */   }
/*  54:    */   
/*  55:    */   private void handleNonSsl(ChannelHandlerContext context)
/*  56:    */   {
/*  57: 70 */     ChannelHandler handler = newNonSslHandler(context);
/*  58: 71 */     if (handler != null) {
/*  59: 72 */       context.pipeline().replace(this, newNonSslHandlerName(), handler);
/*  60:    */     } else {
/*  61: 74 */       context.pipeline().remove(this);
/*  62:    */     }
/*  63:    */   }
/*  64:    */   
/*  65:    */   protected String newSslHandlerName()
/*  66:    */   {
/*  67: 83 */     return null;
/*  68:    */   }
/*  69:    */   
/*  70:    */   protected SslHandler newSslHandler(ChannelHandlerContext context, SslContext sslContext)
/*  71:    */   {
/*  72: 97 */     return sslContext.newHandler(context.alloc());
/*  73:    */   }
/*  74:    */   
/*  75:    */   protected String newNonSslHandlerName()
/*  76:    */   {
/*  77:105 */     return null;
/*  78:    */   }
/*  79:    */   
/*  80:    */   protected ChannelHandler newNonSslHandler(ChannelHandlerContext context)
/*  81:    */   {
/*  82:115 */     return null;
/*  83:    */   }
/*  84:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.OptionalSslHandler
 * JD-Core Version:    0.7.0.1
 */