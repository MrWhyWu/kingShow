/*   1:    */ package io.protostuff;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import java.io.OutputStream;
/*   5:    */ import java.nio.ByteBuffer;
/*   6:    */ 
/*   7:    */ public final class ProtostuffOutput
/*   8:    */   extends WriteSession
/*   9:    */   implements Output
/*  10:    */ {
/*  11:    */   public ProtostuffOutput(LinkedBuffer buffer)
/*  12:    */   {
/*  13: 42 */     super(buffer);
/*  14:    */   }
/*  15:    */   
/*  16:    */   public ProtostuffOutput(LinkedBuffer buffer, OutputStream out)
/*  17:    */   {
/*  18: 47 */     super(buffer, out);
/*  19:    */   }
/*  20:    */   
/*  21:    */   public ProtostuffOutput(LinkedBuffer buffer, OutputStream out, WriteSession.FlushHandler flushHandler, int nextBufferSize)
/*  22:    */   {
/*  23: 53 */     super(buffer, out, flushHandler, nextBufferSize);
/*  24:    */   }
/*  25:    */   
/*  26:    */   public ProtostuffOutput clear()
/*  27:    */   {
/*  28: 62 */     super.clear();
/*  29: 63 */     return this;
/*  30:    */   }
/*  31:    */   
/*  32:    */   public void writeInt32(int fieldNumber, int value, boolean repeated)
/*  33:    */     throws IOException
/*  34:    */   {
/*  35: 69 */     if (value < 0) {
/*  36: 71 */       this.tail = this.sink.writeVarInt64(value, this, this.sink
/*  37:    */       
/*  38:    */ 
/*  39: 74 */         .writeVarInt32(
/*  40: 75 */         WireFormat.makeTag(fieldNumber, 0), this, this.tail));
/*  41:    */     } else {
/*  42: 81 */       this.tail = this.sink.writeVarInt32(value, this, this.sink
/*  43:    */       
/*  44:    */ 
/*  45: 84 */         .writeVarInt32(
/*  46: 85 */         WireFormat.makeTag(fieldNumber, 0), this, this.tail));
/*  47:    */     }
/*  48:    */   }
/*  49:    */   
/*  50:    */   public void writeUInt32(int fieldNumber, int value, boolean repeated)
/*  51:    */     throws IOException
/*  52:    */   {
/*  53: 99 */     this.tail = this.sink.writeVarInt32(value, this, this.sink
/*  54:    */     
/*  55:    */ 
/*  56:102 */       .writeVarInt32(
/*  57:103 */       WireFormat.makeTag(fieldNumber, 0), this, this.tail));
/*  58:    */   }
/*  59:    */   
/*  60:    */   public void writeSInt32(int fieldNumber, int value, boolean repeated)
/*  61:    */     throws IOException
/*  62:    */   {
/*  63:115 */     this.tail = this.sink.writeVarInt32(
/*  64:116 */       ProtobufOutput.encodeZigZag32(value), this, this.sink
/*  65:    */       
/*  66:118 */       .writeVarInt32(
/*  67:119 */       WireFormat.makeTag(fieldNumber, 0), this, this.tail));
/*  68:    */   }
/*  69:    */   
/*  70:    */   public void writeFixed32(int fieldNumber, int value, boolean repeated)
/*  71:    */     throws IOException
/*  72:    */   {
/*  73:131 */     this.tail = this.sink.writeInt32LE(value, this, this.sink
/*  74:    */     
/*  75:    */ 
/*  76:134 */       .writeVarInt32(
/*  77:135 */       WireFormat.makeTag(fieldNumber, 5), this, this.tail));
/*  78:    */   }
/*  79:    */   
/*  80:    */   public void writeSFixed32(int fieldNumber, int value, boolean repeated)
/*  81:    */     throws IOException
/*  82:    */   {
/*  83:147 */     this.tail = this.sink.writeInt32LE(value, this, this.sink
/*  84:    */     
/*  85:    */ 
/*  86:150 */       .writeVarInt32(
/*  87:151 */       WireFormat.makeTag(fieldNumber, 5), this, this.tail));
/*  88:    */   }
/*  89:    */   
/*  90:    */   public void writeInt64(int fieldNumber, long value, boolean repeated)
/*  91:    */     throws IOException
/*  92:    */   {
/*  93:163 */     this.tail = this.sink.writeVarInt64(value, this, this.sink
/*  94:    */     
/*  95:    */ 
/*  96:166 */       .writeVarInt32(
/*  97:167 */       WireFormat.makeTag(fieldNumber, 0), this, this.tail));
/*  98:    */   }
/*  99:    */   
/* 100:    */   public void writeUInt64(int fieldNumber, long value, boolean repeated)
/* 101:    */     throws IOException
/* 102:    */   {
/* 103:179 */     this.tail = this.sink.writeVarInt64(value, this, this.sink
/* 104:    */     
/* 105:    */ 
/* 106:182 */       .writeVarInt32(
/* 107:183 */       WireFormat.makeTag(fieldNumber, 0), this, this.tail));
/* 108:    */   }
/* 109:    */   
/* 110:    */   public void writeSInt64(int fieldNumber, long value, boolean repeated)
/* 111:    */     throws IOException
/* 112:    */   {
/* 113:195 */     this.tail = this.sink.writeVarInt64(
/* 114:196 */       ProtobufOutput.encodeZigZag64(value), this, this.sink
/* 115:    */       
/* 116:198 */       .writeVarInt32(
/* 117:199 */       WireFormat.makeTag(fieldNumber, 0), this, this.tail));
/* 118:    */   }
/* 119:    */   
/* 120:    */   public void writeFixed64(int fieldNumber, long value, boolean repeated)
/* 121:    */     throws IOException
/* 122:    */   {
/* 123:211 */     this.tail = this.sink.writeInt64LE(value, this, this.sink
/* 124:    */     
/* 125:    */ 
/* 126:214 */       .writeVarInt32(
/* 127:215 */       WireFormat.makeTag(fieldNumber, 1), this, this.tail));
/* 128:    */   }
/* 129:    */   
/* 130:    */   public void writeSFixed64(int fieldNumber, long value, boolean repeated)
/* 131:    */     throws IOException
/* 132:    */   {
/* 133:227 */     this.tail = this.sink.writeInt64LE(value, this, this.sink
/* 134:    */     
/* 135:    */ 
/* 136:230 */       .writeVarInt32(
/* 137:231 */       WireFormat.makeTag(fieldNumber, 1), this, this.tail));
/* 138:    */   }
/* 139:    */   
/* 140:    */   public void writeFloat(int fieldNumber, float value, boolean repeated)
/* 141:    */     throws IOException
/* 142:    */   {
/* 143:243 */     this.tail = this.sink.writeInt32LE(
/* 144:244 */       Float.floatToRawIntBits(value), this, this.sink
/* 145:    */       
/* 146:246 */       .writeVarInt32(
/* 147:247 */       WireFormat.makeTag(fieldNumber, 5), this, this.tail));
/* 148:    */   }
/* 149:    */   
/* 150:    */   public void writeDouble(int fieldNumber, double value, boolean repeated)
/* 151:    */     throws IOException
/* 152:    */   {
/* 153:260 */     this.tail = this.sink.writeInt64LE(
/* 154:261 */       Double.doubleToRawLongBits(value), this, this.sink
/* 155:    */       
/* 156:263 */       .writeVarInt32(
/* 157:264 */       WireFormat.makeTag(fieldNumber, 1), this, this.tail));
/* 158:    */   }
/* 159:    */   
/* 160:    */   public void writeBool(int fieldNumber, boolean value, boolean repeated)
/* 161:    */     throws IOException
/* 162:    */   {
/* 163:277 */     this.tail = this.sink.writeByte((byte)(value ? 1 : 0), this, this.sink
/* 164:    */     
/* 165:    */ 
/* 166:280 */       .writeVarInt32(
/* 167:281 */       WireFormat.makeTag(fieldNumber, 0), this, this.tail));
/* 168:    */   }
/* 169:    */   
/* 170:    */   public void writeEnum(int fieldNumber, int number, boolean repeated)
/* 171:    */     throws IOException
/* 172:    */   {
/* 173:293 */     writeInt32(fieldNumber, number, repeated);
/* 174:    */   }
/* 175:    */   
/* 176:    */   public void writeString(int fieldNumber, CharSequence value, boolean repeated)
/* 177:    */     throws IOException
/* 178:    */   {
/* 179:299 */     this.tail = this.sink.writeStrUTF8VarDelimited(value, this, this.sink
/* 180:    */     
/* 181:    */ 
/* 182:302 */       .writeVarInt32(
/* 183:303 */       WireFormat.makeTag(fieldNumber, 2), this, this.tail));
/* 184:    */   }
/* 185:    */   
/* 186:    */   public void writeBytes(int fieldNumber, ByteString value, boolean repeated)
/* 187:    */     throws IOException
/* 188:    */   {
/* 189:316 */     writeByteArray(fieldNumber, value.getBytes(), repeated);
/* 190:    */   }
/* 191:    */   
/* 192:    */   public void writeByteArray(int fieldNumber, byte[] bytes, boolean repeated)
/* 193:    */     throws IOException
/* 194:    */   {
/* 195:322 */     this.tail = this.sink.writeByteArray(bytes, 0, bytes.length, this, this.sink
/* 196:    */     
/* 197:    */ 
/* 198:325 */       .writeVarInt32(bytes.length, this, this.sink
/* 199:    */       
/* 200:    */ 
/* 201:328 */       .writeVarInt32(
/* 202:329 */       WireFormat.makeTag(fieldNumber, 2), this, this.tail)));
/* 203:    */   }
/* 204:    */   
/* 205:    */   public void writeByteRange(boolean utf8String, int fieldNumber, byte[] value, int offset, int length, boolean repeated)
/* 206:    */     throws IOException
/* 207:    */   {
/* 208:342 */     this.tail = this.sink.writeByteArray(value, offset, length, this, this.sink
/* 209:    */     
/* 210:    */ 
/* 211:345 */       .writeVarInt32(length, this, this.sink
/* 212:    */       
/* 213:    */ 
/* 214:348 */       .writeVarInt32(
/* 215:349 */       WireFormat.makeTag(fieldNumber, 2), this, this.tail)));
/* 216:    */   }
/* 217:    */   
/* 218:    */   public <T> void writeObject(int fieldNumber, T value, Schema<T> schema, boolean repeated)
/* 219:    */     throws IOException
/* 220:    */   {
/* 221:358 */     this.tail = this.sink.writeVarInt32(
/* 222:359 */       WireFormat.makeTag(fieldNumber, 3), this, this.tail);
/* 223:    */     
/* 224:    */ 
/* 225:    */ 
/* 226:363 */     schema.writeTo(this, value);
/* 227:    */     
/* 228:365 */     this.tail = this.sink.writeVarInt32(
/* 229:366 */       WireFormat.makeTag(fieldNumber, 4), this, this.tail);
/* 230:    */   }
/* 231:    */   
/* 232:    */   public void writeBytes(int fieldNumber, ByteBuffer value, boolean repeated)
/* 233:    */     throws IOException
/* 234:    */   {
/* 235:377 */     writeByteRange(false, fieldNumber, value.array(), value.arrayOffset() + value.position(), value
/* 236:378 */       .remaining(), repeated);
/* 237:    */   }
/* 238:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.ProtostuffOutput
 * JD-Core Version:    0.7.0.1
 */