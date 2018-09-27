/*   1:    */ package io.netty.channel.socket.nio;
/*   2:    */ 
/*   3:    */ import io.netty.channel.ChannelException;
/*   4:    */ import io.netty.channel.ChannelMetadata;
/*   5:    */ import io.netty.channel.ChannelOutboundBuffer;
/*   6:    */ import io.netty.channel.nio.AbstractNioMessageChannel;
/*   7:    */ import io.netty.channel.socket.DefaultServerSocketChannelConfig;
/*   8:    */ import io.netty.channel.socket.ServerSocketChannelConfig;
/*   9:    */ import io.netty.util.internal.PlatformDependent;
/*  10:    */ import io.netty.util.internal.SocketUtils;
/*  11:    */ import io.netty.util.internal.logging.InternalLogger;
/*  12:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  13:    */ import java.io.IOException;
/*  14:    */ import java.net.InetSocketAddress;
/*  15:    */ import java.net.ServerSocket;
/*  16:    */ import java.net.SocketAddress;
/*  17:    */ import java.nio.channels.SocketChannel;
/*  18:    */ import java.nio.channels.spi.SelectorProvider;
/*  19:    */ import java.util.List;
/*  20:    */ 
/*  21:    */ public class NioServerSocketChannel
/*  22:    */   extends AbstractNioMessageChannel
/*  23:    */   implements io.netty.channel.socket.ServerSocketChannel
/*  24:    */ {
/*  25: 46 */   private static final ChannelMetadata METADATA = new ChannelMetadata(false, 16);
/*  26: 47 */   private static final SelectorProvider DEFAULT_SELECTOR_PROVIDER = SelectorProvider.provider();
/*  27: 49 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(NioServerSocketChannel.class);
/*  28:    */   private final ServerSocketChannelConfig config;
/*  29:    */   
/*  30:    */   private static java.nio.channels.ServerSocketChannel newSocket(SelectorProvider provider)
/*  31:    */   {
/*  32:    */     try
/*  33:    */     {
/*  34: 59 */       return provider.openServerSocketChannel();
/*  35:    */     }
/*  36:    */     catch (IOException e)
/*  37:    */     {
/*  38: 61 */       throw new ChannelException("Failed to open a server socket.", e);
/*  39:    */     }
/*  40:    */   }
/*  41:    */   
/*  42:    */   public NioServerSocketChannel()
/*  43:    */   {
/*  44: 72 */     this(newSocket(DEFAULT_SELECTOR_PROVIDER));
/*  45:    */   }
/*  46:    */   
/*  47:    */   public NioServerSocketChannel(SelectorProvider provider)
/*  48:    */   {
/*  49: 79 */     this(newSocket(provider));
/*  50:    */   }
/*  51:    */   
/*  52:    */   public NioServerSocketChannel(java.nio.channels.ServerSocketChannel channel)
/*  53:    */   {
/*  54: 86 */     super(null, channel, 16);
/*  55: 87 */     this.config = new NioServerSocketChannelConfig(this, javaChannel().socket(), null);
/*  56:    */   }
/*  57:    */   
/*  58:    */   public InetSocketAddress localAddress()
/*  59:    */   {
/*  60: 92 */     return (InetSocketAddress)super.localAddress();
/*  61:    */   }
/*  62:    */   
/*  63:    */   public ChannelMetadata metadata()
/*  64:    */   {
/*  65: 97 */     return METADATA;
/*  66:    */   }
/*  67:    */   
/*  68:    */   public ServerSocketChannelConfig config()
/*  69:    */   {
/*  70:102 */     return this.config;
/*  71:    */   }
/*  72:    */   
/*  73:    */   public boolean isActive()
/*  74:    */   {
/*  75:107 */     return javaChannel().socket().isBound();
/*  76:    */   }
/*  77:    */   
/*  78:    */   public InetSocketAddress remoteAddress()
/*  79:    */   {
/*  80:112 */     return null;
/*  81:    */   }
/*  82:    */   
/*  83:    */   protected java.nio.channels.ServerSocketChannel javaChannel()
/*  84:    */   {
/*  85:117 */     return (java.nio.channels.ServerSocketChannel)super.javaChannel();
/*  86:    */   }
/*  87:    */   
/*  88:    */   protected SocketAddress localAddress0()
/*  89:    */   {
/*  90:122 */     return SocketUtils.localSocketAddress(javaChannel().socket());
/*  91:    */   }
/*  92:    */   
/*  93:    */   protected void doBind(SocketAddress localAddress)
/*  94:    */     throws Exception
/*  95:    */   {
/*  96:127 */     if (PlatformDependent.javaVersion() >= 7) {
/*  97:128 */       javaChannel().bind(localAddress, this.config.getBacklog());
/*  98:    */     } else {
/*  99:130 */       javaChannel().socket().bind(localAddress, this.config.getBacklog());
/* 100:    */     }
/* 101:    */   }
/* 102:    */   
/* 103:    */   protected void doClose()
/* 104:    */     throws Exception
/* 105:    */   {
/* 106:136 */     javaChannel().close();
/* 107:    */   }
/* 108:    */   
/* 109:    */   protected int doReadMessages(List<Object> buf)
/* 110:    */     throws Exception
/* 111:    */   {
/* 112:141 */     SocketChannel ch = SocketUtils.accept(javaChannel());
/* 113:    */     try
/* 114:    */     {
/* 115:144 */       if (ch != null)
/* 116:    */       {
/* 117:145 */         buf.add(new NioSocketChannel(this, ch));
/* 118:146 */         return 1;
/* 119:    */       }
/* 120:    */     }
/* 121:    */     catch (Throwable t)
/* 122:    */     {
/* 123:149 */       logger.warn("Failed to create a new channel from an accepted socket.", t);
/* 124:    */       try
/* 125:    */       {
/* 126:152 */         ch.close();
/* 127:    */       }
/* 128:    */       catch (Throwable t2)
/* 129:    */       {
/* 130:154 */         logger.warn("Failed to close a socket.", t2);
/* 131:    */       }
/* 132:    */     }
/* 133:158 */     return 0;
/* 134:    */   }
/* 135:    */   
/* 136:    */   protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress)
/* 137:    */     throws Exception
/* 138:    */   {
/* 139:165 */     throw new UnsupportedOperationException();
/* 140:    */   }
/* 141:    */   
/* 142:    */   protected void doFinishConnect()
/* 143:    */     throws Exception
/* 144:    */   {
/* 145:170 */     throw new UnsupportedOperationException();
/* 146:    */   }
/* 147:    */   
/* 148:    */   protected SocketAddress remoteAddress0()
/* 149:    */   {
/* 150:175 */     return null;
/* 151:    */   }
/* 152:    */   
/* 153:    */   protected void doDisconnect()
/* 154:    */     throws Exception
/* 155:    */   {
/* 156:180 */     throw new UnsupportedOperationException();
/* 157:    */   }
/* 158:    */   
/* 159:    */   protected boolean doWriteMessage(Object msg, ChannelOutboundBuffer in)
/* 160:    */     throws Exception
/* 161:    */   {
/* 162:185 */     throw new UnsupportedOperationException();
/* 163:    */   }
/* 164:    */   
/* 165:    */   protected final Object filterOutboundMessage(Object msg)
/* 166:    */     throws Exception
/* 167:    */   {
/* 168:190 */     throw new UnsupportedOperationException();
/* 169:    */   }
/* 170:    */   
/* 171:    */   private final class NioServerSocketChannelConfig
/* 172:    */     extends DefaultServerSocketChannelConfig
/* 173:    */   {
/* 174:    */     private NioServerSocketChannelConfig(NioServerSocketChannel channel, ServerSocket javaSocket)
/* 175:    */     {
/* 176:195 */       super(javaSocket);
/* 177:    */     }
/* 178:    */     
/* 179:    */     protected void autoReadCleared()
/* 180:    */     {
/* 181:200 */       NioServerSocketChannel.this.clearReadPending();
/* 182:    */     }
/* 183:    */   }
/* 184:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.socket.nio.NioServerSocketChannel
 * JD-Core Version:    0.7.0.1
 */