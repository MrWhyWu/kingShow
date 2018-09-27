/*   1:    */ package io.netty.handler.codec.compression;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.buffer.Unpooled;
/*   6:    */ import io.netty.channel.ChannelFuture;
/*   7:    */ import io.netty.channel.ChannelFutureListener;
/*   8:    */ import io.netty.channel.ChannelHandlerContext;
/*   9:    */ import io.netty.channel.ChannelPromise;
/*  10:    */ import io.netty.channel.ChannelPromiseNotifier;
/*  11:    */ import io.netty.handler.codec.EncoderException;
/*  12:    */ import io.netty.handler.codec.MessageToByteEncoder;
/*  13:    */ import io.netty.util.concurrent.EventExecutor;
/*  14:    */ import io.netty.util.internal.ObjectUtil;
/*  15:    */ import io.netty.util.internal.ThrowableUtil;
/*  16:    */ import java.nio.ByteBuffer;
/*  17:    */ import java.util.concurrent.TimeUnit;
/*  18:    */ import java.util.zip.Checksum;
/*  19:    */ import net.jpountz.lz4.LZ4Compressor;
/*  20:    */ import net.jpountz.lz4.LZ4Exception;
/*  21:    */ import net.jpountz.lz4.LZ4Factory;
/*  22:    */ import net.jpountz.xxhash.StreamingXXHash32;
/*  23:    */ import net.jpountz.xxhash.XXHashFactory;
/*  24:    */ 
/*  25:    */ public class Lz4FrameEncoder
/*  26:    */   extends MessageToByteEncoder<ByteBuf>
/*  27:    */ {
/*  28: 72 */   private static final EncoderException ENCODE_FINSHED_EXCEPTION = (EncoderException)ThrowableUtil.unknownStackTrace(new EncoderException(new IllegalStateException("encode finished and not enough space to write remaining data")), Lz4FrameEncoder.class, "encode");
/*  29:    */   static final int DEFAULT_MAX_ENCODE_SIZE = 2147483647;
/*  30:    */   private final int blockSize;
/*  31:    */   private final LZ4Compressor compressor;
/*  32:    */   private final ByteBufChecksum checksum;
/*  33:    */   private final int compressionLevel;
/*  34:    */   private ByteBuf buffer;
/*  35:    */   private final int maxEncodeSize;
/*  36:    */   private volatile boolean finished;
/*  37:    */   private volatile ChannelHandlerContext ctx;
/*  38:    */   
/*  39:    */   public Lz4FrameEncoder()
/*  40:    */   {
/*  41:120 */     this(false);
/*  42:    */   }
/*  43:    */   
/*  44:    */   public Lz4FrameEncoder(boolean highCompressor)
/*  45:    */   {
/*  46:132 */     this(LZ4Factory.fastestInstance(), highCompressor, 65536, 
/*  47:133 */       XXHashFactory.fastestInstance().newStreamingHash32(-1756908916).asChecksum());
/*  48:    */   }
/*  49:    */   
/*  50:    */   public Lz4FrameEncoder(LZ4Factory factory, boolean highCompressor, int blockSize, Checksum checksum)
/*  51:    */   {
/*  52:149 */     this(factory, highCompressor, blockSize, checksum, 2147483647);
/*  53:    */   }
/*  54:    */   
/*  55:    */   public Lz4FrameEncoder(LZ4Factory factory, boolean highCompressor, int blockSize, Checksum checksum, int maxEncodeSize)
/*  56:    */   {
/*  57:167 */     if (factory == null) {
/*  58:168 */       throw new NullPointerException("factory");
/*  59:    */     }
/*  60:170 */     if (checksum == null) {
/*  61:171 */       throw new NullPointerException("checksum");
/*  62:    */     }
/*  63:174 */     this.compressor = (highCompressor ? factory.highCompressor() : factory.fastCompressor());
/*  64:175 */     this.checksum = ByteBufChecksum.wrapChecksum(checksum);
/*  65:    */     
/*  66:177 */     this.compressionLevel = compressionLevel(blockSize);
/*  67:178 */     this.blockSize = blockSize;
/*  68:179 */     this.maxEncodeSize = ObjectUtil.checkPositive(maxEncodeSize, "maxEncodeSize");
/*  69:180 */     this.finished = false;
/*  70:    */   }
/*  71:    */   
/*  72:    */   private static int compressionLevel(int blockSize)
/*  73:    */   {
/*  74:187 */     if ((blockSize < 64) || (blockSize > 33554432)) {
/*  75:188 */       throw new IllegalArgumentException(String.format("blockSize: %d (expected: %d-%d)", new Object[] {
/*  76:189 */         Integer.valueOf(blockSize), Integer.valueOf(64), Integer.valueOf(33554432) }));
/*  77:    */     }
/*  78:191 */     int compressionLevel = 32 - Integer.numberOfLeadingZeros(blockSize - 1);
/*  79:192 */     compressionLevel = Math.max(0, compressionLevel - 10);
/*  80:193 */     return compressionLevel;
/*  81:    */   }
/*  82:    */   
/*  83:    */   protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, ByteBuf msg, boolean preferDirect)
/*  84:    */   {
/*  85:198 */     return allocateBuffer(ctx, msg, preferDirect, true);
/*  86:    */   }
/*  87:    */   
/*  88:    */   private ByteBuf allocateBuffer(ChannelHandlerContext ctx, ByteBuf msg, boolean preferDirect, boolean allowEmptyReturn)
/*  89:    */   {
/*  90:203 */     int targetBufSize = 0;
/*  91:204 */     int remaining = msg.readableBytes() + this.buffer.readableBytes();
/*  92:207 */     if (remaining < 0) {
/*  93:208 */       throw new EncoderException("too much data to allocate a buffer for compression");
/*  94:    */     }
/*  95:211 */     while (remaining > 0)
/*  96:    */     {
/*  97:212 */       int curSize = Math.min(this.blockSize, remaining);
/*  98:213 */       remaining -= curSize;
/*  99:    */       
/* 100:215 */       targetBufSize += this.compressor.maxCompressedLength(curSize) + 21;
/* 101:    */     }
/* 102:221 */     if ((targetBufSize > this.maxEncodeSize) || (0 > targetBufSize)) {
/* 103:222 */       throw new EncoderException(String.format("requested encode buffer size (%d bytes) exceeds the maximum allowable size (%d bytes)", new Object[] {
/* 104:223 */         Integer.valueOf(targetBufSize), Integer.valueOf(this.maxEncodeSize) }));
/* 105:    */     }
/* 106:226 */     if ((allowEmptyReturn) && (targetBufSize < this.blockSize)) {
/* 107:227 */       return Unpooled.EMPTY_BUFFER;
/* 108:    */     }
/* 109:230 */     if (preferDirect) {
/* 110:231 */       return ctx.alloc().ioBuffer(targetBufSize, targetBufSize);
/* 111:    */     }
/* 112:233 */     return ctx.alloc().heapBuffer(targetBufSize, targetBufSize);
/* 113:    */   }
/* 114:    */   
/* 115:    */   protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out)
/* 116:    */     throws Exception
/* 117:    */   {
/* 118:246 */     if (this.finished)
/* 119:    */     {
/* 120:247 */       if (!out.isWritable(in.readableBytes())) {
/* 121:249 */         throw ENCODE_FINSHED_EXCEPTION;
/* 122:    */       }
/* 123:251 */       out.writeBytes(in);
/* 124:252 */       return;
/* 125:    */     }
/* 126:255 */     ByteBuf buffer = this.buffer;
/* 127:    */     int length;
/* 128:257 */     while ((length = in.readableBytes()) > 0)
/* 129:    */     {
/* 130:258 */       int nextChunkSize = Math.min(length, buffer.writableBytes());
/* 131:259 */       in.readBytes(buffer, nextChunkSize);
/* 132:261 */       if (!buffer.isWritable()) {
/* 133:262 */         flushBufferedData(out);
/* 134:    */       }
/* 135:    */     }
/* 136:    */   }
/* 137:    */   
/* 138:    */   private void flushBufferedData(ByteBuf out)
/* 139:    */   {
/* 140:268 */     int flushableBytes = this.buffer.readableBytes();
/* 141:269 */     if (flushableBytes == 0) {
/* 142:270 */       return;
/* 143:    */     }
/* 144:272 */     this.checksum.reset();
/* 145:273 */     this.checksum.update(this.buffer, this.buffer.readerIndex(), flushableBytes);
/* 146:274 */     int check = (int)this.checksum.getValue();
/* 147:    */     
/* 148:276 */     int bufSize = this.compressor.maxCompressedLength(flushableBytes) + 21;
/* 149:277 */     out.ensureWritable(bufSize);
/* 150:278 */     int idx = out.writerIndex();
/* 151:    */     try
/* 152:    */     {
/* 153:281 */       ByteBuffer outNioBuffer = out.internalNioBuffer(idx + 21, out.writableBytes() - 21);
/* 154:282 */       int pos = outNioBuffer.position();
/* 155:    */       
/* 156:284 */       this.compressor.compress(this.buffer.internalNioBuffer(this.buffer.readerIndex(), flushableBytes), outNioBuffer);
/* 157:285 */       compressedLength = outNioBuffer.position() - pos;
/* 158:    */     }
/* 159:    */     catch (LZ4Exception e)
/* 160:    */     {
/* 161:    */       int compressedLength;
/* 162:287 */       throw new CompressionException(e);
/* 163:    */     }
/* 164:    */     int compressedLength;
/* 165:    */     int blockType;
/* 166:290 */     if (compressedLength >= flushableBytes)
/* 167:    */     {
/* 168:291 */       int blockType = 16;
/* 169:292 */       compressedLength = flushableBytes;
/* 170:293 */       out.setBytes(idx + 21, this.buffer, 0, flushableBytes);
/* 171:    */     }
/* 172:    */     else
/* 173:    */     {
/* 174:295 */       blockType = 32;
/* 175:    */     }
/* 176:298 */     out.setLong(idx, 5501767354678207339L);
/* 177:299 */     out.setByte(idx + 8, (byte)(blockType | this.compressionLevel));
/* 178:300 */     out.setIntLE(idx + 9, compressedLength);
/* 179:301 */     out.setIntLE(idx + 13, flushableBytes);
/* 180:302 */     out.setIntLE(idx + 17, check);
/* 181:303 */     out.writerIndex(idx + 21 + compressedLength);
/* 182:304 */     this.buffer.clear();
/* 183:    */   }
/* 184:    */   
/* 185:    */   public void flush(ChannelHandlerContext ctx)
/* 186:    */     throws Exception
/* 187:    */   {
/* 188:309 */     if ((this.buffer != null) && (this.buffer.isReadable()))
/* 189:    */     {
/* 190:310 */       ByteBuf buf = allocateBuffer(ctx, Unpooled.EMPTY_BUFFER, isPreferDirect(), false);
/* 191:311 */       flushBufferedData(buf);
/* 192:312 */       ctx.write(buf);
/* 193:    */     }
/* 194:314 */     ctx.flush();
/* 195:    */   }
/* 196:    */   
/* 197:    */   private ChannelFuture finishEncode(ChannelHandlerContext ctx, ChannelPromise promise)
/* 198:    */   {
/* 199:318 */     if (this.finished)
/* 200:    */     {
/* 201:319 */       promise.setSuccess();
/* 202:320 */       return promise;
/* 203:    */     }
/* 204:322 */     this.finished = true;
/* 205:    */     
/* 206:324 */     ByteBuf footer = ctx.alloc().heapBuffer(this.compressor
/* 207:325 */       .maxCompressedLength(this.buffer.readableBytes()) + 21);
/* 208:326 */     flushBufferedData(footer);
/* 209:    */     
/* 210:328 */     int idx = footer.writerIndex();
/* 211:329 */     footer.setLong(idx, 5501767354678207339L);
/* 212:330 */     footer.setByte(idx + 8, (byte)(0x10 | this.compressionLevel));
/* 213:331 */     footer.setInt(idx + 9, 0);
/* 214:332 */     footer.setInt(idx + 13, 0);
/* 215:333 */     footer.setInt(idx + 17, 0);
/* 216:    */     
/* 217:335 */     footer.writerIndex(idx + 21);
/* 218:    */     
/* 219:337 */     return ctx.writeAndFlush(footer, promise);
/* 220:    */   }
/* 221:    */   
/* 222:    */   public boolean isClosed()
/* 223:    */   {
/* 224:344 */     return this.finished;
/* 225:    */   }
/* 226:    */   
/* 227:    */   public ChannelFuture close()
/* 228:    */   {
/* 229:353 */     return close(ctx().newPromise());
/* 230:    */   }
/* 231:    */   
/* 232:    */   public ChannelFuture close(final ChannelPromise promise)
/* 233:    */   {
/* 234:362 */     ChannelHandlerContext ctx = ctx();
/* 235:363 */     EventExecutor executor = ctx.executor();
/* 236:364 */     if (executor.inEventLoop()) {
/* 237:365 */       return finishEncode(ctx, promise);
/* 238:    */     }
/* 239:367 */     executor.execute(new Runnable()
/* 240:    */     {
/* 241:    */       public void run()
/* 242:    */       {
/* 243:370 */         ChannelFuture f = Lz4FrameEncoder.this.finishEncode(Lz4FrameEncoder.access$000(Lz4FrameEncoder.this), promise);
/* 244:371 */         f.addListener(new ChannelPromiseNotifier(new ChannelPromise[] { promise }));
/* 245:    */       }
/* 246:373 */     });
/* 247:374 */     return promise;
/* 248:    */   }
/* 249:    */   
/* 250:    */   public void close(final ChannelHandlerContext ctx, final ChannelPromise promise)
/* 251:    */     throws Exception
/* 252:    */   {
/* 253:380 */     ChannelFuture f = finishEncode(ctx, ctx.newPromise());
/* 254:381 */     f.addListener(new ChannelFutureListener()
/* 255:    */     {
/* 256:    */       public void operationComplete(ChannelFuture f)
/* 257:    */         throws Exception
/* 258:    */       {
/* 259:384 */         ctx.close(promise);
/* 260:    */       }
/* 261:    */     });
/* 262:388 */     if (!f.isDone()) {
/* 263:390 */       ctx.executor().schedule(new Runnable()
/* 264:    */       {
/* 265:    */         public void run()
/* 266:    */         {
/* 267:393 */           ctx.close(promise);
/* 268:    */         }
/* 269:393 */       }, 10L, TimeUnit.SECONDS);
/* 270:    */     }
/* 271:    */   }
/* 272:    */   
/* 273:    */   private ChannelHandlerContext ctx()
/* 274:    */   {
/* 275:400 */     ChannelHandlerContext ctx = this.ctx;
/* 276:401 */     if (ctx == null) {
/* 277:402 */       throw new IllegalStateException("not added to a pipeline");
/* 278:    */     }
/* 279:404 */     return ctx;
/* 280:    */   }
/* 281:    */   
/* 282:    */   public void handlerAdded(ChannelHandlerContext ctx)
/* 283:    */   {
/* 284:409 */     this.ctx = ctx;
/* 285:    */     
/* 286:411 */     this.buffer = Unpooled.wrappedBuffer(new byte[this.blockSize]);
/* 287:412 */     this.buffer.clear();
/* 288:    */   }
/* 289:    */   
/* 290:    */   public void handlerRemoved(ChannelHandlerContext ctx)
/* 291:    */     throws Exception
/* 292:    */   {
/* 293:417 */     super.handlerRemoved(ctx);
/* 294:418 */     if (this.buffer != null)
/* 295:    */     {
/* 296:419 */       this.buffer.release();
/* 297:420 */       this.buffer = null;
/* 298:    */     }
/* 299:    */   }
/* 300:    */   
/* 301:    */   final ByteBuf getBackingBuffer()
/* 302:    */   {
/* 303:425 */     return this.buffer;
/* 304:    */   }
/* 305:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.compression.Lz4FrameEncoder
 * JD-Core Version:    0.7.0.1
 */