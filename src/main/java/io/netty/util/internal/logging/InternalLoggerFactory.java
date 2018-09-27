/*  1:   */ package io.netty.util.internal.logging;
/*  2:   */ 
/*  3:   */ public abstract class InternalLoggerFactory
/*  4:   */ {
/*  5:   */   private static volatile InternalLoggerFactory defaultFactory;
/*  6:   */   
/*  7:   */   private static InternalLoggerFactory newDefaultFactory(String name)
/*  8:   */   {
/*  9:   */     InternalLoggerFactory f;
/* 10:   */     try
/* 11:   */     {
/* 12:42 */       InternalLoggerFactory f = new Slf4JLoggerFactory(true);
/* 13:43 */       f.newInstance(name).debug("Using SLF4J as the default logging framework");
/* 14:   */     }
/* 15:   */     catch (Throwable t1)
/* 16:   */     {
/* 17:   */       try
/* 18:   */       {
/* 19:46 */         InternalLoggerFactory f = Log4JLoggerFactory.INSTANCE;
/* 20:47 */         f.newInstance(name).debug("Using Log4J as the default logging framework");
/* 21:   */       }
/* 22:   */       catch (Throwable t2)
/* 23:   */       {
/* 24:49 */         f = JdkLoggerFactory.INSTANCE;
/* 25:50 */         f.newInstance(name).debug("Using java.util.logging as the default logging framework");
/* 26:   */       }
/* 27:   */     }
/* 28:53 */     return f;
/* 29:   */   }
/* 30:   */   
/* 31:   */   public static InternalLoggerFactory getDefaultFactory()
/* 32:   */   {
/* 33:61 */     if (defaultFactory == null) {
/* 34:62 */       defaultFactory = newDefaultFactory(InternalLoggerFactory.class.getName());
/* 35:   */     }
/* 36:64 */     return defaultFactory;
/* 37:   */   }
/* 38:   */   
/* 39:   */   public static void setDefaultFactory(InternalLoggerFactory defaultFactory)
/* 40:   */   {
/* 41:71 */     if (defaultFactory == null) {
/* 42:72 */       throw new NullPointerException("defaultFactory");
/* 43:   */     }
/* 44:74 */     defaultFactory = defaultFactory;
/* 45:   */   }
/* 46:   */   
/* 47:   */   public static InternalLogger getInstance(Class<?> clazz)
/* 48:   */   {
/* 49:81 */     return getInstance(clazz.getName());
/* 50:   */   }
/* 51:   */   
/* 52:   */   public static InternalLogger getInstance(String name)
/* 53:   */   {
/* 54:88 */     return getDefaultFactory().newInstance(name);
/* 55:   */   }
/* 56:   */   
/* 57:   */   protected abstract InternalLogger newInstance(String paramString);
/* 58:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.logging.InternalLoggerFactory
 * JD-Core Version:    0.7.0.1
 */