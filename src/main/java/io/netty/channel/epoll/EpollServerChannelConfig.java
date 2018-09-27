/*   1:    */ package io.netty.channel.epoll;
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
/*  14:    */ public class EpollServerChannelConfig
/*  15:    */   extends EpollChannelConfig
/*  16:    */   implements ServerSocketChannelConfig
/*  17:    */ {
/*  18:    */   protected final AbstractEpollChannel channel;
/*  19: 36 */   private volatile int backlog = NetUtil.SOMAXCONN;
/*  20:    */   private volatile int pendingFastOpenRequestsThreshold;
/*  21:    */   
/*  22:    */   EpollServerChannelConfig(AbstractEpollChannel channel)
/*  23:    */   {
/*  24: 40 */     super(channel);
/*  25: 41 */     this.channel = channel;
/*  26:    */   }
/*  27:    */   
/*  28:    */   public Map<ChannelOption<?>, Object> getOptions()
/*  29:    */   {
/*  30: 46 */     return getOptions(super.getOptions(), new ChannelOption[] { ChannelOption.SO_RCVBUF, ChannelOption.SO_REUSEADDR, ChannelOption.SO_BACKLOG, EpollChannelOption.TCP_FASTOPEN });
/*  31:    */   }
/*  32:    */   
/*  33:    */   public <T> T getOption(ChannelOption<T> option)
/*  34:    */   {
/*  35: 52 */     if (option == ChannelOption.SO_RCVBUF) {
/*  36: 53 */       return Integer.valueOf(getReceiveBufferSize());
/*  37:    */     }
/*  38: 55 */     if (option == ChannelOption.SO_REUSEADDR) {
/*  39: 56 */       return Boolean.valueOf(isReuseAddress());
/*  40:    */     }
/*  41: 58 */     if (option == ChannelOption.SO_BACKLOG) {
/*  42: 59 */       return Integer.valueOf(getBacklog());
/*  43:    */     }
/*  44: 61 */     if (option == EpollChannelOption.TCP_FASTOPEN) {
/*  45: 62 */       return Integer.valueOf(getTcpFastopen());
/*  46:    */     }
/*  47: 64 */     return super.getOption(option);
/*  48:    */   }
/*  49:    */   
/*  50:    */   public <T> boolean setOption(ChannelOption<T> option, T value)
/*  51:    */   {
/*  52: 69 */     validate(option, value);
/*  53: 71 */     if (option == ChannelOption.SO_RCVBUF) {
/*  54: 72 */       setReceiveBufferSize(((Integer)value).intValue());
/*  55: 73 */     } else if (option == ChannelOption.SO_REUSEADDR) {
/*  56: 74 */       setReuseAddress(((Boolean)value).booleanValue());
/*  57: 75 */     } else if (option == ChannelOption.SO_BACKLOG) {
/*  58: 76 */       setBacklog(((Integer)value).intValue());
/*  59: 77 */     } else if (option == EpollChannelOption.TCP_FASTOPEN) {
/*  60: 78 */       setTcpFastopen(((Integer)value).intValue());
/*  61:    */     } else {
/*  62: 80 */       return super.setOption(option, value);
/*  63:    */     }
/*  64: 83 */     return true;
/*  65:    */   }
/*  66:    */   
/*  67:    */   public boolean isReuseAddress()
/*  68:    */   {
/*  69:    */     try
/*  70:    */     {
/*  71: 88 */       return this.channel.socket.isReuseAddress();
/*  72:    */     }
/*  73:    */     catch (IOException e)
/*  74:    */     {
/*  75: 90 */       throw new ChannelException(e);
/*  76:    */     }
/*  77:    */   }
/*  78:    */   
/*  79:    */   public EpollServerChannelConfig setReuseAddress(boolean reuseAddress)
/*  80:    */   {
/*  81:    */     try
/*  82:    */     {
/*  83: 96 */       this.channel.socket.setReuseAddress(reuseAddress);
/*  84: 97 */       return this;
/*  85:    */     }
/*  86:    */     catch (IOException e)
/*  87:    */     {
/*  88: 99 */       throw new ChannelException(e);
/*  89:    */     }
/*  90:    */   }
/*  91:    */   
/*  92:    */   public int getReceiveBufferSize()
/*  93:    */   {
/*  94:    */     try
/*  95:    */     {
/*  96:105 */       return this.channel.socket.getReceiveBufferSize();
/*  97:    */     }
/*  98:    */     catch (IOException e)
/*  99:    */     {
/* 100:107 */       throw new ChannelException(e);
/* 101:    */     }
/* 102:    */   }
/* 103:    */   
/* 104:    */   public EpollServerChannelConfig setReceiveBufferSize(int receiveBufferSize)
/* 105:    */   {
/* 106:    */     try
/* 107:    */     {
/* 108:113 */       this.channel.socket.setReceiveBufferSize(receiveBufferSize);
/* 109:114 */       return this;
/* 110:    */     }
/* 111:    */     catch (IOException e)
/* 112:    */     {
/* 113:116 */       throw new ChannelException(e);
/* 114:    */     }
/* 115:    */   }
/* 116:    */   
/* 117:    */   public int getBacklog()
/* 118:    */   {
/* 119:121 */     return this.backlog;
/* 120:    */   }
/* 121:    */   
/* 122:    */   public EpollServerChannelConfig setBacklog(int backlog)
/* 123:    */   {
/* 124:125 */     if (backlog < 0) {
/* 125:126 */       throw new IllegalArgumentException("backlog: " + backlog);
/* 126:    */     }
/* 127:128 */     this.backlog = backlog;
/* 128:129 */     return this;
/* 129:    */   }
/* 130:    */   
/* 131:    */   public int getTcpFastopen()
/* 132:    */   {
/* 133:138 */     return this.pendingFastOpenRequestsThreshold;
/* 134:    */   }
/* 135:    */   
/* 136:    */   public EpollServerChannelConfig setTcpFastopen(int pendingFastOpenRequestsThreshold)
/* 137:    */   {
/* 138:151 */     if (this.pendingFastOpenRequestsThreshold < 0) {
/* 139:152 */       throw new IllegalArgumentException("pendingFastOpenRequestsThreshold: " + pendingFastOpenRequestsThreshold);
/* 140:    */     }
/* 141:154 */     this.pendingFastOpenRequestsThreshold = pendingFastOpenRequestsThreshold;
/* 142:155 */     return this;
/* 143:    */   }
/* 144:    */   
/* 145:    */   public EpollServerChannelConfig setPerformancePreferences(int connectionTime, int latency, int bandwidth)
/* 146:    */   {
/* 147:160 */     return this;
/* 148:    */   }
/* 149:    */   
/* 150:    */   public EpollServerChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis)
/* 151:    */   {
/* 152:165 */     super.setConnectTimeoutMillis(connectTimeoutMillis);
/* 153:166 */     return this;
/* 154:    */   }
/* 155:    */   
/* 156:    */   @Deprecated
/* 157:    */   public EpollServerChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead)
/* 158:    */   {
/* 159:172 */     super.setMaxMessagesPerRead(maxMessagesPerRead);
/* 160:173 */     return this;
/* 161:    */   }
/* 162:    */   
/* 163:    */   public EpollServerChannelConfig setWriteSpinCount(int writeSpinCount)
/* 164:    */   {
/* 165:178 */     super.setWriteSpinCount(writeSpinCount);
/* 166:179 */     return this;
/* 167:    */   }
/* 168:    */   
/* 169:    */   public EpollServerChannelConfig setAllocator(ByteBufAllocator allocator)
/* 170:    */   {
/* 171:184 */     super.setAllocator(allocator);
/* 172:185 */     return this;
/* 173:    */   }
/* 174:    */   
/* 175:    */   public EpollServerChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator)
/* 176:    */   {
/* 177:190 */     super.setRecvByteBufAllocator(allocator);
/* 178:191 */     return this;
/* 179:    */   }
/* 180:    */   
/* 181:    */   public EpollServerChannelConfig setAutoRead(boolean autoRead)
/* 182:    */   {
/* 183:196 */     super.setAutoRead(autoRead);
/* 184:197 */     return this;
/* 185:    */   }
/* 186:    */   
/* 187:    */   @Deprecated
/* 188:    */   public EpollServerChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark)
/* 189:    */   {
/* 190:203 */     super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
/* 191:204 */     return this;
/* 192:    */   }
/* 193:    */   
/* 194:    */   @Deprecated
/* 195:    */   public EpollServerChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark)
/* 196:    */   {
/* 197:210 */     super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
/* 198:211 */     return this;
/* 199:    */   }
/* 200:    */   
/* 201:    */   public EpollServerChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark)
/* 202:    */   {
/* 203:216 */     super.setWriteBufferWaterMark(writeBufferWaterMark);
/* 204:217 */     return this;
/* 205:    */   }
/* 206:    */   
/* 207:    */   public EpollServerChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator)
/* 208:    */   {
/* 209:222 */     super.setMessageSizeEstimator(estimator);
/* 210:223 */     return this;
/* 211:    */   }
/* 212:    */   
/* 213:    */   public EpollServerChannelConfig setEpollMode(EpollMode mode)
/* 214:    */   {
/* 215:228 */     super.setEpollMode(mode);
/* 216:229 */     return this;
/* 217:    */   }
/* 218:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.epoll.EpollServerChannelConfig
 * JD-Core Version:    0.7.0.1
 */