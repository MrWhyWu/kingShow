/*   1:    */ package io.netty.handler.flush;
/*   2:    */ 
/*   3:    */ import io.netty.channel.Channel;
/*   4:    */ import io.netty.channel.ChannelDuplexHandler;
/*   5:    */ import io.netty.channel.ChannelHandlerContext;
/*   6:    */ import io.netty.channel.ChannelPromise;
/*   7:    */ import io.netty.channel.EventLoop;
/*   8:    */ import java.util.concurrent.Future;
/*   9:    */ 
/*  10:    */ public class FlushConsolidationHandler
/*  11:    */   extends ChannelDuplexHandler
/*  12:    */ {
/*  13:    */   private final int explicitFlushAfterFlushes;
/*  14:    */   private final boolean consolidateWhenNoReadInProgress;
/*  15:    */   private final Runnable flushTask;
/*  16:    */   private int flushPendingCount;
/*  17:    */   private boolean readInProgress;
/*  18:    */   private ChannelHandlerContext ctx;
/*  19:    */   private Future<?> nextScheduledFlush;
/*  20:    */   
/*  21:    */   public FlushConsolidationHandler()
/*  22:    */   {
/*  23: 71 */     this(256, false);
/*  24:    */   }
/*  25:    */   
/*  26:    */   public FlushConsolidationHandler(int explicitFlushAfterFlushes)
/*  27:    */   {
/*  28: 80 */     this(explicitFlushAfterFlushes, false);
/*  29:    */   }
/*  30:    */   
/*  31:    */   public FlushConsolidationHandler(int explicitFlushAfterFlushes, boolean consolidateWhenNoReadInProgress)
/*  32:    */   {
/*  33: 91 */     if (explicitFlushAfterFlushes <= 0) {
/*  34: 92 */       throw new IllegalArgumentException("explicitFlushAfterFlushes: " + explicitFlushAfterFlushes + " (expected: > 0)");
/*  35:    */     }
/*  36: 95 */     this.explicitFlushAfterFlushes = explicitFlushAfterFlushes;
/*  37: 96 */     this.consolidateWhenNoReadInProgress = consolidateWhenNoReadInProgress;
/*  38: 97 */     this.flushTask = (consolidateWhenNoReadInProgress ? new Runnable()
/*  39:    */     {
/*  40:    */       public void run()
/*  41:    */       {
/*  42:101 */         if ((FlushConsolidationHandler.this.flushPendingCount > 0) && (!FlushConsolidationHandler.this.readInProgress))
/*  43:    */         {
/*  44:102 */           FlushConsolidationHandler.this.flushPendingCount = 0;
/*  45:103 */           FlushConsolidationHandler.this.ctx.flush();
/*  46:104 */           FlushConsolidationHandler.this.nextScheduledFlush = null;
/*  47:    */         }
/*  48:    */       }
/*  49:104 */     } : null);
/*  50:    */   }
/*  51:    */   
/*  52:    */   public void handlerAdded(ChannelHandlerContext ctx)
/*  53:    */     throws Exception
/*  54:    */   {
/*  55:113 */     this.ctx = ctx;
/*  56:    */   }
/*  57:    */   
/*  58:    */   public void flush(ChannelHandlerContext ctx)
/*  59:    */     throws Exception
/*  60:    */   {
/*  61:118 */     if (this.readInProgress)
/*  62:    */     {
/*  63:121 */       if (++this.flushPendingCount == this.explicitFlushAfterFlushes) {
/*  64:122 */         flushNow(ctx);
/*  65:    */       }
/*  66:    */     }
/*  67:124 */     else if (this.consolidateWhenNoReadInProgress)
/*  68:    */     {
/*  69:126 */       if (++this.flushPendingCount == this.explicitFlushAfterFlushes) {
/*  70:127 */         flushNow(ctx);
/*  71:    */       } else {
/*  72:129 */         scheduleFlush(ctx);
/*  73:    */       }
/*  74:    */     }
/*  75:    */     else {
/*  76:133 */       flushNow(ctx);
/*  77:    */     }
/*  78:    */   }
/*  79:    */   
/*  80:    */   public void channelReadComplete(ChannelHandlerContext ctx)
/*  81:    */     throws Exception
/*  82:    */   {
/*  83:140 */     resetReadAndFlushIfNeeded(ctx);
/*  84:141 */     ctx.fireChannelReadComplete();
/*  85:    */   }
/*  86:    */   
/*  87:    */   public void channelRead(ChannelHandlerContext ctx, Object msg)
/*  88:    */     throws Exception
/*  89:    */   {
/*  90:146 */     this.readInProgress = true;
/*  91:147 */     ctx.fireChannelRead(msg);
/*  92:    */   }
/*  93:    */   
/*  94:    */   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
/*  95:    */     throws Exception
/*  96:    */   {
/*  97:153 */     resetReadAndFlushIfNeeded(ctx);
/*  98:154 */     ctx.fireExceptionCaught(cause);
/*  99:    */   }
/* 100:    */   
/* 101:    */   public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise)
/* 102:    */     throws Exception
/* 103:    */   {
/* 104:160 */     resetReadAndFlushIfNeeded(ctx);
/* 105:161 */     ctx.disconnect(promise);
/* 106:    */   }
/* 107:    */   
/* 108:    */   public void close(ChannelHandlerContext ctx, ChannelPromise promise)
/* 109:    */     throws Exception
/* 110:    */   {
/* 111:167 */     resetReadAndFlushIfNeeded(ctx);
/* 112:168 */     ctx.close(promise);
/* 113:    */   }
/* 114:    */   
/* 115:    */   public void channelWritabilityChanged(ChannelHandlerContext ctx)
/* 116:    */     throws Exception
/* 117:    */   {
/* 118:173 */     if (!ctx.channel().isWritable()) {
/* 119:175 */       flushIfNeeded(ctx);
/* 120:    */     }
/* 121:177 */     ctx.fireChannelWritabilityChanged();
/* 122:    */   }
/* 123:    */   
/* 124:    */   public void handlerRemoved(ChannelHandlerContext ctx)
/* 125:    */     throws Exception
/* 126:    */   {
/* 127:182 */     flushIfNeeded(ctx);
/* 128:    */   }
/* 129:    */   
/* 130:    */   private void resetReadAndFlushIfNeeded(ChannelHandlerContext ctx)
/* 131:    */   {
/* 132:186 */     this.readInProgress = false;
/* 133:187 */     flushIfNeeded(ctx);
/* 134:    */   }
/* 135:    */   
/* 136:    */   private void flushIfNeeded(ChannelHandlerContext ctx)
/* 137:    */   {
/* 138:191 */     if (this.flushPendingCount > 0) {
/* 139:192 */       flushNow(ctx);
/* 140:    */     }
/* 141:    */   }
/* 142:    */   
/* 143:    */   private void flushNow(ChannelHandlerContext ctx)
/* 144:    */   {
/* 145:197 */     cancelScheduledFlush();
/* 146:198 */     this.flushPendingCount = 0;
/* 147:199 */     ctx.flush();
/* 148:    */   }
/* 149:    */   
/* 150:    */   private void scheduleFlush(ChannelHandlerContext ctx)
/* 151:    */   {
/* 152:203 */     if (this.nextScheduledFlush == null) {
/* 153:205 */       this.nextScheduledFlush = ctx.channel().eventLoop().submit(this.flushTask);
/* 154:    */     }
/* 155:    */   }
/* 156:    */   
/* 157:    */   private void cancelScheduledFlush()
/* 158:    */   {
/* 159:210 */     if (this.nextScheduledFlush != null)
/* 160:    */     {
/* 161:211 */       this.nextScheduledFlush.cancel(false);
/* 162:212 */       this.nextScheduledFlush = null;
/* 163:    */     }
/* 164:    */   }
/* 165:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.flush.FlushConsolidationHandler
 * JD-Core Version:    0.7.0.1
 */