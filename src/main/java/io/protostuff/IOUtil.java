/*   1:    */ package io.protostuff;
/*   2:    */ 
/*   3:    */ import java.io.DataInput;
/*   4:    */ import java.io.EOFException;
/*   5:    */ import java.io.IOException;
/*   6:    */ import java.io.InputStream;
/*   7:    */ 
/*   8:    */ final class IOUtil
/*   9:    */ {
/*  10:    */   static <T> void mergeFrom(byte[] data, int offset, int length, T message, Schema<T> schema, boolean decodeNestedMessageAsGroup)
/*  11:    */   {
/*  12:    */     try
/*  13:    */     {
/*  14: 43 */       ByteArrayInput input = new ByteArrayInput(data, offset, length, decodeNestedMessageAsGroup);
/*  15:    */       
/*  16: 45 */       schema.mergeFrom(input, message);
/*  17: 46 */       input.checkLastTagWas(0);
/*  18:    */     }
/*  19:    */     catch (ArrayIndexOutOfBoundsException ae)
/*  20:    */     {
/*  21: 50 */       throw new RuntimeException("Truncated.", ProtobufException.truncatedMessage(ae));
/*  22:    */     }
/*  23:    */     catch (IOException e)
/*  24:    */     {
/*  25: 54 */       throw new RuntimeException("Reading from a byte array threw an IOException (should never happen).", e);
/*  26:    */     }
/*  27:    */   }
/*  28:    */   
/*  29:    */   static <T> void mergeFrom(InputStream in, byte[] buf, T message, Schema<T> schema, boolean decodeNestedMessageAsGroup)
/*  30:    */     throws IOException
/*  31:    */   {
/*  32: 65 */     CodedInput input = new CodedInput(in, buf, decodeNestedMessageAsGroup);
/*  33: 66 */     schema.mergeFrom(input, message);
/*  34: 67 */     input.checkLastTagWas(0);
/*  35:    */   }
/*  36:    */   
/*  37:    */   static <T> void mergeFrom(InputStream in, T message, Schema<T> schema, boolean decodeNestedMessageAsGroup)
/*  38:    */     throws IOException
/*  39:    */   {
/*  40: 76 */     CodedInput input = new CodedInput(in, decodeNestedMessageAsGroup);
/*  41: 77 */     schema.mergeFrom(input, message);
/*  42: 78 */     input.checkLastTagWas(0);
/*  43:    */   }
/*  44:    */   
/*  45:    */   static <T> int mergeDelimitedFrom(InputStream in, byte[] buf, T message, Schema<T> schema, boolean decodeNestedMessageAsGroup)
/*  46:    */     throws IOException
/*  47:    */   {
/*  48: 88 */     int size = in.read();
/*  49: 89 */     if (size == -1) {
/*  50: 90 */       throw new EOFException("mergeDelimitedFrom");
/*  51:    */     }
/*  52: 92 */     int len = size < 128 ? size : CodedInput.readRawVarint32(in, size);
/*  53: 94 */     if (len < 0) {
/*  54: 95 */       throw ProtobufException.negativeSize();
/*  55:    */     }
/*  56: 97 */     if (len != 0)
/*  57:    */     {
/*  58:100 */       if (len > buf.length) {
/*  59:103 */         throw new ProtobufException("size limit exceeded. " + len + " > " + buf.length);
/*  60:    */       }
/*  61:107 */       fillBufferFrom(in, buf, 0, len);
/*  62:108 */       ByteArrayInput input = new ByteArrayInput(buf, 0, len, decodeNestedMessageAsGroup);
/*  63:    */       try
/*  64:    */       {
/*  65:112 */         schema.mergeFrom(input, message);
/*  66:    */       }
/*  67:    */       catch (ArrayIndexOutOfBoundsException e)
/*  68:    */       {
/*  69:116 */         throw ProtobufException.truncatedMessage(e);
/*  70:    */       }
/*  71:118 */       input.checkLastTagWas(0);
/*  72:    */     }
/*  73:121 */     return len;
/*  74:    */   }
/*  75:    */   
/*  76:    */   static <T> int mergeDelimitedFrom(InputStream in, T message, Schema<T> schema, boolean decodeNestedMessageAsGroup)
/*  77:    */     throws IOException
/*  78:    */   {
/*  79:130 */     int size = in.read();
/*  80:131 */     if (size == -1) {
/*  81:132 */       throw new EOFException("mergeDelimitedFrom");
/*  82:    */     }
/*  83:134 */     int len = size < 128 ? size : CodedInput.readRawVarint32(in, size);
/*  84:136 */     if (len < 0) {
/*  85:137 */       throw ProtobufException.negativeSize();
/*  86:    */     }
/*  87:139 */     if (len != 0)
/*  88:    */     {
/*  89:142 */       if (len > 4096)
/*  90:    */       {
/*  91:145 */         CodedInput input = new CodedInput(new LimitedInputStream(in, len), decodeNestedMessageAsGroup);
/*  92:    */         
/*  93:147 */         schema.mergeFrom(input, message);
/*  94:148 */         input.checkLastTagWas(0);
/*  95:149 */         return len;
/*  96:    */       }
/*  97:152 */       byte[] buf = new byte[len];
/*  98:153 */       fillBufferFrom(in, buf, 0, len);
/*  99:154 */       ByteArrayInput input = new ByteArrayInput(buf, 0, len, decodeNestedMessageAsGroup);
/* 100:    */       try
/* 101:    */       {
/* 102:158 */         schema.mergeFrom(input, message);
/* 103:    */       }
/* 104:    */       catch (ArrayIndexOutOfBoundsException e)
/* 105:    */       {
/* 106:162 */         throw ProtobufException.truncatedMessage(e);
/* 107:    */       }
/* 108:164 */       input.checkLastTagWas(0);
/* 109:    */     }
/* 110:167 */     return len;
/* 111:    */   }
/* 112:    */   
/* 113:    */   static <T> int mergeDelimitedFrom(DataInput in, T message, Schema<T> schema, boolean decodeNestedMessageAsGroup)
/* 114:    */     throws IOException
/* 115:    */   {
/* 116:177 */     byte size = in.readByte();
/* 117:178 */     int len = 0 == (size & 0x80) ? size : CodedInput.readRawVarint32(in, size);
/* 118:180 */     if (len < 0) {
/* 119:181 */       throw ProtobufException.negativeSize();
/* 120:    */     }
/* 121:183 */     if (len != 0) {
/* 122:186 */       if ((len > 4096) && ((in instanceof InputStream)))
/* 123:    */       {
/* 124:189 */         CodedInput input = new CodedInput(new LimitedInputStream((InputStream)in, len), decodeNestedMessageAsGroup);
/* 125:    */         
/* 126:191 */         schema.mergeFrom(input, message);
/* 127:192 */         input.checkLastTagWas(0);
/* 128:    */       }
/* 129:    */       else
/* 130:    */       {
/* 131:196 */         byte[] buf = new byte[len];
/* 132:197 */         in.readFully(buf, 0, len);
/* 133:198 */         ByteArrayInput input = new ByteArrayInput(buf, 0, len, decodeNestedMessageAsGroup);
/* 134:    */         try
/* 135:    */         {
/* 136:202 */           schema.mergeFrom(input, message);
/* 137:    */         }
/* 138:    */         catch (ArrayIndexOutOfBoundsException e)
/* 139:    */         {
/* 140:206 */           throw ProtobufException.truncatedMessage(e);
/* 141:    */         }
/* 142:208 */         input.checkLastTagWas(0);
/* 143:    */       }
/* 144:    */     }
/* 145:213 */     if (!schema.isInitialized(message)) {
/* 146:214 */       throw new UninitializedMessageException(message, schema);
/* 147:    */     }
/* 148:216 */     return len;
/* 149:    */   }
/* 150:    */   
/* 151:    */   static void fillBufferFrom(InputStream in, byte[] buf, int offset, int len)
/* 152:    */     throws IOException
/* 153:    */   {
/* 154:225 */     for (int read = 0; len > 0; offset += read)
/* 155:    */     {
/* 156:227 */       read = in.read(buf, offset, len);
/* 157:228 */       if (read == -1) {
/* 158:229 */         throw ProtobufException.truncatedMessage();
/* 159:    */       }
/* 160:225 */       len -= read;
/* 161:    */     }
/* 162:    */   }
/* 163:    */   
/* 164:    */   static int fillBufferWithDelimitedMessageFrom(InputStream in, boolean drainRemainingBytesIfTooLarge, LinkedBuffer lb)
/* 165:    */     throws IOException
/* 166:    */   {
/* 167:244 */     byte[] buf = lb.buffer;
/* 168:245 */     int offset = lb.start;int len = buf.length - offset;int read = in.read(buf, offset, len);
/* 169:247 */     if (read < 1) {
/* 170:248 */       throw new EOFException("fillBufferWithDelimitedMessageFrom");
/* 171:    */     }
/* 172:250 */     int last = offset + read;int size = buf[(offset++)];
/* 173:251 */     if (0 != (size & 0x80))
/* 174:    */     {
/* 175:253 */       size &= 0x7F;
/* 176:255 */       for (int shift = 7;; shift += 7)
/* 177:    */       {
/* 178:257 */         if (offset == last)
/* 179:    */         {
/* 180:260 */           read = in.read(buf, last, len - (last - lb.start));
/* 181:261 */           if (read < 1) {
/* 182:262 */             throw new EOFException("fillBufferWithDelimitedMessageFrom");
/* 183:    */           }
/* 184:264 */           last += read;
/* 185:    */         }
/* 186:267 */         byte b = buf[(offset++)];
/* 187:268 */         size |= (b & 0x7F) << shift;
/* 188:270 */         if (0 == (b & 0x80)) {
/* 189:    */           break;
/* 190:    */         }
/* 191:273 */         if (shift == 28)
/* 192:    */         {
/* 193:276 */           int i = 0;
/* 194:    */           do
/* 195:    */           {
/* 196:278 */             if (offset == last)
/* 197:    */             {
/* 198:281 */               read = in.read(buf, last, len - (last - lb.start));
/* 199:282 */               if (read < 1) {
/* 200:283 */                 throw new EOFException("fillBufferWithDelimitedMessageFrom");
/* 201:    */               }
/* 202:285 */               last += read;
/* 203:    */             }
/* 204:288 */             if (buf[(offset++)] >= 0) {
/* 205:    */               break;
/* 206:    */             }
/* 207:291 */           } while (5 != ++i);
/* 208:294 */           throw ProtobufException.malformedVarint();
/* 209:    */         }
/* 210:    */       }
/* 211:    */     }
/* 212:302 */     if (size == 0)
/* 213:    */     {
/* 214:304 */       if (offset != last) {
/* 215:305 */         throw ProtobufException.misreportedSize();
/* 216:    */       }
/* 217:307 */       return size;
/* 218:    */     }
/* 219:310 */     if (size < 0) {
/* 220:311 */       throw ProtobufException.negativeSize();
/* 221:    */     }
/* 222:313 */     int partial = last - offset;
/* 223:314 */     if (partial < size)
/* 224:    */     {
/* 225:317 */       int delimSize = offset - lb.start;
/* 226:318 */       if (size + delimSize > len)
/* 227:    */       {
/* 228:322 */         if (!drainRemainingBytesIfTooLarge) {
/* 229:323 */           return size;
/* 230:    */         }
/* 231:326 */         for (int remaining = size - partial; remaining > 0;)
/* 232:    */         {
/* 233:328 */           read = in.read(buf, lb.start, Math.min(remaining, len));
/* 234:329 */           if (read < 1) {
/* 235:330 */             throw new EOFException("fillBufferWithDelimitedMessageFrom");
/* 236:    */           }
/* 237:332 */           remaining -= read;
/* 238:    */         }
/* 239:335 */         return size;
/* 240:    */       }
/* 241:339 */       fillBufferFrom(in, buf, last, size - partial);
/* 242:    */     }
/* 243:343 */     lb.offset = offset;
/* 244:    */     
/* 245:345 */     return size;
/* 246:    */   }
/* 247:    */   
/* 248:    */   static int putVarInt32AndGetOffset(int value, byte[] buffer, int variableOffset)
/* 249:    */   {
/* 250:355 */     switch (ProtobufOutput.computeRawVarint32Size(value))
/* 251:    */     {
/* 252:    */     case 1: 
/* 253:358 */       buffer[(variableOffset + 4)] = ((byte)value);
/* 254:359 */       return variableOffset + 4;
/* 255:    */     case 2: 
/* 256:362 */       buffer[(variableOffset + 3)] = ((byte)(value & 0x7F | 0x80));
/* 257:363 */       buffer[(variableOffset + 4)] = ((byte)(value >>> 7));
/* 258:364 */       return variableOffset + 3;
/* 259:    */     case 3: 
/* 260:367 */       buffer[(variableOffset + 2)] = ((byte)(value & 0x7F | 0x80));
/* 261:368 */       buffer[(variableOffset + 3)] = ((byte)(value >>> 7 & 0x7F | 0x80));
/* 262:369 */       buffer[(variableOffset + 4)] = ((byte)(value >>> 14));
/* 263:370 */       return variableOffset + 2;
/* 264:    */     case 4: 
/* 265:373 */       buffer[(variableOffset + 1)] = ((byte)(value & 0x7F | 0x80));
/* 266:374 */       buffer[(variableOffset + 2)] = ((byte)(value >>> 7 & 0x7F | 0x80));
/* 267:375 */       buffer[(variableOffset + 3)] = ((byte)(value >>> 14 & 0x7F | 0x80));
/* 268:376 */       buffer[(variableOffset + 4)] = ((byte)(value >>> 21));
/* 269:377 */       return variableOffset + 1;
/* 270:    */     }
/* 271:380 */     buffer[variableOffset] = ((byte)(value & 0x7F | 0x80));
/* 272:381 */     buffer[(variableOffset + 1)] = ((byte)(value >>> 7 & 0x7F | 0x80));
/* 273:382 */     buffer[(variableOffset + 2)] = ((byte)(value >>> 14 & 0x7F | 0x80));
/* 274:383 */     buffer[(variableOffset + 3)] = ((byte)(value >>> 21 & 0x7F | 0x80));
/* 275:384 */     buffer[(variableOffset + 4)] = ((byte)(value >>> 28));
/* 276:385 */     return variableOffset;
/* 277:    */   }
/* 278:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.IOUtil
 * JD-Core Version:    0.7.0.1
 */