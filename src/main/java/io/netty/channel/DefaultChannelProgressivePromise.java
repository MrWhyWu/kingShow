/*   1:    */ package io.netty.channel;
/*   2:    */ 
/*   3:    */ import io.netty.util.concurrent.DefaultProgressivePromise;
/*   4:    */ import io.netty.util.concurrent.EventExecutor;
/*   5:    */ import io.netty.util.concurrent.Future;
/*   6:    */ import io.netty.util.concurrent.GenericFutureListener;
/*   7:    */ 
/*   8:    */ public class DefaultChannelProgressivePromise
/*   9:    */   extends DefaultProgressivePromise<Void>
/*  10:    */   implements ChannelProgressivePromise, ChannelFlushPromiseNotifier.FlushCheckpoint
/*  11:    */ {
/*  12:    */   private final Channel channel;
/*  13:    */   private long checkpoint;
/*  14:    */   
/*  15:    */   public DefaultChannelProgressivePromise(Channel channel)
/*  16:    */   {
/*  17: 42 */     this.channel = channel;
/*  18:    */   }
/*  19:    */   
/*  20:    */   public DefaultChannelProgressivePromise(Channel channel, EventExecutor executor)
/*  21:    */   {
/*  22: 52 */     super(executor);
/*  23: 53 */     this.channel = channel;
/*  24:    */   }
/*  25:    */   
/*  26:    */   protected EventExecutor executor()
/*  27:    */   {
/*  28: 58 */     EventExecutor e = super.executor();
/*  29: 59 */     if (e == null) {
/*  30: 60 */       return channel().eventLoop();
/*  31:    */     }
/*  32: 62 */     return e;
/*  33:    */   }
/*  34:    */   
/*  35:    */   public Channel channel()
/*  36:    */   {
/*  37: 68 */     return this.channel;
/*  38:    */   }
/*  39:    */   
/*  40:    */   public ChannelProgressivePromise setSuccess()
/*  41:    */   {
/*  42: 73 */     return setSuccess(null);
/*  43:    */   }
/*  44:    */   
/*  45:    */   public ChannelProgressivePromise setSuccess(Void result)
/*  46:    */   {
/*  47: 78 */     super.setSuccess(result);
/*  48: 79 */     return this;
/*  49:    */   }
/*  50:    */   
/*  51:    */   public boolean trySuccess()
/*  52:    */   {
/*  53: 84 */     return trySuccess(null);
/*  54:    */   }
/*  55:    */   
/*  56:    */   public ChannelProgressivePromise setFailure(Throwable cause)
/*  57:    */   {
/*  58: 89 */     super.setFailure(cause);
/*  59: 90 */     return this;
/*  60:    */   }
/*  61:    */   
/*  62:    */   public ChannelProgressivePromise setProgress(long progress, long total)
/*  63:    */   {
/*  64: 95 */     super.setProgress(progress, total);
/*  65: 96 */     return this;
/*  66:    */   }
/*  67:    */   
/*  68:    */   public ChannelProgressivePromise addListener(GenericFutureListener<? extends Future<? super Void>> listener)
/*  69:    */   {
/*  70:101 */     super.addListener(listener);
/*  71:102 */     return this;
/*  72:    */   }
/*  73:    */   
/*  74:    */   public ChannelProgressivePromise addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners)
/*  75:    */   {
/*  76:107 */     super.addListeners(listeners);
/*  77:108 */     return this;
/*  78:    */   }
/*  79:    */   
/*  80:    */   public ChannelProgressivePromise removeListener(GenericFutureListener<? extends Future<? super Void>> listener)
/*  81:    */   {
/*  82:113 */     super.removeListener(listener);
/*  83:114 */     return this;
/*  84:    */   }
/*  85:    */   
/*  86:    */   public ChannelProgressivePromise removeListeners(GenericFutureListener<? extends Future<? super Void>>... listeners)
/*  87:    */   {
/*  88:120 */     super.removeListeners(listeners);
/*  89:121 */     return this;
/*  90:    */   }
/*  91:    */   
/*  92:    */   public ChannelProgressivePromise sync()
/*  93:    */     throws InterruptedException
/*  94:    */   {
/*  95:126 */     super.sync();
/*  96:127 */     return this;
/*  97:    */   }
/*  98:    */   
/*  99:    */   public ChannelProgressivePromise syncUninterruptibly()
/* 100:    */   {
/* 101:132 */     super.syncUninterruptibly();
/* 102:133 */     return this;
/* 103:    */   }
/* 104:    */   
/* 105:    */   public ChannelProgressivePromise await()
/* 106:    */     throws InterruptedException
/* 107:    */   {
/* 108:138 */     super.await();
/* 109:139 */     return this;
/* 110:    */   }
/* 111:    */   
/* 112:    */   public ChannelProgressivePromise awaitUninterruptibly()
/* 113:    */   {
/* 114:144 */     super.awaitUninterruptibly();
/* 115:145 */     return this;
/* 116:    */   }
/* 117:    */   
/* 118:    */   public long flushCheckpoint()
/* 119:    */   {
/* 120:150 */     return this.checkpoint;
/* 121:    */   }
/* 122:    */   
/* 123:    */   public void flushCheckpoint(long checkpoint)
/* 124:    */   {
/* 125:155 */     this.checkpoint = checkpoint;
/* 126:    */   }
/* 127:    */   
/* 128:    */   public ChannelProgressivePromise promise()
/* 129:    */   {
/* 130:160 */     return this;
/* 131:    */   }
/* 132:    */   
/* 133:    */   protected void checkDeadLock()
/* 134:    */   {
/* 135:165 */     if (channel().isRegistered()) {
/* 136:166 */       super.checkDeadLock();
/* 137:    */     }
/* 138:    */   }
/* 139:    */   
/* 140:    */   public ChannelProgressivePromise unvoid()
/* 141:    */   {
/* 142:172 */     return this;
/* 143:    */   }
/* 144:    */   
/* 145:    */   public boolean isVoid()
/* 146:    */   {
/* 147:177 */     return false;
/* 148:    */   }
/* 149:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.DefaultChannelProgressivePromise
 * JD-Core Version:    0.7.0.1
 */