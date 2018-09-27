/*   1:    */ package io.netty.handler.codec;
/*   2:    */ 
/*   3:    */ public final class UnsupportedValueConverter<V>
/*   4:    */   implements ValueConverter<V>
/*   5:    */ {
/*   6: 23 */   private static final UnsupportedValueConverter INSTANCE = new UnsupportedValueConverter();
/*   7:    */   
/*   8:    */   public static <V> UnsupportedValueConverter<V> instance()
/*   9:    */   {
/*  10: 28 */     return INSTANCE;
/*  11:    */   }
/*  12:    */   
/*  13:    */   public V convertObject(Object value)
/*  14:    */   {
/*  15: 33 */     throw new UnsupportedOperationException();
/*  16:    */   }
/*  17:    */   
/*  18:    */   public V convertBoolean(boolean value)
/*  19:    */   {
/*  20: 38 */     throw new UnsupportedOperationException();
/*  21:    */   }
/*  22:    */   
/*  23:    */   public boolean convertToBoolean(V value)
/*  24:    */   {
/*  25: 43 */     throw new UnsupportedOperationException();
/*  26:    */   }
/*  27:    */   
/*  28:    */   public V convertByte(byte value)
/*  29:    */   {
/*  30: 48 */     throw new UnsupportedOperationException();
/*  31:    */   }
/*  32:    */   
/*  33:    */   public byte convertToByte(V value)
/*  34:    */   {
/*  35: 53 */     throw new UnsupportedOperationException();
/*  36:    */   }
/*  37:    */   
/*  38:    */   public V convertChar(char value)
/*  39:    */   {
/*  40: 58 */     throw new UnsupportedOperationException();
/*  41:    */   }
/*  42:    */   
/*  43:    */   public char convertToChar(V value)
/*  44:    */   {
/*  45: 63 */     throw new UnsupportedOperationException();
/*  46:    */   }
/*  47:    */   
/*  48:    */   public V convertShort(short value)
/*  49:    */   {
/*  50: 68 */     throw new UnsupportedOperationException();
/*  51:    */   }
/*  52:    */   
/*  53:    */   public short convertToShort(V value)
/*  54:    */   {
/*  55: 73 */     throw new UnsupportedOperationException();
/*  56:    */   }
/*  57:    */   
/*  58:    */   public V convertInt(int value)
/*  59:    */   {
/*  60: 78 */     throw new UnsupportedOperationException();
/*  61:    */   }
/*  62:    */   
/*  63:    */   public int convertToInt(V value)
/*  64:    */   {
/*  65: 83 */     throw new UnsupportedOperationException();
/*  66:    */   }
/*  67:    */   
/*  68:    */   public V convertLong(long value)
/*  69:    */   {
/*  70: 88 */     throw new UnsupportedOperationException();
/*  71:    */   }
/*  72:    */   
/*  73:    */   public long convertToLong(V value)
/*  74:    */   {
/*  75: 93 */     throw new UnsupportedOperationException();
/*  76:    */   }
/*  77:    */   
/*  78:    */   public V convertTimeMillis(long value)
/*  79:    */   {
/*  80: 98 */     throw new UnsupportedOperationException();
/*  81:    */   }
/*  82:    */   
/*  83:    */   public long convertToTimeMillis(V value)
/*  84:    */   {
/*  85:103 */     throw new UnsupportedOperationException();
/*  86:    */   }
/*  87:    */   
/*  88:    */   public V convertFloat(float value)
/*  89:    */   {
/*  90:108 */     throw new UnsupportedOperationException();
/*  91:    */   }
/*  92:    */   
/*  93:    */   public float convertToFloat(V value)
/*  94:    */   {
/*  95:113 */     throw new UnsupportedOperationException();
/*  96:    */   }
/*  97:    */   
/*  98:    */   public V convertDouble(double value)
/*  99:    */   {
/* 100:118 */     throw new UnsupportedOperationException();
/* 101:    */   }
/* 102:    */   
/* 103:    */   public double convertToDouble(V value)
/* 104:    */   {
/* 105:123 */     throw new UnsupportedOperationException();
/* 106:    */   }
/* 107:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.UnsupportedValueConverter
 * JD-Core Version:    0.7.0.1
 */