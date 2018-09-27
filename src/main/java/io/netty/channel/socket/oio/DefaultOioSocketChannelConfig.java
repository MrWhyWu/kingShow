/*   1:    */ package io.netty.channel.socket.oio;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.channel.ChannelException;
/*   5:    */ import io.netty.channel.ChannelOption;
/*   6:    */ import io.netty.channel.MessageSizeEstimator;
/*   7:    */ import io.netty.channel.PreferHeapByteBufAllocator;
/*   8:    */ import io.netty.channel.RecvByteBufAllocator;
/*   9:    */ import io.netty.channel.WriteBufferWaterMark;
/*  10:    */ import io.netty.channel.socket.DefaultSocketChannelConfig;
/*  11:    */ import io.netty.channel.socket.SocketChannel;
/*  12:    */ import java.io.IOException;
/*  13:    */ import java.net.Socket;
/*  14:    */ import java.util.Map;
/*  15:    */ 
/*  16:    */ public class DefaultOioSocketChannelConfig
/*  17:    */   extends DefaultSocketChannelConfig
/*  18:    */   implements OioSocketChannelConfig
/*  19:    */ {
/*  20:    */   @Deprecated
/*  21:    */   public DefaultOioSocketChannelConfig(SocketChannel channel, Socket javaSocket)
/*  22:    */   {
/*  23: 40 */     super(channel, javaSocket);
/*  24: 41 */     setAllocator(new PreferHeapByteBufAllocator(getAllocator()));
/*  25:    */   }
/*  26:    */   
/*  27:    */   DefaultOioSocketChannelConfig(OioSocketChannel channel, Socket javaSocket)
/*  28:    */   {
/*  29: 45 */     super(channel, javaSocket);
/*  30: 46 */     setAllocator(new PreferHeapByteBufAllocator(getAllocator()));
/*  31:    */   }
/*  32:    */   
/*  33:    */   public Map<ChannelOption<?>, Object> getOptions()
/*  34:    */   {
/*  35: 51 */     return getOptions(
/*  36: 52 */       super.getOptions(), new ChannelOption[] { ChannelOption.SO_TIMEOUT });
/*  37:    */   }
/*  38:    */   
/*  39:    */   public <T> T getOption(ChannelOption<T> option)
/*  40:    */   {
/*  41: 58 */     if (option == ChannelOption.SO_TIMEOUT) {
/*  42: 59 */       return Integer.valueOf(getSoTimeout());
/*  43:    */     }
/*  44: 61 */     return super.getOption(option);
/*  45:    */   }
/*  46:    */   
/*  47:    */   public <T> boolean setOption(ChannelOption<T> option, T value)
/*  48:    */   {
/*  49: 66 */     validate(option, value);
/*  50: 68 */     if (option == ChannelOption.SO_TIMEOUT) {
/*  51: 69 */       setSoTimeout(((Integer)value).intValue());
/*  52:    */     } else {
/*  53: 71 */       return super.setOption(option, value);
/*  54:    */     }
/*  55: 73 */     return true;
/*  56:    */   }
/*  57:    */   
/*  58:    */   public OioSocketChannelConfig setSoTimeout(int timeout)
/*  59:    */   {
/*  60:    */     try
/*  61:    */     {
/*  62: 79 */       this.javaSocket.setSoTimeout(timeout);
/*  63:    */     }
/*  64:    */     catch (IOException e)
/*  65:    */     {
/*  66: 81 */       throw new ChannelException(e);
/*  67:    */     }
/*  68: 83 */     return this;
/*  69:    */   }
/*  70:    */   
/*  71:    */   public int getSoTimeout()
/*  72:    */   {
/*  73:    */     try
/*  74:    */     {
/*  75: 89 */       return this.javaSocket.getSoTimeout();
/*  76:    */     }
/*  77:    */     catch (IOException e)
/*  78:    */     {
/*  79: 91 */       throw new ChannelException(e);
/*  80:    */     }
/*  81:    */   }
/*  82:    */   
/*  83:    */   public OioSocketChannelConfig setTcpNoDelay(boolean tcpNoDelay)
/*  84:    */   {
/*  85: 97 */     super.setTcpNoDelay(tcpNoDelay);
/*  86: 98 */     return this;
/*  87:    */   }
/*  88:    */   
/*  89:    */   public OioSocketChannelConfig setSoLinger(int soLinger)
/*  90:    */   {
/*  91:103 */     super.setSoLinger(soLinger);
/*  92:104 */     return this;
/*  93:    */   }
/*  94:    */   
/*  95:    */   public OioSocketChannelConfig setSendBufferSize(int sendBufferSize)
/*  96:    */   {
/*  97:109 */     super.setSendBufferSize(sendBufferSize);
/*  98:110 */     return this;
/*  99:    */   }
/* 100:    */   
/* 101:    */   public OioSocketChannelConfig setReceiveBufferSize(int receiveBufferSize)
/* 102:    */   {
/* 103:115 */     super.setReceiveBufferSize(receiveBufferSize);
/* 104:116 */     return this;
/* 105:    */   }
/* 106:    */   
/* 107:    */   public OioSocketChannelConfig setKeepAlive(boolean keepAlive)
/* 108:    */   {
/* 109:121 */     super.setKeepAlive(keepAlive);
/* 110:122 */     return this;
/* 111:    */   }
/* 112:    */   
/* 113:    */   public OioSocketChannelConfig setTrafficClass(int trafficClass)
/* 114:    */   {
/* 115:127 */     super.setTrafficClass(trafficClass);
/* 116:128 */     return this;
/* 117:    */   }
/* 118:    */   
/* 119:    */   public OioSocketChannelConfig setReuseAddress(boolean reuseAddress)
/* 120:    */   {
/* 121:133 */     super.setReuseAddress(reuseAddress);
/* 122:134 */     return this;
/* 123:    */   }
/* 124:    */   
/* 125:    */   public OioSocketChannelConfig setPerformancePreferences(int connectionTime, int latency, int bandwidth)
/* 126:    */   {
/* 127:139 */     super.setPerformancePreferences(connectionTime, latency, bandwidth);
/* 128:140 */     return this;
/* 129:    */   }
/* 130:    */   
/* 131:    */   public OioSocketChannelConfig setAllowHalfClosure(boolean allowHalfClosure)
/* 132:    */   {
/* 133:145 */     super.setAllowHalfClosure(allowHalfClosure);
/* 134:146 */     return this;
/* 135:    */   }
/* 136:    */   
/* 137:    */   public OioSocketChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis)
/* 138:    */   {
/* 139:151 */     super.setConnectTimeoutMillis(connectTimeoutMillis);
/* 140:152 */     return this;
/* 141:    */   }
/* 142:    */   
/* 143:    */   @Deprecated
/* 144:    */   public OioSocketChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead)
/* 145:    */   {
/* 146:158 */     super.setMaxMessagesPerRead(maxMessagesPerRead);
/* 147:159 */     return this;
/* 148:    */   }
/* 149:    */   
/* 150:    */   public OioSocketChannelConfig setWriteSpinCount(int writeSpinCount)
/* 151:    */   {
/* 152:164 */     super.setWriteSpinCount(writeSpinCount);
/* 153:165 */     return this;
/* 154:    */   }
/* 155:    */   
/* 156:    */   public OioSocketChannelConfig setAllocator(ByteBufAllocator allocator)
/* 157:    */   {
/* 158:170 */     super.setAllocator(allocator);
/* 159:171 */     return this;
/* 160:    */   }
/* 161:    */   
/* 162:    */   public OioSocketChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator)
/* 163:    */   {
/* 164:176 */     super.setRecvByteBufAllocator(allocator);
/* 165:177 */     return this;
/* 166:    */   }
/* 167:    */   
/* 168:    */   public OioSocketChannelConfig setAutoRead(boolean autoRead)
/* 169:    */   {
/* 170:182 */     super.setAutoRead(autoRead);
/* 171:183 */     return this;
/* 172:    */   }
/* 173:    */   
/* 174:    */   protected void autoReadCleared()
/* 175:    */   {
/* 176:188 */     if ((this.channel instanceof OioSocketChannel)) {
/* 177:189 */       ((OioSocketChannel)this.channel).clearReadPending0();
/* 178:    */     }
/* 179:    */   }
/* 180:    */   
/* 181:    */   public OioSocketChannelConfig setAutoClose(boolean autoClose)
/* 182:    */   {
/* 183:195 */     super.setAutoClose(autoClose);
/* 184:196 */     return this;
/* 185:    */   }
/* 186:    */   
/* 187:    */   public OioSocketChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark)
/* 188:    */   {
/* 189:201 */     super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
/* 190:202 */     return this;
/* 191:    */   }
/* 192:    */   
/* 193:    */   public OioSocketChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark)
/* 194:    */   {
/* 195:207 */     super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
/* 196:208 */     return this;
/* 197:    */   }
/* 198:    */   
/* 199:    */   public OioSocketChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark)
/* 200:    */   {
/* 201:213 */     super.setWriteBufferWaterMark(writeBufferWaterMark);
/* 202:214 */     return this;
/* 203:    */   }
/* 204:    */   
/* 205:    */   public OioSocketChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator)
/* 206:    */   {
/* 207:219 */     super.setMessageSizeEstimator(estimator);
/* 208:220 */     return this;
/* 209:    */   }
/* 210:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.socket.oio.DefaultOioSocketChannelConfig
 * JD-Core Version:    0.7.0.1
 */