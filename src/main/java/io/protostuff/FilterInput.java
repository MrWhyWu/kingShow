/*   1:    */ package io.protostuff;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import java.nio.ByteBuffer;
/*   5:    */ 
/*   6:    */ public class FilterInput<F extends Input>
/*   7:    */   implements Input
/*   8:    */ {
/*   9:    */   protected final F input;
/*  10:    */   
/*  11:    */   public FilterInput(F input)
/*  12:    */   {
/*  13: 33 */     this.input = input;
/*  14:    */   }
/*  15:    */   
/*  16:    */   public <T> void handleUnknownField(int fieldNumber, Schema<T> schema)
/*  17:    */     throws IOException
/*  18:    */   {
/*  19: 39 */     this.input.handleUnknownField(fieldNumber, schema);
/*  20:    */   }
/*  21:    */   
/*  22:    */   public <T> int readFieldNumber(Schema<T> schema)
/*  23:    */     throws IOException
/*  24:    */   {
/*  25: 45 */     return this.input.readFieldNumber(schema);
/*  26:    */   }
/*  27:    */   
/*  28:    */   public boolean readBool()
/*  29:    */     throws IOException
/*  30:    */   {
/*  31: 51 */     return this.input.readBool();
/*  32:    */   }
/*  33:    */   
/*  34:    */   public byte[] readByteArray()
/*  35:    */     throws IOException
/*  36:    */   {
/*  37: 57 */     return this.input.readByteArray();
/*  38:    */   }
/*  39:    */   
/*  40:    */   public ByteString readBytes()
/*  41:    */     throws IOException
/*  42:    */   {
/*  43: 63 */     return this.input.readBytes();
/*  44:    */   }
/*  45:    */   
/*  46:    */   public void readBytes(ByteBuffer bb)
/*  47:    */     throws IOException
/*  48:    */   {
/*  49: 69 */     this.input.readBytes(bb);
/*  50:    */   }
/*  51:    */   
/*  52:    */   public double readDouble()
/*  53:    */     throws IOException
/*  54:    */   {
/*  55: 75 */     return this.input.readDouble();
/*  56:    */   }
/*  57:    */   
/*  58:    */   public int readEnum()
/*  59:    */     throws IOException
/*  60:    */   {
/*  61: 81 */     return this.input.readEnum();
/*  62:    */   }
/*  63:    */   
/*  64:    */   public int readFixed32()
/*  65:    */     throws IOException
/*  66:    */   {
/*  67: 87 */     return this.input.readFixed32();
/*  68:    */   }
/*  69:    */   
/*  70:    */   public long readFixed64()
/*  71:    */     throws IOException
/*  72:    */   {
/*  73: 93 */     return this.input.readFixed64();
/*  74:    */   }
/*  75:    */   
/*  76:    */   public float readFloat()
/*  77:    */     throws IOException
/*  78:    */   {
/*  79: 99 */     return this.input.readFloat();
/*  80:    */   }
/*  81:    */   
/*  82:    */   public int readInt32()
/*  83:    */     throws IOException
/*  84:    */   {
/*  85:105 */     return this.input.readInt32();
/*  86:    */   }
/*  87:    */   
/*  88:    */   public long readInt64()
/*  89:    */     throws IOException
/*  90:    */   {
/*  91:111 */     return this.input.readInt64();
/*  92:    */   }
/*  93:    */   
/*  94:    */   public int readSFixed32()
/*  95:    */     throws IOException
/*  96:    */   {
/*  97:117 */     return this.input.readSFixed32();
/*  98:    */   }
/*  99:    */   
/* 100:    */   public long readSFixed64()
/* 101:    */     throws IOException
/* 102:    */   {
/* 103:123 */     return this.input.readSFixed64();
/* 104:    */   }
/* 105:    */   
/* 106:    */   public int readSInt32()
/* 107:    */     throws IOException
/* 108:    */   {
/* 109:129 */     return this.input.readSInt32();
/* 110:    */   }
/* 111:    */   
/* 112:    */   public long readSInt64()
/* 113:    */     throws IOException
/* 114:    */   {
/* 115:135 */     return this.input.readSInt64();
/* 116:    */   }
/* 117:    */   
/* 118:    */   public String readString()
/* 119:    */     throws IOException
/* 120:    */   {
/* 121:141 */     return this.input.readString();
/* 122:    */   }
/* 123:    */   
/* 124:    */   public int readUInt32()
/* 125:    */     throws IOException
/* 126:    */   {
/* 127:147 */     return this.input.readUInt32();
/* 128:    */   }
/* 129:    */   
/* 130:    */   public long readUInt64()
/* 131:    */     throws IOException
/* 132:    */   {
/* 133:153 */     return this.input.readUInt64();
/* 134:    */   }
/* 135:    */   
/* 136:    */   public <T> T mergeObject(T value, Schema<T> schema)
/* 137:    */     throws IOException
/* 138:    */   {
/* 139:159 */     return this.input.mergeObject(value, schema);
/* 140:    */   }
/* 141:    */   
/* 142:    */   public void transferByteRangeTo(Output output, boolean utf8String, int fieldNumber, boolean repeated)
/* 143:    */     throws IOException
/* 144:    */   {
/* 145:166 */     this.input.transferByteRangeTo(output, utf8String, fieldNumber, repeated);
/* 146:    */   }
/* 147:    */   
/* 148:    */   public ByteBuffer readByteBuffer()
/* 149:    */     throws IOException
/* 150:    */   {
/* 151:175 */     return ByteBuffer.wrap(readByteArray());
/* 152:    */   }
/* 153:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.FilterInput
 * JD-Core Version:    0.7.0.1
 */