/*  1:   */ package io.protostuff.runtime;
/*  2:   */ 
/*  3:   */ import java.util.ArrayList;
/*  4:   */ import java.util.Collection;
/*  5:   */ import java.util.Collections;
/*  6:   */ import java.util.HashMap;
/*  7:   */ import java.util.Iterator;
/*  8:   */ import java.util.List;
/*  9:   */ import java.util.Map;
/* 10:   */ 
/* 11:   */ final class ArrayFieldMap<T>
/* 12:   */   implements FieldMap<T>
/* 13:   */ {
/* 14:   */   private final List<Field<T>> fields;
/* 15:   */   private final Field<T>[] fieldsByNumber;
/* 16:   */   private final Map<String, Field<T>> fieldsByName;
/* 17:   */   
/* 18:   */   public ArrayFieldMap(Collection<Field<T>> fields, int lastFieldNumber)
/* 19:   */   {
/* 20:32 */     this.fieldsByName = new HashMap();
/* 21:33 */     this.fieldsByNumber = ((Field[])new Field[lastFieldNumber + 1]);
/* 22:34 */     for (Iterator localIterator = fields.iterator(); localIterator.hasNext();)
/* 23:   */     {
/* 24:34 */       f = (Field)localIterator.next();
/* 25:   */       
/* 26:36 */       last = (Field)this.fieldsByName.put(f.name, f);
/* 27:37 */       if (last != null) {
/* 28:39 */         throw new IllegalStateException(last + " and " + f + " cannot have the same name.");
/* 29:   */       }
/* 30:42 */       if (this.fieldsByNumber[f.number] != null) {
/* 31:44 */         throw new IllegalStateException(this.fieldsByNumber[f.number] + " and " + f + " cannot have the same number.");
/* 32:   */       }
/* 33:48 */       this.fieldsByNumber[f.number] = f;
/* 34:   */     }
/* 35:51 */     Object fieldList = new ArrayList(fields.size());
/* 36:52 */     Field<T> f = this.fieldsByNumber;Field<T> last = f.length;
/* 37:52 */     for (Field<T> localField1 = 0; localField1 < last; localField1++)
/* 38:   */     {
/* 39:52 */       Field<T> field = f[localField1];
/* 40:54 */       if (field != null) {
/* 41:55 */         ((List)fieldList).add(field);
/* 42:   */       }
/* 43:   */     }
/* 44:57 */     this.fields = Collections.unmodifiableList((List)fieldList);
/* 45:   */   }
/* 46:   */   
/* 47:   */   public Field<T> getFieldByNumber(int n)
/* 48:   */   {
/* 49:63 */     return n < this.fieldsByNumber.length ? this.fieldsByNumber[n] : null;
/* 50:   */   }
/* 51:   */   
/* 52:   */   public Field<T> getFieldByName(String fieldName)
/* 53:   */   {
/* 54:69 */     return (Field)this.fieldsByName.get(fieldName);
/* 55:   */   }
/* 56:   */   
/* 57:   */   public int getFieldCount()
/* 58:   */   {
/* 59:78 */     return this.fields.size();
/* 60:   */   }
/* 61:   */   
/* 62:   */   public List<Field<T>> getFields()
/* 63:   */   {
/* 64:84 */     return this.fields;
/* 65:   */   }
/* 66:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.ArrayFieldMap
 * JD-Core Version:    0.7.0.1
 */