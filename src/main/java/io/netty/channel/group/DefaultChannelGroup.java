/*   1:    */ package io.netty.channel.group;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufHolder;
/*   5:    */ import io.netty.channel.Channel;
/*   6:    */ import io.netty.channel.ChannelFuture;
/*   7:    */ import io.netty.channel.ChannelFutureListener;
/*   8:    */ import io.netty.channel.ChannelId;
/*   9:    */ import io.netty.channel.ServerChannel;
/*  10:    */ import io.netty.util.ReferenceCountUtil;
/*  11:    */ import io.netty.util.concurrent.EventExecutor;
/*  12:    */ import io.netty.util.internal.PlatformDependent;
/*  13:    */ import io.netty.util.internal.StringUtil;
/*  14:    */ import java.util.AbstractSet;
/*  15:    */ import java.util.ArrayList;
/*  16:    */ import java.util.Collection;
/*  17:    */ import java.util.Iterator;
/*  18:    */ import java.util.LinkedHashMap;
/*  19:    */ import java.util.Map;
/*  20:    */ import java.util.concurrent.ConcurrentMap;
/*  21:    */ import java.util.concurrent.atomic.AtomicInteger;
/*  22:    */ 
/*  23:    */ public class DefaultChannelGroup
/*  24:    */   extends AbstractSet<Channel>
/*  25:    */   implements ChannelGroup
/*  26:    */ {
/*  27: 44 */   private static final AtomicInteger nextId = new AtomicInteger();
/*  28:    */   private final String name;
/*  29:    */   private final EventExecutor executor;
/*  30: 47 */   private final ConcurrentMap<ChannelId, Channel> serverChannels = PlatformDependent.newConcurrentHashMap();
/*  31: 48 */   private final ConcurrentMap<ChannelId, Channel> nonServerChannels = PlatformDependent.newConcurrentHashMap();
/*  32: 49 */   private final ChannelFutureListener remover = new ChannelFutureListener()
/*  33:    */   {
/*  34:    */     public void operationComplete(ChannelFuture future)
/*  35:    */       throws Exception
/*  36:    */     {
/*  37: 52 */       DefaultChannelGroup.this.remove(future.channel());
/*  38:    */     }
/*  39:    */   };
/*  40: 55 */   private final VoidChannelGroupFuture voidFuture = new VoidChannelGroupFuture(this);
/*  41:    */   private final boolean stayClosed;
/*  42:    */   private volatile boolean closed;
/*  43:    */   
/*  44:    */   public DefaultChannelGroup(EventExecutor executor)
/*  45:    */   {
/*  46: 64 */     this(executor, false);
/*  47:    */   }
/*  48:    */   
/*  49:    */   public DefaultChannelGroup(String name, EventExecutor executor)
/*  50:    */   {
/*  51: 73 */     this(name, executor, false);
/*  52:    */   }
/*  53:    */   
/*  54:    */   public DefaultChannelGroup(EventExecutor executor, boolean stayClosed)
/*  55:    */   {
/*  56: 83 */     this("group-0x" + Integer.toHexString(nextId.incrementAndGet()), executor, stayClosed);
/*  57:    */   }
/*  58:    */   
/*  59:    */   public DefaultChannelGroup(String name, EventExecutor executor, boolean stayClosed)
/*  60:    */   {
/*  61: 94 */     if (name == null) {
/*  62: 95 */       throw new NullPointerException("name");
/*  63:    */     }
/*  64: 97 */     this.name = name;
/*  65: 98 */     this.executor = executor;
/*  66: 99 */     this.stayClosed = stayClosed;
/*  67:    */   }
/*  68:    */   
/*  69:    */   public String name()
/*  70:    */   {
/*  71:104 */     return this.name;
/*  72:    */   }
/*  73:    */   
/*  74:    */   public Channel find(ChannelId id)
/*  75:    */   {
/*  76:109 */     Channel c = (Channel)this.nonServerChannels.get(id);
/*  77:110 */     if (c != null) {
/*  78:111 */       return c;
/*  79:    */     }
/*  80:113 */     return (Channel)this.serverChannels.get(id);
/*  81:    */   }
/*  82:    */   
/*  83:    */   public boolean isEmpty()
/*  84:    */   {
/*  85:119 */     return (this.nonServerChannels.isEmpty()) && (this.serverChannels.isEmpty());
/*  86:    */   }
/*  87:    */   
/*  88:    */   public int size()
/*  89:    */   {
/*  90:124 */     return this.nonServerChannels.size() + this.serverChannels.size();
/*  91:    */   }
/*  92:    */   
/*  93:    */   public boolean contains(Object o)
/*  94:    */   {
/*  95:129 */     if ((o instanceof Channel))
/*  96:    */     {
/*  97:130 */       Channel c = (Channel)o;
/*  98:131 */       if ((o instanceof ServerChannel)) {
/*  99:132 */         return this.serverChannels.containsValue(c);
/* 100:    */       }
/* 101:134 */       return this.nonServerChannels.containsValue(c);
/* 102:    */     }
/* 103:137 */     return false;
/* 104:    */   }
/* 105:    */   
/* 106:    */   public boolean add(Channel channel)
/* 107:    */   {
/* 108:143 */     ConcurrentMap<ChannelId, Channel> map = (channel instanceof ServerChannel) ? this.serverChannels : this.nonServerChannels;
/* 109:    */     
/* 110:    */ 
/* 111:146 */     boolean added = map.putIfAbsent(channel.id(), channel) == null;
/* 112:147 */     if (added) {
/* 113:148 */       channel.closeFuture().addListener(this.remover);
/* 114:    */     }
/* 115:151 */     if ((this.stayClosed) && (this.closed)) {
/* 116:164 */       channel.close();
/* 117:    */     }
/* 118:167 */     return added;
/* 119:    */   }
/* 120:    */   
/* 121:    */   public boolean remove(Object o)
/* 122:    */   {
/* 123:172 */     Channel c = null;
/* 124:173 */     if ((o instanceof ChannelId))
/* 125:    */     {
/* 126:174 */       c = (Channel)this.nonServerChannels.remove(o);
/* 127:175 */       if (c == null) {
/* 128:176 */         c = (Channel)this.serverChannels.remove(o);
/* 129:    */       }
/* 130:    */     }
/* 131:178 */     else if ((o instanceof Channel))
/* 132:    */     {
/* 133:179 */       c = (Channel)o;
/* 134:180 */       if ((c instanceof ServerChannel)) {
/* 135:181 */         c = (Channel)this.serverChannels.remove(c.id());
/* 136:    */       } else {
/* 137:183 */         c = (Channel)this.nonServerChannels.remove(c.id());
/* 138:    */       }
/* 139:    */     }
/* 140:187 */     if (c == null) {
/* 141:188 */       return false;
/* 142:    */     }
/* 143:191 */     c.closeFuture().removeListener(this.remover);
/* 144:192 */     return true;
/* 145:    */   }
/* 146:    */   
/* 147:    */   public void clear()
/* 148:    */   {
/* 149:197 */     this.nonServerChannels.clear();
/* 150:198 */     this.serverChannels.clear();
/* 151:    */   }
/* 152:    */   
/* 153:    */   public Iterator<Channel> iterator()
/* 154:    */   {
/* 155:203 */     return new CombinedIterator(this.serverChannels
/* 156:204 */       .values().iterator(), this.nonServerChannels
/* 157:205 */       .values().iterator());
/* 158:    */   }
/* 159:    */   
/* 160:    */   public Object[] toArray()
/* 161:    */   {
/* 162:210 */     Collection<Channel> channels = new ArrayList(size());
/* 163:211 */     channels.addAll(this.serverChannels.values());
/* 164:212 */     channels.addAll(this.nonServerChannels.values());
/* 165:213 */     return channels.toArray();
/* 166:    */   }
/* 167:    */   
/* 168:    */   public <T> T[] toArray(T[] a)
/* 169:    */   {
/* 170:218 */     Collection<Channel> channels = new ArrayList(size());
/* 171:219 */     channels.addAll(this.serverChannels.values());
/* 172:220 */     channels.addAll(this.nonServerChannels.values());
/* 173:221 */     return channels.toArray(a);
/* 174:    */   }
/* 175:    */   
/* 176:    */   public ChannelGroupFuture close()
/* 177:    */   {
/* 178:226 */     return close(ChannelMatchers.all());
/* 179:    */   }
/* 180:    */   
/* 181:    */   public ChannelGroupFuture disconnect()
/* 182:    */   {
/* 183:231 */     return disconnect(ChannelMatchers.all());
/* 184:    */   }
/* 185:    */   
/* 186:    */   public ChannelGroupFuture deregister()
/* 187:    */   {
/* 188:236 */     return deregister(ChannelMatchers.all());
/* 189:    */   }
/* 190:    */   
/* 191:    */   public ChannelGroupFuture write(Object message)
/* 192:    */   {
/* 193:241 */     return write(message, ChannelMatchers.all());
/* 194:    */   }
/* 195:    */   
/* 196:    */   private static Object safeDuplicate(Object message)
/* 197:    */   {
/* 198:247 */     if ((message instanceof ByteBuf)) {
/* 199:248 */       return ((ByteBuf)message).retainedDuplicate();
/* 200:    */     }
/* 201:249 */     if ((message instanceof ByteBufHolder)) {
/* 202:250 */       return ((ByteBufHolder)message).retainedDuplicate();
/* 203:    */     }
/* 204:252 */     return ReferenceCountUtil.retain(message);
/* 205:    */   }
/* 206:    */   
/* 207:    */   public ChannelGroupFuture write(Object message, ChannelMatcher matcher)
/* 208:    */   {
/* 209:258 */     return write(message, matcher, false);
/* 210:    */   }
/* 211:    */   
/* 212:    */   public ChannelGroupFuture write(Object message, ChannelMatcher matcher, boolean voidPromise)
/* 213:    */   {
/* 214:263 */     if (message == null) {
/* 215:264 */       throw new NullPointerException("message");
/* 216:    */     }
/* 217:266 */     if (matcher == null) {
/* 218:267 */       throw new NullPointerException("matcher");
/* 219:    */     }
/* 220:    */     Channel c;
/* 221:    */     ChannelGroupFuture future;
/* 222:    */     ChannelGroupFuture future;
/* 223:271 */     if (voidPromise)
/* 224:    */     {
/* 225:272 */       for (Iterator localIterator = this.nonServerChannels.values().iterator(); localIterator.hasNext();)
/* 226:    */       {
/* 227:272 */         c = (Channel)localIterator.next();
/* 228:273 */         if (matcher.matches(c)) {
/* 229:274 */           c.write(safeDuplicate(message), c.voidPromise());
/* 230:    */         }
/* 231:    */       }
/* 232:277 */       future = this.voidFuture;
/* 233:    */     }
/* 234:    */     else
/* 235:    */     {
/* 236:279 */       Object futures = new LinkedHashMap(size());
/* 237:280 */       for (Channel c : this.nonServerChannels.values()) {
/* 238:281 */         if (matcher.matches(c)) {
/* 239:282 */           ((Map)futures).put(c, c.write(safeDuplicate(message)));
/* 240:    */         }
/* 241:    */       }
/* 242:285 */       future = new DefaultChannelGroupFuture(this, (Map)futures, this.executor);
/* 243:    */     }
/* 244:287 */     ReferenceCountUtil.release(message);
/* 245:288 */     return future;
/* 246:    */   }
/* 247:    */   
/* 248:    */   public ChannelGroup flush()
/* 249:    */   {
/* 250:293 */     return flush(ChannelMatchers.all());
/* 251:    */   }
/* 252:    */   
/* 253:    */   public ChannelGroupFuture flushAndWrite(Object message)
/* 254:    */   {
/* 255:298 */     return writeAndFlush(message);
/* 256:    */   }
/* 257:    */   
/* 258:    */   public ChannelGroupFuture writeAndFlush(Object message)
/* 259:    */   {
/* 260:303 */     return writeAndFlush(message, ChannelMatchers.all());
/* 261:    */   }
/* 262:    */   
/* 263:    */   public ChannelGroupFuture disconnect(ChannelMatcher matcher)
/* 264:    */   {
/* 265:308 */     if (matcher == null) {
/* 266:309 */       throw new NullPointerException("matcher");
/* 267:    */     }
/* 268:313 */     Map<Channel, ChannelFuture> futures = new LinkedHashMap(size());
/* 269:315 */     for (Channel c : this.serverChannels.values()) {
/* 270:316 */       if (matcher.matches(c)) {
/* 271:317 */         futures.put(c, c.disconnect());
/* 272:    */       }
/* 273:    */     }
/* 274:320 */     for (Channel c : this.nonServerChannels.values()) {
/* 275:321 */       if (matcher.matches(c)) {
/* 276:322 */         futures.put(c, c.disconnect());
/* 277:    */       }
/* 278:    */     }
/* 279:326 */     return new DefaultChannelGroupFuture(this, futures, this.executor);
/* 280:    */   }
/* 281:    */   
/* 282:    */   public ChannelGroupFuture close(ChannelMatcher matcher)
/* 283:    */   {
/* 284:331 */     if (matcher == null) {
/* 285:332 */       throw new NullPointerException("matcher");
/* 286:    */     }
/* 287:336 */     Map<Channel, ChannelFuture> futures = new LinkedHashMap(size());
/* 288:338 */     if (this.stayClosed) {
/* 289:345 */       this.closed = true;
/* 290:    */     }
/* 291:348 */     for (Channel c : this.serverChannels.values()) {
/* 292:349 */       if (matcher.matches(c)) {
/* 293:350 */         futures.put(c, c.close());
/* 294:    */       }
/* 295:    */     }
/* 296:353 */     for (Channel c : this.nonServerChannels.values()) {
/* 297:354 */       if (matcher.matches(c)) {
/* 298:355 */         futures.put(c, c.close());
/* 299:    */       }
/* 300:    */     }
/* 301:359 */     return new DefaultChannelGroupFuture(this, futures, this.executor);
/* 302:    */   }
/* 303:    */   
/* 304:    */   public ChannelGroupFuture deregister(ChannelMatcher matcher)
/* 305:    */   {
/* 306:364 */     if (matcher == null) {
/* 307:365 */       throw new NullPointerException("matcher");
/* 308:    */     }
/* 309:369 */     Map<Channel, ChannelFuture> futures = new LinkedHashMap(size());
/* 310:371 */     for (Channel c : this.serverChannels.values()) {
/* 311:372 */       if (matcher.matches(c)) {
/* 312:373 */         futures.put(c, c.deregister());
/* 313:    */       }
/* 314:    */     }
/* 315:376 */     for (Channel c : this.nonServerChannels.values()) {
/* 316:377 */       if (matcher.matches(c)) {
/* 317:378 */         futures.put(c, c.deregister());
/* 318:    */       }
/* 319:    */     }
/* 320:382 */     return new DefaultChannelGroupFuture(this, futures, this.executor);
/* 321:    */   }
/* 322:    */   
/* 323:    */   public ChannelGroup flush(ChannelMatcher matcher)
/* 324:    */   {
/* 325:387 */     for (Channel c : this.nonServerChannels.values()) {
/* 326:388 */       if (matcher.matches(c)) {
/* 327:389 */         c.flush();
/* 328:    */       }
/* 329:    */     }
/* 330:392 */     return this;
/* 331:    */   }
/* 332:    */   
/* 333:    */   public ChannelGroupFuture flushAndWrite(Object message, ChannelMatcher matcher)
/* 334:    */   {
/* 335:397 */     return writeAndFlush(message, matcher);
/* 336:    */   }
/* 337:    */   
/* 338:    */   public ChannelGroupFuture writeAndFlush(Object message, ChannelMatcher matcher)
/* 339:    */   {
/* 340:402 */     return writeAndFlush(message, matcher, false);
/* 341:    */   }
/* 342:    */   
/* 343:    */   public ChannelGroupFuture writeAndFlush(Object message, ChannelMatcher matcher, boolean voidPromise)
/* 344:    */   {
/* 345:407 */     if (message == null) {
/* 346:408 */       throw new NullPointerException("message");
/* 347:    */     }
/* 348:    */     Channel c;
/* 349:    */     ChannelGroupFuture future;
/* 350:    */     ChannelGroupFuture future;
/* 351:412 */     if (voidPromise)
/* 352:    */     {
/* 353:413 */       for (Iterator localIterator = this.nonServerChannels.values().iterator(); localIterator.hasNext();)
/* 354:    */       {
/* 355:413 */         c = (Channel)localIterator.next();
/* 356:414 */         if (matcher.matches(c)) {
/* 357:415 */           c.writeAndFlush(safeDuplicate(message), c.voidPromise());
/* 358:    */         }
/* 359:    */       }
/* 360:418 */       future = this.voidFuture;
/* 361:    */     }
/* 362:    */     else
/* 363:    */     {
/* 364:420 */       Object futures = new LinkedHashMap(size());
/* 365:421 */       for (Channel c : this.nonServerChannels.values()) {
/* 366:422 */         if (matcher.matches(c)) {
/* 367:423 */           ((Map)futures).put(c, c.writeAndFlush(safeDuplicate(message)));
/* 368:    */         }
/* 369:    */       }
/* 370:426 */       future = new DefaultChannelGroupFuture(this, (Map)futures, this.executor);
/* 371:    */     }
/* 372:428 */     ReferenceCountUtil.release(message);
/* 373:429 */     return future;
/* 374:    */   }
/* 375:    */   
/* 376:    */   public ChannelGroupFuture newCloseFuture()
/* 377:    */   {
/* 378:434 */     return newCloseFuture(ChannelMatchers.all());
/* 379:    */   }
/* 380:    */   
/* 381:    */   public ChannelGroupFuture newCloseFuture(ChannelMatcher matcher)
/* 382:    */   {
/* 383:440 */     Map<Channel, ChannelFuture> futures = new LinkedHashMap(size());
/* 384:442 */     for (Channel c : this.serverChannels.values()) {
/* 385:443 */       if (matcher.matches(c)) {
/* 386:444 */         futures.put(c, c.closeFuture());
/* 387:    */       }
/* 388:    */     }
/* 389:447 */     for (Channel c : this.nonServerChannels.values()) {
/* 390:448 */       if (matcher.matches(c)) {
/* 391:449 */         futures.put(c, c.closeFuture());
/* 392:    */       }
/* 393:    */     }
/* 394:453 */     return new DefaultChannelGroupFuture(this, futures, this.executor);
/* 395:    */   }
/* 396:    */   
/* 397:    */   public int hashCode()
/* 398:    */   {
/* 399:458 */     return System.identityHashCode(this);
/* 400:    */   }
/* 401:    */   
/* 402:    */   public boolean equals(Object o)
/* 403:    */   {
/* 404:463 */     return this == o;
/* 405:    */   }
/* 406:    */   
/* 407:    */   public int compareTo(ChannelGroup o)
/* 408:    */   {
/* 409:468 */     int v = name().compareTo(o.name());
/* 410:469 */     if (v != 0) {
/* 411:470 */       return v;
/* 412:    */     }
/* 413:473 */     return System.identityHashCode(this) - System.identityHashCode(o);
/* 414:    */   }
/* 415:    */   
/* 416:    */   public String toString()
/* 417:    */   {
/* 418:478 */     return StringUtil.simpleClassName(this) + "(name: " + name() + ", size: " + size() + ')';
/* 419:    */   }
/* 420:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.group.DefaultChannelGroup
 * JD-Core Version:    0.7.0.1
 */