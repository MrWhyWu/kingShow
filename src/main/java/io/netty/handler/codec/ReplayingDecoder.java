/*   1:    */ package io.netty.handler.codec;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.Unpooled;
/*   5:    */ import io.netty.channel.ChannelHandlerContext;
/*   6:    */ import io.netty.util.Signal;
/*   7:    */ import io.netty.util.internal.StringUtil;
/*   8:    */ import java.util.List;
/*   9:    */ 
/*  10:    */ public abstract class ReplayingDecoder<S>
/*  11:    */   extends ByteToMessageDecoder
/*  12:    */ {
/*  13:270 */   static final Signal REPLAY = Signal.valueOf(ReplayingDecoder.class, "REPLAY");
/*  14:272 */   private final ReplayingDecoderByteBuf replayable = new ReplayingDecoderByteBuf();
/*  15:    */   private S state;
/*  16:274 */   private int checkpoint = -1;
/*  17:    */   
/*  18:    */   protected ReplayingDecoder()
/*  19:    */   {
/*  20:280 */     this(null);
/*  21:    */   }
/*  22:    */   
/*  23:    */   protected ReplayingDecoder(S initialState)
/*  24:    */   {
/*  25:287 */     this.state = initialState;
/*  26:    */   }
/*  27:    */   
/*  28:    */   protected void checkpoint()
/*  29:    */   {
/*  30:294 */     this.checkpoint = internalBuffer().readerIndex();
/*  31:    */   }
/*  32:    */   
/*  33:    */   protected void checkpoint(S state)
/*  34:    */   {
/*  35:302 */     checkpoint();
/*  36:303 */     state(state);
/*  37:    */   }
/*  38:    */   
/*  39:    */   protected S state()
/*  40:    */   {
/*  41:311 */     return this.state;
/*  42:    */   }
/*  43:    */   
/*  44:    */   protected S state(S newState)
/*  45:    */   {
/*  46:319 */     S oldState = this.state;
/*  47:320 */     this.state = newState;
/*  48:321 */     return oldState;
/*  49:    */   }
/*  50:    */   
/*  51:    */   final void channelInputClosed(ChannelHandlerContext ctx, List<Object> out)
/*  52:    */     throws Exception
/*  53:    */   {
/*  54:    */     try
/*  55:    */     {
/*  56:327 */       this.replayable.terminate();
/*  57:328 */       if (this.cumulation != null)
/*  58:    */       {
/*  59:329 */         callDecode(ctx, internalBuffer(), out);
/*  60:330 */         decodeLast(ctx, this.replayable, out);
/*  61:    */       }
/*  62:    */       else
/*  63:    */       {
/*  64:332 */         this.replayable.setCumulation(Unpooled.EMPTY_BUFFER);
/*  65:333 */         decodeLast(ctx, this.replayable, out);
/*  66:    */       }
/*  67:    */     }
/*  68:    */     catch (Signal replay)
/*  69:    */     {
/*  70:337 */       replay.expect(REPLAY);
/*  71:    */     }
/*  72:    */   }
/*  73:    */   
/*  74:    */   protected void callDecode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/*  75:    */   {
/*  76:343 */     this.replayable.setCumulation(in);
/*  77:    */     try
/*  78:    */     {
/*  79:345 */       while (in.isReadable())
/*  80:    */       {
/*  81:346 */         int oldReaderIndex = this.checkpoint = in.readerIndex();
/*  82:347 */         int outSize = out.size();
/*  83:349 */         if (outSize > 0)
/*  84:    */         {
/*  85:350 */           fireChannelRead(ctx, out, outSize);
/*  86:351 */           out.clear();
/*  87:358 */           if (!ctx.isRemoved()) {
/*  88:361 */             outSize = 0;
/*  89:    */           }
/*  90:    */         }
/*  91:    */         else
/*  92:    */         {
/*  93:364 */           S oldState = this.state;
/*  94:365 */           int oldInputLength = in.readableBytes();
/*  95:    */           try
/*  96:    */           {
/*  97:367 */             decodeRemovalReentryProtection(ctx, this.replayable, out);
/*  98:373 */             if (ctx.isRemoved()) {
/*  99:    */               break;
/* 100:    */             }
/* 101:377 */             if (outSize == out.size())
/* 102:    */             {
/* 103:378 */               if ((oldInputLength == in.readableBytes()) && (oldState == this.state)) {
/* 104:380 */                 throw new DecoderException(StringUtil.simpleClassName(getClass()) + ".decode() must consume the inbound data or change its state if it did not decode anything.");
/* 105:    */               }
/* 106:385 */               continue;
/* 107:    */             }
/* 108:    */           }
/* 109:    */           catch (Signal replay)
/* 110:    */           {
/* 111:389 */             replay.expect(REPLAY);
/* 112:395 */             if (!ctx.isRemoved()) {
/* 113:    */               break label191;
/* 114:    */             }
/* 115:    */           }
/* 116:396 */           break;
/* 117:    */           label191:
/* 118:400 */           int checkpoint = this.checkpoint;
/* 119:401 */           if (checkpoint >= 0) {
/* 120:402 */             in.readerIndex(checkpoint);
/* 121:    */           }
/* 122:407 */           break;
/* 123:410 */           if ((oldReaderIndex == in.readerIndex()) && (oldState == this.state)) {
/* 124:412 */             throw new DecoderException(StringUtil.simpleClassName(getClass()) + ".decode() method must consume the inbound data or change its state if it decoded something.");
/* 125:    */           }
/* 126:415 */           if (isSingleDecode()) {
/* 127:    */             break;
/* 128:    */           }
/* 129:    */         }
/* 130:    */       }
/* 131:    */     }
/* 132:    */     catch (DecoderException e)
/* 133:    */     {
/* 134:420 */       throw e;
/* 135:    */     }
/* 136:    */     catch (Exception cause)
/* 137:    */     {
/* 138:422 */       throw new DecoderException(cause);
/* 139:    */     }
/* 140:    */   }
/* 141:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.ReplayingDecoder
 * JD-Core Version:    0.7.0.1
 */