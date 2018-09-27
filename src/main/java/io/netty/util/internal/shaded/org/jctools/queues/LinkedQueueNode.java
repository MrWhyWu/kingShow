/*  1:   */ package io.netty.util.internal.shaded.org.jctools.queues;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;
/*  4:   */ import sun.misc.Unsafe;
/*  5:   */ 
/*  6:   */ final class LinkedQueueNode<E>
/*  7:   */ {
/*  8:   */   private static final long NEXT_OFFSET;
/*  9:   */   private E value;
/* 10:   */   private volatile LinkedQueueNode<E> next;
/* 11:   */   
/* 12:   */   static
/* 13:   */   {
/* 14:   */     try
/* 15:   */     {
/* 16:26 */       NEXT_OFFSET = UnsafeAccess.UNSAFE.objectFieldOffset(LinkedQueueNode.class.getDeclaredField("next"));
/* 17:   */     }
/* 18:   */     catch (NoSuchFieldException e)
/* 19:   */     {
/* 20:30 */       throw new RuntimeException(e);
/* 21:   */     }
/* 22:   */   }
/* 23:   */   
/* 24:   */   LinkedQueueNode()
/* 25:   */   {
/* 26:39 */     this(null);
/* 27:   */   }
/* 28:   */   
/* 29:   */   LinkedQueueNode(E val)
/* 30:   */   {
/* 31:44 */     spValue(val);
/* 32:   */   }
/* 33:   */   
/* 34:   */   public E getAndNullValue()
/* 35:   */   {
/* 36:54 */     E temp = lpValue();
/* 37:55 */     spValue(null);
/* 38:56 */     return temp;
/* 39:   */   }
/* 40:   */   
/* 41:   */   public E lpValue()
/* 42:   */   {
/* 43:61 */     return this.value;
/* 44:   */   }
/* 45:   */   
/* 46:   */   public void spValue(E newValue)
/* 47:   */   {
/* 48:66 */     this.value = newValue;
/* 49:   */   }
/* 50:   */   
/* 51:   */   public void soNext(LinkedQueueNode<E> n)
/* 52:   */   {
/* 53:71 */     UnsafeAccess.UNSAFE.putOrderedObject(this, NEXT_OFFSET, n);
/* 54:   */   }
/* 55:   */   
/* 56:   */   public LinkedQueueNode<E> lvNext()
/* 57:   */   {
/* 58:76 */     return this.next;
/* 59:   */   }
/* 60:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.LinkedQueueNode
 * JD-Core Version:    0.7.0.1
 */