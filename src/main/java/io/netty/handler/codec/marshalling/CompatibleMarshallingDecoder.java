/*   1:    */ package io.netty.handler.codec.marshalling;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.ChannelHandlerContext;
/*   5:    */ import io.netty.handler.codec.ReplayingDecoder;
/*   6:    */ import io.netty.handler.codec.TooLongFrameException;
/*   7:    */ import java.util.List;
/*   8:    */ import org.jboss.marshalling.ByteInput;
/*   9:    */ import org.jboss.marshalling.Unmarshaller;
/*  10:    */ 
/*  11:    */ public class CompatibleMarshallingDecoder
/*  12:    */   extends ReplayingDecoder<Void>
/*  13:    */ {
/*  14:    */   protected final UnmarshallerProvider provider;
/*  15:    */   protected final int maxObjectSize;
/*  16:    */   private boolean discardingTooLongFrame;
/*  17:    */   
/*  18:    */   public CompatibleMarshallingDecoder(UnmarshallerProvider provider, int maxObjectSize)
/*  19:    */   {
/*  20: 53 */     this.provider = provider;
/*  21: 54 */     this.maxObjectSize = maxObjectSize;
/*  22:    */   }
/*  23:    */   
/*  24:    */   protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out)
/*  25:    */     throws Exception
/*  26:    */   {
/*  27: 59 */     if (this.discardingTooLongFrame)
/*  28:    */     {
/*  29: 60 */       buffer.skipBytes(actualReadableBytes());
/*  30: 61 */       checkpoint();
/*  31: 62 */       return;
/*  32:    */     }
/*  33: 65 */     Unmarshaller unmarshaller = this.provider.getUnmarshaller(ctx);
/*  34: 66 */     ByteInput input = new ChannelBufferByteInput(buffer);
/*  35: 67 */     if (this.maxObjectSize != 2147483647) {
/*  36: 68 */       input = new LimitingByteInput(input, this.maxObjectSize);
/*  37:    */     }
/*  38:    */     try
/*  39:    */     {
/*  40: 71 */       unmarshaller.start(input);
/*  41: 72 */       Object obj = unmarshaller.readObject();
/*  42: 73 */       unmarshaller.finish();
/*  43: 74 */       out.add(obj);
/*  44:    */     }
/*  45:    */     catch (LimitingByteInput.TooBigObjectException ignored)
/*  46:    */     {
/*  47: 76 */       this.discardingTooLongFrame = true;
/*  48: 77 */       throw new TooLongFrameException();
/*  49:    */     }
/*  50:    */     finally
/*  51:    */     {
/*  52: 81 */       unmarshaller.close();
/*  53:    */     }
/*  54:    */   }
/*  55:    */   
/*  56:    */   protected void decodeLast(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out)
/*  57:    */     throws Exception
/*  58:    */   {
/*  59: 87 */     switch (buffer.readableBytes())
/*  60:    */     {
/*  61:    */     case 0: 
/*  62: 89 */       return;
/*  63:    */     case 1: 
/*  64: 92 */       if (buffer.getByte(buffer.readerIndex()) == 121)
/*  65:    */       {
/*  66: 93 */         buffer.skipBytes(1); return;
/*  67:    */       }
/*  68:    */       break;
/*  69:    */     }
/*  70: 98 */     decode(ctx, buffer, out);
/*  71:    */   }
/*  72:    */   
/*  73:    */   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
/*  74:    */     throws Exception
/*  75:    */   {
/*  76:103 */     if ((cause instanceof TooLongFrameException)) {
/*  77:104 */       ctx.close();
/*  78:    */     } else {
/*  79:106 */       super.exceptionCaught(ctx, cause);
/*  80:    */     }
/*  81:    */   }
/*  82:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.marshalling.CompatibleMarshallingDecoder
 * JD-Core Version:    0.7.0.1
 */