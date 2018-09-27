/*   1:    */ package io.netty.channel.pool;
/*   2:    */ 
/*   3:    */ import io.netty.bootstrap.Bootstrap;
/*   4:    */ import io.netty.bootstrap.BootstrapConfig;
/*   5:    */ import io.netty.channel.Channel;
/*   6:    */ import io.netty.channel.ChannelFuture;
/*   7:    */ import io.netty.channel.ChannelFutureListener;
/*   8:    */ import io.netty.channel.ChannelInitializer;
/*   9:    */ import io.netty.channel.EventLoop;
/*  10:    */ import io.netty.channel.EventLoopGroup;
/*  11:    */ import io.netty.util.Attribute;
/*  12:    */ import io.netty.util.AttributeKey;
/*  13:    */ import io.netty.util.concurrent.Future;
/*  14:    */ import io.netty.util.concurrent.FutureListener;
/*  15:    */ import io.netty.util.concurrent.Promise;
/*  16:    */ import io.netty.util.internal.ObjectUtil;
/*  17:    */ import io.netty.util.internal.PlatformDependent;
/*  18:    */ import io.netty.util.internal.ThrowableUtil;
/*  19:    */ import java.util.Deque;
/*  20:    */ 
/*  21:    */ public class SimpleChannelPool
/*  22:    */   implements ChannelPool
/*  23:    */ {
/*  24: 43 */   private static final AttributeKey<SimpleChannelPool> POOL_KEY = AttributeKey.newInstance("channelPool");
/*  25: 44 */   private static final IllegalStateException FULL_EXCEPTION = (IllegalStateException)ThrowableUtil.unknownStackTrace(new IllegalStateException("ChannelPool full"), SimpleChannelPool.class, "releaseAndOffer(...)");
/*  26: 47 */   private final Deque<Channel> deque = PlatformDependent.newConcurrentDeque();
/*  27:    */   private final ChannelPoolHandler handler;
/*  28:    */   private final ChannelHealthChecker healthCheck;
/*  29:    */   private final Bootstrap bootstrap;
/*  30:    */   private final boolean releaseHealthCheck;
/*  31:    */   private final boolean lastRecentUsed;
/*  32:    */   
/*  33:    */   public SimpleChannelPool(Bootstrap bootstrap, ChannelPoolHandler handler)
/*  34:    */   {
/*  35: 61 */     this(bootstrap, handler, ChannelHealthChecker.ACTIVE);
/*  36:    */   }
/*  37:    */   
/*  38:    */   public SimpleChannelPool(Bootstrap bootstrap, ChannelPoolHandler handler, ChannelHealthChecker healthCheck)
/*  39:    */   {
/*  40: 73 */     this(bootstrap, handler, healthCheck, true);
/*  41:    */   }
/*  42:    */   
/*  43:    */   public SimpleChannelPool(Bootstrap bootstrap, ChannelPoolHandler handler, ChannelHealthChecker healthCheck, boolean releaseHealthCheck)
/*  44:    */   {
/*  45: 88 */     this(bootstrap, handler, healthCheck, releaseHealthCheck, true);
/*  46:    */   }
/*  47:    */   
/*  48:    */   public SimpleChannelPool(Bootstrap bootstrap, final ChannelPoolHandler handler, ChannelHealthChecker healthCheck, boolean releaseHealthCheck, boolean lastRecentUsed)
/*  49:    */   {
/*  50:104 */     this.handler = ((ChannelPoolHandler)ObjectUtil.checkNotNull(handler, "handler"));
/*  51:105 */     this.healthCheck = ((ChannelHealthChecker)ObjectUtil.checkNotNull(healthCheck, "healthCheck"));
/*  52:106 */     this.releaseHealthCheck = releaseHealthCheck;
/*  53:    */     
/*  54:108 */     this.bootstrap = ((Bootstrap)ObjectUtil.checkNotNull(bootstrap, "bootstrap")).clone();
/*  55:109 */     this.bootstrap.handler(new ChannelInitializer()
/*  56:    */     {
/*  57:    */       protected void initChannel(Channel ch)
/*  58:    */         throws Exception
/*  59:    */       {
/*  60:112 */         assert (ch.eventLoop().inEventLoop());
/*  61:113 */         handler.channelCreated(ch);
/*  62:    */       }
/*  63:115 */     });
/*  64:116 */     this.lastRecentUsed = lastRecentUsed;
/*  65:    */   }
/*  66:    */   
/*  67:    */   protected Bootstrap bootstrap()
/*  68:    */   {
/*  69:125 */     return this.bootstrap;
/*  70:    */   }
/*  71:    */   
/*  72:    */   protected ChannelPoolHandler handler()
/*  73:    */   {
/*  74:134 */     return this.handler;
/*  75:    */   }
/*  76:    */   
/*  77:    */   protected ChannelHealthChecker healthChecker()
/*  78:    */   {
/*  79:143 */     return this.healthCheck;
/*  80:    */   }
/*  81:    */   
/*  82:    */   protected boolean releaseHealthCheck()
/*  83:    */   {
/*  84:153 */     return this.releaseHealthCheck;
/*  85:    */   }
/*  86:    */   
/*  87:    */   public final Future<Channel> acquire()
/*  88:    */   {
/*  89:158 */     return acquire(this.bootstrap.config().group().next().newPromise());
/*  90:    */   }
/*  91:    */   
/*  92:    */   public Future<Channel> acquire(Promise<Channel> promise)
/*  93:    */   {
/*  94:163 */     ObjectUtil.checkNotNull(promise, "promise");
/*  95:164 */     return acquireHealthyFromPoolOrNew(promise);
/*  96:    */   }
/*  97:    */   
/*  98:    */   private Future<Channel> acquireHealthyFromPoolOrNew(final Promise<Channel> promise)
/*  99:    */   {
/* 100:    */     try
/* 101:    */     {
/* 102:174 */       final Channel ch = pollChannel();
/* 103:175 */       if (ch == null)
/* 104:    */       {
/* 105:177 */         Bootstrap bs = this.bootstrap.clone();
/* 106:178 */         bs.attr(POOL_KEY, this);
/* 107:179 */         ChannelFuture f = connectChannel(bs);
/* 108:180 */         if (f.isDone()) {
/* 109:181 */           notifyConnect(f, promise);
/* 110:    */         } else {
/* 111:183 */           f.addListener(new ChannelFutureListener()
/* 112:    */           {
/* 113:    */             public void operationComplete(ChannelFuture future)
/* 114:    */               throws Exception
/* 115:    */             {
/* 116:186 */               SimpleChannelPool.this.notifyConnect(future, promise);
/* 117:    */             }
/* 118:    */           });
/* 119:    */         }
/* 120:190 */         return promise;
/* 121:    */       }
/* 122:192 */       EventLoop loop = ch.eventLoop();
/* 123:193 */       if (loop.inEventLoop()) {
/* 124:194 */         doHealthCheck(ch, promise);
/* 125:    */       } else {
/* 126:196 */         loop.execute(new Runnable()
/* 127:    */         {
/* 128:    */           public void run()
/* 129:    */           {
/* 130:199 */             SimpleChannelPool.this.doHealthCheck(ch, promise);
/* 131:    */           }
/* 132:    */         });
/* 133:    */       }
/* 134:    */     }
/* 135:    */     catch (Throwable cause)
/* 136:    */     {
/* 137:204 */       promise.tryFailure(cause);
/* 138:    */     }
/* 139:206 */     return promise;
/* 140:    */   }
/* 141:    */   
/* 142:    */   private void notifyConnect(ChannelFuture future, Promise<Channel> promise)
/* 143:    */   {
/* 144:210 */     if (future.isSuccess())
/* 145:    */     {
/* 146:211 */       Channel channel = future.channel();
/* 147:212 */       if (!promise.trySuccess(channel)) {
/* 148:214 */         release(channel);
/* 149:    */       }
/* 150:    */     }
/* 151:    */     else
/* 152:    */     {
/* 153:217 */       promise.tryFailure(future.cause());
/* 154:    */     }
/* 155:    */   }
/* 156:    */   
/* 157:    */   private void doHealthCheck(final Channel ch, final Promise<Channel> promise)
/* 158:    */   {
/* 159:222 */     assert (ch.eventLoop().inEventLoop());
/* 160:    */     
/* 161:224 */     Future<Boolean> f = this.healthCheck.isHealthy(ch);
/* 162:225 */     if (f.isDone()) {
/* 163:226 */       notifyHealthCheck(f, ch, promise);
/* 164:    */     } else {
/* 165:228 */       f.addListener(new FutureListener()
/* 166:    */       {
/* 167:    */         public void operationComplete(Future<Boolean> future)
/* 168:    */           throws Exception
/* 169:    */         {
/* 170:231 */           SimpleChannelPool.this.notifyHealthCheck(future, ch, promise);
/* 171:    */         }
/* 172:    */       });
/* 173:    */     }
/* 174:    */   }
/* 175:    */   
/* 176:    */   private void notifyHealthCheck(Future<Boolean> future, Channel ch, Promise<Channel> promise)
/* 177:    */   {
/* 178:238 */     assert (ch.eventLoop().inEventLoop());
/* 179:240 */     if (future.isSuccess())
/* 180:    */     {
/* 181:241 */       if (((Boolean)future.getNow()).booleanValue())
/* 182:    */       {
/* 183:    */         try
/* 184:    */         {
/* 185:243 */           ch.attr(POOL_KEY).set(this);
/* 186:244 */           this.handler.channelAcquired(ch);
/* 187:245 */           promise.setSuccess(ch);
/* 188:    */         }
/* 189:    */         catch (Throwable cause)
/* 190:    */         {
/* 191:247 */           closeAndFail(ch, cause, promise);
/* 192:    */         }
/* 193:    */       }
/* 194:    */       else
/* 195:    */       {
/* 196:250 */         closeChannel(ch);
/* 197:251 */         acquireHealthyFromPoolOrNew(promise);
/* 198:    */       }
/* 199:    */     }
/* 200:    */     else
/* 201:    */     {
/* 202:254 */       closeChannel(ch);
/* 203:255 */       acquireHealthyFromPoolOrNew(promise);
/* 204:    */     }
/* 205:    */   }
/* 206:    */   
/* 207:    */   protected ChannelFuture connectChannel(Bootstrap bs)
/* 208:    */   {
/* 209:266 */     return bs.connect();
/* 210:    */   }
/* 211:    */   
/* 212:    */   public final Future<Void> release(Channel channel)
/* 213:    */   {
/* 214:271 */     return release(channel, channel.eventLoop().newPromise());
/* 215:    */   }
/* 216:    */   
/* 217:    */   public Future<Void> release(final Channel channel, final Promise<Void> promise)
/* 218:    */   {
/* 219:276 */     ObjectUtil.checkNotNull(channel, "channel");
/* 220:277 */     ObjectUtil.checkNotNull(promise, "promise");
/* 221:    */     try
/* 222:    */     {
/* 223:279 */       EventLoop loop = channel.eventLoop();
/* 224:280 */       if (loop.inEventLoop()) {
/* 225:281 */         doReleaseChannel(channel, promise);
/* 226:    */       } else {
/* 227:283 */         loop.execute(new Runnable()
/* 228:    */         {
/* 229:    */           public void run()
/* 230:    */           {
/* 231:286 */             SimpleChannelPool.this.doReleaseChannel(channel, promise);
/* 232:    */           }
/* 233:    */         });
/* 234:    */       }
/* 235:    */     }
/* 236:    */     catch (Throwable cause)
/* 237:    */     {
/* 238:291 */       closeAndFail(channel, cause, promise);
/* 239:    */     }
/* 240:293 */     return promise;
/* 241:    */   }
/* 242:    */   
/* 243:    */   private void doReleaseChannel(Channel channel, Promise<Void> promise)
/* 244:    */   {
/* 245:297 */     assert (channel.eventLoop().inEventLoop());
/* 246:299 */     if (channel.attr(POOL_KEY).getAndSet(null) != this) {
/* 247:300 */       closeAndFail(channel, new IllegalArgumentException("Channel " + channel + " was not acquired from this ChannelPool"), promise);
/* 248:    */     } else {
/* 249:    */       try
/* 250:    */       {
/* 251:307 */         if (this.releaseHealthCheck) {
/* 252:308 */           doHealthCheckOnRelease(channel, promise);
/* 253:    */         } else {
/* 254:310 */           releaseAndOffer(channel, promise);
/* 255:    */         }
/* 256:    */       }
/* 257:    */       catch (Throwable cause)
/* 258:    */       {
/* 259:313 */         closeAndFail(channel, cause, promise);
/* 260:    */       }
/* 261:    */     }
/* 262:    */   }
/* 263:    */   
/* 264:    */   private void doHealthCheckOnRelease(final Channel channel, final Promise<Void> promise)
/* 265:    */     throws Exception
/* 266:    */   {
/* 267:319 */     final Future<Boolean> f = this.healthCheck.isHealthy(channel);
/* 268:320 */     if (f.isDone()) {
/* 269:321 */       releaseAndOfferIfHealthy(channel, promise, f);
/* 270:    */     } else {
/* 271:323 */       f.addListener(new FutureListener()
/* 272:    */       {
/* 273:    */         public void operationComplete(Future<Boolean> future)
/* 274:    */           throws Exception
/* 275:    */         {
/* 276:326 */           SimpleChannelPool.this.releaseAndOfferIfHealthy(channel, promise, f);
/* 277:    */         }
/* 278:    */       });
/* 279:    */     }
/* 280:    */   }
/* 281:    */   
/* 282:    */   private void releaseAndOfferIfHealthy(Channel channel, Promise<Void> promise, Future<Boolean> future)
/* 283:    */     throws Exception
/* 284:    */   {
/* 285:341 */     if (((Boolean)future.getNow()).booleanValue())
/* 286:    */     {
/* 287:342 */       releaseAndOffer(channel, promise);
/* 288:    */     }
/* 289:    */     else
/* 290:    */     {
/* 291:344 */       this.handler.channelReleased(channel);
/* 292:345 */       promise.setSuccess(null);
/* 293:    */     }
/* 294:    */   }
/* 295:    */   
/* 296:    */   private void releaseAndOffer(Channel channel, Promise<Void> promise)
/* 297:    */     throws Exception
/* 298:    */   {
/* 299:350 */     if (offerChannel(channel))
/* 300:    */     {
/* 301:351 */       this.handler.channelReleased(channel);
/* 302:352 */       promise.setSuccess(null);
/* 303:    */     }
/* 304:    */     else
/* 305:    */     {
/* 306:354 */       closeAndFail(channel, FULL_EXCEPTION, promise);
/* 307:    */     }
/* 308:    */   }
/* 309:    */   
/* 310:    */   private static void closeChannel(Channel channel)
/* 311:    */   {
/* 312:359 */     channel.attr(POOL_KEY).getAndSet(null);
/* 313:360 */     channel.close();
/* 314:    */   }
/* 315:    */   
/* 316:    */   private static void closeAndFail(Channel channel, Throwable cause, Promise<?> promise)
/* 317:    */   {
/* 318:364 */     closeChannel(channel);
/* 319:365 */     promise.tryFailure(cause);
/* 320:    */   }
/* 321:    */   
/* 322:    */   protected Channel pollChannel()
/* 323:    */   {
/* 324:376 */     return this.lastRecentUsed ? (Channel)this.deque.pollLast() : (Channel)this.deque.pollFirst();
/* 325:    */   }
/* 326:    */   
/* 327:    */   protected boolean offerChannel(Channel channel)
/* 328:    */   {
/* 329:387 */     return this.deque.offer(channel);
/* 330:    */   }
/* 331:    */   
/* 332:    */   public void close()
/* 333:    */   {
/* 334:    */     for (;;)
/* 335:    */     {
/* 336:393 */       Channel channel = pollChannel();
/* 337:394 */       if (channel == null) {
/* 338:    */         break;
/* 339:    */       }
/* 340:397 */       channel.close();
/* 341:    */     }
/* 342:    */   }
/* 343:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.pool.SimpleChannelPool
 * JD-Core Version:    0.7.0.1
 */