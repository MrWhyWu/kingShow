/*  1:   */ package io.protostuff.runtime;
/*  2:   */ 
/*  3:   */ import java.lang.reflect.Field;
/*  4:   */ import sun.misc.Unsafe;
/*  5:   */ 
/*  6:   */ public final class UnsafeAccessor
/*  7:   */   extends Accessor
/*  8:   */ {
/*  9:27 */   static final Accessor.Factory FACTORY = new Accessor.Factory()
/* 10:   */   {
/* 11:   */     public Accessor create(Field f)
/* 12:   */     {
/* 13:31 */       return new UnsafeAccessor(f);
/* 14:   */     }
/* 15:   */   };
/* 16:   */   public final long offset;
/* 17:   */   
/* 18:   */   public UnsafeAccessor(Field f)
/* 19:   */   {
/* 20:39 */     super(f);
/* 21:40 */     this.offset = RuntimeUnsafeFieldFactory.us.objectFieldOffset(f);
/* 22:   */   }
/* 23:   */   
/* 24:   */   public void set(Object owner, Object value)
/* 25:   */   {
/* 26:46 */     RuntimeUnsafeFieldFactory.us.putObject(owner, this.offset, value);
/* 27:   */   }
/* 28:   */   
/* 29:   */   public <T> T get(Object owner)
/* 30:   */   {
/* 31:53 */     return RuntimeUnsafeFieldFactory.us.getObject(owner, this.offset);
/* 32:   */   }
/* 33:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.UnsafeAccessor
 * JD-Core Version:    0.7.0.1
 */