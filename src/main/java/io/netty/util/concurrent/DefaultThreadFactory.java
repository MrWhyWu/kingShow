/*   1:    */ package io.netty.util.concurrent;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.StringUtil;
/*   4:    */ import java.util.Locale;
/*   5:    */ import java.util.concurrent.ThreadFactory;
/*   6:    */ import java.util.concurrent.atomic.AtomicInteger;
/*   7:    */ 
/*   8:    */ public class DefaultThreadFactory
/*   9:    */   implements ThreadFactory
/*  10:    */ {
/*  11: 30 */   private static final AtomicInteger poolId = new AtomicInteger();
/*  12: 32 */   private final AtomicInteger nextId = new AtomicInteger();
/*  13:    */   private final String prefix;
/*  14:    */   private final boolean daemon;
/*  15:    */   private final int priority;
/*  16:    */   protected final ThreadGroup threadGroup;
/*  17:    */   
/*  18:    */   public DefaultThreadFactory(Class<?> poolType)
/*  19:    */   {
/*  20: 39 */     this(poolType, false, 5);
/*  21:    */   }
/*  22:    */   
/*  23:    */   public DefaultThreadFactory(String poolName)
/*  24:    */   {
/*  25: 43 */     this(poolName, false, 5);
/*  26:    */   }
/*  27:    */   
/*  28:    */   public DefaultThreadFactory(Class<?> poolType, boolean daemon)
/*  29:    */   {
/*  30: 47 */     this(poolType, daemon, 5);
/*  31:    */   }
/*  32:    */   
/*  33:    */   public DefaultThreadFactory(String poolName, boolean daemon)
/*  34:    */   {
/*  35: 51 */     this(poolName, daemon, 5);
/*  36:    */   }
/*  37:    */   
/*  38:    */   public DefaultThreadFactory(Class<?> poolType, int priority)
/*  39:    */   {
/*  40: 55 */     this(poolType, false, priority);
/*  41:    */   }
/*  42:    */   
/*  43:    */   public DefaultThreadFactory(String poolName, int priority)
/*  44:    */   {
/*  45: 59 */     this(poolName, false, priority);
/*  46:    */   }
/*  47:    */   
/*  48:    */   public DefaultThreadFactory(Class<?> poolType, boolean daemon, int priority)
/*  49:    */   {
/*  50: 63 */     this(toPoolName(poolType), daemon, priority);
/*  51:    */   }
/*  52:    */   
/*  53:    */   public static String toPoolName(Class<?> poolType)
/*  54:    */   {
/*  55: 67 */     if (poolType == null) {
/*  56: 68 */       throw new NullPointerException("poolType");
/*  57:    */     }
/*  58: 71 */     String poolName = StringUtil.simpleClassName(poolType);
/*  59: 72 */     switch (poolName.length())
/*  60:    */     {
/*  61:    */     case 0: 
/*  62: 74 */       return "unknown";
/*  63:    */     case 1: 
/*  64: 76 */       return poolName.toLowerCase(Locale.US);
/*  65:    */     }
/*  66: 78 */     if ((Character.isUpperCase(poolName.charAt(0))) && (Character.isLowerCase(poolName.charAt(1)))) {
/*  67: 79 */       return Character.toLowerCase(poolName.charAt(0)) + poolName.substring(1);
/*  68:    */     }
/*  69: 81 */     return poolName;
/*  70:    */   }
/*  71:    */   
/*  72:    */   public DefaultThreadFactory(String poolName, boolean daemon, int priority, ThreadGroup threadGroup)
/*  73:    */   {
/*  74: 87 */     if (poolName == null) {
/*  75: 88 */       throw new NullPointerException("poolName");
/*  76:    */     }
/*  77: 90 */     if ((priority < 1) || (priority > 10)) {
/*  78: 91 */       throw new IllegalArgumentException("priority: " + priority + " (expected: Thread.MIN_PRIORITY <= priority <= Thread.MAX_PRIORITY)");
/*  79:    */     }
/*  80: 95 */     this.prefix = (poolName + '-' + poolId.incrementAndGet() + '-');
/*  81: 96 */     this.daemon = daemon;
/*  82: 97 */     this.priority = priority;
/*  83: 98 */     this.threadGroup = threadGroup;
/*  84:    */   }
/*  85:    */   
/*  86:    */   public DefaultThreadFactory(String poolName, boolean daemon, int priority)
/*  87:    */   {
/*  88:102 */     this(poolName, daemon, priority, System.getSecurityManager() == null ? 
/*  89:103 */       Thread.currentThread().getThreadGroup() : System.getSecurityManager().getThreadGroup());
/*  90:    */   }
/*  91:    */   
/*  92:    */   public Thread newThread(Runnable r)
/*  93:    */   {
/*  94:108 */     Thread t = newThread(FastThreadLocalRunnable.wrap(r), this.prefix + this.nextId.incrementAndGet());
/*  95:    */     try
/*  96:    */     {
/*  97:110 */       if (t.isDaemon() != this.daemon) {
/*  98:111 */         t.setDaemon(this.daemon);
/*  99:    */       }
/* 100:114 */       if (t.getPriority() != this.priority) {
/* 101:115 */         t.setPriority(this.priority);
/* 102:    */       }
/* 103:    */     }
/* 104:    */     catch (Exception localException) {}
/* 105:120 */     return t;
/* 106:    */   }
/* 107:    */   
/* 108:    */   protected Thread newThread(Runnable r, String name)
/* 109:    */   {
/* 110:124 */     return new FastThreadLocalThread(this.threadGroup, r, name);
/* 111:    */   }
/* 112:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.concurrent.DefaultThreadFactory
 * JD-Core Version:    0.7.0.1
 */