/*   1:    */ package io.netty.channel.kqueue;
/*   2:    */ 
/*   3:    */ import io.netty.channel.Channel;
/*   4:    */ import io.netty.channel.ChannelConfig;
/*   5:    */ import io.netty.channel.ChannelOutboundBuffer;
/*   6:    */ import io.netty.channel.ChannelPipeline;
/*   7:    */ import io.netty.channel.unix.DomainSocketAddress;
/*   8:    */ import io.netty.channel.unix.DomainSocketChannel;
/*   9:    */ import io.netty.channel.unix.FileDescriptor;
/*  10:    */ import io.netty.channel.unix.PeerCredentials;
/*  11:    */ import java.io.IOException;
/*  12:    */ import java.net.SocketAddress;
/*  13:    */ 
/*  14:    */ public final class KQueueDomainSocketChannel
/*  15:    */   extends AbstractKQueueStreamChannel
/*  16:    */   implements DomainSocketChannel
/*  17:    */ {
/*  18: 35 */   private final KQueueDomainSocketChannelConfig config = new KQueueDomainSocketChannelConfig(this);
/*  19:    */   private volatile DomainSocketAddress local;
/*  20:    */   private volatile DomainSocketAddress remote;
/*  21:    */   
/*  22:    */   public KQueueDomainSocketChannel()
/*  23:    */   {
/*  24: 41 */     super(null, BsdSocket.newSocketDomain(), false);
/*  25:    */   }
/*  26:    */   
/*  27:    */   public KQueueDomainSocketChannel(int fd)
/*  28:    */   {
/*  29: 45 */     this(null, new BsdSocket(fd));
/*  30:    */   }
/*  31:    */   
/*  32:    */   KQueueDomainSocketChannel(Channel parent, BsdSocket fd)
/*  33:    */   {
/*  34: 49 */     super(parent, fd, true);
/*  35:    */   }
/*  36:    */   
/*  37:    */   protected AbstractKQueueChannel.AbstractKQueueUnsafe newUnsafe()
/*  38:    */   {
/*  39: 54 */     return new KQueueDomainUnsafe(null);
/*  40:    */   }
/*  41:    */   
/*  42:    */   protected DomainSocketAddress localAddress0()
/*  43:    */   {
/*  44: 59 */     return this.local;
/*  45:    */   }
/*  46:    */   
/*  47:    */   protected DomainSocketAddress remoteAddress0()
/*  48:    */   {
/*  49: 64 */     return this.remote;
/*  50:    */   }
/*  51:    */   
/*  52:    */   protected void doBind(SocketAddress localAddress)
/*  53:    */     throws Exception
/*  54:    */   {
/*  55: 69 */     this.socket.bind(localAddress);
/*  56: 70 */     this.local = ((DomainSocketAddress)localAddress);
/*  57:    */   }
/*  58:    */   
/*  59:    */   public KQueueDomainSocketChannelConfig config()
/*  60:    */   {
/*  61: 75 */     return this.config;
/*  62:    */   }
/*  63:    */   
/*  64:    */   protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress)
/*  65:    */     throws Exception
/*  66:    */   {
/*  67: 80 */     if (super.doConnect(remoteAddress, localAddress))
/*  68:    */     {
/*  69: 81 */       this.local = ((DomainSocketAddress)localAddress);
/*  70: 82 */       this.remote = ((DomainSocketAddress)remoteAddress);
/*  71: 83 */       return true;
/*  72:    */     }
/*  73: 85 */     return false;
/*  74:    */   }
/*  75:    */   
/*  76:    */   public DomainSocketAddress remoteAddress()
/*  77:    */   {
/*  78: 90 */     return (DomainSocketAddress)super.remoteAddress();
/*  79:    */   }
/*  80:    */   
/*  81:    */   public DomainSocketAddress localAddress()
/*  82:    */   {
/*  83: 95 */     return (DomainSocketAddress)super.localAddress();
/*  84:    */   }
/*  85:    */   
/*  86:    */   protected int doWriteSingle(ChannelOutboundBuffer in)
/*  87:    */     throws Exception
/*  88:    */   {
/*  89:100 */     Object msg = in.current();
/*  90:101 */     if (((msg instanceof FileDescriptor)) && (this.socket.sendFd(((FileDescriptor)msg).intValue()) > 0))
/*  91:    */     {
/*  92:103 */       in.remove();
/*  93:104 */       return 1;
/*  94:    */     }
/*  95:106 */     return super.doWriteSingle(in);
/*  96:    */   }
/*  97:    */   
/*  98:    */   protected Object filterOutboundMessage(Object msg)
/*  99:    */   {
/* 100:111 */     if ((msg instanceof FileDescriptor)) {
/* 101:112 */       return msg;
/* 102:    */     }
/* 103:114 */     return super.filterOutboundMessage(msg);
/* 104:    */   }
/* 105:    */   
/* 106:    */   public PeerCredentials peerCredentials()
/* 107:    */     throws IOException
/* 108:    */   {
/* 109:123 */     return this.socket.getPeerCredentials();
/* 110:    */   }
/* 111:    */   
/* 112:    */   private final class KQueueDomainUnsafe
/* 113:    */     extends AbstractKQueueStreamChannel.KQueueStreamUnsafe
/* 114:    */   {
/* 115:    */     private KQueueDomainUnsafe()
/* 116:    */     {
/* 117:126 */       super();
/* 118:    */     }
/* 119:    */     
/* 120:    */     void readReady(KQueueRecvByteAllocatorHandle allocHandle)
/* 121:    */     {
/* 122:129 */       switch (KQueueDomainSocketChannel.1.$SwitchMap$io$netty$channel$unix$DomainSocketReadMode[KQueueDomainSocketChannel.this.config().getReadMode().ordinal()])
/* 123:    */       {
/* 124:    */       case 1: 
/* 125:131 */         super.readReady(allocHandle);
/* 126:132 */         break;
/* 127:    */       case 2: 
/* 128:134 */         readReadyFd();
/* 129:135 */         break;
/* 130:    */       default: 
/* 131:137 */         throw new Error();
/* 132:    */       }
/* 133:    */     }
/* 134:    */     
/* 135:    */     private void readReadyFd()
/* 136:    */     {
/* 137:142 */       if (KQueueDomainSocketChannel.this.socket.isInputShutdown())
/* 138:    */       {
/* 139:143 */         super.clearReadFilter0();
/* 140:144 */         return;
/* 141:    */       }
/* 142:146 */       ChannelConfig config = KQueueDomainSocketChannel.this.config();
/* 143:147 */       KQueueRecvByteAllocatorHandle allocHandle = recvBufAllocHandle();
/* 144:    */       
/* 145:149 */       ChannelPipeline pipeline = KQueueDomainSocketChannel.this.pipeline();
/* 146:150 */       allocHandle.reset(config);
/* 147:151 */       readReadyBefore();
/* 148:    */       try
/* 149:    */       {
/* 150:    */         do
/* 151:    */         {
/* 152:158 */           int recvFd = KQueueDomainSocketChannel.this.socket.recvFd();
/* 153:159 */           switch (recvFd)
/* 154:    */           {
/* 155:    */           case 0: 
/* 156:161 */             allocHandle.lastBytesRead(0);
/* 157:162 */             break;
/* 158:    */           case -1: 
/* 159:164 */             allocHandle.lastBytesRead(-1);
/* 160:165 */             close(voidPromise());
/* 161:166 */             return;
/* 162:    */           default: 
/* 163:168 */             allocHandle.lastBytesRead(1);
/* 164:169 */             allocHandle.incMessagesRead(1);
/* 165:170 */             this.readPending = false;
/* 166:171 */             pipeline.fireChannelRead(new FileDescriptor(recvFd));
/* 167:    */           }
/* 168:174 */         } while (allocHandle.continueReading());
/* 169:176 */         allocHandle.readComplete();
/* 170:177 */         pipeline.fireChannelReadComplete();
/* 171:    */       }
/* 172:    */       catch (Throwable t)
/* 173:    */       {
/* 174:179 */         allocHandle.readComplete();
/* 175:180 */         pipeline.fireChannelReadComplete();
/* 176:181 */         pipeline.fireExceptionCaught(t);
/* 177:    */       }
/* 178:    */       finally
/* 179:    */       {
/* 180:183 */         readReadyFinally(config);
/* 181:    */       }
/* 182:    */     }
/* 183:    */   }
/* 184:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.kqueue.KQueueDomainSocketChannel
 * JD-Core Version:    0.7.0.1
 */