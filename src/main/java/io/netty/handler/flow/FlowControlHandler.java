/*   1:    */ package io.netty.handler.flow;
/*   2:    */ 
/*   3:    */ import io.netty.channel.Channel;
/*   4:    */ import io.netty.channel.ChannelConfig;
/*   5:    */ import io.netty.channel.ChannelDuplexHandler;
/*   6:    */ import io.netty.channel.ChannelHandlerContext;
/*   7:    */ import io.netty.util.Recycler;
/*   8:    */ import io.netty.util.Recycler.Handle;
/*   9:    */ import io.netty.util.ReferenceCountUtil;
/*  10:    */ import io.netty.util.internal.logging.InternalLogger;
/*  11:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  12:    */ import java.util.ArrayDeque;
/*  13:    */ 
/*  14:    */ public class FlowControlHandler
/*  15:    */   extends ChannelDuplexHandler
/*  16:    */ {
/*  17: 68 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(FlowControlHandler.class);
/*  18:    */   private final boolean releaseMessages;
/*  19:    */   private RecyclableArrayDeque queue;
/*  20:    */   private ChannelConfig config;
/*  21:    */   private boolean shouldConsume;
/*  22:    */   
/*  23:    */   public FlowControlHandler()
/*  24:    */   {
/*  25: 79 */     this(true);
/*  26:    */   }
/*  27:    */   
/*  28:    */   public FlowControlHandler(boolean releaseMessages)
/*  29:    */   {
/*  30: 83 */     this.releaseMessages = releaseMessages;
/*  31:    */   }
/*  32:    */   
/*  33:    */   boolean isQueueEmpty()
/*  34:    */   {
/*  35: 91 */     return this.queue.isEmpty();
/*  36:    */   }
/*  37:    */   
/*  38:    */   private void destroy()
/*  39:    */   {
/*  40: 98 */     if (this.queue != null)
/*  41:    */     {
/*  42:100 */       if (!this.queue.isEmpty())
/*  43:    */       {
/*  44:101 */         logger.trace("Non-empty queue: {}", this.queue);
/*  45:103 */         if (this.releaseMessages)
/*  46:    */         {
/*  47:    */           Object msg;
/*  48:105 */           while ((msg = this.queue.poll()) != null) {
/*  49:106 */             ReferenceCountUtil.safeRelease(msg);
/*  50:    */           }
/*  51:    */         }
/*  52:    */       }
/*  53:111 */       this.queue.recycle();
/*  54:112 */       this.queue = null;
/*  55:    */     }
/*  56:    */   }
/*  57:    */   
/*  58:    */   public void handlerAdded(ChannelHandlerContext ctx)
/*  59:    */     throws Exception
/*  60:    */   {
/*  61:118 */     this.config = ctx.channel().config();
/*  62:    */   }
/*  63:    */   
/*  64:    */   public void channelInactive(ChannelHandlerContext ctx)
/*  65:    */     throws Exception
/*  66:    */   {
/*  67:123 */     destroy();
/*  68:124 */     ctx.fireChannelInactive();
/*  69:    */   }
/*  70:    */   
/*  71:    */   public void read(ChannelHandlerContext ctx)
/*  72:    */     throws Exception
/*  73:    */   {
/*  74:129 */     if (dequeue(ctx, 1) == 0)
/*  75:    */     {
/*  76:133 */       this.shouldConsume = true;
/*  77:134 */       ctx.read();
/*  78:    */     }
/*  79:    */   }
/*  80:    */   
/*  81:    */   public void channelRead(ChannelHandlerContext ctx, Object msg)
/*  82:    */     throws Exception
/*  83:    */   {
/*  84:140 */     if (this.queue == null) {
/*  85:141 */       this.queue = RecyclableArrayDeque.newInstance();
/*  86:    */     }
/*  87:144 */     this.queue.offer(msg);
/*  88:    */     
/*  89:    */ 
/*  90:    */ 
/*  91:    */ 
/*  92:149 */     int minConsume = this.shouldConsume ? 1 : 0;
/*  93:150 */     this.shouldConsume = false;
/*  94:    */     
/*  95:152 */     dequeue(ctx, minConsume);
/*  96:    */   }
/*  97:    */   
/*  98:    */   public void channelReadComplete(ChannelHandlerContext ctx)
/*  99:    */     throws Exception
/* 100:    */   {}
/* 101:    */   
/* 102:    */   private int dequeue(ChannelHandlerContext ctx, int minConsume)
/* 103:    */   {
/* 104:175 */     if (this.queue != null)
/* 105:    */     {
/* 106:177 */       int consumed = 0;
/* 107:180 */       while ((consumed < minConsume) || (this.config.isAutoRead()))
/* 108:    */       {
/* 109:181 */         Object msg = this.queue.poll();
/* 110:182 */         if (msg == null) {
/* 111:    */           break;
/* 112:    */         }
/* 113:186 */         consumed++;
/* 114:187 */         ctx.fireChannelRead(msg);
/* 115:    */       }
/* 116:193 */       if ((this.queue.isEmpty()) && (consumed > 0)) {
/* 117:194 */         ctx.fireChannelReadComplete();
/* 118:    */       }
/* 119:197 */       return consumed;
/* 120:    */     }
/* 121:200 */     return 0;
/* 122:    */   }
/* 123:    */   
/* 124:    */   private static final class RecyclableArrayDeque
/* 125:    */     extends ArrayDeque<Object>
/* 126:    */   {
/* 127:    */     private static final long serialVersionUID = 0L;
/* 128:    */     private static final int DEFAULT_NUM_ELEMENTS = 2;
/* 129:215 */     private static final Recycler<RecyclableArrayDeque> RECYCLER = new Recycler()
/* 130:    */     {
/* 131:    */       protected FlowControlHandler.RecyclableArrayDeque newObject(Recycler.Handle<FlowControlHandler.RecyclableArrayDeque> handle)
/* 132:    */       {
/* 133:218 */         return new FlowControlHandler.RecyclableArrayDeque(2, handle, null);
/* 134:    */       }
/* 135:    */     };
/* 136:    */     private final Recycler.Handle<RecyclableArrayDeque> handle;
/* 137:    */     
/* 138:    */     public static RecyclableArrayDeque newInstance()
/* 139:    */     {
/* 140:223 */       return (RecyclableArrayDeque)RECYCLER.get();
/* 141:    */     }
/* 142:    */     
/* 143:    */     private RecyclableArrayDeque(int numElements, Recycler.Handle<RecyclableArrayDeque> handle)
/* 144:    */     {
/* 145:229 */       super();
/* 146:230 */       this.handle = handle;
/* 147:    */     }
/* 148:    */     
/* 149:    */     public void recycle()
/* 150:    */     {
/* 151:234 */       clear();
/* 152:235 */       this.handle.recycle(this);
/* 153:    */     }
/* 154:    */   }
/* 155:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.flow.FlowControlHandler
 * JD-Core Version:    0.7.0.1
 */