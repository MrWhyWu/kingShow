/*  1:   */ package io.netty.channel;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.ObjectUtil;
/*  4:   */ 
/*  5:   */ abstract class PendingBytesTracker
/*  6:   */   implements MessageSizeEstimator.Handle
/*  7:   */ {
/*  8:   */   private final MessageSizeEstimator.Handle estimatorHandle;
/*  9:   */   
/* 10:   */   private PendingBytesTracker(MessageSizeEstimator.Handle estimatorHandle)
/* 11:   */   {
/* 12:24 */     this.estimatorHandle = ((MessageSizeEstimator.Handle)ObjectUtil.checkNotNull(estimatorHandle, "estimatorHandle"));
/* 13:   */   }
/* 14:   */   
/* 15:   */   public final int size(Object msg)
/* 16:   */   {
/* 17:29 */     return this.estimatorHandle.size(msg);
/* 18:   */   }
/* 19:   */   
/* 20:   */   public abstract void incrementPendingOutboundBytes(long paramLong);
/* 21:   */   
/* 22:   */   public abstract void decrementPendingOutboundBytes(long paramLong);
/* 23:   */   
/* 24:   */   static PendingBytesTracker newTracker(Channel channel)
/* 25:   */   {
/* 26:36 */     if ((channel.pipeline() instanceof DefaultChannelPipeline)) {
/* 27:37 */       return new DefaultChannelPipelinePendingBytesTracker((DefaultChannelPipeline)channel.pipeline());
/* 28:   */     }
/* 29:39 */     ChannelOutboundBuffer buffer = channel.unsafe().outboundBuffer();
/* 30:40 */     MessageSizeEstimator.Handle handle = channel.config().getMessageSizeEstimator().newHandle();
/* 31:   */     
/* 32:   */ 
/* 33:   */ 
/* 34:44 */     return buffer == null ? new NoopPendingBytesTracker(handle) : new ChannelOutboundBufferPendingBytesTracker(buffer, handle);
/* 35:   */   }
/* 36:   */   
/* 37:   */   private static final class DefaultChannelPipelinePendingBytesTracker
/* 38:   */     extends PendingBytesTracker
/* 39:   */   {
/* 40:   */     private final DefaultChannelPipeline pipeline;
/* 41:   */     
/* 42:   */     DefaultChannelPipelinePendingBytesTracker(DefaultChannelPipeline pipeline)
/* 43:   */     {
/* 44:53 */       super(null);
/* 45:54 */       this.pipeline = pipeline;
/* 46:   */     }
/* 47:   */     
/* 48:   */     public void incrementPendingOutboundBytes(long bytes)
/* 49:   */     {
/* 50:59 */       this.pipeline.incrementPendingOutboundBytes(bytes);
/* 51:   */     }
/* 52:   */     
/* 53:   */     public void decrementPendingOutboundBytes(long bytes)
/* 54:   */     {
/* 55:64 */       this.pipeline.decrementPendingOutboundBytes(bytes);
/* 56:   */     }
/* 57:   */   }
/* 58:   */   
/* 59:   */   private static final class ChannelOutboundBufferPendingBytesTracker
/* 60:   */     extends PendingBytesTracker
/* 61:   */   {
/* 62:   */     private final ChannelOutboundBuffer buffer;
/* 63:   */     
/* 64:   */     ChannelOutboundBufferPendingBytesTracker(ChannelOutboundBuffer buffer, MessageSizeEstimator.Handle estimatorHandle)
/* 65:   */     {
/* 66:73 */       super(null);
/* 67:74 */       this.buffer = buffer;
/* 68:   */     }
/* 69:   */     
/* 70:   */     public void incrementPendingOutboundBytes(long bytes)
/* 71:   */     {
/* 72:79 */       this.buffer.incrementPendingOutboundBytes(bytes);
/* 73:   */     }
/* 74:   */     
/* 75:   */     public void decrementPendingOutboundBytes(long bytes)
/* 76:   */     {
/* 77:84 */       this.buffer.decrementPendingOutboundBytes(bytes);
/* 78:   */     }
/* 79:   */   }
/* 80:   */   
/* 81:   */   private static final class NoopPendingBytesTracker
/* 82:   */     extends PendingBytesTracker
/* 83:   */   {
/* 84:   */     NoopPendingBytesTracker(MessageSizeEstimator.Handle estimatorHandle)
/* 85:   */     {
/* 86:91 */       super(null);
/* 87:   */     }
/* 88:   */     
/* 89:   */     public void incrementPendingOutboundBytes(long bytes) {}
/* 90:   */     
/* 91:   */     public void decrementPendingOutboundBytes(long bytes) {}
/* 92:   */   }
/* 93:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.PendingBytesTracker
 * JD-Core Version:    0.7.0.1
 */