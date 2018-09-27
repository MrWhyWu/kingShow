/*   1:    */ package io.netty.handler.traffic;
/*   2:    */ 
/*   3:    */ import java.util.concurrent.ConcurrentMap;
/*   4:    */ import java.util.concurrent.ScheduledExecutorService;
/*   5:    */ import java.util.concurrent.ScheduledFuture;
/*   6:    */ import java.util.concurrent.TimeUnit;
/*   7:    */ import java.util.concurrent.atomic.AtomicLong;
/*   8:    */ 
/*   9:    */ public class GlobalChannelTrafficCounter
/*  10:    */   extends TrafficCounter
/*  11:    */ {
/*  12:    */   public GlobalChannelTrafficCounter(GlobalChannelTrafficShapingHandler trafficShapingHandler, ScheduledExecutorService executor, String name, long checkInterval)
/*  13:    */   {
/*  14: 38 */     super(trafficShapingHandler, executor, name, checkInterval);
/*  15: 39 */     if (executor == null) {
/*  16: 40 */       throw new IllegalArgumentException("Executor must not be null");
/*  17:    */     }
/*  18:    */   }
/*  19:    */   
/*  20:    */   private static class MixedTrafficMonitoringTask
/*  21:    */     implements Runnable
/*  22:    */   {
/*  23:    */     private final GlobalChannelTrafficShapingHandler trafficShapingHandler1;
/*  24:    */     private final TrafficCounter counter;
/*  25:    */     
/*  26:    */     MixedTrafficMonitoringTask(GlobalChannelTrafficShapingHandler trafficShapingHandler, TrafficCounter counter)
/*  27:    */     {
/*  28: 66 */       this.trafficShapingHandler1 = trafficShapingHandler;
/*  29: 67 */       this.counter = counter;
/*  30:    */     }
/*  31:    */     
/*  32:    */     public void run()
/*  33:    */     {
/*  34: 72 */       if (!this.counter.monitorActive) {
/*  35: 73 */         return;
/*  36:    */       }
/*  37: 75 */       long newLastTime = TrafficCounter.milliSecondFromNano();
/*  38: 76 */       this.counter.resetAccounting(newLastTime);
/*  39: 77 */       for (GlobalChannelTrafficShapingHandler.PerChannel perChannel : this.trafficShapingHandler1.channelQueues.values()) {
/*  40: 78 */         perChannel.channelTrafficCounter.resetAccounting(newLastTime);
/*  41:    */       }
/*  42: 80 */       this.trafficShapingHandler1.doAccounting(this.counter);
/*  43: 81 */       this.counter.scheduledFuture = this.counter.executor.schedule(this, this.counter.checkInterval.get(), TimeUnit.MILLISECONDS);
/*  44:    */     }
/*  45:    */   }
/*  46:    */   
/*  47:    */   public synchronized void start()
/*  48:    */   {
/*  49: 91 */     if (this.monitorActive) {
/*  50: 92 */       return;
/*  51:    */     }
/*  52: 94 */     this.lastTime.set(milliSecondFromNano());
/*  53: 95 */     long localCheckInterval = this.checkInterval.get();
/*  54: 96 */     if (localCheckInterval > 0L)
/*  55:    */     {
/*  56: 97 */       this.monitorActive = true;
/*  57: 98 */       this.monitor = new MixedTrafficMonitoringTask((GlobalChannelTrafficShapingHandler)this.trafficShapingHandler, this);
/*  58:    */       
/*  59:100 */       this.scheduledFuture = this.executor.schedule(this.monitor, localCheckInterval, TimeUnit.MILLISECONDS);
/*  60:    */     }
/*  61:    */   }
/*  62:    */   
/*  63:    */   public synchronized void stop()
/*  64:    */   {
/*  65:109 */     if (!this.monitorActive) {
/*  66:110 */       return;
/*  67:    */     }
/*  68:112 */     this.monitorActive = false;
/*  69:113 */     resetAccounting(milliSecondFromNano());
/*  70:114 */     this.trafficShapingHandler.doAccounting(this);
/*  71:115 */     if (this.scheduledFuture != null) {
/*  72:116 */       this.scheduledFuture.cancel(true);
/*  73:    */     }
/*  74:    */   }
/*  75:    */   
/*  76:    */   public void resetCumulativeTime()
/*  77:    */   {
/*  78:123 */     for (GlobalChannelTrafficShapingHandler.PerChannel perChannel : ((GlobalChannelTrafficShapingHandler)this.trafficShapingHandler).channelQueues.values()) {
/*  79:124 */       perChannel.channelTrafficCounter.resetCumulativeTime();
/*  80:    */     }
/*  81:126 */     super.resetCumulativeTime();
/*  82:    */   }
/*  83:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.traffic.GlobalChannelTrafficCounter
 * JD-Core Version:    0.7.0.1
 */