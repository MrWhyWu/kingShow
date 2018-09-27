/*   1:    */ package io.netty.channel.kqueue;
/*   2:    */ 
/*   3:    */ import io.netty.channel.Channel;
/*   4:    */ import io.netty.channel.ChannelConfig;
/*   5:    */ import io.netty.channel.ChannelMetadata;
/*   6:    */ import io.netty.channel.ChannelOutboundBuffer;
/*   7:    */ import io.netty.channel.ChannelPipeline;
/*   8:    */ import io.netty.channel.EventLoop;
/*   9:    */ import io.netty.channel.ServerChannel;
/*  10:    */ import java.net.InetSocketAddress;
/*  11:    */ import java.net.SocketAddress;
/*  12:    */ 
/*  13:    */ public abstract class AbstractKQueueServerChannel
/*  14:    */   extends AbstractKQueueChannel
/*  15:    */   implements ServerChannel
/*  16:    */ {
/*  17: 32 */   private static final ChannelMetadata METADATA = new ChannelMetadata(false, 16);
/*  18:    */   
/*  19:    */   AbstractKQueueServerChannel(BsdSocket fd)
/*  20:    */   {
/*  21: 35 */     this(fd, isSoErrorZero(fd));
/*  22:    */   }
/*  23:    */   
/*  24:    */   AbstractKQueueServerChannel(BsdSocket fd, boolean active)
/*  25:    */   {
/*  26: 39 */     super(null, fd, active);
/*  27:    */   }
/*  28:    */   
/*  29:    */   public ChannelMetadata metadata()
/*  30:    */   {
/*  31: 44 */     return METADATA;
/*  32:    */   }
/*  33:    */   
/*  34:    */   protected boolean isCompatible(EventLoop loop)
/*  35:    */   {
/*  36: 49 */     return loop instanceof KQueueEventLoop;
/*  37:    */   }
/*  38:    */   
/*  39:    */   protected InetSocketAddress remoteAddress0()
/*  40:    */   {
/*  41: 54 */     return null;
/*  42:    */   }
/*  43:    */   
/*  44:    */   protected AbstractKQueueChannel.AbstractKQueueUnsafe newUnsafe()
/*  45:    */   {
/*  46: 59 */     return new KQueueServerSocketUnsafe();
/*  47:    */   }
/*  48:    */   
/*  49:    */   protected void doWrite(ChannelOutboundBuffer in)
/*  50:    */     throws Exception
/*  51:    */   {
/*  52: 64 */     throw new UnsupportedOperationException();
/*  53:    */   }
/*  54:    */   
/*  55:    */   protected Object filterOutboundMessage(Object msg)
/*  56:    */     throws Exception
/*  57:    */   {
/*  58: 69 */     throw new UnsupportedOperationException();
/*  59:    */   }
/*  60:    */   
/*  61:    */   abstract Channel newChildChannel(int paramInt1, byte[] paramArrayOfByte, int paramInt2, int paramInt3)
/*  62:    */     throws Exception;
/*  63:    */   
/*  64:    */   protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress)
/*  65:    */     throws Exception
/*  66:    */   {
/*  67: 76 */     throw new UnsupportedOperationException();
/*  68:    */   }
/*  69:    */   
/*  70:    */   final class KQueueServerSocketUnsafe
/*  71:    */     extends AbstractKQueueChannel.AbstractKQueueUnsafe
/*  72:    */   {
/*  73:    */     KQueueServerSocketUnsafe()
/*  74:    */     {
/*  75: 79 */       super();
/*  76:    */     }
/*  77:    */     
/*  78: 83 */     private final byte[] acceptedAddress = new byte[26];
/*  79:    */     
/*  80:    */     void readReady(KQueueRecvByteAllocatorHandle allocHandle)
/*  81:    */     {
/*  82: 87 */       assert (AbstractKQueueServerChannel.this.eventLoop().inEventLoop());
/*  83: 88 */       ChannelConfig config = AbstractKQueueServerChannel.this.config();
/*  84: 89 */       if (AbstractKQueueServerChannel.this.shouldBreakReadReady(config))
/*  85:    */       {
/*  86: 90 */         clearReadFilter0();
/*  87: 91 */         return;
/*  88:    */       }
/*  89: 93 */       ChannelPipeline pipeline = AbstractKQueueServerChannel.this.pipeline();
/*  90: 94 */       allocHandle.reset(config);
/*  91: 95 */       allocHandle.attemptedBytesRead(1);
/*  92: 96 */       readReadyBefore();
/*  93:    */       
/*  94: 98 */       Throwable exception = null;
/*  95:    */       try
/*  96:    */       {
/*  97:    */         try
/*  98:    */         {
/*  99:    */           do
/* 100:    */           {
/* 101:102 */             int acceptFd = AbstractKQueueServerChannel.this.socket.accept(this.acceptedAddress);
/* 102:103 */             if (acceptFd == -1)
/* 103:    */             {
/* 104:105 */               allocHandle.lastBytesRead(-1);
/* 105:106 */               break;
/* 106:    */             }
/* 107:108 */             allocHandle.lastBytesRead(1);
/* 108:109 */             allocHandle.incMessagesRead(1);
/* 109:    */             
/* 110:111 */             this.readPending = false;
/* 111:112 */             pipeline.fireChannelRead(AbstractKQueueServerChannel.this.newChildChannel(acceptFd, this.acceptedAddress, 1, this.acceptedAddress[0]));
/* 112:114 */           } while (allocHandle.continueReading());
/* 113:    */         }
/* 114:    */         catch (Throwable t)
/* 115:    */         {
/* 116:116 */           exception = t;
/* 117:    */         }
/* 118:118 */         allocHandle.readComplete();
/* 119:119 */         pipeline.fireChannelReadComplete();
/* 120:121 */         if (exception != null) {
/* 121:122 */           pipeline.fireExceptionCaught(exception);
/* 122:    */         }
/* 123:    */       }
/* 124:    */       finally
/* 125:    */       {
/* 126:125 */         readReadyFinally(config);
/* 127:    */       }
/* 128:    */     }
/* 129:    */   }
/* 130:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.kqueue.AbstractKQueueServerChannel
 * JD-Core Version:    0.7.0.1
 */