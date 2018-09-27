/*   1:    */ package io.protostuff;
/*   2:    */ 
/*   3:    */ import java.io.DataInput;
/*   4:    */ import java.io.DataOutput;
/*   5:    */ import java.io.IOException;
/*   6:    */ import java.io.InputStream;
/*   7:    */ import java.io.OutputStream;
/*   8:    */ 
/*   9:    */ public final class GraphIOUtil
/*  10:    */ {
/*  11:    */   public static <T> void mergeFrom(byte[] data, T message, Schema<T> schema)
/*  12:    */   {
/*  13: 41 */     mergeFrom(data, 0, data.length, message, schema);
/*  14:    */   }
/*  15:    */   
/*  16:    */   public static <T> void mergeFrom(byte[] data, int offset, int length, T message, Schema<T> schema)
/*  17:    */   {
/*  18:    */     try
/*  19:    */     {
/*  20: 52 */       ByteArrayInput input = new ByteArrayInput(data, offset, length, true);
/*  21: 53 */       GraphByteArrayInput graphInput = new GraphByteArrayInput(input);
/*  22: 54 */       schema.mergeFrom(graphInput, message);
/*  23: 55 */       input.checkLastTagWas(0);
/*  24:    */     }
/*  25:    */     catch (ArrayIndexOutOfBoundsException ae)
/*  26:    */     {
/*  27: 59 */       throw new RuntimeException("Truncated.", ProtobufException.truncatedMessage(ae));
/*  28:    */     }
/*  29:    */     catch (IOException e)
/*  30:    */     {
/*  31: 63 */       throw new RuntimeException("Reading from a byte array threw an IOException (should never happen).", e);
/*  32:    */     }
/*  33:    */   }
/*  34:    */   
/*  35:    */   public static <T> void mergeFrom(InputStream in, T message, Schema<T> schema)
/*  36:    */     throws IOException
/*  37:    */   {
/*  38: 74 */     CodedInput input = new CodedInput(in, true);
/*  39: 75 */     GraphCodedInput graphInput = new GraphCodedInput(input);
/*  40: 76 */     schema.mergeFrom(graphInput, message);
/*  41: 77 */     input.checkLastTagWas(0);
/*  42:    */   }
/*  43:    */   
/*  44:    */   public static <T> void mergeFrom(InputStream in, T message, Schema<T> schema, LinkedBuffer buffer)
/*  45:    */     throws IOException
/*  46:    */   {
/*  47: 88 */     CodedInput input = new CodedInput(in, buffer.buffer, true);
/*  48: 89 */     GraphCodedInput graphInput = new GraphCodedInput(input);
/*  49: 90 */     schema.mergeFrom(graphInput, message);
/*  50: 91 */     input.checkLastTagWas(0);
/*  51:    */   }
/*  52:    */   
/*  53:    */   public static <T> int mergeDelimitedFrom(InputStream in, T message, Schema<T> schema)
/*  54:    */     throws IOException
/*  55:    */   {
/*  56:102 */     int size = in.read();
/*  57:103 */     if (size == -1) {
/*  58:104 */       throw ProtobufException.truncatedMessage();
/*  59:    */     }
/*  60:106 */     int len = size < 128 ? size : CodedInput.readRawVarint32(in, size);
/*  61:108 */     if (len < 0) {
/*  62:109 */       throw ProtobufException.negativeSize();
/*  63:    */     }
/*  64:111 */     if (len != 0)
/*  65:    */     {
/*  66:114 */       if (len > 4096)
/*  67:    */       {
/*  68:117 */         CodedInput input = new CodedInput(new LimitedInputStream(in, len), true);
/*  69:    */         
/*  70:119 */         GraphCodedInput graphInput = new GraphCodedInput(input);
/*  71:120 */         schema.mergeFrom(graphInput, message);
/*  72:121 */         input.checkLastTagWas(0);
/*  73:122 */         return len;
/*  74:    */       }
/*  75:125 */       byte[] buf = new byte[len];
/*  76:126 */       IOUtil.fillBufferFrom(in, buf, 0, len);
/*  77:127 */       ByteArrayInput input = new ByteArrayInput(buf, 0, len, true);
/*  78:    */       
/*  79:129 */       GraphByteArrayInput graphInput = new GraphByteArrayInput(input);
/*  80:    */       try
/*  81:    */       {
/*  82:132 */         schema.mergeFrom(graphInput, message);
/*  83:    */       }
/*  84:    */       catch (ArrayIndexOutOfBoundsException e)
/*  85:    */       {
/*  86:136 */         throw ProtobufException.truncatedMessage(e);
/*  87:    */       }
/*  88:138 */       input.checkLastTagWas(0);
/*  89:    */     }
/*  90:141 */     return len;
/*  91:    */   }
/*  92:    */   
/*  93:    */   public static <T> int mergeDelimitedFrom(InputStream in, T message, Schema<T> schema, LinkedBuffer buffer)
/*  94:    */     throws IOException
/*  95:    */   {
/*  96:155 */     int size = in.read();
/*  97:156 */     if (size == -1) {
/*  98:157 */       throw ProtobufException.truncatedMessage();
/*  99:    */     }
/* 100:159 */     byte[] buf = buffer.buffer;
/* 101:    */     
/* 102:161 */     int len = size < 128 ? size : CodedInput.readRawVarint32(in, size);
/* 103:163 */     if (len < 0) {
/* 104:164 */       throw ProtobufException.negativeSize();
/* 105:    */     }
/* 106:166 */     if (len != 0)
/* 107:    */     {
/* 108:169 */       if (len > buf.length) {
/* 109:172 */         throw new ProtobufException("size limit exceeded. " + len + " > " + buf.length);
/* 110:    */       }
/* 111:176 */       IOUtil.fillBufferFrom(in, buf, 0, len);
/* 112:177 */       ByteArrayInput input = new ByteArrayInput(buf, 0, len, true);
/* 113:    */       
/* 114:179 */       GraphByteArrayInput graphInput = new GraphByteArrayInput(input);
/* 115:    */       try
/* 116:    */       {
/* 117:182 */         schema.mergeFrom(graphInput, message);
/* 118:    */       }
/* 119:    */       catch (ArrayIndexOutOfBoundsException e)
/* 120:    */       {
/* 121:186 */         throw ProtobufException.truncatedMessage(e);
/* 122:    */       }
/* 123:188 */       input.checkLastTagWas(0);
/* 124:    */     }
/* 125:191 */     return len;
/* 126:    */   }
/* 127:    */   
/* 128:    */   public static <T> int mergeDelimitedFrom(DataInput in, T message, Schema<T> schema)
/* 129:    */     throws IOException
/* 130:    */   {
/* 131:203 */     byte size = in.readByte();
/* 132:204 */     int len = 0 == (size & 0x80) ? size : CodedInput.readRawVarint32(in, size);
/* 133:206 */     if (len < 0) {
/* 134:207 */       throw ProtobufException.negativeSize();
/* 135:    */     }
/* 136:209 */     if (len != 0) {
/* 137:212 */       if ((len > 4096) && ((in instanceof InputStream)))
/* 138:    */       {
/* 139:215 */         CodedInput input = new CodedInput(new LimitedInputStream((InputStream)in, len), true);
/* 140:    */         
/* 141:217 */         GraphCodedInput graphInput = new GraphCodedInput(input);
/* 142:218 */         schema.mergeFrom(graphInput, message);
/* 143:219 */         input.checkLastTagWas(0);
/* 144:    */       }
/* 145:    */       else
/* 146:    */       {
/* 147:223 */         byte[] buf = new byte[len];
/* 148:224 */         in.readFully(buf, 0, len);
/* 149:225 */         ByteArrayInput input = new ByteArrayInput(buf, 0, len, true);
/* 150:    */         
/* 151:227 */         GraphByteArrayInput graphInput = new GraphByteArrayInput(input);
/* 152:    */         try
/* 153:    */         {
/* 154:230 */           schema.mergeFrom(graphInput, message);
/* 155:    */         }
/* 156:    */         catch (ArrayIndexOutOfBoundsException e)
/* 157:    */         {
/* 158:234 */           throw ProtobufException.truncatedMessage(e);
/* 159:    */         }
/* 160:236 */         input.checkLastTagWas(0);
/* 161:    */       }
/* 162:    */     }
/* 163:241 */     if (!schema.isInitialized(message)) {
/* 164:242 */       throw new UninitializedMessageException(message, schema);
/* 165:    */     }
/* 166:244 */     return len;
/* 167:    */   }
/* 168:    */   
/* 169:    */   public static <T> byte[] toByteArray(T message, Schema<T> schema, LinkedBuffer buffer)
/* 170:    */   {
/* 171:254 */     if (buffer.start != buffer.offset) {
/* 172:255 */       throw new IllegalArgumentException("Buffer previously used and had not been reset.");
/* 173:    */     }
/* 174:257 */     ProtostuffOutput output = new ProtostuffOutput(buffer);
/* 175:258 */     GraphProtostuffOutput graphOutput = new GraphProtostuffOutput(output);
/* 176:    */     try
/* 177:    */     {
/* 178:261 */       schema.writeTo(graphOutput, message);
/* 179:    */     }
/* 180:    */     catch (IOException e)
/* 181:    */     {
/* 182:265 */       throw new RuntimeException("Serializing to a byte array threw an IOException (should never happen).", e);
/* 183:    */     }
/* 184:269 */     return output.toByteArray();
/* 185:    */   }
/* 186:    */   
/* 187:    */   public static <T> int writeTo(LinkedBuffer buffer, T message, Schema<T> schema)
/* 188:    */   {
/* 189:279 */     if (buffer.start != buffer.offset) {
/* 190:280 */       throw new IllegalArgumentException("Buffer previously used and had not been reset.");
/* 191:    */     }
/* 192:282 */     ProtostuffOutput output = new ProtostuffOutput(buffer);
/* 193:283 */     GraphProtostuffOutput graphOutput = new GraphProtostuffOutput(output);
/* 194:    */     try
/* 195:    */     {
/* 196:286 */       schema.writeTo(graphOutput, message);
/* 197:    */     }
/* 198:    */     catch (IOException e)
/* 199:    */     {
/* 200:290 */       throw new RuntimeException("Serializing to a LinkedBuffer threw an IOException (should never happen).", e);
/* 201:    */     }
/* 202:294 */     return output.getSize();
/* 203:    */   }
/* 204:    */   
/* 205:    */   public static <T> int writeTo(OutputStream out, T message, Schema<T> schema, LinkedBuffer buffer)
/* 206:    */     throws IOException
/* 207:    */   {
/* 208:305 */     if (buffer.start != buffer.offset) {
/* 209:306 */       throw new IllegalArgumentException("Buffer previously used and had not been reset.");
/* 210:    */     }
/* 211:308 */     ProtostuffOutput output = new ProtostuffOutput(buffer, out);
/* 212:309 */     GraphProtostuffOutput graphOutput = new GraphProtostuffOutput(output);
/* 213:310 */     schema.writeTo(graphOutput, message);
/* 214:311 */     LinkedBuffer.writeTo(out, buffer);
/* 215:312 */     return output.size;
/* 216:    */   }
/* 217:    */   
/* 218:    */   public static <T> int writeDelimitedTo(OutputStream out, T message, Schema<T> schema, LinkedBuffer buffer)
/* 219:    */     throws IOException
/* 220:    */   {
/* 221:323 */     if (buffer.start != buffer.offset) {
/* 222:324 */       throw new IllegalArgumentException("Buffer previously used and had not been reset.");
/* 223:    */     }
/* 224:326 */     ProtostuffOutput output = new ProtostuffOutput(buffer);
/* 225:327 */     GraphProtostuffOutput graphOutput = new GraphProtostuffOutput(output);
/* 226:328 */     schema.writeTo(graphOutput, message);
/* 227:329 */     ProtobufOutput.writeRawVarInt32Bytes(out, output.size);
/* 228:330 */     LinkedBuffer.writeTo(out, buffer);
/* 229:331 */     return output.size;
/* 230:    */   }
/* 231:    */   
/* 232:    */   public static <T> int writeDelimitedTo(DataOutput out, T message, Schema<T> schema)
/* 233:    */     throws IOException
/* 234:    */   {
/* 235:343 */     LinkedBuffer buffer = new LinkedBuffer(256);
/* 236:344 */     ProtostuffOutput output = new ProtostuffOutput(buffer);
/* 237:345 */     GraphProtostuffOutput graphOutput = new GraphProtostuffOutput(output);
/* 238:346 */     schema.writeTo(graphOutput, message);
/* 239:347 */     ProtobufOutput.writeRawVarInt32Bytes(out, output.size);
/* 240:348 */     LinkedBuffer.writeTo(out, buffer);
/* 241:349 */     return output.size;
/* 242:    */   }
/* 243:    */   
/* 244:    */   public static <T> boolean optMergeDelimitedFrom(InputStream in, T message, Schema<T> schema, LinkedBuffer buffer)
/* 245:    */     throws IOException
/* 246:    */   {
/* 247:365 */     return optMergeDelimitedFrom(in, message, schema, true, buffer);
/* 248:    */   }
/* 249:    */   
/* 250:    */   public static <T> boolean optMergeDelimitedFrom(InputStream in, T message, Schema<T> schema, boolean drainRemainingBytesIfTooLarge, LinkedBuffer buffer)
/* 251:    */     throws IOException
/* 252:    */   {
/* 253:379 */     if (buffer.start != buffer.offset) {
/* 254:380 */       throw new IllegalArgumentException("Buffer previously used and had not been reset.");
/* 255:    */     }
/* 256:382 */     int size = IOUtil.fillBufferWithDelimitedMessageFrom(in, drainRemainingBytesIfTooLarge, buffer);
/* 257:385 */     if (size == 0) {
/* 258:388 */       return true;
/* 259:    */     }
/* 260:391 */     if (buffer.start == buffer.offset) {
/* 261:394 */       return false;
/* 262:    */     }
/* 263:397 */     ByteArrayInput input = new ByteArrayInput(buffer.buffer, buffer.offset, size, true);
/* 264:    */     
/* 265:399 */     GraphByteArrayInput graphInput = new GraphByteArrayInput(input);
/* 266:    */     try
/* 267:    */     {
/* 268:402 */       schema.mergeFrom(graphInput, message);
/* 269:403 */       input.checkLastTagWas(0);
/* 270:    */     }
/* 271:    */     catch (ArrayIndexOutOfBoundsException e)
/* 272:    */     {
/* 273:407 */       throw ProtobufException.truncatedMessage(e);
/* 274:    */     }
/* 275:    */     finally
/* 276:    */     {
/* 277:412 */       buffer.offset = buffer.start;
/* 278:    */     }
/* 279:415 */     return true;
/* 280:    */   }
/* 281:    */   
/* 282:    */   public static <T> int optWriteDelimitedTo(OutputStream out, T message, Schema<T> schema, LinkedBuffer buffer)
/* 283:    */     throws IOException
/* 284:    */   {
/* 285:427 */     if (buffer.start != buffer.offset) {
/* 286:428 */       throw new IllegalArgumentException("Buffer previously used and had not been reset.");
/* 287:    */     }
/* 288:430 */     ProtostuffOutput output = new ProtostuffOutput(buffer);
/* 289:431 */     GraphProtostuffOutput graphOutput = new GraphProtostuffOutput(output);
/* 290:    */     
/* 291:    */ 
/* 292:434 */     buffer.offset = (buffer.start + 5);
/* 293:435 */     output.size += 5;
/* 294:    */     
/* 295:437 */     schema.writeTo(graphOutput, message);
/* 296:    */     
/* 297:439 */     int size = output.size - 5;
/* 298:    */     
/* 299:441 */     int delimOffset = IOUtil.putVarInt32AndGetOffset(size, buffer.buffer, buffer.start);
/* 300:    */     
/* 301:    */ 
/* 302:    */ 
/* 303:445 */     out.write(buffer.buffer, delimOffset, buffer.offset - delimOffset);
/* 304:448 */     if (buffer.next != null) {
/* 305:449 */       LinkedBuffer.writeTo(out, buffer.next);
/* 306:    */     }
/* 307:451 */     return size;
/* 308:    */   }
/* 309:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.GraphIOUtil
 * JD-Core Version:    0.7.0.1
 */