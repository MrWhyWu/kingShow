/*   1:    */ package io.netty.channel.local;
/*   2:    */ 
/*   3:    */ import io.netty.channel.AbstractServerChannel;
/*   4:    */ import io.netty.channel.Channel.Unsafe;
/*   5:    */ import io.netty.channel.ChannelConfig;
/*   6:    */ import io.netty.channel.ChannelPipeline;
/*   7:    */ import io.netty.channel.DefaultChannelConfig;
/*   8:    */ import io.netty.channel.EventLoop;
/*   9:    */ import io.netty.channel.PreferHeapByteBufAllocator;
/*  10:    */ import io.netty.channel.SingleThreadEventLoop;
/*  11:    */ import io.netty.util.concurrent.SingleThreadEventExecutor;
/*  12:    */ import java.net.SocketAddress;
/*  13:    */ import java.util.ArrayDeque;
/*  14:    */ import java.util.Queue;
/*  15:    */ 
/*  16:    */ public class LocalServerChannel
/*  17:    */   extends AbstractServerChannel
/*  18:    */ {
/*  19: 37 */   private final ChannelConfig config = new DefaultChannelConfig(this);
/*  20: 38 */   private final Queue<Object> inboundBuffer = new ArrayDeque();
/*  21: 39 */   private final Runnable shutdownHook = new Runnable()
/*  22:    */   {
/*  23:    */     public void run()
/*  24:    */     {
/*  25: 42 */       LocalServerChannel.this.unsafe().close(LocalServerChannel.this.unsafe().voidPromise());
/*  26:    */     }
/*  27:    */   };
/*  28:    */   private volatile int state;
/*  29:    */   private volatile LocalAddress localAddress;
/*  30:    */   private volatile boolean acceptInProgress;
/*  31:    */   
/*  32:    */   public LocalServerChannel()
/*  33:    */   {
/*  34: 51 */     config().setAllocator(new PreferHeapByteBufAllocator(this.config.getAllocator()));
/*  35:    */   }
/*  36:    */   
/*  37:    */   public ChannelConfig config()
/*  38:    */   {
/*  39: 56 */     return this.config;
/*  40:    */   }
/*  41:    */   
/*  42:    */   public LocalAddress localAddress()
/*  43:    */   {
/*  44: 61 */     return (LocalAddress)super.localAddress();
/*  45:    */   }
/*  46:    */   
/*  47:    */   public LocalAddress remoteAddress()
/*  48:    */   {
/*  49: 66 */     return (LocalAddress)super.remoteAddress();
/*  50:    */   }
/*  51:    */   
/*  52:    */   public boolean isOpen()
/*  53:    */   {
/*  54: 71 */     return this.state < 2;
/*  55:    */   }
/*  56:    */   
/*  57:    */   public boolean isActive()
/*  58:    */   {
/*  59: 76 */     return this.state == 1;
/*  60:    */   }
/*  61:    */   
/*  62:    */   protected boolean isCompatible(EventLoop loop)
/*  63:    */   {
/*  64: 81 */     return loop instanceof SingleThreadEventLoop;
/*  65:    */   }
/*  66:    */   
/*  67:    */   protected SocketAddress localAddress0()
/*  68:    */   {
/*  69: 86 */     return this.localAddress;
/*  70:    */   }
/*  71:    */   
/*  72:    */   protected void doRegister()
/*  73:    */     throws Exception
/*  74:    */   {
/*  75: 91 */     ((SingleThreadEventExecutor)eventLoop()).addShutdownHook(this.shutdownHook);
/*  76:    */   }
/*  77:    */   
/*  78:    */   protected void doBind(SocketAddress localAddress)
/*  79:    */     throws Exception
/*  80:    */   {
/*  81: 96 */     this.localAddress = LocalChannelRegistry.register(this, this.localAddress, localAddress);
/*  82: 97 */     this.state = 1;
/*  83:    */   }
/*  84:    */   
/*  85:    */   protected void doClose()
/*  86:    */     throws Exception
/*  87:    */   {
/*  88:102 */     if (this.state <= 1)
/*  89:    */     {
/*  90:104 */       if (this.localAddress != null)
/*  91:    */       {
/*  92:105 */         LocalChannelRegistry.unregister(this.localAddress);
/*  93:106 */         this.localAddress = null;
/*  94:    */       }
/*  95:108 */       this.state = 2;
/*  96:    */     }
/*  97:    */   }
/*  98:    */   
/*  99:    */   protected void doDeregister()
/* 100:    */     throws Exception
/* 101:    */   {
/* 102:114 */     ((SingleThreadEventExecutor)eventLoop()).removeShutdownHook(this.shutdownHook);
/* 103:    */   }
/* 104:    */   
/* 105:    */   protected void doBeginRead()
/* 106:    */     throws Exception
/* 107:    */   {
/* 108:119 */     if (this.acceptInProgress) {
/* 109:120 */       return;
/* 110:    */     }
/* 111:123 */     Queue<Object> inboundBuffer = this.inboundBuffer;
/* 112:124 */     if (inboundBuffer.isEmpty())
/* 113:    */     {
/* 114:125 */       this.acceptInProgress = true;
/* 115:126 */       return;
/* 116:    */     }
/* 117:129 */     ChannelPipeline pipeline = pipeline();
/* 118:    */     for (;;)
/* 119:    */     {
/* 120:131 */       Object m = inboundBuffer.poll();
/* 121:132 */       if (m == null) {
/* 122:    */         break;
/* 123:    */       }
/* 124:135 */       pipeline.fireChannelRead(m);
/* 125:    */     }
/* 126:137 */     pipeline.fireChannelReadComplete();
/* 127:    */   }
/* 128:    */   
/* 129:    */   LocalChannel serve(LocalChannel peer)
/* 130:    */   {
/* 131:141 */     final LocalChannel child = newLocalChannel(peer);
/* 132:142 */     if (eventLoop().inEventLoop()) {
/* 133:143 */       serve0(child);
/* 134:    */     } else {
/* 135:145 */       eventLoop().execute(new Runnable()
/* 136:    */       {
/* 137:    */         public void run()
/* 138:    */         {
/* 139:148 */           LocalServerChannel.this.serve0(child);
/* 140:    */         }
/* 141:    */       });
/* 142:    */     }
/* 143:152 */     return child;
/* 144:    */   }
/* 145:    */   
/* 146:    */   protected LocalChannel newLocalChannel(LocalChannel peer)
/* 147:    */   {
/* 148:160 */     return new LocalChannel(this, peer);
/* 149:    */   }
/* 150:    */   
/* 151:    */   private void serve0(LocalChannel child)
/* 152:    */   {
/* 153:164 */     this.inboundBuffer.add(child);
/* 154:165 */     if (this.acceptInProgress)
/* 155:    */     {
/* 156:166 */       this.acceptInProgress = false;
/* 157:167 */       ChannelPipeline pipeline = pipeline();
/* 158:    */       for (;;)
/* 159:    */       {
/* 160:169 */         Object m = this.inboundBuffer.poll();
/* 161:170 */         if (m == null) {
/* 162:    */           break;
/* 163:    */         }
/* 164:173 */         pipeline.fireChannelRead(m);
/* 165:    */       }
/* 166:175 */       pipeline.fireChannelReadComplete();
/* 167:    */     }
/* 168:    */   }
/* 169:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.local.LocalServerChannel
 * JD-Core Version:    0.7.0.1
 */