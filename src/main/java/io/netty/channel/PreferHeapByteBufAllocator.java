/*   1:    */ package io.netty.channel;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.buffer.CompositeByteBuf;
/*   6:    */ import io.netty.util.internal.ObjectUtil;
/*   7:    */ 
/*   8:    */ public final class PreferHeapByteBufAllocator
/*   9:    */   implements ByteBufAllocator
/*  10:    */ {
/*  11:    */   private final ByteBufAllocator allocator;
/*  12:    */   
/*  13:    */   public PreferHeapByteBufAllocator(ByteBufAllocator allocator)
/*  14:    */   {
/*  15: 33 */     this.allocator = ((ByteBufAllocator)ObjectUtil.checkNotNull(allocator, "allocator"));
/*  16:    */   }
/*  17:    */   
/*  18:    */   public ByteBuf buffer()
/*  19:    */   {
/*  20: 38 */     return this.allocator.heapBuffer();
/*  21:    */   }
/*  22:    */   
/*  23:    */   public ByteBuf buffer(int initialCapacity)
/*  24:    */   {
/*  25: 43 */     return this.allocator.heapBuffer(initialCapacity);
/*  26:    */   }
/*  27:    */   
/*  28:    */   public ByteBuf buffer(int initialCapacity, int maxCapacity)
/*  29:    */   {
/*  30: 48 */     return this.allocator.heapBuffer(initialCapacity, maxCapacity);
/*  31:    */   }
/*  32:    */   
/*  33:    */   public ByteBuf ioBuffer()
/*  34:    */   {
/*  35: 53 */     return this.allocator.heapBuffer();
/*  36:    */   }
/*  37:    */   
/*  38:    */   public ByteBuf ioBuffer(int initialCapacity)
/*  39:    */   {
/*  40: 58 */     return this.allocator.heapBuffer(initialCapacity);
/*  41:    */   }
/*  42:    */   
/*  43:    */   public ByteBuf ioBuffer(int initialCapacity, int maxCapacity)
/*  44:    */   {
/*  45: 63 */     return this.allocator.heapBuffer(initialCapacity, maxCapacity);
/*  46:    */   }
/*  47:    */   
/*  48:    */   public ByteBuf heapBuffer()
/*  49:    */   {
/*  50: 68 */     return this.allocator.heapBuffer();
/*  51:    */   }
/*  52:    */   
/*  53:    */   public ByteBuf heapBuffer(int initialCapacity)
/*  54:    */   {
/*  55: 73 */     return this.allocator.heapBuffer(initialCapacity);
/*  56:    */   }
/*  57:    */   
/*  58:    */   public ByteBuf heapBuffer(int initialCapacity, int maxCapacity)
/*  59:    */   {
/*  60: 78 */     return this.allocator.heapBuffer(initialCapacity, maxCapacity);
/*  61:    */   }
/*  62:    */   
/*  63:    */   public ByteBuf directBuffer()
/*  64:    */   {
/*  65: 83 */     return this.allocator.directBuffer();
/*  66:    */   }
/*  67:    */   
/*  68:    */   public ByteBuf directBuffer(int initialCapacity)
/*  69:    */   {
/*  70: 88 */     return this.allocator.directBuffer(initialCapacity);
/*  71:    */   }
/*  72:    */   
/*  73:    */   public ByteBuf directBuffer(int initialCapacity, int maxCapacity)
/*  74:    */   {
/*  75: 93 */     return this.allocator.directBuffer(initialCapacity, maxCapacity);
/*  76:    */   }
/*  77:    */   
/*  78:    */   public CompositeByteBuf compositeBuffer()
/*  79:    */   {
/*  80: 98 */     return this.allocator.compositeHeapBuffer();
/*  81:    */   }
/*  82:    */   
/*  83:    */   public CompositeByteBuf compositeBuffer(int maxNumComponents)
/*  84:    */   {
/*  85:103 */     return this.allocator.compositeHeapBuffer(maxNumComponents);
/*  86:    */   }
/*  87:    */   
/*  88:    */   public CompositeByteBuf compositeHeapBuffer()
/*  89:    */   {
/*  90:108 */     return this.allocator.compositeHeapBuffer();
/*  91:    */   }
/*  92:    */   
/*  93:    */   public CompositeByteBuf compositeHeapBuffer(int maxNumComponents)
/*  94:    */   {
/*  95:113 */     return this.allocator.compositeHeapBuffer(maxNumComponents);
/*  96:    */   }
/*  97:    */   
/*  98:    */   public CompositeByteBuf compositeDirectBuffer()
/*  99:    */   {
/* 100:118 */     return this.allocator.compositeDirectBuffer();
/* 101:    */   }
/* 102:    */   
/* 103:    */   public CompositeByteBuf compositeDirectBuffer(int maxNumComponents)
/* 104:    */   {
/* 105:123 */     return this.allocator.compositeDirectBuffer(maxNumComponents);
/* 106:    */   }
/* 107:    */   
/* 108:    */   public boolean isDirectBufferPooled()
/* 109:    */   {
/* 110:128 */     return this.allocator.isDirectBufferPooled();
/* 111:    */   }
/* 112:    */   
/* 113:    */   public int calculateNewCapacity(int minNewCapacity, int maxCapacity)
/* 114:    */   {
/* 115:133 */     return this.allocator.calculateNewCapacity(minNewCapacity, maxCapacity);
/* 116:    */   }
/* 117:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.PreferHeapByteBufAllocator
 * JD-Core Version:    0.7.0.1
 */