/*    1:     */ package io.netty.buffer;
/*    2:     */ 
/*    3:     */ import io.netty.util.internal.EmptyArrays;
/*    4:     */ import io.netty.util.internal.ObjectUtil;
/*    5:     */ import java.io.IOException;
/*    6:     */ import java.io.InputStream;
/*    7:     */ import java.io.OutputStream;
/*    8:     */ import java.nio.ByteBuffer;
/*    9:     */ import java.nio.ByteOrder;
/*   10:     */ import java.nio.channels.FileChannel;
/*   11:     */ import java.nio.channels.GatheringByteChannel;
/*   12:     */ import java.nio.channels.ScatteringByteChannel;
/*   13:     */ import java.util.ArrayList;
/*   14:     */ import java.util.Collection;
/*   15:     */ import java.util.Collections;
/*   16:     */ import java.util.ConcurrentModificationException;
/*   17:     */ import java.util.Iterator;
/*   18:     */ import java.util.List;
/*   19:     */ import java.util.ListIterator;
/*   20:     */ import java.util.NoSuchElementException;
/*   21:     */ 
/*   22:     */ public class CompositeByteBuf
/*   23:     */   extends AbstractReferenceCountedByteBuf
/*   24:     */   implements Iterable<ByteBuf>
/*   25:     */ {
/*   26:  46 */   private static final ByteBuffer EMPTY_NIO_BUFFER = Unpooled.EMPTY_BUFFER.nioBuffer();
/*   27:  47 */   private static final Iterator<ByteBuf> EMPTY_ITERATOR = Collections.emptyList().iterator();
/*   28:     */   private final ByteBufAllocator alloc;
/*   29:     */   private final boolean direct;
/*   30:     */   private final ComponentList components;
/*   31:     */   private final int maxNumComponents;
/*   32:     */   private boolean freed;
/*   33:     */   
/*   34:     */   public CompositeByteBuf(ByteBufAllocator alloc, boolean direct, int maxNumComponents)
/*   35:     */   {
/*   36:  57 */     super(2147483647);
/*   37:  58 */     if (alloc == null) {
/*   38:  59 */       throw new NullPointerException("alloc");
/*   39:     */     }
/*   40:  61 */     this.alloc = alloc;
/*   41:  62 */     this.direct = direct;
/*   42:  63 */     this.maxNumComponents = maxNumComponents;
/*   43:  64 */     this.components = newList(maxNumComponents);
/*   44:     */   }
/*   45:     */   
/*   46:     */   public CompositeByteBuf(ByteBufAllocator alloc, boolean direct, int maxNumComponents, ByteBuf... buffers)
/*   47:     */   {
/*   48:  68 */     this(alloc, direct, maxNumComponents, buffers, 0, buffers.length);
/*   49:     */   }
/*   50:     */   
/*   51:     */   CompositeByteBuf(ByteBufAllocator alloc, boolean direct, int maxNumComponents, ByteBuf[] buffers, int offset, int len)
/*   52:     */   {
/*   53:  73 */     super(2147483647);
/*   54:  74 */     if (alloc == null) {
/*   55:  75 */       throw new NullPointerException("alloc");
/*   56:     */     }
/*   57:  77 */     if (maxNumComponents < 2) {
/*   58:  78 */       throw new IllegalArgumentException("maxNumComponents: " + maxNumComponents + " (expected: >= 2)");
/*   59:     */     }
/*   60:  82 */     this.alloc = alloc;
/*   61:  83 */     this.direct = direct;
/*   62:  84 */     this.maxNumComponents = maxNumComponents;
/*   63:  85 */     this.components = newList(maxNumComponents);
/*   64:     */     
/*   65:  87 */     addComponents0(false, 0, buffers, offset, len);
/*   66:  88 */     consolidateIfNeeded();
/*   67:  89 */     setIndex(0, capacity());
/*   68:     */   }
/*   69:     */   
/*   70:     */   public CompositeByteBuf(ByteBufAllocator alloc, boolean direct, int maxNumComponents, Iterable<ByteBuf> buffers)
/*   71:     */   {
/*   72:  94 */     super(2147483647);
/*   73:  95 */     if (alloc == null) {
/*   74:  96 */       throw new NullPointerException("alloc");
/*   75:     */     }
/*   76:  98 */     if (maxNumComponents < 2) {
/*   77:  99 */       throw new IllegalArgumentException("maxNumComponents: " + maxNumComponents + " (expected: >= 2)");
/*   78:     */     }
/*   79: 103 */     this.alloc = alloc;
/*   80: 104 */     this.direct = direct;
/*   81: 105 */     this.maxNumComponents = maxNumComponents;
/*   82: 106 */     this.components = newList(maxNumComponents);
/*   83:     */     
/*   84: 108 */     addComponents0(false, 0, buffers);
/*   85: 109 */     consolidateIfNeeded();
/*   86: 110 */     setIndex(0, capacity());
/*   87:     */   }
/*   88:     */   
/*   89:     */   private static ComponentList newList(int maxNumComponents)
/*   90:     */   {
/*   91: 114 */     return new ComponentList(Math.min(16, maxNumComponents));
/*   92:     */   }
/*   93:     */   
/*   94:     */   CompositeByteBuf(ByteBufAllocator alloc)
/*   95:     */   {
/*   96: 119 */     super(2147483647);
/*   97: 120 */     this.alloc = alloc;
/*   98: 121 */     this.direct = false;
/*   99: 122 */     this.maxNumComponents = 0;
/*  100: 123 */     this.components = null;
/*  101:     */   }
/*  102:     */   
/*  103:     */   public CompositeByteBuf addComponent(ByteBuf buffer)
/*  104:     */   {
/*  105: 137 */     return addComponent(false, buffer);
/*  106:     */   }
/*  107:     */   
/*  108:     */   public CompositeByteBuf addComponents(ByteBuf... buffers)
/*  109:     */   {
/*  110: 152 */     return addComponents(false, buffers);
/*  111:     */   }
/*  112:     */   
/*  113:     */   public CompositeByteBuf addComponents(Iterable<ByteBuf> buffers)
/*  114:     */   {
/*  115: 167 */     return addComponents(false, buffers);
/*  116:     */   }
/*  117:     */   
/*  118:     */   public CompositeByteBuf addComponent(int cIndex, ByteBuf buffer)
/*  119:     */   {
/*  120: 182 */     return addComponent(false, cIndex, buffer);
/*  121:     */   }
/*  122:     */   
/*  123:     */   public CompositeByteBuf addComponent(boolean increaseWriterIndex, ByteBuf buffer)
/*  124:     */   {
/*  125: 194 */     ObjectUtil.checkNotNull(buffer, "buffer");
/*  126: 195 */     addComponent0(increaseWriterIndex, this.components.size(), buffer);
/*  127: 196 */     consolidateIfNeeded();
/*  128: 197 */     return this;
/*  129:     */   }
/*  130:     */   
/*  131:     */   public CompositeByteBuf addComponents(boolean increaseWriterIndex, ByteBuf... buffers)
/*  132:     */   {
/*  133: 210 */     addComponents0(increaseWriterIndex, this.components.size(), buffers, 0, buffers.length);
/*  134: 211 */     consolidateIfNeeded();
/*  135: 212 */     return this;
/*  136:     */   }
/*  137:     */   
/*  138:     */   public CompositeByteBuf addComponents(boolean increaseWriterIndex, Iterable<ByteBuf> buffers)
/*  139:     */   {
/*  140: 225 */     addComponents0(increaseWriterIndex, this.components.size(), buffers);
/*  141: 226 */     consolidateIfNeeded();
/*  142: 227 */     return this;
/*  143:     */   }
/*  144:     */   
/*  145:     */   public CompositeByteBuf addComponent(boolean increaseWriterIndex, int cIndex, ByteBuf buffer)
/*  146:     */   {
/*  147: 240 */     ObjectUtil.checkNotNull(buffer, "buffer");
/*  148: 241 */     addComponent0(increaseWriterIndex, cIndex, buffer);
/*  149: 242 */     consolidateIfNeeded();
/*  150: 243 */     return this;
/*  151:     */   }
/*  152:     */   
/*  153:     */   private int addComponent0(boolean increaseWriterIndex, int cIndex, ByteBuf buffer)
/*  154:     */   {
/*  155: 250 */     assert (buffer != null);
/*  156: 251 */     boolean wasAdded = false;
/*  157:     */     try
/*  158:     */     {
/*  159: 253 */       checkComponentIndex(cIndex);
/*  160:     */       
/*  161: 255 */       int readableBytes = buffer.readableBytes();
/*  162:     */       
/*  163:     */ 
/*  164:     */ 
/*  165: 259 */       Component c = new Component(buffer.order(ByteOrder.BIG_ENDIAN).slice());
/*  166:     */       Component prev;
/*  167: 260 */       if (cIndex == this.components.size())
/*  168:     */       {
/*  169: 261 */         wasAdded = this.components.add(c);
/*  170: 262 */         if (cIndex == 0)
/*  171:     */         {
/*  172: 263 */           c.endOffset = readableBytes;
/*  173:     */         }
/*  174:     */         else
/*  175:     */         {
/*  176: 265 */           prev = (Component)this.components.get(cIndex - 1);
/*  177: 266 */           c.offset = prev.endOffset;
/*  178: 267 */           c.endOffset = (c.offset + readableBytes);
/*  179:     */         }
/*  180:     */       }
/*  181:     */       else
/*  182:     */       {
/*  183: 270 */         this.components.add(cIndex, c);
/*  184: 271 */         wasAdded = true;
/*  185: 272 */         if (readableBytes != 0) {
/*  186: 273 */           updateComponentOffsets(cIndex);
/*  187:     */         }
/*  188:     */       }
/*  189: 276 */       if (increaseWriterIndex) {
/*  190: 277 */         writerIndex(writerIndex() + buffer.readableBytes());
/*  191:     */       }
/*  192: 279 */       return cIndex;
/*  193:     */     }
/*  194:     */     finally
/*  195:     */     {
/*  196: 281 */       if (!wasAdded) {
/*  197: 282 */         buffer.release();
/*  198:     */       }
/*  199:     */     }
/*  200:     */   }
/*  201:     */   
/*  202:     */   public CompositeByteBuf addComponents(int cIndex, ByteBuf... buffers)
/*  203:     */   {
/*  204: 302 */     addComponents0(false, cIndex, buffers, 0, buffers.length);
/*  205: 303 */     consolidateIfNeeded();
/*  206: 304 */     return this;
/*  207:     */   }
/*  208:     */   
/*  209:     */   private int addComponents0(boolean increaseWriterIndex, int cIndex, ByteBuf[] buffers, int offset, int len)
/*  210:     */   {
/*  211: 308 */     ObjectUtil.checkNotNull(buffers, "buffers");
/*  212: 309 */     int i = offset;
/*  213:     */     try
/*  214:     */     {
/*  215: 311 */       checkComponentIndex(cIndex);
/*  216:     */       ByteBuf b;
/*  217: 314 */       while (i < len)
/*  218:     */       {
/*  219: 317 */         b = buffers[(i++)];
/*  220: 318 */         if (b == null) {
/*  221:     */           break;
/*  222:     */         }
/*  223: 321 */         cIndex = addComponent0(increaseWriterIndex, cIndex, b) + 1;
/*  224: 322 */         int size = this.components.size();
/*  225: 323 */         if (cIndex > size) {
/*  226: 324 */           cIndex = size;
/*  227:     */         }
/*  228:     */       }
/*  229:     */       ByteBuf b;
/*  230: 327 */       return cIndex;
/*  231:     */     }
/*  232:     */     finally
/*  233:     */     {
/*  234: 329 */       for (; i < len; i++)
/*  235:     */       {
/*  236: 330 */         ByteBuf b = buffers[i];
/*  237: 331 */         if (b != null) {
/*  238:     */           try
/*  239:     */           {
/*  240: 333 */             b.release();
/*  241:     */           }
/*  242:     */           catch (Throwable localThrowable1) {}
/*  243:     */         }
/*  244:     */       }
/*  245:     */     }
/*  246:     */   }
/*  247:     */   
/*  248:     */   public CompositeByteBuf addComponents(int cIndex, Iterable<ByteBuf> buffers)
/*  249:     */   {
/*  250: 356 */     addComponents0(false, cIndex, buffers);
/*  251: 357 */     consolidateIfNeeded();
/*  252: 358 */     return this;
/*  253:     */   }
/*  254:     */   
/*  255:     */   private int addComponents0(boolean increaseIndex, int cIndex, Iterable<ByteBuf> buffers)
/*  256:     */   {
/*  257: 362 */     if ((buffers instanceof ByteBuf)) {
/*  258: 364 */       return addComponent0(increaseIndex, cIndex, (ByteBuf)buffers);
/*  259:     */     }
/*  260: 366 */     ObjectUtil.checkNotNull(buffers, "buffers");
/*  261: 368 */     if (!(buffers instanceof Collection))
/*  262:     */     {
/*  263: 369 */       List<ByteBuf> list = new ArrayList();
/*  264:     */       try
/*  265:     */       {
/*  266: 371 */         for (ByteBuf b : buffers) {
/*  267: 372 */           list.add(b);
/*  268:     */         }
/*  269: 374 */         buffers = list;
/*  270: 376 */         if (buffers != list) {
/*  271: 377 */           for (ByteBuf b : buffers) {
/*  272: 378 */             if (b != null) {
/*  273:     */               try
/*  274:     */               {
/*  275: 380 */                 b.release();
/*  276:     */               }
/*  277:     */               catch (Throwable localThrowable) {}
/*  278:     */             }
/*  279:     */           }
/*  280:     */         }
/*  281:     */       }
/*  282:     */       finally
/*  283:     */       {
/*  284: 376 */         if (buffers != list) {
/*  285: 377 */           for (ByteBuf b : buffers) {
/*  286: 378 */             if (b != null) {
/*  287:     */               try
/*  288:     */               {
/*  289: 380 */                 b.release();
/*  290:     */               }
/*  291:     */               catch (Throwable localThrowable1) {}
/*  292:     */             }
/*  293:     */           }
/*  294:     */         }
/*  295:     */       }
/*  296:     */     }
/*  297: 390 */     Collection<ByteBuf> col = (Collection)buffers;
/*  298: 391 */     return addComponents0(increaseIndex, cIndex, (ByteBuf[])col.toArray(new ByteBuf[col.size()]), 0, col.size());
/*  299:     */   }
/*  300:     */   
/*  301:     */   private void consolidateIfNeeded()
/*  302:     */   {
/*  303: 401 */     int numComponents = this.components.size();
/*  304: 402 */     if (numComponents > this.maxNumComponents)
/*  305:     */     {
/*  306: 403 */       int capacity = ((Component)this.components.get(numComponents - 1)).endOffset;
/*  307:     */       
/*  308: 405 */       ByteBuf consolidated = allocBuffer(capacity);
/*  309: 408 */       for (int i = 0; i < numComponents; i++)
/*  310:     */       {
/*  311: 409 */         Component c = (Component)this.components.get(i);
/*  312: 410 */         ByteBuf b = c.buf;
/*  313: 411 */         consolidated.writeBytes(b);
/*  314: 412 */         c.freeIfNecessary();
/*  315:     */       }
/*  316: 414 */       Component c = new Component(consolidated);
/*  317: 415 */       c.endOffset = c.length;
/*  318: 416 */       this.components.clear();
/*  319: 417 */       this.components.add(c);
/*  320:     */     }
/*  321:     */   }
/*  322:     */   
/*  323:     */   private void checkComponentIndex(int cIndex)
/*  324:     */   {
/*  325: 422 */     ensureAccessible();
/*  326: 423 */     if ((cIndex < 0) || (cIndex > this.components.size())) {
/*  327: 424 */       throw new IndexOutOfBoundsException(String.format("cIndex: %d (expected: >= 0 && <= numComponents(%d))", new Object[] {
/*  328:     */       
/*  329: 426 */         Integer.valueOf(cIndex), Integer.valueOf(this.components.size()) }));
/*  330:     */     }
/*  331:     */   }
/*  332:     */   
/*  333:     */   private void checkComponentIndex(int cIndex, int numComponents)
/*  334:     */   {
/*  335: 431 */     ensureAccessible();
/*  336: 432 */     if ((cIndex < 0) || (cIndex + numComponents > this.components.size())) {
/*  337: 433 */       throw new IndexOutOfBoundsException(String.format("cIndex: %d, numComponents: %d (expected: cIndex >= 0 && cIndex + numComponents <= totalNumComponents(%d))", new Object[] {
/*  338:     */       
/*  339:     */ 
/*  340: 436 */         Integer.valueOf(cIndex), Integer.valueOf(numComponents), Integer.valueOf(this.components.size()) }));
/*  341:     */     }
/*  342:     */   }
/*  343:     */   
/*  344:     */   private void updateComponentOffsets(int cIndex)
/*  345:     */   {
/*  346: 441 */     int size = this.components.size();
/*  347: 442 */     if (size <= cIndex) {
/*  348: 443 */       return;
/*  349:     */     }
/*  350: 446 */     Component c = (Component)this.components.get(cIndex);
/*  351: 447 */     if (cIndex == 0)
/*  352:     */     {
/*  353: 448 */       c.offset = 0;
/*  354: 449 */       c.endOffset = c.length;
/*  355: 450 */       cIndex++;
/*  356:     */     }
/*  357: 453 */     for (int i = cIndex; i < size; i++)
/*  358:     */     {
/*  359: 454 */       Component prev = (Component)this.components.get(i - 1);
/*  360: 455 */       Component cur = (Component)this.components.get(i);
/*  361: 456 */       cur.offset = prev.endOffset;
/*  362: 457 */       cur.endOffset = (cur.offset + cur.length);
/*  363:     */     }
/*  364:     */   }
/*  365:     */   
/*  366:     */   public CompositeByteBuf removeComponent(int cIndex)
/*  367:     */   {
/*  368: 467 */     checkComponentIndex(cIndex);
/*  369: 468 */     Component comp = (Component)this.components.remove(cIndex);
/*  370: 469 */     comp.freeIfNecessary();
/*  371: 470 */     if (comp.length > 0) {
/*  372: 472 */       updateComponentOffsets(cIndex);
/*  373:     */     }
/*  374: 474 */     return this;
/*  375:     */   }
/*  376:     */   
/*  377:     */   public CompositeByteBuf removeComponents(int cIndex, int numComponents)
/*  378:     */   {
/*  379: 484 */     checkComponentIndex(cIndex, numComponents);
/*  380: 486 */     if (numComponents == 0) {
/*  381: 487 */       return this;
/*  382:     */     }
/*  383: 489 */     int endIndex = cIndex + numComponents;
/*  384: 490 */     boolean needsUpdate = false;
/*  385: 491 */     for (int i = cIndex; i < endIndex; i++)
/*  386:     */     {
/*  387: 492 */       Component c = (Component)this.components.get(i);
/*  388: 493 */       if (c.length > 0) {
/*  389: 494 */         needsUpdate = true;
/*  390:     */       }
/*  391: 496 */       c.freeIfNecessary();
/*  392:     */     }
/*  393: 498 */     this.components.removeRange(cIndex, endIndex);
/*  394: 500 */     if (needsUpdate) {
/*  395: 502 */       updateComponentOffsets(cIndex);
/*  396:     */     }
/*  397: 504 */     return this;
/*  398:     */   }
/*  399:     */   
/*  400:     */   public Iterator<ByteBuf> iterator()
/*  401:     */   {
/*  402: 509 */     ensureAccessible();
/*  403: 510 */     if (this.components.isEmpty()) {
/*  404: 511 */       return EMPTY_ITERATOR;
/*  405:     */     }
/*  406: 513 */     return new CompositeByteBufIterator(null);
/*  407:     */   }
/*  408:     */   
/*  409:     */   public List<ByteBuf> decompose(int offset, int length)
/*  410:     */   {
/*  411: 520 */     checkIndex(offset, length);
/*  412: 521 */     if (length == 0) {
/*  413: 522 */       return Collections.emptyList();
/*  414:     */     }
/*  415: 525 */     int componentId = toComponentIndex(offset);
/*  416: 526 */     List<ByteBuf> slice = new ArrayList(this.components.size());
/*  417:     */     
/*  418:     */ 
/*  419: 529 */     Component firstC = (Component)this.components.get(componentId);
/*  420: 530 */     ByteBuf first = firstC.buf.duplicate();
/*  421: 531 */     first.readerIndex(offset - firstC.offset);
/*  422:     */     
/*  423: 533 */     ByteBuf buf = first;
/*  424: 534 */     int bytesToSlice = length;
/*  425:     */     do
/*  426:     */     {
/*  427: 536 */       int readableBytes = buf.readableBytes();
/*  428: 537 */       if (bytesToSlice <= readableBytes)
/*  429:     */       {
/*  430: 539 */         buf.writerIndex(buf.readerIndex() + bytesToSlice);
/*  431: 540 */         slice.add(buf);
/*  432: 541 */         break;
/*  433:     */       }
/*  434: 544 */       slice.add(buf);
/*  435: 545 */       bytesToSlice -= readableBytes;
/*  436: 546 */       componentId++;
/*  437:     */       
/*  438:     */ 
/*  439: 549 */       buf = ((Component)this.components.get(componentId)).buf.duplicate();
/*  440: 551 */     } while (bytesToSlice > 0);
/*  441: 554 */     for (int i = 0; i < slice.size(); i++) {
/*  442: 555 */       slice.set(i, ((ByteBuf)slice.get(i)).slice());
/*  443:     */     }
/*  444: 558 */     return slice;
/*  445:     */   }
/*  446:     */   
/*  447:     */   public boolean isDirect()
/*  448:     */   {
/*  449: 563 */     int size = this.components.size();
/*  450: 564 */     if (size == 0) {
/*  451: 565 */       return false;
/*  452:     */     }
/*  453: 567 */     for (int i = 0; i < size; i++) {
/*  454: 568 */       if (!((Component)this.components.get(i)).buf.isDirect()) {
/*  455: 569 */         return false;
/*  456:     */       }
/*  457:     */     }
/*  458: 572 */     return true;
/*  459:     */   }
/*  460:     */   
/*  461:     */   public boolean hasArray()
/*  462:     */   {
/*  463: 577 */     switch (this.components.size())
/*  464:     */     {
/*  465:     */     case 0: 
/*  466: 579 */       return true;
/*  467:     */     case 1: 
/*  468: 581 */       return ((Component)this.components.get(0)).buf.hasArray();
/*  469:     */     }
/*  470: 583 */     return false;
/*  471:     */   }
/*  472:     */   
/*  473:     */   public byte[] array()
/*  474:     */   {
/*  475: 589 */     switch (this.components.size())
/*  476:     */     {
/*  477:     */     case 0: 
/*  478: 591 */       return EmptyArrays.EMPTY_BYTES;
/*  479:     */     case 1: 
/*  480: 593 */       return ((Component)this.components.get(0)).buf.array();
/*  481:     */     }
/*  482: 595 */     throw new UnsupportedOperationException();
/*  483:     */   }
/*  484:     */   
/*  485:     */   public int arrayOffset()
/*  486:     */   {
/*  487: 601 */     switch (this.components.size())
/*  488:     */     {
/*  489:     */     case 0: 
/*  490: 603 */       return 0;
/*  491:     */     case 1: 
/*  492: 605 */       return ((Component)this.components.get(0)).buf.arrayOffset();
/*  493:     */     }
/*  494: 607 */     throw new UnsupportedOperationException();
/*  495:     */   }
/*  496:     */   
/*  497:     */   public boolean hasMemoryAddress()
/*  498:     */   {
/*  499: 613 */     switch (this.components.size())
/*  500:     */     {
/*  501:     */     case 0: 
/*  502: 615 */       return Unpooled.EMPTY_BUFFER.hasMemoryAddress();
/*  503:     */     case 1: 
/*  504: 617 */       return ((Component)this.components.get(0)).buf.hasMemoryAddress();
/*  505:     */     }
/*  506: 619 */     return false;
/*  507:     */   }
/*  508:     */   
/*  509:     */   public long memoryAddress()
/*  510:     */   {
/*  511: 625 */     switch (this.components.size())
/*  512:     */     {
/*  513:     */     case 0: 
/*  514: 627 */       return Unpooled.EMPTY_BUFFER.memoryAddress();
/*  515:     */     case 1: 
/*  516: 629 */       return ((Component)this.components.get(0)).buf.memoryAddress();
/*  517:     */     }
/*  518: 631 */     throw new UnsupportedOperationException();
/*  519:     */   }
/*  520:     */   
/*  521:     */   public int capacity()
/*  522:     */   {
/*  523: 637 */     int numComponents = this.components.size();
/*  524: 638 */     if (numComponents == 0) {
/*  525: 639 */       return 0;
/*  526:     */     }
/*  527: 641 */     return ((Component)this.components.get(numComponents - 1)).endOffset;
/*  528:     */   }
/*  529:     */   
/*  530:     */   public CompositeByteBuf capacity(int newCapacity)
/*  531:     */   {
/*  532: 646 */     checkNewCapacity(newCapacity);
/*  533:     */     
/*  534: 648 */     int oldCapacity = capacity();
/*  535: 649 */     if (newCapacity > oldCapacity)
/*  536:     */     {
/*  537: 650 */       int paddingLength = newCapacity - oldCapacity;
/*  538:     */       
/*  539: 652 */       int nComponents = this.components.size();
/*  540: 653 */       if (nComponents < this.maxNumComponents)
/*  541:     */       {
/*  542: 654 */         ByteBuf padding = allocBuffer(paddingLength);
/*  543: 655 */         padding.setIndex(0, paddingLength);
/*  544: 656 */         addComponent0(false, this.components.size(), padding);
/*  545:     */       }
/*  546:     */       else
/*  547:     */       {
/*  548: 658 */         ByteBuf padding = allocBuffer(paddingLength);
/*  549: 659 */         padding.setIndex(0, paddingLength);
/*  550:     */         
/*  551:     */ 
/*  552: 662 */         addComponent0(false, this.components.size(), padding);
/*  553: 663 */         consolidateIfNeeded();
/*  554:     */       }
/*  555:     */     }
/*  556: 665 */     else if (newCapacity < oldCapacity)
/*  557:     */     {
/*  558: 666 */       int bytesToTrim = oldCapacity - newCapacity;
/*  559: 667 */       for (ListIterator<Component> i = this.components.listIterator(this.components.size()); i.hasPrevious();)
/*  560:     */       {
/*  561: 668 */         Component c = (Component)i.previous();
/*  562: 669 */         if (bytesToTrim >= c.length)
/*  563:     */         {
/*  564: 670 */           bytesToTrim -= c.length;
/*  565: 671 */           i.remove();
/*  566:     */         }
/*  567:     */         else
/*  568:     */         {
/*  569: 676 */           Component newC = new Component(c.buf.slice(0, c.length - bytesToTrim));
/*  570: 677 */           newC.offset = c.offset;
/*  571: 678 */           newC.endOffset = (newC.offset + newC.length);
/*  572: 679 */           i.set(newC);
/*  573:     */         }
/*  574:     */       }
/*  575: 683 */       if (readerIndex() > newCapacity) {
/*  576: 684 */         setIndex(newCapacity, newCapacity);
/*  577: 685 */       } else if (writerIndex() > newCapacity) {
/*  578: 686 */         writerIndex(newCapacity);
/*  579:     */       }
/*  580:     */     }
/*  581: 689 */     return this;
/*  582:     */   }
/*  583:     */   
/*  584:     */   public ByteBufAllocator alloc()
/*  585:     */   {
/*  586: 694 */     return this.alloc;
/*  587:     */   }
/*  588:     */   
/*  589:     */   public ByteOrder order()
/*  590:     */   {
/*  591: 699 */     return ByteOrder.BIG_ENDIAN;
/*  592:     */   }
/*  593:     */   
/*  594:     */   public int numComponents()
/*  595:     */   {
/*  596: 706 */     return this.components.size();
/*  597:     */   }
/*  598:     */   
/*  599:     */   public int maxNumComponents()
/*  600:     */   {
/*  601: 713 */     return this.maxNumComponents;
/*  602:     */   }
/*  603:     */   
/*  604:     */   public int toComponentIndex(int offset)
/*  605:     */   {
/*  606: 720 */     checkIndex(offset);
/*  607:     */     
/*  608: 722 */     int low = 0;
/*  609: 722 */     for (int high = this.components.size(); low <= high;)
/*  610:     */     {
/*  611: 723 */       int mid = low + high >>> 1;
/*  612: 724 */       Component c = (Component)this.components.get(mid);
/*  613: 725 */       if (offset >= c.endOffset) {
/*  614: 726 */         low = mid + 1;
/*  615: 727 */       } else if (offset < c.offset) {
/*  616: 728 */         high = mid - 1;
/*  617:     */       } else {
/*  618: 730 */         return mid;
/*  619:     */       }
/*  620:     */     }
/*  621: 734 */     throw new Error("should not reach here");
/*  622:     */   }
/*  623:     */   
/*  624:     */   public int toByteIndex(int cIndex)
/*  625:     */   {
/*  626: 738 */     checkComponentIndex(cIndex);
/*  627: 739 */     return ((Component)this.components.get(cIndex)).offset;
/*  628:     */   }
/*  629:     */   
/*  630:     */   public byte getByte(int index)
/*  631:     */   {
/*  632: 744 */     return _getByte(index);
/*  633:     */   }
/*  634:     */   
/*  635:     */   protected byte _getByte(int index)
/*  636:     */   {
/*  637: 749 */     Component c = findComponent(index);
/*  638: 750 */     return c.buf.getByte(index - c.offset);
/*  639:     */   }
/*  640:     */   
/*  641:     */   protected short _getShort(int index)
/*  642:     */   {
/*  643: 755 */     Component c = findComponent(index);
/*  644: 756 */     if (index + 2 <= c.endOffset) {
/*  645: 757 */       return c.buf.getShort(index - c.offset);
/*  646:     */     }
/*  647: 758 */     if (order() == ByteOrder.BIG_ENDIAN) {
/*  648: 759 */       return (short)((_getByte(index) & 0xFF) << 8 | _getByte(index + 1) & 0xFF);
/*  649:     */     }
/*  650: 761 */     return (short)(_getByte(index) & 0xFF | (_getByte(index + 1) & 0xFF) << 8);
/*  651:     */   }
/*  652:     */   
/*  653:     */   protected short _getShortLE(int index)
/*  654:     */   {
/*  655: 767 */     Component c = findComponent(index);
/*  656: 768 */     if (index + 2 <= c.endOffset) {
/*  657: 769 */       return c.buf.getShortLE(index - c.offset);
/*  658:     */     }
/*  659: 770 */     if (order() == ByteOrder.BIG_ENDIAN) {
/*  660: 771 */       return (short)(_getByte(index) & 0xFF | (_getByte(index + 1) & 0xFF) << 8);
/*  661:     */     }
/*  662: 773 */     return (short)((_getByte(index) & 0xFF) << 8 | _getByte(index + 1) & 0xFF);
/*  663:     */   }
/*  664:     */   
/*  665:     */   protected int _getUnsignedMedium(int index)
/*  666:     */   {
/*  667: 779 */     Component c = findComponent(index);
/*  668: 780 */     if (index + 3 <= c.endOffset) {
/*  669: 781 */       return c.buf.getUnsignedMedium(index - c.offset);
/*  670:     */     }
/*  671: 782 */     if (order() == ByteOrder.BIG_ENDIAN) {
/*  672: 783 */       return (_getShort(index) & 0xFFFF) << 8 | _getByte(index + 2) & 0xFF;
/*  673:     */     }
/*  674: 785 */     return _getShort(index) & 0xFFFF | (_getByte(index + 2) & 0xFF) << 16;
/*  675:     */   }
/*  676:     */   
/*  677:     */   protected int _getUnsignedMediumLE(int index)
/*  678:     */   {
/*  679: 791 */     Component c = findComponent(index);
/*  680: 792 */     if (index + 3 <= c.endOffset) {
/*  681: 793 */       return c.buf.getUnsignedMediumLE(index - c.offset);
/*  682:     */     }
/*  683: 794 */     if (order() == ByteOrder.BIG_ENDIAN) {
/*  684: 795 */       return _getShortLE(index) & 0xFFFF | (_getByte(index + 2) & 0xFF) << 16;
/*  685:     */     }
/*  686: 797 */     return (_getShortLE(index) & 0xFFFF) << 8 | _getByte(index + 2) & 0xFF;
/*  687:     */   }
/*  688:     */   
/*  689:     */   protected int _getInt(int index)
/*  690:     */   {
/*  691: 803 */     Component c = findComponent(index);
/*  692: 804 */     if (index + 4 <= c.endOffset) {
/*  693: 805 */       return c.buf.getInt(index - c.offset);
/*  694:     */     }
/*  695: 806 */     if (order() == ByteOrder.BIG_ENDIAN) {
/*  696: 807 */       return (_getShort(index) & 0xFFFF) << 16 | _getShort(index + 2) & 0xFFFF;
/*  697:     */     }
/*  698: 809 */     return _getShort(index) & 0xFFFF | (_getShort(index + 2) & 0xFFFF) << 16;
/*  699:     */   }
/*  700:     */   
/*  701:     */   protected int _getIntLE(int index)
/*  702:     */   {
/*  703: 815 */     Component c = findComponent(index);
/*  704: 816 */     if (index + 4 <= c.endOffset) {
/*  705: 817 */       return c.buf.getIntLE(index - c.offset);
/*  706:     */     }
/*  707: 818 */     if (order() == ByteOrder.BIG_ENDIAN) {
/*  708: 819 */       return _getShortLE(index) & 0xFFFF | (_getShortLE(index + 2) & 0xFFFF) << 16;
/*  709:     */     }
/*  710: 821 */     return (_getShortLE(index) & 0xFFFF) << 16 | _getShortLE(index + 2) & 0xFFFF;
/*  711:     */   }
/*  712:     */   
/*  713:     */   protected long _getLong(int index)
/*  714:     */   {
/*  715: 827 */     Component c = findComponent(index);
/*  716: 828 */     if (index + 8 <= c.endOffset) {
/*  717: 829 */       return c.buf.getLong(index - c.offset);
/*  718:     */     }
/*  719: 830 */     if (order() == ByteOrder.BIG_ENDIAN) {
/*  720: 831 */       return (_getInt(index) & 0xFFFFFFFF) << 32 | _getInt(index + 4) & 0xFFFFFFFF;
/*  721:     */     }
/*  722: 833 */     return _getInt(index) & 0xFFFFFFFF | (_getInt(index + 4) & 0xFFFFFFFF) << 32;
/*  723:     */   }
/*  724:     */   
/*  725:     */   protected long _getLongLE(int index)
/*  726:     */   {
/*  727: 839 */     Component c = findComponent(index);
/*  728: 840 */     if (index + 8 <= c.endOffset) {
/*  729: 841 */       return c.buf.getLongLE(index - c.offset);
/*  730:     */     }
/*  731: 842 */     if (order() == ByteOrder.BIG_ENDIAN) {
/*  732: 843 */       return _getIntLE(index) & 0xFFFFFFFF | (_getIntLE(index + 4) & 0xFFFFFFFF) << 32;
/*  733:     */     }
/*  734: 845 */     return (_getIntLE(index) & 0xFFFFFFFF) << 32 | _getIntLE(index + 4) & 0xFFFFFFFF;
/*  735:     */   }
/*  736:     */   
/*  737:     */   public CompositeByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/*  738:     */   {
/*  739: 851 */     checkDstIndex(index, length, dstIndex, dst.length);
/*  740: 852 */     if (length == 0) {
/*  741: 853 */       return this;
/*  742:     */     }
/*  743: 856 */     int i = toComponentIndex(index);
/*  744: 857 */     while (length > 0)
/*  745:     */     {
/*  746: 858 */       Component c = (Component)this.components.get(i);
/*  747: 859 */       ByteBuf s = c.buf;
/*  748: 860 */       int adjustment = c.offset;
/*  749: 861 */       int localLength = Math.min(length, s.capacity() - (index - adjustment));
/*  750: 862 */       s.getBytes(index - adjustment, dst, dstIndex, localLength);
/*  751: 863 */       index += localLength;
/*  752: 864 */       dstIndex += localLength;
/*  753: 865 */       length -= localLength;
/*  754: 866 */       i++;
/*  755:     */     }
/*  756: 868 */     return this;
/*  757:     */   }
/*  758:     */   
/*  759:     */   public CompositeByteBuf getBytes(int index, ByteBuffer dst)
/*  760:     */   {
/*  761: 873 */     int limit = dst.limit();
/*  762: 874 */     int length = dst.remaining();
/*  763:     */     
/*  764: 876 */     checkIndex(index, length);
/*  765: 877 */     if (length == 0) {
/*  766: 878 */       return this;
/*  767:     */     }
/*  768: 881 */     int i = toComponentIndex(index);
/*  769:     */     try
/*  770:     */     {
/*  771: 883 */       while (length > 0)
/*  772:     */       {
/*  773: 884 */         Component c = (Component)this.components.get(i);
/*  774: 885 */         ByteBuf s = c.buf;
/*  775: 886 */         int adjustment = c.offset;
/*  776: 887 */         int localLength = Math.min(length, s.capacity() - (index - adjustment));
/*  777: 888 */         dst.limit(dst.position() + localLength);
/*  778: 889 */         s.getBytes(index - adjustment, dst);
/*  779: 890 */         index += localLength;
/*  780: 891 */         length -= localLength;
/*  781: 892 */         i++;
/*  782:     */       }
/*  783:     */     }
/*  784:     */     finally
/*  785:     */     {
/*  786: 895 */       dst.limit(limit);
/*  787:     */     }
/*  788: 897 */     return this;
/*  789:     */   }
/*  790:     */   
/*  791:     */   public CompositeByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/*  792:     */   {
/*  793: 902 */     checkDstIndex(index, length, dstIndex, dst.capacity());
/*  794: 903 */     if (length == 0) {
/*  795: 904 */       return this;
/*  796:     */     }
/*  797: 907 */     int i = toComponentIndex(index);
/*  798: 908 */     while (length > 0)
/*  799:     */     {
/*  800: 909 */       Component c = (Component)this.components.get(i);
/*  801: 910 */       ByteBuf s = c.buf;
/*  802: 911 */       int adjustment = c.offset;
/*  803: 912 */       int localLength = Math.min(length, s.capacity() - (index - adjustment));
/*  804: 913 */       s.getBytes(index - adjustment, dst, dstIndex, localLength);
/*  805: 914 */       index += localLength;
/*  806: 915 */       dstIndex += localLength;
/*  807: 916 */       length -= localLength;
/*  808: 917 */       i++;
/*  809:     */     }
/*  810: 919 */     return this;
/*  811:     */   }
/*  812:     */   
/*  813:     */   public int getBytes(int index, GatheringByteChannel out, int length)
/*  814:     */     throws IOException
/*  815:     */   {
/*  816: 925 */     int count = nioBufferCount();
/*  817: 926 */     if (count == 1) {
/*  818: 927 */       return out.write(internalNioBuffer(index, length));
/*  819:     */     }
/*  820: 929 */     long writtenBytes = out.write(nioBuffers(index, length));
/*  821: 930 */     if (writtenBytes > 2147483647L) {
/*  822: 931 */       return 2147483647;
/*  823:     */     }
/*  824: 933 */     return (int)writtenBytes;
/*  825:     */   }
/*  826:     */   
/*  827:     */   public int getBytes(int index, FileChannel out, long position, int length)
/*  828:     */     throws IOException
/*  829:     */   {
/*  830: 941 */     int count = nioBufferCount();
/*  831: 942 */     if (count == 1) {
/*  832: 943 */       return out.write(internalNioBuffer(index, length), position);
/*  833:     */     }
/*  834: 945 */     long writtenBytes = 0L;
/*  835: 946 */     for (ByteBuffer buf : nioBuffers(index, length)) {
/*  836: 947 */       writtenBytes += out.write(buf, position + writtenBytes);
/*  837:     */     }
/*  838: 949 */     if (writtenBytes > 2147483647L) {
/*  839: 950 */       return 2147483647;
/*  840:     */     }
/*  841: 952 */     return (int)writtenBytes;
/*  842:     */   }
/*  843:     */   
/*  844:     */   public CompositeByteBuf getBytes(int index, OutputStream out, int length)
/*  845:     */     throws IOException
/*  846:     */   {
/*  847: 958 */     checkIndex(index, length);
/*  848: 959 */     if (length == 0) {
/*  849: 960 */       return this;
/*  850:     */     }
/*  851: 963 */     int i = toComponentIndex(index);
/*  852: 964 */     while (length > 0)
/*  853:     */     {
/*  854: 965 */       Component c = (Component)this.components.get(i);
/*  855: 966 */       ByteBuf s = c.buf;
/*  856: 967 */       int adjustment = c.offset;
/*  857: 968 */       int localLength = Math.min(length, s.capacity() - (index - adjustment));
/*  858: 969 */       s.getBytes(index - adjustment, out, localLength);
/*  859: 970 */       index += localLength;
/*  860: 971 */       length -= localLength;
/*  861: 972 */       i++;
/*  862:     */     }
/*  863: 974 */     return this;
/*  864:     */   }
/*  865:     */   
/*  866:     */   public CompositeByteBuf setByte(int index, int value)
/*  867:     */   {
/*  868: 979 */     Component c = findComponent(index);
/*  869: 980 */     c.buf.setByte(index - c.offset, value);
/*  870: 981 */     return this;
/*  871:     */   }
/*  872:     */   
/*  873:     */   protected void _setByte(int index, int value)
/*  874:     */   {
/*  875: 986 */     setByte(index, value);
/*  876:     */   }
/*  877:     */   
/*  878:     */   public CompositeByteBuf setShort(int index, int value)
/*  879:     */   {
/*  880: 991 */     return (CompositeByteBuf)super.setShort(index, value);
/*  881:     */   }
/*  882:     */   
/*  883:     */   protected void _setShort(int index, int value)
/*  884:     */   {
/*  885: 996 */     Component c = findComponent(index);
/*  886: 997 */     if (index + 2 <= c.endOffset)
/*  887:     */     {
/*  888: 998 */       c.buf.setShort(index - c.offset, value);
/*  889:     */     }
/*  890: 999 */     else if (order() == ByteOrder.BIG_ENDIAN)
/*  891:     */     {
/*  892:1000 */       _setByte(index, (byte)(value >>> 8));
/*  893:1001 */       _setByte(index + 1, (byte)value);
/*  894:     */     }
/*  895:     */     else
/*  896:     */     {
/*  897:1003 */       _setByte(index, (byte)value);
/*  898:1004 */       _setByte(index + 1, (byte)(value >>> 8));
/*  899:     */     }
/*  900:     */   }
/*  901:     */   
/*  902:     */   protected void _setShortLE(int index, int value)
/*  903:     */   {
/*  904:1010 */     Component c = findComponent(index);
/*  905:1011 */     if (index + 2 <= c.endOffset)
/*  906:     */     {
/*  907:1012 */       c.buf.setShortLE(index - c.offset, value);
/*  908:     */     }
/*  909:1013 */     else if (order() == ByteOrder.BIG_ENDIAN)
/*  910:     */     {
/*  911:1014 */       _setByte(index, (byte)value);
/*  912:1015 */       _setByte(index + 1, (byte)(value >>> 8));
/*  913:     */     }
/*  914:     */     else
/*  915:     */     {
/*  916:1017 */       _setByte(index, (byte)(value >>> 8));
/*  917:1018 */       _setByte(index + 1, (byte)value);
/*  918:     */     }
/*  919:     */   }
/*  920:     */   
/*  921:     */   public CompositeByteBuf setMedium(int index, int value)
/*  922:     */   {
/*  923:1024 */     return (CompositeByteBuf)super.setMedium(index, value);
/*  924:     */   }
/*  925:     */   
/*  926:     */   protected void _setMedium(int index, int value)
/*  927:     */   {
/*  928:1029 */     Component c = findComponent(index);
/*  929:1030 */     if (index + 3 <= c.endOffset)
/*  930:     */     {
/*  931:1031 */       c.buf.setMedium(index - c.offset, value);
/*  932:     */     }
/*  933:1032 */     else if (order() == ByteOrder.BIG_ENDIAN)
/*  934:     */     {
/*  935:1033 */       _setShort(index, (short)(value >> 8));
/*  936:1034 */       _setByte(index + 2, (byte)value);
/*  937:     */     }
/*  938:     */     else
/*  939:     */     {
/*  940:1036 */       _setShort(index, (short)value);
/*  941:1037 */       _setByte(index + 2, (byte)(value >>> 16));
/*  942:     */     }
/*  943:     */   }
/*  944:     */   
/*  945:     */   protected void _setMediumLE(int index, int value)
/*  946:     */   {
/*  947:1043 */     Component c = findComponent(index);
/*  948:1044 */     if (index + 3 <= c.endOffset)
/*  949:     */     {
/*  950:1045 */       c.buf.setMediumLE(index - c.offset, value);
/*  951:     */     }
/*  952:1046 */     else if (order() == ByteOrder.BIG_ENDIAN)
/*  953:     */     {
/*  954:1047 */       _setShortLE(index, (short)value);
/*  955:1048 */       _setByte(index + 2, (byte)(value >>> 16));
/*  956:     */     }
/*  957:     */     else
/*  958:     */     {
/*  959:1050 */       _setShortLE(index, (short)(value >> 8));
/*  960:1051 */       _setByte(index + 2, (byte)value);
/*  961:     */     }
/*  962:     */   }
/*  963:     */   
/*  964:     */   public CompositeByteBuf setInt(int index, int value)
/*  965:     */   {
/*  966:1057 */     return (CompositeByteBuf)super.setInt(index, value);
/*  967:     */   }
/*  968:     */   
/*  969:     */   protected void _setInt(int index, int value)
/*  970:     */   {
/*  971:1062 */     Component c = findComponent(index);
/*  972:1063 */     if (index + 4 <= c.endOffset)
/*  973:     */     {
/*  974:1064 */       c.buf.setInt(index - c.offset, value);
/*  975:     */     }
/*  976:1065 */     else if (order() == ByteOrder.BIG_ENDIAN)
/*  977:     */     {
/*  978:1066 */       _setShort(index, (short)(value >>> 16));
/*  979:1067 */       _setShort(index + 2, (short)value);
/*  980:     */     }
/*  981:     */     else
/*  982:     */     {
/*  983:1069 */       _setShort(index, (short)value);
/*  984:1070 */       _setShort(index + 2, (short)(value >>> 16));
/*  985:     */     }
/*  986:     */   }
/*  987:     */   
/*  988:     */   protected void _setIntLE(int index, int value)
/*  989:     */   {
/*  990:1076 */     Component c = findComponent(index);
/*  991:1077 */     if (index + 4 <= c.endOffset)
/*  992:     */     {
/*  993:1078 */       c.buf.setIntLE(index - c.offset, value);
/*  994:     */     }
/*  995:1079 */     else if (order() == ByteOrder.BIG_ENDIAN)
/*  996:     */     {
/*  997:1080 */       _setShortLE(index, (short)value);
/*  998:1081 */       _setShortLE(index + 2, (short)(value >>> 16));
/*  999:     */     }
/* 1000:     */     else
/* 1001:     */     {
/* 1002:1083 */       _setShortLE(index, (short)(value >>> 16));
/* 1003:1084 */       _setShortLE(index + 2, (short)value);
/* 1004:     */     }
/* 1005:     */   }
/* 1006:     */   
/* 1007:     */   public CompositeByteBuf setLong(int index, long value)
/* 1008:     */   {
/* 1009:1090 */     return (CompositeByteBuf)super.setLong(index, value);
/* 1010:     */   }
/* 1011:     */   
/* 1012:     */   protected void _setLong(int index, long value)
/* 1013:     */   {
/* 1014:1095 */     Component c = findComponent(index);
/* 1015:1096 */     if (index + 8 <= c.endOffset)
/* 1016:     */     {
/* 1017:1097 */       c.buf.setLong(index - c.offset, value);
/* 1018:     */     }
/* 1019:1098 */     else if (order() == ByteOrder.BIG_ENDIAN)
/* 1020:     */     {
/* 1021:1099 */       _setInt(index, (int)(value >>> 32));
/* 1022:1100 */       _setInt(index + 4, (int)value);
/* 1023:     */     }
/* 1024:     */     else
/* 1025:     */     {
/* 1026:1102 */       _setInt(index, (int)value);
/* 1027:1103 */       _setInt(index + 4, (int)(value >>> 32));
/* 1028:     */     }
/* 1029:     */   }
/* 1030:     */   
/* 1031:     */   protected void _setLongLE(int index, long value)
/* 1032:     */   {
/* 1033:1109 */     Component c = findComponent(index);
/* 1034:1110 */     if (index + 8 <= c.endOffset)
/* 1035:     */     {
/* 1036:1111 */       c.buf.setLongLE(index - c.offset, value);
/* 1037:     */     }
/* 1038:1112 */     else if (order() == ByteOrder.BIG_ENDIAN)
/* 1039:     */     {
/* 1040:1113 */       _setIntLE(index, (int)value);
/* 1041:1114 */       _setIntLE(index + 4, (int)(value >>> 32));
/* 1042:     */     }
/* 1043:     */     else
/* 1044:     */     {
/* 1045:1116 */       _setIntLE(index, (int)(value >>> 32));
/* 1046:1117 */       _setIntLE(index + 4, (int)value);
/* 1047:     */     }
/* 1048:     */   }
/* 1049:     */   
/* 1050:     */   public CompositeByteBuf setBytes(int index, byte[] src, int srcIndex, int length)
/* 1051:     */   {
/* 1052:1123 */     checkSrcIndex(index, length, srcIndex, src.length);
/* 1053:1124 */     if (length == 0) {
/* 1054:1125 */       return this;
/* 1055:     */     }
/* 1056:1128 */     int i = toComponentIndex(index);
/* 1057:1129 */     while (length > 0)
/* 1058:     */     {
/* 1059:1130 */       Component c = (Component)this.components.get(i);
/* 1060:1131 */       ByteBuf s = c.buf;
/* 1061:1132 */       int adjustment = c.offset;
/* 1062:1133 */       int localLength = Math.min(length, s.capacity() - (index - adjustment));
/* 1063:1134 */       s.setBytes(index - adjustment, src, srcIndex, localLength);
/* 1064:1135 */       index += localLength;
/* 1065:1136 */       srcIndex += localLength;
/* 1066:1137 */       length -= localLength;
/* 1067:1138 */       i++;
/* 1068:     */     }
/* 1069:1140 */     return this;
/* 1070:     */   }
/* 1071:     */   
/* 1072:     */   public CompositeByteBuf setBytes(int index, ByteBuffer src)
/* 1073:     */   {
/* 1074:1145 */     int limit = src.limit();
/* 1075:1146 */     int length = src.remaining();
/* 1076:     */     
/* 1077:1148 */     checkIndex(index, length);
/* 1078:1149 */     if (length == 0) {
/* 1079:1150 */       return this;
/* 1080:     */     }
/* 1081:1153 */     int i = toComponentIndex(index);
/* 1082:     */     try
/* 1083:     */     {
/* 1084:1155 */       while (length > 0)
/* 1085:     */       {
/* 1086:1156 */         Component c = (Component)this.components.get(i);
/* 1087:1157 */         ByteBuf s = c.buf;
/* 1088:1158 */         int adjustment = c.offset;
/* 1089:1159 */         int localLength = Math.min(length, s.capacity() - (index - adjustment));
/* 1090:1160 */         src.limit(src.position() + localLength);
/* 1091:1161 */         s.setBytes(index - adjustment, src);
/* 1092:1162 */         index += localLength;
/* 1093:1163 */         length -= localLength;
/* 1094:1164 */         i++;
/* 1095:     */       }
/* 1096:     */     }
/* 1097:     */     finally
/* 1098:     */     {
/* 1099:1167 */       src.limit(limit);
/* 1100:     */     }
/* 1101:1169 */     return this;
/* 1102:     */   }
/* 1103:     */   
/* 1104:     */   public CompositeByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length)
/* 1105:     */   {
/* 1106:1174 */     checkSrcIndex(index, length, srcIndex, src.capacity());
/* 1107:1175 */     if (length == 0) {
/* 1108:1176 */       return this;
/* 1109:     */     }
/* 1110:1179 */     int i = toComponentIndex(index);
/* 1111:1180 */     while (length > 0)
/* 1112:     */     {
/* 1113:1181 */       Component c = (Component)this.components.get(i);
/* 1114:1182 */       ByteBuf s = c.buf;
/* 1115:1183 */       int adjustment = c.offset;
/* 1116:1184 */       int localLength = Math.min(length, s.capacity() - (index - adjustment));
/* 1117:1185 */       s.setBytes(index - adjustment, src, srcIndex, localLength);
/* 1118:1186 */       index += localLength;
/* 1119:1187 */       srcIndex += localLength;
/* 1120:1188 */       length -= localLength;
/* 1121:1189 */       i++;
/* 1122:     */     }
/* 1123:1191 */     return this;
/* 1124:     */   }
/* 1125:     */   
/* 1126:     */   public int setBytes(int index, InputStream in, int length)
/* 1127:     */     throws IOException
/* 1128:     */   {
/* 1129:1196 */     checkIndex(index, length);
/* 1130:1197 */     if (length == 0) {
/* 1131:1198 */       return in.read(EmptyArrays.EMPTY_BYTES);
/* 1132:     */     }
/* 1133:1201 */     int i = toComponentIndex(index);
/* 1134:1202 */     int readBytes = 0;
/* 1135:     */     do
/* 1136:     */     {
/* 1137:1205 */       Component c = (Component)this.components.get(i);
/* 1138:1206 */       ByteBuf s = c.buf;
/* 1139:1207 */       int adjustment = c.offset;
/* 1140:1208 */       int localLength = Math.min(length, s.capacity() - (index - adjustment));
/* 1141:1209 */       if (localLength == 0)
/* 1142:     */       {
/* 1143:1211 */         i++;
/* 1144:     */       }
/* 1145:     */       else
/* 1146:     */       {
/* 1147:1214 */         int localReadBytes = s.setBytes(index - adjustment, in, localLength);
/* 1148:1215 */         if (localReadBytes < 0)
/* 1149:     */         {
/* 1150:1216 */           if (readBytes != 0) {
/* 1151:     */             break;
/* 1152:     */           }
/* 1153:1217 */           return -1;
/* 1154:     */         }
/* 1155:1223 */         if (localReadBytes == localLength)
/* 1156:     */         {
/* 1157:1224 */           index += localLength;
/* 1158:1225 */           length -= localLength;
/* 1159:1226 */           readBytes += localLength;
/* 1160:1227 */           i++;
/* 1161:     */         }
/* 1162:     */         else
/* 1163:     */         {
/* 1164:1229 */           index += localReadBytes;
/* 1165:1230 */           length -= localReadBytes;
/* 1166:1231 */           readBytes += localReadBytes;
/* 1167:     */         }
/* 1168:     */       }
/* 1169:1233 */     } while (length > 0);
/* 1170:1235 */     return readBytes;
/* 1171:     */   }
/* 1172:     */   
/* 1173:     */   public int setBytes(int index, ScatteringByteChannel in, int length)
/* 1174:     */     throws IOException
/* 1175:     */   {
/* 1176:1240 */     checkIndex(index, length);
/* 1177:1241 */     if (length == 0) {
/* 1178:1242 */       return in.read(EMPTY_NIO_BUFFER);
/* 1179:     */     }
/* 1180:1245 */     int i = toComponentIndex(index);
/* 1181:1246 */     int readBytes = 0;
/* 1182:     */     do
/* 1183:     */     {
/* 1184:1248 */       Component c = (Component)this.components.get(i);
/* 1185:1249 */       ByteBuf s = c.buf;
/* 1186:1250 */       int adjustment = c.offset;
/* 1187:1251 */       int localLength = Math.min(length, s.capacity() - (index - adjustment));
/* 1188:1252 */       if (localLength == 0)
/* 1189:     */       {
/* 1190:1254 */         i++;
/* 1191:     */       }
/* 1192:     */       else
/* 1193:     */       {
/* 1194:1257 */         int localReadBytes = s.setBytes(index - adjustment, in, localLength);
/* 1195:1259 */         if (localReadBytes == 0) {
/* 1196:     */           break;
/* 1197:     */         }
/* 1198:1263 */         if (localReadBytes < 0)
/* 1199:     */         {
/* 1200:1264 */           if (readBytes != 0) {
/* 1201:     */             break;
/* 1202:     */           }
/* 1203:1265 */           return -1;
/* 1204:     */         }
/* 1205:1271 */         if (localReadBytes == localLength)
/* 1206:     */         {
/* 1207:1272 */           index += localLength;
/* 1208:1273 */           length -= localLength;
/* 1209:1274 */           readBytes += localLength;
/* 1210:1275 */           i++;
/* 1211:     */         }
/* 1212:     */         else
/* 1213:     */         {
/* 1214:1277 */           index += localReadBytes;
/* 1215:1278 */           length -= localReadBytes;
/* 1216:1279 */           readBytes += localReadBytes;
/* 1217:     */         }
/* 1218:     */       }
/* 1219:1281 */     } while (length > 0);
/* 1220:1283 */     return readBytes;
/* 1221:     */   }
/* 1222:     */   
/* 1223:     */   public int setBytes(int index, FileChannel in, long position, int length)
/* 1224:     */     throws IOException
/* 1225:     */   {
/* 1226:1288 */     checkIndex(index, length);
/* 1227:1289 */     if (length == 0) {
/* 1228:1290 */       return in.read(EMPTY_NIO_BUFFER, position);
/* 1229:     */     }
/* 1230:1293 */     int i = toComponentIndex(index);
/* 1231:1294 */     int readBytes = 0;
/* 1232:     */     do
/* 1233:     */     {
/* 1234:1296 */       Component c = (Component)this.components.get(i);
/* 1235:1297 */       ByteBuf s = c.buf;
/* 1236:1298 */       int adjustment = c.offset;
/* 1237:1299 */       int localLength = Math.min(length, s.capacity() - (index - adjustment));
/* 1238:1300 */       if (localLength == 0)
/* 1239:     */       {
/* 1240:1302 */         i++;
/* 1241:     */       }
/* 1242:     */       else
/* 1243:     */       {
/* 1244:1305 */         int localReadBytes = s.setBytes(index - adjustment, in, position + readBytes, localLength);
/* 1245:1307 */         if (localReadBytes == 0) {
/* 1246:     */           break;
/* 1247:     */         }
/* 1248:1311 */         if (localReadBytes < 0)
/* 1249:     */         {
/* 1250:1312 */           if (readBytes != 0) {
/* 1251:     */             break;
/* 1252:     */           }
/* 1253:1313 */           return -1;
/* 1254:     */         }
/* 1255:1319 */         if (localReadBytes == localLength)
/* 1256:     */         {
/* 1257:1320 */           index += localLength;
/* 1258:1321 */           length -= localLength;
/* 1259:1322 */           readBytes += localLength;
/* 1260:1323 */           i++;
/* 1261:     */         }
/* 1262:     */         else
/* 1263:     */         {
/* 1264:1325 */           index += localReadBytes;
/* 1265:1326 */           length -= localReadBytes;
/* 1266:1327 */           readBytes += localReadBytes;
/* 1267:     */         }
/* 1268:     */       }
/* 1269:1329 */     } while (length > 0);
/* 1270:1331 */     return readBytes;
/* 1271:     */   }
/* 1272:     */   
/* 1273:     */   public ByteBuf copy(int index, int length)
/* 1274:     */   {
/* 1275:1336 */     checkIndex(index, length);
/* 1276:1337 */     ByteBuf dst = allocBuffer(length);
/* 1277:1338 */     if (length != 0) {
/* 1278:1339 */       copyTo(index, length, toComponentIndex(index), dst);
/* 1279:     */     }
/* 1280:1341 */     return dst;
/* 1281:     */   }
/* 1282:     */   
/* 1283:     */   private void copyTo(int index, int length, int componentId, ByteBuf dst)
/* 1284:     */   {
/* 1285:1345 */     int dstIndex = 0;
/* 1286:1346 */     int i = componentId;
/* 1287:1348 */     while (length > 0)
/* 1288:     */     {
/* 1289:1349 */       Component c = (Component)this.components.get(i);
/* 1290:1350 */       ByteBuf s = c.buf;
/* 1291:1351 */       int adjustment = c.offset;
/* 1292:1352 */       int localLength = Math.min(length, s.capacity() - (index - adjustment));
/* 1293:1353 */       s.getBytes(index - adjustment, dst, dstIndex, localLength);
/* 1294:1354 */       index += localLength;
/* 1295:1355 */       dstIndex += localLength;
/* 1296:1356 */       length -= localLength;
/* 1297:1357 */       i++;
/* 1298:     */     }
/* 1299:1360 */     dst.writerIndex(dst.capacity());
/* 1300:     */   }
/* 1301:     */   
/* 1302:     */   public ByteBuf component(int cIndex)
/* 1303:     */   {
/* 1304:1370 */     return internalComponent(cIndex).duplicate();
/* 1305:     */   }
/* 1306:     */   
/* 1307:     */   public ByteBuf componentAtOffset(int offset)
/* 1308:     */   {
/* 1309:1380 */     return internalComponentAtOffset(offset).duplicate();
/* 1310:     */   }
/* 1311:     */   
/* 1312:     */   public ByteBuf internalComponent(int cIndex)
/* 1313:     */   {
/* 1314:1390 */     checkComponentIndex(cIndex);
/* 1315:1391 */     return ((Component)this.components.get(cIndex)).buf;
/* 1316:     */   }
/* 1317:     */   
/* 1318:     */   public ByteBuf internalComponentAtOffset(int offset)
/* 1319:     */   {
/* 1320:1401 */     return findComponent(offset).buf;
/* 1321:     */   }
/* 1322:     */   
/* 1323:     */   private Component findComponent(int offset)
/* 1324:     */   {
/* 1325:1405 */     checkIndex(offset);
/* 1326:     */     
/* 1327:1407 */     int low = 0;
/* 1328:1407 */     for (int high = this.components.size(); low <= high;)
/* 1329:     */     {
/* 1330:1408 */       int mid = low + high >>> 1;
/* 1331:1409 */       Component c = (Component)this.components.get(mid);
/* 1332:1410 */       if (offset >= c.endOffset)
/* 1333:     */       {
/* 1334:1411 */         low = mid + 1;
/* 1335:     */       }
/* 1336:1412 */       else if (offset < c.offset)
/* 1337:     */       {
/* 1338:1413 */         high = mid - 1;
/* 1339:     */       }
/* 1340:     */       else
/* 1341:     */       {
/* 1342:1415 */         assert (c.length != 0);
/* 1343:1416 */         return c;
/* 1344:     */       }
/* 1345:     */     }
/* 1346:1420 */     throw new Error("should not reach here");
/* 1347:     */   }
/* 1348:     */   
/* 1349:     */   public int nioBufferCount()
/* 1350:     */   {
/* 1351:1425 */     switch (this.components.size())
/* 1352:     */     {
/* 1353:     */     case 0: 
/* 1354:1427 */       return 1;
/* 1355:     */     case 1: 
/* 1356:1429 */       return ((Component)this.components.get(0)).buf.nioBufferCount();
/* 1357:     */     }
/* 1358:1431 */     int count = 0;
/* 1359:1432 */     int componentsCount = this.components.size();
/* 1360:1433 */     for (int i = 0; i < componentsCount; i++)
/* 1361:     */     {
/* 1362:1434 */       Component c = (Component)this.components.get(i);
/* 1363:1435 */       count += c.buf.nioBufferCount();
/* 1364:     */     }
/* 1365:1437 */     return count;
/* 1366:     */   }
/* 1367:     */   
/* 1368:     */   public ByteBuffer internalNioBuffer(int index, int length)
/* 1369:     */   {
/* 1370:1443 */     switch (this.components.size())
/* 1371:     */     {
/* 1372:     */     case 0: 
/* 1373:1445 */       return EMPTY_NIO_BUFFER;
/* 1374:     */     case 1: 
/* 1375:1447 */       return ((Component)this.components.get(0)).buf.internalNioBuffer(index, length);
/* 1376:     */     }
/* 1377:1449 */     throw new UnsupportedOperationException();
/* 1378:     */   }
/* 1379:     */   
/* 1380:     */   public ByteBuffer nioBuffer(int index, int length)
/* 1381:     */   {
/* 1382:1455 */     checkIndex(index, length);
/* 1383:1457 */     switch (this.components.size())
/* 1384:     */     {
/* 1385:     */     case 0: 
/* 1386:1459 */       return EMPTY_NIO_BUFFER;
/* 1387:     */     case 1: 
/* 1388:1461 */       ByteBuf buf = ((Component)this.components.get(0)).buf;
/* 1389:1462 */       if (buf.nioBufferCount() == 1) {
/* 1390:1463 */         return ((Component)this.components.get(0)).buf.nioBuffer(index, length);
/* 1391:     */       }
/* 1392:     */       break;
/* 1393:     */     }
/* 1394:1467 */     ByteBuffer merged = ByteBuffer.allocate(length).order(order());
/* 1395:1468 */     ByteBuffer[] buffers = nioBuffers(index, length);
/* 1396:1470 */     for (ByteBuffer buf : buffers) {
/* 1397:1471 */       merged.put(buf);
/* 1398:     */     }
/* 1399:1474 */     merged.flip();
/* 1400:1475 */     return merged;
/* 1401:     */   }
/* 1402:     */   
/* 1403:     */   public ByteBuffer[] nioBuffers(int index, int length)
/* 1404:     */   {
/* 1405:1480 */     checkIndex(index, length);
/* 1406:1481 */     if (length == 0) {
/* 1407:1482 */       return new ByteBuffer[] { EMPTY_NIO_BUFFER };
/* 1408:     */     }
/* 1409:1485 */     List<ByteBuffer> buffers = new ArrayList(this.components.size());
/* 1410:1486 */     int i = toComponentIndex(index);
/* 1411:1487 */     while (length > 0)
/* 1412:     */     {
/* 1413:1488 */       Component c = (Component)this.components.get(i);
/* 1414:1489 */       ByteBuf s = c.buf;
/* 1415:1490 */       int adjustment = c.offset;
/* 1416:1491 */       int localLength = Math.min(length, s.capacity() - (index - adjustment));
/* 1417:1492 */       switch (s.nioBufferCount())
/* 1418:     */       {
/* 1419:     */       case 0: 
/* 1420:1494 */         throw new UnsupportedOperationException();
/* 1421:     */       case 1: 
/* 1422:1496 */         buffers.add(s.nioBuffer(index - adjustment, localLength));
/* 1423:1497 */         break;
/* 1424:     */       default: 
/* 1425:1499 */         Collections.addAll(buffers, s.nioBuffers(index - adjustment, localLength));
/* 1426:     */       }
/* 1427:1502 */       index += localLength;
/* 1428:1503 */       length -= localLength;
/* 1429:1504 */       i++;
/* 1430:     */     }
/* 1431:1507 */     return (ByteBuffer[])buffers.toArray(new ByteBuffer[buffers.size()]);
/* 1432:     */   }
/* 1433:     */   
/* 1434:     */   public CompositeByteBuf consolidate()
/* 1435:     */   {
/* 1436:1514 */     ensureAccessible();
/* 1437:1515 */     int numComponents = numComponents();
/* 1438:1516 */     if (numComponents <= 1) {
/* 1439:1517 */       return this;
/* 1440:     */     }
/* 1441:1520 */     Component last = (Component)this.components.get(numComponents - 1);
/* 1442:1521 */     int capacity = last.endOffset;
/* 1443:1522 */     ByteBuf consolidated = allocBuffer(capacity);
/* 1444:1524 */     for (int i = 0; i < numComponents; i++)
/* 1445:     */     {
/* 1446:1525 */       Component c = (Component)this.components.get(i);
/* 1447:1526 */       ByteBuf b = c.buf;
/* 1448:1527 */       consolidated.writeBytes(b);
/* 1449:1528 */       c.freeIfNecessary();
/* 1450:     */     }
/* 1451:1531 */     this.components.clear();
/* 1452:1532 */     this.components.add(new Component(consolidated));
/* 1453:1533 */     updateComponentOffsets(0);
/* 1454:1534 */     return this;
/* 1455:     */   }
/* 1456:     */   
/* 1457:     */   public CompositeByteBuf consolidate(int cIndex, int numComponents)
/* 1458:     */   {
/* 1459:1544 */     checkComponentIndex(cIndex, numComponents);
/* 1460:1545 */     if (numComponents <= 1) {
/* 1461:1546 */       return this;
/* 1462:     */     }
/* 1463:1549 */     int endCIndex = cIndex + numComponents;
/* 1464:1550 */     Component last = (Component)this.components.get(endCIndex - 1);
/* 1465:1551 */     int capacity = last.endOffset - ((Component)this.components.get(cIndex)).offset;
/* 1466:1552 */     ByteBuf consolidated = allocBuffer(capacity);
/* 1467:1554 */     for (int i = cIndex; i < endCIndex; i++)
/* 1468:     */     {
/* 1469:1555 */       Component c = (Component)this.components.get(i);
/* 1470:1556 */       ByteBuf b = c.buf;
/* 1471:1557 */       consolidated.writeBytes(b);
/* 1472:1558 */       c.freeIfNecessary();
/* 1473:     */     }
/* 1474:1561 */     this.components.removeRange(cIndex + 1, endCIndex);
/* 1475:1562 */     this.components.set(cIndex, new Component(consolidated));
/* 1476:1563 */     updateComponentOffsets(cIndex);
/* 1477:1564 */     return this;
/* 1478:     */   }
/* 1479:     */   
/* 1480:     */   public CompositeByteBuf discardReadComponents()
/* 1481:     */   {
/* 1482:1571 */     ensureAccessible();
/* 1483:1572 */     int readerIndex = readerIndex();
/* 1484:1573 */     if (readerIndex == 0) {
/* 1485:1574 */       return this;
/* 1486:     */     }
/* 1487:1578 */     int writerIndex = writerIndex();
/* 1488:1579 */     if ((readerIndex == writerIndex) && (writerIndex == capacity()))
/* 1489:     */     {
/* 1490:1580 */       int size = this.components.size();
/* 1491:1581 */       for (int i = 0; i < size; i++) {
/* 1492:1582 */         ((Component)this.components.get(i)).freeIfNecessary();
/* 1493:     */       }
/* 1494:1584 */       this.components.clear();
/* 1495:1585 */       setIndex(0, 0);
/* 1496:1586 */       adjustMarkers(readerIndex);
/* 1497:1587 */       return this;
/* 1498:     */     }
/* 1499:1591 */     int firstComponentId = toComponentIndex(readerIndex);
/* 1500:1592 */     for (int i = 0; i < firstComponentId; i++) {
/* 1501:1593 */       ((Component)this.components.get(i)).freeIfNecessary();
/* 1502:     */     }
/* 1503:1595 */     this.components.removeRange(0, firstComponentId);
/* 1504:     */     
/* 1505:     */ 
/* 1506:1598 */     Component first = (Component)this.components.get(0);
/* 1507:1599 */     int offset = first.offset;
/* 1508:1600 */     updateComponentOffsets(0);
/* 1509:1601 */     setIndex(readerIndex - offset, writerIndex - offset);
/* 1510:1602 */     adjustMarkers(offset);
/* 1511:1603 */     return this;
/* 1512:     */   }
/* 1513:     */   
/* 1514:     */   public CompositeByteBuf discardReadBytes()
/* 1515:     */   {
/* 1516:1608 */     ensureAccessible();
/* 1517:1609 */     int readerIndex = readerIndex();
/* 1518:1610 */     if (readerIndex == 0) {
/* 1519:1611 */       return this;
/* 1520:     */     }
/* 1521:1615 */     int writerIndex = writerIndex();
/* 1522:1616 */     if ((readerIndex == writerIndex) && (writerIndex == capacity()))
/* 1523:     */     {
/* 1524:1617 */       int size = this.components.size();
/* 1525:1618 */       for (int i = 0; i < size; i++) {
/* 1526:1619 */         ((Component)this.components.get(i)).freeIfNecessary();
/* 1527:     */       }
/* 1528:1621 */       this.components.clear();
/* 1529:1622 */       setIndex(0, 0);
/* 1530:1623 */       adjustMarkers(readerIndex);
/* 1531:1624 */       return this;
/* 1532:     */     }
/* 1533:1628 */     int firstComponentId = toComponentIndex(readerIndex);
/* 1534:1629 */     for (int i = 0; i < firstComponentId; i++) {
/* 1535:1630 */       ((Component)this.components.get(i)).freeIfNecessary();
/* 1536:     */     }
/* 1537:1634 */     Component c = (Component)this.components.get(firstComponentId);
/* 1538:1635 */     int adjustment = readerIndex - c.offset;
/* 1539:1636 */     if (adjustment == c.length)
/* 1540:     */     {
/* 1541:1638 */       firstComponentId++;
/* 1542:     */     }
/* 1543:     */     else
/* 1544:     */     {
/* 1545:1640 */       Component newC = new Component(c.buf.slice(adjustment, c.length - adjustment));
/* 1546:1641 */       this.components.set(firstComponentId, newC);
/* 1547:     */     }
/* 1548:1644 */     this.components.removeRange(0, firstComponentId);
/* 1549:     */     
/* 1550:     */ 
/* 1551:1647 */     updateComponentOffsets(0);
/* 1552:1648 */     setIndex(0, writerIndex - readerIndex);
/* 1553:1649 */     adjustMarkers(readerIndex);
/* 1554:1650 */     return this;
/* 1555:     */   }
/* 1556:     */   
/* 1557:     */   private ByteBuf allocBuffer(int capacity)
/* 1558:     */   {
/* 1559:1654 */     return this.direct ? alloc().directBuffer(capacity) : alloc().heapBuffer(capacity);
/* 1560:     */   }
/* 1561:     */   
/* 1562:     */   public String toString()
/* 1563:     */   {
/* 1564:1659 */     String result = super.toString();
/* 1565:1660 */     result = result.substring(0, result.length() - 1);
/* 1566:1661 */     return result + ", components=" + this.components.size() + ')';
/* 1567:     */   }
/* 1568:     */   
/* 1569:     */   private static final class Component
/* 1570:     */   {
/* 1571:     */     final ByteBuf buf;
/* 1572:     */     final int length;
/* 1573:     */     int offset;
/* 1574:     */     int endOffset;
/* 1575:     */     
/* 1576:     */     Component(ByteBuf buf)
/* 1577:     */     {
/* 1578:1671 */       this.buf = buf;
/* 1579:1672 */       this.length = buf.readableBytes();
/* 1580:     */     }
/* 1581:     */     
/* 1582:     */     void freeIfNecessary()
/* 1583:     */     {
/* 1584:1676 */       this.buf.release();
/* 1585:     */     }
/* 1586:     */   }
/* 1587:     */   
/* 1588:     */   public CompositeByteBuf readerIndex(int readerIndex)
/* 1589:     */   {
/* 1590:1682 */     return (CompositeByteBuf)super.readerIndex(readerIndex);
/* 1591:     */   }
/* 1592:     */   
/* 1593:     */   public CompositeByteBuf writerIndex(int writerIndex)
/* 1594:     */   {
/* 1595:1687 */     return (CompositeByteBuf)super.writerIndex(writerIndex);
/* 1596:     */   }
/* 1597:     */   
/* 1598:     */   public CompositeByteBuf setIndex(int readerIndex, int writerIndex)
/* 1599:     */   {
/* 1600:1692 */     return (CompositeByteBuf)super.setIndex(readerIndex, writerIndex);
/* 1601:     */   }
/* 1602:     */   
/* 1603:     */   public CompositeByteBuf clear()
/* 1604:     */   {
/* 1605:1697 */     return (CompositeByteBuf)super.clear();
/* 1606:     */   }
/* 1607:     */   
/* 1608:     */   public CompositeByteBuf markReaderIndex()
/* 1609:     */   {
/* 1610:1702 */     return (CompositeByteBuf)super.markReaderIndex();
/* 1611:     */   }
/* 1612:     */   
/* 1613:     */   public CompositeByteBuf resetReaderIndex()
/* 1614:     */   {
/* 1615:1707 */     return (CompositeByteBuf)super.resetReaderIndex();
/* 1616:     */   }
/* 1617:     */   
/* 1618:     */   public CompositeByteBuf markWriterIndex()
/* 1619:     */   {
/* 1620:1712 */     return (CompositeByteBuf)super.markWriterIndex();
/* 1621:     */   }
/* 1622:     */   
/* 1623:     */   public CompositeByteBuf resetWriterIndex()
/* 1624:     */   {
/* 1625:1717 */     return (CompositeByteBuf)super.resetWriterIndex();
/* 1626:     */   }
/* 1627:     */   
/* 1628:     */   public CompositeByteBuf ensureWritable(int minWritableBytes)
/* 1629:     */   {
/* 1630:1722 */     return (CompositeByteBuf)super.ensureWritable(minWritableBytes);
/* 1631:     */   }
/* 1632:     */   
/* 1633:     */   public CompositeByteBuf getBytes(int index, ByteBuf dst)
/* 1634:     */   {
/* 1635:1727 */     return (CompositeByteBuf)super.getBytes(index, dst);
/* 1636:     */   }
/* 1637:     */   
/* 1638:     */   public CompositeByteBuf getBytes(int index, ByteBuf dst, int length)
/* 1639:     */   {
/* 1640:1732 */     return (CompositeByteBuf)super.getBytes(index, dst, length);
/* 1641:     */   }
/* 1642:     */   
/* 1643:     */   public CompositeByteBuf getBytes(int index, byte[] dst)
/* 1644:     */   {
/* 1645:1737 */     return (CompositeByteBuf)super.getBytes(index, dst);
/* 1646:     */   }
/* 1647:     */   
/* 1648:     */   public CompositeByteBuf setBoolean(int index, boolean value)
/* 1649:     */   {
/* 1650:1742 */     return (CompositeByteBuf)super.setBoolean(index, value);
/* 1651:     */   }
/* 1652:     */   
/* 1653:     */   public CompositeByteBuf setChar(int index, int value)
/* 1654:     */   {
/* 1655:1747 */     return (CompositeByteBuf)super.setChar(index, value);
/* 1656:     */   }
/* 1657:     */   
/* 1658:     */   public CompositeByteBuf setFloat(int index, float value)
/* 1659:     */   {
/* 1660:1752 */     return (CompositeByteBuf)super.setFloat(index, value);
/* 1661:     */   }
/* 1662:     */   
/* 1663:     */   public CompositeByteBuf setDouble(int index, double value)
/* 1664:     */   {
/* 1665:1757 */     return (CompositeByteBuf)super.setDouble(index, value);
/* 1666:     */   }
/* 1667:     */   
/* 1668:     */   public CompositeByteBuf setBytes(int index, ByteBuf src)
/* 1669:     */   {
/* 1670:1762 */     return (CompositeByteBuf)super.setBytes(index, src);
/* 1671:     */   }
/* 1672:     */   
/* 1673:     */   public CompositeByteBuf setBytes(int index, ByteBuf src, int length)
/* 1674:     */   {
/* 1675:1767 */     return (CompositeByteBuf)super.setBytes(index, src, length);
/* 1676:     */   }
/* 1677:     */   
/* 1678:     */   public CompositeByteBuf setBytes(int index, byte[] src)
/* 1679:     */   {
/* 1680:1772 */     return (CompositeByteBuf)super.setBytes(index, src);
/* 1681:     */   }
/* 1682:     */   
/* 1683:     */   public CompositeByteBuf setZero(int index, int length)
/* 1684:     */   {
/* 1685:1777 */     return (CompositeByteBuf)super.setZero(index, length);
/* 1686:     */   }
/* 1687:     */   
/* 1688:     */   public CompositeByteBuf readBytes(ByteBuf dst)
/* 1689:     */   {
/* 1690:1782 */     return (CompositeByteBuf)super.readBytes(dst);
/* 1691:     */   }
/* 1692:     */   
/* 1693:     */   public CompositeByteBuf readBytes(ByteBuf dst, int length)
/* 1694:     */   {
/* 1695:1787 */     return (CompositeByteBuf)super.readBytes(dst, length);
/* 1696:     */   }
/* 1697:     */   
/* 1698:     */   public CompositeByteBuf readBytes(ByteBuf dst, int dstIndex, int length)
/* 1699:     */   {
/* 1700:1792 */     return (CompositeByteBuf)super.readBytes(dst, dstIndex, length);
/* 1701:     */   }
/* 1702:     */   
/* 1703:     */   public CompositeByteBuf readBytes(byte[] dst)
/* 1704:     */   {
/* 1705:1797 */     return (CompositeByteBuf)super.readBytes(dst);
/* 1706:     */   }
/* 1707:     */   
/* 1708:     */   public CompositeByteBuf readBytes(byte[] dst, int dstIndex, int length)
/* 1709:     */   {
/* 1710:1802 */     return (CompositeByteBuf)super.readBytes(dst, dstIndex, length);
/* 1711:     */   }
/* 1712:     */   
/* 1713:     */   public CompositeByteBuf readBytes(ByteBuffer dst)
/* 1714:     */   {
/* 1715:1807 */     return (CompositeByteBuf)super.readBytes(dst);
/* 1716:     */   }
/* 1717:     */   
/* 1718:     */   public CompositeByteBuf readBytes(OutputStream out, int length)
/* 1719:     */     throws IOException
/* 1720:     */   {
/* 1721:1812 */     return (CompositeByteBuf)super.readBytes(out, length);
/* 1722:     */   }
/* 1723:     */   
/* 1724:     */   public CompositeByteBuf skipBytes(int length)
/* 1725:     */   {
/* 1726:1817 */     return (CompositeByteBuf)super.skipBytes(length);
/* 1727:     */   }
/* 1728:     */   
/* 1729:     */   public CompositeByteBuf writeBoolean(boolean value)
/* 1730:     */   {
/* 1731:1822 */     return (CompositeByteBuf)super.writeBoolean(value);
/* 1732:     */   }
/* 1733:     */   
/* 1734:     */   public CompositeByteBuf writeByte(int value)
/* 1735:     */   {
/* 1736:1827 */     return (CompositeByteBuf)super.writeByte(value);
/* 1737:     */   }
/* 1738:     */   
/* 1739:     */   public CompositeByteBuf writeShort(int value)
/* 1740:     */   {
/* 1741:1832 */     return (CompositeByteBuf)super.writeShort(value);
/* 1742:     */   }
/* 1743:     */   
/* 1744:     */   public CompositeByteBuf writeMedium(int value)
/* 1745:     */   {
/* 1746:1837 */     return (CompositeByteBuf)super.writeMedium(value);
/* 1747:     */   }
/* 1748:     */   
/* 1749:     */   public CompositeByteBuf writeInt(int value)
/* 1750:     */   {
/* 1751:1842 */     return (CompositeByteBuf)super.writeInt(value);
/* 1752:     */   }
/* 1753:     */   
/* 1754:     */   public CompositeByteBuf writeLong(long value)
/* 1755:     */   {
/* 1756:1847 */     return (CompositeByteBuf)super.writeLong(value);
/* 1757:     */   }
/* 1758:     */   
/* 1759:     */   public CompositeByteBuf writeChar(int value)
/* 1760:     */   {
/* 1761:1852 */     return (CompositeByteBuf)super.writeChar(value);
/* 1762:     */   }
/* 1763:     */   
/* 1764:     */   public CompositeByteBuf writeFloat(float value)
/* 1765:     */   {
/* 1766:1857 */     return (CompositeByteBuf)super.writeFloat(value);
/* 1767:     */   }
/* 1768:     */   
/* 1769:     */   public CompositeByteBuf writeDouble(double value)
/* 1770:     */   {
/* 1771:1862 */     return (CompositeByteBuf)super.writeDouble(value);
/* 1772:     */   }
/* 1773:     */   
/* 1774:     */   public CompositeByteBuf writeBytes(ByteBuf src)
/* 1775:     */   {
/* 1776:1867 */     return (CompositeByteBuf)super.writeBytes(src);
/* 1777:     */   }
/* 1778:     */   
/* 1779:     */   public CompositeByteBuf writeBytes(ByteBuf src, int length)
/* 1780:     */   {
/* 1781:1872 */     return (CompositeByteBuf)super.writeBytes(src, length);
/* 1782:     */   }
/* 1783:     */   
/* 1784:     */   public CompositeByteBuf writeBytes(ByteBuf src, int srcIndex, int length)
/* 1785:     */   {
/* 1786:1877 */     return (CompositeByteBuf)super.writeBytes(src, srcIndex, length);
/* 1787:     */   }
/* 1788:     */   
/* 1789:     */   public CompositeByteBuf writeBytes(byte[] src)
/* 1790:     */   {
/* 1791:1882 */     return (CompositeByteBuf)super.writeBytes(src);
/* 1792:     */   }
/* 1793:     */   
/* 1794:     */   public CompositeByteBuf writeBytes(byte[] src, int srcIndex, int length)
/* 1795:     */   {
/* 1796:1887 */     return (CompositeByteBuf)super.writeBytes(src, srcIndex, length);
/* 1797:     */   }
/* 1798:     */   
/* 1799:     */   public CompositeByteBuf writeBytes(ByteBuffer src)
/* 1800:     */   {
/* 1801:1892 */     return (CompositeByteBuf)super.writeBytes(src);
/* 1802:     */   }
/* 1803:     */   
/* 1804:     */   public CompositeByteBuf writeZero(int length)
/* 1805:     */   {
/* 1806:1897 */     return (CompositeByteBuf)super.writeZero(length);
/* 1807:     */   }
/* 1808:     */   
/* 1809:     */   public CompositeByteBuf retain(int increment)
/* 1810:     */   {
/* 1811:1902 */     return (CompositeByteBuf)super.retain(increment);
/* 1812:     */   }
/* 1813:     */   
/* 1814:     */   public CompositeByteBuf retain()
/* 1815:     */   {
/* 1816:1907 */     return (CompositeByteBuf)super.retain();
/* 1817:     */   }
/* 1818:     */   
/* 1819:     */   public CompositeByteBuf touch()
/* 1820:     */   {
/* 1821:1912 */     return this;
/* 1822:     */   }
/* 1823:     */   
/* 1824:     */   public CompositeByteBuf touch(Object hint)
/* 1825:     */   {
/* 1826:1917 */     return this;
/* 1827:     */   }
/* 1828:     */   
/* 1829:     */   public ByteBuffer[] nioBuffers()
/* 1830:     */   {
/* 1831:1922 */     return nioBuffers(readerIndex(), readableBytes());
/* 1832:     */   }
/* 1833:     */   
/* 1834:     */   public CompositeByteBuf discardSomeReadBytes()
/* 1835:     */   {
/* 1836:1927 */     return discardReadComponents();
/* 1837:     */   }
/* 1838:     */   
/* 1839:     */   protected void deallocate()
/* 1840:     */   {
/* 1841:1932 */     if (this.freed) {
/* 1842:1933 */       return;
/* 1843:     */     }
/* 1844:1936 */     this.freed = true;
/* 1845:1937 */     int size = this.components.size();
/* 1846:1940 */     for (int i = 0; i < size; i++) {
/* 1847:1941 */       ((Component)this.components.get(i)).freeIfNecessary();
/* 1848:     */     }
/* 1849:     */   }
/* 1850:     */   
/* 1851:     */   public ByteBuf unwrap()
/* 1852:     */   {
/* 1853:1947 */     return null;
/* 1854:     */   }
/* 1855:     */   
/* 1856:     */   private final class CompositeByteBufIterator
/* 1857:     */     implements Iterator<ByteBuf>
/* 1858:     */   {
/* 1859:1951 */     private final int size = CompositeByteBuf.this.components.size();
/* 1860:     */     private int index;
/* 1861:     */     
/* 1862:     */     private CompositeByteBufIterator() {}
/* 1863:     */     
/* 1864:     */     public boolean hasNext()
/* 1865:     */     {
/* 1866:1956 */       return this.size > this.index;
/* 1867:     */     }
/* 1868:     */     
/* 1869:     */     public ByteBuf next()
/* 1870:     */     {
/* 1871:1961 */       if (this.size != CompositeByteBuf.this.components.size()) {
/* 1872:1962 */         throw new ConcurrentModificationException();
/* 1873:     */       }
/* 1874:1964 */       if (!hasNext()) {
/* 1875:1965 */         throw new NoSuchElementException();
/* 1876:     */       }
/* 1877:     */       try
/* 1878:     */       {
/* 1879:1968 */         return ((CompositeByteBuf.Component)CompositeByteBuf.this.components.get(this.index++)).buf;
/* 1880:     */       }
/* 1881:     */       catch (IndexOutOfBoundsException e)
/* 1882:     */       {
/* 1883:1970 */         throw new ConcurrentModificationException();
/* 1884:     */       }
/* 1885:     */     }
/* 1886:     */     
/* 1887:     */     public void remove()
/* 1888:     */     {
/* 1889:1976 */       throw new UnsupportedOperationException("Read-Only");
/* 1890:     */     }
/* 1891:     */   }
/* 1892:     */   
/* 1893:     */   private static final class ComponentList
/* 1894:     */     extends ArrayList<CompositeByteBuf.Component>
/* 1895:     */   {
/* 1896:     */     ComponentList(int initialCapacity)
/* 1897:     */     {
/* 1898:1983 */       super();
/* 1899:     */     }
/* 1900:     */     
/* 1901:     */     public void removeRange(int fromIndex, int toIndex)
/* 1902:     */     {
/* 1903:1989 */       super.removeRange(fromIndex, toIndex);
/* 1904:     */     }
/* 1905:     */   }
/* 1906:     */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.CompositeByteBuf
 * JD-Core Version:    0.7.0.1
 */