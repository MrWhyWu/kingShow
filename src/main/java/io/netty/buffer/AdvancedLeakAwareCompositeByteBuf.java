/*    1:     */ package io.netty.buffer;
/*    2:     */ 
/*    3:     */ import io.netty.util.ByteProcessor;
/*    4:     */ import io.netty.util.ResourceLeakTracker;
/*    5:     */ import java.io.IOException;
/*    6:     */ import java.io.InputStream;
/*    7:     */ import java.io.OutputStream;
/*    8:     */ import java.nio.ByteBuffer;
/*    9:     */ import java.nio.ByteOrder;
/*   10:     */ import java.nio.channels.FileChannel;
/*   11:     */ import java.nio.channels.GatheringByteChannel;
/*   12:     */ import java.nio.channels.ScatteringByteChannel;
/*   13:     */ import java.nio.charset.Charset;
/*   14:     */ import java.util.Iterator;
/*   15:     */ import java.util.List;
/*   16:     */ 
/*   17:     */ final class AdvancedLeakAwareCompositeByteBuf
/*   18:     */   extends SimpleLeakAwareCompositeByteBuf
/*   19:     */ {
/*   20:     */   AdvancedLeakAwareCompositeByteBuf(CompositeByteBuf wrapped, ResourceLeakTracker<ByteBuf> leak)
/*   21:     */   {
/*   22:  39 */     super(wrapped, leak);
/*   23:     */   }
/*   24:     */   
/*   25:     */   public ByteBuf order(ByteOrder endianness)
/*   26:     */   {
/*   27:  44 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*   28:  45 */     return super.order(endianness);
/*   29:     */   }
/*   30:     */   
/*   31:     */   public ByteBuf slice()
/*   32:     */   {
/*   33:  50 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*   34:  51 */     return super.slice();
/*   35:     */   }
/*   36:     */   
/*   37:     */   public ByteBuf retainedSlice()
/*   38:     */   {
/*   39:  56 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*   40:  57 */     return super.retainedSlice();
/*   41:     */   }
/*   42:     */   
/*   43:     */   public ByteBuf slice(int index, int length)
/*   44:     */   {
/*   45:  62 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*   46:  63 */     return super.slice(index, length);
/*   47:     */   }
/*   48:     */   
/*   49:     */   public ByteBuf retainedSlice(int index, int length)
/*   50:     */   {
/*   51:  68 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*   52:  69 */     return super.retainedSlice(index, length);
/*   53:     */   }
/*   54:     */   
/*   55:     */   public ByteBuf duplicate()
/*   56:     */   {
/*   57:  74 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*   58:  75 */     return super.duplicate();
/*   59:     */   }
/*   60:     */   
/*   61:     */   public ByteBuf retainedDuplicate()
/*   62:     */   {
/*   63:  80 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*   64:  81 */     return super.retainedDuplicate();
/*   65:     */   }
/*   66:     */   
/*   67:     */   public ByteBuf readSlice(int length)
/*   68:     */   {
/*   69:  86 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*   70:  87 */     return super.readSlice(length);
/*   71:     */   }
/*   72:     */   
/*   73:     */   public ByteBuf readRetainedSlice(int length)
/*   74:     */   {
/*   75:  92 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*   76:  93 */     return super.readRetainedSlice(length);
/*   77:     */   }
/*   78:     */   
/*   79:     */   public ByteBuf asReadOnly()
/*   80:     */   {
/*   81:  98 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*   82:  99 */     return super.asReadOnly();
/*   83:     */   }
/*   84:     */   
/*   85:     */   public boolean isReadOnly()
/*   86:     */   {
/*   87: 104 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*   88: 105 */     return super.isReadOnly();
/*   89:     */   }
/*   90:     */   
/*   91:     */   public CompositeByteBuf discardReadBytes()
/*   92:     */   {
/*   93: 110 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*   94: 111 */     return super.discardReadBytes();
/*   95:     */   }
/*   96:     */   
/*   97:     */   public CompositeByteBuf discardSomeReadBytes()
/*   98:     */   {
/*   99: 116 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  100: 117 */     return super.discardSomeReadBytes();
/*  101:     */   }
/*  102:     */   
/*  103:     */   public CompositeByteBuf ensureWritable(int minWritableBytes)
/*  104:     */   {
/*  105: 122 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  106: 123 */     return super.ensureWritable(minWritableBytes);
/*  107:     */   }
/*  108:     */   
/*  109:     */   public int ensureWritable(int minWritableBytes, boolean force)
/*  110:     */   {
/*  111: 128 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  112: 129 */     return super.ensureWritable(minWritableBytes, force);
/*  113:     */   }
/*  114:     */   
/*  115:     */   public boolean getBoolean(int index)
/*  116:     */   {
/*  117: 134 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  118: 135 */     return super.getBoolean(index);
/*  119:     */   }
/*  120:     */   
/*  121:     */   public byte getByte(int index)
/*  122:     */   {
/*  123: 140 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  124: 141 */     return super.getByte(index);
/*  125:     */   }
/*  126:     */   
/*  127:     */   public short getUnsignedByte(int index)
/*  128:     */   {
/*  129: 146 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  130: 147 */     return super.getUnsignedByte(index);
/*  131:     */   }
/*  132:     */   
/*  133:     */   public short getShort(int index)
/*  134:     */   {
/*  135: 152 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  136: 153 */     return super.getShort(index);
/*  137:     */   }
/*  138:     */   
/*  139:     */   public int getUnsignedShort(int index)
/*  140:     */   {
/*  141: 158 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  142: 159 */     return super.getUnsignedShort(index);
/*  143:     */   }
/*  144:     */   
/*  145:     */   public int getMedium(int index)
/*  146:     */   {
/*  147: 164 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  148: 165 */     return super.getMedium(index);
/*  149:     */   }
/*  150:     */   
/*  151:     */   public int getUnsignedMedium(int index)
/*  152:     */   {
/*  153: 170 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  154: 171 */     return super.getUnsignedMedium(index);
/*  155:     */   }
/*  156:     */   
/*  157:     */   public int getInt(int index)
/*  158:     */   {
/*  159: 176 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  160: 177 */     return super.getInt(index);
/*  161:     */   }
/*  162:     */   
/*  163:     */   public long getUnsignedInt(int index)
/*  164:     */   {
/*  165: 182 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  166: 183 */     return super.getUnsignedInt(index);
/*  167:     */   }
/*  168:     */   
/*  169:     */   public long getLong(int index)
/*  170:     */   {
/*  171: 188 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  172: 189 */     return super.getLong(index);
/*  173:     */   }
/*  174:     */   
/*  175:     */   public char getChar(int index)
/*  176:     */   {
/*  177: 194 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  178: 195 */     return super.getChar(index);
/*  179:     */   }
/*  180:     */   
/*  181:     */   public float getFloat(int index)
/*  182:     */   {
/*  183: 200 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  184: 201 */     return super.getFloat(index);
/*  185:     */   }
/*  186:     */   
/*  187:     */   public double getDouble(int index)
/*  188:     */   {
/*  189: 206 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  190: 207 */     return super.getDouble(index);
/*  191:     */   }
/*  192:     */   
/*  193:     */   public CompositeByteBuf getBytes(int index, ByteBuf dst)
/*  194:     */   {
/*  195: 212 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  196: 213 */     return super.getBytes(index, dst);
/*  197:     */   }
/*  198:     */   
/*  199:     */   public CompositeByteBuf getBytes(int index, ByteBuf dst, int length)
/*  200:     */   {
/*  201: 218 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  202: 219 */     return super.getBytes(index, dst, length);
/*  203:     */   }
/*  204:     */   
/*  205:     */   public CompositeByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/*  206:     */   {
/*  207: 224 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  208: 225 */     return super.getBytes(index, dst, dstIndex, length);
/*  209:     */   }
/*  210:     */   
/*  211:     */   public CompositeByteBuf getBytes(int index, byte[] dst)
/*  212:     */   {
/*  213: 230 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  214: 231 */     return super.getBytes(index, dst);
/*  215:     */   }
/*  216:     */   
/*  217:     */   public CompositeByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/*  218:     */   {
/*  219: 236 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  220: 237 */     return super.getBytes(index, dst, dstIndex, length);
/*  221:     */   }
/*  222:     */   
/*  223:     */   public CompositeByteBuf getBytes(int index, ByteBuffer dst)
/*  224:     */   {
/*  225: 242 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  226: 243 */     return super.getBytes(index, dst);
/*  227:     */   }
/*  228:     */   
/*  229:     */   public CompositeByteBuf getBytes(int index, OutputStream out, int length)
/*  230:     */     throws IOException
/*  231:     */   {
/*  232: 248 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  233: 249 */     return super.getBytes(index, out, length);
/*  234:     */   }
/*  235:     */   
/*  236:     */   public int getBytes(int index, GatheringByteChannel out, int length)
/*  237:     */     throws IOException
/*  238:     */   {
/*  239: 254 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  240: 255 */     return super.getBytes(index, out, length);
/*  241:     */   }
/*  242:     */   
/*  243:     */   public CharSequence getCharSequence(int index, int length, Charset charset)
/*  244:     */   {
/*  245: 260 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  246: 261 */     return super.getCharSequence(index, length, charset);
/*  247:     */   }
/*  248:     */   
/*  249:     */   public CompositeByteBuf setBoolean(int index, boolean value)
/*  250:     */   {
/*  251: 266 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  252: 267 */     return super.setBoolean(index, value);
/*  253:     */   }
/*  254:     */   
/*  255:     */   public CompositeByteBuf setByte(int index, int value)
/*  256:     */   {
/*  257: 272 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  258: 273 */     return super.setByte(index, value);
/*  259:     */   }
/*  260:     */   
/*  261:     */   public CompositeByteBuf setShort(int index, int value)
/*  262:     */   {
/*  263: 278 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  264: 279 */     return super.setShort(index, value);
/*  265:     */   }
/*  266:     */   
/*  267:     */   public CompositeByteBuf setMedium(int index, int value)
/*  268:     */   {
/*  269: 284 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  270: 285 */     return super.setMedium(index, value);
/*  271:     */   }
/*  272:     */   
/*  273:     */   public CompositeByteBuf setInt(int index, int value)
/*  274:     */   {
/*  275: 290 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  276: 291 */     return super.setInt(index, value);
/*  277:     */   }
/*  278:     */   
/*  279:     */   public CompositeByteBuf setLong(int index, long value)
/*  280:     */   {
/*  281: 296 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  282: 297 */     return super.setLong(index, value);
/*  283:     */   }
/*  284:     */   
/*  285:     */   public CompositeByteBuf setChar(int index, int value)
/*  286:     */   {
/*  287: 302 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  288: 303 */     return super.setChar(index, value);
/*  289:     */   }
/*  290:     */   
/*  291:     */   public CompositeByteBuf setFloat(int index, float value)
/*  292:     */   {
/*  293: 308 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  294: 309 */     return super.setFloat(index, value);
/*  295:     */   }
/*  296:     */   
/*  297:     */   public CompositeByteBuf setDouble(int index, double value)
/*  298:     */   {
/*  299: 314 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  300: 315 */     return super.setDouble(index, value);
/*  301:     */   }
/*  302:     */   
/*  303:     */   public CompositeByteBuf setBytes(int index, ByteBuf src)
/*  304:     */   {
/*  305: 320 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  306: 321 */     return super.setBytes(index, src);
/*  307:     */   }
/*  308:     */   
/*  309:     */   public CompositeByteBuf setBytes(int index, ByteBuf src, int length)
/*  310:     */   {
/*  311: 326 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  312: 327 */     return super.setBytes(index, src, length);
/*  313:     */   }
/*  314:     */   
/*  315:     */   public CompositeByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length)
/*  316:     */   {
/*  317: 332 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  318: 333 */     return super.setBytes(index, src, srcIndex, length);
/*  319:     */   }
/*  320:     */   
/*  321:     */   public CompositeByteBuf setBytes(int index, byte[] src)
/*  322:     */   {
/*  323: 338 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  324: 339 */     return super.setBytes(index, src);
/*  325:     */   }
/*  326:     */   
/*  327:     */   public CompositeByteBuf setBytes(int index, byte[] src, int srcIndex, int length)
/*  328:     */   {
/*  329: 344 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  330: 345 */     return super.setBytes(index, src, srcIndex, length);
/*  331:     */   }
/*  332:     */   
/*  333:     */   public CompositeByteBuf setBytes(int index, ByteBuffer src)
/*  334:     */   {
/*  335: 350 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  336: 351 */     return super.setBytes(index, src);
/*  337:     */   }
/*  338:     */   
/*  339:     */   public int setBytes(int index, InputStream in, int length)
/*  340:     */     throws IOException
/*  341:     */   {
/*  342: 356 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  343: 357 */     return super.setBytes(index, in, length);
/*  344:     */   }
/*  345:     */   
/*  346:     */   public int setBytes(int index, ScatteringByteChannel in, int length)
/*  347:     */     throws IOException
/*  348:     */   {
/*  349: 362 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  350: 363 */     return super.setBytes(index, in, length);
/*  351:     */   }
/*  352:     */   
/*  353:     */   public CompositeByteBuf setZero(int index, int length)
/*  354:     */   {
/*  355: 368 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  356: 369 */     return super.setZero(index, length);
/*  357:     */   }
/*  358:     */   
/*  359:     */   public boolean readBoolean()
/*  360:     */   {
/*  361: 374 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  362: 375 */     return super.readBoolean();
/*  363:     */   }
/*  364:     */   
/*  365:     */   public byte readByte()
/*  366:     */   {
/*  367: 380 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  368: 381 */     return super.readByte();
/*  369:     */   }
/*  370:     */   
/*  371:     */   public short readUnsignedByte()
/*  372:     */   {
/*  373: 386 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  374: 387 */     return super.readUnsignedByte();
/*  375:     */   }
/*  376:     */   
/*  377:     */   public short readShort()
/*  378:     */   {
/*  379: 392 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  380: 393 */     return super.readShort();
/*  381:     */   }
/*  382:     */   
/*  383:     */   public int readUnsignedShort()
/*  384:     */   {
/*  385: 398 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  386: 399 */     return super.readUnsignedShort();
/*  387:     */   }
/*  388:     */   
/*  389:     */   public int readMedium()
/*  390:     */   {
/*  391: 404 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  392: 405 */     return super.readMedium();
/*  393:     */   }
/*  394:     */   
/*  395:     */   public int readUnsignedMedium()
/*  396:     */   {
/*  397: 410 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  398: 411 */     return super.readUnsignedMedium();
/*  399:     */   }
/*  400:     */   
/*  401:     */   public int readInt()
/*  402:     */   {
/*  403: 416 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  404: 417 */     return super.readInt();
/*  405:     */   }
/*  406:     */   
/*  407:     */   public long readUnsignedInt()
/*  408:     */   {
/*  409: 422 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  410: 423 */     return super.readUnsignedInt();
/*  411:     */   }
/*  412:     */   
/*  413:     */   public long readLong()
/*  414:     */   {
/*  415: 428 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  416: 429 */     return super.readLong();
/*  417:     */   }
/*  418:     */   
/*  419:     */   public char readChar()
/*  420:     */   {
/*  421: 434 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  422: 435 */     return super.readChar();
/*  423:     */   }
/*  424:     */   
/*  425:     */   public float readFloat()
/*  426:     */   {
/*  427: 440 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  428: 441 */     return super.readFloat();
/*  429:     */   }
/*  430:     */   
/*  431:     */   public double readDouble()
/*  432:     */   {
/*  433: 446 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  434: 447 */     return super.readDouble();
/*  435:     */   }
/*  436:     */   
/*  437:     */   public ByteBuf readBytes(int length)
/*  438:     */   {
/*  439: 452 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  440: 453 */     return super.readBytes(length);
/*  441:     */   }
/*  442:     */   
/*  443:     */   public CompositeByteBuf readBytes(ByteBuf dst)
/*  444:     */   {
/*  445: 458 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  446: 459 */     return super.readBytes(dst);
/*  447:     */   }
/*  448:     */   
/*  449:     */   public CompositeByteBuf readBytes(ByteBuf dst, int length)
/*  450:     */   {
/*  451: 464 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  452: 465 */     return super.readBytes(dst, length);
/*  453:     */   }
/*  454:     */   
/*  455:     */   public CompositeByteBuf readBytes(ByteBuf dst, int dstIndex, int length)
/*  456:     */   {
/*  457: 470 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  458: 471 */     return super.readBytes(dst, dstIndex, length);
/*  459:     */   }
/*  460:     */   
/*  461:     */   public CompositeByteBuf readBytes(byte[] dst)
/*  462:     */   {
/*  463: 476 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  464: 477 */     return super.readBytes(dst);
/*  465:     */   }
/*  466:     */   
/*  467:     */   public CompositeByteBuf readBytes(byte[] dst, int dstIndex, int length)
/*  468:     */   {
/*  469: 482 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  470: 483 */     return super.readBytes(dst, dstIndex, length);
/*  471:     */   }
/*  472:     */   
/*  473:     */   public CompositeByteBuf readBytes(ByteBuffer dst)
/*  474:     */   {
/*  475: 488 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  476: 489 */     return super.readBytes(dst);
/*  477:     */   }
/*  478:     */   
/*  479:     */   public CompositeByteBuf readBytes(OutputStream out, int length)
/*  480:     */     throws IOException
/*  481:     */   {
/*  482: 494 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  483: 495 */     return super.readBytes(out, length);
/*  484:     */   }
/*  485:     */   
/*  486:     */   public int readBytes(GatheringByteChannel out, int length)
/*  487:     */     throws IOException
/*  488:     */   {
/*  489: 500 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  490: 501 */     return super.readBytes(out, length);
/*  491:     */   }
/*  492:     */   
/*  493:     */   public CharSequence readCharSequence(int length, Charset charset)
/*  494:     */   {
/*  495: 506 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  496: 507 */     return super.readCharSequence(length, charset);
/*  497:     */   }
/*  498:     */   
/*  499:     */   public CompositeByteBuf skipBytes(int length)
/*  500:     */   {
/*  501: 512 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  502: 513 */     return super.skipBytes(length);
/*  503:     */   }
/*  504:     */   
/*  505:     */   public CompositeByteBuf writeBoolean(boolean value)
/*  506:     */   {
/*  507: 518 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  508: 519 */     return super.writeBoolean(value);
/*  509:     */   }
/*  510:     */   
/*  511:     */   public CompositeByteBuf writeByte(int value)
/*  512:     */   {
/*  513: 524 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  514: 525 */     return super.writeByte(value);
/*  515:     */   }
/*  516:     */   
/*  517:     */   public CompositeByteBuf writeShort(int value)
/*  518:     */   {
/*  519: 530 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  520: 531 */     return super.writeShort(value);
/*  521:     */   }
/*  522:     */   
/*  523:     */   public CompositeByteBuf writeMedium(int value)
/*  524:     */   {
/*  525: 536 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  526: 537 */     return super.writeMedium(value);
/*  527:     */   }
/*  528:     */   
/*  529:     */   public CompositeByteBuf writeInt(int value)
/*  530:     */   {
/*  531: 542 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  532: 543 */     return super.writeInt(value);
/*  533:     */   }
/*  534:     */   
/*  535:     */   public CompositeByteBuf writeLong(long value)
/*  536:     */   {
/*  537: 548 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  538: 549 */     return super.writeLong(value);
/*  539:     */   }
/*  540:     */   
/*  541:     */   public CompositeByteBuf writeChar(int value)
/*  542:     */   {
/*  543: 554 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  544: 555 */     return super.writeChar(value);
/*  545:     */   }
/*  546:     */   
/*  547:     */   public CompositeByteBuf writeFloat(float value)
/*  548:     */   {
/*  549: 560 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  550: 561 */     return super.writeFloat(value);
/*  551:     */   }
/*  552:     */   
/*  553:     */   public CompositeByteBuf writeDouble(double value)
/*  554:     */   {
/*  555: 566 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  556: 567 */     return super.writeDouble(value);
/*  557:     */   }
/*  558:     */   
/*  559:     */   public CompositeByteBuf writeBytes(ByteBuf src)
/*  560:     */   {
/*  561: 572 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  562: 573 */     return super.writeBytes(src);
/*  563:     */   }
/*  564:     */   
/*  565:     */   public CompositeByteBuf writeBytes(ByteBuf src, int length)
/*  566:     */   {
/*  567: 578 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  568: 579 */     return super.writeBytes(src, length);
/*  569:     */   }
/*  570:     */   
/*  571:     */   public CompositeByteBuf writeBytes(ByteBuf src, int srcIndex, int length)
/*  572:     */   {
/*  573: 584 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  574: 585 */     return super.writeBytes(src, srcIndex, length);
/*  575:     */   }
/*  576:     */   
/*  577:     */   public CompositeByteBuf writeBytes(byte[] src)
/*  578:     */   {
/*  579: 590 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  580: 591 */     return super.writeBytes(src);
/*  581:     */   }
/*  582:     */   
/*  583:     */   public CompositeByteBuf writeBytes(byte[] src, int srcIndex, int length)
/*  584:     */   {
/*  585: 596 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  586: 597 */     return super.writeBytes(src, srcIndex, length);
/*  587:     */   }
/*  588:     */   
/*  589:     */   public CompositeByteBuf writeBytes(ByteBuffer src)
/*  590:     */   {
/*  591: 602 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  592: 603 */     return super.writeBytes(src);
/*  593:     */   }
/*  594:     */   
/*  595:     */   public int writeBytes(InputStream in, int length)
/*  596:     */     throws IOException
/*  597:     */   {
/*  598: 608 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  599: 609 */     return super.writeBytes(in, length);
/*  600:     */   }
/*  601:     */   
/*  602:     */   public int writeBytes(ScatteringByteChannel in, int length)
/*  603:     */     throws IOException
/*  604:     */   {
/*  605: 614 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  606: 615 */     return super.writeBytes(in, length);
/*  607:     */   }
/*  608:     */   
/*  609:     */   public CompositeByteBuf writeZero(int length)
/*  610:     */   {
/*  611: 620 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  612: 621 */     return super.writeZero(length);
/*  613:     */   }
/*  614:     */   
/*  615:     */   public int writeCharSequence(CharSequence sequence, Charset charset)
/*  616:     */   {
/*  617: 626 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  618: 627 */     return super.writeCharSequence(sequence, charset);
/*  619:     */   }
/*  620:     */   
/*  621:     */   public int indexOf(int fromIndex, int toIndex, byte value)
/*  622:     */   {
/*  623: 632 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  624: 633 */     return super.indexOf(fromIndex, toIndex, value);
/*  625:     */   }
/*  626:     */   
/*  627:     */   public int bytesBefore(byte value)
/*  628:     */   {
/*  629: 638 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  630: 639 */     return super.bytesBefore(value);
/*  631:     */   }
/*  632:     */   
/*  633:     */   public int bytesBefore(int length, byte value)
/*  634:     */   {
/*  635: 644 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  636: 645 */     return super.bytesBefore(length, value);
/*  637:     */   }
/*  638:     */   
/*  639:     */   public int bytesBefore(int index, int length, byte value)
/*  640:     */   {
/*  641: 650 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  642: 651 */     return super.bytesBefore(index, length, value);
/*  643:     */   }
/*  644:     */   
/*  645:     */   public int forEachByte(ByteProcessor processor)
/*  646:     */   {
/*  647: 656 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  648: 657 */     return super.forEachByte(processor);
/*  649:     */   }
/*  650:     */   
/*  651:     */   public int forEachByte(int index, int length, ByteProcessor processor)
/*  652:     */   {
/*  653: 662 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  654: 663 */     return super.forEachByte(index, length, processor);
/*  655:     */   }
/*  656:     */   
/*  657:     */   public int forEachByteDesc(ByteProcessor processor)
/*  658:     */   {
/*  659: 668 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  660: 669 */     return super.forEachByteDesc(processor);
/*  661:     */   }
/*  662:     */   
/*  663:     */   public int forEachByteDesc(int index, int length, ByteProcessor processor)
/*  664:     */   {
/*  665: 674 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  666: 675 */     return super.forEachByteDesc(index, length, processor);
/*  667:     */   }
/*  668:     */   
/*  669:     */   public ByteBuf copy()
/*  670:     */   {
/*  671: 680 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  672: 681 */     return super.copy();
/*  673:     */   }
/*  674:     */   
/*  675:     */   public ByteBuf copy(int index, int length)
/*  676:     */   {
/*  677: 686 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  678: 687 */     return super.copy(index, length);
/*  679:     */   }
/*  680:     */   
/*  681:     */   public int nioBufferCount()
/*  682:     */   {
/*  683: 692 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  684: 693 */     return super.nioBufferCount();
/*  685:     */   }
/*  686:     */   
/*  687:     */   public ByteBuffer nioBuffer()
/*  688:     */   {
/*  689: 698 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  690: 699 */     return super.nioBuffer();
/*  691:     */   }
/*  692:     */   
/*  693:     */   public ByteBuffer nioBuffer(int index, int length)
/*  694:     */   {
/*  695: 704 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  696: 705 */     return super.nioBuffer(index, length);
/*  697:     */   }
/*  698:     */   
/*  699:     */   public ByteBuffer[] nioBuffers()
/*  700:     */   {
/*  701: 710 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  702: 711 */     return super.nioBuffers();
/*  703:     */   }
/*  704:     */   
/*  705:     */   public ByteBuffer[] nioBuffers(int index, int length)
/*  706:     */   {
/*  707: 716 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  708: 717 */     return super.nioBuffers(index, length);
/*  709:     */   }
/*  710:     */   
/*  711:     */   public ByteBuffer internalNioBuffer(int index, int length)
/*  712:     */   {
/*  713: 722 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  714: 723 */     return super.internalNioBuffer(index, length);
/*  715:     */   }
/*  716:     */   
/*  717:     */   public String toString(Charset charset)
/*  718:     */   {
/*  719: 728 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  720: 729 */     return super.toString(charset);
/*  721:     */   }
/*  722:     */   
/*  723:     */   public String toString(int index, int length, Charset charset)
/*  724:     */   {
/*  725: 734 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  726: 735 */     return super.toString(index, length, charset);
/*  727:     */   }
/*  728:     */   
/*  729:     */   public CompositeByteBuf capacity(int newCapacity)
/*  730:     */   {
/*  731: 740 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  732: 741 */     return super.capacity(newCapacity);
/*  733:     */   }
/*  734:     */   
/*  735:     */   public short getShortLE(int index)
/*  736:     */   {
/*  737: 746 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  738: 747 */     return super.getShortLE(index);
/*  739:     */   }
/*  740:     */   
/*  741:     */   public int getUnsignedShortLE(int index)
/*  742:     */   {
/*  743: 752 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  744: 753 */     return super.getUnsignedShortLE(index);
/*  745:     */   }
/*  746:     */   
/*  747:     */   public int getUnsignedMediumLE(int index)
/*  748:     */   {
/*  749: 758 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  750: 759 */     return super.getUnsignedMediumLE(index);
/*  751:     */   }
/*  752:     */   
/*  753:     */   public int getMediumLE(int index)
/*  754:     */   {
/*  755: 764 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  756: 765 */     return super.getMediumLE(index);
/*  757:     */   }
/*  758:     */   
/*  759:     */   public int getIntLE(int index)
/*  760:     */   {
/*  761: 770 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  762: 771 */     return super.getIntLE(index);
/*  763:     */   }
/*  764:     */   
/*  765:     */   public long getUnsignedIntLE(int index)
/*  766:     */   {
/*  767: 776 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  768: 777 */     return super.getUnsignedIntLE(index);
/*  769:     */   }
/*  770:     */   
/*  771:     */   public long getLongLE(int index)
/*  772:     */   {
/*  773: 782 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  774: 783 */     return super.getLongLE(index);
/*  775:     */   }
/*  776:     */   
/*  777:     */   public ByteBuf setShortLE(int index, int value)
/*  778:     */   {
/*  779: 788 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  780: 789 */     return super.setShortLE(index, value);
/*  781:     */   }
/*  782:     */   
/*  783:     */   public ByteBuf setMediumLE(int index, int value)
/*  784:     */   {
/*  785: 794 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  786: 795 */     return super.setMediumLE(index, value);
/*  787:     */   }
/*  788:     */   
/*  789:     */   public ByteBuf setIntLE(int index, int value)
/*  790:     */   {
/*  791: 800 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  792: 801 */     return super.setIntLE(index, value);
/*  793:     */   }
/*  794:     */   
/*  795:     */   public ByteBuf setLongLE(int index, long value)
/*  796:     */   {
/*  797: 806 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  798: 807 */     return super.setLongLE(index, value);
/*  799:     */   }
/*  800:     */   
/*  801:     */   public int setCharSequence(int index, CharSequence sequence, Charset charset)
/*  802:     */   {
/*  803: 812 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  804: 813 */     return super.setCharSequence(index, sequence, charset);
/*  805:     */   }
/*  806:     */   
/*  807:     */   public short readShortLE()
/*  808:     */   {
/*  809: 818 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  810: 819 */     return super.readShortLE();
/*  811:     */   }
/*  812:     */   
/*  813:     */   public int readUnsignedShortLE()
/*  814:     */   {
/*  815: 824 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  816: 825 */     return super.readUnsignedShortLE();
/*  817:     */   }
/*  818:     */   
/*  819:     */   public int readMediumLE()
/*  820:     */   {
/*  821: 830 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  822: 831 */     return super.readMediumLE();
/*  823:     */   }
/*  824:     */   
/*  825:     */   public int readUnsignedMediumLE()
/*  826:     */   {
/*  827: 836 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  828: 837 */     return super.readUnsignedMediumLE();
/*  829:     */   }
/*  830:     */   
/*  831:     */   public int readIntLE()
/*  832:     */   {
/*  833: 842 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  834: 843 */     return super.readIntLE();
/*  835:     */   }
/*  836:     */   
/*  837:     */   public long readUnsignedIntLE()
/*  838:     */   {
/*  839: 848 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  840: 849 */     return super.readUnsignedIntLE();
/*  841:     */   }
/*  842:     */   
/*  843:     */   public long readLongLE()
/*  844:     */   {
/*  845: 854 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  846: 855 */     return super.readLongLE();
/*  847:     */   }
/*  848:     */   
/*  849:     */   public ByteBuf writeShortLE(int value)
/*  850:     */   {
/*  851: 860 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  852: 861 */     return super.writeShortLE(value);
/*  853:     */   }
/*  854:     */   
/*  855:     */   public ByteBuf writeMediumLE(int value)
/*  856:     */   {
/*  857: 866 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  858: 867 */     return super.writeMediumLE(value);
/*  859:     */   }
/*  860:     */   
/*  861:     */   public ByteBuf writeIntLE(int value)
/*  862:     */   {
/*  863: 872 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  864: 873 */     return super.writeIntLE(value);
/*  865:     */   }
/*  866:     */   
/*  867:     */   public ByteBuf writeLongLE(long value)
/*  868:     */   {
/*  869: 878 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  870: 879 */     return super.writeLongLE(value);
/*  871:     */   }
/*  872:     */   
/*  873:     */   public CompositeByteBuf addComponent(ByteBuf buffer)
/*  874:     */   {
/*  875: 884 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  876: 885 */     return super.addComponent(buffer);
/*  877:     */   }
/*  878:     */   
/*  879:     */   public CompositeByteBuf addComponents(ByteBuf... buffers)
/*  880:     */   {
/*  881: 890 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  882: 891 */     return super.addComponents(buffers);
/*  883:     */   }
/*  884:     */   
/*  885:     */   public CompositeByteBuf addComponents(Iterable<ByteBuf> buffers)
/*  886:     */   {
/*  887: 896 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  888: 897 */     return super.addComponents(buffers);
/*  889:     */   }
/*  890:     */   
/*  891:     */   public CompositeByteBuf addComponent(int cIndex, ByteBuf buffer)
/*  892:     */   {
/*  893: 902 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  894: 903 */     return super.addComponent(cIndex, buffer);
/*  895:     */   }
/*  896:     */   
/*  897:     */   public CompositeByteBuf addComponents(int cIndex, ByteBuf... buffers)
/*  898:     */   {
/*  899: 908 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  900: 909 */     return super.addComponents(cIndex, buffers);
/*  901:     */   }
/*  902:     */   
/*  903:     */   public CompositeByteBuf addComponents(int cIndex, Iterable<ByteBuf> buffers)
/*  904:     */   {
/*  905: 914 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  906: 915 */     return super.addComponents(cIndex, buffers);
/*  907:     */   }
/*  908:     */   
/*  909:     */   public CompositeByteBuf addComponent(boolean increaseWriterIndex, ByteBuf buffer)
/*  910:     */   {
/*  911: 920 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  912: 921 */     return super.addComponent(increaseWriterIndex, buffer);
/*  913:     */   }
/*  914:     */   
/*  915:     */   public CompositeByteBuf addComponents(boolean increaseWriterIndex, ByteBuf... buffers)
/*  916:     */   {
/*  917: 926 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  918: 927 */     return super.addComponents(increaseWriterIndex, buffers);
/*  919:     */   }
/*  920:     */   
/*  921:     */   public CompositeByteBuf addComponents(boolean increaseWriterIndex, Iterable<ByteBuf> buffers)
/*  922:     */   {
/*  923: 932 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  924: 933 */     return super.addComponents(increaseWriterIndex, buffers);
/*  925:     */   }
/*  926:     */   
/*  927:     */   public CompositeByteBuf addComponent(boolean increaseWriterIndex, int cIndex, ByteBuf buffer)
/*  928:     */   {
/*  929: 938 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  930: 939 */     return super.addComponent(increaseWriterIndex, cIndex, buffer);
/*  931:     */   }
/*  932:     */   
/*  933:     */   public CompositeByteBuf removeComponent(int cIndex)
/*  934:     */   {
/*  935: 944 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  936: 945 */     return super.removeComponent(cIndex);
/*  937:     */   }
/*  938:     */   
/*  939:     */   public CompositeByteBuf removeComponents(int cIndex, int numComponents)
/*  940:     */   {
/*  941: 950 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  942: 951 */     return super.removeComponents(cIndex, numComponents);
/*  943:     */   }
/*  944:     */   
/*  945:     */   public Iterator<ByteBuf> iterator()
/*  946:     */   {
/*  947: 956 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  948: 957 */     return super.iterator();
/*  949:     */   }
/*  950:     */   
/*  951:     */   public List<ByteBuf> decompose(int offset, int length)
/*  952:     */   {
/*  953: 962 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  954: 963 */     return super.decompose(offset, length);
/*  955:     */   }
/*  956:     */   
/*  957:     */   public CompositeByteBuf consolidate()
/*  958:     */   {
/*  959: 968 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  960: 969 */     return super.consolidate();
/*  961:     */   }
/*  962:     */   
/*  963:     */   public CompositeByteBuf discardReadComponents()
/*  964:     */   {
/*  965: 974 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  966: 975 */     return super.discardReadComponents();
/*  967:     */   }
/*  968:     */   
/*  969:     */   public CompositeByteBuf consolidate(int cIndex, int numComponents)
/*  970:     */   {
/*  971: 980 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  972: 981 */     return super.consolidate(cIndex, numComponents);
/*  973:     */   }
/*  974:     */   
/*  975:     */   public int getBytes(int index, FileChannel out, long position, int length)
/*  976:     */     throws IOException
/*  977:     */   {
/*  978: 986 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  979: 987 */     return super.getBytes(index, out, position, length);
/*  980:     */   }
/*  981:     */   
/*  982:     */   public int setBytes(int index, FileChannel in, long position, int length)
/*  983:     */     throws IOException
/*  984:     */   {
/*  985: 992 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  986: 993 */     return super.setBytes(index, in, position, length);
/*  987:     */   }
/*  988:     */   
/*  989:     */   public int readBytes(FileChannel out, long position, int length)
/*  990:     */     throws IOException
/*  991:     */   {
/*  992: 998 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/*  993: 999 */     return super.readBytes(out, position, length);
/*  994:     */   }
/*  995:     */   
/*  996:     */   public int writeBytes(FileChannel in, long position, int length)
/*  997:     */     throws IOException
/*  998:     */   {
/*  999:1004 */     AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
/* 1000:1005 */     return super.writeBytes(in, position, length);
/* 1001:     */   }
/* 1002:     */   
/* 1003:     */   public CompositeByteBuf retain()
/* 1004:     */   {
/* 1005:1010 */     this.leak.record();
/* 1006:1011 */     return super.retain();
/* 1007:     */   }
/* 1008:     */   
/* 1009:     */   public CompositeByteBuf retain(int increment)
/* 1010:     */   {
/* 1011:1016 */     this.leak.record();
/* 1012:1017 */     return super.retain(increment);
/* 1013:     */   }
/* 1014:     */   
/* 1015:     */   public boolean release()
/* 1016:     */   {
/* 1017:1022 */     this.leak.record();
/* 1018:1023 */     return super.release();
/* 1019:     */   }
/* 1020:     */   
/* 1021:     */   public boolean release(int decrement)
/* 1022:     */   {
/* 1023:1028 */     this.leak.record();
/* 1024:1029 */     return super.release(decrement);
/* 1025:     */   }
/* 1026:     */   
/* 1027:     */   public CompositeByteBuf touch()
/* 1028:     */   {
/* 1029:1034 */     this.leak.record();
/* 1030:1035 */     return this;
/* 1031:     */   }
/* 1032:     */   
/* 1033:     */   public CompositeByteBuf touch(Object hint)
/* 1034:     */   {
/* 1035:1040 */     this.leak.record(hint);
/* 1036:1041 */     return this;
/* 1037:     */   }
/* 1038:     */   
/* 1039:     */   protected AdvancedLeakAwareByteBuf newLeakAwareByteBuf(ByteBuf wrapped, ByteBuf trackedByteBuf, ResourceLeakTracker<ByteBuf> leakTracker)
/* 1040:     */   {
/* 1041:1047 */     return new AdvancedLeakAwareByteBuf(wrapped, trackedByteBuf, leakTracker);
/* 1042:     */   }
/* 1043:     */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.AdvancedLeakAwareCompositeByteBuf
 * JD-Core Version:    0.7.0.1
 */