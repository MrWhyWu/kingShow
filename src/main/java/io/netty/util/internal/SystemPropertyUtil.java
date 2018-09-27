/*   1:    */ package io.netty.util.internal;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.logging.InternalLogger;
/*   4:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   5:    */ import java.security.AccessController;
/*   6:    */ import java.security.PrivilegedAction;
/*   7:    */ 
/*   8:    */ public final class SystemPropertyUtil
/*   9:    */ {
/*  10: 29 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(SystemPropertyUtil.class);
/*  11:    */   
/*  12:    */   public static boolean contains(String key)
/*  13:    */   {
/*  14: 36 */     return get(key) != null;
/*  15:    */   }
/*  16:    */   
/*  17:    */   public static String get(String key)
/*  18:    */   {
/*  19: 46 */     return get(key, null);
/*  20:    */   }
/*  21:    */   
/*  22:    */   public static String get(String key, String def)
/*  23:    */   {
/*  24: 59 */     if (key == null) {
/*  25: 60 */       throw new NullPointerException("key");
/*  26:    */     }
/*  27: 62 */     if (key.isEmpty()) {
/*  28: 63 */       throw new IllegalArgumentException("key must not be empty.");
/*  29:    */     }
/*  30: 66 */     String value = null;
/*  31:    */     try
/*  32:    */     {
/*  33: 68 */       if (System.getSecurityManager() == null) {
/*  34: 69 */         value = System.getProperty(key);
/*  35:    */       } else {
/*  36: 71 */         value = (String)AccessController.doPrivileged(new PrivilegedAction()
/*  37:    */         {
/*  38:    */           public String run()
/*  39:    */           {
/*  40: 74 */             return System.getProperty(this.val$key);
/*  41:    */           }
/*  42:    */         });
/*  43:    */       }
/*  44:    */     }
/*  45:    */     catch (SecurityException e)
/*  46:    */     {
/*  47: 79 */       logger.warn("Unable to retrieve a system property '{}'; default values will be used.", key, e);
/*  48:    */     }
/*  49: 82 */     if (value == null) {
/*  50: 83 */       return def;
/*  51:    */     }
/*  52: 86 */     return value;
/*  53:    */   }
/*  54:    */   
/*  55:    */   public static boolean getBoolean(String key, boolean def)
/*  56:    */   {
/*  57: 99 */     String value = get(key);
/*  58:100 */     if (value == null) {
/*  59:101 */       return def;
/*  60:    */     }
/*  61:104 */     value = value.trim().toLowerCase();
/*  62:105 */     if (value.isEmpty()) {
/*  63:106 */       return def;
/*  64:    */     }
/*  65:109 */     if (("true".equals(value)) || ("yes".equals(value)) || ("1".equals(value))) {
/*  66:110 */       return true;
/*  67:    */     }
/*  68:113 */     if (("false".equals(value)) || ("no".equals(value)) || ("0".equals(value))) {
/*  69:114 */       return false;
/*  70:    */     }
/*  71:117 */     logger.warn("Unable to parse the boolean system property '{}':{} - using the default value: {}", new Object[] { key, value, 
/*  72:    */     
/*  73:119 */       Boolean.valueOf(def) });
/*  74:    */     
/*  75:    */ 
/*  76:122 */     return def;
/*  77:    */   }
/*  78:    */   
/*  79:    */   public static int getInt(String key, int def)
/*  80:    */   {
/*  81:135 */     String value = get(key);
/*  82:136 */     if (value == null) {
/*  83:137 */       return def;
/*  84:    */     }
/*  85:140 */     value = value.trim();
/*  86:    */     try
/*  87:    */     {
/*  88:142 */       return Integer.parseInt(value);
/*  89:    */     }
/*  90:    */     catch (Exception localException)
/*  91:    */     {
/*  92:147 */       logger.warn("Unable to parse the integer system property '{}':{} - using the default value: {}", new Object[] { key, value, 
/*  93:    */       
/*  94:149 */         Integer.valueOf(def) });
/*  95:    */     }
/*  96:152 */     return def;
/*  97:    */   }
/*  98:    */   
/*  99:    */   public static long getLong(String key, long def)
/* 100:    */   {
/* 101:165 */     String value = get(key);
/* 102:166 */     if (value == null) {
/* 103:167 */       return def;
/* 104:    */     }
/* 105:170 */     value = value.trim();
/* 106:    */     try
/* 107:    */     {
/* 108:172 */       return Long.parseLong(value);
/* 109:    */     }
/* 110:    */     catch (Exception localException)
/* 111:    */     {
/* 112:177 */       logger.warn("Unable to parse the long integer system property '{}':{} - using the default value: {}", new Object[] { key, value, 
/* 113:    */       
/* 114:179 */         Long.valueOf(def) });
/* 115:    */     }
/* 116:182 */     return def;
/* 117:    */   }
/* 118:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.SystemPropertyUtil
 * JD-Core Version:    0.7.0.1
 */