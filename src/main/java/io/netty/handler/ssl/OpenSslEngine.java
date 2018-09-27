/*  1:   */ package io.netty.handler.ssl;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBufAllocator;
/*  4:   */ 
/*  5:   */ public final class OpenSslEngine
/*  6:   */   extends ReferenceCountedOpenSslEngine
/*  7:   */ {
/*  8:   */   OpenSslEngine(OpenSslContext context, ByteBufAllocator alloc, String peerHost, int peerPort, boolean jdkCompatibilityMode)
/*  9:   */   {
/* 10:32 */     super(context, alloc, peerHost, peerPort, jdkCompatibilityMode, false);
/* 11:   */   }
/* 12:   */   
/* 13:   */   protected void finalize()
/* 14:   */     throws Throwable
/* 15:   */   {
/* 16:38 */     super.finalize();
/* 17:39 */     OpenSsl.releaseIfNeeded(this);
/* 18:   */   }
/* 19:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.OpenSslEngine
 * JD-Core Version:    0.7.0.1
 */