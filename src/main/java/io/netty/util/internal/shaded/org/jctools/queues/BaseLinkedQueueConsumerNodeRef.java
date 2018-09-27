/*   1:    */ package io.netty.util.internal.shaded.org.jctools.queues;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;
/*   4:    */ import java.lang.reflect.Field;
/*   5:    */ import sun.misc.Unsafe;
/*   6:    */ 
/*   7:    */ abstract class BaseLinkedQueueConsumerNodeRef<E>
/*   8:    */   extends BaseLinkedQueuePad1<E>
/*   9:    */ {
/*  10:    */   protected static final long C_NODE_OFFSET;
/*  11:    */   protected LinkedQueueNode<E> consumerNode;
/*  12:    */   
/*  13:    */   static
/*  14:    */   {
/*  15:    */     try
/*  16:    */     {
/*  17: 86 */       Field cNodeField = BaseLinkedQueueConsumerNodeRef.class.getDeclaredField("consumerNode");
/*  18: 87 */       C_NODE_OFFSET = UnsafeAccess.UNSAFE.objectFieldOffset(cNodeField);
/*  19:    */     }
/*  20:    */     catch (NoSuchFieldException e)
/*  21:    */     {
/*  22: 91 */       throw new RuntimeException(e);
/*  23:    */     }
/*  24:    */   }
/*  25:    */   
/*  26:    */   protected final void spConsumerNode(LinkedQueueNode<E> newValue)
/*  27:    */   {
/*  28: 99 */     this.consumerNode = newValue;
/*  29:    */   }
/*  30:    */   
/*  31:    */   protected final LinkedQueueNode<E> lvConsumerNode()
/*  32:    */   {
/*  33:105 */     return (LinkedQueueNode)UnsafeAccess.UNSAFE.getObjectVolatile(this, C_NODE_OFFSET);
/*  34:    */   }
/*  35:    */   
/*  36:    */   protected final LinkedQueueNode<E> lpConsumerNode()
/*  37:    */   {
/*  38:110 */     return this.consumerNode;
/*  39:    */   }
/*  40:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.BaseLinkedQueueConsumerNodeRef
 * JD-Core Version:    0.7.0.1
 */