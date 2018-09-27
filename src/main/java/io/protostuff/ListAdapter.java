/*  1:   */ package io.protostuff;
/*  2:   */ 
/*  3:   */ import java.util.AbstractList;
/*  4:   */ import java.util.List;
/*  5:   */ 
/*  6:   */ public final class ListAdapter<F, T>
/*  7:   */   extends AbstractList<T>
/*  8:   */ {
/*  9:   */   private final List<F> fromList;
/* 10:   */   private final Converter<F, T> converter;
/* 11:   */   
/* 12:   */   public ListAdapter(List<F> fromList, Converter<F, T> converter)
/* 13:   */   {
/* 14:48 */     this.fromList = fromList;
/* 15:49 */     this.converter = converter;
/* 16:   */   }
/* 17:   */   
/* 18:   */   public T get(int index)
/* 19:   */   {
/* 20:55 */     return this.converter.convert(this.fromList.get(index));
/* 21:   */   }
/* 22:   */   
/* 23:   */   public int size()
/* 24:   */   {
/* 25:61 */     return this.fromList.size();
/* 26:   */   }
/* 27:   */   
/* 28:   */   public static abstract interface Converter<F, T>
/* 29:   */   {
/* 30:   */     public abstract T convert(F paramF);
/* 31:   */   }
/* 32:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.ListAdapter
 * JD-Core Version:    0.7.0.1
 */