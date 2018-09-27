/*  1:   */ package io.netty.handler.codec.compression;
/*  2:   */ 
/*  3:   */ import com.jcraft.jzlib.Deflater;
/*  4:   */ import com.jcraft.jzlib.Inflater;
/*  5:   */ import com.jcraft.jzlib.JZlib;
/*  6:   */ import com.jcraft.jzlib.JZlib.WrapperType;
/*  7:   */ 
/*  8:   */ final class ZlibUtil
/*  9:   */ {
/* 10:   */   static void fail(Inflater z, String message, int resultCode)
/* 11:   */   {
/* 12:28 */     throw inflaterException(z, message, resultCode);
/* 13:   */   }
/* 14:   */   
/* 15:   */   static void fail(Deflater z, String message, int resultCode)
/* 16:   */   {
/* 17:32 */     throw deflaterException(z, message, resultCode);
/* 18:   */   }
/* 19:   */   
/* 20:   */   static DecompressionException inflaterException(Inflater z, String message, int resultCode)
/* 21:   */   {
/* 22:36 */     return new DecompressionException(message + " (" + resultCode + ')' + (z.msg != null ? ": " + z.msg : ""));
/* 23:   */   }
/* 24:   */   
/* 25:   */   static CompressionException deflaterException(Deflater z, String message, int resultCode)
/* 26:   */   {
/* 27:40 */     return new CompressionException(message + " (" + resultCode + ')' + (z.msg != null ? ": " + z.msg : ""));
/* 28:   */   }
/* 29:   */   
/* 30:   */   static JZlib.WrapperType convertWrapperType(ZlibWrapper wrapper)
/* 31:   */   {
/* 32:   */     JZlib.WrapperType convertedWrapperType;
/* 33:   */     JZlib.WrapperType convertedWrapperType;
/* 34:   */     JZlib.WrapperType convertedWrapperType;
/* 35:   */     JZlib.WrapperType convertedWrapperType;
/* 36:45 */     switch (1.$SwitchMap$io$netty$handler$codec$compression$ZlibWrapper[wrapper.ordinal()])
/* 37:   */     {
/* 38:   */     case 1: 
/* 39:47 */       convertedWrapperType = JZlib.W_NONE;
/* 40:48 */       break;
/* 41:   */     case 2: 
/* 42:50 */       convertedWrapperType = JZlib.W_ZLIB;
/* 43:51 */       break;
/* 44:   */     case 3: 
/* 45:53 */       convertedWrapperType = JZlib.W_GZIP;
/* 46:54 */       break;
/* 47:   */     case 4: 
/* 48:56 */       convertedWrapperType = JZlib.W_ANY;
/* 49:57 */       break;
/* 50:   */     default: 
/* 51:59 */       throw new Error();
/* 52:   */     }
/* 53:   */     JZlib.WrapperType convertedWrapperType;
/* 54:61 */     return convertedWrapperType;
/* 55:   */   }
/* 56:   */   
/* 57:   */   static int wrapperOverhead(ZlibWrapper wrapper)
/* 58:   */   {
/* 59:   */     int overhead;
/* 60:   */     int overhead;
/* 61:   */     int overhead;
/* 62:66 */     switch (1.$SwitchMap$io$netty$handler$codec$compression$ZlibWrapper[wrapper.ordinal()])
/* 63:   */     {
/* 64:   */     case 1: 
/* 65:68 */       overhead = 0;
/* 66:69 */       break;
/* 67:   */     case 2: 
/* 68:   */     case 4: 
/* 69:72 */       overhead = 2;
/* 70:73 */       break;
/* 71:   */     case 3: 
/* 72:75 */       overhead = 10;
/* 73:76 */       break;
/* 74:   */     default: 
/* 75:78 */       throw new Error();
/* 76:   */     }
/* 77:   */     int overhead;
/* 78:80 */     return overhead;
/* 79:   */   }
/* 80:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.compression.ZlibUtil
 * JD-Core Version:    0.7.0.1
 */