/*   1:    */ package io.protostuff;
/*   2:    */ 
/*   3:    */ import java.io.DataInput;
/*   4:    */ import java.io.DataOutput;
/*   5:    */ import java.io.IOException;
/*   6:    */ import java.io.InputStream;
/*   7:    */ import java.io.OutputStream;
/*   8:    */ import java.util.ArrayList;
/*   9:    */ import java.util.List;
/*  10:    */ 
/*  11:    */ public final class ProtobufIOUtil
/*  12:    */ {
/*  13:    */   public static Pipe newPipe(byte[] data)
/*  14:    */   {
/*  15: 43 */     return newPipe(data, 0, data.length);
/*  16:    */   }
/*  17:    */   
/*  18:    */   public static Pipe newPipe(byte[] data, int offset, int len)
/*  19:    */   {
/*  20: 51 */     ByteArrayInput byteArrayInput = new ByteArrayInput(data, offset, len, false);
/*  21: 52 */     new Pipe()
/*  22:    */     {
/*  23:    */       protected Input begin(Pipe.Schema<?> pipeSchema)
/*  24:    */         throws IOException
/*  25:    */       {
/*  26: 57 */         return this.val$byteArrayInput;
/*  27:    */       }
/*  28:    */       
/*  29:    */       protected void end(Pipe.Schema<?> pipeSchema, Input input, boolean cleanupOnly)
/*  30:    */         throws IOException
/*  31:    */       {
/*  32: 64 */         if (cleanupOnly) {
/*  33: 65 */           return;
/*  34:    */         }
/*  35: 67 */         assert (input == this.val$byteArrayInput);
/*  36:    */       }
/*  37:    */     };
/*  38:    */   }
/*  39:    */   
/*  40:    */   public static Pipe newPipe(InputStream in)
/*  41:    */   {
/*  42: 77 */     CodedInput codedInput = new CodedInput(in, false);
/*  43: 78 */     new Pipe()
/*  44:    */     {
/*  45:    */       protected Input begin(Pipe.Schema<?> pipeSchema)
/*  46:    */         throws IOException
/*  47:    */       {
/*  48: 83 */         return this.val$codedInput;
/*  49:    */       }
/*  50:    */       
/*  51:    */       protected void end(Pipe.Schema<?> pipeSchema, Input input, boolean cleanupOnly)
/*  52:    */         throws IOException
/*  53:    */       {
/*  54: 90 */         if (cleanupOnly) {
/*  55: 91 */           return;
/*  56:    */         }
/*  57: 93 */         assert (input == this.val$codedInput);
/*  58:    */       }
/*  59:    */     };
/*  60:    */   }
/*  61:    */   
/*  62:    */   public static <T> void mergeFrom(byte[] data, T message, Schema<T> schema)
/*  63:    */   {
/*  64:103 */     IOUtil.mergeFrom(data, 0, data.length, message, schema, false);
/*  65:    */   }
/*  66:    */   
/*  67:    */   public static <T> void mergeFrom(byte[] data, int offset, int length, T message, Schema<T> schema)
/*  68:    */   {
/*  69:112 */     IOUtil.mergeFrom(data, offset, length, message, schema, false);
/*  70:    */   }
/*  71:    */   
/*  72:    */   public static <T> void mergeFrom(InputStream in, T message, Schema<T> schema)
/*  73:    */     throws IOException
/*  74:    */   {
/*  75:121 */     IOUtil.mergeFrom(in, message, schema, false);
/*  76:    */   }
/*  77:    */   
/*  78:    */   public static <T> void mergeFrom(InputStream in, T message, Schema<T> schema, LinkedBuffer buffer)
/*  79:    */     throws IOException
/*  80:    */   {
/*  81:132 */     IOUtil.mergeFrom(in, buffer.buffer, message, schema, false);
/*  82:    */   }
/*  83:    */   
/*  84:    */   public static <T> int mergeDelimitedFrom(InputStream in, T message, Schema<T> schema)
/*  85:    */     throws IOException
/*  86:    */   {
/*  87:143 */     return IOUtil.mergeDelimitedFrom(in, message, schema, false);
/*  88:    */   }
/*  89:    */   
/*  90:    */   public static <T> int mergeDelimitedFrom(InputStream in, T message, Schema<T> schema, LinkedBuffer buffer)
/*  91:    */     throws IOException
/*  92:    */   {
/*  93:157 */     return IOUtil.mergeDelimitedFrom(in, buffer.buffer, message, schema, false);
/*  94:    */   }
/*  95:    */   
/*  96:    */   public static <T> int mergeDelimitedFrom(DataInput in, T message, Schema<T> schema)
/*  97:    */     throws IOException
/*  98:    */   {
/*  99:169 */     return IOUtil.mergeDelimitedFrom(in, message, schema, false);
/* 100:    */   }
/* 101:    */   
/* 102:    */   public static <T> byte[] toByteArray(T message, Schema<T> schema, LinkedBuffer buffer)
/* 103:    */   {
/* 104:179 */     if (buffer.start != buffer.offset) {
/* 105:180 */       throw new IllegalArgumentException("Buffer previously used and had not been reset.");
/* 106:    */     }
/* 107:182 */     ProtobufOutput output = new ProtobufOutput(buffer);
/* 108:    */     try
/* 109:    */     {
/* 110:185 */       schema.writeTo(output, message);
/* 111:    */     }
/* 112:    */     catch (IOException e)
/* 113:    */     {
/* 114:189 */       throw new RuntimeException("Serializing to a byte array threw an IOException (should never happen).", e);
/* 115:    */     }
/* 116:193 */     return output.toByteArray();
/* 117:    */   }
/* 118:    */   
/* 119:    */   public static <T> int writeTo(LinkedBuffer buffer, T message, Schema<T> schema)
/* 120:    */   {
/* 121:203 */     if (buffer.start != buffer.offset) {
/* 122:204 */       throw new IllegalArgumentException("Buffer previously used and had not been reset.");
/* 123:    */     }
/* 124:206 */     ProtobufOutput output = new ProtobufOutput(buffer);
/* 125:    */     try
/* 126:    */     {
/* 127:209 */       schema.writeTo(output, message);
/* 128:    */     }
/* 129:    */     catch (IOException e)
/* 130:    */     {
/* 131:213 */       throw new RuntimeException("Serializing to a LinkedBuffer threw an IOException (should never happen).", e);
/* 132:    */     }
/* 133:217 */     return output.getSize();
/* 134:    */   }
/* 135:    */   
/* 136:    */   public static <T> int writeTo(OutputStream out, T message, Schema<T> schema, LinkedBuffer buffer)
/* 137:    */     throws IOException
/* 138:    */   {
/* 139:228 */     if (buffer.start != buffer.offset) {
/* 140:229 */       throw new IllegalArgumentException("Buffer previously used and had not been reset.");
/* 141:    */     }
/* 142:231 */     ProtobufOutput output = new ProtobufOutput(buffer);
/* 143:232 */     schema.writeTo(output, message);
/* 144:233 */     return LinkedBuffer.writeTo(out, buffer);
/* 145:    */   }
/* 146:    */   
/* 147:    */   public static <T> int writeDelimitedTo(OutputStream out, T message, Schema<T> schema, LinkedBuffer buffer)
/* 148:    */     throws IOException
/* 149:    */   {
/* 150:244 */     if (buffer.start != buffer.offset) {
/* 151:245 */       throw new IllegalArgumentException("Buffer previously used and had not been reset.");
/* 152:    */     }
/* 153:247 */     ProtobufOutput output = new ProtobufOutput(buffer);
/* 154:248 */     schema.writeTo(output, message);
/* 155:249 */     int size = output.getSize();
/* 156:250 */     ProtobufOutput.writeRawVarInt32Bytes(out, size);
/* 157:251 */     int msgSize = LinkedBuffer.writeTo(out, buffer);
/* 158:    */     
/* 159:253 */     assert (size == msgSize);
/* 160:    */     
/* 161:255 */     return size;
/* 162:    */   }
/* 163:    */   
/* 164:    */   public static <T> int writeDelimitedTo(DataOutput out, T message, Schema<T> schema)
/* 165:    */     throws IOException
/* 166:    */   {
/* 167:267 */     LinkedBuffer buffer = new LinkedBuffer(256);
/* 168:268 */     ProtobufOutput output = new ProtobufOutput(buffer);
/* 169:269 */     schema.writeTo(output, message);
/* 170:270 */     int size = output.getSize();
/* 171:271 */     ProtobufOutput.writeRawVarInt32Bytes(out, size);
/* 172:    */     
/* 173:273 */     int msgSize = LinkedBuffer.writeTo(out, buffer);
/* 174:    */     
/* 175:275 */     assert (size == msgSize);
/* 176:    */     
/* 177:277 */     return size;
/* 178:    */   }
/* 179:    */   
/* 180:    */   public static <T> int writeListTo(OutputStream out, List<T> messages, Schema<T> schema, LinkedBuffer buffer)
/* 181:    */     throws IOException
/* 182:    */   {
/* 183:288 */     if (buffer.start != buffer.offset) {
/* 184:289 */       throw new IllegalArgumentException("Buffer previously used and had not been reset.");
/* 185:    */     }
/* 186:291 */     ProtobufOutput output = new ProtobufOutput(buffer);
/* 187:292 */     int totalSize = 0;
/* 188:293 */     for (T m : messages)
/* 189:    */     {
/* 190:295 */       schema.writeTo(output, m);
/* 191:296 */       int size = output.getSize();
/* 192:297 */       ProtobufOutput.writeRawVarInt32Bytes(out, size);
/* 193:298 */       int msgSize = LinkedBuffer.writeTo(out, buffer);
/* 194:    */       
/* 195:300 */       assert (size == msgSize);
/* 196:    */       
/* 197:302 */       totalSize += size;
/* 198:303 */       output.clear();
/* 199:    */     }
/* 200:305 */     return totalSize;
/* 201:    */   }
/* 202:    */   
/* 203:    */   public static <T> List<T> parseListFrom(InputStream in, Schema<T> schema)
/* 204:    */     throws IOException
/* 205:    */   {
/* 206:315 */     ArrayList<T> list = new ArrayList();
/* 207:316 */     byte[] buf = null;
/* 208:317 */     int biggestLen = 0;
/* 209:318 */     LimitedInputStream lin = null;
/* 210:319 */     for (int size = in.read(); size != -1; size = in.read())
/* 211:    */     {
/* 212:321 */       T message = schema.newMessage();
/* 213:322 */       list.add(message);
/* 214:323 */       int len = size < 128 ? size : CodedInput.readRawVarint32(in, size);
/* 215:324 */       if (len != 0) {
/* 216:327 */         if (len > 4096)
/* 217:    */         {
/* 218:330 */           if (lin == null) {
/* 219:331 */             lin = new LimitedInputStream(in);
/* 220:    */           }
/* 221:332 */           CodedInput input = new CodedInput(lin.limit(len), false);
/* 222:333 */           schema.mergeFrom(input, message);
/* 223:334 */           input.checkLastTagWas(0);
/* 224:    */         }
/* 225:    */         else
/* 226:    */         {
/* 227:338 */           if (biggestLen < len)
/* 228:    */           {
/* 229:342 */             buf = new byte[len];
/* 230:343 */             biggestLen = len;
/* 231:    */           }
/* 232:345 */           IOUtil.fillBufferFrom(in, buf, 0, len);
/* 233:346 */           ByteArrayInput input = new ByteArrayInput(buf, 0, len, false);
/* 234:    */           try
/* 235:    */           {
/* 236:349 */             schema.mergeFrom(input, message);
/* 237:    */           }
/* 238:    */           catch (ArrayIndexOutOfBoundsException e)
/* 239:    */           {
/* 240:353 */             throw ProtobufException.truncatedMessage(e);
/* 241:    */           }
/* 242:355 */           input.checkLastTagWas(0);
/* 243:    */         }
/* 244:    */       }
/* 245:    */     }
/* 246:358 */     return list;
/* 247:    */   }
/* 248:    */   
/* 249:    */   public static <T> boolean optMergeDelimitedFrom(InputStream in, T message, Schema<T> schema, LinkedBuffer buffer)
/* 250:    */     throws IOException
/* 251:    */   {
/* 252:374 */     return optMergeDelimitedFrom(in, message, schema, true, buffer);
/* 253:    */   }
/* 254:    */   
/* 255:    */   public static <T> boolean optMergeDelimitedFrom(InputStream in, T message, Schema<T> schema, boolean drainRemainingBytesIfTooLarge, LinkedBuffer buffer)
/* 256:    */     throws IOException
/* 257:    */   {
/* 258:388 */     if (buffer.start != buffer.offset) {
/* 259:389 */       throw new IllegalArgumentException("Buffer previously used and had not been reset.");
/* 260:    */     }
/* 261:391 */     int size = IOUtil.fillBufferWithDelimitedMessageFrom(in, drainRemainingBytesIfTooLarge, buffer);
/* 262:394 */     if (size == 0) {
/* 263:397 */       return true;
/* 264:    */     }
/* 265:400 */     if (buffer.start == buffer.offset) {
/* 266:403 */       return false;
/* 267:    */     }
/* 268:406 */     ByteArrayInput input = new ByteArrayInput(buffer.buffer, buffer.offset, size, false);
/* 269:    */     try
/* 270:    */     {
/* 271:410 */       schema.mergeFrom(input, message);
/* 272:411 */       input.checkLastTagWas(0);
/* 273:    */     }
/* 274:    */     catch (ArrayIndexOutOfBoundsException e)
/* 275:    */     {
/* 276:415 */       throw ProtobufException.truncatedMessage(e);
/* 277:    */     }
/* 278:    */     finally
/* 279:    */     {
/* 280:420 */       buffer.offset = buffer.start;
/* 281:    */     }
/* 282:423 */     return true;
/* 283:    */   }
/* 284:    */   
/* 285:    */   public static <T> int optWriteDelimitedTo(OutputStream out, T message, Schema<T> schema, LinkedBuffer buffer)
/* 286:    */     throws IOException
/* 287:    */   {
/* 288:435 */     if (buffer.start != buffer.offset) {
/* 289:436 */       throw new IllegalArgumentException("Buffer previously used and had not been reset.");
/* 290:    */     }
/* 291:438 */     ProtobufOutput output = new ProtobufOutput(buffer);
/* 292:    */     
/* 293:    */ 
/* 294:441 */     buffer.offset = (buffer.start + 5);
/* 295:442 */     output.size += 5;
/* 296:    */     
/* 297:444 */     schema.writeTo(output, message);
/* 298:    */     
/* 299:446 */     int size = output.size - 5;
/* 300:    */     
/* 301:448 */     int delimOffset = IOUtil.putVarInt32AndGetOffset(size, buffer.buffer, buffer.start);
/* 302:    */     
/* 303:    */ 
/* 304:    */ 
/* 305:452 */     out.write(buffer.buffer, delimOffset, buffer.offset - delimOffset);
/* 306:455 */     if (buffer.next != null) {
/* 307:456 */       LinkedBuffer.writeTo(out, buffer.next);
/* 308:    */     }
/* 309:458 */     return size;
/* 310:    */   }
/* 311:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.ProtobufIOUtil
 * JD-Core Version:    0.7.0.1
 */