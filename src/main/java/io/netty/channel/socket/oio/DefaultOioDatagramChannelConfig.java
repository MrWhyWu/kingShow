/*   1:    */ package io.netty.channel.socket.oio;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.channel.ChannelException;
/*   5:    */ import io.netty.channel.ChannelOption;
/*   6:    */ import io.netty.channel.MessageSizeEstimator;
/*   7:    */ import io.netty.channel.PreferHeapByteBufAllocator;
/*   8:    */ import io.netty.channel.RecvByteBufAllocator;
/*   9:    */ import io.netty.channel.WriteBufferWaterMark;
/*  10:    */ import io.netty.channel.socket.DatagramChannel;
/*  11:    */ import io.netty.channel.socket.DefaultDatagramChannelConfig;
/*  12:    */ import java.io.IOException;
/*  13:    */ import java.net.DatagramSocket;
/*  14:    */ import java.net.InetAddress;
/*  15:    */ import java.net.NetworkInterface;
/*  16:    */ import java.util.Map;
/*  17:    */ 
/*  18:    */ final class DefaultOioDatagramChannelConfig
/*  19:    */   extends DefaultDatagramChannelConfig
/*  20:    */   implements OioDatagramChannelConfig
/*  21:    */ {
/*  22:    */   DefaultOioDatagramChannelConfig(DatagramChannel channel, DatagramSocket javaSocket)
/*  23:    */   {
/*  24: 39 */     super(channel, javaSocket);
/*  25: 40 */     setAllocator(new PreferHeapByteBufAllocator(getAllocator()));
/*  26:    */   }
/*  27:    */   
/*  28:    */   public Map<ChannelOption<?>, Object> getOptions()
/*  29:    */   {
/*  30: 45 */     return getOptions(super.getOptions(), new ChannelOption[] { ChannelOption.SO_TIMEOUT });
/*  31:    */   }
/*  32:    */   
/*  33:    */   public <T> T getOption(ChannelOption<T> option)
/*  34:    */   {
/*  35: 51 */     if (option == ChannelOption.SO_TIMEOUT) {
/*  36: 52 */       return Integer.valueOf(getSoTimeout());
/*  37:    */     }
/*  38: 54 */     return super.getOption(option);
/*  39:    */   }
/*  40:    */   
/*  41:    */   public <T> boolean setOption(ChannelOption<T> option, T value)
/*  42:    */   {
/*  43: 59 */     validate(option, value);
/*  44: 61 */     if (option == ChannelOption.SO_TIMEOUT) {
/*  45: 62 */       setSoTimeout(((Integer)value).intValue());
/*  46:    */     } else {
/*  47: 64 */       return super.setOption(option, value);
/*  48:    */     }
/*  49: 66 */     return true;
/*  50:    */   }
/*  51:    */   
/*  52:    */   public OioDatagramChannelConfig setSoTimeout(int timeout)
/*  53:    */   {
/*  54:    */     try
/*  55:    */     {
/*  56: 72 */       javaSocket().setSoTimeout(timeout);
/*  57:    */     }
/*  58:    */     catch (IOException e)
/*  59:    */     {
/*  60: 74 */       throw new ChannelException(e);
/*  61:    */     }
/*  62: 76 */     return this;
/*  63:    */   }
/*  64:    */   
/*  65:    */   public int getSoTimeout()
/*  66:    */   {
/*  67:    */     try
/*  68:    */     {
/*  69: 82 */       return javaSocket().getSoTimeout();
/*  70:    */     }
/*  71:    */     catch (IOException e)
/*  72:    */     {
/*  73: 84 */       throw new ChannelException(e);
/*  74:    */     }
/*  75:    */   }
/*  76:    */   
/*  77:    */   public OioDatagramChannelConfig setBroadcast(boolean broadcast)
/*  78:    */   {
/*  79: 90 */     super.setBroadcast(broadcast);
/*  80: 91 */     return this;
/*  81:    */   }
/*  82:    */   
/*  83:    */   public OioDatagramChannelConfig setInterface(InetAddress interfaceAddress)
/*  84:    */   {
/*  85: 96 */     super.setInterface(interfaceAddress);
/*  86: 97 */     return this;
/*  87:    */   }
/*  88:    */   
/*  89:    */   public OioDatagramChannelConfig setLoopbackModeDisabled(boolean loopbackModeDisabled)
/*  90:    */   {
/*  91:102 */     super.setLoopbackModeDisabled(loopbackModeDisabled);
/*  92:103 */     return this;
/*  93:    */   }
/*  94:    */   
/*  95:    */   public OioDatagramChannelConfig setNetworkInterface(NetworkInterface networkInterface)
/*  96:    */   {
/*  97:108 */     super.setNetworkInterface(networkInterface);
/*  98:109 */     return this;
/*  99:    */   }
/* 100:    */   
/* 101:    */   public OioDatagramChannelConfig setReuseAddress(boolean reuseAddress)
/* 102:    */   {
/* 103:114 */     super.setReuseAddress(reuseAddress);
/* 104:115 */     return this;
/* 105:    */   }
/* 106:    */   
/* 107:    */   public OioDatagramChannelConfig setReceiveBufferSize(int receiveBufferSize)
/* 108:    */   {
/* 109:120 */     super.setReceiveBufferSize(receiveBufferSize);
/* 110:121 */     return this;
/* 111:    */   }
/* 112:    */   
/* 113:    */   public OioDatagramChannelConfig setSendBufferSize(int sendBufferSize)
/* 114:    */   {
/* 115:126 */     super.setSendBufferSize(sendBufferSize);
/* 116:127 */     return this;
/* 117:    */   }
/* 118:    */   
/* 119:    */   public OioDatagramChannelConfig setTimeToLive(int ttl)
/* 120:    */   {
/* 121:132 */     super.setTimeToLive(ttl);
/* 122:133 */     return this;
/* 123:    */   }
/* 124:    */   
/* 125:    */   public OioDatagramChannelConfig setTrafficClass(int trafficClass)
/* 126:    */   {
/* 127:138 */     super.setTrafficClass(trafficClass);
/* 128:139 */     return this;
/* 129:    */   }
/* 130:    */   
/* 131:    */   public OioDatagramChannelConfig setWriteSpinCount(int writeSpinCount)
/* 132:    */   {
/* 133:144 */     super.setWriteSpinCount(writeSpinCount);
/* 134:145 */     return this;
/* 135:    */   }
/* 136:    */   
/* 137:    */   public OioDatagramChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis)
/* 138:    */   {
/* 139:150 */     super.setConnectTimeoutMillis(connectTimeoutMillis);
/* 140:151 */     return this;
/* 141:    */   }
/* 142:    */   
/* 143:    */   public OioDatagramChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead)
/* 144:    */   {
/* 145:156 */     super.setMaxMessagesPerRead(maxMessagesPerRead);
/* 146:157 */     return this;
/* 147:    */   }
/* 148:    */   
/* 149:    */   public OioDatagramChannelConfig setAllocator(ByteBufAllocator allocator)
/* 150:    */   {
/* 151:162 */     super.setAllocator(allocator);
/* 152:163 */     return this;
/* 153:    */   }
/* 154:    */   
/* 155:    */   public OioDatagramChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator)
/* 156:    */   {
/* 157:168 */     super.setRecvByteBufAllocator(allocator);
/* 158:169 */     return this;
/* 159:    */   }
/* 160:    */   
/* 161:    */   public OioDatagramChannelConfig setAutoRead(boolean autoRead)
/* 162:    */   {
/* 163:174 */     super.setAutoRead(autoRead);
/* 164:175 */     return this;
/* 165:    */   }
/* 166:    */   
/* 167:    */   public OioDatagramChannelConfig setAutoClose(boolean autoClose)
/* 168:    */   {
/* 169:180 */     super.setAutoClose(autoClose);
/* 170:181 */     return this;
/* 171:    */   }
/* 172:    */   
/* 173:    */   public OioDatagramChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark)
/* 174:    */   {
/* 175:186 */     super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
/* 176:187 */     return this;
/* 177:    */   }
/* 178:    */   
/* 179:    */   public OioDatagramChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark)
/* 180:    */   {
/* 181:192 */     super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
/* 182:193 */     return this;
/* 183:    */   }
/* 184:    */   
/* 185:    */   public OioDatagramChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark)
/* 186:    */   {
/* 187:198 */     super.setWriteBufferWaterMark(writeBufferWaterMark);
/* 188:199 */     return this;
/* 189:    */   }
/* 190:    */   
/* 191:    */   public OioDatagramChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator)
/* 192:    */   {
/* 193:204 */     super.setMessageSizeEstimator(estimator);
/* 194:205 */     return this;
/* 195:    */   }
/* 196:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.socket.oio.DefaultOioDatagramChannelConfig
 * JD-Core Version:    0.7.0.1
 */