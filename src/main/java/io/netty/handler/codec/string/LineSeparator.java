/*  1:   */ package io.netty.handler.codec.string;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBufUtil;
/*  4:   */ import io.netty.util.CharsetUtil;
/*  5:   */ import io.netty.util.internal.ObjectUtil;
/*  6:   */ import io.netty.util.internal.StringUtil;
/*  7:   */ 
/*  8:   */ public final class LineSeparator
/*  9:   */ {
/* 10:31 */   public static final LineSeparator DEFAULT = new LineSeparator(StringUtil.NEWLINE);
/* 11:36 */   public static final LineSeparator UNIX = new LineSeparator("\n");
/* 12:41 */   public static final LineSeparator WINDOWS = new LineSeparator("\r\n");
/* 13:   */   private final String value;
/* 14:   */   
/* 15:   */   public LineSeparator(String lineSeparator)
/* 16:   */   {
/* 17:49 */     this.value = ((String)ObjectUtil.checkNotNull(lineSeparator, "lineSeparator"));
/* 18:   */   }
/* 19:   */   
/* 20:   */   public String value()
/* 21:   */   {
/* 22:56 */     return this.value;
/* 23:   */   }
/* 24:   */   
/* 25:   */   public boolean equals(Object o)
/* 26:   */   {
/* 27:61 */     if (this == o) {
/* 28:62 */       return true;
/* 29:   */     }
/* 30:64 */     if (!(o instanceof LineSeparator)) {
/* 31:65 */       return false;
/* 32:   */     }
/* 33:67 */     LineSeparator that = (LineSeparator)o;
/* 34:68 */     return that.value == null ? true : this.value != null ? this.value.equals(that.value) : false;
/* 35:   */   }
/* 36:   */   
/* 37:   */   public int hashCode()
/* 38:   */   {
/* 39:73 */     return this.value != null ? this.value.hashCode() : 0;
/* 40:   */   }
/* 41:   */   
/* 42:   */   public String toString()
/* 43:   */   {
/* 44:81 */     return ByteBufUtil.hexDump(this.value.getBytes(CharsetUtil.UTF_8));
/* 45:   */   }
/* 46:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.string.LineSeparator
 * JD-Core Version:    0.7.0.1
 */