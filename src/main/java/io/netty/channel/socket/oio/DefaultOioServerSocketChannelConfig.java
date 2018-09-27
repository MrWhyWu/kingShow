/*   1:    */ package io.netty.channel.socket.oio;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.channel.ChannelException;
/*   5:    */ import io.netty.channel.ChannelOption;
/*   6:    */ import io.netty.channel.MessageSizeEstimator;
/*   7:    */ import io.netty.channel.PreferHeapByteBufAllocator;
/*   8:    */ import io.netty.channel.RecvByteBufAllocator;
/*   9:    */ import io.netty.channel.WriteBufferWaterMark;
/*  10:    */ import io.netty.channel.socket.DefaultServerSocketChannelConfig;
/*  11:    */ import io.netty.channel.socket.ServerSocketChannel;
/*  12:    */ import java.io.IOException;
/*  13:    */ import java.net.ServerSocket;
/*  14:    */ import java.util.Map;
/*  15:    */ 
/*  16:    */ public class DefaultOioServerSocketChannelConfig
/*  17:    */   extends DefaultServerSocketChannelConfig
/*  18:    */   implements OioServerSocketChannelConfig
/*  19:    */ {
/*  20:    */   @Deprecated
/*  21:    */   public DefaultOioServerSocketChannelConfig(ServerSocketChannel channel, ServerSocket javaSocket)
/*  22:    */   {
/*  23: 42 */     super(channel, javaSocket);
/*  24: 43 */     setAllocator(new PreferHeapByteBufAllocator(getAllocator()));
/*  25:    */   }
/*  26:    */   
/*  27:    */   DefaultOioServerSocketChannelConfig(OioServerSocketChannel channel, ServerSocket javaSocket)
/*  28:    */   {
/*  29: 47 */     super(channel, javaSocket);
/*  30: 48 */     setAllocator(new PreferHeapByteBufAllocator(getAllocator()));
/*  31:    */   }
/*  32:    */   
/*  33:    */   public Map<ChannelOption<?>, Object> getOptions()
/*  34:    */   {
/*  35: 53 */     return getOptions(
/*  36: 54 */       super.getOptions(), new ChannelOption[] { ChannelOption.SO_TIMEOUT });
/*  37:    */   }
/*  38:    */   
/*  39:    */   public <T> T getOption(ChannelOption<T> option)
/*  40:    */   {
/*  41: 60 */     if (option == ChannelOption.SO_TIMEOUT) {
/*  42: 61 */       return Integer.valueOf(getSoTimeout());
/*  43:    */     }
/*  44: 63 */     return super.getOption(option);
/*  45:    */   }
/*  46:    */   
/*  47:    */   public <T> boolean setOption(ChannelOption<T> option, T value)
/*  48:    */   {
/*  49: 68 */     validate(option, value);
/*  50: 70 */     if (option == ChannelOption.SO_TIMEOUT) {
/*  51: 71 */       setSoTimeout(((Integer)value).intValue());
/*  52:    */     } else {
/*  53: 73 */       return super.setOption(option, value);
/*  54:    */     }
/*  55: 75 */     return true;
/*  56:    */   }
/*  57:    */   
/*  58:    */   public OioServerSocketChannelConfig setSoTimeout(int timeout)
/*  59:    */   {
/*  60:    */     try
/*  61:    */     {
/*  62: 81 */       this.javaSocket.setSoTimeout(timeout);
/*  63:    */     }
/*  64:    */     catch (IOException e)
/*  65:    */     {
/*  66: 83 */       throw new ChannelException(e);
/*  67:    */     }
/*  68: 85 */     return this;
/*  69:    */   }
/*  70:    */   
/*  71:    */   public int getSoTimeout()
/*  72:    */   {
/*  73:    */     try
/*  74:    */     {
/*  75: 91 */       return this.javaSocket.getSoTimeout();
/*  76:    */     }
/*  77:    */     catch (IOException e)
/*  78:    */     {
/*  79: 93 */       throw new ChannelException(e);
/*  80:    */     }
/*  81:    */   }
/*  82:    */   
/*  83:    */   public OioServerSocketChannelConfig setBacklog(int backlog)
/*  84:    */   {
/*  85: 99 */     super.setBacklog(backlog);
/*  86:100 */     return this;
/*  87:    */   }
/*  88:    */   
/*  89:    */   public OioServerSocketChannelConfig setReuseAddress(boolean reuseAddress)
/*  90:    */   {
/*  91:105 */     super.setReuseAddress(reuseAddress);
/*  92:106 */     return this;
/*  93:    */   }
/*  94:    */   
/*  95:    */   public OioServerSocketChannelConfig setReceiveBufferSize(int receiveBufferSize)
/*  96:    */   {
/*  97:111 */     super.setReceiveBufferSize(receiveBufferSize);
/*  98:112 */     return this;
/*  99:    */   }
/* 100:    */   
/* 101:    */   public OioServerSocketChannelConfig setPerformancePreferences(int connectionTime, int latency, int bandwidth)
/* 102:    */   {
/* 103:117 */     super.setPerformancePreferences(connectionTime, latency, bandwidth);
/* 104:118 */     return this;
/* 105:    */   }
/* 106:    */   
/* 107:    */   public OioServerSocketChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis)
/* 108:    */   {
/* 109:123 */     super.setConnectTimeoutMillis(connectTimeoutMillis);
/* 110:124 */     return this;
/* 111:    */   }
/* 112:    */   
/* 113:    */   @Deprecated
/* 114:    */   public OioServerSocketChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead)
/* 115:    */   {
/* 116:130 */     super.setMaxMessagesPerRead(maxMessagesPerRead);
/* 117:131 */     return this;
/* 118:    */   }
/* 119:    */   
/* 120:    */   public OioServerSocketChannelConfig setWriteSpinCount(int writeSpinCount)
/* 121:    */   {
/* 122:136 */     super.setWriteSpinCount(writeSpinCount);
/* 123:137 */     return this;
/* 124:    */   }
/* 125:    */   
/* 126:    */   public OioServerSocketChannelConfig setAllocator(ByteBufAllocator allocator)
/* 127:    */   {
/* 128:142 */     super.setAllocator(allocator);
/* 129:143 */     return this;
/* 130:    */   }
/* 131:    */   
/* 132:    */   public OioServerSocketChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator)
/* 133:    */   {
/* 134:148 */     super.setRecvByteBufAllocator(allocator);
/* 135:149 */     return this;
/* 136:    */   }
/* 137:    */   
/* 138:    */   public OioServerSocketChannelConfig setAutoRead(boolean autoRead)
/* 139:    */   {
/* 140:154 */     super.setAutoRead(autoRead);
/* 141:155 */     return this;
/* 142:    */   }
/* 143:    */   
/* 144:    */   protected void autoReadCleared()
/* 145:    */   {
/* 146:160 */     if ((this.channel instanceof OioServerSocketChannel)) {
/* 147:161 */       ((OioServerSocketChannel)this.channel).clearReadPending0();
/* 148:    */     }
/* 149:    */   }
/* 150:    */   
/* 151:    */   public OioServerSocketChannelConfig setAutoClose(boolean autoClose)
/* 152:    */   {
/* 153:167 */     super.setAutoClose(autoClose);
/* 154:168 */     return this;
/* 155:    */   }
/* 156:    */   
/* 157:    */   public OioServerSocketChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark)
/* 158:    */   {
/* 159:173 */     super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
/* 160:174 */     return this;
/* 161:    */   }
/* 162:    */   
/* 163:    */   public OioServerSocketChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark)
/* 164:    */   {
/* 165:179 */     super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
/* 166:180 */     return this;
/* 167:    */   }
/* 168:    */   
/* 169:    */   public OioServerSocketChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark)
/* 170:    */   {
/* 171:185 */     super.setWriteBufferWaterMark(writeBufferWaterMark);
/* 172:186 */     return this;
/* 173:    */   }
/* 174:    */   
/* 175:    */   public OioServerSocketChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator)
/* 176:    */   {
/* 177:191 */     super.setMessageSizeEstimator(estimator);
/* 178:192 */     return this;
/* 179:    */   }
/* 180:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.socket.oio.DefaultOioServerSocketChannelConfig
 * JD-Core Version:    0.7.0.1
 */