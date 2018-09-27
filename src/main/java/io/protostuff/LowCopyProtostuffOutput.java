/*   1:    */ package io.protostuff;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import java.nio.ByteBuffer;
/*   5:    */ 
/*   6:    */ public final class LowCopyProtostuffOutput
/*   7:    */   implements Output
/*   8:    */ {
/*   9:    */   public LinkBuffer buffer;
/*  10:    */   
/*  11:    */   public LowCopyProtostuffOutput()
/*  12:    */   {
/*  13: 30 */     this.buffer = new LinkBuffer();
/*  14:    */   }
/*  15:    */   
/*  16:    */   public LowCopyProtostuffOutput(LinkBuffer buffer)
/*  17:    */   {
/*  18: 35 */     this.buffer = buffer;
/*  19:    */   }
/*  20:    */   
/*  21:    */   public void writeInt32(int fieldNumber, int value, boolean repeated)
/*  22:    */     throws IOException
/*  23:    */   {
/*  24: 41 */     if (value < 0)
/*  25:    */     {
/*  26: 43 */       this.buffer.writeVarInt32(WireFormat.makeTag(fieldNumber, 0));
/*  27: 44 */       this.buffer.writeVarInt64(value);
/*  28:    */     }
/*  29:    */     else
/*  30:    */     {
/*  31: 48 */       this.buffer.writeVarInt32(WireFormat.makeTag(fieldNumber, 0));
/*  32: 49 */       this.buffer.writeVarInt32(value);
/*  33:    */     }
/*  34:    */   }
/*  35:    */   
/*  36:    */   public void writeUInt32(int fieldNumber, int value, boolean repeated)
/*  37:    */     throws IOException
/*  38:    */   {
/*  39: 56 */     this.buffer.writeVarInt32(WireFormat.makeTag(fieldNumber, 0));
/*  40: 57 */     this.buffer.writeVarInt32(value);
/*  41:    */   }
/*  42:    */   
/*  43:    */   public void writeSInt32(int fieldNumber, int value, boolean repeated)
/*  44:    */     throws IOException
/*  45:    */   {
/*  46: 63 */     this.buffer.writeVarInt32(WireFormat.makeTag(fieldNumber, 0));
/*  47: 64 */     this.buffer.writeVarInt32(ProtobufOutput.encodeZigZag32(value));
/*  48:    */   }
/*  49:    */   
/*  50:    */   public void writeFixed32(int fieldNumber, int value, boolean repeated)
/*  51:    */     throws IOException
/*  52:    */   {
/*  53: 70 */     this.buffer.writeVarInt32(WireFormat.makeTag(fieldNumber, 5));
/*  54: 71 */     this.buffer.writeInt32LE(value);
/*  55:    */   }
/*  56:    */   
/*  57:    */   public void writeSFixed32(int fieldNumber, int value, boolean repeated)
/*  58:    */     throws IOException
/*  59:    */   {
/*  60: 77 */     this.buffer.writeVarInt32(WireFormat.makeTag(fieldNumber, 5));
/*  61: 78 */     this.buffer.writeInt32LE(value);
/*  62:    */   }
/*  63:    */   
/*  64:    */   public void writeInt64(int fieldNumber, long value, boolean repeated)
/*  65:    */     throws IOException
/*  66:    */   {
/*  67: 84 */     this.buffer.writeVarInt32(WireFormat.makeTag(fieldNumber, 0));
/*  68: 85 */     this.buffer.writeVarInt64(value);
/*  69:    */   }
/*  70:    */   
/*  71:    */   public void writeUInt64(int fieldNumber, long value, boolean repeated)
/*  72:    */     throws IOException
/*  73:    */   {
/*  74: 91 */     this.buffer.writeVarInt32(WireFormat.makeTag(fieldNumber, 0));
/*  75: 92 */     this.buffer.writeVarInt64(value);
/*  76:    */   }
/*  77:    */   
/*  78:    */   public void writeSInt64(int fieldNumber, long value, boolean repeated)
/*  79:    */     throws IOException
/*  80:    */   {
/*  81: 98 */     this.buffer.writeVarInt32(WireFormat.makeTag(fieldNumber, 0));
/*  82: 99 */     this.buffer.writeVarInt64(ProtobufOutput.encodeZigZag64(value));
/*  83:    */   }
/*  84:    */   
/*  85:    */   public void writeFixed64(int fieldNumber, long value, boolean repeated)
/*  86:    */     throws IOException
/*  87:    */   {
/*  88:105 */     this.buffer.writeVarInt32(WireFormat.makeTag(fieldNumber, 1));
/*  89:106 */     this.buffer.writeInt64LE(value);
/*  90:    */   }
/*  91:    */   
/*  92:    */   public void writeSFixed64(int fieldNumber, long value, boolean repeated)
/*  93:    */     throws IOException
/*  94:    */   {
/*  95:112 */     this.buffer.writeVarInt32(WireFormat.makeTag(fieldNumber, 1));
/*  96:113 */     this.buffer.writeInt64LE(value);
/*  97:    */   }
/*  98:    */   
/*  99:    */   public void writeFloat(int fieldNumber, float value, boolean repeated)
/* 100:    */     throws IOException
/* 101:    */   {
/* 102:119 */     this.buffer.writeVarInt32(WireFormat.makeTag(fieldNumber, 5));
/* 103:120 */     this.buffer.writeInt32LE(Float.floatToRawIntBits(value));
/* 104:    */   }
/* 105:    */   
/* 106:    */   public void writeDouble(int fieldNumber, double value, boolean repeated)
/* 107:    */     throws IOException
/* 108:    */   {
/* 109:126 */     this.buffer.writeVarInt32(WireFormat.makeTag(fieldNumber, 1));
/* 110:127 */     this.buffer.writeInt64LE(Double.doubleToRawLongBits(value));
/* 111:    */   }
/* 112:    */   
/* 113:    */   public void writeBool(int fieldNumber, boolean value, boolean repeated)
/* 114:    */     throws IOException
/* 115:    */   {
/* 116:133 */     this.buffer.writeVarInt32(WireFormat.makeTag(fieldNumber, 0));
/* 117:134 */     this.buffer.writeByte((byte)(value ? 1 : 0));
/* 118:    */   }
/* 119:    */   
/* 120:    */   public void writeEnum(int fieldNumber, int number, boolean repeated)
/* 121:    */     throws IOException
/* 122:    */   {
/* 123:140 */     writeInt32(fieldNumber, number, repeated);
/* 124:    */   }
/* 125:    */   
/* 126:    */   public void writeString(int fieldNumber, CharSequence value, boolean repeated)
/* 127:    */     throws IOException
/* 128:    */   {
/* 129:147 */     byte[] strbytes = value.toString().getBytes("UTF-8");
/* 130:148 */     writeByteArray(fieldNumber, strbytes, repeated);
/* 131:    */   }
/* 132:    */   
/* 133:    */   public void writeBytes(int fieldNumber, ByteString value, boolean repeated)
/* 134:    */     throws IOException
/* 135:    */   {
/* 136:154 */     writeByteArray(fieldNumber, value.getBytes(), repeated);
/* 137:    */   }
/* 138:    */   
/* 139:    */   public void writeByteArray(int fieldNumber, byte[] bytes, boolean repeated)
/* 140:    */     throws IOException
/* 141:    */   {
/* 142:160 */     writeByteRange(false, fieldNumber, bytes, 0, bytes.length, repeated);
/* 143:    */   }
/* 144:    */   
/* 145:    */   public void writeByteRange(boolean utf8String, int fieldNumber, byte[] value, int offset, int length, boolean repeated)
/* 146:    */     throws IOException
/* 147:    */   {
/* 148:167 */     this.buffer.writeVarInt32(WireFormat.makeTag(fieldNumber, 2));
/* 149:168 */     this.buffer.writeVarInt32(length);
/* 150:169 */     this.buffer.writeByteArray(value, offset, length);
/* 151:    */   }
/* 152:    */   
/* 153:    */   public <T> void writeObject(int fieldNumber, T value, Schema<T> schema, boolean repeated)
/* 154:    */     throws IOException
/* 155:    */   {
/* 156:176 */     this.buffer.writeVarInt32(WireFormat.makeTag(fieldNumber, 3));
/* 157:177 */     schema.writeTo(this, value);
/* 158:178 */     this.buffer.writeVarInt32(WireFormat.makeTag(fieldNumber, 4));
/* 159:    */   }
/* 160:    */   
/* 161:    */   public void writeBytes(int fieldNumber, ByteBuffer value, boolean repeated)
/* 162:    */     throws IOException
/* 163:    */   {
/* 164:184 */     writeByteRange(false, fieldNumber, value.array(), value.arrayOffset() + value.position(), value
/* 165:185 */       .remaining(), repeated);
/* 166:    */   }
/* 167:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.LowCopyProtostuffOutput
 * JD-Core Version:    0.7.0.1
 */