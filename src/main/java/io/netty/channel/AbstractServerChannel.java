/*  1:   */ package io.netty.channel;
/*  2:   */ 
/*  3:   */ import java.net.SocketAddress;
/*  4:   */ 
/*  5:   */ public abstract class AbstractServerChannel
/*  6:   */   extends AbstractChannel
/*  7:   */   implements ServerChannel
/*  8:   */ {
/*  9:32 */   private static final ChannelMetadata METADATA = new ChannelMetadata(false, 16);
/* 10:   */   
/* 11:   */   protected AbstractServerChannel()
/* 12:   */   {
/* 13:38 */     super(null);
/* 14:   */   }
/* 15:   */   
/* 16:   */   public ChannelMetadata metadata()
/* 17:   */   {
/* 18:43 */     return METADATA;
/* 19:   */   }
/* 20:   */   
/* 21:   */   public SocketAddress remoteAddress()
/* 22:   */   {
/* 23:48 */     return null;
/* 24:   */   }
/* 25:   */   
/* 26:   */   protected SocketAddress remoteAddress0()
/* 27:   */   {
/* 28:53 */     return null;
/* 29:   */   }
/* 30:   */   
/* 31:   */   protected void doDisconnect()
/* 32:   */     throws Exception
/* 33:   */   {
/* 34:58 */     throw new UnsupportedOperationException();
/* 35:   */   }
/* 36:   */   
/* 37:   */   protected AbstractChannel.AbstractUnsafe newUnsafe()
/* 38:   */   {
/* 39:63 */     return new DefaultServerUnsafe(null);
/* 40:   */   }
/* 41:   */   
/* 42:   */   protected void doWrite(ChannelOutboundBuffer in)
/* 43:   */     throws Exception
/* 44:   */   {
/* 45:68 */     throw new UnsupportedOperationException();
/* 46:   */   }
/* 47:   */   
/* 48:   */   protected final Object filterOutboundMessage(Object msg)
/* 49:   */   {
/* 50:73 */     throw new UnsupportedOperationException();
/* 51:   */   }
/* 52:   */   
/* 53:   */   private final class DefaultServerUnsafe
/* 54:   */     extends AbstractChannel.AbstractUnsafe
/* 55:   */   {
/* 56:   */     private DefaultServerUnsafe()
/* 57:   */     {
/* 58:76 */       super();
/* 59:   */     }
/* 60:   */     
/* 61:   */     public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
/* 62:   */     {
/* 63:79 */       safeSetFailure(promise, new UnsupportedOperationException());
/* 64:   */     }
/* 65:   */   }
/* 66:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.AbstractServerChannel
 * JD-Core Version:    0.7.0.1
 */