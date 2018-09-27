/*  1:   */ package io.netty.channel.kqueue;
/*  2:   */ 
/*  3:   */ import io.netty.channel.Channel;
/*  4:   */ import io.netty.channel.unix.DomainSocketAddress;
/*  5:   */ import io.netty.channel.unix.ServerDomainSocketChannel;
/*  6:   */ import io.netty.util.internal.logging.InternalLogger;
/*  7:   */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  8:   */ import java.io.File;
/*  9:   */ import java.net.SocketAddress;
/* 10:   */ 
/* 11:   */ public final class KQueueServerDomainSocketChannel
/* 12:   */   extends AbstractKQueueServerChannel
/* 13:   */   implements ServerDomainSocketChannel
/* 14:   */ {
/* 15:33 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(KQueueServerDomainSocketChannel.class);
/* 16:36 */   private final KQueueServerChannelConfig config = new KQueueServerChannelConfig(this);
/* 17:   */   private volatile DomainSocketAddress local;
/* 18:   */   
/* 19:   */   public KQueueServerDomainSocketChannel()
/* 20:   */   {
/* 21:40 */     super(BsdSocket.newSocketDomain(), false);
/* 22:   */   }
/* 23:   */   
/* 24:   */   public KQueueServerDomainSocketChannel(int fd)
/* 25:   */   {
/* 26:44 */     this(new BsdSocket(fd), false);
/* 27:   */   }
/* 28:   */   
/* 29:   */   KQueueServerDomainSocketChannel(BsdSocket socket, boolean active)
/* 30:   */   {
/* 31:48 */     super(socket, active);
/* 32:   */   }
/* 33:   */   
/* 34:   */   protected Channel newChildChannel(int fd, byte[] addr, int offset, int len)
/* 35:   */     throws Exception
/* 36:   */   {
/* 37:53 */     return new KQueueDomainSocketChannel(this, new BsdSocket(fd));
/* 38:   */   }
/* 39:   */   
/* 40:   */   protected DomainSocketAddress localAddress0()
/* 41:   */   {
/* 42:58 */     return this.local;
/* 43:   */   }
/* 44:   */   
/* 45:   */   protected void doBind(SocketAddress localAddress)
/* 46:   */     throws Exception
/* 47:   */   {
/* 48:63 */     this.socket.bind(localAddress);
/* 49:64 */     this.socket.listen(this.config.getBacklog());
/* 50:65 */     this.local = ((DomainSocketAddress)localAddress);
/* 51:66 */     this.active = true;
/* 52:   */   }
/* 53:   */   
/* 54:   */   protected void doClose()
/* 55:   */     throws Exception
/* 56:   */   {
/* 57:   */     try
/* 58:   */     {
/* 59:72 */       super.doClose();
/* 60:   */     }
/* 61:   */     finally
/* 62:   */     {
/* 63:   */       DomainSocketAddress local;
/* 64:   */       File socketFile;
/* 65:   */       boolean success;
/* 66:74 */       DomainSocketAddress local = this.local;
/* 67:75 */       if (local != null)
/* 68:   */       {
/* 69:77 */         File socketFile = new File(local.path());
/* 70:78 */         boolean success = socketFile.delete();
/* 71:79 */         if ((!success) && (logger.isDebugEnabled())) {
/* 72:80 */           logger.debug("Failed to delete a domain socket file: {}", local.path());
/* 73:   */         }
/* 74:   */       }
/* 75:   */     }
/* 76:   */   }
/* 77:   */   
/* 78:   */   public KQueueServerChannelConfig config()
/* 79:   */   {
/* 80:88 */     return this.config;
/* 81:   */   }
/* 82:   */   
/* 83:   */   public DomainSocketAddress remoteAddress()
/* 84:   */   {
/* 85:93 */     return (DomainSocketAddress)super.remoteAddress();
/* 86:   */   }
/* 87:   */   
/* 88:   */   public DomainSocketAddress localAddress()
/* 89:   */   {
/* 90:98 */     return (DomainSocketAddress)super.localAddress();
/* 91:   */   }
/* 92:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.kqueue.KQueueServerDomainSocketChannel
 * JD-Core Version:    0.7.0.1
 */