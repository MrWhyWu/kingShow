/*   1:    */ package io.netty.handler.timeout;
/*   2:    */ 
/*   3:    */ import io.netty.channel.ChannelFuture;
/*   4:    */ import io.netty.channel.ChannelFutureListener;
/*   5:    */ import io.netty.channel.ChannelHandlerContext;
/*   6:    */ import io.netty.channel.ChannelOutboundHandlerAdapter;
/*   7:    */ import io.netty.channel.ChannelPromise;
/*   8:    */ import io.netty.util.concurrent.EventExecutor;
/*   9:    */ import java.util.concurrent.ScheduledFuture;
/*  10:    */ import java.util.concurrent.TimeUnit;
/*  11:    */ 
/*  12:    */ public class WriteTimeoutHandler
/*  13:    */   extends ChannelOutboundHandlerAdapter
/*  14:    */ {
/*  15: 66 */   private static final long MIN_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(1L);
/*  16:    */   private final long timeoutNanos;
/*  17:    */   private WriteTimeoutTask lastTask;
/*  18:    */   private boolean closed;
/*  19:    */   
/*  20:    */   public WriteTimeoutHandler(int timeoutSeconds)
/*  21:    */   {
/*  22: 84 */     this(timeoutSeconds, TimeUnit.SECONDS);
/*  23:    */   }
/*  24:    */   
/*  25:    */   public WriteTimeoutHandler(long timeout, TimeUnit unit)
/*  26:    */   {
/*  27: 96 */     if (unit == null) {
/*  28: 97 */       throw new NullPointerException("unit");
/*  29:    */     }
/*  30:100 */     if (timeout <= 0L) {
/*  31:101 */       this.timeoutNanos = 0L;
/*  32:    */     } else {
/*  33:103 */       this.timeoutNanos = Math.max(unit.toNanos(timeout), MIN_TIMEOUT_NANOS);
/*  34:    */     }
/*  35:    */   }
/*  36:    */   
/*  37:    */   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
/*  38:    */     throws Exception
/*  39:    */   {
/*  40:109 */     if (this.timeoutNanos > 0L)
/*  41:    */     {
/*  42:110 */       promise = promise.unvoid();
/*  43:111 */       scheduleTimeout(ctx, promise);
/*  44:    */     }
/*  45:113 */     ctx.write(msg, promise);
/*  46:    */   }
/*  47:    */   
/*  48:    */   public void handlerRemoved(ChannelHandlerContext ctx)
/*  49:    */     throws Exception
/*  50:    */   {
/*  51:118 */     WriteTimeoutTask task = this.lastTask;
/*  52:119 */     this.lastTask = null;
/*  53:120 */     while (task != null)
/*  54:    */     {
/*  55:121 */       task.scheduledFuture.cancel(false);
/*  56:122 */       WriteTimeoutTask prev = task.prev;
/*  57:123 */       task.prev = null;
/*  58:124 */       task.next = null;
/*  59:125 */       task = prev;
/*  60:    */     }
/*  61:    */   }
/*  62:    */   
/*  63:    */   private void scheduleTimeout(ChannelHandlerContext ctx, ChannelPromise promise)
/*  64:    */   {
/*  65:131 */     WriteTimeoutTask task = new WriteTimeoutTask(ctx, promise);
/*  66:132 */     task.scheduledFuture = ctx.executor().schedule(task, this.timeoutNanos, TimeUnit.NANOSECONDS);
/*  67:134 */     if (!task.scheduledFuture.isDone())
/*  68:    */     {
/*  69:135 */       addWriteTimeoutTask(task);
/*  70:    */       
/*  71:    */ 
/*  72:138 */       promise.addListener(task);
/*  73:    */     }
/*  74:    */   }
/*  75:    */   
/*  76:    */   private void addWriteTimeoutTask(WriteTimeoutTask task)
/*  77:    */   {
/*  78:143 */     if (this.lastTask == null)
/*  79:    */     {
/*  80:144 */       this.lastTask = task;
/*  81:    */     }
/*  82:    */     else
/*  83:    */     {
/*  84:146 */       this.lastTask.next = task;
/*  85:147 */       task.prev = this.lastTask;
/*  86:148 */       this.lastTask = task;
/*  87:    */     }
/*  88:    */   }
/*  89:    */   
/*  90:    */   private void removeWriteTimeoutTask(WriteTimeoutTask task)
/*  91:    */   {
/*  92:153 */     if (task == this.lastTask)
/*  93:    */     {
/*  94:155 */       assert (task.next == null);
/*  95:156 */       this.lastTask = this.lastTask.prev;
/*  96:157 */       if (this.lastTask != null) {
/*  97:158 */         this.lastTask.next = null;
/*  98:    */       }
/*  99:    */     }
/* 100:    */     else
/* 101:    */     {
/* 102:160 */       if ((task.prev == null) && (task.next == null)) {
/* 103:162 */         return;
/* 104:    */       }
/* 105:163 */       if (task.prev == null)
/* 106:    */       {
/* 107:165 */         task.next.prev = null;
/* 108:    */       }
/* 109:    */       else
/* 110:    */       {
/* 111:167 */         task.prev.next = task.next;
/* 112:168 */         task.next.prev = task.prev;
/* 113:    */       }
/* 114:    */     }
/* 115:170 */     task.prev = null;
/* 116:171 */     task.next = null;
/* 117:    */   }
/* 118:    */   
/* 119:    */   protected void writeTimedOut(ChannelHandlerContext ctx)
/* 120:    */     throws Exception
/* 121:    */   {
/* 122:178 */     if (!this.closed)
/* 123:    */     {
/* 124:179 */       ctx.fireExceptionCaught(WriteTimeoutException.INSTANCE);
/* 125:180 */       ctx.close();
/* 126:181 */       this.closed = true;
/* 127:    */     }
/* 128:    */   }
/* 129:    */   
/* 130:    */   private final class WriteTimeoutTask
/* 131:    */     implements Runnable, ChannelFutureListener
/* 132:    */   {
/* 133:    */     private final ChannelHandlerContext ctx;
/* 134:    */     private final ChannelPromise promise;
/* 135:    */     WriteTimeoutTask prev;
/* 136:    */     WriteTimeoutTask next;
/* 137:    */     ScheduledFuture<?> scheduledFuture;
/* 138:    */     
/* 139:    */     WriteTimeoutTask(ChannelHandlerContext ctx, ChannelPromise promise)
/* 140:    */     {
/* 141:197 */       this.ctx = ctx;
/* 142:198 */       this.promise = promise;
/* 143:    */     }
/* 144:    */     
/* 145:    */     public void run()
/* 146:    */     {
/* 147:206 */       if (!this.promise.isDone()) {
/* 148:    */         try
/* 149:    */         {
/* 150:208 */           WriteTimeoutHandler.this.writeTimedOut(this.ctx);
/* 151:    */         }
/* 152:    */         catch (Throwable t)
/* 153:    */         {
/* 154:210 */           this.ctx.fireExceptionCaught(t);
/* 155:    */         }
/* 156:    */       }
/* 157:213 */       WriteTimeoutHandler.this.removeWriteTimeoutTask(this);
/* 158:    */     }
/* 159:    */     
/* 160:    */     public void operationComplete(ChannelFuture future)
/* 161:    */       throws Exception
/* 162:    */     {
/* 163:219 */       this.scheduledFuture.cancel(false);
/* 164:220 */       WriteTimeoutHandler.this.removeWriteTimeoutTask(this);
/* 165:    */     }
/* 166:    */   }
/* 167:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.timeout.WriteTimeoutHandler
 * JD-Core Version:    0.7.0.1
 */