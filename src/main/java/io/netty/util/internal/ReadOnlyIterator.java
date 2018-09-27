/*  1:   */ package io.netty.util.internal;
/*  2:   */ 
/*  3:   */ import java.util.Iterator;
/*  4:   */ 
/*  5:   */ public final class ReadOnlyIterator<T>
/*  6:   */   implements Iterator<T>
/*  7:   */ {
/*  8:   */   private final Iterator<? extends T> iterator;
/*  9:   */   
/* 10:   */   public ReadOnlyIterator(Iterator<? extends T> iterator)
/* 11:   */   {
/* 12:25 */     if (iterator == null) {
/* 13:26 */       throw new NullPointerException("iterator");
/* 14:   */     }
/* 15:28 */     this.iterator = iterator;
/* 16:   */   }
/* 17:   */   
/* 18:   */   public boolean hasNext()
/* 19:   */   {
/* 20:33 */     return this.iterator.hasNext();
/* 21:   */   }
/* 22:   */   
/* 23:   */   public T next()
/* 24:   */   {
/* 25:38 */     return this.iterator.next();
/* 26:   */   }
/* 27:   */   
/* 28:   */   public void remove()
/* 29:   */   {
/* 30:43 */     throw new UnsupportedOperationException("read-only");
/* 31:   */   }
/* 32:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.ReadOnlyIterator
 * JD-Core Version:    0.7.0.1
 */