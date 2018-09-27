/*   1:    */ package io.netty.channel;
/*   2:    */ 
/*   3:    */ import io.netty.util.Recycler;
/*   4:    */ import io.netty.util.Recycler.Handle;
/*   5:    */ import io.netty.util.ReferenceCountUtil;
/*   6:    */ import io.netty.util.concurrent.EventExecutor;
/*   7:    */ import io.netty.util.concurrent.PromiseCombiner;
/*   8:    */ import io.netty.util.internal.SystemPropertyUtil;
/*   9:    */ import io.netty.util.internal.logging.InternalLogger;
/*  10:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  11:    */ 
/*  12:    */ public final class PendingWriteQueue
/*  13:    */ {
/*  14: 32 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(PendingWriteQueue.class);
/*  15: 38 */   private static final int PENDING_WRITE_OVERHEAD = SystemPropertyUtil.getInt("io.netty.transport.pendingWriteSizeOverhead", 64);
/*  16:    */   private final ChannelHandlerContext ctx;
/*  17:    */   private final PendingBytesTracker tracker;
/*  18:    */   private PendingWrite head;
/*  19:    */   private PendingWrite tail;
/*  20:    */   private int size;
/*  21:    */   private long bytes;
/*  22:    */   
/*  23:    */   public PendingWriteQueue(ChannelHandlerContext ctx)
/*  24:    */   {
/*  25: 50 */     this.tracker = PendingBytesTracker.newTracker(ctx.channel());
/*  26: 51 */     this.ctx = ctx;
/*  27:    */   }
/*  28:    */   
/*  29:    */   public boolean isEmpty()
/*  30:    */   {
/*  31: 58 */     assert (this.ctx.executor().inEventLoop());
/*  32: 59 */     return this.head == null;
/*  33:    */   }
/*  34:    */   
/*  35:    */   public int size()
/*  36:    */   {
/*  37: 66 */     assert (this.ctx.executor().inEventLoop());
/*  38: 67 */     return this.size;
/*  39:    */   }
/*  40:    */   
/*  41:    */   public long bytes()
/*  42:    */   {
/*  43: 75 */     assert (this.ctx.executor().inEventLoop());
/*  44: 76 */     return this.bytes;
/*  45:    */   }
/*  46:    */   
/*  47:    */   private int size(Object msg)
/*  48:    */   {
/*  49: 82 */     int messageSize = this.tracker.size(msg);
/*  50: 83 */     if (messageSize < 0) {
/*  51: 85 */       messageSize = 0;
/*  52:    */     }
/*  53: 87 */     return messageSize + PENDING_WRITE_OVERHEAD;
/*  54:    */   }
/*  55:    */   
/*  56:    */   public void add(Object msg, ChannelPromise promise)
/*  57:    */   {
/*  58: 94 */     assert (this.ctx.executor().inEventLoop());
/*  59: 95 */     if (msg == null) {
/*  60: 96 */       throw new NullPointerException("msg");
/*  61:    */     }
/*  62: 98 */     if (promise == null) {
/*  63: 99 */       throw new NullPointerException("promise");
/*  64:    */     }
/*  65:103 */     int messageSize = size(msg);
/*  66:    */     
/*  67:105 */     PendingWrite write = PendingWrite.newInstance(msg, messageSize, promise);
/*  68:106 */     PendingWrite currentTail = this.tail;
/*  69:107 */     if (currentTail == null)
/*  70:    */     {
/*  71:108 */       this.tail = (this.head = write);
/*  72:    */     }
/*  73:    */     else
/*  74:    */     {
/*  75:110 */       currentTail.next = write;
/*  76:111 */       this.tail = write;
/*  77:    */     }
/*  78:113 */     this.size += 1;
/*  79:114 */     this.bytes += messageSize;
/*  80:115 */     this.tracker.incrementPendingOutboundBytes(write.size);
/*  81:    */   }
/*  82:    */   
/*  83:    */   public ChannelFuture removeAndWriteAll()
/*  84:    */   {
/*  85:126 */     assert (this.ctx.executor().inEventLoop());
/*  86:128 */     if (isEmpty()) {
/*  87:129 */       return null;
/*  88:    */     }
/*  89:132 */     ChannelPromise p = this.ctx.newPromise();
/*  90:133 */     PromiseCombiner combiner = new PromiseCombiner();
/*  91:    */     try
/*  92:    */     {
/*  93:137 */       for (PendingWrite write = this.head; write != null; write = this.head)
/*  94:    */       {
/*  95:138 */         this.head = (this.tail = null);
/*  96:139 */         this.size = 0;
/*  97:140 */         this.bytes = 0L;
/*  98:142 */         while (write != null)
/*  99:    */         {
/* 100:143 */           PendingWrite next = write.next;
/* 101:144 */           Object msg = write.msg;
/* 102:145 */           ChannelPromise promise = write.promise;
/* 103:146 */           recycle(write, false);
/* 104:147 */           combiner.add(promise);
/* 105:148 */           this.ctx.write(msg, promise);
/* 106:149 */           write = next;
/* 107:    */         }
/* 108:    */       }
/* 109:152 */       combiner.finish(p);
/* 110:    */     }
/* 111:    */     catch (Throwable cause)
/* 112:    */     {
/* 113:154 */       p.setFailure(cause);
/* 114:    */     }
/* 115:156 */     assertEmpty();
/* 116:157 */     return p;
/* 117:    */   }
/* 118:    */   
/* 119:    */   public void removeAndFailAll(Throwable cause)
/* 120:    */   {
/* 121:165 */     assert (this.ctx.executor().inEventLoop());
/* 122:166 */     if (cause == null) {
/* 123:167 */       throw new NullPointerException("cause");
/* 124:    */     }
/* 125:171 */     for (PendingWrite write = this.head; write != null; write = this.head)
/* 126:    */     {
/* 127:172 */       this.head = (this.tail = null);
/* 128:173 */       this.size = 0;
/* 129:174 */       this.bytes = 0L;
/* 130:175 */       while (write != null)
/* 131:    */       {
/* 132:176 */         PendingWrite next = write.next;
/* 133:177 */         ReferenceCountUtil.safeRelease(write.msg);
/* 134:178 */         ChannelPromise promise = write.promise;
/* 135:179 */         recycle(write, false);
/* 136:180 */         safeFail(promise, cause);
/* 137:181 */         write = next;
/* 138:    */       }
/* 139:    */     }
/* 140:184 */     assertEmpty();
/* 141:    */   }
/* 142:    */   
/* 143:    */   public void removeAndFail(Throwable cause)
/* 144:    */   {
/* 145:192 */     assert (this.ctx.executor().inEventLoop());
/* 146:193 */     if (cause == null) {
/* 147:194 */       throw new NullPointerException("cause");
/* 148:    */     }
/* 149:196 */     PendingWrite write = this.head;
/* 150:198 */     if (write == null) {
/* 151:199 */       return;
/* 152:    */     }
/* 153:201 */     ReferenceCountUtil.safeRelease(write.msg);
/* 154:202 */     ChannelPromise promise = write.promise;
/* 155:203 */     safeFail(promise, cause);
/* 156:204 */     recycle(write, true);
/* 157:    */   }
/* 158:    */   
/* 159:    */   private void assertEmpty()
/* 160:    */   {
/* 161:208 */     assert ((this.tail == null) && (this.head == null) && (this.size == 0));
/* 162:    */   }
/* 163:    */   
/* 164:    */   public ChannelFuture removeAndWrite()
/* 165:    */   {
/* 166:219 */     assert (this.ctx.executor().inEventLoop());
/* 167:220 */     PendingWrite write = this.head;
/* 168:221 */     if (write == null) {
/* 169:222 */       return null;
/* 170:    */     }
/* 171:224 */     Object msg = write.msg;
/* 172:225 */     ChannelPromise promise = write.promise;
/* 173:226 */     recycle(write, true);
/* 174:227 */     return this.ctx.write(msg, promise);
/* 175:    */   }
/* 176:    */   
/* 177:    */   public ChannelPromise remove()
/* 178:    */   {
/* 179:237 */     assert (this.ctx.executor().inEventLoop());
/* 180:238 */     PendingWrite write = this.head;
/* 181:239 */     if (write == null) {
/* 182:240 */       return null;
/* 183:    */     }
/* 184:242 */     ChannelPromise promise = write.promise;
/* 185:243 */     ReferenceCountUtil.safeRelease(write.msg);
/* 186:244 */     recycle(write, true);
/* 187:245 */     return promise;
/* 188:    */   }
/* 189:    */   
/* 190:    */   public Object current()
/* 191:    */   {
/* 192:252 */     assert (this.ctx.executor().inEventLoop());
/* 193:253 */     PendingWrite write = this.head;
/* 194:254 */     if (write == null) {
/* 195:255 */       return null;
/* 196:    */     }
/* 197:257 */     return write.msg;
/* 198:    */   }
/* 199:    */   
/* 200:    */   private void recycle(PendingWrite write, boolean update)
/* 201:    */   {
/* 202:261 */     PendingWrite next = write.next;
/* 203:262 */     long writeSize = write.size;
/* 204:264 */     if (update) {
/* 205:265 */       if (next == null)
/* 206:    */       {
/* 207:268 */         this.head = (this.tail = null);
/* 208:269 */         this.size = 0;
/* 209:270 */         this.bytes = 0L;
/* 210:    */       }
/* 211:    */       else
/* 212:    */       {
/* 213:272 */         this.head = next;
/* 214:273 */         this.size -= 1;
/* 215:274 */         this.bytes -= writeSize;
/* 216:275 */         assert ((this.size > 0) && (this.bytes >= 0L));
/* 217:    */       }
/* 218:    */     }
/* 219:279 */     write.recycle();
/* 220:280 */     this.tracker.decrementPendingOutboundBytes(writeSize);
/* 221:    */   }
/* 222:    */   
/* 223:    */   private static void safeFail(ChannelPromise promise, Throwable cause)
/* 224:    */   {
/* 225:284 */     if ((!(promise instanceof VoidChannelPromise)) && (!promise.tryFailure(cause))) {
/* 226:285 */       logger.warn("Failed to mark a promise as failure because it's done already: {}", promise, cause);
/* 227:    */     }
/* 228:    */   }
/* 229:    */   
/* 230:    */   static final class PendingWrite
/* 231:    */   {
/* 232:293 */     private static final Recycler<PendingWrite> RECYCLER = new Recycler()
/* 233:    */     {
/* 234:    */       protected PendingWriteQueue.PendingWrite newObject(Recycler.Handle<PendingWriteQueue.PendingWrite> handle)
/* 235:    */       {
/* 236:296 */         return new PendingWriteQueue.PendingWrite(handle, null);
/* 237:    */       }
/* 238:    */     };
/* 239:    */     private final Recycler.Handle<PendingWrite> handle;
/* 240:    */     private PendingWrite next;
/* 241:    */     private long size;
/* 242:    */     private ChannelPromise promise;
/* 243:    */     private Object msg;
/* 244:    */     
/* 245:    */     private PendingWrite(Recycler.Handle<PendingWrite> handle)
/* 246:    */     {
/* 247:307 */       this.handle = handle;
/* 248:    */     }
/* 249:    */     
/* 250:    */     static PendingWrite newInstance(Object msg, int size, ChannelPromise promise)
/* 251:    */     {
/* 252:311 */       PendingWrite write = (PendingWrite)RECYCLER.get();
/* 253:312 */       write.size = size;
/* 254:313 */       write.msg = msg;
/* 255:314 */       write.promise = promise;
/* 256:315 */       return write;
/* 257:    */     }
/* 258:    */     
/* 259:    */     private void recycle()
/* 260:    */     {
/* 261:319 */       this.size = 0L;
/* 262:320 */       this.next = null;
/* 263:321 */       this.msg = null;
/* 264:322 */       this.promise = null;
/* 265:323 */       this.handle.recycle(this);
/* 266:    */     }
/* 267:    */   }
/* 268:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.PendingWriteQueue
 * JD-Core Version:    0.7.0.1
 */