/*   1:    */ package io.netty.channel;
/*   2:    */ 
/*   3:    */ import io.netty.util.concurrent.CompleteFuture;
/*   4:    */ import io.netty.util.concurrent.EventExecutor;
/*   5:    */ import io.netty.util.concurrent.Future;
/*   6:    */ import io.netty.util.concurrent.GenericFutureListener;
/*   7:    */ 
/*   8:    */ abstract class CompleteChannelFuture
/*   9:    */   extends CompleteFuture<Void>
/*  10:    */   implements ChannelFuture
/*  11:    */ {
/*  12:    */   private final Channel channel;
/*  13:    */   
/*  14:    */   protected CompleteChannelFuture(Channel channel, EventExecutor executor)
/*  15:    */   {
/*  16: 37 */     super(executor);
/*  17: 38 */     if (channel == null) {
/*  18: 39 */       throw new NullPointerException("channel");
/*  19:    */     }
/*  20: 41 */     this.channel = channel;
/*  21:    */   }
/*  22:    */   
/*  23:    */   protected EventExecutor executor()
/*  24:    */   {
/*  25: 46 */     EventExecutor e = super.executor();
/*  26: 47 */     if (e == null) {
/*  27: 48 */       return channel().eventLoop();
/*  28:    */     }
/*  29: 50 */     return e;
/*  30:    */   }
/*  31:    */   
/*  32:    */   public ChannelFuture addListener(GenericFutureListener<? extends Future<? super Void>> listener)
/*  33:    */   {
/*  34: 56 */     super.addListener(listener);
/*  35: 57 */     return this;
/*  36:    */   }
/*  37:    */   
/*  38:    */   public ChannelFuture addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners)
/*  39:    */   {
/*  40: 62 */     super.addListeners(listeners);
/*  41: 63 */     return this;
/*  42:    */   }
/*  43:    */   
/*  44:    */   public ChannelFuture removeListener(GenericFutureListener<? extends Future<? super Void>> listener)
/*  45:    */   {
/*  46: 68 */     super.removeListener(listener);
/*  47: 69 */     return this;
/*  48:    */   }
/*  49:    */   
/*  50:    */   public ChannelFuture removeListeners(GenericFutureListener<? extends Future<? super Void>>... listeners)
/*  51:    */   {
/*  52: 74 */     super.removeListeners(listeners);
/*  53: 75 */     return this;
/*  54:    */   }
/*  55:    */   
/*  56:    */   public ChannelFuture syncUninterruptibly()
/*  57:    */   {
/*  58: 80 */     return this;
/*  59:    */   }
/*  60:    */   
/*  61:    */   public ChannelFuture sync()
/*  62:    */     throws InterruptedException
/*  63:    */   {
/*  64: 85 */     return this;
/*  65:    */   }
/*  66:    */   
/*  67:    */   public ChannelFuture await()
/*  68:    */     throws InterruptedException
/*  69:    */   {
/*  70: 90 */     return this;
/*  71:    */   }
/*  72:    */   
/*  73:    */   public ChannelFuture awaitUninterruptibly()
/*  74:    */   {
/*  75: 95 */     return this;
/*  76:    */   }
/*  77:    */   
/*  78:    */   public Channel channel()
/*  79:    */   {
/*  80:100 */     return this.channel;
/*  81:    */   }
/*  82:    */   
/*  83:    */   public Void getNow()
/*  84:    */   {
/*  85:105 */     return null;
/*  86:    */   }
/*  87:    */   
/*  88:    */   public boolean isVoid()
/*  89:    */   {
/*  90:110 */     return false;
/*  91:    */   }
/*  92:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.CompleteChannelFuture
 * JD-Core Version:    0.7.0.1
 */