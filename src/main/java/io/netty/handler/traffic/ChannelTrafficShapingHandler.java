/*   1:    */ package io.netty.handler.traffic;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.Channel;
/*   5:    */ import io.netty.channel.ChannelHandlerContext;
/*   6:    */ import io.netty.channel.ChannelPromise;
/*   7:    */ import io.netty.util.concurrent.EventExecutor;
/*   8:    */ import java.util.ArrayDeque;
/*   9:    */ import java.util.concurrent.TimeUnit;
/*  10:    */ 
/*  11:    */ public class ChannelTrafficShapingHandler
/*  12:    */   extends AbstractTrafficShapingHandler
/*  13:    */ {
/*  14: 66 */   private final ArrayDeque<ToSend> messagesQueue = new ArrayDeque();
/*  15:    */   private long queueSize;
/*  16:    */   
/*  17:    */   public ChannelTrafficShapingHandler(long writeLimit, long readLimit, long checkInterval, long maxTime)
/*  18:    */   {
/*  19: 84 */     super(writeLimit, readLimit, checkInterval, maxTime);
/*  20:    */   }
/*  21:    */   
/*  22:    */   public ChannelTrafficShapingHandler(long writeLimit, long readLimit, long checkInterval)
/*  23:    */   {
/*  24:101 */     super(writeLimit, readLimit, checkInterval);
/*  25:    */   }
/*  26:    */   
/*  27:    */   public ChannelTrafficShapingHandler(long writeLimit, long readLimit)
/*  28:    */   {
/*  29:115 */     super(writeLimit, readLimit);
/*  30:    */   }
/*  31:    */   
/*  32:    */   public ChannelTrafficShapingHandler(long checkInterval)
/*  33:    */   {
/*  34:127 */     super(checkInterval);
/*  35:    */   }
/*  36:    */   
/*  37:    */   public void handlerAdded(ChannelHandlerContext ctx)
/*  38:    */     throws Exception
/*  39:    */   {
/*  40:133 */     TrafficCounter trafficCounter = new TrafficCounter(this, ctx.executor(), "ChannelTC" + ctx.channel().hashCode(), this.checkInterval);
/*  41:134 */     setTrafficCounter(trafficCounter);
/*  42:135 */     trafficCounter.start();
/*  43:136 */     super.handlerAdded(ctx);
/*  44:    */   }
/*  45:    */   
/*  46:    */   public void handlerRemoved(ChannelHandlerContext ctx)
/*  47:    */     throws Exception
/*  48:    */   {
/*  49:141 */     this.trafficCounter.stop();
/*  50:143 */     synchronized (this)
/*  51:    */     {
/*  52:144 */       if (ctx.channel().isActive()) {
/*  53:145 */         for (ToSend toSend : this.messagesQueue)
/*  54:    */         {
/*  55:146 */           long size = calculateSize(toSend.toSend);
/*  56:147 */           this.trafficCounter.bytesRealWriteFlowControl(size);
/*  57:148 */           this.queueSize -= size;
/*  58:149 */           ctx.write(toSend.toSend, toSend.promise);
/*  59:    */         }
/*  60:    */       } else {
/*  61:152 */         for (ToSend toSend : this.messagesQueue) {
/*  62:153 */           if ((toSend.toSend instanceof ByteBuf)) {
/*  63:154 */             ((ByteBuf)toSend.toSend).release();
/*  64:    */           }
/*  65:    */         }
/*  66:    */       }
/*  67:158 */       this.messagesQueue.clear();
/*  68:    */     }
/*  69:160 */     releaseWriteSuspended(ctx);
/*  70:161 */     releaseReadSuspended(ctx);
/*  71:162 */     super.handlerRemoved(ctx);
/*  72:    */   }
/*  73:    */   
/*  74:    */   private static final class ToSend
/*  75:    */   {
/*  76:    */     final long relativeTimeAction;
/*  77:    */     final Object toSend;
/*  78:    */     final ChannelPromise promise;
/*  79:    */     
/*  80:    */     private ToSend(long delay, Object toSend, ChannelPromise promise)
/*  81:    */     {
/*  82:171 */       this.relativeTimeAction = delay;
/*  83:172 */       this.toSend = toSend;
/*  84:173 */       this.promise = promise;
/*  85:    */     }
/*  86:    */   }
/*  87:    */   
/*  88:    */   void submitWrite(final ChannelHandlerContext ctx, Object msg, long size, long delay, long now, ChannelPromise promise)
/*  89:    */   {
/*  90:183 */     synchronized (this)
/*  91:    */     {
/*  92:184 */       if ((delay == 0L) && (this.messagesQueue.isEmpty()))
/*  93:    */       {
/*  94:185 */         this.trafficCounter.bytesRealWriteFlowControl(size);
/*  95:186 */         ctx.write(msg, promise);
/*  96:187 */         return;
/*  97:    */       }
/*  98:189 */       ToSend newToSend = new ToSend(delay + now, msg, promise, null);
/*  99:190 */       this.messagesQueue.addLast(newToSend);
/* 100:191 */       this.queueSize += size;
/* 101:192 */       checkWriteSuspend(ctx, delay, this.queueSize);
/* 102:    */     }
/* 103:    */     ToSend newToSend;
/* 104:194 */     final long futureNow = newToSend.relativeTimeAction;
/* 105:195 */     ctx.executor().schedule(new Runnable()
/* 106:    */     {
/* 107:    */       public void run()
/* 108:    */       {
/* 109:198 */         ChannelTrafficShapingHandler.this.sendAllValid(ctx, futureNow);
/* 110:    */       }
/* 111:198 */     }, delay, TimeUnit.MILLISECONDS);
/* 112:    */   }
/* 113:    */   
/* 114:    */   private void sendAllValid(ChannelHandlerContext ctx, long now)
/* 115:    */   {
/* 116:205 */     synchronized (this)
/* 117:    */     {
/* 118:206 */       for (ToSend newToSend = (ToSend)this.messagesQueue.pollFirst(); newToSend != null; newToSend = (ToSend)this.messagesQueue.pollFirst()) {
/* 119:208 */         if (newToSend.relativeTimeAction <= now)
/* 120:    */         {
/* 121:209 */           long size = calculateSize(newToSend.toSend);
/* 122:210 */           this.trafficCounter.bytesRealWriteFlowControl(size);
/* 123:211 */           this.queueSize -= size;
/* 124:212 */           ctx.write(newToSend.toSend, newToSend.promise);
/* 125:    */         }
/* 126:    */         else
/* 127:    */         {
/* 128:214 */           this.messagesQueue.addFirst(newToSend);
/* 129:215 */           break;
/* 130:    */         }
/* 131:    */       }
/* 132:218 */       if (this.messagesQueue.isEmpty()) {
/* 133:219 */         releaseWriteSuspended(ctx);
/* 134:    */       }
/* 135:    */     }
/* 136:222 */     ctx.flush();
/* 137:    */   }
/* 138:    */   
/* 139:    */   public long queueSize()
/* 140:    */   {
/* 141:229 */     return this.queueSize;
/* 142:    */   }
/* 143:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.traffic.ChannelTrafficShapingHandler
 * JD-Core Version:    0.7.0.1
 */