/*   1:    */ package io.protostuff;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import java.util.ArrayDeque;
/*   5:    */ import java.util.ArrayList;
/*   6:    */ import java.util.Collection;
/*   7:    */ import java.util.HashSet;
/*   8:    */ import java.util.LinkedHashSet;
/*   9:    */ import java.util.LinkedList;
/*  10:    */ import java.util.PriorityQueue;
/*  11:    */ import java.util.Stack;
/*  12:    */ import java.util.TreeSet;
/*  13:    */ import java.util.Vector;
/*  14:    */ import java.util.concurrent.ArrayBlockingQueue;
/*  15:    */ import java.util.concurrent.ConcurrentLinkedDeque;
/*  16:    */ import java.util.concurrent.ConcurrentLinkedQueue;
/*  17:    */ import java.util.concurrent.ConcurrentSkipListSet;
/*  18:    */ import java.util.concurrent.CopyOnWriteArrayList;
/*  19:    */ import java.util.concurrent.CopyOnWriteArraySet;
/*  20:    */ import java.util.concurrent.LinkedBlockingDeque;
/*  21:    */ import java.util.concurrent.LinkedBlockingQueue;
/*  22:    */ import java.util.concurrent.PriorityBlockingQueue;
/*  23:    */ 
/*  24:    */ public abstract class CollectionSchema<V>
/*  25:    */   implements Schema<Collection<V>>
/*  26:    */ {
/*  27:    */   public static final String FIELD_NAME_VALUE = "v";
/*  28:    */   public final MessageFactory messageFactory;
/*  29:    */   
/*  30:    */   public static abstract enum MessageFactories
/*  31:    */     implements CollectionSchema.MessageFactory
/*  32:    */   {
/*  33: 56 */     Collection(ArrayList.class),  List(ArrayList.class),  ArrayList(ArrayList.class),  LinkedList(LinkedList.class),  CopyOnWriteArrayList(CopyOnWriteArrayList.class),  Stack(Stack.class),  Vector(Vector.class),  Set(HashSet.class),  HashSet(HashSet.class),  LinkedHashSet(LinkedHashSet.class),  SortedSet(TreeSet.class),  NavigableSet(TreeSet.class),  TreeSet(TreeSet.class),  ConcurrentSkipListSet(ConcurrentSkipListSet.class),  CopyOnWriteArraySet(CopyOnWriteArraySet.class),  Queue(LinkedList.class),  BlockingQueue(LinkedBlockingQueue.class),  LinkedBlockingQueue(LinkedBlockingQueue.class),  Deque(LinkedList.class),  BlockingDeque(LinkedBlockingDeque.class),  LinkedBlockingDeque(LinkedBlockingDeque.class),  ArrayBlockingQueue(ArrayBlockingQueue.class),  ArrayDeque(ArrayDeque.class),  ConcurrentLinkedQueue(ConcurrentLinkedQueue.class),  ConcurrentLinkedDeque(ConcurrentLinkedDeque.class),  PriorityBlockingQueue(PriorityBlockingQueue.class),  PriorityQueue(PriorityQueue.class);
/*  34:    */     
/*  35:    */     public final Class<?> typeClass;
/*  36:    */     
/*  37:    */     private MessageFactories(Class<?> typeClass)
/*  38:    */     {
/*  39:286 */       this.typeClass = typeClass;
/*  40:    */     }
/*  41:    */     
/*  42:    */     public Class<?> typeClass()
/*  43:    */     {
/*  44:292 */       return this.typeClass;
/*  45:    */     }
/*  46:    */     
/*  47:    */     public static MessageFactories getFactory(Class<? extends Collection<?>> clazz)
/*  48:    */     {
/*  49:300 */       return clazz.getName().startsWith("java.util") ? 
/*  50:301 */         valueOf(clazz.getSimpleName()) : null;
/*  51:    */     }
/*  52:    */     
/*  53:    */     public static MessageFactories getFactory(String name)
/*  54:    */     {
/*  55:309 */       return valueOf(name);
/*  56:    */     }
/*  57:    */   }
/*  58:    */   
/*  59:    */   public CollectionSchema()
/*  60:    */   {
/*  61:320 */     this(MessageFactories.ArrayList);
/*  62:    */   }
/*  63:    */   
/*  64:    */   public CollectionSchema(MessageFactory messageFactory)
/*  65:    */   {
/*  66:325 */     this.messageFactory = messageFactory;
/*  67:    */   }
/*  68:    */   
/*  69:    */   protected abstract void addValueFrom(Input paramInput, Collection<V> paramCollection)
/*  70:    */     throws IOException;
/*  71:    */   
/*  72:    */   protected abstract void writeValueTo(Output paramOutput, int paramInt, V paramV, boolean paramBoolean)
/*  73:    */     throws IOException;
/*  74:    */   
/*  75:    */   protected abstract void transferValue(Pipe paramPipe, Input paramInput, Output paramOutput, int paramInt, boolean paramBoolean)
/*  76:    */     throws IOException;
/*  77:    */   
/*  78:    */   public final String getFieldName(int number)
/*  79:    */   {
/*  80:349 */     return number == 1 ? "v" : null;
/*  81:    */   }
/*  82:    */   
/*  83:    */   public final int getFieldNumber(String name)
/*  84:    */   {
/*  85:355 */     return (name.length() == 1) && (name.charAt(0) == 'v') ? 1 : 0;
/*  86:    */   }
/*  87:    */   
/*  88:    */   public final boolean isInitialized(Collection<V> map)
/*  89:    */   {
/*  90:361 */     return true;
/*  91:    */   }
/*  92:    */   
/*  93:    */   public final String messageFullName()
/*  94:    */   {
/*  95:367 */     return Collection.class.getName();
/*  96:    */   }
/*  97:    */   
/*  98:    */   public final String messageName()
/*  99:    */   {
/* 100:373 */     return Collection.class.getSimpleName();
/* 101:    */   }
/* 102:    */   
/* 103:    */   public final Class<? super Collection<V>> typeClass()
/* 104:    */   {
/* 105:379 */     return Collection.class;
/* 106:    */   }
/* 107:    */   
/* 108:    */   public final Collection<V> newMessage()
/* 109:    */   {
/* 110:385 */     return this.messageFactory.newMessage();
/* 111:    */   }
/* 112:    */   
/* 113:    */   public void mergeFrom(Input input, Collection<V> message)
/* 114:    */     throws IOException
/* 115:    */   {
/* 116:391 */     for (int number = input.readFieldNumber(this);; number = input.readFieldNumber(this)) {
/* 117:393 */       switch (number)
/* 118:    */       {
/* 119:    */       case 0: 
/* 120:396 */         return;
/* 121:    */       case 1: 
/* 122:398 */         addValueFrom(input, message);
/* 123:399 */         break;
/* 124:    */       default: 
/* 125:401 */         throw new ProtostuffException("The collection was incorrectly serialized.");
/* 126:    */       }
/* 127:    */     }
/* 128:    */   }
/* 129:    */   
/* 130:    */   public void writeTo(Output output, Collection<V> message)
/* 131:    */     throws IOException
/* 132:    */   {
/* 133:410 */     for (V value : message) {
/* 134:413 */       if (value != null) {
/* 135:414 */         writeValueTo(output, 1, value, true);
/* 136:    */       }
/* 137:    */     }
/* 138:    */   }
/* 139:    */   
/* 140:418 */   public final Pipe.Schema<Collection<V>> pipeSchema = new Pipe.Schema(this)
/* 141:    */   {
/* 142:    */     protected void transfer(Pipe pipe, Input input, Output output)
/* 143:    */       throws IOException
/* 144:    */     {
/* 145:425 */       int number = input.readFieldNumber(this);
/* 146:426 */       for (;; number = input.readFieldNumber(this)) {
/* 147:428 */         switch (number)
/* 148:    */         {
/* 149:    */         case 0: 
/* 150:431 */           return;
/* 151:    */         case 1: 
/* 152:433 */           CollectionSchema.this.transferValue(pipe, input, output, 1, true);
/* 153:434 */           break;
/* 154:    */         default: 
/* 155:436 */           throw new ProtostuffException("The collection was incorrectly serialized.");
/* 156:    */         }
/* 157:    */       }
/* 158:    */     }
/* 159:    */   };
/* 160:    */   
/* 161:    */   public static abstract interface MessageFactory
/* 162:    */   {
/* 163:    */     public abstract <V> Collection<V> newMessage();
/* 164:    */     
/* 165:    */     public abstract Class<?> typeClass();
/* 166:    */   }
/* 167:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.CollectionSchema
 * JD-Core Version:    0.7.0.1
 */