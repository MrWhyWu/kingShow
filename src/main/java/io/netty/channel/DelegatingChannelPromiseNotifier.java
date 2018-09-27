/*   1:    */ package io.netty.channel;
/*   2:    */ 
/*   3:    */ import io.netty.util.concurrent.Future;
/*   4:    */ import io.netty.util.concurrent.GenericFutureListener;
/*   5:    */ import io.netty.util.internal.ObjectUtil;
/*   6:    */ import io.netty.util.internal.PromiseNotificationUtil;
/*   7:    */ import io.netty.util.internal.logging.InternalLogger;
/*   8:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   9:    */ import java.util.concurrent.ExecutionException;
/*  10:    */ import java.util.concurrent.TimeUnit;
/*  11:    */ import java.util.concurrent.TimeoutException;
/*  12:    */ 
/*  13:    */ public final class DelegatingChannelPromiseNotifier
/*  14:    */   implements ChannelPromise, ChannelFutureListener
/*  15:    */ {
/*  16: 34 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(DelegatingChannelPromiseNotifier.class);
/*  17:    */   private final ChannelPromise delegate;
/*  18:    */   private final boolean logNotifyFailure;
/*  19:    */   
/*  20:    */   public DelegatingChannelPromiseNotifier(ChannelPromise delegate)
/*  21:    */   {
/*  22: 39 */     this(delegate, !(delegate instanceof VoidChannelPromise));
/*  23:    */   }
/*  24:    */   
/*  25:    */   public DelegatingChannelPromiseNotifier(ChannelPromise delegate, boolean logNotifyFailure)
/*  26:    */   {
/*  27: 43 */     this.delegate = ((ChannelPromise)ObjectUtil.checkNotNull(delegate, "delegate"));
/*  28: 44 */     this.logNotifyFailure = logNotifyFailure;
/*  29:    */   }
/*  30:    */   
/*  31:    */   public void operationComplete(ChannelFuture future)
/*  32:    */     throws Exception
/*  33:    */   {
/*  34: 49 */     InternalLogger internalLogger = this.logNotifyFailure ? logger : null;
/*  35: 50 */     if (future.isSuccess())
/*  36:    */     {
/*  37: 51 */       Void result = (Void)future.get();
/*  38: 52 */       PromiseNotificationUtil.trySuccess(this.delegate, result, internalLogger);
/*  39:    */     }
/*  40: 53 */     else if (future.isCancelled())
/*  41:    */     {
/*  42: 54 */       PromiseNotificationUtil.tryCancel(this.delegate, internalLogger);
/*  43:    */     }
/*  44:    */     else
/*  45:    */     {
/*  46: 56 */       Throwable cause = future.cause();
/*  47: 57 */       PromiseNotificationUtil.tryFailure(this.delegate, cause, internalLogger);
/*  48:    */     }
/*  49:    */   }
/*  50:    */   
/*  51:    */   public Channel channel()
/*  52:    */   {
/*  53: 63 */     return this.delegate.channel();
/*  54:    */   }
/*  55:    */   
/*  56:    */   public ChannelPromise setSuccess(Void result)
/*  57:    */   {
/*  58: 68 */     this.delegate.setSuccess(result);
/*  59: 69 */     return this;
/*  60:    */   }
/*  61:    */   
/*  62:    */   public ChannelPromise setSuccess()
/*  63:    */   {
/*  64: 74 */     this.delegate.setSuccess();
/*  65: 75 */     return this;
/*  66:    */   }
/*  67:    */   
/*  68:    */   public boolean trySuccess()
/*  69:    */   {
/*  70: 80 */     return this.delegate.trySuccess();
/*  71:    */   }
/*  72:    */   
/*  73:    */   public boolean trySuccess(Void result)
/*  74:    */   {
/*  75: 85 */     return this.delegate.trySuccess(result);
/*  76:    */   }
/*  77:    */   
/*  78:    */   public ChannelPromise setFailure(Throwable cause)
/*  79:    */   {
/*  80: 90 */     this.delegate.setFailure(cause);
/*  81: 91 */     return this;
/*  82:    */   }
/*  83:    */   
/*  84:    */   public ChannelPromise addListener(GenericFutureListener<? extends Future<? super Void>> listener)
/*  85:    */   {
/*  86: 96 */     this.delegate.addListener(listener);
/*  87: 97 */     return this;
/*  88:    */   }
/*  89:    */   
/*  90:    */   public ChannelPromise addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners)
/*  91:    */   {
/*  92:102 */     this.delegate.addListeners(listeners);
/*  93:103 */     return this;
/*  94:    */   }
/*  95:    */   
/*  96:    */   public ChannelPromise removeListener(GenericFutureListener<? extends Future<? super Void>> listener)
/*  97:    */   {
/*  98:108 */     this.delegate.removeListener(listener);
/*  99:109 */     return this;
/* 100:    */   }
/* 101:    */   
/* 102:    */   public ChannelPromise removeListeners(GenericFutureListener<? extends Future<? super Void>>... listeners)
/* 103:    */   {
/* 104:114 */     this.delegate.removeListeners(listeners);
/* 105:115 */     return this;
/* 106:    */   }
/* 107:    */   
/* 108:    */   public boolean tryFailure(Throwable cause)
/* 109:    */   {
/* 110:120 */     return this.delegate.tryFailure(cause);
/* 111:    */   }
/* 112:    */   
/* 113:    */   public boolean setUncancellable()
/* 114:    */   {
/* 115:125 */     return this.delegate.setUncancellable();
/* 116:    */   }
/* 117:    */   
/* 118:    */   public ChannelPromise await()
/* 119:    */     throws InterruptedException
/* 120:    */   {
/* 121:130 */     this.delegate.await();
/* 122:131 */     return this;
/* 123:    */   }
/* 124:    */   
/* 125:    */   public ChannelPromise awaitUninterruptibly()
/* 126:    */   {
/* 127:136 */     this.delegate.awaitUninterruptibly();
/* 128:137 */     return this;
/* 129:    */   }
/* 130:    */   
/* 131:    */   public boolean isVoid()
/* 132:    */   {
/* 133:142 */     return this.delegate.isVoid();
/* 134:    */   }
/* 135:    */   
/* 136:    */   public ChannelPromise unvoid()
/* 137:    */   {
/* 138:147 */     return isVoid() ? new DelegatingChannelPromiseNotifier(this.delegate.unvoid()) : this;
/* 139:    */   }
/* 140:    */   
/* 141:    */   public boolean await(long timeout, TimeUnit unit)
/* 142:    */     throws InterruptedException
/* 143:    */   {
/* 144:152 */     return this.delegate.await(timeout, unit);
/* 145:    */   }
/* 146:    */   
/* 147:    */   public boolean await(long timeoutMillis)
/* 148:    */     throws InterruptedException
/* 149:    */   {
/* 150:157 */     return this.delegate.await(timeoutMillis);
/* 151:    */   }
/* 152:    */   
/* 153:    */   public boolean awaitUninterruptibly(long timeout, TimeUnit unit)
/* 154:    */   {
/* 155:162 */     return this.delegate.awaitUninterruptibly(timeout, unit);
/* 156:    */   }
/* 157:    */   
/* 158:    */   public boolean awaitUninterruptibly(long timeoutMillis)
/* 159:    */   {
/* 160:167 */     return this.delegate.awaitUninterruptibly(timeoutMillis);
/* 161:    */   }
/* 162:    */   
/* 163:    */   public Void getNow()
/* 164:    */   {
/* 165:172 */     return (Void)this.delegate.getNow();
/* 166:    */   }
/* 167:    */   
/* 168:    */   public boolean cancel(boolean mayInterruptIfRunning)
/* 169:    */   {
/* 170:177 */     return this.delegate.cancel(mayInterruptIfRunning);
/* 171:    */   }
/* 172:    */   
/* 173:    */   public boolean isCancelled()
/* 174:    */   {
/* 175:182 */     return this.delegate.isCancelled();
/* 176:    */   }
/* 177:    */   
/* 178:    */   public boolean isDone()
/* 179:    */   {
/* 180:187 */     return this.delegate.isDone();
/* 181:    */   }
/* 182:    */   
/* 183:    */   public Void get()
/* 184:    */     throws InterruptedException, ExecutionException
/* 185:    */   {
/* 186:192 */     return (Void)this.delegate.get();
/* 187:    */   }
/* 188:    */   
/* 189:    */   public Void get(long timeout, TimeUnit unit)
/* 190:    */     throws InterruptedException, ExecutionException, TimeoutException
/* 191:    */   {
/* 192:197 */     return (Void)this.delegate.get(timeout, unit);
/* 193:    */   }
/* 194:    */   
/* 195:    */   public ChannelPromise sync()
/* 196:    */     throws InterruptedException
/* 197:    */   {
/* 198:202 */     this.delegate.sync();
/* 199:203 */     return this;
/* 200:    */   }
/* 201:    */   
/* 202:    */   public ChannelPromise syncUninterruptibly()
/* 203:    */   {
/* 204:208 */     this.delegate.syncUninterruptibly();
/* 205:209 */     return this;
/* 206:    */   }
/* 207:    */   
/* 208:    */   public boolean isSuccess()
/* 209:    */   {
/* 210:214 */     return this.delegate.isSuccess();
/* 211:    */   }
/* 212:    */   
/* 213:    */   public boolean isCancellable()
/* 214:    */   {
/* 215:219 */     return this.delegate.isCancellable();
/* 216:    */   }
/* 217:    */   
/* 218:    */   public Throwable cause()
/* 219:    */   {
/* 220:224 */     return this.delegate.cause();
/* 221:    */   }
/* 222:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.DelegatingChannelPromiseNotifier
 * JD-Core Version:    0.7.0.1
 */