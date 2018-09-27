/*  1:   */ package io.netty.util.internal;
/*  2:   */ 
/*  3:   */ import io.netty.util.Recycler;
/*  4:   */ import io.netty.util.Recycler.Handle;
/*  5:   */ import io.netty.util.ReferenceCountUtil;
/*  6:   */ import io.netty.util.concurrent.Promise;
/*  7:   */ 
/*  8:   */ public final class PendingWrite
/*  9:   */ {
/* 10:26 */   private static final Recycler<PendingWrite> RECYCLER = new Recycler()
/* 11:   */   {
/* 12:   */     protected PendingWrite newObject(Recycler.Handle<PendingWrite> handle)
/* 13:   */     {
/* 14:29 */       return new PendingWrite(handle, null);
/* 15:   */     }
/* 16:   */   };
/* 17:   */   private final Recycler.Handle<PendingWrite> handle;
/* 18:   */   private Object msg;
/* 19:   */   private Promise<Void> promise;
/* 20:   */   
/* 21:   */   public static PendingWrite newInstance(Object msg, Promise<Void> promise)
/* 22:   */   {
/* 23:37 */     PendingWrite pending = (PendingWrite)RECYCLER.get();
/* 24:38 */     pending.msg = msg;
/* 25:39 */     pending.promise = promise;
/* 26:40 */     return pending;
/* 27:   */   }
/* 28:   */   
/* 29:   */   private PendingWrite(Recycler.Handle<PendingWrite> handle)
/* 30:   */   {
/* 31:48 */     this.handle = handle;
/* 32:   */   }
/* 33:   */   
/* 34:   */   public boolean recycle()
/* 35:   */   {
/* 36:55 */     this.msg = null;
/* 37:56 */     this.promise = null;
/* 38:57 */     this.handle.recycle(this);
/* 39:58 */     return true;
/* 40:   */   }
/* 41:   */   
/* 42:   */   public boolean failAndRecycle(Throwable cause)
/* 43:   */   {
/* 44:65 */     ReferenceCountUtil.release(this.msg);
/* 45:66 */     if (this.promise != null) {
/* 46:67 */       this.promise.setFailure(cause);
/* 47:   */     }
/* 48:69 */     return recycle();
/* 49:   */   }
/* 50:   */   
/* 51:   */   public boolean successAndRecycle()
/* 52:   */   {
/* 53:76 */     if (this.promise != null) {
/* 54:77 */       this.promise.setSuccess(null);
/* 55:   */     }
/* 56:79 */     return recycle();
/* 57:   */   }
/* 58:   */   
/* 59:   */   public Object msg()
/* 60:   */   {
/* 61:83 */     return this.msg;
/* 62:   */   }
/* 63:   */   
/* 64:   */   public Promise<Void> promise()
/* 65:   */   {
/* 66:87 */     return this.promise;
/* 67:   */   }
/* 68:   */   
/* 69:   */   public Promise<Void> recycleAndGet()
/* 70:   */   {
/* 71:94 */     Promise<Void> promise = this.promise;
/* 72:95 */     recycle();
/* 73:96 */     return promise;
/* 74:   */   }
/* 75:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.PendingWrite
 * JD-Core Version:    0.7.0.1
 */