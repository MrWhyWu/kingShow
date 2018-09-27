/*   1:    */ package io.netty.handler.codec.protobuf;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.ChannelHandlerContext;
/*   5:    */ import io.netty.handler.codec.ByteToMessageDecoder;
/*   6:    */ import io.netty.handler.codec.CorruptedFrameException;
/*   7:    */ import java.util.List;
/*   8:    */ 
/*   9:    */ public class ProtobufVarint32FrameDecoder
/*  10:    */   extends ByteToMessageDecoder
/*  11:    */ {
/*  12:    */   protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/*  13:    */     throws Exception
/*  14:    */   {
/*  15: 51 */     in.markReaderIndex();
/*  16: 52 */     int preIndex = in.readerIndex();
/*  17: 53 */     int length = readRawVarint32(in);
/*  18: 54 */     if (preIndex == in.readerIndex()) {
/*  19: 55 */       return;
/*  20:    */     }
/*  21: 57 */     if (length < 0) {
/*  22: 58 */       throw new CorruptedFrameException("negative length: " + length);
/*  23:    */     }
/*  24: 61 */     if (in.readableBytes() < length) {
/*  25: 62 */       in.resetReaderIndex();
/*  26:    */     } else {
/*  27: 64 */       out.add(in.readRetainedSlice(length));
/*  28:    */     }
/*  29:    */   }
/*  30:    */   
/*  31:    */   private static int readRawVarint32(ByteBuf buffer)
/*  32:    */   {
/*  33: 74 */     if (!buffer.isReadable()) {
/*  34: 75 */       return 0;
/*  35:    */     }
/*  36: 77 */     buffer.markReaderIndex();
/*  37: 78 */     byte tmp = buffer.readByte();
/*  38: 79 */     if (tmp >= 0) {
/*  39: 80 */       return tmp;
/*  40:    */     }
/*  41: 82 */     int result = tmp & 0x7F;
/*  42: 83 */     if (!buffer.isReadable())
/*  43:    */     {
/*  44: 84 */       buffer.resetReaderIndex();
/*  45: 85 */       return 0;
/*  46:    */     }
/*  47: 87 */     if ((tmp = buffer.readByte()) >= 0)
/*  48:    */     {
/*  49: 88 */       result |= tmp << 7;
/*  50:    */     }
/*  51:    */     else
/*  52:    */     {
/*  53: 90 */       result |= (tmp & 0x7F) << 7;
/*  54: 91 */       if (!buffer.isReadable())
/*  55:    */       {
/*  56: 92 */         buffer.resetReaderIndex();
/*  57: 93 */         return 0;
/*  58:    */       }
/*  59: 95 */       if ((tmp = buffer.readByte()) >= 0)
/*  60:    */       {
/*  61: 96 */         result |= tmp << 14;
/*  62:    */       }
/*  63:    */       else
/*  64:    */       {
/*  65: 98 */         result |= (tmp & 0x7F) << 14;
/*  66: 99 */         if (!buffer.isReadable())
/*  67:    */         {
/*  68:100 */           buffer.resetReaderIndex();
/*  69:101 */           return 0;
/*  70:    */         }
/*  71:103 */         if ((tmp = buffer.readByte()) >= 0)
/*  72:    */         {
/*  73:104 */           result |= tmp << 21;
/*  74:    */         }
/*  75:    */         else
/*  76:    */         {
/*  77:106 */           result |= (tmp & 0x7F) << 21;
/*  78:107 */           if (!buffer.isReadable())
/*  79:    */           {
/*  80:108 */             buffer.resetReaderIndex();
/*  81:109 */             return 0;
/*  82:    */           }
/*  83:111 */           result |= (tmp = buffer.readByte()) << 28;
/*  84:112 */           if (tmp < 0) {
/*  85:113 */             throw new CorruptedFrameException("malformed varint.");
/*  86:    */           }
/*  87:    */         }
/*  88:    */       }
/*  89:    */     }
/*  90:118 */     return result;
/*  91:    */   }
/*  92:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder
 * JD-Core Version:    0.7.0.1
 */