/*   1:    */ package io.netty.handler.codec.compression;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.channel.ChannelHandlerContext;
/*   6:    */ import io.netty.handler.codec.ByteToMessageDecoder;
/*   7:    */ import io.netty.util.internal.EmptyArrays;
/*   8:    */ import java.util.List;
/*   9:    */ import java.util.zip.Adler32;
/*  10:    */ import java.util.zip.Checksum;
/*  11:    */ 
/*  12:    */ public class FastLzFrameDecoder
/*  13:    */   extends ByteToMessageDecoder
/*  14:    */ {
/*  15:    */   private static enum State
/*  16:    */   {
/*  17: 39 */     INIT_BLOCK,  INIT_BLOCK_PARAMS,  DECOMPRESS_DATA,  CORRUPTED;
/*  18:    */     
/*  19:    */     private State() {}
/*  20:    */   }
/*  21:    */   
/*  22: 45 */   private State currentState = State.INIT_BLOCK;
/*  23:    */   private final Checksum checksum;
/*  24:    */   private int chunkLength;
/*  25:    */   private int originalLength;
/*  26:    */   private boolean isCompressed;
/*  27:    */   private boolean hasChecksum;
/*  28:    */   private int currentChecksum;
/*  29:    */   
/*  30:    */   public FastLzFrameDecoder()
/*  31:    */   {
/*  32: 82 */     this(false);
/*  33:    */   }
/*  34:    */   
/*  35:    */   public FastLzFrameDecoder(boolean validateChecksums)
/*  36:    */   {
/*  37: 96 */     this(validateChecksums ? new Adler32() : null);
/*  38:    */   }
/*  39:    */   
/*  40:    */   public FastLzFrameDecoder(Checksum checksum)
/*  41:    */   {
/*  42:107 */     this.checksum = checksum;
/*  43:    */   }
/*  44:    */   
/*  45:    */   protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/*  46:    */     throws Exception
/*  47:    */   {
/*  48:    */     try
/*  49:    */     {
/*  50:113 */       switch (1.$SwitchMap$io$netty$handler$codec$compression$FastLzFrameDecoder$State[this.currentState.ordinal()])
/*  51:    */       {
/*  52:    */       case 1: 
/*  53:115 */         if (in.readableBytes() >= 4)
/*  54:    */         {
/*  55:119 */           int magic = in.readUnsignedMedium();
/*  56:120 */           if (magic != 4607066) {
/*  57:121 */             throw new DecompressionException("unexpected block identifier");
/*  58:    */           }
/*  59:124 */           byte options = in.readByte();
/*  60:125 */           this.isCompressed = ((options & 0x1) == 1);
/*  61:126 */           this.hasChecksum = ((options & 0x10) == 16);
/*  62:    */           
/*  63:128 */           this.currentState = State.INIT_BLOCK_PARAMS;
/*  64:    */         }
/*  65:    */         break;
/*  66:    */       case 2: 
/*  67:131 */         if (in.readableBytes() >= 2 + (this.isCompressed ? 2 : 0) + (this.hasChecksum ? 4 : 0))
/*  68:    */         {
/*  69:134 */           this.currentChecksum = (this.hasChecksum ? in.readInt() : 0);
/*  70:135 */           this.chunkLength = in.readUnsignedShort();
/*  71:136 */           this.originalLength = (this.isCompressed ? in.readUnsignedShort() : this.chunkLength);
/*  72:    */           
/*  73:138 */           this.currentState = State.DECOMPRESS_DATA;
/*  74:    */         }
/*  75:    */         break;
/*  76:    */       case 3: 
/*  77:141 */         int chunkLength = this.chunkLength;
/*  78:142 */         if (in.readableBytes() >= chunkLength)
/*  79:    */         {
/*  80:146 */           int idx = in.readerIndex();
/*  81:147 */           int originalLength = this.originalLength;
/*  82:    */           int outputPtr;
/*  83:    */           ByteBuf uncompressed;
/*  84:    */           byte[] output;
/*  85:    */           int outputPtr;
/*  86:153 */           if (originalLength != 0)
/*  87:    */           {
/*  88:154 */             ByteBuf uncompressed = ctx.alloc().heapBuffer(originalLength, originalLength);
/*  89:155 */             byte[] output = uncompressed.array();
/*  90:156 */             outputPtr = uncompressed.arrayOffset() + uncompressed.writerIndex();
/*  91:    */           }
/*  92:    */           else
/*  93:    */           {
/*  94:158 */             uncompressed = null;
/*  95:159 */             output = EmptyArrays.EMPTY_BYTES;
/*  96:160 */             outputPtr = 0;
/*  97:    */           }
/*  98:163 */           boolean success = false;
/*  99:    */           try
/* 100:    */           {
/* 101:165 */             if (this.isCompressed)
/* 102:    */             {
/* 103:    */               int inputPtr;
/* 104:    */               byte[] input;
/* 105:    */               int inputPtr;
/* 106:168 */               if (in.hasArray())
/* 107:    */               {
/* 108:169 */                 byte[] input = in.array();
/* 109:170 */                 inputPtr = in.arrayOffset() + idx;
/* 110:    */               }
/* 111:    */               else
/* 112:    */               {
/* 113:172 */                 input = new byte[chunkLength];
/* 114:173 */                 in.getBytes(idx, input);
/* 115:174 */                 inputPtr = 0;
/* 116:    */               }
/* 117:177 */               int decompressedBytes = FastLz.decompress(input, inputPtr, chunkLength, output, outputPtr, originalLength);
/* 118:179 */               if (originalLength != decompressedBytes) {
/* 119:180 */                 throw new DecompressionException(String.format("stream corrupted: originalLength(%d) and actual length(%d) mismatch", new Object[] {
/* 120:    */                 
/* 121:182 */                   Integer.valueOf(originalLength), Integer.valueOf(decompressedBytes) }));
/* 122:    */               }
/* 123:    */             }
/* 124:    */             else
/* 125:    */             {
/* 126:185 */               in.getBytes(idx, output, outputPtr, chunkLength);
/* 127:    */             }
/* 128:188 */             Checksum checksum = this.checksum;
/* 129:189 */             if ((this.hasChecksum) && (checksum != null))
/* 130:    */             {
/* 131:190 */               checksum.reset();
/* 132:191 */               checksum.update(output, outputPtr, originalLength);
/* 133:192 */               int checksumResult = (int)checksum.getValue();
/* 134:193 */               if (checksumResult != this.currentChecksum) {
/* 135:194 */                 throw new DecompressionException(String.format("stream corrupted: mismatching checksum: %d (expected: %d)", new Object[] {
/* 136:    */                 
/* 137:196 */                   Integer.valueOf(checksumResult), Integer.valueOf(this.currentChecksum) }));
/* 138:    */               }
/* 139:    */             }
/* 140:200 */             if (uncompressed != null)
/* 141:    */             {
/* 142:201 */               uncompressed.writerIndex(uncompressed.writerIndex() + originalLength);
/* 143:202 */               out.add(uncompressed);
/* 144:    */             }
/* 145:204 */             in.skipBytes(chunkLength);
/* 146:    */             
/* 147:206 */             this.currentState = State.INIT_BLOCK;
/* 148:207 */             success = true;
/* 149:    */           }
/* 150:    */           finally
/* 151:    */           {
/* 152:209 */             if ((!success) && (uncompressed != null)) {
/* 153:210 */               uncompressed.release();
/* 154:    */             }
/* 155:    */           }
/* 156:    */         }
/* 157:213 */         break;
/* 158:    */       case 4: 
/* 159:215 */         in.skipBytes(in.readableBytes());
/* 160:216 */         break;
/* 161:    */       default: 
/* 162:218 */         throw new IllegalStateException();
/* 163:    */       }
/* 164:    */     }
/* 165:    */     catch (Exception e)
/* 166:    */     {
/* 167:221 */       this.currentState = State.CORRUPTED;
/* 168:222 */       throw e;
/* 169:    */     }
/* 170:    */   }
/* 171:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.compression.FastLzFrameDecoder
 * JD-Core Version:    0.7.0.1
 */