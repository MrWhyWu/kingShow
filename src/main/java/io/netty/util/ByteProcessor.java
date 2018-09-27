/*   1:    */ package io.netty.util;
/*   2:    */ 
/*   3:    */ public abstract interface ByteProcessor
/*   4:    */ {
/*   5:    */   public abstract boolean process(byte paramByte)
/*   6:    */     throws Exception;
/*   7:    */   
/*   8:    */   public static class IndexOfProcessor
/*   9:    */     implements ByteProcessor
/*  10:    */   {
/*  11:    */     private final byte byteToFind;
/*  12:    */     
/*  13:    */     public IndexOfProcessor(byte byteToFind)
/*  14:    */     {
/*  15: 28 */       this.byteToFind = byteToFind;
/*  16:    */     }
/*  17:    */     
/*  18:    */     public boolean process(byte value)
/*  19:    */     {
/*  20: 33 */       return value != this.byteToFind;
/*  21:    */     }
/*  22:    */   }
/*  23:    */   
/*  24:    */   public static class IndexNotOfProcessor
/*  25:    */     implements ByteProcessor
/*  26:    */   {
/*  27:    */     private final byte byteToNotFind;
/*  28:    */     
/*  29:    */     public IndexNotOfProcessor(byte byteToNotFind)
/*  30:    */     {
/*  31: 44 */       this.byteToNotFind = byteToNotFind;
/*  32:    */     }
/*  33:    */     
/*  34:    */     public boolean process(byte value)
/*  35:    */     {
/*  36: 49 */       return value == this.byteToNotFind;
/*  37:    */     }
/*  38:    */   }
/*  39:    */   
/*  40: 56 */   public static final ByteProcessor FIND_NUL = new IndexOfProcessor((byte)0);
/*  41: 61 */   public static final ByteProcessor FIND_NON_NUL = new IndexNotOfProcessor((byte)0);
/*  42: 66 */   public static final ByteProcessor FIND_CR = new IndexOfProcessor((byte)13);
/*  43: 71 */   public static final ByteProcessor FIND_NON_CR = new IndexNotOfProcessor((byte)13);
/*  44: 76 */   public static final ByteProcessor FIND_LF = new IndexOfProcessor((byte)10);
/*  45: 81 */   public static final ByteProcessor FIND_NON_LF = new IndexNotOfProcessor((byte)10);
/*  46: 86 */   public static final ByteProcessor FIND_SEMI_COLON = new IndexOfProcessor((byte)59);
/*  47: 91 */   public static final ByteProcessor FIND_COMMA = new IndexOfProcessor((byte)44);
/*  48: 96 */   public static final ByteProcessor FIND_CRLF = new ByteProcessor()
/*  49:    */   {
/*  50:    */     public boolean process(byte value)
/*  51:    */     {
/*  52: 99 */       return (value != 13) && (value != 10);
/*  53:    */     }
/*  54:    */   };
/*  55:106 */   public static final ByteProcessor FIND_NON_CRLF = new ByteProcessor()
/*  56:    */   {
/*  57:    */     public boolean process(byte value)
/*  58:    */     {
/*  59:109 */       return (value == 13) || (value == 10);
/*  60:    */     }
/*  61:    */   };
/*  62:116 */   public static final ByteProcessor FIND_LINEAR_WHITESPACE = new ByteProcessor()
/*  63:    */   {
/*  64:    */     public boolean process(byte value)
/*  65:    */     {
/*  66:119 */       return (value != 32) && (value != 9);
/*  67:    */     }
/*  68:    */   };
/*  69:126 */   public static final ByteProcessor FIND_NON_LINEAR_WHITESPACE = new ByteProcessor()
/*  70:    */   {
/*  71:    */     public boolean process(byte value)
/*  72:    */     {
/*  73:129 */       return (value == 32) || (value == 9);
/*  74:    */     }
/*  75:    */   };
/*  76:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.ByteProcessor
 * JD-Core Version:    0.7.0.1
 */