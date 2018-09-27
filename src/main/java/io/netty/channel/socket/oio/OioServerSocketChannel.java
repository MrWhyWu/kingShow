/*   1:    */ package io.netty.channel.socket.oio;
/*   2:    */ 
/*   3:    */ import io.netty.channel.ChannelException;
/*   4:    */ import io.netty.channel.ChannelMetadata;
/*   5:    */ import io.netty.channel.ChannelOutboundBuffer;
/*   6:    */ import io.netty.channel.oio.AbstractOioMessageChannel;
/*   7:    */ import io.netty.channel.socket.ServerSocketChannel;
/*   8:    */ import io.netty.util.internal.SocketUtils;
/*   9:    */ import io.netty.util.internal.logging.InternalLogger;
/*  10:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  11:    */ import java.io.IOException;
/*  12:    */ import java.net.InetSocketAddress;
/*  13:    */ import java.net.ServerSocket;
/*  14:    */ import java.net.Socket;
/*  15:    */ import java.net.SocketAddress;
/*  16:    */ import java.net.SocketTimeoutException;
/*  17:    */ import java.util.List;
/*  18:    */ import java.util.concurrent.locks.Lock;
/*  19:    */ import java.util.concurrent.locks.ReentrantLock;
/*  20:    */ 
/*  21:    */ public class OioServerSocketChannel
/*  22:    */   extends AbstractOioMessageChannel
/*  23:    */   implements ServerSocketChannel
/*  24:    */ {
/*  25: 46 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(OioServerSocketChannel.class);
/*  26: 48 */   private static final ChannelMetadata METADATA = new ChannelMetadata(false, 1);
/*  27:    */   final ServerSocket socket;
/*  28:    */   
/*  29:    */   private static ServerSocket newServerSocket()
/*  30:    */   {
/*  31:    */     try
/*  32:    */     {
/*  33: 52 */       return new ServerSocket();
/*  34:    */     }
/*  35:    */     catch (IOException e)
/*  36:    */     {
/*  37: 54 */       throw new ChannelException("failed to create a server socket", e);
/*  38:    */     }
/*  39:    */   }
/*  40:    */   
/*  41: 59 */   final Lock shutdownLock = new ReentrantLock();
/*  42:    */   private final OioServerSocketChannelConfig config;
/*  43:    */   
/*  44:    */   public OioServerSocketChannel()
/*  45:    */   {
/*  46: 66 */     this(newServerSocket());
/*  47:    */   }
/*  48:    */   
/*  49:    */   public OioServerSocketChannel(ServerSocket socket)
/*  50:    */   {
/*  51: 75 */     super(null);
/*  52: 76 */     if (socket == null) {
/*  53: 77 */       throw new NullPointerException("socket");
/*  54:    */     }
/*  55: 80 */     boolean success = false;
/*  56:    */     try
/*  57:    */     {
/*  58: 82 */       socket.setSoTimeout(1000);
/*  59: 83 */       success = true;
/*  60: 88 */       if (!success) {
/*  61:    */         try
/*  62:    */         {
/*  63: 90 */           socket.close();
/*  64:    */         }
/*  65:    */         catch (IOException e)
/*  66:    */         {
/*  67: 92 */           if (logger.isWarnEnabled()) {
/*  68: 93 */             logger.warn("Failed to close a partially initialized socket.", e);
/*  69:    */           }
/*  70:    */         }
/*  71:    */       }
/*  72: 99 */       this.socket = socket;
/*  73:    */     }
/*  74:    */     catch (IOException e)
/*  75:    */     {
/*  76: 85 */       throw new ChannelException("Failed to set the server socket timeout.", e);
/*  77:    */     }
/*  78:    */     finally
/*  79:    */     {
/*  80: 88 */       if (!success) {
/*  81:    */         try
/*  82:    */         {
/*  83: 90 */           socket.close();
/*  84:    */         }
/*  85:    */         catch (IOException e)
/*  86:    */         {
/*  87: 92 */           if (logger.isWarnEnabled()) {
/*  88: 93 */             logger.warn("Failed to close a partially initialized socket.", e);
/*  89:    */           }
/*  90:    */         }
/*  91:    */       }
/*  92:    */     }
/*  93:100 */     this.config = new DefaultOioServerSocketChannelConfig(this, socket);
/*  94:    */   }
/*  95:    */   
/*  96:    */   public InetSocketAddress localAddress()
/*  97:    */   {
/*  98:105 */     return (InetSocketAddress)super.localAddress();
/*  99:    */   }
/* 100:    */   
/* 101:    */   public ChannelMetadata metadata()
/* 102:    */   {
/* 103:110 */     return METADATA;
/* 104:    */   }
/* 105:    */   
/* 106:    */   public OioServerSocketChannelConfig config()
/* 107:    */   {
/* 108:115 */     return this.config;
/* 109:    */   }
/* 110:    */   
/* 111:    */   public InetSocketAddress remoteAddress()
/* 112:    */   {
/* 113:120 */     return null;
/* 114:    */   }
/* 115:    */   
/* 116:    */   public boolean isOpen()
/* 117:    */   {
/* 118:125 */     return !this.socket.isClosed();
/* 119:    */   }
/* 120:    */   
/* 121:    */   public boolean isActive()
/* 122:    */   {
/* 123:130 */     return (isOpen()) && (this.socket.isBound());
/* 124:    */   }
/* 125:    */   
/* 126:    */   protected SocketAddress localAddress0()
/* 127:    */   {
/* 128:135 */     return SocketUtils.localSocketAddress(this.socket);
/* 129:    */   }
/* 130:    */   
/* 131:    */   protected void doBind(SocketAddress localAddress)
/* 132:    */     throws Exception
/* 133:    */   {
/* 134:140 */     this.socket.bind(localAddress, this.config.getBacklog());
/* 135:    */   }
/* 136:    */   
/* 137:    */   protected void doClose()
/* 138:    */     throws Exception
/* 139:    */   {
/* 140:145 */     this.socket.close();
/* 141:    */   }
/* 142:    */   
/* 143:    */   protected int doReadMessages(List<Object> buf)
/* 144:    */     throws Exception
/* 145:    */   {
/* 146:150 */     if (this.socket.isClosed()) {
/* 147:151 */       return -1;
/* 148:    */     }
/* 149:    */     try
/* 150:    */     {
/* 151:155 */       Socket s = this.socket.accept();
/* 152:    */       try
/* 153:    */       {
/* 154:157 */         buf.add(new OioSocketChannel(this, s));
/* 155:158 */         return 1;
/* 156:    */       }
/* 157:    */       catch (Throwable t)
/* 158:    */       {
/* 159:160 */         logger.warn("Failed to create a new channel from an accepted socket.", t);
/* 160:    */         try
/* 161:    */         {
/* 162:162 */           s.close();
/* 163:    */         }
/* 164:    */         catch (Throwable t2)
/* 165:    */         {
/* 166:164 */           logger.warn("Failed to close a socket.", t2);
/* 167:    */         }
/* 168:    */       }
/* 169:170 */       return 0;
/* 170:    */     }
/* 171:    */     catch (SocketTimeoutException localSocketTimeoutException) {}
/* 172:    */   }
/* 173:    */   
/* 174:    */   protected void doWrite(ChannelOutboundBuffer in)
/* 175:    */     throws Exception
/* 176:    */   {
/* 177:175 */     throw new UnsupportedOperationException();
/* 178:    */   }
/* 179:    */   
/* 180:    */   protected Object filterOutboundMessage(Object msg)
/* 181:    */     throws Exception
/* 182:    */   {
/* 183:180 */     throw new UnsupportedOperationException();
/* 184:    */   }
/* 185:    */   
/* 186:    */   protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress)
/* 187:    */     throws Exception
/* 188:    */   {
/* 189:186 */     throw new UnsupportedOperationException();
/* 190:    */   }
/* 191:    */   
/* 192:    */   protected SocketAddress remoteAddress0()
/* 193:    */   {
/* 194:191 */     return null;
/* 195:    */   }
/* 196:    */   
/* 197:    */   protected void doDisconnect()
/* 198:    */     throws Exception
/* 199:    */   {
/* 200:196 */     throw new UnsupportedOperationException();
/* 201:    */   }
/* 202:    */   
/* 203:    */   @Deprecated
/* 204:    */   protected void setReadPending(boolean readPending)
/* 205:    */   {
/* 206:202 */     super.setReadPending(readPending);
/* 207:    */   }
/* 208:    */   
/* 209:    */   final void clearReadPending0()
/* 210:    */   {
/* 211:206 */     super.clearReadPending();
/* 212:    */   }
/* 213:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.socket.oio.OioServerSocketChannel
 * JD-Core Version:    0.7.0.1
 */