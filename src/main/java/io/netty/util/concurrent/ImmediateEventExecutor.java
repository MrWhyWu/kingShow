/*   1:    */ package io.netty.util.concurrent;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.logging.InternalLogger;
/*   4:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   5:    */ import java.util.ArrayDeque;
/*   6:    */ import java.util.Queue;
/*   7:    */ import java.util.concurrent.TimeUnit;
/*   8:    */ 
/*   9:    */ public final class ImmediateEventExecutor
/*  10:    */   extends AbstractEventExecutor
/*  11:    */ {
/*  12: 33 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(ImmediateEventExecutor.class);
/*  13: 34 */   public static final ImmediateEventExecutor INSTANCE = new ImmediateEventExecutor();
/*  14: 38 */   private static final FastThreadLocal<Queue<Runnable>> DELAYED_RUNNABLES = new FastThreadLocal()
/*  15:    */   {
/*  16:    */     protected Queue<Runnable> initialValue()
/*  17:    */       throws Exception
/*  18:    */     {
/*  19: 41 */       return new ArrayDeque();
/*  20:    */     }
/*  21:    */   };
/*  22: 47 */   private static final FastThreadLocal<Boolean> RUNNING = new FastThreadLocal()
/*  23:    */   {
/*  24:    */     protected Boolean initialValue()
/*  25:    */       throws Exception
/*  26:    */     {
/*  27: 50 */       return Boolean.valueOf(false);
/*  28:    */     }
/*  29:    */   };
/*  30: 54 */   private final Future<?> terminationFuture = new FailedFuture(GlobalEventExecutor.INSTANCE, new UnsupportedOperationException());
/*  31:    */   
/*  32:    */   public boolean inEventLoop()
/*  33:    */   {
/*  34: 61 */     return true;
/*  35:    */   }
/*  36:    */   
/*  37:    */   public boolean inEventLoop(Thread thread)
/*  38:    */   {
/*  39: 66 */     return true;
/*  40:    */   }
/*  41:    */   
/*  42:    */   public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit)
/*  43:    */   {
/*  44: 71 */     return terminationFuture();
/*  45:    */   }
/*  46:    */   
/*  47:    */   public Future<?> terminationFuture()
/*  48:    */   {
/*  49: 76 */     return this.terminationFuture;
/*  50:    */   }
/*  51:    */   
/*  52:    */   @Deprecated
/*  53:    */   public void shutdown() {}
/*  54:    */   
/*  55:    */   public boolean isShuttingDown()
/*  56:    */   {
/*  57: 85 */     return false;
/*  58:    */   }
/*  59:    */   
/*  60:    */   public boolean isShutdown()
/*  61:    */   {
/*  62: 90 */     return false;
/*  63:    */   }
/*  64:    */   
/*  65:    */   public boolean isTerminated()
/*  66:    */   {
/*  67: 95 */     return false;
/*  68:    */   }
/*  69:    */   
/*  70:    */   public boolean awaitTermination(long timeout, TimeUnit unit)
/*  71:    */   {
/*  72:100 */     return false;
/*  73:    */   }
/*  74:    */   
/*  75:    */   /* Error */
/*  76:    */   public void execute(Runnable command)
/*  77:    */   {
/*  78:    */     // Byte code:
/*  79:    */     //   0: aload_1
/*  80:    */     //   1: ifnonnull +13 -> 14
/*  81:    */     //   4: new 75	java/lang/NullPointerException
/*  82:    */     //   7: dup
/*  83:    */     //   8: ldc 77
/*  84:    */     //   10: invokespecial 80	java/lang/NullPointerException:<init>	(Ljava/lang/String;)V
/*  85:    */     //   13: athrow
/*  86:    */     //   14: getstatic 82	io/netty/util/concurrent/ImmediateEventExecutor:RUNNING	Lio/netty/util/concurrent/FastThreadLocal;
/*  87:    */     //   17: invokevirtual 88	io/netty/util/concurrent/FastThreadLocal:get	()Ljava/lang/Object;
/*  88:    */     //   20: checkcast 90	java/lang/Boolean
/*  89:    */     //   23: invokevirtual 93	java/lang/Boolean:booleanValue	()Z
/*  90:    */     //   26: ifne +234 -> 260
/*  91:    */     //   29: getstatic 82	io/netty/util/concurrent/ImmediateEventExecutor:RUNNING	Lio/netty/util/concurrent/FastThreadLocal;
/*  92:    */     //   32: iconst_1
/*  93:    */     //   33: invokestatic 97	java/lang/Boolean:valueOf	(Z)Ljava/lang/Boolean;
/*  94:    */     //   36: invokevirtual 101	io/netty/util/concurrent/FastThreadLocal:set	(Ljava/lang/Object;)V
/*  95:    */     //   39: aload_1
/*  96:    */     //   40: invokeinterface 106 1 0
/*  97:    */     //   45: getstatic 108	io/netty/util/concurrent/ImmediateEventExecutor:DELAYED_RUNNABLES	Lio/netty/util/concurrent/FastThreadLocal;
/*  98:    */     //   48: invokevirtual 88	io/netty/util/concurrent/FastThreadLocal:get	()Ljava/lang/Object;
/*  99:    */     //   51: checkcast 110	java/util/Queue
/* 100:    */     //   54: astore_2
/* 101:    */     //   55: aload_2
/* 102:    */     //   56: invokeinterface 113 1 0
/* 103:    */     //   61: checkcast 103	java/lang/Runnable
/* 104:    */     //   64: dup
/* 105:    */     //   65: astore_3
/* 106:    */     //   66: ifnull +30 -> 96
/* 107:    */     //   69: aload_3
/* 108:    */     //   70: invokeinterface 106 1 0
/* 109:    */     //   75: goto -20 -> 55
/* 110:    */     //   78: astore 4
/* 111:    */     //   80: getstatic 115	io/netty/util/concurrent/ImmediateEventExecutor:logger	Lio/netty/util/internal/logging/InternalLogger;
/* 112:    */     //   83: ldc 117
/* 113:    */     //   85: aload_3
/* 114:    */     //   86: aload 4
/* 115:    */     //   88: invokeinterface 123 4 0
/* 116:    */     //   93: goto -38 -> 55
/* 117:    */     //   96: getstatic 82	io/netty/util/concurrent/ImmediateEventExecutor:RUNNING	Lio/netty/util/concurrent/FastThreadLocal;
/* 118:    */     //   99: iconst_0
/* 119:    */     //   100: invokestatic 97	java/lang/Boolean:valueOf	(Z)Ljava/lang/Boolean;
/* 120:    */     //   103: invokevirtual 101	io/netty/util/concurrent/FastThreadLocal:set	(Ljava/lang/Object;)V
/* 121:    */     //   106: goto +151 -> 257
/* 122:    */     //   109: astore_2
/* 123:    */     //   110: getstatic 115	io/netty/util/concurrent/ImmediateEventExecutor:logger	Lio/netty/util/internal/logging/InternalLogger;
/* 124:    */     //   113: ldc 117
/* 125:    */     //   115: aload_1
/* 126:    */     //   116: aload_2
/* 127:    */     //   117: invokeinterface 123 4 0
/* 128:    */     //   122: getstatic 108	io/netty/util/concurrent/ImmediateEventExecutor:DELAYED_RUNNABLES	Lio/netty/util/concurrent/FastThreadLocal;
/* 129:    */     //   125: invokevirtual 88	io/netty/util/concurrent/FastThreadLocal:get	()Ljava/lang/Object;
/* 130:    */     //   128: checkcast 110	java/util/Queue
/* 131:    */     //   131: astore_2
/* 132:    */     //   132: aload_2
/* 133:    */     //   133: invokeinterface 113 1 0
/* 134:    */     //   138: checkcast 103	java/lang/Runnable
/* 135:    */     //   141: dup
/* 136:    */     //   142: astore_3
/* 137:    */     //   143: ifnull +30 -> 173
/* 138:    */     //   146: aload_3
/* 139:    */     //   147: invokeinterface 106 1 0
/* 140:    */     //   152: goto -20 -> 132
/* 141:    */     //   155: astore 4
/* 142:    */     //   157: getstatic 115	io/netty/util/concurrent/ImmediateEventExecutor:logger	Lio/netty/util/internal/logging/InternalLogger;
/* 143:    */     //   160: ldc 117
/* 144:    */     //   162: aload_3
/* 145:    */     //   163: aload 4
/* 146:    */     //   165: invokeinterface 123 4 0
/* 147:    */     //   170: goto -38 -> 132
/* 148:    */     //   173: getstatic 82	io/netty/util/concurrent/ImmediateEventExecutor:RUNNING	Lio/netty/util/concurrent/FastThreadLocal;
/* 149:    */     //   176: iconst_0
/* 150:    */     //   177: invokestatic 97	java/lang/Boolean:valueOf	(Z)Ljava/lang/Boolean;
/* 151:    */     //   180: invokevirtual 101	io/netty/util/concurrent/FastThreadLocal:set	(Ljava/lang/Object;)V
/* 152:    */     //   183: goto +74 -> 257
/* 153:    */     //   186: astore 5
/* 154:    */     //   188: getstatic 108	io/netty/util/concurrent/ImmediateEventExecutor:DELAYED_RUNNABLES	Lio/netty/util/concurrent/FastThreadLocal;
/* 155:    */     //   191: invokevirtual 88	io/netty/util/concurrent/FastThreadLocal:get	()Ljava/lang/Object;
/* 156:    */     //   194: checkcast 110	java/util/Queue
/* 157:    */     //   197: astore 6
/* 158:    */     //   199: aload 6
/* 159:    */     //   201: invokeinterface 113 1 0
/* 160:    */     //   206: checkcast 103	java/lang/Runnable
/* 161:    */     //   209: dup
/* 162:    */     //   210: astore 7
/* 163:    */     //   212: ifnull +32 -> 244
/* 164:    */     //   215: aload 7
/* 165:    */     //   217: invokeinterface 106 1 0
/* 166:    */     //   222: goto -23 -> 199
/* 167:    */     //   225: astore 8
/* 168:    */     //   227: getstatic 115	io/netty/util/concurrent/ImmediateEventExecutor:logger	Lio/netty/util/internal/logging/InternalLogger;
/* 169:    */     //   230: ldc 117
/* 170:    */     //   232: aload 7
/* 171:    */     //   234: aload 8
/* 172:    */     //   236: invokeinterface 123 4 0
/* 173:    */     //   241: goto -42 -> 199
/* 174:    */     //   244: getstatic 82	io/netty/util/concurrent/ImmediateEventExecutor:RUNNING	Lio/netty/util/concurrent/FastThreadLocal;
/* 175:    */     //   247: iconst_0
/* 176:    */     //   248: invokestatic 97	java/lang/Boolean:valueOf	(Z)Ljava/lang/Boolean;
/* 177:    */     //   251: invokevirtual 101	io/netty/util/concurrent/FastThreadLocal:set	(Ljava/lang/Object;)V
/* 178:    */     //   254: aload 5
/* 179:    */     //   256: athrow
/* 180:    */     //   257: goto +19 -> 276
/* 181:    */     //   260: getstatic 108	io/netty/util/concurrent/ImmediateEventExecutor:DELAYED_RUNNABLES	Lio/netty/util/concurrent/FastThreadLocal;
/* 182:    */     //   263: invokevirtual 88	io/netty/util/concurrent/FastThreadLocal:get	()Ljava/lang/Object;
/* 183:    */     //   266: checkcast 110	java/util/Queue
/* 184:    */     //   269: aload_1
/* 185:    */     //   270: invokeinterface 127 2 0
/* 186:    */     //   275: pop
/* 187:    */     //   276: return
/* 188:    */     // Line number table:
/* 189:    */     //   Java source line #105	-> byte code offset #0
/* 190:    */     //   Java source line #106	-> byte code offset #4
/* 191:    */     //   Java source line #108	-> byte code offset #14
/* 192:    */     //   Java source line #109	-> byte code offset #29
/* 193:    */     //   Java source line #111	-> byte code offset #39
/* 194:    */     //   Java source line #115	-> byte code offset #45
/* 195:    */     //   Java source line #117	-> byte code offset #55
/* 196:    */     //   Java source line #119	-> byte code offset #69
/* 197:    */     //   Java source line #122	-> byte code offset #75
/* 198:    */     //   Java source line #120	-> byte code offset #78
/* 199:    */     //   Java source line #121	-> byte code offset #80
/* 200:    */     //   Java source line #122	-> byte code offset #93
/* 201:    */     //   Java source line #124	-> byte code offset #96
/* 202:    */     //   Java source line #125	-> byte code offset #106
/* 203:    */     //   Java source line #112	-> byte code offset #109
/* 204:    */     //   Java source line #113	-> byte code offset #110
/* 205:    */     //   Java source line #115	-> byte code offset #122
/* 206:    */     //   Java source line #117	-> byte code offset #132
/* 207:    */     //   Java source line #119	-> byte code offset #146
/* 208:    */     //   Java source line #122	-> byte code offset #152
/* 209:    */     //   Java source line #120	-> byte code offset #155
/* 210:    */     //   Java source line #121	-> byte code offset #157
/* 211:    */     //   Java source line #122	-> byte code offset #170
/* 212:    */     //   Java source line #124	-> byte code offset #173
/* 213:    */     //   Java source line #125	-> byte code offset #183
/* 214:    */     //   Java source line #115	-> byte code offset #186
/* 215:    */     //   Java source line #117	-> byte code offset #199
/* 216:    */     //   Java source line #119	-> byte code offset #215
/* 217:    */     //   Java source line #122	-> byte code offset #222
/* 218:    */     //   Java source line #120	-> byte code offset #225
/* 219:    */     //   Java source line #121	-> byte code offset #227
/* 220:    */     //   Java source line #122	-> byte code offset #241
/* 221:    */     //   Java source line #124	-> byte code offset #244
/* 222:    */     //   Java source line #125	-> byte code offset #254
/* 223:    */     //   Java source line #127	-> byte code offset #260
/* 224:    */     //   Java source line #129	-> byte code offset #276
/* 225:    */     // Local variable table:
/* 226:    */     //   start	length	slot	name	signature
/* 227:    */     //   0	277	0	this	ImmediateEventExecutor
/* 228:    */     //   0	277	1	command	Runnable
/* 229:    */     //   54	2	2	delayedRunnables	Queue<Runnable>
/* 230:    */     //   109	8	2	cause	java.lang.Throwable
/* 231:    */     //   131	2	2	delayedRunnables	Queue<Runnable>
/* 232:    */     //   65	21	3	runnable	Runnable
/* 233:    */     //   142	21	3	runnable	Runnable
/* 234:    */     //   78	9	4	cause	java.lang.Throwable
/* 235:    */     //   155	9	4	cause	java.lang.Throwable
/* 236:    */     //   186	69	5	localObject	Object
/* 237:    */     //   197	3	6	delayedRunnables	Queue<Runnable>
/* 238:    */     //   210	23	7	runnable	Runnable
/* 239:    */     //   225	10	8	cause	java.lang.Throwable
/* 240:    */     // Exception table:
/* 241:    */     //   from	to	target	type
/* 242:    */     //   69	75	78	java/lang/Throwable
/* 243:    */     //   39	45	109	java/lang/Throwable
/* 244:    */     //   146	152	155	java/lang/Throwable
/* 245:    */     //   39	45	186	finally
/* 246:    */     //   109	122	186	finally
/* 247:    */     //   186	188	186	finally
/* 248:    */     //   215	222	225	java/lang/Throwable
/* 249:    */   }
/* 250:    */   
/* 251:    */   public <V> Promise<V> newPromise()
/* 252:    */   {
/* 253:133 */     return new ImmediatePromise(this);
/* 254:    */   }
/* 255:    */   
/* 256:    */   public <V> ProgressivePromise<V> newProgressivePromise()
/* 257:    */   {
/* 258:138 */     return new ImmediateProgressivePromise(this);
/* 259:    */   }
/* 260:    */   
/* 261:    */   static class ImmediatePromise<V>
/* 262:    */     extends DefaultPromise<V>
/* 263:    */   {
/* 264:    */     ImmediatePromise(EventExecutor executor)
/* 265:    */     {
/* 266:143 */       super();
/* 267:    */     }
/* 268:    */     
/* 269:    */     protected void checkDeadLock() {}
/* 270:    */   }
/* 271:    */   
/* 272:    */   static class ImmediateProgressivePromise<V>
/* 273:    */     extends DefaultProgressivePromise<V>
/* 274:    */   {
/* 275:    */     ImmediateProgressivePromise(EventExecutor executor)
/* 276:    */     {
/* 277:154 */       super();
/* 278:    */     }
/* 279:    */     
/* 280:    */     protected void checkDeadLock() {}
/* 281:    */   }
/* 282:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.concurrent.ImmediateEventExecutor
 * JD-Core Version:    0.7.0.1
 */