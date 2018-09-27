/*    1:     */ package io.netty.channel;
/*    2:     */ 
/*    3:     */ import io.netty.util.ReferenceCountUtil;
/*    4:     */ import io.netty.util.ResourceLeakDetector;
/*    5:     */ import io.netty.util.concurrent.EventExecutor;
/*    6:     */ import io.netty.util.concurrent.EventExecutorGroup;
/*    7:     */ import io.netty.util.concurrent.FastThreadLocal;
/*    8:     */ import io.netty.util.internal.ObjectUtil;
/*    9:     */ import io.netty.util.internal.StringUtil;
/*   10:     */ import io.netty.util.internal.logging.InternalLogger;
/*   11:     */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   12:     */ import java.net.SocketAddress;
/*   13:     */ import java.util.ArrayList;
/*   14:     */ import java.util.IdentityHashMap;
/*   15:     */ import java.util.Iterator;
/*   16:     */ import java.util.LinkedHashMap;
/*   17:     */ import java.util.List;
/*   18:     */ import java.util.Map;
/*   19:     */ import java.util.Map.Entry;
/*   20:     */ import java.util.NoSuchElementException;
/*   21:     */ import java.util.Set;
/*   22:     */ import java.util.WeakHashMap;
/*   23:     */ import java.util.concurrent.RejectedExecutionException;
/*   24:     */ import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
/*   25:     */ 
/*   26:     */ public class DefaultChannelPipeline
/*   27:     */   implements ChannelPipeline
/*   28:     */ {
/*   29:  48 */   static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultChannelPipeline.class);
/*   30:  50 */   private static final String HEAD_NAME = generateName0(HeadContext.class);
/*   31:  51 */   private static final String TAIL_NAME = generateName0(TailContext.class);
/*   32:  53 */   private static final FastThreadLocal<Map<Class<?>, String>> nameCaches = new FastThreadLocal()
/*   33:     */   {
/*   34:     */     protected Map<Class<?>, String> initialValue()
/*   35:     */       throws Exception
/*   36:     */     {
/*   37:  57 */       return new WeakHashMap();
/*   38:     */     }
/*   39:     */   };
/*   40:  62 */   private static final AtomicReferenceFieldUpdater<DefaultChannelPipeline, MessageSizeEstimator.Handle> ESTIMATOR = AtomicReferenceFieldUpdater.newUpdater(DefaultChannelPipeline.class, MessageSizeEstimator.Handle.class, "estimatorHandle");
/*   41:     */   final AbstractChannelHandlerContext head;
/*   42:     */   final AbstractChannelHandlerContext tail;
/*   43:     */   private final Channel channel;
/*   44:     */   private final ChannelFuture succeededFuture;
/*   45:     */   private final VoidChannelPromise voidPromise;
/*   46:  70 */   private final boolean touch = ResourceLeakDetector.isEnabled();
/*   47:     */   private Map<EventExecutorGroup, EventExecutor> childExecutors;
/*   48:     */   private volatile MessageSizeEstimator.Handle estimatorHandle;
/*   49:  74 */   private boolean firstRegistration = true;
/*   50:     */   private PendingHandlerCallback pendingHandlerCallbackHead;
/*   51:     */   private boolean registered;
/*   52:     */   
/*   53:     */   protected DefaultChannelPipeline(Channel channel)
/*   54:     */   {
/*   55:  93 */     this.channel = ((Channel)ObjectUtil.checkNotNull(channel, "channel"));
/*   56:  94 */     this.succeededFuture = new SucceededChannelFuture(channel, null);
/*   57:  95 */     this.voidPromise = new VoidChannelPromise(channel, true);
/*   58:     */     
/*   59:  97 */     this.tail = new TailContext(this);
/*   60:  98 */     this.head = new HeadContext(this);
/*   61:     */     
/*   62: 100 */     this.head.next = this.tail;
/*   63: 101 */     this.tail.prev = this.head;
/*   64:     */   }
/*   65:     */   
/*   66:     */   final MessageSizeEstimator.Handle estimatorHandle()
/*   67:     */   {
/*   68: 105 */     MessageSizeEstimator.Handle handle = this.estimatorHandle;
/*   69: 106 */     if (handle == null)
/*   70:     */     {
/*   71: 107 */       handle = this.channel.config().getMessageSizeEstimator().newHandle();
/*   72: 108 */       if (!ESTIMATOR.compareAndSet(this, null, handle)) {
/*   73: 109 */         handle = this.estimatorHandle;
/*   74:     */       }
/*   75:     */     }
/*   76: 112 */     return handle;
/*   77:     */   }
/*   78:     */   
/*   79:     */   final Object touch(Object msg, AbstractChannelHandlerContext next)
/*   80:     */   {
/*   81: 116 */     return this.touch ? ReferenceCountUtil.touch(msg, next) : msg;
/*   82:     */   }
/*   83:     */   
/*   84:     */   private AbstractChannelHandlerContext newContext(EventExecutorGroup group, String name, ChannelHandler handler)
/*   85:     */   {
/*   86: 120 */     return new DefaultChannelHandlerContext(this, childExecutor(group), name, handler);
/*   87:     */   }
/*   88:     */   
/*   89:     */   private EventExecutor childExecutor(EventExecutorGroup group)
/*   90:     */   {
/*   91: 124 */     if (group == null) {
/*   92: 125 */       return null;
/*   93:     */     }
/*   94: 127 */     Boolean pinEventExecutor = (Boolean)this.channel.config().getOption(ChannelOption.SINGLE_EVENTEXECUTOR_PER_GROUP);
/*   95: 128 */     if ((pinEventExecutor != null) && (!pinEventExecutor.booleanValue())) {
/*   96: 129 */       return group.next();
/*   97:     */     }
/*   98: 131 */     Map<EventExecutorGroup, EventExecutor> childExecutors = this.childExecutors;
/*   99: 132 */     if (childExecutors == null) {
/*  100: 134 */       childExecutors = this.childExecutors = new IdentityHashMap(4);
/*  101:     */     }
/*  102: 138 */     EventExecutor childExecutor = (EventExecutor)childExecutors.get(group);
/*  103: 139 */     if (childExecutor == null)
/*  104:     */     {
/*  105: 140 */       childExecutor = group.next();
/*  106: 141 */       childExecutors.put(group, childExecutor);
/*  107:     */     }
/*  108: 143 */     return childExecutor;
/*  109:     */   }
/*  110:     */   
/*  111:     */   public final Channel channel()
/*  112:     */   {
/*  113: 147 */     return this.channel;
/*  114:     */   }
/*  115:     */   
/*  116:     */   public final ChannelPipeline addFirst(String name, ChannelHandler handler)
/*  117:     */   {
/*  118: 152 */     return addFirst(null, name, handler);
/*  119:     */   }
/*  120:     */   
/*  121:     */   public final ChannelPipeline addFirst(EventExecutorGroup group, String name, ChannelHandler handler)
/*  122:     */   {
/*  123: 158 */     synchronized (this)
/*  124:     */     {
/*  125: 159 */       checkMultiplicity(handler);
/*  126: 160 */       name = filterName(name, handler);
/*  127:     */       
/*  128: 162 */       final AbstractChannelHandlerContext newCtx = newContext(group, name, handler);
/*  129:     */       
/*  130: 164 */       addFirst0(newCtx);
/*  131: 169 */       if (!this.registered)
/*  132:     */       {
/*  133: 170 */         newCtx.setAddPending();
/*  134: 171 */         callHandlerCallbackLater(newCtx, true);
/*  135: 172 */         return this;
/*  136:     */       }
/*  137: 175 */       EventExecutor executor = newCtx.executor();
/*  138: 176 */       if (!executor.inEventLoop())
/*  139:     */       {
/*  140: 177 */         newCtx.setAddPending();
/*  141: 178 */         executor.execute(new Runnable()
/*  142:     */         {
/*  143:     */           public void run()
/*  144:     */           {
/*  145: 181 */             DefaultChannelPipeline.this.callHandlerAdded0(newCtx);
/*  146:     */           }
/*  147: 183 */         });
/*  148: 184 */         return this;
/*  149:     */       }
/*  150:     */     }
/*  151:     */     AbstractChannelHandlerContext newCtx;
/*  152: 187 */     callHandlerAdded0(newCtx);
/*  153: 188 */     return this;
/*  154:     */   }
/*  155:     */   
/*  156:     */   private void addFirst0(AbstractChannelHandlerContext newCtx)
/*  157:     */   {
/*  158: 192 */     AbstractChannelHandlerContext nextCtx = this.head.next;
/*  159: 193 */     newCtx.prev = this.head;
/*  160: 194 */     newCtx.next = nextCtx;
/*  161: 195 */     this.head.next = newCtx;
/*  162: 196 */     nextCtx.prev = newCtx;
/*  163:     */   }
/*  164:     */   
/*  165:     */   public final ChannelPipeline addLast(String name, ChannelHandler handler)
/*  166:     */   {
/*  167: 201 */     return addLast(null, name, handler);
/*  168:     */   }
/*  169:     */   
/*  170:     */   public final ChannelPipeline addLast(EventExecutorGroup group, String name, ChannelHandler handler)
/*  171:     */   {
/*  172: 207 */     synchronized (this)
/*  173:     */     {
/*  174: 208 */       checkMultiplicity(handler);
/*  175:     */       
/*  176: 210 */       final AbstractChannelHandlerContext newCtx = newContext(group, filterName(name, handler), handler);
/*  177:     */       
/*  178: 212 */       addLast0(newCtx);
/*  179: 217 */       if (!this.registered)
/*  180:     */       {
/*  181: 218 */         newCtx.setAddPending();
/*  182: 219 */         callHandlerCallbackLater(newCtx, true);
/*  183: 220 */         return this;
/*  184:     */       }
/*  185: 223 */       EventExecutor executor = newCtx.executor();
/*  186: 224 */       if (!executor.inEventLoop())
/*  187:     */       {
/*  188: 225 */         newCtx.setAddPending();
/*  189: 226 */         executor.execute(new Runnable()
/*  190:     */         {
/*  191:     */           public void run()
/*  192:     */           {
/*  193: 229 */             DefaultChannelPipeline.this.callHandlerAdded0(newCtx);
/*  194:     */           }
/*  195: 231 */         });
/*  196: 232 */         return this;
/*  197:     */       }
/*  198:     */     }
/*  199:     */     AbstractChannelHandlerContext newCtx;
/*  200: 235 */     callHandlerAdded0(newCtx);
/*  201: 236 */     return this;
/*  202:     */   }
/*  203:     */   
/*  204:     */   private void addLast0(AbstractChannelHandlerContext newCtx)
/*  205:     */   {
/*  206: 240 */     AbstractChannelHandlerContext prev = this.tail.prev;
/*  207: 241 */     newCtx.prev = prev;
/*  208: 242 */     newCtx.next = this.tail;
/*  209: 243 */     prev.next = newCtx;
/*  210: 244 */     this.tail.prev = newCtx;
/*  211:     */   }
/*  212:     */   
/*  213:     */   public final ChannelPipeline addBefore(String baseName, String name, ChannelHandler handler)
/*  214:     */   {
/*  215: 249 */     return addBefore(null, baseName, name, handler);
/*  216:     */   }
/*  217:     */   
/*  218:     */   public final ChannelPipeline addBefore(EventExecutorGroup group, String baseName, String name, ChannelHandler handler)
/*  219:     */   {
/*  220: 257 */     synchronized (this)
/*  221:     */     {
/*  222: 258 */       checkMultiplicity(handler);
/*  223: 259 */       name = filterName(name, handler);
/*  224: 260 */       AbstractChannelHandlerContext ctx = getContextOrDie(baseName);
/*  225:     */       
/*  226: 262 */       final AbstractChannelHandlerContext newCtx = newContext(group, name, handler);
/*  227:     */       
/*  228: 264 */       addBefore0(ctx, newCtx);
/*  229: 269 */       if (!this.registered)
/*  230:     */       {
/*  231: 270 */         newCtx.setAddPending();
/*  232: 271 */         callHandlerCallbackLater(newCtx, true);
/*  233: 272 */         return this;
/*  234:     */       }
/*  235: 275 */       EventExecutor executor = newCtx.executor();
/*  236: 276 */       if (!executor.inEventLoop())
/*  237:     */       {
/*  238: 277 */         newCtx.setAddPending();
/*  239: 278 */         executor.execute(new Runnable()
/*  240:     */         {
/*  241:     */           public void run()
/*  242:     */           {
/*  243: 281 */             DefaultChannelPipeline.this.callHandlerAdded0(newCtx);
/*  244:     */           }
/*  245: 283 */         });
/*  246: 284 */         return this;
/*  247:     */       }
/*  248:     */     }
/*  249:     */     AbstractChannelHandlerContext ctx;
/*  250:     */     AbstractChannelHandlerContext newCtx;
/*  251: 287 */     callHandlerAdded0(newCtx);
/*  252: 288 */     return this;
/*  253:     */   }
/*  254:     */   
/*  255:     */   private static void addBefore0(AbstractChannelHandlerContext ctx, AbstractChannelHandlerContext newCtx)
/*  256:     */   {
/*  257: 292 */     newCtx.prev = ctx.prev;
/*  258: 293 */     newCtx.next = ctx;
/*  259: 294 */     ctx.prev.next = newCtx;
/*  260: 295 */     ctx.prev = newCtx;
/*  261:     */   }
/*  262:     */   
/*  263:     */   private String filterName(String name, ChannelHandler handler)
/*  264:     */   {
/*  265: 299 */     if (name == null) {
/*  266: 300 */       return generateName(handler);
/*  267:     */     }
/*  268: 302 */     checkDuplicateName(name);
/*  269: 303 */     return name;
/*  270:     */   }
/*  271:     */   
/*  272:     */   public final ChannelPipeline addAfter(String baseName, String name, ChannelHandler handler)
/*  273:     */   {
/*  274: 308 */     return addAfter(null, baseName, name, handler);
/*  275:     */   }
/*  276:     */   
/*  277:     */   public final ChannelPipeline addAfter(EventExecutorGroup group, String baseName, String name, ChannelHandler handler)
/*  278:     */   {
/*  279: 317 */     synchronized (this)
/*  280:     */     {
/*  281: 318 */       checkMultiplicity(handler);
/*  282: 319 */       name = filterName(name, handler);
/*  283: 320 */       AbstractChannelHandlerContext ctx = getContextOrDie(baseName);
/*  284:     */       
/*  285: 322 */       final AbstractChannelHandlerContext newCtx = newContext(group, name, handler);
/*  286:     */       
/*  287: 324 */       addAfter0(ctx, newCtx);
/*  288: 329 */       if (!this.registered)
/*  289:     */       {
/*  290: 330 */         newCtx.setAddPending();
/*  291: 331 */         callHandlerCallbackLater(newCtx, true);
/*  292: 332 */         return this;
/*  293:     */       }
/*  294: 334 */       EventExecutor executor = newCtx.executor();
/*  295: 335 */       if (!executor.inEventLoop())
/*  296:     */       {
/*  297: 336 */         newCtx.setAddPending();
/*  298: 337 */         executor.execute(new Runnable()
/*  299:     */         {
/*  300:     */           public void run()
/*  301:     */           {
/*  302: 340 */             DefaultChannelPipeline.this.callHandlerAdded0(newCtx);
/*  303:     */           }
/*  304: 342 */         });
/*  305: 343 */         return this;
/*  306:     */       }
/*  307:     */     }
/*  308:     */     AbstractChannelHandlerContext ctx;
/*  309:     */     AbstractChannelHandlerContext newCtx;
/*  310: 346 */     callHandlerAdded0(newCtx);
/*  311: 347 */     return this;
/*  312:     */   }
/*  313:     */   
/*  314:     */   private static void addAfter0(AbstractChannelHandlerContext ctx, AbstractChannelHandlerContext newCtx)
/*  315:     */   {
/*  316: 351 */     newCtx.prev = ctx;
/*  317: 352 */     newCtx.next = ctx.next;
/*  318: 353 */     ctx.next.prev = newCtx;
/*  319: 354 */     ctx.next = newCtx;
/*  320:     */   }
/*  321:     */   
/*  322:     */   public final ChannelPipeline addFirst(ChannelHandler... handlers)
/*  323:     */   {
/*  324: 359 */     return addFirst(null, handlers);
/*  325:     */   }
/*  326:     */   
/*  327:     */   public final ChannelPipeline addFirst(EventExecutorGroup executor, ChannelHandler... handlers)
/*  328:     */   {
/*  329: 364 */     if (handlers == null) {
/*  330: 365 */       throw new NullPointerException("handlers");
/*  331:     */     }
/*  332: 367 */     if ((handlers.length == 0) || (handlers[0] == null)) {
/*  333: 368 */       return this;
/*  334:     */     }
/*  335: 372 */     for (int size = 1; size < handlers.length; size++) {
/*  336: 373 */       if (handlers[size] == null) {
/*  337:     */         break;
/*  338:     */       }
/*  339:     */     }
/*  340: 378 */     for (int i = size - 1; i >= 0; i--)
/*  341:     */     {
/*  342: 379 */       ChannelHandler h = handlers[i];
/*  343: 380 */       addFirst(executor, null, h);
/*  344:     */     }
/*  345: 383 */     return this;
/*  346:     */   }
/*  347:     */   
/*  348:     */   public final ChannelPipeline addLast(ChannelHandler... handlers)
/*  349:     */   {
/*  350: 388 */     return addLast(null, handlers);
/*  351:     */   }
/*  352:     */   
/*  353:     */   public final ChannelPipeline addLast(EventExecutorGroup executor, ChannelHandler... handlers)
/*  354:     */   {
/*  355: 393 */     if (handlers == null) {
/*  356: 394 */       throw new NullPointerException("handlers");
/*  357:     */     }
/*  358: 397 */     for (ChannelHandler h : handlers)
/*  359:     */     {
/*  360: 398 */       if (h == null) {
/*  361:     */         break;
/*  362:     */       }
/*  363: 401 */       addLast(executor, null, h);
/*  364:     */     }
/*  365: 404 */     return this;
/*  366:     */   }
/*  367:     */   
/*  368:     */   private String generateName(ChannelHandler handler)
/*  369:     */   {
/*  370: 408 */     Map<Class<?>, String> cache = (Map)nameCaches.get();
/*  371: 409 */     Class<?> handlerType = handler.getClass();
/*  372: 410 */     String name = (String)cache.get(handlerType);
/*  373: 411 */     if (name == null)
/*  374:     */     {
/*  375: 412 */       name = generateName0(handlerType);
/*  376: 413 */       cache.put(handlerType, name);
/*  377:     */     }
/*  378: 418 */     if (context0(name) != null)
/*  379:     */     {
/*  380: 419 */       String baseName = name.substring(0, name.length() - 1);
/*  381: 420 */       for (int i = 1;; i++)
/*  382:     */       {
/*  383: 421 */         String newName = baseName + i;
/*  384: 422 */         if (context0(newName) == null)
/*  385:     */         {
/*  386: 423 */           name = newName;
/*  387: 424 */           break;
/*  388:     */         }
/*  389:     */       }
/*  390:     */     }
/*  391: 428 */     return name;
/*  392:     */   }
/*  393:     */   
/*  394:     */   private static String generateName0(Class<?> handlerType)
/*  395:     */   {
/*  396: 432 */     return StringUtil.simpleClassName(handlerType) + "#0";
/*  397:     */   }
/*  398:     */   
/*  399:     */   public final ChannelPipeline remove(ChannelHandler handler)
/*  400:     */   {
/*  401: 437 */     remove(getContextOrDie(handler));
/*  402: 438 */     return this;
/*  403:     */   }
/*  404:     */   
/*  405:     */   public final ChannelHandler remove(String name)
/*  406:     */   {
/*  407: 443 */     return remove(getContextOrDie(name)).handler();
/*  408:     */   }
/*  409:     */   
/*  410:     */   public final <T extends ChannelHandler> T remove(Class<T> handlerType)
/*  411:     */   {
/*  412: 449 */     return remove(getContextOrDie(handlerType)).handler();
/*  413:     */   }
/*  414:     */   
/*  415:     */   private AbstractChannelHandlerContext remove(final AbstractChannelHandlerContext ctx)
/*  416:     */   {
/*  417: 453 */     assert ((ctx != this.head) && (ctx != this.tail));
/*  418: 455 */     synchronized (this)
/*  419:     */     {
/*  420: 456 */       remove0(ctx);
/*  421: 461 */       if (!this.registered)
/*  422:     */       {
/*  423: 462 */         callHandlerCallbackLater(ctx, false);
/*  424: 463 */         return ctx;
/*  425:     */       }
/*  426: 466 */       EventExecutor executor = ctx.executor();
/*  427: 467 */       if (!executor.inEventLoop())
/*  428:     */       {
/*  429: 468 */         executor.execute(new Runnable()
/*  430:     */         {
/*  431:     */           public void run()
/*  432:     */           {
/*  433: 471 */             DefaultChannelPipeline.this.callHandlerRemoved0(ctx);
/*  434:     */           }
/*  435: 473 */         });
/*  436: 474 */         return ctx;
/*  437:     */       }
/*  438:     */     }
/*  439: 477 */     callHandlerRemoved0(ctx);
/*  440: 478 */     return ctx;
/*  441:     */   }
/*  442:     */   
/*  443:     */   private static void remove0(AbstractChannelHandlerContext ctx)
/*  444:     */   {
/*  445: 482 */     AbstractChannelHandlerContext prev = ctx.prev;
/*  446: 483 */     AbstractChannelHandlerContext next = ctx.next;
/*  447: 484 */     prev.next = next;
/*  448: 485 */     next.prev = prev;
/*  449:     */   }
/*  450:     */   
/*  451:     */   public final ChannelHandler removeFirst()
/*  452:     */   {
/*  453: 490 */     if (this.head.next == this.tail) {
/*  454: 491 */       throw new NoSuchElementException();
/*  455:     */     }
/*  456: 493 */     return remove(this.head.next).handler();
/*  457:     */   }
/*  458:     */   
/*  459:     */   public final ChannelHandler removeLast()
/*  460:     */   {
/*  461: 498 */     if (this.head.next == this.tail) {
/*  462: 499 */       throw new NoSuchElementException();
/*  463:     */     }
/*  464: 501 */     return remove(this.tail.prev).handler();
/*  465:     */   }
/*  466:     */   
/*  467:     */   public final ChannelPipeline replace(ChannelHandler oldHandler, String newName, ChannelHandler newHandler)
/*  468:     */   {
/*  469: 506 */     replace(getContextOrDie(oldHandler), newName, newHandler);
/*  470: 507 */     return this;
/*  471:     */   }
/*  472:     */   
/*  473:     */   public final ChannelHandler replace(String oldName, String newName, ChannelHandler newHandler)
/*  474:     */   {
/*  475: 512 */     return replace(getContextOrDie(oldName), newName, newHandler);
/*  476:     */   }
/*  477:     */   
/*  478:     */   public final <T extends ChannelHandler> T replace(Class<T> oldHandlerType, String newName, ChannelHandler newHandler)
/*  479:     */   {
/*  480: 519 */     return replace(getContextOrDie(oldHandlerType), newName, newHandler);
/*  481:     */   }
/*  482:     */   
/*  483:     */   private ChannelHandler replace(final AbstractChannelHandlerContext ctx, String newName, ChannelHandler newHandler)
/*  484:     */   {
/*  485: 524 */     assert ((ctx != this.head) && (ctx != this.tail));
/*  486: 527 */     synchronized (this)
/*  487:     */     {
/*  488: 528 */       checkMultiplicity(newHandler);
/*  489: 529 */       if (newName == null)
/*  490:     */       {
/*  491: 530 */         newName = generateName(newHandler);
/*  492:     */       }
/*  493:     */       else
/*  494:     */       {
/*  495: 532 */         boolean sameName = ctx.name().equals(newName);
/*  496: 533 */         if (!sameName) {
/*  497: 534 */           checkDuplicateName(newName);
/*  498:     */         }
/*  499:     */       }
/*  500: 538 */       final AbstractChannelHandlerContext newCtx = newContext(ctx.executor, newName, newHandler);
/*  501:     */       
/*  502: 540 */       replace0(ctx, newCtx);
/*  503: 546 */       if (!this.registered)
/*  504:     */       {
/*  505: 547 */         callHandlerCallbackLater(newCtx, true);
/*  506: 548 */         callHandlerCallbackLater(ctx, false);
/*  507: 549 */         return ctx.handler();
/*  508:     */       }
/*  509: 551 */       EventExecutor executor = ctx.executor();
/*  510: 552 */       if (!executor.inEventLoop())
/*  511:     */       {
/*  512: 553 */         executor.execute(new Runnable()
/*  513:     */         {
/*  514:     */           public void run()
/*  515:     */           {
/*  516: 559 */             DefaultChannelPipeline.this.callHandlerAdded0(newCtx);
/*  517: 560 */             DefaultChannelPipeline.this.callHandlerRemoved0(ctx);
/*  518:     */           }
/*  519: 562 */         });
/*  520: 563 */         return ctx.handler();
/*  521:     */       }
/*  522:     */     }
/*  523:     */     AbstractChannelHandlerContext newCtx;
/*  524: 569 */     callHandlerAdded0(newCtx);
/*  525: 570 */     callHandlerRemoved0(ctx);
/*  526: 571 */     return ctx.handler();
/*  527:     */   }
/*  528:     */   
/*  529:     */   private static void replace0(AbstractChannelHandlerContext oldCtx, AbstractChannelHandlerContext newCtx)
/*  530:     */   {
/*  531: 575 */     AbstractChannelHandlerContext prev = oldCtx.prev;
/*  532: 576 */     AbstractChannelHandlerContext next = oldCtx.next;
/*  533: 577 */     newCtx.prev = prev;
/*  534: 578 */     newCtx.next = next;
/*  535:     */     
/*  536:     */ 
/*  537:     */ 
/*  538:     */ 
/*  539:     */ 
/*  540: 584 */     prev.next = newCtx;
/*  541: 585 */     next.prev = newCtx;
/*  542:     */     
/*  543:     */ 
/*  544: 588 */     oldCtx.prev = newCtx;
/*  545: 589 */     oldCtx.next = newCtx;
/*  546:     */   }
/*  547:     */   
/*  548:     */   private static void checkMultiplicity(ChannelHandler handler)
/*  549:     */   {
/*  550: 593 */     if ((handler instanceof ChannelHandlerAdapter))
/*  551:     */     {
/*  552: 594 */       ChannelHandlerAdapter h = (ChannelHandlerAdapter)handler;
/*  553: 595 */       if ((!h.isSharable()) && (h.added)) {
/*  554: 597 */         throw new ChannelPipelineException(h.getClass().getName() + " is not a @Sharable handler, so can't be added or removed multiple times.");
/*  555:     */       }
/*  556: 600 */       h.added = true;
/*  557:     */     }
/*  558:     */   }
/*  559:     */   
/*  560:     */   private void callHandlerAdded0(AbstractChannelHandlerContext ctx)
/*  561:     */   {
/*  562:     */     try
/*  563:     */     {
/*  564: 606 */       ctx.handler().handlerAdded(ctx);
/*  565: 607 */       ctx.setAddComplete();
/*  566:     */     }
/*  567:     */     catch (Throwable t)
/*  568:     */     {
/*  569: 609 */       boolean removed = false;
/*  570:     */       try
/*  571:     */       {
/*  572: 611 */         remove0(ctx);
/*  573:     */         try
/*  574:     */         {
/*  575: 613 */           ctx.handler().handlerRemoved(ctx);
/*  576:     */         }
/*  577:     */         finally
/*  578:     */         {
/*  579: 615 */           ctx.setRemoved();
/*  580:     */         }
/*  581: 617 */         removed = true;
/*  582:     */       }
/*  583:     */       catch (Throwable t2)
/*  584:     */       {
/*  585: 619 */         if (logger.isWarnEnabled()) {
/*  586: 620 */           logger.warn("Failed to remove a handler: " + ctx.name(), t2);
/*  587:     */         }
/*  588:     */       }
/*  589: 624 */       if (removed) {
/*  590: 625 */         fireExceptionCaught(new ChannelPipelineException(ctx
/*  591: 626 */           .handler().getClass().getName() + ".handlerAdded() has thrown an exception; removed.", t));
/*  592:     */       } else {
/*  593: 629 */         fireExceptionCaught(new ChannelPipelineException(ctx
/*  594: 630 */           .handler().getClass().getName() + ".handlerAdded() has thrown an exception; also failed to remove.", t));
/*  595:     */       }
/*  596:     */     }
/*  597:     */   }
/*  598:     */   
/*  599:     */   private void callHandlerRemoved0(AbstractChannelHandlerContext ctx)
/*  600:     */   {
/*  601:     */     try
/*  602:     */     {
/*  603:     */       try
/*  604:     */       {
/*  605: 640 */         ctx.handler().handlerRemoved(ctx);
/*  606:     */         
/*  607: 642 */         ctx.setRemoved();
/*  608:     */       }
/*  609:     */       finally
/*  610:     */       {
/*  611: 642 */         ctx.setRemoved();
/*  612:     */       }
/*  613: 648 */       return;
/*  614:     */     }
/*  615:     */     catch (Throwable t)
/*  616:     */     {
/*  617: 645 */       fireExceptionCaught(new ChannelPipelineException(ctx
/*  618: 646 */         .handler().getClass().getName() + ".handlerRemoved() has thrown an exception.", t));
/*  619:     */     }
/*  620:     */   }
/*  621:     */   
/*  622:     */   final void invokeHandlerAddedIfNeeded()
/*  623:     */   {
/*  624: 651 */     assert (this.channel.eventLoop().inEventLoop());
/*  625: 652 */     if (this.firstRegistration)
/*  626:     */     {
/*  627: 653 */       this.firstRegistration = false;
/*  628:     */       
/*  629:     */ 
/*  630: 656 */       callHandlerAddedForAllHandlers();
/*  631:     */     }
/*  632:     */   }
/*  633:     */   
/*  634:     */   public final ChannelHandler first()
/*  635:     */   {
/*  636: 662 */     ChannelHandlerContext first = firstContext();
/*  637: 663 */     if (first == null) {
/*  638: 664 */       return null;
/*  639:     */     }
/*  640: 666 */     return first.handler();
/*  641:     */   }
/*  642:     */   
/*  643:     */   public final ChannelHandlerContext firstContext()
/*  644:     */   {
/*  645: 671 */     AbstractChannelHandlerContext first = this.head.next;
/*  646: 672 */     if (first == this.tail) {
/*  647: 673 */       return null;
/*  648:     */     }
/*  649: 675 */     return this.head.next;
/*  650:     */   }
/*  651:     */   
/*  652:     */   public final ChannelHandler last()
/*  653:     */   {
/*  654: 680 */     AbstractChannelHandlerContext last = this.tail.prev;
/*  655: 681 */     if (last == this.head) {
/*  656: 682 */       return null;
/*  657:     */     }
/*  658: 684 */     return last.handler();
/*  659:     */   }
/*  660:     */   
/*  661:     */   public final ChannelHandlerContext lastContext()
/*  662:     */   {
/*  663: 689 */     AbstractChannelHandlerContext last = this.tail.prev;
/*  664: 690 */     if (last == this.head) {
/*  665: 691 */       return null;
/*  666:     */     }
/*  667: 693 */     return last;
/*  668:     */   }
/*  669:     */   
/*  670:     */   public final ChannelHandler get(String name)
/*  671:     */   {
/*  672: 698 */     ChannelHandlerContext ctx = context(name);
/*  673: 699 */     if (ctx == null) {
/*  674: 700 */       return null;
/*  675:     */     }
/*  676: 702 */     return ctx.handler();
/*  677:     */   }
/*  678:     */   
/*  679:     */   public final <T extends ChannelHandler> T get(Class<T> handlerType)
/*  680:     */   {
/*  681: 709 */     ChannelHandlerContext ctx = context(handlerType);
/*  682: 710 */     if (ctx == null) {
/*  683: 711 */       return null;
/*  684:     */     }
/*  685: 713 */     return ctx.handler();
/*  686:     */   }
/*  687:     */   
/*  688:     */   public final ChannelHandlerContext context(String name)
/*  689:     */   {
/*  690: 719 */     if (name == null) {
/*  691: 720 */       throw new NullPointerException("name");
/*  692:     */     }
/*  693: 723 */     return context0(name);
/*  694:     */   }
/*  695:     */   
/*  696:     */   public final ChannelHandlerContext context(ChannelHandler handler)
/*  697:     */   {
/*  698: 728 */     if (handler == null) {
/*  699: 729 */       throw new NullPointerException("handler");
/*  700:     */     }
/*  701: 732 */     AbstractChannelHandlerContext ctx = this.head.next;
/*  702:     */     for (;;)
/*  703:     */     {
/*  704: 735 */       if (ctx == null) {
/*  705: 736 */         return null;
/*  706:     */       }
/*  707: 739 */       if (ctx.handler() == handler) {
/*  708: 740 */         return ctx;
/*  709:     */       }
/*  710: 743 */       ctx = ctx.next;
/*  711:     */     }
/*  712:     */   }
/*  713:     */   
/*  714:     */   public final ChannelHandlerContext context(Class<? extends ChannelHandler> handlerType)
/*  715:     */   {
/*  716: 749 */     if (handlerType == null) {
/*  717: 750 */       throw new NullPointerException("handlerType");
/*  718:     */     }
/*  719: 753 */     AbstractChannelHandlerContext ctx = this.head.next;
/*  720:     */     for (;;)
/*  721:     */     {
/*  722: 755 */       if (ctx == null) {
/*  723: 756 */         return null;
/*  724:     */       }
/*  725: 758 */       if (handlerType.isAssignableFrom(ctx.handler().getClass())) {
/*  726: 759 */         return ctx;
/*  727:     */       }
/*  728: 761 */       ctx = ctx.next;
/*  729:     */     }
/*  730:     */   }
/*  731:     */   
/*  732:     */   public final List<String> names()
/*  733:     */   {
/*  734: 767 */     List<String> list = new ArrayList();
/*  735: 768 */     AbstractChannelHandlerContext ctx = this.head.next;
/*  736:     */     for (;;)
/*  737:     */     {
/*  738: 770 */       if (ctx == null) {
/*  739: 771 */         return list;
/*  740:     */       }
/*  741: 773 */       list.add(ctx.name());
/*  742: 774 */       ctx = ctx.next;
/*  743:     */     }
/*  744:     */   }
/*  745:     */   
/*  746:     */   public final Map<String, ChannelHandler> toMap()
/*  747:     */   {
/*  748: 780 */     Map<String, ChannelHandler> map = new LinkedHashMap();
/*  749: 781 */     AbstractChannelHandlerContext ctx = this.head.next;
/*  750:     */     for (;;)
/*  751:     */     {
/*  752: 783 */       if (ctx == this.tail) {
/*  753: 784 */         return map;
/*  754:     */       }
/*  755: 786 */       map.put(ctx.name(), ctx.handler());
/*  756: 787 */       ctx = ctx.next;
/*  757:     */     }
/*  758:     */   }
/*  759:     */   
/*  760:     */   public final Iterator<Map.Entry<String, ChannelHandler>> iterator()
/*  761:     */   {
/*  762: 793 */     return toMap().entrySet().iterator();
/*  763:     */   }
/*  764:     */   
/*  765:     */   public final String toString()
/*  766:     */   {
/*  767: 803 */     StringBuilder buf = new StringBuilder().append(StringUtil.simpleClassName(this)).append('{');
/*  768: 804 */     AbstractChannelHandlerContext ctx = this.head.next;
/*  769: 806 */     while (ctx != this.tail)
/*  770:     */     {
/*  771: 814 */       buf.append('(').append(ctx.name()).append(" = ").append(ctx.handler().getClass().getName()).append(')');
/*  772:     */       
/*  773: 816 */       ctx = ctx.next;
/*  774: 817 */       if (ctx == this.tail) {
/*  775:     */         break;
/*  776:     */       }
/*  777: 821 */       buf.append(", ");
/*  778:     */     }
/*  779: 823 */     buf.append('}');
/*  780: 824 */     return buf.toString();
/*  781:     */   }
/*  782:     */   
/*  783:     */   public final ChannelPipeline fireChannelRegistered()
/*  784:     */   {
/*  785: 829 */     AbstractChannelHandlerContext.invokeChannelRegistered(this.head);
/*  786: 830 */     return this;
/*  787:     */   }
/*  788:     */   
/*  789:     */   public final ChannelPipeline fireChannelUnregistered()
/*  790:     */   {
/*  791: 835 */     AbstractChannelHandlerContext.invokeChannelUnregistered(this.head);
/*  792: 836 */     return this;
/*  793:     */   }
/*  794:     */   
/*  795:     */   private synchronized void destroy()
/*  796:     */   {
/*  797: 850 */     destroyUp(this.head.next, false);
/*  798:     */   }
/*  799:     */   
/*  800:     */   private void destroyUp(AbstractChannelHandlerContext ctx, boolean inEventLoop)
/*  801:     */   {
/*  802: 854 */     Thread currentThread = Thread.currentThread();
/*  803: 855 */     AbstractChannelHandlerContext tail = this.tail;
/*  804:     */     for (;;)
/*  805:     */     {
/*  806: 857 */       if (ctx == tail)
/*  807:     */       {
/*  808: 858 */         destroyDown(currentThread, tail.prev, inEventLoop);
/*  809: 859 */         break;
/*  810:     */       }
/*  811: 862 */       EventExecutor executor = ctx.executor();
/*  812: 863 */       if ((!inEventLoop) && (!executor.inEventLoop(currentThread)))
/*  813:     */       {
/*  814: 864 */         final AbstractChannelHandlerContext finalCtx = ctx;
/*  815: 865 */         executor.execute(new Runnable()
/*  816:     */         {
/*  817:     */           public void run()
/*  818:     */           {
/*  819: 868 */             DefaultChannelPipeline.this.destroyUp(finalCtx, true);
/*  820:     */           }
/*  821: 870 */         });
/*  822: 871 */         break;
/*  823:     */       }
/*  824: 874 */       ctx = ctx.next;
/*  825: 875 */       inEventLoop = false;
/*  826:     */     }
/*  827:     */   }
/*  828:     */   
/*  829:     */   private void destroyDown(Thread currentThread, AbstractChannelHandlerContext ctx, boolean inEventLoop)
/*  830:     */   {
/*  831: 881 */     AbstractChannelHandlerContext head = this.head;
/*  832: 883 */     while (ctx != head)
/*  833:     */     {
/*  834: 887 */       EventExecutor executor = ctx.executor();
/*  835: 888 */       if ((inEventLoop) || (executor.inEventLoop(currentThread)))
/*  836:     */       {
/*  837: 889 */         synchronized (this)
/*  838:     */         {
/*  839: 890 */           remove0(ctx);
/*  840:     */         }
/*  841: 892 */         callHandlerRemoved0(ctx);
/*  842:     */       }
/*  843:     */       else
/*  844:     */       {
/*  845: 894 */         final AbstractChannelHandlerContext finalCtx = ctx;
/*  846: 895 */         executor.execute(new Runnable()
/*  847:     */         {
/*  848:     */           public void run()
/*  849:     */           {
/*  850: 898 */             DefaultChannelPipeline.this.destroyDown(Thread.currentThread(), finalCtx, true);
/*  851:     */           }
/*  852: 900 */         });
/*  853: 901 */         break;
/*  854:     */       }
/*  855: 904 */       ctx = ctx.prev;
/*  856: 905 */       inEventLoop = false;
/*  857:     */     }
/*  858:     */   }
/*  859:     */   
/*  860:     */   public final ChannelPipeline fireChannelActive()
/*  861:     */   {
/*  862: 911 */     AbstractChannelHandlerContext.invokeChannelActive(this.head);
/*  863: 912 */     return this;
/*  864:     */   }
/*  865:     */   
/*  866:     */   public final ChannelPipeline fireChannelInactive()
/*  867:     */   {
/*  868: 917 */     AbstractChannelHandlerContext.invokeChannelInactive(this.head);
/*  869: 918 */     return this;
/*  870:     */   }
/*  871:     */   
/*  872:     */   public final ChannelPipeline fireExceptionCaught(Throwable cause)
/*  873:     */   {
/*  874: 923 */     AbstractChannelHandlerContext.invokeExceptionCaught(this.head, cause);
/*  875: 924 */     return this;
/*  876:     */   }
/*  877:     */   
/*  878:     */   public final ChannelPipeline fireUserEventTriggered(Object event)
/*  879:     */   {
/*  880: 929 */     AbstractChannelHandlerContext.invokeUserEventTriggered(this.head, event);
/*  881: 930 */     return this;
/*  882:     */   }
/*  883:     */   
/*  884:     */   public final ChannelPipeline fireChannelRead(Object msg)
/*  885:     */   {
/*  886: 935 */     AbstractChannelHandlerContext.invokeChannelRead(this.head, msg);
/*  887: 936 */     return this;
/*  888:     */   }
/*  889:     */   
/*  890:     */   public final ChannelPipeline fireChannelReadComplete()
/*  891:     */   {
/*  892: 941 */     AbstractChannelHandlerContext.invokeChannelReadComplete(this.head);
/*  893: 942 */     return this;
/*  894:     */   }
/*  895:     */   
/*  896:     */   public final ChannelPipeline fireChannelWritabilityChanged()
/*  897:     */   {
/*  898: 947 */     AbstractChannelHandlerContext.invokeChannelWritabilityChanged(this.head);
/*  899: 948 */     return this;
/*  900:     */   }
/*  901:     */   
/*  902:     */   public final ChannelFuture bind(SocketAddress localAddress)
/*  903:     */   {
/*  904: 953 */     return this.tail.bind(localAddress);
/*  905:     */   }
/*  906:     */   
/*  907:     */   public final ChannelFuture connect(SocketAddress remoteAddress)
/*  908:     */   {
/*  909: 958 */     return this.tail.connect(remoteAddress);
/*  910:     */   }
/*  911:     */   
/*  912:     */   public final ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress)
/*  913:     */   {
/*  914: 963 */     return this.tail.connect(remoteAddress, localAddress);
/*  915:     */   }
/*  916:     */   
/*  917:     */   public final ChannelFuture disconnect()
/*  918:     */   {
/*  919: 968 */     return this.tail.disconnect();
/*  920:     */   }
/*  921:     */   
/*  922:     */   public final ChannelFuture close()
/*  923:     */   {
/*  924: 973 */     return this.tail.close();
/*  925:     */   }
/*  926:     */   
/*  927:     */   public final ChannelFuture deregister()
/*  928:     */   {
/*  929: 978 */     return this.tail.deregister();
/*  930:     */   }
/*  931:     */   
/*  932:     */   public final ChannelPipeline flush()
/*  933:     */   {
/*  934: 983 */     this.tail.flush();
/*  935: 984 */     return this;
/*  936:     */   }
/*  937:     */   
/*  938:     */   public final ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise)
/*  939:     */   {
/*  940: 989 */     return this.tail.bind(localAddress, promise);
/*  941:     */   }
/*  942:     */   
/*  943:     */   public final ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise)
/*  944:     */   {
/*  945: 994 */     return this.tail.connect(remoteAddress, promise);
/*  946:     */   }
/*  947:     */   
/*  948:     */   public final ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
/*  949:     */   {
/*  950:1000 */     return this.tail.connect(remoteAddress, localAddress, promise);
/*  951:     */   }
/*  952:     */   
/*  953:     */   public final ChannelFuture disconnect(ChannelPromise promise)
/*  954:     */   {
/*  955:1005 */     return this.tail.disconnect(promise);
/*  956:     */   }
/*  957:     */   
/*  958:     */   public final ChannelFuture close(ChannelPromise promise)
/*  959:     */   {
/*  960:1010 */     return this.tail.close(promise);
/*  961:     */   }
/*  962:     */   
/*  963:     */   public final ChannelFuture deregister(ChannelPromise promise)
/*  964:     */   {
/*  965:1015 */     return this.tail.deregister(promise);
/*  966:     */   }
/*  967:     */   
/*  968:     */   public final ChannelPipeline read()
/*  969:     */   {
/*  970:1020 */     this.tail.read();
/*  971:1021 */     return this;
/*  972:     */   }
/*  973:     */   
/*  974:     */   public final ChannelFuture write(Object msg)
/*  975:     */   {
/*  976:1026 */     return this.tail.write(msg);
/*  977:     */   }
/*  978:     */   
/*  979:     */   public final ChannelFuture write(Object msg, ChannelPromise promise)
/*  980:     */   {
/*  981:1031 */     return this.tail.write(msg, promise);
/*  982:     */   }
/*  983:     */   
/*  984:     */   public final ChannelFuture writeAndFlush(Object msg, ChannelPromise promise)
/*  985:     */   {
/*  986:1036 */     return this.tail.writeAndFlush(msg, promise);
/*  987:     */   }
/*  988:     */   
/*  989:     */   public final ChannelFuture writeAndFlush(Object msg)
/*  990:     */   {
/*  991:1041 */     return this.tail.writeAndFlush(msg);
/*  992:     */   }
/*  993:     */   
/*  994:     */   public final ChannelPromise newPromise()
/*  995:     */   {
/*  996:1046 */     return new DefaultChannelPromise(this.channel);
/*  997:     */   }
/*  998:     */   
/*  999:     */   public final ChannelProgressivePromise newProgressivePromise()
/* 1000:     */   {
/* 1001:1051 */     return new DefaultChannelProgressivePromise(this.channel);
/* 1002:     */   }
/* 1003:     */   
/* 1004:     */   public final ChannelFuture newSucceededFuture()
/* 1005:     */   {
/* 1006:1056 */     return this.succeededFuture;
/* 1007:     */   }
/* 1008:     */   
/* 1009:     */   public final ChannelFuture newFailedFuture(Throwable cause)
/* 1010:     */   {
/* 1011:1061 */     return new FailedChannelFuture(this.channel, null, cause);
/* 1012:     */   }
/* 1013:     */   
/* 1014:     */   public final ChannelPromise voidPromise()
/* 1015:     */   {
/* 1016:1066 */     return this.voidPromise;
/* 1017:     */   }
/* 1018:     */   
/* 1019:     */   private void checkDuplicateName(String name)
/* 1020:     */   {
/* 1021:1070 */     if (context0(name) != null) {
/* 1022:1071 */       throw new IllegalArgumentException("Duplicate handler name: " + name);
/* 1023:     */     }
/* 1024:     */   }
/* 1025:     */   
/* 1026:     */   private AbstractChannelHandlerContext context0(String name)
/* 1027:     */   {
/* 1028:1076 */     AbstractChannelHandlerContext context = this.head.next;
/* 1029:1077 */     while (context != this.tail)
/* 1030:     */     {
/* 1031:1078 */       if (context.name().equals(name)) {
/* 1032:1079 */         return context;
/* 1033:     */       }
/* 1034:1081 */       context = context.next;
/* 1035:     */     }
/* 1036:1083 */     return null;
/* 1037:     */   }
/* 1038:     */   
/* 1039:     */   private AbstractChannelHandlerContext getContextOrDie(String name)
/* 1040:     */   {
/* 1041:1087 */     AbstractChannelHandlerContext ctx = (AbstractChannelHandlerContext)context(name);
/* 1042:1088 */     if (ctx == null) {
/* 1043:1089 */       throw new NoSuchElementException(name);
/* 1044:     */     }
/* 1045:1091 */     return ctx;
/* 1046:     */   }
/* 1047:     */   
/* 1048:     */   private AbstractChannelHandlerContext getContextOrDie(ChannelHandler handler)
/* 1049:     */   {
/* 1050:1096 */     AbstractChannelHandlerContext ctx = (AbstractChannelHandlerContext)context(handler);
/* 1051:1097 */     if (ctx == null) {
/* 1052:1098 */       throw new NoSuchElementException(handler.getClass().getName());
/* 1053:     */     }
/* 1054:1100 */     return ctx;
/* 1055:     */   }
/* 1056:     */   
/* 1057:     */   private AbstractChannelHandlerContext getContextOrDie(Class<? extends ChannelHandler> handlerType)
/* 1058:     */   {
/* 1059:1105 */     AbstractChannelHandlerContext ctx = (AbstractChannelHandlerContext)context(handlerType);
/* 1060:1106 */     if (ctx == null) {
/* 1061:1107 */       throw new NoSuchElementException(handlerType.getName());
/* 1062:     */     }
/* 1063:1109 */     return ctx;
/* 1064:     */   }
/* 1065:     */   
/* 1066:     */   private void callHandlerAddedForAllHandlers()
/* 1067:     */   {
/* 1068:1115 */     synchronized (this)
/* 1069:     */     {
/* 1070:1116 */       assert (!this.registered);
/* 1071:     */       
/* 1072:     */ 
/* 1073:1119 */       this.registered = true;
/* 1074:     */       
/* 1075:1121 */       PendingHandlerCallback pendingHandlerCallbackHead = this.pendingHandlerCallbackHead;
/* 1076:     */       
/* 1077:1123 */       this.pendingHandlerCallbackHead = null;
/* 1078:     */     }
/* 1079:     */     PendingHandlerCallback pendingHandlerCallbackHead;
/* 1080:1129 */     PendingHandlerCallback task = pendingHandlerCallbackHead;
/* 1081:1130 */     while (task != null)
/* 1082:     */     {
/* 1083:1131 */       task.execute();
/* 1084:1132 */       task = task.next;
/* 1085:     */     }
/* 1086:     */   }
/* 1087:     */   
/* 1088:     */   private void callHandlerCallbackLater(AbstractChannelHandlerContext ctx, boolean added)
/* 1089:     */   {
/* 1090:1137 */     assert (!this.registered);
/* 1091:     */     
/* 1092:1139 */     PendingHandlerCallback task = added ? new PendingHandlerAddedTask(ctx) : new PendingHandlerRemovedTask(ctx);
/* 1093:1140 */     PendingHandlerCallback pending = this.pendingHandlerCallbackHead;
/* 1094:1141 */     if (pending == null)
/* 1095:     */     {
/* 1096:1142 */       this.pendingHandlerCallbackHead = task;
/* 1097:     */     }
/* 1098:     */     else
/* 1099:     */     {
/* 1100:1145 */       while (pending.next != null) {
/* 1101:1146 */         pending = pending.next;
/* 1102:     */       }
/* 1103:1148 */       pending.next = task;
/* 1104:     */     }
/* 1105:     */   }
/* 1106:     */   
/* 1107:     */   protected void onUnhandledInboundException(Throwable cause)
/* 1108:     */   {
/* 1109:     */     try
/* 1110:     */     {
/* 1111:1158 */       logger.warn("An exceptionCaught() event was fired, and it reached at the tail of the pipeline. It usually means the last handler in the pipeline did not handle the exception.", cause);
/* 1112:     */       
/* 1113:     */ 
/* 1114:     */ 
/* 1115:     */ 
/* 1116:1163 */       ReferenceCountUtil.release(cause);
/* 1117:     */     }
/* 1118:     */     finally
/* 1119:     */     {
/* 1120:1163 */       ReferenceCountUtil.release(cause);
/* 1121:     */     }
/* 1122:     */   }
/* 1123:     */   
/* 1124:     */   protected void onUnhandledInboundMessage(Object msg)
/* 1125:     */   {
/* 1126:     */     try
/* 1127:     */     {
/* 1128:1174 */       logger.debug("Discarded inbound message {} that reached at the tail of the pipeline. Please check your pipeline configuration.", msg);
/* 1129:     */       
/* 1130:     */ 
/* 1131:     */ 
/* 1132:1178 */       ReferenceCountUtil.release(msg);
/* 1133:     */     }
/* 1134:     */     finally
/* 1135:     */     {
/* 1136:1178 */       ReferenceCountUtil.release(msg);
/* 1137:     */     }
/* 1138:     */   }
/* 1139:     */   
/* 1140:     */   protected void incrementPendingOutboundBytes(long size)
/* 1141:     */   {
/* 1142:1184 */     ChannelOutboundBuffer buffer = this.channel.unsafe().outboundBuffer();
/* 1143:1185 */     if (buffer != null) {
/* 1144:1186 */       buffer.incrementPendingOutboundBytes(size);
/* 1145:     */     }
/* 1146:     */   }
/* 1147:     */   
/* 1148:     */   protected void decrementPendingOutboundBytes(long size)
/* 1149:     */   {
/* 1150:1192 */     ChannelOutboundBuffer buffer = this.channel.unsafe().outboundBuffer();
/* 1151:1193 */     if (buffer != null) {
/* 1152:1194 */       buffer.decrementPendingOutboundBytes(size);
/* 1153:     */     }
/* 1154:     */   }
/* 1155:     */   
/* 1156:     */   final class TailContext
/* 1157:     */     extends AbstractChannelHandlerContext
/* 1158:     */     implements ChannelInboundHandler
/* 1159:     */   {
/* 1160:     */     TailContext(DefaultChannelPipeline pipeline)
/* 1161:     */     {
/* 1162:1202 */       super(null, DefaultChannelPipeline.TAIL_NAME, true, false);
/* 1163:1203 */       setAddComplete();
/* 1164:     */     }
/* 1165:     */     
/* 1166:     */     public ChannelHandler handler()
/* 1167:     */     {
/* 1168:1208 */       return this;
/* 1169:     */     }
/* 1170:     */     
/* 1171:     */     public void channelRegistered(ChannelHandlerContext ctx)
/* 1172:     */       throws Exception
/* 1173:     */     {}
/* 1174:     */     
/* 1175:     */     public void channelUnregistered(ChannelHandlerContext ctx)
/* 1176:     */       throws Exception
/* 1177:     */     {}
/* 1178:     */     
/* 1179:     */     public void channelActive(ChannelHandlerContext ctx)
/* 1180:     */       throws Exception
/* 1181:     */     {}
/* 1182:     */     
/* 1183:     */     public void channelInactive(ChannelHandlerContext ctx)
/* 1184:     */       throws Exception
/* 1185:     */     {}
/* 1186:     */     
/* 1187:     */     public void channelWritabilityChanged(ChannelHandlerContext ctx)
/* 1188:     */       throws Exception
/* 1189:     */     {}
/* 1190:     */     
/* 1191:     */     public void handlerAdded(ChannelHandlerContext ctx)
/* 1192:     */       throws Exception
/* 1193:     */     {}
/* 1194:     */     
/* 1195:     */     public void handlerRemoved(ChannelHandlerContext ctx)
/* 1196:     */       throws Exception
/* 1197:     */     {}
/* 1198:     */     
/* 1199:     */     public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
/* 1200:     */       throws Exception
/* 1201:     */     {
/* 1202:1236 */       ReferenceCountUtil.release(evt);
/* 1203:     */     }
/* 1204:     */     
/* 1205:     */     public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
/* 1206:     */       throws Exception
/* 1207:     */     {
/* 1208:1241 */       DefaultChannelPipeline.this.onUnhandledInboundException(cause);
/* 1209:     */     }
/* 1210:     */     
/* 1211:     */     public void channelRead(ChannelHandlerContext ctx, Object msg)
/* 1212:     */       throws Exception
/* 1213:     */     {
/* 1214:1246 */       DefaultChannelPipeline.this.onUnhandledInboundMessage(msg);
/* 1215:     */     }
/* 1216:     */     
/* 1217:     */     public void channelReadComplete(ChannelHandlerContext ctx)
/* 1218:     */       throws Exception
/* 1219:     */     {}
/* 1220:     */   }
/* 1221:     */   
/* 1222:     */   final class HeadContext
/* 1223:     */     extends AbstractChannelHandlerContext
/* 1224:     */     implements ChannelOutboundHandler, ChannelInboundHandler
/* 1225:     */   {
/* 1226:     */     private final Channel.Unsafe unsafe;
/* 1227:     */     
/* 1228:     */     HeadContext(DefaultChannelPipeline pipeline)
/* 1229:     */     {
/* 1230:1259 */       super(null, DefaultChannelPipeline.HEAD_NAME, false, true);
/* 1231:1260 */       this.unsafe = pipeline.channel().unsafe();
/* 1232:1261 */       setAddComplete();
/* 1233:     */     }
/* 1234:     */     
/* 1235:     */     public ChannelHandler handler()
/* 1236:     */     {
/* 1237:1266 */       return this;
/* 1238:     */     }
/* 1239:     */     
/* 1240:     */     public void handlerAdded(ChannelHandlerContext ctx)
/* 1241:     */       throws Exception
/* 1242:     */     {}
/* 1243:     */     
/* 1244:     */     public void handlerRemoved(ChannelHandlerContext ctx)
/* 1245:     */       throws Exception
/* 1246:     */     {}
/* 1247:     */     
/* 1248:     */     public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise)
/* 1249:     */       throws Exception
/* 1250:     */     {
/* 1251:1283 */       this.unsafe.bind(localAddress, promise);
/* 1252:     */     }
/* 1253:     */     
/* 1254:     */     public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
/* 1255:     */       throws Exception
/* 1256:     */     {
/* 1257:1291 */       this.unsafe.connect(remoteAddress, localAddress, promise);
/* 1258:     */     }
/* 1259:     */     
/* 1260:     */     public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise)
/* 1261:     */       throws Exception
/* 1262:     */     {
/* 1263:1296 */       this.unsafe.disconnect(promise);
/* 1264:     */     }
/* 1265:     */     
/* 1266:     */     public void close(ChannelHandlerContext ctx, ChannelPromise promise)
/* 1267:     */       throws Exception
/* 1268:     */     {
/* 1269:1301 */       this.unsafe.close(promise);
/* 1270:     */     }
/* 1271:     */     
/* 1272:     */     public void deregister(ChannelHandlerContext ctx, ChannelPromise promise)
/* 1273:     */       throws Exception
/* 1274:     */     {
/* 1275:1306 */       this.unsafe.deregister(promise);
/* 1276:     */     }
/* 1277:     */     
/* 1278:     */     public void read(ChannelHandlerContext ctx)
/* 1279:     */     {
/* 1280:1311 */       this.unsafe.beginRead();
/* 1281:     */     }
/* 1282:     */     
/* 1283:     */     public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
/* 1284:     */       throws Exception
/* 1285:     */     {
/* 1286:1316 */       this.unsafe.write(msg, promise);
/* 1287:     */     }
/* 1288:     */     
/* 1289:     */     public void flush(ChannelHandlerContext ctx)
/* 1290:     */       throws Exception
/* 1291:     */     {
/* 1292:1321 */       this.unsafe.flush();
/* 1293:     */     }
/* 1294:     */     
/* 1295:     */     public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
/* 1296:     */       throws Exception
/* 1297:     */     {
/* 1298:1326 */       ctx.fireExceptionCaught(cause);
/* 1299:     */     }
/* 1300:     */     
/* 1301:     */     public void channelRegistered(ChannelHandlerContext ctx)
/* 1302:     */       throws Exception
/* 1303:     */     {
/* 1304:1331 */       DefaultChannelPipeline.this.invokeHandlerAddedIfNeeded();
/* 1305:1332 */       ctx.fireChannelRegistered();
/* 1306:     */     }
/* 1307:     */     
/* 1308:     */     public void channelUnregistered(ChannelHandlerContext ctx)
/* 1309:     */       throws Exception
/* 1310:     */     {
/* 1311:1337 */       ctx.fireChannelUnregistered();
/* 1312:1340 */       if (!DefaultChannelPipeline.this.channel.isOpen()) {
/* 1313:1341 */         DefaultChannelPipeline.this.destroy();
/* 1314:     */       }
/* 1315:     */     }
/* 1316:     */     
/* 1317:     */     public void channelActive(ChannelHandlerContext ctx)
/* 1318:     */       throws Exception
/* 1319:     */     {
/* 1320:1347 */       ctx.fireChannelActive();
/* 1321:     */       
/* 1322:1349 */       readIfIsAutoRead();
/* 1323:     */     }
/* 1324:     */     
/* 1325:     */     public void channelInactive(ChannelHandlerContext ctx)
/* 1326:     */       throws Exception
/* 1327:     */     {
/* 1328:1354 */       ctx.fireChannelInactive();
/* 1329:     */     }
/* 1330:     */     
/* 1331:     */     public void channelRead(ChannelHandlerContext ctx, Object msg)
/* 1332:     */       throws Exception
/* 1333:     */     {
/* 1334:1359 */       ctx.fireChannelRead(msg);
/* 1335:     */     }
/* 1336:     */     
/* 1337:     */     public void channelReadComplete(ChannelHandlerContext ctx)
/* 1338:     */       throws Exception
/* 1339:     */     {
/* 1340:1364 */       ctx.fireChannelReadComplete();
/* 1341:     */       
/* 1342:1366 */       readIfIsAutoRead();
/* 1343:     */     }
/* 1344:     */     
/* 1345:     */     private void readIfIsAutoRead()
/* 1346:     */     {
/* 1347:1370 */       if (DefaultChannelPipeline.this.channel.config().isAutoRead()) {
/* 1348:1371 */         DefaultChannelPipeline.this.channel.read();
/* 1349:     */       }
/* 1350:     */     }
/* 1351:     */     
/* 1352:     */     public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
/* 1353:     */       throws Exception
/* 1354:     */     {
/* 1355:1377 */       ctx.fireUserEventTriggered(evt);
/* 1356:     */     }
/* 1357:     */     
/* 1358:     */     public void channelWritabilityChanged(ChannelHandlerContext ctx)
/* 1359:     */       throws Exception
/* 1360:     */     {
/* 1361:1382 */       ctx.fireChannelWritabilityChanged();
/* 1362:     */     }
/* 1363:     */   }
/* 1364:     */   
/* 1365:     */   private static abstract class PendingHandlerCallback
/* 1366:     */     implements Runnable
/* 1367:     */   {
/* 1368:     */     final AbstractChannelHandlerContext ctx;
/* 1369:     */     PendingHandlerCallback next;
/* 1370:     */     
/* 1371:     */     PendingHandlerCallback(AbstractChannelHandlerContext ctx)
/* 1372:     */     {
/* 1373:1391 */       this.ctx = ctx;
/* 1374:     */     }
/* 1375:     */     
/* 1376:     */     abstract void execute();
/* 1377:     */   }
/* 1378:     */   
/* 1379:     */   private final class PendingHandlerAddedTask
/* 1380:     */     extends DefaultChannelPipeline.PendingHandlerCallback
/* 1381:     */   {
/* 1382:     */     PendingHandlerAddedTask(AbstractChannelHandlerContext ctx)
/* 1383:     */     {
/* 1384:1400 */       super();
/* 1385:     */     }
/* 1386:     */     
/* 1387:     */     public void run()
/* 1388:     */     {
/* 1389:1405 */       DefaultChannelPipeline.this.callHandlerAdded0(this.ctx);
/* 1390:     */     }
/* 1391:     */     
/* 1392:     */     void execute()
/* 1393:     */     {
/* 1394:1410 */       EventExecutor executor = this.ctx.executor();
/* 1395:1411 */       if (executor.inEventLoop()) {
/* 1396:1412 */         DefaultChannelPipeline.this.callHandlerAdded0(this.ctx);
/* 1397:     */       } else {
/* 1398:     */         try
/* 1399:     */         {
/* 1400:1415 */           executor.execute(this);
/* 1401:     */         }
/* 1402:     */         catch (RejectedExecutionException e)
/* 1403:     */         {
/* 1404:1417 */           if (DefaultChannelPipeline.logger.isWarnEnabled()) {
/* 1405:1418 */             DefaultChannelPipeline.logger.warn("Can't invoke handlerAdded() as the EventExecutor {} rejected it, removing handler {}.", new Object[] { executor, this.ctx
/* 1406:     */             
/* 1407:1420 */               .name(), e });
/* 1408:     */           }
/* 1409:1422 */           DefaultChannelPipeline.remove0(this.ctx);
/* 1410:1423 */           this.ctx.setRemoved();
/* 1411:     */         }
/* 1412:     */       }
/* 1413:     */     }
/* 1414:     */   }
/* 1415:     */   
/* 1416:     */   private final class PendingHandlerRemovedTask
/* 1417:     */     extends DefaultChannelPipeline.PendingHandlerCallback
/* 1418:     */   {
/* 1419:     */     PendingHandlerRemovedTask(AbstractChannelHandlerContext ctx)
/* 1420:     */     {
/* 1421:1432 */       super();
/* 1422:     */     }
/* 1423:     */     
/* 1424:     */     public void run()
/* 1425:     */     {
/* 1426:1437 */       DefaultChannelPipeline.this.callHandlerRemoved0(this.ctx);
/* 1427:     */     }
/* 1428:     */     
/* 1429:     */     void execute()
/* 1430:     */     {
/* 1431:1442 */       EventExecutor executor = this.ctx.executor();
/* 1432:1443 */       if (executor.inEventLoop()) {
/* 1433:1444 */         DefaultChannelPipeline.this.callHandlerRemoved0(this.ctx);
/* 1434:     */       } else {
/* 1435:     */         try
/* 1436:     */         {
/* 1437:1447 */           executor.execute(this);
/* 1438:     */         }
/* 1439:     */         catch (RejectedExecutionException e)
/* 1440:     */         {
/* 1441:1449 */           if (DefaultChannelPipeline.logger.isWarnEnabled()) {
/* 1442:1450 */             DefaultChannelPipeline.logger.warn("Can't invoke handlerRemoved() as the EventExecutor {} rejected it, removing handler {}.", new Object[] { executor, this.ctx
/* 1443:     */             
/* 1444:1452 */               .name(), e });
/* 1445:     */           }
/* 1446:1455 */           this.ctx.setRemoved();
/* 1447:     */         }
/* 1448:     */       }
/* 1449:     */     }
/* 1450:     */   }
/* 1451:     */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.DefaultChannelPipeline
 * JD-Core Version:    0.7.0.1
 */