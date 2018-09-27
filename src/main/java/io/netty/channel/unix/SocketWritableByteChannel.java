/*  1:   */ package io.netty.channel.unix;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.buffer.ByteBufAllocator;
/*  5:   */ import io.netty.buffer.ByteBufUtil;
/*  6:   */ import io.netty.buffer.Unpooled;
/*  7:   */ import io.netty.util.internal.ObjectUtil;
/*  8:   */ import java.io.IOException;
/*  9:   */ import java.nio.ByteBuffer;
/* 10:   */ import java.nio.channels.WritableByteChannel;
/* 11:   */ 
/* 12:   */ public abstract class SocketWritableByteChannel
/* 13:   */   implements WritableByteChannel
/* 14:   */ {
/* 15:   */   private final FileDescriptor fd;
/* 16:   */   
/* 17:   */   protected SocketWritableByteChannel(FileDescriptor fd)
/* 18:   */   {
/* 19:26 */     this.fd = ((FileDescriptor)ObjectUtil.checkNotNull(fd, "fd"));
/* 20:   */   }
/* 21:   */   
/* 22:   */   public final int write(ByteBuffer src)
/* 23:   */     throws IOException
/* 24:   */   {
/* 25:32 */     int position = src.position();
/* 26:33 */     int limit = src.limit();
/* 27:   */     int written;
/* 28:34 */     if (src.isDirect())
/* 29:   */     {
/* 30:35 */       written = this.fd.write(src, position, src.limit());
/* 31:   */     }
/* 32:   */     else
/* 33:   */     {
/* 34:37 */       int readableBytes = limit - position;
/* 35:38 */       ByteBuf buffer = null;
/* 36:   */       try
/* 37:   */       {
/* 38:40 */         if (readableBytes == 0)
/* 39:   */         {
/* 40:41 */           buffer = Unpooled.EMPTY_BUFFER;
/* 41:   */         }
/* 42:   */         else
/* 43:   */         {
/* 44:43 */           ByteBufAllocator alloc = alloc();
/* 45:44 */           if (alloc.isDirectBufferPooled())
/* 46:   */           {
/* 47:45 */             buffer = alloc.directBuffer(readableBytes);
/* 48:   */           }
/* 49:   */           else
/* 50:   */           {
/* 51:47 */             buffer = ByteBufUtil.threadLocalDirectBuffer();
/* 52:48 */             if (buffer == null) {
/* 53:49 */               buffer = Unpooled.directBuffer(readableBytes);
/* 54:   */             }
/* 55:   */           }
/* 56:   */         }
/* 57:53 */         buffer.writeBytes(src.duplicate());
/* 58:54 */         ByteBuffer nioBuffer = buffer.internalNioBuffer(buffer.readerIndex(), readableBytes);
/* 59:55 */         written = this.fd.write(nioBuffer, nioBuffer.position(), nioBuffer.limit());
/* 60:   */       }
/* 61:   */       finally
/* 62:   */       {
/* 63:   */         int written;
/* 64:57 */         if (buffer != null) {
/* 65:58 */           buffer.release();
/* 66:   */         }
/* 67:   */       }
/* 68:   */     }
/* 69:   */     int written;
/* 70:62 */     if (written > 0) {
/* 71:63 */       src.position(position + written);
/* 72:   */     }
/* 73:65 */     return written;
/* 74:   */   }
/* 75:   */   
/* 76:   */   public final boolean isOpen()
/* 77:   */   {
/* 78:70 */     return this.fd.isOpen();
/* 79:   */   }
/* 80:   */   
/* 81:   */   public final void close()
/* 82:   */     throws IOException
/* 83:   */   {
/* 84:75 */     this.fd.close();
/* 85:   */   }
/* 86:   */   
/* 87:   */   protected abstract ByteBufAllocator alloc();
/* 88:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.unix.SocketWritableByteChannel
 * JD-Core Version:    0.7.0.1
 */