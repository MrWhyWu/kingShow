/*   1:    */ package io.netty.util.internal;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.logging.InternalLogger;
/*   4:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   5:    */ import java.security.SecureRandom;
/*   6:    */ import java.util.Random;
/*   7:    */ import java.util.concurrent.BlockingQueue;
/*   8:    */ import java.util.concurrent.LinkedBlockingQueue;
/*   9:    */ import java.util.concurrent.TimeUnit;
/*  10:    */ import java.util.concurrent.atomic.AtomicLong;
/*  11:    */ 
/*  12:    */ public final class ThreadLocalRandom
/*  13:    */   extends Random
/*  14:    */ {
/*  15: 63 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(ThreadLocalRandom.class);
/*  16: 65 */   private static final AtomicLong seedUniquifier = new AtomicLong();
/*  17: 75 */   private static volatile long initialSeedUniquifier = SystemPropertyUtil.getLong("io.netty.initialSeedUniquifier", 0L);
/*  18:    */   private static final Thread seedGeneratorThread;
/*  19:    */   private static final BlockingQueue<Long> seedQueue;
/*  20:    */   private static final long seedGeneratorStartTime;
/*  21:    */   private static volatile long seedGeneratorEndTime;
/*  22:    */   private static final long multiplier = 25214903917L;
/*  23:    */   private static final long addend = 11L;
/*  24:    */   private static final long mask = 281474976710655L;
/*  25:    */   private long rnd;
/*  26:    */   boolean initialized;
/*  27:    */   private long pad0;
/*  28:    */   private long pad1;
/*  29:    */   private long pad2;
/*  30:    */   private long pad3;
/*  31:    */   private long pad4;
/*  32:    */   private long pad5;
/*  33:    */   private long pad6;
/*  34:    */   private long pad7;
/*  35:    */   private static final long serialVersionUID = -5851777807851030925L;
/*  36:    */   
/*  37:    */   static
/*  38:    */   {
/*  39: 76 */     if (initialSeedUniquifier == 0L)
/*  40:    */     {
/*  41: 77 */       boolean secureRandom = SystemPropertyUtil.getBoolean("java.util.secureRandomSeed", false);
/*  42: 78 */       if (secureRandom)
/*  43:    */       {
/*  44: 79 */         seedQueue = new LinkedBlockingQueue();
/*  45: 80 */         seedGeneratorStartTime = System.nanoTime();
/*  46:    */         
/*  47:    */ 
/*  48:    */ 
/*  49: 84 */         seedGeneratorThread = new Thread("initialSeedUniquifierGenerator")
/*  50:    */         {
/*  51:    */           public void run()
/*  52:    */           {
/*  53: 87 */             SecureRandom random = new SecureRandom();
/*  54: 88 */             byte[] seed = random.generateSeed(8);
/*  55: 89 */             ThreadLocalRandom.access$002(System.nanoTime());
/*  56: 90 */             long s = (seed[0] & 0xFF) << 56 | (seed[1] & 0xFF) << 48 | (seed[2] & 0xFF) << 40 | (seed[3] & 0xFF) << 32 | (seed[4] & 0xFF) << 24 | (seed[5] & 0xFF) << 16 | (seed[6] & 0xFF) << 8 | seed[7] & 0xFF;
/*  57:    */             
/*  58:    */ 
/*  59:    */ 
/*  60:    */ 
/*  61:    */ 
/*  62:    */ 
/*  63:    */ 
/*  64: 98 */             ThreadLocalRandom.seedQueue.add(Long.valueOf(s));
/*  65:    */           }
/*  66:100 */         };
/*  67:101 */         seedGeneratorThread.setDaemon(true);
/*  68:102 */         seedGeneratorThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
/*  69:    */         {
/*  70:    */           public void uncaughtException(Thread t, Throwable e)
/*  71:    */           {
/*  72:105 */             ThreadLocalRandom.logger.debug("An exception has been raised by {}", t.getName(), e);
/*  73:    */           }
/*  74:107 */         });
/*  75:108 */         seedGeneratorThread.start();
/*  76:    */       }
/*  77:    */       else
/*  78:    */       {
/*  79:110 */         initialSeedUniquifier = mix64(System.currentTimeMillis()) ^ mix64(System.nanoTime());
/*  80:111 */         seedGeneratorThread = null;
/*  81:112 */         seedQueue = null;
/*  82:113 */         seedGeneratorStartTime = 0L;
/*  83:    */       }
/*  84:    */     }
/*  85:    */     else
/*  86:    */     {
/*  87:116 */       seedGeneratorThread = null;
/*  88:117 */       seedQueue = null;
/*  89:118 */       seedGeneratorStartTime = 0L;
/*  90:    */     }
/*  91:    */   }
/*  92:    */   
/*  93:    */   public static void setInitialSeedUniquifier(long initialSeedUniquifier)
/*  94:    */   {
/*  95:123 */     initialSeedUniquifier = initialSeedUniquifier;
/*  96:    */   }
/*  97:    */   
/*  98:    */   public static long getInitialSeedUniquifier()
/*  99:    */   {
/* 100:128 */     long initialSeedUniquifier = initialSeedUniquifier;
/* 101:129 */     if (initialSeedUniquifier != 0L) {
/* 102:130 */       return initialSeedUniquifier;
/* 103:    */     }
/* 104:133 */     synchronized (ThreadLocalRandom.class)
/* 105:    */     {
/* 106:134 */       initialSeedUniquifier = initialSeedUniquifier;
/* 107:135 */       if (initialSeedUniquifier != 0L) {
/* 108:136 */         return initialSeedUniquifier;
/* 109:    */       }
/* 110:140 */       long timeoutSeconds = 3L;
/* 111:141 */       long deadLine = seedGeneratorStartTime + TimeUnit.SECONDS.toNanos(3L);
/* 112:142 */       boolean interrupted = false;
/* 113:    */       for (;;)
/* 114:    */       {
/* 115:144 */         long waitTime = deadLine - System.nanoTime();
/* 116:    */         try
/* 117:    */         {
/* 118:    */           Long seed;
/* 119:    */           Long seed;
/* 120:147 */           if (waitTime <= 0L) {
/* 121:148 */             seed = (Long)seedQueue.poll();
/* 122:    */           } else {
/* 123:150 */             seed = (Long)seedQueue.poll(waitTime, TimeUnit.NANOSECONDS);
/* 124:    */           }
/* 125:153 */           if (seed != null)
/* 126:    */           {
/* 127:154 */             initialSeedUniquifier = seed.longValue();
/* 128:155 */             break;
/* 129:    */           }
/* 130:    */         }
/* 131:    */         catch (InterruptedException e)
/* 132:    */         {
/* 133:158 */           interrupted = true;
/* 134:159 */           logger.warn("Failed to generate a seed from SecureRandom due to an InterruptedException.");
/* 135:160 */           break;
/* 136:    */         }
/* 137:163 */         if (waitTime <= 0L)
/* 138:    */         {
/* 139:164 */           seedGeneratorThread.interrupt();
/* 140:165 */           logger.warn("Failed to generate a seed from SecureRandom within {} seconds. Not enough entropy?", 
/* 141:    */           
/* 142:167 */             Long.valueOf(3L));
/* 143:    */           
/* 144:169 */           break;
/* 145:    */         }
/* 146:    */       }
/* 147:174 */       initialSeedUniquifier ^= 0x33BAE119;
/* 148:175 */       initialSeedUniquifier ^= Long.reverse(System.nanoTime());
/* 149:    */       
/* 150:177 */       initialSeedUniquifier = initialSeedUniquifier;
/* 151:179 */       if (interrupted)
/* 152:    */       {
/* 153:181 */         Thread.currentThread().interrupt();
/* 154:    */         
/* 155:    */ 
/* 156:    */ 
/* 157:185 */         seedGeneratorThread.interrupt();
/* 158:    */       }
/* 159:188 */       if (seedGeneratorEndTime == 0L) {
/* 160:189 */         seedGeneratorEndTime = System.nanoTime();
/* 161:    */       }
/* 162:192 */       return initialSeedUniquifier;
/* 163:    */     }
/* 164:    */   }
/* 165:    */   
/* 166:    */   private static long newSeed()
/* 167:    */   {
/* 168:    */     for (;;)
/* 169:    */     {
/* 170:198 */       long current = seedUniquifier.get();
/* 171:199 */       long actualCurrent = current != 0L ? current : getInitialSeedUniquifier();
/* 172:    */       
/* 173:    */ 
/* 174:202 */       long next = actualCurrent * 181783497276652981L;
/* 175:204 */       if (seedUniquifier.compareAndSet(current, next))
/* 176:    */       {
/* 177:205 */         if ((current == 0L) && (logger.isDebugEnabled())) {
/* 178:206 */           if (seedGeneratorEndTime != 0L) {
/* 179:207 */             logger.debug(String.format("-Dio.netty.initialSeedUniquifier: 0x%016x (took %d ms)", new Object[] {
/* 180:    */             
/* 181:209 */               Long.valueOf(actualCurrent), 
/* 182:210 */               Long.valueOf(TimeUnit.NANOSECONDS.toMillis(seedGeneratorEndTime - seedGeneratorStartTime)) }));
/* 183:    */           } else {
/* 184:212 */             logger.debug(String.format("-Dio.netty.initialSeedUniquifier: 0x%016x", new Object[] { Long.valueOf(actualCurrent) }));
/* 185:    */           }
/* 186:    */         }
/* 187:215 */         return next ^ System.nanoTime();
/* 188:    */       }
/* 189:    */     }
/* 190:    */   }
/* 191:    */   
/* 192:    */   private static long mix64(long z)
/* 193:    */   {
/* 194:223 */     z = (z ^ z >>> 33) * -49064778989728563L;
/* 195:224 */     z = (z ^ z >>> 33) * -4265267296055464877L;
/* 196:225 */     return z ^ z >>> 33;
/* 197:    */   }
/* 198:    */   
/* 199:    */   ThreadLocalRandom()
/* 200:    */   {
/* 201:255 */     super(newSeed());
/* 202:256 */     this.initialized = true;
/* 203:    */   }
/* 204:    */   
/* 205:    */   public static ThreadLocalRandom current()
/* 206:    */   {
/* 207:265 */     return InternalThreadLocalMap.get().random();
/* 208:    */   }
/* 209:    */   
/* 210:    */   public void setSeed(long seed)
/* 211:    */   {
/* 212:275 */     if (this.initialized) {
/* 213:276 */       throw new UnsupportedOperationException();
/* 214:    */     }
/* 215:278 */     this.rnd = ((seed ^ 0xDEECE66D) & 0xFFFFFFFF);
/* 216:    */   }
/* 217:    */   
/* 218:    */   protected int next(int bits)
/* 219:    */   {
/* 220:282 */     this.rnd = (this.rnd * 25214903917L + 11L & 0xFFFFFFFF);
/* 221:283 */     return (int)(this.rnd >>> 48 - bits);
/* 222:    */   }
/* 223:    */   
/* 224:    */   public int nextInt(int least, int bound)
/* 225:    */   {
/* 226:297 */     if (least >= bound) {
/* 227:298 */       throw new IllegalArgumentException();
/* 228:    */     }
/* 229:300 */     return nextInt(bound - least) + least;
/* 230:    */   }
/* 231:    */   
/* 232:    */   public long nextLong(long n)
/* 233:    */   {
/* 234:313 */     if (n <= 0L) {
/* 235:314 */       throw new IllegalArgumentException("n must be positive");
/* 236:    */     }
/* 237:322 */     long offset = 0L;
/* 238:323 */     while (n >= 2147483647L)
/* 239:    */     {
/* 240:324 */       int bits = next(2);
/* 241:325 */       long half = n >>> 1;
/* 242:326 */       long nextn = (bits & 0x2) == 0 ? half : n - half;
/* 243:327 */       if ((bits & 0x1) == 0) {
/* 244:328 */         offset += n - nextn;
/* 245:    */       }
/* 246:330 */       n = nextn;
/* 247:    */     }
/* 248:332 */     return offset + nextInt((int)n);
/* 249:    */   }
/* 250:    */   
/* 251:    */   public long nextLong(long least, long bound)
/* 252:    */   {
/* 253:346 */     if (least >= bound) {
/* 254:347 */       throw new IllegalArgumentException();
/* 255:    */     }
/* 256:349 */     return nextLong(bound - least) + least;
/* 257:    */   }
/* 258:    */   
/* 259:    */   public double nextDouble(double n)
/* 260:    */   {
/* 261:362 */     if (n <= 0.0D) {
/* 262:363 */       throw new IllegalArgumentException("n must be positive");
/* 263:    */     }
/* 264:365 */     return nextDouble() * n;
/* 265:    */   }
/* 266:    */   
/* 267:    */   public double nextDouble(double least, double bound)
/* 268:    */   {
/* 269:379 */     if (least >= bound) {
/* 270:380 */       throw new IllegalArgumentException();
/* 271:    */     }
/* 272:382 */     return nextDouble() * (bound - least) + least;
/* 273:    */   }
/* 274:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.ThreadLocalRandom
 * JD-Core Version:    0.7.0.1
 */