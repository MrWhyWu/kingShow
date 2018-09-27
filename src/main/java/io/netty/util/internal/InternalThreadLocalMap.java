/*   1:    */ package io.netty.util.internal;
/*   2:    */ 
/*   3:    */ import io.netty.util.concurrent.FastThreadLocalThread;
/*   4:    */ import io.netty.util.internal.logging.InternalLogger;
/*   5:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   6:    */ import java.nio.charset.Charset;
/*   7:    */ import java.nio.charset.CharsetDecoder;
/*   8:    */ import java.nio.charset.CharsetEncoder;
/*   9:    */ import java.util.ArrayList;
/*  10:    */ import java.util.Arrays;
/*  11:    */ import java.util.IdentityHashMap;
/*  12:    */ import java.util.Map;
/*  13:    */ import java.util.WeakHashMap;
/*  14:    */ import java.util.concurrent.atomic.AtomicInteger;
/*  15:    */ 
/*  16:    */ public final class InternalThreadLocalMap
/*  17:    */   extends UnpaddedInternalThreadLocalMap
/*  18:    */ {
/*  19: 40 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(InternalThreadLocalMap.class);
/*  20:    */   private static final int DEFAULT_ARRAY_LIST_INITIAL_CAPACITY = 8;
/*  21:    */   private static final int STRING_BUILDER_INITIAL_SIZE;
/*  22:    */   private static final int STRING_BUILDER_MAX_SIZE;
/*  23: 46 */   public static final Object UNSET = new Object();
/*  24:    */   public long rp1;
/*  25:    */   public long rp2;
/*  26:    */   public long rp3;
/*  27:    */   public long rp4;
/*  28:    */   public long rp5;
/*  29:    */   public long rp6;
/*  30:    */   public long rp7;
/*  31:    */   public long rp8;
/*  32:    */   public long rp9;
/*  33:    */   
/*  34:    */   static
/*  35:    */   {
/*  36: 50 */     STRING_BUILDER_INITIAL_SIZE = SystemPropertyUtil.getInt("io.netty.threadLocalMap.stringBuilder.initialSize", 1024);
/*  37: 51 */     logger.debug("-Dio.netty.threadLocalMap.stringBuilder.initialSize: {}", Integer.valueOf(STRING_BUILDER_INITIAL_SIZE));
/*  38:    */     
/*  39: 53 */     STRING_BUILDER_MAX_SIZE = SystemPropertyUtil.getInt("io.netty.threadLocalMap.stringBuilder.maxSize", 4096);
/*  40: 54 */     logger.debug("-Dio.netty.threadLocalMap.stringBuilder.maxSize: {}", Integer.valueOf(STRING_BUILDER_MAX_SIZE));
/*  41:    */   }
/*  42:    */   
/*  43:    */   public static InternalThreadLocalMap getIfSet()
/*  44:    */   {
/*  45: 58 */     Thread thread = Thread.currentThread();
/*  46: 59 */     if ((thread instanceof FastThreadLocalThread)) {
/*  47: 60 */       return ((FastThreadLocalThread)thread).threadLocalMap();
/*  48:    */     }
/*  49: 62 */     return (InternalThreadLocalMap)slowThreadLocalMap.get();
/*  50:    */   }
/*  51:    */   
/*  52:    */   public static InternalThreadLocalMap get()
/*  53:    */   {
/*  54: 66 */     Thread thread = Thread.currentThread();
/*  55: 67 */     if ((thread instanceof FastThreadLocalThread)) {
/*  56: 68 */       return fastGet((FastThreadLocalThread)thread);
/*  57:    */     }
/*  58: 70 */     return slowGet();
/*  59:    */   }
/*  60:    */   
/*  61:    */   private static InternalThreadLocalMap fastGet(FastThreadLocalThread thread)
/*  62:    */   {
/*  63: 75 */     InternalThreadLocalMap threadLocalMap = thread.threadLocalMap();
/*  64: 76 */     if (threadLocalMap == null) {
/*  65: 77 */       thread.setThreadLocalMap(threadLocalMap = new InternalThreadLocalMap());
/*  66:    */     }
/*  67: 79 */     return threadLocalMap;
/*  68:    */   }
/*  69:    */   
/*  70:    */   private static InternalThreadLocalMap slowGet()
/*  71:    */   {
/*  72: 83 */     ThreadLocal<InternalThreadLocalMap> slowThreadLocalMap = UnpaddedInternalThreadLocalMap.slowThreadLocalMap;
/*  73: 84 */     InternalThreadLocalMap ret = (InternalThreadLocalMap)slowThreadLocalMap.get();
/*  74: 85 */     if (ret == null)
/*  75:    */     {
/*  76: 86 */       ret = new InternalThreadLocalMap();
/*  77: 87 */       slowThreadLocalMap.set(ret);
/*  78:    */     }
/*  79: 89 */     return ret;
/*  80:    */   }
/*  81:    */   
/*  82:    */   public static void remove()
/*  83:    */   {
/*  84: 93 */     Thread thread = Thread.currentThread();
/*  85: 94 */     if ((thread instanceof FastThreadLocalThread)) {
/*  86: 95 */       ((FastThreadLocalThread)thread).setThreadLocalMap(null);
/*  87:    */     } else {
/*  88: 97 */       slowThreadLocalMap.remove();
/*  89:    */     }
/*  90:    */   }
/*  91:    */   
/*  92:    */   public static void destroy()
/*  93:    */   {
/*  94:102 */     slowThreadLocalMap.remove();
/*  95:    */   }
/*  96:    */   
/*  97:    */   public static int nextVariableIndex()
/*  98:    */   {
/*  99:106 */     int index = nextIndex.getAndIncrement();
/* 100:107 */     if (index < 0)
/* 101:    */     {
/* 102:108 */       nextIndex.decrementAndGet();
/* 103:109 */       throw new IllegalStateException("too many thread-local indexed variables");
/* 104:    */     }
/* 105:111 */     return index;
/* 106:    */   }
/* 107:    */   
/* 108:    */   public static int lastVariableIndex()
/* 109:    */   {
/* 110:115 */     return nextIndex.get() - 1;
/* 111:    */   }
/* 112:    */   
/* 113:    */   private InternalThreadLocalMap()
/* 114:    */   {
/* 115:123 */     super(newIndexedVariableTable());
/* 116:    */   }
/* 117:    */   
/* 118:    */   private static Object[] newIndexedVariableTable()
/* 119:    */   {
/* 120:127 */     Object[] array = new Object[32];
/* 121:128 */     Arrays.fill(array, UNSET);
/* 122:129 */     return array;
/* 123:    */   }
/* 124:    */   
/* 125:    */   public int size()
/* 126:    */   {
/* 127:133 */     int count = 0;
/* 128:135 */     if (this.futureListenerStackDepth != 0) {
/* 129:136 */       count++;
/* 130:    */     }
/* 131:138 */     if (this.localChannelReaderStackDepth != 0) {
/* 132:139 */       count++;
/* 133:    */     }
/* 134:141 */     if (this.handlerSharableCache != null) {
/* 135:142 */       count++;
/* 136:    */     }
/* 137:144 */     if (this.counterHashCode != null) {
/* 138:145 */       count++;
/* 139:    */     }
/* 140:147 */     if (this.random != null) {
/* 141:148 */       count++;
/* 142:    */     }
/* 143:150 */     if (this.typeParameterMatcherGetCache != null) {
/* 144:151 */       count++;
/* 145:    */     }
/* 146:153 */     if (this.typeParameterMatcherFindCache != null) {
/* 147:154 */       count++;
/* 148:    */     }
/* 149:156 */     if (this.stringBuilder != null) {
/* 150:157 */       count++;
/* 151:    */     }
/* 152:159 */     if (this.charsetEncoderCache != null) {
/* 153:160 */       count++;
/* 154:    */     }
/* 155:162 */     if (this.charsetDecoderCache != null) {
/* 156:163 */       count++;
/* 157:    */     }
/* 158:165 */     if (this.arrayList != null) {
/* 159:166 */       count++;
/* 160:    */     }
/* 161:169 */     for (Object o : this.indexedVariables) {
/* 162:170 */       if (o != UNSET) {
/* 163:171 */         count++;
/* 164:    */       }
/* 165:    */     }
/* 166:177 */     return count - 1;
/* 167:    */   }
/* 168:    */   
/* 169:    */   public StringBuilder stringBuilder()
/* 170:    */   {
/* 171:181 */     StringBuilder sb = this.stringBuilder;
/* 172:182 */     if (sb == null) {
/* 173:183 */       return this.stringBuilder = new StringBuilder(STRING_BUILDER_INITIAL_SIZE);
/* 174:    */     }
/* 175:185 */     if (sb.capacity() > STRING_BUILDER_MAX_SIZE)
/* 176:    */     {
/* 177:186 */       sb.setLength(STRING_BUILDER_INITIAL_SIZE);
/* 178:187 */       sb.trimToSize();
/* 179:    */     }
/* 180:189 */     sb.setLength(0);
/* 181:190 */     return sb;
/* 182:    */   }
/* 183:    */   
/* 184:    */   public Map<Charset, CharsetEncoder> charsetEncoderCache()
/* 185:    */   {
/* 186:194 */     Map<Charset, CharsetEncoder> cache = this.charsetEncoderCache;
/* 187:195 */     if (cache == null) {
/* 188:196 */       this.charsetEncoderCache = (cache = new IdentityHashMap());
/* 189:    */     }
/* 190:198 */     return cache;
/* 191:    */   }
/* 192:    */   
/* 193:    */   public Map<Charset, CharsetDecoder> charsetDecoderCache()
/* 194:    */   {
/* 195:202 */     Map<Charset, CharsetDecoder> cache = this.charsetDecoderCache;
/* 196:203 */     if (cache == null) {
/* 197:204 */       this.charsetDecoderCache = (cache = new IdentityHashMap());
/* 198:    */     }
/* 199:206 */     return cache;
/* 200:    */   }
/* 201:    */   
/* 202:    */   public <E> ArrayList<E> arrayList()
/* 203:    */   {
/* 204:210 */     return arrayList(8);
/* 205:    */   }
/* 206:    */   
/* 207:    */   public <E> ArrayList<E> arrayList(int minCapacity)
/* 208:    */   {
/* 209:215 */     ArrayList<E> list = this.arrayList;
/* 210:216 */     if (list == null)
/* 211:    */     {
/* 212:217 */       this.arrayList = new ArrayList(minCapacity);
/* 213:218 */       return this.arrayList;
/* 214:    */     }
/* 215:220 */     list.clear();
/* 216:221 */     list.ensureCapacity(minCapacity);
/* 217:222 */     return list;
/* 218:    */   }
/* 219:    */   
/* 220:    */   public int futureListenerStackDepth()
/* 221:    */   {
/* 222:226 */     return this.futureListenerStackDepth;
/* 223:    */   }
/* 224:    */   
/* 225:    */   public void setFutureListenerStackDepth(int futureListenerStackDepth)
/* 226:    */   {
/* 227:230 */     this.futureListenerStackDepth = futureListenerStackDepth;
/* 228:    */   }
/* 229:    */   
/* 230:    */   public ThreadLocalRandom random()
/* 231:    */   {
/* 232:234 */     ThreadLocalRandom r = this.random;
/* 233:235 */     if (r == null) {
/* 234:236 */       this.random = (r = new ThreadLocalRandom());
/* 235:    */     }
/* 236:238 */     return r;
/* 237:    */   }
/* 238:    */   
/* 239:    */   public Map<Class<?>, TypeParameterMatcher> typeParameterMatcherGetCache()
/* 240:    */   {
/* 241:242 */     Map<Class<?>, TypeParameterMatcher> cache = this.typeParameterMatcherGetCache;
/* 242:243 */     if (cache == null) {
/* 243:244 */       this.typeParameterMatcherGetCache = (cache = new IdentityHashMap());
/* 244:    */     }
/* 245:246 */     return cache;
/* 246:    */   }
/* 247:    */   
/* 248:    */   public Map<Class<?>, Map<String, TypeParameterMatcher>> typeParameterMatcherFindCache()
/* 249:    */   {
/* 250:250 */     Map<Class<?>, Map<String, TypeParameterMatcher>> cache = this.typeParameterMatcherFindCache;
/* 251:251 */     if (cache == null) {
/* 252:252 */       this.typeParameterMatcherFindCache = (cache = new IdentityHashMap());
/* 253:    */     }
/* 254:254 */     return cache;
/* 255:    */   }
/* 256:    */   
/* 257:    */   public IntegerHolder counterHashCode()
/* 258:    */   {
/* 259:258 */     return this.counterHashCode;
/* 260:    */   }
/* 261:    */   
/* 262:    */   public void setCounterHashCode(IntegerHolder counterHashCode)
/* 263:    */   {
/* 264:262 */     this.counterHashCode = counterHashCode;
/* 265:    */   }
/* 266:    */   
/* 267:    */   public Map<Class<?>, Boolean> handlerSharableCache()
/* 268:    */   {
/* 269:266 */     Map<Class<?>, Boolean> cache = this.handlerSharableCache;
/* 270:267 */     if (cache == null) {
/* 271:269 */       this.handlerSharableCache = (cache = new WeakHashMap(4));
/* 272:    */     }
/* 273:271 */     return cache;
/* 274:    */   }
/* 275:    */   
/* 276:    */   public int localChannelReaderStackDepth()
/* 277:    */   {
/* 278:275 */     return this.localChannelReaderStackDepth;
/* 279:    */   }
/* 280:    */   
/* 281:    */   public void setLocalChannelReaderStackDepth(int localChannelReaderStackDepth)
/* 282:    */   {
/* 283:279 */     this.localChannelReaderStackDepth = localChannelReaderStackDepth;
/* 284:    */   }
/* 285:    */   
/* 286:    */   public Object indexedVariable(int index)
/* 287:    */   {
/* 288:283 */     Object[] lookup = this.indexedVariables;
/* 289:284 */     return index < lookup.length ? lookup[index] : UNSET;
/* 290:    */   }
/* 291:    */   
/* 292:    */   public boolean setIndexedVariable(int index, Object value)
/* 293:    */   {
/* 294:291 */     Object[] lookup = this.indexedVariables;
/* 295:292 */     if (index < lookup.length)
/* 296:    */     {
/* 297:293 */       Object oldValue = lookup[index];
/* 298:294 */       lookup[index] = value;
/* 299:295 */       return oldValue == UNSET;
/* 300:    */     }
/* 301:297 */     expandIndexedVariableTableAndSet(index, value);
/* 302:298 */     return true;
/* 303:    */   }
/* 304:    */   
/* 305:    */   private void expandIndexedVariableTableAndSet(int index, Object value)
/* 306:    */   {
/* 307:303 */     Object[] oldArray = this.indexedVariables;
/* 308:304 */     int oldCapacity = oldArray.length;
/* 309:305 */     int newCapacity = index;
/* 310:306 */     newCapacity |= newCapacity >>> 1;
/* 311:307 */     newCapacity |= newCapacity >>> 2;
/* 312:308 */     newCapacity |= newCapacity >>> 4;
/* 313:309 */     newCapacity |= newCapacity >>> 8;
/* 314:310 */     newCapacity |= newCapacity >>> 16;
/* 315:311 */     newCapacity++;
/* 316:    */     
/* 317:313 */     Object[] newArray = Arrays.copyOf(oldArray, newCapacity);
/* 318:314 */     Arrays.fill(newArray, oldCapacity, newArray.length, UNSET);
/* 319:315 */     newArray[index] = value;
/* 320:316 */     this.indexedVariables = newArray;
/* 321:    */   }
/* 322:    */   
/* 323:    */   public Object removeIndexedVariable(int index)
/* 324:    */   {
/* 325:320 */     Object[] lookup = this.indexedVariables;
/* 326:321 */     if (index < lookup.length)
/* 327:    */     {
/* 328:322 */       Object v = lookup[index];
/* 329:323 */       lookup[index] = UNSET;
/* 330:324 */       return v;
/* 331:    */     }
/* 332:326 */     return UNSET;
/* 333:    */   }
/* 334:    */   
/* 335:    */   public boolean isIndexedVariableSet(int index)
/* 336:    */   {
/* 337:331 */     Object[] lookup = this.indexedVariables;
/* 338:332 */     return (index < lookup.length) && (lookup[index] != UNSET);
/* 339:    */   }
/* 340:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.InternalThreadLocalMap
 * JD-Core Version:    0.7.0.1
 */