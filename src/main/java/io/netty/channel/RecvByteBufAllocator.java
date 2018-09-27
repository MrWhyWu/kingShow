/*   1:    */ package io.netty.channel;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.util.UncheckedBooleanSupplier;
/*   6:    */ import io.netty.util.internal.ObjectUtil;
/*   7:    */ 
/*   8:    */ public abstract interface RecvByteBufAllocator
/*   9:    */ {
/*  10:    */   public abstract Handle newHandle();
/*  11:    */   
/*  12:    */   public static class DelegatingHandle
/*  13:    */     implements RecvByteBufAllocator.Handle
/*  14:    */   {
/*  15:    */     private final RecvByteBufAllocator.Handle delegate;
/*  16:    */     
/*  17:    */     public DelegatingHandle(RecvByteBufAllocator.Handle delegate)
/*  18:    */     {
/*  19:127 */       this.delegate = ((RecvByteBufAllocator.Handle)ObjectUtil.checkNotNull(delegate, "delegate"));
/*  20:    */     }
/*  21:    */     
/*  22:    */     protected final RecvByteBufAllocator.Handle delegate()
/*  23:    */     {
/*  24:135 */       return this.delegate;
/*  25:    */     }
/*  26:    */     
/*  27:    */     public ByteBuf allocate(ByteBufAllocator alloc)
/*  28:    */     {
/*  29:140 */       return this.delegate.allocate(alloc);
/*  30:    */     }
/*  31:    */     
/*  32:    */     public int guess()
/*  33:    */     {
/*  34:145 */       return this.delegate.guess();
/*  35:    */     }
/*  36:    */     
/*  37:    */     public void reset(ChannelConfig config)
/*  38:    */     {
/*  39:150 */       this.delegate.reset(config);
/*  40:    */     }
/*  41:    */     
/*  42:    */     public void incMessagesRead(int numMessages)
/*  43:    */     {
/*  44:155 */       this.delegate.incMessagesRead(numMessages);
/*  45:    */     }
/*  46:    */     
/*  47:    */     public void lastBytesRead(int bytes)
/*  48:    */     {
/*  49:160 */       this.delegate.lastBytesRead(bytes);
/*  50:    */     }
/*  51:    */     
/*  52:    */     public int lastBytesRead()
/*  53:    */     {
/*  54:165 */       return this.delegate.lastBytesRead();
/*  55:    */     }
/*  56:    */     
/*  57:    */     public boolean continueReading()
/*  58:    */     {
/*  59:170 */       return this.delegate.continueReading();
/*  60:    */     }
/*  61:    */     
/*  62:    */     public int attemptedBytesRead()
/*  63:    */     {
/*  64:175 */       return this.delegate.attemptedBytesRead();
/*  65:    */     }
/*  66:    */     
/*  67:    */     public void attemptedBytesRead(int bytes)
/*  68:    */     {
/*  69:180 */       this.delegate.attemptedBytesRead(bytes);
/*  70:    */     }
/*  71:    */     
/*  72:    */     public void readComplete()
/*  73:    */     {
/*  74:185 */       this.delegate.readComplete();
/*  75:    */     }
/*  76:    */   }
/*  77:    */   
/*  78:    */   public static abstract interface ExtendedHandle
/*  79:    */     extends RecvByteBufAllocator.Handle
/*  80:    */   {
/*  81:    */     public abstract boolean continueReading(UncheckedBooleanSupplier paramUncheckedBooleanSupplier);
/*  82:    */   }
/*  83:    */   
/*  84:    */   @Deprecated
/*  85:    */   public static abstract interface Handle
/*  86:    */   {
/*  87:    */     public abstract ByteBuf allocate(ByteBufAllocator paramByteBufAllocator);
/*  88:    */     
/*  89:    */     public abstract int guess();
/*  90:    */     
/*  91:    */     public abstract void reset(ChannelConfig paramChannelConfig);
/*  92:    */     
/*  93:    */     public abstract void incMessagesRead(int paramInt);
/*  94:    */     
/*  95:    */     public abstract void lastBytesRead(int paramInt);
/*  96:    */     
/*  97:    */     public abstract int lastBytesRead();
/*  98:    */     
/*  99:    */     public abstract void attemptedBytesRead(int paramInt);
/* 100:    */     
/* 101:    */     public abstract int attemptedBytesRead();
/* 102:    */     
/* 103:    */     public abstract boolean continueReading();
/* 104:    */     
/* 105:    */     public abstract void readComplete();
/* 106:    */   }
/* 107:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.RecvByteBufAllocator
 * JD-Core Version:    0.7.0.1
 */