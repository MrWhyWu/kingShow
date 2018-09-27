/*   1:    */ package io.netty.handler.codec.serialization;
/*   2:    */ 
/*   3:    */ import java.io.DataInputStream;
/*   4:    */ import java.io.IOException;
/*   5:    */ import java.io.InputStream;
/*   6:    */ import java.io.ObjectInput;
/*   7:    */ import java.io.StreamCorruptedException;
/*   8:    */ 
/*   9:    */ public class ObjectDecoderInputStream
/*  10:    */   extends InputStream
/*  11:    */   implements ObjectInput
/*  12:    */ {
/*  13:    */   private final DataInputStream in;
/*  14:    */   private final int maxObjectSize;
/*  15:    */   private final ClassResolver classResolver;
/*  16:    */   
/*  17:    */   public ObjectDecoderInputStream(InputStream in)
/*  18:    */   {
/*  19: 44 */     this(in, null);
/*  20:    */   }
/*  21:    */   
/*  22:    */   public ObjectDecoderInputStream(InputStream in, ClassLoader classLoader)
/*  23:    */   {
/*  24: 58 */     this(in, classLoader, 1048576);
/*  25:    */   }
/*  26:    */   
/*  27:    */   public ObjectDecoderInputStream(InputStream in, int maxObjectSize)
/*  28:    */   {
/*  29: 73 */     this(in, null, maxObjectSize);
/*  30:    */   }
/*  31:    */   
/*  32:    */   public ObjectDecoderInputStream(InputStream in, ClassLoader classLoader, int maxObjectSize)
/*  33:    */   {
/*  34: 91 */     if (in == null) {
/*  35: 92 */       throw new NullPointerException("in");
/*  36:    */     }
/*  37: 94 */     if (maxObjectSize <= 0) {
/*  38: 95 */       throw new IllegalArgumentException("maxObjectSize: " + maxObjectSize);
/*  39:    */     }
/*  40: 97 */     if ((in instanceof DataInputStream)) {
/*  41: 98 */       this.in = ((DataInputStream)in);
/*  42:    */     } else {
/*  43:100 */       this.in = new DataInputStream(in);
/*  44:    */     }
/*  45:102 */     this.classResolver = ClassResolvers.weakCachingResolver(classLoader);
/*  46:103 */     this.maxObjectSize = maxObjectSize;
/*  47:    */   }
/*  48:    */   
/*  49:    */   public Object readObject()
/*  50:    */     throws ClassNotFoundException, IOException
/*  51:    */   {
/*  52:108 */     int dataLen = readInt();
/*  53:109 */     if (dataLen <= 0) {
/*  54:110 */       throw new StreamCorruptedException("invalid data length: " + dataLen);
/*  55:    */     }
/*  56:112 */     if (dataLen > this.maxObjectSize) {
/*  57:113 */       throw new StreamCorruptedException("data length too big: " + dataLen + " (max: " + this.maxObjectSize + ')');
/*  58:    */     }
/*  59:117 */     return new CompactObjectInputStream(this.in, this.classResolver).readObject();
/*  60:    */   }
/*  61:    */   
/*  62:    */   public int available()
/*  63:    */     throws IOException
/*  64:    */   {
/*  65:122 */     return this.in.available();
/*  66:    */   }
/*  67:    */   
/*  68:    */   public void close()
/*  69:    */     throws IOException
/*  70:    */   {
/*  71:127 */     this.in.close();
/*  72:    */   }
/*  73:    */   
/*  74:    */   public void mark(int readlimit)
/*  75:    */   {
/*  76:132 */     this.in.mark(readlimit);
/*  77:    */   }
/*  78:    */   
/*  79:    */   public boolean markSupported()
/*  80:    */   {
/*  81:137 */     return this.in.markSupported();
/*  82:    */   }
/*  83:    */   
/*  84:    */   public int read()
/*  85:    */     throws IOException
/*  86:    */   {
/*  87:142 */     return this.in.read();
/*  88:    */   }
/*  89:    */   
/*  90:    */   public final int read(byte[] b, int off, int len)
/*  91:    */     throws IOException
/*  92:    */   {
/*  93:147 */     return this.in.read(b, off, len);
/*  94:    */   }
/*  95:    */   
/*  96:    */   public final int read(byte[] b)
/*  97:    */     throws IOException
/*  98:    */   {
/*  99:152 */     return this.in.read(b);
/* 100:    */   }
/* 101:    */   
/* 102:    */   public final boolean readBoolean()
/* 103:    */     throws IOException
/* 104:    */   {
/* 105:157 */     return this.in.readBoolean();
/* 106:    */   }
/* 107:    */   
/* 108:    */   public final byte readByte()
/* 109:    */     throws IOException
/* 110:    */   {
/* 111:162 */     return this.in.readByte();
/* 112:    */   }
/* 113:    */   
/* 114:    */   public final char readChar()
/* 115:    */     throws IOException
/* 116:    */   {
/* 117:167 */     return this.in.readChar();
/* 118:    */   }
/* 119:    */   
/* 120:    */   public final double readDouble()
/* 121:    */     throws IOException
/* 122:    */   {
/* 123:172 */     return this.in.readDouble();
/* 124:    */   }
/* 125:    */   
/* 126:    */   public final float readFloat()
/* 127:    */     throws IOException
/* 128:    */   {
/* 129:177 */     return this.in.readFloat();
/* 130:    */   }
/* 131:    */   
/* 132:    */   public final void readFully(byte[] b, int off, int len)
/* 133:    */     throws IOException
/* 134:    */   {
/* 135:182 */     this.in.readFully(b, off, len);
/* 136:    */   }
/* 137:    */   
/* 138:    */   public final void readFully(byte[] b)
/* 139:    */     throws IOException
/* 140:    */   {
/* 141:187 */     this.in.readFully(b);
/* 142:    */   }
/* 143:    */   
/* 144:    */   public final int readInt()
/* 145:    */     throws IOException
/* 146:    */   {
/* 147:192 */     return this.in.readInt();
/* 148:    */   }
/* 149:    */   
/* 150:    */   @Deprecated
/* 151:    */   public final String readLine()
/* 152:    */     throws IOException
/* 153:    */   {
/* 154:201 */     return this.in.readLine();
/* 155:    */   }
/* 156:    */   
/* 157:    */   public final long readLong()
/* 158:    */     throws IOException
/* 159:    */   {
/* 160:206 */     return this.in.readLong();
/* 161:    */   }
/* 162:    */   
/* 163:    */   public final short readShort()
/* 164:    */     throws IOException
/* 165:    */   {
/* 166:211 */     return this.in.readShort();
/* 167:    */   }
/* 168:    */   
/* 169:    */   public final int readUnsignedByte()
/* 170:    */     throws IOException
/* 171:    */   {
/* 172:216 */     return this.in.readUnsignedByte();
/* 173:    */   }
/* 174:    */   
/* 175:    */   public final int readUnsignedShort()
/* 176:    */     throws IOException
/* 177:    */   {
/* 178:221 */     return this.in.readUnsignedShort();
/* 179:    */   }
/* 180:    */   
/* 181:    */   public final String readUTF()
/* 182:    */     throws IOException
/* 183:    */   {
/* 184:226 */     return this.in.readUTF();
/* 185:    */   }
/* 186:    */   
/* 187:    */   public void reset()
/* 188:    */     throws IOException
/* 189:    */   {
/* 190:231 */     this.in.reset();
/* 191:    */   }
/* 192:    */   
/* 193:    */   public long skip(long n)
/* 194:    */     throws IOException
/* 195:    */   {
/* 196:236 */     return this.in.skip(n);
/* 197:    */   }
/* 198:    */   
/* 199:    */   public final int skipBytes(int n)
/* 200:    */     throws IOException
/* 201:    */   {
/* 202:241 */     return this.in.skipBytes(n);
/* 203:    */   }
/* 204:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.serialization.ObjectDecoderInputStream
 * JD-Core Version:    0.7.0.1
 */