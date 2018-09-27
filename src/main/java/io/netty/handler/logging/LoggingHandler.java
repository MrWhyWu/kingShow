/*   1:    */ package io.netty.handler.logging;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufHolder;
/*   5:    */ import io.netty.buffer.ByteBufUtil;
/*   6:    */ import io.netty.channel.ChannelDuplexHandler;
/*   7:    */ import io.netty.channel.ChannelHandler.Sharable;
/*   8:    */ import io.netty.channel.ChannelHandlerContext;
/*   9:    */ import io.netty.channel.ChannelPromise;
/*  10:    */ import io.netty.util.internal.StringUtil;
/*  11:    */ import io.netty.util.internal.logging.InternalLogLevel;
/*  12:    */ import io.netty.util.internal.logging.InternalLogger;
/*  13:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  14:    */ import java.net.SocketAddress;
/*  15:    */ 
/*  16:    */ @ChannelHandler.Sharable
/*  17:    */ public class LoggingHandler
/*  18:    */   extends ChannelDuplexHandler
/*  19:    */ {
/*  20: 43 */   private static final LogLevel DEFAULT_LEVEL = LogLevel.DEBUG;
/*  21:    */   protected final InternalLogger logger;
/*  22:    */   protected final InternalLogLevel internalLevel;
/*  23:    */   private final LogLevel level;
/*  24:    */   
/*  25:    */   public LoggingHandler()
/*  26:    */   {
/*  27: 55 */     this(DEFAULT_LEVEL);
/*  28:    */   }
/*  29:    */   
/*  30:    */   public LoggingHandler(LogLevel level)
/*  31:    */   {
/*  32: 65 */     if (level == null) {
/*  33: 66 */       throw new NullPointerException("level");
/*  34:    */     }
/*  35: 69 */     this.logger = InternalLoggerFactory.getInstance(getClass());
/*  36: 70 */     this.level = level;
/*  37: 71 */     this.internalLevel = level.toInternalLevel();
/*  38:    */   }
/*  39:    */   
/*  40:    */   public LoggingHandler(Class<?> clazz)
/*  41:    */   {
/*  42: 81 */     this(clazz, DEFAULT_LEVEL);
/*  43:    */   }
/*  44:    */   
/*  45:    */   public LoggingHandler(Class<?> clazz, LogLevel level)
/*  46:    */   {
/*  47: 91 */     if (clazz == null) {
/*  48: 92 */       throw new NullPointerException("clazz");
/*  49:    */     }
/*  50: 94 */     if (level == null) {
/*  51: 95 */       throw new NullPointerException("level");
/*  52:    */     }
/*  53: 98 */     this.logger = InternalLoggerFactory.getInstance(clazz);
/*  54: 99 */     this.level = level;
/*  55:100 */     this.internalLevel = level.toInternalLevel();
/*  56:    */   }
/*  57:    */   
/*  58:    */   public LoggingHandler(String name)
/*  59:    */   {
/*  60:109 */     this(name, DEFAULT_LEVEL);
/*  61:    */   }
/*  62:    */   
/*  63:    */   public LoggingHandler(String name, LogLevel level)
/*  64:    */   {
/*  65:119 */     if (name == null) {
/*  66:120 */       throw new NullPointerException("name");
/*  67:    */     }
/*  68:122 */     if (level == null) {
/*  69:123 */       throw new NullPointerException("level");
/*  70:    */     }
/*  71:126 */     this.logger = InternalLoggerFactory.getInstance(name);
/*  72:127 */     this.level = level;
/*  73:128 */     this.internalLevel = level.toInternalLevel();
/*  74:    */   }
/*  75:    */   
/*  76:    */   public LogLevel level()
/*  77:    */   {
/*  78:135 */     return this.level;
/*  79:    */   }
/*  80:    */   
/*  81:    */   public void channelRegistered(ChannelHandlerContext ctx)
/*  82:    */     throws Exception
/*  83:    */   {
/*  84:140 */     if (this.logger.isEnabled(this.internalLevel)) {
/*  85:141 */       this.logger.log(this.internalLevel, format(ctx, "REGISTERED"));
/*  86:    */     }
/*  87:143 */     ctx.fireChannelRegistered();
/*  88:    */   }
/*  89:    */   
/*  90:    */   public void channelUnregistered(ChannelHandlerContext ctx)
/*  91:    */     throws Exception
/*  92:    */   {
/*  93:148 */     if (this.logger.isEnabled(this.internalLevel)) {
/*  94:149 */       this.logger.log(this.internalLevel, format(ctx, "UNREGISTERED"));
/*  95:    */     }
/*  96:151 */     ctx.fireChannelUnregistered();
/*  97:    */   }
/*  98:    */   
/*  99:    */   public void channelActive(ChannelHandlerContext ctx)
/* 100:    */     throws Exception
/* 101:    */   {
/* 102:156 */     if (this.logger.isEnabled(this.internalLevel)) {
/* 103:157 */       this.logger.log(this.internalLevel, format(ctx, "ACTIVE"));
/* 104:    */     }
/* 105:159 */     ctx.fireChannelActive();
/* 106:    */   }
/* 107:    */   
/* 108:    */   public void channelInactive(ChannelHandlerContext ctx)
/* 109:    */     throws Exception
/* 110:    */   {
/* 111:164 */     if (this.logger.isEnabled(this.internalLevel)) {
/* 112:165 */       this.logger.log(this.internalLevel, format(ctx, "INACTIVE"));
/* 113:    */     }
/* 114:167 */     ctx.fireChannelInactive();
/* 115:    */   }
/* 116:    */   
/* 117:    */   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
/* 118:    */     throws Exception
/* 119:    */   {
/* 120:172 */     if (this.logger.isEnabled(this.internalLevel)) {
/* 121:173 */       this.logger.log(this.internalLevel, format(ctx, "EXCEPTION", cause), cause);
/* 122:    */     }
/* 123:175 */     ctx.fireExceptionCaught(cause);
/* 124:    */   }
/* 125:    */   
/* 126:    */   public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
/* 127:    */     throws Exception
/* 128:    */   {
/* 129:180 */     if (this.logger.isEnabled(this.internalLevel)) {
/* 130:181 */       this.logger.log(this.internalLevel, format(ctx, "USER_EVENT", evt));
/* 131:    */     }
/* 132:183 */     ctx.fireUserEventTriggered(evt);
/* 133:    */   }
/* 134:    */   
/* 135:    */   public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise)
/* 136:    */     throws Exception
/* 137:    */   {
/* 138:188 */     if (this.logger.isEnabled(this.internalLevel)) {
/* 139:189 */       this.logger.log(this.internalLevel, format(ctx, "BIND", localAddress));
/* 140:    */     }
/* 141:191 */     ctx.bind(localAddress, promise);
/* 142:    */   }
/* 143:    */   
/* 144:    */   public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
/* 145:    */     throws Exception
/* 146:    */   {
/* 147:198 */     if (this.logger.isEnabled(this.internalLevel)) {
/* 148:199 */       this.logger.log(this.internalLevel, format(ctx, "CONNECT", remoteAddress, localAddress));
/* 149:    */     }
/* 150:201 */     ctx.connect(remoteAddress, localAddress, promise);
/* 151:    */   }
/* 152:    */   
/* 153:    */   public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise)
/* 154:    */     throws Exception
/* 155:    */   {
/* 156:206 */     if (this.logger.isEnabled(this.internalLevel)) {
/* 157:207 */       this.logger.log(this.internalLevel, format(ctx, "DISCONNECT"));
/* 158:    */     }
/* 159:209 */     ctx.disconnect(promise);
/* 160:    */   }
/* 161:    */   
/* 162:    */   public void close(ChannelHandlerContext ctx, ChannelPromise promise)
/* 163:    */     throws Exception
/* 164:    */   {
/* 165:214 */     if (this.logger.isEnabled(this.internalLevel)) {
/* 166:215 */       this.logger.log(this.internalLevel, format(ctx, "CLOSE"));
/* 167:    */     }
/* 168:217 */     ctx.close(promise);
/* 169:    */   }
/* 170:    */   
/* 171:    */   public void deregister(ChannelHandlerContext ctx, ChannelPromise promise)
/* 172:    */     throws Exception
/* 173:    */   {
/* 174:222 */     if (this.logger.isEnabled(this.internalLevel)) {
/* 175:223 */       this.logger.log(this.internalLevel, format(ctx, "DEREGISTER"));
/* 176:    */     }
/* 177:225 */     ctx.deregister(promise);
/* 178:    */   }
/* 179:    */   
/* 180:    */   public void channelReadComplete(ChannelHandlerContext ctx)
/* 181:    */     throws Exception
/* 182:    */   {
/* 183:230 */     if (this.logger.isEnabled(this.internalLevel)) {
/* 184:231 */       this.logger.log(this.internalLevel, format(ctx, "READ COMPLETE"));
/* 185:    */     }
/* 186:233 */     ctx.fireChannelReadComplete();
/* 187:    */   }
/* 188:    */   
/* 189:    */   public void channelRead(ChannelHandlerContext ctx, Object msg)
/* 190:    */     throws Exception
/* 191:    */   {
/* 192:238 */     if (this.logger.isEnabled(this.internalLevel)) {
/* 193:239 */       this.logger.log(this.internalLevel, format(ctx, "READ", msg));
/* 194:    */     }
/* 195:241 */     ctx.fireChannelRead(msg);
/* 196:    */   }
/* 197:    */   
/* 198:    */   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
/* 199:    */     throws Exception
/* 200:    */   {
/* 201:246 */     if (this.logger.isEnabled(this.internalLevel)) {
/* 202:247 */       this.logger.log(this.internalLevel, format(ctx, "WRITE", msg));
/* 203:    */     }
/* 204:249 */     ctx.write(msg, promise);
/* 205:    */   }
/* 206:    */   
/* 207:    */   public void channelWritabilityChanged(ChannelHandlerContext ctx)
/* 208:    */     throws Exception
/* 209:    */   {
/* 210:254 */     if (this.logger.isEnabled(this.internalLevel)) {
/* 211:255 */       this.logger.log(this.internalLevel, format(ctx, "WRITABILITY CHANGED"));
/* 212:    */     }
/* 213:257 */     ctx.fireChannelWritabilityChanged();
/* 214:    */   }
/* 215:    */   
/* 216:    */   public void flush(ChannelHandlerContext ctx)
/* 217:    */     throws Exception
/* 218:    */   {
/* 219:262 */     if (this.logger.isEnabled(this.internalLevel)) {
/* 220:263 */       this.logger.log(this.internalLevel, format(ctx, "FLUSH"));
/* 221:    */     }
/* 222:265 */     ctx.flush();
/* 223:    */   }
/* 224:    */   
/* 225:    */   protected String format(ChannelHandlerContext ctx, String eventName)
/* 226:    */   {
/* 227:274 */     String chStr = ctx.channel().toString();
/* 228:275 */     return chStr.length() + 1 + eventName.length() + chStr + 
/* 229:276 */       ' ' + 
/* 230:277 */       eventName;
/* 231:    */   }
/* 232:    */   
/* 233:    */   protected String format(ChannelHandlerContext ctx, String eventName, Object arg)
/* 234:    */   {
/* 235:289 */     if ((arg instanceof ByteBuf)) {
/* 236:290 */       return formatByteBuf(ctx, eventName, (ByteBuf)arg);
/* 237:    */     }
/* 238:291 */     if ((arg instanceof ByteBufHolder)) {
/* 239:292 */       return formatByteBufHolder(ctx, eventName, (ByteBufHolder)arg);
/* 240:    */     }
/* 241:294 */     return formatSimple(ctx, eventName, arg);
/* 242:    */   }
/* 243:    */   
/* 244:    */   protected String format(ChannelHandlerContext ctx, String eventName, Object firstArg, Object secondArg)
/* 245:    */   {
/* 246:307 */     if (secondArg == null) {
/* 247:308 */       return formatSimple(ctx, eventName, firstArg);
/* 248:    */     }
/* 249:311 */     String chStr = ctx.channel().toString();
/* 250:312 */     String arg1Str = String.valueOf(firstArg);
/* 251:313 */     String arg2Str = secondArg.toString();
/* 252:    */     
/* 253:315 */     StringBuilder buf = new StringBuilder(chStr.length() + 1 + eventName.length() + 2 + arg1Str.length() + 2 + arg2Str.length());
/* 254:316 */     buf.append(chStr).append(' ').append(eventName).append(": ").append(arg1Str).append(", ").append(arg2Str);
/* 255:317 */     return buf.toString();
/* 256:    */   }
/* 257:    */   
/* 258:    */   private static String formatByteBuf(ChannelHandlerContext ctx, String eventName, ByteBuf msg)
/* 259:    */   {
/* 260:324 */     String chStr = ctx.channel().toString();
/* 261:325 */     int length = msg.readableBytes();
/* 262:326 */     if (length == 0)
/* 263:    */     {
/* 264:327 */       StringBuilder buf = new StringBuilder(chStr.length() + 1 + eventName.length() + 4);
/* 265:328 */       buf.append(chStr).append(' ').append(eventName).append(": 0B");
/* 266:329 */       return buf.toString();
/* 267:    */     }
/* 268:331 */     int rows = length / 16 + (length % 15 == 0 ? 0 : 1) + 4;
/* 269:332 */     StringBuilder buf = new StringBuilder(chStr.length() + 1 + eventName.length() + 2 + 10 + 1 + 2 + rows * 80);
/* 270:    */     
/* 271:334 */     buf.append(chStr).append(' ').append(eventName).append(": ").append(length).append('B').append(StringUtil.NEWLINE);
/* 272:335 */     ByteBufUtil.appendPrettyHexDump(buf, msg);
/* 273:    */     
/* 274:337 */     return buf.toString();
/* 275:    */   }
/* 276:    */   
/* 277:    */   private static String formatByteBufHolder(ChannelHandlerContext ctx, String eventName, ByteBufHolder msg)
/* 278:    */   {
/* 279:345 */     String chStr = ctx.channel().toString();
/* 280:346 */     String msgStr = msg.toString();
/* 281:347 */     ByteBuf content = msg.content();
/* 282:348 */     int length = content.readableBytes();
/* 283:349 */     if (length == 0)
/* 284:    */     {
/* 285:350 */       StringBuilder buf = new StringBuilder(chStr.length() + 1 + eventName.length() + 2 + msgStr.length() + 4);
/* 286:351 */       buf.append(chStr).append(' ').append(eventName).append(", ").append(msgStr).append(", 0B");
/* 287:352 */       return buf.toString();
/* 288:    */     }
/* 289:354 */     int rows = length / 16 + (length % 15 == 0 ? 0 : 1) + 4;
/* 290:    */     
/* 291:356 */     StringBuilder buf = new StringBuilder(chStr.length() + 1 + eventName.length() + 2 + msgStr.length() + 2 + 10 + 1 + 2 + rows * 80);
/* 292:    */     
/* 293:358 */     buf.append(chStr).append(' ').append(eventName).append(": ")
/* 294:359 */       .append(msgStr).append(", ").append(length).append('B').append(StringUtil.NEWLINE);
/* 295:360 */     ByteBufUtil.appendPrettyHexDump(buf, content);
/* 296:    */     
/* 297:362 */     return buf.toString();
/* 298:    */   }
/* 299:    */   
/* 300:    */   private static String formatSimple(ChannelHandlerContext ctx, String eventName, Object msg)
/* 301:    */   {
/* 302:370 */     String chStr = ctx.channel().toString();
/* 303:371 */     String msgStr = String.valueOf(msg);
/* 304:372 */     StringBuilder buf = new StringBuilder(chStr.length() + 1 + eventName.length() + 2 + msgStr.length());
/* 305:373 */     return chStr + ' ' + eventName + ": " + msgStr;
/* 306:    */   }
/* 307:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.logging.LoggingHandler
 * JD-Core Version:    0.7.0.1
 */