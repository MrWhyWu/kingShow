/*   1:    */ package io.netty.channel.nio;
/*   2:    */ 
/*   3:    */ import io.netty.channel.Channel;
/*   4:    */ import io.netty.channel.ChannelConfig;
/*   5:    */ import io.netty.channel.ChannelOutboundBuffer;
/*   6:    */ import io.netty.channel.ChannelPipeline;
/*   7:    */ import io.netty.channel.RecvByteBufAllocator.Handle;
/*   8:    */ import io.netty.channel.ServerChannel;
/*   9:    */ import java.io.IOException;
/*  10:    */ import java.net.PortUnreachableException;
/*  11:    */ import java.nio.channels.SelectableChannel;
/*  12:    */ import java.nio.channels.SelectionKey;
/*  13:    */ import java.util.ArrayList;
/*  14:    */ import java.util.List;
/*  15:    */ 
/*  16:    */ public abstract class AbstractNioMessageChannel
/*  17:    */   extends AbstractNioChannel
/*  18:    */ {
/*  19:    */   boolean inputShutdown;
/*  20:    */   
/*  21:    */   protected AbstractNioMessageChannel(Channel parent, SelectableChannel ch, int readInterestOp)
/*  22:    */   {
/*  23: 42 */     super(parent, ch, readInterestOp);
/*  24:    */   }
/*  25:    */   
/*  26:    */   protected AbstractNioChannel.AbstractNioUnsafe newUnsafe()
/*  27:    */   {
/*  28: 47 */     return new NioMessageUnsafe(null);
/*  29:    */   }
/*  30:    */   
/*  31:    */   protected void doBeginRead()
/*  32:    */     throws Exception
/*  33:    */   {
/*  34: 52 */     if (this.inputShutdown) {
/*  35: 53 */       return;
/*  36:    */     }
/*  37: 55 */     super.doBeginRead();
/*  38:    */   }
/*  39:    */   
/*  40:    */   private final class NioMessageUnsafe
/*  41:    */     extends AbstractNioChannel.AbstractNioUnsafe
/*  42:    */   {
/*  43:    */     private NioMessageUnsafe()
/*  44:    */     {
/*  45: 58 */       super();
/*  46:    */     }
/*  47:    */     
/*  48: 60 */     private final List<Object> readBuf = new ArrayList();
/*  49:    */     
/*  50:    */     public void read()
/*  51:    */     {
/*  52: 64 */       assert (AbstractNioMessageChannel.this.eventLoop().inEventLoop());
/*  53: 65 */       ChannelConfig config = AbstractNioMessageChannel.this.config();
/*  54: 66 */       ChannelPipeline pipeline = AbstractNioMessageChannel.this.pipeline();
/*  55: 67 */       RecvByteBufAllocator.Handle allocHandle = AbstractNioMessageChannel.this.unsafe().recvBufAllocHandle();
/*  56: 68 */       allocHandle.reset(config);
/*  57:    */       
/*  58: 70 */       boolean closed = false;
/*  59: 71 */       Throwable exception = null;
/*  60:    */       try
/*  61:    */       {
/*  62:    */         try
/*  63:    */         {
/*  64:    */           do
/*  65:    */           {
/*  66: 75 */             int localRead = AbstractNioMessageChannel.this.doReadMessages(this.readBuf);
/*  67: 76 */             if (localRead == 0) {
/*  68:    */               break;
/*  69:    */             }
/*  70: 79 */             if (localRead < 0)
/*  71:    */             {
/*  72: 80 */               closed = true;
/*  73: 81 */               break;
/*  74:    */             }
/*  75: 84 */             allocHandle.incMessagesRead(localRead);
/*  76: 85 */           } while (allocHandle.continueReading());
/*  77:    */         }
/*  78:    */         catch (Throwable t)
/*  79:    */         {
/*  80: 87 */           exception = t;
/*  81:    */         }
/*  82: 90 */         int size = this.readBuf.size();
/*  83: 91 */         for (int i = 0; i < size; i++)
/*  84:    */         {
/*  85: 92 */           AbstractNioMessageChannel.this.readPending = false;
/*  86: 93 */           pipeline.fireChannelRead(this.readBuf.get(i));
/*  87:    */         }
/*  88: 95 */         this.readBuf.clear();
/*  89: 96 */         allocHandle.readComplete();
/*  90: 97 */         pipeline.fireChannelReadComplete();
/*  91: 99 */         if (exception != null)
/*  92:    */         {
/*  93:100 */           closed = AbstractNioMessageChannel.this.closeOnReadError(exception);
/*  94:    */           
/*  95:102 */           pipeline.fireExceptionCaught(exception);
/*  96:    */         }
/*  97:105 */         if (closed)
/*  98:    */         {
/*  99:106 */           AbstractNioMessageChannel.this.inputShutdown = true;
/* 100:107 */           if (AbstractNioMessageChannel.this.isOpen()) {
/* 101:108 */             close(voidPromise());
/* 102:    */           }
/* 103:    */         }
/* 104:    */       }
/* 105:    */       finally
/* 106:    */       {
/* 107:118 */         if ((!AbstractNioMessageChannel.this.readPending) && (!config.isAutoRead())) {
/* 108:119 */           removeReadOp();
/* 109:    */         }
/* 110:    */       }
/* 111:    */     }
/* 112:    */   }
/* 113:    */   
/* 114:    */   protected void doWrite(ChannelOutboundBuffer in)
/* 115:    */     throws Exception
/* 116:    */   {
/* 117:127 */     SelectionKey key = selectionKey();
/* 118:128 */     int interestOps = key.interestOps();
/* 119:    */     for (;;)
/* 120:    */     {
/* 121:131 */       Object msg = in.current();
/* 122:132 */       if (msg == null)
/* 123:    */       {
/* 124:134 */         if ((interestOps & 0x4) == 0) {
/* 125:    */           break;
/* 126:    */         }
/* 127:135 */         key.interestOps(interestOps & 0xFFFFFFFB); break;
/* 128:    */       }
/* 129:    */       try
/* 130:    */       {
/* 131:140 */         boolean done = false;
/* 132:141 */         for (int i = config().getWriteSpinCount() - 1; i >= 0; i--) {
/* 133:142 */           if (doWriteMessage(msg, in))
/* 134:    */           {
/* 135:143 */             done = true;
/* 136:144 */             break;
/* 137:    */           }
/* 138:    */         }
/* 139:148 */         if (done)
/* 140:    */         {
/* 141:149 */           in.remove();
/* 142:    */         }
/* 143:    */         else
/* 144:    */         {
/* 145:152 */           if ((interestOps & 0x4) == 0) {
/* 146:153 */             key.interestOps(interestOps | 0x4);
/* 147:    */           }
/* 148:155 */           break;
/* 149:    */         }
/* 150:    */       }
/* 151:    */       catch (Exception e)
/* 152:    */       {
/* 153:158 */         if (continueOnWriteError()) {
/* 154:159 */           in.remove(e);
/* 155:    */         } else {
/* 156:161 */           throw e;
/* 157:    */         }
/* 158:    */       }
/* 159:    */     }
/* 160:    */   }
/* 161:    */   
/* 162:    */   protected boolean continueOnWriteError()
/* 163:    */   {
/* 164:171 */     return false;
/* 165:    */   }
/* 166:    */   
/* 167:    */   protected boolean closeOnReadError(Throwable cause)
/* 168:    */   {
/* 169:177 */     return ((cause instanceof IOException)) && (!(cause instanceof PortUnreachableException)) && (!(this instanceof ServerChannel));
/* 170:    */   }
/* 171:    */   
/* 172:    */   protected abstract int doReadMessages(List<Object> paramList)
/* 173:    */     throws Exception;
/* 174:    */   
/* 175:    */   protected abstract boolean doWriteMessage(Object paramObject, ChannelOutboundBuffer paramChannelOutboundBuffer)
/* 176:    */     throws Exception;
/* 177:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.nio.AbstractNioMessageChannel
 * JD-Core Version:    0.7.0.1
 */