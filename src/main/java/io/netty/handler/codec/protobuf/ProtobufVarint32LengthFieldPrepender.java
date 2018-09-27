/*  1:   */ package io.netty.handler.codec.protobuf;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.channel.ChannelHandler.Sharable;
/*  5:   */ import io.netty.channel.ChannelHandlerContext;
/*  6:   */ import io.netty.handler.codec.MessageToByteEncoder;
/*  7:   */ 
/*  8:   */ @ChannelHandler.Sharable
/*  9:   */ public class ProtobufVarint32LengthFieldPrepender
/* 10:   */   extends MessageToByteEncoder<ByteBuf>
/* 11:   */ {
/* 12:   */   protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out)
/* 13:   */     throws Exception
/* 14:   */   {
/* 15:46 */     int bodyLen = msg.readableBytes();
/* 16:47 */     int headerLen = computeRawVarint32Size(bodyLen);
/* 17:48 */     out.ensureWritable(headerLen + bodyLen);
/* 18:49 */     writeRawVarint32(out, bodyLen);
/* 19:50 */     out.writeBytes(msg, msg.readerIndex(), bodyLen);
/* 20:   */   }
/* 21:   */   
/* 22:   */   static void writeRawVarint32(ByteBuf out, int value)
/* 23:   */   {
/* 24:   */     for (;;)
/* 25:   */     {
/* 26:60 */       if ((value & 0xFFFFFF80) == 0)
/* 27:   */       {
/* 28:61 */         out.writeByte(value);
/* 29:62 */         return;
/* 30:   */       }
/* 31:64 */       out.writeByte(value & 0x7F | 0x80);
/* 32:65 */       value >>>= 7;
/* 33:   */     }
/* 34:   */   }
/* 35:   */   
/* 36:   */   static int computeRawVarint32Size(int value)
/* 37:   */   {
/* 38:76 */     if ((value & 0xFFFFFF80) == 0) {
/* 39:77 */       return 1;
/* 40:   */     }
/* 41:79 */     if ((value & 0xFFFFC000) == 0) {
/* 42:80 */       return 2;
/* 43:   */     }
/* 44:82 */     if ((value & 0xFFE00000) == 0) {
/* 45:83 */       return 3;
/* 46:   */     }
/* 47:85 */     if ((value & 0xF0000000) == 0) {
/* 48:86 */       return 4;
/* 49:   */     }
/* 50:88 */     return 5;
/* 51:   */   }
/* 52:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender
 * JD-Core Version:    0.7.0.1
 */