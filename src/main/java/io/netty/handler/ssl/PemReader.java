/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.Unpooled;
/*   5:    */ import io.netty.handler.codec.base64.Base64;
/*   6:    */ import io.netty.util.CharsetUtil;
/*   7:    */ import io.netty.util.internal.logging.InternalLogger;
/*   8:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   9:    */ import java.io.ByteArrayOutputStream;
/*  10:    */ import java.io.IOException;
/*  11:    */ import java.io.InputStream;
/*  12:    */ import java.io.OutputStream;
/*  13:    */ import java.nio.charset.Charset;
/*  14:    */ import java.security.KeyException;
/*  15:    */ import java.security.cert.CertificateException;
/*  16:    */ import java.util.ArrayList;
/*  17:    */ import java.util.List;
/*  18:    */ import java.util.regex.Matcher;
/*  19:    */ import java.util.regex.Pattern;
/*  20:    */ 
/*  21:    */ final class PemReader
/*  22:    */ {
/*  23: 46 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(PemReader.class);
/*  24: 48 */   private static final Pattern CERT_PATTERN = Pattern.compile("-+BEGIN\\s+.*CERTIFICATE[^-]*-+(?:\\s|\\r|\\n)+([a-z0-9+/=\\r\\n]+)-+END\\s+.*CERTIFICATE[^-]*-+", 2);
/*  25: 53 */   private static final Pattern KEY_PATTERN = Pattern.compile("-+BEGIN\\s+.*PRIVATE\\s+KEY[^-]*-+(?:\\s|\\r|\\n)+([a-z0-9+/=\\r\\n]+)-+END\\s+.*PRIVATE\\s+KEY[^-]*-+", 2);
/*  26:    */   
/*  27:    */   /* Error */
/*  28:    */   static ByteBuf[] readCertificates(java.io.File file)
/*  29:    */     throws CertificateException
/*  30:    */   {
/*  31:    */     // Byte code:
/*  32:    */     //   0: new 1	java/io/FileInputStream
/*  33:    */     //   3: dup
/*  34:    */     //   4: aload_0
/*  35:    */     //   5: invokespecial 2	java/io/FileInputStream:<init>	(Ljava/io/File;)V
/*  36:    */     //   8: astore_1
/*  37:    */     //   9: aload_1
/*  38:    */     //   10: invokestatic 3	io/netty/handler/ssl/PemReader:readCertificates	(Ljava/io/InputStream;)[Lio/netty/buffer/ByteBuf;
/*  39:    */     //   13: astore_2
/*  40:    */     //   14: aload_1
/*  41:    */     //   15: invokestatic 4	io/netty/handler/ssl/PemReader:safeClose	(Ljava/io/InputStream;)V
/*  42:    */     //   18: aload_2
/*  43:    */     //   19: areturn
/*  44:    */     //   20: astore_3
/*  45:    */     //   21: aload_1
/*  46:    */     //   22: invokestatic 4	io/netty/handler/ssl/PemReader:safeClose	(Ljava/io/InputStream;)V
/*  47:    */     //   25: aload_3
/*  48:    */     //   26: athrow
/*  49:    */     //   27: astore_1
/*  50:    */     //   28: new 6	java/security/cert/CertificateException
/*  51:    */     //   31: dup
/*  52:    */     //   32: new 7	java/lang/StringBuilder
/*  53:    */     //   35: dup
/*  54:    */     //   36: invokespecial 8	java/lang/StringBuilder:<init>	()V
/*  55:    */     //   39: ldc 9
/*  56:    */     //   41: invokevirtual 10	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*  57:    */     //   44: aload_0
/*  58:    */     //   45: invokevirtual 11	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
/*  59:    */     //   48: invokevirtual 12	java/lang/StringBuilder:toString	()Ljava/lang/String;
/*  60:    */     //   51: invokespecial 13	java/security/cert/CertificateException:<init>	(Ljava/lang/String;)V
/*  61:    */     //   54: athrow
/*  62:    */     // Line number table:
/*  63:    */     //   Java source line #61	-> byte code offset #0
/*  64:    */     //   Java source line #64	-> byte code offset #9
/*  65:    */     //   Java source line #66	-> byte code offset #14
/*  66:    */     //   Java source line #64	-> byte code offset #18
/*  67:    */     //   Java source line #66	-> byte code offset #20
/*  68:    */     //   Java source line #68	-> byte code offset #27
/*  69:    */     //   Java source line #69	-> byte code offset #28
/*  70:    */     // Local variable table:
/*  71:    */     //   start	length	slot	name	signature
/*  72:    */     //   0	55	0	file	java.io.File
/*  73:    */     //   8	14	1	in	InputStream
/*  74:    */     //   27	2	1	e	java.io.FileNotFoundException
/*  75:    */     //   20	6	3	localObject	Object
/*  76:    */     // Exception table:
/*  77:    */     //   from	to	target	type
/*  78:    */     //   9	14	20	finally
/*  79:    */     //   0	18	27	java/io/FileNotFoundException
/*  80:    */     //   20	27	27	java/io/FileNotFoundException
/*  81:    */   }
/*  82:    */   
/*  83:    */   static ByteBuf[] readCertificates(InputStream in)
/*  84:    */     throws CertificateException
/*  85:    */   {
/*  86:    */     try
/*  87:    */     {
/*  88: 76 */       content = readContent(in);
/*  89:    */     }
/*  90:    */     catch (IOException e)
/*  91:    */     {
/*  92:    */       String content;
/*  93: 78 */       throw new CertificateException("failed to read certificate input stream", e);
/*  94:    */     }
/*  95:    */     String content;
/*  96: 81 */     List<ByteBuf> certs = new ArrayList();
/*  97: 82 */     Matcher m = CERT_PATTERN.matcher(content);
/*  98: 83 */     int start = 0;
/*  99: 85 */     while (m.find(start))
/* 100:    */     {
/* 101: 89 */       ByteBuf base64 = Unpooled.copiedBuffer(m.group(1), CharsetUtil.US_ASCII);
/* 102: 90 */       ByteBuf der = Base64.decode(base64);
/* 103: 91 */       base64.release();
/* 104: 92 */       certs.add(der);
/* 105:    */       
/* 106: 94 */       start = m.end();
/* 107:    */     }
/* 108: 97 */     if (certs.isEmpty()) {
/* 109: 98 */       throw new CertificateException("found no certificates in input stream");
/* 110:    */     }
/* 111:101 */     return (ByteBuf[])certs.toArray(new ByteBuf[certs.size()]);
/* 112:    */   }
/* 113:    */   
/* 114:    */   /* Error */
/* 115:    */   static ByteBuf readPrivateKey(java.io.File file)
/* 116:    */     throws KeyException
/* 117:    */   {
/* 118:    */     // Byte code:
/* 119:    */     //   0: new 1	java/io/FileInputStream
/* 120:    */     //   3: dup
/* 121:    */     //   4: aload_0
/* 122:    */     //   5: invokespecial 2	java/io/FileInputStream:<init>	(Ljava/io/File;)V
/* 123:    */     //   8: astore_1
/* 124:    */     //   9: aload_1
/* 125:    */     //   10: invokestatic 36	io/netty/handler/ssl/PemReader:readPrivateKey	(Ljava/io/InputStream;)Lio/netty/buffer/ByteBuf;
/* 126:    */     //   13: astore_2
/* 127:    */     //   14: aload_1
/* 128:    */     //   15: invokestatic 4	io/netty/handler/ssl/PemReader:safeClose	(Ljava/io/InputStream;)V
/* 129:    */     //   18: aload_2
/* 130:    */     //   19: areturn
/* 131:    */     //   20: astore_3
/* 132:    */     //   21: aload_1
/* 133:    */     //   22: invokestatic 4	io/netty/handler/ssl/PemReader:safeClose	(Ljava/io/InputStream;)V
/* 134:    */     //   25: aload_3
/* 135:    */     //   26: athrow
/* 136:    */     //   27: astore_1
/* 137:    */     //   28: new 37	java/security/KeyException
/* 138:    */     //   31: dup
/* 139:    */     //   32: new 7	java/lang/StringBuilder
/* 140:    */     //   35: dup
/* 141:    */     //   36: invokespecial 8	java/lang/StringBuilder:<init>	()V
/* 142:    */     //   39: ldc 38
/* 143:    */     //   41: invokevirtual 10	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/* 144:    */     //   44: aload_0
/* 145:    */     //   45: invokevirtual 11	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
/* 146:    */     //   48: invokevirtual 12	java/lang/StringBuilder:toString	()Ljava/lang/String;
/* 147:    */     //   51: invokespecial 39	java/security/KeyException:<init>	(Ljava/lang/String;)V
/* 148:    */     //   54: athrow
/* 149:    */     // Line number table:
/* 150:    */     //   Java source line #106	-> byte code offset #0
/* 151:    */     //   Java source line #109	-> byte code offset #9
/* 152:    */     //   Java source line #111	-> byte code offset #14
/* 153:    */     //   Java source line #109	-> byte code offset #18
/* 154:    */     //   Java source line #111	-> byte code offset #20
/* 155:    */     //   Java source line #113	-> byte code offset #27
/* 156:    */     //   Java source line #114	-> byte code offset #28
/* 157:    */     // Local variable table:
/* 158:    */     //   start	length	slot	name	signature
/* 159:    */     //   0	55	0	file	java.io.File
/* 160:    */     //   8	14	1	in	InputStream
/* 161:    */     //   27	2	1	e	java.io.FileNotFoundException
/* 162:    */     //   20	6	3	localObject	Object
/* 163:    */     // Exception table:
/* 164:    */     //   from	to	target	type
/* 165:    */     //   9	14	20	finally
/* 166:    */     //   0	18	27	java/io/FileNotFoundException
/* 167:    */     //   20	27	27	java/io/FileNotFoundException
/* 168:    */   }
/* 169:    */   
/* 170:    */   static ByteBuf readPrivateKey(InputStream in)
/* 171:    */     throws KeyException
/* 172:    */   {
/* 173:    */     try
/* 174:    */     {
/* 175:121 */       content = readContent(in);
/* 176:    */     }
/* 177:    */     catch (IOException e)
/* 178:    */     {
/* 179:    */       String content;
/* 180:123 */       throw new KeyException("failed to read key input stream", e);
/* 181:    */     }
/* 182:    */     String content;
/* 183:126 */     Matcher m = KEY_PATTERN.matcher(content);
/* 184:127 */     if (!m.find()) {
/* 185:128 */       throw new KeyException("could not find a PKCS #8 private key in input stream (see http://netty.io/wiki/sslcontextbuilder-and-private-key.html for more information)");
/* 186:    */     }
/* 187:132 */     ByteBuf base64 = Unpooled.copiedBuffer(m.group(1), CharsetUtil.US_ASCII);
/* 188:133 */     ByteBuf der = Base64.decode(base64);
/* 189:134 */     base64.release();
/* 190:135 */     return der;
/* 191:    */   }
/* 192:    */   
/* 193:    */   private static String readContent(InputStream in)
/* 194:    */     throws IOException
/* 195:    */   {
/* 196:139 */     ByteArrayOutputStream out = new ByteArrayOutputStream();
/* 197:    */     try
/* 198:    */     {
/* 199:141 */       byte[] buf = new byte[8192];
/* 200:    */       int ret;
/* 201:    */       for (;;)
/* 202:    */       {
/* 203:143 */         ret = in.read(buf);
/* 204:144 */         if (ret < 0) {
/* 205:    */           break;
/* 206:    */         }
/* 207:147 */         out.write(buf, 0, ret);
/* 208:    */       }
/* 209:149 */       return out.toString(CharsetUtil.US_ASCII.name());
/* 210:    */     }
/* 211:    */     finally
/* 212:    */     {
/* 213:151 */       safeClose(out);
/* 214:    */     }
/* 215:    */   }
/* 216:    */   
/* 217:    */   private static void safeClose(InputStream in)
/* 218:    */   {
/* 219:    */     try
/* 220:    */     {
/* 221:157 */       in.close();
/* 222:    */     }
/* 223:    */     catch (IOException e)
/* 224:    */     {
/* 225:159 */       logger.warn("Failed to close a stream.", e);
/* 226:    */     }
/* 227:    */   }
/* 228:    */   
/* 229:    */   private static void safeClose(OutputStream out)
/* 230:    */   {
/* 231:    */     try
/* 232:    */     {
/* 233:165 */       out.close();
/* 234:    */     }
/* 235:    */     catch (IOException e)
/* 236:    */     {
/* 237:167 */       logger.warn("Failed to close a stream.", e);
/* 238:    */     }
/* 239:    */   }
/* 240:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.PemReader
 * JD-Core Version:    0.7.0.1
 */