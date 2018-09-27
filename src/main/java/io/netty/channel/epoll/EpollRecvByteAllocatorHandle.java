/*   1:    */ package io.netty.channel.epoll;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.channel.ChannelConfig;
/*   6:    */ import io.netty.channel.RecvByteBufAllocator.ExtendedHandle;
/*   7:    */ import io.netty.util.UncheckedBooleanSupplier;
/*   8:    */ import io.netty.util.internal.ObjectUtil;
/*   9:    */ 
/*  10:    */ class EpollRecvByteAllocatorHandle
/*  11:    */   implements RecvByteBufAllocator.ExtendedHandle
/*  12:    */ {
/*  13:    */   private final RecvByteBufAllocator.ExtendedHandle delegate;
/*  14: 27 */   private final UncheckedBooleanSupplier defaultMaybeMoreDataSupplier = new UncheckedBooleanSupplier()
/*  15:    */   {
/*  16:    */     public boolean get()
/*  17:    */     {
/*  18: 30 */       return EpollRecvByteAllocatorHandle.this.maybeMoreDataToRead();
/*  19:    */     }
/*  20:    */   };
/*  21:    */   private boolean isEdgeTriggered;
/*  22:    */   private boolean receivedRdHup;
/*  23:    */   
/*  24:    */   EpollRecvByteAllocatorHandle(RecvByteBufAllocator.ExtendedHandle handle)
/*  25:    */   {
/*  26: 37 */     this.delegate = ((RecvByteBufAllocator.ExtendedHandle)ObjectUtil.checkNotNull(handle, "handle"));
/*  27:    */   }
/*  28:    */   
/*  29:    */   final void receivedRdHup()
/*  30:    */   {
/*  31: 41 */     this.receivedRdHup = true;
/*  32:    */   }
/*  33:    */   
/*  34:    */   final boolean isReceivedRdHup()
/*  35:    */   {
/*  36: 45 */     return this.receivedRdHup;
/*  37:    */   }
/*  38:    */   
/*  39:    */   boolean maybeMoreDataToRead()
/*  40:    */   {
/*  41: 56 */     return ((this.isEdgeTriggered) && (lastBytesRead() > 0)) || ((!this.isEdgeTriggered) && 
/*  42: 57 */       (lastBytesRead() == attemptedBytesRead())) || (this.receivedRdHup);
/*  43:    */   }
/*  44:    */   
/*  45:    */   final void edgeTriggered(boolean edgeTriggered)
/*  46:    */   {
/*  47: 62 */     this.isEdgeTriggered = edgeTriggered;
/*  48:    */   }
/*  49:    */   
/*  50:    */   final boolean isEdgeTriggered()
/*  51:    */   {
/*  52: 66 */     return this.isEdgeTriggered;
/*  53:    */   }
/*  54:    */   
/*  55:    */   public final ByteBuf allocate(ByteBufAllocator alloc)
/*  56:    */   {
/*  57: 71 */     return this.delegate.allocate(alloc);
/*  58:    */   }
/*  59:    */   
/*  60:    */   public final int guess()
/*  61:    */   {
/*  62: 76 */     return this.delegate.guess();
/*  63:    */   }
/*  64:    */   
/*  65:    */   public final void reset(ChannelConfig config)
/*  66:    */   {
/*  67: 81 */     this.delegate.reset(config);
/*  68:    */   }
/*  69:    */   
/*  70:    */   public final void incMessagesRead(int numMessages)
/*  71:    */   {
/*  72: 86 */     this.delegate.incMessagesRead(numMessages);
/*  73:    */   }
/*  74:    */   
/*  75:    */   public final void lastBytesRead(int bytes)
/*  76:    */   {
/*  77: 91 */     this.delegate.lastBytesRead(bytes);
/*  78:    */   }
/*  79:    */   
/*  80:    */   public final int lastBytesRead()
/*  81:    */   {
/*  82: 96 */     return this.delegate.lastBytesRead();
/*  83:    */   }
/*  84:    */   
/*  85:    */   public final int attemptedBytesRead()
/*  86:    */   {
/*  87:101 */     return this.delegate.attemptedBytesRead();
/*  88:    */   }
/*  89:    */   
/*  90:    */   public final void attemptedBytesRead(int bytes)
/*  91:    */   {
/*  92:106 */     this.delegate.attemptedBytesRead(bytes);
/*  93:    */   }
/*  94:    */   
/*  95:    */   public final void readComplete()
/*  96:    */   {
/*  97:111 */     this.delegate.readComplete();
/*  98:    */   }
/*  99:    */   
/* 100:    */   public final boolean continueReading(UncheckedBooleanSupplier maybeMoreDataSupplier)
/* 101:    */   {
/* 102:116 */     return this.delegate.continueReading(maybeMoreDataSupplier);
/* 103:    */   }
/* 104:    */   
/* 105:    */   public final boolean continueReading()
/* 106:    */   {
/* 107:122 */     return this.delegate.continueReading(this.defaultMaybeMoreDataSupplier);
/* 108:    */   }
/* 109:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.epoll.EpollRecvByteAllocatorHandle
 * JD-Core Version:    0.7.0.1
 */