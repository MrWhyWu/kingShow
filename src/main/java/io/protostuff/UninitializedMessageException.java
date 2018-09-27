/*  1:   */ package io.protostuff;
/*  2:   */ 
/*  3:   */ public final class UninitializedMessageException
/*  4:   */   extends RuntimeException
/*  5:   */ {
/*  6:   */   private static final long serialVersionUID = -7466929953374883507L;
/*  7:   */   public final Object targetMessage;
/*  8:   */   public final Schema<?> targetSchema;
/*  9:   */   
/* 10:   */   public UninitializedMessageException(Message<?> targetMessage)
/* 11:   */   {
/* 12:64 */     this(targetMessage, targetMessage.cachedSchema());
/* 13:   */   }
/* 14:   */   
/* 15:   */   public UninitializedMessageException(Object targetMessage, Schema<?> targetSchema)
/* 16:   */   {
/* 17:69 */     this.targetMessage = targetMessage;
/* 18:70 */     this.targetSchema = targetSchema;
/* 19:   */   }
/* 20:   */   
/* 21:   */   public UninitializedMessageException(String msg, Message<?> targetMessage)
/* 22:   */   {
/* 23:75 */     this(msg, targetMessage, targetMessage.cachedSchema());
/* 24:   */   }
/* 25:   */   
/* 26:   */   public UninitializedMessageException(String msg, Object targetMessage, Schema<?> targetSchema)
/* 27:   */   {
/* 28:81 */     super(msg);
/* 29:82 */     this.targetMessage = targetMessage;
/* 30:83 */     this.targetSchema = targetSchema;
/* 31:   */   }
/* 32:   */   
/* 33:   */   public <T> T getTargetMessage()
/* 34:   */   {
/* 35:89 */     return this.targetMessage;
/* 36:   */   }
/* 37:   */   
/* 38:   */   public <T> Schema<T> getTargetSchema()
/* 39:   */   {
/* 40:95 */     return this.targetSchema;
/* 41:   */   }
/* 42:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.UninitializedMessageException
 * JD-Core Version:    0.7.0.1
 */