/*   1:    */ package io.netty.handler.codec.compression;
/*   2:    */ 
/*   3:    */ final class Bzip2HuffmanAllocator
/*   4:    */ {
/*   5:    */   private static int first(int[] array, int i, int nodesToMove)
/*   6:    */   {
/*   7: 34 */     int length = array.length;
/*   8: 35 */     int limit = i;
/*   9: 36 */     int k = array.length - 2;
/*  10: 38 */     while ((i >= nodesToMove) && (array[i] % length > limit))
/*  11:    */     {
/*  12: 39 */       k = i;
/*  13: 40 */       i -= limit - i + 1;
/*  14:    */     }
/*  15: 42 */     i = Math.max(nodesToMove - 1, i);
/*  16: 44 */     while (k > i + 1)
/*  17:    */     {
/*  18: 45 */       int temp = i + k >>> 1;
/*  19: 46 */       if (array[temp] % length > limit) {
/*  20: 47 */         k = temp;
/*  21:    */       } else {
/*  22: 49 */         i = temp;
/*  23:    */       }
/*  24:    */     }
/*  25: 52 */     return k;
/*  26:    */   }
/*  27:    */   
/*  28:    */   private static void setExtendedParentPointers(int[] array)
/*  29:    */   {
/*  30: 60 */     int length = array.length;
/*  31: 61 */     array[0] += array[1];
/*  32:    */     
/*  33: 63 */     int headNode = 0;int tailNode = 1;
/*  34: 63 */     for (int topNode = 2; tailNode < length - 1; tailNode++)
/*  35:    */     {
/*  36:    */       int temp;
/*  37: 65 */       if ((topNode >= length) || (array[headNode] < array[topNode]))
/*  38:    */       {
/*  39: 66 */         int temp = array[headNode];
/*  40: 67 */         array[(headNode++)] = tailNode;
/*  41:    */       }
/*  42:    */       else
/*  43:    */       {
/*  44: 69 */         temp = array[(topNode++)];
/*  45:    */       }
/*  46: 72 */       if ((topNode >= length) || ((headNode < tailNode) && (array[headNode] < array[topNode])))
/*  47:    */       {
/*  48: 73 */         temp += array[headNode];
/*  49: 74 */         array[(headNode++)] = (tailNode + length);
/*  50:    */       }
/*  51:    */       else
/*  52:    */       {
/*  53: 76 */         temp += array[(topNode++)];
/*  54:    */       }
/*  55: 78 */       array[tailNode] = temp;
/*  56:    */     }
/*  57:    */   }
/*  58:    */   
/*  59:    */   private static int findNodesToRelocate(int[] array, int maximumLength)
/*  60:    */   {
/*  61: 89 */     int currentNode = array.length - 2;
/*  62: 90 */     for (int currentDepth = 1; (currentDepth < maximumLength - 1) && (currentNode > 1); currentDepth++) {
/*  63: 91 */       currentNode = first(array, currentNode - 1, 0);
/*  64:    */     }
/*  65: 93 */     return currentNode;
/*  66:    */   }
/*  67:    */   
/*  68:    */   private static void allocateNodeLengths(int[] array)
/*  69:    */   {
/*  70:101 */     int firstNode = array.length - 2;
/*  71:102 */     int nextNode = array.length - 1;
/*  72:    */     
/*  73:104 */     int currentDepth = 1;
/*  74:104 */     for (int availableNodes = 2; availableNodes > 0; currentDepth++)
/*  75:    */     {
/*  76:105 */       int lastNode = firstNode;
/*  77:106 */       firstNode = first(array, lastNode - 1, 0);
/*  78:108 */       for (int i = availableNodes - (lastNode - firstNode); i > 0; i--) {
/*  79:109 */         array[(nextNode--)] = currentDepth;
/*  80:    */       }
/*  81:112 */       availableNodes = lastNode - firstNode << 1;
/*  82:    */     }
/*  83:    */   }
/*  84:    */   
/*  85:    */   private static void allocateNodeLengthsWithRelocation(int[] array, int nodesToMove, int insertDepth)
/*  86:    */   {
/*  87:124 */     int firstNode = array.length - 2;
/*  88:125 */     int nextNode = array.length - 1;
/*  89:126 */     int currentDepth = insertDepth == 1 ? 2 : 1;
/*  90:127 */     int nodesLeftToMove = insertDepth == 1 ? nodesToMove - 2 : nodesToMove;
/*  91:129 */     for (int availableNodes = currentDepth << 1; availableNodes > 0; currentDepth++)
/*  92:    */     {
/*  93:130 */       int lastNode = firstNode;
/*  94:131 */       firstNode = firstNode <= nodesToMove ? firstNode : first(array, lastNode - 1, nodesToMove);
/*  95:    */       
/*  96:133 */       int offset = 0;
/*  97:134 */       if (currentDepth >= insertDepth)
/*  98:    */       {
/*  99:135 */         offset = Math.min(nodesLeftToMove, 1 << currentDepth - insertDepth);
/* 100:    */       }
/* 101:136 */       else if (currentDepth == insertDepth - 1)
/* 102:    */       {
/* 103:137 */         offset = 1;
/* 104:138 */         if (array[firstNode] == lastNode) {
/* 105:139 */           firstNode++;
/* 106:    */         }
/* 107:    */       }
/* 108:143 */       for (int i = availableNodes - (lastNode - firstNode + offset); i > 0; i--) {
/* 109:144 */         array[(nextNode--)] = currentDepth;
/* 110:    */       }
/* 111:147 */       nodesLeftToMove -= offset;
/* 112:148 */       availableNodes = lastNode - firstNode + offset << 1;
/* 113:    */     }
/* 114:    */   }
/* 115:    */   
/* 116:    */   static void allocateHuffmanCodeLengths(int[] array, int maximumLength)
/* 117:    */   {
/* 118:159 */     switch (array.length)
/* 119:    */     {
/* 120:    */     case 2: 
/* 121:161 */       array[1] = 1;
/* 122:    */     case 1: 
/* 123:164 */       array[0] = 1;
/* 124:165 */       return;
/* 125:    */     }
/* 126:169 */     setExtendedParentPointers(array);
/* 127:    */     
/* 128:    */ 
/* 129:172 */     int nodesToRelocate = findNodesToRelocate(array, maximumLength);
/* 130:175 */     if (array[0] % array.length >= nodesToRelocate)
/* 131:    */     {
/* 132:176 */       allocateNodeLengths(array);
/* 133:    */     }
/* 134:    */     else
/* 135:    */     {
/* 136:178 */       int insertDepth = maximumLength - (32 - Integer.numberOfLeadingZeros(nodesToRelocate - 1));
/* 137:179 */       allocateNodeLengthsWithRelocation(array, nodesToRelocate, insertDepth);
/* 138:    */     }
/* 139:    */   }
/* 140:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.compression.Bzip2HuffmanAllocator
 * JD-Core Version:    0.7.0.1
 */