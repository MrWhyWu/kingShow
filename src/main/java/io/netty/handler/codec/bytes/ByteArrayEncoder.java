/*  1:   */ package io.netty.handler.codec.bytes;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.Unpooled;
/*  4:   */ import io.netty.channel.ChannelHandler.Sharable;
/*  5:   */ import io.netty.channel.ChannelHandlerContext;
/*  6:   */ import io.netty.handler.codec.MessageToMessageEncoder;
/*  7:   */ import java.util.List;
/*  8:   */ 
/*  9:   */ @ChannelHandler.Sharable
/* 10:   */ public class ByteArrayEncoder
/* 11:   */   extends MessageToMessageEncoder<byte[]>
/* 12:   */ {
/* 13:   */   protected void encode(ChannelHandlerContext ctx, byte[] msg, List<Object> out)
/* 14:   */     throws Exception
/* 15:   */   {
/* 16:57 */     out.add(Unpooled.wrappedBuffer(msg));
/* 17:   */   }
/* 18:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.bytes.ByteArrayEncoder
 * JD-Core Version:    0.7.0.1
 */