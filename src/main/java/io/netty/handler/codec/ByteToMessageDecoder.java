/*   1:    */ package io.netty.handler.codec;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.buffer.CompositeByteBuf;
/*   6:    */ import io.netty.buffer.Unpooled;
/*   7:    */ import io.netty.channel.Channel;
/*   8:    */ import io.netty.channel.ChannelConfig;
/*   9:    */ import io.netty.channel.ChannelHandlerContext;
/*  10:    */ import io.netty.channel.ChannelInboundHandlerAdapter;
/*  11:    */ import io.netty.channel.socket.ChannelInputShutdownEvent;
/*  12:    */ import io.netty.util.internal.StringUtil;
/*  13:    */ import java.util.List;
/*  14:    */ 
/*  15:    */ public abstract class ByteToMessageDecoder
/*  16:    */   extends ChannelInboundHandlerAdapter
/*  17:    */ {
/*  18: 75 */   public static final Cumulator MERGE_CUMULATOR = new Cumulator()
/*  19:    */   {
/*  20:    */     public ByteBuf cumulate(ByteBufAllocator alloc, ByteBuf cumulation, ByteBuf in)
/*  21:    */     {
/*  22:    */       ByteBuf buffer;
/*  23:    */       ByteBuf buffer;
/*  24: 79 */       if ((cumulation.writerIndex() > cumulation.maxCapacity() - in.readableBytes()) || 
/*  25: 80 */         (cumulation.refCnt() > 1) || (cumulation.isReadOnly())) {
/*  26: 88 */         buffer = ByteToMessageDecoder.expandCumulation(alloc, cumulation, in.readableBytes());
/*  27:    */       } else {
/*  28: 90 */         buffer = cumulation;
/*  29:    */       }
/*  30: 92 */       buffer.writeBytes(in);
/*  31: 93 */       in.release();
/*  32: 94 */       return buffer;
/*  33:    */     }
/*  34:    */   };
/*  35:103 */   public static final Cumulator COMPOSITE_CUMULATOR = new Cumulator()
/*  36:    */   {
/*  37:    */     public ByteBuf cumulate(ByteBufAllocator alloc, ByteBuf cumulation, ByteBuf in)
/*  38:    */     {
/*  39:    */       ByteBuf buffer;
/*  40:107 */       if (cumulation.refCnt() > 1)
/*  41:    */       {
/*  42:114 */         ByteBuf buffer = ByteToMessageDecoder.expandCumulation(alloc, cumulation, in.readableBytes());
/*  43:115 */         buffer.writeBytes(in);
/*  44:116 */         in.release();
/*  45:    */       }
/*  46:    */       else
/*  47:    */       {
/*  48:    */         CompositeByteBuf composite;
/*  49:    */         CompositeByteBuf composite;
/*  50:119 */         if ((cumulation instanceof CompositeByteBuf))
/*  51:    */         {
/*  52:120 */           composite = (CompositeByteBuf)cumulation;
/*  53:    */         }
/*  54:    */         else
/*  55:    */         {
/*  56:122 */           composite = alloc.compositeBuffer(2147483647);
/*  57:123 */           composite.addComponent(true, cumulation);
/*  58:    */         }
/*  59:125 */         composite.addComponent(true, in);
/*  60:126 */         buffer = composite;
/*  61:    */       }
/*  62:128 */       return buffer;
/*  63:    */     }
/*  64:    */   };
/*  65:    */   private static final byte STATE_INIT = 0;
/*  66:    */   private static final byte STATE_CALLING_CHILD_DECODE = 1;
/*  67:    */   private static final byte STATE_HANDLER_REMOVED_PENDING = 2;
/*  68:    */   ByteBuf cumulation;
/*  69:137 */   private Cumulator cumulator = MERGE_CUMULATOR;
/*  70:    */   private boolean singleDecode;
/*  71:    */   private boolean decodeWasNull;
/*  72:    */   private boolean first;
/*  73:149 */   private byte decodeState = 0;
/*  74:150 */   private int discardAfterReads = 16;
/*  75:    */   private int numReads;
/*  76:    */   
/*  77:    */   protected ByteToMessageDecoder()
/*  78:    */   {
/*  79:154 */     ensureNotSharable();
/*  80:    */   }
/*  81:    */   
/*  82:    */   public void setSingleDecode(boolean singleDecode)
/*  83:    */   {
/*  84:164 */     this.singleDecode = singleDecode;
/*  85:    */   }
/*  86:    */   
/*  87:    */   public boolean isSingleDecode()
/*  88:    */   {
/*  89:174 */     return this.singleDecode;
/*  90:    */   }
/*  91:    */   
/*  92:    */   public void setCumulator(Cumulator cumulator)
/*  93:    */   {
/*  94:181 */     if (cumulator == null) {
/*  95:182 */       throw new NullPointerException("cumulator");
/*  96:    */     }
/*  97:184 */     this.cumulator = cumulator;
/*  98:    */   }
/*  99:    */   
/* 100:    */   public void setDiscardAfterReads(int discardAfterReads)
/* 101:    */   {
/* 102:192 */     if (discardAfterReads <= 0) {
/* 103:193 */       throw new IllegalArgumentException("discardAfterReads must be > 0");
/* 104:    */     }
/* 105:195 */     this.discardAfterReads = discardAfterReads;
/* 106:    */   }
/* 107:    */   
/* 108:    */   protected int actualReadableBytes()
/* 109:    */   {
/* 110:205 */     return internalBuffer().readableBytes();
/* 111:    */   }
/* 112:    */   
/* 113:    */   protected ByteBuf internalBuffer()
/* 114:    */   {
/* 115:214 */     if (this.cumulation != null) {
/* 116:215 */       return this.cumulation;
/* 117:    */     }
/* 118:217 */     return Unpooled.EMPTY_BUFFER;
/* 119:    */   }
/* 120:    */   
/* 121:    */   public final void handlerRemoved(ChannelHandlerContext ctx)
/* 122:    */     throws Exception
/* 123:    */   {
/* 124:223 */     if (this.decodeState == 1)
/* 125:    */     {
/* 126:224 */       this.decodeState = 2;
/* 127:225 */       return;
/* 128:    */     }
/* 129:227 */     ByteBuf buf = this.cumulation;
/* 130:228 */     if (buf != null)
/* 131:    */     {
/* 132:230 */       this.cumulation = null;
/* 133:    */       
/* 134:232 */       int readable = buf.readableBytes();
/* 135:233 */       if (readable > 0)
/* 136:    */       {
/* 137:234 */         ByteBuf bytes = buf.readBytes(readable);
/* 138:235 */         buf.release();
/* 139:236 */         ctx.fireChannelRead(bytes);
/* 140:    */       }
/* 141:    */       else
/* 142:    */       {
/* 143:238 */         buf.release();
/* 144:    */       }
/* 145:241 */       this.numReads = 0;
/* 146:242 */       ctx.fireChannelReadComplete();
/* 147:    */     }
/* 148:244 */     handlerRemoved0(ctx);
/* 149:    */   }
/* 150:    */   
/* 151:    */   protected void handlerRemoved0(ChannelHandlerContext ctx)
/* 152:    */     throws Exception
/* 153:    */   {}
/* 154:    */   
/* 155:    */   public void channelRead(ChannelHandlerContext ctx, Object msg)
/* 156:    */     throws Exception
/* 157:    */   {
/* 158:255 */     if ((msg instanceof ByteBuf))
/* 159:    */     {
/* 160:256 */       CodecOutputList out = CodecOutputList.newInstance();
/* 161:    */       try
/* 162:    */       {
/* 163:258 */         ByteBuf data = (ByteBuf)msg;
/* 164:259 */         this.first = (this.cumulation == null);
/* 165:260 */         if (this.first) {
/* 166:261 */           this.cumulation = data;
/* 167:    */         } else {
/* 168:263 */           this.cumulation = this.cumulator.cumulate(ctx.alloc(), this.cumulation, data);
/* 169:    */         }
/* 170:265 */         callDecode(ctx, this.cumulation, out);
/* 171:    */       }
/* 172:    */       catch (DecoderException e)
/* 173:    */       {
/* 174:    */         int size;
/* 175:267 */         throw e;
/* 176:    */       }
/* 177:    */       catch (Exception e)
/* 178:    */       {
/* 179:269 */         throw new DecoderException(e);
/* 180:    */       }
/* 181:    */       finally
/* 182:    */       {
/* 183:271 */         if ((this.cumulation != null) && (!this.cumulation.isReadable()))
/* 184:    */         {
/* 185:272 */           this.numReads = 0;
/* 186:273 */           this.cumulation.release();
/* 187:274 */           this.cumulation = null;
/* 188:    */         }
/* 189:275 */         else if (++this.numReads >= this.discardAfterReads)
/* 190:    */         {
/* 191:278 */           this.numReads = 0;
/* 192:279 */           discardSomeReadBytes();
/* 193:    */         }
/* 194:282 */         int size = out.size();
/* 195:283 */         this.decodeWasNull = (!out.insertSinceRecycled());
/* 196:284 */         fireChannelRead(ctx, out, size);
/* 197:285 */         out.recycle();
/* 198:    */       }
/* 199:    */     }
/* 200:    */     else
/* 201:    */     {
/* 202:288 */       ctx.fireChannelRead(msg);
/* 203:    */     }
/* 204:    */   }
/* 205:    */   
/* 206:    */   static void fireChannelRead(ChannelHandlerContext ctx, List<Object> msgs, int numElements)
/* 207:    */   {
/* 208:296 */     if ((msgs instanceof CodecOutputList)) {
/* 209:297 */       fireChannelRead(ctx, (CodecOutputList)msgs, numElements);
/* 210:    */     } else {
/* 211:299 */       for (int i = 0; i < numElements; i++) {
/* 212:300 */         ctx.fireChannelRead(msgs.get(i));
/* 213:    */       }
/* 214:    */     }
/* 215:    */   }
/* 216:    */   
/* 217:    */   static void fireChannelRead(ChannelHandlerContext ctx, CodecOutputList msgs, int numElements)
/* 218:    */   {
/* 219:309 */     for (int i = 0; i < numElements; i++) {
/* 220:310 */       ctx.fireChannelRead(msgs.getUnsafe(i));
/* 221:    */     }
/* 222:    */   }
/* 223:    */   
/* 224:    */   public void channelReadComplete(ChannelHandlerContext ctx)
/* 225:    */     throws Exception
/* 226:    */   {
/* 227:316 */     this.numReads = 0;
/* 228:317 */     discardSomeReadBytes();
/* 229:318 */     if (this.decodeWasNull)
/* 230:    */     {
/* 231:319 */       this.decodeWasNull = false;
/* 232:320 */       if (!ctx.channel().config().isAutoRead()) {
/* 233:321 */         ctx.read();
/* 234:    */       }
/* 235:    */     }
/* 236:324 */     ctx.fireChannelReadComplete();
/* 237:    */   }
/* 238:    */   
/* 239:    */   protected final void discardSomeReadBytes()
/* 240:    */   {
/* 241:328 */     if ((this.cumulation != null) && (!this.first) && (this.cumulation.refCnt() == 1)) {
/* 242:336 */       this.cumulation.discardSomeReadBytes();
/* 243:    */     }
/* 244:    */   }
/* 245:    */   
/* 246:    */   public void channelInactive(ChannelHandlerContext ctx)
/* 247:    */     throws Exception
/* 248:    */   {
/* 249:342 */     channelInputClosed(ctx, true);
/* 250:    */   }
/* 251:    */   
/* 252:    */   public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
/* 253:    */     throws Exception
/* 254:    */   {
/* 255:347 */     if ((evt instanceof ChannelInputShutdownEvent)) {
/* 256:351 */       channelInputClosed(ctx, false);
/* 257:    */     }
/* 258:353 */     super.userEventTriggered(ctx, evt);
/* 259:    */   }
/* 260:    */   
/* 261:    */   private void channelInputClosed(ChannelHandlerContext ctx, boolean callChannelInactive)
/* 262:    */     throws Exception
/* 263:    */   {
/* 264:357 */     CodecOutputList out = CodecOutputList.newInstance();
/* 265:    */     try
/* 266:    */     {
/* 267:359 */       channelInputClosed(ctx, out);
/* 268:    */     }
/* 269:    */     catch (DecoderException e)
/* 270:    */     {
/* 271:    */       int size;
/* 272:361 */       throw e;
/* 273:    */     }
/* 274:    */     catch (Exception e)
/* 275:    */     {
/* 276:363 */       throw new DecoderException(e);
/* 277:    */     }
/* 278:    */     finally
/* 279:    */     {
/* 280:    */       try
/* 281:    */       {
/* 282:366 */         if (this.cumulation != null)
/* 283:    */         {
/* 284:367 */           this.cumulation.release();
/* 285:368 */           this.cumulation = null;
/* 286:    */         }
/* 287:370 */         int size = out.size();
/* 288:371 */         fireChannelRead(ctx, out, size);
/* 289:372 */         if (size > 0) {
/* 290:374 */           ctx.fireChannelReadComplete();
/* 291:    */         }
/* 292:376 */         if (callChannelInactive) {
/* 293:377 */           ctx.fireChannelInactive();
/* 294:    */         }
/* 295:    */       }
/* 296:    */       finally
/* 297:    */       {
/* 298:381 */         out.recycle();
/* 299:    */       }
/* 300:    */     }
/* 301:    */   }
/* 302:    */   
/* 303:    */   void channelInputClosed(ChannelHandlerContext ctx, List<Object> out)
/* 304:    */     throws Exception
/* 305:    */   {
/* 306:391 */     if (this.cumulation != null)
/* 307:    */     {
/* 308:392 */       callDecode(ctx, this.cumulation, out);
/* 309:393 */       decodeLast(ctx, this.cumulation, out);
/* 310:    */     }
/* 311:    */     else
/* 312:    */     {
/* 313:395 */       decodeLast(ctx, Unpooled.EMPTY_BUFFER, out);
/* 314:    */     }
/* 315:    */   }
/* 316:    */   
/* 317:    */   protected void callDecode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/* 318:    */   {
/* 319:    */     try
/* 320:    */     {
/* 321:409 */       while (in.isReadable())
/* 322:    */       {
/* 323:410 */         int outSize = out.size();
/* 324:412 */         if (outSize > 0)
/* 325:    */         {
/* 326:413 */           fireChannelRead(ctx, out, outSize);
/* 327:414 */           out.clear();
/* 328:421 */           if (!ctx.isRemoved()) {
/* 329:424 */             outSize = 0;
/* 330:    */           }
/* 331:    */         }
/* 332:    */         else
/* 333:    */         {
/* 334:427 */           int oldInputLength = in.readableBytes();
/* 335:428 */           decodeRemovalReentryProtection(ctx, in, out);
/* 336:434 */           if (!ctx.isRemoved()) {
/* 337:438 */             if (outSize == out.size())
/* 338:    */             {
/* 339:439 */               if (oldInputLength != in.readableBytes()) {
/* 340:    */                 continue;
/* 341:    */               }
/* 342:    */             }
/* 343:    */             else
/* 344:    */             {
/* 345:446 */               if (oldInputLength == in.readableBytes()) {
/* 346:448 */                 throw new DecoderException(StringUtil.simpleClassName(getClass()) + ".decode() did not read anything but decoded a message.");
/* 347:    */               }
/* 348:452 */               if (isSingleDecode()) {
/* 349:    */                 break;
/* 350:    */               }
/* 351:    */             }
/* 352:    */           }
/* 353:    */         }
/* 354:    */       }
/* 355:    */     }
/* 356:    */     catch (DecoderException e)
/* 357:    */     {
/* 358:457 */       throw e;
/* 359:    */     }
/* 360:    */     catch (Exception cause)
/* 361:    */     {
/* 362:459 */       throw new DecoderException(cause);
/* 363:    */     }
/* 364:    */   }
/* 365:    */   
/* 366:    */   protected abstract void decode(ChannelHandlerContext paramChannelHandlerContext, ByteBuf paramByteBuf, List<Object> paramList)
/* 367:    */     throws Exception;
/* 368:    */   
/* 369:    */   final void decodeRemovalReentryProtection(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/* 370:    */     throws Exception
/* 371:    */   {
/* 372:487 */     this.decodeState = 1;
/* 373:    */     try
/* 374:    */     {
/* 375:489 */       decode(ctx, in, out);
/* 376:    */     }
/* 377:    */     finally
/* 378:    */     {
/* 379:    */       boolean removePending;
/* 380:491 */       boolean removePending = this.decodeState == 2;
/* 381:492 */       this.decodeState = 0;
/* 382:493 */       if (removePending) {
/* 383:494 */         handlerRemoved(ctx);
/* 384:    */       }
/* 385:    */     }
/* 386:    */   }
/* 387:    */   
/* 388:    */   protected void decodeLast(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/* 389:    */     throws Exception
/* 390:    */   {
/* 391:507 */     if (in.isReadable()) {
/* 392:510 */       decodeRemovalReentryProtection(ctx, in, out);
/* 393:    */     }
/* 394:    */   }
/* 395:    */   
/* 396:    */   static ByteBuf expandCumulation(ByteBufAllocator alloc, ByteBuf cumulation, int readable)
/* 397:    */   {
/* 398:515 */     ByteBuf oldCumulation = cumulation;
/* 399:516 */     cumulation = alloc.buffer(oldCumulation.readableBytes() + readable);
/* 400:517 */     cumulation.writeBytes(oldCumulation);
/* 401:518 */     oldCumulation.release();
/* 402:519 */     return cumulation;
/* 403:    */   }
/* 404:    */   
/* 405:    */   public static abstract interface Cumulator
/* 406:    */   {
/* 407:    */     public abstract ByteBuf cumulate(ByteBufAllocator paramByteBufAllocator, ByteBuf paramByteBuf1, ByteBuf paramByteBuf2);
/* 408:    */   }
/* 409:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.ByteToMessageDecoder
 * JD-Core Version:    0.7.0.1
 */