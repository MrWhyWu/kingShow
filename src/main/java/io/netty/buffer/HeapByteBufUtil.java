/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ final class HeapByteBufUtil
/*   4:    */ {
/*   5:    */   static byte getByte(byte[] memory, int index)
/*   6:    */   {
/*   7: 24 */     return memory[index];
/*   8:    */   }
/*   9:    */   
/*  10:    */   static short getShort(byte[] memory, int index)
/*  11:    */   {
/*  12: 28 */     return (short)(memory[index] << 8 | memory[(index + 1)] & 0xFF);
/*  13:    */   }
/*  14:    */   
/*  15:    */   static short getShortLE(byte[] memory, int index)
/*  16:    */   {
/*  17: 32 */     return (short)(memory[index] & 0xFF | memory[(index + 1)] << 8);
/*  18:    */   }
/*  19:    */   
/*  20:    */   static int getUnsignedMedium(byte[] memory, int index)
/*  21:    */   {
/*  22: 36 */     return (memory[index] & 0xFF) << 16 | (memory[(index + 1)] & 0xFF) << 8 | memory[(index + 2)] & 0xFF;
/*  23:    */   }
/*  24:    */   
/*  25:    */   static int getUnsignedMediumLE(byte[] memory, int index)
/*  26:    */   {
/*  27: 42 */     return memory[index] & 0xFF | (memory[(index + 1)] & 0xFF) << 8 | (memory[(index + 2)] & 0xFF) << 16;
/*  28:    */   }
/*  29:    */   
/*  30:    */   static int getInt(byte[] memory, int index)
/*  31:    */   {
/*  32: 48 */     return (memory[index] & 0xFF) << 24 | (memory[(index + 1)] & 0xFF) << 16 | (memory[(index + 2)] & 0xFF) << 8 | memory[(index + 3)] & 0xFF;
/*  33:    */   }
/*  34:    */   
/*  35:    */   static int getIntLE(byte[] memory, int index)
/*  36:    */   {
/*  37: 55 */     return memory[index] & 0xFF | (memory[(index + 1)] & 0xFF) << 8 | (memory[(index + 2)] & 0xFF) << 16 | (memory[(index + 3)] & 0xFF) << 24;
/*  38:    */   }
/*  39:    */   
/*  40:    */   static long getLong(byte[] memory, int index)
/*  41:    */   {
/*  42: 62 */     return (memory[index] & 0xFF) << 56 | (memory[(index + 1)] & 0xFF) << 48 | (memory[(index + 2)] & 0xFF) << 40 | (memory[(index + 3)] & 0xFF) << 32 | (memory[(index + 4)] & 0xFF) << 24 | (memory[(index + 5)] & 0xFF) << 16 | (memory[(index + 6)] & 0xFF) << 8 | memory[(index + 7)] & 0xFF;
/*  43:    */   }
/*  44:    */   
/*  45:    */   static long getLongLE(byte[] memory, int index)
/*  46:    */   {
/*  47: 73 */     return memory[index] & 0xFF | (memory[(index + 1)] & 0xFF) << 8 | (memory[(index + 2)] & 0xFF) << 16 | (memory[(index + 3)] & 0xFF) << 24 | (memory[(index + 4)] & 0xFF) << 32 | (memory[(index + 5)] & 0xFF) << 40 | (memory[(index + 6)] & 0xFF) << 48 | (memory[(index + 7)] & 0xFF) << 56;
/*  48:    */   }
/*  49:    */   
/*  50:    */   static void setByte(byte[] memory, int index, int value)
/*  51:    */   {
/*  52: 84 */     memory[index] = ((byte)value);
/*  53:    */   }
/*  54:    */   
/*  55:    */   static void setShort(byte[] memory, int index, int value)
/*  56:    */   {
/*  57: 88 */     memory[index] = ((byte)(value >>> 8));
/*  58: 89 */     memory[(index + 1)] = ((byte)value);
/*  59:    */   }
/*  60:    */   
/*  61:    */   static void setShortLE(byte[] memory, int index, int value)
/*  62:    */   {
/*  63: 93 */     memory[index] = ((byte)value);
/*  64: 94 */     memory[(index + 1)] = ((byte)(value >>> 8));
/*  65:    */   }
/*  66:    */   
/*  67:    */   static void setMedium(byte[] memory, int index, int value)
/*  68:    */   {
/*  69: 98 */     memory[index] = ((byte)(value >>> 16));
/*  70: 99 */     memory[(index + 1)] = ((byte)(value >>> 8));
/*  71:100 */     memory[(index + 2)] = ((byte)value);
/*  72:    */   }
/*  73:    */   
/*  74:    */   static void setMediumLE(byte[] memory, int index, int value)
/*  75:    */   {
/*  76:104 */     memory[index] = ((byte)value);
/*  77:105 */     memory[(index + 1)] = ((byte)(value >>> 8));
/*  78:106 */     memory[(index + 2)] = ((byte)(value >>> 16));
/*  79:    */   }
/*  80:    */   
/*  81:    */   static void setInt(byte[] memory, int index, int value)
/*  82:    */   {
/*  83:110 */     memory[index] = ((byte)(value >>> 24));
/*  84:111 */     memory[(index + 1)] = ((byte)(value >>> 16));
/*  85:112 */     memory[(index + 2)] = ((byte)(value >>> 8));
/*  86:113 */     memory[(index + 3)] = ((byte)value);
/*  87:    */   }
/*  88:    */   
/*  89:    */   static void setIntLE(byte[] memory, int index, int value)
/*  90:    */   {
/*  91:117 */     memory[index] = ((byte)value);
/*  92:118 */     memory[(index + 1)] = ((byte)(value >>> 8));
/*  93:119 */     memory[(index + 2)] = ((byte)(value >>> 16));
/*  94:120 */     memory[(index + 3)] = ((byte)(value >>> 24));
/*  95:    */   }
/*  96:    */   
/*  97:    */   static void setLong(byte[] memory, int index, long value)
/*  98:    */   {
/*  99:124 */     memory[index] = ((byte)(int)(value >>> 56));
/* 100:125 */     memory[(index + 1)] = ((byte)(int)(value >>> 48));
/* 101:126 */     memory[(index + 2)] = ((byte)(int)(value >>> 40));
/* 102:127 */     memory[(index + 3)] = ((byte)(int)(value >>> 32));
/* 103:128 */     memory[(index + 4)] = ((byte)(int)(value >>> 24));
/* 104:129 */     memory[(index + 5)] = ((byte)(int)(value >>> 16));
/* 105:130 */     memory[(index + 6)] = ((byte)(int)(value >>> 8));
/* 106:131 */     memory[(index + 7)] = ((byte)(int)value);
/* 107:    */   }
/* 108:    */   
/* 109:    */   static void setLongLE(byte[] memory, int index, long value)
/* 110:    */   {
/* 111:135 */     memory[index] = ((byte)(int)value);
/* 112:136 */     memory[(index + 1)] = ((byte)(int)(value >>> 8));
/* 113:137 */     memory[(index + 2)] = ((byte)(int)(value >>> 16));
/* 114:138 */     memory[(index + 3)] = ((byte)(int)(value >>> 24));
/* 115:139 */     memory[(index + 4)] = ((byte)(int)(value >>> 32));
/* 116:140 */     memory[(index + 5)] = ((byte)(int)(value >>> 40));
/* 117:141 */     memory[(index + 6)] = ((byte)(int)(value >>> 48));
/* 118:142 */     memory[(index + 7)] = ((byte)(int)(value >>> 56));
/* 119:    */   }
/* 120:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.HeapByteBufUtil
 * JD-Core Version:    0.7.0.1
 */