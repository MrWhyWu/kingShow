/*   1:    */ package io.netty.channel.kqueue;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.channel.ChannelOption;
/*   5:    */ import io.netty.channel.MessageSizeEstimator;
/*   6:    */ import io.netty.channel.RecvByteBufAllocator;
/*   7:    */ import io.netty.channel.WriteBufferWaterMark;
/*   8:    */ import io.netty.channel.unix.DomainSocketChannelConfig;
/*   9:    */ import io.netty.channel.unix.DomainSocketReadMode;
/*  10:    */ import io.netty.channel.unix.UnixChannelOption;
/*  11:    */ import java.util.Map;
/*  12:    */ 
/*  13:    */ public final class KQueueDomainSocketChannelConfig
/*  14:    */   extends KQueueChannelConfig
/*  15:    */   implements DomainSocketChannelConfig
/*  16:    */ {
/*  17: 33 */   private volatile DomainSocketReadMode mode = DomainSocketReadMode.BYTES;
/*  18:    */   
/*  19:    */   KQueueDomainSocketChannelConfig(AbstractKQueueChannel channel)
/*  20:    */   {
/*  21: 36 */     super(channel);
/*  22:    */   }
/*  23:    */   
/*  24:    */   public Map<ChannelOption<?>, Object> getOptions()
/*  25:    */   {
/*  26: 41 */     return getOptions(super.getOptions(), new ChannelOption[] { UnixChannelOption.DOMAIN_SOCKET_READ_MODE });
/*  27:    */   }
/*  28:    */   
/*  29:    */   public <T> T getOption(ChannelOption<T> option)
/*  30:    */   {
/*  31: 47 */     if (option == UnixChannelOption.DOMAIN_SOCKET_READ_MODE) {
/*  32: 48 */       return getReadMode();
/*  33:    */     }
/*  34: 50 */     return super.getOption(option);
/*  35:    */   }
/*  36:    */   
/*  37:    */   public <T> boolean setOption(ChannelOption<T> option, T value)
/*  38:    */   {
/*  39: 55 */     validate(option, value);
/*  40: 57 */     if (option == UnixChannelOption.DOMAIN_SOCKET_READ_MODE) {
/*  41: 58 */       setReadMode((DomainSocketReadMode)value);
/*  42:    */     } else {
/*  43: 60 */       return super.setOption(option, value);
/*  44:    */     }
/*  45: 63 */     return true;
/*  46:    */   }
/*  47:    */   
/*  48:    */   public KQueueDomainSocketChannelConfig setRcvAllocTransportProvidesGuess(boolean transportProvidesGuess)
/*  49:    */   {
/*  50: 68 */     super.setRcvAllocTransportProvidesGuess(transportProvidesGuess);
/*  51: 69 */     return this;
/*  52:    */   }
/*  53:    */   
/*  54:    */   @Deprecated
/*  55:    */   public KQueueDomainSocketChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead)
/*  56:    */   {
/*  57: 75 */     super.setMaxMessagesPerRead(maxMessagesPerRead);
/*  58: 76 */     return this;
/*  59:    */   }
/*  60:    */   
/*  61:    */   public KQueueDomainSocketChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis)
/*  62:    */   {
/*  63: 81 */     super.setConnectTimeoutMillis(connectTimeoutMillis);
/*  64: 82 */     return this;
/*  65:    */   }
/*  66:    */   
/*  67:    */   public KQueueDomainSocketChannelConfig setWriteSpinCount(int writeSpinCount)
/*  68:    */   {
/*  69: 87 */     super.setWriteSpinCount(writeSpinCount);
/*  70: 88 */     return this;
/*  71:    */   }
/*  72:    */   
/*  73:    */   public KQueueDomainSocketChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator)
/*  74:    */   {
/*  75: 93 */     super.setRecvByteBufAllocator(allocator);
/*  76: 94 */     return this;
/*  77:    */   }
/*  78:    */   
/*  79:    */   public KQueueDomainSocketChannelConfig setAllocator(ByteBufAllocator allocator)
/*  80:    */   {
/*  81: 99 */     super.setAllocator(allocator);
/*  82:100 */     return this;
/*  83:    */   }
/*  84:    */   
/*  85:    */   public KQueueDomainSocketChannelConfig setAutoClose(boolean autoClose)
/*  86:    */   {
/*  87:105 */     super.setAutoClose(autoClose);
/*  88:106 */     return this;
/*  89:    */   }
/*  90:    */   
/*  91:    */   public KQueueDomainSocketChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator)
/*  92:    */   {
/*  93:111 */     super.setMessageSizeEstimator(estimator);
/*  94:112 */     return this;
/*  95:    */   }
/*  96:    */   
/*  97:    */   @Deprecated
/*  98:    */   public KQueueDomainSocketChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark)
/*  99:    */   {
/* 100:118 */     super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
/* 101:119 */     return this;
/* 102:    */   }
/* 103:    */   
/* 104:    */   @Deprecated
/* 105:    */   public KQueueDomainSocketChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark)
/* 106:    */   {
/* 107:125 */     super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
/* 108:126 */     return this;
/* 109:    */   }
/* 110:    */   
/* 111:    */   public KQueueDomainSocketChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark)
/* 112:    */   {
/* 113:131 */     super.setWriteBufferWaterMark(writeBufferWaterMark);
/* 114:132 */     return this;
/* 115:    */   }
/* 116:    */   
/* 117:    */   public KQueueDomainSocketChannelConfig setAutoRead(boolean autoRead)
/* 118:    */   {
/* 119:137 */     super.setAutoRead(autoRead);
/* 120:138 */     return this;
/* 121:    */   }
/* 122:    */   
/* 123:    */   public KQueueDomainSocketChannelConfig setReadMode(DomainSocketReadMode mode)
/* 124:    */   {
/* 125:143 */     if (mode == null) {
/* 126:144 */       throw new NullPointerException("mode");
/* 127:    */     }
/* 128:146 */     this.mode = mode;
/* 129:147 */     return this;
/* 130:    */   }
/* 131:    */   
/* 132:    */   public DomainSocketReadMode getReadMode()
/* 133:    */   {
/* 134:152 */     return this.mode;
/* 135:    */   }
/* 136:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.kqueue.KQueueDomainSocketChannelConfig
 * JD-Core Version:    0.7.0.1
 */