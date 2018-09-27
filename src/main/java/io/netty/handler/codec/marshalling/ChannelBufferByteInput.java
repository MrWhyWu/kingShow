/*  1:   */ package io.netty.handler.codec.marshalling;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import java.io.IOException;
/*  5:   */ import org.jboss.marshalling.ByteInput;
/*  6:   */ 
/*  7:   */ class ChannelBufferByteInput
/*  8:   */   implements ByteInput
/*  9:   */ {
/* 10:   */   private final ByteBuf buffer;
/* 11:   */   
/* 12:   */   ChannelBufferByteInput(ByteBuf buffer)
/* 13:   */   {
/* 14:31 */     this.buffer = buffer;
/* 15:   */   }
/* 16:   */   
/* 17:   */   public void close()
/* 18:   */     throws IOException
/* 19:   */   {}
/* 20:   */   
/* 21:   */   public int available()
/* 22:   */     throws IOException
/* 23:   */   {
/* 24:41 */     return this.buffer.readableBytes();
/* 25:   */   }
/* 26:   */   
/* 27:   */   public int read()
/* 28:   */     throws IOException
/* 29:   */   {
/* 30:46 */     if (this.buffer.isReadable()) {
/* 31:47 */       return this.buffer.readByte() & 0xFF;
/* 32:   */     }
/* 33:49 */     return -1;
/* 34:   */   }
/* 35:   */   
/* 36:   */   public int read(byte[] array)
/* 37:   */     throws IOException
/* 38:   */   {
/* 39:54 */     return read(array, 0, array.length);
/* 40:   */   }
/* 41:   */   
/* 42:   */   public int read(byte[] dst, int dstIndex, int length)
/* 43:   */     throws IOException
/* 44:   */   {
/* 45:59 */     int available = available();
/* 46:60 */     if (available == 0) {
/* 47:61 */       return -1;
/* 48:   */     }
/* 49:64 */     length = Math.min(available, length);
/* 50:65 */     this.buffer.readBytes(dst, dstIndex, length);
/* 51:66 */     return length;
/* 52:   */   }
/* 53:   */   
/* 54:   */   public long skip(long bytes)
/* 55:   */     throws IOException
/* 56:   */   {
/* 57:71 */     int readable = this.buffer.readableBytes();
/* 58:72 */     if (readable < bytes) {
/* 59:73 */       bytes = readable;
/* 60:   */     }
/* 61:75 */     this.buffer.readerIndex((int)(this.buffer.readerIndex() + bytes));
/* 62:76 */     return bytes;
/* 63:   */   }
/* 64:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.marshalling.ChannelBufferByteInput
 * JD-Core Version:    0.7.0.1
 */