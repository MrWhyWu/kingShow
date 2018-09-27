/*   1:    */ package io.netty.handler.codec;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufUtil;
/*   5:    */ import io.netty.util.AsciiString;
/*   6:    */ import io.netty.util.CharsetUtil;
/*   7:    */ import java.util.Map.Entry;
/*   8:    */ 
/*   9:    */ public final class AsciiHeadersEncoder
/*  10:    */ {
/*  11:    */   private final ByteBuf buf;
/*  12:    */   private final SeparatorType separatorType;
/*  13:    */   private final NewlineType newlineType;
/*  14:    */   
/*  15:    */   public static enum SeparatorType
/*  16:    */   {
/*  17: 36 */     COLON,  COLON_SPACE;
/*  18:    */     
/*  19:    */     private SeparatorType() {}
/*  20:    */   }
/*  21:    */   
/*  22:    */   public static enum NewlineType
/*  23:    */   {
/*  24: 50 */     LF,  CRLF;
/*  25:    */     
/*  26:    */     private NewlineType() {}
/*  27:    */   }
/*  28:    */   
/*  29:    */   public AsciiHeadersEncoder(ByteBuf buf)
/*  30:    */   {
/*  31: 62 */     this(buf, SeparatorType.COLON_SPACE, NewlineType.CRLF);
/*  32:    */   }
/*  33:    */   
/*  34:    */   public AsciiHeadersEncoder(ByteBuf buf, SeparatorType separatorType, NewlineType newlineType)
/*  35:    */   {
/*  36: 66 */     if (buf == null) {
/*  37: 67 */       throw new NullPointerException("buf");
/*  38:    */     }
/*  39: 69 */     if (separatorType == null) {
/*  40: 70 */       throw new NullPointerException("separatorType");
/*  41:    */     }
/*  42: 72 */     if (newlineType == null) {
/*  43: 73 */       throw new NullPointerException("newlineType");
/*  44:    */     }
/*  45: 76 */     this.buf = buf;
/*  46: 77 */     this.separatorType = separatorType;
/*  47: 78 */     this.newlineType = newlineType;
/*  48:    */   }
/*  49:    */   
/*  50:    */   public void encode(Map.Entry<CharSequence, CharSequence> entry)
/*  51:    */   {
/*  52: 82 */     CharSequence name = (CharSequence)entry.getKey();
/*  53: 83 */     CharSequence value = (CharSequence)entry.getValue();
/*  54: 84 */     ByteBuf buf = this.buf;
/*  55: 85 */     int nameLen = name.length();
/*  56: 86 */     int valueLen = value.length();
/*  57: 87 */     int entryLen = nameLen + valueLen + 4;
/*  58: 88 */     int offset = buf.writerIndex();
/*  59: 89 */     buf.ensureWritable(entryLen);
/*  60: 90 */     writeAscii(buf, offset, name);
/*  61: 91 */     offset += nameLen;
/*  62: 93 */     switch (this.separatorType)
/*  63:    */     {
/*  64:    */     case COLON: 
/*  65: 95 */       buf.setByte(offset++, 58);
/*  66: 96 */       break;
/*  67:    */     case COLON_SPACE: 
/*  68: 98 */       buf.setByte(offset++, 58);
/*  69: 99 */       buf.setByte(offset++, 32);
/*  70:100 */       break;
/*  71:    */     default: 
/*  72:102 */       throw new Error();
/*  73:    */     }
/*  74:105 */     writeAscii(buf, offset, value);
/*  75:106 */     offset += valueLen;
/*  76:108 */     switch (this.newlineType)
/*  77:    */     {
/*  78:    */     case LF: 
/*  79:110 */       buf.setByte(offset++, 10);
/*  80:111 */       break;
/*  81:    */     case CRLF: 
/*  82:113 */       buf.setByte(offset++, 13);
/*  83:114 */       buf.setByte(offset++, 10);
/*  84:115 */       break;
/*  85:    */     default: 
/*  86:117 */       throw new Error();
/*  87:    */     }
/*  88:120 */     buf.writerIndex(offset);
/*  89:    */   }
/*  90:    */   
/*  91:    */   private static void writeAscii(ByteBuf buf, int offset, CharSequence value)
/*  92:    */   {
/*  93:124 */     if ((value instanceof AsciiString)) {
/*  94:125 */       ByteBufUtil.copy((AsciiString)value, 0, buf, offset, value.length());
/*  95:    */     } else {
/*  96:127 */       buf.setCharSequence(offset, value, CharsetUtil.US_ASCII);
/*  97:    */     }
/*  98:    */   }
/*  99:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.AsciiHeadersEncoder
 * JD-Core Version:    0.7.0.1
 */