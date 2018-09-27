/*  1:   */ package io.protostuff.runtime;
/*  2:   */ 
/*  3:   */ import java.lang.reflect.Field;
/*  4:   */ 
/*  5:   */ public abstract class Accessor
/*  6:   */ {
/*  7:   */   public final Field f;
/*  8:   */   
/*  9:   */   protected Accessor(Field f)
/* 10:   */   {
/* 11:35 */     this.f = f;
/* 12:   */   }
/* 13:   */   
/* 14:   */   public abstract void set(Object paramObject1, Object paramObject2);
/* 15:   */   
/* 16:   */   public abstract <T> T get(Object paramObject);
/* 17:   */   
/* 18:   */   public static abstract interface Factory
/* 19:   */   {
/* 20:   */     public abstract Accessor create(Field paramField);
/* 21:   */   }
/* 22:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.Accessor
 * JD-Core Version:    0.7.0.1
 */