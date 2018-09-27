/*   1:    */ package io.netty.handler.codec.compression;
/*   2:    */ 
/*   3:    */ final class Bzip2MTFAndRLE2StageEncoder
/*   4:    */ {
/*   5:    */   private final int[] bwtBlock;
/*   6:    */   private final int bwtLength;
/*   7:    */   private final boolean[] bwtValuesPresent;
/*   8:    */   private final char[] mtfBlock;
/*   9:    */   private int mtfLength;
/*  10: 55 */   private final int[] mtfSymbolFrequencies = new int[258];
/*  11:    */   private int alphabetSize;
/*  12:    */   
/*  13:    */   Bzip2MTFAndRLE2StageEncoder(int[] bwtBlock, int bwtLength, boolean[] bwtValuesPresent)
/*  14:    */   {
/*  15: 69 */     this.bwtBlock = bwtBlock;
/*  16: 70 */     this.bwtLength = bwtLength;
/*  17: 71 */     this.bwtValuesPresent = bwtValuesPresent;
/*  18: 72 */     this.mtfBlock = new char[bwtLength + 1];
/*  19:    */   }
/*  20:    */   
/*  21:    */   void encode()
/*  22:    */   {
/*  23: 79 */     int bwtLength = this.bwtLength;
/*  24: 80 */     boolean[] bwtValuesPresent = this.bwtValuesPresent;
/*  25: 81 */     int[] bwtBlock = this.bwtBlock;
/*  26: 82 */     char[] mtfBlock = this.mtfBlock;
/*  27: 83 */     int[] mtfSymbolFrequencies = this.mtfSymbolFrequencies;
/*  28: 84 */     byte[] huffmanSymbolMap = new byte[256];
/*  29: 85 */     Bzip2MoveToFrontTable symbolMTF = new Bzip2MoveToFrontTable();
/*  30:    */     
/*  31: 87 */     int totalUniqueValues = 0;
/*  32: 88 */     for (int i = 0; i < huffmanSymbolMap.length; i++) {
/*  33: 89 */       if (bwtValuesPresent[i] != 0) {
/*  34: 90 */         huffmanSymbolMap[i] = ((byte)totalUniqueValues++);
/*  35:    */       }
/*  36:    */     }
/*  37: 93 */     int endOfBlockSymbol = totalUniqueValues + 1;
/*  38:    */     
/*  39: 95 */     int mtfIndex = 0;
/*  40: 96 */     int repeatCount = 0;
/*  41: 97 */     int totalRunAs = 0;
/*  42: 98 */     int totalRunBs = 0;
/*  43: 99 */     for (int i = 0; i < bwtLength; i++)
/*  44:    */     {
/*  45:101 */       int mtfPosition = symbolMTF.valueToFront(huffmanSymbolMap[(bwtBlock[i] & 0xFF)]);
/*  46:103 */       if (mtfPosition == 0)
/*  47:    */       {
/*  48:104 */         repeatCount++;
/*  49:    */       }
/*  50:    */       else
/*  51:    */       {
/*  52:106 */         if (repeatCount > 0)
/*  53:    */         {
/*  54:107 */           repeatCount--;
/*  55:    */           for (;;)
/*  56:    */           {
/*  57:109 */             if ((repeatCount & 0x1) == 0)
/*  58:    */             {
/*  59:110 */               mtfBlock[(mtfIndex++)] = '\000';
/*  60:111 */               totalRunAs++;
/*  61:    */             }
/*  62:    */             else
/*  63:    */             {
/*  64:113 */               mtfBlock[(mtfIndex++)] = '\001';
/*  65:114 */               totalRunBs++;
/*  66:    */             }
/*  67:117 */             if (repeatCount <= 1) {
/*  68:    */               break;
/*  69:    */             }
/*  70:120 */             repeatCount = repeatCount - 2 >>> 1;
/*  71:    */           }
/*  72:122 */           repeatCount = 0;
/*  73:    */         }
/*  74:124 */         mtfBlock[(mtfIndex++)] = ((char)(mtfPosition + 1));
/*  75:125 */         mtfSymbolFrequencies[(mtfPosition + 1)] += 1;
/*  76:    */       }
/*  77:    */     }
/*  78:129 */     if (repeatCount > 0)
/*  79:    */     {
/*  80:130 */       repeatCount--;
/*  81:    */       for (;;)
/*  82:    */       {
/*  83:132 */         if ((repeatCount & 0x1) == 0)
/*  84:    */         {
/*  85:133 */           mtfBlock[(mtfIndex++)] = '\000';
/*  86:134 */           totalRunAs++;
/*  87:    */         }
/*  88:    */         else
/*  89:    */         {
/*  90:136 */           mtfBlock[(mtfIndex++)] = '\001';
/*  91:137 */           totalRunBs++;
/*  92:    */         }
/*  93:140 */         if (repeatCount <= 1) {
/*  94:    */           break;
/*  95:    */         }
/*  96:143 */         repeatCount = repeatCount - 2 >>> 1;
/*  97:    */       }
/*  98:    */     }
/*  99:147 */     mtfBlock[mtfIndex] = ((char)endOfBlockSymbol);
/* 100:148 */     mtfSymbolFrequencies[endOfBlockSymbol] += 1;
/* 101:149 */     mtfSymbolFrequencies[0] += totalRunAs;
/* 102:150 */     mtfSymbolFrequencies[1] += totalRunBs;
/* 103:    */     
/* 104:152 */     this.mtfLength = (mtfIndex + 1);
/* 105:153 */     this.alphabetSize = (endOfBlockSymbol + 1);
/* 106:    */   }
/* 107:    */   
/* 108:    */   char[] mtfBlock()
/* 109:    */   {
/* 110:160 */     return this.mtfBlock;
/* 111:    */   }
/* 112:    */   
/* 113:    */   int mtfLength()
/* 114:    */   {
/* 115:167 */     return this.mtfLength;
/* 116:    */   }
/* 117:    */   
/* 118:    */   int mtfAlphabetSize()
/* 119:    */   {
/* 120:174 */     return this.alphabetSize;
/* 121:    */   }
/* 122:    */   
/* 123:    */   int[] mtfSymbolFrequencies()
/* 124:    */   {
/* 125:181 */     return this.mtfSymbolFrequencies;
/* 126:    */   }
/* 127:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.compression.Bzip2MTFAndRLE2StageEncoder
 * JD-Core Version:    0.7.0.1
 */