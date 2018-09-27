/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.PlatformDependent;
/*   4:    */ 
/*   5:    */ class UnpooledUnsafeHeapByteBuf
/*   6:    */   extends UnpooledHeapByteBuf
/*   7:    */ {
/*   8:    */   UnpooledUnsafeHeapByteBuf(ByteBufAllocator alloc, int initialCapacity, int maxCapacity)
/*   9:    */   {
/*  10: 29 */     super(alloc, initialCapacity, maxCapacity);
/*  11:    */   }
/*  12:    */   
/*  13:    */   byte[] allocateArray(int initialCapacity)
/*  14:    */   {
/*  15: 34 */     return PlatformDependent.allocateUninitializedArray(initialCapacity);
/*  16:    */   }
/*  17:    */   
/*  18:    */   public byte getByte(int index)
/*  19:    */   {
/*  20: 39 */     checkIndex(index);
/*  21: 40 */     return _getByte(index);
/*  22:    */   }
/*  23:    */   
/*  24:    */   protected byte _getByte(int index)
/*  25:    */   {
/*  26: 45 */     return UnsafeByteBufUtil.getByte(this.array, index);
/*  27:    */   }
/*  28:    */   
/*  29:    */   public short getShort(int index)
/*  30:    */   {
/*  31: 50 */     checkIndex(index, 2);
/*  32: 51 */     return _getShort(index);
/*  33:    */   }
/*  34:    */   
/*  35:    */   protected short _getShort(int index)
/*  36:    */   {
/*  37: 56 */     return UnsafeByteBufUtil.getShort(this.array, index);
/*  38:    */   }
/*  39:    */   
/*  40:    */   public short getShortLE(int index)
/*  41:    */   {
/*  42: 61 */     checkIndex(index, 2);
/*  43: 62 */     return _getShortLE(index);
/*  44:    */   }
/*  45:    */   
/*  46:    */   protected short _getShortLE(int index)
/*  47:    */   {
/*  48: 67 */     return UnsafeByteBufUtil.getShortLE(this.array, index);
/*  49:    */   }
/*  50:    */   
/*  51:    */   public int getUnsignedMedium(int index)
/*  52:    */   {
/*  53: 72 */     checkIndex(index, 3);
/*  54: 73 */     return _getUnsignedMedium(index);
/*  55:    */   }
/*  56:    */   
/*  57:    */   protected int _getUnsignedMedium(int index)
/*  58:    */   {
/*  59: 78 */     return UnsafeByteBufUtil.getUnsignedMedium(this.array, index);
/*  60:    */   }
/*  61:    */   
/*  62:    */   public int getUnsignedMediumLE(int index)
/*  63:    */   {
/*  64: 83 */     checkIndex(index, 3);
/*  65: 84 */     return _getUnsignedMediumLE(index);
/*  66:    */   }
/*  67:    */   
/*  68:    */   protected int _getUnsignedMediumLE(int index)
/*  69:    */   {
/*  70: 89 */     return UnsafeByteBufUtil.getUnsignedMediumLE(this.array, index);
/*  71:    */   }
/*  72:    */   
/*  73:    */   public int getInt(int index)
/*  74:    */   {
/*  75: 94 */     checkIndex(index, 4);
/*  76: 95 */     return _getInt(index);
/*  77:    */   }
/*  78:    */   
/*  79:    */   protected int _getInt(int index)
/*  80:    */   {
/*  81:100 */     return UnsafeByteBufUtil.getInt(this.array, index);
/*  82:    */   }
/*  83:    */   
/*  84:    */   public int getIntLE(int index)
/*  85:    */   {
/*  86:105 */     checkIndex(index, 4);
/*  87:106 */     return _getIntLE(index);
/*  88:    */   }
/*  89:    */   
/*  90:    */   protected int _getIntLE(int index)
/*  91:    */   {
/*  92:111 */     return UnsafeByteBufUtil.getIntLE(this.array, index);
/*  93:    */   }
/*  94:    */   
/*  95:    */   public long getLong(int index)
/*  96:    */   {
/*  97:116 */     checkIndex(index, 8);
/*  98:117 */     return _getLong(index);
/*  99:    */   }
/* 100:    */   
/* 101:    */   protected long _getLong(int index)
/* 102:    */   {
/* 103:122 */     return UnsafeByteBufUtil.getLong(this.array, index);
/* 104:    */   }
/* 105:    */   
/* 106:    */   public long getLongLE(int index)
/* 107:    */   {
/* 108:127 */     checkIndex(index, 8);
/* 109:128 */     return _getLongLE(index);
/* 110:    */   }
/* 111:    */   
/* 112:    */   protected long _getLongLE(int index)
/* 113:    */   {
/* 114:133 */     return UnsafeByteBufUtil.getLongLE(this.array, index);
/* 115:    */   }
/* 116:    */   
/* 117:    */   public ByteBuf setByte(int index, int value)
/* 118:    */   {
/* 119:138 */     checkIndex(index);
/* 120:139 */     _setByte(index, value);
/* 121:140 */     return this;
/* 122:    */   }
/* 123:    */   
/* 124:    */   protected void _setByte(int index, int value)
/* 125:    */   {
/* 126:145 */     UnsafeByteBufUtil.setByte(this.array, index, value);
/* 127:    */   }
/* 128:    */   
/* 129:    */   public ByteBuf setShort(int index, int value)
/* 130:    */   {
/* 131:150 */     checkIndex(index, 2);
/* 132:151 */     _setShort(index, value);
/* 133:152 */     return this;
/* 134:    */   }
/* 135:    */   
/* 136:    */   protected void _setShort(int index, int value)
/* 137:    */   {
/* 138:157 */     UnsafeByteBufUtil.setShort(this.array, index, value);
/* 139:    */   }
/* 140:    */   
/* 141:    */   public ByteBuf setShortLE(int index, int value)
/* 142:    */   {
/* 143:162 */     checkIndex(index, 2);
/* 144:163 */     _setShortLE(index, value);
/* 145:164 */     return this;
/* 146:    */   }
/* 147:    */   
/* 148:    */   protected void _setShortLE(int index, int value)
/* 149:    */   {
/* 150:169 */     UnsafeByteBufUtil.setShortLE(this.array, index, value);
/* 151:    */   }
/* 152:    */   
/* 153:    */   public ByteBuf setMedium(int index, int value)
/* 154:    */   {
/* 155:174 */     checkIndex(index, 3);
/* 156:175 */     _setMedium(index, value);
/* 157:176 */     return this;
/* 158:    */   }
/* 159:    */   
/* 160:    */   protected void _setMedium(int index, int value)
/* 161:    */   {
/* 162:181 */     UnsafeByteBufUtil.setMedium(this.array, index, value);
/* 163:    */   }
/* 164:    */   
/* 165:    */   public ByteBuf setMediumLE(int index, int value)
/* 166:    */   {
/* 167:186 */     checkIndex(index, 3);
/* 168:187 */     _setMediumLE(index, value);
/* 169:188 */     return this;
/* 170:    */   }
/* 171:    */   
/* 172:    */   protected void _setMediumLE(int index, int value)
/* 173:    */   {
/* 174:193 */     UnsafeByteBufUtil.setMediumLE(this.array, index, value);
/* 175:    */   }
/* 176:    */   
/* 177:    */   public ByteBuf setInt(int index, int value)
/* 178:    */   {
/* 179:198 */     checkIndex(index, 4);
/* 180:199 */     _setInt(index, value);
/* 181:200 */     return this;
/* 182:    */   }
/* 183:    */   
/* 184:    */   protected void _setInt(int index, int value)
/* 185:    */   {
/* 186:205 */     UnsafeByteBufUtil.setInt(this.array, index, value);
/* 187:    */   }
/* 188:    */   
/* 189:    */   public ByteBuf setIntLE(int index, int value)
/* 190:    */   {
/* 191:210 */     checkIndex(index, 4);
/* 192:211 */     _setIntLE(index, value);
/* 193:212 */     return this;
/* 194:    */   }
/* 195:    */   
/* 196:    */   protected void _setIntLE(int index, int value)
/* 197:    */   {
/* 198:217 */     UnsafeByteBufUtil.setIntLE(this.array, index, value);
/* 199:    */   }
/* 200:    */   
/* 201:    */   public ByteBuf setLong(int index, long value)
/* 202:    */   {
/* 203:222 */     checkIndex(index, 8);
/* 204:223 */     _setLong(index, value);
/* 205:224 */     return this;
/* 206:    */   }
/* 207:    */   
/* 208:    */   protected void _setLong(int index, long value)
/* 209:    */   {
/* 210:229 */     UnsafeByteBufUtil.setLong(this.array, index, value);
/* 211:    */   }
/* 212:    */   
/* 213:    */   public ByteBuf setLongLE(int index, long value)
/* 214:    */   {
/* 215:234 */     checkIndex(index, 8);
/* 216:235 */     _setLongLE(index, value);
/* 217:236 */     return this;
/* 218:    */   }
/* 219:    */   
/* 220:    */   protected void _setLongLE(int index, long value)
/* 221:    */   {
/* 222:241 */     UnsafeByteBufUtil.setLongLE(this.array, index, value);
/* 223:    */   }
/* 224:    */   
/* 225:    */   public ByteBuf setZero(int index, int length)
/* 226:    */   {
/* 227:246 */     if (PlatformDependent.javaVersion() >= 7)
/* 228:    */     {
/* 229:248 */       checkIndex(index, length);
/* 230:249 */       UnsafeByteBufUtil.setZero(this.array, index, length);
/* 231:250 */       return this;
/* 232:    */     }
/* 233:252 */     return super.setZero(index, length);
/* 234:    */   }
/* 235:    */   
/* 236:    */   public ByteBuf writeZero(int length)
/* 237:    */   {
/* 238:257 */     if (PlatformDependent.javaVersion() >= 7)
/* 239:    */     {
/* 240:259 */       ensureWritable(length);
/* 241:260 */       int wIndex = this.writerIndex;
/* 242:261 */       UnsafeByteBufUtil.setZero(this.array, wIndex, length);
/* 243:262 */       this.writerIndex = (wIndex + length);
/* 244:263 */       return this;
/* 245:    */     }
/* 246:265 */     return super.writeZero(length);
/* 247:    */   }
/* 248:    */   
/* 249:    */   @Deprecated
/* 250:    */   protected SwappedByteBuf newSwappedByteBuf()
/* 251:    */   {
/* 252:271 */     if (PlatformDependent.isUnaligned()) {
/* 253:273 */       return new UnsafeHeapSwappedByteBuf(this);
/* 254:    */     }
/* 255:275 */     return super.newSwappedByteBuf();
/* 256:    */   }
/* 257:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.UnpooledUnsafeHeapByteBuf
 * JD-Core Version:    0.7.0.1
 */