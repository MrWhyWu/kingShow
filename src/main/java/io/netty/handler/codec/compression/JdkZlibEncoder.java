/*   1:    */ package io.netty.handler.codec.compression;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.channel.ChannelFuture;
/*   6:    */ import io.netty.channel.ChannelFutureListener;
/*   7:    */ import io.netty.channel.ChannelHandlerContext;
/*   8:    */ import io.netty.channel.ChannelPromise;
/*   9:    */ import io.netty.channel.ChannelPromiseNotifier;
/*  10:    */ import io.netty.util.concurrent.EventExecutor;
/*  11:    */ import java.util.concurrent.TimeUnit;
/*  12:    */ import java.util.zip.CRC32;
/*  13:    */ import java.util.zip.Deflater;
/*  14:    */ 
/*  15:    */ public class JdkZlibEncoder
/*  16:    */   extends ZlibEncoder
/*  17:    */ {
/*  18:    */   private final ZlibWrapper wrapper;
/*  19:    */   private final Deflater deflater;
/*  20:    */   private volatile boolean finished;
/*  21:    */   private volatile ChannelHandlerContext ctx;
/*  22: 43 */   private final CRC32 crc = new CRC32();
/*  23: 44 */   private static final byte[] gzipHeader = { 31, -117, 8, 0, 0, 0, 0, 0, 0, 0 };
/*  24: 45 */   private boolean writeHeader = true;
/*  25:    */   
/*  26:    */   public JdkZlibEncoder()
/*  27:    */   {
/*  28: 54 */     this(6);
/*  29:    */   }
/*  30:    */   
/*  31:    */   public JdkZlibEncoder(int compressionLevel)
/*  32:    */   {
/*  33: 69 */     this(ZlibWrapper.ZLIB, compressionLevel);
/*  34:    */   }
/*  35:    */   
/*  36:    */   public JdkZlibEncoder(ZlibWrapper wrapper)
/*  37:    */   {
/*  38: 79 */     this(wrapper, 6);
/*  39:    */   }
/*  40:    */   
/*  41:    */   public JdkZlibEncoder(ZlibWrapper wrapper, int compressionLevel)
/*  42:    */   {
/*  43: 94 */     if ((compressionLevel < 0) || (compressionLevel > 9)) {
/*  44: 95 */       throw new IllegalArgumentException("compressionLevel: " + compressionLevel + " (expected: 0-9)");
/*  45:    */     }
/*  46: 98 */     if (wrapper == null) {
/*  47: 99 */       throw new NullPointerException("wrapper");
/*  48:    */     }
/*  49:101 */     if (wrapper == ZlibWrapper.ZLIB_OR_NONE) {
/*  50:102 */       throw new IllegalArgumentException("wrapper '" + ZlibWrapper.ZLIB_OR_NONE + "' is not allowed for compression.");
/*  51:    */     }
/*  52:107 */     this.wrapper = wrapper;
/*  53:108 */     this.deflater = new Deflater(compressionLevel, wrapper != ZlibWrapper.ZLIB);
/*  54:    */   }
/*  55:    */   
/*  56:    */   public JdkZlibEncoder(byte[] dictionary)
/*  57:    */   {
/*  58:122 */     this(6, dictionary);
/*  59:    */   }
/*  60:    */   
/*  61:    */   public JdkZlibEncoder(int compressionLevel, byte[] dictionary)
/*  62:    */   {
/*  63:140 */     if ((compressionLevel < 0) || (compressionLevel > 9)) {
/*  64:141 */       throw new IllegalArgumentException("compressionLevel: " + compressionLevel + " (expected: 0-9)");
/*  65:    */     }
/*  66:144 */     if (dictionary == null) {
/*  67:145 */       throw new NullPointerException("dictionary");
/*  68:    */     }
/*  69:148 */     this.wrapper = ZlibWrapper.ZLIB;
/*  70:149 */     this.deflater = new Deflater(compressionLevel);
/*  71:150 */     this.deflater.setDictionary(dictionary);
/*  72:    */   }
/*  73:    */   
/*  74:    */   public ChannelFuture close()
/*  75:    */   {
/*  76:155 */     return close(ctx().newPromise());
/*  77:    */   }
/*  78:    */   
/*  79:    */   public ChannelFuture close(final ChannelPromise promise)
/*  80:    */   {
/*  81:160 */     ChannelHandlerContext ctx = ctx();
/*  82:161 */     EventExecutor executor = ctx.executor();
/*  83:162 */     if (executor.inEventLoop()) {
/*  84:163 */       return finishEncode(ctx, promise);
/*  85:    */     }
/*  86:165 */     final ChannelPromise p = ctx.newPromise();
/*  87:166 */     executor.execute(new Runnable()
/*  88:    */     {
/*  89:    */       public void run()
/*  90:    */       {
/*  91:169 */         ChannelFuture f = JdkZlibEncoder.this.finishEncode(JdkZlibEncoder.access$000(JdkZlibEncoder.this), p);
/*  92:170 */         f.addListener(new ChannelPromiseNotifier(new ChannelPromise[] { promise }));
/*  93:    */       }
/*  94:172 */     });
/*  95:173 */     return p;
/*  96:    */   }
/*  97:    */   
/*  98:    */   private ChannelHandlerContext ctx()
/*  99:    */   {
/* 100:178 */     ChannelHandlerContext ctx = this.ctx;
/* 101:179 */     if (ctx == null) {
/* 102:180 */       throw new IllegalStateException("not added to a pipeline");
/* 103:    */     }
/* 104:182 */     return ctx;
/* 105:    */   }
/* 106:    */   
/* 107:    */   public boolean isClosed()
/* 108:    */   {
/* 109:187 */     return this.finished;
/* 110:    */   }
/* 111:    */   
/* 112:    */   protected void encode(ChannelHandlerContext ctx, ByteBuf uncompressed, ByteBuf out)
/* 113:    */     throws Exception
/* 114:    */   {
/* 115:192 */     if (this.finished)
/* 116:    */     {
/* 117:193 */       out.writeBytes(uncompressed);
/* 118:194 */       return;
/* 119:    */     }
/* 120:197 */     int len = uncompressed.readableBytes();
/* 121:198 */     if (len == 0) {
/* 122:    */       return;
/* 123:    */     }
/* 124:    */     byte[] inAry;
/* 125:    */     int offset;
/* 126:204 */     if (uncompressed.hasArray())
/* 127:    */     {
/* 128:206 */       byte[] inAry = uncompressed.array();
/* 129:207 */       int offset = uncompressed.arrayOffset() + uncompressed.readerIndex();
/* 130:    */       
/* 131:209 */       uncompressed.skipBytes(len);
/* 132:    */     }
/* 133:    */     else
/* 134:    */     {
/* 135:211 */       inAry = new byte[len];
/* 136:212 */       uncompressed.readBytes(inAry);
/* 137:213 */       offset = 0;
/* 138:    */     }
/* 139:216 */     if (this.writeHeader)
/* 140:    */     {
/* 141:217 */       this.writeHeader = false;
/* 142:218 */       if (this.wrapper == ZlibWrapper.GZIP) {
/* 143:219 */         out.writeBytes(gzipHeader);
/* 144:    */       }
/* 145:    */     }
/* 146:223 */     if (this.wrapper == ZlibWrapper.GZIP) {
/* 147:224 */       this.crc.update(inAry, offset, len);
/* 148:    */     }
/* 149:227 */     this.deflater.setInput(inAry, offset, len);
/* 150:228 */     while (!this.deflater.needsInput()) {
/* 151:229 */       deflate(out);
/* 152:    */     }
/* 153:    */   }
/* 154:    */   
/* 155:    */   protected final ByteBuf allocateBuffer(ChannelHandlerContext ctx, ByteBuf msg, boolean preferDirect)
/* 156:    */     throws Exception
/* 157:    */   {
/* 158:236 */     int sizeEstimate = (int)Math.ceil(msg.readableBytes() * 1.001D) + 12;
/* 159:237 */     if (this.writeHeader) {
/* 160:238 */       switch (4.$SwitchMap$io$netty$handler$codec$compression$ZlibWrapper[this.wrapper.ordinal()])
/* 161:    */       {
/* 162:    */       case 1: 
/* 163:240 */         sizeEstimate += gzipHeader.length;
/* 164:241 */         break;
/* 165:    */       case 2: 
/* 166:243 */         sizeEstimate += 2;
/* 167:244 */         break;
/* 168:    */       }
/* 169:    */     }
/* 170:249 */     return ctx.alloc().heapBuffer(sizeEstimate);
/* 171:    */   }
/* 172:    */   
/* 173:    */   public void close(final ChannelHandlerContext ctx, final ChannelPromise promise)
/* 174:    */     throws Exception
/* 175:    */   {
/* 176:254 */     ChannelFuture f = finishEncode(ctx, ctx.newPromise());
/* 177:255 */     f.addListener(new ChannelFutureListener()
/* 178:    */     {
/* 179:    */       public void operationComplete(ChannelFuture f)
/* 180:    */         throws Exception
/* 181:    */       {
/* 182:258 */         ctx.close(promise);
/* 183:    */       }
/* 184:    */     });
/* 185:262 */     if (!f.isDone()) {
/* 186:264 */       ctx.executor().schedule(new Runnable()
/* 187:    */       {
/* 188:    */         public void run()
/* 189:    */         {
/* 190:267 */           ctx.close(promise);
/* 191:    */         }
/* 192:267 */       }, 10L, TimeUnit.SECONDS);
/* 193:    */     }
/* 194:    */   }
/* 195:    */   
/* 196:    */   private ChannelFuture finishEncode(ChannelHandlerContext ctx, ChannelPromise promise)
/* 197:    */   {
/* 198:274 */     if (this.finished)
/* 199:    */     {
/* 200:275 */       promise.setSuccess();
/* 201:276 */       return promise;
/* 202:    */     }
/* 203:279 */     this.finished = true;
/* 204:280 */     ByteBuf footer = ctx.alloc().heapBuffer();
/* 205:281 */     if ((this.writeHeader) && (this.wrapper == ZlibWrapper.GZIP))
/* 206:    */     {
/* 207:283 */       this.writeHeader = false;
/* 208:284 */       footer.writeBytes(gzipHeader);
/* 209:    */     }
/* 210:287 */     this.deflater.finish();
/* 211:289 */     while (!this.deflater.finished())
/* 212:    */     {
/* 213:290 */       deflate(footer);
/* 214:291 */       if (!footer.isWritable())
/* 215:    */       {
/* 216:293 */         ctx.write(footer);
/* 217:294 */         footer = ctx.alloc().heapBuffer();
/* 218:    */       }
/* 219:    */     }
/* 220:297 */     if (this.wrapper == ZlibWrapper.GZIP)
/* 221:    */     {
/* 222:298 */       int crcValue = (int)this.crc.getValue();
/* 223:299 */       int uncBytes = this.deflater.getTotalIn();
/* 224:300 */       footer.writeByte(crcValue);
/* 225:301 */       footer.writeByte(crcValue >>> 8);
/* 226:302 */       footer.writeByte(crcValue >>> 16);
/* 227:303 */       footer.writeByte(crcValue >>> 24);
/* 228:304 */       footer.writeByte(uncBytes);
/* 229:305 */       footer.writeByte(uncBytes >>> 8);
/* 230:306 */       footer.writeByte(uncBytes >>> 16);
/* 231:307 */       footer.writeByte(uncBytes >>> 24);
/* 232:    */     }
/* 233:309 */     this.deflater.end();
/* 234:310 */     return ctx.writeAndFlush(footer, promise);
/* 235:    */   }
/* 236:    */   
/* 237:    */   private void deflate(ByteBuf out)
/* 238:    */   {
/* 239:    */     int numBytes;
/* 240:    */     do
/* 241:    */     {
/* 242:316 */       int writerIndex = out.writerIndex();
/* 243:317 */       numBytes = this.deflater.deflate(out
/* 244:318 */         .array(), out.arrayOffset() + writerIndex, out.writableBytes(), 2);
/* 245:319 */       out.writerIndex(writerIndex + numBytes);
/* 246:320 */     } while (numBytes > 0);
/* 247:    */   }
/* 248:    */   
/* 249:    */   public void handlerAdded(ChannelHandlerContext ctx)
/* 250:    */     throws Exception
/* 251:    */   {
/* 252:325 */     this.ctx = ctx;
/* 253:    */   }
/* 254:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.compression.JdkZlibEncoder
 * JD-Core Version:    0.7.0.1
 */