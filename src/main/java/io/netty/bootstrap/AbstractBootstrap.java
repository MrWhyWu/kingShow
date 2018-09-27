/*   1:    */ package io.netty.bootstrap;
/*   2:    */ 
/*   3:    */ import io.netty.channel.Channel;
/*   4:    */ import io.netty.channel.Channel.Unsafe;
/*   5:    */ import io.netty.channel.ChannelConfig;
/*   6:    */ import io.netty.channel.ChannelFuture;
/*   7:    */ import io.netty.channel.ChannelFutureListener;
/*   8:    */ import io.netty.channel.ChannelHandler;
/*   9:    */ import io.netty.channel.ChannelOption;
/*  10:    */ import io.netty.channel.ChannelPromise;
/*  11:    */ import io.netty.channel.DefaultChannelPromise;
/*  12:    */ import io.netty.channel.EventLoop;
/*  13:    */ import io.netty.channel.EventLoopGroup;
/*  14:    */ import io.netty.channel.ReflectiveChannelFactory;
/*  15:    */ import io.netty.util.AttributeKey;
/*  16:    */ import io.netty.util.concurrent.EventExecutor;
/*  17:    */ import io.netty.util.concurrent.GlobalEventExecutor;
/*  18:    */ import io.netty.util.internal.SocketUtils;
/*  19:    */ import io.netty.util.internal.StringUtil;
/*  20:    */ import io.netty.util.internal.logging.InternalLogger;
/*  21:    */ import java.net.InetAddress;
/*  22:    */ import java.net.InetSocketAddress;
/*  23:    */ import java.net.SocketAddress;
/*  24:    */ import java.util.Collections;
/*  25:    */ import java.util.LinkedHashMap;
/*  26:    */ import java.util.Map;
/*  27:    */ import java.util.Map.Entry;
/*  28:    */ 
/*  29:    */ public abstract class AbstractBootstrap<B extends AbstractBootstrap<B, C>, C extends Channel>
/*  30:    */   implements Cloneable
/*  31:    */ {
/*  32:    */   volatile EventLoopGroup group;
/*  33:    */   private volatile ChannelFactory<? extends C> channelFactory;
/*  34:    */   private volatile SocketAddress localAddress;
/*  35: 56 */   private final Map<ChannelOption<?>, Object> options = new LinkedHashMap();
/*  36: 57 */   private final Map<AttributeKey<?>, Object> attrs = new LinkedHashMap();
/*  37:    */   private volatile ChannelHandler handler;
/*  38:    */   
/*  39:    */   AbstractBootstrap() {}
/*  40:    */   
/*  41:    */   AbstractBootstrap(AbstractBootstrap<B, C> bootstrap)
/*  42:    */   {
/*  43: 65 */     this.group = bootstrap.group;
/*  44: 66 */     this.channelFactory = bootstrap.channelFactory;
/*  45: 67 */     this.handler = bootstrap.handler;
/*  46: 68 */     this.localAddress = bootstrap.localAddress;
/*  47: 69 */     synchronized (bootstrap.options)
/*  48:    */     {
/*  49: 70 */       this.options.putAll(bootstrap.options);
/*  50:    */     }
/*  51: 72 */     synchronized (bootstrap.attrs)
/*  52:    */     {
/*  53: 73 */       this.attrs.putAll(bootstrap.attrs);
/*  54:    */     }
/*  55:    */   }
/*  56:    */   
/*  57:    */   public B group(EventLoopGroup group)
/*  58:    */   {
/*  59: 82 */     if (group == null) {
/*  60: 83 */       throw new NullPointerException("group");
/*  61:    */     }
/*  62: 85 */     if (this.group != null) {
/*  63: 86 */       throw new IllegalStateException("group set already");
/*  64:    */     }
/*  65: 88 */     this.group = group;
/*  66: 89 */     return self();
/*  67:    */   }
/*  68:    */   
/*  69:    */   private B self()
/*  70:    */   {
/*  71: 94 */     return this;
/*  72:    */   }
/*  73:    */   
/*  74:    */   public B channel(Class<? extends C> channelClass)
/*  75:    */   {
/*  76:103 */     if (channelClass == null) {
/*  77:104 */       throw new NullPointerException("channelClass");
/*  78:    */     }
/*  79:106 */     return channelFactory(new ReflectiveChannelFactory(channelClass));
/*  80:    */   }
/*  81:    */   
/*  82:    */   @Deprecated
/*  83:    */   public B channelFactory(ChannelFactory<? extends C> channelFactory)
/*  84:    */   {
/*  85:114 */     if (channelFactory == null) {
/*  86:115 */       throw new NullPointerException("channelFactory");
/*  87:    */     }
/*  88:117 */     if (this.channelFactory != null) {
/*  89:118 */       throw new IllegalStateException("channelFactory set already");
/*  90:    */     }
/*  91:121 */     this.channelFactory = channelFactory;
/*  92:122 */     return self();
/*  93:    */   }
/*  94:    */   
/*  95:    */   public B channelFactory(io.netty.channel.ChannelFactory<? extends C> channelFactory)
/*  96:    */   {
/*  97:134 */     return channelFactory(channelFactory);
/*  98:    */   }
/*  99:    */   
/* 100:    */   public B localAddress(SocketAddress localAddress)
/* 101:    */   {
/* 102:141 */     this.localAddress = localAddress;
/* 103:142 */     return self();
/* 104:    */   }
/* 105:    */   
/* 106:    */   public B localAddress(int inetPort)
/* 107:    */   {
/* 108:149 */     return localAddress(new InetSocketAddress(inetPort));
/* 109:    */   }
/* 110:    */   
/* 111:    */   public B localAddress(String inetHost, int inetPort)
/* 112:    */   {
/* 113:156 */     return localAddress(SocketUtils.socketAddress(inetHost, inetPort));
/* 114:    */   }
/* 115:    */   
/* 116:    */   public B localAddress(InetAddress inetHost, int inetPort)
/* 117:    */   {
/* 118:163 */     return localAddress(new InetSocketAddress(inetHost, inetPort));
/* 119:    */   }
/* 120:    */   
/* 121:    */   public <T> B option(ChannelOption<T> option, T value)
/* 122:    */   {
/* 123:171 */     if (option == null) {
/* 124:172 */       throw new NullPointerException("option");
/* 125:    */     }
/* 126:174 */     if (value == null) {
/* 127:175 */       synchronized (this.options)
/* 128:    */       {
/* 129:176 */         this.options.remove(option);
/* 130:    */       }
/* 131:    */     } else {
/* 132:179 */       synchronized (this.options)
/* 133:    */       {
/* 134:180 */         this.options.put(option, value);
/* 135:    */       }
/* 136:    */     }
/* 137:183 */     return self();
/* 138:    */   }
/* 139:    */   
/* 140:    */   public <T> B attr(AttributeKey<T> key, T value)
/* 141:    */   {
/* 142:191 */     if (key == null) {
/* 143:192 */       throw new NullPointerException("key");
/* 144:    */     }
/* 145:194 */     if (value == null) {
/* 146:195 */       synchronized (this.attrs)
/* 147:    */       {
/* 148:196 */         this.attrs.remove(key);
/* 149:    */       }
/* 150:    */     } else {
/* 151:199 */       synchronized (this.attrs)
/* 152:    */       {
/* 153:200 */         this.attrs.put(key, value);
/* 154:    */       }
/* 155:    */     }
/* 156:203 */     return self();
/* 157:    */   }
/* 158:    */   
/* 159:    */   public B validate()
/* 160:    */   {
/* 161:211 */     if (this.group == null) {
/* 162:212 */       throw new IllegalStateException("group not set");
/* 163:    */     }
/* 164:214 */     if (this.channelFactory == null) {
/* 165:215 */       throw new IllegalStateException("channel or channelFactory not set");
/* 166:    */     }
/* 167:217 */     return self();
/* 168:    */   }
/* 169:    */   
/* 170:    */   public abstract B clone();
/* 171:    */   
/* 172:    */   public ChannelFuture register()
/* 173:    */   {
/* 174:233 */     validate();
/* 175:234 */     return initAndRegister();
/* 176:    */   }
/* 177:    */   
/* 178:    */   public ChannelFuture bind()
/* 179:    */   {
/* 180:241 */     validate();
/* 181:242 */     SocketAddress localAddress = this.localAddress;
/* 182:243 */     if (localAddress == null) {
/* 183:244 */       throw new IllegalStateException("localAddress not set");
/* 184:    */     }
/* 185:246 */     return doBind(localAddress);
/* 186:    */   }
/* 187:    */   
/* 188:    */   public ChannelFuture bind(int inetPort)
/* 189:    */   {
/* 190:253 */     return bind(new InetSocketAddress(inetPort));
/* 191:    */   }
/* 192:    */   
/* 193:    */   public ChannelFuture bind(String inetHost, int inetPort)
/* 194:    */   {
/* 195:260 */     return bind(SocketUtils.socketAddress(inetHost, inetPort));
/* 196:    */   }
/* 197:    */   
/* 198:    */   public ChannelFuture bind(InetAddress inetHost, int inetPort)
/* 199:    */   {
/* 200:267 */     return bind(new InetSocketAddress(inetHost, inetPort));
/* 201:    */   }
/* 202:    */   
/* 203:    */   public ChannelFuture bind(SocketAddress localAddress)
/* 204:    */   {
/* 205:274 */     validate();
/* 206:275 */     if (localAddress == null) {
/* 207:276 */       throw new NullPointerException("localAddress");
/* 208:    */     }
/* 209:278 */     return doBind(localAddress);
/* 210:    */   }
/* 211:    */   
/* 212:    */   private ChannelFuture doBind(final SocketAddress localAddress)
/* 213:    */   {
/* 214:282 */     final ChannelFuture regFuture = initAndRegister();
/* 215:283 */     final Channel channel = regFuture.channel();
/* 216:284 */     if (regFuture.cause() != null) {
/* 217:285 */       return regFuture;
/* 218:    */     }
/* 219:288 */     if (regFuture.isDone())
/* 220:    */     {
/* 221:290 */       ChannelPromise promise = channel.newPromise();
/* 222:291 */       doBind0(regFuture, channel, localAddress, promise);
/* 223:292 */       return promise;
/* 224:    */     }
/* 225:295 */     final PendingRegistrationPromise promise = new PendingRegistrationPromise(channel);
/* 226:296 */     regFuture.addListener(new ChannelFutureListener()
/* 227:    */     {
/* 228:    */       public void operationComplete(ChannelFuture future)
/* 229:    */         throws Exception
/* 230:    */       {
/* 231:299 */         Throwable cause = future.cause();
/* 232:300 */         if (cause != null)
/* 233:    */         {
/* 234:303 */           promise.setFailure(cause);
/* 235:    */         }
/* 236:    */         else
/* 237:    */         {
/* 238:307 */           promise.registered();
/* 239:    */           
/* 240:309 */           AbstractBootstrap.doBind0(regFuture, channel, localAddress, promise);
/* 241:    */         }
/* 242:    */       }
/* 243:312 */     });
/* 244:313 */     return promise;
/* 245:    */   }
/* 246:    */   
/* 247:    */   final ChannelFuture initAndRegister()
/* 248:    */   {
/* 249:318 */     Channel channel = null;
/* 250:    */     try
/* 251:    */     {
/* 252:320 */       channel = this.channelFactory.newChannel();
/* 253:321 */       init(channel);
/* 254:    */     }
/* 255:    */     catch (Throwable t)
/* 256:    */     {
/* 257:323 */       if (channel != null) {
/* 258:325 */         channel.unsafe().closeForcibly();
/* 259:    */       }
/* 260:328 */       return new DefaultChannelPromise(channel, GlobalEventExecutor.INSTANCE).setFailure(t);
/* 261:    */     }
/* 262:331 */     ChannelFuture regFuture = config().group().register(channel);
/* 263:332 */     if (regFuture.cause() != null) {
/* 264:333 */       if (channel.isRegistered()) {
/* 265:334 */         channel.close();
/* 266:    */       } else {
/* 267:336 */         channel.unsafe().closeForcibly();
/* 268:    */       }
/* 269:    */     }
/* 270:349 */     return regFuture;
/* 271:    */   }
/* 272:    */   
/* 273:    */   abstract void init(Channel paramChannel)
/* 274:    */     throws Exception;
/* 275:    */   
/* 276:    */   private static void doBind0(ChannelFuture regFuture, final Channel channel, final SocketAddress localAddress, final ChannelPromise promise)
/* 277:    */   {
/* 278:360 */     channel.eventLoop().execute(new Runnable()
/* 279:    */     {
/* 280:    */       public void run()
/* 281:    */       {
/* 282:363 */         if (this.val$regFuture.isSuccess()) {
/* 283:364 */           channel.bind(localAddress, promise).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
/* 284:    */         } else {
/* 285:366 */           promise.setFailure(this.val$regFuture.cause());
/* 286:    */         }
/* 287:    */       }
/* 288:    */     });
/* 289:    */   }
/* 290:    */   
/* 291:    */   public B handler(ChannelHandler handler)
/* 292:    */   {
/* 293:376 */     if (handler == null) {
/* 294:377 */       throw new NullPointerException("handler");
/* 295:    */     }
/* 296:379 */     this.handler = handler;
/* 297:380 */     return self();
/* 298:    */   }
/* 299:    */   
/* 300:    */   @Deprecated
/* 301:    */   public final EventLoopGroup group()
/* 302:    */   {
/* 303:390 */     return this.group;
/* 304:    */   }
/* 305:    */   
/* 306:    */   public abstract AbstractBootstrapConfig<B, C> config();
/* 307:    */   
/* 308:    */   static <K, V> Map<K, V> copiedMap(Map<K, V> map)
/* 309:    */   {
/* 310:    */     Map<K, V> copied;
/* 311:401 */     synchronized (map)
/* 312:    */     {
/* 313:402 */       if (map.isEmpty()) {
/* 314:403 */         return Collections.emptyMap();
/* 315:    */       }
/* 316:405 */       copied = new LinkedHashMap(map);
/* 317:    */     }
/* 318:    */     Map<K, V> copied;
/* 319:407 */     return Collections.unmodifiableMap(copied);
/* 320:    */   }
/* 321:    */   
/* 322:    */   final Map<ChannelOption<?>, Object> options0()
/* 323:    */   {
/* 324:411 */     return this.options;
/* 325:    */   }
/* 326:    */   
/* 327:    */   final Map<AttributeKey<?>, Object> attrs0()
/* 328:    */   {
/* 329:415 */     return this.attrs;
/* 330:    */   }
/* 331:    */   
/* 332:    */   final SocketAddress localAddress()
/* 333:    */   {
/* 334:419 */     return this.localAddress;
/* 335:    */   }
/* 336:    */   
/* 337:    */   final ChannelFactory<? extends C> channelFactory()
/* 338:    */   {
/* 339:424 */     return this.channelFactory;
/* 340:    */   }
/* 341:    */   
/* 342:    */   final ChannelHandler handler()
/* 343:    */   {
/* 344:428 */     return this.handler;
/* 345:    */   }
/* 346:    */   
/* 347:    */   final Map<ChannelOption<?>, Object> options()
/* 348:    */   {
/* 349:432 */     return copiedMap(this.options);
/* 350:    */   }
/* 351:    */   
/* 352:    */   final Map<AttributeKey<?>, Object> attrs()
/* 353:    */   {
/* 354:436 */     return copiedMap(this.attrs);
/* 355:    */   }
/* 356:    */   
/* 357:    */   static void setChannelOptions(Channel channel, Map<ChannelOption<?>, Object> options, InternalLogger logger)
/* 358:    */   {
/* 359:441 */     for (Map.Entry<ChannelOption<?>, Object> e : options.entrySet()) {
/* 360:442 */       setChannelOption(channel, (ChannelOption)e.getKey(), e.getValue(), logger);
/* 361:    */     }
/* 362:    */   }
/* 363:    */   
/* 364:    */   static void setChannelOptions(Channel channel, Map.Entry<ChannelOption<?>, Object>[] options, InternalLogger logger)
/* 365:    */   {
/* 366:448 */     for (Map.Entry<ChannelOption<?>, Object> e : options) {
/* 367:449 */       setChannelOption(channel, (ChannelOption)e.getKey(), e.getValue(), logger);
/* 368:    */     }
/* 369:    */   }
/* 370:    */   
/* 371:    */   private static void setChannelOption(Channel channel, ChannelOption<?> option, Object value, InternalLogger logger)
/* 372:    */   {
/* 373:    */     try
/* 374:    */     {
/* 375:457 */       if (!channel.config().setOption(option, value)) {
/* 376:458 */         logger.warn("Unknown channel option '{}' for channel '{}'", option, channel);
/* 377:    */       }
/* 378:    */     }
/* 379:    */     catch (Throwable t)
/* 380:    */     {
/* 381:461 */       logger.warn("Failed to set channel option '{}' with value '{}' for channel '{}'", new Object[] { option, value, channel, t });
/* 382:    */     }
/* 383:    */   }
/* 384:    */   
/* 385:    */   public String toString()
/* 386:    */   {
/* 387:470 */     StringBuilder buf = new StringBuilder().append(StringUtil.simpleClassName(this)).append('(').append(config()).append(')');
/* 388:471 */     return buf.toString();
/* 389:    */   }
/* 390:    */   
/* 391:    */   static final class PendingRegistrationPromise
/* 392:    */     extends DefaultChannelPromise
/* 393:    */   {
/* 394:    */     private volatile boolean registered;
/* 395:    */     
/* 396:    */     PendingRegistrationPromise(Channel channel)
/* 397:    */     {
/* 398:481 */       super();
/* 399:    */     }
/* 400:    */     
/* 401:    */     void registered()
/* 402:    */     {
/* 403:485 */       this.registered = true;
/* 404:    */     }
/* 405:    */     
/* 406:    */     protected EventExecutor executor()
/* 407:    */     {
/* 408:490 */       if (this.registered) {
/* 409:494 */         return super.executor();
/* 410:    */       }
/* 411:497 */       return GlobalEventExecutor.INSTANCE;
/* 412:    */     }
/* 413:    */   }
/* 414:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.bootstrap.AbstractBootstrap
 * JD-Core Version:    0.7.0.1
 */