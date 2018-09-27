/*   1:    */ package io.netty.channel;
/*   2:    */ 
/*   3:    */ import java.util.ArrayDeque;
/*   4:    */ import java.util.Queue;
/*   5:    */ 
/*   6:    */ public final class ChannelFlushPromiseNotifier
/*   7:    */ {
/*   8:    */   private long writeCounter;
/*   9: 28 */   private final Queue<FlushCheckpoint> flushCheckpoints = new ArrayDeque();
/*  10:    */   private final boolean tryNotify;
/*  11:    */   
/*  12:    */   public ChannelFlushPromiseNotifier(boolean tryNotify)
/*  13:    */   {
/*  14: 40 */     this.tryNotify = tryNotify;
/*  15:    */   }
/*  16:    */   
/*  17:    */   public ChannelFlushPromiseNotifier()
/*  18:    */   {
/*  19: 48 */     this(false);
/*  20:    */   }
/*  21:    */   
/*  22:    */   @Deprecated
/*  23:    */   public ChannelFlushPromiseNotifier add(ChannelPromise promise, int pendingDataSize)
/*  24:    */   {
/*  25: 56 */     return add(promise, pendingDataSize);
/*  26:    */   }
/*  27:    */   
/*  28:    */   public ChannelFlushPromiseNotifier add(ChannelPromise promise, long pendingDataSize)
/*  29:    */   {
/*  30: 64 */     if (promise == null) {
/*  31: 65 */       throw new NullPointerException("promise");
/*  32:    */     }
/*  33: 67 */     if (pendingDataSize < 0L) {
/*  34: 68 */       throw new IllegalArgumentException("pendingDataSize must be >= 0 but was " + pendingDataSize);
/*  35:    */     }
/*  36: 70 */     long checkpoint = this.writeCounter + pendingDataSize;
/*  37: 71 */     if ((promise instanceof FlushCheckpoint))
/*  38:    */     {
/*  39: 72 */       FlushCheckpoint cp = (FlushCheckpoint)promise;
/*  40: 73 */       cp.flushCheckpoint(checkpoint);
/*  41: 74 */       this.flushCheckpoints.add(cp);
/*  42:    */     }
/*  43:    */     else
/*  44:    */     {
/*  45: 76 */       this.flushCheckpoints.add(new DefaultFlushCheckpoint(checkpoint, promise));
/*  46:    */     }
/*  47: 78 */     return this;
/*  48:    */   }
/*  49:    */   
/*  50:    */   public ChannelFlushPromiseNotifier increaseWriteCounter(long delta)
/*  51:    */   {
/*  52: 84 */     if (delta < 0L) {
/*  53: 85 */       throw new IllegalArgumentException("delta must be >= 0 but was " + delta);
/*  54:    */     }
/*  55: 87 */     this.writeCounter += delta;
/*  56: 88 */     return this;
/*  57:    */   }
/*  58:    */   
/*  59:    */   public long writeCounter()
/*  60:    */   {
/*  61: 95 */     return this.writeCounter;
/*  62:    */   }
/*  63:    */   
/*  64:    */   public ChannelFlushPromiseNotifier notifyPromises()
/*  65:    */   {
/*  66:106 */     notifyPromises0(null);
/*  67:107 */     return this;
/*  68:    */   }
/*  69:    */   
/*  70:    */   @Deprecated
/*  71:    */   public ChannelFlushPromiseNotifier notifyFlushFutures()
/*  72:    */   {
/*  73:115 */     return notifyPromises();
/*  74:    */   }
/*  75:    */   
/*  76:    */   public ChannelFlushPromiseNotifier notifyPromises(Throwable cause)
/*  77:    */   {
/*  78:130 */     notifyPromises();
/*  79:    */     for (;;)
/*  80:    */     {
/*  81:132 */       FlushCheckpoint cp = (FlushCheckpoint)this.flushCheckpoints.poll();
/*  82:133 */       if (cp == null) {
/*  83:    */         break;
/*  84:    */       }
/*  85:136 */       if (this.tryNotify) {
/*  86:137 */         cp.promise().tryFailure(cause);
/*  87:    */       } else {
/*  88:139 */         cp.promise().setFailure(cause);
/*  89:    */       }
/*  90:    */     }
/*  91:142 */     return this;
/*  92:    */   }
/*  93:    */   
/*  94:    */   @Deprecated
/*  95:    */   public ChannelFlushPromiseNotifier notifyFlushFutures(Throwable cause)
/*  96:    */   {
/*  97:150 */     return notifyPromises(cause);
/*  98:    */   }
/*  99:    */   
/* 100:    */   public ChannelFlushPromiseNotifier notifyPromises(Throwable cause1, Throwable cause2)
/* 101:    */   {
/* 102:170 */     notifyPromises0(cause1);
/* 103:    */     for (;;)
/* 104:    */     {
/* 105:172 */       FlushCheckpoint cp = (FlushCheckpoint)this.flushCheckpoints.poll();
/* 106:173 */       if (cp == null) {
/* 107:    */         break;
/* 108:    */       }
/* 109:176 */       if (this.tryNotify) {
/* 110:177 */         cp.promise().tryFailure(cause2);
/* 111:    */       } else {
/* 112:179 */         cp.promise().setFailure(cause2);
/* 113:    */       }
/* 114:    */     }
/* 115:182 */     return this;
/* 116:    */   }
/* 117:    */   
/* 118:    */   @Deprecated
/* 119:    */   public ChannelFlushPromiseNotifier notifyFlushFutures(Throwable cause1, Throwable cause2)
/* 120:    */   {
/* 121:190 */     return notifyPromises(cause1, cause2);
/* 122:    */   }
/* 123:    */   
/* 124:    */   private void notifyPromises0(Throwable cause)
/* 125:    */   {
/* 126:194 */     if (this.flushCheckpoints.isEmpty())
/* 127:    */     {
/* 128:195 */       this.writeCounter = 0L;
/* 129:196 */       return;
/* 130:    */     }
/* 131:199 */     long writeCounter = this.writeCounter;
/* 132:    */     for (;;)
/* 133:    */     {
/* 134:201 */       FlushCheckpoint cp = (FlushCheckpoint)this.flushCheckpoints.peek();
/* 135:202 */       if (cp == null)
/* 136:    */       {
/* 137:204 */         this.writeCounter = 0L;
/* 138:205 */         break;
/* 139:    */       }
/* 140:208 */       if (cp.flushCheckpoint() > writeCounter)
/* 141:    */       {
/* 142:209 */         if ((writeCounter <= 0L) || (this.flushCheckpoints.size() != 1)) {
/* 143:    */           break;
/* 144:    */         }
/* 145:210 */         this.writeCounter = 0L;
/* 146:211 */         cp.flushCheckpoint(cp.flushCheckpoint() - writeCounter); break;
/* 147:    */       }
/* 148:216 */       this.flushCheckpoints.remove();
/* 149:217 */       ChannelPromise promise = cp.promise();
/* 150:218 */       if (cause == null)
/* 151:    */       {
/* 152:219 */         if (this.tryNotify) {
/* 153:220 */           promise.trySuccess();
/* 154:    */         } else {
/* 155:222 */           promise.setSuccess();
/* 156:    */         }
/* 157:    */       }
/* 158:225 */       else if (this.tryNotify) {
/* 159:226 */         promise.tryFailure(cause);
/* 160:    */       } else {
/* 161:228 */         promise.setFailure(cause);
/* 162:    */       }
/* 163:    */     }
/* 164:234 */     long newWriteCounter = this.writeCounter;
/* 165:235 */     if (newWriteCounter >= 549755813888L)
/* 166:    */     {
/* 167:238 */       this.writeCounter = 0L;
/* 168:239 */       for (FlushCheckpoint cp : this.flushCheckpoints) {
/* 169:240 */         cp.flushCheckpoint(cp.flushCheckpoint() - newWriteCounter);
/* 170:    */       }
/* 171:    */     }
/* 172:    */   }
/* 173:    */   
/* 174:    */   private static class DefaultFlushCheckpoint
/* 175:    */     implements ChannelFlushPromiseNotifier.FlushCheckpoint
/* 176:    */   {
/* 177:    */     private long checkpoint;
/* 178:    */     private final ChannelPromise future;
/* 179:    */     
/* 180:    */     DefaultFlushCheckpoint(long checkpoint, ChannelPromise future)
/* 181:    */     {
/* 182:256 */       this.checkpoint = checkpoint;
/* 183:257 */       this.future = future;
/* 184:    */     }
/* 185:    */     
/* 186:    */     public long flushCheckpoint()
/* 187:    */     {
/* 188:262 */       return this.checkpoint;
/* 189:    */     }
/* 190:    */     
/* 191:    */     public void flushCheckpoint(long checkpoint)
/* 192:    */     {
/* 193:267 */       this.checkpoint = checkpoint;
/* 194:    */     }
/* 195:    */     
/* 196:    */     public ChannelPromise promise()
/* 197:    */     {
/* 198:272 */       return this.future;
/* 199:    */     }
/* 200:    */   }
/* 201:    */   
/* 202:    */   static abstract interface FlushCheckpoint
/* 203:    */   {
/* 204:    */     public abstract long flushCheckpoint();
/* 205:    */     
/* 206:    */     public abstract void flushCheckpoint(long paramLong);
/* 207:    */     
/* 208:    */     public abstract ChannelPromise promise();
/* 209:    */   }
/* 210:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.ChannelFlushPromiseNotifier
 * JD-Core Version:    0.7.0.1
 */