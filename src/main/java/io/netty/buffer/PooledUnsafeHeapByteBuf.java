/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.Recycler;
/*   4:    */ import io.netty.util.Recycler.Handle;
/*   5:    */ import io.netty.util.internal.PlatformDependent;
/*   6:    */ 
/*   7:    */ final class PooledUnsafeHeapByteBuf
/*   8:    */   extends PooledHeapByteBuf
/*   9:    */ {
/*  10: 24 */   private static final Recycler<PooledUnsafeHeapByteBuf> RECYCLER = new Recycler()
/*  11:    */   {
/*  12:    */     protected PooledUnsafeHeapByteBuf newObject(Recycler.Handle<PooledUnsafeHeapByteBuf> handle)
/*  13:    */     {
/*  14: 27 */       return new PooledUnsafeHeapByteBuf(handle, 0, null);
/*  15:    */     }
/*  16:    */   };
/*  17:    */   
/*  18:    */   static PooledUnsafeHeapByteBuf newUnsafeInstance(int maxCapacity)
/*  19:    */   {
/*  20: 32 */     PooledUnsafeHeapByteBuf buf = (PooledUnsafeHeapByteBuf)RECYCLER.get();
/*  21: 33 */     buf.reuse(maxCapacity);
/*  22: 34 */     return buf;
/*  23:    */   }
/*  24:    */   
/*  25:    */   private PooledUnsafeHeapByteBuf(Recycler.Handle<PooledUnsafeHeapByteBuf> recyclerHandle, int maxCapacity)
/*  26:    */   {
/*  27: 38 */     super(recyclerHandle, maxCapacity);
/*  28:    */   }
/*  29:    */   
/*  30:    */   protected byte _getByte(int index)
/*  31:    */   {
/*  32: 43 */     return UnsafeByteBufUtil.getByte((byte[])this.memory, idx(index));
/*  33:    */   }
/*  34:    */   
/*  35:    */   protected short _getShort(int index)
/*  36:    */   {
/*  37: 48 */     return UnsafeByteBufUtil.getShort((byte[])this.memory, idx(index));
/*  38:    */   }
/*  39:    */   
/*  40:    */   protected short _getShortLE(int index)
/*  41:    */   {
/*  42: 53 */     return UnsafeByteBufUtil.getShortLE((byte[])this.memory, idx(index));
/*  43:    */   }
/*  44:    */   
/*  45:    */   protected int _getUnsignedMedium(int index)
/*  46:    */   {
/*  47: 58 */     return UnsafeByteBufUtil.getUnsignedMedium((byte[])this.memory, idx(index));
/*  48:    */   }
/*  49:    */   
/*  50:    */   protected int _getUnsignedMediumLE(int index)
/*  51:    */   {
/*  52: 63 */     return UnsafeByteBufUtil.getUnsignedMediumLE((byte[])this.memory, idx(index));
/*  53:    */   }
/*  54:    */   
/*  55:    */   protected int _getInt(int index)
/*  56:    */   {
/*  57: 68 */     return UnsafeByteBufUtil.getInt((byte[])this.memory, idx(index));
/*  58:    */   }
/*  59:    */   
/*  60:    */   protected int _getIntLE(int index)
/*  61:    */   {
/*  62: 73 */     return UnsafeByteBufUtil.getIntLE((byte[])this.memory, idx(index));
/*  63:    */   }
/*  64:    */   
/*  65:    */   protected long _getLong(int index)
/*  66:    */   {
/*  67: 78 */     return UnsafeByteBufUtil.getLong((byte[])this.memory, idx(index));
/*  68:    */   }
/*  69:    */   
/*  70:    */   protected long _getLongLE(int index)
/*  71:    */   {
/*  72: 83 */     return UnsafeByteBufUtil.getLongLE((byte[])this.memory, idx(index));
/*  73:    */   }
/*  74:    */   
/*  75:    */   protected void _setByte(int index, int value)
/*  76:    */   {
/*  77: 88 */     UnsafeByteBufUtil.setByte((byte[])this.memory, idx(index), value);
/*  78:    */   }
/*  79:    */   
/*  80:    */   protected void _setShort(int index, int value)
/*  81:    */   {
/*  82: 93 */     UnsafeByteBufUtil.setShort((byte[])this.memory, idx(index), value);
/*  83:    */   }
/*  84:    */   
/*  85:    */   protected void _setShortLE(int index, int value)
/*  86:    */   {
/*  87: 98 */     UnsafeByteBufUtil.setShortLE((byte[])this.memory, idx(index), value);
/*  88:    */   }
/*  89:    */   
/*  90:    */   protected void _setMedium(int index, int value)
/*  91:    */   {
/*  92:103 */     UnsafeByteBufUtil.setMedium((byte[])this.memory, idx(index), value);
/*  93:    */   }
/*  94:    */   
/*  95:    */   protected void _setMediumLE(int index, int value)
/*  96:    */   {
/*  97:108 */     UnsafeByteBufUtil.setMediumLE((byte[])this.memory, idx(index), value);
/*  98:    */   }
/*  99:    */   
/* 100:    */   protected void _setInt(int index, int value)
/* 101:    */   {
/* 102:113 */     UnsafeByteBufUtil.setInt((byte[])this.memory, idx(index), value);
/* 103:    */   }
/* 104:    */   
/* 105:    */   protected void _setIntLE(int index, int value)
/* 106:    */   {
/* 107:118 */     UnsafeByteBufUtil.setIntLE((byte[])this.memory, idx(index), value);
/* 108:    */   }
/* 109:    */   
/* 110:    */   protected void _setLong(int index, long value)
/* 111:    */   {
/* 112:123 */     UnsafeByteBufUtil.setLong((byte[])this.memory, idx(index), value);
/* 113:    */   }
/* 114:    */   
/* 115:    */   protected void _setLongLE(int index, long value)
/* 116:    */   {
/* 117:128 */     UnsafeByteBufUtil.setLongLE((byte[])this.memory, idx(index), value);
/* 118:    */   }
/* 119:    */   
/* 120:    */   public ByteBuf setZero(int index, int length)
/* 121:    */   {
/* 122:133 */     if (PlatformDependent.javaVersion() >= 7)
/* 123:    */     {
/* 124:134 */       checkIndex(index, length);
/* 125:    */       
/* 126:136 */       UnsafeByteBufUtil.setZero((byte[])this.memory, idx(index), length);
/* 127:137 */       return this;
/* 128:    */     }
/* 129:139 */     return super.setZero(index, length);
/* 130:    */   }
/* 131:    */   
/* 132:    */   public ByteBuf writeZero(int length)
/* 133:    */   {
/* 134:144 */     if (PlatformDependent.javaVersion() >= 7)
/* 135:    */     {
/* 136:146 */       ensureWritable(length);
/* 137:147 */       int wIndex = this.writerIndex;
/* 138:148 */       UnsafeByteBufUtil.setZero((byte[])this.memory, idx(wIndex), length);
/* 139:149 */       this.writerIndex = (wIndex + length);
/* 140:150 */       return this;
/* 141:    */     }
/* 142:152 */     return super.writeZero(length);
/* 143:    */   }
/* 144:    */   
/* 145:    */   @Deprecated
/* 146:    */   protected SwappedByteBuf newSwappedByteBuf()
/* 147:    */   {
/* 148:158 */     if (PlatformDependent.isUnaligned()) {
/* 149:160 */       return new UnsafeHeapSwappedByteBuf(this);
/* 150:    */     }
/* 151:162 */     return super.newSwappedByteBuf();
/* 152:    */   }
/* 153:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.PooledUnsafeHeapByteBuf
 * JD-Core Version:    0.7.0.1
 */