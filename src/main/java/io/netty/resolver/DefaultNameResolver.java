/*  1:   */ package io.netty.resolver;
/*  2:   */ 
/*  3:   */ import io.netty.util.concurrent.EventExecutor;
/*  4:   */ import io.netty.util.concurrent.Promise;
/*  5:   */ import io.netty.util.internal.SocketUtils;
/*  6:   */ import java.net.InetAddress;
/*  7:   */ import java.net.UnknownHostException;
/*  8:   */ import java.util.Arrays;
/*  9:   */ import java.util.List;
/* 10:   */ 
/* 11:   */ public class DefaultNameResolver
/* 12:   */   extends InetNameResolver
/* 13:   */ {
/* 14:   */   public DefaultNameResolver(EventExecutor executor)
/* 15:   */   {
/* 16:37 */     super(executor);
/* 17:   */   }
/* 18:   */   
/* 19:   */   protected void doResolve(String inetHost, Promise<InetAddress> promise)
/* 20:   */     throws Exception
/* 21:   */   {
/* 22:   */     try
/* 23:   */     {
/* 24:43 */       promise.setSuccess(SocketUtils.addressByName(inetHost));
/* 25:   */     }
/* 26:   */     catch (UnknownHostException e)
/* 27:   */     {
/* 28:45 */       promise.setFailure(e);
/* 29:   */     }
/* 30:   */   }
/* 31:   */   
/* 32:   */   protected void doResolveAll(String inetHost, Promise<List<InetAddress>> promise)
/* 33:   */     throws Exception
/* 34:   */   {
/* 35:   */     try
/* 36:   */     {
/* 37:52 */       promise.setSuccess(Arrays.asList(SocketUtils.allAddressesByName(inetHost)));
/* 38:   */     }
/* 39:   */     catch (UnknownHostException e)
/* 40:   */     {
/* 41:54 */       promise.setFailure(e);
/* 42:   */     }
/* 43:   */   }
/* 44:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.resolver.DefaultNameResolver
 * JD-Core Version:    0.7.0.1
 */