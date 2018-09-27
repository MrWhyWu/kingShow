/*   1:    */ package io.netty.handler.codec;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.ChannelHandlerContext;
/*   5:    */ import io.netty.util.ByteProcessor;
/*   6:    */ import java.util.List;
/*   7:    */ 
/*   8:    */ public class LineBasedFrameDecoder
/*   9:    */   extends ByteToMessageDecoder
/*  10:    */ {
/*  11:    */   private final int maxLength;
/*  12:    */   private final boolean failFast;
/*  13:    */   private final boolean stripDelimiter;
/*  14:    */   private boolean discarding;
/*  15:    */   private int discardedBytes;
/*  16:    */   private int offset;
/*  17:    */   
/*  18:    */   public LineBasedFrameDecoder(int maxLength)
/*  19:    */   {
/*  20: 52 */     this(maxLength, true, false);
/*  21:    */   }
/*  22:    */   
/*  23:    */   public LineBasedFrameDecoder(int maxLength, boolean stripDelimiter, boolean failFast)
/*  24:    */   {
/*  25: 71 */     this.maxLength = maxLength;
/*  26: 72 */     this.failFast = failFast;
/*  27: 73 */     this.stripDelimiter = stripDelimiter;
/*  28:    */   }
/*  29:    */   
/*  30:    */   protected final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/*  31:    */     throws Exception
/*  32:    */   {
/*  33: 78 */     Object decoded = decode(ctx, in);
/*  34: 79 */     if (decoded != null) {
/*  35: 80 */       out.add(decoded);
/*  36:    */     }
/*  37:    */   }
/*  38:    */   
/*  39:    */   protected Object decode(ChannelHandlerContext ctx, ByteBuf buffer)
/*  40:    */     throws Exception
/*  41:    */   {
/*  42: 93 */     int eol = findEndOfLine(buffer);
/*  43: 94 */     if (!this.discarding)
/*  44:    */     {
/*  45: 95 */       if (eol >= 0)
/*  46:    */       {
/*  47: 97 */         int length = eol - buffer.readerIndex();
/*  48: 98 */         int delimLength = buffer.getByte(eol) == 13 ? 2 : 1;
/*  49:100 */         if (length > this.maxLength)
/*  50:    */         {
/*  51:101 */           buffer.readerIndex(eol + delimLength);
/*  52:102 */           fail(ctx, length);
/*  53:103 */           return null;
/*  54:    */         }
/*  55:    */         ByteBuf frame;
/*  56:106 */         if (this.stripDelimiter)
/*  57:    */         {
/*  58:107 */           ByteBuf frame = buffer.readRetainedSlice(length);
/*  59:108 */           buffer.skipBytes(delimLength);
/*  60:    */         }
/*  61:    */         else
/*  62:    */         {
/*  63:110 */           frame = buffer.readRetainedSlice(length + delimLength);
/*  64:    */         }
/*  65:113 */         return frame;
/*  66:    */       }
/*  67:115 */       int length = buffer.readableBytes();
/*  68:116 */       if (length > this.maxLength)
/*  69:    */       {
/*  70:117 */         this.discardedBytes = length;
/*  71:118 */         buffer.readerIndex(buffer.writerIndex());
/*  72:119 */         this.discarding = true;
/*  73:120 */         this.offset = 0;
/*  74:121 */         if (this.failFast) {
/*  75:122 */           fail(ctx, "over " + this.discardedBytes);
/*  76:    */         }
/*  77:    */       }
/*  78:125 */       return null;
/*  79:    */     }
/*  80:128 */     if (eol >= 0)
/*  81:    */     {
/*  82:129 */       int length = this.discardedBytes + eol - buffer.readerIndex();
/*  83:130 */       int delimLength = buffer.getByte(eol) == 13 ? 2 : 1;
/*  84:131 */       buffer.readerIndex(eol + delimLength);
/*  85:132 */       this.discardedBytes = 0;
/*  86:133 */       this.discarding = false;
/*  87:134 */       if (!this.failFast) {
/*  88:135 */         fail(ctx, length);
/*  89:    */       }
/*  90:    */     }
/*  91:    */     else
/*  92:    */     {
/*  93:138 */       this.discardedBytes += buffer.readableBytes();
/*  94:139 */       buffer.readerIndex(buffer.writerIndex());
/*  95:    */     }
/*  96:141 */     return null;
/*  97:    */   }
/*  98:    */   
/*  99:    */   private void fail(ChannelHandlerContext ctx, int length)
/* 100:    */   {
/* 101:146 */     fail(ctx, String.valueOf(length));
/* 102:    */   }
/* 103:    */   
/* 104:    */   private void fail(ChannelHandlerContext ctx, String length)
/* 105:    */   {
/* 106:150 */     ctx.fireExceptionCaught(new TooLongFrameException("frame length (" + length + ") exceeds the allowed maximum (" + this.maxLength + ')'));
/* 107:    */   }
/* 108:    */   
/* 109:    */   private int findEndOfLine(ByteBuf buffer)
/* 110:    */   {
/* 111:160 */     int totalLength = buffer.readableBytes();
/* 112:161 */     int i = buffer.forEachByte(buffer.readerIndex() + this.offset, totalLength - this.offset, ByteProcessor.FIND_LF);
/* 113:162 */     if (i >= 0)
/* 114:    */     {
/* 115:163 */       this.offset = 0;
/* 116:164 */       if ((i > 0) && (buffer.getByte(i - 1) == 13)) {
/* 117:165 */         i--;
/* 118:    */       }
/* 119:    */     }
/* 120:    */     else
/* 121:    */     {
/* 122:168 */       this.offset = totalLength;
/* 123:    */     }
/* 124:170 */     return i;
/* 125:    */   }
/* 126:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.LineBasedFrameDecoder
 * JD-Core Version:    0.7.0.1
 */