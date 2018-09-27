/*   1:    */ package io.netty.handler.codec.compression;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.channel.ChannelHandlerContext;
/*   6:    */ import io.netty.handler.codec.ByteToMessageDecoder;
/*   7:    */ import java.util.List;
/*   8:    */ import java.util.zip.Checksum;
/*   9:    */ import net.jpountz.lz4.LZ4Exception;
/*  10:    */ import net.jpountz.lz4.LZ4Factory;
/*  11:    */ import net.jpountz.lz4.LZ4FastDecompressor;
/*  12:    */ import net.jpountz.xxhash.StreamingXXHash32;
/*  13:    */ import net.jpountz.xxhash.XXHashFactory;
/*  14:    */ 
/*  15:    */ public class Lz4FrameDecoder
/*  16:    */   extends ByteToMessageDecoder
/*  17:    */ {
/*  18:    */   private static enum State
/*  19:    */   {
/*  20: 52 */     INIT_BLOCK,  DECOMPRESS_DATA,  FINISHED,  CORRUPTED;
/*  21:    */     
/*  22:    */     private State() {}
/*  23:    */   }
/*  24:    */   
/*  25: 58 */   private State currentState = State.INIT_BLOCK;
/*  26:    */   private LZ4FastDecompressor decompressor;
/*  27:    */   private ByteBufChecksum checksum;
/*  28:    */   private int blockType;
/*  29:    */   private int compressedLength;
/*  30:    */   private int decompressedLength;
/*  31:    */   private int currentChecksum;
/*  32:    */   
/*  33:    */   public Lz4FrameDecoder()
/*  34:    */   {
/*  35:100 */     this(false);
/*  36:    */   }
/*  37:    */   
/*  38:    */   public Lz4FrameDecoder(boolean validateChecksums)
/*  39:    */   {
/*  40:111 */     this(LZ4Factory.fastestInstance(), validateChecksums);
/*  41:    */   }
/*  42:    */   
/*  43:    */   public Lz4FrameDecoder(LZ4Factory factory, boolean validateChecksums)
/*  44:    */   {
/*  45:127 */     this(factory, validateChecksums ? 
/*  46:128 */       XXHashFactory.fastestInstance().newStreamingHash32(-1756908916).asChecksum() : null);
/*  47:    */   }
/*  48:    */   
/*  49:    */   public Lz4FrameDecoder(LZ4Factory factory, Checksum checksum)
/*  50:    */   {
/*  51:142 */     if (factory == null) {
/*  52:143 */       throw new NullPointerException("factory");
/*  53:    */     }
/*  54:145 */     this.decompressor = factory.fastDecompressor();
/*  55:146 */     this.checksum = (checksum == null ? null : ByteBufChecksum.wrapChecksum(checksum));
/*  56:    */   }
/*  57:    */   
/*  58:    */   protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/*  59:    */     throws Exception
/*  60:    */   {
/*  61:    */     try
/*  62:    */     {
/*  63:152 */       switch (1.$SwitchMap$io$netty$handler$codec$compression$Lz4FrameDecoder$State[this.currentState.ordinal()])
/*  64:    */       {
/*  65:    */       case 1: 
/*  66:154 */         if (in.readableBytes() >= 21)
/*  67:    */         {
/*  68:157 */           long magic = in.readLong();
/*  69:158 */           if (magic != 5501767354678207339L) {
/*  70:159 */             throw new DecompressionException("unexpected block identifier");
/*  71:    */           }
/*  72:162 */           int token = in.readByte();
/*  73:163 */           int compressionLevel = (token & 0xF) + 10;
/*  74:164 */           int blockType = token & 0xF0;
/*  75:    */           
/*  76:166 */           int compressedLength = Integer.reverseBytes(in.readInt());
/*  77:167 */           if ((compressedLength < 0) || (compressedLength > 33554432)) {
/*  78:168 */             throw new DecompressionException(String.format("invalid compressedLength: %d (expected: 0-%d)", new Object[] {
/*  79:    */             
/*  80:170 */               Integer.valueOf(compressedLength), Integer.valueOf(33554432) }));
/*  81:    */           }
/*  82:173 */           int decompressedLength = Integer.reverseBytes(in.readInt());
/*  83:174 */           int maxDecompressedLength = 1 << compressionLevel;
/*  84:175 */           if ((decompressedLength < 0) || (decompressedLength > maxDecompressedLength)) {
/*  85:176 */             throw new DecompressionException(String.format("invalid decompressedLength: %d (expected: 0-%d)", new Object[] {
/*  86:    */             
/*  87:178 */               Integer.valueOf(decompressedLength), Integer.valueOf(maxDecompressedLength) }));
/*  88:    */           }
/*  89:180 */           if (((decompressedLength == 0) && (compressedLength != 0)) || ((decompressedLength != 0) && (compressedLength == 0)) || ((blockType == 16) && (decompressedLength != compressedLength))) {
/*  90:183 */             throw new DecompressionException(String.format("stream corrupted: compressedLength(%d) and decompressedLength(%d) mismatch", new Object[] {
/*  91:    */             
/*  92:185 */               Integer.valueOf(compressedLength), Integer.valueOf(decompressedLength) }));
/*  93:    */           }
/*  94:188 */           int currentChecksum = Integer.reverseBytes(in.readInt());
/*  95:189 */           if ((decompressedLength == 0) && (compressedLength == 0))
/*  96:    */           {
/*  97:190 */             if (currentChecksum != 0) {
/*  98:191 */               throw new DecompressionException("stream corrupted: checksum error");
/*  99:    */             }
/* 100:193 */             this.currentState = State.FINISHED;
/* 101:194 */             this.decompressor = null;
/* 102:195 */             this.checksum = null;
/* 103:    */           }
/* 104:    */           else
/* 105:    */           {
/* 106:199 */             this.blockType = blockType;
/* 107:200 */             this.compressedLength = compressedLength;
/* 108:201 */             this.decompressedLength = decompressedLength;
/* 109:202 */             this.currentChecksum = currentChecksum;
/* 110:    */             
/* 111:204 */             this.currentState = State.DECOMPRESS_DATA;
/* 112:    */           }
/* 113:    */         }
/* 114:    */         break;
/* 115:    */       case 2: 
/* 116:207 */         int blockType = this.blockType;
/* 117:208 */         int compressedLength = this.compressedLength;
/* 118:209 */         int decompressedLength = this.decompressedLength;
/* 119:210 */         int currentChecksum = this.currentChecksum;
/* 120:212 */         if (in.readableBytes() >= compressedLength)
/* 121:    */         {
/* 122:216 */           ByteBufChecksum checksum = this.checksum;
/* 123:217 */           ByteBuf uncompressed = null;
/* 124:    */           try
/* 125:    */           {
/* 126:220 */             switch (blockType)
/* 127:    */             {
/* 128:    */             case 16: 
/* 129:224 */               uncompressed = in.retainedSlice(in.readerIndex(), decompressedLength);
/* 130:225 */               break;
/* 131:    */             case 32: 
/* 132:227 */               uncompressed = ctx.alloc().buffer(decompressedLength, decompressedLength);
/* 133:    */               
/* 134:229 */               this.decompressor.decompress(CompressionUtil.safeNioBuffer(in), uncompressed
/* 135:230 */                 .internalNioBuffer(uncompressed.writerIndex(), decompressedLength));
/* 136:    */               
/* 137:232 */               uncompressed.writerIndex(uncompressed.writerIndex() + decompressedLength);
/* 138:233 */               break;
/* 139:    */             default: 
/* 140:235 */               throw new DecompressionException(String.format("unexpected blockType: %d (expected: %d or %d)", new Object[] {
/* 141:    */               
/* 142:237 */                 Integer.valueOf(blockType), Integer.valueOf(16), Integer.valueOf(32) }));
/* 143:    */             }
/* 144:240 */             in.skipBytes(compressedLength);
/* 145:242 */             if (checksum != null) {
/* 146:243 */               CompressionUtil.checkChecksum(checksum, uncompressed, currentChecksum);
/* 147:    */             }
/* 148:245 */             out.add(uncompressed);
/* 149:246 */             uncompressed = null;
/* 150:247 */             this.currentState = State.INIT_BLOCK;
/* 151:    */           }
/* 152:    */           catch (LZ4Exception e)
/* 153:    */           {
/* 154:249 */             throw new DecompressionException(e);
/* 155:    */           }
/* 156:    */           finally
/* 157:    */           {
/* 158:251 */             if (uncompressed != null) {
/* 159:252 */               uncompressed.release();
/* 160:    */             }
/* 161:    */           }
/* 162:    */         }
/* 163:255 */         break;
/* 164:    */       case 3: 
/* 165:    */       case 4: 
/* 166:258 */         in.skipBytes(in.readableBytes());
/* 167:259 */         break;
/* 168:    */       default: 
/* 169:261 */         throw new IllegalStateException();
/* 170:    */       }
/* 171:    */     }
/* 172:    */     catch (Exception e)
/* 173:    */     {
/* 174:264 */       this.currentState = State.CORRUPTED;
/* 175:265 */       throw e;
/* 176:    */     }
/* 177:    */   }
/* 178:    */   
/* 179:    */   public boolean isClosed()
/* 180:    */   {
/* 181:274 */     return this.currentState == State.FINISHED;
/* 182:    */   }
/* 183:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.compression.Lz4FrameDecoder
 * JD-Core Version:    0.7.0.1
 */