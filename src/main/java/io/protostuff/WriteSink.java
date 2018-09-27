/*   1:    */ package io.protostuff;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ 
/*   5:    */ public enum WriteSink
/*   6:    */ {
/*   7: 27 */   BUFFERED,  STREAMED;
/*   8:    */   
/*   9:    */   private WriteSink() {}
/*  10:    */   
/*  11:    */   public abstract LinkedBuffer drain(WriteSession paramWriteSession, LinkedBuffer paramLinkedBuffer)
/*  12:    */     throws IOException;
/*  13:    */   
/*  14:    */   public final LinkedBuffer writeByteArrayB64(byte[] value, WriteSession session, LinkedBuffer lb)
/*  15:    */     throws IOException
/*  16:    */   {
/*  17:610 */     return writeByteArrayB64(value, 0, value.length, session, lb);
/*  18:    */   }
/*  19:    */   
/*  20:    */   public abstract LinkedBuffer writeByteArrayB64(byte[] paramArrayOfByte, int paramInt1, int paramInt2, WriteSession paramWriteSession, LinkedBuffer paramLinkedBuffer)
/*  21:    */     throws IOException;
/*  22:    */   
/*  23:    */   public final LinkedBuffer writeByteArray(byte[] value, WriteSession session, LinkedBuffer lb)
/*  24:    */     throws IOException
/*  25:    */   {
/*  26:620 */     return writeByteArray(value, 0, value.length, session, lb);
/*  27:    */   }
/*  28:    */   
/*  29:    */   public abstract LinkedBuffer writeByteArray(byte[] paramArrayOfByte, int paramInt1, int paramInt2, WriteSession paramWriteSession, LinkedBuffer paramLinkedBuffer)
/*  30:    */     throws IOException;
/*  31:    */   
/*  32:    */   public abstract LinkedBuffer writeByte(byte paramByte, WriteSession paramWriteSession, LinkedBuffer paramLinkedBuffer)
/*  33:    */     throws IOException;
/*  34:    */   
/*  35:    */   public abstract LinkedBuffer writeInt32(int paramInt, WriteSession paramWriteSession, LinkedBuffer paramLinkedBuffer)
/*  36:    */     throws IOException;
/*  37:    */   
/*  38:    */   public abstract LinkedBuffer writeInt64(long paramLong, WriteSession paramWriteSession, LinkedBuffer paramLinkedBuffer)
/*  39:    */     throws IOException;
/*  40:    */   
/*  41:    */   public final LinkedBuffer writeFloat(float value, WriteSession session, LinkedBuffer lb)
/*  42:    */     throws IOException
/*  43:    */   {
/*  44:642 */     return writeInt32(Float.floatToRawIntBits(value), session, lb);
/*  45:    */   }
/*  46:    */   
/*  47:    */   public final LinkedBuffer writeDouble(double value, WriteSession session, LinkedBuffer lb)
/*  48:    */     throws IOException
/*  49:    */   {
/*  50:648 */     return writeInt64(Double.doubleToRawLongBits(value), session, lb);
/*  51:    */   }
/*  52:    */   
/*  53:    */   public abstract LinkedBuffer writeInt16(int paramInt, WriteSession paramWriteSession, LinkedBuffer paramLinkedBuffer)
/*  54:    */     throws IOException;
/*  55:    */   
/*  56:    */   public abstract LinkedBuffer writeInt16LE(int paramInt, WriteSession paramWriteSession, LinkedBuffer paramLinkedBuffer)
/*  57:    */     throws IOException;
/*  58:    */   
/*  59:    */   public abstract LinkedBuffer writeInt32LE(int paramInt, WriteSession paramWriteSession, LinkedBuffer paramLinkedBuffer)
/*  60:    */     throws IOException;
/*  61:    */   
/*  62:    */   public abstract LinkedBuffer writeInt64LE(long paramLong, WriteSession paramWriteSession, LinkedBuffer paramLinkedBuffer)
/*  63:    */     throws IOException;
/*  64:    */   
/*  65:    */   public final LinkedBuffer writeFloatLE(float value, WriteSession session, LinkedBuffer lb)
/*  66:    */     throws IOException
/*  67:    */   {
/*  68:666 */     return writeInt32LE(Float.floatToRawIntBits(value), session, lb);
/*  69:    */   }
/*  70:    */   
/*  71:    */   public final LinkedBuffer writeDoubleLE(double value, WriteSession session, LinkedBuffer lb)
/*  72:    */     throws IOException
/*  73:    */   {
/*  74:672 */     return writeInt64LE(Double.doubleToRawLongBits(value), session, lb);
/*  75:    */   }
/*  76:    */   
/*  77:    */   public abstract LinkedBuffer writeVarInt32(int paramInt, WriteSession paramWriteSession, LinkedBuffer paramLinkedBuffer)
/*  78:    */     throws IOException;
/*  79:    */   
/*  80:    */   public abstract LinkedBuffer writeVarInt64(long paramLong, WriteSession paramWriteSession, LinkedBuffer paramLinkedBuffer)
/*  81:    */     throws IOException;
/*  82:    */   
/*  83:    */   public abstract LinkedBuffer writeStrFromInt(int paramInt, WriteSession paramWriteSession, LinkedBuffer paramLinkedBuffer)
/*  84:    */     throws IOException;
/*  85:    */   
/*  86:    */   public abstract LinkedBuffer writeStrFromLong(long paramLong, WriteSession paramWriteSession, LinkedBuffer paramLinkedBuffer)
/*  87:    */     throws IOException;
/*  88:    */   
/*  89:    */   public abstract LinkedBuffer writeStrFromFloat(float paramFloat, WriteSession paramWriteSession, LinkedBuffer paramLinkedBuffer)
/*  90:    */     throws IOException;
/*  91:    */   
/*  92:    */   public abstract LinkedBuffer writeStrFromDouble(double paramDouble, WriteSession paramWriteSession, LinkedBuffer paramLinkedBuffer)
/*  93:    */     throws IOException;
/*  94:    */   
/*  95:    */   public abstract LinkedBuffer writeStrAscii(CharSequence paramCharSequence, WriteSession paramWriteSession, LinkedBuffer paramLinkedBuffer)
/*  96:    */     throws IOException;
/*  97:    */   
/*  98:    */   public abstract LinkedBuffer writeStrUTF8(CharSequence paramCharSequence, WriteSession paramWriteSession, LinkedBuffer paramLinkedBuffer)
/*  99:    */     throws IOException;
/* 100:    */   
/* 101:    */   public abstract LinkedBuffer writeStrUTF8VarDelimited(CharSequence paramCharSequence, WriteSession paramWriteSession, LinkedBuffer paramLinkedBuffer)
/* 102:    */     throws IOException;
/* 103:    */   
/* 104:    */   public abstract LinkedBuffer writeStrUTF8FixedDelimited(CharSequence paramCharSequence, boolean paramBoolean, WriteSession paramWriteSession, LinkedBuffer paramLinkedBuffer)
/* 105:    */     throws IOException;
/* 106:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.WriteSink
 * JD-Core Version:    0.7.0.1
 */