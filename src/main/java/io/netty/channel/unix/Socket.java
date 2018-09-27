/*   1:    */ package io.netty.channel.unix;
/*   2:    */ 
/*   3:    */ import io.netty.channel.ChannelException;
/*   4:    */ import io.netty.util.CharsetUtil;
/*   5:    */ import io.netty.util.NetUtil;
/*   6:    */ import io.netty.util.internal.ThrowableUtil;
/*   7:    */ import java.io.IOException;
/*   8:    */ import java.net.Inet6Address;
/*   9:    */ import java.net.InetAddress;
/*  10:    */ import java.net.InetSocketAddress;
/*  11:    */ import java.net.PortUnreachableException;
/*  12:    */ import java.net.SocketAddress;
/*  13:    */ import java.nio.ByteBuffer;
/*  14:    */ import java.nio.channels.ClosedChannelException;
/*  15:    */ import java.util.concurrent.atomic.AtomicBoolean;
/*  16:    */ 
/*  17:    */ public class Socket
/*  18:    */   extends FileDescriptor
/*  19:    */ {
/*  20: 49 */   private static final ClosedChannelException SHUTDOWN_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), Socket.class, "shutdown(..)");
/*  21: 51 */   private static final ClosedChannelException SEND_TO_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), Socket.class, "sendTo(..)");
/*  22: 54 */   private static final ClosedChannelException SEND_TO_ADDRESS_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), Socket.class, "sendToAddress(..)");
/*  23: 56 */   private static final ClosedChannelException SEND_TO_ADDRESSES_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), Socket.class, "sendToAddresses(..)");
/*  24: 57 */   private static final Errors.NativeIoException SEND_TO_CONNECTION_RESET_EXCEPTION = (Errors.NativeIoException)ThrowableUtil.unknownStackTrace(
/*  25: 58 */     Errors.newConnectionResetException("syscall:sendto", Errors.ERRNO_EPIPE_NEGATIVE), Socket.class, "sendTo(..)");
/*  26: 61 */   private static final Errors.NativeIoException SEND_TO_ADDRESS_CONNECTION_RESET_EXCEPTION = (Errors.NativeIoException)ThrowableUtil.unknownStackTrace(Errors.newConnectionResetException("syscall:sendto", Errors.ERRNO_EPIPE_NEGATIVE), Socket.class, "sendToAddress");
/*  27: 63 */   private static final Errors.NativeIoException CONNECTION_RESET_EXCEPTION_SENDMSG = (Errors.NativeIoException)ThrowableUtil.unknownStackTrace(
/*  28: 64 */     Errors.newConnectionResetException("syscall:sendmsg", Errors.ERRNO_EPIPE_NEGATIVE), Socket.class, "sendToAddresses(..)");
/*  29: 67 */   private static final Errors.NativeIoException CONNECTION_RESET_SHUTDOWN_EXCEPTION = (Errors.NativeIoException)ThrowableUtil.unknownStackTrace(Errors.newConnectionResetException("syscall:shutdown", Errors.ERRNO_ECONNRESET_NEGATIVE), Socket.class, "shutdown");
/*  30: 70 */   private static final Errors.NativeConnectException FINISH_CONNECT_REFUSED_EXCEPTION = (Errors.NativeConnectException)ThrowableUtil.unknownStackTrace(new Errors.NativeConnectException("syscall:getsockopt", Errors.ERROR_ECONNREFUSED_NEGATIVE), Socket.class, "finishConnect(..)");
/*  31: 73 */   private static final Errors.NativeConnectException CONNECT_REFUSED_EXCEPTION = (Errors.NativeConnectException)ThrowableUtil.unknownStackTrace(new Errors.NativeConnectException("syscall:connect", Errors.ERROR_ECONNREFUSED_NEGATIVE), Socket.class, "connect(..)");
/*  32: 76 */   public static final int UDS_SUN_PATH_SIZE = LimitsStaticallyReferencedJniMethods.udsSunPathSize();
/*  33:    */   
/*  34:    */   public Socket(int fd)
/*  35:    */   {
/*  36: 79 */     super(fd);
/*  37:    */   }
/*  38:    */   
/*  39:    */   public final void shutdown()
/*  40:    */     throws IOException
/*  41:    */   {
/*  42: 83 */     shutdown(true, true);
/*  43:    */   }
/*  44:    */   
/*  45:    */   public final void shutdown(boolean read, boolean write)
/*  46:    */     throws IOException
/*  47:    */   {
/*  48:    */     for (;;)
/*  49:    */     {
/*  50: 92 */       int oldState = this.state;
/*  51: 93 */       if (isClosed(oldState)) {
/*  52: 94 */         throw new ClosedChannelException();
/*  53:    */       }
/*  54: 96 */       int newState = oldState;
/*  55: 97 */       if ((read) && (!isInputShutdown(newState))) {
/*  56: 98 */         newState = inputShutdown(newState);
/*  57:    */       }
/*  58:100 */       if ((write) && (!isOutputShutdown(newState))) {
/*  59:101 */         newState = outputShutdown(newState);
/*  60:    */       }
/*  61:105 */       if (newState == oldState) {
/*  62:106 */         return;
/*  63:    */       }
/*  64:108 */       if (casState(oldState, newState)) {
/*  65:    */         break;
/*  66:    */       }
/*  67:    */     }
/*  68:112 */     int res = shutdown(this.fd, read, write);
/*  69:113 */     if (res < 0) {
/*  70:114 */       Errors.ioResult("shutdown", res, CONNECTION_RESET_SHUTDOWN_EXCEPTION, SHUTDOWN_CLOSED_CHANNEL_EXCEPTION);
/*  71:    */     }
/*  72:    */   }
/*  73:    */   
/*  74:    */   public final boolean isShutdown()
/*  75:    */   {
/*  76:119 */     int state = this.state;
/*  77:120 */     return (isInputShutdown(state)) && (isOutputShutdown(state));
/*  78:    */   }
/*  79:    */   
/*  80:    */   public final boolean isInputShutdown()
/*  81:    */   {
/*  82:124 */     return isInputShutdown(this.state);
/*  83:    */   }
/*  84:    */   
/*  85:    */   public final boolean isOutputShutdown()
/*  86:    */   {
/*  87:128 */     return isOutputShutdown(this.state);
/*  88:    */   }
/*  89:    */   
/*  90:    */   public final int sendTo(ByteBuffer buf, int pos, int limit, InetAddress addr, int port)
/*  91:    */     throws IOException
/*  92:    */   {
/*  93:    */     int scopeId;
/*  94:    */     int scopeId;
/*  95:    */     byte[] address;
/*  96:136 */     if ((addr instanceof Inet6Address))
/*  97:    */     {
/*  98:137 */       byte[] address = addr.getAddress();
/*  99:138 */       scopeId = ((Inet6Address)addr).getScopeId();
/* 100:    */     }
/* 101:    */     else
/* 102:    */     {
/* 103:141 */       scopeId = 0;
/* 104:142 */       address = NativeInetAddress.ipv4MappedIpv6Address(addr.getAddress());
/* 105:    */     }
/* 106:144 */     int res = sendTo(this.fd, buf, pos, limit, address, scopeId, port);
/* 107:145 */     if (res >= 0) {
/* 108:146 */       return res;
/* 109:    */     }
/* 110:148 */     if (res == Errors.ERROR_ECONNREFUSED_NEGATIVE) {
/* 111:149 */       throw new PortUnreachableException("sendTo failed");
/* 112:    */     }
/* 113:151 */     return Errors.ioResult("sendTo", res, SEND_TO_CONNECTION_RESET_EXCEPTION, SEND_TO_CLOSED_CHANNEL_EXCEPTION);
/* 114:    */   }
/* 115:    */   
/* 116:    */   public final int sendToAddress(long memoryAddress, int pos, int limit, InetAddress addr, int port)
/* 117:    */     throws IOException
/* 118:    */   {
/* 119:    */     int scopeId;
/* 120:    */     int scopeId;
/* 121:    */     byte[] address;
/* 122:160 */     if ((addr instanceof Inet6Address))
/* 123:    */     {
/* 124:161 */       byte[] address = addr.getAddress();
/* 125:162 */       scopeId = ((Inet6Address)addr).getScopeId();
/* 126:    */     }
/* 127:    */     else
/* 128:    */     {
/* 129:165 */       scopeId = 0;
/* 130:166 */       address = NativeInetAddress.ipv4MappedIpv6Address(addr.getAddress());
/* 131:    */     }
/* 132:168 */     int res = sendToAddress(this.fd, memoryAddress, pos, limit, address, scopeId, port);
/* 133:169 */     if (res >= 0) {
/* 134:170 */       return res;
/* 135:    */     }
/* 136:172 */     if (res == Errors.ERROR_ECONNREFUSED_NEGATIVE) {
/* 137:173 */       throw new PortUnreachableException("sendToAddress failed");
/* 138:    */     }
/* 139:175 */     return Errors.ioResult("sendToAddress", res, SEND_TO_ADDRESS_CONNECTION_RESET_EXCEPTION, SEND_TO_ADDRESS_CLOSED_CHANNEL_EXCEPTION);
/* 140:    */   }
/* 141:    */   
/* 142:    */   public final int sendToAddresses(long memoryAddress, int length, InetAddress addr, int port)
/* 143:    */     throws IOException
/* 144:    */   {
/* 145:    */     int scopeId;
/* 146:    */     int scopeId;
/* 147:    */     byte[] address;
/* 148:184 */     if ((addr instanceof Inet6Address))
/* 149:    */     {
/* 150:185 */       byte[] address = addr.getAddress();
/* 151:186 */       scopeId = ((Inet6Address)addr).getScopeId();
/* 152:    */     }
/* 153:    */     else
/* 154:    */     {
/* 155:189 */       scopeId = 0;
/* 156:190 */       address = NativeInetAddress.ipv4MappedIpv6Address(addr.getAddress());
/* 157:    */     }
/* 158:192 */     int res = sendToAddresses(this.fd, memoryAddress, length, address, scopeId, port);
/* 159:193 */     if (res >= 0) {
/* 160:194 */       return res;
/* 161:    */     }
/* 162:197 */     if (res == Errors.ERROR_ECONNREFUSED_NEGATIVE) {
/* 163:198 */       throw new PortUnreachableException("sendToAddresses failed");
/* 164:    */     }
/* 165:200 */     return Errors.ioResult("sendToAddresses", res, CONNECTION_RESET_EXCEPTION_SENDMSG, SEND_TO_ADDRESSES_CLOSED_CHANNEL_EXCEPTION);
/* 166:    */   }
/* 167:    */   
/* 168:    */   public final DatagramSocketAddress recvFrom(ByteBuffer buf, int pos, int limit)
/* 169:    */     throws IOException
/* 170:    */   {
/* 171:205 */     return recvFrom(this.fd, buf, pos, limit);
/* 172:    */   }
/* 173:    */   
/* 174:    */   public final DatagramSocketAddress recvFromAddress(long memoryAddress, int pos, int limit)
/* 175:    */     throws IOException
/* 176:    */   {
/* 177:209 */     return recvFromAddress(this.fd, memoryAddress, pos, limit);
/* 178:    */   }
/* 179:    */   
/* 180:    */   public final int recvFd()
/* 181:    */     throws IOException
/* 182:    */   {
/* 183:213 */     int res = recvFd(this.fd);
/* 184:214 */     if (res > 0) {
/* 185:215 */       return res;
/* 186:    */     }
/* 187:217 */     if (res == 0) {
/* 188:218 */       return -1;
/* 189:    */     }
/* 190:221 */     if ((res == Errors.ERRNO_EAGAIN_NEGATIVE) || (res == Errors.ERRNO_EWOULDBLOCK_NEGATIVE)) {
/* 191:223 */       return 0;
/* 192:    */     }
/* 193:225 */     throw Errors.newIOException("recvFd", res);
/* 194:    */   }
/* 195:    */   
/* 196:    */   public final int sendFd(int fdToSend)
/* 197:    */     throws IOException
/* 198:    */   {
/* 199:229 */     int res = sendFd(this.fd, fdToSend);
/* 200:230 */     if (res >= 0) {
/* 201:231 */       return res;
/* 202:    */     }
/* 203:233 */     if ((res == Errors.ERRNO_EAGAIN_NEGATIVE) || (res == Errors.ERRNO_EWOULDBLOCK_NEGATIVE)) {
/* 204:235 */       return -1;
/* 205:    */     }
/* 206:237 */     throw Errors.newIOException("sendFd", res);
/* 207:    */   }
/* 208:    */   
/* 209:    */   public final boolean connect(SocketAddress socketAddress)
/* 210:    */     throws IOException
/* 211:    */   {
/* 212:    */     int res;
/* 213:242 */     if ((socketAddress instanceof InetSocketAddress))
/* 214:    */     {
/* 215:243 */       InetSocketAddress inetSocketAddress = (InetSocketAddress)socketAddress;
/* 216:244 */       NativeInetAddress address = NativeInetAddress.newInstance(inetSocketAddress.getAddress());
/* 217:245 */       res = connect(this.fd, address.address, address.scopeId, inetSocketAddress.getPort());
/* 218:    */     }
/* 219:    */     else
/* 220:    */     {
/* 221:    */       int res;
/* 222:246 */       if ((socketAddress instanceof DomainSocketAddress))
/* 223:    */       {
/* 224:247 */         DomainSocketAddress unixDomainSocketAddress = (DomainSocketAddress)socketAddress;
/* 225:248 */         res = connectDomainSocket(this.fd, unixDomainSocketAddress.path().getBytes(CharsetUtil.UTF_8));
/* 226:    */       }
/* 227:    */       else
/* 228:    */       {
/* 229:250 */         throw new Error("Unexpected SocketAddress implementation " + socketAddress);
/* 230:    */       }
/* 231:    */     }
/* 232:    */     int res;
/* 233:252 */     if (res < 0)
/* 234:    */     {
/* 235:253 */       if (res == Errors.ERRNO_EINPROGRESS_NEGATIVE) {
/* 236:255 */         return false;
/* 237:    */       }
/* 238:257 */       Errors.throwConnectException("connect", CONNECT_REFUSED_EXCEPTION, res);
/* 239:    */     }
/* 240:259 */     return true;
/* 241:    */   }
/* 242:    */   
/* 243:    */   public final boolean finishConnect()
/* 244:    */     throws IOException
/* 245:    */   {
/* 246:263 */     int res = finishConnect(this.fd);
/* 247:264 */     if (res < 0)
/* 248:    */     {
/* 249:265 */       if (res == Errors.ERRNO_EINPROGRESS_NEGATIVE) {
/* 250:267 */         return false;
/* 251:    */       }
/* 252:269 */       Errors.throwConnectException("finishConnect", FINISH_CONNECT_REFUSED_EXCEPTION, res);
/* 253:    */     }
/* 254:271 */     return true;
/* 255:    */   }
/* 256:    */   
/* 257:    */   public final void disconnect()
/* 258:    */     throws IOException
/* 259:    */   {
/* 260:275 */     int res = disconnect(this.fd);
/* 261:276 */     if (res < 0) {
/* 262:277 */       Errors.throwConnectException("disconnect", FINISH_CONNECT_REFUSED_EXCEPTION, res);
/* 263:    */     }
/* 264:    */   }
/* 265:    */   
/* 266:    */   public final void bind(SocketAddress socketAddress)
/* 267:    */     throws IOException
/* 268:    */   {
/* 269:282 */     if ((socketAddress instanceof InetSocketAddress))
/* 270:    */     {
/* 271:283 */       InetSocketAddress addr = (InetSocketAddress)socketAddress;
/* 272:284 */       NativeInetAddress address = NativeInetAddress.newInstance(addr.getAddress());
/* 273:285 */       int res = bind(this.fd, address.address, address.scopeId, addr.getPort());
/* 274:286 */       if (res < 0) {
/* 275:287 */         throw Errors.newIOException("bind", res);
/* 276:    */       }
/* 277:    */     }
/* 278:289 */     else if ((socketAddress instanceof DomainSocketAddress))
/* 279:    */     {
/* 280:290 */       DomainSocketAddress addr = (DomainSocketAddress)socketAddress;
/* 281:291 */       int res = bindDomainSocket(this.fd, addr.path().getBytes(CharsetUtil.UTF_8));
/* 282:292 */       if (res < 0) {
/* 283:293 */         throw Errors.newIOException("bind", res);
/* 284:    */       }
/* 285:    */     }
/* 286:    */     else
/* 287:    */     {
/* 288:296 */       throw new Error("Unexpected SocketAddress implementation " + socketAddress);
/* 289:    */     }
/* 290:    */   }
/* 291:    */   
/* 292:    */   public final void listen(int backlog)
/* 293:    */     throws IOException
/* 294:    */   {
/* 295:301 */     int res = listen(this.fd, backlog);
/* 296:302 */     if (res < 0) {
/* 297:303 */       throw Errors.newIOException("listen", res);
/* 298:    */     }
/* 299:    */   }
/* 300:    */   
/* 301:    */   public final int accept(byte[] addr)
/* 302:    */     throws IOException
/* 303:    */   {
/* 304:308 */     int res = accept(this.fd, addr);
/* 305:309 */     if (res >= 0) {
/* 306:310 */       return res;
/* 307:    */     }
/* 308:312 */     if ((res == Errors.ERRNO_EAGAIN_NEGATIVE) || (res == Errors.ERRNO_EWOULDBLOCK_NEGATIVE)) {
/* 309:314 */       return -1;
/* 310:    */     }
/* 311:316 */     throw Errors.newIOException("accept", res);
/* 312:    */   }
/* 313:    */   
/* 314:    */   public final InetSocketAddress remoteAddress()
/* 315:    */   {
/* 316:320 */     byte[] addr = remoteAddress(this.fd);
/* 317:    */     
/* 318:    */ 
/* 319:323 */     return addr == null ? null : NativeInetAddress.address(addr, 0, addr.length);
/* 320:    */   }
/* 321:    */   
/* 322:    */   public final InetSocketAddress localAddress()
/* 323:    */   {
/* 324:327 */     byte[] addr = localAddress(this.fd);
/* 325:    */     
/* 326:    */ 
/* 327:330 */     return addr == null ? null : NativeInetAddress.address(addr, 0, addr.length);
/* 328:    */   }
/* 329:    */   
/* 330:    */   public final int getReceiveBufferSize()
/* 331:    */     throws IOException
/* 332:    */   {
/* 333:334 */     return getReceiveBufferSize(this.fd);
/* 334:    */   }
/* 335:    */   
/* 336:    */   public final int getSendBufferSize()
/* 337:    */     throws IOException
/* 338:    */   {
/* 339:338 */     return getSendBufferSize(this.fd);
/* 340:    */   }
/* 341:    */   
/* 342:    */   public final boolean isKeepAlive()
/* 343:    */     throws IOException
/* 344:    */   {
/* 345:342 */     return isKeepAlive(this.fd) != 0;
/* 346:    */   }
/* 347:    */   
/* 348:    */   public final boolean isTcpNoDelay()
/* 349:    */     throws IOException
/* 350:    */   {
/* 351:346 */     return isTcpNoDelay(this.fd) != 0;
/* 352:    */   }
/* 353:    */   
/* 354:    */   public final boolean isReuseAddress()
/* 355:    */     throws IOException
/* 356:    */   {
/* 357:350 */     return isReuseAddress(this.fd) != 0;
/* 358:    */   }
/* 359:    */   
/* 360:    */   public final boolean isReusePort()
/* 361:    */     throws IOException
/* 362:    */   {
/* 363:354 */     return isReusePort(this.fd) != 0;
/* 364:    */   }
/* 365:    */   
/* 366:    */   public final boolean isBroadcast()
/* 367:    */     throws IOException
/* 368:    */   {
/* 369:358 */     return isBroadcast(this.fd) != 0;
/* 370:    */   }
/* 371:    */   
/* 372:    */   public final int getSoLinger()
/* 373:    */     throws IOException
/* 374:    */   {
/* 375:362 */     return getSoLinger(this.fd);
/* 376:    */   }
/* 377:    */   
/* 378:    */   public final int getSoError()
/* 379:    */     throws IOException
/* 380:    */   {
/* 381:366 */     return getSoError(this.fd);
/* 382:    */   }
/* 383:    */   
/* 384:    */   public final int getTrafficClass()
/* 385:    */     throws IOException
/* 386:    */   {
/* 387:370 */     return getTrafficClass(this.fd);
/* 388:    */   }
/* 389:    */   
/* 390:    */   public final void setKeepAlive(boolean keepAlive)
/* 391:    */     throws IOException
/* 392:    */   {
/* 393:374 */     setKeepAlive(this.fd, keepAlive ? 1 : 0);
/* 394:    */   }
/* 395:    */   
/* 396:    */   public final void setReceiveBufferSize(int receiveBufferSize)
/* 397:    */     throws IOException
/* 398:    */   {
/* 399:378 */     setReceiveBufferSize(this.fd, receiveBufferSize);
/* 400:    */   }
/* 401:    */   
/* 402:    */   public final void setSendBufferSize(int sendBufferSize)
/* 403:    */     throws IOException
/* 404:    */   {
/* 405:382 */     setSendBufferSize(this.fd, sendBufferSize);
/* 406:    */   }
/* 407:    */   
/* 408:    */   public final void setTcpNoDelay(boolean tcpNoDelay)
/* 409:    */     throws IOException
/* 410:    */   {
/* 411:386 */     setTcpNoDelay(this.fd, tcpNoDelay ? 1 : 0);
/* 412:    */   }
/* 413:    */   
/* 414:    */   public final void setSoLinger(int soLinger)
/* 415:    */     throws IOException
/* 416:    */   {
/* 417:390 */     setSoLinger(this.fd, soLinger);
/* 418:    */   }
/* 419:    */   
/* 420:    */   public final void setReuseAddress(boolean reuseAddress)
/* 421:    */     throws IOException
/* 422:    */   {
/* 423:394 */     setReuseAddress(this.fd, reuseAddress ? 1 : 0);
/* 424:    */   }
/* 425:    */   
/* 426:    */   public final void setReusePort(boolean reusePort)
/* 427:    */     throws IOException
/* 428:    */   {
/* 429:398 */     setReusePort(this.fd, reusePort ? 1 : 0);
/* 430:    */   }
/* 431:    */   
/* 432:    */   public final void setBroadcast(boolean broadcast)
/* 433:    */     throws IOException
/* 434:    */   {
/* 435:402 */     setBroadcast(this.fd, broadcast ? 1 : 0);
/* 436:    */   }
/* 437:    */   
/* 438:    */   public final void setTrafficClass(int trafficClass)
/* 439:    */     throws IOException
/* 440:    */   {
/* 441:406 */     setTrafficClass(this.fd, trafficClass);
/* 442:    */   }
/* 443:    */   
/* 444:    */   public String toString()
/* 445:    */   {
/* 446:411 */     return "Socket{fd=" + this.fd + '}';
/* 447:    */   }
/* 448:    */   
/* 449:416 */   private static final AtomicBoolean INITIALIZED = new AtomicBoolean();
/* 450:    */   
/* 451:    */   public static Socket newSocketStream()
/* 452:    */   {
/* 453:419 */     return new Socket(newSocketStream0());
/* 454:    */   }
/* 455:    */   
/* 456:    */   public static Socket newSocketDgram()
/* 457:    */   {
/* 458:423 */     return new Socket(newSocketDgram0());
/* 459:    */   }
/* 460:    */   
/* 461:    */   public static Socket newSocketDomain()
/* 462:    */   {
/* 463:427 */     return new Socket(newSocketDomain0());
/* 464:    */   }
/* 465:    */   
/* 466:    */   public static void initialize()
/* 467:    */   {
/* 468:431 */     if (INITIALIZED.compareAndSet(false, true)) {
/* 469:432 */       initialize(NetUtil.isIpV4StackPreferred());
/* 470:    */     }
/* 471:    */   }
/* 472:    */   
/* 473:    */   protected static int newSocketStream0()
/* 474:    */   {
/* 475:437 */     int res = newSocketStreamFd();
/* 476:438 */     if (res < 0) {
/* 477:439 */       throw new ChannelException(Errors.newIOException("newSocketStream", res));
/* 478:    */     }
/* 479:441 */     return res;
/* 480:    */   }
/* 481:    */   
/* 482:    */   protected static int newSocketDgram0()
/* 483:    */   {
/* 484:445 */     int res = newSocketDgramFd();
/* 485:446 */     if (res < 0) {
/* 486:447 */       throw new ChannelException(Errors.newIOException("newSocketDgram", res));
/* 487:    */     }
/* 488:449 */     return res;
/* 489:    */   }
/* 490:    */   
/* 491:    */   protected static int newSocketDomain0()
/* 492:    */   {
/* 493:453 */     int res = newSocketDomainFd();
/* 494:454 */     if (res < 0) {
/* 495:455 */       throw new ChannelException(Errors.newIOException("newSocketDomain", res));
/* 496:    */     }
/* 497:457 */     return res;
/* 498:    */   }
/* 499:    */   
/* 500:    */   private static native int shutdown(int paramInt, boolean paramBoolean1, boolean paramBoolean2);
/* 501:    */   
/* 502:    */   private static native int connect(int paramInt1, byte[] paramArrayOfByte, int paramInt2, int paramInt3);
/* 503:    */   
/* 504:    */   private static native int connectDomainSocket(int paramInt, byte[] paramArrayOfByte);
/* 505:    */   
/* 506:    */   private static native int finishConnect(int paramInt);
/* 507:    */   
/* 508:    */   private static native int disconnect(int paramInt);
/* 509:    */   
/* 510:    */   private static native int bind(int paramInt1, byte[] paramArrayOfByte, int paramInt2, int paramInt3);
/* 511:    */   
/* 512:    */   private static native int bindDomainSocket(int paramInt, byte[] paramArrayOfByte);
/* 513:    */   
/* 514:    */   private static native int listen(int paramInt1, int paramInt2);
/* 515:    */   
/* 516:    */   private static native int accept(int paramInt, byte[] paramArrayOfByte);
/* 517:    */   
/* 518:    */   private static native byte[] remoteAddress(int paramInt);
/* 519:    */   
/* 520:    */   private static native byte[] localAddress(int paramInt);
/* 521:    */   
/* 522:    */   private static native int sendTo(int paramInt1, ByteBuffer paramByteBuffer, int paramInt2, int paramInt3, byte[] paramArrayOfByte, int paramInt4, int paramInt5);
/* 523:    */   
/* 524:    */   private static native int sendToAddress(int paramInt1, long paramLong, int paramInt2, int paramInt3, byte[] paramArrayOfByte, int paramInt4, int paramInt5);
/* 525:    */   
/* 526:    */   private static native int sendToAddresses(int paramInt1, long paramLong, int paramInt2, byte[] paramArrayOfByte, int paramInt3, int paramInt4);
/* 527:    */   
/* 528:    */   private static native DatagramSocketAddress recvFrom(int paramInt1, ByteBuffer paramByteBuffer, int paramInt2, int paramInt3)
/* 529:    */     throws IOException;
/* 530:    */   
/* 531:    */   private static native DatagramSocketAddress recvFromAddress(int paramInt1, long paramLong, int paramInt2, int paramInt3)
/* 532:    */     throws IOException;
/* 533:    */   
/* 534:    */   private static native int recvFd(int paramInt);
/* 535:    */   
/* 536:    */   private static native int sendFd(int paramInt1, int paramInt2);
/* 537:    */   
/* 538:    */   private static native int newSocketStreamFd();
/* 539:    */   
/* 540:    */   private static native int newSocketDgramFd();
/* 541:    */   
/* 542:    */   private static native int newSocketDomainFd();
/* 543:    */   
/* 544:    */   private static native int isReuseAddress(int paramInt)
/* 545:    */     throws IOException;
/* 546:    */   
/* 547:    */   private static native int isReusePort(int paramInt)
/* 548:    */     throws IOException;
/* 549:    */   
/* 550:    */   private static native int getReceiveBufferSize(int paramInt)
/* 551:    */     throws IOException;
/* 552:    */   
/* 553:    */   private static native int getSendBufferSize(int paramInt)
/* 554:    */     throws IOException;
/* 555:    */   
/* 556:    */   private static native int isKeepAlive(int paramInt)
/* 557:    */     throws IOException;
/* 558:    */   
/* 559:    */   private static native int isTcpNoDelay(int paramInt)
/* 560:    */     throws IOException;
/* 561:    */   
/* 562:    */   private static native int isBroadcast(int paramInt)
/* 563:    */     throws IOException;
/* 564:    */   
/* 565:    */   private static native int getSoLinger(int paramInt)
/* 566:    */     throws IOException;
/* 567:    */   
/* 568:    */   private static native int getSoError(int paramInt)
/* 569:    */     throws IOException;
/* 570:    */   
/* 571:    */   private static native int getTrafficClass(int paramInt)
/* 572:    */     throws IOException;
/* 573:    */   
/* 574:    */   private static native void setReuseAddress(int paramInt1, int paramInt2)
/* 575:    */     throws IOException;
/* 576:    */   
/* 577:    */   private static native void setReusePort(int paramInt1, int paramInt2)
/* 578:    */     throws IOException;
/* 579:    */   
/* 580:    */   private static native void setKeepAlive(int paramInt1, int paramInt2)
/* 581:    */     throws IOException;
/* 582:    */   
/* 583:    */   private static native void setReceiveBufferSize(int paramInt1, int paramInt2)
/* 584:    */     throws IOException;
/* 585:    */   
/* 586:    */   private static native void setSendBufferSize(int paramInt1, int paramInt2)
/* 587:    */     throws IOException;
/* 588:    */   
/* 589:    */   private static native void setTcpNoDelay(int paramInt1, int paramInt2)
/* 590:    */     throws IOException;
/* 591:    */   
/* 592:    */   private static native void setSoLinger(int paramInt1, int paramInt2)
/* 593:    */     throws IOException;
/* 594:    */   
/* 595:    */   private static native void setBroadcast(int paramInt1, int paramInt2)
/* 596:    */     throws IOException;
/* 597:    */   
/* 598:    */   private static native void setTrafficClass(int paramInt1, int paramInt2)
/* 599:    */     throws IOException;
/* 600:    */   
/* 601:    */   private static native void initialize(boolean paramBoolean);
/* 602:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.unix.Socket
 * JD-Core Version:    0.7.0.1
 */