/*  1:   */ package io.netty.handler.codec;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.channel.ChannelHandlerContext;
/*  5:   */ import java.util.List;
/*  6:   */ 
/*  7:   */ public class FixedLengthFrameDecoder
/*  8:   */   extends ByteToMessageDecoder
/*  9:   */ {
/* 10:   */   private final int frameLength;
/* 11:   */   
/* 12:   */   public FixedLengthFrameDecoder(int frameLength)
/* 13:   */   {
/* 14:49 */     if (frameLength <= 0) {
/* 15:50 */       throw new IllegalArgumentException("frameLength must be a positive integer: " + frameLength);
/* 16:   */     }
/* 17:53 */     this.frameLength = frameLength;
/* 18:   */   }
/* 19:   */   
/* 20:   */   protected final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/* 21:   */     throws Exception
/* 22:   */   {
/* 23:58 */     Object decoded = decode(ctx, in);
/* 24:59 */     if (decoded != null) {
/* 25:60 */       out.add(decoded);
/* 26:   */     }
/* 27:   */   }
/* 28:   */   
/* 29:   */   protected Object decode(ChannelHandlerContext ctx, ByteBuf in)
/* 30:   */     throws Exception
/* 31:   */   {
/* 32:74 */     if (in.readableBytes() < this.frameLength) {
/* 33:75 */       return null;
/* 34:   */     }
/* 35:77 */     return in.readRetainedSlice(this.frameLength);
/* 36:   */   }
/* 37:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.FixedLengthFrameDecoder
 * JD-Core Version:    0.7.0.1
 */