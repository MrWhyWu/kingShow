/*   1:    */ package io.netty.resolver;
/*   2:    */ 
/*   3:    */ import io.netty.util.concurrent.EventExecutor;
/*   4:    */ import io.netty.util.concurrent.Future;
/*   5:    */ import io.netty.util.concurrent.FutureListener;
/*   6:    */ import io.netty.util.concurrent.Promise;
/*   7:    */ import io.netty.util.internal.PlatformDependent;
/*   8:    */ import java.net.InetAddress;
/*   9:    */ import java.net.UnknownHostException;
/*  10:    */ import java.util.ArrayList;
/*  11:    */ import java.util.Collections;
/*  12:    */ import java.util.List;
/*  13:    */ import java.util.Random;
/*  14:    */ 
/*  15:    */ public class RoundRobinInetAddressResolver
/*  16:    */   extends InetNameResolver
/*  17:    */ {
/*  18:    */   private final NameResolver<InetAddress> nameResolver;
/*  19:    */   
/*  20:    */   public RoundRobinInetAddressResolver(EventExecutor executor, NameResolver<InetAddress> nameResolver)
/*  21:    */   {
/*  22: 48 */     super(executor);
/*  23: 49 */     this.nameResolver = nameResolver;
/*  24:    */   }
/*  25:    */   
/*  26:    */   protected void doResolve(final String inetHost, final Promise<InetAddress> promise)
/*  27:    */     throws Exception
/*  28:    */   {
/*  29: 57 */     this.nameResolver.resolveAll(inetHost).addListener(new FutureListener()
/*  30:    */     {
/*  31:    */       public void operationComplete(Future<List<InetAddress>> future)
/*  32:    */         throws Exception
/*  33:    */       {
/*  34: 60 */         if (future.isSuccess())
/*  35:    */         {
/*  36: 61 */           List<InetAddress> inetAddresses = (List)future.getNow();
/*  37: 62 */           int numAddresses = inetAddresses.size();
/*  38: 63 */           if (numAddresses > 0) {
/*  39: 66 */             promise.setSuccess(inetAddresses.get(RoundRobinInetAddressResolver.randomIndex(numAddresses)));
/*  40:    */           } else {
/*  41: 68 */             promise.setFailure(new UnknownHostException(inetHost));
/*  42:    */           }
/*  43:    */         }
/*  44:    */         else
/*  45:    */         {
/*  46: 71 */           promise.setFailure(future.cause());
/*  47:    */         }
/*  48:    */       }
/*  49:    */     });
/*  50:    */   }
/*  51:    */   
/*  52:    */   protected void doResolveAll(String inetHost, final Promise<List<InetAddress>> promise)
/*  53:    */     throws Exception
/*  54:    */   {
/*  55: 79 */     this.nameResolver.resolveAll(inetHost).addListener(new FutureListener()
/*  56:    */     {
/*  57:    */       public void operationComplete(Future<List<InetAddress>> future)
/*  58:    */         throws Exception
/*  59:    */       {
/*  60: 82 */         if (future.isSuccess())
/*  61:    */         {
/*  62: 83 */           List<InetAddress> inetAddresses = (List)future.getNow();
/*  63: 84 */           if (!inetAddresses.isEmpty())
/*  64:    */           {
/*  65: 86 */             List<InetAddress> result = new ArrayList(inetAddresses);
/*  66:    */             
/*  67: 88 */             Collections.rotate(result, RoundRobinInetAddressResolver.randomIndex(inetAddresses.size()));
/*  68: 89 */             promise.setSuccess(result);
/*  69:    */           }
/*  70:    */           else
/*  71:    */           {
/*  72: 91 */             promise.setSuccess(inetAddresses);
/*  73:    */           }
/*  74:    */         }
/*  75:    */         else
/*  76:    */         {
/*  77: 94 */           promise.setFailure(future.cause());
/*  78:    */         }
/*  79:    */       }
/*  80:    */     });
/*  81:    */   }
/*  82:    */   
/*  83:    */   private static int randomIndex(int numAddresses)
/*  84:    */   {
/*  85:101 */     return numAddresses == 1 ? 0 : PlatformDependent.threadLocalRandom().nextInt(numAddresses);
/*  86:    */   }
/*  87:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.resolver.RoundRobinInetAddressResolver
 * JD-Core Version:    0.7.0.1
 */