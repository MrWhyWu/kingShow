/*   1:    */ package io.netty.util;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.ObjectUtil;
/*   4:    */ import io.netty.util.internal.PlatformDependent;
/*   5:    */ import java.util.concurrent.ConcurrentMap;
/*   6:    */ import java.util.concurrent.atomic.AtomicInteger;
/*   7:    */ 
/*   8:    */ public abstract class ConstantPool<T extends Constant<T>>
/*   9:    */ {
/*  10: 32 */   private final ConcurrentMap<String, T> constants = PlatformDependent.newConcurrentHashMap();
/*  11: 34 */   private final AtomicInteger nextId = new AtomicInteger(1);
/*  12:    */   
/*  13:    */   public T valueOf(Class<?> firstNameComponent, String secondNameComponent)
/*  14:    */   {
/*  15: 40 */     if (firstNameComponent == null) {
/*  16: 41 */       throw new NullPointerException("firstNameComponent");
/*  17:    */     }
/*  18: 43 */     if (secondNameComponent == null) {
/*  19: 44 */       throw new NullPointerException("secondNameComponent");
/*  20:    */     }
/*  21: 47 */     return valueOf(firstNameComponent.getName() + '#' + secondNameComponent);
/*  22:    */   }
/*  23:    */   
/*  24:    */   public T valueOf(String name)
/*  25:    */   {
/*  26: 59 */     checkNotNullAndNotEmpty(name);
/*  27: 60 */     return getOrCreate(name);
/*  28:    */   }
/*  29:    */   
/*  30:    */   private T getOrCreate(String name)
/*  31:    */   {
/*  32: 69 */     T constant = (Constant)this.constants.get(name);
/*  33: 70 */     if (constant == null)
/*  34:    */     {
/*  35: 71 */       T tempConstant = newConstant(nextId(), name);
/*  36: 72 */       constant = (Constant)this.constants.putIfAbsent(name, tempConstant);
/*  37: 73 */       if (constant == null) {
/*  38: 74 */         return tempConstant;
/*  39:    */       }
/*  40:    */     }
/*  41: 78 */     return constant;
/*  42:    */   }
/*  43:    */   
/*  44:    */   public boolean exists(String name)
/*  45:    */   {
/*  46: 85 */     checkNotNullAndNotEmpty(name);
/*  47: 86 */     return this.constants.containsKey(name);
/*  48:    */   }
/*  49:    */   
/*  50:    */   public T newInstance(String name)
/*  51:    */   {
/*  52: 94 */     checkNotNullAndNotEmpty(name);
/*  53: 95 */     return createOrThrow(name);
/*  54:    */   }
/*  55:    */   
/*  56:    */   private T createOrThrow(String name)
/*  57:    */   {
/*  58:104 */     T constant = (Constant)this.constants.get(name);
/*  59:105 */     if (constant == null)
/*  60:    */     {
/*  61:106 */       T tempConstant = newConstant(nextId(), name);
/*  62:107 */       constant = (Constant)this.constants.putIfAbsent(name, tempConstant);
/*  63:108 */       if (constant == null) {
/*  64:109 */         return tempConstant;
/*  65:    */       }
/*  66:    */     }
/*  67:113 */     throw new IllegalArgumentException(String.format("'%s' is already in use", new Object[] { name }));
/*  68:    */   }
/*  69:    */   
/*  70:    */   private static String checkNotNullAndNotEmpty(String name)
/*  71:    */   {
/*  72:117 */     ObjectUtil.checkNotNull(name, "name");
/*  73:119 */     if (name.isEmpty()) {
/*  74:120 */       throw new IllegalArgumentException("empty name");
/*  75:    */     }
/*  76:123 */     return name;
/*  77:    */   }
/*  78:    */   
/*  79:    */   protected abstract T newConstant(int paramInt, String paramString);
/*  80:    */   
/*  81:    */   @Deprecated
/*  82:    */   public final int nextId()
/*  83:    */   {
/*  84:130 */     return this.nextId.getAndIncrement();
/*  85:    */   }
/*  86:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.ConstantPool
 * JD-Core Version:    0.7.0.1
 */