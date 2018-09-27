/*   1:    */ package io.netty.util.internal.shaded.org.jctools.queues.atomic;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.ExitCondition;
/*   4:    */ import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Supplier;
/*   5:    */ import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.WaitStrategy;
/*   6:    */ 
/*   7:    */ public class SpscLinkedAtomicQueue<E>
/*   8:    */   extends BaseLinkedAtomicQueue<E>
/*   9:    */ {
/*  10:    */   public SpscLinkedAtomicQueue()
/*  11:    */   {
/*  12: 48 */     LinkedQueueAtomicNode<E> node = newNode();
/*  13: 49 */     spProducerNode(node);
/*  14: 50 */     spConsumerNode(node);
/*  15:    */     
/*  16: 52 */     node.soNext(null);
/*  17:    */   }
/*  18:    */   
/*  19:    */   public boolean offer(E e)
/*  20:    */   {
/*  21: 72 */     if (null == e) {
/*  22: 73 */       throw new NullPointerException();
/*  23:    */     }
/*  24: 75 */     LinkedQueueAtomicNode<E> nextNode = newNode(e);
/*  25: 76 */     lpProducerNode().soNext(nextNode);
/*  26: 77 */     spProducerNode(nextNode);
/*  27: 78 */     return true;
/*  28:    */   }
/*  29:    */   
/*  30:    */   public E poll()
/*  31:    */   {
/*  32: 97 */     return relaxedPoll();
/*  33:    */   }
/*  34:    */   
/*  35:    */   public E peek()
/*  36:    */   {
/*  37:102 */     return relaxedPeek();
/*  38:    */   }
/*  39:    */   
/*  40:    */   public int fill(MessagePassingQueue.Supplier<E> s)
/*  41:    */   {
/*  42:108 */     long result = 0L;
/*  43:    */     do
/*  44:    */     {
/*  45:110 */       fill(s, 4096);
/*  46:111 */       result += 4096L;
/*  47:112 */     } while (result <= 2147479551L);
/*  48:113 */     return (int)result;
/*  49:    */   }
/*  50:    */   
/*  51:    */   public int fill(MessagePassingQueue.Supplier<E> s, int limit)
/*  52:    */   {
/*  53:118 */     if (limit == 0) {
/*  54:119 */       return 0;
/*  55:    */     }
/*  56:121 */     LinkedQueueAtomicNode<E> tail = newNode(s.get());
/*  57:122 */     LinkedQueueAtomicNode<E> head = tail;
/*  58:123 */     for (int i = 1; i < limit; i++)
/*  59:    */     {
/*  60:124 */       LinkedQueueAtomicNode<E> temp = newNode(s.get());
/*  61:125 */       tail.soNext(temp);
/*  62:126 */       tail = temp;
/*  63:    */     }
/*  64:128 */     LinkedQueueAtomicNode<E> oldPNode = lpProducerNode();
/*  65:129 */     oldPNode.soNext(head);
/*  66:130 */     spProducerNode(tail);
/*  67:131 */     return limit;
/*  68:    */   }
/*  69:    */   
/*  70:    */   public void fill(MessagePassingQueue.Supplier<E> s, MessagePassingQueue.WaitStrategy wait, MessagePassingQueue.ExitCondition exit)
/*  71:    */   {
/*  72:136 */     LinkedQueueAtomicNode<E> chaserNode = this.producerNode;
/*  73:137 */     while (exit.keepRunning()) {
/*  74:138 */       for (int i = 0; i < 4096; i++)
/*  75:    */       {
/*  76:139 */         LinkedQueueAtomicNode<E> nextNode = newNode(s.get());
/*  77:140 */         chaserNode.soNext(nextNode);
/*  78:141 */         chaserNode = nextNode;
/*  79:142 */         this.producerNode = chaserNode;
/*  80:    */       }
/*  81:    */     }
/*  82:    */   }
/*  83:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.atomic.SpscLinkedAtomicQueue
 * JD-Core Version:    0.7.0.1
 */