/*  1:   */ package io.netty.util;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.ObjectUtil;
/*  4:   */ import io.netty.util.internal.SystemPropertyUtil;
/*  5:   */ import java.util.Locale;
/*  6:   */ 
/*  7:   */ public final class NettyRuntime
/*  8:   */ {
/*  9:   */   static class AvailableProcessorsHolder
/* 10:   */   {
/* 11:   */     private int availableProcessors;
/* 12:   */     
/* 13:   */     synchronized void setAvailableProcessors(int availableProcessors)
/* 14:   */     {
/* 15:44 */       ObjectUtil.checkPositive(availableProcessors, "availableProcessors");
/* 16:45 */       if (this.availableProcessors != 0)
/* 17:   */       {
/* 18:46 */         String message = String.format(Locale.ROOT, "availableProcessors is already set to [%d], rejecting [%d]", new Object[] {
/* 19:   */         
/* 20:   */ 
/* 21:49 */           Integer.valueOf(this.availableProcessors), 
/* 22:50 */           Integer.valueOf(availableProcessors) });
/* 23:51 */         throw new IllegalStateException(message);
/* 24:   */       }
/* 25:53 */       this.availableProcessors = availableProcessors;
/* 26:   */     }
/* 27:   */     
/* 28:   */     @SuppressForbidden(reason="to obtain default number of available processors")
/* 29:   */     synchronized int availableProcessors()
/* 30:   */     {
/* 31:65 */       if (this.availableProcessors == 0)
/* 32:   */       {
/* 33:67 */         int availableProcessors = SystemPropertyUtil.getInt("io.netty.availableProcessors", 
/* 34:   */         
/* 35:69 */           Runtime.getRuntime().availableProcessors());
/* 36:70 */         setAvailableProcessors(availableProcessors);
/* 37:   */       }
/* 38:72 */       return this.availableProcessors;
/* 39:   */     }
/* 40:   */   }
/* 41:   */   
/* 42:76 */   private static final AvailableProcessorsHolder holder = new AvailableProcessorsHolder();
/* 43:   */   
/* 44:   */   public static void setAvailableProcessors(int availableProcessors)
/* 45:   */   {
/* 46:87 */     holder.setAvailableProcessors(availableProcessors);
/* 47:   */   }
/* 48:   */   
/* 49:   */   public static int availableProcessors()
/* 50:   */   {
/* 51:98 */     return holder.availableProcessors();
/* 52:   */   }
/* 53:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.NettyRuntime
 * JD-Core Version:    0.7.0.1
 */