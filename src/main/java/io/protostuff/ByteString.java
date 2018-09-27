/*   1:    */ package io.protostuff;
/*   2:    */ 
/*   3:    */ import java.io.DataOutput;
/*   4:    */ import java.io.IOException;
/*   5:    */ import java.io.OutputStream;
/*   6:    */ import java.io.UnsupportedEncodingException;
/*   7:    */ import java.nio.ByteBuffer;
/*   8:    */ 
/*   9:    */ public final class ByteString
/*  10:    */ {
/*  11:    */   private final byte[] bytes;
/*  12:    */   public static final String EMPTY_STRING = "";
/*  13:    */   
/*  14:    */   static ByteString wrap(byte[] bytes)
/*  15:    */   {
/*  16: 68 */     return new ByteString(bytes);
/*  17:    */   }
/*  18:    */   
/*  19:    */   byte[] getBytes()
/*  20:    */   {
/*  21: 74 */     return this.bytes;
/*  22:    */   }
/*  23:    */   
/*  24:    */   public static void writeTo(OutputStream out, ByteString bs)
/*  25:    */     throws IOException
/*  26:    */   {
/*  27: 82 */     out.write(bs.bytes);
/*  28:    */   }
/*  29:    */   
/*  30:    */   public static void writeTo(DataOutput out, ByteString bs)
/*  31:    */     throws IOException
/*  32:    */   {
/*  33: 90 */     out.write(bs.bytes);
/*  34:    */   }
/*  35:    */   
/*  36:    */   public static void writeTo(Output output, ByteString bs, int fieldNumber, boolean repeated)
/*  37:    */     throws IOException
/*  38:    */   {
/*  39: 99 */     output.writeByteArray(fieldNumber, bs.bytes, repeated);
/*  40:    */   }
/*  41:    */   
/*  42:    */   public String toString()
/*  43:    */   {
/*  44:105 */     return String.format("<ByteString@%s size=%d>", new Object[] {
/*  45:106 */       Integer.toHexString(System.identityHashCode(this)), Integer.valueOf(size()) });
/*  46:    */   }
/*  47:    */   
/*  48:    */   private ByteString(byte[] bytes)
/*  49:    */   {
/*  50:114 */     this.bytes = bytes;
/*  51:    */   }
/*  52:    */   
/*  53:    */   public byte byteAt(int index)
/*  54:    */   {
/*  55:125 */     return this.bytes[index];
/*  56:    */   }
/*  57:    */   
/*  58:    */   public int size()
/*  59:    */   {
/*  60:133 */     return this.bytes.length;
/*  61:    */   }
/*  62:    */   
/*  63:    */   public boolean isEmpty()
/*  64:    */   {
/*  65:141 */     return this.bytes.length == 0;
/*  66:    */   }
/*  67:    */   
/*  68:155 */   public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
/*  69:160 */   public static final ByteString EMPTY = new ByteString(EMPTY_BYTE_ARRAY);
/*  70:    */   
/*  71:    */   public static ByteString copyFrom(byte[] bytes, int offset, int size)
/*  72:    */   {
/*  73:168 */     byte[] copy = new byte[size];
/*  74:169 */     System.arraycopy(bytes, offset, copy, 0, size);
/*  75:170 */     return new ByteString(copy);
/*  76:    */   }
/*  77:    */   
/*  78:    */   public static ByteString copyFrom(byte[] bytes)
/*  79:    */   {
/*  80:178 */     return copyFrom(bytes, 0, bytes.length);
/*  81:    */   }
/*  82:    */   
/*  83:    */   public static ByteString copyFrom(String text, String charsetName)
/*  84:    */   {
/*  85:    */     try
/*  86:    */     {
/*  87:189 */       return new ByteString(text.getBytes(charsetName));
/*  88:    */     }
/*  89:    */     catch (UnsupportedEncodingException e)
/*  90:    */     {
/*  91:193 */       throw new RuntimeException(charsetName + " not supported?", e);
/*  92:    */     }
/*  93:    */   }
/*  94:    */   
/*  95:    */   public static ByteString copyFromUtf8(String text)
/*  96:    */   {
/*  97:202 */     return new ByteString(StringSerializer.STRING.ser(text));
/*  98:    */   }
/*  99:    */   
/* 100:    */   public void copyTo(byte[] target, int offset)
/* 101:    */   {
/* 102:222 */     System.arraycopy(this.bytes, 0, target, offset, this.bytes.length);
/* 103:    */   }
/* 104:    */   
/* 105:    */   public void copyTo(byte[] target, int sourceOffset, int targetOffset, int size)
/* 106:    */   {
/* 107:241 */     System.arraycopy(this.bytes, sourceOffset, target, targetOffset, size);
/* 108:    */   }
/* 109:    */   
/* 110:    */   public byte[] toByteArray()
/* 111:    */   {
/* 112:249 */     int size = this.bytes.length;
/* 113:250 */     byte[] copy = new byte[size];
/* 114:251 */     System.arraycopy(this.bytes, 0, copy, 0, size);
/* 115:252 */     return copy;
/* 116:    */   }
/* 117:    */   
/* 118:    */   public ByteBuffer asReadOnlyByteBuffer()
/* 119:    */   {
/* 120:260 */     ByteBuffer byteBuffer = ByteBuffer.wrap(this.bytes);
/* 121:261 */     return byteBuffer.asReadOnlyBuffer();
/* 122:    */   }
/* 123:    */   
/* 124:    */   public String toStringUtf8()
/* 125:    */   {
/* 126:277 */     return StringSerializer.STRING.deser(this.bytes);
/* 127:    */   }
/* 128:    */   
/* 129:    */   public boolean equals(Object o)
/* 130:    */   {
/* 131:290 */     return (o == this) || (((o instanceof ByteString)) && (equals(this, (ByteString)o, false)));
/* 132:    */   }
/* 133:    */   
/* 134:    */   public static boolean equals(ByteString bs, ByteString other, boolean checkHash)
/* 135:    */   {
/* 136:298 */     int size = bs.bytes.length;
/* 137:299 */     if (size != other.bytes.length) {
/* 138:301 */       return false;
/* 139:    */     }
/* 140:304 */     if (checkHash)
/* 141:    */     {
/* 142:307 */       int h1 = bs.hash;int h2 = other.hash;
/* 143:308 */       if ((h1 != 0) && (h2 != 0) && (h1 != h2)) {
/* 144:310 */         return false;
/* 145:    */       }
/* 146:    */     }
/* 147:314 */     byte[] thisBytes = bs.bytes;
/* 148:315 */     byte[] otherBytes = other.bytes;
/* 149:316 */     for (int i = 0; i < size; i++) {
/* 150:318 */       if (thisBytes[i] != otherBytes[i]) {
/* 151:320 */         return false;
/* 152:    */       }
/* 153:    */     }
/* 154:324 */     return true;
/* 155:    */   }
/* 156:    */   
/* 157:    */   public boolean equals(byte[] data)
/* 158:    */   {
/* 159:332 */     return equals(data, 0, data.length);
/* 160:    */   }
/* 161:    */   
/* 162:    */   public boolean equals(byte[] data, int offset, int len)
/* 163:    */   {
/* 164:340 */     byte[] bytes = this.bytes;
/* 165:341 */     if (len != bytes.length) {
/* 166:342 */       return false;
/* 167:    */     }
/* 168:344 */     for (int i = 0; i < len;) {
/* 169:346 */       if (bytes[(i++)] != data[(offset++)]) {
/* 170:348 */         return false;
/* 171:    */       }
/* 172:    */     }
/* 173:352 */     return true;
/* 174:    */   }
/* 175:    */   
/* 176:355 */   private volatile int hash = 0;
/* 177:    */   
/* 178:    */   public int hashCode()
/* 179:    */   {
/* 180:360 */     int h = this.hash;
/* 181:362 */     if (h == 0)
/* 182:    */     {
/* 183:364 */       byte[] thisBytes = this.bytes;
/* 184:365 */       int size = this.bytes.length;
/* 185:    */       
/* 186:367 */       h = size;
/* 187:368 */       for (int i = 0; i < size; i++) {
/* 188:370 */         h = h * 31 + thisBytes[i];
/* 189:    */       }
/* 190:372 */       if (h == 0) {
/* 191:374 */         h = 1;
/* 192:    */       }
/* 193:377 */       this.hash = h;
/* 194:    */     }
/* 195:380 */     return h;
/* 196:    */   }
/* 197:    */   
/* 198:    */   public static String stringDefaultValue(String bytes)
/* 199:    */   {
/* 200:    */     try
/* 201:    */     {
/* 202:487 */       return new String(bytes.getBytes("ISO-8859-1"), "UTF-8");
/* 203:    */     }
/* 204:    */     catch (UnsupportedEncodingException e)
/* 205:    */     {
/* 206:493 */       throw new IllegalStateException("Java VM does not support a standard character set.", e);
/* 207:    */     }
/* 208:    */   }
/* 209:    */   
/* 210:    */   public static ByteString bytesDefaultValue(String bytes)
/* 211:    */   {
/* 212:506 */     return new ByteString(byteArrayDefaultValue(bytes));
/* 213:    */   }
/* 214:    */   
/* 215:    */   public static byte[] byteArrayDefaultValue(String bytes)
/* 216:    */   {
/* 217:    */     try
/* 218:    */     {
/* 219:519 */       return bytes.getBytes("ISO-8859-1");
/* 220:    */     }
/* 221:    */     catch (UnsupportedEncodingException e)
/* 222:    */     {
/* 223:525 */       throw new IllegalStateException("Java VM does not support a standard character set.", e);
/* 224:    */     }
/* 225:    */   }
/* 226:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.ByteString
 * JD-Core Version:    0.7.0.1
 */