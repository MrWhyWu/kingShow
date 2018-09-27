/*  1:   */ package io.netty.channel;
/*  2:   */ 
/*  3:   */ public final class WriteBufferWaterMark
/*  4:   */ {
/*  5:   */   private static final int DEFAULT_LOW_WATER_MARK = 32768;
/*  6:   */   private static final int DEFAULT_HIGH_WATER_MARK = 65536;
/*  7:36 */   public static final WriteBufferWaterMark DEFAULT = new WriteBufferWaterMark(32768, 65536, false);
/*  8:   */   private final int low;
/*  9:   */   private final int high;
/* 10:   */   
/* 11:   */   public WriteBufferWaterMark(int low, int high)
/* 12:   */   {
/* 13:49 */     this(low, high, true);
/* 14:   */   }
/* 15:   */   
/* 16:   */   WriteBufferWaterMark(int low, int high, boolean validate)
/* 17:   */   {
/* 18:56 */     if (validate)
/* 19:   */     {
/* 20:57 */       if (low < 0) {
/* 21:58 */         throw new IllegalArgumentException("write buffer's low water mark must be >= 0");
/* 22:   */       }
/* 23:60 */       if (high < low) {
/* 24:61 */         throw new IllegalArgumentException("write buffer's high water mark cannot be less than  low water mark (" + low + "): " + high);
/* 25:   */       }
/* 26:   */     }
/* 27:67 */     this.low = low;
/* 28:68 */     this.high = high;
/* 29:   */   }
/* 30:   */   
/* 31:   */   public int low()
/* 32:   */   {
/* 33:75 */     return this.low;
/* 34:   */   }
/* 35:   */   
/* 36:   */   public int high()
/* 37:   */   {
/* 38:82 */     return this.high;
/* 39:   */   }
/* 40:   */   
/* 41:   */   public String toString()
/* 42:   */   {
/* 43:92 */     StringBuilder builder = new StringBuilder(55).append("WriteBufferWaterMark(low: ").append(this.low).append(", high: ").append(this.high).append(")");
/* 44:93 */     return builder.toString();
/* 45:   */   }
/* 46:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.WriteBufferWaterMark
 * JD-Core Version:    0.7.0.1
 */