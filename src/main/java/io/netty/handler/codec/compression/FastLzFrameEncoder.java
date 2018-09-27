/*   1:    */ package io.netty.handler.codec.compression;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.ChannelHandlerContext;
/*   5:    */ import io.netty.handler.codec.MessageToByteEncoder;
/*   6:    */ import java.util.zip.Adler32;
/*   7:    */ import java.util.zip.Checksum;
/*   8:    */ 
/*   9:    */ public class FastLzFrameEncoder
/*  10:    */   extends MessageToByteEncoder<ByteBuf>
/*  11:    */ {
/*  12:    */   private final int level;
/*  13:    */   private final Checksum checksum;
/*  14:    */   
/*  15:    */   public FastLzFrameEncoder()
/*  16:    */   {
/*  17: 47 */     this(0, null);
/*  18:    */   }
/*  19:    */   
/*  20:    */   public FastLzFrameEncoder(int level)
/*  21:    */   {
/*  22: 59 */     this(level, null);
/*  23:    */   }
/*  24:    */   
/*  25:    */   public FastLzFrameEncoder(boolean validateChecksums)
/*  26:    */   {
/*  27: 73 */     this(0, validateChecksums ? new Adler32() : null);
/*  28:    */   }
/*  29:    */   
/*  30:    */   public FastLzFrameEncoder(int level, Checksum checksum)
/*  31:    */   {
/*  32: 88 */     super(false);
/*  33: 89 */     if ((level != 0) && (level != 1) && (level != 2)) {
/*  34: 90 */       throw new IllegalArgumentException(String.format("level: %d (expected: %d or %d or %d)", new Object[] {
/*  35: 91 */         Integer.valueOf(level), Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2) }));
/*  36:    */     }
/*  37: 93 */     this.level = level;
/*  38: 94 */     this.checksum = checksum;
/*  39:    */   }
/*  40:    */   
/*  41:    */   protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out)
/*  42:    */     throws Exception
/*  43:    */   {
/*  44: 99 */     Checksum checksum = this.checksum;
/*  45:    */     for (;;)
/*  46:    */     {
/*  47:102 */       if (!in.isReadable()) {
/*  48:103 */         return;
/*  49:    */       }
/*  50:105 */       int idx = in.readerIndex();
/*  51:106 */       int length = Math.min(in.readableBytes(), 65535);
/*  52:    */       
/*  53:108 */       int outputIdx = out.writerIndex();
/*  54:109 */       out.setMedium(outputIdx, 4607066);
/*  55:110 */       int outputOffset = outputIdx + 4 + (checksum != null ? 4 : 0);
/*  56:    */       int chunkLength;
/*  57:    */       byte blockType;
/*  58:    */       int chunkLength;
/*  59:114 */       if (length < 32)
/*  60:    */       {
/*  61:115 */         byte blockType = 0;
/*  62:    */         
/*  63:117 */         out.ensureWritable(outputOffset + 2 + length);
/*  64:118 */         byte[] output = out.array();
/*  65:119 */         int outputPtr = out.arrayOffset() + outputOffset + 2;
/*  66:121 */         if (checksum != null)
/*  67:    */         {
/*  68:    */           int inputPtr;
/*  69:    */           byte[] input;
/*  70:    */           int inputPtr;
/*  71:124 */           if (in.hasArray())
/*  72:    */           {
/*  73:125 */             byte[] input = in.array();
/*  74:126 */             inputPtr = in.arrayOffset() + idx;
/*  75:    */           }
/*  76:    */           else
/*  77:    */           {
/*  78:128 */             input = new byte[length];
/*  79:129 */             in.getBytes(idx, input);
/*  80:130 */             inputPtr = 0;
/*  81:    */           }
/*  82:133 */           checksum.reset();
/*  83:134 */           checksum.update(input, inputPtr, length);
/*  84:135 */           out.setInt(outputIdx + 4, (int)checksum.getValue());
/*  85:    */           
/*  86:137 */           System.arraycopy(input, inputPtr, output, outputPtr, length);
/*  87:    */         }
/*  88:    */         else
/*  89:    */         {
/*  90:139 */           in.getBytes(idx, output, outputPtr, length);
/*  91:    */         }
/*  92:141 */         chunkLength = length;
/*  93:    */       }
/*  94:    */       else
/*  95:    */       {
/*  96:    */         int inputPtr;
/*  97:    */         byte[] input;
/*  98:    */         int inputPtr;
/*  99:146 */         if (in.hasArray())
/* 100:    */         {
/* 101:147 */           byte[] input = in.array();
/* 102:148 */           inputPtr = in.arrayOffset() + idx;
/* 103:    */         }
/* 104:    */         else
/* 105:    */         {
/* 106:150 */           input = new byte[length];
/* 107:151 */           in.getBytes(idx, input);
/* 108:152 */           inputPtr = 0;
/* 109:    */         }
/* 110:155 */         if (checksum != null)
/* 111:    */         {
/* 112:156 */           checksum.reset();
/* 113:157 */           checksum.update(input, inputPtr, length);
/* 114:158 */           out.setInt(outputIdx + 4, (int)checksum.getValue());
/* 115:    */         }
/* 116:161 */         int maxOutputLength = FastLz.calculateOutputBufferLength(length);
/* 117:162 */         out.ensureWritable(outputOffset + 4 + maxOutputLength);
/* 118:163 */         byte[] output = out.array();
/* 119:164 */         int outputPtr = out.arrayOffset() + outputOffset + 4;
/* 120:165 */         int compressedLength = FastLz.compress(input, inputPtr, length, output, outputPtr, this.level);
/* 121:166 */         if (compressedLength < length)
/* 122:    */         {
/* 123:167 */           byte blockType = 1;
/* 124:168 */           int chunkLength = compressedLength;
/* 125:    */           
/* 126:170 */           out.setShort(outputOffset, chunkLength);
/* 127:171 */           outputOffset += 2;
/* 128:    */         }
/* 129:    */         else
/* 130:    */         {
/* 131:173 */           blockType = 0;
/* 132:174 */           System.arraycopy(input, inputPtr, output, outputPtr - 2, length);
/* 133:175 */           chunkLength = length;
/* 134:    */         }
/* 135:    */       }
/* 136:178 */       out.setShort(outputOffset, length);
/* 137:    */       
/* 138:180 */       out.setByte(outputIdx + 3, blockType | (checksum != null ? 16 : 0));
/* 139:    */       
/* 140:182 */       out.writerIndex(outputOffset + 2 + chunkLength);
/* 141:183 */       in.skipBytes(length);
/* 142:    */     }
/* 143:    */   }
/* 144:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.compression.FastLzFrameEncoder
 * JD-Core Version:    0.7.0.1
 */