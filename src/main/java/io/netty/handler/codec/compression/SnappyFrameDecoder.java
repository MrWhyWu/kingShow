/*   1:    */ package io.netty.handler.codec.compression;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.channel.ChannelHandlerContext;
/*   6:    */ import io.netty.handler.codec.ByteToMessageDecoder;
/*   7:    */ import java.util.List;
/*   8:    */ 
/*   9:    */ public class SnappyFrameDecoder
/*  10:    */   extends ByteToMessageDecoder
/*  11:    */ {
/*  12:    */   private static final int SNAPPY_IDENTIFIER_LEN = 6;
/*  13:    */   private static final int MAX_UNCOMPRESSED_DATA_SIZE = 65540;
/*  14:    */   
/*  15:    */   private static enum ChunkType
/*  16:    */   {
/*  17: 40 */     STREAM_IDENTIFIER,  COMPRESSED_DATA,  UNCOMPRESSED_DATA,  RESERVED_UNSKIPPABLE,  RESERVED_SKIPPABLE;
/*  18:    */     
/*  19:    */     private ChunkType() {}
/*  20:    */   }
/*  21:    */   
/*  22: 50 */   private final Snappy snappy = new Snappy();
/*  23:    */   private final boolean validateChecksums;
/*  24:    */   private boolean started;
/*  25:    */   private boolean corrupted;
/*  26:    */   
/*  27:    */   public SnappyFrameDecoder()
/*  28:    */   {
/*  29: 62 */     this(false);
/*  30:    */   }
/*  31:    */   
/*  32:    */   public SnappyFrameDecoder(boolean validateChecksums)
/*  33:    */   {
/*  34: 75 */     this.validateChecksums = validateChecksums;
/*  35:    */   }
/*  36:    */   
/*  37:    */   protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/*  38:    */     throws Exception
/*  39:    */   {
/*  40: 80 */     if (this.corrupted)
/*  41:    */     {
/*  42: 81 */       in.skipBytes(in.readableBytes());
/*  43: 82 */       return;
/*  44:    */     }
/*  45:    */     try
/*  46:    */     {
/*  47: 86 */       int idx = in.readerIndex();
/*  48: 87 */       int inSize = in.readableBytes();
/*  49: 88 */       if (inSize < 4) {
/*  50: 91 */         return;
/*  51:    */       }
/*  52: 94 */       int chunkTypeVal = in.getUnsignedByte(idx);
/*  53: 95 */       ChunkType chunkType = mapChunkType((byte)chunkTypeVal);
/*  54: 96 */       int chunkLength = in.getUnsignedMediumLE(idx + 1);
/*  55: 98 */       switch (1.$SwitchMap$io$netty$handler$codec$compression$SnappyFrameDecoder$ChunkType[chunkType.ordinal()])
/*  56:    */       {
/*  57:    */       case 1: 
/*  58:100 */         if (chunkLength != 6) {
/*  59:101 */           throw new DecompressionException("Unexpected length of stream identifier: " + chunkLength);
/*  60:    */         }
/*  61:104 */         if (inSize >= 10)
/*  62:    */         {
/*  63:108 */           in.skipBytes(4);
/*  64:109 */           int offset = in.readerIndex();
/*  65:110 */           in.skipBytes(6);
/*  66:    */           
/*  67:112 */           checkByte(in.getByte(offset++), (byte)115);
/*  68:113 */           checkByte(in.getByte(offset++), (byte)78);
/*  69:114 */           checkByte(in.getByte(offset++), (byte)97);
/*  70:115 */           checkByte(in.getByte(offset++), (byte)80);
/*  71:116 */           checkByte(in.getByte(offset++), (byte)112);
/*  72:117 */           checkByte(in.getByte(offset), (byte)89);
/*  73:    */           
/*  74:119 */           this.started = true;
/*  75:    */         }
/*  76:120 */         break;
/*  77:    */       case 2: 
/*  78:122 */         if (!this.started) {
/*  79:123 */           throw new DecompressionException("Received RESERVED_SKIPPABLE tag before STREAM_IDENTIFIER");
/*  80:    */         }
/*  81:126 */         if (inSize < 4 + chunkLength) {
/*  82:128 */           return;
/*  83:    */         }
/*  84:131 */         in.skipBytes(4 + chunkLength);
/*  85:132 */         break;
/*  86:    */       case 3: 
/*  87:138 */         throw new DecompressionException("Found reserved unskippable chunk type: 0x" + Integer.toHexString(chunkTypeVal));
/*  88:    */       case 4: 
/*  89:140 */         if (!this.started) {
/*  90:141 */           throw new DecompressionException("Received UNCOMPRESSED_DATA tag before STREAM_IDENTIFIER");
/*  91:    */         }
/*  92:143 */         if (chunkLength > 65540) {
/*  93:144 */           throw new DecompressionException("Received UNCOMPRESSED_DATA larger than 65540 bytes");
/*  94:    */         }
/*  95:147 */         if (inSize < 4 + chunkLength) {
/*  96:148 */           return;
/*  97:    */         }
/*  98:151 */         in.skipBytes(4);
/*  99:152 */         if (this.validateChecksums)
/* 100:    */         {
/* 101:153 */           int checksum = in.readIntLE();
/* 102:154 */           Snappy.validateChecksum(checksum, in, in.readerIndex(), chunkLength - 4);
/* 103:    */         }
/* 104:    */         else
/* 105:    */         {
/* 106:156 */           in.skipBytes(4);
/* 107:    */         }
/* 108:158 */         out.add(in.readRetainedSlice(chunkLength - 4));
/* 109:159 */         break;
/* 110:    */       case 5: 
/* 111:161 */         if (!this.started) {
/* 112:162 */           throw new DecompressionException("Received COMPRESSED_DATA tag before STREAM_IDENTIFIER");
/* 113:    */         }
/* 114:165 */         if (inSize < 4 + chunkLength) {
/* 115:166 */           return;
/* 116:    */         }
/* 117:169 */         in.skipBytes(4);
/* 118:170 */         int checksum = in.readIntLE();
/* 119:171 */         ByteBuf uncompressed = ctx.alloc().buffer();
/* 120:    */         try
/* 121:    */         {
/* 122:173 */           if (this.validateChecksums)
/* 123:    */           {
/* 124:174 */             int oldWriterIndex = in.writerIndex();
/* 125:    */             try
/* 126:    */             {
/* 127:176 */               in.writerIndex(in.readerIndex() + chunkLength - 4);
/* 128:177 */               this.snappy.decode(in, uncompressed);
/* 129:    */             }
/* 130:    */             finally
/* 131:    */             {
/* 132:179 */               in.writerIndex(oldWriterIndex);
/* 133:    */             }
/* 134:181 */             Snappy.validateChecksum(checksum, uncompressed, 0, uncompressed.writerIndex());
/* 135:    */           }
/* 136:    */           else
/* 137:    */           {
/* 138:183 */             this.snappy.decode(in.readSlice(chunkLength - 4), uncompressed);
/* 139:    */           }
/* 140:185 */           out.add(uncompressed);
/* 141:186 */           uncompressed = null;
/* 142:    */         }
/* 143:    */         finally
/* 144:    */         {
/* 145:188 */           if (uncompressed != null) {
/* 146:189 */             uncompressed.release();
/* 147:    */           }
/* 148:    */         }
/* 149:192 */         this.snappy.reset();
/* 150:    */       }
/* 151:    */     }
/* 152:    */     catch (Exception e)
/* 153:    */     {
/* 154:196 */       this.corrupted = true;
/* 155:197 */       throw e;
/* 156:    */     }
/* 157:    */   }
/* 158:    */   
/* 159:    */   private static void checkByte(byte actual, byte expect)
/* 160:    */   {
/* 161:202 */     if (actual != expect) {
/* 162:203 */       throw new DecompressionException("Unexpected stream identifier contents. Mismatched snappy protocol version?");
/* 163:    */     }
/* 164:    */   }
/* 165:    */   
/* 166:    */   private static ChunkType mapChunkType(byte type)
/* 167:    */   {
/* 168:215 */     if (type == 0) {
/* 169:216 */       return ChunkType.COMPRESSED_DATA;
/* 170:    */     }
/* 171:217 */     if (type == 1) {
/* 172:218 */       return ChunkType.UNCOMPRESSED_DATA;
/* 173:    */     }
/* 174:219 */     if (type == -1) {
/* 175:220 */       return ChunkType.STREAM_IDENTIFIER;
/* 176:    */     }
/* 177:221 */     if ((type & 0x80) == 128) {
/* 178:222 */       return ChunkType.RESERVED_SKIPPABLE;
/* 179:    */     }
/* 180:224 */     return ChunkType.RESERVED_UNSKIPPABLE;
/* 181:    */   }
/* 182:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.compression.SnappyFrameDecoder
 * JD-Core Version:    0.7.0.1
 */