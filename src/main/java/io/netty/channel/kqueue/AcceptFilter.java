/*  1:   */ package io.netty.channel.kqueue;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.ObjectUtil;
/*  4:   */ 
/*  5:   */ public final class AcceptFilter
/*  6:   */ {
/*  7:23 */   static final AcceptFilter PLATFORM_UNSUPPORTED = new AcceptFilter("", "");
/*  8:   */   private final String filterName;
/*  9:   */   private final String filterArgs;
/* 10:   */   
/* 11:   */   public AcceptFilter(String filterName, String filterArgs)
/* 12:   */   {
/* 13:28 */     this.filterName = ((String)ObjectUtil.checkNotNull(filterName, "filterName"));
/* 14:29 */     this.filterArgs = ((String)ObjectUtil.checkNotNull(filterArgs, "filterArgs"));
/* 15:   */   }
/* 16:   */   
/* 17:   */   public String filterName()
/* 18:   */   {
/* 19:33 */     return this.filterName;
/* 20:   */   }
/* 21:   */   
/* 22:   */   public String filterArgs()
/* 23:   */   {
/* 24:37 */     return this.filterArgs;
/* 25:   */   }
/* 26:   */   
/* 27:   */   public boolean equals(Object o)
/* 28:   */   {
/* 29:42 */     if (o == this) {
/* 30:43 */       return true;
/* 31:   */     }
/* 32:45 */     if (!(o instanceof AcceptFilter)) {
/* 33:46 */       return false;
/* 34:   */     }
/* 35:48 */     AcceptFilter rhs = (AcceptFilter)o;
/* 36:49 */     return (this.filterName.equals(rhs.filterName)) && (this.filterArgs.equals(rhs.filterArgs));
/* 37:   */   }
/* 38:   */   
/* 39:   */   public int hashCode()
/* 40:   */   {
/* 41:54 */     return 31 * (31 + this.filterName.hashCode()) + this.filterArgs.hashCode();
/* 42:   */   }
/* 43:   */   
/* 44:   */   public String toString()
/* 45:   */   {
/* 46:59 */     return this.filterName + ", " + this.filterArgs;
/* 47:   */   }
/* 48:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.kqueue.AcceptFilter
 * JD-Core Version:    0.7.0.1
 */