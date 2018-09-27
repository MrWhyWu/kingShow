/*  1:   */ package io.netty.handler.codec.protobuf;
/*  2:   */ 
/*  3:   */ import com.google.protobuf.nano.CodedOutputByteBufferNano;
/*  4:   */ import com.google.protobuf.nano.MessageNano;
/*  5:   */ import io.netty.buffer.ByteBuf;
/*  6:   */ import io.netty.buffer.ByteBufAllocator;
/*  7:   */ import io.netty.channel.ChannelHandler.Sharable;
/*  8:   */ import io.netty.channel.ChannelHandlerContext;
/*  9:   */ import io.netty.handler.codec.MessageToMessageEncoder;
/* 10:   */ import java.util.List;
/* 11:   */ 
/* 12:   */ @ChannelHandler.Sharable
/* 13:   */ public class ProtobufEncoderNano
/* 14:   */   extends MessageToMessageEncoder<MessageNano>
/* 15:   */ {
/* 16:   */   protected void encode(ChannelHandlerContext ctx, MessageNano msg, List<Object> out)
/* 17:   */     throws Exception
/* 18:   */   {
/* 19:64 */     int size = msg.getSerializedSize();
/* 20:65 */     ByteBuf buffer = ctx.alloc().heapBuffer(size, size);
/* 21:66 */     byte[] array = buffer.array();
/* 22:67 */     CodedOutputByteBufferNano cobbn = CodedOutputByteBufferNano.newInstance(array, buffer
/* 23:68 */       .arrayOffset(), buffer.capacity());
/* 24:69 */     msg.writeTo(cobbn);
/* 25:70 */     buffer.writerIndex(size);
/* 26:71 */     out.add(buffer);
/* 27:   */   }
/* 28:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.protobuf.ProtobufEncoderNano
 * JD-Core Version:    0.7.0.1
 */