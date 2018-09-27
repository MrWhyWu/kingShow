/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import java.nio.ByteOrder;
/*   4:    */ 
/*   5:    */ final class UnreleasableByteBuf
/*   6:    */   extends WrappedByteBuf
/*   7:    */ {
/*   8:    */   private SwappedByteBuf swappedBuf;
/*   9:    */   
/*  10:    */   UnreleasableByteBuf(ByteBuf buf)
/*  11:    */   {
/*  12: 29 */     super((buf instanceof UnreleasableByteBuf) ? buf.unwrap() : buf);
/*  13:    */   }
/*  14:    */   
/*  15:    */   public ByteBuf order(ByteOrder endianness)
/*  16:    */   {
/*  17: 34 */     if (endianness == null) {
/*  18: 35 */       throw new NullPointerException("endianness");
/*  19:    */     }
/*  20: 37 */     if (endianness == order()) {
/*  21: 38 */       return this;
/*  22:    */     }
/*  23: 41 */     SwappedByteBuf swappedBuf = this.swappedBuf;
/*  24: 42 */     if (swappedBuf == null) {
/*  25: 43 */       this.swappedBuf = (swappedBuf = new SwappedByteBuf(this));
/*  26:    */     }
/*  27: 45 */     return swappedBuf;
/*  28:    */   }
/*  29:    */   
/*  30:    */   public ByteBuf asReadOnly()
/*  31:    */   {
/*  32: 50 */     return this.buf.isReadOnly() ? this : new UnreleasableByteBuf(this.buf.asReadOnly());
/*  33:    */   }
/*  34:    */   
/*  35:    */   public ByteBuf readSlice(int length)
/*  36:    */   {
/*  37: 55 */     return new UnreleasableByteBuf(this.buf.readSlice(length));
/*  38:    */   }
/*  39:    */   
/*  40:    */   public ByteBuf readRetainedSlice(int length)
/*  41:    */   {
/*  42: 63 */     return readSlice(length);
/*  43:    */   }
/*  44:    */   
/*  45:    */   public ByteBuf slice()
/*  46:    */   {
/*  47: 68 */     return new UnreleasableByteBuf(this.buf.slice());
/*  48:    */   }
/*  49:    */   
/*  50:    */   public ByteBuf retainedSlice()
/*  51:    */   {
/*  52: 76 */     return slice();
/*  53:    */   }
/*  54:    */   
/*  55:    */   public ByteBuf slice(int index, int length)
/*  56:    */   {
/*  57: 81 */     return new UnreleasableByteBuf(this.buf.slice(index, length));
/*  58:    */   }
/*  59:    */   
/*  60:    */   public ByteBuf retainedSlice(int index, int length)
/*  61:    */   {
/*  62: 89 */     return slice(index, length);
/*  63:    */   }
/*  64:    */   
/*  65:    */   public ByteBuf duplicate()
/*  66:    */   {
/*  67: 94 */     return new UnreleasableByteBuf(this.buf.duplicate());
/*  68:    */   }
/*  69:    */   
/*  70:    */   public ByteBuf retainedDuplicate()
/*  71:    */   {
/*  72:102 */     return duplicate();
/*  73:    */   }
/*  74:    */   
/*  75:    */   public ByteBuf retain(int increment)
/*  76:    */   {
/*  77:107 */     return this;
/*  78:    */   }
/*  79:    */   
/*  80:    */   public ByteBuf retain()
/*  81:    */   {
/*  82:112 */     return this;
/*  83:    */   }
/*  84:    */   
/*  85:    */   public ByteBuf touch()
/*  86:    */   {
/*  87:117 */     return this;
/*  88:    */   }
/*  89:    */   
/*  90:    */   public ByteBuf touch(Object hint)
/*  91:    */   {
/*  92:122 */     return this;
/*  93:    */   }
/*  94:    */   
/*  95:    */   public boolean release()
/*  96:    */   {
/*  97:127 */     return false;
/*  98:    */   }
/*  99:    */   
/* 100:    */   public boolean release(int decrement)
/* 101:    */   {
/* 102:132 */     return false;
/* 103:    */   }
/* 104:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.UnreleasableByteBuf
 * JD-Core Version:    0.7.0.1
 */