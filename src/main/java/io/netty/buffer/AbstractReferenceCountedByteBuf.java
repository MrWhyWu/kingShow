/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.IllegalReferenceCountException;
/*   4:    */ import io.netty.util.internal.ObjectUtil;
/*   5:    */ import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
/*   6:    */ 
/*   7:    */ public abstract class AbstractReferenceCountedByteBuf
/*   8:    */   extends AbstractByteBuf
/*   9:    */ {
/*  10: 31 */   private static final AtomicIntegerFieldUpdater<AbstractReferenceCountedByteBuf> refCntUpdater = AtomicIntegerFieldUpdater.newUpdater(AbstractReferenceCountedByteBuf.class, "refCnt");
/*  11:    */   private volatile int refCnt;
/*  12:    */   
/*  13:    */   protected AbstractReferenceCountedByteBuf(int maxCapacity)
/*  14:    */   {
/*  15: 36 */     super(maxCapacity);
/*  16: 37 */     refCntUpdater.set(this, 1);
/*  17:    */   }
/*  18:    */   
/*  19:    */   public int refCnt()
/*  20:    */   {
/*  21: 42 */     return this.refCnt;
/*  22:    */   }
/*  23:    */   
/*  24:    */   protected final void setRefCnt(int refCnt)
/*  25:    */   {
/*  26: 49 */     refCntUpdater.set(this, refCnt);
/*  27:    */   }
/*  28:    */   
/*  29:    */   public ByteBuf retain()
/*  30:    */   {
/*  31: 54 */     return retain0(1);
/*  32:    */   }
/*  33:    */   
/*  34:    */   public ByteBuf retain(int increment)
/*  35:    */   {
/*  36: 59 */     return retain0(ObjectUtil.checkPositive(increment, "increment"));
/*  37:    */   }
/*  38:    */   
/*  39:    */   private ByteBuf retain0(int increment)
/*  40:    */   {
/*  41: 63 */     int oldRef = refCntUpdater.getAndAdd(this, increment);
/*  42: 64 */     if ((oldRef <= 0) || (oldRef + increment < oldRef))
/*  43:    */     {
/*  44: 66 */       refCntUpdater.getAndAdd(this, -increment);
/*  45: 67 */       throw new IllegalReferenceCountException(oldRef, increment);
/*  46:    */     }
/*  47: 69 */     return this;
/*  48:    */   }
/*  49:    */   
/*  50:    */   public ByteBuf touch()
/*  51:    */   {
/*  52: 74 */     return this;
/*  53:    */   }
/*  54:    */   
/*  55:    */   public ByteBuf touch(Object hint)
/*  56:    */   {
/*  57: 79 */     return this;
/*  58:    */   }
/*  59:    */   
/*  60:    */   public boolean release()
/*  61:    */   {
/*  62: 84 */     return release0(1);
/*  63:    */   }
/*  64:    */   
/*  65:    */   public boolean release(int decrement)
/*  66:    */   {
/*  67: 89 */     return release0(ObjectUtil.checkPositive(decrement, "decrement"));
/*  68:    */   }
/*  69:    */   
/*  70:    */   private boolean release0(int decrement)
/*  71:    */   {
/*  72: 93 */     int oldRef = refCntUpdater.getAndAdd(this, -decrement);
/*  73: 94 */     if (oldRef == decrement)
/*  74:    */     {
/*  75: 95 */       deallocate();
/*  76: 96 */       return true;
/*  77:    */     }
/*  78: 97 */     if ((oldRef < decrement) || (oldRef - decrement > oldRef))
/*  79:    */     {
/*  80: 99 */       refCntUpdater.getAndAdd(this, decrement);
/*  81:100 */       throw new IllegalReferenceCountException(oldRef, decrement);
/*  82:    */     }
/*  83:102 */     return false;
/*  84:    */   }
/*  85:    */   
/*  86:    */   protected abstract void deallocate();
/*  87:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.AbstractReferenceCountedByteBuf
 * JD-Core Version:    0.7.0.1
 */