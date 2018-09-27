/*   1:    */ package io.netty.channel.epoll;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.PlatformDependent;
/*   4:    */ 
/*   5:    */ final class EpollEventArray
/*   6:    */ {
/*   7: 40 */   private static final int EPOLL_EVENT_SIZE = ;
/*   8: 42 */   private static final int EPOLL_DATA_OFFSET = Native.offsetofEpollData();
/*   9:    */   private long memoryAddress;
/*  10:    */   private int length;
/*  11:    */   
/*  12:    */   EpollEventArray(int length)
/*  13:    */   {
/*  14: 48 */     if (length < 1) {
/*  15: 49 */       throw new IllegalArgumentException("length must be >= 1 but was " + length);
/*  16:    */     }
/*  17: 51 */     this.length = length;
/*  18: 52 */     this.memoryAddress = allocate(length);
/*  19:    */   }
/*  20:    */   
/*  21:    */   private static long allocate(int length)
/*  22:    */   {
/*  23: 56 */     return PlatformDependent.allocateMemory(length * EPOLL_EVENT_SIZE);
/*  24:    */   }
/*  25:    */   
/*  26:    */   long memoryAddress()
/*  27:    */   {
/*  28: 63 */     return this.memoryAddress;
/*  29:    */   }
/*  30:    */   
/*  31:    */   int length()
/*  32:    */   {
/*  33: 71 */     return this.length;
/*  34:    */   }
/*  35:    */   
/*  36:    */   void increase()
/*  37:    */   {
/*  38: 79 */     this.length <<= 1;
/*  39: 80 */     free();
/*  40: 81 */     this.memoryAddress = allocate(this.length);
/*  41:    */   }
/*  42:    */   
/*  43:    */   void free()
/*  44:    */   {
/*  45: 88 */     PlatformDependent.freeMemory(this.memoryAddress);
/*  46:    */   }
/*  47:    */   
/*  48:    */   int events(int index)
/*  49:    */   {
/*  50: 95 */     return PlatformDependent.getInt(this.memoryAddress + index * EPOLL_EVENT_SIZE);
/*  51:    */   }
/*  52:    */   
/*  53:    */   int fd(int index)
/*  54:    */   {
/*  55:102 */     return PlatformDependent.getInt(this.memoryAddress + index * EPOLL_EVENT_SIZE + EPOLL_DATA_OFFSET);
/*  56:    */   }
/*  57:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.epoll.EpollEventArray
 * JD-Core Version:    0.7.0.1
 */