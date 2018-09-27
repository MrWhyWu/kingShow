/*   1:    */ package io.netty.handler.codec.compression;
/*   2:    */ 
/*   3:    */ final class Bzip2HuffmanStageDecoder
/*   4:    */ {
/*   5:    */   private final Bzip2BitReader reader;
/*   6:    */   byte[] selectors;
/*   7:    */   private final int[] minimumLengths;
/*   8:    */   private final int[][] codeBases;
/*   9:    */   private final int[][] codeLimits;
/*  10:    */   private final int[][] codeSymbols;
/*  11:    */   private int currentTable;
/*  12: 64 */   private int groupIndex = -1;
/*  13: 69 */   private int groupPosition = -1;
/*  14:    */   final int totalTables;
/*  15:    */   final int alphabetSize;
/*  16: 84 */   final Bzip2MoveToFrontTable tableMTF = new Bzip2MoveToFrontTable();
/*  17:    */   int currentSelector;
/*  18:    */   final byte[][] tableCodeLengths;
/*  19:    */   int currentGroup;
/*  20: 96 */   int currentLength = -1;
/*  21:    */   int currentAlpha;
/*  22:    */   boolean modifyLength;
/*  23:    */   
/*  24:    */   Bzip2HuffmanStageDecoder(Bzip2BitReader reader, int totalTables, int alphabetSize)
/*  25:    */   {
/*  26:101 */     this.reader = reader;
/*  27:102 */     this.totalTables = totalTables;
/*  28:103 */     this.alphabetSize = alphabetSize;
/*  29:    */     
/*  30:105 */     this.minimumLengths = new int[totalTables];
/*  31:106 */     this.codeBases = new int[totalTables][25];
/*  32:107 */     this.codeLimits = new int[totalTables][24];
/*  33:108 */     this.codeSymbols = new int[totalTables][258];
/*  34:109 */     this.tableCodeLengths = new byte[totalTables][258];
/*  35:    */   }
/*  36:    */   
/*  37:    */   void createHuffmanDecodingTables()
/*  38:    */   {
/*  39:116 */     int alphabetSize = this.alphabetSize;
/*  40:118 */     for (int table = 0; table < this.tableCodeLengths.length; table++)
/*  41:    */     {
/*  42:119 */       int[] tableBases = this.codeBases[table];
/*  43:120 */       int[] tableLimits = this.codeLimits[table];
/*  44:121 */       int[] tableSymbols = this.codeSymbols[table];
/*  45:122 */       byte[] codeLengths = this.tableCodeLengths[table];
/*  46:    */       
/*  47:124 */       int minimumLength = 23;
/*  48:125 */       int maximumLength = 0;
/*  49:128 */       for (int i = 0; i < alphabetSize; i++)
/*  50:    */       {
/*  51:129 */         byte currLength = codeLengths[i];
/*  52:130 */         maximumLength = Math.max(currLength, maximumLength);
/*  53:131 */         minimumLength = Math.min(currLength, minimumLength);
/*  54:    */       }
/*  55:133 */       this.minimumLengths[table] = minimumLength;
/*  56:136 */       for (int i = 0; i < alphabetSize; i++) {
/*  57:137 */         tableBases[(codeLengths[i] + 1)] += 1;
/*  58:    */       }
/*  59:139 */       int i = 1;
/*  60:139 */       for (int b = tableBases[0]; i < 25; i++)
/*  61:    */       {
/*  62:140 */         b += tableBases[i];
/*  63:141 */         tableBases[i] = b;
/*  64:    */       }
/*  65:146 */       int i = minimumLength;
/*  66:146 */       for (int code = 0; i <= maximumLength; i++)
/*  67:    */       {
/*  68:147 */         int base = code;
/*  69:148 */         code += tableBases[(i + 1)] - tableBases[i];
/*  70:149 */         tableBases[i] = (base - tableBases[i]);
/*  71:150 */         tableLimits[i] = (code - 1);
/*  72:151 */         code <<= 1;
/*  73:    */       }
/*  74:155 */       int bitLength = minimumLength;
/*  75:155 */       for (int codeIndex = 0; bitLength <= maximumLength; bitLength++) {
/*  76:156 */         for (int symbol = 0; symbol < alphabetSize; symbol++) {
/*  77:157 */           if (codeLengths[symbol] == bitLength) {
/*  78:158 */             tableSymbols[(codeIndex++)] = symbol;
/*  79:    */           }
/*  80:    */         }
/*  81:    */       }
/*  82:    */     }
/*  83:164 */     this.currentTable = this.selectors[0];
/*  84:    */   }
/*  85:    */   
/*  86:    */   int nextSymbol()
/*  87:    */   {
/*  88:173 */     if (++this.groupPosition % 50 == 0)
/*  89:    */     {
/*  90:174 */       this.groupIndex += 1;
/*  91:175 */       if (this.groupIndex == this.selectors.length) {
/*  92:176 */         throw new DecompressionException("error decoding block");
/*  93:    */       }
/*  94:178 */       this.currentTable = (this.selectors[this.groupIndex] & 0xFF);
/*  95:    */     }
/*  96:181 */     Bzip2BitReader reader = this.reader;
/*  97:182 */     int currentTable = this.currentTable;
/*  98:183 */     int[] tableLimits = this.codeLimits[currentTable];
/*  99:184 */     int[] tableBases = this.codeBases[currentTable];
/* 100:185 */     int[] tableSymbols = this.codeSymbols[currentTable];
/* 101:186 */     int codeLength = this.minimumLengths[currentTable];
/* 102:    */     
/* 103:    */ 
/* 104:    */ 
/* 105:190 */     int codeBits = reader.readBits(codeLength);
/* 106:191 */     for (; codeLength <= 23; codeLength++)
/* 107:    */     {
/* 108:192 */       if (codeBits <= tableLimits[codeLength]) {
/* 109:194 */         return tableSymbols[(codeBits - tableBases[codeLength])];
/* 110:    */       }
/* 111:196 */       codeBits = codeBits << 1 | reader.readBits(1);
/* 112:    */     }
/* 113:199 */     throw new DecompressionException("a valid code was not recognised");
/* 114:    */   }
/* 115:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.compression.Bzip2HuffmanStageDecoder
 * JD-Core Version:    0.7.0.1
 */