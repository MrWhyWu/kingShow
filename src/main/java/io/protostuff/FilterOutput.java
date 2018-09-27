/*   1:    */ package io.protostuff;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import java.nio.ByteBuffer;
/*   5:    */ 
/*   6:    */ public class FilterOutput<F extends Output>
/*   7:    */   implements Output
/*   8:    */ {
/*   9:    */   protected final F output;
/*  10:    */   
/*  11:    */   public FilterOutput(F output)
/*  12:    */   {
/*  13: 33 */     this.output = output;
/*  14:    */   }
/*  15:    */   
/*  16:    */   public void writeBool(int fieldNumber, boolean value, boolean repeated)
/*  17:    */     throws IOException
/*  18:    */   {
/*  19: 39 */     this.output.writeBool(fieldNumber, value, repeated);
/*  20:    */   }
/*  21:    */   
/*  22:    */   public void writeByteArray(int fieldNumber, byte[] value, boolean repeated)
/*  23:    */     throws IOException
/*  24:    */   {
/*  25: 45 */     this.output.writeByteArray(fieldNumber, value, repeated);
/*  26:    */   }
/*  27:    */   
/*  28:    */   public void writeByteRange(boolean utf8String, int fieldNumber, byte[] value, int offset, int length, boolean repeated)
/*  29:    */     throws IOException
/*  30:    */   {
/*  31: 52 */     this.output.writeByteRange(utf8String, fieldNumber, value, offset, length, repeated);
/*  32:    */   }
/*  33:    */   
/*  34:    */   public void writeBytes(int fieldNumber, ByteString value, boolean repeated)
/*  35:    */     throws IOException
/*  36:    */   {
/*  37: 58 */     this.output.writeBytes(fieldNumber, value, repeated);
/*  38:    */   }
/*  39:    */   
/*  40:    */   public void writeDouble(int fieldNumber, double value, boolean repeated)
/*  41:    */     throws IOException
/*  42:    */   {
/*  43: 64 */     this.output.writeDouble(fieldNumber, value, repeated);
/*  44:    */   }
/*  45:    */   
/*  46:    */   public void writeEnum(int fieldNumber, int value, boolean repeated)
/*  47:    */     throws IOException
/*  48:    */   {
/*  49: 70 */     this.output.writeEnum(fieldNumber, value, repeated);
/*  50:    */   }
/*  51:    */   
/*  52:    */   public void writeFixed32(int fieldNumber, int value, boolean repeated)
/*  53:    */     throws IOException
/*  54:    */   {
/*  55: 76 */     this.output.writeFixed32(fieldNumber, value, repeated);
/*  56:    */   }
/*  57:    */   
/*  58:    */   public void writeFixed64(int fieldNumber, long value, boolean repeated)
/*  59:    */     throws IOException
/*  60:    */   {
/*  61: 82 */     this.output.writeFixed64(fieldNumber, value, repeated);
/*  62:    */   }
/*  63:    */   
/*  64:    */   public void writeFloat(int fieldNumber, float value, boolean repeated)
/*  65:    */     throws IOException
/*  66:    */   {
/*  67: 88 */     this.output.writeFloat(fieldNumber, value, repeated);
/*  68:    */   }
/*  69:    */   
/*  70:    */   public void writeInt32(int fieldNumber, int value, boolean repeated)
/*  71:    */     throws IOException
/*  72:    */   {
/*  73: 94 */     this.output.writeInt32(fieldNumber, value, repeated);
/*  74:    */   }
/*  75:    */   
/*  76:    */   public void writeInt64(int fieldNumber, long value, boolean repeated)
/*  77:    */     throws IOException
/*  78:    */   {
/*  79:100 */     this.output.writeInt64(fieldNumber, value, repeated);
/*  80:    */   }
/*  81:    */   
/*  82:    */   public <T> void writeObject(int fieldNumber, T value, Schema<T> schema, boolean repeated)
/*  83:    */     throws IOException
/*  84:    */   {
/*  85:107 */     this.output.writeObject(fieldNumber, value, schema, repeated);
/*  86:    */   }
/*  87:    */   
/*  88:    */   public void writeSFixed32(int fieldNumber, int value, boolean repeated)
/*  89:    */     throws IOException
/*  90:    */   {
/*  91:113 */     this.output.writeSFixed32(fieldNumber, value, repeated);
/*  92:    */   }
/*  93:    */   
/*  94:    */   public void writeSFixed64(int fieldNumber, long value, boolean repeated)
/*  95:    */     throws IOException
/*  96:    */   {
/*  97:119 */     this.output.writeSFixed64(fieldNumber, value, repeated);
/*  98:    */   }
/*  99:    */   
/* 100:    */   public void writeSInt32(int fieldNumber, int value, boolean repeated)
/* 101:    */     throws IOException
/* 102:    */   {
/* 103:125 */     this.output.writeSInt32(fieldNumber, value, repeated);
/* 104:    */   }
/* 105:    */   
/* 106:    */   public void writeSInt64(int fieldNumber, long value, boolean repeated)
/* 107:    */     throws IOException
/* 108:    */   {
/* 109:131 */     this.output.writeSInt64(fieldNumber, value, repeated);
/* 110:    */   }
/* 111:    */   
/* 112:    */   public void writeString(int fieldNumber, CharSequence value, boolean repeated)
/* 113:    */     throws IOException
/* 114:    */   {
/* 115:137 */     this.output.writeString(fieldNumber, value, repeated);
/* 116:    */   }
/* 117:    */   
/* 118:    */   public void writeUInt32(int fieldNumber, int value, boolean repeated)
/* 119:    */     throws IOException
/* 120:    */   {
/* 121:143 */     this.output.writeUInt32(fieldNumber, value, repeated);
/* 122:    */   }
/* 123:    */   
/* 124:    */   public void writeUInt64(int fieldNumber, long value, boolean repeated)
/* 125:    */     throws IOException
/* 126:    */   {
/* 127:149 */     this.output.writeUInt64(fieldNumber, value, repeated);
/* 128:    */   }
/* 129:    */   
/* 130:    */   public void writeBytes(int fieldNumber, ByteBuffer value, boolean repeated)
/* 131:    */     throws IOException
/* 132:    */   {
/* 133:158 */     writeByteRange(false, fieldNumber, value.array(), value.arrayOffset() + value.position(), value
/* 134:159 */       .remaining(), repeated);
/* 135:    */   }
/* 136:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.FilterOutput
 * JD-Core Version:    0.7.0.1
 */