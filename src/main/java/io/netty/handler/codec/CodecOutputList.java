/*   1:    */ package io.netty.handler.codec;
/*   2:    */ 
/*   3:    */ import io.netty.util.Recycler;
/*   4:    */ import io.netty.util.Recycler.Handle;
/*   5:    */ import io.netty.util.internal.ObjectUtil;
/*   6:    */ import java.util.AbstractList;
/*   7:    */ import java.util.RandomAccess;
/*   8:    */ 
/*   9:    */ final class CodecOutputList
/*  10:    */   extends AbstractList<Object>
/*  11:    */   implements RandomAccess
/*  12:    */ {
/*  13: 30 */   private static final Recycler<CodecOutputList> RECYCLER = new Recycler()
/*  14:    */   {
/*  15:    */     protected CodecOutputList newObject(Recycler.Handle<CodecOutputList> handle)
/*  16:    */     {
/*  17: 33 */       return new CodecOutputList(handle, null);
/*  18:    */     }
/*  19:    */   };
/*  20:    */   private final Recycler.Handle<CodecOutputList> handle;
/*  21:    */   private int size;
/*  22:    */   
/*  23:    */   static CodecOutputList newInstance()
/*  24:    */   {
/*  25: 38 */     return (CodecOutputList)RECYCLER.get();
/*  26:    */   }
/*  27:    */   
/*  28: 44 */   private Object[] array = new Object[16];
/*  29:    */   private boolean insertSinceRecycled;
/*  30:    */   
/*  31:    */   private CodecOutputList(Recycler.Handle<CodecOutputList> handle)
/*  32:    */   {
/*  33: 48 */     this.handle = handle;
/*  34:    */   }
/*  35:    */   
/*  36:    */   public Object get(int index)
/*  37:    */   {
/*  38: 53 */     checkIndex(index);
/*  39: 54 */     return this.array[index];
/*  40:    */   }
/*  41:    */   
/*  42:    */   public int size()
/*  43:    */   {
/*  44: 59 */     return this.size;
/*  45:    */   }
/*  46:    */   
/*  47:    */   public boolean add(Object element)
/*  48:    */   {
/*  49: 64 */     ObjectUtil.checkNotNull(element, "element");
/*  50:    */     try
/*  51:    */     {
/*  52: 66 */       insert(this.size, element);
/*  53:    */     }
/*  54:    */     catch (IndexOutOfBoundsException ignore)
/*  55:    */     {
/*  56: 69 */       expandArray();
/*  57: 70 */       insert(this.size, element);
/*  58:    */     }
/*  59: 72 */     this.size += 1;
/*  60: 73 */     return true;
/*  61:    */   }
/*  62:    */   
/*  63:    */   public Object set(int index, Object element)
/*  64:    */   {
/*  65: 78 */     ObjectUtil.checkNotNull(element, "element");
/*  66: 79 */     checkIndex(index);
/*  67:    */     
/*  68: 81 */     Object old = this.array[index];
/*  69: 82 */     insert(index, element);
/*  70: 83 */     return old;
/*  71:    */   }
/*  72:    */   
/*  73:    */   public void add(int index, Object element)
/*  74:    */   {
/*  75: 88 */     ObjectUtil.checkNotNull(element, "element");
/*  76: 89 */     checkIndex(index);
/*  77: 91 */     if (this.size == this.array.length) {
/*  78: 92 */       expandArray();
/*  79:    */     }
/*  80: 95 */     if (index != this.size - 1) {
/*  81: 96 */       System.arraycopy(this.array, index, this.array, index + 1, this.size - index);
/*  82:    */     }
/*  83: 99 */     insert(index, element);
/*  84:100 */     this.size += 1;
/*  85:    */   }
/*  86:    */   
/*  87:    */   public Object remove(int index)
/*  88:    */   {
/*  89:105 */     checkIndex(index);
/*  90:106 */     Object old = this.array[index];
/*  91:    */     
/*  92:108 */     int len = this.size - index - 1;
/*  93:109 */     if (len > 0) {
/*  94:110 */       System.arraycopy(this.array, index + 1, this.array, index, len);
/*  95:    */     }
/*  96:112 */     this.array[(--this.size)] = null;
/*  97:    */     
/*  98:114 */     return old;
/*  99:    */   }
/* 100:    */   
/* 101:    */   public void clear()
/* 102:    */   {
/* 103:121 */     this.size = 0;
/* 104:    */   }
/* 105:    */   
/* 106:    */   boolean insertSinceRecycled()
/* 107:    */   {
/* 108:128 */     return this.insertSinceRecycled;
/* 109:    */   }
/* 110:    */   
/* 111:    */   void recycle()
/* 112:    */   {
/* 113:135 */     for (int i = 0; i < this.size; i++) {
/* 114:136 */       this.array[i] = null;
/* 115:    */     }
/* 116:138 */     clear();
/* 117:139 */     this.insertSinceRecycled = false;
/* 118:140 */     this.handle.recycle(this);
/* 119:    */   }
/* 120:    */   
/* 121:    */   Object getUnsafe(int index)
/* 122:    */   {
/* 123:147 */     return this.array[index];
/* 124:    */   }
/* 125:    */   
/* 126:    */   private void checkIndex(int index)
/* 127:    */   {
/* 128:151 */     if (index >= this.size) {
/* 129:152 */       throw new IndexOutOfBoundsException();
/* 130:    */     }
/* 131:    */   }
/* 132:    */   
/* 133:    */   private void insert(int index, Object element)
/* 134:    */   {
/* 135:157 */     this.array[index] = element;
/* 136:158 */     this.insertSinceRecycled = true;
/* 137:    */   }
/* 138:    */   
/* 139:    */   private void expandArray()
/* 140:    */   {
/* 141:163 */     int newCapacity = this.array.length << 1;
/* 142:165 */     if (newCapacity < 0) {
/* 143:166 */       throw new OutOfMemoryError();
/* 144:    */     }
/* 145:169 */     Object[] newArray = new Object[newCapacity];
/* 146:170 */     System.arraycopy(this.array, 0, newArray, 0, this.array.length);
/* 147:    */     
/* 148:172 */     this.array = newArray;
/* 149:    */   }
/* 150:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.CodecOutputList
 * JD-Core Version:    0.7.0.1
 */