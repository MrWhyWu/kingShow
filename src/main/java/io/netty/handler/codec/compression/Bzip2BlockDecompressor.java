/*   1:    */ package io.netty.handler.codec.compression;
/*   2:    */ 
/*   3:    */ final class Bzip2BlockDecompressor
/*   4:    */ {
/*   5:    */   private final Bzip2BitReader reader;
/*   6: 42 */   private final Crc32 crc = new Crc32();
/*   7:    */   private final int blockCRC;
/*   8:    */   private final boolean blockRandomised;
/*   9:    */   int huffmanEndOfBlockSymbol;
/*  10:    */   int huffmanInUse16;
/*  11: 70 */   final byte[] huffmanSymbolMap = new byte[256];
/*  12: 77 */   private final int[] bwtByteCounts = new int[256];
/*  13:    */   private final byte[] bwtBlock;
/*  14:    */   private final int bwtStartPointer;
/*  15:    */   private int[] bwtMergedPointers;
/*  16:    */   private int bwtCurrentMergedPointer;
/*  17:    */   private int bwtBlockLength;
/*  18:    */   private int bwtBytesDecoded;
/*  19:122 */   private int rleLastDecodedByte = -1;
/*  20:    */   private int rleAccumulator;
/*  21:    */   private int rleRepeat;
/*  22:    */   private int randomIndex;
/*  23:143 */   private int randomCount = Bzip2Rand.rNums(0) - 1;
/*  24:148 */   private final Bzip2MoveToFrontTable symbolMTF = new Bzip2MoveToFrontTable();
/*  25:    */   private int repeatCount;
/*  26:152 */   private int repeatIncrement = 1;
/*  27:    */   private int mtfValue;
/*  28:    */   
/*  29:    */   Bzip2BlockDecompressor(int blockSize, int blockCRC, boolean blockRandomised, int bwtStartPointer, Bzip2BitReader reader)
/*  30:    */   {
/*  31:158 */     this.bwtBlock = new byte[blockSize];
/*  32:    */     
/*  33:160 */     this.blockCRC = blockCRC;
/*  34:161 */     this.blockRandomised = blockRandomised;
/*  35:162 */     this.bwtStartPointer = bwtStartPointer;
/*  36:    */     
/*  37:164 */     this.reader = reader;
/*  38:    */   }
/*  39:    */   
/*  40:    */   boolean decodeHuffmanData(Bzip2HuffmanStageDecoder huffmanDecoder)
/*  41:    */   {
/*  42:172 */     Bzip2BitReader reader = this.reader;
/*  43:173 */     byte[] bwtBlock = this.bwtBlock;
/*  44:174 */     byte[] huffmanSymbolMap = this.huffmanSymbolMap;
/*  45:175 */     int streamBlockSize = this.bwtBlock.length;
/*  46:176 */     int huffmanEndOfBlockSymbol = this.huffmanEndOfBlockSymbol;
/*  47:177 */     int[] bwtByteCounts = this.bwtByteCounts;
/*  48:178 */     Bzip2MoveToFrontTable symbolMTF = this.symbolMTF;
/*  49:    */     
/*  50:180 */     int bwtBlockLength = this.bwtBlockLength;
/*  51:181 */     int repeatCount = this.repeatCount;
/*  52:182 */     int repeatIncrement = this.repeatIncrement;
/*  53:183 */     int mtfValue = this.mtfValue;
/*  54:    */     for (;;)
/*  55:    */     {
/*  56:186 */       if (!reader.hasReadableBits(23))
/*  57:    */       {
/*  58:187 */         this.bwtBlockLength = bwtBlockLength;
/*  59:188 */         this.repeatCount = repeatCount;
/*  60:189 */         this.repeatIncrement = repeatIncrement;
/*  61:190 */         this.mtfValue = mtfValue;
/*  62:191 */         return false;
/*  63:    */       }
/*  64:193 */       int nextSymbol = huffmanDecoder.nextSymbol();
/*  65:195 */       if (nextSymbol == 0)
/*  66:    */       {
/*  67:196 */         repeatCount += repeatIncrement;
/*  68:197 */         repeatIncrement <<= 1;
/*  69:    */       }
/*  70:198 */       else if (nextSymbol == 1)
/*  71:    */       {
/*  72:199 */         repeatCount += (repeatIncrement << 1);
/*  73:200 */         repeatIncrement <<= 1;
/*  74:    */       }
/*  75:    */       else
/*  76:    */       {
/*  77:202 */         if (repeatCount > 0)
/*  78:    */         {
/*  79:203 */           if (bwtBlockLength + repeatCount > streamBlockSize) {
/*  80:204 */             throw new DecompressionException("block exceeds declared block size");
/*  81:    */           }
/*  82:206 */           byte nextByte = huffmanSymbolMap[mtfValue];
/*  83:207 */           bwtByteCounts[(nextByte & 0xFF)] += repeatCount;
/*  84:    */           for (;;)
/*  85:    */           {
/*  86:208 */             repeatCount--;
/*  87:208 */             if (repeatCount < 0) {
/*  88:    */               break;
/*  89:    */             }
/*  90:209 */             bwtBlock[(bwtBlockLength++)] = nextByte;
/*  91:    */           }
/*  92:212 */           repeatCount = 0;
/*  93:213 */           repeatIncrement = 1;
/*  94:    */         }
/*  95:216 */         if (nextSymbol == huffmanEndOfBlockSymbol) {
/*  96:    */           break;
/*  97:    */         }
/*  98:220 */         if (bwtBlockLength >= streamBlockSize) {
/*  99:221 */           throw new DecompressionException("block exceeds declared block size");
/* 100:    */         }
/* 101:224 */         mtfValue = symbolMTF.indexToFront(nextSymbol - 1) & 0xFF;
/* 102:    */         
/* 103:226 */         byte nextByte = huffmanSymbolMap[mtfValue];
/* 104:227 */         bwtByteCounts[(nextByte & 0xFF)] += 1;
/* 105:228 */         bwtBlock[(bwtBlockLength++)] = nextByte;
/* 106:    */       }
/* 107:    */     }
/* 108:231 */     this.bwtBlockLength = bwtBlockLength;
/* 109:232 */     initialiseInverseBWT();
/* 110:233 */     return true;
/* 111:    */   }
/* 112:    */   
/* 113:    */   private void initialiseInverseBWT()
/* 114:    */   {
/* 115:240 */     int bwtStartPointer = this.bwtStartPointer;
/* 116:241 */     byte[] bwtBlock = this.bwtBlock;
/* 117:242 */     int[] bwtMergedPointers = new int[this.bwtBlockLength];
/* 118:243 */     int[] characterBase = new int[256];
/* 119:245 */     if ((bwtStartPointer < 0) || (bwtStartPointer >= this.bwtBlockLength)) {
/* 120:246 */       throw new DecompressionException("start pointer invalid");
/* 121:    */     }
/* 122:250 */     System.arraycopy(this.bwtByteCounts, 0, characterBase, 1, 255);
/* 123:251 */     for (int i = 2; i <= 255; i++) {
/* 124:252 */       characterBase[i] += characterBase[(i - 1)];
/* 125:    */     }
/* 126:259 */     for (int i = 0; i < this.bwtBlockLength; i++)
/* 127:    */     {
/* 128:260 */       int value = bwtBlock[i] & 0xFF; int 
/* 129:261 */         tmp119_117 = value; int[] tmp119_115 = characterBase; int tmp121_120 = tmp119_115[tmp119_117];tmp119_115[tmp119_117] = (tmp121_120 + 1);bwtMergedPointers[tmp121_120] = ((i << 8) + value);
/* 130:    */     }
/* 131:264 */     this.bwtMergedPointers = bwtMergedPointers;
/* 132:265 */     this.bwtCurrentMergedPointer = bwtMergedPointers[bwtStartPointer];
/* 133:    */   }
/* 134:    */   
/* 135:    */   public int read()
/* 136:    */   {
/* 137:274 */     while (this.rleRepeat < 1)
/* 138:    */     {
/* 139:275 */       if (this.bwtBytesDecoded == this.bwtBlockLength) {
/* 140:276 */         return -1;
/* 141:    */       }
/* 142:279 */       int nextByte = decodeNextBWTByte();
/* 143:280 */       if (nextByte != this.rleLastDecodedByte)
/* 144:    */       {
/* 145:282 */         this.rleLastDecodedByte = nextByte;
/* 146:283 */         this.rleRepeat = 1;
/* 147:284 */         this.rleAccumulator = 1;
/* 148:285 */         this.crc.updateCRC(nextByte);
/* 149:    */       }
/* 150:287 */       else if (++this.rleAccumulator == 4)
/* 151:    */       {
/* 152:289 */         int rleRepeat = decodeNextBWTByte() + 1;
/* 153:290 */         this.rleRepeat = rleRepeat;
/* 154:291 */         this.rleAccumulator = 0;
/* 155:292 */         this.crc.updateCRC(nextByte, rleRepeat);
/* 156:    */       }
/* 157:    */       else
/* 158:    */       {
/* 159:294 */         this.rleRepeat = 1;
/* 160:295 */         this.crc.updateCRC(nextByte);
/* 161:    */       }
/* 162:    */     }
/* 163:299 */     this.rleRepeat -= 1;
/* 164:    */     
/* 165:301 */     return this.rleLastDecodedByte;
/* 166:    */   }
/* 167:    */   
/* 168:    */   private int decodeNextBWTByte()
/* 169:    */   {
/* 170:310 */     int mergedPointer = this.bwtCurrentMergedPointer;
/* 171:311 */     int nextDecodedByte = mergedPointer & 0xFF;
/* 172:312 */     this.bwtCurrentMergedPointer = this.bwtMergedPointers[(mergedPointer >>> 8)];
/* 173:314 */     if ((this.blockRandomised) && 
/* 174:315 */       (--this.randomCount == 0))
/* 175:    */     {
/* 176:316 */       nextDecodedByte ^= 0x1;
/* 177:317 */       this.randomIndex = ((this.randomIndex + 1) % 512);
/* 178:318 */       this.randomCount = Bzip2Rand.rNums(this.randomIndex);
/* 179:    */     }
/* 180:321 */     this.bwtBytesDecoded += 1;
/* 181:    */     
/* 182:323 */     return nextDecodedByte;
/* 183:    */   }
/* 184:    */   
/* 185:    */   public int blockLength()
/* 186:    */   {
/* 187:327 */     return this.bwtBlockLength;
/* 188:    */   }
/* 189:    */   
/* 190:    */   int checkCRC()
/* 191:    */   {
/* 192:336 */     int computedBlockCRC = this.crc.getCRC();
/* 193:337 */     if (this.blockCRC != computedBlockCRC) {
/* 194:338 */       throw new DecompressionException("block CRC error");
/* 195:    */     }
/* 196:340 */     return computedBlockCRC;
/* 197:    */   }
/* 198:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.compression.Bzip2BlockDecompressor
 * JD-Core Version:    0.7.0.1
 */