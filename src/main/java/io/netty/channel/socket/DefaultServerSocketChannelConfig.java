/*   1:    */ package io.netty.channel.socket;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.channel.ChannelException;
/*   5:    */ import io.netty.channel.ChannelOption;
/*   6:    */ import io.netty.channel.DefaultChannelConfig;
/*   7:    */ import io.netty.channel.MessageSizeEstimator;
/*   8:    */ import io.netty.channel.RecvByteBufAllocator;
/*   9:    */ import io.netty.channel.WriteBufferWaterMark;
/*  10:    */ import io.netty.util.NetUtil;
/*  11:    */ import java.net.ServerSocket;
/*  12:    */ import java.net.SocketException;
/*  13:    */ import java.util.Map;
/*  14:    */ 
/*  15:    */ public class DefaultServerSocketChannelConfig
/*  16:    */   extends DefaultChannelConfig
/*  17:    */   implements ServerSocketChannelConfig
/*  18:    */ {
/*  19:    */   protected final ServerSocket javaSocket;
/*  20: 42 */   private volatile int backlog = NetUtil.SOMAXCONN;
/*  21:    */   
/*  22:    */   public DefaultServerSocketChannelConfig(ServerSocketChannel channel, ServerSocket javaSocket)
/*  23:    */   {
/*  24: 48 */     super(channel);
/*  25: 49 */     if (javaSocket == null) {
/*  26: 50 */       throw new NullPointerException("javaSocket");
/*  27:    */     }
/*  28: 52 */     this.javaSocket = javaSocket;
/*  29:    */   }
/*  30:    */   
/*  31:    */   public Map<ChannelOption<?>, Object> getOptions()
/*  32:    */   {
/*  33: 57 */     return getOptions(super.getOptions(), new ChannelOption[] { ChannelOption.SO_RCVBUF, ChannelOption.SO_REUSEADDR, ChannelOption.SO_BACKLOG });
/*  34:    */   }
/*  35:    */   
/*  36:    */   public <T> T getOption(ChannelOption<T> option)
/*  37:    */   {
/*  38: 63 */     if (option == ChannelOption.SO_RCVBUF) {
/*  39: 64 */       return Integer.valueOf(getReceiveBufferSize());
/*  40:    */     }
/*  41: 66 */     if (option == ChannelOption.SO_REUSEADDR) {
/*  42: 67 */       return Boolean.valueOf(isReuseAddress());
/*  43:    */     }
/*  44: 69 */     if (option == ChannelOption.SO_BACKLOG) {
/*  45: 70 */       return Integer.valueOf(getBacklog());
/*  46:    */     }
/*  47: 73 */     return super.getOption(option);
/*  48:    */   }
/*  49:    */   
/*  50:    */   public <T> boolean setOption(ChannelOption<T> option, T value)
/*  51:    */   {
/*  52: 78 */     validate(option, value);
/*  53: 80 */     if (option == ChannelOption.SO_RCVBUF) {
/*  54: 81 */       setReceiveBufferSize(((Integer)value).intValue());
/*  55: 82 */     } else if (option == ChannelOption.SO_REUSEADDR) {
/*  56: 83 */       setReuseAddress(((Boolean)value).booleanValue());
/*  57: 84 */     } else if (option == ChannelOption.SO_BACKLOG) {
/*  58: 85 */       setBacklog(((Integer)value).intValue());
/*  59:    */     } else {
/*  60: 87 */       return super.setOption(option, value);
/*  61:    */     }
/*  62: 90 */     return true;
/*  63:    */   }
/*  64:    */   
/*  65:    */   public boolean isReuseAddress()
/*  66:    */   {
/*  67:    */     try
/*  68:    */     {
/*  69: 96 */       return this.javaSocket.getReuseAddress();
/*  70:    */     }
/*  71:    */     catch (SocketException e)
/*  72:    */     {
/*  73: 98 */       throw new ChannelException(e);
/*  74:    */     }
/*  75:    */   }
/*  76:    */   
/*  77:    */   public ServerSocketChannelConfig setReuseAddress(boolean reuseAddress)
/*  78:    */   {
/*  79:    */     try
/*  80:    */     {
/*  81:105 */       this.javaSocket.setReuseAddress(reuseAddress);
/*  82:    */     }
/*  83:    */     catch (SocketException e)
/*  84:    */     {
/*  85:107 */       throw new ChannelException(e);
/*  86:    */     }
/*  87:109 */     return this;
/*  88:    */   }
/*  89:    */   
/*  90:    */   public int getReceiveBufferSize()
/*  91:    */   {
/*  92:    */     try
/*  93:    */     {
/*  94:115 */       return this.javaSocket.getReceiveBufferSize();
/*  95:    */     }
/*  96:    */     catch (SocketException e)
/*  97:    */     {
/*  98:117 */       throw new ChannelException(e);
/*  99:    */     }
/* 100:    */   }
/* 101:    */   
/* 102:    */   public ServerSocketChannelConfig setReceiveBufferSize(int receiveBufferSize)
/* 103:    */   {
/* 104:    */     try
/* 105:    */     {
/* 106:124 */       this.javaSocket.setReceiveBufferSize(receiveBufferSize);
/* 107:    */     }
/* 108:    */     catch (SocketException e)
/* 109:    */     {
/* 110:126 */       throw new ChannelException(e);
/* 111:    */     }
/* 112:128 */     return this;
/* 113:    */   }
/* 114:    */   
/* 115:    */   public ServerSocketChannelConfig setPerformancePreferences(int connectionTime, int latency, int bandwidth)
/* 116:    */   {
/* 117:133 */     this.javaSocket.setPerformancePreferences(connectionTime, latency, bandwidth);
/* 118:134 */     return this;
/* 119:    */   }
/* 120:    */   
/* 121:    */   public int getBacklog()
/* 122:    */   {
/* 123:139 */     return this.backlog;
/* 124:    */   }
/* 125:    */   
/* 126:    */   public ServerSocketChannelConfig setBacklog(int backlog)
/* 127:    */   {
/* 128:144 */     if (backlog < 0) {
/* 129:145 */       throw new IllegalArgumentException("backlog: " + backlog);
/* 130:    */     }
/* 131:147 */     this.backlog = backlog;
/* 132:148 */     return this;
/* 133:    */   }
/* 134:    */   
/* 135:    */   public ServerSocketChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis)
/* 136:    */   {
/* 137:153 */     super.setConnectTimeoutMillis(connectTimeoutMillis);
/* 138:154 */     return this;
/* 139:    */   }
/* 140:    */   
/* 141:    */   @Deprecated
/* 142:    */   public ServerSocketChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead)
/* 143:    */   {
/* 144:160 */     super.setMaxMessagesPerRead(maxMessagesPerRead);
/* 145:161 */     return this;
/* 146:    */   }
/* 147:    */   
/* 148:    */   public ServerSocketChannelConfig setWriteSpinCount(int writeSpinCount)
/* 149:    */   {
/* 150:166 */     super.setWriteSpinCount(writeSpinCount);
/* 151:167 */     return this;
/* 152:    */   }
/* 153:    */   
/* 154:    */   public ServerSocketChannelConfig setAllocator(ByteBufAllocator allocator)
/* 155:    */   {
/* 156:172 */     super.setAllocator(allocator);
/* 157:173 */     return this;
/* 158:    */   }
/* 159:    */   
/* 160:    */   public ServerSocketChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator)
/* 161:    */   {
/* 162:178 */     super.setRecvByteBufAllocator(allocator);
/* 163:179 */     return this;
/* 164:    */   }
/* 165:    */   
/* 166:    */   public ServerSocketChannelConfig setAutoRead(boolean autoRead)
/* 167:    */   {
/* 168:184 */     super.setAutoRead(autoRead);
/* 169:185 */     return this;
/* 170:    */   }
/* 171:    */   
/* 172:    */   public ServerSocketChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark)
/* 173:    */   {
/* 174:190 */     super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
/* 175:191 */     return this;
/* 176:    */   }
/* 177:    */   
/* 178:    */   public ServerSocketChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark)
/* 179:    */   {
/* 180:196 */     super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
/* 181:197 */     return this;
/* 182:    */   }
/* 183:    */   
/* 184:    */   public ServerSocketChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark)
/* 185:    */   {
/* 186:202 */     super.setWriteBufferWaterMark(writeBufferWaterMark);
/* 187:203 */     return this;
/* 188:    */   }
/* 189:    */   
/* 190:    */   public ServerSocketChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator)
/* 191:    */   {
/* 192:208 */     super.setMessageSizeEstimator(estimator);
/* 193:209 */     return this;
/* 194:    */   }
/* 195:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.socket.DefaultServerSocketChannelConfig
 * JD-Core Version:    0.7.0.1
 */