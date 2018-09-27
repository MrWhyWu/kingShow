/*   1:    */ package io.netty.channel.epoll;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.channel.ChannelException;
/*   5:    */ import io.netty.channel.ChannelOption;
/*   6:    */ import io.netty.channel.MessageSizeEstimator;
/*   7:    */ import io.netty.channel.RecvByteBufAllocator;
/*   8:    */ import io.netty.channel.WriteBufferWaterMark;
/*   9:    */ import io.netty.channel.socket.ServerSocketChannelConfig;
/*  10:    */ import java.io.IOException;
/*  11:    */ import java.net.InetAddress;
/*  12:    */ import java.util.Map;
/*  13:    */ 
/*  14:    */ public final class EpollServerSocketChannelConfig
/*  15:    */   extends EpollServerChannelConfig
/*  16:    */   implements ServerSocketChannelConfig
/*  17:    */ {
/*  18:    */   EpollServerSocketChannelConfig(EpollServerSocketChannel channel)
/*  19:    */   {
/*  20: 34 */     super(channel);
/*  21:    */     
/*  22:    */ 
/*  23:    */ 
/*  24:    */ 
/*  25: 39 */     setReuseAddress(true);
/*  26:    */   }
/*  27:    */   
/*  28:    */   public Map<ChannelOption<?>, Object> getOptions()
/*  29:    */   {
/*  30: 44 */     return getOptions(super.getOptions(), new ChannelOption[] { EpollChannelOption.SO_REUSEPORT, EpollChannelOption.IP_FREEBIND, EpollChannelOption.IP_TRANSPARENT, EpollChannelOption.TCP_DEFER_ACCEPT });
/*  31:    */   }
/*  32:    */   
/*  33:    */   public <T> T getOption(ChannelOption<T> option)
/*  34:    */   {
/*  35: 51 */     if (option == EpollChannelOption.SO_REUSEPORT) {
/*  36: 52 */       return Boolean.valueOf(isReusePort());
/*  37:    */     }
/*  38: 54 */     if (option == EpollChannelOption.IP_FREEBIND) {
/*  39: 55 */       return Boolean.valueOf(isFreeBind());
/*  40:    */     }
/*  41: 57 */     if (option == EpollChannelOption.IP_TRANSPARENT) {
/*  42: 58 */       return Boolean.valueOf(isIpTransparent());
/*  43:    */     }
/*  44: 60 */     if (option == EpollChannelOption.TCP_DEFER_ACCEPT) {
/*  45: 61 */       return Integer.valueOf(getTcpDeferAccept());
/*  46:    */     }
/*  47: 63 */     return super.getOption(option);
/*  48:    */   }
/*  49:    */   
/*  50:    */   public <T> boolean setOption(ChannelOption<T> option, T value)
/*  51:    */   {
/*  52: 68 */     validate(option, value);
/*  53: 70 */     if (option == EpollChannelOption.SO_REUSEPORT)
/*  54:    */     {
/*  55: 71 */       setReusePort(((Boolean)value).booleanValue());
/*  56:    */     }
/*  57: 72 */     else if (option == EpollChannelOption.IP_FREEBIND)
/*  58:    */     {
/*  59: 73 */       setFreeBind(((Boolean)value).booleanValue());
/*  60:    */     }
/*  61: 74 */     else if (option == EpollChannelOption.IP_TRANSPARENT)
/*  62:    */     {
/*  63: 75 */       setIpTransparent(((Boolean)value).booleanValue());
/*  64:    */     }
/*  65: 76 */     else if (option == EpollChannelOption.TCP_MD5SIG)
/*  66:    */     {
/*  67: 78 */       Map<InetAddress, byte[]> m = (Map)value;
/*  68: 79 */       setTcpMd5Sig(m);
/*  69:    */     }
/*  70: 80 */     else if (option == EpollChannelOption.TCP_DEFER_ACCEPT)
/*  71:    */     {
/*  72: 81 */       setTcpDeferAccept(((Integer)value).intValue());
/*  73:    */     }
/*  74:    */     else
/*  75:    */     {
/*  76: 83 */       return super.setOption(option, value);
/*  77:    */     }
/*  78: 86 */     return true;
/*  79:    */   }
/*  80:    */   
/*  81:    */   public EpollServerSocketChannelConfig setReuseAddress(boolean reuseAddress)
/*  82:    */   {
/*  83: 91 */     super.setReuseAddress(reuseAddress);
/*  84: 92 */     return this;
/*  85:    */   }
/*  86:    */   
/*  87:    */   public EpollServerSocketChannelConfig setReceiveBufferSize(int receiveBufferSize)
/*  88:    */   {
/*  89: 97 */     super.setReceiveBufferSize(receiveBufferSize);
/*  90: 98 */     return this;
/*  91:    */   }
/*  92:    */   
/*  93:    */   public EpollServerSocketChannelConfig setPerformancePreferences(int connectionTime, int latency, int bandwidth)
/*  94:    */   {
/*  95:103 */     return this;
/*  96:    */   }
/*  97:    */   
/*  98:    */   public EpollServerSocketChannelConfig setBacklog(int backlog)
/*  99:    */   {
/* 100:108 */     super.setBacklog(backlog);
/* 101:109 */     return this;
/* 102:    */   }
/* 103:    */   
/* 104:    */   public EpollServerSocketChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis)
/* 105:    */   {
/* 106:114 */     super.setConnectTimeoutMillis(connectTimeoutMillis);
/* 107:115 */     return this;
/* 108:    */   }
/* 109:    */   
/* 110:    */   @Deprecated
/* 111:    */   public EpollServerSocketChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead)
/* 112:    */   {
/* 113:121 */     super.setMaxMessagesPerRead(maxMessagesPerRead);
/* 114:122 */     return this;
/* 115:    */   }
/* 116:    */   
/* 117:    */   public EpollServerSocketChannelConfig setWriteSpinCount(int writeSpinCount)
/* 118:    */   {
/* 119:127 */     super.setWriteSpinCount(writeSpinCount);
/* 120:128 */     return this;
/* 121:    */   }
/* 122:    */   
/* 123:    */   public EpollServerSocketChannelConfig setAllocator(ByteBufAllocator allocator)
/* 124:    */   {
/* 125:133 */     super.setAllocator(allocator);
/* 126:134 */     return this;
/* 127:    */   }
/* 128:    */   
/* 129:    */   public EpollServerSocketChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator)
/* 130:    */   {
/* 131:139 */     super.setRecvByteBufAllocator(allocator);
/* 132:140 */     return this;
/* 133:    */   }
/* 134:    */   
/* 135:    */   public EpollServerSocketChannelConfig setAutoRead(boolean autoRead)
/* 136:    */   {
/* 137:145 */     super.setAutoRead(autoRead);
/* 138:146 */     return this;
/* 139:    */   }
/* 140:    */   
/* 141:    */   @Deprecated
/* 142:    */   public EpollServerSocketChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark)
/* 143:    */   {
/* 144:152 */     super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
/* 145:153 */     return this;
/* 146:    */   }
/* 147:    */   
/* 148:    */   @Deprecated
/* 149:    */   public EpollServerSocketChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark)
/* 150:    */   {
/* 151:159 */     super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
/* 152:160 */     return this;
/* 153:    */   }
/* 154:    */   
/* 155:    */   public EpollServerSocketChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark)
/* 156:    */   {
/* 157:165 */     super.setWriteBufferWaterMark(writeBufferWaterMark);
/* 158:166 */     return this;
/* 159:    */   }
/* 160:    */   
/* 161:    */   public EpollServerSocketChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator)
/* 162:    */   {
/* 163:171 */     super.setMessageSizeEstimator(estimator);
/* 164:172 */     return this;
/* 165:    */   }
/* 166:    */   
/* 167:    */   public EpollServerSocketChannelConfig setTcpMd5Sig(Map<InetAddress, byte[]> keys)
/* 168:    */   {
/* 169:    */     try
/* 170:    */     {
/* 171:182 */       ((EpollServerSocketChannel)this.channel).setTcpMd5Sig(keys);
/* 172:183 */       return this;
/* 173:    */     }
/* 174:    */     catch (IOException e)
/* 175:    */     {
/* 176:185 */       throw new ChannelException(e);
/* 177:    */     }
/* 178:    */   }
/* 179:    */   
/* 180:    */   public boolean isReusePort()
/* 181:    */   {
/* 182:    */     try
/* 183:    */     {
/* 184:194 */       return this.channel.socket.isReusePort();
/* 185:    */     }
/* 186:    */     catch (IOException e)
/* 187:    */     {
/* 188:196 */       throw new ChannelException(e);
/* 189:    */     }
/* 190:    */   }
/* 191:    */   
/* 192:    */   public EpollServerSocketChannelConfig setReusePort(boolean reusePort)
/* 193:    */   {
/* 194:    */     try
/* 195:    */     {
/* 196:209 */       this.channel.socket.setReusePort(reusePort);
/* 197:210 */       return this;
/* 198:    */     }
/* 199:    */     catch (IOException e)
/* 200:    */     {
/* 201:212 */       throw new ChannelException(e);
/* 202:    */     }
/* 203:    */   }
/* 204:    */   
/* 205:    */   public boolean isFreeBind()
/* 206:    */   {
/* 207:    */     try
/* 208:    */     {
/* 209:222 */       return this.channel.socket.isIpFreeBind();
/* 210:    */     }
/* 211:    */     catch (IOException e)
/* 212:    */     {
/* 213:224 */       throw new ChannelException(e);
/* 214:    */     }
/* 215:    */   }
/* 216:    */   
/* 217:    */   public EpollServerSocketChannelConfig setFreeBind(boolean freeBind)
/* 218:    */   {
/* 219:    */     try
/* 220:    */     {
/* 221:234 */       this.channel.socket.setIpFreeBind(freeBind);
/* 222:235 */       return this;
/* 223:    */     }
/* 224:    */     catch (IOException e)
/* 225:    */     {
/* 226:237 */       throw new ChannelException(e);
/* 227:    */     }
/* 228:    */   }
/* 229:    */   
/* 230:    */   public boolean isIpTransparent()
/* 231:    */   {
/* 232:    */     try
/* 233:    */     {
/* 234:247 */       return this.channel.socket.isIpTransparent();
/* 235:    */     }
/* 236:    */     catch (IOException e)
/* 237:    */     {
/* 238:249 */       throw new ChannelException(e);
/* 239:    */     }
/* 240:    */   }
/* 241:    */   
/* 242:    */   public EpollServerSocketChannelConfig setIpTransparent(boolean transparent)
/* 243:    */   {
/* 244:    */     try
/* 245:    */     {
/* 246:259 */       this.channel.socket.setIpTransparent(transparent);
/* 247:260 */       return this;
/* 248:    */     }
/* 249:    */     catch (IOException e)
/* 250:    */     {
/* 251:262 */       throw new ChannelException(e);
/* 252:    */     }
/* 253:    */   }
/* 254:    */   
/* 255:    */   public EpollServerSocketChannelConfig setTcpDeferAccept(int deferAccept)
/* 256:    */   {
/* 257:    */     try
/* 258:    */     {
/* 259:271 */       this.channel.socket.setTcpDeferAccept(deferAccept);
/* 260:272 */       return this;
/* 261:    */     }
/* 262:    */     catch (IOException e)
/* 263:    */     {
/* 264:274 */       throw new ChannelException(e);
/* 265:    */     }
/* 266:    */   }
/* 267:    */   
/* 268:    */   public int getTcpDeferAccept()
/* 269:    */   {
/* 270:    */     try
/* 271:    */     {
/* 272:283 */       return this.channel.socket.getTcpDeferAccept();
/* 273:    */     }
/* 274:    */     catch (IOException e)
/* 275:    */     {
/* 276:285 */       throw new ChannelException(e);
/* 277:    */     }
/* 278:    */   }
/* 279:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.epoll.EpollServerSocketChannelConfig
 * JD-Core Version:    0.7.0.1
 */