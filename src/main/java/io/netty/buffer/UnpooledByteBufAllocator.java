/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.LongCounter;
/*   4:    */ import io.netty.util.internal.PlatformDependent;
/*   5:    */ import io.netty.util.internal.StringUtil;
/*   6:    */ import java.nio.ByteBuffer;
/*   7:    */ 
/*   8:    */ public final class UnpooledByteBufAllocator
/*   9:    */   extends AbstractByteBufAllocator
/*  10:    */   implements ByteBufAllocatorMetricProvider
/*  11:    */ {
/*  12: 29 */   private final UnpooledByteBufAllocatorMetric metric = new UnpooledByteBufAllocatorMetric(null);
/*  13:    */   private final boolean disableLeakDetector;
/*  14:    */   private final boolean noCleaner;
/*  15: 36 */   public static final UnpooledByteBufAllocator DEFAULT = new UnpooledByteBufAllocator(
/*  16: 37 */     PlatformDependent.directBufferPreferred());
/*  17:    */   
/*  18:    */   public UnpooledByteBufAllocator(boolean preferDirect)
/*  19:    */   {
/*  20: 46 */     this(preferDirect, false);
/*  21:    */   }
/*  22:    */   
/*  23:    */   public UnpooledByteBufAllocator(boolean preferDirect, boolean disableLeakDetector)
/*  24:    */   {
/*  25: 59 */     this(preferDirect, disableLeakDetector, PlatformDependent.useDirectBufferNoCleaner());
/*  26:    */   }
/*  27:    */   
/*  28:    */   public UnpooledByteBufAllocator(boolean preferDirect, boolean disableLeakDetector, boolean tryNoCleaner)
/*  29:    */   {
/*  30: 74 */     super(preferDirect);
/*  31: 75 */     this.disableLeakDetector = disableLeakDetector;
/*  32:    */     
/*  33: 77 */     this.noCleaner = ((tryNoCleaner) && (PlatformDependent.hasUnsafe()) && (PlatformDependent.hasDirectBufferNoCleanerConstructor()));
/*  34:    */   }
/*  35:    */   
/*  36:    */   protected ByteBuf newHeapBuffer(int initialCapacity, int maxCapacity)
/*  37:    */   {
/*  38: 82 */     return PlatformDependent.hasUnsafe() ? new InstrumentedUnpooledUnsafeHeapByteBuf(this, initialCapacity, maxCapacity) : new InstrumentedUnpooledHeapByteBuf(this, initialCapacity, maxCapacity);
/*  39:    */   }
/*  40:    */   
/*  41:    */   protected ByteBuf newDirectBuffer(int initialCapacity, int maxCapacity)
/*  42:    */   {
/*  43:    */     ByteBuf buf;
/*  44:    */     ByteBuf buf;
/*  45: 90 */     if (PlatformDependent.hasUnsafe()) {
/*  46: 91 */       buf = this.noCleaner ? new InstrumentedUnpooledUnsafeNoCleanerDirectByteBuf(this, initialCapacity, maxCapacity) : new InstrumentedUnpooledUnsafeDirectByteBuf(this, initialCapacity, maxCapacity);
/*  47:    */     } else {
/*  48: 94 */       buf = new InstrumentedUnpooledDirectByteBuf(this, initialCapacity, maxCapacity);
/*  49:    */     }
/*  50: 96 */     return this.disableLeakDetector ? buf : toLeakAwareBuffer(buf);
/*  51:    */   }
/*  52:    */   
/*  53:    */   public CompositeByteBuf compositeHeapBuffer(int maxNumComponents)
/*  54:    */   {
/*  55:101 */     CompositeByteBuf buf = new CompositeByteBuf(this, false, maxNumComponents);
/*  56:102 */     return this.disableLeakDetector ? buf : toLeakAwareBuffer(buf);
/*  57:    */   }
/*  58:    */   
/*  59:    */   public CompositeByteBuf compositeDirectBuffer(int maxNumComponents)
/*  60:    */   {
/*  61:107 */     CompositeByteBuf buf = new CompositeByteBuf(this, true, maxNumComponents);
/*  62:108 */     return this.disableLeakDetector ? buf : toLeakAwareBuffer(buf);
/*  63:    */   }
/*  64:    */   
/*  65:    */   public boolean isDirectBufferPooled()
/*  66:    */   {
/*  67:113 */     return false;
/*  68:    */   }
/*  69:    */   
/*  70:    */   public ByteBufAllocatorMetric metric()
/*  71:    */   {
/*  72:118 */     return this.metric;
/*  73:    */   }
/*  74:    */   
/*  75:    */   void incrementDirect(int amount)
/*  76:    */   {
/*  77:122 */     this.metric.directCounter.add(amount);
/*  78:    */   }
/*  79:    */   
/*  80:    */   void decrementDirect(int amount)
/*  81:    */   {
/*  82:126 */     this.metric.directCounter.add(-amount);
/*  83:    */   }
/*  84:    */   
/*  85:    */   void incrementHeap(int amount)
/*  86:    */   {
/*  87:130 */     this.metric.heapCounter.add(amount);
/*  88:    */   }
/*  89:    */   
/*  90:    */   void decrementHeap(int amount)
/*  91:    */   {
/*  92:134 */     this.metric.heapCounter.add(-amount);
/*  93:    */   }
/*  94:    */   
/*  95:    */   private static final class InstrumentedUnpooledUnsafeHeapByteBuf
/*  96:    */     extends UnpooledUnsafeHeapByteBuf
/*  97:    */   {
/*  98:    */     InstrumentedUnpooledUnsafeHeapByteBuf(UnpooledByteBufAllocator alloc, int initialCapacity, int maxCapacity)
/*  99:    */     {
/* 100:139 */       super(initialCapacity, maxCapacity);
/* 101:    */     }
/* 102:    */     
/* 103:    */     byte[] allocateArray(int initialCapacity)
/* 104:    */     {
/* 105:144 */       byte[] bytes = super.allocateArray(initialCapacity);
/* 106:145 */       ((UnpooledByteBufAllocator)alloc()).incrementHeap(bytes.length);
/* 107:146 */       return bytes;
/* 108:    */     }
/* 109:    */     
/* 110:    */     void freeArray(byte[] array)
/* 111:    */     {
/* 112:151 */       int length = array.length;
/* 113:152 */       super.freeArray(array);
/* 114:153 */       ((UnpooledByteBufAllocator)alloc()).decrementHeap(length);
/* 115:    */     }
/* 116:    */   }
/* 117:    */   
/* 118:    */   private static final class InstrumentedUnpooledHeapByteBuf
/* 119:    */     extends UnpooledHeapByteBuf
/* 120:    */   {
/* 121:    */     InstrumentedUnpooledHeapByteBuf(UnpooledByteBufAllocator alloc, int initialCapacity, int maxCapacity)
/* 122:    */     {
/* 123:159 */       super(initialCapacity, maxCapacity);
/* 124:    */     }
/* 125:    */     
/* 126:    */     byte[] allocateArray(int initialCapacity)
/* 127:    */     {
/* 128:164 */       byte[] bytes = super.allocateArray(initialCapacity);
/* 129:165 */       ((UnpooledByteBufAllocator)alloc()).incrementHeap(bytes.length);
/* 130:166 */       return bytes;
/* 131:    */     }
/* 132:    */     
/* 133:    */     void freeArray(byte[] array)
/* 134:    */     {
/* 135:171 */       int length = array.length;
/* 136:172 */       super.freeArray(array);
/* 137:173 */       ((UnpooledByteBufAllocator)alloc()).decrementHeap(length);
/* 138:    */     }
/* 139:    */   }
/* 140:    */   
/* 141:    */   private static final class InstrumentedUnpooledUnsafeNoCleanerDirectByteBuf
/* 142:    */     extends UnpooledUnsafeNoCleanerDirectByteBuf
/* 143:    */   {
/* 144:    */     InstrumentedUnpooledUnsafeNoCleanerDirectByteBuf(UnpooledByteBufAllocator alloc, int initialCapacity, int maxCapacity)
/* 145:    */     {
/* 146:181 */       super(initialCapacity, maxCapacity);
/* 147:    */     }
/* 148:    */     
/* 149:    */     protected ByteBuffer allocateDirect(int initialCapacity)
/* 150:    */     {
/* 151:186 */       ByteBuffer buffer = super.allocateDirect(initialCapacity);
/* 152:187 */       ((UnpooledByteBufAllocator)alloc()).incrementDirect(buffer.capacity());
/* 153:188 */       return buffer;
/* 154:    */     }
/* 155:    */     
/* 156:    */     ByteBuffer reallocateDirect(ByteBuffer oldBuffer, int initialCapacity)
/* 157:    */     {
/* 158:193 */       int capacity = oldBuffer.capacity();
/* 159:194 */       ByteBuffer buffer = super.reallocateDirect(oldBuffer, initialCapacity);
/* 160:195 */       ((UnpooledByteBufAllocator)alloc()).incrementDirect(buffer.capacity() - capacity);
/* 161:196 */       return buffer;
/* 162:    */     }
/* 163:    */     
/* 164:    */     protected void freeDirect(ByteBuffer buffer)
/* 165:    */     {
/* 166:201 */       int capacity = buffer.capacity();
/* 167:202 */       super.freeDirect(buffer);
/* 168:203 */       ((UnpooledByteBufAllocator)alloc()).decrementDirect(capacity);
/* 169:    */     }
/* 170:    */   }
/* 171:    */   
/* 172:    */   private static final class InstrumentedUnpooledUnsafeDirectByteBuf
/* 173:    */     extends UnpooledUnsafeDirectByteBuf
/* 174:    */   {
/* 175:    */     InstrumentedUnpooledUnsafeDirectByteBuf(UnpooledByteBufAllocator alloc, int initialCapacity, int maxCapacity)
/* 176:    */     {
/* 177:210 */       super(initialCapacity, maxCapacity);
/* 178:    */     }
/* 179:    */     
/* 180:    */     protected ByteBuffer allocateDirect(int initialCapacity)
/* 181:    */     {
/* 182:215 */       ByteBuffer buffer = super.allocateDirect(initialCapacity);
/* 183:216 */       ((UnpooledByteBufAllocator)alloc()).incrementDirect(buffer.capacity());
/* 184:217 */       return buffer;
/* 185:    */     }
/* 186:    */     
/* 187:    */     protected void freeDirect(ByteBuffer buffer)
/* 188:    */     {
/* 189:222 */       int capacity = buffer.capacity();
/* 190:223 */       super.freeDirect(buffer);
/* 191:224 */       ((UnpooledByteBufAllocator)alloc()).decrementDirect(capacity);
/* 192:    */     }
/* 193:    */   }
/* 194:    */   
/* 195:    */   private static final class InstrumentedUnpooledDirectByteBuf
/* 196:    */     extends UnpooledDirectByteBuf
/* 197:    */   {
/* 198:    */     InstrumentedUnpooledDirectByteBuf(UnpooledByteBufAllocator alloc, int initialCapacity, int maxCapacity)
/* 199:    */     {
/* 200:231 */       super(initialCapacity, maxCapacity);
/* 201:    */     }
/* 202:    */     
/* 203:    */     protected ByteBuffer allocateDirect(int initialCapacity)
/* 204:    */     {
/* 205:236 */       ByteBuffer buffer = super.allocateDirect(initialCapacity);
/* 206:237 */       ((UnpooledByteBufAllocator)alloc()).incrementDirect(buffer.capacity());
/* 207:238 */       return buffer;
/* 208:    */     }
/* 209:    */     
/* 210:    */     protected void freeDirect(ByteBuffer buffer)
/* 211:    */     {
/* 212:243 */       int capacity = buffer.capacity();
/* 213:244 */       super.freeDirect(buffer);
/* 214:245 */       ((UnpooledByteBufAllocator)alloc()).decrementDirect(capacity);
/* 215:    */     }
/* 216:    */   }
/* 217:    */   
/* 218:    */   private static final class UnpooledByteBufAllocatorMetric
/* 219:    */     implements ByteBufAllocatorMetric
/* 220:    */   {
/* 221:250 */     final LongCounter directCounter = PlatformDependent.newLongCounter();
/* 222:251 */     final LongCounter heapCounter = PlatformDependent.newLongCounter();
/* 223:    */     
/* 224:    */     public long usedHeapMemory()
/* 225:    */     {
/* 226:255 */       return this.heapCounter.value();
/* 227:    */     }
/* 228:    */     
/* 229:    */     public long usedDirectMemory()
/* 230:    */     {
/* 231:260 */       return this.directCounter.value();
/* 232:    */     }
/* 233:    */     
/* 234:    */     public String toString()
/* 235:    */     {
/* 236:265 */       return 
/* 237:266 */         StringUtil.simpleClassName(this) + "(usedHeapMemory: " + usedHeapMemory() + "; usedDirectMemory: " + usedDirectMemory() + ')';
/* 238:    */     }
/* 239:    */   }
/* 240:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.UnpooledByteBufAllocator
 * JD-Core Version:    0.7.0.1
 */