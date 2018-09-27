/*   1:    */ package io.netty.handler.codec.compression;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.ChannelHandlerContext;
/*   5:    */ import io.netty.handler.codec.MessageToByteEncoder;
/*   6:    */ 
/*   7:    */ public class SnappyFrameEncoder
/*   8:    */   extends MessageToByteEncoder<ByteBuf>
/*   9:    */ {
/*  10:    */   private static final int MIN_COMPRESSIBLE_LENGTH = 18;
/*  11: 41 */   private static final byte[] STREAM_START = { -1, 6, 0, 0, 115, 78, 97, 80, 112, 89 };
/*  12: 45 */   private final Snappy snappy = new Snappy();
/*  13:    */   private boolean started;
/*  14:    */   
/*  15:    */   protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out)
/*  16:    */     throws Exception
/*  17:    */   {
/*  18: 50 */     if (!in.isReadable()) {
/*  19: 51 */       return;
/*  20:    */     }
/*  21: 54 */     if (!this.started)
/*  22:    */     {
/*  23: 55 */       this.started = true;
/*  24: 56 */       out.writeBytes(STREAM_START);
/*  25:    */     }
/*  26: 59 */     int dataLength = in.readableBytes();
/*  27: 60 */     if (dataLength > 18) {
/*  28:    */       for (;;)
/*  29:    */       {
/*  30: 62 */         int lengthIdx = out.writerIndex() + 1;
/*  31: 63 */         if (dataLength < 18)
/*  32:    */         {
/*  33: 64 */           ByteBuf slice = in.readSlice(dataLength);
/*  34: 65 */           writeUnencodedChunk(slice, out, dataLength);
/*  35: 66 */           break;
/*  36:    */         }
/*  37: 69 */         out.writeInt(0);
/*  38: 70 */         if (dataLength > 32767)
/*  39:    */         {
/*  40: 71 */           ByteBuf slice = in.readSlice(32767);
/*  41: 72 */           calculateAndWriteChecksum(slice, out);
/*  42: 73 */           this.snappy.encode(slice, out, 32767);
/*  43: 74 */           setChunkLength(out, lengthIdx);
/*  44: 75 */           dataLength -= 32767;
/*  45:    */         }
/*  46:    */         else
/*  47:    */         {
/*  48: 77 */           ByteBuf slice = in.readSlice(dataLength);
/*  49: 78 */           calculateAndWriteChecksum(slice, out);
/*  50: 79 */           this.snappy.encode(slice, out, dataLength);
/*  51: 80 */           setChunkLength(out, lengthIdx);
/*  52: 81 */           break;
/*  53:    */         }
/*  54:    */       }
/*  55:    */     }
/*  56: 85 */     writeUnencodedChunk(in, out, dataLength);
/*  57:    */   }
/*  58:    */   
/*  59:    */   private static void writeUnencodedChunk(ByteBuf in, ByteBuf out, int dataLength)
/*  60:    */   {
/*  61: 90 */     out.writeByte(1);
/*  62: 91 */     writeChunkLength(out, dataLength + 4);
/*  63: 92 */     calculateAndWriteChecksum(in, out);
/*  64: 93 */     out.writeBytes(in, dataLength);
/*  65:    */   }
/*  66:    */   
/*  67:    */   private static void setChunkLength(ByteBuf out, int lengthIdx)
/*  68:    */   {
/*  69: 97 */     int chunkLength = out.writerIndex() - lengthIdx - 3;
/*  70: 98 */     if (chunkLength >>> 24 != 0) {
/*  71: 99 */       throw new CompressionException("compressed data too large: " + chunkLength);
/*  72:    */     }
/*  73:101 */     out.setMediumLE(lengthIdx, chunkLength);
/*  74:    */   }
/*  75:    */   
/*  76:    */   private static void writeChunkLength(ByteBuf out, int chunkLength)
/*  77:    */   {
/*  78:111 */     out.writeMediumLE(chunkLength);
/*  79:    */   }
/*  80:    */   
/*  81:    */   private static void calculateAndWriteChecksum(ByteBuf slice, ByteBuf out)
/*  82:    */   {
/*  83:121 */     out.writeIntLE(Snappy.calculateChecksum(slice));
/*  84:    */   }
/*  85:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.compression.SnappyFrameEncoder
 * JD-Core Version:    0.7.0.1
 */