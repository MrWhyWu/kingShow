/*   1:    */ package io.netty.channel.oio;
/*   2:    */ 
/*   3:    */ import io.netty.channel.Channel;
/*   4:    */ import io.netty.channel.Channel.Unsafe;
/*   5:    */ import io.netty.channel.ChannelConfig;
/*   6:    */ import io.netty.channel.ChannelPipeline;
/*   7:    */ import io.netty.channel.RecvByteBufAllocator.Handle;
/*   8:    */ import java.io.IOException;
/*   9:    */ import java.util.ArrayList;
/*  10:    */ import java.util.List;
/*  11:    */ 
/*  12:    */ public abstract class AbstractOioMessageChannel
/*  13:    */   extends AbstractOioChannel
/*  14:    */ {
/*  15: 32 */   private final List<Object> readBuf = new ArrayList();
/*  16:    */   
/*  17:    */   protected AbstractOioMessageChannel(Channel parent)
/*  18:    */   {
/*  19: 35 */     super(parent);
/*  20:    */   }
/*  21:    */   
/*  22:    */   protected void doRead()
/*  23:    */   {
/*  24: 40 */     if (!this.readPending) {
/*  25: 43 */       return;
/*  26:    */     }
/*  27: 47 */     this.readPending = false;
/*  28:    */     
/*  29: 49 */     ChannelConfig config = config();
/*  30: 50 */     ChannelPipeline pipeline = pipeline();
/*  31: 51 */     RecvByteBufAllocator.Handle allocHandle = unsafe().recvBufAllocHandle();
/*  32: 52 */     allocHandle.reset(config);
/*  33:    */     
/*  34: 54 */     boolean closed = false;
/*  35: 55 */     Throwable exception = null;
/*  36:    */     try
/*  37:    */     {
/*  38:    */       do
/*  39:    */       {
/*  40: 59 */         int localRead = doReadMessages(this.readBuf);
/*  41: 60 */         if (localRead == 0) {
/*  42:    */           break;
/*  43:    */         }
/*  44: 63 */         if (localRead < 0)
/*  45:    */         {
/*  46: 64 */           closed = true;
/*  47: 65 */           break;
/*  48:    */         }
/*  49: 68 */         allocHandle.incMessagesRead(localRead);
/*  50: 69 */       } while (allocHandle.continueReading());
/*  51:    */     }
/*  52:    */     catch (Throwable t)
/*  53:    */     {
/*  54: 71 */       exception = t;
/*  55:    */     }
/*  56: 74 */     boolean readData = false;
/*  57: 75 */     int size = this.readBuf.size();
/*  58: 76 */     if (size > 0)
/*  59:    */     {
/*  60: 77 */       readData = true;
/*  61: 78 */       for (int i = 0; i < size; i++)
/*  62:    */       {
/*  63: 79 */         this.readPending = false;
/*  64: 80 */         pipeline.fireChannelRead(this.readBuf.get(i));
/*  65:    */       }
/*  66: 82 */       this.readBuf.clear();
/*  67: 83 */       allocHandle.readComplete();
/*  68: 84 */       pipeline.fireChannelReadComplete();
/*  69:    */     }
/*  70: 87 */     if (exception != null)
/*  71:    */     {
/*  72: 88 */       if ((exception instanceof IOException)) {
/*  73: 89 */         closed = true;
/*  74:    */       }
/*  75: 92 */       pipeline.fireExceptionCaught(exception);
/*  76:    */     }
/*  77: 95 */     if (closed)
/*  78:    */     {
/*  79: 96 */       if (isOpen()) {
/*  80: 97 */         unsafe().close(unsafe().voidPromise());
/*  81:    */       }
/*  82:    */     }
/*  83: 99 */     else if ((this.readPending) || (config.isAutoRead()) || ((!readData) && (isActive()))) {
/*  84:102 */       read();
/*  85:    */     }
/*  86:    */   }
/*  87:    */   
/*  88:    */   protected abstract int doReadMessages(List<Object> paramList)
/*  89:    */     throws Exception;
/*  90:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.oio.AbstractOioMessageChannel
 * JD-Core Version:    0.7.0.1
 */