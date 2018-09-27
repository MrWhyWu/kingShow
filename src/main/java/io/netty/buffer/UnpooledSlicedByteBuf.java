/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ class UnpooledSlicedByteBuf
/*   4:    */   extends AbstractUnpooledSlicedByteBuf
/*   5:    */ {
/*   6:    */   UnpooledSlicedByteBuf(AbstractByteBuf buffer, int index, int length)
/*   7:    */   {
/*   8: 24 */     super(buffer, index, length);
/*   9:    */   }
/*  10:    */   
/*  11:    */   public int capacity()
/*  12:    */   {
/*  13: 29 */     return maxCapacity();
/*  14:    */   }
/*  15:    */   
/*  16:    */   public AbstractByteBuf unwrap()
/*  17:    */   {
/*  18: 34 */     return (AbstractByteBuf)super.unwrap();
/*  19:    */   }
/*  20:    */   
/*  21:    */   protected byte _getByte(int index)
/*  22:    */   {
/*  23: 39 */     return unwrap()._getByte(idx(index));
/*  24:    */   }
/*  25:    */   
/*  26:    */   protected short _getShort(int index)
/*  27:    */   {
/*  28: 44 */     return unwrap()._getShort(idx(index));
/*  29:    */   }
/*  30:    */   
/*  31:    */   protected short _getShortLE(int index)
/*  32:    */   {
/*  33: 49 */     return unwrap()._getShortLE(idx(index));
/*  34:    */   }
/*  35:    */   
/*  36:    */   protected int _getUnsignedMedium(int index)
/*  37:    */   {
/*  38: 54 */     return unwrap()._getUnsignedMedium(idx(index));
/*  39:    */   }
/*  40:    */   
/*  41:    */   protected int _getUnsignedMediumLE(int index)
/*  42:    */   {
/*  43: 59 */     return unwrap()._getUnsignedMediumLE(idx(index));
/*  44:    */   }
/*  45:    */   
/*  46:    */   protected int _getInt(int index)
/*  47:    */   {
/*  48: 64 */     return unwrap()._getInt(idx(index));
/*  49:    */   }
/*  50:    */   
/*  51:    */   protected int _getIntLE(int index)
/*  52:    */   {
/*  53: 69 */     return unwrap()._getIntLE(idx(index));
/*  54:    */   }
/*  55:    */   
/*  56:    */   protected long _getLong(int index)
/*  57:    */   {
/*  58: 74 */     return unwrap()._getLong(idx(index));
/*  59:    */   }
/*  60:    */   
/*  61:    */   protected long _getLongLE(int index)
/*  62:    */   {
/*  63: 79 */     return unwrap()._getLongLE(idx(index));
/*  64:    */   }
/*  65:    */   
/*  66:    */   protected void _setByte(int index, int value)
/*  67:    */   {
/*  68: 84 */     unwrap()._setByte(idx(index), value);
/*  69:    */   }
/*  70:    */   
/*  71:    */   protected void _setShort(int index, int value)
/*  72:    */   {
/*  73: 89 */     unwrap()._setShort(idx(index), value);
/*  74:    */   }
/*  75:    */   
/*  76:    */   protected void _setShortLE(int index, int value)
/*  77:    */   {
/*  78: 94 */     unwrap()._setShortLE(idx(index), value);
/*  79:    */   }
/*  80:    */   
/*  81:    */   protected void _setMedium(int index, int value)
/*  82:    */   {
/*  83: 99 */     unwrap()._setMedium(idx(index), value);
/*  84:    */   }
/*  85:    */   
/*  86:    */   protected void _setMediumLE(int index, int value)
/*  87:    */   {
/*  88:104 */     unwrap()._setMediumLE(idx(index), value);
/*  89:    */   }
/*  90:    */   
/*  91:    */   protected void _setInt(int index, int value)
/*  92:    */   {
/*  93:109 */     unwrap()._setInt(idx(index), value);
/*  94:    */   }
/*  95:    */   
/*  96:    */   protected void _setIntLE(int index, int value)
/*  97:    */   {
/*  98:114 */     unwrap()._setIntLE(idx(index), value);
/*  99:    */   }
/* 100:    */   
/* 101:    */   protected void _setLong(int index, long value)
/* 102:    */   {
/* 103:119 */     unwrap()._setLong(idx(index), value);
/* 104:    */   }
/* 105:    */   
/* 106:    */   protected void _setLongLE(int index, long value)
/* 107:    */   {
/* 108:124 */     unwrap()._setLongLE(idx(index), value);
/* 109:    */   }
/* 110:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.UnpooledSlicedByteBuf
 * JD-Core Version:    0.7.0.1
 */