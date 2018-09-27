/*  1:   */ package io.netty.util;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.ObjectUtil;
/*  4:   */ import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
/*  5:   */ 
/*  6:   */ public abstract class AbstractReferenceCounted
/*  7:   */   implements ReferenceCounted
/*  8:   */ {
/*  9:28 */   private static final AtomicIntegerFieldUpdater<AbstractReferenceCounted> refCntUpdater = AtomicIntegerFieldUpdater.newUpdater(AbstractReferenceCounted.class, "refCnt");
/* 10:30 */   private volatile int refCnt = 1;
/* 11:   */   
/* 12:   */   public final int refCnt()
/* 13:   */   {
/* 14:34 */     return this.refCnt;
/* 15:   */   }
/* 16:   */   
/* 17:   */   protected final void setRefCnt(int refCnt)
/* 18:   */   {
/* 19:41 */     refCntUpdater.set(this, refCnt);
/* 20:   */   }
/* 21:   */   
/* 22:   */   public ReferenceCounted retain()
/* 23:   */   {
/* 24:46 */     return retain0(1);
/* 25:   */   }
/* 26:   */   
/* 27:   */   public ReferenceCounted retain(int increment)
/* 28:   */   {
/* 29:51 */     return retain0(ObjectUtil.checkPositive(increment, "increment"));
/* 30:   */   }
/* 31:   */   
/* 32:   */   private ReferenceCounted retain0(int increment)
/* 33:   */   {
/* 34:55 */     int oldRef = refCntUpdater.getAndAdd(this, increment);
/* 35:56 */     if ((oldRef <= 0) || (oldRef + increment < oldRef))
/* 36:   */     {
/* 37:58 */       refCntUpdater.getAndAdd(this, -increment);
/* 38:59 */       throw new IllegalReferenceCountException(oldRef, increment);
/* 39:   */     }
/* 40:61 */     return this;
/* 41:   */   }
/* 42:   */   
/* 43:   */   public ReferenceCounted touch()
/* 44:   */   {
/* 45:66 */     return touch(null);
/* 46:   */   }
/* 47:   */   
/* 48:   */   public boolean release()
/* 49:   */   {
/* 50:71 */     return release0(1);
/* 51:   */   }
/* 52:   */   
/* 53:   */   public boolean release(int decrement)
/* 54:   */   {
/* 55:76 */     return release0(ObjectUtil.checkPositive(decrement, "decrement"));
/* 56:   */   }
/* 57:   */   
/* 58:   */   private boolean release0(int decrement)
/* 59:   */   {
/* 60:80 */     int oldRef = refCntUpdater.getAndAdd(this, -decrement);
/* 61:81 */     if (oldRef == decrement)
/* 62:   */     {
/* 63:82 */       deallocate();
/* 64:83 */       return true;
/* 65:   */     }
/* 66:84 */     if ((oldRef < decrement) || (oldRef - decrement > oldRef))
/* 67:   */     {
/* 68:86 */       refCntUpdater.getAndAdd(this, decrement);
/* 69:87 */       throw new IllegalReferenceCountException(oldRef, decrement);
/* 70:   */     }
/* 71:89 */     return false;
/* 72:   */   }
/* 73:   */   
/* 74:   */   protected abstract void deallocate();
/* 75:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.AbstractReferenceCounted
 * JD-Core Version:    0.7.0.1
 */