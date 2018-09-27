/*   1:    */ package io.netty.handler.codec;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.ChannelHandlerContext;
/*   5:    */ import java.nio.ByteOrder;
/*   6:    */ import java.util.List;
/*   7:    */ 
/*   8:    */ public class LengthFieldBasedFrameDecoder
/*   9:    */   extends ByteToMessageDecoder
/*  10:    */ {
/*  11:    */   private final ByteOrder byteOrder;
/*  12:    */   private final int maxFrameLength;
/*  13:    */   private final int lengthFieldOffset;
/*  14:    */   private final int lengthFieldLength;
/*  15:    */   private final int lengthFieldEndOffset;
/*  16:    */   private final int lengthAdjustment;
/*  17:    */   private final int initialBytesToStrip;
/*  18:    */   private final boolean failFast;
/*  19:    */   private boolean discardingTooLongFrame;
/*  20:    */   private long tooLongFrameLength;
/*  21:    */   private long bytesToDiscard;
/*  22:    */   
/*  23:    */   public LengthFieldBasedFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength)
/*  24:    */   {
/*  25:213 */     this(maxFrameLength, lengthFieldOffset, lengthFieldLength, 0, 0);
/*  26:    */   }
/*  27:    */   
/*  28:    */   public LengthFieldBasedFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip)
/*  29:    */   {
/*  30:236 */     this(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, true);
/*  31:    */   }
/*  32:    */   
/*  33:    */   public LengthFieldBasedFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip, boolean failFast)
/*  34:    */   {
/*  35:268 */     this(ByteOrder.BIG_ENDIAN, maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, failFast);
/*  36:    */   }
/*  37:    */   
/*  38:    */   public LengthFieldBasedFrameDecoder(ByteOrder byteOrder, int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip, boolean failFast)
/*  39:    */   {
/*  40:301 */     if (byteOrder == null) {
/*  41:302 */       throw new NullPointerException("byteOrder");
/*  42:    */     }
/*  43:305 */     if (maxFrameLength <= 0) {
/*  44:306 */       throw new IllegalArgumentException("maxFrameLength must be a positive integer: " + maxFrameLength);
/*  45:    */     }
/*  46:311 */     if (lengthFieldOffset < 0) {
/*  47:312 */       throw new IllegalArgumentException("lengthFieldOffset must be a non-negative integer: " + lengthFieldOffset);
/*  48:    */     }
/*  49:317 */     if (initialBytesToStrip < 0) {
/*  50:318 */       throw new IllegalArgumentException("initialBytesToStrip must be a non-negative integer: " + initialBytesToStrip);
/*  51:    */     }
/*  52:323 */     if (lengthFieldOffset > maxFrameLength - lengthFieldLength) {
/*  53:324 */       throw new IllegalArgumentException("maxFrameLength (" + maxFrameLength + ") must be equal to or greater than lengthFieldOffset (" + lengthFieldOffset + ") + lengthFieldLength (" + lengthFieldLength + ").");
/*  54:    */     }
/*  55:331 */     this.byteOrder = byteOrder;
/*  56:332 */     this.maxFrameLength = maxFrameLength;
/*  57:333 */     this.lengthFieldOffset = lengthFieldOffset;
/*  58:334 */     this.lengthFieldLength = lengthFieldLength;
/*  59:335 */     this.lengthAdjustment = lengthAdjustment;
/*  60:336 */     this.lengthFieldEndOffset = (lengthFieldOffset + lengthFieldLength);
/*  61:337 */     this.initialBytesToStrip = initialBytesToStrip;
/*  62:338 */     this.failFast = failFast;
/*  63:    */   }
/*  64:    */   
/*  65:    */   protected final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/*  66:    */     throws Exception
/*  67:    */   {
/*  68:343 */     Object decoded = decode(ctx, in);
/*  69:344 */     if (decoded != null) {
/*  70:345 */       out.add(decoded);
/*  71:    */     }
/*  72:    */   }
/*  73:    */   
/*  74:    */   private void discardingTooLongFrame(ByteBuf in)
/*  75:    */   {
/*  76:350 */     long bytesToDiscard = this.bytesToDiscard;
/*  77:351 */     int localBytesToDiscard = (int)Math.min(bytesToDiscard, in.readableBytes());
/*  78:352 */     in.skipBytes(localBytesToDiscard);
/*  79:353 */     bytesToDiscard -= localBytesToDiscard;
/*  80:354 */     this.bytesToDiscard = bytesToDiscard;
/*  81:    */     
/*  82:356 */     failIfNecessary(false);
/*  83:    */   }
/*  84:    */   
/*  85:    */   private static void failOnNegativeLengthField(ByteBuf in, long frameLength, int lengthFieldEndOffset)
/*  86:    */   {
/*  87:360 */     in.skipBytes(lengthFieldEndOffset);
/*  88:361 */     throw new CorruptedFrameException("negative pre-adjustment length field: " + frameLength);
/*  89:    */   }
/*  90:    */   
/*  91:    */   private static void failOnFrameLengthLessThanLengthFieldEndOffset(ByteBuf in, long frameLength, int lengthFieldEndOffset)
/*  92:    */   {
/*  93:368 */     in.skipBytes(lengthFieldEndOffset);
/*  94:369 */     throw new CorruptedFrameException("Adjusted frame length (" + frameLength + ") is less than lengthFieldEndOffset: " + lengthFieldEndOffset);
/*  95:    */   }
/*  96:    */   
/*  97:    */   private void exceededFrameLength(ByteBuf in, long frameLength)
/*  98:    */   {
/*  99:375 */     long discard = frameLength - in.readableBytes();
/* 100:376 */     this.tooLongFrameLength = frameLength;
/* 101:378 */     if (discard < 0L)
/* 102:    */     {
/* 103:380 */       in.skipBytes((int)frameLength);
/* 104:    */     }
/* 105:    */     else
/* 106:    */     {
/* 107:383 */       this.discardingTooLongFrame = true;
/* 108:384 */       this.bytesToDiscard = discard;
/* 109:385 */       in.skipBytes(in.readableBytes());
/* 110:    */     }
/* 111:387 */     failIfNecessary(true);
/* 112:    */   }
/* 113:    */   
/* 114:    */   private static void failOnFrameLengthLessThanInitialBytesToStrip(ByteBuf in, long frameLength, int initialBytesToStrip)
/* 115:    */   {
/* 116:393 */     in.skipBytes((int)frameLength);
/* 117:394 */     throw new CorruptedFrameException("Adjusted frame length (" + frameLength + ") is less than initialBytesToStrip: " + initialBytesToStrip);
/* 118:    */   }
/* 119:    */   
/* 120:    */   protected Object decode(ChannelHandlerContext ctx, ByteBuf in)
/* 121:    */     throws Exception
/* 122:    */   {
/* 123:408 */     if (this.discardingTooLongFrame) {
/* 124:409 */       discardingTooLongFrame(in);
/* 125:    */     }
/* 126:412 */     if (in.readableBytes() < this.lengthFieldEndOffset) {
/* 127:413 */       return null;
/* 128:    */     }
/* 129:416 */     int actualLengthFieldOffset = in.readerIndex() + this.lengthFieldOffset;
/* 130:417 */     long frameLength = getUnadjustedFrameLength(in, actualLengthFieldOffset, this.lengthFieldLength, this.byteOrder);
/* 131:419 */     if (frameLength < 0L) {
/* 132:420 */       failOnNegativeLengthField(in, frameLength, this.lengthFieldEndOffset);
/* 133:    */     }
/* 134:423 */     frameLength += this.lengthAdjustment + this.lengthFieldEndOffset;
/* 135:425 */     if (frameLength < this.lengthFieldEndOffset) {
/* 136:426 */       failOnFrameLengthLessThanLengthFieldEndOffset(in, frameLength, this.lengthFieldEndOffset);
/* 137:    */     }
/* 138:429 */     if (frameLength > this.maxFrameLength)
/* 139:    */     {
/* 140:430 */       exceededFrameLength(in, frameLength);
/* 141:431 */       return null;
/* 142:    */     }
/* 143:435 */     int frameLengthInt = (int)frameLength;
/* 144:436 */     if (in.readableBytes() < frameLengthInt) {
/* 145:437 */       return null;
/* 146:    */     }
/* 147:440 */     if (this.initialBytesToStrip > frameLengthInt) {
/* 148:441 */       failOnFrameLengthLessThanInitialBytesToStrip(in, frameLength, this.initialBytesToStrip);
/* 149:    */     }
/* 150:443 */     in.skipBytes(this.initialBytesToStrip);
/* 151:    */     
/* 152:    */ 
/* 153:446 */     int readerIndex = in.readerIndex();
/* 154:447 */     int actualFrameLength = frameLengthInt - this.initialBytesToStrip;
/* 155:448 */     ByteBuf frame = extractFrame(ctx, in, readerIndex, actualFrameLength);
/* 156:449 */     in.readerIndex(readerIndex + actualFrameLength);
/* 157:450 */     return frame;
/* 158:    */   }
/* 159:    */   
/* 160:    */   protected long getUnadjustedFrameLength(ByteBuf buf, int offset, int length, ByteOrder order)
/* 161:    */   {
/* 162:462 */     buf = buf.order(order);
/* 163:    */     long frameLength;
/* 164:    */     long frameLength;
/* 165:    */     long frameLength;
/* 166:    */     long frameLength;
/* 167:    */     long frameLength;
/* 168:464 */     switch (length)
/* 169:    */     {
/* 170:    */     case 1: 
/* 171:466 */       frameLength = buf.getUnsignedByte(offset);
/* 172:467 */       break;
/* 173:    */     case 2: 
/* 174:469 */       frameLength = buf.getUnsignedShort(offset);
/* 175:470 */       break;
/* 176:    */     case 3: 
/* 177:472 */       frameLength = buf.getUnsignedMedium(offset);
/* 178:473 */       break;
/* 179:    */     case 4: 
/* 180:475 */       frameLength = buf.getUnsignedInt(offset);
/* 181:476 */       break;
/* 182:    */     case 8: 
/* 183:478 */       frameLength = buf.getLong(offset);
/* 184:479 */       break;
/* 185:    */     case 5: 
/* 186:    */     case 6: 
/* 187:    */     case 7: 
/* 188:    */     default: 
/* 189:481 */       throw new DecoderException("unsupported lengthFieldLength: " + this.lengthFieldLength + " (expected: 1, 2, 3, 4, or 8)");
/* 190:    */     }
/* 191:    */     long frameLength;
/* 192:484 */     return frameLength;
/* 193:    */   }
/* 194:    */   
/* 195:    */   private void failIfNecessary(boolean firstDetectionOfTooLongFrame)
/* 196:    */   {
/* 197:488 */     if (this.bytesToDiscard == 0L)
/* 198:    */     {
/* 199:491 */       long tooLongFrameLength = this.tooLongFrameLength;
/* 200:492 */       this.tooLongFrameLength = 0L;
/* 201:493 */       this.discardingTooLongFrame = false;
/* 202:494 */       if ((!this.failFast) || (firstDetectionOfTooLongFrame)) {
/* 203:495 */         fail(tooLongFrameLength);
/* 204:    */       }
/* 205:    */     }
/* 206:499 */     else if ((this.failFast) && (firstDetectionOfTooLongFrame))
/* 207:    */     {
/* 208:500 */       fail(this.tooLongFrameLength);
/* 209:    */     }
/* 210:    */   }
/* 211:    */   
/* 212:    */   protected ByteBuf extractFrame(ChannelHandlerContext ctx, ByteBuf buffer, int index, int length)
/* 213:    */   {
/* 214:517 */     return buffer.retainedSlice(index, length);
/* 215:    */   }
/* 216:    */   
/* 217:    */   private void fail(long frameLength)
/* 218:    */   {
/* 219:521 */     if (frameLength > 0L) {
/* 220:522 */       throw new TooLongFrameException("Adjusted frame length exceeds " + this.maxFrameLength + ": " + frameLength + " - discarded");
/* 221:    */     }
/* 222:526 */     throw new TooLongFrameException("Adjusted frame length exceeds " + this.maxFrameLength + " - discarding");
/* 223:    */   }
/* 224:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.LengthFieldBasedFrameDecoder
 * JD-Core Version:    0.7.0.1
 */