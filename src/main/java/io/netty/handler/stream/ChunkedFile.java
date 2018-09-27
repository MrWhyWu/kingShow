/*   1:    */ package io.netty.handler.stream;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.channel.ChannelHandlerContext;
/*   6:    */ import java.io.File;
/*   7:    */ import java.io.IOException;
/*   8:    */ import java.io.RandomAccessFile;
/*   9:    */ import java.nio.channels.FileChannel;
/*  10:    */ 
/*  11:    */ public class ChunkedFile
/*  12:    */   implements ChunkedInput<ByteBuf>
/*  13:    */ {
/*  14:    */   private final RandomAccessFile file;
/*  15:    */   private final long startOffset;
/*  16:    */   private final long endOffset;
/*  17:    */   private final int chunkSize;
/*  18:    */   private long offset;
/*  19:    */   
/*  20:    */   public ChunkedFile(File file)
/*  21:    */     throws IOException
/*  22:    */   {
/*  23: 46 */     this(file, 8192);
/*  24:    */   }
/*  25:    */   
/*  26:    */   public ChunkedFile(File file, int chunkSize)
/*  27:    */     throws IOException
/*  28:    */   {
/*  29: 56 */     this(new RandomAccessFile(file, "r"), chunkSize);
/*  30:    */   }
/*  31:    */   
/*  32:    */   public ChunkedFile(RandomAccessFile file)
/*  33:    */     throws IOException
/*  34:    */   {
/*  35: 63 */     this(file, 8192);
/*  36:    */   }
/*  37:    */   
/*  38:    */   public ChunkedFile(RandomAccessFile file, int chunkSize)
/*  39:    */     throws IOException
/*  40:    */   {
/*  41: 73 */     this(file, 0L, file.length(), chunkSize);
/*  42:    */   }
/*  43:    */   
/*  44:    */   public ChunkedFile(RandomAccessFile file, long offset, long length, int chunkSize)
/*  45:    */     throws IOException
/*  46:    */   {
/*  47: 85 */     if (file == null) {
/*  48: 86 */       throw new NullPointerException("file");
/*  49:    */     }
/*  50: 88 */     if (offset < 0L) {
/*  51: 89 */       throw new IllegalArgumentException("offset: " + offset + " (expected: 0 or greater)");
/*  52:    */     }
/*  53: 92 */     if (length < 0L) {
/*  54: 93 */       throw new IllegalArgumentException("length: " + length + " (expected: 0 or greater)");
/*  55:    */     }
/*  56: 96 */     if (chunkSize <= 0) {
/*  57: 97 */       throw new IllegalArgumentException("chunkSize: " + chunkSize + " (expected: a positive integer)");
/*  58:    */     }
/*  59:102 */     this.file = file;
/*  60:103 */     this.offset = (this.startOffset = offset);
/*  61:104 */     this.endOffset = (offset + length);
/*  62:105 */     this.chunkSize = chunkSize;
/*  63:    */     
/*  64:107 */     file.seek(offset);
/*  65:    */   }
/*  66:    */   
/*  67:    */   public long startOffset()
/*  68:    */   {
/*  69:114 */     return this.startOffset;
/*  70:    */   }
/*  71:    */   
/*  72:    */   public long endOffset()
/*  73:    */   {
/*  74:121 */     return this.endOffset;
/*  75:    */   }
/*  76:    */   
/*  77:    */   public long currentOffset()
/*  78:    */   {
/*  79:128 */     return this.offset;
/*  80:    */   }
/*  81:    */   
/*  82:    */   public boolean isEndOfInput()
/*  83:    */     throws Exception
/*  84:    */   {
/*  85:133 */     return (this.offset >= this.endOffset) || (!this.file.getChannel().isOpen());
/*  86:    */   }
/*  87:    */   
/*  88:    */   public void close()
/*  89:    */     throws Exception
/*  90:    */   {
/*  91:138 */     this.file.close();
/*  92:    */   }
/*  93:    */   
/*  94:    */   @Deprecated
/*  95:    */   public ByteBuf readChunk(ChannelHandlerContext ctx)
/*  96:    */     throws Exception
/*  97:    */   {
/*  98:144 */     return readChunk(ctx.alloc());
/*  99:    */   }
/* 100:    */   
/* 101:    */   public ByteBuf readChunk(ByteBufAllocator allocator)
/* 102:    */     throws Exception
/* 103:    */   {
/* 104:149 */     long offset = this.offset;
/* 105:150 */     if (offset >= this.endOffset) {
/* 106:151 */       return null;
/* 107:    */     }
/* 108:154 */     int chunkSize = (int)Math.min(this.chunkSize, this.endOffset - offset);
/* 109:    */     
/* 110:    */ 
/* 111:157 */     ByteBuf buf = allocator.heapBuffer(chunkSize);
/* 112:158 */     boolean release = true;
/* 113:    */     try
/* 114:    */     {
/* 115:160 */       this.file.readFully(buf.array(), buf.arrayOffset(), chunkSize);
/* 116:161 */       buf.writerIndex(chunkSize);
/* 117:162 */       this.offset = (offset + chunkSize);
/* 118:163 */       release = false;
/* 119:164 */       return buf;
/* 120:    */     }
/* 121:    */     finally
/* 122:    */     {
/* 123:166 */       if (release) {
/* 124:167 */         buf.release();
/* 125:    */       }
/* 126:    */     }
/* 127:    */   }
/* 128:    */   
/* 129:    */   public long length()
/* 130:    */   {
/* 131:174 */     return this.endOffset - this.startOffset;
/* 132:    */   }
/* 133:    */   
/* 134:    */   public long progress()
/* 135:    */   {
/* 136:179 */     return this.offset - this.startOffset;
/* 137:    */   }
/* 138:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.stream.ChunkedFile
 * JD-Core Version:    0.7.0.1
 */