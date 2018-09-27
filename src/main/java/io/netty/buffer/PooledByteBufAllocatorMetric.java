/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.StringUtil;
/*   4:    */ import java.util.List;
/*   5:    */ 
/*   6:    */ public final class PooledByteBufAllocatorMetric
/*   7:    */   implements ByteBufAllocatorMetric
/*   8:    */ {
/*   9:    */   private final PooledByteBufAllocator allocator;
/*  10:    */   
/*  11:    */   PooledByteBufAllocatorMetric(PooledByteBufAllocator allocator)
/*  12:    */   {
/*  13: 31 */     this.allocator = allocator;
/*  14:    */   }
/*  15:    */   
/*  16:    */   public int numHeapArenas()
/*  17:    */   {
/*  18: 38 */     return this.allocator.numHeapArenas();
/*  19:    */   }
/*  20:    */   
/*  21:    */   public int numDirectArenas()
/*  22:    */   {
/*  23: 45 */     return this.allocator.numDirectArenas();
/*  24:    */   }
/*  25:    */   
/*  26:    */   public List<PoolArenaMetric> heapArenas()
/*  27:    */   {
/*  28: 52 */     return this.allocator.heapArenas();
/*  29:    */   }
/*  30:    */   
/*  31:    */   public List<PoolArenaMetric> directArenas()
/*  32:    */   {
/*  33: 59 */     return this.allocator.directArenas();
/*  34:    */   }
/*  35:    */   
/*  36:    */   public int numThreadLocalCaches()
/*  37:    */   {
/*  38: 66 */     return this.allocator.numThreadLocalCaches();
/*  39:    */   }
/*  40:    */   
/*  41:    */   public int tinyCacheSize()
/*  42:    */   {
/*  43: 73 */     return this.allocator.tinyCacheSize();
/*  44:    */   }
/*  45:    */   
/*  46:    */   public int smallCacheSize()
/*  47:    */   {
/*  48: 80 */     return this.allocator.smallCacheSize();
/*  49:    */   }
/*  50:    */   
/*  51:    */   public int normalCacheSize()
/*  52:    */   {
/*  53: 87 */     return this.allocator.normalCacheSize();
/*  54:    */   }
/*  55:    */   
/*  56:    */   public int chunkSize()
/*  57:    */   {
/*  58: 94 */     return this.allocator.chunkSize();
/*  59:    */   }
/*  60:    */   
/*  61:    */   public long usedHeapMemory()
/*  62:    */   {
/*  63: 99 */     return this.allocator.usedHeapMemory();
/*  64:    */   }
/*  65:    */   
/*  66:    */   public long usedDirectMemory()
/*  67:    */   {
/*  68:104 */     return this.allocator.usedDirectMemory();
/*  69:    */   }
/*  70:    */   
/*  71:    */   public String toString()
/*  72:    */   {
/*  73:109 */     StringBuilder sb = new StringBuilder(256);
/*  74:110 */     sb.append(StringUtil.simpleClassName(this))
/*  75:111 */       .append("(usedHeapMemory: ").append(usedHeapMemory())
/*  76:112 */       .append("; usedDirectMemory: ").append(usedDirectMemory())
/*  77:113 */       .append("; numHeapArenas: ").append(numHeapArenas())
/*  78:114 */       .append("; numDirectArenas: ").append(numDirectArenas())
/*  79:115 */       .append("; tinyCacheSize: ").append(tinyCacheSize())
/*  80:116 */       .append("; smallCacheSize: ").append(smallCacheSize())
/*  81:117 */       .append("; normalCacheSize: ").append(normalCacheSize())
/*  82:118 */       .append("; numThreadLocalCaches: ").append(numThreadLocalCaches())
/*  83:119 */       .append("; chunkSize: ").append(chunkSize()).append(')');
/*  84:120 */     return sb.toString();
/*  85:    */   }
/*  86:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.PooledByteBufAllocatorMetric
 * JD-Core Version:    0.7.0.1
 */