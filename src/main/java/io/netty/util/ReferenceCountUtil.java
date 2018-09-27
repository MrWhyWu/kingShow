/*   1:    */ package io.netty.util;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.StringUtil;
/*   4:    */ import io.netty.util.internal.logging.InternalLogger;
/*   5:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   6:    */ 
/*   7:    */ public final class ReferenceCountUtil
/*   8:    */ {
/*   9: 27 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(ReferenceCountUtil.class);
/*  10:    */   
/*  11:    */   static
/*  12:    */   {
/*  13: 30 */     ResourceLeakDetector.addExclusions(ReferenceCountUtil.class, new String[] { "touch" });
/*  14:    */   }
/*  15:    */   
/*  16:    */   public static <T> T retain(T msg)
/*  17:    */   {
/*  18: 39 */     if ((msg instanceof ReferenceCounted)) {
/*  19: 40 */       return ((ReferenceCounted)msg).retain();
/*  20:    */     }
/*  21: 42 */     return msg;
/*  22:    */   }
/*  23:    */   
/*  24:    */   public static <T> T retain(T msg, int increment)
/*  25:    */   {
/*  26: 51 */     if ((msg instanceof ReferenceCounted)) {
/*  27: 52 */       return ((ReferenceCounted)msg).retain(increment);
/*  28:    */     }
/*  29: 54 */     return msg;
/*  30:    */   }
/*  31:    */   
/*  32:    */   public static <T> T touch(T msg)
/*  33:    */   {
/*  34: 63 */     if ((msg instanceof ReferenceCounted)) {
/*  35: 64 */       return ((ReferenceCounted)msg).touch();
/*  36:    */     }
/*  37: 66 */     return msg;
/*  38:    */   }
/*  39:    */   
/*  40:    */   public static <T> T touch(T msg, Object hint)
/*  41:    */   {
/*  42: 76 */     if ((msg instanceof ReferenceCounted)) {
/*  43: 77 */       return ((ReferenceCounted)msg).touch(hint);
/*  44:    */     }
/*  45: 79 */     return msg;
/*  46:    */   }
/*  47:    */   
/*  48:    */   public static boolean release(Object msg)
/*  49:    */   {
/*  50: 87 */     if ((msg instanceof ReferenceCounted)) {
/*  51: 88 */       return ((ReferenceCounted)msg).release();
/*  52:    */     }
/*  53: 90 */     return false;
/*  54:    */   }
/*  55:    */   
/*  56:    */   public static boolean release(Object msg, int decrement)
/*  57:    */   {
/*  58: 98 */     if ((msg instanceof ReferenceCounted)) {
/*  59: 99 */       return ((ReferenceCounted)msg).release(decrement);
/*  60:    */     }
/*  61:101 */     return false;
/*  62:    */   }
/*  63:    */   
/*  64:    */   public static void safeRelease(Object msg)
/*  65:    */   {
/*  66:    */     try
/*  67:    */     {
/*  68:113 */       release(msg);
/*  69:    */     }
/*  70:    */     catch (Throwable t)
/*  71:    */     {
/*  72:115 */       logger.warn("Failed to release a message: {}", msg, t);
/*  73:    */     }
/*  74:    */   }
/*  75:    */   
/*  76:    */   public static void safeRelease(Object msg, int decrement)
/*  77:    */   {
/*  78:    */     try
/*  79:    */     {
/*  80:128 */       release(msg, decrement);
/*  81:    */     }
/*  82:    */     catch (Throwable t)
/*  83:    */     {
/*  84:130 */       if (logger.isWarnEnabled()) {
/*  85:131 */         logger.warn("Failed to release a message: {} (decrement: {})", new Object[] { msg, Integer.valueOf(decrement), t });
/*  86:    */       }
/*  87:    */     }
/*  88:    */   }
/*  89:    */   
/*  90:    */   @Deprecated
/*  91:    */   public static <T> T releaseLater(T msg)
/*  92:    */   {
/*  93:145 */     return releaseLater(msg, 1);
/*  94:    */   }
/*  95:    */   
/*  96:    */   @Deprecated
/*  97:    */   public static <T> T releaseLater(T msg, int decrement)
/*  98:    */   {
/*  99:157 */     if ((msg instanceof ReferenceCounted)) {
/* 100:158 */       ThreadDeathWatcher.watch(Thread.currentThread(), new ReleasingTask((ReferenceCounted)msg, decrement));
/* 101:    */     }
/* 102:160 */     return msg;
/* 103:    */   }
/* 104:    */   
/* 105:    */   public static int refCnt(Object msg)
/* 106:    */   {
/* 107:168 */     return (msg instanceof ReferenceCounted) ? ((ReferenceCounted)msg).refCnt() : -1;
/* 108:    */   }
/* 109:    */   
/* 110:    */   private static final class ReleasingTask
/* 111:    */     implements Runnable
/* 112:    */   {
/* 113:    */     private final ReferenceCounted obj;
/* 114:    */     private final int decrement;
/* 115:    */     
/* 116:    */     ReleasingTask(ReferenceCounted obj, int decrement)
/* 117:    */     {
/* 118:180 */       this.obj = obj;
/* 119:181 */       this.decrement = decrement;
/* 120:    */     }
/* 121:    */     
/* 122:    */     public void run()
/* 123:    */     {
/* 124:    */       try
/* 125:    */       {
/* 126:187 */         if (!this.obj.release(this.decrement)) {
/* 127:188 */           ReferenceCountUtil.logger.warn("Non-zero refCnt: {}", this);
/* 128:    */         } else {
/* 129:190 */           ReferenceCountUtil.logger.debug("Released: {}", this);
/* 130:    */         }
/* 131:    */       }
/* 132:    */       catch (Exception ex)
/* 133:    */       {
/* 134:193 */         ReferenceCountUtil.logger.warn("Failed to release an object: {}", this.obj, ex);
/* 135:    */       }
/* 136:    */     }
/* 137:    */     
/* 138:    */     public String toString()
/* 139:    */     {
/* 140:199 */       return StringUtil.simpleClassName(this.obj) + ".release(" + this.decrement + ") refCnt: " + this.obj.refCnt();
/* 141:    */     }
/* 142:    */   }
/* 143:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.ReferenceCountUtil
 * JD-Core Version:    0.7.0.1
 */