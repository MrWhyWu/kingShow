/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.PlatformDependent;
/*   4:    */ import java.nio.ByteOrder;
/*   5:    */ 
/*   6:    */ abstract class AbstractUnsafeSwappedByteBuf
/*   7:    */   extends SwappedByteBuf
/*   8:    */ {
/*   9:    */   private final boolean nativeByteOrder;
/*  10:    */   private final AbstractByteBuf wrapped;
/*  11:    */   
/*  12:    */   AbstractUnsafeSwappedByteBuf(AbstractByteBuf buf)
/*  13:    */   {
/*  14: 32 */     super(buf);
/*  15: 33 */     assert (PlatformDependent.isUnaligned());
/*  16: 34 */     this.wrapped = buf;
/*  17: 35 */     this.nativeByteOrder = (PlatformDependent.BIG_ENDIAN_NATIVE_ORDER == (order() == ByteOrder.BIG_ENDIAN));
/*  18:    */   }
/*  19:    */   
/*  20:    */   public final long getLong(int index)
/*  21:    */   {
/*  22: 40 */     this.wrapped.checkIndex(index, 8);
/*  23: 41 */     long v = _getLong(this.wrapped, index);
/*  24: 42 */     return this.nativeByteOrder ? v : Long.reverseBytes(v);
/*  25:    */   }
/*  26:    */   
/*  27:    */   public final float getFloat(int index)
/*  28:    */   {
/*  29: 47 */     return Float.intBitsToFloat(getInt(index));
/*  30:    */   }
/*  31:    */   
/*  32:    */   public final double getDouble(int index)
/*  33:    */   {
/*  34: 52 */     return Double.longBitsToDouble(getLong(index));
/*  35:    */   }
/*  36:    */   
/*  37:    */   public final char getChar(int index)
/*  38:    */   {
/*  39: 57 */     return (char)getShort(index);
/*  40:    */   }
/*  41:    */   
/*  42:    */   public final long getUnsignedInt(int index)
/*  43:    */   {
/*  44: 62 */     return getInt(index) & 0xFFFFFFFF;
/*  45:    */   }
/*  46:    */   
/*  47:    */   public final int getInt(int index)
/*  48:    */   {
/*  49: 67 */     this.wrapped.checkIndex0(index, 4);
/*  50: 68 */     int v = _getInt(this.wrapped, index);
/*  51: 69 */     return this.nativeByteOrder ? v : Integer.reverseBytes(v);
/*  52:    */   }
/*  53:    */   
/*  54:    */   public final int getUnsignedShort(int index)
/*  55:    */   {
/*  56: 74 */     return getShort(index) & 0xFFFF;
/*  57:    */   }
/*  58:    */   
/*  59:    */   public final short getShort(int index)
/*  60:    */   {
/*  61: 79 */     this.wrapped.checkIndex0(index, 2);
/*  62: 80 */     short v = _getShort(this.wrapped, index);
/*  63: 81 */     return this.nativeByteOrder ? v : Short.reverseBytes(v);
/*  64:    */   }
/*  65:    */   
/*  66:    */   public final ByteBuf setShort(int index, int value)
/*  67:    */   {
/*  68: 86 */     this.wrapped.checkIndex0(index, 2);
/*  69: 87 */     _setShort(this.wrapped, index, this.nativeByteOrder ? (short)value : Short.reverseBytes((short)value));
/*  70: 88 */     return this;
/*  71:    */   }
/*  72:    */   
/*  73:    */   public final ByteBuf setInt(int index, int value)
/*  74:    */   {
/*  75: 93 */     this.wrapped.checkIndex0(index, 4);
/*  76: 94 */     _setInt(this.wrapped, index, this.nativeByteOrder ? value : Integer.reverseBytes(value));
/*  77: 95 */     return this;
/*  78:    */   }
/*  79:    */   
/*  80:    */   public final ByteBuf setLong(int index, long value)
/*  81:    */   {
/*  82:100 */     this.wrapped.checkIndex(index, 8);
/*  83:101 */     _setLong(this.wrapped, index, this.nativeByteOrder ? value : Long.reverseBytes(value));
/*  84:102 */     return this;
/*  85:    */   }
/*  86:    */   
/*  87:    */   public final ByteBuf setChar(int index, int value)
/*  88:    */   {
/*  89:107 */     setShort(index, value);
/*  90:108 */     return this;
/*  91:    */   }
/*  92:    */   
/*  93:    */   public final ByteBuf setFloat(int index, float value)
/*  94:    */   {
/*  95:113 */     setInt(index, Float.floatToRawIntBits(value));
/*  96:114 */     return this;
/*  97:    */   }
/*  98:    */   
/*  99:    */   public final ByteBuf setDouble(int index, double value)
/* 100:    */   {
/* 101:119 */     setLong(index, Double.doubleToRawLongBits(value));
/* 102:120 */     return this;
/* 103:    */   }
/* 104:    */   
/* 105:    */   public final ByteBuf writeShort(int value)
/* 106:    */   {
/* 107:125 */     this.wrapped.ensureWritable0(2);
/* 108:126 */     _setShort(this.wrapped, this.wrapped.writerIndex, this.nativeByteOrder ? (short)value : Short.reverseBytes((short)value));
/* 109:127 */     this.wrapped.writerIndex += 2;
/* 110:128 */     return this;
/* 111:    */   }
/* 112:    */   
/* 113:    */   public final ByteBuf writeInt(int value)
/* 114:    */   {
/* 115:133 */     this.wrapped.ensureWritable0(4);
/* 116:134 */     _setInt(this.wrapped, this.wrapped.writerIndex, this.nativeByteOrder ? value : Integer.reverseBytes(value));
/* 117:135 */     this.wrapped.writerIndex += 4;
/* 118:136 */     return this;
/* 119:    */   }
/* 120:    */   
/* 121:    */   public final ByteBuf writeLong(long value)
/* 122:    */   {
/* 123:141 */     this.wrapped.ensureWritable0(8);
/* 124:142 */     _setLong(this.wrapped, this.wrapped.writerIndex, this.nativeByteOrder ? value : Long.reverseBytes(value));
/* 125:143 */     this.wrapped.writerIndex += 8;
/* 126:144 */     return this;
/* 127:    */   }
/* 128:    */   
/* 129:    */   public final ByteBuf writeChar(int value)
/* 130:    */   {
/* 131:149 */     writeShort(value);
/* 132:150 */     return this;
/* 133:    */   }
/* 134:    */   
/* 135:    */   public final ByteBuf writeFloat(float value)
/* 136:    */   {
/* 137:155 */     writeInt(Float.floatToRawIntBits(value));
/* 138:156 */     return this;
/* 139:    */   }
/* 140:    */   
/* 141:    */   public final ByteBuf writeDouble(double value)
/* 142:    */   {
/* 143:161 */     writeLong(Double.doubleToRawLongBits(value));
/* 144:162 */     return this;
/* 145:    */   }
/* 146:    */   
/* 147:    */   protected abstract short _getShort(AbstractByteBuf paramAbstractByteBuf, int paramInt);
/* 148:    */   
/* 149:    */   protected abstract int _getInt(AbstractByteBuf paramAbstractByteBuf, int paramInt);
/* 150:    */   
/* 151:    */   protected abstract long _getLong(AbstractByteBuf paramAbstractByteBuf, int paramInt);
/* 152:    */   
/* 153:    */   protected abstract void _setShort(AbstractByteBuf paramAbstractByteBuf, int paramInt, short paramShort);
/* 154:    */   
/* 155:    */   protected abstract void _setInt(AbstractByteBuf paramAbstractByteBuf, int paramInt1, int paramInt2);
/* 156:    */   
/* 157:    */   protected abstract void _setLong(AbstractByteBuf paramAbstractByteBuf, int paramInt, long paramLong);
/* 158:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.AbstractUnsafeSwappedByteBuf
 * JD-Core Version:    0.7.0.1
 */