/*  1:   */ package io.netty.handler.ssl;
/*  2:   */ 
/*  3:   */ import java.security.AlgorithmConstraints;
/*  4:   */ import javax.net.ssl.SSLParameters;
/*  5:   */ 
/*  6:   */ final class Java7SslParametersUtils
/*  7:   */ {
/*  8:   */   static void setAlgorithmConstraints(SSLParameters sslParameters, Object algorithmConstraints)
/*  9:   */   {
/* 10:33 */     sslParameters.setAlgorithmConstraints((AlgorithmConstraints)algorithmConstraints);
/* 11:   */   }
/* 12:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.Java7SslParametersUtils
 * JD-Core Version:    0.7.0.1
 */