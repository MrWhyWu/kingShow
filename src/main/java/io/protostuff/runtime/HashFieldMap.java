/*  1:   */ package io.protostuff.runtime;
/*  2:   */ 
/*  3:   */ import java.util.ArrayList;
/*  4:   */ import java.util.Collection;
/*  5:   */ import java.util.Collections;
/*  6:   */ import java.util.Comparator;
/*  7:   */ import java.util.HashMap;
/*  8:   */ import java.util.List;
/*  9:   */ import java.util.Map;
/* 10:   */ 
/* 11:   */ final class HashFieldMap<T>
/* 12:   */   implements FieldMap<T>
/* 13:   */ {
/* 14:24 */   private static final FieldComparator FIELD_COMPARATOR = new FieldComparator(null);
/* 15:   */   private final List<Field<T>> fields;
/* 16:   */   private final Map<Integer, Field<T>> fieldsByNumber;
/* 17:   */   private final Map<String, Field<T>> fieldsByName;
/* 18:   */   
/* 19:   */   public HashFieldMap(Collection<Field<T>> fields)
/* 20:   */   {
/* 21:31 */     this.fieldsByName = new HashMap();
/* 22:32 */     this.fieldsByNumber = new HashMap();
/* 23:33 */     for (Field<T> f : fields)
/* 24:   */     {
/* 25:35 */       if (this.fieldsByName.containsKey(f.name))
/* 26:   */       {
/* 27:37 */         Field<T> prev = (Field)this.fieldsByName.get(f.name);
/* 28:38 */         throw new IllegalStateException(prev + " and " + f + " cannot have the same name.");
/* 29:   */       }
/* 30:40 */       if (this.fieldsByNumber.containsKey(Integer.valueOf(f.number)))
/* 31:   */       {
/* 32:42 */         Field<T> prev = (Field)this.fieldsByNumber.get(Integer.valueOf(f.number));
/* 33:43 */         throw new IllegalStateException(prev + " and " + f + " cannot have the same number.");
/* 34:   */       }
/* 35:45 */       this.fieldsByNumber.put(Integer.valueOf(f.number), f);
/* 36:46 */       this.fieldsByName.put(f.name, f);
/* 37:   */     }
/* 38:49 */     Object fieldList = new ArrayList(fields.size());
/* 39:50 */     ((List)fieldList).addAll(fields);
/* 40:51 */     Collections.sort((List)fieldList, FIELD_COMPARATOR);
/* 41:52 */     this.fields = Collections.unmodifiableList((List)fieldList);
/* 42:   */   }
/* 43:   */   
/* 44:   */   public Field<T> getFieldByNumber(int n)
/* 45:   */   {
/* 46:58 */     return (Field)this.fieldsByNumber.get(Integer.valueOf(n));
/* 47:   */   }
/* 48:   */   
/* 49:   */   public Field<T> getFieldByName(String fieldName)
/* 50:   */   {
/* 51:64 */     return (Field)this.fieldsByName.get(fieldName);
/* 52:   */   }
/* 53:   */   
/* 54:   */   public int getFieldCount()
/* 55:   */   {
/* 56:70 */     return this.fields.size();
/* 57:   */   }
/* 58:   */   
/* 59:   */   public List<Field<T>> getFields()
/* 60:   */   {
/* 61:76 */     return this.fields;
/* 62:   */   }
/* 63:   */   
/* 64:   */   private static class FieldComparator
/* 65:   */     implements Comparator<Field<?>>
/* 66:   */   {
/* 67:   */     public int compare(Field<?> o1, Field<?> o2)
/* 68:   */     {
/* 69:84 */       return compare(o1.number, o2.number);
/* 70:   */     }
/* 71:   */     
/* 72:   */     public static int compare(int x, int y)
/* 73:   */     {
/* 74:88 */       return x == y ? 0 : x < y ? -1 : 1;
/* 75:   */     }
/* 76:   */   }
/* 77:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.HashFieldMap
 * JD-Core Version:    0.7.0.1
 */