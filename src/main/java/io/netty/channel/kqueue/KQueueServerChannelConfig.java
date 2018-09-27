/*   1:    */ package io.netty.channel.kqueue;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.channel.ChannelException;
/*   5:    */ import io.netty.channel.ChannelOption;
/*   6:    */ import io.netty.channel.MessageSizeEstimator;
/*   7:    */ import io.netty.channel.RecvByteBufAllocator;
/*   8:    */ import io.netty.channel.WriteBufferWaterMark;
/*   9:    */ import io.netty.channel.socket.ServerSocketChannelConfig;
/*  10:    */ import io.netty.util.NetUtil;
/*  11:    */ import java.io.IOException;
/*  12:    */ import java.util.Map;
/*  13:    */ 
/*  14:    */ public class KQueueServerChannelConfig
/*  15:    */   extends KQueueChannelConfig
/*  16:    */   implements ServerSocketChannelConfig
/*  17:    */ {
/*  18:    */   protected final AbstractKQueueChannel channel;
/*  19: 38 */   private volatile int backlog = NetUtil.SOMAXCONN;
/*  20:    */   
/*  21:    */   KQueueServerChannelConfig(AbstractKQueueChannel channel)
/*  22:    */   {
/*  23: 41 */     super(channel);
/*  24: 42 */     this.channel = channel;
/*  25:    */   }
/*  26:    */   
/*  27:    */   public Map<ChannelOption<?>, Object> getOptions()
/*  28:    */   {
/*  29: 47 */     return getOptions(super.getOptions(), new ChannelOption[] { ChannelOption.SO_RCVBUF, ChannelOption.SO_REUSEADDR, ChannelOption.SO_BACKLOG });
/*  30:    */   }
/*  31:    */   
/*  32:    */   public <T> T getOption(ChannelOption<T> option)
/*  33:    */   {
/*  34: 53 */     if (option == ChannelOption.SO_RCVBUF) {
/*  35: 54 */       return Integer.valueOf(getReceiveBufferSize());
/*  36:    */     }
/*  37: 56 */     if (option == ChannelOption.SO_REUSEADDR) {
/*  38: 57 */       return Boolean.valueOf(isReuseAddress());
/*  39:    */     }
/*  40: 59 */     if (option == ChannelOption.SO_BACKLOG) {
/*  41: 60 */       return Integer.valueOf(getBacklog());
/*  42:    */     }
/*  43: 62 */     return super.getOption(option);
/*  44:    */   }
/*  45:    */   
/*  46:    */   public <T> boolean setOption(ChannelOption<T> option, T value)
/*  47:    */   {
/*  48: 67 */     validate(option, value);
/*  49: 69 */     if (option == ChannelOption.SO_RCVBUF) {
/*  50: 70 */       setReceiveBufferSize(((Integer)value).intValue());
/*  51: 71 */     } else if (option == ChannelOption.SO_REUSEADDR) {
/*  52: 72 */       setReuseAddress(((Boolean)value).booleanValue());
/*  53: 73 */     } else if (option == ChannelOption.SO_BACKLOG) {
/*  54: 74 */       setBacklog(((Integer)value).intValue());
/*  55:    */     } else {
/*  56: 76 */       return super.setOption(option, value);
/*  57:    */     }
/*  58: 79 */     return true;
/*  59:    */   }
/*  60:    */   
/*  61:    */   public boolean isReuseAddress()
/*  62:    */   {
/*  63:    */     try
/*  64:    */     {
/*  65: 84 */       return this.channel.socket.isReuseAddress();
/*  66:    */     }
/*  67:    */     catch (IOException e)
/*  68:    */     {
/*  69: 86 */       throw new ChannelException(e);
/*  70:    */     }
/*  71:    */   }
/*  72:    */   
/*  73:    */   public KQueueServerChannelConfig setReuseAddress(boolean reuseAddress)
/*  74:    */   {
/*  75:    */     try
/*  76:    */     {
/*  77: 92 */       this.channel.socket.setReuseAddress(reuseAddress);
/*  78: 93 */       return this;
/*  79:    */     }
/*  80:    */     catch (IOException e)
/*  81:    */     {
/*  82: 95 */       throw new ChannelException(e);
/*  83:    */     }
/*  84:    */   }
/*  85:    */   
/*  86:    */   public int getReceiveBufferSize()
/*  87:    */   {
/*  88:    */     try
/*  89:    */     {
/*  90:101 */       return this.channel.socket.getReceiveBufferSize();
/*  91:    */     }
/*  92:    */     catch (IOException e)
/*  93:    */     {
/*  94:103 */       throw new ChannelException(e);
/*  95:    */     }
/*  96:    */   }
/*  97:    */   
/*  98:    */   public KQueueServerChannelConfig setReceiveBufferSize(int receiveBufferSize)
/*  99:    */   {
/* 100:    */     try
/* 101:    */     {
/* 102:109 */       this.channel.socket.setReceiveBufferSize(receiveBufferSize);
/* 103:110 */       return this;
/* 104:    */     }
/* 105:    */     catch (IOException e)
/* 106:    */     {
/* 107:112 */       throw new ChannelException(e);
/* 108:    */     }
/* 109:    */   }
/* 110:    */   
/* 111:    */   public int getBacklog()
/* 112:    */   {
/* 113:117 */     return this.backlog;
/* 114:    */   }
/* 115:    */   
/* 116:    */   public KQueueServerChannelConfig setBacklog(int backlog)
/* 117:    */   {
/* 118:121 */     if (backlog < 0) {
/* 119:122 */       throw new IllegalArgumentException("backlog: " + backlog);
/* 120:    */     }
/* 121:124 */     this.backlog = backlog;
/* 122:125 */     return this;
/* 123:    */   }
/* 124:    */   
/* 125:    */   public KQueueServerChannelConfig setRcvAllocTransportProvidesGuess(boolean transportProvidesGuess)
/* 126:    */   {
/* 127:130 */     super.setRcvAllocTransportProvidesGuess(transportProvidesGuess);
/* 128:131 */     return this;
/* 129:    */   }
/* 130:    */   
/* 131:    */   public KQueueServerChannelConfig setPerformancePreferences(int connectionTime, int latency, int bandwidth)
/* 132:    */   {
/* 133:136 */     return this;
/* 134:    */   }
/* 135:    */   
/* 136:    */   public KQueueServerChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis)
/* 137:    */   {
/* 138:141 */     super.setConnectTimeoutMillis(connectTimeoutMillis);
/* 139:142 */     return this;
/* 140:    */   }
/* 141:    */   
/* 142:    */   @Deprecated
/* 143:    */   public KQueueServerChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead)
/* 144:    */   {
/* 145:148 */     super.setMaxMessagesPerRead(maxMessagesPerRead);
/* 146:149 */     return this;
/* 147:    */   }
/* 148:    */   
/* 149:    */   public KQueueServerChannelConfig setWriteSpinCount(int writeSpinCount)
/* 150:    */   {
/* 151:154 */     super.setWriteSpinCount(writeSpinCount);
/* 152:155 */     return this;
/* 153:    */   }
/* 154:    */   
/* 155:    */   public KQueueServerChannelConfig setAllocator(ByteBufAllocator allocator)
/* 156:    */   {
/* 157:160 */     super.setAllocator(allocator);
/* 158:161 */     return this;
/* 159:    */   }
/* 160:    */   
/* 161:    */   public KQueueServerChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator)
/* 162:    */   {
/* 163:166 */     super.setRecvByteBufAllocator(allocator);
/* 164:167 */     return this;
/* 165:    */   }
/* 166:    */   
/* 167:    */   public KQueueServerChannelConfig setAutoRead(boolean autoRead)
/* 168:    */   {
/* 169:172 */     super.setAutoRead(autoRead);
/* 170:173 */     return this;
/* 171:    */   }
/* 172:    */   
/* 173:    */   @Deprecated
/* 174:    */   public KQueueServerChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark)
/* 175:    */   {
/* 176:179 */     super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
/* 177:180 */     return this;
/* 178:    */   }
/* 179:    */   
/* 180:    */   @Deprecated
/* 181:    */   public KQueueServerChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark)
/* 182:    */   {
/* 183:186 */     super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
/* 184:187 */     return this;
/* 185:    */   }
/* 186:    */   
/* 187:    */   public KQueueServerChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark)
/* 188:    */   {
/* 189:192 */     super.setWriteBufferWaterMark(writeBufferWaterMark);
/* 190:193 */     return this;
/* 191:    */   }
/* 192:    */   
/* 193:    */   public KQueueServerChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator)
/* 194:    */   {
/* 195:198 */     super.setMessageSizeEstimator(estimator);
/* 196:199 */     return this;
/* 197:    */   }
/* 198:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.kqueue.KQueueServerChannelConfig
 * JD-Core Version:    0.7.0.1
 */