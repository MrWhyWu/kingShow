/*  1:   */ package io.opentracing.propagation;
/*  2:   */ 
/*  3:   */ import java.nio.ByteBuffer;
/*  4:   */ 
/*  5:   */ public abstract interface Format<C>
/*  6:   */ {
/*  7:   */   public static final class Builtin<C>
/*  8:   */     implements Format<C>
/*  9:   */   {
/* 10:   */     private final String name;
/* 11:   */     
/* 12:   */     private Builtin(String name)
/* 13:   */     {
/* 14:41 */       this.name = name;
/* 15:   */     }
/* 16:   */     
/* 17:55 */     public static final Format<TextMap> TEXT_MAP = new Builtin("TEXT_MAP");
/* 18:69 */     public static final Format<TextMap> HTTP_HEADERS = new Builtin("HTTP_HEADERS");
/* 19:79 */     public static final Format<ByteBuffer> BINARY = new Builtin("BINARY");
/* 20:   */     
/* 21:   */     public String toString()
/* 22:   */     {
/* 23:86 */       return Builtin.class.getSimpleName() + "." + this.name;
/* 24:   */     }
/* 25:   */   }
/* 26:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.opentracing.propagation.Format
 * JD-Core Version:    0.7.0.1
 */