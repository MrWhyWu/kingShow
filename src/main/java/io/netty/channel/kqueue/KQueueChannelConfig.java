/*   1:    */ package io.netty.channel.kqueue;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.channel.ChannelOption;
/*   5:    */ import io.netty.channel.DefaultChannelConfig;
/*   6:    */ import io.netty.channel.MessageSizeEstimator;
/*   7:    */ import io.netty.channel.RecvByteBufAllocator;
/*   8:    */ import io.netty.channel.RecvByteBufAllocator.ExtendedHandle;
/*   9:    */ import io.netty.channel.WriteBufferWaterMark;
/*  10:    */ import io.netty.channel.unix.Limits;
/*  11:    */ import java.util.Map;
/*  12:    */ 
/*  13:    */ public class KQueueChannelConfig
/*  14:    */   extends DefaultChannelConfig
/*  15:    */ {
/*  16:    */   final AbstractKQueueChannel channel;
/*  17:    */   private volatile boolean transportProvidesGuess;
/*  18: 36 */   private volatile long maxBytesPerGatheringWrite = Limits.SSIZE_MAX;
/*  19:    */   
/*  20:    */   KQueueChannelConfig(AbstractKQueueChannel channel)
/*  21:    */   {
/*  22: 39 */     super(channel);
/*  23: 40 */     this.channel = channel;
/*  24:    */   }
/*  25:    */   
/*  26:    */   public Map<ChannelOption<?>, Object> getOptions()
/*  27:    */   {
/*  28: 46 */     return getOptions(super.getOptions(), new ChannelOption[] { KQueueChannelOption.RCV_ALLOC_TRANSPORT_PROVIDES_GUESS });
/*  29:    */   }
/*  30:    */   
/*  31:    */   public <T> T getOption(ChannelOption<T> option)
/*  32:    */   {
/*  33: 52 */     if (option == KQueueChannelOption.RCV_ALLOC_TRANSPORT_PROVIDES_GUESS) {
/*  34: 53 */       return Boolean.valueOf(getRcvAllocTransportProvidesGuess());
/*  35:    */     }
/*  36: 55 */     return super.getOption(option);
/*  37:    */   }
/*  38:    */   
/*  39:    */   public <T> boolean setOption(ChannelOption<T> option, T value)
/*  40:    */   {
/*  41: 60 */     validate(option, value);
/*  42: 62 */     if (option == KQueueChannelOption.RCV_ALLOC_TRANSPORT_PROVIDES_GUESS) {
/*  43: 63 */       setRcvAllocTransportProvidesGuess(((Boolean)value).booleanValue());
/*  44:    */     } else {
/*  45: 65 */       return super.setOption(option, value);
/*  46:    */     }
/*  47: 68 */     return true;
/*  48:    */   }
/*  49:    */   
/*  50:    */   public KQueueChannelConfig setRcvAllocTransportProvidesGuess(boolean transportProvidesGuess)
/*  51:    */   {
/*  52: 76 */     this.transportProvidesGuess = transportProvidesGuess;
/*  53: 77 */     return this;
/*  54:    */   }
/*  55:    */   
/*  56:    */   public boolean getRcvAllocTransportProvidesGuess()
/*  57:    */   {
/*  58: 85 */     return this.transportProvidesGuess;
/*  59:    */   }
/*  60:    */   
/*  61:    */   public KQueueChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis)
/*  62:    */   {
/*  63: 90 */     super.setConnectTimeoutMillis(connectTimeoutMillis);
/*  64: 91 */     return this;
/*  65:    */   }
/*  66:    */   
/*  67:    */   @Deprecated
/*  68:    */   public KQueueChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead)
/*  69:    */   {
/*  70: 97 */     super.setMaxMessagesPerRead(maxMessagesPerRead);
/*  71: 98 */     return this;
/*  72:    */   }
/*  73:    */   
/*  74:    */   public KQueueChannelConfig setWriteSpinCount(int writeSpinCount)
/*  75:    */   {
/*  76:103 */     super.setWriteSpinCount(writeSpinCount);
/*  77:104 */     return this;
/*  78:    */   }
/*  79:    */   
/*  80:    */   public KQueueChannelConfig setAllocator(ByteBufAllocator allocator)
/*  81:    */   {
/*  82:109 */     super.setAllocator(allocator);
/*  83:110 */     return this;
/*  84:    */   }
/*  85:    */   
/*  86:    */   public KQueueChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator)
/*  87:    */   {
/*  88:115 */     if (!(allocator.newHandle() instanceof RecvByteBufAllocator.ExtendedHandle)) {
/*  89:116 */       throw new IllegalArgumentException("allocator.newHandle() must return an object of type: " + RecvByteBufAllocator.ExtendedHandle.class);
/*  90:    */     }
/*  91:119 */     super.setRecvByteBufAllocator(allocator);
/*  92:120 */     return this;
/*  93:    */   }
/*  94:    */   
/*  95:    */   public KQueueChannelConfig setAutoRead(boolean autoRead)
/*  96:    */   {
/*  97:125 */     super.setAutoRead(autoRead);
/*  98:126 */     return this;
/*  99:    */   }
/* 100:    */   
/* 101:    */   @Deprecated
/* 102:    */   public KQueueChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark)
/* 103:    */   {
/* 104:132 */     super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
/* 105:133 */     return this;
/* 106:    */   }
/* 107:    */   
/* 108:    */   @Deprecated
/* 109:    */   public KQueueChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark)
/* 110:    */   {
/* 111:139 */     super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
/* 112:140 */     return this;
/* 113:    */   }
/* 114:    */   
/* 115:    */   public KQueueChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark)
/* 116:    */   {
/* 117:145 */     super.setWriteBufferWaterMark(writeBufferWaterMark);
/* 118:146 */     return this;
/* 119:    */   }
/* 120:    */   
/* 121:    */   public KQueueChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator)
/* 122:    */   {
/* 123:151 */     super.setMessageSizeEstimator(estimator);
/* 124:152 */     return this;
/* 125:    */   }
/* 126:    */   
/* 127:    */   protected final void autoReadCleared()
/* 128:    */   {
/* 129:157 */     this.channel.clearReadFilter();
/* 130:    */   }
/* 131:    */   
/* 132:    */   final void setMaxBytesPerGatheringWrite(long maxBytesPerGatheringWrite)
/* 133:    */   {
/* 134:161 */     this.maxBytesPerGatheringWrite = Math.min(Limits.SSIZE_MAX, maxBytesPerGatheringWrite);
/* 135:    */   }
/* 136:    */   
/* 137:    */   final long getMaxBytesPerGatheringWrite()
/* 138:    */   {
/* 139:165 */     return this.maxBytesPerGatheringWrite;
/* 140:    */   }
/* 141:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.kqueue.KQueueChannelConfig
 * JD-Core Version:    0.7.0.1
 */