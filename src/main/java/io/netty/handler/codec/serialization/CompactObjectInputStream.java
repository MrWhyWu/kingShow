/*  1:   */ package io.netty.handler.codec.serialization;
/*  2:   */ 
/*  3:   */ import java.io.EOFException;
/*  4:   */ import java.io.IOException;
/*  5:   */ import java.io.InputStream;
/*  6:   */ import java.io.ObjectInputStream;
/*  7:   */ import java.io.ObjectStreamClass;
/*  8:   */ import java.io.StreamCorruptedException;
/*  9:   */ 
/* 10:   */ class CompactObjectInputStream
/* 11:   */   extends ObjectInputStream
/* 12:   */ {
/* 13:   */   private final ClassResolver classResolver;
/* 14:   */   
/* 15:   */   CompactObjectInputStream(InputStream in, ClassResolver classResolver)
/* 16:   */     throws IOException
/* 17:   */   {
/* 18:30 */     super(in);
/* 19:31 */     this.classResolver = classResolver;
/* 20:   */   }
/* 21:   */   
/* 22:   */   protected void readStreamHeader()
/* 23:   */     throws IOException
/* 24:   */   {
/* 25:36 */     int version = readByte() & 0xFF;
/* 26:37 */     if (version != 5) {
/* 27:38 */       throw new StreamCorruptedException("Unsupported version: " + version);
/* 28:   */     }
/* 29:   */   }
/* 30:   */   
/* 31:   */   protected ObjectStreamClass readClassDescriptor()
/* 32:   */     throws IOException, ClassNotFoundException
/* 33:   */   {
/* 34:46 */     int type = read();
/* 35:47 */     if (type < 0) {
/* 36:48 */       throw new EOFException();
/* 37:   */     }
/* 38:50 */     switch (type)
/* 39:   */     {
/* 40:   */     case 0: 
/* 41:52 */       return super.readClassDescriptor();
/* 42:   */     case 1: 
/* 43:54 */       String className = readUTF();
/* 44:55 */       Class<?> clazz = this.classResolver.resolve(className);
/* 45:56 */       return ObjectStreamClass.lookupAny(clazz);
/* 46:   */     }
/* 47:58 */     throw new StreamCorruptedException("Unexpected class descriptor type: " + type);
/* 48:   */   }
/* 49:   */   
/* 50:   */   protected Class<?> resolveClass(ObjectStreamClass desc)
/* 51:   */     throws IOException, ClassNotFoundException
/* 52:   */   {
/* 53:   */     Class<?> clazz;
/* 54:   */     try
/* 55:   */     {
/* 56:67 */       clazz = this.classResolver.resolve(desc.getName());
/* 57:   */     }
/* 58:   */     catch (ClassNotFoundException ignored)
/* 59:   */     {
/* 60:   */       Class<?> clazz;
/* 61:69 */       clazz = super.resolveClass(desc);
/* 62:   */     }
/* 63:72 */     return clazz;
/* 64:   */   }
/* 65:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.serialization.CompactObjectInputStream
 * JD-Core Version:    0.7.0.1
 */