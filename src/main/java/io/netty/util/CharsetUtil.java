/*   1:    */ package io.netty.util;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.InternalThreadLocalMap;
/*   4:    */ import io.netty.util.internal.ObjectUtil;
/*   5:    */ import java.nio.charset.Charset;
/*   6:    */ import java.nio.charset.CharsetDecoder;
/*   7:    */ import java.nio.charset.CharsetEncoder;
/*   8:    */ import java.nio.charset.CodingErrorAction;
/*   9:    */ import java.util.Map;
/*  10:    */ 
/*  11:    */ public final class CharsetUtil
/*  12:    */ {
/*  13: 37 */   public static final Charset UTF_16 = Charset.forName("UTF-16");
/*  14: 42 */   public static final Charset UTF_16BE = Charset.forName("UTF-16BE");
/*  15: 47 */   public static final Charset UTF_16LE = Charset.forName("UTF-16LE");
/*  16: 52 */   public static final Charset UTF_8 = Charset.forName("UTF-8");
/*  17: 57 */   public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
/*  18: 63 */   public static final Charset US_ASCII = Charset.forName("US-ASCII");
/*  19: 65 */   private static final Charset[] CHARSETS = { UTF_16, UTF_16BE, UTF_16LE, UTF_8, ISO_8859_1, US_ASCII };
/*  20:    */   
/*  21:    */   public static Charset[] values()
/*  22:    */   {
/*  23: 68 */     return CHARSETS;
/*  24:    */   }
/*  25:    */   
/*  26:    */   @Deprecated
/*  27:    */   public static CharsetEncoder getEncoder(Charset charset)
/*  28:    */   {
/*  29: 75 */     return encoder(charset);
/*  30:    */   }
/*  31:    */   
/*  32:    */   public static CharsetEncoder encoder(Charset charset, CodingErrorAction malformedInputAction, CodingErrorAction unmappableCharacterAction)
/*  33:    */   {
/*  34: 88 */     ObjectUtil.checkNotNull(charset, "charset");
/*  35: 89 */     CharsetEncoder e = charset.newEncoder();
/*  36: 90 */     e.onMalformedInput(malformedInputAction).onUnmappableCharacter(unmappableCharacterAction);
/*  37: 91 */     return e;
/*  38:    */   }
/*  39:    */   
/*  40:    */   public static CharsetEncoder encoder(Charset charset, CodingErrorAction codingErrorAction)
/*  41:    */   {
/*  42:102 */     return encoder(charset, codingErrorAction, codingErrorAction);
/*  43:    */   }
/*  44:    */   
/*  45:    */   public static CharsetEncoder encoder(Charset charset)
/*  46:    */   {
/*  47:112 */     ObjectUtil.checkNotNull(charset, "charset");
/*  48:    */     
/*  49:114 */     Map<Charset, CharsetEncoder> map = InternalThreadLocalMap.get().charsetEncoderCache();
/*  50:115 */     CharsetEncoder e = (CharsetEncoder)map.get(charset);
/*  51:116 */     if (e != null)
/*  52:    */     {
/*  53:117 */       e.reset().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
/*  54:118 */       return e;
/*  55:    */     }
/*  56:121 */     e = encoder(charset, CodingErrorAction.REPLACE, CodingErrorAction.REPLACE);
/*  57:122 */     map.put(charset, e);
/*  58:123 */     return e;
/*  59:    */   }
/*  60:    */   
/*  61:    */   @Deprecated
/*  62:    */   public static CharsetDecoder getDecoder(Charset charset)
/*  63:    */   {
/*  64:131 */     return decoder(charset);
/*  65:    */   }
/*  66:    */   
/*  67:    */   public static CharsetDecoder decoder(Charset charset, CodingErrorAction malformedInputAction, CodingErrorAction unmappableCharacterAction)
/*  68:    */   {
/*  69:144 */     ObjectUtil.checkNotNull(charset, "charset");
/*  70:145 */     CharsetDecoder d = charset.newDecoder();
/*  71:146 */     d.onMalformedInput(malformedInputAction).onUnmappableCharacter(unmappableCharacterAction);
/*  72:147 */     return d;
/*  73:    */   }
/*  74:    */   
/*  75:    */   public static CharsetDecoder decoder(Charset charset, CodingErrorAction codingErrorAction)
/*  76:    */   {
/*  77:158 */     return decoder(charset, codingErrorAction, codingErrorAction);
/*  78:    */   }
/*  79:    */   
/*  80:    */   public static CharsetDecoder decoder(Charset charset)
/*  81:    */   {
/*  82:168 */     ObjectUtil.checkNotNull(charset, "charset");
/*  83:    */     
/*  84:170 */     Map<Charset, CharsetDecoder> map = InternalThreadLocalMap.get().charsetDecoderCache();
/*  85:171 */     CharsetDecoder d = (CharsetDecoder)map.get(charset);
/*  86:172 */     if (d != null)
/*  87:    */     {
/*  88:173 */       d.reset().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
/*  89:174 */       return d;
/*  90:    */     }
/*  91:177 */     d = decoder(charset, CodingErrorAction.REPLACE, CodingErrorAction.REPLACE);
/*  92:178 */     map.put(charset, d);
/*  93:179 */     return d;
/*  94:    */   }
/*  95:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.CharsetUtil
 * JD-Core Version:    0.7.0.1
 */