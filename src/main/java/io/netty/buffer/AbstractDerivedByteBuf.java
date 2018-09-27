/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import java.nio.ByteBuffer;
/*   4:    */ 
/*   5:    */ @Deprecated
/*   6:    */ public abstract class AbstractDerivedByteBuf
/*   7:    */   extends AbstractByteBuf
/*   8:    */ {
/*   9:    */   protected AbstractDerivedByteBuf(int maxCapacity)
/*  10:    */   {
/*  11: 31 */     super(maxCapacity);
/*  12:    */   }
/*  13:    */   
/*  14:    */   public final int refCnt()
/*  15:    */   {
/*  16: 36 */     return refCnt0();
/*  17:    */   }
/*  18:    */   
/*  19:    */   int refCnt0()
/*  20:    */   {
/*  21: 40 */     return unwrap().refCnt();
/*  22:    */   }
/*  23:    */   
/*  24:    */   public final ByteBuf retain()
/*  25:    */   {
/*  26: 45 */     return retain0();
/*  27:    */   }
/*  28:    */   
/*  29:    */   ByteBuf retain0()
/*  30:    */   {
/*  31: 49 */     unwrap().retain();
/*  32: 50 */     return this;
/*  33:    */   }
/*  34:    */   
/*  35:    */   public final ByteBuf retain(int increment)
/*  36:    */   {
/*  37: 55 */     return retain0(increment);
/*  38:    */   }
/*  39:    */   
/*  40:    */   ByteBuf retain0(int increment)
/*  41:    */   {
/*  42: 59 */     unwrap().retain(increment);
/*  43: 60 */     return this;
/*  44:    */   }
/*  45:    */   
/*  46:    */   public final ByteBuf touch()
/*  47:    */   {
/*  48: 65 */     return touch0();
/*  49:    */   }
/*  50:    */   
/*  51:    */   ByteBuf touch0()
/*  52:    */   {
/*  53: 69 */     unwrap().touch();
/*  54: 70 */     return this;
/*  55:    */   }
/*  56:    */   
/*  57:    */   public final ByteBuf touch(Object hint)
/*  58:    */   {
/*  59: 75 */     return touch0(hint);
/*  60:    */   }
/*  61:    */   
/*  62:    */   ByteBuf touch0(Object hint)
/*  63:    */   {
/*  64: 79 */     unwrap().touch(hint);
/*  65: 80 */     return this;
/*  66:    */   }
/*  67:    */   
/*  68:    */   public final boolean release()
/*  69:    */   {
/*  70: 85 */     return release0();
/*  71:    */   }
/*  72:    */   
/*  73:    */   boolean release0()
/*  74:    */   {
/*  75: 89 */     return unwrap().release();
/*  76:    */   }
/*  77:    */   
/*  78:    */   public final boolean release(int decrement)
/*  79:    */   {
/*  80: 94 */     return release0(decrement);
/*  81:    */   }
/*  82:    */   
/*  83:    */   boolean release0(int decrement)
/*  84:    */   {
/*  85: 98 */     return unwrap().release(decrement);
/*  86:    */   }
/*  87:    */   
/*  88:    */   public boolean isReadOnly()
/*  89:    */   {
/*  90:103 */     return unwrap().isReadOnly();
/*  91:    */   }
/*  92:    */   
/*  93:    */   public ByteBuffer internalNioBuffer(int index, int length)
/*  94:    */   {
/*  95:108 */     return nioBuffer(index, length);
/*  96:    */   }
/*  97:    */   
/*  98:    */   public ByteBuffer nioBuffer(int index, int length)
/*  99:    */   {
/* 100:113 */     return unwrap().nioBuffer(index, length);
/* 101:    */   }
/* 102:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.AbstractDerivedByteBuf
 * JD-Core Version:    0.7.0.1
 */