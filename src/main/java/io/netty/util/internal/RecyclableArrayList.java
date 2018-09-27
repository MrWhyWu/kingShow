/*   1:    */ package io.netty.util.internal;
/*   2:    */ 
/*   3:    */ import io.netty.util.Recycler;
/*   4:    */ import io.netty.util.Recycler.Handle;
/*   5:    */ import java.util.ArrayList;
/*   6:    */ import java.util.Collection;
/*   7:    */ import java.util.List;
/*   8:    */ import java.util.RandomAccess;
/*   9:    */ 
/*  10:    */ public final class RecyclableArrayList
/*  11:    */   extends ArrayList<Object>
/*  12:    */ {
/*  13:    */   private static final long serialVersionUID = -8605125654176467947L;
/*  14:    */   private static final int DEFAULT_INITIAL_CAPACITY = 8;
/*  15: 36 */   private static final Recycler<RecyclableArrayList> RECYCLER = new Recycler()
/*  16:    */   {
/*  17:    */     protected RecyclableArrayList newObject(Recycler.Handle<RecyclableArrayList> handle)
/*  18:    */     {
/*  19: 39 */       return new RecyclableArrayList(handle, null);
/*  20:    */     }
/*  21:    */   };
/*  22:    */   private boolean insertSinceRecycled;
/*  23:    */   private final Recycler.Handle<RecyclableArrayList> handle;
/*  24:    */   
/*  25:    */   public static RecyclableArrayList newInstance()
/*  26:    */   {
/*  27: 49 */     return newInstance(8);
/*  28:    */   }
/*  29:    */   
/*  30:    */   public static RecyclableArrayList newInstance(int minCapacity)
/*  31:    */   {
/*  32: 56 */     RecyclableArrayList ret = (RecyclableArrayList)RECYCLER.get();
/*  33: 57 */     ret.ensureCapacity(minCapacity);
/*  34: 58 */     return ret;
/*  35:    */   }
/*  36:    */   
/*  37:    */   private RecyclableArrayList(Recycler.Handle<RecyclableArrayList> handle)
/*  38:    */   {
/*  39: 64 */     this(handle, 8);
/*  40:    */   }
/*  41:    */   
/*  42:    */   private RecyclableArrayList(Recycler.Handle<RecyclableArrayList> handle, int initialCapacity)
/*  43:    */   {
/*  44: 68 */     super(initialCapacity);
/*  45: 69 */     this.handle = handle;
/*  46:    */   }
/*  47:    */   
/*  48:    */   public boolean addAll(Collection<?> c)
/*  49:    */   {
/*  50: 74 */     checkNullElements(c);
/*  51: 75 */     if (super.addAll(c))
/*  52:    */     {
/*  53: 76 */       this.insertSinceRecycled = true;
/*  54: 77 */       return true;
/*  55:    */     }
/*  56: 79 */     return false;
/*  57:    */   }
/*  58:    */   
/*  59:    */   public boolean addAll(int index, Collection<?> c)
/*  60:    */   {
/*  61: 84 */     checkNullElements(c);
/*  62: 85 */     if (super.addAll(index, c))
/*  63:    */     {
/*  64: 86 */       this.insertSinceRecycled = true;
/*  65: 87 */       return true;
/*  66:    */     }
/*  67: 89 */     return false;
/*  68:    */   }
/*  69:    */   
/*  70:    */   private static void checkNullElements(Collection<?> c)
/*  71:    */   {
/*  72:    */     List<?> list;
/*  73: 93 */     if (((c instanceof RandomAccess)) && ((c instanceof List)))
/*  74:    */     {
/*  75: 95 */       list = (List)c;
/*  76: 96 */       int size = list.size();
/*  77: 97 */       for (int i = 0; i < size; i++) {
/*  78: 98 */         if (list.get(i) == null) {
/*  79: 99 */           throw new IllegalArgumentException("c contains null values");
/*  80:    */         }
/*  81:    */       }
/*  82:    */     }
/*  83:    */     else
/*  84:    */     {
/*  85:103 */       for (Object element : c) {
/*  86:104 */         if (element == null) {
/*  87:105 */           throw new IllegalArgumentException("c contains null values");
/*  88:    */         }
/*  89:    */       }
/*  90:    */     }
/*  91:    */   }
/*  92:    */   
/*  93:    */   public boolean add(Object element)
/*  94:    */   {
/*  95:113 */     if (element == null) {
/*  96:114 */       throw new NullPointerException("element");
/*  97:    */     }
/*  98:116 */     if (super.add(element))
/*  99:    */     {
/* 100:117 */       this.insertSinceRecycled = true;
/* 101:118 */       return true;
/* 102:    */     }
/* 103:120 */     return false;
/* 104:    */   }
/* 105:    */   
/* 106:    */   public void add(int index, Object element)
/* 107:    */   {
/* 108:125 */     if (element == null) {
/* 109:126 */       throw new NullPointerException("element");
/* 110:    */     }
/* 111:128 */     super.add(index, element);
/* 112:129 */     this.insertSinceRecycled = true;
/* 113:    */   }
/* 114:    */   
/* 115:    */   public Object set(int index, Object element)
/* 116:    */   {
/* 117:134 */     if (element == null) {
/* 118:135 */       throw new NullPointerException("element");
/* 119:    */     }
/* 120:137 */     Object old = super.set(index, element);
/* 121:138 */     this.insertSinceRecycled = true;
/* 122:139 */     return old;
/* 123:    */   }
/* 124:    */   
/* 125:    */   public boolean insertSinceRecycled()
/* 126:    */   {
/* 127:146 */     return this.insertSinceRecycled;
/* 128:    */   }
/* 129:    */   
/* 130:    */   public boolean recycle()
/* 131:    */   {
/* 132:153 */     clear();
/* 133:154 */     this.insertSinceRecycled = false;
/* 134:155 */     this.handle.recycle(this);
/* 135:156 */     return true;
/* 136:    */   }
/* 137:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.RecyclableArrayList
 * JD-Core Version:    0.7.0.1
 */