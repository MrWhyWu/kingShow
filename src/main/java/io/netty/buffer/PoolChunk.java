/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ final class PoolChunk<T>
/*   4:    */   implements PoolChunkMetric
/*   5:    */ {
/*   6:    */   private static final int INTEGER_SIZE_MINUS_ONE = 31;
/*   7:    */   final PoolArena<T> arena;
/*   8:    */   final T memory;
/*   9:    */   final boolean unpooled;
/*  10:    */   final int offset;
/*  11:    */   private final byte[] memoryMap;
/*  12:    */   private final byte[] depthMap;
/*  13:    */   private final PoolSubpage<T>[] subpages;
/*  14:    */   private final int subpageOverflowMask;
/*  15:    */   private final int pageSize;
/*  16:    */   private final int pageShifts;
/*  17:    */   private final int maxOrder;
/*  18:    */   private final int chunkSize;
/*  19:    */   private final int log2ChunkSize;
/*  20:    */   private final int maxSubpageAllocs;
/*  21:    */   private final byte unusable;
/*  22:    */   private int freeBytes;
/*  23:    */   PoolChunkList<T> parent;
/*  24:    */   PoolChunk<T> prev;
/*  25:    */   PoolChunk<T> next;
/*  26:    */   
/*  27:    */   PoolChunk(PoolArena<T> arena, T memory, int pageSize, int maxOrder, int pageShifts, int chunkSize, int offset)
/*  28:    */   {
/*  29:136 */     this.unpooled = false;
/*  30:137 */     this.arena = arena;
/*  31:138 */     this.memory = memory;
/*  32:139 */     this.pageSize = pageSize;
/*  33:140 */     this.pageShifts = pageShifts;
/*  34:141 */     this.maxOrder = maxOrder;
/*  35:142 */     this.chunkSize = chunkSize;
/*  36:143 */     this.offset = offset;
/*  37:144 */     this.unusable = ((byte)(maxOrder + 1));
/*  38:145 */     this.log2ChunkSize = log2(chunkSize);
/*  39:146 */     this.subpageOverflowMask = (pageSize - 1 ^ 0xFFFFFFFF);
/*  40:147 */     this.freeBytes = chunkSize;
/*  41:    */     
/*  42:149 */     assert (maxOrder < 30) : ("maxOrder should be < 30, but is: " + maxOrder);
/*  43:150 */     this.maxSubpageAllocs = (1 << maxOrder);
/*  44:    */     
/*  45:    */ 
/*  46:153 */     this.memoryMap = new byte[this.maxSubpageAllocs << 1];
/*  47:154 */     this.depthMap = new byte[this.memoryMap.length];
/*  48:155 */     int memoryMapIndex = 1;
/*  49:156 */     for (int d = 0; d <= maxOrder; d++)
/*  50:    */     {
/*  51:157 */       int depth = 1 << d;
/*  52:158 */       for (int p = 0; p < depth; p++)
/*  53:    */       {
/*  54:160 */         this.memoryMap[memoryMapIndex] = ((byte)d);
/*  55:161 */         this.depthMap[memoryMapIndex] = ((byte)d);
/*  56:162 */         memoryMapIndex++;
/*  57:    */       }
/*  58:    */     }
/*  59:166 */     this.subpages = newSubpageArray(this.maxSubpageAllocs);
/*  60:    */   }
/*  61:    */   
/*  62:    */   PoolChunk(PoolArena<T> arena, T memory, int size, int offset)
/*  63:    */   {
/*  64:171 */     this.unpooled = true;
/*  65:172 */     this.arena = arena;
/*  66:173 */     this.memory = memory;
/*  67:174 */     this.offset = offset;
/*  68:175 */     this.memoryMap = null;
/*  69:176 */     this.depthMap = null;
/*  70:177 */     this.subpages = null;
/*  71:178 */     this.subpageOverflowMask = 0;
/*  72:179 */     this.pageSize = 0;
/*  73:180 */     this.pageShifts = 0;
/*  74:181 */     this.maxOrder = 0;
/*  75:182 */     this.unusable = ((byte)(this.maxOrder + 1));
/*  76:183 */     this.chunkSize = size;
/*  77:184 */     this.log2ChunkSize = log2(this.chunkSize);
/*  78:185 */     this.maxSubpageAllocs = 0;
/*  79:    */   }
/*  80:    */   
/*  81:    */   private PoolSubpage<T>[] newSubpageArray(int size)
/*  82:    */   {
/*  83:190 */     return new PoolSubpage[size];
/*  84:    */   }
/*  85:    */   
/*  86:    */   public int usage()
/*  87:    */   {
/*  88:    */     int freeBytes;
/*  89:196 */     synchronized (this.arena)
/*  90:    */     {
/*  91:197 */       freeBytes = this.freeBytes;
/*  92:    */     }
/*  93:    */     int freeBytes;
/*  94:199 */     return usage(freeBytes);
/*  95:    */   }
/*  96:    */   
/*  97:    */   private int usage(int freeBytes)
/*  98:    */   {
/*  99:203 */     if (freeBytes == 0) {
/* 100:204 */       return 100;
/* 101:    */     }
/* 102:207 */     int freePercentage = (int)(freeBytes * 100L / this.chunkSize);
/* 103:208 */     if (freePercentage == 0) {
/* 104:209 */       return 99;
/* 105:    */     }
/* 106:211 */     return 100 - freePercentage;
/* 107:    */   }
/* 108:    */   
/* 109:    */   long allocate(int normCapacity)
/* 110:    */   {
/* 111:215 */     if ((normCapacity & this.subpageOverflowMask) != 0) {
/* 112:216 */       return allocateRun(normCapacity);
/* 113:    */     }
/* 114:218 */     return allocateSubpage(normCapacity);
/* 115:    */   }
/* 116:    */   
/* 117:    */   private void updateParentsAlloc(int id)
/* 118:    */   {
/* 119:231 */     while (id > 1)
/* 120:    */     {
/* 121:232 */       int parentId = id >>> 1;
/* 122:233 */       byte val1 = value(id);
/* 123:234 */       byte val2 = value(id ^ 0x1);
/* 124:235 */       byte val = val1 < val2 ? val1 : val2;
/* 125:236 */       setValue(parentId, val);
/* 126:237 */       id = parentId;
/* 127:    */     }
/* 128:    */   }
/* 129:    */   
/* 130:    */   private void updateParentsFree(int id)
/* 131:    */   {
/* 132:249 */     int logChild = depth(id) + 1;
/* 133:250 */     while (id > 1)
/* 134:    */     {
/* 135:251 */       int parentId = id >>> 1;
/* 136:252 */       byte val1 = value(id);
/* 137:253 */       byte val2 = value(id ^ 0x1);
/* 138:254 */       logChild--;
/* 139:256 */       if ((val1 == logChild) && (val2 == logChild))
/* 140:    */       {
/* 141:257 */         setValue(parentId, (byte)(logChild - 1));
/* 142:    */       }
/* 143:    */       else
/* 144:    */       {
/* 145:259 */         byte val = val1 < val2 ? val1 : val2;
/* 146:260 */         setValue(parentId, val);
/* 147:    */       }
/* 148:263 */       id = parentId;
/* 149:    */     }
/* 150:    */   }
/* 151:    */   
/* 152:    */   private int allocateNode(int d)
/* 153:    */   {
/* 154:275 */     int id = 1;
/* 155:276 */     int initial = -(1 << d);
/* 156:277 */     byte val = value(id);
/* 157:278 */     if (val > d) {
/* 158:279 */       return -1;
/* 159:    */     }
/* 160:281 */     while ((val < d) || ((id & initial) == 0))
/* 161:    */     {
/* 162:282 */       id <<= 1;
/* 163:283 */       val = value(id);
/* 164:284 */       if (val > d)
/* 165:    */       {
/* 166:285 */         id ^= 0x1;
/* 167:286 */         val = value(id);
/* 168:    */       }
/* 169:    */     }
/* 170:289 */     byte value = value(id);
/* 171:290 */     if ((!$assertionsDisabled) && ((value != d) || ((id & initial) != 1 << d))) {
/* 172:290 */       throw new AssertionError(String.format("val = %d, id & initial = %d, d = %d", new Object[] {
/* 173:291 */         Byte.valueOf(value), Integer.valueOf(id & initial), Integer.valueOf(d) }));
/* 174:    */     }
/* 175:292 */     setValue(id, this.unusable);
/* 176:293 */     updateParentsAlloc(id);
/* 177:294 */     return id;
/* 178:    */   }
/* 179:    */   
/* 180:    */   private long allocateRun(int normCapacity)
/* 181:    */   {
/* 182:304 */     int d = this.maxOrder - (log2(normCapacity) - this.pageShifts);
/* 183:305 */     int id = allocateNode(d);
/* 184:306 */     if (id < 0) {
/* 185:307 */       return id;
/* 186:    */     }
/* 187:309 */     this.freeBytes -= runLength(id);
/* 188:310 */     return id;
/* 189:    */   }
/* 190:    */   
/* 191:    */   private long allocateSubpage(int normCapacity)
/* 192:    */   {
/* 193:323 */     PoolSubpage<T> head = this.arena.findSubpagePoolHead(normCapacity);
/* 194:324 */     synchronized (head)
/* 195:    */     {
/* 196:325 */       int d = this.maxOrder;
/* 197:326 */       int id = allocateNode(d);
/* 198:327 */       if (id < 0) {
/* 199:328 */         return id;
/* 200:    */       }
/* 201:331 */       PoolSubpage<T>[] subpages = this.subpages;
/* 202:332 */       int pageSize = this.pageSize;
/* 203:    */       
/* 204:334 */       this.freeBytes -= pageSize;
/* 205:    */       
/* 206:336 */       int subpageIdx = subpageIdx(id);
/* 207:337 */       PoolSubpage<T> subpage = subpages[subpageIdx];
/* 208:338 */       if (subpage == null)
/* 209:    */       {
/* 210:339 */         subpage = new PoolSubpage(head, this, id, runOffset(id), pageSize, normCapacity);
/* 211:340 */         subpages[subpageIdx] = subpage;
/* 212:    */       }
/* 213:    */       else
/* 214:    */       {
/* 215:342 */         subpage.init(head, normCapacity);
/* 216:    */       }
/* 217:344 */       return subpage.allocate();
/* 218:    */     }
/* 219:    */   }
/* 220:    */   
/* 221:    */   void free(long handle)
/* 222:    */   {
/* 223:357 */     int memoryMapIdx = memoryMapIdx(handle);
/* 224:358 */     int bitmapIdx = bitmapIdx(handle);
/* 225:360 */     if (bitmapIdx != 0)
/* 226:    */     {
/* 227:361 */       PoolSubpage<T> subpage = this.subpages[subpageIdx(memoryMapIdx)];
/* 228:362 */       assert ((subpage != null) && (subpage.doNotDestroy));
/* 229:    */       
/* 230:    */ 
/* 231:    */ 
/* 232:366 */       PoolSubpage<T> head = this.arena.findSubpagePoolHead(subpage.elemSize);
/* 233:367 */       synchronized (head)
/* 234:    */       {
/* 235:368 */         if (subpage.free(head, bitmapIdx & 0x3FFFFFFF)) {
/* 236:369 */           return;
/* 237:    */         }
/* 238:    */       }
/* 239:    */     }
/* 240:373 */     this.freeBytes += runLength(memoryMapIdx);
/* 241:374 */     setValue(memoryMapIdx, depth(memoryMapIdx));
/* 242:375 */     updateParentsFree(memoryMapIdx);
/* 243:    */   }
/* 244:    */   
/* 245:    */   void initBuf(PooledByteBuf<T> buf, long handle, int reqCapacity)
/* 246:    */   {
/* 247:379 */     int memoryMapIdx = memoryMapIdx(handle);
/* 248:380 */     int bitmapIdx = bitmapIdx(handle);
/* 249:381 */     if (bitmapIdx == 0)
/* 250:    */     {
/* 251:382 */       byte val = value(memoryMapIdx);
/* 252:383 */       assert (val == this.unusable) : String.valueOf(val);
/* 253:384 */       buf.init(this, handle, runOffset(memoryMapIdx) + this.offset, reqCapacity, runLength(memoryMapIdx), this.arena.parent
/* 254:385 */         .threadCache());
/* 255:    */     }
/* 256:    */     else
/* 257:    */     {
/* 258:387 */       initBufWithSubpage(buf, handle, bitmapIdx, reqCapacity);
/* 259:    */     }
/* 260:    */   }
/* 261:    */   
/* 262:    */   void initBufWithSubpage(PooledByteBuf<T> buf, long handle, int reqCapacity)
/* 263:    */   {
/* 264:392 */     initBufWithSubpage(buf, handle, bitmapIdx(handle), reqCapacity);
/* 265:    */   }
/* 266:    */   
/* 267:    */   private void initBufWithSubpage(PooledByteBuf<T> buf, long handle, int bitmapIdx, int reqCapacity)
/* 268:    */   {
/* 269:396 */     assert (bitmapIdx != 0);
/* 270:    */     
/* 271:398 */     int memoryMapIdx = memoryMapIdx(handle);
/* 272:    */     
/* 273:400 */     PoolSubpage<T> subpage = this.subpages[subpageIdx(memoryMapIdx)];
/* 274:401 */     assert (subpage.doNotDestroy);
/* 275:402 */     assert (reqCapacity <= subpage.elemSize);
/* 276:    */     
/* 277:404 */     buf.init(this, handle, 
/* 278:    */     
/* 279:406 */       runOffset(memoryMapIdx) + (bitmapIdx & 0x3FFFFFFF) * subpage.elemSize + this.offset, reqCapacity, subpage.elemSize, this.arena.parent
/* 280:407 */       .threadCache());
/* 281:    */   }
/* 282:    */   
/* 283:    */   private byte value(int id)
/* 284:    */   {
/* 285:411 */     return this.memoryMap[id];
/* 286:    */   }
/* 287:    */   
/* 288:    */   private void setValue(int id, byte val)
/* 289:    */   {
/* 290:415 */     this.memoryMap[id] = val;
/* 291:    */   }
/* 292:    */   
/* 293:    */   private byte depth(int id)
/* 294:    */   {
/* 295:419 */     return this.depthMap[id];
/* 296:    */   }
/* 297:    */   
/* 298:    */   private static int log2(int val)
/* 299:    */   {
/* 300:424 */     return 31 - Integer.numberOfLeadingZeros(val);
/* 301:    */   }
/* 302:    */   
/* 303:    */   private int runLength(int id)
/* 304:    */   {
/* 305:429 */     return 1 << this.log2ChunkSize - depth(id);
/* 306:    */   }
/* 307:    */   
/* 308:    */   private int runOffset(int id)
/* 309:    */   {
/* 310:434 */     int shift = id ^ 1 << depth(id);
/* 311:435 */     return shift * runLength(id);
/* 312:    */   }
/* 313:    */   
/* 314:    */   private int subpageIdx(int memoryMapIdx)
/* 315:    */   {
/* 316:439 */     return memoryMapIdx ^ this.maxSubpageAllocs;
/* 317:    */   }
/* 318:    */   
/* 319:    */   private static int memoryMapIdx(long handle)
/* 320:    */   {
/* 321:443 */     return (int)handle;
/* 322:    */   }
/* 323:    */   
/* 324:    */   private static int bitmapIdx(long handle)
/* 325:    */   {
/* 326:447 */     return (int)(handle >>> 32);
/* 327:    */   }
/* 328:    */   
/* 329:    */   public int chunkSize()
/* 330:    */   {
/* 331:452 */     return this.chunkSize;
/* 332:    */   }
/* 333:    */   
/* 334:    */   public int freeBytes()
/* 335:    */   {
/* 336:457 */     synchronized (this.arena)
/* 337:    */     {
/* 338:458 */       return this.freeBytes;
/* 339:    */     }
/* 340:    */   }
/* 341:    */   
/* 342:    */   public String toString()
/* 343:    */   {
/* 344:    */     int freeBytes;
/* 345:465 */     synchronized (this.arena)
/* 346:    */     {
/* 347:466 */       freeBytes = this.freeBytes;
/* 348:    */     }
/* 349:    */     int freeBytes;
/* 350:469 */     return 
/* 351:    */     
/* 352:    */ 
/* 353:    */ 
/* 354:    */ 
/* 355:    */ 
/* 356:    */ 
/* 357:    */ 
/* 358:    */ 
/* 359:478 */       "Chunk(" + Integer.toHexString(System.identityHashCode(this)) + ": " + usage(freeBytes) + "%, " + (this.chunkSize - freeBytes) + '/' + this.chunkSize + ')';
/* 360:    */   }
/* 361:    */   
/* 362:    */   void destroy()
/* 363:    */   {
/* 364:483 */     this.arena.destroyChunk(this);
/* 365:    */   }
/* 366:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.PoolChunk
 * JD-Core Version:    0.7.0.1
 */