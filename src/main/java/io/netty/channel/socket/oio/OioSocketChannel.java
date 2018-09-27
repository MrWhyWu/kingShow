/*   1:    */ package io.netty.channel.socket.oio;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.Channel;
/*   5:    */ import io.netty.channel.ChannelException;
/*   6:    */ import io.netty.channel.ChannelFuture;
/*   7:    */ import io.netty.channel.ChannelFutureListener;
/*   8:    */ import io.netty.channel.ChannelPromise;
/*   9:    */ import io.netty.channel.ConnectTimeoutException;
/*  10:    */ import io.netty.channel.EventLoop;
/*  11:    */ import io.netty.channel.oio.OioByteStreamChannel;
/*  12:    */ import io.netty.channel.socket.ServerSocketChannel;
/*  13:    */ import io.netty.channel.socket.SocketChannel;
/*  14:    */ import io.netty.util.internal.SocketUtils;
/*  15:    */ import io.netty.util.internal.logging.InternalLogger;
/*  16:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  17:    */ import java.io.IOException;
/*  18:    */ import java.net.InetSocketAddress;
/*  19:    */ import java.net.Socket;
/*  20:    */ import java.net.SocketAddress;
/*  21:    */ import java.net.SocketTimeoutException;
/*  22:    */ 
/*  23:    */ public class OioSocketChannel
/*  24:    */   extends OioByteStreamChannel
/*  25:    */   implements SocketChannel
/*  26:    */ {
/*  27: 45 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(OioSocketChannel.class);
/*  28:    */   private final Socket socket;
/*  29:    */   private final OioSocketChannelConfig config;
/*  30:    */   
/*  31:    */   public OioSocketChannel()
/*  32:    */   {
/*  33: 54 */     this(new Socket());
/*  34:    */   }
/*  35:    */   
/*  36:    */   public OioSocketChannel(Socket socket)
/*  37:    */   {
/*  38: 63 */     this(null, socket);
/*  39:    */   }
/*  40:    */   
/*  41:    */   public OioSocketChannel(Channel parent, Socket socket)
/*  42:    */   {
/*  43: 74 */     super(parent);
/*  44: 75 */     this.socket = socket;
/*  45: 76 */     this.config = new DefaultOioSocketChannelConfig(this, socket);
/*  46:    */     
/*  47: 78 */     boolean success = false;
/*  48:    */     try
/*  49:    */     {
/*  50: 80 */       if (socket.isConnected()) {
/*  51: 81 */         activate(socket.getInputStream(), socket.getOutputStream());
/*  52:    */       }
/*  53: 83 */       socket.setSoTimeout(1000);
/*  54: 84 */       success = true; return;
/*  55:    */     }
/*  56:    */     catch (Exception e)
/*  57:    */     {
/*  58: 86 */       throw new ChannelException("failed to initialize a socket", e);
/*  59:    */     }
/*  60:    */     finally
/*  61:    */     {
/*  62: 88 */       if (!success) {
/*  63:    */         try
/*  64:    */         {
/*  65: 90 */           socket.close();
/*  66:    */         }
/*  67:    */         catch (IOException e)
/*  68:    */         {
/*  69: 92 */           logger.warn("Failed to close a socket.", e);
/*  70:    */         }
/*  71:    */       }
/*  72:    */     }
/*  73:    */   }
/*  74:    */   
/*  75:    */   public ServerSocketChannel parent()
/*  76:    */   {
/*  77:100 */     return (ServerSocketChannel)super.parent();
/*  78:    */   }
/*  79:    */   
/*  80:    */   public OioSocketChannelConfig config()
/*  81:    */   {
/*  82:105 */     return this.config;
/*  83:    */   }
/*  84:    */   
/*  85:    */   public boolean isOpen()
/*  86:    */   {
/*  87:110 */     return !this.socket.isClosed();
/*  88:    */   }
/*  89:    */   
/*  90:    */   public boolean isActive()
/*  91:    */   {
/*  92:115 */     return (!this.socket.isClosed()) && (this.socket.isConnected());
/*  93:    */   }
/*  94:    */   
/*  95:    */   public boolean isOutputShutdown()
/*  96:    */   {
/*  97:120 */     return (this.socket.isOutputShutdown()) || (!isActive());
/*  98:    */   }
/*  99:    */   
/* 100:    */   public boolean isInputShutdown()
/* 101:    */   {
/* 102:125 */     return (this.socket.isInputShutdown()) || (!isActive());
/* 103:    */   }
/* 104:    */   
/* 105:    */   public boolean isShutdown()
/* 106:    */   {
/* 107:130 */     return ((this.socket.isInputShutdown()) && (this.socket.isOutputShutdown())) || (!isActive());
/* 108:    */   }
/* 109:    */   
/* 110:    */   protected final void doShutdownOutput()
/* 111:    */     throws Exception
/* 112:    */   {
/* 113:136 */     shutdownOutput0();
/* 114:    */   }
/* 115:    */   
/* 116:    */   public ChannelFuture shutdownOutput()
/* 117:    */   {
/* 118:141 */     return shutdownOutput(newPromise());
/* 119:    */   }
/* 120:    */   
/* 121:    */   public ChannelFuture shutdownInput()
/* 122:    */   {
/* 123:146 */     return shutdownInput(newPromise());
/* 124:    */   }
/* 125:    */   
/* 126:    */   public ChannelFuture shutdown()
/* 127:    */   {
/* 128:151 */     return shutdown(newPromise());
/* 129:    */   }
/* 130:    */   
/* 131:    */   protected int doReadBytes(ByteBuf buf)
/* 132:    */     throws Exception
/* 133:    */   {
/* 134:156 */     if (this.socket.isClosed()) {
/* 135:157 */       return -1;
/* 136:    */     }
/* 137:    */     try
/* 138:    */     {
/* 139:160 */       return super.doReadBytes(buf);
/* 140:    */     }
/* 141:    */     catch (SocketTimeoutException ignored) {}
/* 142:162 */     return 0;
/* 143:    */   }
/* 144:    */   
/* 145:    */   public ChannelFuture shutdownOutput(final ChannelPromise promise)
/* 146:    */   {
/* 147:168 */     EventLoop loop = eventLoop();
/* 148:169 */     if (loop.inEventLoop()) {
/* 149:170 */       shutdownOutput0(promise);
/* 150:    */     } else {
/* 151:172 */       loop.execute(new Runnable()
/* 152:    */       {
/* 153:    */         public void run()
/* 154:    */         {
/* 155:175 */           OioSocketChannel.this.shutdownOutput0(promise);
/* 156:    */         }
/* 157:    */       });
/* 158:    */     }
/* 159:179 */     return promise;
/* 160:    */   }
/* 161:    */   
/* 162:    */   private void shutdownOutput0(ChannelPromise promise)
/* 163:    */   {
/* 164:    */     try
/* 165:    */     {
/* 166:184 */       shutdownOutput0();
/* 167:185 */       promise.setSuccess();
/* 168:    */     }
/* 169:    */     catch (Throwable t)
/* 170:    */     {
/* 171:187 */       promise.setFailure(t);
/* 172:    */     }
/* 173:    */   }
/* 174:    */   
/* 175:    */   private void shutdownOutput0()
/* 176:    */     throws IOException
/* 177:    */   {
/* 178:192 */     this.socket.shutdownOutput();
/* 179:    */   }
/* 180:    */   
/* 181:    */   public ChannelFuture shutdownInput(final ChannelPromise promise)
/* 182:    */   {
/* 183:197 */     EventLoop loop = eventLoop();
/* 184:198 */     if (loop.inEventLoop()) {
/* 185:199 */       shutdownInput0(promise);
/* 186:    */     } else {
/* 187:201 */       loop.execute(new Runnable()
/* 188:    */       {
/* 189:    */         public void run()
/* 190:    */         {
/* 191:204 */           OioSocketChannel.this.shutdownInput0(promise);
/* 192:    */         }
/* 193:    */       });
/* 194:    */     }
/* 195:208 */     return promise;
/* 196:    */   }
/* 197:    */   
/* 198:    */   private void shutdownInput0(ChannelPromise promise)
/* 199:    */   {
/* 200:    */     try
/* 201:    */     {
/* 202:213 */       this.socket.shutdownInput();
/* 203:214 */       promise.setSuccess();
/* 204:    */     }
/* 205:    */     catch (Throwable t)
/* 206:    */     {
/* 207:216 */       promise.setFailure(t);
/* 208:    */     }
/* 209:    */   }
/* 210:    */   
/* 211:    */   public ChannelFuture shutdown(final ChannelPromise promise)
/* 212:    */   {
/* 213:222 */     ChannelFuture shutdownOutputFuture = shutdownOutput();
/* 214:223 */     if (shutdownOutputFuture.isDone()) {
/* 215:224 */       shutdownOutputDone(shutdownOutputFuture, promise);
/* 216:    */     } else {
/* 217:226 */       shutdownOutputFuture.addListener(new ChannelFutureListener()
/* 218:    */       {
/* 219:    */         public void operationComplete(ChannelFuture shutdownOutputFuture)
/* 220:    */           throws Exception
/* 221:    */         {
/* 222:229 */           OioSocketChannel.this.shutdownOutputDone(shutdownOutputFuture, promise);
/* 223:    */         }
/* 224:    */       });
/* 225:    */     }
/* 226:233 */     return promise;
/* 227:    */   }
/* 228:    */   
/* 229:    */   private void shutdownOutputDone(final ChannelFuture shutdownOutputFuture, final ChannelPromise promise)
/* 230:    */   {
/* 231:237 */     ChannelFuture shutdownInputFuture = shutdownInput();
/* 232:238 */     if (shutdownInputFuture.isDone()) {
/* 233:239 */       shutdownDone(shutdownOutputFuture, shutdownInputFuture, promise);
/* 234:    */     } else {
/* 235:241 */       shutdownInputFuture.addListener(new ChannelFutureListener()
/* 236:    */       {
/* 237:    */         public void operationComplete(ChannelFuture shutdownInputFuture)
/* 238:    */           throws Exception
/* 239:    */         {
/* 240:244 */           OioSocketChannel.shutdownDone(shutdownOutputFuture, shutdownInputFuture, promise);
/* 241:    */         }
/* 242:    */       });
/* 243:    */     }
/* 244:    */   }
/* 245:    */   
/* 246:    */   private static void shutdownDone(ChannelFuture shutdownOutputFuture, ChannelFuture shutdownInputFuture, ChannelPromise promise)
/* 247:    */   {
/* 248:253 */     Throwable shutdownOutputCause = shutdownOutputFuture.cause();
/* 249:254 */     Throwable shutdownInputCause = shutdownInputFuture.cause();
/* 250:255 */     if (shutdownOutputCause != null)
/* 251:    */     {
/* 252:256 */       if (shutdownInputCause != null) {
/* 253:257 */         logger.debug("Exception suppressed because a previous exception occurred.", shutdownInputCause);
/* 254:    */       }
/* 255:260 */       promise.setFailure(shutdownOutputCause);
/* 256:    */     }
/* 257:261 */     else if (shutdownInputCause != null)
/* 258:    */     {
/* 259:262 */       promise.setFailure(shutdownInputCause);
/* 260:    */     }
/* 261:    */     else
/* 262:    */     {
/* 263:264 */       promise.setSuccess();
/* 264:    */     }
/* 265:    */   }
/* 266:    */   
/* 267:    */   public InetSocketAddress localAddress()
/* 268:    */   {
/* 269:270 */     return (InetSocketAddress)super.localAddress();
/* 270:    */   }
/* 271:    */   
/* 272:    */   public InetSocketAddress remoteAddress()
/* 273:    */   {
/* 274:275 */     return (InetSocketAddress)super.remoteAddress();
/* 275:    */   }
/* 276:    */   
/* 277:    */   protected SocketAddress localAddress0()
/* 278:    */   {
/* 279:280 */     return this.socket.getLocalSocketAddress();
/* 280:    */   }
/* 281:    */   
/* 282:    */   protected SocketAddress remoteAddress0()
/* 283:    */   {
/* 284:285 */     return this.socket.getRemoteSocketAddress();
/* 285:    */   }
/* 286:    */   
/* 287:    */   protected void doBind(SocketAddress localAddress)
/* 288:    */     throws Exception
/* 289:    */   {
/* 290:290 */     SocketUtils.bind(this.socket, localAddress);
/* 291:    */   }
/* 292:    */   
/* 293:    */   protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress)
/* 294:    */     throws Exception
/* 295:    */   {
/* 296:296 */     if (localAddress != null) {
/* 297:297 */       SocketUtils.bind(this.socket, localAddress);
/* 298:    */     }
/* 299:300 */     boolean success = false;
/* 300:    */     try
/* 301:    */     {
/* 302:302 */       SocketUtils.connect(this.socket, remoteAddress, config().getConnectTimeoutMillis());
/* 303:303 */       activate(this.socket.getInputStream(), this.socket.getOutputStream());
/* 304:304 */       success = true;
/* 305:    */     }
/* 306:    */     catch (SocketTimeoutException e)
/* 307:    */     {
/* 308:306 */       ConnectTimeoutException cause = new ConnectTimeoutException("connection timed out: " + remoteAddress);
/* 309:307 */       cause.setStackTrace(e.getStackTrace());
/* 310:308 */       throw cause;
/* 311:    */     }
/* 312:    */     finally
/* 313:    */     {
/* 314:310 */       if (!success) {
/* 315:311 */         doClose();
/* 316:    */       }
/* 317:    */     }
/* 318:    */   }
/* 319:    */   
/* 320:    */   protected void doDisconnect()
/* 321:    */     throws Exception
/* 322:    */   {
/* 323:318 */     doClose();
/* 324:    */   }
/* 325:    */   
/* 326:    */   protected void doClose()
/* 327:    */     throws Exception
/* 328:    */   {
/* 329:323 */     this.socket.close();
/* 330:    */   }
/* 331:    */   
/* 332:    */   protected boolean checkInputShutdown()
/* 333:    */   {
/* 334:327 */     if (isInputShutdown())
/* 335:    */     {
/* 336:    */       try
/* 337:    */       {
/* 338:329 */         Thread.sleep(config().getSoTimeout());
/* 339:    */       }
/* 340:    */       catch (Throwable localThrowable) {}
/* 341:333 */       return true;
/* 342:    */     }
/* 343:335 */     return false;
/* 344:    */   }
/* 345:    */   
/* 346:    */   @Deprecated
/* 347:    */   protected void setReadPending(boolean readPending)
/* 348:    */   {
/* 349:341 */     super.setReadPending(readPending);
/* 350:    */   }
/* 351:    */   
/* 352:    */   final void clearReadPending0()
/* 353:    */   {
/* 354:345 */     clearReadPending();
/* 355:    */   }
/* 356:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.socket.oio.OioSocketChannel
 * JD-Core Version:    0.7.0.1
 */