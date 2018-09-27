/*   1:    */ package io.netty.channel.socket.oio;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.channel.AddressedEnvelope;
/*   6:    */ import io.netty.channel.Channel.Unsafe;
/*   7:    */ import io.netty.channel.ChannelException;
/*   8:    */ import io.netty.channel.ChannelFuture;
/*   9:    */ import io.netty.channel.ChannelMetadata;
/*  10:    */ import io.netty.channel.ChannelOption;
/*  11:    */ import io.netty.channel.ChannelOutboundBuffer;
/*  12:    */ import io.netty.channel.ChannelPromise;
/*  13:    */ import io.netty.channel.RecvByteBufAllocator.Handle;
/*  14:    */ import io.netty.channel.oio.AbstractOioMessageChannel;
/*  15:    */ import io.netty.channel.socket.DatagramChannel;
/*  16:    */ import io.netty.channel.socket.DatagramChannelConfig;
/*  17:    */ import io.netty.util.internal.EmptyArrays;
/*  18:    */ import io.netty.util.internal.PlatformDependent;
/*  19:    */ import io.netty.util.internal.StringUtil;
/*  20:    */ import io.netty.util.internal.logging.InternalLogger;
/*  21:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  22:    */ import java.io.IOException;
/*  23:    */ import java.net.InetAddress;
/*  24:    */ import java.net.InetSocketAddress;
/*  25:    */ import java.net.MulticastSocket;
/*  26:    */ import java.net.NetworkInterface;
/*  27:    */ import java.net.SocketAddress;
/*  28:    */ import java.net.SocketException;
/*  29:    */ import java.net.SocketTimeoutException;
/*  30:    */ import java.nio.channels.NotYetConnectedException;
/*  31:    */ import java.util.List;
/*  32:    */ import java.util.Locale;
/*  33:    */ 
/*  34:    */ public class OioDatagramChannel
/*  35:    */   extends AbstractOioMessageChannel
/*  36:    */   implements DatagramChannel
/*  37:    */ {
/*  38: 60 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(OioDatagramChannel.class);
/*  39: 62 */   private static final ChannelMetadata METADATA = new ChannelMetadata(true);
/*  40: 63 */   private static final String EXPECTED_TYPES = " (expected: " + 
/*  41: 64 */     StringUtil.simpleClassName(io.netty.channel.socket.DatagramPacket.class) + ", " + 
/*  42: 65 */     StringUtil.simpleClassName(AddressedEnvelope.class) + '<' + 
/*  43: 66 */     StringUtil.simpleClassName(ByteBuf.class) + ", " + 
/*  44: 67 */     StringUtil.simpleClassName(SocketAddress.class) + ">, " + 
/*  45: 68 */     StringUtil.simpleClassName(ByteBuf.class) + ')';
/*  46:    */   private final MulticastSocket socket;
/*  47:    */   private final OioDatagramChannelConfig config;
/*  48: 72 */   private final java.net.DatagramPacket tmpPacket = new java.net.DatagramPacket(EmptyArrays.EMPTY_BYTES, 0);
/*  49:    */   
/*  50:    */   private static MulticastSocket newSocket()
/*  51:    */   {
/*  52:    */     try
/*  53:    */     {
/*  54: 76 */       return new MulticastSocket(null);
/*  55:    */     }
/*  56:    */     catch (Exception e)
/*  57:    */     {
/*  58: 78 */       throw new ChannelException("failed to create a new socket", e);
/*  59:    */     }
/*  60:    */   }
/*  61:    */   
/*  62:    */   public OioDatagramChannel()
/*  63:    */   {
/*  64: 86 */     this(newSocket());
/*  65:    */   }
/*  66:    */   
/*  67:    */   public OioDatagramChannel(MulticastSocket socket)
/*  68:    */   {
/*  69: 95 */     super(null);
/*  70:    */     
/*  71: 97 */     boolean success = false;
/*  72:    */     try
/*  73:    */     {
/*  74: 99 */       socket.setSoTimeout(1000);
/*  75:100 */       socket.setBroadcast(false);
/*  76:101 */       success = true;
/*  77:    */     }
/*  78:    */     catch (SocketException e)
/*  79:    */     {
/*  80:103 */       throw new ChannelException("Failed to configure the datagram socket timeout.", e);
/*  81:    */     }
/*  82:    */     finally
/*  83:    */     {
/*  84:106 */       if (!success) {
/*  85:107 */         socket.close();
/*  86:    */       }
/*  87:    */     }
/*  88:111 */     this.socket = socket;
/*  89:112 */     this.config = new DefaultOioDatagramChannelConfig(this, socket);
/*  90:    */   }
/*  91:    */   
/*  92:    */   public ChannelMetadata metadata()
/*  93:    */   {
/*  94:117 */     return METADATA;
/*  95:    */   }
/*  96:    */   
/*  97:    */   public DatagramChannelConfig config()
/*  98:    */   {
/*  99:128 */     return this.config;
/* 100:    */   }
/* 101:    */   
/* 102:    */   public boolean isOpen()
/* 103:    */   {
/* 104:133 */     return !this.socket.isClosed();
/* 105:    */   }
/* 106:    */   
/* 107:    */   public boolean isActive()
/* 108:    */   {
/* 109:139 */     return (isOpen()) && (
/* 110:140 */       ((((Boolean)this.config.getOption(ChannelOption.DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION)).booleanValue()) && (isRegistered())) || 
/* 111:141 */       (this.socket.isBound()));
/* 112:    */   }
/* 113:    */   
/* 114:    */   public boolean isConnected()
/* 115:    */   {
/* 116:146 */     return this.socket.isConnected();
/* 117:    */   }
/* 118:    */   
/* 119:    */   protected SocketAddress localAddress0()
/* 120:    */   {
/* 121:151 */     return this.socket.getLocalSocketAddress();
/* 122:    */   }
/* 123:    */   
/* 124:    */   protected SocketAddress remoteAddress0()
/* 125:    */   {
/* 126:156 */     return this.socket.getRemoteSocketAddress();
/* 127:    */   }
/* 128:    */   
/* 129:    */   protected void doBind(SocketAddress localAddress)
/* 130:    */     throws Exception
/* 131:    */   {
/* 132:161 */     this.socket.bind(localAddress);
/* 133:    */   }
/* 134:    */   
/* 135:    */   public InetSocketAddress localAddress()
/* 136:    */   {
/* 137:166 */     return (InetSocketAddress)super.localAddress();
/* 138:    */   }
/* 139:    */   
/* 140:    */   public InetSocketAddress remoteAddress()
/* 141:    */   {
/* 142:171 */     return (InetSocketAddress)super.remoteAddress();
/* 143:    */   }
/* 144:    */   
/* 145:    */   protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress)
/* 146:    */     throws Exception
/* 147:    */   {
/* 148:177 */     if (localAddress != null) {
/* 149:178 */       this.socket.bind(localAddress);
/* 150:    */     }
/* 151:181 */     boolean success = false;
/* 152:    */     try
/* 153:    */     {
/* 154:183 */       this.socket.connect(remoteAddress);
/* 155:184 */       success = true; return;
/* 156:    */     }
/* 157:    */     finally
/* 158:    */     {
/* 159:186 */       if (!success) {
/* 160:    */         try
/* 161:    */         {
/* 162:188 */           this.socket.close();
/* 163:    */         }
/* 164:    */         catch (Throwable t)
/* 165:    */         {
/* 166:190 */           logger.warn("Failed to close a socket.", t);
/* 167:    */         }
/* 168:    */       }
/* 169:    */     }
/* 170:    */   }
/* 171:    */   
/* 172:    */   protected void doDisconnect()
/* 173:    */     throws Exception
/* 174:    */   {
/* 175:198 */     this.socket.disconnect();
/* 176:    */   }
/* 177:    */   
/* 178:    */   protected void doClose()
/* 179:    */     throws Exception
/* 180:    */   {
/* 181:203 */     this.socket.close();
/* 182:    */   }
/* 183:    */   
/* 184:    */   protected int doReadMessages(List<Object> buf)
/* 185:    */     throws Exception
/* 186:    */   {
/* 187:208 */     DatagramChannelConfig config = config();
/* 188:209 */     RecvByteBufAllocator.Handle allocHandle = unsafe().recvBufAllocHandle();
/* 189:    */     
/* 190:211 */     ByteBuf data = config.getAllocator().heapBuffer(allocHandle.guess());
/* 191:212 */     boolean free = true;
/* 192:    */     try
/* 193:    */     {
/* 194:215 */       this.tmpPacket.setAddress(null);
/* 195:216 */       this.tmpPacket.setData(data.array(), data.arrayOffset(), data.capacity());
/* 196:217 */       this.socket.receive(this.tmpPacket);
/* 197:    */       
/* 198:219 */       InetSocketAddress remoteAddr = (InetSocketAddress)this.tmpPacket.getSocketAddress();
/* 199:    */       
/* 200:221 */       allocHandle.lastBytesRead(this.tmpPacket.getLength());
/* 201:222 */       buf.add(new io.netty.channel.socket.DatagramPacket(data.writerIndex(allocHandle.lastBytesRead()), localAddress(), remoteAddr));
/* 202:223 */       free = false;
/* 203:224 */       return 1;
/* 204:    */     }
/* 205:    */     catch (SocketTimeoutException e)
/* 206:    */     {
/* 207:227 */       return 0;
/* 208:    */     }
/* 209:    */     catch (SocketException e)
/* 210:    */     {
/* 211:229 */       if (!e.getMessage().toLowerCase(Locale.US).contains("socket closed")) {
/* 212:230 */         throw e;
/* 213:    */       }
/* 214:232 */       return -1;
/* 215:    */     }
/* 216:    */     catch (Throwable cause)
/* 217:    */     {
/* 218:    */       int i;
/* 219:234 */       PlatformDependent.throwException(cause);
/* 220:235 */       return -1;
/* 221:    */     }
/* 222:    */     finally
/* 223:    */     {
/* 224:237 */       if (free) {
/* 225:238 */         data.release();
/* 226:    */       }
/* 227:    */     }
/* 228:    */   }
/* 229:    */   
/* 230:    */   protected void doWrite(ChannelOutboundBuffer in)
/* 231:    */     throws Exception
/* 232:    */   {
/* 233:    */     for (;;)
/* 234:    */     {
/* 235:246 */       Object o = in.current();
/* 236:247 */       if (o == null) {
/* 237:    */         break;
/* 238:    */       }
/* 239:    */       ByteBuf data;
/* 240:    */       ByteBuf data;
/* 241:    */       SocketAddress remoteAddress;
/* 242:253 */       if ((o instanceof AddressedEnvelope))
/* 243:    */       {
/* 244:255 */         AddressedEnvelope<ByteBuf, SocketAddress> envelope = (AddressedEnvelope)o;
/* 245:256 */         SocketAddress remoteAddress = envelope.recipient();
/* 246:257 */         data = (ByteBuf)envelope.content();
/* 247:    */       }
/* 248:    */       else
/* 249:    */       {
/* 250:259 */         data = (ByteBuf)o;
/* 251:260 */         remoteAddress = null;
/* 252:    */       }
/* 253:263 */       int length = data.readableBytes();
/* 254:    */       try
/* 255:    */       {
/* 256:265 */         if (remoteAddress != null)
/* 257:    */         {
/* 258:266 */           this.tmpPacket.setSocketAddress(remoteAddress);
/* 259:    */         }
/* 260:    */         else
/* 261:    */         {
/* 262:268 */           if (!isConnected()) {
/* 263:271 */             throw new NotYetConnectedException();
/* 264:    */           }
/* 265:274 */           this.tmpPacket.setAddress(null);
/* 266:    */         }
/* 267:276 */         if (data.hasArray())
/* 268:    */         {
/* 269:277 */           this.tmpPacket.setData(data.array(), data.arrayOffset() + data.readerIndex(), length);
/* 270:    */         }
/* 271:    */         else
/* 272:    */         {
/* 273:279 */           byte[] tmp = new byte[length];
/* 274:280 */           data.getBytes(data.readerIndex(), tmp);
/* 275:281 */           this.tmpPacket.setData(tmp);
/* 276:    */         }
/* 277:283 */         this.socket.send(this.tmpPacket);
/* 278:284 */         in.remove();
/* 279:    */       }
/* 280:    */       catch (Exception e)
/* 281:    */       {
/* 282:289 */         in.remove(e);
/* 283:    */       }
/* 284:    */     }
/* 285:    */   }
/* 286:    */   
/* 287:    */   protected Object filterOutboundMessage(Object msg)
/* 288:    */   {
/* 289:296 */     if (((msg instanceof io.netty.channel.socket.DatagramPacket)) || ((msg instanceof ByteBuf))) {
/* 290:297 */       return msg;
/* 291:    */     }
/* 292:300 */     if ((msg instanceof AddressedEnvelope))
/* 293:    */     {
/* 294:302 */       AddressedEnvelope<Object, SocketAddress> e = (AddressedEnvelope)msg;
/* 295:303 */       if ((e.content() instanceof ByteBuf)) {
/* 296:304 */         return msg;
/* 297:    */       }
/* 298:    */     }
/* 299:309 */     throw new UnsupportedOperationException("unsupported message type: " + StringUtil.simpleClassName(msg) + EXPECTED_TYPES);
/* 300:    */   }
/* 301:    */   
/* 302:    */   public ChannelFuture joinGroup(InetAddress multicastAddress)
/* 303:    */   {
/* 304:314 */     return joinGroup(multicastAddress, newPromise());
/* 305:    */   }
/* 306:    */   
/* 307:    */   public ChannelFuture joinGroup(InetAddress multicastAddress, ChannelPromise promise)
/* 308:    */   {
/* 309:319 */     ensureBound();
/* 310:    */     try
/* 311:    */     {
/* 312:321 */       this.socket.joinGroup(multicastAddress);
/* 313:322 */       promise.setSuccess();
/* 314:    */     }
/* 315:    */     catch (IOException e)
/* 316:    */     {
/* 317:324 */       promise.setFailure(e);
/* 318:    */     }
/* 319:326 */     return promise;
/* 320:    */   }
/* 321:    */   
/* 322:    */   public ChannelFuture joinGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface)
/* 323:    */   {
/* 324:331 */     return joinGroup(multicastAddress, networkInterface, newPromise());
/* 325:    */   }
/* 326:    */   
/* 327:    */   public ChannelFuture joinGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface, ChannelPromise promise)
/* 328:    */   {
/* 329:338 */     ensureBound();
/* 330:    */     try
/* 331:    */     {
/* 332:340 */       this.socket.joinGroup(multicastAddress, networkInterface);
/* 333:341 */       promise.setSuccess();
/* 334:    */     }
/* 335:    */     catch (IOException e)
/* 336:    */     {
/* 337:343 */       promise.setFailure(e);
/* 338:    */     }
/* 339:345 */     return promise;
/* 340:    */   }
/* 341:    */   
/* 342:    */   public ChannelFuture joinGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source)
/* 343:    */   {
/* 344:351 */     return newFailedFuture(new UnsupportedOperationException());
/* 345:    */   }
/* 346:    */   
/* 347:    */   public ChannelFuture joinGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source, ChannelPromise promise)
/* 348:    */   {
/* 349:358 */     promise.setFailure(new UnsupportedOperationException());
/* 350:359 */     return promise;
/* 351:    */   }
/* 352:    */   
/* 353:    */   private void ensureBound()
/* 354:    */   {
/* 355:363 */     if (!isActive()) {
/* 356:365 */       throw new IllegalStateException(DatagramChannel.class.getName() + " must be bound to join a group.");
/* 357:    */     }
/* 358:    */   }
/* 359:    */   
/* 360:    */   public ChannelFuture leaveGroup(InetAddress multicastAddress)
/* 361:    */   {
/* 362:372 */     return leaveGroup(multicastAddress, newPromise());
/* 363:    */   }
/* 364:    */   
/* 365:    */   public ChannelFuture leaveGroup(InetAddress multicastAddress, ChannelPromise promise)
/* 366:    */   {
/* 367:    */     try
/* 368:    */     {
/* 369:378 */       this.socket.leaveGroup(multicastAddress);
/* 370:379 */       promise.setSuccess();
/* 371:    */     }
/* 372:    */     catch (IOException e)
/* 373:    */     {
/* 374:381 */       promise.setFailure(e);
/* 375:    */     }
/* 376:383 */     return promise;
/* 377:    */   }
/* 378:    */   
/* 379:    */   public ChannelFuture leaveGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface)
/* 380:    */   {
/* 381:389 */     return leaveGroup(multicastAddress, networkInterface, newPromise());
/* 382:    */   }
/* 383:    */   
/* 384:    */   public ChannelFuture leaveGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface, ChannelPromise promise)
/* 385:    */   {
/* 386:    */     try
/* 387:    */     {
/* 388:397 */       this.socket.leaveGroup(multicastAddress, networkInterface);
/* 389:398 */       promise.setSuccess();
/* 390:    */     }
/* 391:    */     catch (IOException e)
/* 392:    */     {
/* 393:400 */       promise.setFailure(e);
/* 394:    */     }
/* 395:402 */     return promise;
/* 396:    */   }
/* 397:    */   
/* 398:    */   public ChannelFuture leaveGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source)
/* 399:    */   {
/* 400:408 */     return newFailedFuture(new UnsupportedOperationException());
/* 401:    */   }
/* 402:    */   
/* 403:    */   public ChannelFuture leaveGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source, ChannelPromise promise)
/* 404:    */   {
/* 405:415 */     promise.setFailure(new UnsupportedOperationException());
/* 406:416 */     return promise;
/* 407:    */   }
/* 408:    */   
/* 409:    */   public ChannelFuture block(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress sourceToBlock)
/* 410:    */   {
/* 411:422 */     return newFailedFuture(new UnsupportedOperationException());
/* 412:    */   }
/* 413:    */   
/* 414:    */   public ChannelFuture block(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress sourceToBlock, ChannelPromise promise)
/* 415:    */   {
/* 416:429 */     promise.setFailure(new UnsupportedOperationException());
/* 417:430 */     return promise;
/* 418:    */   }
/* 419:    */   
/* 420:    */   public ChannelFuture block(InetAddress multicastAddress, InetAddress sourceToBlock)
/* 421:    */   {
/* 422:436 */     return newFailedFuture(new UnsupportedOperationException());
/* 423:    */   }
/* 424:    */   
/* 425:    */   public ChannelFuture block(InetAddress multicastAddress, InetAddress sourceToBlock, ChannelPromise promise)
/* 426:    */   {
/* 427:442 */     promise.setFailure(new UnsupportedOperationException());
/* 428:443 */     return promise;
/* 429:    */   }
/* 430:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.socket.oio.OioDatagramChannel
 * JD-Core Version:    0.7.0.1
 */