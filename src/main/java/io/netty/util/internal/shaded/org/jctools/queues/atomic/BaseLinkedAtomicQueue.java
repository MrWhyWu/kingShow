/*   1:    */ package io.netty.util.internal.shaded.org.jctools.queues.atomic;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer;
/*   4:    */ import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.ExitCondition;
/*   5:    */ import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.WaitStrategy;
/*   6:    */ import java.util.Iterator;
/*   7:    */ 
/*   8:    */ abstract class BaseLinkedAtomicQueue<E>
/*   9:    */   extends BaseLinkedAtomicQueuePad2<E>
/*  10:    */ {
/*  11:    */   public final Iterator<E> iterator()
/*  12:    */   {
/*  13:134 */     throw new UnsupportedOperationException();
/*  14:    */   }
/*  15:    */   
/*  16:    */   public String toString()
/*  17:    */   {
/*  18:139 */     return getClass().getName();
/*  19:    */   }
/*  20:    */   
/*  21:    */   protected final LinkedQueueAtomicNode<E> newNode()
/*  22:    */   {
/*  23:143 */     return new LinkedQueueAtomicNode();
/*  24:    */   }
/*  25:    */   
/*  26:    */   protected final LinkedQueueAtomicNode<E> newNode(E e)
/*  27:    */   {
/*  28:147 */     return new LinkedQueueAtomicNode(e);
/*  29:    */   }
/*  30:    */   
/*  31:    */   public final int size()
/*  32:    */   {
/*  33:164 */     LinkedQueueAtomicNode<E> chaserNode = lvConsumerNode();
/*  34:165 */     LinkedQueueAtomicNode<E> producerNode = lvProducerNode();
/*  35:166 */     int size = 0;
/*  36:168 */     while ((chaserNode != producerNode) && (chaserNode != null) && (size < 2147483647))
/*  37:    */     {
/*  38:173 */       LinkedQueueAtomicNode<E> next = chaserNode.lvNext();
/*  39:175 */       if (next == chaserNode) {
/*  40:176 */         return size;
/*  41:    */       }
/*  42:178 */       chaserNode = next;
/*  43:179 */       size++;
/*  44:    */     }
/*  45:181 */     return size;
/*  46:    */   }
/*  47:    */   
/*  48:    */   public final boolean isEmpty()
/*  49:    */   {
/*  50:196 */     return lvConsumerNode() == lvProducerNode();
/*  51:    */   }
/*  52:    */   
/*  53:    */   protected E getSingleConsumerNodeValue(LinkedQueueAtomicNode<E> currConsumerNode, LinkedQueueAtomicNode<E> nextNode)
/*  54:    */   {
/*  55:201 */     E nextValue = nextNode.getAndNullValue();
/*  56:    */     
/*  57:    */ 
/*  58:    */ 
/*  59:205 */     currConsumerNode.soNext(currConsumerNode);
/*  60:206 */     spConsumerNode(nextNode);
/*  61:    */     
/*  62:208 */     return nextValue;
/*  63:    */   }
/*  64:    */   
/*  65:    */   public E relaxedPoll()
/*  66:    */   {
/*  67:213 */     LinkedQueueAtomicNode<E> currConsumerNode = lpConsumerNode();
/*  68:214 */     LinkedQueueAtomicNode<E> nextNode = currConsumerNode.lvNext();
/*  69:215 */     if (nextNode != null) {
/*  70:216 */       return getSingleConsumerNodeValue(currConsumerNode, nextNode);
/*  71:    */     }
/*  72:218 */     return null;
/*  73:    */   }
/*  74:    */   
/*  75:    */   public E relaxedPeek()
/*  76:    */   {
/*  77:223 */     LinkedQueueAtomicNode<E> nextNode = lpConsumerNode().lvNext();
/*  78:224 */     if (nextNode != null) {
/*  79:225 */       return nextNode.lpValue();
/*  80:    */     }
/*  81:227 */     return null;
/*  82:    */   }
/*  83:    */   
/*  84:    */   public boolean relaxedOffer(E e)
/*  85:    */   {
/*  86:232 */     return offer(e);
/*  87:    */   }
/*  88:    */   
/*  89:    */   public int drain(MessagePassingQueue.Consumer<E> c)
/*  90:    */   {
/*  91:238 */     long result = 0L;
/*  92:    */     int drained;
/*  93:    */     do
/*  94:    */     {
/*  95:241 */       drained = drain(c, 4096);
/*  96:242 */       result += drained;
/*  97:243 */     } while ((drained == 4096) && (result <= 2147479551L));
/*  98:244 */     return (int)result;
/*  99:    */   }
/* 100:    */   
/* 101:    */   public int drain(MessagePassingQueue.Consumer<E> c, int limit)
/* 102:    */   {
/* 103:249 */     LinkedQueueAtomicNode<E> chaserNode = this.consumerNode;
/* 104:250 */     for (int i = 0; i < limit; i++)
/* 105:    */     {
/* 106:251 */       LinkedQueueAtomicNode<E> nextNode = chaserNode.lvNext();
/* 107:252 */       if (nextNode == null) {
/* 108:253 */         return i;
/* 109:    */       }
/* 110:256 */       E nextValue = getSingleConsumerNodeValue(chaserNode, nextNode);
/* 111:257 */       chaserNode = nextNode;
/* 112:258 */       c.accept(nextValue);
/* 113:    */     }
/* 114:260 */     return limit;
/* 115:    */   }
/* 116:    */   
/* 117:    */   public void drain(MessagePassingQueue.Consumer<E> c, MessagePassingQueue.WaitStrategy wait, MessagePassingQueue.ExitCondition exit)
/* 118:    */   {
/* 119:265 */     LinkedQueueAtomicNode<E> chaserNode = this.consumerNode;
/* 120:266 */     int idleCounter = 0;
/* 121:267 */     while (exit.keepRunning()) {
/* 122:268 */       for (int i = 0; i < 4096; i++)
/* 123:    */       {
/* 124:269 */         LinkedQueueAtomicNode<E> nextNode = chaserNode.lvNext();
/* 125:270 */         if (nextNode == null)
/* 126:    */         {
/* 127:271 */           idleCounter = wait.idle(idleCounter);
/* 128:    */         }
/* 129:    */         else
/* 130:    */         {
/* 131:274 */           idleCounter = 0;
/* 132:    */           
/* 133:276 */           E nextValue = getSingleConsumerNodeValue(chaserNode, nextNode);
/* 134:277 */           chaserNode = nextNode;
/* 135:278 */           c.accept(nextValue);
/* 136:    */         }
/* 137:    */       }
/* 138:    */     }
/* 139:    */   }
/* 140:    */   
/* 141:    */   public int capacity()
/* 142:    */   {
/* 143:285 */     return -1;
/* 144:    */   }
/* 145:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.atomic.BaseLinkedAtomicQueue
 * JD-Core Version:    0.7.0.1
 */