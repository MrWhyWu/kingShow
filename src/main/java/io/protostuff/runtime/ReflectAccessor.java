/*  1:   */ package io.protostuff.runtime;
/*  2:   */ 
/*  3:   */ import java.lang.reflect.Field;
/*  4:   */ 
/*  5:   */ public final class ReflectAccessor
/*  6:   */   extends Accessor
/*  7:   */ {
/*  8:26 */   static final Accessor.Factory FACTORY = new Accessor.Factory()
/*  9:   */   {
/* 10:   */     public Accessor create(Field f)
/* 11:   */     {
/* 12:30 */       return new ReflectAccessor(f);
/* 13:   */     }
/* 14:   */   };
/* 15:   */   
/* 16:   */   public ReflectAccessor(Field f)
/* 17:   */   {
/* 18:37 */     super(f);
/* 19:38 */     f.setAccessible(true);
/* 20:   */   }
/* 21:   */   
/* 22:   */   public void set(Object owner, Object value)
/* 23:   */   {
/* 24:   */     try
/* 25:   */     {
/* 26:46 */       this.f.set(owner, value);
/* 27:   */     }
/* 28:   */     catch (IllegalArgumentException e)
/* 29:   */     {
/* 30:50 */       throw new RuntimeException(e);
/* 31:   */     }
/* 32:   */     catch (IllegalAccessException e)
/* 33:   */     {
/* 34:54 */       throw new RuntimeException(e);
/* 35:   */     }
/* 36:   */   }
/* 37:   */   
/* 38:   */   public <T> T get(Object owner)
/* 39:   */   {
/* 40:   */     try
/* 41:   */     {
/* 42:64 */       return this.f.get(owner);
/* 43:   */     }
/* 44:   */     catch (IllegalArgumentException e)
/* 45:   */     {
/* 46:68 */       throw new RuntimeException(e);
/* 47:   */     }
/* 48:   */     catch (IllegalAccessException e)
/* 49:   */     {
/* 50:72 */       throw new RuntimeException(e);
/* 51:   */     }
/* 52:   */   }
/* 53:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.ReflectAccessor
 * JD-Core Version:    0.7.0.1
 */