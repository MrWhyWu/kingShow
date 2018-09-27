/*   1:    */ package io.protostuff;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import java.nio.ByteBuffer;
/*   5:    */ import java.util.List;
/*   6:    */ 
/*   7:    */ public final class LowCopyProtobufOutput
/*   8:    */   implements Output
/*   9:    */ {
/*  10:    */   public LinkBuffer buffer;
/*  11:    */   
/*  12:    */   public LowCopyProtobufOutput()
/*  13:    */   {
/*  14: 29 */     this.buffer = new LinkBuffer();
/*  15:    */   }
/*  16:    */   
/*  17:    */   public LowCopyProtobufOutput(LinkBuffer buffer)
/*  18:    */   {
/*  19: 34 */     this.buffer = buffer;
/*  20:    */   }
/*  21:    */   
/*  22:    */   public void writeInt32(int fieldNumber, int value, boolean repeated)
/*  23:    */     throws IOException
/*  24:    */   {
/*  25: 40 */     if (value < 0)
/*  26:    */     {
/*  27: 42 */       this.buffer.writeVarInt32(WireFormat.makeTag(fieldNumber, 0));
/*  28: 43 */       this.buffer.writeVarInt64(value);
/*  29:    */     }
/*  30:    */     else
/*  31:    */     {
/*  32: 47 */       this.buffer.writeVarInt32(WireFormat.makeTag(fieldNumber, 0));
/*  33: 48 */       this.buffer.writeVarInt32(value);
/*  34:    */     }
/*  35:    */   }
/*  36:    */   
/*  37:    */   public void writeUInt32(int fieldNumber, int value, boolean repeated)
/*  38:    */     throws IOException
/*  39:    */   {
/*  40: 55 */     this.buffer.writeVarInt32(WireFormat.makeTag(fieldNumber, 0));
/*  41: 56 */     this.buffer.writeVarInt32(value);
/*  42:    */   }
/*  43:    */   
/*  44:    */   public void writeSInt32(int fieldNumber, int value, boolean repeated)
/*  45:    */     throws IOException
/*  46:    */   {
/*  47: 62 */     this.buffer.writeVarInt32(WireFormat.makeTag(fieldNumber, 0));
/*  48: 63 */     this.buffer.writeVarInt32(ProtobufOutput.encodeZigZag32(value));
/*  49:    */   }
/*  50:    */   
/*  51:    */   public void writeFixed32(int fieldNumber, int value, boolean repeated)
/*  52:    */     throws IOException
/*  53:    */   {
/*  54: 69 */     this.buffer.writeVarInt32(WireFormat.makeTag(fieldNumber, 5));
/*  55: 70 */     this.buffer.writeInt32LE(value);
/*  56:    */   }
/*  57:    */   
/*  58:    */   public void writeSFixed32(int fieldNumber, int value, boolean repeated)
/*  59:    */     throws IOException
/*  60:    */   {
/*  61: 76 */     this.buffer.writeVarInt32(WireFormat.makeTag(fieldNumber, 5));
/*  62: 77 */     this.buffer.writeInt32LE(value);
/*  63:    */   }
/*  64:    */   
/*  65:    */   public void writeInt64(int fieldNumber, long value, boolean repeated)
/*  66:    */     throws IOException
/*  67:    */   {
/*  68: 83 */     this.buffer.writeVarInt32(WireFormat.makeTag(fieldNumber, 0));
/*  69: 84 */     this.buffer.writeVarInt64(value);
/*  70:    */   }
/*  71:    */   
/*  72:    */   public void writeUInt64(int fieldNumber, long value, boolean repeated)
/*  73:    */     throws IOException
/*  74:    */   {
/*  75: 90 */     this.buffer.writeVarInt32(WireFormat.makeTag(fieldNumber, 0));
/*  76: 91 */     this.buffer.writeVarInt64(value);
/*  77:    */   }
/*  78:    */   
/*  79:    */   public void writeSInt64(int fieldNumber, long value, boolean repeated)
/*  80:    */     throws IOException
/*  81:    */   {
/*  82: 97 */     this.buffer.writeVarInt32(WireFormat.makeTag(fieldNumber, 0));
/*  83: 98 */     this.buffer.writeVarInt64(ProtobufOutput.encodeZigZag64(value));
/*  84:    */   }
/*  85:    */   
/*  86:    */   public void writeFixed64(int fieldNumber, long value, boolean repeated)
/*  87:    */     throws IOException
/*  88:    */   {
/*  89:104 */     this.buffer.writeVarInt32(WireFormat.makeTag(fieldNumber, 1));
/*  90:105 */     this.buffer.writeInt64LE(value);
/*  91:    */   }
/*  92:    */   
/*  93:    */   public void writeSFixed64(int fieldNumber, long value, boolean repeated)
/*  94:    */     throws IOException
/*  95:    */   {
/*  96:111 */     this.buffer.writeVarInt32(WireFormat.makeTag(fieldNumber, 1));
/*  97:112 */     this.buffer.writeInt64LE(value);
/*  98:    */   }
/*  99:    */   
/* 100:    */   public void writeFloat(int fieldNumber, float value, boolean repeated)
/* 101:    */     throws IOException
/* 102:    */   {
/* 103:118 */     this.buffer.writeVarInt32(WireFormat.makeTag(fieldNumber, 5));
/* 104:119 */     this.buffer.writeInt32LE(Float.floatToRawIntBits(value));
/* 105:    */   }
/* 106:    */   
/* 107:    */   public void writeDouble(int fieldNumber, double value, boolean repeated)
/* 108:    */     throws IOException
/* 109:    */   {
/* 110:125 */     this.buffer.writeVarInt32(WireFormat.makeTag(fieldNumber, 1));
/* 111:126 */     this.buffer.writeInt64LE(Double.doubleToRawLongBits(value));
/* 112:    */   }
/* 113:    */   
/* 114:    */   public void writeBool(int fieldNumber, boolean value, boolean repeated)
/* 115:    */     throws IOException
/* 116:    */   {
/* 117:132 */     this.buffer.writeVarInt32(WireFormat.makeTag(fieldNumber, 0));
/* 118:133 */     this.buffer.writeByte((byte)(value ? 1 : 0));
/* 119:    */   }
/* 120:    */   
/* 121:    */   public void writeEnum(int fieldNumber, int number, boolean repeated)
/* 122:    */     throws IOException
/* 123:    */   {
/* 124:139 */     writeInt32(fieldNumber, number, repeated);
/* 125:    */   }
/* 126:    */   
/* 127:    */   public void writeString(int fieldNumber, CharSequence value, boolean repeated)
/* 128:    */     throws IOException
/* 129:    */   {
/* 130:146 */     byte[] strbytes = value.toString().getBytes("UTF-8");
/* 131:147 */     writeByteArray(fieldNumber, strbytes, repeated);
/* 132:    */   }
/* 133:    */   
/* 134:    */   public void writeBytes(int fieldNumber, ByteString value, boolean repeated)
/* 135:    */     throws IOException
/* 136:    */   {
/* 137:153 */     writeByteArray(fieldNumber, value.getBytes(), repeated);
/* 138:    */   }
/* 139:    */   
/* 140:    */   public void writeByteArray(int fieldNumber, byte[] bytes, boolean repeated)
/* 141:    */     throws IOException
/* 142:    */   {
/* 143:159 */     writeByteRange(false, fieldNumber, bytes, 0, bytes.length, repeated);
/* 144:    */   }
/* 145:    */   
/* 146:    */   public void writeByteRange(boolean utf8String, int fieldNumber, byte[] value, int offset, int length, boolean repeated)
/* 147:    */     throws IOException
/* 148:    */   {
/* 149:166 */     this.buffer.writeVarInt32(WireFormat.makeTag(fieldNumber, 2));
/* 150:167 */     this.buffer.writeVarInt32(length);
/* 151:168 */     this.buffer.writeByteArray(value, offset, length);
/* 152:    */   }
/* 153:    */   
/* 154:    */   public <T> void writeObject(int fieldNumber, T value, Schema<T> schema, boolean repeated)
/* 155:    */     throws IOException
/* 156:    */   {
/* 157:175 */     LinkBuffer subBuf = new LinkBuffer(this.buffer.allocSize);
/* 158:    */     
/* 159:177 */     LowCopyProtobufOutput subOutput = new LowCopyProtobufOutput(subBuf);
/* 160:178 */     schema.writeTo(subOutput, value);
/* 161:179 */     List<ByteBuffer> subBuffers = subBuf.finish();
/* 162:    */     
/* 163:181 */     long subSize = subBuf.size();
/* 164:    */     
/* 165:183 */     this.buffer.writeVarInt32(WireFormat.makeTag(fieldNumber, 2));
/* 166:184 */     this.buffer.writeVarInt64(subSize);
/* 167:185 */     for (ByteBuffer b : subBuffers) {
/* 168:187 */       this.buffer.writeByteBuffer(b);
/* 169:    */     }
/* 170:    */   }
/* 171:    */   
/* 172:    */   public void writeBytes(int fieldNumber, ByteBuffer value, boolean repeated)
/* 173:    */     throws IOException
/* 174:    */   {
/* 175:194 */     writeByteRange(false, fieldNumber, value.array(), value.arrayOffset() + value.position(), value
/* 176:195 */       .remaining(), repeated);
/* 177:    */   }
/* 178:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.LowCopyProtobufOutput
 * JD-Core Version:    0.7.0.1
 */