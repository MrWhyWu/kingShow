/*   1:    */ package io.netty.channel.epoll;
/*   2:    */ 
/*   3:    */ import io.netty.channel.Channel;
/*   4:    */ import io.netty.channel.ChannelConfig;
/*   5:    */ import io.netty.channel.ChannelMetadata;
/*   6:    */ import io.netty.channel.ChannelOutboundBuffer;
/*   7:    */ import io.netty.channel.ChannelPipeline;
/*   8:    */ import io.netty.channel.ChannelPromise;
/*   9:    */ import io.netty.channel.EventLoop;
/*  10:    */ import io.netty.channel.ServerChannel;
/*  11:    */ import java.net.InetSocketAddress;
/*  12:    */ import java.net.SocketAddress;
/*  13:    */ 
/*  14:    */ public abstract class AbstractEpollServerChannel
/*  15:    */   extends AbstractEpollChannel
/*  16:    */   implements ServerChannel
/*  17:    */ {
/*  18: 31 */   private static final ChannelMetadata METADATA = new ChannelMetadata(false, 16);
/*  19:    */   
/*  20:    */   protected AbstractEpollServerChannel(int fd)
/*  21:    */   {
/*  22: 34 */     this(new LinuxSocket(fd), false);
/*  23:    */   }
/*  24:    */   
/*  25:    */   AbstractEpollServerChannel(LinuxSocket fd)
/*  26:    */   {
/*  27: 38 */     this(fd, isSoErrorZero(fd));
/*  28:    */   }
/*  29:    */   
/*  30:    */   AbstractEpollServerChannel(LinuxSocket fd, boolean active)
/*  31:    */   {
/*  32: 42 */     super(null, fd, Native.EPOLLIN, active);
/*  33:    */   }
/*  34:    */   
/*  35:    */   public ChannelMetadata metadata()
/*  36:    */   {
/*  37: 47 */     return METADATA;
/*  38:    */   }
/*  39:    */   
/*  40:    */   protected boolean isCompatible(EventLoop loop)
/*  41:    */   {
/*  42: 52 */     return loop instanceof EpollEventLoop;
/*  43:    */   }
/*  44:    */   
/*  45:    */   protected InetSocketAddress remoteAddress0()
/*  46:    */   {
/*  47: 57 */     return null;
/*  48:    */   }
/*  49:    */   
/*  50:    */   protected AbstractEpollChannel.AbstractEpollUnsafe newUnsafe()
/*  51:    */   {
/*  52: 62 */     return new EpollServerSocketUnsafe();
/*  53:    */   }
/*  54:    */   
/*  55:    */   protected void doWrite(ChannelOutboundBuffer in)
/*  56:    */     throws Exception
/*  57:    */   {
/*  58: 67 */     throw new UnsupportedOperationException();
/*  59:    */   }
/*  60:    */   
/*  61:    */   protected Object filterOutboundMessage(Object msg)
/*  62:    */     throws Exception
/*  63:    */   {
/*  64: 72 */     throw new UnsupportedOperationException();
/*  65:    */   }
/*  66:    */   
/*  67:    */   abstract Channel newChildChannel(int paramInt1, byte[] paramArrayOfByte, int paramInt2, int paramInt3)
/*  68:    */     throws Exception;
/*  69:    */   
/*  70:    */   final class EpollServerSocketUnsafe
/*  71:    */     extends AbstractEpollChannel.AbstractEpollUnsafe
/*  72:    */   {
/*  73:    */     EpollServerSocketUnsafe()
/*  74:    */     {
/*  75: 77 */       super();
/*  76:    */     }
/*  77:    */     
/*  78: 81 */     private final byte[] acceptedAddress = new byte[26];
/*  79:    */     
/*  80:    */     public void connect(SocketAddress socketAddress, SocketAddress socketAddress2, ChannelPromise channelPromise)
/*  81:    */     {
/*  82: 86 */       channelPromise.setFailure(new UnsupportedOperationException());
/*  83:    */     }
/*  84:    */     
/*  85:    */     void epollInReady()
/*  86:    */     {
/*  87: 91 */       assert (AbstractEpollServerChannel.this.eventLoop().inEventLoop());
/*  88: 92 */       ChannelConfig config = AbstractEpollServerChannel.this.config();
/*  89: 93 */       if (AbstractEpollServerChannel.this.shouldBreakEpollInReady(config))
/*  90:    */       {
/*  91: 94 */         clearEpollIn0();
/*  92: 95 */         return;
/*  93:    */       }
/*  94: 97 */       EpollRecvByteAllocatorHandle allocHandle = recvBufAllocHandle();
/*  95: 98 */       allocHandle.edgeTriggered(AbstractEpollServerChannel.this.isFlagSet(Native.EPOLLET));
/*  96:    */       
/*  97:100 */       ChannelPipeline pipeline = AbstractEpollServerChannel.this.pipeline();
/*  98:101 */       allocHandle.reset(config);
/*  99:102 */       allocHandle.attemptedBytesRead(1);
/* 100:103 */       epollInBefore();
/* 101:    */       
/* 102:105 */       Throwable exception = null;
/* 103:    */       try
/* 104:    */       {
/* 105:    */         try
/* 106:    */         {
/* 107:    */           do
/* 108:    */           {
/* 109:112 */             allocHandle.lastBytesRead(AbstractEpollServerChannel.this.socket.accept(this.acceptedAddress));
/* 110:113 */             if (allocHandle.lastBytesRead() == -1) {
/* 111:    */               break;
/* 112:    */             }
/* 113:117 */             allocHandle.incMessagesRead(1);
/* 114:    */             
/* 115:119 */             this.readPending = false;
/* 116:120 */             pipeline.fireChannelRead(AbstractEpollServerChannel.this.newChildChannel(allocHandle.lastBytesRead(), this.acceptedAddress, 1, this.acceptedAddress[0]));
/* 117:122 */           } while (allocHandle.continueReading());
/* 118:    */         }
/* 119:    */         catch (Throwable t)
/* 120:    */         {
/* 121:124 */           exception = t;
/* 122:    */         }
/* 123:126 */         allocHandle.readComplete();
/* 124:127 */         pipeline.fireChannelReadComplete();
/* 125:129 */         if (exception != null) {
/* 126:130 */           pipeline.fireExceptionCaught(exception);
/* 127:    */         }
/* 128:    */       }
/* 129:    */       finally
/* 130:    */       {
/* 131:133 */         epollInFinally(config);
/* 132:    */       }
/* 133:    */     }
/* 134:    */   }
/* 135:    */   
/* 136:    */   protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress)
/* 137:    */     throws Exception
/* 138:    */   {
/* 139:140 */     throw new UnsupportedOperationException();
/* 140:    */   }
/* 141:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.epoll.AbstractEpollServerChannel
 * JD-Core Version:    0.7.0.1
 */