/*   1:    */ package io.protostuff;
/*   2:    */ 
/*   3:    */ import java.io.DataInput;
/*   4:    */ import java.io.DataOutput;
/*   5:    */ import java.io.IOException;
/*   6:    */ import java.io.InputStream;
/*   7:    */ import java.io.OutputStream;
/*   8:    */ import java.util.ArrayList;
/*   9:    */ import java.util.Collections;
/*  10:    */ import java.util.List;
/*  11:    */ 
/*  12:    */ public final class ProtostuffIOUtil
/*  13:    */ {
/*  14:    */   public static Pipe newPipe(byte[] data)
/*  15:    */   {
/*  16: 44 */     return newPipe(data, 0, data.length);
/*  17:    */   }
/*  18:    */   
/*  19:    */   public static Pipe newPipe(byte[] data, int offset, int len)
/*  20:    */   {
/*  21: 52 */     ByteArrayInput byteArrayInput = new ByteArrayInput(data, offset, len, true);
/*  22: 53 */     new Pipe()
/*  23:    */     {
/*  24:    */       protected Input begin(Pipe.Schema<?> pipeSchema)
/*  25:    */         throws IOException
/*  26:    */       {
/*  27: 58 */         return this.val$byteArrayInput;
/*  28:    */       }
/*  29:    */       
/*  30:    */       protected void end(Pipe.Schema<?> pipeSchema, Input input, boolean cleanupOnly)
/*  31:    */         throws IOException
/*  32:    */       {
/*  33: 65 */         if (cleanupOnly) {
/*  34: 66 */           return;
/*  35:    */         }
/*  36: 68 */         assert (input == this.val$byteArrayInput);
/*  37:    */       }
/*  38:    */     };
/*  39:    */   }
/*  40:    */   
/*  41:    */   public static Pipe newPipe(InputStream in)
/*  42:    */   {
/*  43: 78 */     CodedInput codedInput = new CodedInput(in, true);
/*  44: 79 */     new Pipe()
/*  45:    */     {
/*  46:    */       protected Input begin(Pipe.Schema<?> pipeSchema)
/*  47:    */         throws IOException
/*  48:    */       {
/*  49: 84 */         return this.val$codedInput;
/*  50:    */       }
/*  51:    */       
/*  52:    */       protected void end(Pipe.Schema<?> pipeSchema, Input input, boolean cleanupOnly)
/*  53:    */         throws IOException
/*  54:    */       {
/*  55: 91 */         if (cleanupOnly) {
/*  56: 92 */           return;
/*  57:    */         }
/*  58: 94 */         assert (input == this.val$codedInput);
/*  59:    */       }
/*  60:    */     };
/*  61:    */   }
/*  62:    */   
/*  63:    */   public static <T> void mergeFrom(byte[] data, T message, Schema<T> schema)
/*  64:    */   {
/*  65:104 */     IOUtil.mergeFrom(data, 0, data.length, message, schema, true);
/*  66:    */   }
/*  67:    */   
/*  68:    */   public static <T> void mergeFrom(byte[] data, int offset, int length, T message, Schema<T> schema)
/*  69:    */   {
/*  70:113 */     IOUtil.mergeFrom(data, offset, length, message, schema, true);
/*  71:    */   }
/*  72:    */   
/*  73:    */   public static <T> void mergeFrom(InputStream in, T message, Schema<T> schema)
/*  74:    */     throws IOException
/*  75:    */   {
/*  76:122 */     IOUtil.mergeFrom(in, message, schema, true);
/*  77:    */   }
/*  78:    */   
/*  79:    */   public static <T> void mergeFrom(InputStream in, T message, Schema<T> schema, LinkedBuffer buffer)
/*  80:    */     throws IOException
/*  81:    */   {
/*  82:133 */     IOUtil.mergeFrom(in, buffer.buffer, message, schema, true);
/*  83:    */   }
/*  84:    */   
/*  85:    */   public static <T> int mergeDelimitedFrom(InputStream in, T message, Schema<T> schema)
/*  86:    */     throws IOException
/*  87:    */   {
/*  88:144 */     return IOUtil.mergeDelimitedFrom(in, message, schema, true);
/*  89:    */   }
/*  90:    */   
/*  91:    */   public static <T> int mergeDelimitedFrom(InputStream in, T message, Schema<T> schema, LinkedBuffer buffer)
/*  92:    */     throws IOException
/*  93:    */   {
/*  94:158 */     return IOUtil.mergeDelimitedFrom(in, buffer.buffer, message, schema, true);
/*  95:    */   }
/*  96:    */   
/*  97:    */   public static <T> int mergeDelimitedFrom(DataInput in, T message, Schema<T> schema)
/*  98:    */     throws IOException
/*  99:    */   {
/* 100:170 */     return IOUtil.mergeDelimitedFrom(in, message, schema, true);
/* 101:    */   }
/* 102:    */   
/* 103:    */   public static <T> byte[] toByteArray(T message, Schema<T> schema, LinkedBuffer buffer)
/* 104:    */   {
/* 105:180 */     if (buffer.start != buffer.offset) {
/* 106:181 */       throw new IllegalArgumentException("Buffer previously used and had not been reset.");
/* 107:    */     }
/* 108:183 */     ProtostuffOutput output = new ProtostuffOutput(buffer);
/* 109:    */     try
/* 110:    */     {
/* 111:186 */       schema.writeTo(output, message);
/* 112:    */     }
/* 113:    */     catch (IOException e)
/* 114:    */     {
/* 115:190 */       throw new RuntimeException("Serializing to a byte array threw an IOException (should never happen).", e);
/* 116:    */     }
/* 117:194 */     return output.toByteArray();
/* 118:    */   }
/* 119:    */   
/* 120:    */   public static <T> int writeTo(LinkedBuffer buffer, T message, Schema<T> schema)
/* 121:    */   {
/* 122:204 */     if (buffer.start != buffer.offset) {
/* 123:205 */       throw new IllegalArgumentException("Buffer previously used and had not been reset.");
/* 124:    */     }
/* 125:207 */     ProtostuffOutput output = new ProtostuffOutput(buffer);
/* 126:    */     try
/* 127:    */     {
/* 128:210 */       schema.writeTo(output, message);
/* 129:    */     }
/* 130:    */     catch (IOException e)
/* 131:    */     {
/* 132:214 */       throw new RuntimeException("Serializing to a LinkedBuffer threw an IOException (should never happen).", e);
/* 133:    */     }
/* 134:218 */     return output.getSize();
/* 135:    */   }
/* 136:    */   
/* 137:    */   public static <T> int writeTo(OutputStream out, T message, Schema<T> schema, LinkedBuffer buffer)
/* 138:    */     throws IOException
/* 139:    */   {
/* 140:229 */     if (buffer.start != buffer.offset) {
/* 141:230 */       throw new IllegalArgumentException("Buffer previously used and had not been reset.");
/* 142:    */     }
/* 143:232 */     ProtostuffOutput output = new ProtostuffOutput(buffer, out);
/* 144:233 */     schema.writeTo(output, message);
/* 145:234 */     LinkedBuffer.writeTo(out, buffer);
/* 146:235 */     return output.size;
/* 147:    */   }
/* 148:    */   
/* 149:    */   public static <T> int writeDelimitedTo(OutputStream out, T message, Schema<T> schema, LinkedBuffer buffer)
/* 150:    */     throws IOException
/* 151:    */   {
/* 152:246 */     if (buffer.start != buffer.offset) {
/* 153:247 */       throw new IllegalArgumentException("Buffer previously used and had not been reset.");
/* 154:    */     }
/* 155:249 */     ProtostuffOutput output = new ProtostuffOutput(buffer);
/* 156:250 */     schema.writeTo(output, message);
/* 157:251 */     ProtobufOutput.writeRawVarInt32Bytes(out, output.size);
/* 158:252 */     LinkedBuffer.writeTo(out, buffer);
/* 159:253 */     return output.size;
/* 160:    */   }
/* 161:    */   
/* 162:    */   public static <T> int writeDelimitedTo(DataOutput out, T message, Schema<T> schema)
/* 163:    */     throws IOException
/* 164:    */   {
/* 165:265 */     LinkedBuffer buffer = new LinkedBuffer(256);
/* 166:266 */     ProtostuffOutput output = new ProtostuffOutput(buffer);
/* 167:267 */     schema.writeTo(output, message);
/* 168:268 */     ProtobufOutput.writeRawVarInt32Bytes(out, output.size);
/* 169:269 */     LinkedBuffer.writeTo(out, buffer);
/* 170:270 */     return output.size;
/* 171:    */   }
/* 172:    */   
/* 173:    */   public static <T> int writeListTo(OutputStream out, List<T> messages, Schema<T> schema, LinkedBuffer buffer)
/* 174:    */     throws IOException
/* 175:    */   {
/* 176:281 */     if (buffer.start != buffer.offset) {
/* 177:282 */       throw new IllegalArgumentException("Buffer previously used and had not been reset.");
/* 178:    */     }
/* 179:284 */     int size = messages.size();
/* 180:285 */     if (size == 0) {
/* 181:286 */       return 0;
/* 182:    */     }
/* 183:288 */     ProtostuffOutput output = new ProtostuffOutput(buffer, out);
/* 184:289 */     output.sink.writeVarInt32(size, output, buffer);
/* 185:291 */     for (T m : messages)
/* 186:    */     {
/* 187:293 */       schema.writeTo(output, m);
/* 188:294 */       output.sink.writeByte((byte)7, output, buffer);
/* 189:    */     }
/* 190:298 */     LinkedBuffer.writeTo(out, buffer);
/* 191:    */     
/* 192:300 */     return output.size;
/* 193:    */   }
/* 194:    */   
/* 195:    */   public static <T> List<T> parseListFrom(InputStream in, Schema<T> schema)
/* 196:    */     throws IOException
/* 197:    */   {
/* 198:311 */     int size = in.read();
/* 199:312 */     if (size == -1) {
/* 200:313 */       return Collections.emptyList();
/* 201:    */     }
/* 202:315 */     if (size > 127) {
/* 203:316 */       size = CodedInput.readRawVarint32(in, size);
/* 204:    */     }
/* 205:318 */     ArrayList<T> list = new ArrayList(size);
/* 206:319 */     CodedInput input = new CodedInput(in, true);
/* 207:320 */     for (int i = 0; i < size; i++)
/* 208:    */     {
/* 209:322 */       T message = schema.newMessage();
/* 210:323 */       list.add(message);
/* 211:324 */       schema.mergeFrom(input, message);
/* 212:325 */       input.checkLastTagWas(0);
/* 213:    */     }
/* 214:328 */     assert (in.read() == -1);
/* 215:    */     
/* 216:330 */     return list;
/* 217:    */   }
/* 218:    */   
/* 219:    */   public static <T> boolean optMergeDelimitedFrom(InputStream in, T message, Schema<T> schema, LinkedBuffer buffer)
/* 220:    */     throws IOException
/* 221:    */   {
/* 222:346 */     return optMergeDelimitedFrom(in, message, schema, true, buffer);
/* 223:    */   }
/* 224:    */   
/* 225:    */   public static <T> boolean optMergeDelimitedFrom(InputStream in, T message, Schema<T> schema, boolean drainRemainingBytesIfTooLarge, LinkedBuffer buffer)
/* 226:    */     throws IOException
/* 227:    */   {
/* 228:360 */     if (buffer.start != buffer.offset) {
/* 229:361 */       throw new IllegalArgumentException("Buffer previously used and had not been reset.");
/* 230:    */     }
/* 231:363 */     int size = IOUtil.fillBufferWithDelimitedMessageFrom(in, drainRemainingBytesIfTooLarge, buffer);
/* 232:366 */     if (size == 0) {
/* 233:369 */       return true;
/* 234:    */     }
/* 235:372 */     if (buffer.start == buffer.offset) {
/* 236:375 */       return false;
/* 237:    */     }
/* 238:378 */     ByteArrayInput input = new ByteArrayInput(buffer.buffer, buffer.offset, size, true);
/* 239:    */     try
/* 240:    */     {
/* 241:382 */       schema.mergeFrom(input, message);
/* 242:383 */       input.checkLastTagWas(0);
/* 243:    */     }
/* 244:    */     catch (ArrayIndexOutOfBoundsException e)
/* 245:    */     {
/* 246:387 */       throw ProtobufException.truncatedMessage(e);
/* 247:    */     }
/* 248:    */     finally
/* 249:    */     {
/* 250:392 */       buffer.offset = buffer.start;
/* 251:    */     }
/* 252:395 */     return true;
/* 253:    */   }
/* 254:    */   
/* 255:    */   public static <T> int optWriteDelimitedTo(OutputStream out, T message, Schema<T> schema, LinkedBuffer buffer)
/* 256:    */     throws IOException
/* 257:    */   {
/* 258:407 */     if (buffer.start != buffer.offset) {
/* 259:408 */       throw new IllegalArgumentException("Buffer previously used and had not been reset.");
/* 260:    */     }
/* 261:410 */     ProtostuffOutput output = new ProtostuffOutput(buffer);
/* 262:    */     
/* 263:    */ 
/* 264:413 */     buffer.offset = (buffer.start + 5);
/* 265:414 */     output.size += 5;
/* 266:    */     
/* 267:416 */     schema.writeTo(output, message);
/* 268:    */     
/* 269:418 */     int size = output.size - 5;
/* 270:    */     
/* 271:420 */     int delimOffset = IOUtil.putVarInt32AndGetOffset(size, buffer.buffer, buffer.start);
/* 272:    */     
/* 273:    */ 
/* 274:    */ 
/* 275:424 */     out.write(buffer.buffer, delimOffset, buffer.offset - delimOffset);
/* 276:427 */     if (buffer.next != null) {
/* 277:428 */       LinkedBuffer.writeTo(out, buffer.next);
/* 278:    */     }
/* 279:430 */     return size;
/* 280:    */   }
/* 281:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.ProtostuffIOUtil
 * JD-Core Version:    0.7.0.1
 */