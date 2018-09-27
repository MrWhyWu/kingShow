/*  1:   */ package io.netty.handler.codec.compression;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import java.nio.ByteBuffer;
/*  5:   */ 
/*  6:   */ final class CompressionUtil
/*  7:   */ {
/*  8:   */   static void checkChecksum(ByteBufChecksum checksum, ByteBuf uncompressed, int currentChecksum)
/*  9:   */   {
/* 10:27 */     checksum.reset();
/* 11:28 */     checksum.update(uncompressed, uncompressed
/* 12:29 */       .readerIndex(), uncompressed.readableBytes());
/* 13:   */     
/* 14:31 */     int checksumResult = (int)checksum.getValue();
/* 15:32 */     if (checksumResult != currentChecksum) {
/* 16:33 */       throw new DecompressionException(String.format("stream corrupted: mismatching checksum: %d (expected: %d)", new Object[] {
/* 17:   */       
/* 18:35 */         Integer.valueOf(checksumResult), Integer.valueOf(currentChecksum) }));
/* 19:   */     }
/* 20:   */   }
/* 21:   */   
/* 22:   */   static ByteBuffer safeNioBuffer(ByteBuf buffer)
/* 23:   */   {
/* 24:40 */     return buffer.nioBufferCount() == 1 ? buffer.internalNioBuffer(buffer.readerIndex(), buffer.readableBytes()) : buffer
/* 25:41 */       .nioBuffer();
/* 26:   */   }
/* 27:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.compression.CompressionUtil
 * JD-Core Version:    0.7.0.1
 */