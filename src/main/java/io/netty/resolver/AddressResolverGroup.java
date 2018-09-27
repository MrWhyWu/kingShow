/*   1:    */ package io.netty.resolver;
/*   2:    */ 
/*   3:    */ import io.netty.util.concurrent.EventExecutor;
/*   4:    */ import io.netty.util.concurrent.Future;
/*   5:    */ import io.netty.util.concurrent.FutureListener;
/*   6:    */ import io.netty.util.internal.logging.InternalLogger;
/*   7:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   8:    */ import java.io.Closeable;
/*   9:    */ import java.net.SocketAddress;
/*  10:    */ import java.util.Collection;
/*  11:    */ import java.util.IdentityHashMap;
/*  12:    */ import java.util.Map;
/*  13:    */ 
/*  14:    */ public abstract class AddressResolverGroup<T extends SocketAddress>
/*  15:    */   implements Closeable
/*  16:    */ {
/*  17: 38 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(AddressResolverGroup.class);
/*  18: 43 */   private final Map<EventExecutor, AddressResolver<T>> resolvers = new IdentityHashMap();
/*  19:    */   
/*  20:    */   public AddressResolver<T> getResolver(final EventExecutor executor)
/*  21:    */   {
/*  22: 55 */     if (executor == null) {
/*  23: 56 */       throw new NullPointerException("executor");
/*  24:    */     }
/*  25: 59 */     if (executor.isShuttingDown()) {
/*  26: 60 */       throw new IllegalStateException("executor not accepting a task");
/*  27:    */     }
/*  28: 64 */     synchronized (this.resolvers)
/*  29:    */     {
/*  30: 65 */       AddressResolver<T> r = (AddressResolver)this.resolvers.get(executor);
/*  31: 66 */       if (r == null)
/*  32:    */       {
/*  33:    */         try
/*  34:    */         {
/*  35: 69 */           newResolver = newResolver(executor);
/*  36:    */         }
/*  37:    */         catch (Exception e)
/*  38:    */         {
/*  39:    */           AddressResolver<T> newResolver;
/*  40: 71 */           throw new IllegalStateException("failed to create a new resolver", e);
/*  41:    */         }
/*  42:    */         final AddressResolver<T> newResolver;
/*  43: 74 */         this.resolvers.put(executor, newResolver);
/*  44: 75 */         executor.terminationFuture().addListener(new FutureListener()
/*  45:    */         {
/*  46:    */           public void operationComplete(Future<Object> future)
/*  47:    */             throws Exception
/*  48:    */           {
/*  49: 78 */             synchronized (AddressResolverGroup.this.resolvers)
/*  50:    */             {
/*  51: 79 */               AddressResolverGroup.this.resolvers.remove(executor);
/*  52:    */             }
/*  53: 81 */             newResolver.close();
/*  54:    */           }
/*  55: 84 */         });
/*  56: 85 */         r = newResolver;
/*  57:    */       }
/*  58:    */     }
/*  59:    */     AddressResolver<T> r;
/*  60: 89 */     return r;
/*  61:    */   }
/*  62:    */   
/*  63:    */   protected abstract AddressResolver<T> newResolver(EventExecutor paramEventExecutor)
/*  64:    */     throws Exception;
/*  65:    */   
/*  66:    */   public void close()
/*  67:    */   {
/*  68:104 */     synchronized (this.resolvers)
/*  69:    */     {
/*  70:105 */       AddressResolver<T>[] rArray = (AddressResolver[])this.resolvers.values().toArray(new AddressResolver[this.resolvers.size()]);
/*  71:106 */       this.resolvers.clear();
/*  72:    */     }
/*  73:    */     AddressResolver<T>[] rArray;
/*  74:109 */     for (AddressResolver<T> r : rArray) {
/*  75:    */       try
/*  76:    */       {
/*  77:111 */         r.close();
/*  78:    */       }
/*  79:    */       catch (Throwable t)
/*  80:    */       {
/*  81:113 */         logger.warn("Failed to close a resolver:", t);
/*  82:    */       }
/*  83:    */     }
/*  84:    */   }
/*  85:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.resolver.AddressResolverGroup
 * JD-Core Version:    0.7.0.1
 */