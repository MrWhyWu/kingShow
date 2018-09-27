/*   1:    */ package io.protostuff;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ 
/*   5:    */ public abstract class Pipe
/*   6:    */ {
/*   7:    */   protected Input input;
/*   8:    */   protected Output output;
/*   9:    */   
/*  10:    */   protected Pipe reset()
/*  11:    */   {
/*  12: 40 */     this.output = null;
/*  13: 41 */     this.input = null;
/*  14: 42 */     return this;
/*  15:    */   }
/*  16:    */   
/*  17:    */   protected abstract Input begin(Schema<?> paramSchema)
/*  18:    */     throws IOException;
/*  19:    */   
/*  20:    */   protected abstract void end(Schema<?> paramSchema, Input paramInput, boolean paramBoolean)
/*  21:    */     throws IOException;
/*  22:    */   
/*  23:    */   public static abstract class Schema<T>
/*  24:    */     implements Schema<Pipe>
/*  25:    */   {
/*  26:    */     public final Schema<T> wrappedSchema;
/*  27:    */     
/*  28:    */     public Schema(Schema<T> wrappedSchema)
/*  29:    */     {
/*  30: 69 */       this.wrappedSchema = wrappedSchema;
/*  31:    */     }
/*  32:    */     
/*  33:    */     public String getFieldName(int number)
/*  34:    */     {
/*  35: 75 */       return this.wrappedSchema.getFieldName(number);
/*  36:    */     }
/*  37:    */     
/*  38:    */     public int getFieldNumber(String name)
/*  39:    */     {
/*  40: 81 */       return this.wrappedSchema.getFieldNumber(name);
/*  41:    */     }
/*  42:    */     
/*  43:    */     public boolean isInitialized(Pipe message)
/*  44:    */     {
/*  45: 90 */       return true;
/*  46:    */     }
/*  47:    */     
/*  48:    */     public String messageFullName()
/*  49:    */     {
/*  50: 96 */       return this.wrappedSchema.messageFullName();
/*  51:    */     }
/*  52:    */     
/*  53:    */     public String messageName()
/*  54:    */     {
/*  55:102 */       return this.wrappedSchema.messageName();
/*  56:    */     }
/*  57:    */     
/*  58:    */     public Pipe newMessage()
/*  59:    */     {
/*  60:108 */       throw new UnsupportedOperationException();
/*  61:    */     }
/*  62:    */     
/*  63:    */     public Class<Pipe> typeClass()
/*  64:    */     {
/*  65:114 */       throw new UnsupportedOperationException();
/*  66:    */     }
/*  67:    */     
/*  68:    */     public final void writeTo(Output output, Pipe pipe)
/*  69:    */       throws IOException
/*  70:    */     {
/*  71:120 */       if (pipe.output == null)
/*  72:    */       {
/*  73:122 */         pipe.output = output;
/*  74:    */         
/*  75:    */ 
/*  76:125 */         Input input = pipe.begin(this);
/*  77:127 */         if (input == null)
/*  78:    */         {
/*  79:130 */           pipe.output = null;
/*  80:131 */           pipe.end(this, input, true);
/*  81:132 */           return;
/*  82:    */         }
/*  83:135 */         pipe.input = input;
/*  84:    */         
/*  85:137 */         boolean transferComplete = false;
/*  86:    */         try
/*  87:    */         {
/*  88:140 */           transfer(pipe, input, output);
/*  89:141 */           transferComplete = true;
/*  90:    */         }
/*  91:    */         finally
/*  92:    */         {
/*  93:145 */           pipe.end(this, input, !transferComplete);
/*  94:    */         }
/*  95:150 */         return;
/*  96:    */       }
/*  97:154 */       pipe.input.mergeObject(pipe, this);
/*  98:    */     }
/*  99:    */     
/* 100:    */     public final void mergeFrom(Input input, Pipe pipe)
/* 101:    */       throws IOException
/* 102:    */     {
/* 103:160 */       transfer(pipe, input, pipe.output);
/* 104:    */     }
/* 105:    */     
/* 106:    */     protected abstract void transfer(Pipe paramPipe, Input paramInput, Output paramOutput)
/* 107:    */       throws IOException;
/* 108:    */   }
/* 109:    */   
/* 110:    */   public static <T> void transferDirect(Schema<T> pipeSchema, Pipe pipe, Input input, Output output)
/* 111:    */     throws IOException
/* 112:    */   {
/* 113:177 */     pipeSchema.transfer(pipe, input, output);
/* 114:    */   }
/* 115:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.Pipe
 * JD-Core Version:    0.7.0.1
 */