/*   1:    */ package io.netty.handler.codec;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.buffer.ByteBufHolder;
/*   6:    */ import io.netty.buffer.CompositeByteBuf;
/*   7:    */ import io.netty.buffer.Unpooled;
/*   8:    */ import io.netty.channel.Channel;
/*   9:    */ import io.netty.channel.ChannelConfig;
/*  10:    */ import io.netty.channel.ChannelFuture;
/*  11:    */ import io.netty.channel.ChannelFutureListener;
/*  12:    */ import io.netty.channel.ChannelHandlerContext;
/*  13:    */ import io.netty.channel.ChannelPipeline;
/*  14:    */ import io.netty.util.ReferenceCountUtil;
/*  15:    */ import java.util.List;
/*  16:    */ 
/*  17:    */ public abstract class MessageAggregator<I, S, C extends ByteBufHolder, O extends ByteBufHolder>
/*  18:    */   extends MessageToMessageDecoder<I>
/*  19:    */ {
/*  20:    */   private static final int DEFAULT_MAX_COMPOSITEBUFFER_COMPONENTS = 1024;
/*  21:    */   private final int maxContentLength;
/*  22:    */   private O currentMessage;
/*  23:    */   private boolean handlingOversizedMessage;
/*  24: 60 */   private int maxCumulationBufferComponents = 1024;
/*  25:    */   private ChannelHandlerContext ctx;
/*  26:    */   private ChannelFutureListener continueResponseWriteListener;
/*  27:    */   
/*  28:    */   protected MessageAggregator(int maxContentLength)
/*  29:    */   {
/*  30: 73 */     validateMaxContentLength(maxContentLength);
/*  31: 74 */     this.maxContentLength = maxContentLength;
/*  32:    */   }
/*  33:    */   
/*  34:    */   protected MessageAggregator(int maxContentLength, Class<? extends I> inboundMessageType)
/*  35:    */   {
/*  36: 78 */     super(inboundMessageType);
/*  37: 79 */     validateMaxContentLength(maxContentLength);
/*  38: 80 */     this.maxContentLength = maxContentLength;
/*  39:    */   }
/*  40:    */   
/*  41:    */   private static void validateMaxContentLength(int maxContentLength)
/*  42:    */   {
/*  43: 84 */     if (maxContentLength < 0) {
/*  44: 85 */       throw new IllegalArgumentException("maxContentLength: " + maxContentLength + " (expected: >= 0)");
/*  45:    */     }
/*  46:    */   }
/*  47:    */   
/*  48:    */   public boolean acceptInboundMessage(Object msg)
/*  49:    */     throws Exception
/*  50:    */   {
/*  51: 92 */     if (!super.acceptInboundMessage(msg)) {
/*  52: 93 */       return false;
/*  53:    */     }
/*  54: 97 */     I in = msg;
/*  55:    */     
/*  56: 99 */     return ((isContentMessage(in)) || (isStartMessage(in))) && (!isAggregated(in));
/*  57:    */   }
/*  58:    */   
/*  59:    */   protected abstract boolean isStartMessage(I paramI)
/*  60:    */     throws Exception;
/*  61:    */   
/*  62:    */   protected abstract boolean isContentMessage(I paramI)
/*  63:    */     throws Exception;
/*  64:    */   
/*  65:    */   protected abstract boolean isLastContentMessage(C paramC)
/*  66:    */     throws Exception;
/*  67:    */   
/*  68:    */   protected abstract boolean isAggregated(I paramI)
/*  69:    */     throws Exception;
/*  70:    */   
/*  71:    */   public final int maxContentLength()
/*  72:    */   {
/*  73:143 */     return this.maxContentLength;
/*  74:    */   }
/*  75:    */   
/*  76:    */   public final int maxCumulationBufferComponents()
/*  77:    */   {
/*  78:153 */     return this.maxCumulationBufferComponents;
/*  79:    */   }
/*  80:    */   
/*  81:    */   public final void setMaxCumulationBufferComponents(int maxCumulationBufferComponents)
/*  82:    */   {
/*  83:164 */     if (maxCumulationBufferComponents < 2) {
/*  84:165 */       throw new IllegalArgumentException("maxCumulationBufferComponents: " + maxCumulationBufferComponents + " (expected: >= 2)");
/*  85:    */     }
/*  86:170 */     if (this.ctx == null) {
/*  87:171 */       this.maxCumulationBufferComponents = maxCumulationBufferComponents;
/*  88:    */     } else {
/*  89:173 */       throw new IllegalStateException("decoder properties cannot be changed once the decoder is added to a pipeline.");
/*  90:    */     }
/*  91:    */   }
/*  92:    */   
/*  93:    */   @Deprecated
/*  94:    */   public final boolean isHandlingOversizedMessage()
/*  95:    */   {
/*  96:183 */     return this.handlingOversizedMessage;
/*  97:    */   }
/*  98:    */   
/*  99:    */   protected final ChannelHandlerContext ctx()
/* 100:    */   {
/* 101:187 */     if (this.ctx == null) {
/* 102:188 */       throw new IllegalStateException("not added to a pipeline yet");
/* 103:    */     }
/* 104:190 */     return this.ctx;
/* 105:    */   }
/* 106:    */   
/* 107:    */   protected void decode(final ChannelHandlerContext ctx, I msg, List<Object> out)
/* 108:    */     throws Exception
/* 109:    */   {
/* 110:195 */     if (isStartMessage(msg))
/* 111:    */     {
/* 112:196 */       this.handlingOversizedMessage = false;
/* 113:197 */       if (this.currentMessage != null)
/* 114:    */       {
/* 115:198 */         this.currentMessage.release();
/* 116:199 */         this.currentMessage = null;
/* 117:200 */         throw new MessageAggregationException();
/* 118:    */       }
/* 119:204 */       S m = msg;
/* 120:    */       
/* 121:    */ 
/* 122:    */ 
/* 123:208 */       Object continueResponse = newContinueResponse(m, this.maxContentLength, ctx.pipeline());
/* 124:209 */       if (continueResponse != null)
/* 125:    */       {
/* 126:211 */         ChannelFutureListener listener = this.continueResponseWriteListener;
/* 127:212 */         if (listener == null) {
/* 128:213 */           this.continueResponseWriteListener = (listener = new ChannelFutureListener()
/* 129:    */           {
/* 130:    */             public void operationComplete(ChannelFuture future)
/* 131:    */               throws Exception
/* 132:    */             {
/* 133:216 */               if (!future.isSuccess()) {
/* 134:217 */                 ctx.fireExceptionCaught(future.cause());
/* 135:    */               }
/* 136:    */             }
/* 137:    */           });
/* 138:    */         }
/* 139:224 */         boolean closeAfterWrite = closeAfterContinueResponse(continueResponse);
/* 140:225 */         this.handlingOversizedMessage = ignoreContentAfterContinueResponse(continueResponse);
/* 141:    */         
/* 142:227 */         ChannelFuture future = ctx.writeAndFlush(continueResponse).addListener(listener);
/* 143:229 */         if (closeAfterWrite)
/* 144:    */         {
/* 145:230 */           future.addListener(ChannelFutureListener.CLOSE);
/* 146:231 */           return;
/* 147:    */         }
/* 148:233 */         if (this.handlingOversizedMessage) {
/* 149:234 */           return;
/* 150:    */         }
/* 151:    */       }
/* 152:236 */       else if (isContentLengthInvalid(m, this.maxContentLength))
/* 153:    */       {
/* 154:238 */         invokeHandleOversizedMessage(ctx, m);
/* 155:239 */         return;
/* 156:    */       }
/* 157:242 */       if (((m instanceof DecoderResultProvider)) && (!((DecoderResultProvider)m).decoderResult().isSuccess()))
/* 158:    */       {
/* 159:    */         O aggregated;
/* 160:    */         O aggregated;
/* 161:244 */         if ((m instanceof ByteBufHolder)) {
/* 162:245 */           aggregated = beginAggregation(m, ((ByteBufHolder)m).content().retain());
/* 163:    */         } else {
/* 164:247 */           aggregated = beginAggregation(m, Unpooled.EMPTY_BUFFER);
/* 165:    */         }
/* 166:249 */         finishAggregation(aggregated);
/* 167:250 */         out.add(aggregated);
/* 168:251 */         return;
/* 169:    */       }
/* 170:255 */       CompositeByteBuf content = ctx.alloc().compositeBuffer(this.maxCumulationBufferComponents);
/* 171:256 */       if ((m instanceof ByteBufHolder)) {
/* 172:257 */         appendPartialContent(content, ((ByteBufHolder)m).content());
/* 173:    */       }
/* 174:259 */       this.currentMessage = beginAggregation(m, content);
/* 175:    */     }
/* 176:260 */     else if (isContentMessage(msg))
/* 177:    */     {
/* 178:261 */       if (this.currentMessage == null) {
/* 179:264 */         return;
/* 180:    */       }
/* 181:268 */       CompositeByteBuf content = (CompositeByteBuf)this.currentMessage.content();
/* 182:    */       
/* 183:    */ 
/* 184:271 */       C m = (ByteBufHolder)msg;
/* 185:273 */       if (content.readableBytes() > this.maxContentLength - m.content().readableBytes())
/* 186:    */       {
/* 187:276 */         S s = this.currentMessage;
/* 188:277 */         invokeHandleOversizedMessage(ctx, s);
/* 189:278 */         return;
/* 190:    */       }
/* 191:282 */       appendPartialContent(content, m.content());
/* 192:    */       
/* 193:    */ 
/* 194:285 */       aggregate(this.currentMessage, m);
/* 195:    */       boolean last;
/* 196:    */       boolean last;
/* 197:288 */       if ((m instanceof DecoderResultProvider))
/* 198:    */       {
/* 199:289 */         DecoderResult decoderResult = ((DecoderResultProvider)m).decoderResult();
/* 200:    */         boolean last;
/* 201:290 */         if (!decoderResult.isSuccess())
/* 202:    */         {
/* 203:291 */           if ((this.currentMessage instanceof DecoderResultProvider)) {
/* 204:292 */             ((DecoderResultProvider)this.currentMessage).setDecoderResult(
/* 205:293 */               DecoderResult.failure(decoderResult.cause()));
/* 206:    */           }
/* 207:295 */           last = true;
/* 208:    */         }
/* 209:    */         else
/* 210:    */         {
/* 211:297 */           last = isLastContentMessage(m);
/* 212:    */         }
/* 213:    */       }
/* 214:    */       else
/* 215:    */       {
/* 216:300 */         last = isLastContentMessage(m);
/* 217:    */       }
/* 218:303 */       if (last)
/* 219:    */       {
/* 220:304 */         finishAggregation(this.currentMessage);
/* 221:    */         
/* 222:    */ 
/* 223:307 */         out.add(this.currentMessage);
/* 224:308 */         this.currentMessage = null;
/* 225:    */       }
/* 226:    */     }
/* 227:    */     else
/* 228:    */     {
/* 229:311 */       throw new MessageAggregationException();
/* 230:    */     }
/* 231:    */   }
/* 232:    */   
/* 233:    */   private static void appendPartialContent(CompositeByteBuf content, ByteBuf partialContent)
/* 234:    */   {
/* 235:316 */     if (partialContent.isReadable()) {
/* 236:317 */       content.addComponent(true, partialContent.retain());
/* 237:    */     }
/* 238:    */   }
/* 239:    */   
/* 240:    */   protected abstract boolean isContentLengthInvalid(S paramS, int paramInt)
/* 241:    */     throws Exception;
/* 242:    */   
/* 243:    */   protected abstract Object newContinueResponse(S paramS, int paramInt, ChannelPipeline paramChannelPipeline)
/* 244:    */     throws Exception;
/* 245:    */   
/* 246:    */   protected abstract boolean closeAfterContinueResponse(Object paramObject)
/* 247:    */     throws Exception;
/* 248:    */   
/* 249:    */   protected abstract boolean ignoreContentAfterContinueResponse(Object paramObject)
/* 250:    */     throws Exception;
/* 251:    */   
/* 252:    */   protected abstract O beginAggregation(S paramS, ByteBuf paramByteBuf)
/* 253:    */     throws Exception;
/* 254:    */   
/* 255:    */   protected void aggregate(O aggregated, C content)
/* 256:    */     throws Exception
/* 257:    */   {}
/* 258:    */   
/* 259:    */   protected void finishAggregation(O aggregated)
/* 260:    */     throws Exception
/* 261:    */   {}
/* 262:    */   
/* 263:    */   private void invokeHandleOversizedMessage(ChannelHandlerContext ctx, S oversized)
/* 264:    */     throws Exception
/* 265:    */   {
/* 266:380 */     this.handlingOversizedMessage = true;
/* 267:381 */     this.currentMessage = null;
/* 268:    */     try
/* 269:    */     {
/* 270:383 */       handleOversizedMessage(ctx, oversized);
/* 271:    */       
/* 272:    */ 
/* 273:386 */       ReferenceCountUtil.release(oversized);
/* 274:    */     }
/* 275:    */     finally
/* 276:    */     {
/* 277:386 */       ReferenceCountUtil.release(oversized);
/* 278:    */     }
/* 279:    */   }
/* 280:    */   
/* 281:    */   protected void handleOversizedMessage(ChannelHandlerContext ctx, S oversized)
/* 282:    */     throws Exception
/* 283:    */   {
/* 284:398 */     ctx.fireExceptionCaught(new TooLongFrameException("content length exceeded " + 
/* 285:399 */       maxContentLength() + " bytes."));
/* 286:    */   }
/* 287:    */   
/* 288:    */   public void channelReadComplete(ChannelHandlerContext ctx)
/* 289:    */     throws Exception
/* 290:    */   {
/* 291:407 */     if ((this.currentMessage != null) && (!ctx.channel().config().isAutoRead())) {
/* 292:408 */       ctx.read();
/* 293:    */     }
/* 294:410 */     ctx.fireChannelReadComplete();
/* 295:    */   }
/* 296:    */   
/* 297:    */   public void channelInactive(ChannelHandlerContext ctx)
/* 298:    */     throws Exception
/* 299:    */   {
/* 300:    */     try
/* 301:    */     {
/* 302:417 */       super.channelInactive(ctx);
/* 303:    */       
/* 304:419 */       releaseCurrentMessage();
/* 305:    */     }
/* 306:    */     finally
/* 307:    */     {
/* 308:419 */       releaseCurrentMessage();
/* 309:    */     }
/* 310:    */   }
/* 311:    */   
/* 312:    */   public void handlerAdded(ChannelHandlerContext ctx)
/* 313:    */     throws Exception
/* 314:    */   {
/* 315:425 */     this.ctx = ctx;
/* 316:    */   }
/* 317:    */   
/* 318:    */   public void handlerRemoved(ChannelHandlerContext ctx)
/* 319:    */     throws Exception
/* 320:    */   {
/* 321:    */     try
/* 322:    */     {
/* 323:431 */       super.handlerRemoved(ctx);
/* 324:    */       
/* 325:    */ 
/* 326:    */ 
/* 327:435 */       releaseCurrentMessage();
/* 328:    */     }
/* 329:    */     finally
/* 330:    */     {
/* 331:435 */       releaseCurrentMessage();
/* 332:    */     }
/* 333:    */   }
/* 334:    */   
/* 335:    */   private void releaseCurrentMessage()
/* 336:    */   {
/* 337:440 */     if (this.currentMessage != null)
/* 338:    */     {
/* 339:441 */       this.currentMessage.release();
/* 340:442 */       this.currentMessage = null;
/* 341:443 */       this.handlingOversizedMessage = false;
/* 342:    */     }
/* 343:    */   }
/* 344:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.MessageAggregator
 * JD-Core Version:    0.7.0.1
 */