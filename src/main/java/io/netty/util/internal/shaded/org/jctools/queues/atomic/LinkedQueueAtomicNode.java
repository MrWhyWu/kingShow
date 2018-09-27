/*  1:   */ package io.netty.util.internal.shaded.org.jctools.queues.atomic;
/*  2:   */ 
/*  3:   */ import java.util.concurrent.atomic.AtomicReference;
/*  4:   */ 
/*  5:   */ public final class LinkedQueueAtomicNode<E>
/*  6:   */   extends AtomicReference<LinkedQueueAtomicNode<E>>
/*  7:   */ {
/*  8:   */   private static final long serialVersionUID = 2404266111789071508L;
/*  9:   */   private E value;
/* 10:   */   
/* 11:   */   LinkedQueueAtomicNode() {}
/* 12:   */   
/* 13:   */   LinkedQueueAtomicNode(E val)
/* 14:   */   {
/* 15:30 */     spValue(val);
/* 16:   */   }
/* 17:   */   
/* 18:   */   public E getAndNullValue()
/* 19:   */   {
/* 20:40 */     E temp = lpValue();
/* 21:41 */     spValue(null);
/* 22:42 */     return temp;
/* 23:   */   }
/* 24:   */   
/* 25:   */   public E lpValue()
/* 26:   */   {
/* 27:47 */     return this.value;
/* 28:   */   }
/* 29:   */   
/* 30:   */   public void spValue(E newValue)
/* 31:   */   {
/* 32:52 */     this.value = newValue;
/* 33:   */   }
/* 34:   */   
/* 35:   */   public void soNext(LinkedQueueAtomicNode<E> n)
/* 36:   */   {
/* 37:57 */     lazySet(n);
/* 38:   */   }
/* 39:   */   
/* 40:   */   public LinkedQueueAtomicNode<E> lvNext()
/* 41:   */   {
/* 42:62 */     return (LinkedQueueAtomicNode)get();
/* 43:   */   }
/* 44:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.atomic.LinkedQueueAtomicNode
 * JD-Core Version:    0.7.0.1
 */