/*  1:   */ package io.netty.util;
/*  2:   */ 
/*  3:   */ public final class AttributeKey<T>
/*  4:   */   extends AbstractConstant<AttributeKey<T>>
/*  5:   */ {
/*  6:27 */   private static final ConstantPool<AttributeKey<Object>> pool = new ConstantPool()
/*  7:   */   {
/*  8:   */     protected AttributeKey<Object> newConstant(int id, String name)
/*  9:   */     {
/* 10:30 */       return new AttributeKey(id, name, null);
/* 11:   */     }
/* 12:   */   };
/* 13:   */   
/* 14:   */   public static <T> AttributeKey<T> valueOf(String name)
/* 15:   */   {
/* 16:39 */     return (AttributeKey)pool.valueOf(name);
/* 17:   */   }
/* 18:   */   
/* 19:   */   public static boolean exists(String name)
/* 20:   */   {
/* 21:46 */     return pool.exists(name);
/* 22:   */   }
/* 23:   */   
/* 24:   */   public static <T> AttributeKey<T> newInstance(String name)
/* 25:   */   {
/* 26:55 */     return (AttributeKey)pool.newInstance(name);
/* 27:   */   }
/* 28:   */   
/* 29:   */   public static <T> AttributeKey<T> valueOf(Class<?> firstNameComponent, String secondNameComponent)
/* 30:   */   {
/* 31:60 */     return (AttributeKey)pool.valueOf(firstNameComponent, secondNameComponent);
/* 32:   */   }
/* 33:   */   
/* 34:   */   private AttributeKey(int id, String name)
/* 35:   */   {
/* 36:64 */     super(id, name);
/* 37:   */   }
/* 38:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.AttributeKey
 * JD-Core Version:    0.7.0.1
 */