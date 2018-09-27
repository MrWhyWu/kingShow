/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.StringUtil;
/*   4:    */ import java.util.ArrayList;
/*   5:    */ import java.util.Collections;
/*   6:    */ import java.util.Iterator;
/*   7:    */ import java.util.List;
/*   8:    */ 
/*   9:    */ final class PoolChunkList<T>
/*  10:    */   implements PoolChunkListMetric
/*  11:    */ {
/*  12: 29 */   private static final Iterator<PoolChunkMetric> EMPTY_METRICS = Collections.emptyList().iterator();
/*  13:    */   private final PoolArena<T> arena;
/*  14:    */   private final PoolChunkList<T> nextList;
/*  15:    */   private final int minUsage;
/*  16:    */   private final int maxUsage;
/*  17:    */   private final int maxCapacity;
/*  18:    */   private PoolChunk<T> head;
/*  19:    */   private PoolChunkList<T> prevList;
/*  20:    */   
/*  21:    */   PoolChunkList(PoolArena<T> arena, PoolChunkList<T> nextList, int minUsage, int maxUsage, int chunkSize)
/*  22:    */   {
/*  23: 44 */     assert (minUsage <= maxUsage);
/*  24: 45 */     this.arena = arena;
/*  25: 46 */     this.nextList = nextList;
/*  26: 47 */     this.minUsage = minUsage;
/*  27: 48 */     this.maxUsage = maxUsage;
/*  28: 49 */     this.maxCapacity = calculateMaxCapacity(minUsage, chunkSize);
/*  29:    */   }
/*  30:    */   
/*  31:    */   private static int calculateMaxCapacity(int minUsage, int chunkSize)
/*  32:    */   {
/*  33: 57 */     minUsage = minUsage0(minUsage);
/*  34: 59 */     if (minUsage == 100) {
/*  35: 61 */       return 0;
/*  36:    */     }
/*  37: 69 */     return (int)(chunkSize * (100L - minUsage) / 100L);
/*  38:    */   }
/*  39:    */   
/*  40:    */   void prevList(PoolChunkList<T> prevList)
/*  41:    */   {
/*  42: 73 */     assert (this.prevList == null);
/*  43: 74 */     this.prevList = prevList;
/*  44:    */   }
/*  45:    */   
/*  46:    */   boolean allocate(PooledByteBuf<T> buf, int reqCapacity, int normCapacity)
/*  47:    */   {
/*  48: 78 */     if ((this.head == null) || (normCapacity > this.maxCapacity)) {
/*  49: 81 */       return false;
/*  50:    */     }
/*  51: 84 */     PoolChunk<T> cur = this.head;
/*  52:    */     for (;;)
/*  53:    */     {
/*  54: 85 */       long handle = cur.allocate(normCapacity);
/*  55: 86 */       if (handle < 0L)
/*  56:    */       {
/*  57: 87 */         cur = cur.next;
/*  58: 88 */         if (cur == null) {
/*  59: 89 */           return false;
/*  60:    */         }
/*  61:    */       }
/*  62:    */       else
/*  63:    */       {
/*  64: 92 */         cur.initBuf(buf, handle, reqCapacity);
/*  65: 93 */         if (cur.usage() >= this.maxUsage)
/*  66:    */         {
/*  67: 94 */           remove(cur);
/*  68: 95 */           this.nextList.add(cur);
/*  69:    */         }
/*  70: 97 */         return true;
/*  71:    */       }
/*  72:    */     }
/*  73:    */   }
/*  74:    */   
/*  75:    */   boolean free(PoolChunk<T> chunk, long handle)
/*  76:    */   {
/*  77:103 */     chunk.free(handle);
/*  78:104 */     if (chunk.usage() < this.minUsage)
/*  79:    */     {
/*  80:105 */       remove(chunk);
/*  81:    */       
/*  82:107 */       return move0(chunk);
/*  83:    */     }
/*  84:109 */     return true;
/*  85:    */   }
/*  86:    */   
/*  87:    */   private boolean move(PoolChunk<T> chunk)
/*  88:    */   {
/*  89:113 */     assert (chunk.usage() < this.maxUsage);
/*  90:115 */     if (chunk.usage() < this.minUsage) {
/*  91:117 */       return move0(chunk);
/*  92:    */     }
/*  93:121 */     add0(chunk);
/*  94:122 */     return true;
/*  95:    */   }
/*  96:    */   
/*  97:    */   private boolean move0(PoolChunk<T> chunk)
/*  98:    */   {
/*  99:130 */     if (this.prevList == null)
/* 100:    */     {
/* 101:133 */       assert (chunk.usage() == 0);
/* 102:134 */       return false;
/* 103:    */     }
/* 104:136 */     return this.prevList.move(chunk);
/* 105:    */   }
/* 106:    */   
/* 107:    */   void add(PoolChunk<T> chunk)
/* 108:    */   {
/* 109:140 */     if (chunk.usage() >= this.maxUsage)
/* 110:    */     {
/* 111:141 */       this.nextList.add(chunk);
/* 112:142 */       return;
/* 113:    */     }
/* 114:144 */     add0(chunk);
/* 115:    */   }
/* 116:    */   
/* 117:    */   void add0(PoolChunk<T> chunk)
/* 118:    */   {
/* 119:151 */     chunk.parent = this;
/* 120:152 */     if (this.head == null)
/* 121:    */     {
/* 122:153 */       this.head = chunk;
/* 123:154 */       chunk.prev = null;
/* 124:155 */       chunk.next = null;
/* 125:    */     }
/* 126:    */     else
/* 127:    */     {
/* 128:157 */       chunk.prev = null;
/* 129:158 */       chunk.next = this.head;
/* 130:159 */       this.head.prev = chunk;
/* 131:160 */       this.head = chunk;
/* 132:    */     }
/* 133:    */   }
/* 134:    */   
/* 135:    */   private void remove(PoolChunk<T> cur)
/* 136:    */   {
/* 137:165 */     if (cur == this.head)
/* 138:    */     {
/* 139:166 */       this.head = cur.next;
/* 140:167 */       if (this.head != null) {
/* 141:168 */         this.head.prev = null;
/* 142:    */       }
/* 143:    */     }
/* 144:    */     else
/* 145:    */     {
/* 146:171 */       PoolChunk<T> next = cur.next;
/* 147:172 */       cur.prev.next = next;
/* 148:173 */       if (next != null) {
/* 149:174 */         next.prev = cur.prev;
/* 150:    */       }
/* 151:    */     }
/* 152:    */   }
/* 153:    */   
/* 154:    */   public int minUsage()
/* 155:    */   {
/* 156:181 */     return minUsage0(this.minUsage);
/* 157:    */   }
/* 158:    */   
/* 159:    */   public int maxUsage()
/* 160:    */   {
/* 161:186 */     return Math.min(this.maxUsage, 100);
/* 162:    */   }
/* 163:    */   
/* 164:    */   private static int minUsage0(int value)
/* 165:    */   {
/* 166:190 */     return Math.max(1, value);
/* 167:    */   }
/* 168:    */   
/* 169:    */   public Iterator<PoolChunkMetric> iterator()
/* 170:    */   {
/* 171:195 */     synchronized (this.arena)
/* 172:    */     {
/* 173:196 */       if (this.head == null) {
/* 174:197 */         return EMPTY_METRICS;
/* 175:    */       }
/* 176:199 */       List<PoolChunkMetric> metrics = new ArrayList();
/* 177:200 */       PoolChunk<T> cur = this.head;
/* 178:    */       for (;;)
/* 179:    */       {
/* 180:201 */         metrics.add(cur);
/* 181:202 */         cur = cur.next;
/* 182:203 */         if (cur == null) {
/* 183:    */           break;
/* 184:    */         }
/* 185:    */       }
/* 186:207 */       return metrics.iterator();
/* 187:    */     }
/* 188:    */   }
/* 189:    */   
/* 190:    */   public String toString()
/* 191:    */   {
/* 192:213 */     StringBuilder buf = new StringBuilder();
/* 193:214 */     synchronized (this.arena)
/* 194:    */     {
/* 195:215 */       if (this.head == null) {
/* 196:216 */         return "none";
/* 197:    */       }
/* 198:219 */       PoolChunk<T> cur = this.head;
/* 199:    */       for (;;)
/* 200:    */       {
/* 201:220 */         buf.append(cur);
/* 202:221 */         cur = cur.next;
/* 203:222 */         if (cur == null) {
/* 204:    */           break;
/* 205:    */         }
/* 206:225 */         buf.append(StringUtil.NEWLINE);
/* 207:    */       }
/* 208:    */     }
/* 209:228 */     return buf.toString();
/* 210:    */   }
/* 211:    */   
/* 212:    */   void destroy(PoolArena<T> arena)
/* 213:    */   {
/* 214:232 */     PoolChunk<T> chunk = this.head;
/* 215:233 */     while (chunk != null)
/* 216:    */     {
/* 217:234 */       arena.destroyChunk(chunk);
/* 218:235 */       chunk = chunk.next;
/* 219:    */     }
/* 220:237 */     this.head = null;
/* 221:    */   }
/* 222:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.PoolChunkList
 * JD-Core Version:    0.7.0.1
 */