/*  1:   */ package io.netty.util;
/*  2:   */ 
/*  3:   */ import java.util.concurrent.atomic.AtomicLong;
/*  4:   */ 
/*  5:   */ public abstract class AbstractConstant<T extends AbstractConstant<T>>
/*  6:   */   implements Constant<T>
/*  7:   */ {
/*  8:25 */   private static final AtomicLong uniqueIdGenerator = new AtomicLong();
/*  9:   */   private final int id;
/* 10:   */   private final String name;
/* 11:   */   private final long uniquifier;
/* 12:   */   
/* 13:   */   protected AbstractConstant(int id, String name)
/* 14:   */   {
/* 15:34 */     this.id = id;
/* 16:35 */     this.name = name;
/* 17:36 */     this.uniquifier = uniqueIdGenerator.getAndIncrement();
/* 18:   */   }
/* 19:   */   
/* 20:   */   public final String name()
/* 21:   */   {
/* 22:41 */     return this.name;
/* 23:   */   }
/* 24:   */   
/* 25:   */   public final int id()
/* 26:   */   {
/* 27:46 */     return this.id;
/* 28:   */   }
/* 29:   */   
/* 30:   */   public final String toString()
/* 31:   */   {
/* 32:51 */     return name();
/* 33:   */   }
/* 34:   */   
/* 35:   */   public final int hashCode()
/* 36:   */   {
/* 37:56 */     return super.hashCode();
/* 38:   */   }
/* 39:   */   
/* 40:   */   public final boolean equals(Object obj)
/* 41:   */   {
/* 42:61 */     return super.equals(obj);
/* 43:   */   }
/* 44:   */   
/* 45:   */   public final int compareTo(T o)
/* 46:   */   {
/* 47:66 */     if (this == o) {
/* 48:67 */       return 0;
/* 49:   */     }
/* 50:71 */     AbstractConstant<T> other = o;
/* 51:   */     
/* 52:   */ 
/* 53:74 */     int returnCode = hashCode() - other.hashCode();
/* 54:75 */     if (returnCode != 0) {
/* 55:76 */       return returnCode;
/* 56:   */     }
/* 57:79 */     if (this.uniquifier < other.uniquifier) {
/* 58:80 */       return -1;
/* 59:   */     }
/* 60:82 */     if (this.uniquifier > other.uniquifier) {
/* 61:83 */       return 1;
/* 62:   */     }
/* 63:86 */     throw new Error("failed to compare two different constants");
/* 64:   */   }
/* 65:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.AbstractConstant
 * JD-Core Version:    0.7.0.1
 */