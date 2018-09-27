/*   1:    */ package io.netty.util.concurrent;
/*   2:    */ 
/*   3:    */ import java.util.Collections;
/*   4:    */ import java.util.Iterator;
/*   5:    */ import java.util.LinkedHashSet;
/*   6:    */ import java.util.Set;
/*   7:    */ import java.util.concurrent.Executor;
/*   8:    */ import java.util.concurrent.ThreadFactory;
/*   9:    */ import java.util.concurrent.TimeUnit;
/*  10:    */ import java.util.concurrent.atomic.AtomicInteger;
/*  11:    */ 
/*  12:    */ public abstract class MultithreadEventExecutorGroup
/*  13:    */   extends AbstractEventExecutorGroup
/*  14:    */ {
/*  15:    */   private final EventExecutor[] children;
/*  16:    */   private final Set<EventExecutor> readonlyChildren;
/*  17: 35 */   private final AtomicInteger terminatedChildren = new AtomicInteger();
/*  18: 36 */   private final Promise<?> terminationFuture = new DefaultPromise(GlobalEventExecutor.INSTANCE);
/*  19:    */   private final EventExecutorChooserFactory.EventExecutorChooser chooser;
/*  20:    */   
/*  21:    */   protected MultithreadEventExecutorGroup(int nThreads, ThreadFactory threadFactory, Object... args)
/*  22:    */   {
/*  23: 47 */     this(nThreads, threadFactory == null ? null : new ThreadPerTaskExecutor(threadFactory), args);
/*  24:    */   }
/*  25:    */   
/*  26:    */   protected MultithreadEventExecutorGroup(int nThreads, Executor executor, Object... args)
/*  27:    */   {
/*  28: 58 */     this(nThreads, executor, DefaultEventExecutorChooserFactory.INSTANCE, args);
/*  29:    */   }
/*  30:    */   
/*  31:    */   protected MultithreadEventExecutorGroup(int nThreads, Executor executor, EventExecutorChooserFactory chooserFactory, Object... args)
/*  32:    */   {
/*  33: 71 */     if (nThreads <= 0) {
/*  34: 72 */       throw new IllegalArgumentException(String.format("nThreads: %d (expected: > 0)", new Object[] { Integer.valueOf(nThreads) }));
/*  35:    */     }
/*  36: 75 */     if (executor == null) {
/*  37: 76 */       executor = new ThreadPerTaskExecutor(newDefaultThreadFactory());
/*  38:    */     }
/*  39: 79 */     this.children = new EventExecutor[nThreads];
/*  40: 81 */     for (int i = 0; i < nThreads; i++)
/*  41:    */     {
/*  42: 82 */       success = false;
/*  43:    */       try
/*  44:    */       {
/*  45: 84 */         this.children[i] = newChild(executor, args);
/*  46: 85 */         success = true;
/*  47:    */       }
/*  48:    */       catch (Exception e)
/*  49:    */       {
/*  50:    */         int j;
/*  51:    */         int j;
/*  52: 88 */         throw new IllegalStateException("failed to create a child event loop", e);
/*  53:    */       }
/*  54:    */       finally
/*  55:    */       {
/*  56: 90 */         if (!success)
/*  57:    */         {
/*  58: 91 */           for (int j = 0; j < i; j++) {
/*  59: 92 */             this.children[j].shutdownGracefully();
/*  60:    */           }
/*  61: 95 */           for (int j = 0; j < i; j++)
/*  62:    */           {
/*  63: 96 */             EventExecutor e = this.children[j];
/*  64:    */             try
/*  65:    */             {
/*  66: 98 */               while (!e.isTerminated()) {
/*  67: 99 */                 e.awaitTermination(2147483647L, TimeUnit.SECONDS);
/*  68:    */               }
/*  69:    */             }
/*  70:    */             catch (InterruptedException interrupted)
/*  71:    */             {
/*  72:103 */               Thread.currentThread().interrupt();
/*  73:104 */               break;
/*  74:    */             }
/*  75:    */           }
/*  76:    */         }
/*  77:    */       }
/*  78:    */     }
/*  79:111 */     this.chooser = chooserFactory.newChooser(this.children);
/*  80:    */     
/*  81:113 */     FutureListener<Object> terminationListener = new FutureListener()
/*  82:    */     {
/*  83:    */       public void operationComplete(Future<Object> future)
/*  84:    */         throws Exception
/*  85:    */       {
/*  86:116 */         if (MultithreadEventExecutorGroup.this.terminatedChildren.incrementAndGet() == MultithreadEventExecutorGroup.this.children.length) {
/*  87:117 */           MultithreadEventExecutorGroup.this.terminationFuture.setSuccess(null);
/*  88:    */         }
/*  89:    */       }
/*  90:121 */     };
/*  91:122 */     boolean success = this.children;e = success.length;
/*  92:122 */     for (EventExecutor e = 0; e < e; e++)
/*  93:    */     {
/*  94:122 */       EventExecutor e = success[e];
/*  95:123 */       e.terminationFuture().addListener(terminationListener);
/*  96:    */     }
/*  97:126 */     Set<EventExecutor> childrenSet = new LinkedHashSet(this.children.length);
/*  98:127 */     Collections.addAll(childrenSet, this.children);
/*  99:128 */     this.readonlyChildren = Collections.unmodifiableSet(childrenSet);
/* 100:    */   }
/* 101:    */   
/* 102:    */   protected ThreadFactory newDefaultThreadFactory()
/* 103:    */   {
/* 104:132 */     return new DefaultThreadFactory(getClass());
/* 105:    */   }
/* 106:    */   
/* 107:    */   public EventExecutor next()
/* 108:    */   {
/* 109:137 */     return this.chooser.next();
/* 110:    */   }
/* 111:    */   
/* 112:    */   public Iterator<EventExecutor> iterator()
/* 113:    */   {
/* 114:142 */     return this.readonlyChildren.iterator();
/* 115:    */   }
/* 116:    */   
/* 117:    */   public final int executorCount()
/* 118:    */   {
/* 119:150 */     return this.children.length;
/* 120:    */   }
/* 121:    */   
/* 122:    */   protected abstract EventExecutor newChild(Executor paramExecutor, Object... paramVarArgs)
/* 123:    */     throws Exception;
/* 124:    */   
/* 125:    */   public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit)
/* 126:    */   {
/* 127:162 */     for (EventExecutor l : this.children) {
/* 128:163 */       l.shutdownGracefully(quietPeriod, timeout, unit);
/* 129:    */     }
/* 130:165 */     return terminationFuture();
/* 131:    */   }
/* 132:    */   
/* 133:    */   public Future<?> terminationFuture()
/* 134:    */   {
/* 135:170 */     return this.terminationFuture;
/* 136:    */   }
/* 137:    */   
/* 138:    */   @Deprecated
/* 139:    */   public void shutdown()
/* 140:    */   {
/* 141:176 */     for (EventExecutor l : this.children) {
/* 142:177 */       l.shutdown();
/* 143:    */     }
/* 144:    */   }
/* 145:    */   
/* 146:    */   public boolean isShuttingDown()
/* 147:    */   {
/* 148:183 */     for (EventExecutor l : this.children) {
/* 149:184 */       if (!l.isShuttingDown()) {
/* 150:185 */         return false;
/* 151:    */       }
/* 152:    */     }
/* 153:188 */     return true;
/* 154:    */   }
/* 155:    */   
/* 156:    */   public boolean isShutdown()
/* 157:    */   {
/* 158:193 */     for (EventExecutor l : this.children) {
/* 159:194 */       if (!l.isShutdown()) {
/* 160:195 */         return false;
/* 161:    */       }
/* 162:    */     }
/* 163:198 */     return true;
/* 164:    */   }
/* 165:    */   
/* 166:    */   public boolean isTerminated()
/* 167:    */   {
/* 168:203 */     for (EventExecutor l : this.children) {
/* 169:204 */       if (!l.isTerminated()) {
/* 170:205 */         return false;
/* 171:    */       }
/* 172:    */     }
/* 173:208 */     return true;
/* 174:    */   }
/* 175:    */   
/* 176:    */   public boolean awaitTermination(long timeout, TimeUnit unit)
/* 177:    */     throws InterruptedException
/* 178:    */   {
/* 179:214 */     long deadline = System.nanoTime() + unit.toNanos(timeout);
/* 180:215 */     for (EventExecutor l : this.children) {
/* 181:    */       for (;;)
/* 182:    */       {
/* 183:217 */         long timeLeft = deadline - System.nanoTime();
/* 184:218 */         if (timeLeft <= 0L) {
/* 185:    */           break label84;
/* 186:    */         }
/* 187:221 */         if (l.awaitTermination(timeLeft, TimeUnit.NANOSECONDS)) {
/* 188:    */           break;
/* 189:    */         }
/* 190:    */       }
/* 191:    */     }
/* 192:    */     label84:
/* 193:226 */     return isTerminated();
/* 194:    */   }
/* 195:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.concurrent.MultithreadEventExecutorGroup
 * JD-Core Version:    0.7.0.1
 */