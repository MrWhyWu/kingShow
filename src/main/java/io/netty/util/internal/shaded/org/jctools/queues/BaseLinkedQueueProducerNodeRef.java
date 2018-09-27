/*  1:   */ package io.netty.util.internal.shaded.org.jctools.queues;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;
/*  4:   */ import java.lang.reflect.Field;
/*  5:   */ import sun.misc.Unsafe;
/*  6:   */ 
/*  7:   */ abstract class BaseLinkedQueueProducerNodeRef<E>
/*  8:   */   extends BaseLinkedQueuePad0<E>
/*  9:   */ {
/* 10:   */   protected static final long P_NODE_OFFSET;
/* 11:   */   protected LinkedQueueNode<E> producerNode;
/* 12:   */   
/* 13:   */   static
/* 14:   */   {
/* 15:   */     try
/* 16:   */     {
/* 17:37 */       Field pNodeField = BaseLinkedQueueProducerNodeRef.class.getDeclaredField("producerNode");
/* 18:38 */       P_NODE_OFFSET = UnsafeAccess.UNSAFE.objectFieldOffset(pNodeField);
/* 19:   */     }
/* 20:   */     catch (NoSuchFieldException e)
/* 21:   */     {
/* 22:42 */       throw new RuntimeException(e);
/* 23:   */     }
/* 24:   */   }
/* 25:   */   
/* 26:   */   protected final void spProducerNode(LinkedQueueNode<E> newValue)
/* 27:   */   {
/* 28:50 */     this.producerNode = newValue;
/* 29:   */   }
/* 30:   */   
/* 31:   */   protected final LinkedQueueNode<E> lvProducerNode()
/* 32:   */   {
/* 33:56 */     return (LinkedQueueNode)UnsafeAccess.UNSAFE.getObjectVolatile(this, P_NODE_OFFSET);
/* 34:   */   }
/* 35:   */   
/* 36:   */   protected final boolean casProducerNode(LinkedQueueNode<E> expect, LinkedQueueNode<E> newValue)
/* 37:   */   {
/* 38:62 */     return UnsafeAccess.UNSAFE.compareAndSwapObject(this, P_NODE_OFFSET, expect, newValue);
/* 39:   */   }
/* 40:   */   
/* 41:   */   protected final LinkedQueueNode<E> lpProducerNode()
/* 42:   */   {
/* 43:67 */     return this.producerNode;
/* 44:   */   }
/* 45:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.BaseLinkedQueueProducerNodeRef
 * JD-Core Version:    0.7.0.1
 */