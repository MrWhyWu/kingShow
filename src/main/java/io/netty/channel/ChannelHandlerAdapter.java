/*  1:   */ package io.netty.channel;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.InternalThreadLocalMap;
/*  4:   */ import java.util.Map;
/*  5:   */ 
/*  6:   */ public abstract class ChannelHandlerAdapter
/*  7:   */   implements ChannelHandler
/*  8:   */ {
/*  9:   */   boolean added;
/* 10:   */   
/* 11:   */   protected void ensureNotSharable()
/* 12:   */   {
/* 13:35 */     if (isSharable()) {
/* 14:36 */       throw new IllegalStateException("ChannelHandler " + getClass().getName() + " is not allowed to be shared");
/* 15:   */     }
/* 16:   */   }
/* 17:   */   
/* 18:   */   public boolean isSharable()
/* 19:   */   {
/* 20:53 */     Class<?> clazz = getClass();
/* 21:54 */     Map<Class<?>, Boolean> cache = InternalThreadLocalMap.get().handlerSharableCache();
/* 22:55 */     Boolean sharable = (Boolean)cache.get(clazz);
/* 23:56 */     if (sharable == null)
/* 24:   */     {
/* 25:57 */       sharable = Boolean.valueOf(clazz.isAnnotationPresent(ChannelHandler.Sharable.class));
/* 26:58 */       cache.put(clazz, sharable);
/* 27:   */     }
/* 28:60 */     return sharable.booleanValue();
/* 29:   */   }
/* 30:   */   
/* 31:   */   public void handlerAdded(ChannelHandlerContext ctx)
/* 32:   */     throws Exception
/* 33:   */   {}
/* 34:   */   
/* 35:   */   public void handlerRemoved(ChannelHandlerContext ctx)
/* 36:   */     throws Exception
/* 37:   */   {}
/* 38:   */   
/* 39:   */   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
/* 40:   */     throws Exception
/* 41:   */   {
/* 42:87 */     ctx.fireExceptionCaught(cause);
/* 43:   */   }
/* 44:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.ChannelHandlerAdapter
 * JD-Core Version:    0.7.0.1
 */