/*  1:   */ package io.netty.channel.group;
/*  2:   */ 
/*  3:   */ import java.util.Iterator;
/*  4:   */ import java.util.NoSuchElementException;
/*  5:   */ 
/*  6:   */ final class CombinedIterator<E>
/*  7:   */   implements Iterator<E>
/*  8:   */ {
/*  9:   */   private final Iterator<E> i1;
/* 10:   */   private final Iterator<E> i2;
/* 11:   */   private Iterator<E> currentIterator;
/* 12:   */   
/* 13:   */   CombinedIterator(Iterator<E> i1, Iterator<E> i2)
/* 14:   */   {
/* 15:30 */     if (i1 == null) {
/* 16:31 */       throw new NullPointerException("i1");
/* 17:   */     }
/* 18:33 */     if (i2 == null) {
/* 19:34 */       throw new NullPointerException("i2");
/* 20:   */     }
/* 21:36 */     this.i1 = i1;
/* 22:37 */     this.i2 = i2;
/* 23:38 */     this.currentIterator = i1;
/* 24:   */   }
/* 25:   */   
/* 26:   */   public boolean hasNext()
/* 27:   */   {
/* 28:   */     for (;;)
/* 29:   */     {
/* 30:44 */       if (this.currentIterator.hasNext()) {
/* 31:45 */         return true;
/* 32:   */       }
/* 33:48 */       if (this.currentIterator != this.i1) {
/* 34:   */         break;
/* 35:   */       }
/* 36:49 */       this.currentIterator = this.i2;
/* 37:   */     }
/* 38:51 */     return false;
/* 39:   */   }
/* 40:   */   
/* 41:   */   public E next()
/* 42:   */   {
/* 43:   */     for (;;)
/* 44:   */     {
/* 45:   */       try
/* 46:   */       {
/* 47:60 */         return this.currentIterator.next();
/* 48:   */       }
/* 49:   */       catch (NoSuchElementException e)
/* 50:   */       {
/* 51:62 */         if (this.currentIterator == this.i1) {
/* 52:63 */           this.currentIterator = this.i2;
/* 53:   */         } else {
/* 54:65 */           throw e;
/* 55:   */         }
/* 56:   */       }
/* 57:   */     }
/* 58:   */   }
/* 59:   */   
/* 60:   */   public void remove()
/* 61:   */   {
/* 62:73 */     this.currentIterator.remove();
/* 63:   */   }
/* 64:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.group.CombinedIterator
 * JD-Core Version:    0.7.0.1
 */