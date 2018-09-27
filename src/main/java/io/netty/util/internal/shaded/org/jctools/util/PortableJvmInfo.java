/*  1:   */ package io.netty.util.internal.shaded.org.jctools.util;
/*  2:   */ 
/*  3:   */ public abstract interface PortableJvmInfo
/*  4:   */ {
/*  5: 8 */   public static final int CACHE_LINE_SIZE = Integer.getInteger("jctools.cacheLineSize", 64).intValue();
/*  6: 9 */   public static final int CPUs = Runtime.getRuntime().availableProcessors();
/*  7:10 */   public static final int RECOMENDED_OFFER_BATCH = CPUs * 4;
/*  8:11 */   public static final int RECOMENDED_POLL_BATCH = CPUs * 4;
/*  9:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.util.PortableJvmInfo
 * JD-Core Version:    0.7.0.1
 */