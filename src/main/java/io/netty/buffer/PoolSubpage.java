/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ final class PoolSubpage<T>
/*   4:    */   implements PoolSubpageMetric
/*   5:    */ {
/*   6:    */   final PoolChunk<T> chunk;
/*   7:    */   private final int memoryMapIdx;
/*   8:    */   private final int runOffset;
/*   9:    */   private final int pageSize;
/*  10:    */   private final long[] bitmap;
/*  11:    */   PoolSubpage<T> prev;
/*  12:    */   PoolSubpage<T> next;
/*  13:    */   boolean doNotDestroy;
/*  14:    */   int elemSize;
/*  15:    */   private int maxNumElems;
/*  16:    */   private int bitmapLength;
/*  17:    */   private int nextAvail;
/*  18:    */   private int numAvail;
/*  19:    */   
/*  20:    */   PoolSubpage(int pageSize)
/*  21:    */   {
/*  22: 42 */     this.chunk = null;
/*  23: 43 */     this.memoryMapIdx = -1;
/*  24: 44 */     this.runOffset = -1;
/*  25: 45 */     this.elemSize = -1;
/*  26: 46 */     this.pageSize = pageSize;
/*  27: 47 */     this.bitmap = null;
/*  28:    */   }
/*  29:    */   
/*  30:    */   PoolSubpage(PoolSubpage<T> head, PoolChunk<T> chunk, int memoryMapIdx, int runOffset, int pageSize, int elemSize)
/*  31:    */   {
/*  32: 51 */     this.chunk = chunk;
/*  33: 52 */     this.memoryMapIdx = memoryMapIdx;
/*  34: 53 */     this.runOffset = runOffset;
/*  35: 54 */     this.pageSize = pageSize;
/*  36: 55 */     this.bitmap = new long[pageSize >>> 10];
/*  37: 56 */     init(head, elemSize);
/*  38:    */   }
/*  39:    */   
/*  40:    */   void init(PoolSubpage<T> head, int elemSize)
/*  41:    */   {
/*  42: 60 */     this.doNotDestroy = true;
/*  43: 61 */     this.elemSize = elemSize;
/*  44: 62 */     if (elemSize != 0)
/*  45:    */     {
/*  46: 63 */       this.maxNumElems = (this.numAvail = this.pageSize / elemSize);
/*  47: 64 */       this.nextAvail = 0;
/*  48: 65 */       this.bitmapLength = (this.maxNumElems >>> 6);
/*  49: 66 */       if ((this.maxNumElems & 0x3F) != 0) {
/*  50: 67 */         this.bitmapLength += 1;
/*  51:    */       }
/*  52: 70 */       for (int i = 0; i < this.bitmapLength; i++) {
/*  53: 71 */         this.bitmap[i] = 0L;
/*  54:    */       }
/*  55:    */     }
/*  56: 74 */     addToPool(head);
/*  57:    */   }
/*  58:    */   
/*  59:    */   long allocate()
/*  60:    */   {
/*  61: 81 */     if (this.elemSize == 0) {
/*  62: 82 */       return toHandle(0);
/*  63:    */     }
/*  64: 85 */     if ((this.numAvail == 0) || (!this.doNotDestroy)) {
/*  65: 86 */       return -1L;
/*  66:    */     }
/*  67: 89 */     int bitmapIdx = getNextAvail();
/*  68: 90 */     int q = bitmapIdx >>> 6;
/*  69: 91 */     int r = bitmapIdx & 0x3F;
/*  70: 92 */     assert ((this.bitmap[q] >>> r & 1L) == 0L);
/*  71: 93 */     this.bitmap[q] |= 1L << r;
/*  72: 95 */     if (--this.numAvail == 0) {
/*  73: 96 */       removeFromPool();
/*  74:    */     }
/*  75: 99 */     return toHandle(bitmapIdx);
/*  76:    */   }
/*  77:    */   
/*  78:    */   boolean free(PoolSubpage<T> head, int bitmapIdx)
/*  79:    */   {
/*  80:107 */     if (this.elemSize == 0) {
/*  81:108 */       return true;
/*  82:    */     }
/*  83:110 */     int q = bitmapIdx >>> 6;
/*  84:111 */     int r = bitmapIdx & 0x3F;
/*  85:112 */     assert ((this.bitmap[q] >>> r & 1L) != 0L);
/*  86:113 */     this.bitmap[q] ^= 1L << r;
/*  87:    */     
/*  88:115 */     setNextAvail(bitmapIdx);
/*  89:117 */     if (this.numAvail++ == 0)
/*  90:    */     {
/*  91:118 */       addToPool(head);
/*  92:119 */       return true;
/*  93:    */     }
/*  94:122 */     if (this.numAvail != this.maxNumElems) {
/*  95:123 */       return true;
/*  96:    */     }
/*  97:126 */     if (this.prev == this.next) {
/*  98:128 */       return true;
/*  99:    */     }
/* 100:132 */     this.doNotDestroy = false;
/* 101:133 */     removeFromPool();
/* 102:134 */     return false;
/* 103:    */   }
/* 104:    */   
/* 105:    */   private void addToPool(PoolSubpage<T> head)
/* 106:    */   {
/* 107:139 */     assert ((this.prev == null) && (this.next == null));
/* 108:140 */     this.prev = head;
/* 109:141 */     this.next = head.next;
/* 110:142 */     this.next.prev = this;
/* 111:143 */     head.next = this;
/* 112:    */   }
/* 113:    */   
/* 114:    */   private void removeFromPool()
/* 115:    */   {
/* 116:147 */     assert ((this.prev != null) && (this.next != null));
/* 117:148 */     this.prev.next = this.next;
/* 118:149 */     this.next.prev = this.prev;
/* 119:150 */     this.next = null;
/* 120:151 */     this.prev = null;
/* 121:    */   }
/* 122:    */   
/* 123:    */   private void setNextAvail(int bitmapIdx)
/* 124:    */   {
/* 125:155 */     this.nextAvail = bitmapIdx;
/* 126:    */   }
/* 127:    */   
/* 128:    */   private int getNextAvail()
/* 129:    */   {
/* 130:159 */     int nextAvail = this.nextAvail;
/* 131:160 */     if (nextAvail >= 0)
/* 132:    */     {
/* 133:161 */       this.nextAvail = -1;
/* 134:162 */       return nextAvail;
/* 135:    */     }
/* 136:164 */     return findNextAvail();
/* 137:    */   }
/* 138:    */   
/* 139:    */   private int findNextAvail()
/* 140:    */   {
/* 141:168 */     long[] bitmap = this.bitmap;
/* 142:169 */     int bitmapLength = this.bitmapLength;
/* 143:170 */     for (int i = 0; i < bitmapLength; i++)
/* 144:    */     {
/* 145:171 */       long bits = bitmap[i];
/* 146:172 */       if ((bits ^ 0xFFFFFFFF) != 0L) {
/* 147:173 */         return findNextAvail0(i, bits);
/* 148:    */       }
/* 149:    */     }
/* 150:176 */     return -1;
/* 151:    */   }
/* 152:    */   
/* 153:    */   private int findNextAvail0(int i, long bits)
/* 154:    */   {
/* 155:180 */     int maxNumElems = this.maxNumElems;
/* 156:181 */     int baseVal = i << 6;
/* 157:183 */     for (int j = 0; j < 64; j++)
/* 158:    */     {
/* 159:184 */       if ((bits & 1L) == 0L)
/* 160:    */       {
/* 161:185 */         int val = baseVal | j;
/* 162:186 */         if (val >= maxNumElems) {
/* 163:    */           break;
/* 164:    */         }
/* 165:187 */         return val;
/* 166:    */       }
/* 167:192 */       bits >>>= 1;
/* 168:    */     }
/* 169:194 */     return -1;
/* 170:    */   }
/* 171:    */   
/* 172:    */   private long toHandle(int bitmapIdx)
/* 173:    */   {
/* 174:198 */     return 0x0 | bitmapIdx << 32 | this.memoryMapIdx;
/* 175:    */   }
/* 176:    */   
/* 177:    */   public String toString()
/* 178:    */   {
/* 179:    */     int elemSize;
/* 180:207 */     synchronized (this.chunk.arena)
/* 181:    */     {
/* 182:    */       int maxNumElems;
/* 183:208 */       if (!this.doNotDestroy)
/* 184:    */       {
/* 185:209 */         boolean doNotDestroy = false;
/* 186:    */         int elemSize;
/* 187:    */         int numAvail;
/* 188:211 */         maxNumElems = numAvail = elemSize = -1;
/* 189:    */       }
/* 190:    */       else
/* 191:    */       {
/* 192:213 */         boolean doNotDestroy = true;
/* 193:214 */         int maxNumElems = this.maxNumElems;
/* 194:215 */         int numAvail = this.numAvail;
/* 195:216 */         elemSize = this.elemSize;
/* 196:    */       }
/* 197:    */     }
/* 198:    */     int elemSize;
/* 199:    */     int numAvail;
/* 200:    */     int maxNumElems;
/* 201:    */     boolean doNotDestroy;
/* 202:220 */     if (!doNotDestroy) {
/* 203:221 */       return "(" + this.memoryMapIdx + ": not in use)";
/* 204:    */     }
/* 205:224 */     return "(" + this.memoryMapIdx + ": " + (maxNumElems - numAvail) + '/' + maxNumElems + ", offset: " + this.runOffset + ", length: " + this.pageSize + ", elemSize: " + elemSize + ')';
/* 206:    */   }
/* 207:    */   
/* 208:    */   public int maxNumElements()
/* 209:    */   {
/* 210:230 */     synchronized (this.chunk.arena)
/* 211:    */     {
/* 212:231 */       return this.maxNumElems;
/* 213:    */     }
/* 214:    */   }
/* 215:    */   
/* 216:    */   public int numAvailable()
/* 217:    */   {
/* 218:237 */     synchronized (this.chunk.arena)
/* 219:    */     {
/* 220:238 */       return this.numAvail;
/* 221:    */     }
/* 222:    */   }
/* 223:    */   
/* 224:    */   public int elementSize()
/* 225:    */   {
/* 226:244 */     synchronized (this.chunk.arena)
/* 227:    */     {
/* 228:245 */       return this.elemSize;
/* 229:    */     }
/* 230:    */   }
/* 231:    */   
/* 232:    */   public int pageSize()
/* 233:    */   {
/* 234:251 */     return this.pageSize;
/* 235:    */   }
/* 236:    */   
/* 237:    */   void destroy()
/* 238:    */   {
/* 239:255 */     if (this.chunk != null) {
/* 240:256 */       this.chunk.destroy();
/* 241:    */     }
/* 242:    */   }
/* 243:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.PoolSubpage
 * JD-Core Version:    0.7.0.1
 */