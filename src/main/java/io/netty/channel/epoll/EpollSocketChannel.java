/*   1:    */ package io.netty.channel.epoll;
/*   2:    */ 
/*   3:    */ import io.netty.channel.Channel;
/*   4:    */ import io.netty.channel.ChannelException;
/*   5:    */ import io.netty.channel.socket.ServerSocketChannel;
/*   6:    */ import io.netty.channel.socket.SocketChannel;
/*   7:    */ import io.netty.util.concurrent.GlobalEventExecutor;
/*   8:    */ import java.io.IOException;
/*   9:    */ import java.net.InetAddress;
/*  10:    */ import java.net.InetSocketAddress;
/*  11:    */ import java.util.Collection;
/*  12:    */ import java.util.Collections;
/*  13:    */ import java.util.Map;
/*  14:    */ import java.util.concurrent.Executor;
/*  15:    */ 
/*  16:    */ public final class EpollSocketChannel
/*  17:    */   extends AbstractEpollStreamChannel
/*  18:    */   implements SocketChannel
/*  19:    */ {
/*  20:    */   private final EpollSocketChannelConfig config;
/*  21: 42 */   private volatile Collection<InetAddress> tcpMd5SigAddresses = Collections.emptyList();
/*  22:    */   
/*  23:    */   public EpollSocketChannel()
/*  24:    */   {
/*  25: 45 */     super(LinuxSocket.newSocketStream(), false);
/*  26: 46 */     this.config = new EpollSocketChannelConfig(this);
/*  27:    */   }
/*  28:    */   
/*  29:    */   public EpollSocketChannel(int fd)
/*  30:    */   {
/*  31: 50 */     super(fd);
/*  32: 51 */     this.config = new EpollSocketChannelConfig(this);
/*  33:    */   }
/*  34:    */   
/*  35:    */   EpollSocketChannel(LinuxSocket fd, boolean active)
/*  36:    */   {
/*  37: 55 */     super(fd, active);
/*  38: 56 */     this.config = new EpollSocketChannelConfig(this);
/*  39:    */   }
/*  40:    */   
/*  41:    */   EpollSocketChannel(Channel parent, LinuxSocket fd, InetSocketAddress remoteAddress)
/*  42:    */   {
/*  43: 60 */     super(parent, fd, remoteAddress);
/*  44: 61 */     this.config = new EpollSocketChannelConfig(this);
/*  45: 63 */     if ((parent instanceof EpollServerSocketChannel)) {
/*  46: 64 */       this.tcpMd5SigAddresses = ((EpollServerSocketChannel)parent).tcpMd5SigAddresses();
/*  47:    */     }
/*  48:    */   }
/*  49:    */   
/*  50:    */   public EpollTcpInfo tcpInfo()
/*  51:    */   {
/*  52: 72 */     return tcpInfo(new EpollTcpInfo());
/*  53:    */   }
/*  54:    */   
/*  55:    */   public EpollTcpInfo tcpInfo(EpollTcpInfo info)
/*  56:    */   {
/*  57:    */     try
/*  58:    */     {
/*  59: 81 */       this.socket.getTcpInfo(info);
/*  60: 82 */       return info;
/*  61:    */     }
/*  62:    */     catch (IOException e)
/*  63:    */     {
/*  64: 84 */       throw new ChannelException(e);
/*  65:    */     }
/*  66:    */   }
/*  67:    */   
/*  68:    */   public InetSocketAddress remoteAddress()
/*  69:    */   {
/*  70: 90 */     return (InetSocketAddress)super.remoteAddress();
/*  71:    */   }
/*  72:    */   
/*  73:    */   public InetSocketAddress localAddress()
/*  74:    */   {
/*  75: 95 */     return (InetSocketAddress)super.localAddress();
/*  76:    */   }
/*  77:    */   
/*  78:    */   public EpollSocketChannelConfig config()
/*  79:    */   {
/*  80:100 */     return this.config;
/*  81:    */   }
/*  82:    */   
/*  83:    */   public ServerSocketChannel parent()
/*  84:    */   {
/*  85:105 */     return (ServerSocketChannel)super.parent();
/*  86:    */   }
/*  87:    */   
/*  88:    */   protected AbstractEpollChannel.AbstractEpollUnsafe newUnsafe()
/*  89:    */   {
/*  90:110 */     return new EpollSocketChannelUnsafe(null);
/*  91:    */   }
/*  92:    */   
/*  93:    */   private final class EpollSocketChannelUnsafe
/*  94:    */     extends AbstractEpollStreamChannel.EpollStreamUnsafe
/*  95:    */   {
/*  96:    */     private EpollSocketChannelUnsafe()
/*  97:    */     {
/*  98:113 */       super();
/*  99:    */     }
/* 100:    */     
/* 101:    */     protected Executor prepareToClose()
/* 102:    */     {
/* 103:    */       try
/* 104:    */       {
/* 105:119 */         if ((EpollSocketChannel.this.isOpen()) && (EpollSocketChannel.this.config().getSoLinger() > 0))
/* 106:    */         {
/* 107:124 */           ((EpollEventLoop)EpollSocketChannel.this.eventLoop()).remove(EpollSocketChannel.this);
/* 108:125 */           return GlobalEventExecutor.INSTANCE;
/* 109:    */         }
/* 110:    */       }
/* 111:    */       catch (Throwable localThrowable) {}
/* 112:132 */       return null;
/* 113:    */     }
/* 114:    */   }
/* 115:    */   
/* 116:    */   void setTcpMd5Sig(Map<InetAddress, byte[]> keys)
/* 117:    */     throws IOException
/* 118:    */   {
/* 119:137 */     this.tcpMd5SigAddresses = TcpMd5Util.newTcpMd5Sigs(this, this.tcpMd5SigAddresses, keys);
/* 120:    */   }
/* 121:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.epoll.EpollSocketChannel
 * JD-Core Version:    0.7.0.1
 */