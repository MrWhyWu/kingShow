/*   1:    */ package io.netty.channel.epoll;
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
/*  14:    */ public final class EpollDomainSocketChannel
/*  15:    */   extends AbstractEpollStreamChannel
/*  16:    */   implements DomainSocketChannel
/*  17:    */ {
/*  18: 34 */   private final EpollDomainSocketChannelConfig config = new EpollDomainSocketChannelConfig(this);
/*  19:    */   private volatile DomainSocketAddress local;
/*  20:    */   private volatile DomainSocketAddress remote;
/*  21:    */   
/*  22:    */   public EpollDomainSocketChannel()
/*  23:    */   {
/*  24: 40 */     super(LinuxSocket.newSocketDomain(), false);
/*  25:    */   }
/*  26:    */   
/*  27:    */   EpollDomainSocketChannel(Channel parent, FileDescriptor fd)
/*  28:    */   {
/*  29: 44 */     super(parent, new LinuxSocket(fd.intValue()));
/*  30:    */   }
/*  31:    */   
/*  32:    */   public EpollDomainSocketChannel(int fd)
/*  33:    */   {
/*  34: 48 */     super(fd);
/*  35:    */   }
/*  36:    */   
/*  37:    */   public EpollDomainSocketChannel(Channel parent, LinuxSocket fd)
/*  38:    */   {
/*  39: 52 */     super(parent, fd);
/*  40:    */   }
/*  41:    */   
/*  42:    */   public EpollDomainSocketChannel(int fd, boolean active)
/*  43:    */   {
/*  44: 56 */     super(new LinuxSocket(fd), active);
/*  45:    */   }
/*  46:    */   
/*  47:    */   protected AbstractEpollChannel.AbstractEpollUnsafe newUnsafe()
/*  48:    */   {
/*  49: 61 */     return new EpollDomainUnsafe(null);
/*  50:    */   }
/*  51:    */   
/*  52:    */   protected DomainSocketAddress localAddress0()
/*  53:    */   {
/*  54: 66 */     return this.local;
/*  55:    */   }
/*  56:    */   
/*  57:    */   protected DomainSocketAddress remoteAddress0()
/*  58:    */   {
/*  59: 71 */     return this.remote;
/*  60:    */   }
/*  61:    */   
/*  62:    */   protected void doBind(SocketAddress localAddress)
/*  63:    */     throws Exception
/*  64:    */   {
/*  65: 76 */     this.socket.bind(localAddress);
/*  66: 77 */     this.local = ((DomainSocketAddress)localAddress);
/*  67:    */   }
/*  68:    */   
/*  69:    */   public EpollDomainSocketChannelConfig config()
/*  70:    */   {
/*  71: 82 */     return this.config;
/*  72:    */   }
/*  73:    */   
/*  74:    */   protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress)
/*  75:    */     throws Exception
/*  76:    */   {
/*  77: 87 */     if (super.doConnect(remoteAddress, localAddress))
/*  78:    */     {
/*  79: 88 */       this.local = ((DomainSocketAddress)localAddress);
/*  80: 89 */       this.remote = ((DomainSocketAddress)remoteAddress);
/*  81: 90 */       return true;
/*  82:    */     }
/*  83: 92 */     return false;
/*  84:    */   }
/*  85:    */   
/*  86:    */   public DomainSocketAddress remoteAddress()
/*  87:    */   {
/*  88: 97 */     return (DomainSocketAddress)super.remoteAddress();
/*  89:    */   }
/*  90:    */   
/*  91:    */   public DomainSocketAddress localAddress()
/*  92:    */   {
/*  93:102 */     return (DomainSocketAddress)super.localAddress();
/*  94:    */   }
/*  95:    */   
/*  96:    */   protected int doWriteSingle(ChannelOutboundBuffer in)
/*  97:    */     throws Exception
/*  98:    */   {
/*  99:107 */     Object msg = in.current();
/* 100:108 */     if (((msg instanceof FileDescriptor)) && (this.socket.sendFd(((FileDescriptor)msg).intValue()) > 0))
/* 101:    */     {
/* 102:110 */       in.remove();
/* 103:111 */       return 1;
/* 104:    */     }
/* 105:113 */     return super.doWriteSingle(in);
/* 106:    */   }
/* 107:    */   
/* 108:    */   protected Object filterOutboundMessage(Object msg)
/* 109:    */   {
/* 110:118 */     if ((msg instanceof FileDescriptor)) {
/* 111:119 */       return msg;
/* 112:    */     }
/* 113:121 */     return super.filterOutboundMessage(msg);
/* 114:    */   }
/* 115:    */   
/* 116:    */   public PeerCredentials peerCredentials()
/* 117:    */     throws IOException
/* 118:    */   {
/* 119:130 */     return this.socket.getPeerCredentials();
/* 120:    */   }
/* 121:    */   
/* 122:    */   private final class EpollDomainUnsafe
/* 123:    */     extends AbstractEpollStreamChannel.EpollStreamUnsafe
/* 124:    */   {
/* 125:    */     private EpollDomainUnsafe()
/* 126:    */     {
/* 127:133 */       super();
/* 128:    */     }
/* 129:    */     
/* 130:    */     void epollInReady()
/* 131:    */     {
/* 132:136 */       switch (EpollDomainSocketChannel.1.$SwitchMap$io$netty$channel$unix$DomainSocketReadMode[EpollDomainSocketChannel.this.config().getReadMode().ordinal()])
/* 133:    */       {
/* 134:    */       case 1: 
/* 135:138 */         super.epollInReady();
/* 136:139 */         break;
/* 137:    */       case 2: 
/* 138:141 */         epollInReadFd();
/* 139:142 */         break;
/* 140:    */       default: 
/* 141:144 */         throw new Error();
/* 142:    */       }
/* 143:    */     }
/* 144:    */     
/* 145:    */     private void epollInReadFd()
/* 146:    */     {
/* 147:149 */       if (EpollDomainSocketChannel.this.socket.isInputShutdown())
/* 148:    */       {
/* 149:150 */         clearEpollIn0();
/* 150:151 */         return;
/* 151:    */       }
/* 152:153 */       ChannelConfig config = EpollDomainSocketChannel.this.config();
/* 153:154 */       EpollRecvByteAllocatorHandle allocHandle = recvBufAllocHandle();
/* 154:155 */       allocHandle.edgeTriggered(EpollDomainSocketChannel.this.isFlagSet(Native.EPOLLET));
/* 155:    */       
/* 156:157 */       ChannelPipeline pipeline = EpollDomainSocketChannel.this.pipeline();
/* 157:158 */       allocHandle.reset(config);
/* 158:159 */       epollInBefore();
/* 159:    */       try
/* 160:    */       {
/* 161:    */         do
/* 162:    */         {
/* 163:166 */           allocHandle.lastBytesRead(EpollDomainSocketChannel.this.socket.recvFd());
/* 164:167 */           switch (allocHandle.lastBytesRead())
/* 165:    */           {
/* 166:    */           case 0: 
/* 167:    */             break;
/* 168:    */           case -1: 
/* 169:171 */             close(voidPromise());
/* 170:172 */             return;
/* 171:    */           default: 
/* 172:174 */             allocHandle.incMessagesRead(1);
/* 173:175 */             this.readPending = false;
/* 174:176 */             pipeline.fireChannelRead(new FileDescriptor(allocHandle.lastBytesRead()));
/* 175:    */           }
/* 176:179 */         } while (allocHandle.continueReading());
/* 177:181 */         allocHandle.readComplete();
/* 178:182 */         pipeline.fireChannelReadComplete();
/* 179:    */       }
/* 180:    */       catch (Throwable t)
/* 181:    */       {
/* 182:184 */         allocHandle.readComplete();
/* 183:185 */         pipeline.fireChannelReadComplete();
/* 184:186 */         pipeline.fireExceptionCaught(t);
/* 185:    */       }
/* 186:    */       finally
/* 187:    */       {
/* 188:188 */         epollInFinally(config);
/* 189:    */       }
/* 190:    */     }
/* 191:    */   }
/* 192:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.epoll.EpollDomainSocketChannel
 * JD-Core Version:    0.7.0.1
 */