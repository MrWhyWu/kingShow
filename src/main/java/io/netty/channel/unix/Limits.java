/*  1:   */ package io.netty.channel.unix;
/*  2:   */ 
/*  3:   */ public final class Limits
/*  4:   */ {
/*  5:24 */   public static final int IOV_MAX = ;
/*  6:25 */   public static final int UIO_MAX_IOV = LimitsStaticallyReferencedJniMethods.uioMaxIov();
/*  7:26 */   public static final long SSIZE_MAX = LimitsStaticallyReferencedJniMethods.ssizeMax();
/*  8:28 */   public static final int SIZEOF_JLONG = LimitsStaticallyReferencedJniMethods.sizeOfjlong();
/*  9:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.unix.Limits
 * JD-Core Version:    0.7.0.1
 */