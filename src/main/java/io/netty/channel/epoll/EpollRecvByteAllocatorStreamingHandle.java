/*  1:   */ package io.netty.channel.epoll;
/*  2:   */ 
/*  3:   */ import io.netty.channel.RecvByteBufAllocator.ExtendedHandle;
/*  4:   */ 
/*  5:   */ final class EpollRecvByteAllocatorStreamingHandle
/*  6:   */   extends EpollRecvByteAllocatorHandle
/*  7:   */ {
/*  8:   */   public EpollRecvByteAllocatorStreamingHandle(RecvByteBufAllocator.ExtendedHandle handle)
/*  9:   */   {
/* 10:22 */     super(handle);
/* 11:   */   }
/* 12:   */   
/* 13:   */   boolean maybeMoreDataToRead()
/* 14:   */   {
/* 15:33 */     return (lastBytesRead() == attemptedBytesRead()) || (isReceivedRdHup());
/* 16:   */   }
/* 17:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.epoll.EpollRecvByteAllocatorStreamingHandle
 * JD-Core Version:    0.7.0.1
 */