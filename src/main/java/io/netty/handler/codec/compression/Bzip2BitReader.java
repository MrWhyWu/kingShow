/*   1:    */ package io.netty.handler.codec.compression;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ 
/*   5:    */ class Bzip2BitReader
/*   6:    */ {
/*   7:    */   private static final int MAX_COUNT_OF_READABLE_BYTES = 268435455;
/*   8:    */   private ByteBuf in;
/*   9:    */   private long bitBuffer;
/*  10:    */   private int bitCount;
/*  11:    */   
/*  12:    */   void setByteBuf(ByteBuf in)
/*  13:    */   {
/*  14: 50 */     this.in = in;
/*  15:    */   }
/*  16:    */   
/*  17:    */   int readBits(int count)
/*  18:    */   {
/*  19: 59 */     if ((count < 0) || (count > 32)) {
/*  20: 60 */       throw new IllegalArgumentException("count: " + count + " (expected: 0-32 )");
/*  21:    */     }
/*  22: 62 */     int bitCount = this.bitCount;
/*  23: 63 */     long bitBuffer = this.bitBuffer;
/*  24: 65 */     if (bitCount < count)
/*  25:    */     {
/*  26:    */       int offset;
/*  27:    */       int offset;
/*  28:    */       int offset;
/*  29:    */       long readData;
/*  30:    */       int offset;
/*  31: 68 */       switch (this.in.readableBytes())
/*  32:    */       {
/*  33:    */       case 1: 
/*  34: 70 */         long readData = this.in.readUnsignedByte();
/*  35: 71 */         offset = 8;
/*  36: 72 */         break;
/*  37:    */       case 2: 
/*  38: 75 */         long readData = this.in.readUnsignedShort();
/*  39: 76 */         offset = 16;
/*  40: 77 */         break;
/*  41:    */       case 3: 
/*  42: 80 */         long readData = this.in.readUnsignedMedium();
/*  43: 81 */         offset = 24;
/*  44: 82 */         break;
/*  45:    */       default: 
/*  46: 85 */         readData = this.in.readUnsignedInt();
/*  47: 86 */         offset = 32;
/*  48:    */       }
/*  49: 91 */       bitBuffer = bitBuffer << offset | readData;
/*  50: 92 */       bitCount += offset;
/*  51: 93 */       this.bitBuffer = bitBuffer;
/*  52:    */     }
/*  53: 96 */     this.bitCount = (bitCount -= count);
/*  54: 97 */     return (int)(bitBuffer >>> bitCount & (count != 32 ? (1 << count) - 1 : 4294967295L));
/*  55:    */   }
/*  56:    */   
/*  57:    */   boolean readBoolean()
/*  58:    */   {
/*  59:105 */     return readBits(1) != 0;
/*  60:    */   }
/*  61:    */   
/*  62:    */   int readInt()
/*  63:    */   {
/*  64:113 */     return readBits(32);
/*  65:    */   }
/*  66:    */   
/*  67:    */   void refill()
/*  68:    */   {
/*  69:120 */     int readData = this.in.readUnsignedByte();
/*  70:121 */     this.bitBuffer = (this.bitBuffer << 8 | readData);
/*  71:122 */     this.bitCount += 8;
/*  72:    */   }
/*  73:    */   
/*  74:    */   boolean isReadable()
/*  75:    */   {
/*  76:130 */     return (this.bitCount > 0) || (this.in.isReadable());
/*  77:    */   }
/*  78:    */   
/*  79:    */   boolean hasReadableBits(int count)
/*  80:    */   {
/*  81:139 */     if (count < 0) {
/*  82:140 */       throw new IllegalArgumentException("count: " + count + " (expected value greater than 0)");
/*  83:    */     }
/*  84:142 */     return (this.bitCount >= count) || ((this.in.readableBytes() << 3 & 0x7FFFFFFF) >= count - this.bitCount);
/*  85:    */   }
/*  86:    */   
/*  87:    */   boolean hasReadableBytes(int count)
/*  88:    */   {
/*  89:151 */     if ((count < 0) || (count > 268435455)) {
/*  90:152 */       throw new IllegalArgumentException("count: " + count + " (expected: 0-" + 268435455 + ')');
/*  91:    */     }
/*  92:155 */     return hasReadableBits(count << 3);
/*  93:    */   }
/*  94:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.compression.Bzip2BitReader
 * JD-Core Version:    0.7.0.1
 */