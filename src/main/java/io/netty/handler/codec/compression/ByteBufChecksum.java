/*   1:    */ package io.netty.handler.codec.compression;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.util.ByteProcessor;
/*   5:    */ import io.netty.util.internal.ObjectUtil;
/*   6:    */ import io.netty.util.internal.PlatformDependent;
/*   7:    */ import java.lang.reflect.Method;
/*   8:    */ import java.nio.ByteBuffer;
/*   9:    */ import java.util.zip.Adler32;
/*  10:    */ import java.util.zip.CRC32;
/*  11:    */ import java.util.zip.Checksum;
/*  12:    */ 
/*  13:    */ abstract class ByteBufChecksum
/*  14:    */   implements Checksum
/*  15:    */ {
/*  16: 42 */   private static final Method ADLER32_UPDATE_METHOD = updateByteBuffer(new Adler32());
/*  17: 43 */   private static final Method CRC32_UPDATE_METHOD = updateByteBuffer(new CRC32());
/*  18: 46 */   private final ByteProcessor updateProcessor = new ByteProcessor()
/*  19:    */   {
/*  20:    */     public boolean process(byte value)
/*  21:    */       throws Exception
/*  22:    */     {
/*  23: 49 */       ByteBufChecksum.this.update(value);
/*  24: 50 */       return true;
/*  25:    */     }
/*  26:    */   };
/*  27:    */   
/*  28:    */   private static Method updateByteBuffer(Checksum checksum)
/*  29:    */   {
/*  30: 55 */     if (PlatformDependent.javaVersion() >= 8) {
/*  31:    */       try
/*  32:    */       {
/*  33: 57 */         Method method = checksum.getClass().getDeclaredMethod("update", new Class[] { ByteBuffer.class });
/*  34: 58 */         method.invoke(method, new Object[] { ByteBuffer.allocate(1) });
/*  35: 59 */         return method;
/*  36:    */       }
/*  37:    */       catch (Throwable ignore)
/*  38:    */       {
/*  39: 61 */         return null;
/*  40:    */       }
/*  41:    */     }
/*  42: 64 */     return null;
/*  43:    */   }
/*  44:    */   
/*  45:    */   static ByteBufChecksum wrapChecksum(Checksum checksum)
/*  46:    */   {
/*  47: 68 */     ObjectUtil.checkNotNull(checksum, "checksum");
/*  48: 69 */     if (((checksum instanceof Adler32)) && (ADLER32_UPDATE_METHOD != null)) {
/*  49: 70 */       return new ReflectiveByteBufChecksum(checksum, ADLER32_UPDATE_METHOD);
/*  50:    */     }
/*  51: 72 */     if (((checksum instanceof CRC32)) && (CRC32_UPDATE_METHOD != null)) {
/*  52: 73 */       return new ReflectiveByteBufChecksum(checksum, CRC32_UPDATE_METHOD);
/*  53:    */     }
/*  54: 75 */     return new SlowByteBufChecksum(checksum);
/*  55:    */   }
/*  56:    */   
/*  57:    */   public void update(ByteBuf b, int off, int len)
/*  58:    */   {
/*  59: 82 */     if (b.hasArray()) {
/*  60: 83 */       update(b.array(), b.arrayOffset() + off, len);
/*  61:    */     } else {
/*  62: 85 */       b.forEachByte(off, len, this.updateProcessor);
/*  63:    */     }
/*  64:    */   }
/*  65:    */   
/*  66:    */   private static final class ReflectiveByteBufChecksum
/*  67:    */     extends ByteBufChecksum.SlowByteBufChecksum
/*  68:    */   {
/*  69:    */     private final Method method;
/*  70:    */     
/*  71:    */     ReflectiveByteBufChecksum(Checksum checksum, Method method)
/*  72:    */     {
/*  73: 93 */       super();
/*  74: 94 */       this.method = method;
/*  75:    */     }
/*  76:    */     
/*  77:    */     public void update(ByteBuf b, int off, int len)
/*  78:    */     {
/*  79: 99 */       if (b.hasArray()) {
/*  80:100 */         update(b.array(), b.arrayOffset() + off, len);
/*  81:    */       } else {
/*  82:    */         try
/*  83:    */         {
/*  84:103 */           this.method.invoke(this.checksum, new Object[] { CompressionUtil.safeNioBuffer(b) });
/*  85:    */         }
/*  86:    */         catch (Throwable cause)
/*  87:    */         {
/*  88:105 */           throw new Error();
/*  89:    */         }
/*  90:    */       }
/*  91:    */     }
/*  92:    */   }
/*  93:    */   
/*  94:    */   private static class SlowByteBufChecksum
/*  95:    */     extends ByteBufChecksum
/*  96:    */   {
/*  97:    */     protected final Checksum checksum;
/*  98:    */     
/*  99:    */     SlowByteBufChecksum(Checksum checksum)
/* 100:    */     {
/* 101:116 */       this.checksum = checksum;
/* 102:    */     }
/* 103:    */     
/* 104:    */     public void update(int b)
/* 105:    */     {
/* 106:121 */       this.checksum.update(b);
/* 107:    */     }
/* 108:    */     
/* 109:    */     public void update(byte[] b, int off, int len)
/* 110:    */     {
/* 111:126 */       this.checksum.update(b, off, len);
/* 112:    */     }
/* 113:    */     
/* 114:    */     public long getValue()
/* 115:    */     {
/* 116:131 */       return this.checksum.getValue();
/* 117:    */     }
/* 118:    */     
/* 119:    */     public void reset()
/* 120:    */     {
/* 121:136 */       this.checksum.reset();
/* 122:    */     }
/* 123:    */   }
/* 124:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.compression.ByteBufChecksum
 * JD-Core Version:    0.7.0.1
 */