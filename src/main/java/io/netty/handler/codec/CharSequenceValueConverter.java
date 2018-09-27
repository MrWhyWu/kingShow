/*   1:    */ package io.netty.handler.codec;
/*   2:    */ 
/*   3:    */ import io.netty.util.AsciiString;
/*   4:    */ import io.netty.util.internal.PlatformDependent;
/*   5:    */ import java.text.ParseException;
/*   6:    */ import java.util.Date;
/*   7:    */ 
/*   8:    */ public class CharSequenceValueConverter
/*   9:    */   implements ValueConverter<CharSequence>
/*  10:    */ {
/*  11: 27 */   public static final CharSequenceValueConverter INSTANCE = new CharSequenceValueConverter();
/*  12:    */   
/*  13:    */   public CharSequence convertObject(Object value)
/*  14:    */   {
/*  15: 31 */     if ((value instanceof CharSequence)) {
/*  16: 32 */       return (CharSequence)value;
/*  17:    */     }
/*  18: 34 */     return value.toString();
/*  19:    */   }
/*  20:    */   
/*  21:    */   public CharSequence convertInt(int value)
/*  22:    */   {
/*  23: 39 */     return String.valueOf(value);
/*  24:    */   }
/*  25:    */   
/*  26:    */   public CharSequence convertLong(long value)
/*  27:    */   {
/*  28: 44 */     return String.valueOf(value);
/*  29:    */   }
/*  30:    */   
/*  31:    */   public CharSequence convertDouble(double value)
/*  32:    */   {
/*  33: 49 */     return String.valueOf(value);
/*  34:    */   }
/*  35:    */   
/*  36:    */   public CharSequence convertChar(char value)
/*  37:    */   {
/*  38: 54 */     return String.valueOf(value);
/*  39:    */   }
/*  40:    */   
/*  41:    */   public CharSequence convertBoolean(boolean value)
/*  42:    */   {
/*  43: 59 */     return String.valueOf(value);
/*  44:    */   }
/*  45:    */   
/*  46:    */   public CharSequence convertFloat(float value)
/*  47:    */   {
/*  48: 64 */     return String.valueOf(value);
/*  49:    */   }
/*  50:    */   
/*  51:    */   public boolean convertToBoolean(CharSequence value)
/*  52:    */   {
/*  53: 69 */     if ((value instanceof AsciiString)) {
/*  54: 70 */       return ((AsciiString)value).parseBoolean();
/*  55:    */     }
/*  56: 72 */     return Boolean.parseBoolean(value.toString());
/*  57:    */   }
/*  58:    */   
/*  59:    */   public CharSequence convertByte(byte value)
/*  60:    */   {
/*  61: 77 */     return String.valueOf(value);
/*  62:    */   }
/*  63:    */   
/*  64:    */   public byte convertToByte(CharSequence value)
/*  65:    */   {
/*  66: 82 */     if ((value instanceof AsciiString)) {
/*  67: 83 */       return ((AsciiString)value).byteAt(0);
/*  68:    */     }
/*  69: 85 */     return Byte.parseByte(value.toString());
/*  70:    */   }
/*  71:    */   
/*  72:    */   public char convertToChar(CharSequence value)
/*  73:    */   {
/*  74: 90 */     return value.charAt(0);
/*  75:    */   }
/*  76:    */   
/*  77:    */   public CharSequence convertShort(short value)
/*  78:    */   {
/*  79: 95 */     return String.valueOf(value);
/*  80:    */   }
/*  81:    */   
/*  82:    */   public short convertToShort(CharSequence value)
/*  83:    */   {
/*  84:100 */     if ((value instanceof AsciiString)) {
/*  85:101 */       return ((AsciiString)value).parseShort();
/*  86:    */     }
/*  87:103 */     return Short.parseShort(value.toString());
/*  88:    */   }
/*  89:    */   
/*  90:    */   public int convertToInt(CharSequence value)
/*  91:    */   {
/*  92:108 */     if ((value instanceof AsciiString)) {
/*  93:109 */       return ((AsciiString)value).parseInt();
/*  94:    */     }
/*  95:111 */     return Integer.parseInt(value.toString());
/*  96:    */   }
/*  97:    */   
/*  98:    */   public long convertToLong(CharSequence value)
/*  99:    */   {
/* 100:116 */     if ((value instanceof AsciiString)) {
/* 101:117 */       return ((AsciiString)value).parseLong();
/* 102:    */     }
/* 103:119 */     return Long.parseLong(value.toString());
/* 104:    */   }
/* 105:    */   
/* 106:    */   public CharSequence convertTimeMillis(long value)
/* 107:    */   {
/* 108:124 */     return String.valueOf(value);
/* 109:    */   }
/* 110:    */   
/* 111:    */   public long convertToTimeMillis(CharSequence value)
/* 112:    */   {
/* 113:129 */     Date date = DateFormatter.parseHttpDate(value);
/* 114:130 */     if (date == null)
/* 115:    */     {
/* 116:131 */       PlatformDependent.throwException(new ParseException("header can't be parsed into a Date: " + value, 0));
/* 117:132 */       return 0L;
/* 118:    */     }
/* 119:134 */     return date.getTime();
/* 120:    */   }
/* 121:    */   
/* 122:    */   public float convertToFloat(CharSequence value)
/* 123:    */   {
/* 124:139 */     if ((value instanceof AsciiString)) {
/* 125:140 */       return ((AsciiString)value).parseFloat();
/* 126:    */     }
/* 127:142 */     return Float.parseFloat(value.toString());
/* 128:    */   }
/* 129:    */   
/* 130:    */   public double convertToDouble(CharSequence value)
/* 131:    */   {
/* 132:147 */     if ((value instanceof AsciiString)) {
/* 133:148 */       return ((AsciiString)value).parseDouble();
/* 134:    */     }
/* 135:150 */     return Double.parseDouble(value.toString());
/* 136:    */   }
/* 137:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.CharSequenceValueConverter
 * JD-Core Version:    0.7.0.1
 */