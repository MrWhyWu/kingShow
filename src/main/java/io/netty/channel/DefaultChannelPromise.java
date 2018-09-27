/*   1:    */ package io.netty.channel;
/*   2:    */ 
/*   3:    */ import io.netty.util.concurrent.DefaultPromise;
/*   4:    */ import io.netty.util.concurrent.EventExecutor;
/*   5:    */ import io.netty.util.concurrent.Future;
/*   6:    */ import io.netty.util.concurrent.GenericFutureListener;
/*   7:    */ 
/*   8:    */ public class DefaultChannelPromise
/*   9:    */   extends DefaultPromise<Void>
/*  10:    */   implements ChannelPromise, ChannelFlushPromiseNotifier.FlushCheckpoint
/*  11:    */ {
/*  12:    */   private final Channel channel;
/*  13:    */   private long checkpoint;
/*  14:    */   
/*  15:    */   public DefaultChannelPromise(Channel channel)
/*  16:    */   {
/*  17: 40 */     this.channel = channel;
/*  18:    */   }
/*  19:    */   
/*  20:    */   public DefaultChannelPromise(Channel channel, EventExecutor executor)
/*  21:    */   {
/*  22: 50 */     super(executor);
/*  23: 51 */     this.channel = channel;
/*  24:    */   }
/*  25:    */   
/*  26:    */   protected EventExecutor executor()
/*  27:    */   {
/*  28: 56 */     EventExecutor e = super.executor();
/*  29: 57 */     if (e == null) {
/*  30: 58 */       return channel().eventLoop();
/*  31:    */     }
/*  32: 60 */     return e;
/*  33:    */   }
/*  34:    */   
/*  35:    */   public Channel channel()
/*  36:    */   {
/*  37: 66 */     return this.channel;
/*  38:    */   }
/*  39:    */   
/*  40:    */   public ChannelPromise setSuccess()
/*  41:    */   {
/*  42: 71 */     return setSuccess(null);
/*  43:    */   }
/*  44:    */   
/*  45:    */   public ChannelPromise setSuccess(Void result)
/*  46:    */   {
/*  47: 76 */     super.setSuccess(result);
/*  48: 77 */     return this;
/*  49:    */   }
/*  50:    */   
/*  51:    */   public boolean trySuccess()
/*  52:    */   {
/*  53: 82 */     return trySuccess(null);
/*  54:    */   }
/*  55:    */   
/*  56:    */   public ChannelPromise setFailure(Throwable cause)
/*  57:    */   {
/*  58: 87 */     super.setFailure(cause);
/*  59: 88 */     return this;
/*  60:    */   }
/*  61:    */   
/*  62:    */   public ChannelPromise addListener(GenericFutureListener<? extends Future<? super Void>> listener)
/*  63:    */   {
/*  64: 93 */     super.addListener(listener);
/*  65: 94 */     return this;
/*  66:    */   }
/*  67:    */   
/*  68:    */   public ChannelPromise addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners)
/*  69:    */   {
/*  70: 99 */     super.addListeners(listeners);
/*  71:100 */     return this;
/*  72:    */   }
/*  73:    */   
/*  74:    */   public ChannelPromise removeListener(GenericFutureListener<? extends Future<? super Void>> listener)
/*  75:    */   {
/*  76:105 */     super.removeListener(listener);
/*  77:106 */     return this;
/*  78:    */   }
/*  79:    */   
/*  80:    */   public ChannelPromise removeListeners(GenericFutureListener<? extends Future<? super Void>>... listeners)
/*  81:    */   {
/*  82:111 */     super.removeListeners(listeners);
/*  83:112 */     return this;
/*  84:    */   }
/*  85:    */   
/*  86:    */   public ChannelPromise sync()
/*  87:    */     throws InterruptedException
/*  88:    */   {
/*  89:117 */     super.sync();
/*  90:118 */     return this;
/*  91:    */   }
/*  92:    */   
/*  93:    */   public ChannelPromise syncUninterruptibly()
/*  94:    */   {
/*  95:123 */     super.syncUninterruptibly();
/*  96:124 */     return this;
/*  97:    */   }
/*  98:    */   
/*  99:    */   public ChannelPromise await()
/* 100:    */     throws InterruptedException
/* 101:    */   {
/* 102:129 */     super.await();
/* 103:130 */     return this;
/* 104:    */   }
/* 105:    */   
/* 106:    */   public ChannelPromise awaitUninterruptibly()
/* 107:    */   {
/* 108:135 */     super.awaitUninterruptibly();
/* 109:136 */     return this;
/* 110:    */   }
/* 111:    */   
/* 112:    */   public long flushCheckpoint()
/* 113:    */   {
/* 114:141 */     return this.checkpoint;
/* 115:    */   }
/* 116:    */   
/* 117:    */   public void flushCheckpoint(long checkpoint)
/* 118:    */   {
/* 119:146 */     this.checkpoint = checkpoint;
/* 120:    */   }
/* 121:    */   
/* 122:    */   public ChannelPromise promise()
/* 123:    */   {
/* 124:151 */     return this;
/* 125:    */   }
/* 126:    */   
/* 127:    */   protected void checkDeadLock()
/* 128:    */   {
/* 129:156 */     if (channel().isRegistered()) {
/* 130:157 */       super.checkDeadLock();
/* 131:    */     }
/* 132:    */   }
/* 133:    */   
/* 134:    */   public ChannelPromise unvoid()
/* 135:    */   {
/* 136:163 */     return this;
/* 137:    */   }
/* 138:    */   
/* 139:    */   public boolean isVoid()
/* 140:    */   {
/* 141:168 */     return false;
/* 142:    */   }
/* 143:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.DefaultChannelPromise
 * JD-Core Version:    0.7.0.1
 */