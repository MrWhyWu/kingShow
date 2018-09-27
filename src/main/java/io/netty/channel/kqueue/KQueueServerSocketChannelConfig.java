/*   1:    */ package io.netty.channel.kqueue;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.channel.ChannelException;
/*   5:    */ import io.netty.channel.ChannelOption;
/*   6:    */ import io.netty.channel.MessageSizeEstimator;
/*   7:    */ import io.netty.channel.RecvByteBufAllocator;
/*   8:    */ import io.netty.channel.WriteBufferWaterMark;
/*   9:    */ import io.netty.channel.socket.ServerSocketChannelConfig;
/*  10:    */ import io.netty.channel.unix.UnixChannelOption;
/*  11:    */ import java.io.IOException;
/*  12:    */ import java.util.Map;
/*  13:    */ 
/*  14:    */ public class KQueueServerSocketChannelConfig
/*  15:    */   extends KQueueServerChannelConfig
/*  16:    */   implements ServerSocketChannelConfig
/*  17:    */ {
/*  18:    */   KQueueServerSocketChannelConfig(KQueueServerSocketChannel channel)
/*  19:    */   {
/*  20: 36 */     super(channel);
/*  21:    */     
/*  22:    */ 
/*  23:    */ 
/*  24:    */ 
/*  25: 41 */     setReuseAddress(true);
/*  26:    */   }
/*  27:    */   
/*  28:    */   public Map<ChannelOption<?>, Object> getOptions()
/*  29:    */   {
/*  30: 46 */     return getOptions(super.getOptions(), new ChannelOption[] { UnixChannelOption.SO_REUSEPORT, KQueueChannelOption.SO_ACCEPTFILTER });
/*  31:    */   }
/*  32:    */   
/*  33:    */   public <T> T getOption(ChannelOption<T> option)
/*  34:    */   {
/*  35: 52 */     if (option == UnixChannelOption.SO_REUSEPORT) {
/*  36: 53 */       return Boolean.valueOf(isReusePort());
/*  37:    */     }
/*  38: 55 */     if (option == KQueueChannelOption.SO_ACCEPTFILTER) {
/*  39: 56 */       return getAcceptFilter();
/*  40:    */     }
/*  41: 58 */     return super.getOption(option);
/*  42:    */   }
/*  43:    */   
/*  44:    */   public <T> boolean setOption(ChannelOption<T> option, T value)
/*  45:    */   {
/*  46: 63 */     validate(option, value);
/*  47: 65 */     if (option == UnixChannelOption.SO_REUSEPORT) {
/*  48: 66 */       setReusePort(((Boolean)value).booleanValue());
/*  49: 67 */     } else if (option == KQueueChannelOption.SO_ACCEPTFILTER) {
/*  50: 68 */       setAcceptFilter((AcceptFilter)value);
/*  51:    */     } else {
/*  52: 70 */       return super.setOption(option, value);
/*  53:    */     }
/*  54: 73 */     return true;
/*  55:    */   }
/*  56:    */   
/*  57:    */   public KQueueServerSocketChannelConfig setReusePort(boolean reusePort)
/*  58:    */   {
/*  59:    */     try
/*  60:    */     {
/*  61: 78 */       this.channel.socket.setReusePort(reusePort);
/*  62: 79 */       return this;
/*  63:    */     }
/*  64:    */     catch (IOException e)
/*  65:    */     {
/*  66: 81 */       throw new ChannelException(e);
/*  67:    */     }
/*  68:    */   }
/*  69:    */   
/*  70:    */   public boolean isReusePort()
/*  71:    */   {
/*  72:    */     try
/*  73:    */     {
/*  74: 87 */       return this.channel.socket.isReusePort();
/*  75:    */     }
/*  76:    */     catch (IOException e)
/*  77:    */     {
/*  78: 89 */       throw new ChannelException(e);
/*  79:    */     }
/*  80:    */   }
/*  81:    */   
/*  82:    */   public KQueueServerSocketChannelConfig setAcceptFilter(AcceptFilter acceptFilter)
/*  83:    */   {
/*  84:    */     try
/*  85:    */     {
/*  86: 95 */       this.channel.socket.setAcceptFilter(acceptFilter);
/*  87: 96 */       return this;
/*  88:    */     }
/*  89:    */     catch (IOException e)
/*  90:    */     {
/*  91: 98 */       throw new ChannelException(e);
/*  92:    */     }
/*  93:    */   }
/*  94:    */   
/*  95:    */   public AcceptFilter getAcceptFilter()
/*  96:    */   {
/*  97:    */     try
/*  98:    */     {
/*  99:104 */       return this.channel.socket.getAcceptFilter();
/* 100:    */     }
/* 101:    */     catch (IOException e)
/* 102:    */     {
/* 103:106 */       throw new ChannelException(e);
/* 104:    */     }
/* 105:    */   }
/* 106:    */   
/* 107:    */   public KQueueServerSocketChannelConfig setRcvAllocTransportProvidesGuess(boolean transportProvidesGuess)
/* 108:    */   {
/* 109:112 */     super.setRcvAllocTransportProvidesGuess(transportProvidesGuess);
/* 110:113 */     return this;
/* 111:    */   }
/* 112:    */   
/* 113:    */   public KQueueServerSocketChannelConfig setReuseAddress(boolean reuseAddress)
/* 114:    */   {
/* 115:118 */     super.setReuseAddress(reuseAddress);
/* 116:119 */     return this;
/* 117:    */   }
/* 118:    */   
/* 119:    */   public KQueueServerSocketChannelConfig setReceiveBufferSize(int receiveBufferSize)
/* 120:    */   {
/* 121:124 */     super.setReceiveBufferSize(receiveBufferSize);
/* 122:125 */     return this;
/* 123:    */   }
/* 124:    */   
/* 125:    */   public KQueueServerSocketChannelConfig setPerformancePreferences(int connectionTime, int latency, int bandwidth)
/* 126:    */   {
/* 127:130 */     return this;
/* 128:    */   }
/* 129:    */   
/* 130:    */   public KQueueServerSocketChannelConfig setBacklog(int backlog)
/* 131:    */   {
/* 132:135 */     super.setBacklog(backlog);
/* 133:136 */     return this;
/* 134:    */   }
/* 135:    */   
/* 136:    */   public KQueueServerSocketChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis)
/* 137:    */   {
/* 138:141 */     super.setConnectTimeoutMillis(connectTimeoutMillis);
/* 139:142 */     return this;
/* 140:    */   }
/* 141:    */   
/* 142:    */   @Deprecated
/* 143:    */   public KQueueServerSocketChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead)
/* 144:    */   {
/* 145:148 */     super.setMaxMessagesPerRead(maxMessagesPerRead);
/* 146:149 */     return this;
/* 147:    */   }
/* 148:    */   
/* 149:    */   public KQueueServerSocketChannelConfig setWriteSpinCount(int writeSpinCount)
/* 150:    */   {
/* 151:154 */     super.setWriteSpinCount(writeSpinCount);
/* 152:155 */     return this;
/* 153:    */   }
/* 154:    */   
/* 155:    */   public KQueueServerSocketChannelConfig setAllocator(ByteBufAllocator allocator)
/* 156:    */   {
/* 157:160 */     super.setAllocator(allocator);
/* 158:161 */     return this;
/* 159:    */   }
/* 160:    */   
/* 161:    */   public KQueueServerSocketChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator)
/* 162:    */   {
/* 163:166 */     super.setRecvByteBufAllocator(allocator);
/* 164:167 */     return this;
/* 165:    */   }
/* 166:    */   
/* 167:    */   public KQueueServerSocketChannelConfig setAutoRead(boolean autoRead)
/* 168:    */   {
/* 169:172 */     super.setAutoRead(autoRead);
/* 170:173 */     return this;
/* 171:    */   }
/* 172:    */   
/* 173:    */   @Deprecated
/* 174:    */   public KQueueServerSocketChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark)
/* 175:    */   {
/* 176:179 */     super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
/* 177:180 */     return this;
/* 178:    */   }
/* 179:    */   
/* 180:    */   @Deprecated
/* 181:    */   public KQueueServerSocketChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark)
/* 182:    */   {
/* 183:186 */     super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
/* 184:187 */     return this;
/* 185:    */   }
/* 186:    */   
/* 187:    */   public KQueueServerSocketChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark)
/* 188:    */   {
/* 189:192 */     super.setWriteBufferWaterMark(writeBufferWaterMark);
/* 190:193 */     return this;
/* 191:    */   }
/* 192:    */   
/* 193:    */   public KQueueServerSocketChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator)
/* 194:    */   {
/* 195:198 */     super.setMessageSizeEstimator(estimator);
/* 196:199 */     return this;
/* 197:    */   }
/* 198:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.kqueue.KQueueServerSocketChannelConfig
 * JD-Core Version:    0.7.0.1
 */