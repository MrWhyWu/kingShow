/*   1:    */ package io.netty.handler.codec;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.ChannelHandlerContext;
/*   5:    */ import java.util.List;
/*   6:    */ 
/*   7:    */ public class DelimiterBasedFrameDecoder
/*   8:    */   extends ByteToMessageDecoder
/*   9:    */ {
/*  10:    */   private final ByteBuf[] delimiters;
/*  11:    */   private final int maxFrameLength;
/*  12:    */   private final boolean stripDelimiter;
/*  13:    */   private final boolean failFast;
/*  14:    */   private boolean discardingTooLongFrame;
/*  15:    */   private int tooLongFrameLength;
/*  16:    */   private final LineBasedFrameDecoder lineBasedDecoder;
/*  17:    */   
/*  18:    */   public DelimiterBasedFrameDecoder(int maxFrameLength, ByteBuf delimiter)
/*  19:    */   {
/*  20: 78 */     this(maxFrameLength, true, delimiter);
/*  21:    */   }
/*  22:    */   
/*  23:    */   public DelimiterBasedFrameDecoder(int maxFrameLength, boolean stripDelimiter, ByteBuf delimiter)
/*  24:    */   {
/*  25: 93 */     this(maxFrameLength, stripDelimiter, true, delimiter);
/*  26:    */   }
/*  27:    */   
/*  28:    */   public DelimiterBasedFrameDecoder(int maxFrameLength, boolean stripDelimiter, boolean failFast, ByteBuf delimiter)
/*  29:    */   {
/*  30:116 */     this(maxFrameLength, stripDelimiter, failFast, new ByteBuf[] {delimiter
/*  31:117 */       .slice(delimiter.readerIndex(), delimiter.readableBytes()) });
/*  32:    */   }
/*  33:    */   
/*  34:    */   public DelimiterBasedFrameDecoder(int maxFrameLength, ByteBuf... delimiters)
/*  35:    */   {
/*  36:129 */     this(maxFrameLength, true, delimiters);
/*  37:    */   }
/*  38:    */   
/*  39:    */   public DelimiterBasedFrameDecoder(int maxFrameLength, boolean stripDelimiter, ByteBuf... delimiters)
/*  40:    */   {
/*  41:144 */     this(maxFrameLength, stripDelimiter, true, delimiters);
/*  42:    */   }
/*  43:    */   
/*  44:    */   public DelimiterBasedFrameDecoder(int maxFrameLength, boolean stripDelimiter, boolean failFast, ByteBuf... delimiters)
/*  45:    */   {
/*  46:166 */     validateMaxFrameLength(maxFrameLength);
/*  47:167 */     if (delimiters == null) {
/*  48:168 */       throw new NullPointerException("delimiters");
/*  49:    */     }
/*  50:170 */     if (delimiters.length == 0) {
/*  51:171 */       throw new IllegalArgumentException("empty delimiters");
/*  52:    */     }
/*  53:174 */     if ((isLineBased(delimiters)) && (!isSubclass()))
/*  54:    */     {
/*  55:175 */       this.lineBasedDecoder = new LineBasedFrameDecoder(maxFrameLength, stripDelimiter, failFast);
/*  56:176 */       this.delimiters = null;
/*  57:    */     }
/*  58:    */     else
/*  59:    */     {
/*  60:178 */       this.delimiters = new ByteBuf[delimiters.length];
/*  61:179 */       for (int i = 0; i < delimiters.length; i++)
/*  62:    */       {
/*  63:180 */         ByteBuf d = delimiters[i];
/*  64:181 */         validateDelimiter(d);
/*  65:182 */         this.delimiters[i] = d.slice(d.readerIndex(), d.readableBytes());
/*  66:    */       }
/*  67:184 */       this.lineBasedDecoder = null;
/*  68:    */     }
/*  69:186 */     this.maxFrameLength = maxFrameLength;
/*  70:187 */     this.stripDelimiter = stripDelimiter;
/*  71:188 */     this.failFast = failFast;
/*  72:    */   }
/*  73:    */   
/*  74:    */   private static boolean isLineBased(ByteBuf[] delimiters)
/*  75:    */   {
/*  76:193 */     if (delimiters.length != 2) {
/*  77:194 */       return false;
/*  78:    */     }
/*  79:196 */     ByteBuf a = delimiters[0];
/*  80:197 */     ByteBuf b = delimiters[1];
/*  81:198 */     if (a.capacity() < b.capacity())
/*  82:    */     {
/*  83:199 */       a = delimiters[1];
/*  84:200 */       b = delimiters[0];
/*  85:    */     }
/*  86:202 */     return (a.capacity() == 2) && (b.capacity() == 1) && 
/*  87:203 */       (a.getByte(0) == 13) && (a.getByte(1) == 10) && 
/*  88:204 */       (b.getByte(0) == 10);
/*  89:    */   }
/*  90:    */   
/*  91:    */   private boolean isSubclass()
/*  92:    */   {
/*  93:211 */     return getClass() != DelimiterBasedFrameDecoder.class;
/*  94:    */   }
/*  95:    */   
/*  96:    */   protected final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/*  97:    */     throws Exception
/*  98:    */   {
/*  99:216 */     Object decoded = decode(ctx, in);
/* 100:217 */     if (decoded != null) {
/* 101:218 */       out.add(decoded);
/* 102:    */     }
/* 103:    */   }
/* 104:    */   
/* 105:    */   protected Object decode(ChannelHandlerContext ctx, ByteBuf buffer)
/* 106:    */     throws Exception
/* 107:    */   {
/* 108:231 */     if (this.lineBasedDecoder != null) {
/* 109:232 */       return this.lineBasedDecoder.decode(ctx, buffer);
/* 110:    */     }
/* 111:235 */     int minFrameLength = 2147483647;
/* 112:236 */     ByteBuf minDelim = null;
/* 113:237 */     for (ByteBuf delim : this.delimiters)
/* 114:    */     {
/* 115:238 */       int frameLength = indexOf(buffer, delim);
/* 116:239 */       if ((frameLength >= 0) && (frameLength < minFrameLength))
/* 117:    */       {
/* 118:240 */         minFrameLength = frameLength;
/* 119:241 */         minDelim = delim;
/* 120:    */       }
/* 121:    */     }
/* 122:245 */     if (minDelim != null)
/* 123:    */     {
/* 124:246 */       int minDelimLength = minDelim.capacity();
/* 125:249 */       if (this.discardingTooLongFrame)
/* 126:    */       {
/* 127:252 */         this.discardingTooLongFrame = false;
/* 128:253 */         buffer.skipBytes(minFrameLength + minDelimLength);
/* 129:    */         
/* 130:255 */         int tooLongFrameLength = this.tooLongFrameLength;
/* 131:256 */         this.tooLongFrameLength = 0;
/* 132:257 */         if (!this.failFast) {
/* 133:258 */           fail(tooLongFrameLength);
/* 134:    */         }
/* 135:260 */         return null;
/* 136:    */       }
/* 137:263 */       if (minFrameLength > this.maxFrameLength)
/* 138:    */       {
/* 139:265 */         buffer.skipBytes(minFrameLength + minDelimLength);
/* 140:266 */         fail(minFrameLength);
/* 141:267 */         return null;
/* 142:    */       }
/* 143:    */       ByteBuf frame;
/* 144:270 */       if (this.stripDelimiter)
/* 145:    */       {
/* 146:271 */         ByteBuf frame = buffer.readRetainedSlice(minFrameLength);
/* 147:272 */         buffer.skipBytes(minDelimLength);
/* 148:    */       }
/* 149:    */       else
/* 150:    */       {
/* 151:274 */         frame = buffer.readRetainedSlice(minFrameLength + minDelimLength);
/* 152:    */       }
/* 153:277 */       return frame;
/* 154:    */     }
/* 155:279 */     if (!this.discardingTooLongFrame)
/* 156:    */     {
/* 157:280 */       if (buffer.readableBytes() > this.maxFrameLength)
/* 158:    */       {
/* 159:282 */         this.tooLongFrameLength = buffer.readableBytes();
/* 160:283 */         buffer.skipBytes(buffer.readableBytes());
/* 161:284 */         this.discardingTooLongFrame = true;
/* 162:285 */         if (this.failFast) {
/* 163:286 */           fail(this.tooLongFrameLength);
/* 164:    */         }
/* 165:    */       }
/* 166:    */     }
/* 167:    */     else
/* 168:    */     {
/* 169:291 */       this.tooLongFrameLength += buffer.readableBytes();
/* 170:292 */       buffer.skipBytes(buffer.readableBytes());
/* 171:    */     }
/* 172:294 */     return null;
/* 173:    */   }
/* 174:    */   
/* 175:    */   private void fail(long frameLength)
/* 176:    */   {
/* 177:299 */     if (frameLength > 0L) {
/* 178:300 */       throw new TooLongFrameException("frame length exceeds " + this.maxFrameLength + ": " + frameLength + " - discarded");
/* 179:    */     }
/* 180:304 */     throw new TooLongFrameException("frame length exceeds " + this.maxFrameLength + " - discarding");
/* 181:    */   }
/* 182:    */   
/* 183:    */   private static int indexOf(ByteBuf haystack, ByteBuf needle)
/* 184:    */   {
/* 185:316 */     for (int i = haystack.readerIndex(); i < haystack.writerIndex(); i++)
/* 186:    */     {
/* 187:317 */       int haystackIndex = i;
/* 188:319 */       for (int needleIndex = 0; needleIndex < needle.capacity(); needleIndex++)
/* 189:    */       {
/* 190:320 */         if (haystack.getByte(haystackIndex) != needle.getByte(needleIndex)) {
/* 191:    */           break;
/* 192:    */         }
/* 193:323 */         haystackIndex++;
/* 194:324 */         if ((haystackIndex == haystack.writerIndex()) && 
/* 195:325 */           (needleIndex != needle.capacity() - 1)) {
/* 196:326 */           return -1;
/* 197:    */         }
/* 198:    */       }
/* 199:331 */       if (needleIndex == needle.capacity()) {
/* 200:333 */         return i - haystack.readerIndex();
/* 201:    */       }
/* 202:    */     }
/* 203:336 */     return -1;
/* 204:    */   }
/* 205:    */   
/* 206:    */   private static void validateDelimiter(ByteBuf delimiter)
/* 207:    */   {
/* 208:340 */     if (delimiter == null) {
/* 209:341 */       throw new NullPointerException("delimiter");
/* 210:    */     }
/* 211:343 */     if (!delimiter.isReadable()) {
/* 212:344 */       throw new IllegalArgumentException("empty delimiter");
/* 213:    */     }
/* 214:    */   }
/* 215:    */   
/* 216:    */   private static void validateMaxFrameLength(int maxFrameLength)
/* 217:    */   {
/* 218:349 */     if (maxFrameLength <= 0) {
/* 219:350 */       throw new IllegalArgumentException("maxFrameLength must be a positive integer: " + maxFrameLength);
/* 220:    */     }
/* 221:    */   }
/* 222:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.DelimiterBasedFrameDecoder
 * JD-Core Version:    0.7.0.1
 */