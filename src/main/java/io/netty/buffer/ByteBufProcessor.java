/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.ByteProcessor;
/*   4:    */ 
/*   5:    */ @Deprecated
/*   6:    */ public abstract interface ByteBufProcessor
/*   7:    */   extends ByteProcessor
/*   8:    */ {
/*   9:    */   @Deprecated
/*  10: 31 */   public static final ByteBufProcessor FIND_NUL = new ByteBufProcessor()
/*  11:    */   {
/*  12:    */     public boolean process(byte value)
/*  13:    */       throws Exception
/*  14:    */     {
/*  15: 34 */       return value != 0;
/*  16:    */     }
/*  17:    */   };
/*  18:    */   @Deprecated
/*  19: 42 */   public static final ByteBufProcessor FIND_NON_NUL = new ByteBufProcessor()
/*  20:    */   {
/*  21:    */     public boolean process(byte value)
/*  22:    */       throws Exception
/*  23:    */     {
/*  24: 45 */       return value == 0;
/*  25:    */     }
/*  26:    */   };
/*  27:    */   @Deprecated
/*  28: 53 */   public static final ByteBufProcessor FIND_CR = new ByteBufProcessor()
/*  29:    */   {
/*  30:    */     public boolean process(byte value)
/*  31:    */       throws Exception
/*  32:    */     {
/*  33: 56 */       return value != 13;
/*  34:    */     }
/*  35:    */   };
/*  36:    */   @Deprecated
/*  37: 64 */   public static final ByteBufProcessor FIND_NON_CR = new ByteBufProcessor()
/*  38:    */   {
/*  39:    */     public boolean process(byte value)
/*  40:    */       throws Exception
/*  41:    */     {
/*  42: 67 */       return value == 13;
/*  43:    */     }
/*  44:    */   };
/*  45:    */   @Deprecated
/*  46: 75 */   public static final ByteBufProcessor FIND_LF = new ByteBufProcessor()
/*  47:    */   {
/*  48:    */     public boolean process(byte value)
/*  49:    */       throws Exception
/*  50:    */     {
/*  51: 78 */       return value != 10;
/*  52:    */     }
/*  53:    */   };
/*  54:    */   @Deprecated
/*  55: 86 */   public static final ByteBufProcessor FIND_NON_LF = new ByteBufProcessor()
/*  56:    */   {
/*  57:    */     public boolean process(byte value)
/*  58:    */       throws Exception
/*  59:    */     {
/*  60: 89 */       return value == 10;
/*  61:    */     }
/*  62:    */   };
/*  63:    */   @Deprecated
/*  64: 97 */   public static final ByteBufProcessor FIND_CRLF = new ByteBufProcessor()
/*  65:    */   {
/*  66:    */     public boolean process(byte value)
/*  67:    */       throws Exception
/*  68:    */     {
/*  69:100 */       return (value != 13) && (value != 10);
/*  70:    */     }
/*  71:    */   };
/*  72:    */   @Deprecated
/*  73:108 */   public static final ByteBufProcessor FIND_NON_CRLF = new ByteBufProcessor()
/*  74:    */   {
/*  75:    */     public boolean process(byte value)
/*  76:    */       throws Exception
/*  77:    */     {
/*  78:111 */       return (value == 13) || (value == 10);
/*  79:    */     }
/*  80:    */   };
/*  81:    */   @Deprecated
/*  82:119 */   public static final ByteBufProcessor FIND_LINEAR_WHITESPACE = new ByteBufProcessor()
/*  83:    */   {
/*  84:    */     public boolean process(byte value)
/*  85:    */       throws Exception
/*  86:    */     {
/*  87:122 */       return (value != 32) && (value != 9);
/*  88:    */     }
/*  89:    */   };
/*  90:    */   @Deprecated
/*  91:130 */   public static final ByteBufProcessor FIND_NON_LINEAR_WHITESPACE = new ByteBufProcessor()
/*  92:    */   {
/*  93:    */     public boolean process(byte value)
/*  94:    */       throws Exception
/*  95:    */     {
/*  96:133 */       return (value == 32) || (value == 9);
/*  97:    */     }
/*  98:    */   };
/*  99:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.ByteBufProcessor
 * JD-Core Version:    0.7.0.1
 */