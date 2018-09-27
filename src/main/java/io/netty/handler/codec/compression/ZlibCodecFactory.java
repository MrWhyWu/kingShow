/*   1:    */ package io.netty.handler.codec.compression;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.PlatformDependent;
/*   4:    */ import io.netty.util.internal.SystemPropertyUtil;
/*   5:    */ import io.netty.util.internal.logging.InternalLogger;
/*   6:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   7:    */ 
/*   8:    */ public final class ZlibCodecFactory
/*   9:    */ {
/*  10: 27 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(ZlibCodecFactory.class);
/*  11:    */   private static final int DEFAULT_JDK_WINDOW_SIZE = 15;
/*  12:    */   private static final int DEFAULT_JDK_MEM_LEVEL = 8;
/*  13: 37 */   private static final boolean noJdkZlibDecoder = SystemPropertyUtil.getBoolean("io.netty.noJdkZlibDecoder", 
/*  14: 38 */     PlatformDependent.javaVersion() < 7);
/*  15:    */   private static final boolean noJdkZlibEncoder;
/*  16:    */   
/*  17:    */   static
/*  18:    */   {
/*  19: 39 */     logger.debug("-Dio.netty.noJdkZlibDecoder: {}", Boolean.valueOf(noJdkZlibDecoder));
/*  20:    */     
/*  21: 41 */     noJdkZlibEncoder = SystemPropertyUtil.getBoolean("io.netty.noJdkZlibEncoder", false);
/*  22: 42 */     logger.debug("-Dio.netty.noJdkZlibEncoder: {}", Boolean.valueOf(noJdkZlibEncoder));
/*  23:    */   }
/*  24:    */   
/*  25: 44 */   private static final boolean supportsWindowSizeAndMemLevel = (noJdkZlibDecoder) || (PlatformDependent.javaVersion() >= 7);
/*  26:    */   
/*  27:    */   public static boolean isSupportingWindowSizeAndMemLevel()
/*  28:    */   {
/*  29: 51 */     return supportsWindowSizeAndMemLevel;
/*  30:    */   }
/*  31:    */   
/*  32:    */   public static ZlibEncoder newZlibEncoder(int compressionLevel)
/*  33:    */   {
/*  34: 55 */     if ((PlatformDependent.javaVersion() < 7) || (noJdkZlibEncoder)) {
/*  35: 56 */       return new JZlibEncoder(compressionLevel);
/*  36:    */     }
/*  37: 58 */     return new JdkZlibEncoder(compressionLevel);
/*  38:    */   }
/*  39:    */   
/*  40:    */   public static ZlibEncoder newZlibEncoder(ZlibWrapper wrapper)
/*  41:    */   {
/*  42: 63 */     if ((PlatformDependent.javaVersion() < 7) || (noJdkZlibEncoder)) {
/*  43: 64 */       return new JZlibEncoder(wrapper);
/*  44:    */     }
/*  45: 66 */     return new JdkZlibEncoder(wrapper);
/*  46:    */   }
/*  47:    */   
/*  48:    */   public static ZlibEncoder newZlibEncoder(ZlibWrapper wrapper, int compressionLevel)
/*  49:    */   {
/*  50: 71 */     if ((PlatformDependent.javaVersion() < 7) || (noJdkZlibEncoder)) {
/*  51: 72 */       return new JZlibEncoder(wrapper, compressionLevel);
/*  52:    */     }
/*  53: 74 */     return new JdkZlibEncoder(wrapper, compressionLevel);
/*  54:    */   }
/*  55:    */   
/*  56:    */   public static ZlibEncoder newZlibEncoder(ZlibWrapper wrapper, int compressionLevel, int windowBits, int memLevel)
/*  57:    */   {
/*  58: 79 */     if ((PlatformDependent.javaVersion() < 7) || (noJdkZlibEncoder) || (windowBits != 15) || (memLevel != 8)) {
/*  59: 81 */       return new JZlibEncoder(wrapper, compressionLevel, windowBits, memLevel);
/*  60:    */     }
/*  61: 83 */     return new JdkZlibEncoder(wrapper, compressionLevel);
/*  62:    */   }
/*  63:    */   
/*  64:    */   public static ZlibEncoder newZlibEncoder(byte[] dictionary)
/*  65:    */   {
/*  66: 88 */     if ((PlatformDependent.javaVersion() < 7) || (noJdkZlibEncoder)) {
/*  67: 89 */       return new JZlibEncoder(dictionary);
/*  68:    */     }
/*  69: 91 */     return new JdkZlibEncoder(dictionary);
/*  70:    */   }
/*  71:    */   
/*  72:    */   public static ZlibEncoder newZlibEncoder(int compressionLevel, byte[] dictionary)
/*  73:    */   {
/*  74: 96 */     if ((PlatformDependent.javaVersion() < 7) || (noJdkZlibEncoder)) {
/*  75: 97 */       return new JZlibEncoder(compressionLevel, dictionary);
/*  76:    */     }
/*  77: 99 */     return new JdkZlibEncoder(compressionLevel, dictionary);
/*  78:    */   }
/*  79:    */   
/*  80:    */   public static ZlibEncoder newZlibEncoder(int compressionLevel, int windowBits, int memLevel, byte[] dictionary)
/*  81:    */   {
/*  82:104 */     if ((PlatformDependent.javaVersion() < 7) || (noJdkZlibEncoder) || (windowBits != 15) || (memLevel != 8)) {
/*  83:106 */       return new JZlibEncoder(compressionLevel, windowBits, memLevel, dictionary);
/*  84:    */     }
/*  85:108 */     return new JdkZlibEncoder(compressionLevel, dictionary);
/*  86:    */   }
/*  87:    */   
/*  88:    */   public static ZlibDecoder newZlibDecoder()
/*  89:    */   {
/*  90:113 */     if ((PlatformDependent.javaVersion() < 7) || (noJdkZlibDecoder)) {
/*  91:114 */       return new JZlibDecoder();
/*  92:    */     }
/*  93:116 */     return new JdkZlibDecoder();
/*  94:    */   }
/*  95:    */   
/*  96:    */   public static ZlibDecoder newZlibDecoder(ZlibWrapper wrapper)
/*  97:    */   {
/*  98:121 */     if ((PlatformDependent.javaVersion() < 7) || (noJdkZlibDecoder)) {
/*  99:122 */       return new JZlibDecoder(wrapper);
/* 100:    */     }
/* 101:124 */     return new JdkZlibDecoder(wrapper);
/* 102:    */   }
/* 103:    */   
/* 104:    */   public static ZlibDecoder newZlibDecoder(byte[] dictionary)
/* 105:    */   {
/* 106:129 */     if ((PlatformDependent.javaVersion() < 7) || (noJdkZlibDecoder)) {
/* 107:130 */       return new JZlibDecoder(dictionary);
/* 108:    */     }
/* 109:132 */     return new JdkZlibDecoder(dictionary);
/* 110:    */   }
/* 111:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.compression.ZlibCodecFactory
 * JD-Core Version:    0.7.0.1
 */