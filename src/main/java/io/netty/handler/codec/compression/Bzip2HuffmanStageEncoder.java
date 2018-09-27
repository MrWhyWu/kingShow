/*   1:    */ package io.netty.handler.codec.compression;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import java.util.Arrays;
/*   5:    */ 
/*   6:    */ final class Bzip2HuffmanStageEncoder
/*   7:    */ {
/*   8:    */   private static final int HUFFMAN_HIGH_SYMBOL_COST = 15;
/*   9:    */   private final Bzip2BitWriter writer;
/*  10:    */   private final char[] mtfBlock;
/*  11:    */   private final int mtfLength;
/*  12:    */   private final int mtfAlphabetSize;
/*  13:    */   private final int[] mtfSymbolFrequencies;
/*  14:    */   private final int[][] huffmanCodeLengths;
/*  15:    */   private final int[][] huffmanMergedCodeSymbols;
/*  16:    */   private final byte[] selectors;
/*  17:    */   
/*  18:    */   Bzip2HuffmanStageEncoder(Bzip2BitWriter writer, char[] mtfBlock, int mtfLength, int mtfAlphabetSize, int[] mtfSymbolFrequencies)
/*  19:    */   {
/*  20: 82 */     this.writer = writer;
/*  21: 83 */     this.mtfBlock = mtfBlock;
/*  22: 84 */     this.mtfLength = mtfLength;
/*  23: 85 */     this.mtfAlphabetSize = mtfAlphabetSize;
/*  24: 86 */     this.mtfSymbolFrequencies = mtfSymbolFrequencies;
/*  25:    */     
/*  26: 88 */     int totalTables = selectTableCount(mtfLength);
/*  27:    */     
/*  28: 90 */     this.huffmanCodeLengths = new int[totalTables][mtfAlphabetSize];
/*  29: 91 */     this.huffmanMergedCodeSymbols = new int[totalTables][mtfAlphabetSize];
/*  30: 92 */     this.selectors = new byte[(mtfLength + 50 - 1) / 50];
/*  31:    */   }
/*  32:    */   
/*  33:    */   private static int selectTableCount(int mtfLength)
/*  34:    */   {
/*  35:101 */     if (mtfLength >= 2400) {
/*  36:102 */       return 6;
/*  37:    */     }
/*  38:104 */     if (mtfLength >= 1200) {
/*  39:105 */       return 5;
/*  40:    */     }
/*  41:107 */     if (mtfLength >= 600) {
/*  42:108 */       return 4;
/*  43:    */     }
/*  44:110 */     if (mtfLength >= 200) {
/*  45:111 */       return 3;
/*  46:    */     }
/*  47:113 */     return 2;
/*  48:    */   }
/*  49:    */   
/*  50:    */   private static void generateHuffmanCodeLengths(int alphabetSize, int[] symbolFrequencies, int[] codeLengths)
/*  51:    */   {
/*  52:125 */     int[] mergedFrequenciesAndIndices = new int[alphabetSize];
/*  53:126 */     int[] sortedFrequencies = new int[alphabetSize];
/*  54:137 */     for (int i = 0; i < alphabetSize; i++) {
/*  55:138 */       mergedFrequenciesAndIndices[i] = (symbolFrequencies[i] << 9 | i);
/*  56:    */     }
/*  57:140 */     Arrays.sort(mergedFrequenciesAndIndices);
/*  58:141 */     for (int i = 0; i < alphabetSize; i++) {
/*  59:142 */       mergedFrequenciesAndIndices[i] >>>= 9;
/*  60:    */     }
/*  61:147 */     Bzip2HuffmanAllocator.allocateHuffmanCodeLengths(sortedFrequencies, 20);
/*  62:150 */     for (int i = 0; i < alphabetSize; i++) {
/*  63:151 */       codeLengths[(mergedFrequenciesAndIndices[i] & 0x1FF)] = sortedFrequencies[i];
/*  64:    */     }
/*  65:    */   }
/*  66:    */   
/*  67:    */   private void generateHuffmanOptimisationSeeds()
/*  68:    */   {
/*  69:162 */     int[][] huffmanCodeLengths = this.huffmanCodeLengths;
/*  70:163 */     int[] mtfSymbolFrequencies = this.mtfSymbolFrequencies;
/*  71:164 */     int mtfAlphabetSize = this.mtfAlphabetSize;
/*  72:    */     
/*  73:166 */     int totalTables = huffmanCodeLengths.length;
/*  74:    */     
/*  75:168 */     int remainingLength = this.mtfLength;
/*  76:169 */     int lowCostEnd = -1;
/*  77:171 */     for (int i = 0; i < totalTables; i++)
/*  78:    */     {
/*  79:173 */       int targetCumulativeFrequency = remainingLength / (totalTables - i);
/*  80:174 */       int lowCostStart = lowCostEnd + 1;
/*  81:175 */       int actualCumulativeFrequency = 0;
/*  82:177 */       while ((actualCumulativeFrequency < targetCumulativeFrequency) && (lowCostEnd < mtfAlphabetSize - 1)) {
/*  83:178 */         actualCumulativeFrequency += mtfSymbolFrequencies[(++lowCostEnd)];
/*  84:    */       }
/*  85:181 */       if ((lowCostEnd > lowCostStart) && (i != 0) && (i != totalTables - 1) && ((totalTables - i & 0x1) == 0)) {
/*  86:182 */         actualCumulativeFrequency -= mtfSymbolFrequencies[(lowCostEnd--)];
/*  87:    */       }
/*  88:185 */       int[] tableCodeLengths = huffmanCodeLengths[i];
/*  89:186 */       for (int j = 0; j < mtfAlphabetSize; j++) {
/*  90:187 */         if ((j < lowCostStart) || (j > lowCostEnd)) {
/*  91:188 */           tableCodeLengths[j] = 15;
/*  92:    */         }
/*  93:    */       }
/*  94:192 */       remainingLength -= actualCumulativeFrequency;
/*  95:    */     }
/*  96:    */   }
/*  97:    */   
/*  98:    */   private void optimiseSelectorsAndHuffmanTables(boolean storeSelectors)
/*  99:    */   {
/* 100:205 */     char[] mtfBlock = this.mtfBlock;
/* 101:206 */     byte[] selectors = this.selectors;
/* 102:207 */     int[][] huffmanCodeLengths = this.huffmanCodeLengths;
/* 103:208 */     int mtfLength = this.mtfLength;
/* 104:209 */     int mtfAlphabetSize = this.mtfAlphabetSize;
/* 105:    */     
/* 106:211 */     int totalTables = huffmanCodeLengths.length;
/* 107:212 */     int[][] tableFrequencies = new int[totalTables][mtfAlphabetSize];
/* 108:    */     
/* 109:214 */     int selectorIndex = 0;
/* 110:217 */     for (int groupStart = 0; groupStart < mtfLength;)
/* 111:    */     {
/* 112:219 */       int groupEnd = Math.min(groupStart + 50, mtfLength) - 1;
/* 113:    */       
/* 114:    */ 
/* 115:222 */       short[] cost = new short[totalTables];
/* 116:223 */       for (int i = groupStart; i <= groupEnd; i++)
/* 117:    */       {
/* 118:224 */         int value = mtfBlock[i];
/* 119:225 */         for (int j = 0; j < totalTables; j++)
/* 120:    */         {
/* 121:226 */           int tmp107_105 = j; short[] tmp107_103 = cost;tmp107_103[tmp107_105] = ((short)(tmp107_103[tmp107_105] + huffmanCodeLengths[j][value]));
/* 122:    */         }
/* 123:    */       }
/* 124:231 */       byte bestTable = 0;
/* 125:232 */       int bestCost = cost[0];
/* 126:233 */       for (byte i = 1; i < totalTables; i = (byte)(i + 1))
/* 127:    */       {
/* 128:234 */         int tableCost = cost[i];
/* 129:235 */         if (tableCost < bestCost)
/* 130:    */         {
/* 131:236 */           bestCost = tableCost;
/* 132:237 */           bestTable = i;
/* 133:    */         }
/* 134:    */       }
/* 135:242 */       int[] bestGroupFrequencies = tableFrequencies[bestTable];
/* 136:243 */       for (int i = groupStart; i <= groupEnd; i++) {
/* 137:244 */         bestGroupFrequencies[mtfBlock[i]] += 1;
/* 138:    */       }
/* 139:248 */       if (storeSelectors) {
/* 140:249 */         selectors[(selectorIndex++)] = bestTable;
/* 141:    */       }
/* 142:251 */       groupStart = groupEnd + 1;
/* 143:    */     }
/* 144:255 */     for (int i = 0; i < totalTables; i++) {
/* 145:256 */       generateHuffmanCodeLengths(mtfAlphabetSize, tableFrequencies[i], huffmanCodeLengths[i]);
/* 146:    */     }
/* 147:    */   }
/* 148:    */   
/* 149:    */   private void assignHuffmanCodeSymbols()
/* 150:    */   {
/* 151:264 */     int[][] huffmanMergedCodeSymbols = this.huffmanMergedCodeSymbols;
/* 152:265 */     int[][] huffmanCodeLengths = this.huffmanCodeLengths;
/* 153:266 */     int mtfAlphabetSize = this.mtfAlphabetSize;
/* 154:    */     
/* 155:268 */     int totalTables = huffmanCodeLengths.length;
/* 156:270 */     for (int i = 0; i < totalTables; i++)
/* 157:    */     {
/* 158:271 */       int[] tableLengths = huffmanCodeLengths[i];
/* 159:    */       
/* 160:273 */       int minimumLength = 32;
/* 161:274 */       int maximumLength = 0;
/* 162:275 */       for (int j = 0; j < mtfAlphabetSize; j++)
/* 163:    */       {
/* 164:276 */         int length = tableLengths[j];
/* 165:277 */         if (length > maximumLength) {
/* 166:278 */           maximumLength = length;
/* 167:    */         }
/* 168:280 */         if (length < minimumLength) {
/* 169:281 */           minimumLength = length;
/* 170:    */         }
/* 171:    */       }
/* 172:285 */       int code = 0;
/* 173:286 */       for (int j = minimumLength; j <= maximumLength; j++)
/* 174:    */       {
/* 175:287 */         for (int k = 0; k < mtfAlphabetSize; k++) {
/* 176:288 */           if ((huffmanCodeLengths[i][k] & 0xFF) == j)
/* 177:    */           {
/* 178:289 */             huffmanMergedCodeSymbols[i][k] = (j << 24 | code);
/* 179:290 */             code++;
/* 180:    */           }
/* 181:    */         }
/* 182:293 */         code <<= 1;
/* 183:    */       }
/* 184:    */     }
/* 185:    */   }
/* 186:    */   
/* 187:    */   private void writeSelectorsAndHuffmanTables(ByteBuf out)
/* 188:    */   {
/* 189:302 */     Bzip2BitWriter writer = this.writer;
/* 190:303 */     byte[] selectors = this.selectors;
/* 191:304 */     int totalSelectors = selectors.length;
/* 192:305 */     int[][] huffmanCodeLengths = this.huffmanCodeLengths;
/* 193:306 */     int totalTables = huffmanCodeLengths.length;
/* 194:307 */     int mtfAlphabetSize = this.mtfAlphabetSize;
/* 195:    */     
/* 196:309 */     writer.writeBits(out, 3, totalTables);
/* 197:310 */     writer.writeBits(out, 15, totalSelectors);
/* 198:    */     
/* 199:    */ 
/* 200:313 */     Bzip2MoveToFrontTable selectorMTF = new Bzip2MoveToFrontTable();
/* 201:314 */     for (byte selector : selectors) {
/* 202:315 */       writer.writeUnary(out, selectorMTF.valueToFront(selector));
/* 203:    */     }
/* 204:319 */     for (int[] tableLengths : huffmanCodeLengths)
/* 205:    */     {
/* 206:320 */       int currentLength = tableLengths[0];
/* 207:    */       
/* 208:322 */       writer.writeBits(out, 5, currentLength);
/* 209:324 */       for (int j = 0; j < mtfAlphabetSize; j++)
/* 210:    */       {
/* 211:325 */         int codeLength = tableLengths[j];
/* 212:326 */         int value = currentLength < codeLength ? 2 : 3;
/* 213:327 */         int delta = Math.abs(codeLength - currentLength);
/* 214:328 */         while (delta-- > 0) {
/* 215:329 */           writer.writeBits(out, 2, value);
/* 216:    */         }
/* 217:331 */         writer.writeBoolean(out, false);
/* 218:332 */         currentLength = codeLength;
/* 219:    */       }
/* 220:    */     }
/* 221:    */   }
/* 222:    */   
/* 223:    */   private void writeBlockData(ByteBuf out)
/* 224:    */   {
/* 225:341 */     Bzip2BitWriter writer = this.writer;
/* 226:342 */     int[][] huffmanMergedCodeSymbols = this.huffmanMergedCodeSymbols;
/* 227:343 */     byte[] selectors = this.selectors;
/* 228:344 */     char[] mtf = this.mtfBlock;
/* 229:345 */     int mtfLength = this.mtfLength;
/* 230:    */     
/* 231:347 */     int selectorIndex = 0;
/* 232:348 */     for (int mtfIndex = 0; mtfIndex < mtfLength;)
/* 233:    */     {
/* 234:349 */       int groupEnd = Math.min(mtfIndex + 50, mtfLength) - 1;
/* 235:350 */       int[] tableMergedCodeSymbols = huffmanMergedCodeSymbols[selectors[(selectorIndex++)]];
/* 236:352 */       while (mtfIndex <= groupEnd)
/* 237:    */       {
/* 238:353 */         int mergedCodeSymbol = tableMergedCodeSymbols[mtf[(mtfIndex++)]];
/* 239:354 */         writer.writeBits(out, mergedCodeSymbol >>> 24, mergedCodeSymbol);
/* 240:    */       }
/* 241:    */     }
/* 242:    */   }
/* 243:    */   
/* 244:    */   void encode(ByteBuf out)
/* 245:    */   {
/* 246:364 */     generateHuffmanOptimisationSeeds();
/* 247:365 */     for (int i = 3; i >= 0; i--) {
/* 248:366 */       optimiseSelectorsAndHuffmanTables(i == 0);
/* 249:    */     }
/* 250:368 */     assignHuffmanCodeSymbols();
/* 251:    */     
/* 252:    */ 
/* 253:371 */     writeSelectorsAndHuffmanTables(out);
/* 254:372 */     writeBlockData(out);
/* 255:    */   }
/* 256:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.compression.Bzip2HuffmanStageEncoder
 * JD-Core Version:    0.7.0.1
 */