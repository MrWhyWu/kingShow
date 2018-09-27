/*   1:    */ package io.netty.handler.codec.compression;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.channel.ChannelFuture;
/*   6:    */ import io.netty.channel.ChannelFutureListener;
/*   7:    */ import io.netty.channel.ChannelHandlerContext;
/*   8:    */ import io.netty.channel.ChannelPromise;
/*   9:    */ import io.netty.channel.ChannelPromiseNotifier;
/*  10:    */ import io.netty.handler.codec.MessageToByteEncoder;
/*  11:    */ import io.netty.util.concurrent.EventExecutor;
/*  12:    */ import java.util.concurrent.TimeUnit;
/*  13:    */ 
/*  14:    */ public class Bzip2Encoder
/*  15:    */   extends MessageToByteEncoder<ByteBuf>
/*  16:    */ {
/*  17:    */   private static enum State
/*  18:    */   {
/*  19: 42 */     INIT,  INIT_BLOCK,  WRITE_DATA,  CLOSE_BLOCK;
/*  20:    */     
/*  21:    */     private State() {}
/*  22:    */   }
/*  23:    */   
/*  24: 48 */   private State currentState = State.INIT;
/*  25: 53 */   private final Bzip2BitWriter writer = new Bzip2BitWriter();
/*  26:    */   private final int streamBlockSize;
/*  27:    */   private int streamCRC;
/*  28:    */   private Bzip2BlockCompressor blockCompressor;
/*  29:    */   private volatile boolean finished;
/*  30:    */   private volatile ChannelHandlerContext ctx;
/*  31:    */   
/*  32:    */   public Bzip2Encoder()
/*  33:    */   {
/*  34: 84 */     this(9);
/*  35:    */   }
/*  36:    */   
/*  37:    */   public Bzip2Encoder(int blockSizeMultiplier)
/*  38:    */   {
/*  39: 95 */     if ((blockSizeMultiplier < 1) || (blockSizeMultiplier > 9)) {
/*  40: 96 */       throw new IllegalArgumentException("blockSizeMultiplier: " + blockSizeMultiplier + " (expected: 1-9)");
/*  41:    */     }
/*  42: 99 */     this.streamBlockSize = (blockSizeMultiplier * 100000);
/*  43:    */   }
/*  44:    */   
/*  45:    */   protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out)
/*  46:    */     throws Exception
/*  47:    */   {
/*  48:104 */     if (this.finished)
/*  49:    */     {
/*  50:105 */       out.writeBytes(in);
/*  51:    */     }
/*  52:    */     else
/*  53:    */     {
/*  54:    */       for (;;)
/*  55:    */       {
/*  56:110 */         switch (4.$SwitchMap$io$netty$handler$codec$compression$Bzip2Encoder$State[this.currentState.ordinal()])
/*  57:    */         {
/*  58:    */         case 1: 
/*  59:112 */           out.ensureWritable(4);
/*  60:113 */           out.writeMedium(4348520);
/*  61:114 */           out.writeByte(48 + this.streamBlockSize / 100000);
/*  62:115 */           this.currentState = State.INIT_BLOCK;
/*  63:    */         case 2: 
/*  64:118 */           this.blockCompressor = new Bzip2BlockCompressor(this.writer, this.streamBlockSize);
/*  65:119 */           this.currentState = State.WRITE_DATA;
/*  66:    */         case 3: 
/*  67:122 */           if (!in.isReadable()) {
/*  68:123 */             return;
/*  69:    */           }
/*  70:125 */           Bzip2BlockCompressor blockCompressor = this.blockCompressor;
/*  71:126 */           int length = Math.min(in.readableBytes(), blockCompressor.availableSize());
/*  72:127 */           int bytesWritten = blockCompressor.write(in, in.readerIndex(), length);
/*  73:128 */           in.skipBytes(bytesWritten);
/*  74:129 */           if (!blockCompressor.isFull())
/*  75:    */           {
/*  76:130 */             if (in.isReadable()) {}
/*  77:    */           }
/*  78:    */           else {
/*  79:136 */             this.currentState = State.CLOSE_BLOCK;
/*  80:    */           }
/*  81:    */           break;
/*  82:    */         case 4: 
/*  83:139 */           closeBlock(out);
/*  84:140 */           this.currentState = State.INIT_BLOCK;
/*  85:    */         }
/*  86:    */       }
/*  87:143 */       throw new IllegalStateException();
/*  88:    */     }
/*  89:    */   }
/*  90:    */   
/*  91:    */   private void closeBlock(ByteBuf out)
/*  92:    */   {
/*  93:152 */     Bzip2BlockCompressor blockCompressor = this.blockCompressor;
/*  94:153 */     if (!blockCompressor.isEmpty())
/*  95:    */     {
/*  96:154 */       blockCompressor.close(out);
/*  97:155 */       int blockCRC = blockCompressor.crc();
/*  98:156 */       this.streamCRC = ((this.streamCRC << 1 | this.streamCRC >>> 31) ^ blockCRC);
/*  99:    */     }
/* 100:    */   }
/* 101:    */   
/* 102:    */   public boolean isClosed()
/* 103:    */   {
/* 104:164 */     return this.finished;
/* 105:    */   }
/* 106:    */   
/* 107:    */   public ChannelFuture close()
/* 108:    */   {
/* 109:173 */     return close(ctx().newPromise());
/* 110:    */   }
/* 111:    */   
/* 112:    */   public ChannelFuture close(final ChannelPromise promise)
/* 113:    */   {
/* 114:182 */     ChannelHandlerContext ctx = ctx();
/* 115:183 */     EventExecutor executor = ctx.executor();
/* 116:184 */     if (executor.inEventLoop()) {
/* 117:185 */       return finishEncode(ctx, promise);
/* 118:    */     }
/* 119:187 */     executor.execute(new Runnable()
/* 120:    */     {
/* 121:    */       public void run()
/* 122:    */       {
/* 123:190 */         ChannelFuture f = Bzip2Encoder.this.finishEncode(Bzip2Encoder.access$000(Bzip2Encoder.this), promise);
/* 124:191 */         f.addListener(new ChannelPromiseNotifier(new ChannelPromise[] { promise }));
/* 125:    */       }
/* 126:193 */     });
/* 127:194 */     return promise;
/* 128:    */   }
/* 129:    */   
/* 130:    */   public void close(final ChannelHandlerContext ctx, final ChannelPromise promise)
/* 131:    */     throws Exception
/* 132:    */   {
/* 133:200 */     ChannelFuture f = finishEncode(ctx, ctx.newPromise());
/* 134:201 */     f.addListener(new ChannelFutureListener()
/* 135:    */     {
/* 136:    */       public void operationComplete(ChannelFuture f)
/* 137:    */         throws Exception
/* 138:    */       {
/* 139:204 */         ctx.close(promise);
/* 140:    */       }
/* 141:    */     });
/* 142:208 */     if (!f.isDone()) {
/* 143:210 */       ctx.executor().schedule(new Runnable()
/* 144:    */       {
/* 145:    */         public void run()
/* 146:    */         {
/* 147:213 */           ctx.close(promise);
/* 148:    */         }
/* 149:213 */       }, 10L, TimeUnit.SECONDS);
/* 150:    */     }
/* 151:    */   }
/* 152:    */   
/* 153:    */   private ChannelFuture finishEncode(ChannelHandlerContext ctx, ChannelPromise promise)
/* 154:    */   {
/* 155:220 */     if (this.finished)
/* 156:    */     {
/* 157:221 */       promise.setSuccess();
/* 158:222 */       return promise;
/* 159:    */     }
/* 160:224 */     this.finished = true;
/* 161:    */     
/* 162:226 */     ByteBuf footer = ctx.alloc().buffer();
/* 163:227 */     closeBlock(footer);
/* 164:    */     
/* 165:229 */     int streamCRC = this.streamCRC;
/* 166:230 */     Bzip2BitWriter writer = this.writer;
/* 167:    */     try
/* 168:    */     {
/* 169:232 */       writer.writeBits(footer, 24, 1536581L);
/* 170:233 */       writer.writeBits(footer, 24, 3690640L);
/* 171:234 */       writer.writeInt(footer, streamCRC);
/* 172:235 */       writer.flush(footer);
/* 173:    */     }
/* 174:    */     finally
/* 175:    */     {
/* 176:237 */       this.blockCompressor = null;
/* 177:    */     }
/* 178:239 */     return ctx.writeAndFlush(footer, promise);
/* 179:    */   }
/* 180:    */   
/* 181:    */   private ChannelHandlerContext ctx()
/* 182:    */   {
/* 183:243 */     ChannelHandlerContext ctx = this.ctx;
/* 184:244 */     if (ctx == null) {
/* 185:245 */       throw new IllegalStateException("not added to a pipeline");
/* 186:    */     }
/* 187:247 */     return ctx;
/* 188:    */   }
/* 189:    */   
/* 190:    */   public void handlerAdded(ChannelHandlerContext ctx)
/* 191:    */     throws Exception
/* 192:    */   {
/* 193:252 */     this.ctx = ctx;
/* 194:    */   }
/* 195:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.compression.Bzip2Encoder
 * JD-Core Version:    0.7.0.1
 */