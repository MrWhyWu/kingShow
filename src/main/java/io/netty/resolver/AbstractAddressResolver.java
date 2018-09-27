/*   1:    */ package io.netty.resolver;
/*   2:    */ 
/*   3:    */ import io.netty.util.concurrent.EventExecutor;
/*   4:    */ import io.netty.util.concurrent.Future;
/*   5:    */ import io.netty.util.concurrent.Promise;
/*   6:    */ import io.netty.util.internal.ObjectUtil;
/*   7:    */ import io.netty.util.internal.TypeParameterMatcher;
/*   8:    */ import java.net.SocketAddress;
/*   9:    */ import java.nio.channels.UnsupportedAddressTypeException;
/*  10:    */ import java.util.Collections;
/*  11:    */ import java.util.List;
/*  12:    */ 
/*  13:    */ public abstract class AbstractAddressResolver<T extends SocketAddress>
/*  14:    */   implements AddressResolver<T>
/*  15:    */ {
/*  16:    */   private final EventExecutor executor;
/*  17:    */   private final TypeParameterMatcher matcher;
/*  18:    */   
/*  19:    */   protected AbstractAddressResolver(EventExecutor executor)
/*  20:    */   {
/*  21: 46 */     this.executor = ((EventExecutor)ObjectUtil.checkNotNull(executor, "executor"));
/*  22: 47 */     this.matcher = TypeParameterMatcher.find(this, AbstractAddressResolver.class, "T");
/*  23:    */   }
/*  24:    */   
/*  25:    */   protected AbstractAddressResolver(EventExecutor executor, Class<? extends T> addressType)
/*  26:    */   {
/*  27: 56 */     this.executor = ((EventExecutor)ObjectUtil.checkNotNull(executor, "executor"));
/*  28: 57 */     this.matcher = TypeParameterMatcher.get(addressType);
/*  29:    */   }
/*  30:    */   
/*  31:    */   protected EventExecutor executor()
/*  32:    */   {
/*  33: 65 */     return this.executor;
/*  34:    */   }
/*  35:    */   
/*  36:    */   public boolean isSupported(SocketAddress address)
/*  37:    */   {
/*  38: 70 */     return this.matcher.match(address);
/*  39:    */   }
/*  40:    */   
/*  41:    */   public final boolean isResolved(SocketAddress address)
/*  42:    */   {
/*  43: 75 */     if (!isSupported(address)) {
/*  44: 76 */       throw new UnsupportedAddressTypeException();
/*  45:    */     }
/*  46: 80 */     T castAddress = address;
/*  47: 81 */     return doIsResolved(castAddress);
/*  48:    */   }
/*  49:    */   
/*  50:    */   protected abstract boolean doIsResolved(T paramT);
/*  51:    */   
/*  52:    */   public final Future<T> resolve(SocketAddress address)
/*  53:    */   {
/*  54: 92 */     if (!isSupported((SocketAddress)ObjectUtil.checkNotNull(address, "address"))) {
/*  55: 94 */       return executor().newFailedFuture(new UnsupportedAddressTypeException());
/*  56:    */     }
/*  57: 97 */     if (isResolved(address))
/*  58:    */     {
/*  59:100 */       T cast = address;
/*  60:101 */       return this.executor.newSucceededFuture(cast);
/*  61:    */     }
/*  62:    */     try
/*  63:    */     {
/*  64:106 */       T cast = address;
/*  65:107 */       Promise<T> promise = executor().newPromise();
/*  66:108 */       doResolve(cast, promise);
/*  67:109 */       return promise;
/*  68:    */     }
/*  69:    */     catch (Exception e)
/*  70:    */     {
/*  71:111 */       return executor().newFailedFuture(e);
/*  72:    */     }
/*  73:    */   }
/*  74:    */   
/*  75:    */   public final Future<T> resolve(SocketAddress address, Promise<T> promise)
/*  76:    */   {
/*  77:117 */     ObjectUtil.checkNotNull(address, "address");
/*  78:118 */     ObjectUtil.checkNotNull(promise, "promise");
/*  79:120 */     if (!isSupported(address)) {
/*  80:122 */       return promise.setFailure(new UnsupportedAddressTypeException());
/*  81:    */     }
/*  82:125 */     if (isResolved(address))
/*  83:    */     {
/*  84:128 */       T cast = address;
/*  85:129 */       return promise.setSuccess(cast);
/*  86:    */     }
/*  87:    */     try
/*  88:    */     {
/*  89:134 */       T cast = address;
/*  90:135 */       doResolve(cast, promise);
/*  91:136 */       return promise;
/*  92:    */     }
/*  93:    */     catch (Exception e)
/*  94:    */     {
/*  95:138 */       return promise.setFailure(e);
/*  96:    */     }
/*  97:    */   }
/*  98:    */   
/*  99:    */   public final Future<List<T>> resolveAll(SocketAddress address)
/* 100:    */   {
/* 101:144 */     if (!isSupported((SocketAddress)ObjectUtil.checkNotNull(address, "address"))) {
/* 102:146 */       return executor().newFailedFuture(new UnsupportedAddressTypeException());
/* 103:    */     }
/* 104:149 */     if (isResolved(address))
/* 105:    */     {
/* 106:152 */       T cast = address;
/* 107:153 */       return this.executor.newSucceededFuture(Collections.singletonList(cast));
/* 108:    */     }
/* 109:    */     try
/* 110:    */     {
/* 111:158 */       T cast = address;
/* 112:159 */       Promise<List<T>> promise = executor().newPromise();
/* 113:160 */       doResolveAll(cast, promise);
/* 114:161 */       return promise;
/* 115:    */     }
/* 116:    */     catch (Exception e)
/* 117:    */     {
/* 118:163 */       return executor().newFailedFuture(e);
/* 119:    */     }
/* 120:    */   }
/* 121:    */   
/* 122:    */   public final Future<List<T>> resolveAll(SocketAddress address, Promise<List<T>> promise)
/* 123:    */   {
/* 124:169 */     ObjectUtil.checkNotNull(address, "address");
/* 125:170 */     ObjectUtil.checkNotNull(promise, "promise");
/* 126:172 */     if (!isSupported(address)) {
/* 127:174 */       return promise.setFailure(new UnsupportedAddressTypeException());
/* 128:    */     }
/* 129:177 */     if (isResolved(address))
/* 130:    */     {
/* 131:180 */       T cast = address;
/* 132:181 */       return promise.setSuccess(Collections.singletonList(cast));
/* 133:    */     }
/* 134:    */     try
/* 135:    */     {
/* 136:186 */       T cast = address;
/* 137:187 */       doResolveAll(cast, promise);
/* 138:188 */       return promise;
/* 139:    */     }
/* 140:    */     catch (Exception e)
/* 141:    */     {
/* 142:190 */       return promise.setFailure(e);
/* 143:    */     }
/* 144:    */   }
/* 145:    */   
/* 146:    */   protected abstract void doResolve(T paramT, Promise<T> paramPromise)
/* 147:    */     throws Exception;
/* 148:    */   
/* 149:    */   protected abstract void doResolveAll(T paramT, Promise<List<T>> paramPromise)
/* 150:    */     throws Exception;
/* 151:    */   
/* 152:    */   public void close() {}
/* 153:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.resolver.AbstractAddressResolver
 * JD-Core Version:    0.7.0.1
 */