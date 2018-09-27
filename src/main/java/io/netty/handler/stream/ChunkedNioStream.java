/*   1:    */ package io.netty.handler.stream;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.channel.ChannelHandlerContext;
/*   6:    */ import java.nio.ByteBuffer;
/*   7:    */ import java.nio.channels.ReadableByteChannel;
/*   8:    */ 
/*   9:    */ public class ChunkedNioStream
/*  10:    */   implements ChunkedInput<ByteBuf>
/*  11:    */ {
/*  12:    */   private final ReadableByteChannel in;
/*  13:    */   private final int chunkSize;
/*  14:    */   private long offset;
/*  15:    */   private final ByteBuffer byteBuffer;
/*  16:    */   
/*  17:    */   public ChunkedNioStream(ReadableByteChannel in)
/*  18:    */   {
/*  19: 46 */     this(in, 8192);
/*  20:    */   }
/*  21:    */   
/*  22:    */   public ChunkedNioStream(ReadableByteChannel in, int chunkSize)
/*  23:    */   {
/*  24: 56 */     if (in == null) {
/*  25: 57 */       throw new NullPointerException("in");
/*  26:    */     }
/*  27: 59 */     if (chunkSize <= 0) {
/*  28: 60 */       throw new IllegalArgumentException("chunkSize: " + chunkSize + " (expected: a positive integer)");
/*  29:    */     }
/*  30: 63 */     this.in = in;
/*  31: 64 */     this.offset = 0L;
/*  32: 65 */     this.chunkSize = chunkSize;
/*  33: 66 */     this.byteBuffer = ByteBuffer.allocate(chunkSize);
/*  34:    */   }
/*  35:    */   
/*  36:    */   public long transferredBytes()
/*  37:    */   {
/*  38: 73 */     return this.offset;
/*  39:    */   }
/*  40:    */   
/*  41:    */   public boolean isEndOfInput()
/*  42:    */     throws Exception
/*  43:    */   {
/*  44: 78 */     if (this.byteBuffer.position() > 0) {
/*  45: 80 */       return false;
/*  46:    */     }
/*  47: 82 */     if (this.in.isOpen())
/*  48:    */     {
/*  49: 84 */       int b = this.in.read(this.byteBuffer);
/*  50: 85 */       if (b < 0) {
/*  51: 86 */         return true;
/*  52:    */       }
/*  53: 88 */       this.offset += b;
/*  54: 89 */       return false;
/*  55:    */     }
/*  56: 92 */     return true;
/*  57:    */   }
/*  58:    */   
/*  59:    */   public void close()
/*  60:    */     throws Exception
/*  61:    */   {
/*  62: 97 */     this.in.close();
/*  63:    */   }
/*  64:    */   
/*  65:    */   @Deprecated
/*  66:    */   public ByteBuf readChunk(ChannelHandlerContext ctx)
/*  67:    */     throws Exception
/*  68:    */   {
/*  69:103 */     return readChunk(ctx.alloc());
/*  70:    */   }
/*  71:    */   
/*  72:    */   public ByteBuf readChunk(ByteBufAllocator allocator)
/*  73:    */     throws Exception
/*  74:    */   {
/*  75:108 */     if (isEndOfInput()) {
/*  76:109 */       return null;
/*  77:    */     }
/*  78:112 */     int readBytes = this.byteBuffer.position();
/*  79:    */     for (;;)
/*  80:    */     {
/*  81:114 */       int localReadBytes = this.in.read(this.byteBuffer);
/*  82:115 */       if (localReadBytes < 0) {
/*  83:    */         break;
/*  84:    */       }
/*  85:118 */       readBytes += localReadBytes;
/*  86:119 */       this.offset += localReadBytes;
/*  87:120 */       if (readBytes == this.chunkSize) {
/*  88:    */         break;
/*  89:    */       }
/*  90:    */     }
/*  91:124 */     this.byteBuffer.flip();
/*  92:125 */     boolean release = true;
/*  93:126 */     ByteBuf buffer = allocator.buffer(this.byteBuffer.remaining());
/*  94:    */     try
/*  95:    */     {
/*  96:128 */       buffer.writeBytes(this.byteBuffer);
/*  97:129 */       this.byteBuffer.clear();
/*  98:130 */       release = false;
/*  99:131 */       return buffer;
/* 100:    */     }
/* 101:    */     finally
/* 102:    */     {
/* 103:133 */       if (release) {
/* 104:134 */         buffer.release();
/* 105:    */       }
/* 106:    */     }
/* 107:    */   }
/* 108:    */   
/* 109:    */   public long length()
/* 110:    */   {
/* 111:141 */     return -1L;
/* 112:    */   }
/* 113:    */   
/* 114:    */   public long progress()
/* 115:    */   {
/* 116:146 */     return this.offset;
/* 117:    */   }
/* 118:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.stream.ChunkedNioStream
 * JD-Core Version:    0.7.0.1
 */