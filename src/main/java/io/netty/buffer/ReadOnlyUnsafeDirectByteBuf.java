/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.PlatformDependent;
/*   4:    */ import java.nio.Buffer;
/*   5:    */ import java.nio.ByteBuffer;
/*   6:    */ 
/*   7:    */ final class ReadOnlyUnsafeDirectByteBuf
/*   8:    */   extends ReadOnlyByteBufferBuf
/*   9:    */ {
/*  10:    */   private final long memoryAddress;
/*  11:    */   
/*  12:    */   ReadOnlyUnsafeDirectByteBuf(ByteBufAllocator allocator, ByteBuffer buffer)
/*  13:    */   {
/*  14: 31 */     super(allocator, buffer);
/*  15: 32 */     this.memoryAddress = PlatformDependent.directBufferAddress(buffer);
/*  16:    */   }
/*  17:    */   
/*  18:    */   protected byte _getByte(int index)
/*  19:    */   {
/*  20: 37 */     return UnsafeByteBufUtil.getByte(addr(index));
/*  21:    */   }
/*  22:    */   
/*  23:    */   protected short _getShort(int index)
/*  24:    */   {
/*  25: 42 */     return UnsafeByteBufUtil.getShort(addr(index));
/*  26:    */   }
/*  27:    */   
/*  28:    */   protected int _getUnsignedMedium(int index)
/*  29:    */   {
/*  30: 47 */     return UnsafeByteBufUtil.getUnsignedMedium(addr(index));
/*  31:    */   }
/*  32:    */   
/*  33:    */   protected int _getInt(int index)
/*  34:    */   {
/*  35: 52 */     return UnsafeByteBufUtil.getInt(addr(index));
/*  36:    */   }
/*  37:    */   
/*  38:    */   protected long _getLong(int index)
/*  39:    */   {
/*  40: 57 */     return UnsafeByteBufUtil.getLong(addr(index));
/*  41:    */   }
/*  42:    */   
/*  43:    */   public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/*  44:    */   {
/*  45: 62 */     checkIndex(index, length);
/*  46: 63 */     if (dst == null) {
/*  47: 64 */       throw new NullPointerException("dst");
/*  48:    */     }
/*  49: 66 */     if ((dstIndex < 0) || (dstIndex > dst.capacity() - length)) {
/*  50: 67 */       throw new IndexOutOfBoundsException("dstIndex: " + dstIndex);
/*  51:    */     }
/*  52: 70 */     if (dst.hasMemoryAddress()) {
/*  53: 71 */       PlatformDependent.copyMemory(addr(index), dst.memoryAddress() + dstIndex, length);
/*  54: 72 */     } else if (dst.hasArray()) {
/*  55: 73 */       PlatformDependent.copyMemory(addr(index), dst.array(), dst.arrayOffset() + dstIndex, length);
/*  56:    */     } else {
/*  57: 75 */       dst.setBytes(dstIndex, this, index, length);
/*  58:    */     }
/*  59: 77 */     return this;
/*  60:    */   }
/*  61:    */   
/*  62:    */   public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/*  63:    */   {
/*  64: 82 */     checkIndex(index, length);
/*  65: 83 */     if (dst == null) {
/*  66: 84 */       throw new NullPointerException("dst");
/*  67:    */     }
/*  68: 86 */     if ((dstIndex < 0) || (dstIndex > dst.length - length)) {
/*  69: 87 */       throw new IndexOutOfBoundsException(String.format("dstIndex: %d, length: %d (expected: range(0, %d))", new Object[] {
/*  70: 88 */         Integer.valueOf(dstIndex), Integer.valueOf(length), Integer.valueOf(dst.length) }));
/*  71:    */     }
/*  72: 91 */     if (length != 0) {
/*  73: 92 */       PlatformDependent.copyMemory(addr(index), dst, dstIndex, length);
/*  74:    */     }
/*  75: 94 */     return this;
/*  76:    */   }
/*  77:    */   
/*  78:    */   public ByteBuf getBytes(int index, ByteBuffer dst)
/*  79:    */   {
/*  80: 99 */     checkIndex(index);
/*  81:100 */     if (dst == null) {
/*  82:101 */       throw new NullPointerException("dst");
/*  83:    */     }
/*  84:104 */     int bytesToCopy = Math.min(capacity() - index, dst.remaining());
/*  85:105 */     ByteBuffer tmpBuf = internalNioBuffer();
/*  86:106 */     tmpBuf.clear().position(index).limit(index + bytesToCopy);
/*  87:107 */     dst.put(tmpBuf);
/*  88:108 */     return this;
/*  89:    */   }
/*  90:    */   
/*  91:    */   public ByteBuf copy(int index, int length)
/*  92:    */   {
/*  93:113 */     checkIndex(index, length);
/*  94:114 */     ByteBuf copy = alloc().directBuffer(length, maxCapacity());
/*  95:115 */     if (length != 0) {
/*  96:116 */       if (copy.hasMemoryAddress())
/*  97:    */       {
/*  98:117 */         PlatformDependent.copyMemory(addr(index), copy.memoryAddress(), length);
/*  99:118 */         copy.setIndex(0, length);
/* 100:    */       }
/* 101:    */       else
/* 102:    */       {
/* 103:120 */         copy.writeBytes(this, index, length);
/* 104:    */       }
/* 105:    */     }
/* 106:123 */     return copy;
/* 107:    */   }
/* 108:    */   
/* 109:    */   private long addr(int index)
/* 110:    */   {
/* 111:127 */     return this.memoryAddress + index;
/* 112:    */   }
/* 113:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.ReadOnlyUnsafeDirectByteBuf
 * JD-Core Version:    0.7.0.1
 */