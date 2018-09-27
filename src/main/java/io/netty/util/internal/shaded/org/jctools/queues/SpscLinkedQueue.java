/*   1:    */ package io.netty.util.internal.shaded.org.jctools.queues;
/*   2:    */ 
/*   3:    */ public class SpscLinkedQueue<E>
/*   4:    */   extends BaseLinkedQueue<E>
/*   5:    */ {
/*   6:    */   public SpscLinkedQueue()
/*   7:    */   {
/*   8: 36 */     LinkedQueueNode<E> node = newNode();
/*   9: 37 */     spProducerNode(node);
/*  10: 38 */     spConsumerNode(node);
/*  11: 39 */     node.soNext(null);
/*  12:    */   }
/*  13:    */   
/*  14:    */   public boolean offer(E e)
/*  15:    */   {
/*  16: 60 */     if (null == e) {
/*  17: 62 */       throw new NullPointerException();
/*  18:    */     }
/*  19: 64 */     LinkedQueueNode<E> nextNode = newNode(e);
/*  20: 65 */     lpProducerNode().soNext(nextNode);
/*  21: 66 */     spProducerNode(nextNode);
/*  22: 67 */     return true;
/*  23:    */   }
/*  24:    */   
/*  25:    */   public E poll()
/*  26:    */   {
/*  27: 87 */     return relaxedPoll();
/*  28:    */   }
/*  29:    */   
/*  30:    */   public E peek()
/*  31:    */   {
/*  32: 93 */     return relaxedPeek();
/*  33:    */   }
/*  34:    */   
/*  35:    */   public int fill(MessagePassingQueue.Supplier<E> s)
/*  36:    */   {
/*  37: 99 */     long result = 0L;
/*  38:    */     do
/*  39:    */     {
/*  40:102 */       fill(s, 4096);
/*  41:103 */       result += 4096L;
/*  42:105 */     } while (result <= 2147479551L);
/*  43:106 */     return (int)result;
/*  44:    */   }
/*  45:    */   
/*  46:    */   public int fill(MessagePassingQueue.Supplier<E> s, int limit)
/*  47:    */   {
/*  48:112 */     if (limit == 0) {
/*  49:114 */       return 0;
/*  50:    */     }
/*  51:116 */     LinkedQueueNode<E> tail = newNode(s.get());
/*  52:117 */     LinkedQueueNode<E> head = tail;
/*  53:118 */     for (int i = 1; i < limit; i++)
/*  54:    */     {
/*  55:120 */       LinkedQueueNode<E> temp = newNode(s.get());
/*  56:121 */       tail.soNext(temp);
/*  57:122 */       tail = temp;
/*  58:    */     }
/*  59:124 */     LinkedQueueNode<E> oldPNode = lpProducerNode();
/*  60:125 */     oldPNode.soNext(head);
/*  61:126 */     spProducerNode(tail);
/*  62:127 */     return limit;
/*  63:    */   }
/*  64:    */   
/*  65:    */   public void fill(MessagePassingQueue.Supplier<E> s, MessagePassingQueue.WaitStrategy wait, MessagePassingQueue.ExitCondition exit)
/*  66:    */   {
/*  67:133 */     LinkedQueueNode<E> chaserNode = this.producerNode;
/*  68:134 */     while (exit.keepRunning()) {
/*  69:136 */       for (int i = 0; i < 4096; i++)
/*  70:    */       {
/*  71:138 */         LinkedQueueNode<E> nextNode = newNode(s.get());
/*  72:139 */         chaserNode.soNext(nextNode);
/*  73:140 */         chaserNode = nextNode;
/*  74:141 */         this.producerNode = chaserNode;
/*  75:    */       }
/*  76:    */     }
/*  77:    */   }
/*  78:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.SpscLinkedQueue
 * JD-Core Version:    0.7.0.1
 */