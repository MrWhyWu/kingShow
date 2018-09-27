/*   1:    */ package io.netty.channel;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.PlatformDependent;
/*   4:    */ import io.netty.util.internal.logging.InternalLogger;
/*   5:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   6:    */ import java.util.concurrent.ConcurrentMap;
/*   7:    */ 
/*   8:    */ @ChannelHandler.Sharable
/*   9:    */ public abstract class ChannelInitializer<C extends Channel>
/*  10:    */   extends ChannelInboundHandlerAdapter
/*  11:    */ {
/*  12: 55 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(ChannelInitializer.class);
/*  13: 58 */   private final ConcurrentMap<ChannelHandlerContext, Boolean> initMap = PlatformDependent.newConcurrentHashMap();
/*  14:    */   
/*  15:    */   protected abstract void initChannel(C paramC)
/*  16:    */     throws Exception;
/*  17:    */   
/*  18:    */   public final void channelRegistered(ChannelHandlerContext ctx)
/*  19:    */     throws Exception
/*  20:    */   {
/*  21: 76 */     if (initChannel(ctx)) {
/*  22: 79 */       ctx.pipeline().fireChannelRegistered();
/*  23:    */     } else {
/*  24: 82 */       ctx.fireChannelRegistered();
/*  25:    */     }
/*  26:    */   }
/*  27:    */   
/*  28:    */   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
/*  29:    */     throws Exception
/*  30:    */   {
/*  31: 91 */     logger.warn("Failed to initialize a channel. Closing: " + ctx.channel(), cause);
/*  32: 92 */     ctx.close();
/*  33:    */   }
/*  34:    */   
/*  35:    */   public void handlerAdded(ChannelHandlerContext ctx)
/*  36:    */     throws Exception
/*  37:    */   {
/*  38:100 */     if (ctx.channel().isRegistered()) {
/*  39:105 */       initChannel(ctx);
/*  40:    */     }
/*  41:    */   }
/*  42:    */   
/*  43:    */   private boolean initChannel(ChannelHandlerContext ctx)
/*  44:    */     throws Exception
/*  45:    */   {
/*  46:111 */     if (this.initMap.putIfAbsent(ctx, Boolean.TRUE) == null)
/*  47:    */     {
/*  48:    */       try
/*  49:    */       {
/*  50:113 */         initChannel(ctx.channel());
/*  51:    */       }
/*  52:    */       catch (Throwable cause)
/*  53:    */       {
/*  54:117 */         exceptionCaught(ctx, cause);
/*  55:    */       }
/*  56:    */       finally
/*  57:    */       {
/*  58:119 */         remove(ctx);
/*  59:    */       }
/*  60:121 */       return true;
/*  61:    */     }
/*  62:123 */     return false;
/*  63:    */   }
/*  64:    */   
/*  65:    */   private void remove(ChannelHandlerContext ctx)
/*  66:    */   {
/*  67:    */     try
/*  68:    */     {
/*  69:128 */       ChannelPipeline pipeline = ctx.pipeline();
/*  70:129 */       if (pipeline.context(this) != null) {
/*  71:130 */         pipeline.remove(this);
/*  72:    */       }
/*  73:133 */       this.initMap.remove(ctx);
/*  74:    */     }
/*  75:    */     finally
/*  76:    */     {
/*  77:133 */       this.initMap.remove(ctx);
/*  78:    */     }
/*  79:    */   }
/*  80:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.ChannelInitializer
 * JD-Core Version:    0.7.0.1
 */