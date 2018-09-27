/*   1:    */ package io.netty.handler.stream;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.channel.ChannelHandlerContext;
/*   6:    */ import java.io.InputStream;
/*   7:    */ import java.io.PushbackInputStream;
/*   8:    */ 
/*   9:    */ public class ChunkedStream
/*  10:    */   implements ChunkedInput<ByteBuf>
/*  11:    */ {
/*  12:    */   static final int DEFAULT_CHUNK_SIZE = 8192;
/*  13:    */   private final PushbackInputStream in;
/*  14:    */   private final int chunkSize;
/*  15:    */   private long offset;
/*  16:    */   private boolean closed;
/*  17:    */   
/*  18:    */   public ChunkedStream(InputStream in)
/*  19:    */   {
/*  20: 48 */     this(in, 8192);
/*  21:    */   }
/*  22:    */   
/*  23:    */   public ChunkedStream(InputStream in, int chunkSize)
/*  24:    */   {
/*  25: 58 */     if (in == null) {
/*  26: 59 */       throw new NullPointerException("in");
/*  27:    */     }
/*  28: 61 */     if (chunkSize <= 0) {
/*  29: 62 */       throw new IllegalArgumentException("chunkSize: " + chunkSize + " (expected: a positive integer)");
/*  30:    */     }
/*  31: 67 */     if ((in instanceof PushbackInputStream)) {
/*  32: 68 */       this.in = ((PushbackInputStream)in);
/*  33:    */     } else {
/*  34: 70 */       this.in = new PushbackInputStream(in);
/*  35:    */     }
/*  36: 72 */     this.chunkSize = chunkSize;
/*  37:    */   }
/*  38:    */   
/*  39:    */   public long transferredBytes()
/*  40:    */   {
/*  41: 79 */     return this.offset;
/*  42:    */   }
/*  43:    */   
/*  44:    */   public boolean isEndOfInput()
/*  45:    */     throws Exception
/*  46:    */   {
/*  47: 84 */     if (this.closed) {
/*  48: 85 */       return true;
/*  49:    */     }
/*  50: 88 */     int b = this.in.read();
/*  51: 89 */     if (b < 0) {
/*  52: 90 */       return true;
/*  53:    */     }
/*  54: 92 */     this.in.unread(b);
/*  55: 93 */     return false;
/*  56:    */   }
/*  57:    */   
/*  58:    */   public void close()
/*  59:    */     throws Exception
/*  60:    */   {
/*  61: 99 */     this.closed = true;
/*  62:100 */     this.in.close();
/*  63:    */   }
/*  64:    */   
/*  65:    */   @Deprecated
/*  66:    */   public ByteBuf readChunk(ChannelHandlerContext ctx)
/*  67:    */     throws Exception
/*  68:    */   {
/*  69:106 */     return readChunk(ctx.alloc());
/*  70:    */   }
/*  71:    */   
/*  72:    */   public ByteBuf readChunk(ByteBufAllocator allocator)
/*  73:    */     throws Exception
/*  74:    */   {
/*  75:111 */     if (isEndOfInput()) {
/*  76:112 */       return null;
/*  77:    */     }
/*  78:115 */     int availableBytes = this.in.available();
/*  79:    */     int chunkSize;
/*  80:    */     int chunkSize;
/*  81:117 */     if (availableBytes <= 0) {
/*  82:118 */       chunkSize = this.chunkSize;
/*  83:    */     } else {
/*  84:120 */       chunkSize = Math.min(this.chunkSize, this.in.available());
/*  85:    */     }
/*  86:123 */     boolean release = true;
/*  87:124 */     ByteBuf buffer = allocator.buffer(chunkSize);
/*  88:    */     try
/*  89:    */     {
/*  90:127 */       this.offset += buffer.writeBytes(this.in, chunkSize);
/*  91:128 */       release = false;
/*  92:129 */       return buffer;
/*  93:    */     }
/*  94:    */     finally
/*  95:    */     {
/*  96:131 */       if (release) {
/*  97:132 */         buffer.release();
/*  98:    */       }
/*  99:    */     }
/* 100:    */   }
/* 101:    */   
/* 102:    */   public long length()
/* 103:    */   {
/* 104:139 */     return -1L;
/* 105:    */   }
/* 106:    */   
/* 107:    */   public long progress()
/* 108:    */   {
/* 109:144 */     return this.offset;
/* 110:    */   }
/* 111:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.stream.ChunkedStream
 * JD-Core Version:    0.7.0.1
 */