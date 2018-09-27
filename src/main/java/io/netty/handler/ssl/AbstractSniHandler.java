/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufUtil;
/*   5:    */ import io.netty.channel.ChannelHandlerContext;
/*   6:    */ import io.netty.channel.ChannelOutboundHandler;
/*   7:    */ import io.netty.channel.ChannelPromise;
/*   8:    */ import io.netty.handler.codec.ByteToMessageDecoder;
/*   9:    */ import io.netty.handler.codec.DecoderException;
/*  10:    */ import io.netty.util.CharsetUtil;
/*  11:    */ import io.netty.util.concurrent.Future;
/*  12:    */ import io.netty.util.concurrent.FutureListener;
/*  13:    */ import io.netty.util.internal.PlatformDependent;
/*  14:    */ import io.netty.util.internal.logging.InternalLogger;
/*  15:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  16:    */ import java.net.SocketAddress;
/*  17:    */ import java.util.List;
/*  18:    */ import java.util.Locale;
/*  19:    */ 
/*  20:    */ public abstract class AbstractSniHandler<T>
/*  21:    */   extends ByteToMessageDecoder
/*  22:    */   implements ChannelOutboundHandler
/*  23:    */ {
/*  24:    */   private static final int MAX_SSL_RECORDS = 4;
/*  25: 49 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractSniHandler.class);
/*  26:    */   private boolean handshakeFailed;
/*  27:    */   private boolean suppressRead;
/*  28:    */   private boolean readPending;
/*  29:    */   
/*  30:    */   protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/*  31:    */     throws Exception
/*  32:    */   {
/*  33: 57 */     if ((!this.suppressRead) && (!this.handshakeFailed))
/*  34:    */     {
/*  35: 58 */       int writerIndex = in.writerIndex();
/*  36:    */       try
/*  37:    */       {
/*  38: 61 */         for (int i = 0; i < 4; i++)
/*  39:    */         {
/*  40: 62 */           int readerIndex = in.readerIndex();
/*  41: 63 */           int readableBytes = writerIndex - readerIndex;
/*  42: 64 */           if (readableBytes < 5) {
/*  43: 66 */             return;
/*  44:    */           }
/*  45: 69 */           int command = in.getUnsignedByte(readerIndex);
/*  46: 72 */           switch (command)
/*  47:    */           {
/*  48:    */           case 20: 
/*  49:    */           case 21: 
/*  50: 75 */             int len = SslUtils.getEncryptedPacketLength(in, readerIndex);
/*  51: 78 */             if (len == -2)
/*  52:    */             {
/*  53: 79 */               this.handshakeFailed = true;
/*  54:    */               
/*  55: 81 */               NotSslRecordException e = new NotSslRecordException("not an SSL/TLS record: " + ByteBufUtil.hexDump(in));
/*  56: 82 */               in.skipBytes(in.readableBytes());
/*  57: 83 */               ctx.fireUserEventTriggered(new SniCompletionEvent(e));
/*  58: 84 */               SslUtils.notifyHandshakeFailure(ctx, e, true);
/*  59: 85 */               throw e;
/*  60:    */             }
/*  61: 87 */             if ((len == -1) || (writerIndex - readerIndex - 5 < len)) {
/*  62: 90 */               return;
/*  63:    */             }
/*  64: 93 */             in.skipBytes(len);
/*  65: 94 */             break;
/*  66:    */           case 22: 
/*  67: 96 */             int majorVersion = in.getUnsignedByte(readerIndex + 1);
/*  68: 99 */             if (majorVersion == 3)
/*  69:    */             {
/*  70:100 */               int packetLength = in.getUnsignedShort(readerIndex + 3) + 5;
/*  71:103 */               if (readableBytes < packetLength) {
/*  72:105 */                 return;
/*  73:    */               }
/*  74:128 */               int endOffset = readerIndex + packetLength;
/*  75:129 */               int offset = readerIndex + 43;
/*  76:131 */               if (endOffset - offset < 6) {
/*  77:    */                 break;
/*  78:    */               }
/*  79:135 */               int sessionIdLength = in.getUnsignedByte(offset);
/*  80:136 */               offset += sessionIdLength + 1;
/*  81:    */               
/*  82:138 */               int cipherSuitesLength = in.getUnsignedShort(offset);
/*  83:139 */               offset += cipherSuitesLength + 2;
/*  84:    */               
/*  85:141 */               int compressionMethodLength = in.getUnsignedByte(offset);
/*  86:142 */               offset += compressionMethodLength + 1;
/*  87:    */               
/*  88:144 */               int extensionsLength = in.getUnsignedShort(offset);
/*  89:145 */               offset += 2;
/*  90:146 */               int extensionsLimit = offset + extensionsLength;
/*  91:148 */               if (extensionsLimit > endOffset) {
/*  92:    */                 break;
/*  93:    */               }
/*  94:154 */               while (extensionsLimit - offset >= 4)
/*  95:    */               {
/*  96:158 */                 int extensionType = in.getUnsignedShort(offset);
/*  97:159 */                 offset += 2;
/*  98:    */                 
/*  99:161 */                 int extensionLength = in.getUnsignedShort(offset);
/* 100:162 */                 offset += 2;
/* 101:164 */                 if (extensionsLimit - offset < extensionLength) {
/* 102:    */                   break;
/* 103:    */                 }
/* 104:170 */                 if (extensionType == 0)
/* 105:    */                 {
/* 106:171 */                   offset += 2;
/* 107:172 */                   if (extensionsLimit - offset < 3) {
/* 108:    */                     break;
/* 109:    */                   }
/* 110:176 */                   int serverNameType = in.getUnsignedByte(offset);
/* 111:177 */                   offset++;
/* 112:179 */                   if (serverNameType != 0) {
/* 113:    */                     break;
/* 114:    */                   }
/* 115:180 */                   int serverNameLength = in.getUnsignedShort(offset);
/* 116:181 */                   offset += 2;
/* 117:183 */                   if (extensionsLimit - offset < serverNameLength) {
/* 118:    */                     break;
/* 119:    */                   }
/* 120:187 */                   String hostname = in.toString(offset, serverNameLength, CharsetUtil.US_ASCII);
/* 121:    */                   try
/* 122:    */                   {
/* 123:191 */                     select(ctx, hostname.toLowerCase(Locale.US));
/* 124:    */                   }
/* 125:    */                   catch (Throwable t)
/* 126:    */                   {
/* 127:193 */                     PlatformDependent.throwException(t);
/* 128:    */                   }
/* 129:195 */                   return;
/* 130:    */                 }
/* 131:202 */                 offset += extensionLength;
/* 132:    */               }
/* 133:    */             }
/* 134:    */             break;
/* 135:    */           }
/* 136:208 */           break;
/* 137:    */         }
/* 138:    */       }
/* 139:    */       catch (NotSslRecordException e)
/* 140:    */       {
/* 141:213 */         throw e;
/* 142:    */       }
/* 143:    */       catch (Exception e)
/* 144:    */       {
/* 145:216 */         if (logger.isDebugEnabled()) {
/* 146:217 */           logger.debug("Unexpected client hello packet: " + ByteBufUtil.hexDump(in), e);
/* 147:    */         }
/* 148:    */       }
/* 149:221 */       select(ctx, null);
/* 150:    */     }
/* 151:    */   }
/* 152:    */   
/* 153:    */   private void select(final ChannelHandlerContext ctx, final String hostname)
/* 154:    */     throws Exception
/* 155:    */   {
/* 156:226 */     Future<T> future = lookup(ctx, hostname);
/* 157:227 */     if (future.isDone())
/* 158:    */     {
/* 159:228 */       fireSniCompletionEvent(ctx, hostname, future);
/* 160:229 */       onLookupComplete(ctx, hostname, future);
/* 161:    */     }
/* 162:    */     else
/* 163:    */     {
/* 164:231 */       this.suppressRead = true;
/* 165:232 */       future.addListener(new FutureListener()
/* 166:    */       {
/* 167:    */         public void operationComplete(Future<T> future)
/* 168:    */           throws Exception
/* 169:    */         {
/* 170:    */           try
/* 171:    */           {
/* 172:236 */             AbstractSniHandler.this.suppressRead = false;
/* 173:    */             try
/* 174:    */             {
/* 175:238 */               AbstractSniHandler.this.fireSniCompletionEvent(ctx, hostname, future);
/* 176:239 */               AbstractSniHandler.this.onLookupComplete(ctx, hostname, future);
/* 177:    */             }
/* 178:    */             catch (DecoderException err)
/* 179:    */             {
/* 180:241 */               ctx.fireExceptionCaught(err);
/* 181:    */             }
/* 182:    */             catch (Exception cause)
/* 183:    */             {
/* 184:243 */               ctx.fireExceptionCaught(new DecoderException(cause));
/* 185:    */             }
/* 186:    */             catch (Throwable cause)
/* 187:    */             {
/* 188:245 */               ctx.fireExceptionCaught(cause);
/* 189:    */             }
/* 190:248 */             if (AbstractSniHandler.this.readPending)
/* 191:    */             {
/* 192:249 */               AbstractSniHandler.this.readPending = false;
/* 193:250 */               ctx.read();
/* 194:    */             }
/* 195:    */           }
/* 196:    */           finally
/* 197:    */           {
/* 198:248 */             if (AbstractSniHandler.this.readPending)
/* 199:    */             {
/* 200:249 */               AbstractSniHandler.this.readPending = false;
/* 201:250 */               ctx.read();
/* 202:    */             }
/* 203:    */           }
/* 204:    */         }
/* 205:    */       });
/* 206:    */     }
/* 207:    */   }
/* 208:    */   
/* 209:    */   private void fireSniCompletionEvent(ChannelHandlerContext ctx, String hostname, Future<T> future)
/* 210:    */   {
/* 211:259 */     Throwable cause = future.cause();
/* 212:260 */     if (cause == null) {
/* 213:261 */       ctx.fireUserEventTriggered(new SniCompletionEvent(hostname));
/* 214:    */     } else {
/* 215:263 */       ctx.fireUserEventTriggered(new SniCompletionEvent(hostname, cause));
/* 216:    */     }
/* 217:    */   }
/* 218:    */   
/* 219:    */   protected abstract Future<T> lookup(ChannelHandlerContext paramChannelHandlerContext, String paramString)
/* 220:    */     throws Exception;
/* 221:    */   
/* 222:    */   protected abstract void onLookupComplete(ChannelHandlerContext paramChannelHandlerContext, String paramString, Future<T> paramFuture)
/* 223:    */     throws Exception;
/* 224:    */   
/* 225:    */   public void read(ChannelHandlerContext ctx)
/* 226:    */     throws Exception
/* 227:    */   {
/* 228:285 */     if (this.suppressRead) {
/* 229:286 */       this.readPending = true;
/* 230:    */     } else {
/* 231:288 */       ctx.read();
/* 232:    */     }
/* 233:    */   }
/* 234:    */   
/* 235:    */   public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise)
/* 236:    */     throws Exception
/* 237:    */   {
/* 238:294 */     ctx.bind(localAddress, promise);
/* 239:    */   }
/* 240:    */   
/* 241:    */   public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
/* 242:    */     throws Exception
/* 243:    */   {
/* 244:300 */     ctx.connect(remoteAddress, localAddress, promise);
/* 245:    */   }
/* 246:    */   
/* 247:    */   public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise)
/* 248:    */     throws Exception
/* 249:    */   {
/* 250:305 */     ctx.disconnect(promise);
/* 251:    */   }
/* 252:    */   
/* 253:    */   public void close(ChannelHandlerContext ctx, ChannelPromise promise)
/* 254:    */     throws Exception
/* 255:    */   {
/* 256:310 */     ctx.close(promise);
/* 257:    */   }
/* 258:    */   
/* 259:    */   public void deregister(ChannelHandlerContext ctx, ChannelPromise promise)
/* 260:    */     throws Exception
/* 261:    */   {
/* 262:315 */     ctx.deregister(promise);
/* 263:    */   }
/* 264:    */   
/* 265:    */   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
/* 266:    */     throws Exception
/* 267:    */   {
/* 268:320 */     ctx.write(msg, promise);
/* 269:    */   }
/* 270:    */   
/* 271:    */   public void flush(ChannelHandlerContext ctx)
/* 272:    */     throws Exception
/* 273:    */   {
/* 274:325 */     ctx.flush();
/* 275:    */   }
/* 276:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.AbstractSniHandler
 * JD-Core Version:    0.7.0.1
 */