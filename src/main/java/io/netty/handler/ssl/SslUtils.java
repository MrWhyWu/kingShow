/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.buffer.ByteBufUtil;
/*   6:    */ import io.netty.channel.ChannelHandlerContext;
/*   7:    */ import io.netty.handler.codec.base64.Base64;
/*   8:    */ import io.netty.handler.codec.base64.Base64Dialect;
/*   9:    */ import java.nio.ByteBuffer;
/*  10:    */ import java.nio.ByteOrder;
/*  11:    */ import java.util.Arrays;
/*  12:    */ import java.util.List;
/*  13:    */ import java.util.Set;
/*  14:    */ import javax.net.ssl.SSLHandshakeException;
/*  15:    */ 
/*  16:    */ final class SslUtils
/*  17:    */ {
/*  18:    */   static final String PROTOCOL_SSL_V2_HELLO = "SSLv2Hello";
/*  19:    */   static final String PROTOCOL_SSL_V2 = "SSLv2";
/*  20:    */   static final String PROTOCOL_SSL_V3 = "SSLv3";
/*  21:    */   static final String PROTOCOL_TLS_V1 = "TLSv1";
/*  22:    */   static final String PROTOCOL_TLS_V1_1 = "TLSv1.1";
/*  23:    */   static final String PROTOCOL_TLS_V1_2 = "TLSv1.2";
/*  24:    */   static final int SSL_CONTENT_TYPE_CHANGE_CIPHER_SPEC = 20;
/*  25:    */   static final int SSL_CONTENT_TYPE_ALERT = 21;
/*  26:    */   static final int SSL_CONTENT_TYPE_HANDSHAKE = 22;
/*  27:    */   static final int SSL_CONTENT_TYPE_APPLICATION_DATA = 23;
/*  28:    */   static final int SSL_CONTENT_TYPE_EXTENSION_HEARTBEAT = 24;
/*  29:    */   static final int SSL_RECORD_HEADER_LENGTH = 5;
/*  30:    */   static final int NOT_ENOUGH_DATA = -1;
/*  31:    */   static final int NOT_ENCRYPTED = -2;
/*  32: 87 */   static final String[] DEFAULT_CIPHER_SUITES = { "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384", "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA", "TLS_RSA_WITH_AES_128_GCM_SHA256", "TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_256_CBC_SHA" };
/*  33:    */   
/*  34:    */   static void addIfSupported(Set<String> supported, List<String> enabled, String... names)
/*  35:    */   {
/*  36:106 */     for (String n : names) {
/*  37:107 */       if (supported.contains(n)) {
/*  38:108 */         enabled.add(n);
/*  39:    */       }
/*  40:    */     }
/*  41:    */   }
/*  42:    */   
/*  43:    */   static void useFallbackCiphersIfDefaultIsEmpty(List<String> defaultCiphers, Iterable<String> fallbackCiphers)
/*  44:    */   {
/*  45:114 */     if (defaultCiphers.isEmpty()) {
/*  46:115 */       for (String cipher : fallbackCiphers) {
/*  47:116 */         if ((!cipher.startsWith("SSL_")) && (!cipher.contains("_RC4_"))) {
/*  48:119 */           defaultCiphers.add(cipher);
/*  49:    */         }
/*  50:    */       }
/*  51:    */     }
/*  52:    */   }
/*  53:    */   
/*  54:    */   static void useFallbackCiphersIfDefaultIsEmpty(List<String> defaultCiphers, String... fallbackCiphers)
/*  55:    */   {
/*  56:125 */     useFallbackCiphersIfDefaultIsEmpty(defaultCiphers, Arrays.asList(fallbackCiphers));
/*  57:    */   }
/*  58:    */   
/*  59:    */   static SSLHandshakeException toSSLHandshakeException(Throwable e)
/*  60:    */   {
/*  61:132 */     if ((e instanceof SSLHandshakeException)) {
/*  62:133 */       return (SSLHandshakeException)e;
/*  63:    */     }
/*  64:136 */     return (SSLHandshakeException)new SSLHandshakeException(e.getMessage()).initCause(e);
/*  65:    */   }
/*  66:    */   
/*  67:    */   static int getEncryptedPacketLength(ByteBuf buffer, int offset)
/*  68:    */   {
/*  69:157 */     int packetLength = 0;
/*  70:    */     boolean tls;
/*  71:    */     boolean tls;
/*  72:161 */     switch (buffer.getUnsignedByte(offset))
/*  73:    */     {
/*  74:    */     case 20: 
/*  75:    */     case 21: 
/*  76:    */     case 22: 
/*  77:    */     case 23: 
/*  78:    */     case 24: 
/*  79:167 */       tls = true;
/*  80:168 */       break;
/*  81:    */     default: 
/*  82:171 */       tls = false;
/*  83:    */     }
/*  84:174 */     if (tls)
/*  85:    */     {
/*  86:176 */       int majorVersion = buffer.getUnsignedByte(offset + 1);
/*  87:177 */       if (majorVersion == 3)
/*  88:    */       {
/*  89:179 */         packetLength = unsignedShortBE(buffer, offset + 3) + 5;
/*  90:180 */         if (packetLength <= 5) {
/*  91:182 */           tls = false;
/*  92:    */         }
/*  93:    */       }
/*  94:    */       else
/*  95:    */       {
/*  96:186 */         tls = false;
/*  97:    */       }
/*  98:    */     }
/*  99:190 */     if (!tls)
/* 100:    */     {
/* 101:192 */       int headerLength = (buffer.getUnsignedByte(offset) & 0x80) != 0 ? 2 : 3;
/* 102:193 */       int majorVersion = buffer.getUnsignedByte(offset + headerLength + 1);
/* 103:194 */       if ((majorVersion == 2) || (majorVersion == 3))
/* 104:    */       {
/* 105:197 */         packetLength = headerLength == 2 ? (shortBE(buffer, offset) & 0x7FFF) + 2 : (shortBE(buffer, offset) & 0x3FFF) + 3;
/* 106:198 */         if (packetLength <= headerLength) {
/* 107:199 */           return -1;
/* 108:    */         }
/* 109:    */       }
/* 110:    */       else
/* 111:    */       {
/* 112:202 */         return -2;
/* 113:    */       }
/* 114:    */     }
/* 115:205 */     return packetLength;
/* 116:    */   }
/* 117:    */   
/* 118:    */   private static int unsignedShortBE(ByteBuf buffer, int offset)
/* 119:    */   {
/* 120:211 */     return buffer.order() == ByteOrder.BIG_ENDIAN ? buffer
/* 121:212 */       .getUnsignedShort(offset) : buffer.getUnsignedShortLE(offset);
/* 122:    */   }
/* 123:    */   
/* 124:    */   private static short shortBE(ByteBuf buffer, int offset)
/* 125:    */   {
/* 126:218 */     return buffer.order() == ByteOrder.BIG_ENDIAN ? buffer
/* 127:219 */       .getShort(offset) : buffer.getShortLE(offset);
/* 128:    */   }
/* 129:    */   
/* 130:    */   private static short unsignedByte(byte b)
/* 131:    */   {
/* 132:223 */     return (short)(b & 0xFF);
/* 133:    */   }
/* 134:    */   
/* 135:    */   private static int unsignedShortBE(ByteBuffer buffer, int offset)
/* 136:    */   {
/* 137:228 */     return shortBE(buffer, offset) & 0xFFFF;
/* 138:    */   }
/* 139:    */   
/* 140:    */   private static short shortBE(ByteBuffer buffer, int offset)
/* 141:    */   {
/* 142:233 */     return buffer.order() == ByteOrder.BIG_ENDIAN ? buffer
/* 143:234 */       .getShort(offset) : ByteBufUtil.swapShort(buffer.getShort(offset));
/* 144:    */   }
/* 145:    */   
/* 146:    */   static int getEncryptedPacketLength(ByteBuffer[] buffers, int offset)
/* 147:    */   {
/* 148:238 */     ByteBuffer buffer = buffers[offset];
/* 149:241 */     if (buffer.remaining() >= 5) {
/* 150:242 */       return getEncryptedPacketLength(buffer);
/* 151:    */     }
/* 152:246 */     ByteBuffer tmp = ByteBuffer.allocate(5);
/* 153:    */     do
/* 154:    */     {
/* 155:249 */       buffer = buffers[(offset++)].duplicate();
/* 156:250 */       if (buffer.remaining() > tmp.remaining()) {
/* 157:251 */         buffer.limit(buffer.position() + tmp.remaining());
/* 158:    */       }
/* 159:253 */       tmp.put(buffer);
/* 160:254 */     } while (tmp.hasRemaining());
/* 161:257 */     tmp.flip();
/* 162:258 */     return getEncryptedPacketLength(tmp);
/* 163:    */   }
/* 164:    */   
/* 165:    */   private static int getEncryptedPacketLength(ByteBuffer buffer)
/* 166:    */   {
/* 167:262 */     int packetLength = 0;
/* 168:263 */     int pos = buffer.position();
/* 169:    */     boolean tls;
/* 170:    */     boolean tls;
/* 171:266 */     switch (unsignedByte(buffer.get(pos)))
/* 172:    */     {
/* 173:    */     case 20: 
/* 174:    */     case 21: 
/* 175:    */     case 22: 
/* 176:    */     case 23: 
/* 177:    */     case 24: 
/* 178:272 */       tls = true;
/* 179:273 */       break;
/* 180:    */     default: 
/* 181:276 */       tls = false;
/* 182:    */     }
/* 183:279 */     if (tls)
/* 184:    */     {
/* 185:281 */       int majorVersion = unsignedByte(buffer.get(pos + 1));
/* 186:282 */       if (majorVersion == 3)
/* 187:    */       {
/* 188:284 */         packetLength = unsignedShortBE(buffer, pos + 3) + 5;
/* 189:285 */         if (packetLength <= 5) {
/* 190:287 */           tls = false;
/* 191:    */         }
/* 192:    */       }
/* 193:    */       else
/* 194:    */       {
/* 195:291 */         tls = false;
/* 196:    */       }
/* 197:    */     }
/* 198:295 */     if (!tls)
/* 199:    */     {
/* 200:297 */       int headerLength = (unsignedByte(buffer.get(pos)) & 0x80) != 0 ? 2 : 3;
/* 201:298 */       int majorVersion = unsignedByte(buffer.get(pos + headerLength + 1));
/* 202:299 */       if ((majorVersion == 2) || (majorVersion == 3))
/* 203:    */       {
/* 204:302 */         packetLength = headerLength == 2 ? (shortBE(buffer, pos) & 0x7FFF) + 2 : (shortBE(buffer, pos) & 0x3FFF) + 3;
/* 205:303 */         if (packetLength <= headerLength) {
/* 206:304 */           return -1;
/* 207:    */         }
/* 208:    */       }
/* 209:    */       else
/* 210:    */       {
/* 211:307 */         return -2;
/* 212:    */       }
/* 213:    */     }
/* 214:310 */     return packetLength;
/* 215:    */   }
/* 216:    */   
/* 217:    */   static void notifyHandshakeFailure(ChannelHandlerContext ctx, Throwable cause, boolean notify)
/* 218:    */   {
/* 219:316 */     ctx.flush();
/* 220:317 */     if (notify) {
/* 221:318 */       ctx.fireUserEventTriggered(new SslHandshakeCompletionEvent(cause));
/* 222:    */     }
/* 223:320 */     ctx.close();
/* 224:    */   }
/* 225:    */   
/* 226:    */   static void zeroout(ByteBuf buffer)
/* 227:    */   {
/* 228:327 */     if (!buffer.isReadOnly()) {
/* 229:328 */       buffer.setZero(0, buffer.capacity());
/* 230:    */     }
/* 231:    */   }
/* 232:    */   
/* 233:    */   static void zerooutAndRelease(ByteBuf buffer)
/* 234:    */   {
/* 235:336 */     zeroout(buffer);
/* 236:337 */     buffer.release();
/* 237:    */   }
/* 238:    */   
/* 239:    */   static ByteBuf toBase64(ByteBufAllocator allocator, ByteBuf src)
/* 240:    */   {
/* 241:346 */     ByteBuf dst = Base64.encode(src, src.readerIndex(), src
/* 242:347 */       .readableBytes(), true, Base64Dialect.STANDARD, allocator);
/* 243:348 */     src.readerIndex(src.writerIndex());
/* 244:349 */     return dst;
/* 245:    */   }
/* 246:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.SslUtils
 * JD-Core Version:    0.7.0.1
 */