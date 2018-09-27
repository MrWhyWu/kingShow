/*  1:   */ package io.netty.handler.codec;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.buffer.Unpooled;
/*  5:   */ 
/*  6:   */ public final class Delimiters
/*  7:   */ {
/*  8:   */   public static ByteBuf[] nulDelimiter()
/*  9:   */   {
/* 10:31 */     return new ByteBuf[] {
/* 11:32 */       Unpooled.wrappedBuffer(new byte[] { 0 }) };
/* 12:   */   }
/* 13:   */   
/* 14:   */   public static ByteBuf[] lineDelimiter()
/* 15:   */   {
/* 16:40 */     return new ByteBuf[] {
/* 17:41 */       Unpooled.wrappedBuffer(new byte[] { 13, 10 }), 
/* 18:42 */       Unpooled.wrappedBuffer(new byte[] { 10 }) };
/* 19:   */   }
/* 20:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.Delimiters
 * JD-Core Version:    0.7.0.1
 */