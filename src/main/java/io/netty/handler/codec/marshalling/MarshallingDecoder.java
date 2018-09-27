/*  1:   */ package io.netty.handler.codec.marshalling;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.channel.ChannelHandlerContext;
/*  5:   */ import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
/*  6:   */ import org.jboss.marshalling.ByteInput;
/*  7:   */ import org.jboss.marshalling.Unmarshaller;
/*  8:   */ 
/*  9:   */ public class MarshallingDecoder
/* 10:   */   extends LengthFieldBasedFrameDecoder
/* 11:   */ {
/* 12:   */   private final UnmarshallerProvider provider;
/* 13:   */   
/* 14:   */   public MarshallingDecoder(UnmarshallerProvider provider)
/* 15:   */   {
/* 16:45 */     this(provider, 1048576);
/* 17:   */   }
/* 18:   */   
/* 19:   */   public MarshallingDecoder(UnmarshallerProvider provider, int maxObjectSize)
/* 20:   */   {
/* 21:57 */     super(maxObjectSize, 0, 4, 0, 4);
/* 22:58 */     this.provider = provider;
/* 23:   */   }
/* 24:   */   
/* 25:   */   protected Object decode(ChannelHandlerContext ctx, ByteBuf in)
/* 26:   */     throws Exception
/* 27:   */   {
/* 28:63 */     ByteBuf frame = (ByteBuf)super.decode(ctx, in);
/* 29:64 */     if (frame == null) {
/* 30:65 */       return null;
/* 31:   */     }
/* 32:68 */     Unmarshaller unmarshaller = this.provider.getUnmarshaller(ctx);
/* 33:69 */     ByteInput input = new ChannelBufferByteInput(frame);
/* 34:   */     try
/* 35:   */     {
/* 36:72 */       unmarshaller.start(input);
/* 37:73 */       Object obj = unmarshaller.readObject();
/* 38:74 */       unmarshaller.finish();
/* 39:75 */       return obj;
/* 40:   */     }
/* 41:   */     finally
/* 42:   */     {
/* 43:79 */       unmarshaller.close();
/* 44:   */     }
/* 45:   */   }
/* 46:   */   
/* 47:   */   protected ByteBuf extractFrame(ChannelHandlerContext ctx, ByteBuf buffer, int index, int length)
/* 48:   */   {
/* 49:85 */     return buffer.slice(index, length);
/* 50:   */   }
/* 51:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.marshalling.MarshallingDecoder
 * JD-Core Version:    0.7.0.1
 */