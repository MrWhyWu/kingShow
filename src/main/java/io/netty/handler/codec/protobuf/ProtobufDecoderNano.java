/*  1:   */ package io.netty.handler.codec.protobuf;
/*  2:   */ 
/*  3:   */ import com.google.protobuf.nano.MessageNano;
/*  4:   */ import io.netty.buffer.ByteBuf;
/*  5:   */ import io.netty.channel.ChannelHandler.Sharable;
/*  6:   */ import io.netty.channel.ChannelHandlerContext;
/*  7:   */ import io.netty.handler.codec.MessageToMessageDecoder;
/*  8:   */ import io.netty.util.internal.ObjectUtil;
/*  9:   */ import java.lang.reflect.Constructor;
/* 10:   */ import java.util.List;
/* 11:   */ 
/* 12:   */ @ChannelHandler.Sharable
/* 13:   */ public class ProtobufDecoderNano
/* 14:   */   extends MessageToMessageDecoder<ByteBuf>
/* 15:   */ {
/* 16:   */   private final Class<? extends MessageNano> clazz;
/* 17:   */   
/* 18:   */   public ProtobufDecoderNano(Class<? extends MessageNano> clazz)
/* 19:   */   {
/* 20:68 */     this.clazz = ((Class)ObjectUtil.checkNotNull(clazz, "You must provide a Class"));
/* 21:   */   }
/* 22:   */   
/* 23:   */   protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out)
/* 24:   */     throws Exception
/* 25:   */   {
/* 26:76 */     int length = msg.readableBytes();
/* 27:   */     int offset;
/* 28:   */     byte[] array;
/* 29:   */     int offset;
/* 30:77 */     if (msg.hasArray())
/* 31:   */     {
/* 32:78 */       byte[] array = msg.array();
/* 33:79 */       offset = msg.arrayOffset() + msg.readerIndex();
/* 34:   */     }
/* 35:   */     else
/* 36:   */     {
/* 37:81 */       array = new byte[length];
/* 38:82 */       msg.getBytes(msg.readerIndex(), array, 0, length);
/* 39:83 */       offset = 0;
/* 40:   */     }
/* 41:85 */     MessageNano prototype = (MessageNano)this.clazz.getConstructor(new Class[0]).newInstance(new Object[0]);
/* 42:86 */     out.add(MessageNano.mergeFrom(prototype, array, offset, length));
/* 43:   */   }
/* 44:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.protobuf.ProtobufDecoderNano
 * JD-Core Version:    0.7.0.1
 */