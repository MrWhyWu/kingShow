/*  1:   */ package io.netty.handler.ssl.util;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.PlatformDependent;
/*  4:   */ import java.security.SecureRandom;
/*  5:   */ import java.util.Random;
/*  6:   */ 
/*  7:   */ final class ThreadLocalInsecureRandom
/*  8:   */   extends SecureRandom
/*  9:   */ {
/* 10:   */   private static final long serialVersionUID = -8209473337192526191L;
/* 11:32 */   private static final SecureRandom INSTANCE = new ThreadLocalInsecureRandom();
/* 12:   */   
/* 13:   */   static SecureRandom current()
/* 14:   */   {
/* 15:35 */     return INSTANCE;
/* 16:   */   }
/* 17:   */   
/* 18:   */   public String getAlgorithm()
/* 19:   */   {
/* 20:42 */     return "insecure";
/* 21:   */   }
/* 22:   */   
/* 23:   */   public void setSeed(byte[] seed) {}
/* 24:   */   
/* 25:   */   public void setSeed(long seed) {}
/* 26:   */   
/* 27:   */   public void nextBytes(byte[] bytes)
/* 28:   */   {
/* 29:53 */     random().nextBytes(bytes);
/* 30:   */   }
/* 31:   */   
/* 32:   */   public byte[] generateSeed(int numBytes)
/* 33:   */   {
/* 34:58 */     byte[] seed = new byte[numBytes];
/* 35:59 */     random().nextBytes(seed);
/* 36:60 */     return seed;
/* 37:   */   }
/* 38:   */   
/* 39:   */   public int nextInt()
/* 40:   */   {
/* 41:65 */     return random().nextInt();
/* 42:   */   }
/* 43:   */   
/* 44:   */   public int nextInt(int n)
/* 45:   */   {
/* 46:70 */     return random().nextInt(n);
/* 47:   */   }
/* 48:   */   
/* 49:   */   public boolean nextBoolean()
/* 50:   */   {
/* 51:75 */     return random().nextBoolean();
/* 52:   */   }
/* 53:   */   
/* 54:   */   public long nextLong()
/* 55:   */   {
/* 56:80 */     return random().nextLong();
/* 57:   */   }
/* 58:   */   
/* 59:   */   public float nextFloat()
/* 60:   */   {
/* 61:85 */     return random().nextFloat();
/* 62:   */   }
/* 63:   */   
/* 64:   */   public double nextDouble()
/* 65:   */   {
/* 66:90 */     return random().nextDouble();
/* 67:   */   }
/* 68:   */   
/* 69:   */   public double nextGaussian()
/* 70:   */   {
/* 71:95 */     return random().nextGaussian();
/* 72:   */   }
/* 73:   */   
/* 74:   */   private static Random random()
/* 75:   */   {
/* 76:99 */     return PlatformDependent.threadLocalRandom();
/* 77:   */   }
/* 78:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.util.ThreadLocalInsecureRandom
 * JD-Core Version:    0.7.0.1
 */