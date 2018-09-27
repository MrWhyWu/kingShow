/*   1:    */ package io.netty.handler.codec.compression;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.channel.ChannelHandlerContext;
/*   6:    */ import java.util.List;
/*   7:    */ import java.util.zip.CRC32;
/*   8:    */ import java.util.zip.DataFormatException;
/*   9:    */ import java.util.zip.Inflater;
/*  10:    */ 
/*  11:    */ public class JdkZlibDecoder
/*  12:    */   extends ZlibDecoder
/*  13:    */ {
/*  14:    */   private static final int FHCRC = 2;
/*  15:    */   private static final int FEXTRA = 4;
/*  16:    */   private static final int FNAME = 8;
/*  17:    */   private static final int FCOMMENT = 16;
/*  18:    */   private static final int FRESERVED = 224;
/*  19:    */   private Inflater inflater;
/*  20:    */   private final byte[] dictionary;
/*  21:    */   private final ByteBufChecksum crc;
/*  22:    */   
/*  23:    */   private static enum GzipState
/*  24:    */   {
/*  25: 44 */     HEADER_START,  HEADER_END,  FLG_READ,  XLEN_READ,  SKIP_FNAME,  SKIP_COMMENT,  PROCESS_FHCRC,  FOOTER_START;
/*  26:    */     
/*  27:    */     private GzipState() {}
/*  28:    */   }
/*  29:    */   
/*  30: 54 */   private GzipState gzipState = GzipState.HEADER_START;
/*  31: 55 */   private int flags = -1;
/*  32: 56 */   private int xlen = -1;
/*  33:    */   private volatile boolean finished;
/*  34:    */   private boolean decideZlibOrNone;
/*  35:    */   
/*  36:    */   public JdkZlibDecoder()
/*  37:    */   {
/*  38: 66 */     this(ZlibWrapper.ZLIB, null);
/*  39:    */   }
/*  40:    */   
/*  41:    */   public JdkZlibDecoder(byte[] dictionary)
/*  42:    */   {
/*  43: 75 */     this(ZlibWrapper.ZLIB, dictionary);
/*  44:    */   }
/*  45:    */   
/*  46:    */   public JdkZlibDecoder(ZlibWrapper wrapper)
/*  47:    */   {
/*  48: 84 */     this(wrapper, null);
/*  49:    */   }
/*  50:    */   
/*  51:    */   private JdkZlibDecoder(ZlibWrapper wrapper, byte[] dictionary)
/*  52:    */   {
/*  53: 88 */     if (wrapper == null) {
/*  54: 89 */       throw new NullPointerException("wrapper");
/*  55:    */     }
/*  56: 91 */     switch (1.$SwitchMap$io$netty$handler$codec$compression$ZlibWrapper[wrapper.ordinal()])
/*  57:    */     {
/*  58:    */     case 1: 
/*  59: 93 */       this.inflater = new Inflater(true);
/*  60: 94 */       this.crc = ByteBufChecksum.wrapChecksum(new CRC32());
/*  61: 95 */       break;
/*  62:    */     case 2: 
/*  63: 97 */       this.inflater = new Inflater(true);
/*  64: 98 */       this.crc = null;
/*  65: 99 */       break;
/*  66:    */     case 3: 
/*  67:101 */       this.inflater = new Inflater();
/*  68:102 */       this.crc = null;
/*  69:103 */       break;
/*  70:    */     case 4: 
/*  71:106 */       this.decideZlibOrNone = true;
/*  72:107 */       this.crc = null;
/*  73:108 */       break;
/*  74:    */     default: 
/*  75:110 */       throw new IllegalArgumentException("Only GZIP or ZLIB is supported, but you used " + wrapper);
/*  76:    */     }
/*  77:112 */     this.dictionary = dictionary;
/*  78:    */   }
/*  79:    */   
/*  80:    */   public boolean isClosed()
/*  81:    */   {
/*  82:117 */     return this.finished;
/*  83:    */   }
/*  84:    */   
/*  85:    */   protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/*  86:    */     throws Exception
/*  87:    */   {
/*  88:122 */     if (this.finished)
/*  89:    */     {
/*  90:124 */       in.skipBytes(in.readableBytes());
/*  91:125 */       return;
/*  92:    */     }
/*  93:128 */     int readableBytes = in.readableBytes();
/*  94:129 */     if (readableBytes == 0) {
/*  95:130 */       return;
/*  96:    */     }
/*  97:133 */     if (this.decideZlibOrNone)
/*  98:    */     {
/*  99:135 */       if (readableBytes < 2) {
/* 100:136 */         return;
/* 101:    */       }
/* 102:139 */       boolean nowrap = !looksLikeZlib(in.getShort(in.readerIndex()));
/* 103:140 */       this.inflater = new Inflater(nowrap);
/* 104:141 */       this.decideZlibOrNone = false;
/* 105:    */     }
/* 106:144 */     if (this.crc != null)
/* 107:    */     {
/* 108:145 */       switch (this.gzipState)
/* 109:    */       {
/* 110:    */       case FOOTER_START: 
/* 111:147 */         if (readGZIPFooter(in)) {
/* 112:148 */           this.finished = true;
/* 113:    */         }
/* 114:150 */         return;
/* 115:    */       }
/* 116:152 */       if ((this.gzipState != GzipState.HEADER_END) && 
/* 117:153 */         (!readGZIPHeader(in))) {
/* 118:154 */         return;
/* 119:    */       }
/* 120:159 */       readableBytes = in.readableBytes();
/* 121:    */     }
/* 122:162 */     if (in.hasArray())
/* 123:    */     {
/* 124:163 */       this.inflater.setInput(in.array(), in.arrayOffset() + in.readerIndex(), readableBytes);
/* 125:    */     }
/* 126:    */     else
/* 127:    */     {
/* 128:165 */       byte[] array = new byte[readableBytes];
/* 129:166 */       in.getBytes(in.readerIndex(), array);
/* 130:167 */       this.inflater.setInput(array);
/* 131:    */     }
/* 132:170 */     ByteBuf decompressed = ctx.alloc().heapBuffer(this.inflater.getRemaining() << 1);
/* 133:    */     try
/* 134:    */     {
/* 135:172 */       boolean readFooter = false;
/* 136:173 */       while (!this.inflater.needsInput())
/* 137:    */       {
/* 138:174 */         byte[] outArray = decompressed.array();
/* 139:175 */         int writerIndex = decompressed.writerIndex();
/* 140:176 */         int outIndex = decompressed.arrayOffset() + writerIndex;
/* 141:177 */         int outputLength = this.inflater.inflate(outArray, outIndex, decompressed.writableBytes());
/* 142:178 */         if (outputLength > 0)
/* 143:    */         {
/* 144:179 */           decompressed.writerIndex(writerIndex + outputLength);
/* 145:180 */           if (this.crc != null) {
/* 146:181 */             this.crc.update(outArray, outIndex, outputLength);
/* 147:    */           }
/* 148:    */         }
/* 149:184 */         else if (this.inflater.needsDictionary())
/* 150:    */         {
/* 151:185 */           if (this.dictionary == null) {
/* 152:186 */             throw new DecompressionException("decompression failure, unable to set dictionary as non was specified");
/* 153:    */           }
/* 154:189 */           this.inflater.setDictionary(this.dictionary);
/* 155:    */         }
/* 156:193 */         if (this.inflater.finished())
/* 157:    */         {
/* 158:194 */           if (this.crc == null)
/* 159:    */           {
/* 160:195 */             this.finished = true; break;
/* 161:    */           }
/* 162:197 */           readFooter = true;
/* 163:    */           
/* 164:199 */           break;
/* 165:    */         }
/* 166:201 */         decompressed.ensureWritable(this.inflater.getRemaining() << 1);
/* 167:    */       }
/* 168:205 */       in.skipBytes(readableBytes - this.inflater.getRemaining());
/* 169:207 */       if (readFooter)
/* 170:    */       {
/* 171:208 */         this.gzipState = GzipState.FOOTER_START;
/* 172:209 */         if (readGZIPFooter(in)) {
/* 173:210 */           this.finished = true;
/* 174:    */         }
/* 175:    */       }
/* 176:    */     }
/* 177:    */     catch (DataFormatException e)
/* 178:    */     {
/* 179:214 */       throw new DecompressionException("decompression failure", e);
/* 180:    */     }
/* 181:    */     finally
/* 182:    */     {
/* 183:217 */       if (decompressed.isReadable()) {
/* 184:218 */         out.add(decompressed);
/* 185:    */       } else {
/* 186:220 */         decompressed.release();
/* 187:    */       }
/* 188:    */     }
/* 189:    */   }
/* 190:    */   
/* 191:    */   protected void handlerRemoved0(ChannelHandlerContext ctx)
/* 192:    */     throws Exception
/* 193:    */   {
/* 194:227 */     super.handlerRemoved0(ctx);
/* 195:228 */     if (this.inflater != null) {
/* 196:229 */       this.inflater.end();
/* 197:    */     }
/* 198:    */   }
/* 199:    */   
/* 200:    */   private boolean readGZIPHeader(ByteBuf in)
/* 201:    */   {
/* 202:234 */     switch (1.$SwitchMap$io$netty$handler$codec$compression$JdkZlibDecoder$GzipState[this.gzipState.ordinal()])
/* 203:    */     {
/* 204:    */     case 2: 
/* 205:236 */       if (in.readableBytes() < 10) {
/* 206:237 */         return false;
/* 207:    */       }
/* 208:240 */       int magic0 = in.readByte();
/* 209:241 */       int magic1 = in.readByte();
/* 210:243 */       if (magic0 != 31) {
/* 211:244 */         throw new DecompressionException("Input is not in the GZIP format");
/* 212:    */       }
/* 213:246 */       this.crc.update(magic0);
/* 214:247 */       this.crc.update(magic1);
/* 215:    */       
/* 216:249 */       int method = in.readUnsignedByte();
/* 217:250 */       if (method != 8) {
/* 218:251 */         throw new DecompressionException("Unsupported compression method " + method + " in the GZIP header");
/* 219:    */       }
/* 220:254 */       this.crc.update(method);
/* 221:    */       
/* 222:256 */       this.flags = in.readUnsignedByte();
/* 223:257 */       this.crc.update(this.flags);
/* 224:259 */       if ((this.flags & 0xE0) != 0) {
/* 225:260 */         throw new DecompressionException("Reserved flags are set in the GZIP header");
/* 226:    */       }
/* 227:265 */       this.crc.update(in, in.readerIndex(), 4);
/* 228:266 */       in.skipBytes(4);
/* 229:    */       
/* 230:268 */       this.crc.update(in.readUnsignedByte());
/* 231:269 */       this.crc.update(in.readUnsignedByte());
/* 232:    */       
/* 233:271 */       this.gzipState = GzipState.FLG_READ;
/* 234:    */     case 3: 
/* 235:274 */       if ((this.flags & 0x4) != 0)
/* 236:    */       {
/* 237:275 */         if (in.readableBytes() < 2) {
/* 238:276 */           return false;
/* 239:    */         }
/* 240:278 */         int xlen1 = in.readUnsignedByte();
/* 241:279 */         int xlen2 = in.readUnsignedByte();
/* 242:280 */         this.crc.update(xlen1);
/* 243:281 */         this.crc.update(xlen2);
/* 244:    */         
/* 245:283 */         this.xlen |= xlen1 << 8 | xlen2;
/* 246:    */       }
/* 247:285 */       this.gzipState = GzipState.XLEN_READ;
/* 248:    */     case 4: 
/* 249:288 */       if (this.xlen != -1)
/* 250:    */       {
/* 251:289 */         if (in.readableBytes() < this.xlen) {
/* 252:290 */           return false;
/* 253:    */         }
/* 254:292 */         this.crc.update(in, in.readerIndex(), this.xlen);
/* 255:293 */         in.skipBytes(this.xlen);
/* 256:    */       }
/* 257:295 */       this.gzipState = GzipState.SKIP_FNAME;
/* 258:    */     case 5: 
/* 259:298 */       if ((this.flags & 0x8) != 0)
/* 260:    */       {
/* 261:299 */         if (!in.isReadable()) {
/* 262:300 */           return false;
/* 263:    */         }
/* 264:    */         int b;
/* 265:    */         do
/* 266:    */         {
/* 267:303 */           b = in.readUnsignedByte();
/* 268:304 */           this.crc.update(b);
/* 269:305 */         } while ((b != 0) && 
/* 270:    */         
/* 271:    */ 
/* 272:308 */           (in.isReadable()));
/* 273:    */       }
/* 274:310 */       this.gzipState = GzipState.SKIP_COMMENT;
/* 275:    */     case 6: 
/* 276:313 */       if ((this.flags & 0x10) != 0)
/* 277:    */       {
/* 278:314 */         if (!in.isReadable()) {
/* 279:315 */           return false;
/* 280:    */         }
/* 281:    */         int b;
/* 282:    */         do
/* 283:    */         {
/* 284:318 */           b = in.readUnsignedByte();
/* 285:319 */           this.crc.update(b);
/* 286:320 */         } while ((b != 0) && 
/* 287:    */         
/* 288:    */ 
/* 289:323 */           (in.isReadable()));
/* 290:    */       }
/* 291:325 */       this.gzipState = GzipState.PROCESS_FHCRC;
/* 292:    */     case 7: 
/* 293:328 */       if ((this.flags & 0x2) != 0)
/* 294:    */       {
/* 295:329 */         if (in.readableBytes() < 4) {
/* 296:330 */           return false;
/* 297:    */         }
/* 298:332 */         verifyCrc(in);
/* 299:    */       }
/* 300:334 */       this.crc.reset();
/* 301:335 */       this.gzipState = GzipState.HEADER_END;
/* 302:    */     case 8: 
/* 303:338 */       return true;
/* 304:    */     }
/* 305:340 */     throw new IllegalStateException();
/* 306:    */   }
/* 307:    */   
/* 308:    */   private boolean readGZIPFooter(ByteBuf buf)
/* 309:    */   {
/* 310:345 */     if (buf.readableBytes() < 8) {
/* 311:346 */       return false;
/* 312:    */     }
/* 313:349 */     verifyCrc(buf);
/* 314:    */     
/* 315:    */ 
/* 316:352 */     int dataLength = 0;
/* 317:353 */     for (int i = 0; i < 4; i++) {
/* 318:354 */       dataLength |= buf.readUnsignedByte() << i * 8;
/* 319:    */     }
/* 320:356 */     int readLength = this.inflater.getTotalOut();
/* 321:357 */     if (dataLength != readLength) {
/* 322:358 */       throw new DecompressionException("Number of bytes mismatch. Expected: " + dataLength + ", Got: " + readLength);
/* 323:    */     }
/* 324:361 */     return true;
/* 325:    */   }
/* 326:    */   
/* 327:    */   private void verifyCrc(ByteBuf in)
/* 328:    */   {
/* 329:365 */     long crcValue = 0L;
/* 330:366 */     for (int i = 0; i < 4; i++) {
/* 331:367 */       crcValue |= in.readUnsignedByte() << i * 8;
/* 332:    */     }
/* 333:369 */     long readCrc = this.crc.getValue();
/* 334:370 */     if (crcValue != readCrc) {
/* 335:371 */       throw new DecompressionException("CRC value mismatch. Expected: " + crcValue + ", Got: " + readCrc);
/* 336:    */     }
/* 337:    */   }
/* 338:    */   
/* 339:    */   private static boolean looksLikeZlib(short cmf_flg)
/* 340:    */   {
/* 341:384 */     return ((cmf_flg & 0x7800) == 30720) && (cmf_flg % 31 == 0);
/* 342:    */   }
/* 343:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.compression.JdkZlibDecoder
 * JD-Core Version:    0.7.0.1
 */