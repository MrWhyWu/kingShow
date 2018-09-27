/*   1:    */ package io.netty.channel.oio;
/*   2:    */ 
/*   3:    */ import io.netty.channel.AbstractChannel;
/*   4:    */ import io.netty.channel.AbstractChannel.AbstractUnsafe;
/*   5:    */ import io.netty.channel.Channel;
/*   6:    */ import io.netty.channel.ChannelPipeline;
/*   7:    */ import io.netty.channel.ChannelPromise;
/*   8:    */ import io.netty.channel.EventLoop;
/*   9:    */ import io.netty.channel.ThreadPerChannelEventLoop;
/*  10:    */ import java.net.SocketAddress;
/*  11:    */ 
/*  12:    */ public abstract class AbstractOioChannel
/*  13:    */   extends AbstractChannel
/*  14:    */ {
/*  15:    */   protected static final int SO_TIMEOUT = 1000;
/*  16:    */   boolean readPending;
/*  17: 34 */   private final Runnable readTask = new Runnable()
/*  18:    */   {
/*  19:    */     public void run()
/*  20:    */     {
/*  21: 37 */       AbstractOioChannel.this.doRead();
/*  22:    */     }
/*  23:    */   };
/*  24: 40 */   private final Runnable clearReadPendingRunnable = new Runnable()
/*  25:    */   {
/*  26:    */     public void run()
/*  27:    */     {
/*  28: 43 */       AbstractOioChannel.this.readPending = false;
/*  29:    */     }
/*  30:    */   };
/*  31:    */   
/*  32:    */   protected AbstractOioChannel(Channel parent)
/*  33:    */   {
/*  34: 51 */     super(parent);
/*  35:    */   }
/*  36:    */   
/*  37:    */   protected AbstractChannel.AbstractUnsafe newUnsafe()
/*  38:    */   {
/*  39: 56 */     return new DefaultOioUnsafe(null);
/*  40:    */   }
/*  41:    */   
/*  42:    */   private final class DefaultOioUnsafe
/*  43:    */     extends AbstractChannel.AbstractUnsafe
/*  44:    */   {
/*  45:    */     private DefaultOioUnsafe()
/*  46:    */     {
/*  47: 59 */       super();
/*  48:    */     }
/*  49:    */     
/*  50:    */     public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
/*  51:    */     {
/*  52: 64 */       if ((!promise.setUncancellable()) || (!ensureOpen(promise))) {
/*  53: 65 */         return;
/*  54:    */       }
/*  55:    */       try
/*  56:    */       {
/*  57: 69 */         boolean wasActive = AbstractOioChannel.this.isActive();
/*  58: 70 */         AbstractOioChannel.this.doConnect(remoteAddress, localAddress);
/*  59:    */         
/*  60:    */ 
/*  61:    */ 
/*  62: 74 */         boolean active = AbstractOioChannel.this.isActive();
/*  63:    */         
/*  64: 76 */         safeSetSuccess(promise);
/*  65: 77 */         if ((!wasActive) && (active)) {
/*  66: 78 */           AbstractOioChannel.this.pipeline().fireChannelActive();
/*  67:    */         }
/*  68:    */       }
/*  69:    */       catch (Throwable t)
/*  70:    */       {
/*  71: 81 */         safeSetFailure(promise, annotateConnectException(t, remoteAddress));
/*  72: 82 */         closeIfClosed();
/*  73:    */       }
/*  74:    */     }
/*  75:    */   }
/*  76:    */   
/*  77:    */   protected boolean isCompatible(EventLoop loop)
/*  78:    */   {
/*  79: 89 */     return loop instanceof ThreadPerChannelEventLoop;
/*  80:    */   }
/*  81:    */   
/*  82:    */   protected abstract void doConnect(SocketAddress paramSocketAddress1, SocketAddress paramSocketAddress2)
/*  83:    */     throws Exception;
/*  84:    */   
/*  85:    */   protected void doBeginRead()
/*  86:    */     throws Exception
/*  87:    */   {
/*  88:100 */     if (this.readPending) {
/*  89:101 */       return;
/*  90:    */     }
/*  91:104 */     this.readPending = true;
/*  92:105 */     eventLoop().execute(this.readTask);
/*  93:    */   }
/*  94:    */   
/*  95:    */   protected abstract void doRead();
/*  96:    */   
/*  97:    */   @Deprecated
/*  98:    */   protected boolean isReadPending()
/*  99:    */   {
/* 100:116 */     return this.readPending;
/* 101:    */   }
/* 102:    */   
/* 103:    */   @Deprecated
/* 104:    */   protected void setReadPending(final boolean readPending)
/* 105:    */   {
/* 106:125 */     if (isRegistered())
/* 107:    */     {
/* 108:126 */       EventLoop eventLoop = eventLoop();
/* 109:127 */       if (eventLoop.inEventLoop()) {
/* 110:128 */         this.readPending = readPending;
/* 111:    */       } else {
/* 112:130 */         eventLoop.execute(new Runnable()
/* 113:    */         {
/* 114:    */           public void run()
/* 115:    */           {
/* 116:133 */             AbstractOioChannel.this.readPending = readPending;
/* 117:    */           }
/* 118:    */         });
/* 119:    */       }
/* 120:    */     }
/* 121:    */     else
/* 122:    */     {
/* 123:138 */       this.readPending = readPending;
/* 124:    */     }
/* 125:    */   }
/* 126:    */   
/* 127:    */   protected final void clearReadPending()
/* 128:    */   {
/* 129:146 */     if (isRegistered())
/* 130:    */     {
/* 131:147 */       EventLoop eventLoop = eventLoop();
/* 132:148 */       if (eventLoop.inEventLoop()) {
/* 133:149 */         this.readPending = false;
/* 134:    */       } else {
/* 135:151 */         eventLoop.execute(this.clearReadPendingRunnable);
/* 136:    */       }
/* 137:    */     }
/* 138:    */     else
/* 139:    */     {
/* 140:155 */       this.readPending = false;
/* 141:    */     }
/* 142:    */   }
/* 143:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.oio.AbstractOioChannel
 * JD-Core Version:    0.7.0.1
 */