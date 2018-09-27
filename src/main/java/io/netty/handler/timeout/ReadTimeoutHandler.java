/*   1:    */ package io.netty.handler.timeout;
/*   2:    */ 
/*   3:    */ import io.netty.channel.ChannelHandlerContext;
/*   4:    */ import java.util.concurrent.TimeUnit;
/*   5:    */ 
/*   6:    */ public class ReadTimeoutHandler
/*   7:    */   extends IdleStateHandler
/*   8:    */ {
/*   9:    */   private boolean closed;
/*  10:    */   
/*  11:    */   public ReadTimeoutHandler(int timeoutSeconds)
/*  12:    */   {
/*  13: 72 */     this(timeoutSeconds, TimeUnit.SECONDS);
/*  14:    */   }
/*  15:    */   
/*  16:    */   public ReadTimeoutHandler(long timeout, TimeUnit unit)
/*  17:    */   {
/*  18: 84 */     super(timeout, 0L, 0L, unit);
/*  19:    */   }
/*  20:    */   
/*  21:    */   protected final void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt)
/*  22:    */     throws Exception
/*  23:    */   {
/*  24: 89 */     assert (evt.state() == IdleState.READER_IDLE);
/*  25: 90 */     readTimedOut(ctx);
/*  26:    */   }
/*  27:    */   
/*  28:    */   protected void readTimedOut(ChannelHandlerContext ctx)
/*  29:    */     throws Exception
/*  30:    */   {
/*  31: 97 */     if (!this.closed)
/*  32:    */     {
/*  33: 98 */       ctx.fireExceptionCaught(ReadTimeoutException.INSTANCE);
/*  34: 99 */       ctx.close();
/*  35:100 */       this.closed = true;
/*  36:    */     }
/*  37:    */   }
/*  38:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.timeout.ReadTimeoutHandler
 * JD-Core Version:    0.7.0.1
 */