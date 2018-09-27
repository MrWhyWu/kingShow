/*  1:   */ package io.netty.handler.codec.bytes;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.channel.ChannelHandlerContext;
/*  5:   */ import io.netty.handler.codec.MessageToMessageDecoder;
/*  6:   */ import java.util.List;
/*  7:   */ 
/*  8:   */ public class ByteArrayDecoder
/*  9:   */   extends MessageToMessageDecoder<ByteBuf>
/* 10:   */ {
/* 11:   */   protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out)
/* 12:   */     throws Exception
/* 13:   */   {
/* 14:55 */     byte[] array = new byte[msg.readableBytes()];
/* 15:56 */     msg.getBytes(0, array);
/* 16:   */     
/* 17:58 */     out.add(array);
/* 18:   */   }
/* 19:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.bytes.ByteArrayDecoder
 * JD-Core Version:    0.7.0.1
 */