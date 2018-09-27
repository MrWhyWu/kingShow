/*   1:    */ package io.protostuff.runtime;
/*   2:    */ 
/*   3:    */ import io.protostuff.Input;
/*   4:    */ import io.protostuff.Output;
/*   5:    */ import io.protostuff.Pipe;
/*   6:    */ import io.protostuff.Pipe.Schema;
/*   7:    */ import io.protostuff.ProtostuffException;
/*   8:    */ import io.protostuff.Schema;
/*   9:    */ import io.protostuff.StatefulOutput;
/*  10:    */ import java.io.IOException;
/*  11:    */ 
/*  12:    */ public abstract class DerivativeSchema
/*  13:    */   implements Schema<Object>
/*  14:    */ {
/*  15:    */   public final IdStrategy strategy;
/*  16:    */   
/*  17:    */   public DerivativeSchema(IdStrategy strategy)
/*  18:    */   {
/*  19: 42 */     this.strategy = strategy;
/*  20:    */   }
/*  21:    */   
/*  22:    */   public String getFieldName(int number)
/*  23:    */   {
/*  24: 48 */     return number == 127 ? "_" : null;
/*  25:    */   }
/*  26:    */   
/*  27:    */   public int getFieldNumber(String name)
/*  28:    */   {
/*  29: 54 */     return (name.length() == 1) && (name.charAt(0) == '_') ? 127 : 0;
/*  30:    */   }
/*  31:    */   
/*  32:    */   public boolean isInitialized(Object owner)
/*  33:    */   {
/*  34: 60 */     return true;
/*  35:    */   }
/*  36:    */   
/*  37:    */   public String messageFullName()
/*  38:    */   {
/*  39: 66 */     return Object.class.getName();
/*  40:    */   }
/*  41:    */   
/*  42:    */   public String messageName()
/*  43:    */   {
/*  44: 72 */     return Object.class.getSimpleName();
/*  45:    */   }
/*  46:    */   
/*  47:    */   public Object newMessage()
/*  48:    */   {
/*  49: 79 */     throw new UnsupportedOperationException();
/*  50:    */   }
/*  51:    */   
/*  52:    */   public Class<? super Object> typeClass()
/*  53:    */   {
/*  54: 85 */     return Object.class;
/*  55:    */   }
/*  56:    */   
/*  57:    */   public void mergeFrom(Input input, Object owner)
/*  58:    */     throws IOException
/*  59:    */   {
/*  60: 95 */     int first = input.readFieldNumber(this);
/*  61: 96 */     if (first != 127) {
/*  62: 97 */       throw new ProtostuffException("order not preserved.");
/*  63:    */     }
/*  64: 99 */     doMergeFrom(input, this.strategy
/*  65:100 */       .resolvePojoFrom(input, 127).getSchema(), owner);
/*  66:    */   }
/*  67:    */   
/*  68:    */   public void writeTo(Output output, Object value)
/*  69:    */     throws IOException
/*  70:    */   {
/*  71:112 */     Schema<Object> schema = this.strategy.writePojoIdTo(output, 127, value.getClass()).getSchema();
/*  72:114 */     if ((output instanceof StatefulOutput)) {
/*  73:117 */       ((StatefulOutput)output).updateLast(schema, this);
/*  74:    */     }
/*  75:121 */     schema.writeTo(output, value);
/*  76:    */   }
/*  77:    */   
/*  78:127 */   public final Pipe.Schema<Object> pipeSchema = new Pipe.Schema(this)
/*  79:    */   {
/*  80:    */     public void transfer(Pipe pipe, Input input, Output output)
/*  81:    */       throws IOException
/*  82:    */     {
/*  83:134 */       int first = input.readFieldNumber(DerivativeSchema.this);
/*  84:135 */       if (first != 127) {
/*  85:136 */         throw new ProtostuffException("order not preserved.");
/*  86:    */       }
/*  87:139 */       Pipe.Schema<Object> pipeSchema = DerivativeSchema.this.strategy.transferPojoId(input, output, 127).getPipeSchema();
/*  88:141 */       if ((output instanceof StatefulOutput)) {
/*  89:144 */         ((StatefulOutput)output).updateLast(pipeSchema, this);
/*  90:    */       }
/*  91:147 */       Pipe.transferDirect(pipeSchema, pipe, input, output);
/*  92:    */     }
/*  93:    */   };
/*  94:    */   
/*  95:    */   protected abstract void doMergeFrom(Input paramInput, Schema<Object> paramSchema, Object paramObject)
/*  96:    */     throws IOException;
/*  97:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.DerivativeSchema
 * JD-Core Version:    0.7.0.1
 */