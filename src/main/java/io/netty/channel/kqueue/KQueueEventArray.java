/*   1:    */ package io.netty.channel.kqueue;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.PlatformDependent;
/*   4:    */ 
/*   5:    */ final class KQueueEventArray
/*   6:    */ {
/*   7: 33 */   private static final int KQUEUE_EVENT_SIZE = ;
/*   8: 34 */   private static final int KQUEUE_IDENT_OFFSET = Native.offsetofKEventIdent();
/*   9: 35 */   private static final int KQUEUE_FILTER_OFFSET = Native.offsetofKEventFilter();
/*  10: 36 */   private static final int KQUEUE_FFLAGS_OFFSET = Native.offsetofKEventFFlags();
/*  11: 37 */   private static final int KQUEUE_FLAGS_OFFSET = Native.offsetofKEventFlags();
/*  12: 38 */   private static final int KQUEUE_DATA_OFFSET = Native.offsetofKeventData();
/*  13:    */   private long memoryAddress;
/*  14:    */   private int size;
/*  15:    */   private int capacity;
/*  16:    */   
/*  17:    */   KQueueEventArray(int capacity)
/*  18:    */   {
/*  19: 45 */     if (capacity < 1) {
/*  20: 46 */       throw new IllegalArgumentException("capacity must be >= 1 but was " + capacity);
/*  21:    */     }
/*  22: 48 */     this.memoryAddress = PlatformDependent.allocateMemory(capacity * KQUEUE_EVENT_SIZE);
/*  23: 49 */     this.capacity = capacity;
/*  24:    */   }
/*  25:    */   
/*  26:    */   long memoryAddress()
/*  27:    */   {
/*  28: 56 */     return this.memoryAddress;
/*  29:    */   }
/*  30:    */   
/*  31:    */   int capacity()
/*  32:    */   {
/*  33: 64 */     return this.capacity;
/*  34:    */   }
/*  35:    */   
/*  36:    */   int size()
/*  37:    */   {
/*  38: 68 */     return this.size;
/*  39:    */   }
/*  40:    */   
/*  41:    */   void clear()
/*  42:    */   {
/*  43: 72 */     this.size = 0;
/*  44:    */   }
/*  45:    */   
/*  46:    */   void evSet(AbstractKQueueChannel ch, short filter, short flags, int fflags)
/*  47:    */   {
/*  48: 76 */     checkSize();
/*  49: 77 */     evSet(getKEventOffset(this.size++), ch, ch.socket.intValue(), filter, flags, fflags);
/*  50:    */   }
/*  51:    */   
/*  52:    */   private void checkSize()
/*  53:    */   {
/*  54: 81 */     if (this.size == this.capacity) {
/*  55: 82 */       realloc(true);
/*  56:    */     }
/*  57:    */   }
/*  58:    */   
/*  59:    */   void realloc(boolean throwIfFail)
/*  60:    */   {
/*  61: 91 */     int newLength = this.capacity <= 65536 ? this.capacity << 1 : this.capacity + this.capacity >> 1;
/*  62: 92 */     long newMemoryAddress = PlatformDependent.reallocateMemory(this.memoryAddress, newLength * KQUEUE_EVENT_SIZE);
/*  63: 93 */     if (newMemoryAddress != 0L)
/*  64:    */     {
/*  65: 94 */       this.memoryAddress = newMemoryAddress;
/*  66: 95 */       this.capacity = newLength;
/*  67: 96 */       return;
/*  68:    */     }
/*  69: 98 */     if (throwIfFail) {
/*  70: 99 */       throw new OutOfMemoryError("unable to allocate " + newLength + " new bytes! Existing capacity is: " + this.capacity);
/*  71:    */     }
/*  72:    */   }
/*  73:    */   
/*  74:    */   void free()
/*  75:    */   {
/*  76:108 */     PlatformDependent.freeMemory(this.memoryAddress);
/*  77:109 */     this.memoryAddress = (this.size = this.capacity = 0);
/*  78:    */   }
/*  79:    */   
/*  80:    */   long getKEventOffset(int index)
/*  81:    */   {
/*  82:113 */     return this.memoryAddress + index * KQUEUE_EVENT_SIZE;
/*  83:    */   }
/*  84:    */   
/*  85:    */   short flags(int index)
/*  86:    */   {
/*  87:117 */     return PlatformDependent.getShort(getKEventOffset(index) + KQUEUE_FLAGS_OFFSET);
/*  88:    */   }
/*  89:    */   
/*  90:    */   short filter(int index)
/*  91:    */   {
/*  92:121 */     return PlatformDependent.getShort(getKEventOffset(index) + KQUEUE_FILTER_OFFSET);
/*  93:    */   }
/*  94:    */   
/*  95:    */   short fflags(int index)
/*  96:    */   {
/*  97:125 */     return PlatformDependent.getShort(getKEventOffset(index) + KQUEUE_FFLAGS_OFFSET);
/*  98:    */   }
/*  99:    */   
/* 100:    */   int fd(int index)
/* 101:    */   {
/* 102:129 */     return PlatformDependent.getInt(getKEventOffset(index) + KQUEUE_IDENT_OFFSET);
/* 103:    */   }
/* 104:    */   
/* 105:    */   long data(int index)
/* 106:    */   {
/* 107:133 */     return PlatformDependent.getLong(getKEventOffset(index) + KQUEUE_DATA_OFFSET);
/* 108:    */   }
/* 109:    */   
/* 110:    */   AbstractKQueueChannel channel(int index)
/* 111:    */   {
/* 112:137 */     return getChannel(getKEventOffset(index));
/* 113:    */   }
/* 114:    */   
/* 115:    */   private static native void evSet(long paramLong, AbstractKQueueChannel paramAbstractKQueueChannel, int paramInt1, short paramShort1, short paramShort2, int paramInt2);
/* 116:    */   
/* 117:    */   private static native AbstractKQueueChannel getChannel(long paramLong);
/* 118:    */   
/* 119:    */   static native void deleteGlobalRefs(long paramLong1, long paramLong2);
/* 120:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.kqueue.KQueueEventArray
 * JD-Core Version:    0.7.0.1
 */