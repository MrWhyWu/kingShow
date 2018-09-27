/*   1:    */ package io.netty.channel.epoll;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.channel.ChannelException;
/*   5:    */ import io.netty.channel.ChannelOption;
/*   6:    */ import io.netty.channel.DefaultChannelConfig;
/*   7:    */ import io.netty.channel.MessageSizeEstimator;
/*   8:    */ import io.netty.channel.RecvByteBufAllocator;
/*   9:    */ import io.netty.channel.RecvByteBufAllocator.ExtendedHandle;
/*  10:    */ import io.netty.channel.WriteBufferWaterMark;
/*  11:    */ import io.netty.channel.unix.Limits;
/*  12:    */ import java.io.IOException;
/*  13:    */ import java.util.Map;
/*  14:    */ 
/*  15:    */ public class EpollChannelConfig
/*  16:    */   extends DefaultChannelConfig
/*  17:    */ {
/*  18:    */   final AbstractEpollChannel channel;
/*  19: 33 */   private volatile long maxBytesPerGatheringWrite = Limits.SSIZE_MAX;
/*  20:    */   
/*  21:    */   EpollChannelConfig(AbstractEpollChannel channel)
/*  22:    */   {
/*  23: 36 */     super(channel);
/*  24: 37 */     this.channel = channel;
/*  25:    */   }
/*  26:    */   
/*  27:    */   public Map<ChannelOption<?>, Object> getOptions()
/*  28:    */   {
/*  29: 42 */     return getOptions(super.getOptions(), new ChannelOption[] { EpollChannelOption.EPOLL_MODE });
/*  30:    */   }
/*  31:    */   
/*  32:    */   public <T> T getOption(ChannelOption<T> option)
/*  33:    */   {
/*  34: 48 */     if (option == EpollChannelOption.EPOLL_MODE) {
/*  35: 49 */       return getEpollMode();
/*  36:    */     }
/*  37: 51 */     return super.getOption(option);
/*  38:    */   }
/*  39:    */   
/*  40:    */   public <T> boolean setOption(ChannelOption<T> option, T value)
/*  41:    */   {
/*  42: 56 */     validate(option, value);
/*  43: 57 */     if (option == EpollChannelOption.EPOLL_MODE) {
/*  44: 58 */       setEpollMode((EpollMode)value);
/*  45:    */     } else {
/*  46: 60 */       return super.setOption(option, value);
/*  47:    */     }
/*  48: 62 */     return true;
/*  49:    */   }
/*  50:    */   
/*  51:    */   public EpollChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis)
/*  52:    */   {
/*  53: 67 */     super.setConnectTimeoutMillis(connectTimeoutMillis);
/*  54: 68 */     return this;
/*  55:    */   }
/*  56:    */   
/*  57:    */   @Deprecated
/*  58:    */   public EpollChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead)
/*  59:    */   {
/*  60: 74 */     super.setMaxMessagesPerRead(maxMessagesPerRead);
/*  61: 75 */     return this;
/*  62:    */   }
/*  63:    */   
/*  64:    */   public EpollChannelConfig setWriteSpinCount(int writeSpinCount)
/*  65:    */   {
/*  66: 80 */     super.setWriteSpinCount(writeSpinCount);
/*  67: 81 */     return this;
/*  68:    */   }
/*  69:    */   
/*  70:    */   public EpollChannelConfig setAllocator(ByteBufAllocator allocator)
/*  71:    */   {
/*  72: 86 */     super.setAllocator(allocator);
/*  73: 87 */     return this;
/*  74:    */   }
/*  75:    */   
/*  76:    */   public EpollChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator)
/*  77:    */   {
/*  78: 92 */     if (!(allocator.newHandle() instanceof RecvByteBufAllocator.ExtendedHandle)) {
/*  79: 93 */       throw new IllegalArgumentException("allocator.newHandle() must return an object of type: " + RecvByteBufAllocator.ExtendedHandle.class);
/*  80:    */     }
/*  81: 96 */     super.setRecvByteBufAllocator(allocator);
/*  82: 97 */     return this;
/*  83:    */   }
/*  84:    */   
/*  85:    */   public EpollChannelConfig setAutoRead(boolean autoRead)
/*  86:    */   {
/*  87:102 */     super.setAutoRead(autoRead);
/*  88:103 */     return this;
/*  89:    */   }
/*  90:    */   
/*  91:    */   @Deprecated
/*  92:    */   public EpollChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark)
/*  93:    */   {
/*  94:109 */     super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
/*  95:110 */     return this;
/*  96:    */   }
/*  97:    */   
/*  98:    */   @Deprecated
/*  99:    */   public EpollChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark)
/* 100:    */   {
/* 101:116 */     super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
/* 102:117 */     return this;
/* 103:    */   }
/* 104:    */   
/* 105:    */   public EpollChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark)
/* 106:    */   {
/* 107:122 */     super.setWriteBufferWaterMark(writeBufferWaterMark);
/* 108:123 */     return this;
/* 109:    */   }
/* 110:    */   
/* 111:    */   public EpollChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator)
/* 112:    */   {
/* 113:128 */     super.setMessageSizeEstimator(estimator);
/* 114:129 */     return this;
/* 115:    */   }
/* 116:    */   
/* 117:    */   public EpollMode getEpollMode()
/* 118:    */   {
/* 119:139 */     return this.channel.isFlagSet(Native.EPOLLET) ? EpollMode.EDGE_TRIGGERED : EpollMode.LEVEL_TRIGGERED;
/* 120:    */   }
/* 121:    */   
/* 122:    */   public EpollChannelConfig setEpollMode(EpollMode mode)
/* 123:    */   {
/* 124:152 */     if (mode == null) {
/* 125:153 */       throw new NullPointerException("mode");
/* 126:    */     }
/* 127:    */     try
/* 128:    */     {
/* 129:156 */       switch (1.$SwitchMap$io$netty$channel$epoll$EpollMode[mode.ordinal()])
/* 130:    */       {
/* 131:    */       case 1: 
/* 132:158 */         checkChannelNotRegistered();
/* 133:159 */         this.channel.setFlag(Native.EPOLLET);
/* 134:160 */         break;
/* 135:    */       case 2: 
/* 136:162 */         checkChannelNotRegistered();
/* 137:163 */         this.channel.clearFlag(Native.EPOLLET);
/* 138:164 */         break;
/* 139:    */       default: 
/* 140:166 */         throw new Error();
/* 141:    */       }
/* 142:    */     }
/* 143:    */     catch (IOException e)
/* 144:    */     {
/* 145:169 */       throw new ChannelException(e);
/* 146:    */     }
/* 147:171 */     return this;
/* 148:    */   }
/* 149:    */   
/* 150:    */   private void checkChannelNotRegistered()
/* 151:    */   {
/* 152:175 */     if (this.channel.isRegistered()) {
/* 153:176 */       throw new IllegalStateException("EpollMode can only be changed before channel is registered");
/* 154:    */     }
/* 155:    */   }
/* 156:    */   
/* 157:    */   protected final void autoReadCleared()
/* 158:    */   {
/* 159:182 */     this.channel.clearEpollIn();
/* 160:    */   }
/* 161:    */   
/* 162:    */   final void setMaxBytesPerGatheringWrite(long maxBytesPerGatheringWrite)
/* 163:    */   {
/* 164:186 */     this.maxBytesPerGatheringWrite = maxBytesPerGatheringWrite;
/* 165:    */   }
/* 166:    */   
/* 167:    */   final long getMaxBytesPerGatheringWrite()
/* 168:    */   {
/* 169:190 */     return this.maxBytesPerGatheringWrite;
/* 170:    */   }
/* 171:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.epoll.EpollChannelConfig
 * JD-Core Version:    0.7.0.1
 */