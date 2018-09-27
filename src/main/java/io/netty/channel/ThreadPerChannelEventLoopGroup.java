/*   1:    */ package io.netty.channel;
/*   2:    */ 
/*   3:    */ import io.netty.util.concurrent.AbstractEventExecutorGroup;
/*   4:    */ import io.netty.util.concurrent.DefaultPromise;
/*   5:    */ import io.netty.util.concurrent.EventExecutor;
/*   6:    */ import io.netty.util.concurrent.Future;
/*   7:    */ import io.netty.util.concurrent.FutureListener;
/*   8:    */ import io.netty.util.concurrent.GlobalEventExecutor;
/*   9:    */ import io.netty.util.concurrent.Promise;
/*  10:    */ import io.netty.util.concurrent.ThreadPerTaskExecutor;
/*  11:    */ import io.netty.util.internal.EmptyArrays;
/*  12:    */ import io.netty.util.internal.PlatformDependent;
/*  13:    */ import io.netty.util.internal.ReadOnlyIterator;
/*  14:    */ import io.netty.util.internal.ThrowableUtil;
/*  15:    */ import java.util.Collections;
/*  16:    */ import java.util.Iterator;
/*  17:    */ import java.util.Queue;
/*  18:    */ import java.util.Set;
/*  19:    */ import java.util.concurrent.ConcurrentLinkedQueue;
/*  20:    */ import java.util.concurrent.Executor;
/*  21:    */ import java.util.concurrent.Executors;
/*  22:    */ import java.util.concurrent.RejectedExecutionException;
/*  23:    */ import java.util.concurrent.ThreadFactory;
/*  24:    */ import java.util.concurrent.TimeUnit;
/*  25:    */ 
/*  26:    */ public class ThreadPerChannelEventLoopGroup
/*  27:    */   extends AbstractEventExecutorGroup
/*  28:    */   implements EventLoopGroup
/*  29:    */ {
/*  30:    */   private final Object[] childArgs;
/*  31:    */   private final int maxChannels;
/*  32:    */   final Executor executor;
/*  33: 52 */   final Set<EventLoop> activeChildren = Collections.newSetFromMap(PlatformDependent.newConcurrentHashMap());
/*  34: 53 */   final Queue<EventLoop> idleChildren = new ConcurrentLinkedQueue();
/*  35:    */   private final ChannelException tooManyChannels;
/*  36:    */   private volatile boolean shuttingDown;
/*  37: 57 */   private final Promise<?> terminationFuture = new DefaultPromise(GlobalEventExecutor.INSTANCE);
/*  38: 58 */   private final FutureListener<Object> childTerminationListener = new FutureListener()
/*  39:    */   {
/*  40:    */     public void operationComplete(Future<Object> future)
/*  41:    */       throws Exception
/*  42:    */     {
/*  43: 62 */       if (ThreadPerChannelEventLoopGroup.this.isTerminated()) {
/*  44: 63 */         ThreadPerChannelEventLoopGroup.this.terminationFuture.trySuccess(null);
/*  45:    */       }
/*  46:    */     }
/*  47:    */   };
/*  48:    */   
/*  49:    */   protected ThreadPerChannelEventLoopGroup()
/*  50:    */   {
/*  51: 72 */     this(0);
/*  52:    */   }
/*  53:    */   
/*  54:    */   protected ThreadPerChannelEventLoopGroup(int maxChannels)
/*  55:    */   {
/*  56: 85 */     this(maxChannels, Executors.defaultThreadFactory(), new Object[0]);
/*  57:    */   }
/*  58:    */   
/*  59:    */   protected ThreadPerChannelEventLoopGroup(int maxChannels, ThreadFactory threadFactory, Object... args)
/*  60:    */   {
/*  61:101 */     this(maxChannels, new ThreadPerTaskExecutor(threadFactory), args);
/*  62:    */   }
/*  63:    */   
/*  64:    */   protected ThreadPerChannelEventLoopGroup(int maxChannels, Executor executor, Object... args)
/*  65:    */   {
/*  66:117 */     if (maxChannels < 0) {
/*  67:118 */       throw new IllegalArgumentException(String.format("maxChannels: %d (expected: >= 0)", new Object[] {
/*  68:119 */         Integer.valueOf(maxChannels) }));
/*  69:    */     }
/*  70:121 */     if (executor == null) {
/*  71:122 */       throw new NullPointerException("executor");
/*  72:    */     }
/*  73:125 */     if (args == null) {
/*  74:126 */       this.childArgs = EmptyArrays.EMPTY_OBJECTS;
/*  75:    */     } else {
/*  76:128 */       this.childArgs = ((Object[])args.clone());
/*  77:    */     }
/*  78:131 */     this.maxChannels = maxChannels;
/*  79:132 */     this.executor = executor;
/*  80:    */     
/*  81:134 */     this.tooManyChannels = ((ChannelException)ThrowableUtil.unknownStackTrace(new ChannelException("too many channels (max: " + maxChannels + ')'), ThreadPerChannelEventLoopGroup.class, "nextChild()"));
/*  82:    */   }
/*  83:    */   
/*  84:    */   protected EventLoop newChild(Object... args)
/*  85:    */     throws Exception
/*  86:    */   {
/*  87:143 */     return new ThreadPerChannelEventLoop(this);
/*  88:    */   }
/*  89:    */   
/*  90:    */   public Iterator<EventExecutor> iterator()
/*  91:    */   {
/*  92:148 */     return new ReadOnlyIterator(this.activeChildren.iterator());
/*  93:    */   }
/*  94:    */   
/*  95:    */   public EventLoop next()
/*  96:    */   {
/*  97:153 */     throw new UnsupportedOperationException();
/*  98:    */   }
/*  99:    */   
/* 100:    */   public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit)
/* 101:    */   {
/* 102:158 */     this.shuttingDown = true;
/* 103:160 */     for (EventLoop l : this.activeChildren) {
/* 104:161 */       l.shutdownGracefully(quietPeriod, timeout, unit);
/* 105:    */     }
/* 106:163 */     for (EventLoop l : this.idleChildren) {
/* 107:164 */       l.shutdownGracefully(quietPeriod, timeout, unit);
/* 108:    */     }
/* 109:168 */     if (isTerminated()) {
/* 110:169 */       this.terminationFuture.trySuccess(null);
/* 111:    */     }
/* 112:172 */     return terminationFuture();
/* 113:    */   }
/* 114:    */   
/* 115:    */   public Future<?> terminationFuture()
/* 116:    */   {
/* 117:177 */     return this.terminationFuture;
/* 118:    */   }
/* 119:    */   
/* 120:    */   @Deprecated
/* 121:    */   public void shutdown()
/* 122:    */   {
/* 123:183 */     this.shuttingDown = true;
/* 124:185 */     for (EventLoop l : this.activeChildren) {
/* 125:186 */       l.shutdown();
/* 126:    */     }
/* 127:188 */     for (EventLoop l : this.idleChildren) {
/* 128:189 */       l.shutdown();
/* 129:    */     }
/* 130:193 */     if (isTerminated()) {
/* 131:194 */       this.terminationFuture.trySuccess(null);
/* 132:    */     }
/* 133:    */   }
/* 134:    */   
/* 135:    */   public boolean isShuttingDown()
/* 136:    */   {
/* 137:200 */     for (EventLoop l : this.activeChildren) {
/* 138:201 */       if (!l.isShuttingDown()) {
/* 139:202 */         return false;
/* 140:    */       }
/* 141:    */     }
/* 142:205 */     for (EventLoop l : this.idleChildren) {
/* 143:206 */       if (!l.isShuttingDown()) {
/* 144:207 */         return false;
/* 145:    */       }
/* 146:    */     }
/* 147:210 */     return true;
/* 148:    */   }
/* 149:    */   
/* 150:    */   public boolean isShutdown()
/* 151:    */   {
/* 152:215 */     for (EventLoop l : this.activeChildren) {
/* 153:216 */       if (!l.isShutdown()) {
/* 154:217 */         return false;
/* 155:    */       }
/* 156:    */     }
/* 157:220 */     for (EventLoop l : this.idleChildren) {
/* 158:221 */       if (!l.isShutdown()) {
/* 159:222 */         return false;
/* 160:    */       }
/* 161:    */     }
/* 162:225 */     return true;
/* 163:    */   }
/* 164:    */   
/* 165:    */   public boolean isTerminated()
/* 166:    */   {
/* 167:230 */     for (EventLoop l : this.activeChildren) {
/* 168:231 */       if (!l.isTerminated()) {
/* 169:232 */         return false;
/* 170:    */       }
/* 171:    */     }
/* 172:235 */     for (EventLoop l : this.idleChildren) {
/* 173:236 */       if (!l.isTerminated()) {
/* 174:237 */         return false;
/* 175:    */       }
/* 176:    */     }
/* 177:240 */     return true;
/* 178:    */   }
/* 179:    */   
/* 180:    */   public boolean awaitTermination(long timeout, TimeUnit unit)
/* 181:    */     throws InterruptedException
/* 182:    */   {
/* 183:246 */     long deadline = System.nanoTime() + unit.toNanos(timeout);
/* 184:247 */     for (EventLoop l : this.activeChildren) {
/* 185:    */       for (;;)
/* 186:    */       {
/* 187:249 */         long timeLeft = deadline - System.nanoTime();
/* 188:250 */         if (timeLeft <= 0L) {
/* 189:251 */           return isTerminated();
/* 190:    */         }
/* 191:253 */         if (l.awaitTermination(timeLeft, TimeUnit.NANOSECONDS)) {
/* 192:    */           break;
/* 193:    */         }
/* 194:    */       }
/* 195:    */     }
/* 196:258 */     for (EventLoop l : this.idleChildren) {
/* 197:    */       for (;;)
/* 198:    */       {
/* 199:260 */         long timeLeft = deadline - System.nanoTime();
/* 200:261 */         if (timeLeft <= 0L) {
/* 201:262 */           return isTerminated();
/* 202:    */         }
/* 203:264 */         if (l.awaitTermination(timeLeft, TimeUnit.NANOSECONDS)) {
/* 204:    */           break;
/* 205:    */         }
/* 206:    */       }
/* 207:    */     }
/* 208:269 */     return isTerminated();
/* 209:    */   }
/* 210:    */   
/* 211:    */   public ChannelFuture register(Channel channel)
/* 212:    */   {
/* 213:274 */     if (channel == null) {
/* 214:275 */       throw new NullPointerException("channel");
/* 215:    */     }
/* 216:    */     try
/* 217:    */     {
/* 218:278 */       EventLoop l = nextChild();
/* 219:279 */       return l.register(new DefaultChannelPromise(channel, l));
/* 220:    */     }
/* 221:    */     catch (Throwable t)
/* 222:    */     {
/* 223:281 */       return new FailedChannelFuture(channel, GlobalEventExecutor.INSTANCE, t);
/* 224:    */     }
/* 225:    */   }
/* 226:    */   
/* 227:    */   public ChannelFuture register(ChannelPromise promise)
/* 228:    */   {
/* 229:    */     try
/* 230:    */     {
/* 231:288 */       return nextChild().register(promise);
/* 232:    */     }
/* 233:    */     catch (Throwable t)
/* 234:    */     {
/* 235:290 */       promise.setFailure(t);
/* 236:    */     }
/* 237:291 */     return promise;
/* 238:    */   }
/* 239:    */   
/* 240:    */   @Deprecated
/* 241:    */   public ChannelFuture register(Channel channel, ChannelPromise promise)
/* 242:    */   {
/* 243:298 */     if (channel == null) {
/* 244:299 */       throw new NullPointerException("channel");
/* 245:    */     }
/* 246:    */     try
/* 247:    */     {
/* 248:302 */       return nextChild().register(channel, promise);
/* 249:    */     }
/* 250:    */     catch (Throwable t)
/* 251:    */     {
/* 252:304 */       promise.setFailure(t);
/* 253:    */     }
/* 254:305 */     return promise;
/* 255:    */   }
/* 256:    */   
/* 257:    */   private EventLoop nextChild()
/* 258:    */     throws Exception
/* 259:    */   {
/* 260:310 */     if (this.shuttingDown) {
/* 261:311 */       throw new RejectedExecutionException("shutting down");
/* 262:    */     }
/* 263:314 */     EventLoop loop = (EventLoop)this.idleChildren.poll();
/* 264:315 */     if (loop == null)
/* 265:    */     {
/* 266:316 */       if ((this.maxChannels > 0) && (this.activeChildren.size() >= this.maxChannels)) {
/* 267:317 */         throw this.tooManyChannels;
/* 268:    */       }
/* 269:319 */       loop = newChild(this.childArgs);
/* 270:320 */       loop.terminationFuture().addListener(this.childTerminationListener);
/* 271:    */     }
/* 272:322 */     this.activeChildren.add(loop);
/* 273:323 */     return loop;
/* 274:    */   }
/* 275:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.ThreadPerChannelEventLoopGroup
 * JD-Core Version:    0.7.0.1
 */