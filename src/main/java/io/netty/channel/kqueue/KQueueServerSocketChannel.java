/*  1:   */ package io.netty.channel.kqueue;
/*  2:   */ 
/*  3:   */ import io.netty.channel.Channel;
/*  4:   */ import io.netty.channel.EventLoop;
/*  5:   */ import io.netty.channel.socket.ServerSocketChannel;
/*  6:   */ import io.netty.channel.unix.NativeInetAddress;
/*  7:   */ import java.net.InetSocketAddress;
/*  8:   */ import java.net.SocketAddress;
/*  9:   */ 
/* 10:   */ public final class KQueueServerSocketChannel
/* 11:   */   extends AbstractKQueueServerChannel
/* 12:   */   implements ServerSocketChannel
/* 13:   */ {
/* 14:   */   private final KQueueServerSocketChannelConfig config;
/* 15:   */   
/* 16:   */   public KQueueServerSocketChannel()
/* 17:   */   {
/* 18:34 */     super(BsdSocket.newSocketStream(), false);
/* 19:35 */     this.config = new KQueueServerSocketChannelConfig(this);
/* 20:   */   }
/* 21:   */   
/* 22:   */   public KQueueServerSocketChannel(int fd)
/* 23:   */   {
/* 24:41 */     this(new BsdSocket(fd));
/* 25:   */   }
/* 26:   */   
/* 27:   */   KQueueServerSocketChannel(BsdSocket fd)
/* 28:   */   {
/* 29:45 */     super(fd);
/* 30:46 */     this.config = new KQueueServerSocketChannelConfig(this);
/* 31:   */   }
/* 32:   */   
/* 33:   */   KQueueServerSocketChannel(BsdSocket fd, boolean active)
/* 34:   */   {
/* 35:50 */     super(fd, active);
/* 36:51 */     this.config = new KQueueServerSocketChannelConfig(this);
/* 37:   */   }
/* 38:   */   
/* 39:   */   protected boolean isCompatible(EventLoop loop)
/* 40:   */   {
/* 41:56 */     return loop instanceof KQueueEventLoop;
/* 42:   */   }
/* 43:   */   
/* 44:   */   protected void doBind(SocketAddress localAddress)
/* 45:   */     throws Exception
/* 46:   */   {
/* 47:61 */     super.doBind(localAddress);
/* 48:   */     
/* 49:   */ 
/* 50:64 */     this.socket.listen(this.config.getBacklog());
/* 51:65 */     this.active = true;
/* 52:   */   }
/* 53:   */   
/* 54:   */   public InetSocketAddress remoteAddress()
/* 55:   */   {
/* 56:70 */     return (InetSocketAddress)super.remoteAddress();
/* 57:   */   }
/* 58:   */   
/* 59:   */   public InetSocketAddress localAddress()
/* 60:   */   {
/* 61:75 */     return (InetSocketAddress)super.localAddress();
/* 62:   */   }
/* 63:   */   
/* 64:   */   public KQueueServerSocketChannelConfig config()
/* 65:   */   {
/* 66:80 */     return this.config;
/* 67:   */   }
/* 68:   */   
/* 69:   */   protected Channel newChildChannel(int fd, byte[] address, int offset, int len)
/* 70:   */     throws Exception
/* 71:   */   {
/* 72:85 */     return new KQueueSocketChannel(this, new BsdSocket(fd), NativeInetAddress.address(address, offset, len));
/* 73:   */   }
/* 74:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.kqueue.KQueueServerSocketChannel
 * JD-Core Version:    0.7.0.1
 */