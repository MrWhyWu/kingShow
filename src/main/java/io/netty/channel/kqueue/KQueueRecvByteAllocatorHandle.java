/*   1:    */ package io.netty.channel.kqueue;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.channel.ChannelConfig;
/*   6:    */ import io.netty.channel.RecvByteBufAllocator.ExtendedHandle;
/*   7:    */ import io.netty.util.UncheckedBooleanSupplier;
/*   8:    */ import io.netty.util.internal.ObjectUtil;
/*   9:    */ 
/*  10:    */ final class KQueueRecvByteAllocatorHandle
/*  11:    */   implements RecvByteBufAllocator.ExtendedHandle
/*  12:    */ {
/*  13:    */   private final RecvByteBufAllocator.ExtendedHandle delegate;
/*  14: 30 */   private final UncheckedBooleanSupplier defaultMaybeMoreDataSupplier = new UncheckedBooleanSupplier()
/*  15:    */   {
/*  16:    */     public boolean get()
/*  17:    */     {
/*  18: 33 */       return KQueueRecvByteAllocatorHandle.this.maybeMoreDataToRead();
/*  19:    */     }
/*  20:    */   };
/*  21:    */   private boolean overrideGuess;
/*  22:    */   private boolean readEOF;
/*  23:    */   private long numberBytesPending;
/*  24:    */   
/*  25:    */   KQueueRecvByteAllocatorHandle(RecvByteBufAllocator.ExtendedHandle handle)
/*  26:    */   {
/*  27: 41 */     this.delegate = ((RecvByteBufAllocator.ExtendedHandle)ObjectUtil.checkNotNull(handle, "handle"));
/*  28:    */   }
/*  29:    */   
/*  30:    */   public int guess()
/*  31:    */   {
/*  32: 46 */     return this.overrideGuess ? guess0() : this.delegate.guess();
/*  33:    */   }
/*  34:    */   
/*  35:    */   public void reset(ChannelConfig config)
/*  36:    */   {
/*  37: 51 */     this.overrideGuess = ((KQueueChannelConfig)config).getRcvAllocTransportProvidesGuess();
/*  38: 52 */     this.delegate.reset(config);
/*  39:    */   }
/*  40:    */   
/*  41:    */   public void incMessagesRead(int numMessages)
/*  42:    */   {
/*  43: 57 */     this.delegate.incMessagesRead(numMessages);
/*  44:    */   }
/*  45:    */   
/*  46:    */   public ByteBuf allocate(ByteBufAllocator alloc)
/*  47:    */   {
/*  48: 62 */     return this.overrideGuess ? alloc.ioBuffer(guess0()) : this.delegate.allocate(alloc);
/*  49:    */   }
/*  50:    */   
/*  51:    */   public void lastBytesRead(int bytes)
/*  52:    */   {
/*  53: 67 */     this.numberBytesPending = (bytes < 0 ? 0L : Math.max(0L, this.numberBytesPending - bytes));
/*  54: 68 */     this.delegate.lastBytesRead(bytes);
/*  55:    */   }
/*  56:    */   
/*  57:    */   public int lastBytesRead()
/*  58:    */   {
/*  59: 73 */     return this.delegate.lastBytesRead();
/*  60:    */   }
/*  61:    */   
/*  62:    */   public void attemptedBytesRead(int bytes)
/*  63:    */   {
/*  64: 78 */     this.delegate.attemptedBytesRead(bytes);
/*  65:    */   }
/*  66:    */   
/*  67:    */   public int attemptedBytesRead()
/*  68:    */   {
/*  69: 83 */     return this.delegate.attemptedBytesRead();
/*  70:    */   }
/*  71:    */   
/*  72:    */   public void readComplete()
/*  73:    */   {
/*  74: 88 */     this.delegate.readComplete();
/*  75:    */   }
/*  76:    */   
/*  77:    */   public boolean continueReading(UncheckedBooleanSupplier maybeMoreDataSupplier)
/*  78:    */   {
/*  79: 93 */     return this.delegate.continueReading(maybeMoreDataSupplier);
/*  80:    */   }
/*  81:    */   
/*  82:    */   public boolean continueReading()
/*  83:    */   {
/*  84: 99 */     return this.delegate.continueReading(this.defaultMaybeMoreDataSupplier);
/*  85:    */   }
/*  86:    */   
/*  87:    */   void readEOF()
/*  88:    */   {
/*  89:103 */     this.readEOF = true;
/*  90:    */   }
/*  91:    */   
/*  92:    */   void numberBytesPending(long numberBytesPending)
/*  93:    */   {
/*  94:107 */     this.numberBytesPending = numberBytesPending;
/*  95:    */   }
/*  96:    */   
/*  97:    */   boolean maybeMoreDataToRead()
/*  98:    */   {
/*  99:121 */     return (this.numberBytesPending != 0L) || (this.readEOF);
/* 100:    */   }
/* 101:    */   
/* 102:    */   private int guess0()
/* 103:    */   {
/* 104:125 */     return (int)Math.min(this.numberBytesPending, 2147483647L);
/* 105:    */   }
/* 106:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.kqueue.KQueueRecvByteAllocatorHandle
 * JD-Core Version:    0.7.0.1
 */