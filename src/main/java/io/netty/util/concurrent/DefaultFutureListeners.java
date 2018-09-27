/*  1:   */ package io.netty.util.concurrent;
/*  2:   */ 
/*  3:   */ import java.util.Arrays;
/*  4:   */ 
/*  5:   */ final class DefaultFutureListeners
/*  6:   */ {
/*  7:   */   private GenericFutureListener<? extends Future<?>>[] listeners;
/*  8:   */   private int size;
/*  9:   */   private int progressiveSize;
/* 10:   */   
/* 11:   */   DefaultFutureListeners(GenericFutureListener<? extends Future<?>> first, GenericFutureListener<? extends Future<?>> second)
/* 12:   */   {
/* 13:29 */     this.listeners = new GenericFutureListener[2];
/* 14:30 */     this.listeners[0] = first;
/* 15:31 */     this.listeners[1] = second;
/* 16:32 */     this.size = 2;
/* 17:33 */     if ((first instanceof GenericProgressiveFutureListener)) {
/* 18:34 */       this.progressiveSize += 1;
/* 19:   */     }
/* 20:36 */     if ((second instanceof GenericProgressiveFutureListener)) {
/* 21:37 */       this.progressiveSize += 1;
/* 22:   */     }
/* 23:   */   }
/* 24:   */   
/* 25:   */   public void add(GenericFutureListener<? extends Future<?>> l)
/* 26:   */   {
/* 27:42 */     GenericFutureListener<? extends Future<?>>[] listeners = this.listeners;
/* 28:43 */     int size = this.size;
/* 29:44 */     if (size == listeners.length) {
/* 30:45 */       this.listeners = (listeners = (GenericFutureListener[])Arrays.copyOf(listeners, size << 1));
/* 31:   */     }
/* 32:47 */     listeners[size] = l;
/* 33:48 */     this.size = (size + 1);
/* 34:50 */     if ((l instanceof GenericProgressiveFutureListener)) {
/* 35:51 */       this.progressiveSize += 1;
/* 36:   */     }
/* 37:   */   }
/* 38:   */   
/* 39:   */   public void remove(GenericFutureListener<? extends Future<?>> l)
/* 40:   */   {
/* 41:56 */     GenericFutureListener<? extends Future<?>>[] listeners = this.listeners;
/* 42:57 */     int size = this.size;
/* 43:58 */     for (int i = 0; i < size; i++) {
/* 44:59 */       if (listeners[i] == l)
/* 45:   */       {
/* 46:60 */         int listenersToMove = size - i - 1;
/* 47:61 */         if (listenersToMove > 0) {
/* 48:62 */           System.arraycopy(listeners, i + 1, listeners, i, listenersToMove);
/* 49:   */         }
/* 50:64 */         listeners[(--size)] = null;
/* 51:65 */         this.size = size;
/* 52:67 */         if ((l instanceof GenericProgressiveFutureListener)) {
/* 53:68 */           this.progressiveSize -= 1;
/* 54:   */         }
/* 55:70 */         return;
/* 56:   */       }
/* 57:   */     }
/* 58:   */   }
/* 59:   */   
/* 60:   */   public GenericFutureListener<? extends Future<?>>[] listeners()
/* 61:   */   {
/* 62:76 */     return this.listeners;
/* 63:   */   }
/* 64:   */   
/* 65:   */   public int size()
/* 66:   */   {
/* 67:80 */     return this.size;
/* 68:   */   }
/* 69:   */   
/* 70:   */   public int progressiveSize()
/* 71:   */   {
/* 72:84 */     return this.progressiveSize;
/* 73:   */   }
/* 74:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.concurrent.DefaultFutureListeners
 * JD-Core Version:    0.7.0.1
 */