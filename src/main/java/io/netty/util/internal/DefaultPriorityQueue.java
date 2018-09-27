/*   1:    */ package io.netty.util.internal;
/*   2:    */ 
/*   3:    */ import java.util.AbstractQueue;
/*   4:    */ import java.util.Arrays;
/*   5:    */ import java.util.Comparator;
/*   6:    */ import java.util.Iterator;
/*   7:    */ import java.util.NoSuchElementException;
/*   8:    */ 
/*   9:    */ public final class DefaultPriorityQueue<T extends PriorityQueueNode>
/*  10:    */   extends AbstractQueue<T>
/*  11:    */   implements PriorityQueue<T>
/*  12:    */ {
/*  13: 33 */   private static final PriorityQueueNode[] EMPTY_ARRAY = new PriorityQueueNode[0];
/*  14:    */   private final Comparator<T> comparator;
/*  15:    */   private T[] queue;
/*  16:    */   private int size;
/*  17:    */   
/*  18:    */   public DefaultPriorityQueue(Comparator<T> comparator, int initialSize)
/*  19:    */   {
/*  20: 40 */     this.comparator = ((Comparator)ObjectUtil.checkNotNull(comparator, "comparator"));
/*  21: 41 */     this.queue = ((PriorityQueueNode[])(initialSize != 0 ? new PriorityQueueNode[initialSize] : EMPTY_ARRAY));
/*  22:    */   }
/*  23:    */   
/*  24:    */   public int size()
/*  25:    */   {
/*  26: 46 */     return this.size;
/*  27:    */   }
/*  28:    */   
/*  29:    */   public boolean isEmpty()
/*  30:    */   {
/*  31: 51 */     return this.size == 0;
/*  32:    */   }
/*  33:    */   
/*  34:    */   public boolean contains(Object o)
/*  35:    */   {
/*  36: 56 */     if (!(o instanceof PriorityQueueNode)) {
/*  37: 57 */       return false;
/*  38:    */     }
/*  39: 59 */     PriorityQueueNode node = (PriorityQueueNode)o;
/*  40: 60 */     return contains(node, node.priorityQueueIndex(this));
/*  41:    */   }
/*  42:    */   
/*  43:    */   public boolean containsTyped(T node)
/*  44:    */   {
/*  45: 65 */     return contains(node, node.priorityQueueIndex(this));
/*  46:    */   }
/*  47:    */   
/*  48:    */   public void clear()
/*  49:    */   {
/*  50: 70 */     for (int i = 0; i < this.size; i++)
/*  51:    */     {
/*  52: 71 */       T node = this.queue[i];
/*  53: 72 */       if (node != null)
/*  54:    */       {
/*  55: 73 */         node.priorityQueueIndex(this, -1);
/*  56: 74 */         this.queue[i] = null;
/*  57:    */       }
/*  58:    */     }
/*  59: 77 */     this.size = 0;
/*  60:    */   }
/*  61:    */   
/*  62:    */   public void clearIgnoringIndexes()
/*  63:    */   {
/*  64: 82 */     this.size = 0;
/*  65:    */   }
/*  66:    */   
/*  67:    */   public boolean offer(T e)
/*  68:    */   {
/*  69: 87 */     if (e.priorityQueueIndex(this) != -1) {
/*  70: 88 */       throw new IllegalArgumentException("e.priorityQueueIndex(): " + e.priorityQueueIndex(this) + " (expected: " + -1 + ") + e: " + e);
/*  71:    */     }
/*  72: 93 */     if (this.size >= this.queue.length) {
/*  73: 96 */       this.queue = ((PriorityQueueNode[])Arrays.copyOf(this.queue, this.queue.length + (this.queue.length < 64 ? this.queue.length + 2 : this.queue.length >>> 1)));
/*  74:    */     }
/*  75:101 */     bubbleUp(this.size++, e);
/*  76:102 */     return true;
/*  77:    */   }
/*  78:    */   
/*  79:    */   public T poll()
/*  80:    */   {
/*  81:107 */     if (this.size == 0) {
/*  82:108 */       return null;
/*  83:    */     }
/*  84:110 */     T result = this.queue[0];
/*  85:111 */     result.priorityQueueIndex(this, -1);
/*  86:    */     
/*  87:113 */     T last = this.queue[(--this.size)];
/*  88:114 */     this.queue[this.size] = null;
/*  89:115 */     if (this.size != 0) {
/*  90:116 */       bubbleDown(0, last);
/*  91:    */     }
/*  92:119 */     return result;
/*  93:    */   }
/*  94:    */   
/*  95:    */   public T peek()
/*  96:    */   {
/*  97:124 */     return this.size == 0 ? null : this.queue[0];
/*  98:    */   }
/*  99:    */   
/* 100:    */   public boolean remove(Object o)
/* 101:    */   {
/* 102:    */     try
/* 103:    */     {
/* 104:132 */       node = (PriorityQueueNode)o;
/* 105:    */     }
/* 106:    */     catch (ClassCastException e)
/* 107:    */     {
/* 108:    */       T node;
/* 109:134 */       return false;
/* 110:    */     }
/* 111:    */     T node;
/* 112:136 */     return removeTyped(node);
/* 113:    */   }
/* 114:    */   
/* 115:    */   public boolean removeTyped(T node)
/* 116:    */   {
/* 117:141 */     int i = node.priorityQueueIndex(this);
/* 118:142 */     if (!contains(node, i)) {
/* 119:143 */       return false;
/* 120:    */     }
/* 121:146 */     node.priorityQueueIndex(this, -1);
/* 122:147 */     if ((--this.size == 0) || (this.size == i))
/* 123:    */     {
/* 124:149 */       this.queue[i] = null;
/* 125:150 */       return true;
/* 126:    */     }
/* 127:154 */     T moved = this.queue[i] =  = this.queue[this.size];
/* 128:155 */     this.queue[this.size] = null;
/* 129:159 */     if (this.comparator.compare(node, moved) < 0) {
/* 130:160 */       bubbleDown(i, moved);
/* 131:    */     } else {
/* 132:162 */       bubbleUp(i, moved);
/* 133:    */     }
/* 134:164 */     return true;
/* 135:    */   }
/* 136:    */   
/* 137:    */   public void priorityChanged(T node)
/* 138:    */   {
/* 139:169 */     int i = node.priorityQueueIndex(this);
/* 140:170 */     if (!contains(node, i)) {
/* 141:171 */       return;
/* 142:    */     }
/* 143:175 */     if (i == 0)
/* 144:    */     {
/* 145:176 */       bubbleDown(i, node);
/* 146:    */     }
/* 147:    */     else
/* 148:    */     {
/* 149:179 */       int iParent = i - 1 >>> 1;
/* 150:180 */       T parent = this.queue[iParent];
/* 151:181 */       if (this.comparator.compare(node, parent) < 0) {
/* 152:182 */         bubbleUp(i, node);
/* 153:    */       } else {
/* 154:184 */         bubbleDown(i, node);
/* 155:    */       }
/* 156:    */     }
/* 157:    */   }
/* 158:    */   
/* 159:    */   public Object[] toArray()
/* 160:    */   {
/* 161:191 */     return Arrays.copyOf(this.queue, this.size);
/* 162:    */   }
/* 163:    */   
/* 164:    */   public <X> X[] toArray(X[] a)
/* 165:    */   {
/* 166:197 */     if (a.length < this.size) {
/* 167:198 */       return (Object[])Arrays.copyOf(this.queue, this.size, a.getClass());
/* 168:    */     }
/* 169:200 */     System.arraycopy(this.queue, 0, a, 0, this.size);
/* 170:201 */     if (a.length > this.size) {
/* 171:202 */       a[this.size] = null;
/* 172:    */     }
/* 173:204 */     return a;
/* 174:    */   }
/* 175:    */   
/* 176:    */   public Iterator<T> iterator()
/* 177:    */   {
/* 178:212 */     return new PriorityQueueIterator(null);
/* 179:    */   }
/* 180:    */   
/* 181:    */   private final class PriorityQueueIterator
/* 182:    */     implements Iterator<T>
/* 183:    */   {
/* 184:    */     private int index;
/* 185:    */     
/* 186:    */     private PriorityQueueIterator() {}
/* 187:    */     
/* 188:    */     public boolean hasNext()
/* 189:    */     {
/* 190:220 */       return this.index < DefaultPriorityQueue.this.size;
/* 191:    */     }
/* 192:    */     
/* 193:    */     public T next()
/* 194:    */     {
/* 195:225 */       if (this.index >= DefaultPriorityQueue.this.size) {
/* 196:226 */         throw new NoSuchElementException();
/* 197:    */       }
/* 198:229 */       return DefaultPriorityQueue.this.queue[(this.index++)];
/* 199:    */     }
/* 200:    */     
/* 201:    */     public void remove()
/* 202:    */     {
/* 203:234 */       throw new UnsupportedOperationException("remove");
/* 204:    */     }
/* 205:    */   }
/* 206:    */   
/* 207:    */   private boolean contains(PriorityQueueNode node, int i)
/* 208:    */   {
/* 209:239 */     return (i >= 0) && (i < this.size) && (node.equals(this.queue[i]));
/* 210:    */   }
/* 211:    */   
/* 212:    */   private void bubbleDown(int k, T node)
/* 213:    */   {
/* 214:243 */     int half = this.size >>> 1;
/* 215:244 */     while (k < half)
/* 216:    */     {
/* 217:246 */       int iChild = (k << 1) + 1;
/* 218:247 */       T child = this.queue[iChild];
/* 219:    */       
/* 220:    */ 
/* 221:250 */       int rightChild = iChild + 1;
/* 222:251 */       if ((rightChild < this.size) && (this.comparator.compare(child, this.queue[rightChild]) > 0)) {
/* 223:252 */         child = this.queue[(iChild = rightChild)];
/* 224:    */       }
/* 225:256 */       if (this.comparator.compare(node, child) <= 0) {
/* 226:    */         break;
/* 227:    */       }
/* 228:261 */       this.queue[k] = child;
/* 229:262 */       child.priorityQueueIndex(this, k);
/* 230:    */       
/* 231:    */ 
/* 232:265 */       k = iChild;
/* 233:    */     }
/* 234:269 */     this.queue[k] = node;
/* 235:270 */     node.priorityQueueIndex(this, k);
/* 236:    */   }
/* 237:    */   
/* 238:    */   private void bubbleUp(int k, T node)
/* 239:    */   {
/* 240:274 */     while (k > 0)
/* 241:    */     {
/* 242:275 */       int iParent = k - 1 >>> 1;
/* 243:276 */       T parent = this.queue[iParent];
/* 244:280 */       if (this.comparator.compare(node, parent) >= 0) {
/* 245:    */         break;
/* 246:    */       }
/* 247:285 */       this.queue[k] = parent;
/* 248:286 */       parent.priorityQueueIndex(this, k);
/* 249:    */       
/* 250:    */ 
/* 251:289 */       k = iParent;
/* 252:    */     }
/* 253:293 */     this.queue[k] = node;
/* 254:294 */     node.priorityQueueIndex(this, k);
/* 255:    */   }
/* 256:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.DefaultPriorityQueue
 * JD-Core Version:    0.7.0.1
 */