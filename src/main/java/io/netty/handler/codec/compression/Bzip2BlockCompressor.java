/*   1:    */ package io.netty.handler.codec.compression;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.util.ByteProcessor;
/*   5:    */ 
/*   6:    */ final class Bzip2BlockCompressor
/*   7:    */ {
/*   8: 36 */   private final ByteProcessor writeProcessor = new ByteProcessor()
/*   9:    */   {
/*  10:    */     public boolean process(byte value)
/*  11:    */       throws Exception
/*  12:    */     {
/*  13: 39 */       return Bzip2BlockCompressor.this.write(value);
/*  14:    */     }
/*  15:    */   };
/*  16:    */   private final Bzip2BitWriter writer;
/*  17: 51 */   private final Crc32 crc = new Crc32();
/*  18:    */   private final byte[] block;
/*  19:    */   private int blockLength;
/*  20:    */   private final int blockLengthLimit;
/*  21: 72 */   private final boolean[] blockValuesPresent = new boolean[256];
/*  22:    */   private final int[] bwtBlock;
/*  23: 82 */   private int rleCurrentValue = -1;
/*  24:    */   private int rleLength;
/*  25:    */   
/*  26:    */   Bzip2BlockCompressor(Bzip2BitWriter writer, int blockSize)
/*  27:    */   {
/*  28: 95 */     this.writer = writer;
/*  29:    */     
/*  30:    */ 
/*  31: 98 */     this.block = new byte[blockSize + 1];
/*  32: 99 */     this.bwtBlock = new int[blockSize + 1];
/*  33:100 */     this.blockLengthLimit = (blockSize - 6);
/*  34:    */   }
/*  35:    */   
/*  36:    */   private void writeSymbolMap(ByteBuf out)
/*  37:    */   {
/*  38:107 */     Bzip2BitWriter writer = this.writer;
/*  39:    */     
/*  40:109 */     boolean[] blockValuesPresent = this.blockValuesPresent;
/*  41:110 */     boolean[] condensedInUse = new boolean[16];
/*  42:    */     int j;
/*  43:    */     int k;
/*  44:112 */     for (int i = 0; i < condensedInUse.length; i++)
/*  45:    */     {
/*  46:113 */       j = 0;
/*  47:113 */       for (k = i << 4; j < 16; k++)
/*  48:    */       {
/*  49:114 */         if (blockValuesPresent[k] != 0) {
/*  50:115 */           condensedInUse[i] = true;
/*  51:    */         }
/*  52:113 */         j++;
/*  53:    */       }
/*  54:    */     }
/*  55:120 */     for (boolean isCondensedInUse : condensedInUse) {
/*  56:121 */       writer.writeBoolean(out, isCondensedInUse);
/*  57:    */     }
/*  58:124 */     for (int i = 0; i < condensedInUse.length; i++) {
/*  59:125 */       if (condensedInUse[i] != 0)
/*  60:    */       {
/*  61:126 */         int j = 0;
/*  62:126 */         for (int k = i << 4; j < 16; k++)
/*  63:    */         {
/*  64:127 */           writer.writeBoolean(out, blockValuesPresent[k]);j++;
/*  65:    */         }
/*  66:    */       }
/*  67:    */     }
/*  68:    */   }
/*  69:    */   
/*  70:    */   private void writeRun(int value, int runLength)
/*  71:    */   {
/*  72:139 */     int blockLength = this.blockLength;
/*  73:140 */     byte[] block = this.block;
/*  74:    */     
/*  75:142 */     this.blockValuesPresent[value] = true;
/*  76:143 */     this.crc.updateCRC(value, runLength);
/*  77:    */     
/*  78:145 */     byte byteValue = (byte)value;
/*  79:146 */     switch (runLength)
/*  80:    */     {
/*  81:    */     case 1: 
/*  82:148 */       block[blockLength] = byteValue;
/*  83:149 */       this.blockLength = (blockLength + 1);
/*  84:150 */       break;
/*  85:    */     case 2: 
/*  86:152 */       block[blockLength] = byteValue;
/*  87:153 */       block[(blockLength + 1)] = byteValue;
/*  88:154 */       this.blockLength = (blockLength + 2);
/*  89:155 */       break;
/*  90:    */     case 3: 
/*  91:157 */       block[blockLength] = byteValue;
/*  92:158 */       block[(blockLength + 1)] = byteValue;
/*  93:159 */       block[(blockLength + 2)] = byteValue;
/*  94:160 */       this.blockLength = (blockLength + 3);
/*  95:161 */       break;
/*  96:    */     default: 
/*  97:163 */       runLength -= 4;
/*  98:164 */       this.blockValuesPresent[runLength] = true;
/*  99:165 */       block[blockLength] = byteValue;
/* 100:166 */       block[(blockLength + 1)] = byteValue;
/* 101:167 */       block[(blockLength + 2)] = byteValue;
/* 102:168 */       block[(blockLength + 3)] = byteValue;
/* 103:169 */       block[(blockLength + 4)] = ((byte)runLength);
/* 104:170 */       this.blockLength = (blockLength + 5);
/* 105:    */     }
/* 106:    */   }
/* 107:    */   
/* 108:    */   boolean write(int value)
/* 109:    */   {
/* 110:181 */     if (this.blockLength > this.blockLengthLimit) {
/* 111:182 */       return false;
/* 112:    */     }
/* 113:184 */     int rleCurrentValue = this.rleCurrentValue;
/* 114:185 */     int rleLength = this.rleLength;
/* 115:187 */     if (rleLength == 0)
/* 116:    */     {
/* 117:188 */       this.rleCurrentValue = value;
/* 118:189 */       this.rleLength = 1;
/* 119:    */     }
/* 120:190 */     else if (rleCurrentValue != value)
/* 121:    */     {
/* 122:192 */       writeRun(rleCurrentValue & 0xFF, rleLength);
/* 123:193 */       this.rleCurrentValue = value;
/* 124:194 */       this.rleLength = 1;
/* 125:    */     }
/* 126:196 */     else if (rleLength == 254)
/* 127:    */     {
/* 128:197 */       writeRun(rleCurrentValue & 0xFF, 255);
/* 129:198 */       this.rleLength = 0;
/* 130:    */     }
/* 131:    */     else
/* 132:    */     {
/* 133:200 */       this.rleLength = (rleLength + 1);
/* 134:    */     }
/* 135:203 */     return true;
/* 136:    */   }
/* 137:    */   
/* 138:    */   int write(ByteBuf buffer, int offset, int length)
/* 139:    */   {
/* 140:215 */     int index = buffer.forEachByte(offset, length, this.writeProcessor);
/* 141:216 */     return index == -1 ? length : index - offset;
/* 142:    */   }
/* 143:    */   
/* 144:    */   void close(ByteBuf out)
/* 145:    */   {
/* 146:224 */     if (this.rleLength > 0) {
/* 147:225 */       writeRun(this.rleCurrentValue & 0xFF, this.rleLength);
/* 148:    */     }
/* 149:229 */     this.block[this.blockLength] = this.block[0];
/* 150:    */     
/* 151:    */ 
/* 152:232 */     Bzip2DivSufSort divSufSort = new Bzip2DivSufSort(this.block, this.bwtBlock, this.blockLength);
/* 153:233 */     int bwtStartPointer = divSufSort.bwt();
/* 154:    */     
/* 155:235 */     Bzip2BitWriter writer = this.writer;
/* 156:    */     
/* 157:    */ 
/* 158:238 */     writer.writeBits(out, 24, 3227993L);
/* 159:239 */     writer.writeBits(out, 24, 2511705L);
/* 160:240 */     writer.writeInt(out, this.crc.getCRC());
/* 161:241 */     writer.writeBoolean(out, false);
/* 162:242 */     writer.writeBits(out, 24, bwtStartPointer);
/* 163:    */     
/* 164:    */ 
/* 165:245 */     writeSymbolMap(out);
/* 166:    */     
/* 167:    */ 
/* 168:248 */     Bzip2MTFAndRLE2StageEncoder mtfEncoder = new Bzip2MTFAndRLE2StageEncoder(this.bwtBlock, this.blockLength, this.blockValuesPresent);
/* 169:    */     
/* 170:250 */     mtfEncoder.encode();
/* 171:    */     
/* 172:    */ 
/* 173:    */ 
/* 174:    */ 
/* 175:    */ 
/* 176:    */ 
/* 177:257 */     Bzip2HuffmanStageEncoder huffmanEncoder = new Bzip2HuffmanStageEncoder(writer, mtfEncoder.mtfBlock(), mtfEncoder.mtfLength(), mtfEncoder.mtfAlphabetSize(), mtfEncoder.mtfSymbolFrequencies());
/* 178:258 */     huffmanEncoder.encode(out);
/* 179:    */   }
/* 180:    */   
/* 181:    */   int availableSize()
/* 182:    */   {
/* 183:266 */     if (this.blockLength == 0) {
/* 184:267 */       return this.blockLengthLimit + 2;
/* 185:    */     }
/* 186:269 */     return this.blockLengthLimit - this.blockLength + 1;
/* 187:    */   }
/* 188:    */   
/* 189:    */   boolean isFull()
/* 190:    */   {
/* 191:277 */     return this.blockLength > this.blockLengthLimit;
/* 192:    */   }
/* 193:    */   
/* 194:    */   boolean isEmpty()
/* 195:    */   {
/* 196:285 */     return (this.blockLength == 0) && (this.rleLength == 0);
/* 197:    */   }
/* 198:    */   
/* 199:    */   int crc()
/* 200:    */   {
/* 201:293 */     return this.crc.getCRC();
/* 202:    */   }
/* 203:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.compression.Bzip2BlockCompressor
 * JD-Core Version:    0.7.0.1
 */