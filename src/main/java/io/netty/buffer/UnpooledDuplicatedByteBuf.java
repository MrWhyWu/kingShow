/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ class UnpooledDuplicatedByteBuf
/*   4:    */   extends DuplicatedByteBuf
/*   5:    */ {
/*   6:    */   UnpooledDuplicatedByteBuf(AbstractByteBuf buffer)
/*   7:    */   {
/*   8: 24 */     super(buffer);
/*   9:    */   }
/*  10:    */   
/*  11:    */   public AbstractByteBuf unwrap()
/*  12:    */   {
/*  13: 29 */     return (AbstractByteBuf)super.unwrap();
/*  14:    */   }
/*  15:    */   
/*  16:    */   protected byte _getByte(int index)
/*  17:    */   {
/*  18: 34 */     return unwrap()._getByte(index);
/*  19:    */   }
/*  20:    */   
/*  21:    */   protected short _getShort(int index)
/*  22:    */   {
/*  23: 39 */     return unwrap()._getShort(index);
/*  24:    */   }
/*  25:    */   
/*  26:    */   protected short _getShortLE(int index)
/*  27:    */   {
/*  28: 44 */     return unwrap()._getShortLE(index);
/*  29:    */   }
/*  30:    */   
/*  31:    */   protected int _getUnsignedMedium(int index)
/*  32:    */   {
/*  33: 49 */     return unwrap()._getUnsignedMedium(index);
/*  34:    */   }
/*  35:    */   
/*  36:    */   protected int _getUnsignedMediumLE(int index)
/*  37:    */   {
/*  38: 54 */     return unwrap()._getUnsignedMediumLE(index);
/*  39:    */   }
/*  40:    */   
/*  41:    */   protected int _getInt(int index)
/*  42:    */   {
/*  43: 59 */     return unwrap()._getInt(index);
/*  44:    */   }
/*  45:    */   
/*  46:    */   protected int _getIntLE(int index)
/*  47:    */   {
/*  48: 64 */     return unwrap()._getIntLE(index);
/*  49:    */   }
/*  50:    */   
/*  51:    */   protected long _getLong(int index)
/*  52:    */   {
/*  53: 69 */     return unwrap()._getLong(index);
/*  54:    */   }
/*  55:    */   
/*  56:    */   protected long _getLongLE(int index)
/*  57:    */   {
/*  58: 74 */     return unwrap()._getLongLE(index);
/*  59:    */   }
/*  60:    */   
/*  61:    */   protected void _setByte(int index, int value)
/*  62:    */   {
/*  63: 79 */     unwrap()._setByte(index, value);
/*  64:    */   }
/*  65:    */   
/*  66:    */   protected void _setShort(int index, int value)
/*  67:    */   {
/*  68: 84 */     unwrap()._setShort(index, value);
/*  69:    */   }
/*  70:    */   
/*  71:    */   protected void _setShortLE(int index, int value)
/*  72:    */   {
/*  73: 89 */     unwrap()._setShortLE(index, value);
/*  74:    */   }
/*  75:    */   
/*  76:    */   protected void _setMedium(int index, int value)
/*  77:    */   {
/*  78: 94 */     unwrap()._setMedium(index, value);
/*  79:    */   }
/*  80:    */   
/*  81:    */   protected void _setMediumLE(int index, int value)
/*  82:    */   {
/*  83: 99 */     unwrap()._setMediumLE(index, value);
/*  84:    */   }
/*  85:    */   
/*  86:    */   protected void _setInt(int index, int value)
/*  87:    */   {
/*  88:104 */     unwrap()._setInt(index, value);
/*  89:    */   }
/*  90:    */   
/*  91:    */   protected void _setIntLE(int index, int value)
/*  92:    */   {
/*  93:109 */     unwrap()._setIntLE(index, value);
/*  94:    */   }
/*  95:    */   
/*  96:    */   protected void _setLong(int index, long value)
/*  97:    */   {
/*  98:114 */     unwrap()._setLong(index, value);
/*  99:    */   }
/* 100:    */   
/* 101:    */   protected void _setLongLE(int index, long value)
/* 102:    */   {
/* 103:119 */     unwrap()._setLongLE(index, value);
/* 104:    */   }
/* 105:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.UnpooledDuplicatedByteBuf
 * JD-Core Version:    0.7.0.1
 */