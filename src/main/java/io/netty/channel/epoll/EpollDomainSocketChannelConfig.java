/*   1:    */ package io.netty.channel.epoll;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.channel.ChannelOption;
/*   5:    */ import io.netty.channel.MessageSizeEstimator;
/*   6:    */ import io.netty.channel.RecvByteBufAllocator;
/*   7:    */ import io.netty.channel.WriteBufferWaterMark;
/*   8:    */ import io.netty.channel.unix.DomainSocketChannelConfig;
/*   9:    */ import io.netty.channel.unix.DomainSocketReadMode;
/*  10:    */ import java.util.Map;
/*  11:    */ 
/*  12:    */ public final class EpollDomainSocketChannelConfig
/*  13:    */   extends EpollChannelConfig
/*  14:    */   implements DomainSocketChannelConfig
/*  15:    */ {
/*  16: 30 */   private volatile DomainSocketReadMode mode = DomainSocketReadMode.BYTES;
/*  17:    */   
/*  18:    */   EpollDomainSocketChannelConfig(AbstractEpollChannel channel)
/*  19:    */   {
/*  20: 33 */     super(channel);
/*  21:    */   }
/*  22:    */   
/*  23:    */   public Map<ChannelOption<?>, Object> getOptions()
/*  24:    */   {
/*  25: 38 */     return getOptions(super.getOptions(), new ChannelOption[] { EpollChannelOption.DOMAIN_SOCKET_READ_MODE });
/*  26:    */   }
/*  27:    */   
/*  28:    */   public <T> T getOption(ChannelOption<T> option)
/*  29:    */   {
/*  30: 44 */     if (option == EpollChannelOption.DOMAIN_SOCKET_READ_MODE) {
/*  31: 45 */       return getReadMode();
/*  32:    */     }
/*  33: 47 */     return super.getOption(option);
/*  34:    */   }
/*  35:    */   
/*  36:    */   public <T> boolean setOption(ChannelOption<T> option, T value)
/*  37:    */   {
/*  38: 52 */     validate(option, value);
/*  39: 54 */     if (option == EpollChannelOption.DOMAIN_SOCKET_READ_MODE) {
/*  40: 55 */       setReadMode((DomainSocketReadMode)value);
/*  41:    */     } else {
/*  42: 57 */       return super.setOption(option, value);
/*  43:    */     }
/*  44: 60 */     return true;
/*  45:    */   }
/*  46:    */   
/*  47:    */   @Deprecated
/*  48:    */   public EpollDomainSocketChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead)
/*  49:    */   {
/*  50: 66 */     super.setMaxMessagesPerRead(maxMessagesPerRead);
/*  51: 67 */     return this;
/*  52:    */   }
/*  53:    */   
/*  54:    */   public EpollDomainSocketChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis)
/*  55:    */   {
/*  56: 72 */     super.setConnectTimeoutMillis(connectTimeoutMillis);
/*  57: 73 */     return this;
/*  58:    */   }
/*  59:    */   
/*  60:    */   public EpollDomainSocketChannelConfig setWriteSpinCount(int writeSpinCount)
/*  61:    */   {
/*  62: 78 */     super.setWriteSpinCount(writeSpinCount);
/*  63: 79 */     return this;
/*  64:    */   }
/*  65:    */   
/*  66:    */   public EpollDomainSocketChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator)
/*  67:    */   {
/*  68: 84 */     super.setRecvByteBufAllocator(allocator);
/*  69: 85 */     return this;
/*  70:    */   }
/*  71:    */   
/*  72:    */   public EpollDomainSocketChannelConfig setAllocator(ByteBufAllocator allocator)
/*  73:    */   {
/*  74: 90 */     super.setAllocator(allocator);
/*  75: 91 */     return this;
/*  76:    */   }
/*  77:    */   
/*  78:    */   public EpollDomainSocketChannelConfig setAutoClose(boolean autoClose)
/*  79:    */   {
/*  80: 96 */     super.setAutoClose(autoClose);
/*  81: 97 */     return this;
/*  82:    */   }
/*  83:    */   
/*  84:    */   public EpollDomainSocketChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator)
/*  85:    */   {
/*  86:102 */     super.setMessageSizeEstimator(estimator);
/*  87:103 */     return this;
/*  88:    */   }
/*  89:    */   
/*  90:    */   @Deprecated
/*  91:    */   public EpollDomainSocketChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark)
/*  92:    */   {
/*  93:109 */     super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
/*  94:110 */     return this;
/*  95:    */   }
/*  96:    */   
/*  97:    */   @Deprecated
/*  98:    */   public EpollDomainSocketChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark)
/*  99:    */   {
/* 100:116 */     super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
/* 101:117 */     return this;
/* 102:    */   }
/* 103:    */   
/* 104:    */   public EpollDomainSocketChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark)
/* 105:    */   {
/* 106:122 */     super.setWriteBufferWaterMark(writeBufferWaterMark);
/* 107:123 */     return this;
/* 108:    */   }
/* 109:    */   
/* 110:    */   public EpollDomainSocketChannelConfig setAutoRead(boolean autoRead)
/* 111:    */   {
/* 112:128 */     super.setAutoRead(autoRead);
/* 113:129 */     return this;
/* 114:    */   }
/* 115:    */   
/* 116:    */   public EpollDomainSocketChannelConfig setEpollMode(EpollMode mode)
/* 117:    */   {
/* 118:134 */     super.setEpollMode(mode);
/* 119:135 */     return this;
/* 120:    */   }
/* 121:    */   
/* 122:    */   public EpollDomainSocketChannelConfig setReadMode(DomainSocketReadMode mode)
/* 123:    */   {
/* 124:140 */     if (mode == null) {
/* 125:141 */       throw new NullPointerException("mode");
/* 126:    */     }
/* 127:143 */     this.mode = mode;
/* 128:144 */     return this;
/* 129:    */   }
/* 130:    */   
/* 131:    */   public DomainSocketReadMode getReadMode()
/* 132:    */   {
/* 133:149 */     return this.mode;
/* 134:    */   }
/* 135:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.epoll.EpollDomainSocketChannelConfig
 * JD-Core Version:    0.7.0.1
 */