/*   1:    */ package io.netty.handler.codec.compression;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ 
/*   5:    */ final class Bzip2BitWriter
/*   6:    */ {
/*   7:    */   private long bitBuffer;
/*   8:    */   private int bitCount;
/*   9:    */   
/*  10:    */   void writeBits(ByteBuf out, int count, long value)
/*  11:    */   {
/*  12: 42 */     if ((count < 0) || (count > 32)) {
/*  13: 43 */       throw new IllegalArgumentException("count: " + count + " (expected: 0-32)");
/*  14:    */     }
/*  15: 45 */     int bitCount = this.bitCount;
/*  16: 46 */     long bitBuffer = this.bitBuffer | value << 64 - count >>> bitCount;
/*  17: 47 */     bitCount += count;
/*  18: 49 */     if (bitCount >= 32)
/*  19:    */     {
/*  20: 50 */       out.writeInt((int)(bitBuffer >>> 32));
/*  21: 51 */       bitBuffer <<= 32;
/*  22: 52 */       bitCount -= 32;
/*  23:    */     }
/*  24: 54 */     this.bitBuffer = bitBuffer;
/*  25: 55 */     this.bitCount = bitCount;
/*  26:    */   }
/*  27:    */   
/*  28:    */   void writeBoolean(ByteBuf out, boolean value)
/*  29:    */   {
/*  30: 63 */     int bitCount = this.bitCount + 1;
/*  31: 64 */     long bitBuffer = this.bitBuffer | (value ? 1L << 64 - bitCount : 0L);
/*  32: 66 */     if (bitCount == 32)
/*  33:    */     {
/*  34: 67 */       out.writeInt((int)(bitBuffer >>> 32));
/*  35: 68 */       bitBuffer = 0L;
/*  36: 69 */       bitCount = 0;
/*  37:    */     }
/*  38: 71 */     this.bitBuffer = bitBuffer;
/*  39: 72 */     this.bitCount = bitCount;
/*  40:    */   }
/*  41:    */   
/*  42:    */   void writeUnary(ByteBuf out, int value)
/*  43:    */   {
/*  44: 81 */     if (value < 0) {
/*  45: 82 */       throw new IllegalArgumentException("value: " + value + " (expected 0 or more)");
/*  46:    */     }
/*  47: 84 */     while (value-- > 0) {
/*  48: 85 */       writeBoolean(out, true);
/*  49:    */     }
/*  50: 87 */     writeBoolean(out, false);
/*  51:    */   }
/*  52:    */   
/*  53:    */   void writeInt(ByteBuf out, int value)
/*  54:    */   {
/*  55: 95 */     writeBits(out, 32, value);
/*  56:    */   }
/*  57:    */   
/*  58:    */   void flush(ByteBuf out)
/*  59:    */   {
/*  60:103 */     int bitCount = this.bitCount;
/*  61:105 */     if (bitCount > 0)
/*  62:    */     {
/*  63:106 */       long bitBuffer = this.bitBuffer;
/*  64:107 */       int shiftToRight = 64 - bitCount;
/*  65:109 */       if (bitCount <= 8) {
/*  66:110 */         out.writeByte((int)(bitBuffer >>> shiftToRight << 8 - bitCount));
/*  67:111 */       } else if (bitCount <= 16) {
/*  68:112 */         out.writeShort((int)(bitBuffer >>> shiftToRight << 16 - bitCount));
/*  69:113 */       } else if (bitCount <= 24) {
/*  70:114 */         out.writeMedium((int)(bitBuffer >>> shiftToRight << 24 - bitCount));
/*  71:    */       } else {
/*  72:116 */         out.writeInt((int)(bitBuffer >>> shiftToRight << 32 - bitCount));
/*  73:    */       }
/*  74:    */     }
/*  75:    */   }
/*  76:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.compression.Bzip2BitWriter
 * JD-Core Version:    0.7.0.1
 */