/*   1:    */ package io.netty.handler.stream;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.channel.ChannelHandlerContext;
/*   6:    */ import java.io.File;
/*   7:    */ import java.io.FileInputStream;
/*   8:    */ import java.io.IOException;
/*   9:    */ import java.nio.channels.FileChannel;
/*  10:    */ 
/*  11:    */ public class ChunkedNioFile
/*  12:    */   implements ChunkedInput<ByteBuf>
/*  13:    */ {
/*  14:    */   private final FileChannel in;
/*  15:    */   private final long startOffset;
/*  16:    */   private final long endOffset;
/*  17:    */   private final int chunkSize;
/*  18:    */   private long offset;
/*  19:    */   
/*  20:    */   public ChunkedNioFile(File in)
/*  21:    */     throws IOException
/*  22:    */   {
/*  23: 48 */     this(new FileInputStream(in).getChannel());
/*  24:    */   }
/*  25:    */   
/*  26:    */   public ChunkedNioFile(File in, int chunkSize)
/*  27:    */     throws IOException
/*  28:    */   {
/*  29: 58 */     this(new FileInputStream(in).getChannel(), chunkSize);
/*  30:    */   }
/*  31:    */   
/*  32:    */   public ChunkedNioFile(FileChannel in)
/*  33:    */     throws IOException
/*  34:    */   {
/*  35: 65 */     this(in, 8192);
/*  36:    */   }
/*  37:    */   
/*  38:    */   public ChunkedNioFile(FileChannel in, int chunkSize)
/*  39:    */     throws IOException
/*  40:    */   {
/*  41: 75 */     this(in, 0L, in.size(), chunkSize);
/*  42:    */   }
/*  43:    */   
/*  44:    */   public ChunkedNioFile(FileChannel in, long offset, long length, int chunkSize)
/*  45:    */     throws IOException
/*  46:    */   {
/*  47: 88 */     if (in == null) {
/*  48: 89 */       throw new NullPointerException("in");
/*  49:    */     }
/*  50: 91 */     if (offset < 0L) {
/*  51: 92 */       throw new IllegalArgumentException("offset: " + offset + " (expected: 0 or greater)");
/*  52:    */     }
/*  53: 95 */     if (length < 0L) {
/*  54: 96 */       throw new IllegalArgumentException("length: " + length + " (expected: 0 or greater)");
/*  55:    */     }
/*  56: 99 */     if (chunkSize <= 0) {
/*  57:100 */       throw new IllegalArgumentException("chunkSize: " + chunkSize + " (expected: a positive integer)");
/*  58:    */     }
/*  59:105 */     if (offset != 0L) {
/*  60:106 */       in.position(offset);
/*  61:    */     }
/*  62:108 */     this.in = in;
/*  63:109 */     this.chunkSize = chunkSize;
/*  64:110 */     this.offset = (this.startOffset = offset);
/*  65:111 */     this.endOffset = (offset + length);
/*  66:    */   }
/*  67:    */   
/*  68:    */   public long startOffset()
/*  69:    */   {
/*  70:118 */     return this.startOffset;
/*  71:    */   }
/*  72:    */   
/*  73:    */   public long endOffset()
/*  74:    */   {
/*  75:125 */     return this.endOffset;
/*  76:    */   }
/*  77:    */   
/*  78:    */   public long currentOffset()
/*  79:    */   {
/*  80:132 */     return this.offset;
/*  81:    */   }
/*  82:    */   
/*  83:    */   public boolean isEndOfInput()
/*  84:    */     throws Exception
/*  85:    */   {
/*  86:137 */     return (this.offset >= this.endOffset) || (!this.in.isOpen());
/*  87:    */   }
/*  88:    */   
/*  89:    */   public void close()
/*  90:    */     throws Exception
/*  91:    */   {
/*  92:142 */     this.in.close();
/*  93:    */   }
/*  94:    */   
/*  95:    */   @Deprecated
/*  96:    */   public ByteBuf readChunk(ChannelHandlerContext ctx)
/*  97:    */     throws Exception
/*  98:    */   {
/*  99:148 */     return readChunk(ctx.alloc());
/* 100:    */   }
/* 101:    */   
/* 102:    */   public ByteBuf readChunk(ByteBufAllocator allocator)
/* 103:    */     throws Exception
/* 104:    */   {
/* 105:153 */     long offset = this.offset;
/* 106:154 */     if (offset >= this.endOffset) {
/* 107:155 */       return null;
/* 108:    */     }
/* 109:158 */     int chunkSize = (int)Math.min(this.chunkSize, this.endOffset - offset);
/* 110:159 */     ByteBuf buffer = allocator.buffer(chunkSize);
/* 111:160 */     boolean release = true;
/* 112:    */     try
/* 113:    */     {
/* 114:162 */       int readBytes = 0;
/* 115:    */       int localReadBytes;
/* 116:    */       for (;;)
/* 117:    */       {
/* 118:164 */         localReadBytes = buffer.writeBytes(this.in, chunkSize - readBytes);
/* 119:165 */         if (localReadBytes < 0) {
/* 120:    */           break;
/* 121:    */         }
/* 122:168 */         readBytes += localReadBytes;
/* 123:169 */         if (readBytes == chunkSize) {
/* 124:    */           break;
/* 125:    */         }
/* 126:    */       }
/* 127:173 */       this.offset += readBytes;
/* 128:174 */       release = false;
/* 129:175 */       return buffer;
/* 130:    */     }
/* 131:    */     finally
/* 132:    */     {
/* 133:177 */       if (release) {
/* 134:178 */         buffer.release();
/* 135:    */       }
/* 136:    */     }
/* 137:    */   }
/* 138:    */   
/* 139:    */   public long length()
/* 140:    */   {
/* 141:185 */     return this.endOffset - this.startOffset;
/* 142:    */   }
/* 143:    */   
/* 144:    */   public long progress()
/* 145:    */   {
/* 146:190 */     return this.offset - this.startOffset;
/* 147:    */   }
/* 148:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.stream.ChunkedNioFile
 * JD-Core Version:    0.7.0.1
 */