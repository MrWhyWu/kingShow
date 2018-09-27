/*   1:    */ package io.netty.channel.nio;
/*   2:    */ 
/*   3:    */ import io.netty.channel.DefaultSelectStrategyFactory;
/*   4:    */ import io.netty.channel.EventLoop;
/*   5:    */ import io.netty.channel.MultithreadEventLoopGroup;
/*   6:    */ import io.netty.channel.SelectStrategyFactory;
/*   7:    */ import io.netty.util.concurrent.EventExecutor;
/*   8:    */ import io.netty.util.concurrent.EventExecutorChooserFactory;
/*   9:    */ import io.netty.util.concurrent.RejectedExecutionHandler;
/*  10:    */ import io.netty.util.concurrent.RejectedExecutionHandlers;
/*  11:    */ import java.nio.channels.spi.SelectorProvider;
/*  12:    */ import java.util.concurrent.Executor;
/*  13:    */ import java.util.concurrent.ThreadFactory;
/*  14:    */ 
/*  15:    */ public class NioEventLoopGroup
/*  16:    */   extends MultithreadEventLoopGroup
/*  17:    */ {
/*  18:    */   public NioEventLoopGroup()
/*  19:    */   {
/*  20: 43 */     this(0);
/*  21:    */   }
/*  22:    */   
/*  23:    */   public NioEventLoopGroup(int nThreads)
/*  24:    */   {
/*  25: 51 */     this(nThreads, (Executor)null);
/*  26:    */   }
/*  27:    */   
/*  28:    */   public NioEventLoopGroup(int nThreads, ThreadFactory threadFactory)
/*  29:    */   {
/*  30: 59 */     this(nThreads, threadFactory, SelectorProvider.provider());
/*  31:    */   }
/*  32:    */   
/*  33:    */   public NioEventLoopGroup(int nThreads, Executor executor)
/*  34:    */   {
/*  35: 63 */     this(nThreads, executor, SelectorProvider.provider());
/*  36:    */   }
/*  37:    */   
/*  38:    */   public NioEventLoopGroup(int nThreads, ThreadFactory threadFactory, SelectorProvider selectorProvider)
/*  39:    */   {
/*  40: 72 */     this(nThreads, threadFactory, selectorProvider, DefaultSelectStrategyFactory.INSTANCE);
/*  41:    */   }
/*  42:    */   
/*  43:    */   public NioEventLoopGroup(int nThreads, ThreadFactory threadFactory, SelectorProvider selectorProvider, SelectStrategyFactory selectStrategyFactory)
/*  44:    */   {
/*  45: 77 */     super(nThreads, threadFactory, new Object[] { selectorProvider, selectStrategyFactory, RejectedExecutionHandlers.reject() });
/*  46:    */   }
/*  47:    */   
/*  48:    */   public NioEventLoopGroup(int nThreads, Executor executor, SelectorProvider selectorProvider)
/*  49:    */   {
/*  50: 82 */     this(nThreads, executor, selectorProvider, DefaultSelectStrategyFactory.INSTANCE);
/*  51:    */   }
/*  52:    */   
/*  53:    */   public NioEventLoopGroup(int nThreads, Executor executor, SelectorProvider selectorProvider, SelectStrategyFactory selectStrategyFactory)
/*  54:    */   {
/*  55: 87 */     super(nThreads, executor, new Object[] { selectorProvider, selectStrategyFactory, RejectedExecutionHandlers.reject() });
/*  56:    */   }
/*  57:    */   
/*  58:    */   public NioEventLoopGroup(int nThreads, Executor executor, EventExecutorChooserFactory chooserFactory, SelectorProvider selectorProvider, SelectStrategyFactory selectStrategyFactory)
/*  59:    */   {
/*  60: 93 */     super(nThreads, executor, chooserFactory, new Object[] { selectorProvider, selectStrategyFactory, 
/*  61: 94 */       RejectedExecutionHandlers.reject() });
/*  62:    */   }
/*  63:    */   
/*  64:    */   public NioEventLoopGroup(int nThreads, Executor executor, EventExecutorChooserFactory chooserFactory, SelectorProvider selectorProvider, SelectStrategyFactory selectStrategyFactory, RejectedExecutionHandler rejectedExecutionHandler)
/*  65:    */   {
/*  66:101 */     super(nThreads, executor, chooserFactory, new Object[] { selectorProvider, selectStrategyFactory, rejectedExecutionHandler });
/*  67:    */   }
/*  68:    */   
/*  69:    */   public void setIoRatio(int ioRatio)
/*  70:    */   {
/*  71:109 */     for (EventExecutor e : this) {
/*  72:110 */       ((NioEventLoop)e).setIoRatio(ioRatio);
/*  73:    */     }
/*  74:    */   }
/*  75:    */   
/*  76:    */   public void rebuildSelectors()
/*  77:    */   {
/*  78:119 */     for (EventExecutor e : this) {
/*  79:120 */       ((NioEventLoop)e).rebuildSelector();
/*  80:    */     }
/*  81:    */   }
/*  82:    */   
/*  83:    */   protected EventLoop newChild(Executor executor, Object... args)
/*  84:    */     throws Exception
/*  85:    */   {
/*  86:126 */     return new NioEventLoop(this, executor, (SelectorProvider)args[0], ((SelectStrategyFactory)args[1])
/*  87:127 */       .newSelectStrategy(), (RejectedExecutionHandler)args[2]);
/*  88:    */   }
/*  89:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.nio.NioEventLoopGroup
 * JD-Core Version:    0.7.0.1
 */