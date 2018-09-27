/*   1:    */ package io.netty.util;
/*   2:    */ 
/*   3:    */ public final class Signal
/*   4:    */   extends Error
/*   5:    */   implements Constant<Signal>
/*   6:    */ {
/*   7:    */   private static final long serialVersionUID = -221145131122459977L;
/*   8: 27 */   private static final ConstantPool<Signal> pool = new ConstantPool()
/*   9:    */   {
/*  10:    */     protected Signal newConstant(int id, String name)
/*  11:    */     {
/*  12: 30 */       return new Signal(id, name, null);
/*  13:    */     }
/*  14:    */   };
/*  15:    */   private final SignalConstant constant;
/*  16:    */   
/*  17:    */   public static Signal valueOf(String name)
/*  18:    */   {
/*  19: 38 */     return (Signal)pool.valueOf(name);
/*  20:    */   }
/*  21:    */   
/*  22:    */   public static Signal valueOf(Class<?> firstNameComponent, String secondNameComponent)
/*  23:    */   {
/*  24: 45 */     return (Signal)pool.valueOf(firstNameComponent, secondNameComponent);
/*  25:    */   }
/*  26:    */   
/*  27:    */   private Signal(int id, String name)
/*  28:    */   {
/*  29: 54 */     this.constant = new SignalConstant(id, name);
/*  30:    */   }
/*  31:    */   
/*  32:    */   public void expect(Signal signal)
/*  33:    */   {
/*  34: 62 */     if (this != signal) {
/*  35: 63 */       throw new IllegalStateException("unexpected signal: " + signal);
/*  36:    */     }
/*  37:    */   }
/*  38:    */   
/*  39:    */   public Throwable initCause(Throwable cause)
/*  40:    */   {
/*  41: 69 */     return this;
/*  42:    */   }
/*  43:    */   
/*  44:    */   public Throwable fillInStackTrace()
/*  45:    */   {
/*  46: 74 */     return this;
/*  47:    */   }
/*  48:    */   
/*  49:    */   public int id()
/*  50:    */   {
/*  51: 79 */     return this.constant.id();
/*  52:    */   }
/*  53:    */   
/*  54:    */   public String name()
/*  55:    */   {
/*  56: 84 */     return this.constant.name();
/*  57:    */   }
/*  58:    */   
/*  59:    */   public boolean equals(Object obj)
/*  60:    */   {
/*  61: 89 */     return this == obj;
/*  62:    */   }
/*  63:    */   
/*  64:    */   public int hashCode()
/*  65:    */   {
/*  66: 94 */     return System.identityHashCode(this);
/*  67:    */   }
/*  68:    */   
/*  69:    */   public int compareTo(Signal other)
/*  70:    */   {
/*  71: 99 */     if (this == other) {
/*  72:100 */       return 0;
/*  73:    */     }
/*  74:103 */     return this.constant.compareTo(other.constant);
/*  75:    */   }
/*  76:    */   
/*  77:    */   public String toString()
/*  78:    */   {
/*  79:108 */     return name();
/*  80:    */   }
/*  81:    */   
/*  82:    */   private static final class SignalConstant
/*  83:    */     extends AbstractConstant<SignalConstant>
/*  84:    */   {
/*  85:    */     SignalConstant(int id, String name)
/*  86:    */     {
/*  87:113 */       super(name);
/*  88:    */     }
/*  89:    */   }
/*  90:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.Signal
 * JD-Core Version:    0.7.0.1
 */