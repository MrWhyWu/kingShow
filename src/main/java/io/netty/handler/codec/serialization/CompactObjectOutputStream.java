/*  1:   */ package io.netty.handler.codec.serialization;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import java.io.ObjectOutputStream;
/*  5:   */ import java.io.ObjectStreamClass;
/*  6:   */ import java.io.OutputStream;
/*  7:   */ 
/*  8:   */ class CompactObjectOutputStream
/*  9:   */   extends ObjectOutputStream
/* 10:   */ {
/* 11:   */   static final int TYPE_FAT_DESCRIPTOR = 0;
/* 12:   */   static final int TYPE_THIN_DESCRIPTOR = 1;
/* 13:   */   
/* 14:   */   CompactObjectOutputStream(OutputStream out)
/* 15:   */     throws IOException
/* 16:   */   {
/* 17:29 */     super(out);
/* 18:   */   }
/* 19:   */   
/* 20:   */   protected void writeStreamHeader()
/* 21:   */     throws IOException
/* 22:   */   {
/* 23:34 */     writeByte(5);
/* 24:   */   }
/* 25:   */   
/* 26:   */   protected void writeClassDescriptor(ObjectStreamClass desc)
/* 27:   */     throws IOException
/* 28:   */   {
/* 29:39 */     Class<?> clazz = desc.forClass();
/* 30:40 */     if ((clazz.isPrimitive()) || (clazz.isArray()) || (clazz.isInterface()) || 
/* 31:41 */       (desc.getSerialVersionUID() == 0L))
/* 32:   */     {
/* 33:42 */       write(0);
/* 34:43 */       super.writeClassDescriptor(desc);
/* 35:   */     }
/* 36:   */     else
/* 37:   */     {
/* 38:45 */       write(1);
/* 39:46 */       writeUTF(desc.getName());
/* 40:   */     }
/* 41:   */   }
/* 42:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.serialization.CompactObjectOutputStream
 * JD-Core Version:    0.7.0.1
 */