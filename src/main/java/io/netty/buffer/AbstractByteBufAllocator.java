/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.ResourceLeakDetector;
/*   4:    */ import io.netty.util.ResourceLeakTracker;
/*   5:    */ import io.netty.util.internal.PlatformDependent;
/*   6:    */ import io.netty.util.internal.StringUtil;
/*   7:    */ 
/*   8:    */ public abstract class AbstractByteBufAllocator
/*   9:    */   implements ByteBufAllocator
/*  10:    */ {
/*  11:    */   static final int DEFAULT_INITIAL_CAPACITY = 256;
/*  12:    */   static final int DEFAULT_MAX_CAPACITY = 2147483647;
/*  13:    */   static final int DEFAULT_MAX_COMPONENTS = 16;
/*  14:    */   static final int CALCULATE_THRESHOLD = 4194304;
/*  15:    */   private final boolean directByDefault;
/*  16:    */   private final ByteBuf emptyBuf;
/*  17:    */   
/*  18:    */   static
/*  19:    */   {
/*  20: 34 */     ResourceLeakDetector.addExclusions(AbstractByteBufAllocator.class, new String[] { "toLeakAwareBuffer" });
/*  21:    */   }
/*  22:    */   
/*  23:    */   protected static ByteBuf toLeakAwareBuffer(ByteBuf buf)
/*  24:    */   {
/*  25: 39 */     switch (1.$SwitchMap$io$netty$util$ResourceLeakDetector$Level[ResourceLeakDetector.getLevel().ordinal()])
/*  26:    */     {
/*  27:    */     case 1: 
/*  28: 41 */       ResourceLeakTracker<ByteBuf> leak = AbstractByteBuf.leakDetector.track(buf);
/*  29: 42 */       if (leak != null) {
/*  30: 43 */         buf = new SimpleLeakAwareByteBuf(buf, leak);
/*  31:    */       }
/*  32:    */       break;
/*  33:    */     case 2: 
/*  34:    */     case 3: 
/*  35: 48 */       ResourceLeakTracker<ByteBuf> leak = AbstractByteBuf.leakDetector.track(buf);
/*  36: 49 */       if (leak != null) {
/*  37: 50 */         buf = new AdvancedLeakAwareByteBuf(buf, leak);
/*  38:    */       }
/*  39:    */       break;
/*  40:    */     }
/*  41: 56 */     return buf;
/*  42:    */   }
/*  43:    */   
/*  44:    */   protected static CompositeByteBuf toLeakAwareBuffer(CompositeByteBuf buf)
/*  45:    */   {
/*  46: 61 */     switch (1.$SwitchMap$io$netty$util$ResourceLeakDetector$Level[ResourceLeakDetector.getLevel().ordinal()])
/*  47:    */     {
/*  48:    */     case 1: 
/*  49: 63 */       ResourceLeakTracker<ByteBuf> leak = AbstractByteBuf.leakDetector.track(buf);
/*  50: 64 */       if (leak != null) {
/*  51: 65 */         buf = new SimpleLeakAwareCompositeByteBuf(buf, leak);
/*  52:    */       }
/*  53:    */       break;
/*  54:    */     case 2: 
/*  55:    */     case 3: 
/*  56: 70 */       ResourceLeakTracker<ByteBuf> leak = AbstractByteBuf.leakDetector.track(buf);
/*  57: 71 */       if (leak != null) {
/*  58: 72 */         buf = new AdvancedLeakAwareCompositeByteBuf(buf, leak);
/*  59:    */       }
/*  60:    */       break;
/*  61:    */     }
/*  62: 78 */     return buf;
/*  63:    */   }
/*  64:    */   
/*  65:    */   protected AbstractByteBufAllocator()
/*  66:    */   {
/*  67: 88 */     this(false);
/*  68:    */   }
/*  69:    */   
/*  70:    */   protected AbstractByteBufAllocator(boolean preferDirect)
/*  71:    */   {
/*  72: 98 */     this.directByDefault = ((preferDirect) && (PlatformDependent.hasUnsafe()));
/*  73: 99 */     this.emptyBuf = new EmptyByteBuf(this);
/*  74:    */   }
/*  75:    */   
/*  76:    */   public ByteBuf buffer()
/*  77:    */   {
/*  78:104 */     if (this.directByDefault) {
/*  79:105 */       return directBuffer();
/*  80:    */     }
/*  81:107 */     return heapBuffer();
/*  82:    */   }
/*  83:    */   
/*  84:    */   public ByteBuf buffer(int initialCapacity)
/*  85:    */   {
/*  86:112 */     if (this.directByDefault) {
/*  87:113 */       return directBuffer(initialCapacity);
/*  88:    */     }
/*  89:115 */     return heapBuffer(initialCapacity);
/*  90:    */   }
/*  91:    */   
/*  92:    */   public ByteBuf buffer(int initialCapacity, int maxCapacity)
/*  93:    */   {
/*  94:120 */     if (this.directByDefault) {
/*  95:121 */       return directBuffer(initialCapacity, maxCapacity);
/*  96:    */     }
/*  97:123 */     return heapBuffer(initialCapacity, maxCapacity);
/*  98:    */   }
/*  99:    */   
/* 100:    */   public ByteBuf ioBuffer()
/* 101:    */   {
/* 102:128 */     if (PlatformDependent.hasUnsafe()) {
/* 103:129 */       return directBuffer(256);
/* 104:    */     }
/* 105:131 */     return heapBuffer(256);
/* 106:    */   }
/* 107:    */   
/* 108:    */   public ByteBuf ioBuffer(int initialCapacity)
/* 109:    */   {
/* 110:136 */     if (PlatformDependent.hasUnsafe()) {
/* 111:137 */       return directBuffer(initialCapacity);
/* 112:    */     }
/* 113:139 */     return heapBuffer(initialCapacity);
/* 114:    */   }
/* 115:    */   
/* 116:    */   public ByteBuf ioBuffer(int initialCapacity, int maxCapacity)
/* 117:    */   {
/* 118:144 */     if (PlatformDependent.hasUnsafe()) {
/* 119:145 */       return directBuffer(initialCapacity, maxCapacity);
/* 120:    */     }
/* 121:147 */     return heapBuffer(initialCapacity, maxCapacity);
/* 122:    */   }
/* 123:    */   
/* 124:    */   public ByteBuf heapBuffer()
/* 125:    */   {
/* 126:152 */     return heapBuffer(256, 2147483647);
/* 127:    */   }
/* 128:    */   
/* 129:    */   public ByteBuf heapBuffer(int initialCapacity)
/* 130:    */   {
/* 131:157 */     return heapBuffer(initialCapacity, 2147483647);
/* 132:    */   }
/* 133:    */   
/* 134:    */   public ByteBuf heapBuffer(int initialCapacity, int maxCapacity)
/* 135:    */   {
/* 136:162 */     if ((initialCapacity == 0) && (maxCapacity == 0)) {
/* 137:163 */       return this.emptyBuf;
/* 138:    */     }
/* 139:165 */     validate(initialCapacity, maxCapacity);
/* 140:166 */     return newHeapBuffer(initialCapacity, maxCapacity);
/* 141:    */   }
/* 142:    */   
/* 143:    */   public ByteBuf directBuffer()
/* 144:    */   {
/* 145:171 */     return directBuffer(256, 2147483647);
/* 146:    */   }
/* 147:    */   
/* 148:    */   public ByteBuf directBuffer(int initialCapacity)
/* 149:    */   {
/* 150:176 */     return directBuffer(initialCapacity, 2147483647);
/* 151:    */   }
/* 152:    */   
/* 153:    */   public ByteBuf directBuffer(int initialCapacity, int maxCapacity)
/* 154:    */   {
/* 155:181 */     if ((initialCapacity == 0) && (maxCapacity == 0)) {
/* 156:182 */       return this.emptyBuf;
/* 157:    */     }
/* 158:184 */     validate(initialCapacity, maxCapacity);
/* 159:185 */     return newDirectBuffer(initialCapacity, maxCapacity);
/* 160:    */   }
/* 161:    */   
/* 162:    */   public CompositeByteBuf compositeBuffer()
/* 163:    */   {
/* 164:190 */     if (this.directByDefault) {
/* 165:191 */       return compositeDirectBuffer();
/* 166:    */     }
/* 167:193 */     return compositeHeapBuffer();
/* 168:    */   }
/* 169:    */   
/* 170:    */   public CompositeByteBuf compositeBuffer(int maxNumComponents)
/* 171:    */   {
/* 172:198 */     if (this.directByDefault) {
/* 173:199 */       return compositeDirectBuffer(maxNumComponents);
/* 174:    */     }
/* 175:201 */     return compositeHeapBuffer(maxNumComponents);
/* 176:    */   }
/* 177:    */   
/* 178:    */   public CompositeByteBuf compositeHeapBuffer()
/* 179:    */   {
/* 180:206 */     return compositeHeapBuffer(16);
/* 181:    */   }
/* 182:    */   
/* 183:    */   public CompositeByteBuf compositeHeapBuffer(int maxNumComponents)
/* 184:    */   {
/* 185:211 */     return toLeakAwareBuffer(new CompositeByteBuf(this, false, maxNumComponents));
/* 186:    */   }
/* 187:    */   
/* 188:    */   public CompositeByteBuf compositeDirectBuffer()
/* 189:    */   {
/* 190:216 */     return compositeDirectBuffer(16);
/* 191:    */   }
/* 192:    */   
/* 193:    */   public CompositeByteBuf compositeDirectBuffer(int maxNumComponents)
/* 194:    */   {
/* 195:221 */     return toLeakAwareBuffer(new CompositeByteBuf(this, true, maxNumComponents));
/* 196:    */   }
/* 197:    */   
/* 198:    */   private static void validate(int initialCapacity, int maxCapacity)
/* 199:    */   {
/* 200:225 */     if (initialCapacity < 0) {
/* 201:226 */       throw new IllegalArgumentException("initialCapacity: " + initialCapacity + " (expected: 0+)");
/* 202:    */     }
/* 203:228 */     if (initialCapacity > maxCapacity) {
/* 204:229 */       throw new IllegalArgumentException(String.format("initialCapacity: %d (expected: not greater than maxCapacity(%d)", new Object[] {
/* 205:    */       
/* 206:231 */         Integer.valueOf(initialCapacity), Integer.valueOf(maxCapacity) }));
/* 207:    */     }
/* 208:    */   }
/* 209:    */   
/* 210:    */   public String toString()
/* 211:    */   {
/* 212:247 */     return StringUtil.simpleClassName(this) + "(directByDefault: " + this.directByDefault + ')';
/* 213:    */   }
/* 214:    */   
/* 215:    */   public int calculateNewCapacity(int minNewCapacity, int maxCapacity)
/* 216:    */   {
/* 217:252 */     if (minNewCapacity < 0) {
/* 218:253 */       throw new IllegalArgumentException("minNewCapacity: " + minNewCapacity + " (expected: 0+)");
/* 219:    */     }
/* 220:255 */     if (minNewCapacity > maxCapacity) {
/* 221:256 */       throw new IllegalArgumentException(String.format("minNewCapacity: %d (expected: not greater than maxCapacity(%d)", new Object[] {
/* 222:    */       
/* 223:258 */         Integer.valueOf(minNewCapacity), Integer.valueOf(maxCapacity) }));
/* 224:    */     }
/* 225:260 */     int threshold = 4194304;
/* 226:262 */     if (minNewCapacity == 4194304) {
/* 227:263 */       return 4194304;
/* 228:    */     }
/* 229:267 */     if (minNewCapacity > 4194304)
/* 230:    */     {
/* 231:268 */       int newCapacity = minNewCapacity / 4194304 * 4194304;
/* 232:269 */       if (newCapacity > maxCapacity - 4194304) {
/* 233:270 */         newCapacity = maxCapacity;
/* 234:    */       } else {
/* 235:272 */         newCapacity += 4194304;
/* 236:    */       }
/* 237:274 */       return newCapacity;
/* 238:    */     }
/* 239:278 */     int newCapacity = 64;
/* 240:279 */     while (newCapacity < minNewCapacity) {
/* 241:280 */       newCapacity <<= 1;
/* 242:    */     }
/* 243:283 */     return Math.min(newCapacity, maxCapacity);
/* 244:    */   }
/* 245:    */   
/* 246:    */   protected abstract ByteBuf newHeapBuffer(int paramInt1, int paramInt2);
/* 247:    */   
/* 248:    */   protected abstract ByteBuf newDirectBuffer(int paramInt1, int paramInt2);
/* 249:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.AbstractByteBufAllocator
 * JD-Core Version:    0.7.0.1
 */