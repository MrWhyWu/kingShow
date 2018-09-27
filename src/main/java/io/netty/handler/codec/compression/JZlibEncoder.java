/*   1:    */ package io.netty.handler.codec.compression;
/*   2:    */ 
/*   3:    */ import com.jcraft.jzlib.Deflater;
/*   4:    */ import com.jcraft.jzlib.JZlib;
/*   5:    */ import io.netty.buffer.ByteBuf;
/*   6:    */ import io.netty.buffer.Unpooled;
/*   7:    */ import io.netty.channel.Channel;
/*   8:    */ import io.netty.channel.ChannelFuture;
/*   9:    */ import io.netty.channel.ChannelFutureListener;
/*  10:    */ import io.netty.channel.ChannelHandlerContext;
/*  11:    */ import io.netty.channel.ChannelPromise;
/*  12:    */ import io.netty.channel.ChannelPromiseNotifier;
/*  13:    */ import io.netty.util.concurrent.EventExecutor;
/*  14:    */ import io.netty.util.internal.EmptyArrays;
/*  15:    */ import java.util.concurrent.TimeUnit;
/*  16:    */ 
/*  17:    */ public class JZlibEncoder
/*  18:    */   extends ZlibEncoder
/*  19:    */ {
/*  20:    */   private final int wrapperOverhead;
/*  21: 38 */   private final Deflater z = new Deflater();
/*  22:    */   private volatile boolean finished;
/*  23:    */   private volatile ChannelHandlerContext ctx;
/*  24:    */   
/*  25:    */   public JZlibEncoder()
/*  26:    */   {
/*  27: 50 */     this(6);
/*  28:    */   }
/*  29:    */   
/*  30:    */   public JZlibEncoder(int compressionLevel)
/*  31:    */   {
/*  32: 66 */     this(ZlibWrapper.ZLIB, compressionLevel);
/*  33:    */   }
/*  34:    */   
/*  35:    */   public JZlibEncoder(ZlibWrapper wrapper)
/*  36:    */   {
/*  37: 77 */     this(wrapper, 6);
/*  38:    */   }
/*  39:    */   
/*  40:    */   public JZlibEncoder(ZlibWrapper wrapper, int compressionLevel)
/*  41:    */   {
/*  42: 93 */     this(wrapper, compressionLevel, 15, 8);
/*  43:    */   }
/*  44:    */   
/*  45:    */   public JZlibEncoder(ZlibWrapper wrapper, int compressionLevel, int windowBits, int memLevel)
/*  46:    */   {
/*  47:120 */     if ((compressionLevel < 0) || (compressionLevel > 9)) {
/*  48:121 */       throw new IllegalArgumentException("compressionLevel: " + compressionLevel + " (expected: 0-9)");
/*  49:    */     }
/*  50:125 */     if ((windowBits < 9) || (windowBits > 15)) {
/*  51:126 */       throw new IllegalArgumentException("windowBits: " + windowBits + " (expected: 9-15)");
/*  52:    */     }
/*  53:129 */     if ((memLevel < 1) || (memLevel > 9)) {
/*  54:130 */       throw new IllegalArgumentException("memLevel: " + memLevel + " (expected: 1-9)");
/*  55:    */     }
/*  56:133 */     if (wrapper == null) {
/*  57:134 */       throw new NullPointerException("wrapper");
/*  58:    */     }
/*  59:136 */     if (wrapper == ZlibWrapper.ZLIB_OR_NONE) {
/*  60:137 */       throw new IllegalArgumentException("wrapper '" + ZlibWrapper.ZLIB_OR_NONE + "' is not allowed for compression.");
/*  61:    */     }
/*  62:142 */     int resultCode = this.z.init(compressionLevel, windowBits, memLevel, 
/*  63:    */     
/*  64:144 */       ZlibUtil.convertWrapperType(wrapper));
/*  65:145 */     if (resultCode != 0) {
/*  66:146 */       ZlibUtil.fail(this.z, "initialization failure", resultCode);
/*  67:    */     }
/*  68:149 */     this.wrapperOverhead = ZlibUtil.wrapperOverhead(wrapper);
/*  69:    */   }
/*  70:    */   
/*  71:    */   public JZlibEncoder(byte[] dictionary)
/*  72:    */   {
/*  73:164 */     this(6, dictionary);
/*  74:    */   }
/*  75:    */   
/*  76:    */   public JZlibEncoder(int compressionLevel, byte[] dictionary)
/*  77:    */   {
/*  78:183 */     this(compressionLevel, 15, 8, dictionary);
/*  79:    */   }
/*  80:    */   
/*  81:    */   public JZlibEncoder(int compressionLevel, int windowBits, int memLevel, byte[] dictionary)
/*  82:    */   {
/*  83:212 */     if ((compressionLevel < 0) || (compressionLevel > 9)) {
/*  84:213 */       throw new IllegalArgumentException("compressionLevel: " + compressionLevel + " (expected: 0-9)");
/*  85:    */     }
/*  86:215 */     if ((windowBits < 9) || (windowBits > 15)) {
/*  87:216 */       throw new IllegalArgumentException("windowBits: " + windowBits + " (expected: 9-15)");
/*  88:    */     }
/*  89:219 */     if ((memLevel < 1) || (memLevel > 9)) {
/*  90:220 */       throw new IllegalArgumentException("memLevel: " + memLevel + " (expected: 1-9)");
/*  91:    */     }
/*  92:223 */     if (dictionary == null) {
/*  93:224 */       throw new NullPointerException("dictionary");
/*  94:    */     }
/*  95:227 */     int resultCode = this.z.deflateInit(compressionLevel, windowBits, memLevel, JZlib.W_ZLIB);
/*  96:230 */     if (resultCode != 0)
/*  97:    */     {
/*  98:231 */       ZlibUtil.fail(this.z, "initialization failure", resultCode);
/*  99:    */     }
/* 100:    */     else
/* 101:    */     {
/* 102:233 */       resultCode = this.z.deflateSetDictionary(dictionary, dictionary.length);
/* 103:234 */       if (resultCode != 0) {
/* 104:235 */         ZlibUtil.fail(this.z, "failed to set the dictionary", resultCode);
/* 105:    */       }
/* 106:    */     }
/* 107:239 */     this.wrapperOverhead = ZlibUtil.wrapperOverhead(ZlibWrapper.ZLIB);
/* 108:    */   }
/* 109:    */   
/* 110:    */   public ChannelFuture close()
/* 111:    */   {
/* 112:244 */     return close(ctx().channel().newPromise());
/* 113:    */   }
/* 114:    */   
/* 115:    */   public ChannelFuture close(final ChannelPromise promise)
/* 116:    */   {
/* 117:249 */     ChannelHandlerContext ctx = ctx();
/* 118:250 */     EventExecutor executor = ctx.executor();
/* 119:251 */     if (executor.inEventLoop()) {
/* 120:252 */       return finishEncode(ctx, promise);
/* 121:    */     }
/* 122:254 */     final ChannelPromise p = ctx.newPromise();
/* 123:255 */     executor.execute(new Runnable()
/* 124:    */     {
/* 125:    */       public void run()
/* 126:    */       {
/* 127:258 */         ChannelFuture f = JZlibEncoder.this.finishEncode(JZlibEncoder.access$000(JZlibEncoder.this), p);
/* 128:259 */         f.addListener(new ChannelPromiseNotifier(new ChannelPromise[] { promise }));
/* 129:    */       }
/* 130:261 */     });
/* 131:262 */     return p;
/* 132:    */   }
/* 133:    */   
/* 134:    */   private ChannelHandlerContext ctx()
/* 135:    */   {
/* 136:267 */     ChannelHandlerContext ctx = this.ctx;
/* 137:268 */     if (ctx == null) {
/* 138:269 */       throw new IllegalStateException("not added to a pipeline");
/* 139:    */     }
/* 140:271 */     return ctx;
/* 141:    */   }
/* 142:    */   
/* 143:    */   public boolean isClosed()
/* 144:    */   {
/* 145:276 */     return this.finished;
/* 146:    */   }
/* 147:    */   
/* 148:    */   protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out)
/* 149:    */     throws Exception
/* 150:    */   {
/* 151:281 */     if (this.finished)
/* 152:    */     {
/* 153:282 */       out.writeBytes(in);
/* 154:283 */       return;
/* 155:    */     }
/* 156:286 */     int inputLength = in.readableBytes();
/* 157:287 */     if (inputLength == 0) {
/* 158:288 */       return;
/* 159:    */     }
/* 160:    */     try
/* 161:    */     {
/* 162:293 */       boolean inHasArray = in.hasArray();
/* 163:294 */       this.z.avail_in = inputLength;
/* 164:295 */       if (inHasArray)
/* 165:    */       {
/* 166:296 */         this.z.next_in = in.array();
/* 167:297 */         this.z.next_in_index = (in.arrayOffset() + in.readerIndex());
/* 168:    */       }
/* 169:    */       else
/* 170:    */       {
/* 171:299 */         byte[] array = new byte[inputLength];
/* 172:300 */         in.getBytes(in.readerIndex(), array);
/* 173:301 */         this.z.next_in = array;
/* 174:302 */         this.z.next_in_index = 0;
/* 175:    */       }
/* 176:304 */       int oldNextInIndex = this.z.next_in_index;
/* 177:    */       
/* 178:    */ 
/* 179:307 */       int maxOutputLength = (int)Math.ceil(inputLength * 1.001D) + 12 + this.wrapperOverhead;
/* 180:308 */       out.ensureWritable(maxOutputLength);
/* 181:309 */       this.z.avail_out = maxOutputLength;
/* 182:310 */       this.z.next_out = out.array();
/* 183:311 */       this.z.next_out_index = (out.arrayOffset() + out.writerIndex());
/* 184:312 */       int oldNextOutIndex = this.z.next_out_index;
/* 185:    */       try
/* 186:    */       {
/* 187:317 */         resultCode = this.z.deflate(2);
/* 188:    */       }
/* 189:    */       finally
/* 190:    */       {
/* 191:    */         int resultCode;
/* 192:319 */         in.skipBytes(this.z.next_in_index - oldNextInIndex);
/* 193:    */       }
/* 194:    */       int resultCode;
/* 195:322 */       if (resultCode != 0) {
/* 196:323 */         ZlibUtil.fail(this.z, "compression failure", resultCode);
/* 197:    */       }
/* 198:326 */       int outputLength = this.z.next_out_index - oldNextOutIndex;
/* 199:327 */       if (outputLength > 0) {
/* 200:328 */         out.writerIndex(out.writerIndex() + outputLength);
/* 201:    */       }
/* 202:    */     }
/* 203:    */     finally
/* 204:    */     {
/* 205:335 */       this.z.next_in = null;
/* 206:336 */       this.z.next_out = null;
/* 207:    */     }
/* 208:    */   }
/* 209:    */   
/* 210:    */   public void close(final ChannelHandlerContext ctx, final ChannelPromise promise)
/* 211:    */   {
/* 212:344 */     ChannelFuture f = finishEncode(ctx, ctx.newPromise());
/* 213:345 */     f.addListener(new ChannelFutureListener()
/* 214:    */     {
/* 215:    */       public void operationComplete(ChannelFuture f)
/* 216:    */         throws Exception
/* 217:    */       {
/* 218:348 */         ctx.close(promise);
/* 219:    */       }
/* 220:    */     });
/* 221:352 */     if (!f.isDone()) {
/* 222:354 */       ctx.executor().schedule(new Runnable()
/* 223:    */       {
/* 224:    */         public void run()
/* 225:    */         {
/* 226:357 */           ctx.close(promise);
/* 227:    */         }
/* 228:357 */       }, 10L, TimeUnit.SECONDS);
/* 229:    */     }
/* 230:    */   }
/* 231:    */   
/* 232:    */   private ChannelFuture finishEncode(ChannelHandlerContext ctx, ChannelPromise promise)
/* 233:    */   {
/* 234:364 */     if (this.finished)
/* 235:    */     {
/* 236:365 */       promise.setSuccess();
/* 237:366 */       return promise;
/* 238:    */     }
/* 239:368 */     this.finished = true;
/* 240:    */     try
/* 241:    */     {
/* 242:373 */       this.z.next_in = EmptyArrays.EMPTY_BYTES;
/* 243:374 */       this.z.next_in_index = 0;
/* 244:375 */       this.z.avail_in = 0;
/* 245:    */       
/* 246:    */ 
/* 247:378 */       byte[] out = new byte[32];
/* 248:379 */       this.z.next_out = out;
/* 249:380 */       this.z.next_out_index = 0;
/* 250:381 */       this.z.avail_out = out.length;
/* 251:    */       
/* 252:    */ 
/* 253:384 */       int resultCode = this.z.deflate(4);
/* 254:385 */       if ((resultCode != 0) && (resultCode != 1))
/* 255:    */       {
/* 256:386 */         promise.setFailure(ZlibUtil.deflaterException(this.z, "compression failure", resultCode));
/* 257:387 */         return promise;
/* 258:    */       }
/* 259:    */       ByteBuf footer;
/* 260:388 */       if (this.z.next_out_index != 0) {
/* 261:389 */         footer = Unpooled.wrappedBuffer(out, 0, this.z.next_out_index);
/* 262:    */       } else {
/* 263:391 */         footer = Unpooled.EMPTY_BUFFER;
/* 264:    */       }
/* 265:    */     }
/* 266:    */     finally
/* 267:    */     {
/* 268:    */       ByteBuf footer;
/* 269:394 */       this.z.deflateEnd();
/* 270:    */       
/* 271:    */ 
/* 272:    */ 
/* 273:    */ 
/* 274:    */ 
/* 275:400 */       this.z.next_in = null;
/* 276:401 */       this.z.next_out = null;
/* 277:    */     }
/* 278:    */     ByteBuf footer;
/* 279:403 */     return ctx.writeAndFlush(footer, promise);
/* 280:    */   }
/* 281:    */   
/* 282:    */   public void handlerAdded(ChannelHandlerContext ctx)
/* 283:    */     throws Exception
/* 284:    */   {
/* 285:408 */     this.ctx = ctx;
/* 286:    */   }
/* 287:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.compression.JZlibEncoder
 * JD-Core Version:    0.7.0.1
 */