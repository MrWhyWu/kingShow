/*  1:   */ package io.netty.util.internal.shaded.org.jctools.queues.atomic;
/*  2:   */ 
/*  3:   */ import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
/*  4:   */ 
/*  5:   */ abstract class BaseLinkedAtomicQueueProducerNodeRef<E>
/*  6:   */   extends BaseLinkedAtomicQueuePad0<E>
/*  7:   */ {
/*  8:47 */   private static final AtomicReferenceFieldUpdater<BaseLinkedAtomicQueueProducerNodeRef, LinkedQueueAtomicNode> P_NODE_UPDATER = AtomicReferenceFieldUpdater.newUpdater(BaseLinkedAtomicQueueProducerNodeRef.class, LinkedQueueAtomicNode.class, "producerNode");
/*  9:   */   protected volatile LinkedQueueAtomicNode<E> producerNode;
/* 10:   */   
/* 11:   */   protected final void spProducerNode(LinkedQueueAtomicNode<E> newValue)
/* 12:   */   {
/* 13:52 */     P_NODE_UPDATER.lazySet(this, newValue);
/* 14:   */   }
/* 15:   */   
/* 16:   */   protected final LinkedQueueAtomicNode<E> lvProducerNode()
/* 17:   */   {
/* 18:57 */     return this.producerNode;
/* 19:   */   }
/* 20:   */   
/* 21:   */   protected final boolean casProducerNode(LinkedQueueAtomicNode<E> expect, LinkedQueueAtomicNode<E> newValue)
/* 22:   */   {
/* 23:62 */     return P_NODE_UPDATER.compareAndSet(this, expect, newValue);
/* 24:   */   }
/* 25:   */   
/* 26:   */   protected final LinkedQueueAtomicNode<E> lpProducerNode()
/* 27:   */   {
/* 28:66 */     return this.producerNode;
/* 29:   */   }
/* 30:   */   
/* 31:   */   protected final LinkedQueueAtomicNode<E> xchgProducerNode(LinkedQueueAtomicNode<E> newValue)
/* 32:   */   {
/* 33:70 */     return (LinkedQueueAtomicNode)P_NODE_UPDATER.getAndSet(this, newValue);
/* 34:   */   }
/* 35:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.atomic.BaseLinkedAtomicQueueProducerNodeRef
 * JD-Core Version:    0.7.0.1
 */