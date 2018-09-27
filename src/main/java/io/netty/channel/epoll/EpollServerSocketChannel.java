/*   1:    */ package io.netty.channel.epoll;
/*   2:    */ 
/*   3:    */ import io.netty.channel.Channel;
/*   4:    */ import io.netty.channel.EventLoop;
/*   5:    */ import io.netty.channel.socket.ServerSocketChannel;
/*   6:    */ import io.netty.channel.unix.NativeInetAddress;
/*   7:    */ import java.io.IOException;
/*   8:    */ import java.net.InetAddress;
/*   9:    */ import java.net.InetSocketAddress;
/*  10:    */ import java.net.SocketAddress;
/*  11:    */ import java.util.Collection;
/*  12:    */ import java.util.Collections;
/*  13:    */ import java.util.Map;
/*  14:    */ 
/*  15:    */ public final class EpollServerSocketChannel
/*  16:    */   extends AbstractEpollServerChannel
/*  17:    */   implements ServerSocketChannel
/*  18:    */ {
/*  19:    */   private final EpollServerSocketChannelConfig config;
/*  20: 40 */   private volatile Collection<InetAddress> tcpMd5SigAddresses = Collections.emptyList();
/*  21:    */   
/*  22:    */   public EpollServerSocketChannel()
/*  23:    */   {
/*  24: 43 */     super(LinuxSocket.newSocketStream(), false);
/*  25: 44 */     this.config = new EpollServerSocketChannelConfig(this);
/*  26:    */   }
/*  27:    */   
/*  28:    */   public EpollServerSocketChannel(int fd)
/*  29:    */   {
/*  30: 50 */     this(new LinuxSocket(fd));
/*  31:    */   }
/*  32:    */   
/*  33:    */   EpollServerSocketChannel(LinuxSocket fd)
/*  34:    */   {
/*  35: 54 */     super(fd);
/*  36: 55 */     this.config = new EpollServerSocketChannelConfig(this);
/*  37:    */   }
/*  38:    */   
/*  39:    */   EpollServerSocketChannel(LinuxSocket fd, boolean active)
/*  40:    */   {
/*  41: 59 */     super(fd, active);
/*  42: 60 */     this.config = new EpollServerSocketChannelConfig(this);
/*  43:    */   }
/*  44:    */   
/*  45:    */   protected boolean isCompatible(EventLoop loop)
/*  46:    */   {
/*  47: 65 */     return loop instanceof EpollEventLoop;
/*  48:    */   }
/*  49:    */   
/*  50:    */   protected void doBind(SocketAddress localAddress)
/*  51:    */     throws Exception
/*  52:    */   {
/*  53: 70 */     super.doBind(localAddress);
/*  54: 71 */     if ((Native.IS_SUPPORTING_TCP_FASTOPEN) && (this.config.getTcpFastopen() > 0)) {
/*  55: 72 */       this.socket.setTcpFastOpen(this.config.getTcpFastopen());
/*  56:    */     }
/*  57: 74 */     this.socket.listen(this.config.getBacklog());
/*  58: 75 */     this.active = true;
/*  59:    */   }
/*  60:    */   
/*  61:    */   public InetSocketAddress remoteAddress()
/*  62:    */   {
/*  63: 80 */     return (InetSocketAddress)super.remoteAddress();
/*  64:    */   }
/*  65:    */   
/*  66:    */   public InetSocketAddress localAddress()
/*  67:    */   {
/*  68: 85 */     return (InetSocketAddress)super.localAddress();
/*  69:    */   }
/*  70:    */   
/*  71:    */   public EpollServerSocketChannelConfig config()
/*  72:    */   {
/*  73: 90 */     return this.config;
/*  74:    */   }
/*  75:    */   
/*  76:    */   protected Channel newChildChannel(int fd, byte[] address, int offset, int len)
/*  77:    */     throws Exception
/*  78:    */   {
/*  79: 95 */     return new EpollSocketChannel(this, new LinuxSocket(fd), NativeInetAddress.address(address, offset, len));
/*  80:    */   }
/*  81:    */   
/*  82:    */   Collection<InetAddress> tcpMd5SigAddresses()
/*  83:    */   {
/*  84: 99 */     return this.tcpMd5SigAddresses;
/*  85:    */   }
/*  86:    */   
/*  87:    */   void setTcpMd5Sig(Map<InetAddress, byte[]> keys)
/*  88:    */     throws IOException
/*  89:    */   {
/*  90:103 */     this.tcpMd5SigAddresses = TcpMd5Util.newTcpMd5Sigs(this, this.tcpMd5SigAddresses, keys);
/*  91:    */   }
/*  92:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.epoll.EpollServerSocketChannel
 * JD-Core Version:    0.7.0.1
 */