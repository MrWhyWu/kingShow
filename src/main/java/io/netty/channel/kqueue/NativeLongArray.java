/*  1:   */ package io.netty.channel.kqueue;
/*  2:   */ 
/*  3:   */ import io.netty.channel.unix.Limits;
/*  4:   */ import io.netty.util.internal.PlatformDependent;
/*  5:   */ 
/*  6:   */ final class NativeLongArray
/*  7:   */ {
/*  8:   */   private long memoryAddress;
/*  9:   */   private int capacity;
/* 10:   */   private int size;
/* 11:   */   
/* 12:   */   NativeLongArray(int capacity)
/* 13:   */   {
/* 14:28 */     if (capacity < 1) {
/* 15:29 */       throw new IllegalArgumentException("capacity must be >= 1 but was " + capacity);
/* 16:   */     }
/* 17:31 */     this.memoryAddress = PlatformDependent.allocateMemory(capacity * Limits.SIZEOF_JLONG);
/* 18:32 */     this.capacity = capacity;
/* 19:   */   }
/* 20:   */   
/* 21:   */   void add(long value)
/* 22:   */   {
/* 23:36 */     checkSize();
/* 24:37 */     PlatformDependent.putLong(memoryOffset(this.size++), value);
/* 25:   */   }
/* 26:   */   
/* 27:   */   void clear()
/* 28:   */   {
/* 29:41 */     this.size = 0;
/* 30:   */   }
/* 31:   */   
/* 32:   */   boolean isEmpty()
/* 33:   */   {
/* 34:45 */     return this.size == 0;
/* 35:   */   }
/* 36:   */   
/* 37:   */   void free()
/* 38:   */   {
/* 39:49 */     PlatformDependent.freeMemory(this.memoryAddress);
/* 40:50 */     this.memoryAddress = 0L;
/* 41:   */   }
/* 42:   */   
/* 43:   */   long memoryAddress()
/* 44:   */   {
/* 45:54 */     return this.memoryAddress;
/* 46:   */   }
/* 47:   */   
/* 48:   */   long memoryAddressEnd()
/* 49:   */   {
/* 50:58 */     return memoryOffset(this.size);
/* 51:   */   }
/* 52:   */   
/* 53:   */   private long memoryOffset(int index)
/* 54:   */   {
/* 55:62 */     return this.memoryAddress + index * Limits.SIZEOF_JLONG;
/* 56:   */   }
/* 57:   */   
/* 58:   */   private void checkSize()
/* 59:   */   {
/* 60:66 */     if (this.size == this.capacity) {
/* 61:67 */       realloc();
/* 62:   */     }
/* 63:   */   }
/* 64:   */   
/* 65:   */   private void realloc()
/* 66:   */   {
/* 67:73 */     int newLength = this.capacity <= 65536 ? this.capacity << 1 : this.capacity + this.capacity >> 1;
/* 68:74 */     long newMemoryAddress = PlatformDependent.reallocateMemory(this.memoryAddress, newLength * Limits.SIZEOF_JLONG);
/* 69:75 */     if (newMemoryAddress == 0L) {
/* 70:76 */       throw new OutOfMemoryError("unable to allocate " + newLength + " new bytes! Existing capacity is: " + this.capacity);
/* 71:   */     }
/* 72:79 */     this.memoryAddress = newMemoryAddress;
/* 73:80 */     this.capacity = newLength;
/* 74:   */   }
/* 75:   */   
/* 76:   */   public String toString()
/* 77:   */   {
/* 78:85 */     return "memoryAddress: " + this.memoryAddress + " capacity: " + this.capacity + " size: " + this.size;
/* 79:   */   }
/* 80:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.kqueue.NativeLongArray
 * JD-Core Version:    0.7.0.1
 */