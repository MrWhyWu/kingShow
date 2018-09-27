/*  1:   */ package io.netty.handler.codec;
/*  2:   */ 
/*  3:   */ public class UnsupportedMessageTypeException
/*  4:   */   extends CodecException
/*  5:   */ {
/*  6:   */   private static final long serialVersionUID = 2799598826487038726L;
/*  7:   */   
/*  8:   */   public UnsupportedMessageTypeException(Object message, Class<?>... expectedTypes)
/*  9:   */   {
/* 10:27 */     super(message(message == null ? "null" : message
/* 11:28 */       .getClass().getName(), expectedTypes));
/* 12:   */   }
/* 13:   */   
/* 14:   */   public UnsupportedMessageTypeException() {}
/* 15:   */   
/* 16:   */   public UnsupportedMessageTypeException(String message, Throwable cause)
/* 17:   */   {
/* 18:34 */     super(message, cause);
/* 19:   */   }
/* 20:   */   
/* 21:   */   public UnsupportedMessageTypeException(String s)
/* 22:   */   {
/* 23:38 */     super(s);
/* 24:   */   }
/* 25:   */   
/* 26:   */   public UnsupportedMessageTypeException(Throwable cause)
/* 27:   */   {
/* 28:42 */     super(cause);
/* 29:   */   }
/* 30:   */   
/* 31:   */   private static String message(String actualType, Class<?>... expectedTypes)
/* 32:   */   {
/* 33:47 */     StringBuilder buf = new StringBuilder(actualType);
/* 34:49 */     if ((expectedTypes != null) && (expectedTypes.length > 0))
/* 35:   */     {
/* 36:50 */       buf.append(" (expected: ").append(expectedTypes[0].getName());
/* 37:51 */       for (int i = 1; i < expectedTypes.length; i++)
/* 38:   */       {
/* 39:52 */         Class<?> t = expectedTypes[i];
/* 40:53 */         if (t == null) {
/* 41:   */           break;
/* 42:   */         }
/* 43:56 */         buf.append(", ").append(t.getName());
/* 44:   */       }
/* 45:58 */       buf.append(')');
/* 46:   */     }
/* 47:61 */     return buf.toString();
/* 48:   */   }
/* 49:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.UnsupportedMessageTypeException
 * JD-Core Version:    0.7.0.1
 */