/*   1:    */ package io.protostuff;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import java.nio.ByteBuffer;
/*   5:    */ import java.util.ArrayList;
/*   6:    */ import java.util.Collections;
/*   7:    */ import java.util.List;
/*   8:    */ 
/*   9:    */ public class LinkBuffer
/*  10:    */ {
/*  11:    */   public static final int DEFAULT_BUFFER_SIZE = 256;
/*  12:    */   public final int allocSize;
/*  13:    */   ByteBuffer current;
/*  14: 21 */   List<ByteBuffer> buffers = new ArrayList();
/*  15:    */   
/*  16:    */   public LinkBuffer()
/*  17:    */   {
/*  18: 25 */     this(256);
/*  19:    */   }
/*  20:    */   
/*  21:    */   public LinkBuffer(int allocSize)
/*  22:    */   {
/*  23: 30 */     assert (allocSize >= 8);
/*  24:    */     
/*  25: 32 */     this.allocSize = allocSize;
/*  26:    */     
/*  27: 34 */     this.current = ByteBuffer.allocate(allocSize);
/*  28:    */   }
/*  29:    */   
/*  30:    */   public long size()
/*  31:    */   {
/*  32: 39 */     long size = 0L;
/*  33: 40 */     for (ByteBuffer b : this.buffers) {
/*  34: 42 */       size += b.remaining();
/*  35:    */     }
/*  36: 44 */     if (this.current != null) {
/*  37: 45 */       size += this.current.position();
/*  38:    */     }
/*  39: 47 */     return size;
/*  40:    */   }
/*  41:    */   
/*  42:    */   public List<ByteBuffer> getBuffers()
/*  43:    */   {
/*  44: 52 */     int size = this.buffers.size() + (this.current != null ? 1 : 0);
/*  45: 53 */     List<ByteBuffer> copy = new ArrayList(size);
/*  46: 54 */     for (ByteBuffer b : this.buffers) {
/*  47: 56 */       copy.add(b.duplicate());
/*  48:    */     }
/*  49: 58 */     if (this.current != null)
/*  50:    */     {
/*  51: 60 */       ByteBuffer duplicate = this.current.duplicate();
/*  52: 61 */       duplicate.flip();
/*  53: 62 */       copy.add(duplicate);
/*  54:    */     }
/*  55: 64 */     return Collections.unmodifiableList(copy);
/*  56:    */   }
/*  57:    */   
/*  58:    */   private void nextBuffer()
/*  59:    */   {
/*  60: 69 */     this.current.flip();
/*  61: 70 */     this.buffers.add(this.current);
/*  62: 71 */     this.current = ByteBuffer.allocate(this.allocSize);
/*  63:    */   }
/*  64:    */   
/*  65:    */   private void spliceBuffer(ByteBuffer buf)
/*  66:    */   {
/*  67: 77 */     if (this.current.position() == 0)
/*  68:    */     {
/*  69: 79 */       this.buffers.add(buf);
/*  70: 80 */       return;
/*  71:    */     }
/*  72: 83 */     this.current.flip();
/*  73: 84 */     this.buffers.add(this.current);
/*  74: 85 */     this.buffers.add(buf);
/*  75: 86 */     this.current = ByteBuffer.allocate(this.allocSize);
/*  76:    */   }
/*  77:    */   
/*  78:    */   private void ensureCapacity(int needed)
/*  79:    */   {
/*  80: 91 */     if (this.current.remaining() < needed) {
/*  81: 93 */       nextBuffer();
/*  82:    */     }
/*  83:    */   }
/*  84:    */   
/*  85:    */   public List<ByteBuffer> finish()
/*  86:    */   {
/*  87: 99 */     this.current.flip();
/*  88:100 */     this.buffers.add(this.current);
/*  89:101 */     this.current = null;
/*  90:    */     
/*  91:    */ 
/*  92:    */ 
/*  93:105 */     this.buffers = Collections.unmodifiableList(this.buffers);
/*  94:106 */     return getBuffers();
/*  95:    */   }
/*  96:    */   
/*  97:    */   public LinkBuffer writeByte(byte value)
/*  98:    */     throws IOException
/*  99:    */   {
/* 100:112 */     ensureCapacity(1);
/* 101:    */     
/* 102:114 */     this.current.put(value);
/* 103:115 */     return this;
/* 104:    */   }
/* 105:    */   
/* 106:    */   public LinkBuffer writeInt16(int value)
/* 107:    */     throws IOException
/* 108:    */   {
/* 109:121 */     ensureCapacity(2);
/* 110:    */     
/* 111:123 */     this.current.putShort((short)value);
/* 112:    */     
/* 113:125 */     return this;
/* 114:    */   }
/* 115:    */   
/* 116:    */   public LinkBuffer writeInt16LE(int value)
/* 117:    */     throws IOException
/* 118:    */   {
/* 119:130 */     ensureCapacity(2);
/* 120:131 */     IntSerializer.writeInt16LE(value, this.current);
/* 121:132 */     return this;
/* 122:    */   }
/* 123:    */   
/* 124:    */   public LinkBuffer writeInt32(int value)
/* 125:    */     throws IOException
/* 126:    */   {
/* 127:137 */     ensureCapacity(4);
/* 128:138 */     this.current.putInt(value);
/* 129:139 */     return this;
/* 130:    */   }
/* 131:    */   
/* 132:    */   public LinkBuffer writeInt32LE(int value)
/* 133:    */     throws IOException
/* 134:    */   {
/* 135:144 */     ensureCapacity(4);
/* 136:145 */     IntSerializer.writeInt32LE(value, this.current);
/* 137:146 */     return this;
/* 138:    */   }
/* 139:    */   
/* 140:    */   public LinkBuffer writeInt64(long value)
/* 141:    */     throws IOException
/* 142:    */   {
/* 143:151 */     ensureCapacity(8);
/* 144:152 */     this.current.putLong(value);
/* 145:153 */     return this;
/* 146:    */   }
/* 147:    */   
/* 148:    */   public LinkBuffer writeInt64LE(long value)
/* 149:    */     throws IOException
/* 150:    */   {
/* 151:158 */     ensureCapacity(8);
/* 152:159 */     IntSerializer.writeInt64LE(value, this.current);
/* 153:160 */     return this;
/* 154:    */   }
/* 155:    */   
/* 156:    */   public LinkBuffer writeVarInt32(int value)
/* 157:    */     throws IOException
/* 158:    */   {
/* 159:165 */     byte[] buf = new byte[5];
/* 160:166 */     int locPtr = 0;
/* 161:    */     for (;;)
/* 162:    */     {
/* 163:169 */       if ((value & 0xFFFFFF80) == 0)
/* 164:    */       {
/* 165:171 */         buf[(locPtr++)] = ((byte)value);
/* 166:    */         
/* 167:173 */         ensureCapacity(locPtr);
/* 168:174 */         this.current.put(buf, 0, locPtr);
/* 169:    */         
/* 170:176 */         return this;
/* 171:    */       }
/* 172:181 */       buf[(locPtr++)] = ((byte)(value & 0x7F | 0x80));
/* 173:182 */       value >>>= 7;
/* 174:    */     }
/* 175:    */   }
/* 176:    */   
/* 177:    */   public LinkBuffer writeVarInt64(long value)
/* 178:    */     throws IOException
/* 179:    */   {
/* 180:190 */     byte[] buf = new byte[10];
/* 181:191 */     int locPtr = 0;
/* 182:    */     for (;;)
/* 183:    */     {
/* 184:195 */       if ((value & 0xFFFFFF80) == 0L)
/* 185:    */       {
/* 186:197 */         buf[(locPtr++)] = ((byte)(int)value);
/* 187:198 */         ensureCapacity(locPtr);
/* 188:199 */         this.current.put(buf, 0, locPtr);
/* 189:200 */         return this;
/* 190:    */       }
/* 191:204 */       buf[(locPtr++)] = ((byte)((int)value & 0x7F | 0x80));
/* 192:205 */       value >>>= 7;
/* 193:    */     }
/* 194:    */   }
/* 195:    */   
/* 196:    */   public LinkBuffer writeDouble(double value)
/* 197:    */     throws IOException
/* 198:    */   {
/* 199:212 */     return writeInt64(Double.doubleToRawLongBits(value));
/* 200:    */   }
/* 201:    */   
/* 202:    */   public LinkBuffer writeFloat(float value)
/* 203:    */     throws IOException
/* 204:    */   {
/* 205:217 */     return writeInt32(Float.floatToRawIntBits(value));
/* 206:    */   }
/* 207:    */   
/* 208:    */   public LinkBuffer writeByteArray(byte[] value, int offset, int length)
/* 209:    */     throws IOException
/* 210:    */   {
/* 211:224 */     if (this.current.remaining() >= length)
/* 212:    */     {
/* 213:227 */       this.current.put(value, offset, length);
/* 214:    */     }
/* 215:    */     else
/* 216:    */     {
/* 217:232 */       ByteBuffer wrapped = ByteBuffer.wrap(value, offset, length);
/* 218:233 */       spliceBuffer(wrapped);
/* 219:    */     }
/* 220:235 */     return this;
/* 221:    */   }
/* 222:    */   
/* 223:    */   public LinkBuffer writeByteArray(byte[] value)
/* 224:    */     throws IOException
/* 225:    */   {
/* 226:240 */     return writeByteArray(value, 0, value.length);
/* 227:    */   }
/* 228:    */   
/* 229:    */   public LinkBuffer writeByteBuffer(ByteBuffer buf)
/* 230:    */   {
/* 231:245 */     ByteBuffer cp = buf.slice();
/* 232:246 */     if (this.current.remaining() >= cp.remaining()) {
/* 233:248 */       this.current.put(cp);
/* 234:    */     } else {
/* 235:253 */       spliceBuffer(cp);
/* 236:    */     }
/* 237:255 */     return this;
/* 238:    */   }
/* 239:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.LinkBuffer
 * JD-Core Version:    0.7.0.1
 */