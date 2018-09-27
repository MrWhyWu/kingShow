/*   1:    */ package io.protostuff;
/*   2:    */ 
/*   3:    */ public class ProtobufException
/*   4:    */   extends ProtostuffException
/*   5:    */ {
/*   6:    */   private static final long serialVersionUID = 1616151763072450476L;
/*   7:    */   private static final String ERR_TRUNCATED_MESSAGE = "While parsing a protocol message, the input ended unexpectedly in the middle of a field.  This could mean either than the input has been truncated or that an embedded message misreported its own length.";
/*   8:    */   
/*   9:    */   public ProtobufException(String description)
/*  10:    */   {
/*  11: 66 */     super(description);
/*  12:    */   }
/*  13:    */   
/*  14:    */   public ProtobufException(String description, Throwable cause)
/*  15:    */   {
/*  16: 71 */     super(description, cause);
/*  17:    */   }
/*  18:    */   
/*  19:    */   static ProtobufException truncatedMessage(Throwable cause)
/*  20:    */   {
/*  21: 76 */     return new ProtobufException("While parsing a protocol message, the input ended unexpectedly in the middle of a field.  This could mean either than the input has been truncated or that an embedded message misreported its own length.", cause);
/*  22:    */   }
/*  23:    */   
/*  24:    */   static ProtobufException truncatedMessage()
/*  25:    */   {
/*  26: 81 */     return new ProtobufException("While parsing a protocol message, the input ended unexpectedly in the middle of a field.  This could mean either than the input has been truncated or that an embedded message misreported its own length.");
/*  27:    */   }
/*  28:    */   
/*  29:    */   static ProtobufException misreportedSize()
/*  30:    */   {
/*  31: 86 */     return new ProtobufException("CodedInput encountered an embedded string or bytes that misreported its size.");
/*  32:    */   }
/*  33:    */   
/*  34:    */   static ProtobufException negativeSize()
/*  35:    */   {
/*  36: 93 */     return new ProtobufException("CodedInput encountered an embedded string or message which claimed to have negative size.");
/*  37:    */   }
/*  38:    */   
/*  39:    */   static ProtobufException malformedVarint()
/*  40:    */   {
/*  41:100 */     return new ProtobufException("CodedInput encountered a malformed varint.");
/*  42:    */   }
/*  43:    */   
/*  44:    */   static ProtobufException invalidTag()
/*  45:    */   {
/*  46:106 */     return new ProtobufException("Protocol message contained an invalid tag (zero).");
/*  47:    */   }
/*  48:    */   
/*  49:    */   static ProtobufException invalidEndTag()
/*  50:    */   {
/*  51:112 */     return new ProtobufException("Protocol message end-group tag did not match expected tag.");
/*  52:    */   }
/*  53:    */   
/*  54:    */   static ProtobufException invalidWireType()
/*  55:    */   {
/*  56:118 */     return new ProtobufException("Protocol message tag had invalid wire type.");
/*  57:    */   }
/*  58:    */   
/*  59:    */   static ProtobufException recursionLimitExceeded()
/*  60:    */   {
/*  61:124 */     return new ProtobufException("Protocol message had too many levels of nesting.  May be malicious.  Use CodedInput.setRecursionLimit() to increase the depth limit.");
/*  62:    */   }
/*  63:    */   
/*  64:    */   static ProtobufException sizeLimitExceeded()
/*  65:    */   {
/*  66:131 */     return new ProtobufException("Protocol message was too large.  May be malicious.  Use CodedInput.setSizeLimit() to increase the size limit.");
/*  67:    */   }
/*  68:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.ProtobufException
 * JD-Core Version:    0.7.0.1
 */