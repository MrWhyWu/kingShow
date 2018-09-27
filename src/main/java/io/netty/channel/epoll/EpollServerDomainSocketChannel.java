/*   1:    */ package io.netty.channel.epoll;
/*   2:    */ 
/*   3:    */ import io.netty.channel.Channel;
/*   4:    */ import io.netty.channel.unix.DomainSocketAddress;
/*   5:    */ import io.netty.channel.unix.ServerDomainSocketChannel;
/*   6:    */ import io.netty.channel.unix.Socket;
/*   7:    */ import io.netty.util.internal.logging.InternalLogger;
/*   8:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   9:    */ import java.io.File;
/*  10:    */ import java.net.SocketAddress;
/*  11:    */ 
/*  12:    */ public final class EpollServerDomainSocketChannel
/*  13:    */   extends AbstractEpollServerChannel
/*  14:    */   implements ServerDomainSocketChannel
/*  15:    */ {
/*  16: 32 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(EpollServerDomainSocketChannel.class);
/*  17: 35 */   private final EpollServerChannelConfig config = new EpollServerChannelConfig(this);
/*  18:    */   private volatile DomainSocketAddress local;
/*  19:    */   
/*  20:    */   public EpollServerDomainSocketChannel()
/*  21:    */   {
/*  22: 39 */     super(LinuxSocket.newSocketDomain(), false);
/*  23:    */   }
/*  24:    */   
/*  25:    */   public EpollServerDomainSocketChannel(int fd)
/*  26:    */   {
/*  27: 43 */     super(fd);
/*  28:    */   }
/*  29:    */   
/*  30:    */   EpollServerDomainSocketChannel(LinuxSocket fd)
/*  31:    */   {
/*  32: 47 */     super(fd);
/*  33:    */   }
/*  34:    */   
/*  35:    */   EpollServerDomainSocketChannel(LinuxSocket fd, boolean active)
/*  36:    */   {
/*  37: 51 */     super(fd, active);
/*  38:    */   }
/*  39:    */   
/*  40:    */   protected Channel newChildChannel(int fd, byte[] addr, int offset, int len)
/*  41:    */     throws Exception
/*  42:    */   {
/*  43: 56 */     return new EpollDomainSocketChannel(this, new Socket(fd));
/*  44:    */   }
/*  45:    */   
/*  46:    */   protected DomainSocketAddress localAddress0()
/*  47:    */   {
/*  48: 61 */     return this.local;
/*  49:    */   }
/*  50:    */   
/*  51:    */   protected void doBind(SocketAddress localAddress)
/*  52:    */     throws Exception
/*  53:    */   {
/*  54: 66 */     this.socket.bind(localAddress);
/*  55: 67 */     this.socket.listen(this.config.getBacklog());
/*  56: 68 */     this.local = ((DomainSocketAddress)localAddress);
/*  57: 69 */     this.active = true;
/*  58:    */   }
/*  59:    */   
/*  60:    */   protected void doClose()
/*  61:    */     throws Exception
/*  62:    */   {
/*  63:    */     try
/*  64:    */     {
/*  65: 75 */       super.doClose();
/*  66:    */     }
/*  67:    */     finally
/*  68:    */     {
/*  69:    */       DomainSocketAddress local;
/*  70:    */       File socketFile;
/*  71:    */       boolean success;
/*  72: 77 */       DomainSocketAddress local = this.local;
/*  73: 78 */       if (local != null)
/*  74:    */       {
/*  75: 80 */         File socketFile = new File(local.path());
/*  76: 81 */         boolean success = socketFile.delete();
/*  77: 82 */         if ((!success) && (logger.isDebugEnabled())) {
/*  78: 83 */           logger.debug("Failed to delete a domain socket file: {}", local.path());
/*  79:    */         }
/*  80:    */       }
/*  81:    */     }
/*  82:    */   }
/*  83:    */   
/*  84:    */   public EpollServerChannelConfig config()
/*  85:    */   {
/*  86: 91 */     return this.config;
/*  87:    */   }
/*  88:    */   
/*  89:    */   public DomainSocketAddress remoteAddress()
/*  90:    */   {
/*  91: 96 */     return (DomainSocketAddress)super.remoteAddress();
/*  92:    */   }
/*  93:    */   
/*  94:    */   public DomainSocketAddress localAddress()
/*  95:    */   {
/*  96:101 */     return (DomainSocketAddress)super.localAddress();
/*  97:    */   }
/*  98:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.epoll.EpollServerDomainSocketChannel
 * JD-Core Version:    0.7.0.1
 */