/*   1:    */ package io.netty.channel.embedded;
/*   2:    */ 
/*   3:    */ import io.netty.channel.Channel;
/*   4:    */ import io.netty.channel.Channel.Unsafe;
/*   5:    */ import io.netty.channel.ChannelFuture;
/*   6:    */ import io.netty.channel.ChannelPromise;
/*   7:    */ import io.netty.channel.DefaultChannelPromise;
/*   8:    */ import io.netty.channel.EventLoop;
/*   9:    */ import io.netty.channel.EventLoopGroup;
/*  10:    */ import io.netty.util.concurrent.AbstractScheduledEventExecutor;
/*  11:    */ import io.netty.util.concurrent.Future;
/*  12:    */ import io.netty.util.internal.ObjectUtil;
/*  13:    */ import java.util.ArrayDeque;
/*  14:    */ import java.util.Queue;
/*  15:    */ import java.util.concurrent.TimeUnit;
/*  16:    */ 
/*  17:    */ final class EmbeddedEventLoop
/*  18:    */   extends AbstractScheduledEventExecutor
/*  19:    */   implements EventLoop
/*  20:    */ {
/*  21: 34 */   private final Queue<Runnable> tasks = new ArrayDeque(2);
/*  22:    */   
/*  23:    */   public EventLoopGroup parent()
/*  24:    */   {
/*  25: 38 */     return (EventLoopGroup)super.parent();
/*  26:    */   }
/*  27:    */   
/*  28:    */   public EventLoop next()
/*  29:    */   {
/*  30: 43 */     return (EventLoop)super.next();
/*  31:    */   }
/*  32:    */   
/*  33:    */   public void execute(Runnable command)
/*  34:    */   {
/*  35: 48 */     if (command == null) {
/*  36: 49 */       throw new NullPointerException("command");
/*  37:    */     }
/*  38: 51 */     this.tasks.add(command);
/*  39:    */   }
/*  40:    */   
/*  41:    */   void runTasks()
/*  42:    */   {
/*  43:    */     for (;;)
/*  44:    */     {
/*  45: 56 */       Runnable task = (Runnable)this.tasks.poll();
/*  46: 57 */       if (task == null) {
/*  47:    */         break;
/*  48:    */       }
/*  49: 61 */       task.run();
/*  50:    */     }
/*  51:    */   }
/*  52:    */   
/*  53:    */   long runScheduledTasks()
/*  54:    */   {
/*  55: 66 */     long time = AbstractScheduledEventExecutor.nanoTime();
/*  56:    */     for (;;)
/*  57:    */     {
/*  58: 68 */       Runnable task = pollScheduledTask(time);
/*  59: 69 */       if (task == null) {
/*  60: 70 */         return nextScheduledTaskNano();
/*  61:    */       }
/*  62: 73 */       task.run();
/*  63:    */     }
/*  64:    */   }
/*  65:    */   
/*  66:    */   long nextScheduledTask()
/*  67:    */   {
/*  68: 78 */     return nextScheduledTaskNano();
/*  69:    */   }
/*  70:    */   
/*  71:    */   protected void cancelScheduledTasks()
/*  72:    */   {
/*  73: 83 */     super.cancelScheduledTasks();
/*  74:    */   }
/*  75:    */   
/*  76:    */   public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit)
/*  77:    */   {
/*  78: 88 */     throw new UnsupportedOperationException();
/*  79:    */   }
/*  80:    */   
/*  81:    */   public Future<?> terminationFuture()
/*  82:    */   {
/*  83: 93 */     throw new UnsupportedOperationException();
/*  84:    */   }
/*  85:    */   
/*  86:    */   @Deprecated
/*  87:    */   public void shutdown()
/*  88:    */   {
/*  89: 99 */     throw new UnsupportedOperationException();
/*  90:    */   }
/*  91:    */   
/*  92:    */   public boolean isShuttingDown()
/*  93:    */   {
/*  94:104 */     return false;
/*  95:    */   }
/*  96:    */   
/*  97:    */   public boolean isShutdown()
/*  98:    */   {
/*  99:109 */     return false;
/* 100:    */   }
/* 101:    */   
/* 102:    */   public boolean isTerminated()
/* 103:    */   {
/* 104:114 */     return false;
/* 105:    */   }
/* 106:    */   
/* 107:    */   public boolean awaitTermination(long timeout, TimeUnit unit)
/* 108:    */   {
/* 109:119 */     return false;
/* 110:    */   }
/* 111:    */   
/* 112:    */   public ChannelFuture register(Channel channel)
/* 113:    */   {
/* 114:124 */     return register(new DefaultChannelPromise(channel, this));
/* 115:    */   }
/* 116:    */   
/* 117:    */   public ChannelFuture register(ChannelPromise promise)
/* 118:    */   {
/* 119:129 */     ObjectUtil.checkNotNull(promise, "promise");
/* 120:130 */     promise.channel().unsafe().register(this, promise);
/* 121:131 */     return promise;
/* 122:    */   }
/* 123:    */   
/* 124:    */   @Deprecated
/* 125:    */   public ChannelFuture register(Channel channel, ChannelPromise promise)
/* 126:    */   {
/* 127:137 */     channel.unsafe().register(this, promise);
/* 128:138 */     return promise;
/* 129:    */   }
/* 130:    */   
/* 131:    */   public boolean inEventLoop()
/* 132:    */   {
/* 133:143 */     return true;
/* 134:    */   }
/* 135:    */   
/* 136:    */   public boolean inEventLoop(Thread thread)
/* 137:    */   {
/* 138:148 */     return true;
/* 139:    */   }
/* 140:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.embedded.EmbeddedEventLoop
 * JD-Core Version:    0.7.0.1
 */