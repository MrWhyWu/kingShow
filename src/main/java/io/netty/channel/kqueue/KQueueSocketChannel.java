/*  1:   */ package io.netty.channel.kqueue;
/*  2:   */ 
/*  3:   */ import io.netty.channel.Channel;
/*  4:   */ import io.netty.channel.socket.ServerSocketChannel;
/*  5:   */ import io.netty.channel.socket.SocketChannel;
/*  6:   */ import io.netty.util.concurrent.GlobalEventExecutor;
/*  7:   */ import java.net.InetSocketAddress;
/*  8:   */ import java.util.concurrent.Executor;
/*  9:   */ 
/* 10:   */ public final class KQueueSocketChannel
/* 11:   */   extends AbstractKQueueStreamChannel
/* 12:   */   implements SocketChannel
/* 13:   */ {
/* 14:   */   private final KQueueSocketChannelConfig config;
/* 15:   */   
/* 16:   */   public KQueueSocketChannel()
/* 17:   */   {
/* 18:32 */     super(null, BsdSocket.newSocketStream(), false);
/* 19:33 */     this.config = new KQueueSocketChannelConfig(this);
/* 20:   */   }
/* 21:   */   
/* 22:   */   public KQueueSocketChannel(int fd)
/* 23:   */   {
/* 24:37 */     super(new BsdSocket(fd));
/* 25:38 */     this.config = new KQueueSocketChannelConfig(this);
/* 26:   */   }
/* 27:   */   
/* 28:   */   KQueueSocketChannel(Channel parent, BsdSocket fd, InetSocketAddress remoteAddress)
/* 29:   */   {
/* 30:42 */     super(parent, fd, remoteAddress);
/* 31:43 */     this.config = new KQueueSocketChannelConfig(this);
/* 32:   */   }
/* 33:   */   
/* 34:   */   public InetSocketAddress remoteAddress()
/* 35:   */   {
/* 36:48 */     return (InetSocketAddress)super.remoteAddress();
/* 37:   */   }
/* 38:   */   
/* 39:   */   public InetSocketAddress localAddress()
/* 40:   */   {
/* 41:53 */     return (InetSocketAddress)super.localAddress();
/* 42:   */   }
/* 43:   */   
/* 44:   */   public KQueueSocketChannelConfig config()
/* 45:   */   {
/* 46:58 */     return this.config;
/* 47:   */   }
/* 48:   */   
/* 49:   */   public ServerSocketChannel parent()
/* 50:   */   {
/* 51:63 */     return (ServerSocketChannel)super.parent();
/* 52:   */   }
/* 53:   */   
/* 54:   */   protected AbstractKQueueChannel.AbstractKQueueUnsafe newUnsafe()
/* 55:   */   {
/* 56:68 */     return new KQueueSocketChannelUnsafe(null);
/* 57:   */   }
/* 58:   */   
/* 59:   */   private final class KQueueSocketChannelUnsafe
/* 60:   */     extends AbstractKQueueStreamChannel.KQueueStreamUnsafe
/* 61:   */   {
/* 62:   */     private KQueueSocketChannelUnsafe()
/* 63:   */     {
/* 64:71 */       super();
/* 65:   */     }
/* 66:   */     
/* 67:   */     protected Executor prepareToClose()
/* 68:   */     {
/* 69:   */       try
/* 70:   */       {
/* 71:77 */         if ((KQueueSocketChannel.this.isOpen()) && (KQueueSocketChannel.this.config().getSoLinger() > 0))
/* 72:   */         {
/* 73:82 */           ((KQueueEventLoop)KQueueSocketChannel.this.eventLoop()).remove(KQueueSocketChannel.this);
/* 74:83 */           return GlobalEventExecutor.INSTANCE;
/* 75:   */         }
/* 76:   */       }
/* 77:   */       catch (Throwable localThrowable) {}
/* 78:90 */       return null;
/* 79:   */     }
/* 80:   */   }
/* 81:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.kqueue.KQueueSocketChannel
 * JD-Core Version:    0.7.0.1
 */