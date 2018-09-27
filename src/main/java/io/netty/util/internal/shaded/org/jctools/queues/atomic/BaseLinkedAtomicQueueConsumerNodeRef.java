/*   1:    */ package io.netty.util.internal.shaded.org.jctools.queues.atomic;
/*   2:    */ 
/*   3:    */ import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
/*   4:    */ 
/*   5:    */ abstract class BaseLinkedAtomicQueueConsumerNodeRef<E>
/*   6:    */   extends BaseLinkedAtomicQueuePad1<E>
/*   7:    */ {
/*   8: 91 */   private static final AtomicReferenceFieldUpdater<BaseLinkedAtomicQueueConsumerNodeRef, LinkedQueueAtomicNode> C_NODE_UPDATER = AtomicReferenceFieldUpdater.newUpdater(BaseLinkedAtomicQueueConsumerNodeRef.class, LinkedQueueAtomicNode.class, "consumerNode");
/*   9:    */   protected volatile LinkedQueueAtomicNode<E> consumerNode;
/*  10:    */   
/*  11:    */   protected final void spConsumerNode(LinkedQueueAtomicNode<E> newValue)
/*  12:    */   {
/*  13: 96 */     C_NODE_UPDATER.lazySet(this, newValue);
/*  14:    */   }
/*  15:    */   
/*  16:    */   protected final LinkedQueueAtomicNode<E> lvConsumerNode()
/*  17:    */   {
/*  18:101 */     return this.consumerNode;
/*  19:    */   }
/*  20:    */   
/*  21:    */   protected final LinkedQueueAtomicNode<E> lpConsumerNode()
/*  22:    */   {
/*  23:105 */     return this.consumerNode;
/*  24:    */   }
/*  25:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.atomic.BaseLinkedAtomicQueueConsumerNodeRef
 * JD-Core Version:    0.7.0.1
 */