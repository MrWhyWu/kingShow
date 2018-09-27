/*   1:    */ package io.protostuff;
/*   2:    */ 
/*   3:    */ import java.io.FilterInputStream;
/*   4:    */ import java.io.IOException;
/*   5:    */ import java.io.InputStream;
/*   6:    */ 
/*   7:    */ public final class LimitedInputStream
/*   8:    */   extends FilterInputStream
/*   9:    */ {
/*  10:    */   private int limit;
/*  11:    */   
/*  12:    */   public LimitedInputStream(InputStream in)
/*  13:    */   {
/*  14: 64 */     super(in);
/*  15:    */   }
/*  16:    */   
/*  17:    */   public LimitedInputStream(InputStream in, int limit)
/*  18:    */   {
/*  19: 69 */     super(in);
/*  20: 70 */     this.limit = limit;
/*  21:    */   }
/*  22:    */   
/*  23:    */   LimitedInputStream limit(int limit)
/*  24:    */   {
/*  25: 75 */     this.limit = limit;
/*  26: 76 */     return this;
/*  27:    */   }
/*  28:    */   
/*  29:    */   public int available()
/*  30:    */     throws IOException
/*  31:    */   {
/*  32: 82 */     return Math.min(super.available(), this.limit);
/*  33:    */   }
/*  34:    */   
/*  35:    */   public int read()
/*  36:    */     throws IOException
/*  37:    */   {
/*  38: 88 */     if (this.limit <= 0) {
/*  39: 90 */       return -1;
/*  40:    */     }
/*  41: 92 */     int result = super.read();
/*  42: 93 */     if (result >= 0) {
/*  43: 95 */       this.limit -= 1;
/*  44:    */     }
/*  45: 97 */     return result;
/*  46:    */   }
/*  47:    */   
/*  48:    */   public int read(byte[] b, int off, int len)
/*  49:    */     throws IOException
/*  50:    */   {
/*  51:103 */     if (this.limit <= 0) {
/*  52:105 */       return -1;
/*  53:    */     }
/*  54:107 */     len = Math.min(len, this.limit);
/*  55:108 */     int result = super.read(b, off, len);
/*  56:109 */     if (result >= 0) {
/*  57:111 */       this.limit -= result;
/*  58:    */     }
/*  59:113 */     return result;
/*  60:    */   }
/*  61:    */   
/*  62:    */   public long skip(long n)
/*  63:    */     throws IOException
/*  64:    */   {
/*  65:119 */     long result = super.skip(Math.min(n, this.limit));
/*  66:120 */     if (result >= 0L) {
/*  67:122 */       this.limit = ((int)(this.limit - result));
/*  68:    */     }
/*  69:124 */     return result;
/*  70:    */   }
/*  71:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.LimitedInputStream
 * JD-Core Version:    0.7.0.1
 */