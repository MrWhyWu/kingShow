/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.ResourceLeakTracker;
/*   4:    */ import io.netty.util.internal.ObjectUtil;
/*   5:    */ import java.nio.ByteOrder;
/*   6:    */ 
/*   7:    */ class SimpleLeakAwareCompositeByteBuf
/*   8:    */   extends WrappedCompositeByteBuf
/*   9:    */ {
/*  10:    */   final ResourceLeakTracker<ByteBuf> leak;
/*  11:    */   
/*  12:    */   SimpleLeakAwareCompositeByteBuf(CompositeByteBuf wrapped, ResourceLeakTracker<ByteBuf> leak)
/*  13:    */   {
/*  14: 29 */     super(wrapped);
/*  15: 30 */     this.leak = ((ResourceLeakTracker)ObjectUtil.checkNotNull(leak, "leak"));
/*  16:    */   }
/*  17:    */   
/*  18:    */   public boolean release()
/*  19:    */   {
/*  20: 37 */     ByteBuf unwrapped = unwrap();
/*  21: 38 */     if (super.release())
/*  22:    */     {
/*  23: 39 */       closeLeak(unwrapped);
/*  24: 40 */       return true;
/*  25:    */     }
/*  26: 42 */     return false;
/*  27:    */   }
/*  28:    */   
/*  29:    */   public boolean release(int decrement)
/*  30:    */   {
/*  31: 49 */     ByteBuf unwrapped = unwrap();
/*  32: 50 */     if (super.release(decrement))
/*  33:    */     {
/*  34: 51 */       closeLeak(unwrapped);
/*  35: 52 */       return true;
/*  36:    */     }
/*  37: 54 */     return false;
/*  38:    */   }
/*  39:    */   
/*  40:    */   private void closeLeak(ByteBuf trackedByteBuf)
/*  41:    */   {
/*  42: 60 */     boolean closed = this.leak.close(trackedByteBuf);
/*  43: 61 */     assert (closed);
/*  44:    */   }
/*  45:    */   
/*  46:    */   public ByteBuf order(ByteOrder endianness)
/*  47:    */   {
/*  48: 66 */     if (order() == endianness) {
/*  49: 67 */       return this;
/*  50:    */     }
/*  51: 69 */     return newLeakAwareByteBuf(super.order(endianness));
/*  52:    */   }
/*  53:    */   
/*  54:    */   public ByteBuf slice()
/*  55:    */   {
/*  56: 75 */     return newLeakAwareByteBuf(super.slice());
/*  57:    */   }
/*  58:    */   
/*  59:    */   public ByteBuf retainedSlice()
/*  60:    */   {
/*  61: 80 */     return newLeakAwareByteBuf(super.retainedSlice());
/*  62:    */   }
/*  63:    */   
/*  64:    */   public ByteBuf slice(int index, int length)
/*  65:    */   {
/*  66: 85 */     return newLeakAwareByteBuf(super.slice(index, length));
/*  67:    */   }
/*  68:    */   
/*  69:    */   public ByteBuf retainedSlice(int index, int length)
/*  70:    */   {
/*  71: 90 */     return newLeakAwareByteBuf(super.retainedSlice(index, length));
/*  72:    */   }
/*  73:    */   
/*  74:    */   public ByteBuf duplicate()
/*  75:    */   {
/*  76: 95 */     return newLeakAwareByteBuf(super.duplicate());
/*  77:    */   }
/*  78:    */   
/*  79:    */   public ByteBuf retainedDuplicate()
/*  80:    */   {
/*  81:100 */     return newLeakAwareByteBuf(super.retainedDuplicate());
/*  82:    */   }
/*  83:    */   
/*  84:    */   public ByteBuf readSlice(int length)
/*  85:    */   {
/*  86:105 */     return newLeakAwareByteBuf(super.readSlice(length));
/*  87:    */   }
/*  88:    */   
/*  89:    */   public ByteBuf readRetainedSlice(int length)
/*  90:    */   {
/*  91:110 */     return newLeakAwareByteBuf(super.readRetainedSlice(length));
/*  92:    */   }
/*  93:    */   
/*  94:    */   public ByteBuf asReadOnly()
/*  95:    */   {
/*  96:115 */     return newLeakAwareByteBuf(super.asReadOnly());
/*  97:    */   }
/*  98:    */   
/*  99:    */   private SimpleLeakAwareByteBuf newLeakAwareByteBuf(ByteBuf wrapped)
/* 100:    */   {
/* 101:119 */     return newLeakAwareByteBuf(wrapped, unwrap(), this.leak);
/* 102:    */   }
/* 103:    */   
/* 104:    */   protected SimpleLeakAwareByteBuf newLeakAwareByteBuf(ByteBuf wrapped, ByteBuf trackedByteBuf, ResourceLeakTracker<ByteBuf> leakTracker)
/* 105:    */   {
/* 106:124 */     return new SimpleLeakAwareByteBuf(wrapped, trackedByteBuf, leakTracker);
/* 107:    */   }
/* 108:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.SimpleLeakAwareCompositeByteBuf
 * JD-Core Version:    0.7.0.1
 */