/*   1:    */ package io.netty.util.internal.shaded.org.jctools.queues;
/*   2:    */ 
/*   3:    */ import java.util.Iterator;
/*   4:    */ 
/*   5:    */ abstract class BaseLinkedQueue<E>
/*   6:    */   extends BaseLinkedQueuePad2<E>
/*   7:    */ {
/*   8:    */   public final Iterator<E> iterator()
/*   9:    */   {
/*  10:133 */     throw new UnsupportedOperationException();
/*  11:    */   }
/*  12:    */   
/*  13:    */   public String toString()
/*  14:    */   {
/*  15:139 */     return getClass().getName();
/*  16:    */   }
/*  17:    */   
/*  18:    */   protected final LinkedQueueNode<E> newNode()
/*  19:    */   {
/*  20:144 */     return new LinkedQueueNode();
/*  21:    */   }
/*  22:    */   
/*  23:    */   protected final LinkedQueueNode<E> newNode(E e)
/*  24:    */   {
/*  25:149 */     return new LinkedQueueNode(e);
/*  26:    */   }
/*  27:    */   
/*  28:    */   public final int size()
/*  29:    */   {
/*  30:167 */     LinkedQueueNode<E> chaserNode = lvConsumerNode();
/*  31:168 */     LinkedQueueNode<E> producerNode = lvProducerNode();
/*  32:169 */     int size = 0;
/*  33:171 */     while ((chaserNode != producerNode) && (chaserNode != null) && (size < 2147483647))
/*  34:    */     {
/*  35:176 */       LinkedQueueNode<E> next = chaserNode.lvNext();
/*  36:178 */       if (next == chaserNode) {
/*  37:180 */         return size;
/*  38:    */       }
/*  39:182 */       chaserNode = next;
/*  40:183 */       size++;
/*  41:    */     }
/*  42:185 */     return size;
/*  43:    */   }
/*  44:    */   
/*  45:    */   public final boolean isEmpty()
/*  46:    */   {
/*  47:201 */     return lvConsumerNode() == lvProducerNode();
/*  48:    */   }
/*  49:    */   
/*  50:    */   protected E getSingleConsumerNodeValue(LinkedQueueNode<E> currConsumerNode, LinkedQueueNode<E> nextNode)
/*  51:    */   {
/*  52:207 */     E nextValue = nextNode.getAndNullValue();
/*  53:    */     
/*  54:    */ 
/*  55:    */ 
/*  56:    */ 
/*  57:212 */     currConsumerNode.soNext(currConsumerNode);
/*  58:213 */     spConsumerNode(nextNode);
/*  59:    */     
/*  60:215 */     return nextValue;
/*  61:    */   }
/*  62:    */   
/*  63:    */   public E relaxedPoll()
/*  64:    */   {
/*  65:221 */     LinkedQueueNode<E> currConsumerNode = lpConsumerNode();
/*  66:222 */     LinkedQueueNode<E> nextNode = currConsumerNode.lvNext();
/*  67:223 */     if (nextNode != null) {
/*  68:225 */       return getSingleConsumerNodeValue(currConsumerNode, nextNode);
/*  69:    */     }
/*  70:227 */     return null;
/*  71:    */   }
/*  72:    */   
/*  73:    */   public E relaxedPeek()
/*  74:    */   {
/*  75:233 */     LinkedQueueNode<E> nextNode = lpConsumerNode().lvNext();
/*  76:234 */     if (nextNode != null) {
/*  77:236 */       return nextNode.lpValue();
/*  78:    */     }
/*  79:238 */     return null;
/*  80:    */   }
/*  81:    */   
/*  82:    */   public boolean relaxedOffer(E e)
/*  83:    */   {
/*  84:244 */     return offer(e);
/*  85:    */   }
/*  86:    */   
/*  87:    */   public int drain(MessagePassingQueue.Consumer<E> c)
/*  88:    */   {
/*  89:250 */     long result = 0L;
/*  90:    */     int drained;
/*  91:    */     do
/*  92:    */     {
/*  93:254 */       drained = drain(c, 4096);
/*  94:255 */       result += drained;
/*  95:257 */     } while ((drained == 4096) && (result <= 2147479551L));
/*  96:258 */     return (int)result;
/*  97:    */   }
/*  98:    */   
/*  99:    */   public int drain(MessagePassingQueue.Consumer<E> c, int limit)
/* 100:    */   {
/* 101:264 */     LinkedQueueNode<E> chaserNode = this.consumerNode;
/* 102:265 */     for (int i = 0; i < limit; i++)
/* 103:    */     {
/* 104:267 */       LinkedQueueNode<E> nextNode = chaserNode.lvNext();
/* 105:269 */       if (nextNode == null) {
/* 106:271 */         return i;
/* 107:    */       }
/* 108:274 */       E nextValue = getSingleConsumerNodeValue(chaserNode, nextNode);
/* 109:275 */       chaserNode = nextNode;
/* 110:276 */       c.accept(nextValue);
/* 111:    */     }
/* 112:278 */     return limit;
/* 113:    */   }
/* 114:    */   
/* 115:    */   public void drain(MessagePassingQueue.Consumer<E> c, MessagePassingQueue.WaitStrategy wait, MessagePassingQueue.ExitCondition exit)
/* 116:    */   {
/* 117:284 */     LinkedQueueNode<E> chaserNode = this.consumerNode;
/* 118:285 */     int idleCounter = 0;
/* 119:286 */     while (exit.keepRunning()) {
/* 120:288 */       for (int i = 0; i < 4096; i++)
/* 121:    */       {
/* 122:290 */         LinkedQueueNode<E> nextNode = chaserNode.lvNext();
/* 123:291 */         if (nextNode == null)
/* 124:    */         {
/* 125:293 */           idleCounter = wait.idle(idleCounter);
/* 126:    */         }
/* 127:    */         else
/* 128:    */         {
/* 129:297 */           idleCounter = 0;
/* 130:    */           
/* 131:299 */           E nextValue = getSingleConsumerNodeValue(chaserNode, nextNode);
/* 132:300 */           chaserNode = nextNode;
/* 133:301 */           c.accept(nextValue);
/* 134:    */         }
/* 135:    */       }
/* 136:    */     }
/* 137:    */   }
/* 138:    */   
/* 139:    */   public int capacity()
/* 140:    */   {
/* 141:309 */     return -1;
/* 142:    */   }
/* 143:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.BaseLinkedQueue
 * JD-Core Version:    0.7.0.1
 */