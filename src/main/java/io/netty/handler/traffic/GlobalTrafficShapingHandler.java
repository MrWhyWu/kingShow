/*   1:    */ package io.netty.handler.traffic;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.Channel;
/*   5:    */ import io.netty.channel.ChannelHandler.Sharable;
/*   6:    */ import io.netty.channel.ChannelHandlerContext;
/*   7:    */ import io.netty.channel.ChannelPromise;
/*   8:    */ import io.netty.util.concurrent.EventExecutor;
/*   9:    */ import io.netty.util.internal.PlatformDependent;
/*  10:    */ import java.util.ArrayDeque;
/*  11:    */ import java.util.concurrent.ConcurrentMap;
/*  12:    */ import java.util.concurrent.ScheduledExecutorService;
/*  13:    */ import java.util.concurrent.TimeUnit;
/*  14:    */ import java.util.concurrent.atomic.AtomicLong;
/*  15:    */ 
/*  16:    */ @ChannelHandler.Sharable
/*  17:    */ public class GlobalTrafficShapingHandler
/*  18:    */   extends AbstractTrafficShapingHandler
/*  19:    */ {
/*  20: 82 */   private final ConcurrentMap<Integer, PerChannel> channelQueues = PlatformDependent.newConcurrentHashMap();
/*  21: 87 */   private final AtomicLong queuesSize = new AtomicLong();
/*  22: 93 */   long maxGlobalWriteSize = 419430400L;
/*  23:    */   
/*  24:    */   void createGlobalTrafficCounter(ScheduledExecutorService executor)
/*  25:    */   {
/*  26:106 */     if (executor == null) {
/*  27:107 */       throw new NullPointerException("executor");
/*  28:    */     }
/*  29:109 */     TrafficCounter tc = new TrafficCounter(this, executor, "GlobalTC", this.checkInterval);
/*  30:110 */     setTrafficCounter(tc);
/*  31:111 */     tc.start();
/*  32:    */   }
/*  33:    */   
/*  34:    */   protected int userDefinedWritabilityIndex()
/*  35:    */   {
/*  36:116 */     return 2;
/*  37:    */   }
/*  38:    */   
/*  39:    */   public GlobalTrafficShapingHandler(ScheduledExecutorService executor, long writeLimit, long readLimit, long checkInterval, long maxTime)
/*  40:    */   {
/*  41:136 */     super(writeLimit, readLimit, checkInterval, maxTime);
/*  42:137 */     createGlobalTrafficCounter(executor);
/*  43:    */   }
/*  44:    */   
/*  45:    */   public GlobalTrafficShapingHandler(ScheduledExecutorService executor, long writeLimit, long readLimit, long checkInterval)
/*  46:    */   {
/*  47:156 */     super(writeLimit, readLimit, checkInterval);
/*  48:157 */     createGlobalTrafficCounter(executor);
/*  49:    */   }
/*  50:    */   
/*  51:    */   public GlobalTrafficShapingHandler(ScheduledExecutorService executor, long writeLimit, long readLimit)
/*  52:    */   {
/*  53:173 */     super(writeLimit, readLimit);
/*  54:174 */     createGlobalTrafficCounter(executor);
/*  55:    */   }
/*  56:    */   
/*  57:    */   public GlobalTrafficShapingHandler(ScheduledExecutorService executor, long checkInterval)
/*  58:    */   {
/*  59:188 */     super(checkInterval);
/*  60:189 */     createGlobalTrafficCounter(executor);
/*  61:    */   }
/*  62:    */   
/*  63:    */   public GlobalTrafficShapingHandler(EventExecutor executor)
/*  64:    */   {
/*  65:200 */     createGlobalTrafficCounter(executor);
/*  66:    */   }
/*  67:    */   
/*  68:    */   public long getMaxGlobalWriteSize()
/*  69:    */   {
/*  70:207 */     return this.maxGlobalWriteSize;
/*  71:    */   }
/*  72:    */   
/*  73:    */   public void setMaxGlobalWriteSize(long maxGlobalWriteSize)
/*  74:    */   {
/*  75:222 */     this.maxGlobalWriteSize = maxGlobalWriteSize;
/*  76:    */   }
/*  77:    */   
/*  78:    */   public long queuesSize()
/*  79:    */   {
/*  80:229 */     return this.queuesSize.get();
/*  81:    */   }
/*  82:    */   
/*  83:    */   public final void release()
/*  84:    */   {
/*  85:236 */     this.trafficCounter.stop();
/*  86:    */   }
/*  87:    */   
/*  88:    */   private PerChannel getOrSetPerChannel(ChannelHandlerContext ctx)
/*  89:    */   {
/*  90:241 */     Channel channel = ctx.channel();
/*  91:242 */     Integer key = Integer.valueOf(channel.hashCode());
/*  92:243 */     PerChannel perChannel = (PerChannel)this.channelQueues.get(key);
/*  93:244 */     if (perChannel == null)
/*  94:    */     {
/*  95:245 */       perChannel = new PerChannel(null);
/*  96:246 */       perChannel.messagesQueue = new ArrayDeque();
/*  97:247 */       perChannel.queueSize = 0L;
/*  98:248 */       perChannel.lastReadTimestamp = TrafficCounter.milliSecondFromNano();
/*  99:249 */       perChannel.lastWriteTimestamp = perChannel.lastReadTimestamp;
/* 100:250 */       this.channelQueues.put(key, perChannel);
/* 101:    */     }
/* 102:252 */     return perChannel;
/* 103:    */   }
/* 104:    */   
/* 105:    */   public void handlerAdded(ChannelHandlerContext ctx)
/* 106:    */     throws Exception
/* 107:    */   {
/* 108:257 */     getOrSetPerChannel(ctx);
/* 109:258 */     super.handlerAdded(ctx);
/* 110:    */   }
/* 111:    */   
/* 112:    */   public void handlerRemoved(ChannelHandlerContext ctx)
/* 113:    */     throws Exception
/* 114:    */   {
/* 115:263 */     Channel channel = ctx.channel();
/* 116:264 */     Integer key = Integer.valueOf(channel.hashCode());
/* 117:265 */     PerChannel perChannel = (PerChannel)this.channelQueues.remove(key);
/* 118:266 */     if (perChannel != null) {
/* 119:268 */       synchronized (perChannel)
/* 120:    */       {
/* 121:269 */         if (channel.isActive())
/* 122:    */         {
/* 123:270 */           for (ToSend toSend : perChannel.messagesQueue)
/* 124:    */           {
/* 125:271 */             long size = calculateSize(toSend.toSend);
/* 126:272 */             this.trafficCounter.bytesRealWriteFlowControl(size);
/* 127:273 */             perChannel.queueSize -= size;
/* 128:274 */             this.queuesSize.addAndGet(-size);
/* 129:275 */             ctx.write(toSend.toSend, toSend.promise);
/* 130:    */           }
/* 131:    */         }
/* 132:    */         else
/* 133:    */         {
/* 134:278 */           this.queuesSize.addAndGet(-perChannel.queueSize);
/* 135:279 */           for (ToSend toSend : perChannel.messagesQueue) {
/* 136:280 */             if ((toSend.toSend instanceof ByteBuf)) {
/* 137:281 */               ((ByteBuf)toSend.toSend).release();
/* 138:    */             }
/* 139:    */           }
/* 140:    */         }
/* 141:285 */         perChannel.messagesQueue.clear();
/* 142:    */       }
/* 143:    */     }
/* 144:288 */     releaseWriteSuspended(ctx);
/* 145:289 */     releaseReadSuspended(ctx);
/* 146:290 */     super.handlerRemoved(ctx);
/* 147:    */   }
/* 148:    */   
/* 149:    */   long checkWaitReadTime(ChannelHandlerContext ctx, long wait, long now)
/* 150:    */   {
/* 151:295 */     Integer key = Integer.valueOf(ctx.channel().hashCode());
/* 152:296 */     PerChannel perChannel = (PerChannel)this.channelQueues.get(key);
/* 153:297 */     if ((perChannel != null) && 
/* 154:298 */       (wait > this.maxTime) && (now + wait - perChannel.lastReadTimestamp > this.maxTime)) {
/* 155:299 */       wait = this.maxTime;
/* 156:    */     }
/* 157:302 */     return wait;
/* 158:    */   }
/* 159:    */   
/* 160:    */   void informReadOperation(ChannelHandlerContext ctx, long now)
/* 161:    */   {
/* 162:307 */     Integer key = Integer.valueOf(ctx.channel().hashCode());
/* 163:308 */     PerChannel perChannel = (PerChannel)this.channelQueues.get(key);
/* 164:309 */     if (perChannel != null) {
/* 165:310 */       perChannel.lastReadTimestamp = now;
/* 166:    */     }
/* 167:    */   }
/* 168:    */   
/* 169:    */   private static final class ToSend
/* 170:    */   {
/* 171:    */     final long relativeTimeAction;
/* 172:    */     final Object toSend;
/* 173:    */     final long size;
/* 174:    */     final ChannelPromise promise;
/* 175:    */     
/* 176:    */     private ToSend(long delay, Object toSend, long size, ChannelPromise promise)
/* 177:    */     {
/* 178:321 */       this.relativeTimeAction = delay;
/* 179:322 */       this.toSend = toSend;
/* 180:323 */       this.size = size;
/* 181:324 */       this.promise = promise;
/* 182:    */     }
/* 183:    */   }
/* 184:    */   
/* 185:    */   void submitWrite(final ChannelHandlerContext ctx, Object msg, long size, long writedelay, long now, ChannelPromise promise)
/* 186:    */   {
/* 187:332 */     Channel channel = ctx.channel();
/* 188:333 */     Integer key = Integer.valueOf(channel.hashCode());
/* 189:334 */     PerChannel perChannel = (PerChannel)this.channelQueues.get(key);
/* 190:335 */     if (perChannel == null) {
/* 191:338 */       perChannel = getOrSetPerChannel(ctx);
/* 192:    */     }
/* 193:341 */     long delay = writedelay;
/* 194:342 */     boolean globalSizeExceeded = false;
/* 195:344 */     synchronized (perChannel)
/* 196:    */     {
/* 197:345 */       if ((writedelay == 0L) && (perChannel.messagesQueue.isEmpty()))
/* 198:    */       {
/* 199:346 */         this.trafficCounter.bytesRealWriteFlowControl(size);
/* 200:347 */         ctx.write(msg, promise);
/* 201:348 */         perChannel.lastWriteTimestamp = now;
/* 202:349 */         return;
/* 203:    */       }
/* 204:351 */       if ((delay > this.maxTime) && (now + delay - perChannel.lastWriteTimestamp > this.maxTime)) {
/* 205:352 */         delay = this.maxTime;
/* 206:    */       }
/* 207:354 */       ToSend newToSend = new ToSend(delay + now, msg, size, promise, null);
/* 208:355 */       perChannel.messagesQueue.addLast(newToSend);
/* 209:356 */       perChannel.queueSize += size;
/* 210:357 */       this.queuesSize.addAndGet(size);
/* 211:358 */       checkWriteSuspend(ctx, delay, perChannel.queueSize);
/* 212:359 */       if (this.queuesSize.get() > this.maxGlobalWriteSize) {
/* 213:360 */         globalSizeExceeded = true;
/* 214:    */       }
/* 215:    */     }
/* 216:    */     ToSend newToSend;
/* 217:363 */     if (globalSizeExceeded) {
/* 218:364 */       setUserDefinedWritability(ctx, false);
/* 219:    */     }
/* 220:366 */     final long futureNow = newToSend.relativeTimeAction;
/* 221:367 */     final PerChannel forSchedule = perChannel;
/* 222:368 */     ctx.executor().schedule(new Runnable()
/* 223:    */     {
/* 224:    */       public void run()
/* 225:    */       {
/* 226:371 */         GlobalTrafficShapingHandler.this.sendAllValid(ctx, forSchedule, futureNow);
/* 227:    */       }
/* 228:371 */     }, delay, TimeUnit.MILLISECONDS);
/* 229:    */   }
/* 230:    */   
/* 231:    */   private void sendAllValid(ChannelHandlerContext ctx, PerChannel perChannel, long now)
/* 232:    */   {
/* 233:378 */     synchronized (perChannel)
/* 234:    */     {
/* 235:379 */       for (ToSend newToSend = (ToSend)perChannel.messagesQueue.pollFirst(); newToSend != null; newToSend = (ToSend)perChannel.messagesQueue.pollFirst()) {
/* 236:381 */         if (newToSend.relativeTimeAction <= now)
/* 237:    */         {
/* 238:382 */           long size = newToSend.size;
/* 239:383 */           this.trafficCounter.bytesRealWriteFlowControl(size);
/* 240:384 */           perChannel.queueSize -= size;
/* 241:385 */           this.queuesSize.addAndGet(-size);
/* 242:386 */           ctx.write(newToSend.toSend, newToSend.promise);
/* 243:387 */           perChannel.lastWriteTimestamp = now;
/* 244:    */         }
/* 245:    */         else
/* 246:    */         {
/* 247:389 */           perChannel.messagesQueue.addFirst(newToSend);
/* 248:390 */           break;
/* 249:    */         }
/* 250:    */       }
/* 251:393 */       if (perChannel.messagesQueue.isEmpty()) {
/* 252:394 */         releaseWriteSuspended(ctx);
/* 253:    */       }
/* 254:    */     }
/* 255:397 */     ctx.flush();
/* 256:    */   }
/* 257:    */   
/* 258:    */   private static final class PerChannel
/* 259:    */   {
/* 260:    */     ArrayDeque<GlobalTrafficShapingHandler.ToSend> messagesQueue;
/* 261:    */     long queueSize;
/* 262:    */     long lastWriteTimestamp;
/* 263:    */     long lastReadTimestamp;
/* 264:    */   }
/* 265:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.traffic.GlobalTrafficShapingHandler
 * JD-Core Version:    0.7.0.1
 */