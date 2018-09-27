/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import java.io.DataInput;
/*   4:    */ import java.io.DataInputStream;
/*   5:    */ import java.io.EOFException;
/*   6:    */ import java.io.IOException;
/*   7:    */ import java.io.InputStream;
/*   8:    */ 
/*   9:    */ public class ByteBufInputStream
/*  10:    */   extends InputStream
/*  11:    */   implements DataInput
/*  12:    */ {
/*  13:    */   private final ByteBuf buffer;
/*  14:    */   private final int startIndex;
/*  15:    */   private final int endIndex;
/*  16:    */   private boolean closed;
/*  17:    */   private final boolean releaseOnClose;
/*  18:    */   
/*  19:    */   public ByteBufInputStream(ByteBuf buffer)
/*  20:    */   {
/*  21: 62 */     this(buffer, buffer.readableBytes());
/*  22:    */   }
/*  23:    */   
/*  24:    */   public ByteBufInputStream(ByteBuf buffer, int length)
/*  25:    */   {
/*  26: 76 */     this(buffer, length, false);
/*  27:    */   }
/*  28:    */   
/*  29:    */   public ByteBufInputStream(ByteBuf buffer, boolean releaseOnClose)
/*  30:    */   {
/*  31: 88 */     this(buffer, buffer.readableBytes(), releaseOnClose);
/*  32:    */   }
/*  33:    */   
/*  34:    */   public ByteBufInputStream(ByteBuf buffer, int length, boolean releaseOnClose)
/*  35:    */   {
/*  36:104 */     if (buffer == null) {
/*  37:105 */       throw new NullPointerException("buffer");
/*  38:    */     }
/*  39:107 */     if (length < 0)
/*  40:    */     {
/*  41:108 */       if (releaseOnClose) {
/*  42:109 */         buffer.release();
/*  43:    */       }
/*  44:111 */       throw new IllegalArgumentException("length: " + length);
/*  45:    */     }
/*  46:113 */     if (length > buffer.readableBytes())
/*  47:    */     {
/*  48:114 */       if (releaseOnClose) {
/*  49:115 */         buffer.release();
/*  50:    */       }
/*  51:118 */       throw new IndexOutOfBoundsException("Too many bytes to be read - Needs " + length + ", maximum is " + buffer.readableBytes());
/*  52:    */     }
/*  53:121 */     this.releaseOnClose = releaseOnClose;
/*  54:122 */     this.buffer = buffer;
/*  55:123 */     this.startIndex = buffer.readerIndex();
/*  56:124 */     this.endIndex = (this.startIndex + length);
/*  57:125 */     buffer.markReaderIndex();
/*  58:    */   }
/*  59:    */   
/*  60:    */   public int readBytes()
/*  61:    */   {
/*  62:132 */     return this.buffer.readerIndex() - this.startIndex;
/*  63:    */   }
/*  64:    */   
/*  65:    */   public void close()
/*  66:    */     throws IOException
/*  67:    */   {
/*  68:    */     try
/*  69:    */     {
/*  70:138 */       super.close();
/*  71:141 */       if ((this.releaseOnClose) && (!this.closed))
/*  72:    */       {
/*  73:142 */         this.closed = true;
/*  74:143 */         this.buffer.release();
/*  75:    */       }
/*  76:    */     }
/*  77:    */     finally
/*  78:    */     {
/*  79:141 */       if ((this.releaseOnClose) && (!this.closed))
/*  80:    */       {
/*  81:142 */         this.closed = true;
/*  82:143 */         this.buffer.release();
/*  83:    */       }
/*  84:    */     }
/*  85:    */   }
/*  86:    */   
/*  87:    */   public int available()
/*  88:    */     throws IOException
/*  89:    */   {
/*  90:150 */     return this.endIndex - this.buffer.readerIndex();
/*  91:    */   }
/*  92:    */   
/*  93:    */   public void mark(int readlimit)
/*  94:    */   {
/*  95:155 */     this.buffer.markReaderIndex();
/*  96:    */   }
/*  97:    */   
/*  98:    */   public boolean markSupported()
/*  99:    */   {
/* 100:160 */     return true;
/* 101:    */   }
/* 102:    */   
/* 103:    */   public int read()
/* 104:    */     throws IOException
/* 105:    */   {
/* 106:165 */     if (!this.buffer.isReadable()) {
/* 107:166 */       return -1;
/* 108:    */     }
/* 109:168 */     return this.buffer.readByte() & 0xFF;
/* 110:    */   }
/* 111:    */   
/* 112:    */   public int read(byte[] b, int off, int len)
/* 113:    */     throws IOException
/* 114:    */   {
/* 115:173 */     int available = available();
/* 116:174 */     if (available == 0) {
/* 117:175 */       return -1;
/* 118:    */     }
/* 119:178 */     len = Math.min(available, len);
/* 120:179 */     this.buffer.readBytes(b, off, len);
/* 121:180 */     return len;
/* 122:    */   }
/* 123:    */   
/* 124:    */   public void reset()
/* 125:    */     throws IOException
/* 126:    */   {
/* 127:185 */     this.buffer.resetReaderIndex();
/* 128:    */   }
/* 129:    */   
/* 130:    */   public long skip(long n)
/* 131:    */     throws IOException
/* 132:    */   {
/* 133:190 */     if (n > 2147483647L) {
/* 134:191 */       return skipBytes(2147483647);
/* 135:    */     }
/* 136:193 */     return skipBytes((int)n);
/* 137:    */   }
/* 138:    */   
/* 139:    */   public boolean readBoolean()
/* 140:    */     throws IOException
/* 141:    */   {
/* 142:199 */     checkAvailable(1);
/* 143:200 */     return read() != 0;
/* 144:    */   }
/* 145:    */   
/* 146:    */   public byte readByte()
/* 147:    */     throws IOException
/* 148:    */   {
/* 149:205 */     if (!this.buffer.isReadable()) {
/* 150:206 */       throw new EOFException();
/* 151:    */     }
/* 152:208 */     return this.buffer.readByte();
/* 153:    */   }
/* 154:    */   
/* 155:    */   public char readChar()
/* 156:    */     throws IOException
/* 157:    */   {
/* 158:213 */     return (char)readShort();
/* 159:    */   }
/* 160:    */   
/* 161:    */   public double readDouble()
/* 162:    */     throws IOException
/* 163:    */   {
/* 164:218 */     return Double.longBitsToDouble(readLong());
/* 165:    */   }
/* 166:    */   
/* 167:    */   public float readFloat()
/* 168:    */     throws IOException
/* 169:    */   {
/* 170:223 */     return Float.intBitsToFloat(readInt());
/* 171:    */   }
/* 172:    */   
/* 173:    */   public void readFully(byte[] b)
/* 174:    */     throws IOException
/* 175:    */   {
/* 176:228 */     readFully(b, 0, b.length);
/* 177:    */   }
/* 178:    */   
/* 179:    */   public void readFully(byte[] b, int off, int len)
/* 180:    */     throws IOException
/* 181:    */   {
/* 182:233 */     checkAvailable(len);
/* 183:234 */     this.buffer.readBytes(b, off, len);
/* 184:    */   }
/* 185:    */   
/* 186:    */   public int readInt()
/* 187:    */     throws IOException
/* 188:    */   {
/* 189:239 */     checkAvailable(4);
/* 190:240 */     return this.buffer.readInt();
/* 191:    */   }
/* 192:    */   
/* 193:243 */   private final StringBuilder lineBuf = new StringBuilder();
/* 194:    */   
/* 195:    */   public String readLine()
/* 196:    */     throws IOException
/* 197:    */   {
/* 198:247 */     this.lineBuf.setLength(0);
/* 199:    */     for (;;)
/* 200:    */     {
/* 201:250 */       if (!this.buffer.isReadable()) {
/* 202:251 */         return this.lineBuf.length() > 0 ? this.lineBuf.toString() : null;
/* 203:    */       }
/* 204:254 */       int c = this.buffer.readUnsignedByte();
/* 205:255 */       switch (c)
/* 206:    */       {
/* 207:    */       case 10: 
/* 208:    */         break;
/* 209:    */       case 13: 
/* 210:260 */         if ((!this.buffer.isReadable()) || ((char)this.buffer.getUnsignedByte(this.buffer.readerIndex()) != '\n')) {
/* 211:    */           break;
/* 212:    */         }
/* 213:261 */         this.buffer.skipBytes(1); break;
/* 214:    */       default: 
/* 215:266 */         this.lineBuf.append((char)c);
/* 216:    */       }
/* 217:    */     }
/* 218:270 */     return this.lineBuf.toString();
/* 219:    */   }
/* 220:    */   
/* 221:    */   public long readLong()
/* 222:    */     throws IOException
/* 223:    */   {
/* 224:275 */     checkAvailable(8);
/* 225:276 */     return this.buffer.readLong();
/* 226:    */   }
/* 227:    */   
/* 228:    */   public short readShort()
/* 229:    */     throws IOException
/* 230:    */   {
/* 231:281 */     checkAvailable(2);
/* 232:282 */     return this.buffer.readShort();
/* 233:    */   }
/* 234:    */   
/* 235:    */   public String readUTF()
/* 236:    */     throws IOException
/* 237:    */   {
/* 238:287 */     return DataInputStream.readUTF(this);
/* 239:    */   }
/* 240:    */   
/* 241:    */   public int readUnsignedByte()
/* 242:    */     throws IOException
/* 243:    */   {
/* 244:292 */     return readByte() & 0xFF;
/* 245:    */   }
/* 246:    */   
/* 247:    */   public int readUnsignedShort()
/* 248:    */     throws IOException
/* 249:    */   {
/* 250:297 */     return readShort() & 0xFFFF;
/* 251:    */   }
/* 252:    */   
/* 253:    */   public int skipBytes(int n)
/* 254:    */     throws IOException
/* 255:    */   {
/* 256:302 */     int nBytes = Math.min(available(), n);
/* 257:303 */     this.buffer.skipBytes(nBytes);
/* 258:304 */     return nBytes;
/* 259:    */   }
/* 260:    */   
/* 261:    */   private void checkAvailable(int fieldSize)
/* 262:    */     throws IOException
/* 263:    */   {
/* 264:308 */     if (fieldSize < 0) {
/* 265:309 */       throw new IndexOutOfBoundsException("fieldSize cannot be a negative number");
/* 266:    */     }
/* 267:311 */     if (fieldSize > available()) {
/* 268:313 */       throw new EOFException("fieldSize is too long! Length is " + fieldSize + ", but maximum is " + available());
/* 269:    */     }
/* 270:    */   }
/* 271:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.ByteBufInputStream
 * JD-Core Version:    0.7.0.1
 */