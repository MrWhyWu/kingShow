/*   1:    */ package io.protostuff;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import java.util.ArrayList;
/*   5:    */ 
/*   6:    */ public final class GraphByteArrayInput
/*   7:    */   extends FilterInput<ByteArrayInput>
/*   8:    */   implements GraphInput, Schema<Object>
/*   9:    */ {
/*  10:    */   private final ArrayList<Object> references;
/*  11: 33 */   private int lastRef = -1;
/*  12:    */   private Schema<Object> lastSchema;
/*  13: 36 */   private boolean messageReference = false;
/*  14:    */   
/*  15:    */   public GraphByteArrayInput(ByteArrayInput input)
/*  16:    */   {
/*  17: 40 */     super(input);
/*  18:    */     
/*  19:    */ 
/*  20: 43 */     assert (input.decodeNestedMessageAsGroup);
/*  21:    */     
/*  22: 45 */     this.references = new ArrayList();
/*  23:    */   }
/*  24:    */   
/*  25:    */   public GraphByteArrayInput(ByteArrayInput input, int initialCapacity)
/*  26:    */   {
/*  27: 50 */     super(input);
/*  28:    */     
/*  29:    */ 
/*  30: 53 */     assert (input.decodeNestedMessageAsGroup);
/*  31:    */     
/*  32: 55 */     this.references = new ArrayList(initialCapacity);
/*  33:    */   }
/*  34:    */   
/*  35:    */   public void updateLast(Object morphedMessage, Object lastMessage)
/*  36:    */   {
/*  37: 61 */     int last = this.references.size() - 1;
/*  38: 62 */     if ((lastMessage != null) && (lastMessage == this.references.get(last))) {
/*  39: 65 */       this.references.set(last, morphedMessage);
/*  40:    */     }
/*  41:    */   }
/*  42:    */   
/*  43:    */   public boolean isCurrentMessageReference()
/*  44:    */   {
/*  45: 72 */     return this.messageReference;
/*  46:    */   }
/*  47:    */   
/*  48:    */   public <T> int readFieldNumber(Schema<T> schema)
/*  49:    */     throws IOException
/*  50:    */   {
/*  51: 78 */     int fieldNumber = ((ByteArrayInput)this.input).readFieldNumber(schema);
/*  52: 79 */     if (WireFormat.getTagWireType(((ByteArrayInput)this.input).getLastTag()) == 6)
/*  53:    */     {
/*  54: 82 */       this.lastRef = ((ByteArrayInput)this.input).readUInt32();
/*  55: 83 */       this.messageReference = true;
/*  56:    */     }
/*  57:    */     else
/*  58:    */     {
/*  59: 88 */       this.messageReference = false;
/*  60:    */     }
/*  61: 91 */     return fieldNumber;
/*  62:    */   }
/*  63:    */   
/*  64:    */   public <T> T mergeObject(T value, Schema<T> schema)
/*  65:    */     throws IOException
/*  66:    */   {
/*  67: 98 */     if (this.messageReference) {
/*  68:101 */       return this.references.get(this.lastRef);
/*  69:    */     }
/*  70:104 */     this.lastSchema = schema;
/*  71:106 */     if (value == null) {
/*  72:107 */       value = schema.newMessage();
/*  73:    */     }
/*  74:109 */     this.references.add(value);
/*  75:    */     
/*  76:111 */     ((ByteArrayInput)this.input).mergeObject(value, this);
/*  77:    */     
/*  78:113 */     return value;
/*  79:    */   }
/*  80:    */   
/*  81:    */   public <T> void handleUnknownField(int fieldNumber, Schema<T> schema)
/*  82:    */     throws IOException
/*  83:    */   {
/*  84:119 */     if (!this.messageReference) {
/*  85:120 */       ((ByteArrayInput)this.input).skipField(((ByteArrayInput)this.input).getLastTag());
/*  86:    */     }
/*  87:    */   }
/*  88:    */   
/*  89:    */   public String getFieldName(int number)
/*  90:    */   {
/*  91:126 */     throw new UnsupportedOperationException();
/*  92:    */   }
/*  93:    */   
/*  94:    */   public int getFieldNumber(String name)
/*  95:    */   {
/*  96:132 */     throw new UnsupportedOperationException();
/*  97:    */   }
/*  98:    */   
/*  99:    */   public boolean isInitialized(Object owner)
/* 100:    */   {
/* 101:138 */     return true;
/* 102:    */   }
/* 103:    */   
/* 104:    */   public String messageFullName()
/* 105:    */   {
/* 106:144 */     throw new UnsupportedOperationException();
/* 107:    */   }
/* 108:    */   
/* 109:    */   public String messageName()
/* 110:    */   {
/* 111:150 */     throw new UnsupportedOperationException();
/* 112:    */   }
/* 113:    */   
/* 114:    */   public Object newMessage()
/* 115:    */   {
/* 116:156 */     throw new UnsupportedOperationException();
/* 117:    */   }
/* 118:    */   
/* 119:    */   public Class<? super Object> typeClass()
/* 120:    */   {
/* 121:162 */     throw new UnsupportedOperationException();
/* 122:    */   }
/* 123:    */   
/* 124:    */   public void mergeFrom(Input input, Object message)
/* 125:    */     throws IOException
/* 126:    */   {
/* 127:168 */     Schema<Object> schema = this.lastSchema;
/* 128:    */     
/* 129:    */ 
/* 130:171 */     schema.mergeFrom(this, message);
/* 131:172 */     if (!schema.isInitialized(message)) {
/* 132:173 */       throw new UninitializedMessageException(message, schema);
/* 133:    */     }
/* 134:176 */     this.lastSchema = schema;
/* 135:    */   }
/* 136:    */   
/* 137:    */   public void writeTo(Output output, Object message)
/* 138:    */     throws IOException
/* 139:    */   {
/* 140:183 */     throw new UnsupportedOperationException();
/* 141:    */   }
/* 142:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.GraphByteArrayInput
 * JD-Core Version:    0.7.0.1
 */