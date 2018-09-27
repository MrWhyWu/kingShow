/*   1:    */ package io.netty.channel.unix;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.EmptyArrays;
/*   4:    */ import java.io.FileNotFoundException;
/*   5:    */ import java.io.IOException;
/*   6:    */ import java.net.ConnectException;
/*   7:    */ import java.net.NoRouteToHostException;
/*   8:    */ import java.nio.channels.AlreadyConnectedException;
/*   9:    */ import java.nio.channels.ClosedChannelException;
/*  10:    */ import java.nio.channels.ConnectionPendingException;
/*  11:    */ import java.nio.channels.NotYetConnectedException;
/*  12:    */ 
/*  13:    */ public final class Errors
/*  14:    */ {
/*  15: 37 */   public static final int ERRNO_ENOENT_NEGATIVE = -ErrorsStaticallyReferencedJniMethods.errnoENOENT();
/*  16: 38 */   public static final int ERRNO_ENOTCONN_NEGATIVE = -ErrorsStaticallyReferencedJniMethods.errnoENOTCONN();
/*  17: 39 */   public static final int ERRNO_EBADF_NEGATIVE = -ErrorsStaticallyReferencedJniMethods.errnoEBADF();
/*  18: 40 */   public static final int ERRNO_EPIPE_NEGATIVE = -ErrorsStaticallyReferencedJniMethods.errnoEPIPE();
/*  19: 41 */   public static final int ERRNO_ECONNRESET_NEGATIVE = -ErrorsStaticallyReferencedJniMethods.errnoECONNRESET();
/*  20: 42 */   public static final int ERRNO_EAGAIN_NEGATIVE = -ErrorsStaticallyReferencedJniMethods.errnoEAGAIN();
/*  21: 43 */   public static final int ERRNO_EWOULDBLOCK_NEGATIVE = -ErrorsStaticallyReferencedJniMethods.errnoEWOULDBLOCK();
/*  22: 44 */   public static final int ERRNO_EINPROGRESS_NEGATIVE = -ErrorsStaticallyReferencedJniMethods.errnoEINPROGRESS();
/*  23: 45 */   public static final int ERROR_ECONNREFUSED_NEGATIVE = -ErrorsStaticallyReferencedJniMethods.errorECONNREFUSED();
/*  24: 46 */   public static final int ERROR_EISCONN_NEGATIVE = -ErrorsStaticallyReferencedJniMethods.errorEISCONN();
/*  25: 47 */   public static final int ERROR_EALREADY_NEGATIVE = -ErrorsStaticallyReferencedJniMethods.errorEALREADY();
/*  26: 48 */   public static final int ERROR_ENETUNREACH_NEGATIVE = -ErrorsStaticallyReferencedJniMethods.errorENETUNREACH();
/*  27: 57 */   private static final String[] ERRORS = new String[512];
/*  28:    */   
/*  29:    */   public static final class NativeIoException
/*  30:    */     extends IOException
/*  31:    */   {
/*  32:    */     private static final long serialVersionUID = 8222160204268655526L;
/*  33:    */     private final int expectedErr;
/*  34:    */     
/*  35:    */     public NativeIoException(String method, int expectedErr)
/*  36:    */     {
/*  37: 66 */       super();
/*  38: 67 */       this.expectedErr = expectedErr;
/*  39:    */     }
/*  40:    */     
/*  41:    */     public int expectedErr()
/*  42:    */     {
/*  43: 71 */       return this.expectedErr;
/*  44:    */     }
/*  45:    */   }
/*  46:    */   
/*  47:    */   static final class NativeConnectException
/*  48:    */     extends ConnectException
/*  49:    */   {
/*  50:    */     private static final long serialVersionUID = -5532328671712318161L;
/*  51:    */     private final int expectedErr;
/*  52:    */     
/*  53:    */     NativeConnectException(String method, int expectedErr)
/*  54:    */     {
/*  55: 79 */       super();
/*  56: 80 */       this.expectedErr = expectedErr;
/*  57:    */     }
/*  58:    */     
/*  59:    */     int expectedErr()
/*  60:    */     {
/*  61: 84 */       return this.expectedErr;
/*  62:    */     }
/*  63:    */   }
/*  64:    */   
/*  65:    */   static
/*  66:    */   {
/*  67: 89 */     for (int i = 0; i < ERRORS.length; i++) {
/*  68: 91 */       ERRORS[i] = ErrorsStaticallyReferencedJniMethods.strError(i);
/*  69:    */     }
/*  70:    */   }
/*  71:    */   
/*  72:    */   static void throwConnectException(String method, NativeConnectException refusedCause, int err)
/*  73:    */     throws IOException
/*  74:    */   {
/*  75: 97 */     if (err == refusedCause.expectedErr()) {
/*  76: 98 */       throw refusedCause;
/*  77:    */     }
/*  78:100 */     if (err == ERROR_EALREADY_NEGATIVE) {
/*  79:101 */       throw new ConnectionPendingException();
/*  80:    */     }
/*  81:103 */     if (err == ERROR_ENETUNREACH_NEGATIVE) {
/*  82:104 */       throw new NoRouteToHostException();
/*  83:    */     }
/*  84:106 */     if (err == ERROR_EISCONN_NEGATIVE) {
/*  85:107 */       throw new AlreadyConnectedException();
/*  86:    */     }
/*  87:109 */     if (err == ERRNO_ENOENT_NEGATIVE) {
/*  88:110 */       throw new FileNotFoundException();
/*  89:    */     }
/*  90:112 */     throw new ConnectException(method + "(..) failed: " + ERRORS[(-err)]);
/*  91:    */   }
/*  92:    */   
/*  93:    */   public static NativeIoException newConnectionResetException(String method, int errnoNegative)
/*  94:    */   {
/*  95:116 */     NativeIoException exception = newIOException(method, errnoNegative);
/*  96:117 */     exception.setStackTrace(EmptyArrays.EMPTY_STACK_TRACE);
/*  97:118 */     return exception;
/*  98:    */   }
/*  99:    */   
/* 100:    */   public static NativeIoException newIOException(String method, int err)
/* 101:    */   {
/* 102:122 */     return new NativeIoException(method, err);
/* 103:    */   }
/* 104:    */   
/* 105:    */   public static int ioResult(String method, int err, NativeIoException resetCause, ClosedChannelException closedCause)
/* 106:    */     throws IOException
/* 107:    */   {
/* 108:128 */     if ((err == ERRNO_EAGAIN_NEGATIVE) || (err == ERRNO_EWOULDBLOCK_NEGATIVE)) {
/* 109:129 */       return 0;
/* 110:    */     }
/* 111:131 */     if (err == resetCause.expectedErr()) {
/* 112:132 */       throw resetCause;
/* 113:    */     }
/* 114:134 */     if (err == ERRNO_EBADF_NEGATIVE) {
/* 115:135 */       throw closedCause;
/* 116:    */     }
/* 117:137 */     if (err == ERRNO_ENOTCONN_NEGATIVE) {
/* 118:138 */       throw new NotYetConnectedException();
/* 119:    */     }
/* 120:140 */     if (err == ERRNO_ENOENT_NEGATIVE) {
/* 121:141 */       throw new FileNotFoundException();
/* 122:    */     }
/* 123:146 */     throw newIOException(method, err);
/* 124:    */   }
/* 125:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.unix.Errors
 * JD-Core Version:    0.7.0.1
 */