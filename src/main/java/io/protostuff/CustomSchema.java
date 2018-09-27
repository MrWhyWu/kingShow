/*  1:   */ package io.protostuff;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ 
/*  5:   */ public abstract class CustomSchema<T>
/*  6:   */   implements Schema<T>
/*  7:   */ {
/*  8:   */   protected final Schema<T> schema;
/*  9:   */   
/* 10:   */   public CustomSchema(Schema<T> schema)
/* 11:   */   {
/* 12:33 */     this.schema = schema;
/* 13:   */   }
/* 14:   */   
/* 15:   */   public String getFieldName(int number)
/* 16:   */   {
/* 17:39 */     return this.schema.getFieldName(number);
/* 18:   */   }
/* 19:   */   
/* 20:   */   public int getFieldNumber(String name)
/* 21:   */   {
/* 22:45 */     return this.schema.getFieldNumber(name);
/* 23:   */   }
/* 24:   */   
/* 25:   */   public boolean isInitialized(T message)
/* 26:   */   {
/* 27:51 */     return this.schema.isInitialized(message);
/* 28:   */   }
/* 29:   */   
/* 30:   */   public void mergeFrom(Input input, T message)
/* 31:   */     throws IOException
/* 32:   */   {
/* 33:57 */     this.schema.mergeFrom(input, message);
/* 34:   */   }
/* 35:   */   
/* 36:   */   public String messageFullName()
/* 37:   */   {
/* 38:63 */     return this.schema.messageFullName();
/* 39:   */   }
/* 40:   */   
/* 41:   */   public String messageName()
/* 42:   */   {
/* 43:69 */     return this.schema.messageName();
/* 44:   */   }
/* 45:   */   
/* 46:   */   public T newMessage()
/* 47:   */   {
/* 48:75 */     return this.schema.newMessage();
/* 49:   */   }
/* 50:   */   
/* 51:   */   public Class<? super T> typeClass()
/* 52:   */   {
/* 53:81 */     return this.schema.typeClass();
/* 54:   */   }
/* 55:   */   
/* 56:   */   public void writeTo(Output output, T message)
/* 57:   */     throws IOException
/* 58:   */   {
/* 59:87 */     this.schema.writeTo(output, message);
/* 60:   */   }
/* 61:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.CustomSchema
 * JD-Core Version:    0.7.0.1
 */