/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.Recycler.Handle;
/*   4:    */ import java.nio.ByteBuffer;
/*   5:    */ import java.nio.ByteOrder;
/*   6:    */ 
/*   7:    */ abstract class PooledByteBuf<T>
/*   8:    */   extends AbstractReferenceCountedByteBuf
/*   9:    */ {
/*  10:    */   private final Recycler.Handle<PooledByteBuf<T>> recyclerHandle;
/*  11:    */   protected PoolChunk<T> chunk;
/*  12:    */   protected long handle;
/*  13:    */   protected T memory;
/*  14:    */   protected int offset;
/*  15:    */   protected int length;
/*  16:    */   int maxLength;
/*  17:    */   PoolThreadCache cache;
/*  18:    */   private ByteBuffer tmpNioBuf;
/*  19:    */   private ByteBufAllocator allocator;
/*  20:    */   
/*  21:    */   protected PooledByteBuf(Recycler.Handle<? extends PooledByteBuf<T>> recyclerHandle, int maxCapacity)
/*  22:    */   {
/*  23: 41 */     super(maxCapacity);
/*  24: 42 */     this.recyclerHandle = recyclerHandle;
/*  25:    */   }
/*  26:    */   
/*  27:    */   void init(PoolChunk<T> chunk, long handle, int offset, int length, int maxLength, PoolThreadCache cache)
/*  28:    */   {
/*  29: 46 */     init0(chunk, handle, offset, length, maxLength, cache);
/*  30:    */   }
/*  31:    */   
/*  32:    */   void initUnpooled(PoolChunk<T> chunk, int length)
/*  33:    */   {
/*  34: 50 */     init0(chunk, 0L, chunk.offset, length, length, null);
/*  35:    */   }
/*  36:    */   
/*  37:    */   private void init0(PoolChunk<T> chunk, long handle, int offset, int length, int maxLength, PoolThreadCache cache)
/*  38:    */   {
/*  39: 54 */     assert (handle >= 0L);
/*  40: 55 */     assert (chunk != null);
/*  41:    */     
/*  42: 57 */     this.chunk = chunk;
/*  43: 58 */     this.memory = chunk.memory;
/*  44: 59 */     this.allocator = chunk.arena.parent;
/*  45: 60 */     this.cache = cache;
/*  46: 61 */     this.handle = handle;
/*  47: 62 */     this.offset = offset;
/*  48: 63 */     this.length = length;
/*  49: 64 */     this.maxLength = maxLength;
/*  50: 65 */     this.tmpNioBuf = null;
/*  51:    */   }
/*  52:    */   
/*  53:    */   final void reuse(int maxCapacity)
/*  54:    */   {
/*  55: 72 */     maxCapacity(maxCapacity);
/*  56: 73 */     setRefCnt(1);
/*  57: 74 */     setIndex0(0, 0);
/*  58: 75 */     discardMarks();
/*  59:    */   }
/*  60:    */   
/*  61:    */   public final int capacity()
/*  62:    */   {
/*  63: 80 */     return this.length;
/*  64:    */   }
/*  65:    */   
/*  66:    */   public final ByteBuf capacity(int newCapacity)
/*  67:    */   {
/*  68: 85 */     checkNewCapacity(newCapacity);
/*  69: 88 */     if (this.chunk.unpooled)
/*  70:    */     {
/*  71: 89 */       if (newCapacity == this.length) {
/*  72: 90 */         return this;
/*  73:    */       }
/*  74:    */     }
/*  75: 93 */     else if (newCapacity > this.length)
/*  76:    */     {
/*  77: 94 */       if (newCapacity <= this.maxLength)
/*  78:    */       {
/*  79: 95 */         this.length = newCapacity;
/*  80: 96 */         return this;
/*  81:    */       }
/*  82:    */     }
/*  83: 98 */     else if (newCapacity < this.length)
/*  84:    */     {
/*  85: 99 */       if (newCapacity > this.maxLength >>> 1) {
/*  86:100 */         if (this.maxLength <= 512)
/*  87:    */         {
/*  88:101 */           if (newCapacity > this.maxLength - 16)
/*  89:    */           {
/*  90:102 */             this.length = newCapacity;
/*  91:103 */             setIndex(Math.min(readerIndex(), newCapacity), Math.min(writerIndex(), newCapacity));
/*  92:104 */             return this;
/*  93:    */           }
/*  94:    */         }
/*  95:    */         else
/*  96:    */         {
/*  97:107 */           this.length = newCapacity;
/*  98:108 */           setIndex(Math.min(readerIndex(), newCapacity), Math.min(writerIndex(), newCapacity));
/*  99:109 */           return this;
/* 100:    */         }
/* 101:    */       }
/* 102:    */     }
/* 103:    */     else {
/* 104:113 */       return this;
/* 105:    */     }
/* 106:118 */     this.chunk.arena.reallocate(this, newCapacity, true);
/* 107:119 */     return this;
/* 108:    */   }
/* 109:    */   
/* 110:    */   public final ByteBufAllocator alloc()
/* 111:    */   {
/* 112:124 */     return this.allocator;
/* 113:    */   }
/* 114:    */   
/* 115:    */   public final ByteOrder order()
/* 116:    */   {
/* 117:129 */     return ByteOrder.BIG_ENDIAN;
/* 118:    */   }
/* 119:    */   
/* 120:    */   public final ByteBuf unwrap()
/* 121:    */   {
/* 122:134 */     return null;
/* 123:    */   }
/* 124:    */   
/* 125:    */   public final ByteBuf retainedDuplicate()
/* 126:    */   {
/* 127:139 */     return PooledDuplicatedByteBuf.newInstance(this, this, readerIndex(), writerIndex());
/* 128:    */   }
/* 129:    */   
/* 130:    */   public final ByteBuf retainedSlice()
/* 131:    */   {
/* 132:144 */     int index = readerIndex();
/* 133:145 */     return retainedSlice(index, writerIndex() - index);
/* 134:    */   }
/* 135:    */   
/* 136:    */   public final ByteBuf retainedSlice(int index, int length)
/* 137:    */   {
/* 138:150 */     return PooledSlicedByteBuf.newInstance(this, this, index, length);
/* 139:    */   }
/* 140:    */   
/* 141:    */   protected final ByteBuffer internalNioBuffer()
/* 142:    */   {
/* 143:154 */     ByteBuffer tmpNioBuf = this.tmpNioBuf;
/* 144:155 */     if (tmpNioBuf == null) {
/* 145:156 */       this.tmpNioBuf = (tmpNioBuf = newInternalNioBuffer(this.memory));
/* 146:    */     }
/* 147:158 */     return tmpNioBuf;
/* 148:    */   }
/* 149:    */   
/* 150:    */   protected abstract ByteBuffer newInternalNioBuffer(T paramT);
/* 151:    */   
/* 152:    */   protected final void deallocate()
/* 153:    */   {
/* 154:165 */     if (this.handle >= 0L)
/* 155:    */     {
/* 156:166 */       long handle = this.handle;
/* 157:167 */       this.handle = -1L;
/* 158:168 */       this.memory = null;
/* 159:169 */       this.tmpNioBuf = null;
/* 160:170 */       this.chunk.arena.free(this.chunk, handle, this.maxLength, this.cache);
/* 161:171 */       this.chunk = null;
/* 162:172 */       recycle();
/* 163:    */     }
/* 164:    */   }
/* 165:    */   
/* 166:    */   private void recycle()
/* 167:    */   {
/* 168:177 */     this.recyclerHandle.recycle(this);
/* 169:    */   }
/* 170:    */   
/* 171:    */   protected final int idx(int index)
/* 172:    */   {
/* 173:181 */     return this.offset + index;
/* 174:    */   }
/* 175:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.PooledByteBuf
 * JD-Core Version:    0.7.0.1
 */