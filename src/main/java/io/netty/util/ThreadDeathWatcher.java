/*   1:    */ package io.netty.util;
/*   2:    */ 
/*   3:    */ import io.netty.util.concurrent.DefaultThreadFactory;
/*   4:    */ import io.netty.util.internal.StringUtil;
/*   5:    */ import io.netty.util.internal.SystemPropertyUtil;
/*   6:    */ import io.netty.util.internal.logging.InternalLogger;
/*   7:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   8:    */ import java.util.ArrayList;
/*   9:    */ import java.util.List;
/*  10:    */ import java.util.Queue;
/*  11:    */ import java.util.concurrent.ConcurrentLinkedQueue;
/*  12:    */ import java.util.concurrent.ThreadFactory;
/*  13:    */ import java.util.concurrent.TimeUnit;
/*  14:    */ import java.util.concurrent.atomic.AtomicBoolean;
/*  15:    */ 
/*  16:    */ public final class ThreadDeathWatcher
/*  17:    */ {
/*  18: 43 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(ThreadDeathWatcher.class);
/*  19:    */   static final ThreadFactory threadFactory;
/*  20: 49 */   private static final Queue<Entry> pendingEntries = new ConcurrentLinkedQueue();
/*  21: 50 */   private static final Watcher watcher = new Watcher(null);
/*  22: 51 */   private static final AtomicBoolean started = new AtomicBoolean();
/*  23:    */   private static volatile Thread watcherThread;
/*  24:    */   
/*  25:    */   static
/*  26:    */   {
/*  27: 55 */     String poolName = "threadDeathWatcher";
/*  28: 56 */     String serviceThreadPrefix = SystemPropertyUtil.get("io.netty.serviceThreadPrefix");
/*  29: 57 */     if (!StringUtil.isNullOrEmpty(serviceThreadPrefix)) {
/*  30: 58 */       poolName = serviceThreadPrefix + poolName;
/*  31:    */     }
/*  32: 63 */     threadFactory = new DefaultThreadFactory(poolName, true, 1, null);
/*  33:    */   }
/*  34:    */   
/*  35:    */   public static void watch(Thread thread, Runnable task)
/*  36:    */   {
/*  37: 75 */     if (thread == null) {
/*  38: 76 */       throw new NullPointerException("thread");
/*  39:    */     }
/*  40: 78 */     if (task == null) {
/*  41: 79 */       throw new NullPointerException("task");
/*  42:    */     }
/*  43: 81 */     if (!thread.isAlive()) {
/*  44: 82 */       throw new IllegalArgumentException("thread must be alive.");
/*  45:    */     }
/*  46: 85 */     schedule(thread, task, true);
/*  47:    */   }
/*  48:    */   
/*  49:    */   public static void unwatch(Thread thread, Runnable task)
/*  50:    */   {
/*  51: 92 */     if (thread == null) {
/*  52: 93 */       throw new NullPointerException("thread");
/*  53:    */     }
/*  54: 95 */     if (task == null) {
/*  55: 96 */       throw new NullPointerException("task");
/*  56:    */     }
/*  57: 99 */     schedule(thread, task, false);
/*  58:    */   }
/*  59:    */   
/*  60:    */   private static void schedule(Thread thread, Runnable task, boolean isWatch)
/*  61:    */   {
/*  62:103 */     pendingEntries.add(new Entry(thread, task, isWatch));
/*  63:105 */     if (started.compareAndSet(false, true))
/*  64:    */     {
/*  65:106 */       Thread watcherThread = threadFactory.newThread(watcher);
/*  66:    */       
/*  67:    */ 
/*  68:    */ 
/*  69:    */ 
/*  70:    */ 
/*  71:112 */       watcherThread.setContextClassLoader(null);
/*  72:    */       
/*  73:114 */       watcherThread.start();
/*  74:115 */       watcherThread = watcherThread;
/*  75:    */     }
/*  76:    */   }
/*  77:    */   
/*  78:    */   public static boolean awaitInactivity(long timeout, TimeUnit unit)
/*  79:    */     throws InterruptedException
/*  80:    */   {
/*  81:129 */     if (unit == null) {
/*  82:130 */       throw new NullPointerException("unit");
/*  83:    */     }
/*  84:133 */     Thread watcherThread = watcherThread;
/*  85:134 */     if (watcherThread != null)
/*  86:    */     {
/*  87:135 */       watcherThread.join(unit.toMillis(timeout));
/*  88:136 */       return !watcherThread.isAlive();
/*  89:    */     }
/*  90:138 */     return true;
/*  91:    */   }
/*  92:    */   
/*  93:    */   private static final class Watcher
/*  94:    */     implements Runnable
/*  95:    */   {
/*  96:146 */     private final List<ThreadDeathWatcher.Entry> watchees = new ArrayList();
/*  97:    */     
/*  98:    */     public void run()
/*  99:    */     {
/* 100:    */       for (;;)
/* 101:    */       {
/* 102:151 */         fetchWatchees();
/* 103:152 */         notifyWatchees();
/* 104:    */         
/* 105:    */ 
/* 106:155 */         fetchWatchees();
/* 107:156 */         notifyWatchees();
/* 108:    */         try
/* 109:    */         {
/* 110:159 */           Thread.sleep(1000L);
/* 111:    */         }
/* 112:    */         catch (InterruptedException localInterruptedException) {}
/* 113:164 */         if ((this.watchees.isEmpty()) && (ThreadDeathWatcher.pendingEntries.isEmpty()))
/* 114:    */         {
/* 115:169 */           boolean stopped = ThreadDeathWatcher.started.compareAndSet(true, false);
/* 116:170 */           assert (stopped);
/* 117:173 */           if (ThreadDeathWatcher.pendingEntries.isEmpty()) {
/* 118:    */             break;
/* 119:    */           }
/* 120:182 */           if (!ThreadDeathWatcher.started.compareAndSet(false, true)) {
/* 121:    */             break;
/* 122:    */           }
/* 123:    */         }
/* 124:    */       }
/* 125:    */     }
/* 126:    */     
/* 127:    */     private void fetchWatchees()
/* 128:    */     {
/* 129:    */       for (;;)
/* 130:    */       {
/* 131:197 */         ThreadDeathWatcher.Entry e = (ThreadDeathWatcher.Entry)ThreadDeathWatcher.pendingEntries.poll();
/* 132:198 */         if (e == null) {
/* 133:    */           break;
/* 134:    */         }
/* 135:202 */         if (e.isWatch) {
/* 136:203 */           this.watchees.add(e);
/* 137:    */         } else {
/* 138:205 */           this.watchees.remove(e);
/* 139:    */         }
/* 140:    */       }
/* 141:    */     }
/* 142:    */     
/* 143:    */     private void notifyWatchees()
/* 144:    */     {
/* 145:211 */       List<ThreadDeathWatcher.Entry> watchees = this.watchees;
/* 146:212 */       for (int i = 0; i < watchees.size();)
/* 147:    */       {
/* 148:213 */         ThreadDeathWatcher.Entry e = (ThreadDeathWatcher.Entry)watchees.get(i);
/* 149:214 */         if (!e.thread.isAlive())
/* 150:    */         {
/* 151:215 */           watchees.remove(i);
/* 152:    */           try
/* 153:    */           {
/* 154:217 */             e.task.run();
/* 155:    */           }
/* 156:    */           catch (Throwable t)
/* 157:    */           {
/* 158:219 */             ThreadDeathWatcher.logger.warn("Thread death watcher task raised an exception:", t);
/* 159:    */           }
/* 160:    */         }
/* 161:    */         else
/* 162:    */         {
/* 163:222 */           i++;
/* 164:    */         }
/* 165:    */       }
/* 166:    */     }
/* 167:    */   }
/* 168:    */   
/* 169:    */   private static final class Entry
/* 170:    */   {
/* 171:    */     final Thread thread;
/* 172:    */     final Runnable task;
/* 173:    */     final boolean isWatch;
/* 174:    */     
/* 175:    */     Entry(Thread thread, Runnable task, boolean isWatch)
/* 176:    */     {
/* 177:234 */       this.thread = thread;
/* 178:235 */       this.task = task;
/* 179:236 */       this.isWatch = isWatch;
/* 180:    */     }
/* 181:    */     
/* 182:    */     public int hashCode()
/* 183:    */     {
/* 184:241 */       return this.thread.hashCode() ^ this.task.hashCode();
/* 185:    */     }
/* 186:    */     
/* 187:    */     public boolean equals(Object obj)
/* 188:    */     {
/* 189:246 */       if (obj == this) {
/* 190:247 */         return true;
/* 191:    */       }
/* 192:250 */       if (!(obj instanceof Entry)) {
/* 193:251 */         return false;
/* 194:    */       }
/* 195:254 */       Entry that = (Entry)obj;
/* 196:255 */       return (this.thread == that.thread) && (this.task == that.task);
/* 197:    */     }
/* 198:    */   }
/* 199:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.ThreadDeathWatcher
 * JD-Core Version:    0.7.0.1
 */