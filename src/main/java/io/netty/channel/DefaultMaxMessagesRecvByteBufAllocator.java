/*   1:    */ package io.netty.channel;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.util.UncheckedBooleanSupplier;
/*   6:    */ 
/*   7:    */ public abstract class DefaultMaxMessagesRecvByteBufAllocator
/*   8:    */   implements MaxMessagesRecvByteBufAllocator
/*   9:    */ {
/*  10:    */   private volatile int maxMessagesPerRead;
/*  11: 28 */   private volatile boolean respectMaybeMoreData = true;
/*  12:    */   
/*  13:    */   public DefaultMaxMessagesRecvByteBufAllocator()
/*  14:    */   {
/*  15: 31 */     this(1);
/*  16:    */   }
/*  17:    */   
/*  18:    */   public DefaultMaxMessagesRecvByteBufAllocator(int maxMessagesPerRead)
/*  19:    */   {
/*  20: 35 */     maxMessagesPerRead(maxMessagesPerRead);
/*  21:    */   }
/*  22:    */   
/*  23:    */   public int maxMessagesPerRead()
/*  24:    */   {
/*  25: 40 */     return this.maxMessagesPerRead;
/*  26:    */   }
/*  27:    */   
/*  28:    */   public MaxMessagesRecvByteBufAllocator maxMessagesPerRead(int maxMessagesPerRead)
/*  29:    */   {
/*  30: 45 */     if (maxMessagesPerRead <= 0) {
/*  31: 46 */       throw new IllegalArgumentException("maxMessagesPerRead: " + maxMessagesPerRead + " (expected: > 0)");
/*  32:    */     }
/*  33: 48 */     this.maxMessagesPerRead = maxMessagesPerRead;
/*  34: 49 */     return this;
/*  35:    */   }
/*  36:    */   
/*  37:    */   public DefaultMaxMessagesRecvByteBufAllocator respectMaybeMoreData(boolean respectMaybeMoreData)
/*  38:    */   {
/*  39: 65 */     this.respectMaybeMoreData = respectMaybeMoreData;
/*  40: 66 */     return this;
/*  41:    */   }
/*  42:    */   
/*  43:    */   public final boolean respectMaybeMoreData()
/*  44:    */   {
/*  45: 81 */     return this.respectMaybeMoreData;
/*  46:    */   }
/*  47:    */   
/*  48:    */   public abstract class MaxMessageHandle
/*  49:    */     implements RecvByteBufAllocator.ExtendedHandle
/*  50:    */   {
/*  51:    */     private ChannelConfig config;
/*  52:    */     private int maxMessagePerRead;
/*  53:    */     private int totalMessages;
/*  54:    */     private int totalBytesRead;
/*  55:    */     private int attemptedBytesRead;
/*  56:    */     private int lastBytesRead;
/*  57: 94 */     private final boolean respectMaybeMoreData = DefaultMaxMessagesRecvByteBufAllocator.this.respectMaybeMoreData;
/*  58: 95 */     private final UncheckedBooleanSupplier defaultMaybeMoreSupplier = new UncheckedBooleanSupplier()
/*  59:    */     {
/*  60:    */       public boolean get()
/*  61:    */       {
/*  62: 98 */         return DefaultMaxMessagesRecvByteBufAllocator.MaxMessageHandle.this.attemptedBytesRead == DefaultMaxMessagesRecvByteBufAllocator.MaxMessageHandle.this.lastBytesRead;
/*  63:    */       }
/*  64:    */     };
/*  65:    */     
/*  66:    */     public MaxMessageHandle() {}
/*  67:    */     
/*  68:    */     public void reset(ChannelConfig config)
/*  69:    */     {
/*  70:107 */       this.config = config;
/*  71:108 */       this.maxMessagePerRead = DefaultMaxMessagesRecvByteBufAllocator.this.maxMessagesPerRead();
/*  72:109 */       this.totalMessages = (this.totalBytesRead = 0);
/*  73:    */     }
/*  74:    */     
/*  75:    */     public ByteBuf allocate(ByteBufAllocator alloc)
/*  76:    */     {
/*  77:114 */       return alloc.ioBuffer(guess());
/*  78:    */     }
/*  79:    */     
/*  80:    */     public final void incMessagesRead(int amt)
/*  81:    */     {
/*  82:119 */       this.totalMessages += amt;
/*  83:    */     }
/*  84:    */     
/*  85:    */     public void lastBytesRead(int bytes)
/*  86:    */     {
/*  87:124 */       this.lastBytesRead = bytes;
/*  88:125 */       if (bytes > 0) {
/*  89:126 */         this.totalBytesRead += bytes;
/*  90:    */       }
/*  91:    */     }
/*  92:    */     
/*  93:    */     public final int lastBytesRead()
/*  94:    */     {
/*  95:132 */       return this.lastBytesRead;
/*  96:    */     }
/*  97:    */     
/*  98:    */     public boolean continueReading()
/*  99:    */     {
/* 100:137 */       return continueReading(this.defaultMaybeMoreSupplier);
/* 101:    */     }
/* 102:    */     
/* 103:    */     public boolean continueReading(UncheckedBooleanSupplier maybeMoreDataSupplier)
/* 104:    */     {
/* 105:142 */       return (this.config.isAutoRead()) && ((!this.respectMaybeMoreData) || 
/* 106:143 */         (maybeMoreDataSupplier.get())) && (this.totalMessages < this.maxMessagePerRead) && (this.totalBytesRead > 0);
/* 107:    */     }
/* 108:    */     
/* 109:    */     public void readComplete() {}
/* 110:    */     
/* 111:    */     public int attemptedBytesRead()
/* 112:    */     {
/* 113:154 */       return this.attemptedBytesRead;
/* 114:    */     }
/* 115:    */     
/* 116:    */     public void attemptedBytesRead(int bytes)
/* 117:    */     {
/* 118:159 */       this.attemptedBytesRead = bytes;
/* 119:    */     }
/* 120:    */     
/* 121:    */     protected final int totalBytesRead()
/* 122:    */     {
/* 123:163 */       return this.totalBytesRead < 0 ? 2147483647 : this.totalBytesRead;
/* 124:    */     }
/* 125:    */   }
/* 126:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.DefaultMaxMessagesRecvByteBufAllocator
 * JD-Core Version:    0.7.0.1
 */