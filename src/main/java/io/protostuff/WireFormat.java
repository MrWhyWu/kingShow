/*   1:    */ package io.protostuff;
/*   2:    */ 
/*   3:    */ public final class WireFormat
/*   4:    */ {
/*   5:    */   public static final int WIRETYPE_VARINT = 0;
/*   6:    */   public static final int WIRETYPE_FIXED64 = 1;
/*   7:    */   public static final int WIRETYPE_LENGTH_DELIMITED = 2;
/*   8:    */   public static final int WIRETYPE_START_GROUP = 3;
/*   9:    */   public static final int WIRETYPE_END_GROUP = 4;
/*  10:    */   public static final int WIRETYPE_FIXED32 = 5;
/*  11:    */   public static final int WIRETYPE_REFERENCE = 6;
/*  12:    */   public static final int WIRETYPE_TAIL_DELIMITER = 7;
/*  13:    */   static final int TAG_TYPE_BITS = 3;
/*  14:    */   static final int TAG_TYPE_MASK = 7;
/*  15:    */   static final int MESSAGE_SET_ITEM = 1;
/*  16:    */   static final int MESSAGE_SET_TYPE_ID = 2;
/*  17:    */   static final int MESSAGE_SET_MESSAGE = 3;
/*  18:    */   
/*  19:    */   public static int getTagWireType(int tag)
/*  20:    */   {
/*  21: 81 */     return tag & 0x7;
/*  22:    */   }
/*  23:    */   
/*  24:    */   public static int getTagFieldNumber(int tag)
/*  25:    */   {
/*  26: 89 */     return tag >>> 3;
/*  27:    */   }
/*  28:    */   
/*  29:    */   public static int makeTag(int fieldNumber, int wireType)
/*  30:    */   {
/*  31: 97 */     return fieldNumber << 3 | wireType;
/*  32:    */   }
/*  33:    */   
/*  34:    */   public static enum JavaType
/*  35:    */   {
/*  36:105 */     INT(Integer.valueOf(0)),  LONG(Long.valueOf(0L)),  FLOAT(Float.valueOf(0.0F)),  DOUBLE(Double.valueOf(0.0D)),  BOOLEAN(Boolean.valueOf(false)),  STRING(""),  BYTE_STRING(ByteString.EMPTY),  ENUM(null),  MESSAGE(null);
/*  37:    */     
/*  38:    */     private final Object defaultDefault;
/*  39:    */     
/*  40:    */     private JavaType(Object defaultDefault)
/*  41:    */     {
/*  42:117 */       this.defaultDefault = defaultDefault;
/*  43:    */     }
/*  44:    */     
/*  45:    */     Object getDefaultDefault()
/*  46:    */     {
/*  47:125 */       return this.defaultDefault;
/*  48:    */     }
/*  49:    */   }
/*  50:    */   
/*  51:    */   public static enum FieldType
/*  52:    */   {
/*  53:136 */     DOUBLE(WireFormat.JavaType.DOUBLE, 1),  FLOAT(WireFormat.JavaType.FLOAT, 5),  INT64(WireFormat.JavaType.LONG, 0),  UINT64(WireFormat.JavaType.LONG, 0),  INT32(WireFormat.JavaType.INT, 0),  FIXED64(WireFormat.JavaType.LONG, 1),  FIXED32(WireFormat.JavaType.INT, 5),  BOOL(WireFormat.JavaType.BOOLEAN, 0),  STRING(WireFormat.JavaType.STRING, 2),  GROUP(WireFormat.JavaType.MESSAGE, 3),  MESSAGE(WireFormat.JavaType.MESSAGE, 2),  BYTES(WireFormat.JavaType.BYTE_STRING, 2),  UINT32(WireFormat.JavaType.INT, 0),  ENUM(WireFormat.JavaType.ENUM, 0),  SFIXED32(WireFormat.JavaType.INT, 5),  SFIXED64(WireFormat.JavaType.LONG, 1),  SINT32(WireFormat.JavaType.INT, 0),  SINT64(WireFormat.JavaType.LONG, 0);
/*  54:    */     
/*  55:    */     public final WireFormat.JavaType javaType;
/*  56:    */     public final int wireType;
/*  57:    */     
/*  58:    */     private FieldType(WireFormat.JavaType javaType, int wireType)
/*  59:    */     {
/*  60:185 */       this.javaType = javaType;
/*  61:186 */       this.wireType = wireType;
/*  62:    */     }
/*  63:    */     
/*  64:    */     public WireFormat.JavaType getJavaType()
/*  65:    */     {
/*  66:194 */       return this.javaType;
/*  67:    */     }
/*  68:    */     
/*  69:    */     public int getWireType()
/*  70:    */     {
/*  71:199 */       return this.wireType;
/*  72:    */     }
/*  73:    */     
/*  74:    */     public boolean isPackable()
/*  75:    */     {
/*  76:204 */       return true;
/*  77:    */     }
/*  78:    */   }
/*  79:    */   
/*  80:215 */   static final int MESSAGE_SET_ITEM_TAG = makeTag(1, 3);
/*  81:217 */   static final int MESSAGE_SET_ITEM_END_TAG = makeTag(1, 4);
/*  82:219 */   static final int MESSAGE_SET_TYPE_ID_TAG = makeTag(2, 0);
/*  83:221 */   static final int MESSAGE_SET_MESSAGE_TAG = makeTag(3, 2);
/*  84:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.WireFormat
 * JD-Core Version:    0.7.0.1
 */