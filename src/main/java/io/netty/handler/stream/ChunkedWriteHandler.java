/*   1:    */ package io.netty.handler.stream;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.buffer.Unpooled;
/*   5:    */ import io.netty.channel.Channel;
/*   6:    */ import io.netty.channel.ChannelDuplexHandler;
/*   7:    */ import io.netty.channel.ChannelFuture;
/*   8:    */ import io.netty.channel.ChannelFutureListener;
/*   9:    */ import io.netty.channel.ChannelHandlerContext;
/*  10:    */ import io.netty.channel.ChannelProgressivePromise;
/*  11:    */ import io.netty.channel.ChannelPromise;
/*  12:    */ import io.netty.util.ReferenceCountUtil;
/*  13:    */ import io.netty.util.concurrent.EventExecutor;
/*  14:    */ import io.netty.util.internal.logging.InternalLogger;
/*  15:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  16:    */ import java.nio.channels.ClosedChannelException;
/*  17:    */ import java.util.ArrayDeque;
/*  18:    */ import java.util.Queue;
/*  19:    */ 
/*  20:    */ public class ChunkedWriteHandler
/*  21:    */   extends ChannelDuplexHandler
/*  22:    */ {
/*  23: 71 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(ChunkedWriteHandler.class);
/*  24: 73 */   private final Queue<PendingWrite> queue = new ArrayDeque();
/*  25:    */   private volatile ChannelHandlerContext ctx;
/*  26:    */   private PendingWrite currentWrite;
/*  27:    */   
/*  28:    */   public ChunkedWriteHandler() {}
/*  29:    */   
/*  30:    */   @Deprecated
/*  31:    */   public ChunkedWriteHandler(int maxPendingWrites)
/*  32:    */   {
/*  33: 85 */     if (maxPendingWrites <= 0) {
/*  34: 86 */       throw new IllegalArgumentException("maxPendingWrites: " + maxPendingWrites + " (expected: > 0)");
/*  35:    */     }
/*  36:    */   }
/*  37:    */   
/*  38:    */   public void handlerAdded(ChannelHandlerContext ctx)
/*  39:    */     throws Exception
/*  40:    */   {
/*  41: 93 */     this.ctx = ctx;
/*  42:    */   }
/*  43:    */   
/*  44:    */   public void resumeTransfer()
/*  45:    */   {
/*  46:100 */     final ChannelHandlerContext ctx = this.ctx;
/*  47:101 */     if (ctx == null) {
/*  48:102 */       return;
/*  49:    */     }
/*  50:104 */     if (ctx.executor().inEventLoop()) {
/*  51:    */       try
/*  52:    */       {
/*  53:106 */         doFlush(ctx);
/*  54:    */       }
/*  55:    */       catch (Exception e)
/*  56:    */       {
/*  57:108 */         if (logger.isWarnEnabled()) {
/*  58:109 */           logger.warn("Unexpected exception while sending chunks.", e);
/*  59:    */         }
/*  60:    */       }
/*  61:    */     } else {
/*  62:114 */       ctx.executor().execute(new Runnable()
/*  63:    */       {
/*  64:    */         public void run()
/*  65:    */         {
/*  66:    */           try
/*  67:    */           {
/*  68:119 */             ChunkedWriteHandler.this.doFlush(ctx);
/*  69:    */           }
/*  70:    */           catch (Exception e)
/*  71:    */           {
/*  72:121 */             if (ChunkedWriteHandler.logger.isWarnEnabled()) {
/*  73:122 */               ChunkedWriteHandler.logger.warn("Unexpected exception while sending chunks.", e);
/*  74:    */             }
/*  75:    */           }
/*  76:    */         }
/*  77:    */       });
/*  78:    */     }
/*  79:    */   }
/*  80:    */   
/*  81:    */   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
/*  82:    */     throws Exception
/*  83:    */   {
/*  84:132 */     this.queue.add(new PendingWrite(msg, promise));
/*  85:    */   }
/*  86:    */   
/*  87:    */   public void flush(ChannelHandlerContext ctx)
/*  88:    */     throws Exception
/*  89:    */   {
/*  90:137 */     doFlush(ctx);
/*  91:    */   }
/*  92:    */   
/*  93:    */   public void channelInactive(ChannelHandlerContext ctx)
/*  94:    */     throws Exception
/*  95:    */   {
/*  96:142 */     doFlush(ctx);
/*  97:143 */     ctx.fireChannelInactive();
/*  98:    */   }
/*  99:    */   
/* 100:    */   public void channelWritabilityChanged(ChannelHandlerContext ctx)
/* 101:    */     throws Exception
/* 102:    */   {
/* 103:148 */     if (ctx.channel().isWritable()) {
/* 104:150 */       doFlush(ctx);
/* 105:    */     }
/* 106:152 */     ctx.fireChannelWritabilityChanged();
/* 107:    */   }
/* 108:    */   
/* 109:    */   private void discard(Throwable cause)
/* 110:    */   {
/* 111:    */     for (;;)
/* 112:    */     {
/* 113:157 */       PendingWrite currentWrite = this.currentWrite;
/* 114:159 */       if (this.currentWrite == null) {
/* 115:160 */         currentWrite = (PendingWrite)this.queue.poll();
/* 116:    */       } else {
/* 117:162 */         this.currentWrite = null;
/* 118:    */       }
/* 119:165 */       if (currentWrite == null) {
/* 120:    */         break;
/* 121:    */       }
/* 122:168 */       Object message = currentWrite.msg;
/* 123:169 */       if ((message instanceof ChunkedInput))
/* 124:    */       {
/* 125:170 */         ChunkedInput<?> in = (ChunkedInput)message;
/* 126:    */         try
/* 127:    */         {
/* 128:172 */           if (!in.isEndOfInput())
/* 129:    */           {
/* 130:173 */             if (cause == null) {
/* 131:174 */               cause = new ClosedChannelException();
/* 132:    */             }
/* 133:176 */             currentWrite.fail(cause);
/* 134:    */           }
/* 135:    */           else
/* 136:    */           {
/* 137:178 */             currentWrite.success(in.length());
/* 138:    */           }
/* 139:180 */           closeInput(in);
/* 140:    */         }
/* 141:    */         catch (Exception e)
/* 142:    */         {
/* 143:182 */           currentWrite.fail(e);
/* 144:183 */           logger.warn(ChunkedInput.class.getSimpleName() + ".isEndOfInput() failed", e);
/* 145:184 */           closeInput(in);
/* 146:    */         }
/* 147:    */       }
/* 148:    */       else
/* 149:    */       {
/* 150:187 */         if (cause == null) {
/* 151:188 */           cause = new ClosedChannelException();
/* 152:    */         }
/* 153:190 */         currentWrite.fail(cause);
/* 154:    */       }
/* 155:    */     }
/* 156:    */   }
/* 157:    */   
/* 158:    */   private void doFlush(ChannelHandlerContext ctx)
/* 159:    */     throws Exception
/* 160:    */   {
/* 161:196 */     final Channel channel = ctx.channel();
/* 162:197 */     if (!channel.isActive())
/* 163:    */     {
/* 164:198 */       discard(null);
/* 165:199 */       return;
/* 166:    */     }
/* 167:202 */     boolean requiresFlush = true;
/* 168:203 */     ByteBufAllocator allocator = ctx.alloc();
/* 169:204 */     while (channel.isWritable())
/* 170:    */     {
/* 171:205 */       if (this.currentWrite == null) {
/* 172:206 */         this.currentWrite = ((PendingWrite)this.queue.poll());
/* 173:    */       }
/* 174:209 */       if (this.currentWrite == null) {
/* 175:    */         break;
/* 176:    */       }
/* 177:212 */       final PendingWrite currentWrite = this.currentWrite;
/* 178:213 */       final Object pendingMessage = currentWrite.msg;
/* 179:215 */       if ((pendingMessage instanceof ChunkedInput))
/* 180:    */       {
/* 181:216 */         final ChunkedInput<?> chunks = (ChunkedInput)pendingMessage;
/* 182:    */         
/* 183:    */ 
/* 184:219 */         Object message = null;
/* 185:    */         try
/* 186:    */         {
/* 187:221 */           message = chunks.readChunk(allocator);
/* 188:222 */           boolean endOfInput = chunks.isEndOfInput();
/* 189:    */           boolean suspend;
/* 190:224 */           if (message == null) {
/* 191:226 */             suspend = !endOfInput;
/* 192:    */           } else {
/* 193:228 */             suspend = false;
/* 194:    */           }
/* 195:    */         }
/* 196:    */         catch (Throwable t)
/* 197:    */         {
/* 198:    */           boolean suspend;
/* 199:231 */           this.currentWrite = null;
/* 200:233 */           if (message != null) {
/* 201:234 */             ReferenceCountUtil.release(message);
/* 202:    */           }
/* 203:237 */           currentWrite.fail(t);
/* 204:238 */           closeInput(chunks);
/* 205:239 */           break;
/* 206:    */         }
/* 207:    */         boolean suspend;
/* 208:    */         boolean endOfInput;
/* 209:242 */         if (suspend) {
/* 210:    */           break;
/* 211:    */         }
/* 212:249 */         if (message == null) {
/* 213:252 */           message = Unpooled.EMPTY_BUFFER;
/* 214:    */         }
/* 215:255 */         ChannelFuture f = ctx.write(message);
/* 216:256 */         if (endOfInput)
/* 217:    */         {
/* 218:257 */           this.currentWrite = null;
/* 219:    */           
/* 220:    */ 
/* 221:    */ 
/* 222:    */ 
/* 223:    */ 
/* 224:    */ 
/* 225:264 */           f.addListener(new ChannelFutureListener()
/* 226:    */           {
/* 227:    */             public void operationComplete(ChannelFuture future)
/* 228:    */               throws Exception
/* 229:    */             {
/* 230:267 */               currentWrite.progress(chunks.progress(), chunks.length());
/* 231:268 */               currentWrite.success(chunks.length());
/* 232:269 */               ChunkedWriteHandler.closeInput(chunks);
/* 233:    */             }
/* 234:    */           });
/* 235:    */         }
/* 236:272 */         else if (channel.isWritable())
/* 237:    */         {
/* 238:273 */           f.addListener(new ChannelFutureListener()
/* 239:    */           {
/* 240:    */             public void operationComplete(ChannelFuture future)
/* 241:    */               throws Exception
/* 242:    */             {
/* 243:276 */               if (!future.isSuccess())
/* 244:    */               {
/* 245:277 */                 ChunkedWriteHandler.closeInput((ChunkedInput)pendingMessage);
/* 246:278 */                 currentWrite.fail(future.cause());
/* 247:    */               }
/* 248:    */               else
/* 249:    */               {
/* 250:280 */                 currentWrite.progress(chunks.progress(), chunks.length());
/* 251:    */               }
/* 252:    */             }
/* 253:    */           });
/* 254:    */         }
/* 255:    */         else
/* 256:    */         {
/* 257:285 */           f.addListener(new ChannelFutureListener()
/* 258:    */           {
/* 259:    */             public void operationComplete(ChannelFuture future)
/* 260:    */               throws Exception
/* 261:    */             {
/* 262:288 */               if (!future.isSuccess())
/* 263:    */               {
/* 264:289 */                 ChunkedWriteHandler.closeInput((ChunkedInput)pendingMessage);
/* 265:290 */                 currentWrite.fail(future.cause());
/* 266:    */               }
/* 267:    */               else
/* 268:    */               {
/* 269:292 */                 currentWrite.progress(chunks.progress(), chunks.length());
/* 270:293 */                 if (channel.isWritable()) {
/* 271:294 */                   ChunkedWriteHandler.this.resumeTransfer();
/* 272:    */                 }
/* 273:    */               }
/* 274:    */             }
/* 275:    */           });
/* 276:    */         }
/* 277:301 */         ctx.flush();
/* 278:302 */         requiresFlush = false;
/* 279:    */       }
/* 280:    */       else
/* 281:    */       {
/* 282:304 */         ctx.write(pendingMessage, currentWrite.promise);
/* 283:305 */         this.currentWrite = null;
/* 284:306 */         requiresFlush = true;
/* 285:    */       }
/* 286:309 */       if (!channel.isActive())
/* 287:    */       {
/* 288:310 */         discard(new ClosedChannelException());
/* 289:311 */         break;
/* 290:    */       }
/* 291:    */     }
/* 292:315 */     if (requiresFlush) {
/* 293:316 */       ctx.flush();
/* 294:    */     }
/* 295:    */   }
/* 296:    */   
/* 297:    */   static void closeInput(ChunkedInput<?> chunks)
/* 298:    */   {
/* 299:    */     try
/* 300:    */     {
/* 301:322 */       chunks.close();
/* 302:    */     }
/* 303:    */     catch (Throwable t)
/* 304:    */     {
/* 305:324 */       if (logger.isWarnEnabled()) {
/* 306:325 */         logger.warn("Failed to close a chunked input.", t);
/* 307:    */       }
/* 308:    */     }
/* 309:    */   }
/* 310:    */   
/* 311:    */   private static final class PendingWrite
/* 312:    */   {
/* 313:    */     final Object msg;
/* 314:    */     final ChannelPromise promise;
/* 315:    */     
/* 316:    */     PendingWrite(Object msg, ChannelPromise promise)
/* 317:    */     {
/* 318:335 */       this.msg = msg;
/* 319:336 */       this.promise = promise;
/* 320:    */     }
/* 321:    */     
/* 322:    */     void fail(Throwable cause)
/* 323:    */     {
/* 324:340 */       ReferenceCountUtil.release(this.msg);
/* 325:341 */       this.promise.tryFailure(cause);
/* 326:    */     }
/* 327:    */     
/* 328:    */     void success(long total)
/* 329:    */     {
/* 330:345 */       if (this.promise.isDone()) {
/* 331:347 */         return;
/* 332:    */       }
/* 333:350 */       if ((this.promise instanceof ChannelProgressivePromise)) {
/* 334:352 */         ((ChannelProgressivePromise)this.promise).tryProgress(total, total);
/* 335:    */       }
/* 336:355 */       this.promise.trySuccess();
/* 337:    */     }
/* 338:    */     
/* 339:    */     void progress(long progress, long total)
/* 340:    */     {
/* 341:359 */       if ((this.promise instanceof ChannelProgressivePromise)) {
/* 342:360 */         ((ChannelProgressivePromise)this.promise).tryProgress(progress, total);
/* 343:    */       }
/* 344:    */     }
/* 345:    */   }
/* 346:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.stream.ChunkedWriteHandler
 * JD-Core Version:    0.7.0.1
 */