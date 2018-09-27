/*  1:   */ package io.netty.handler.codec.marshalling;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.channel.ChannelHandler.Sharable;
/*  5:   */ import io.netty.channel.ChannelHandlerContext;
/*  6:   */ import io.netty.handler.codec.MessageToByteEncoder;
/*  7:   */ import org.jboss.marshalling.Marshaller;
/*  8:   */ 
/*  9:   */ @ChannelHandler.Sharable
/* 10:   */ public class MarshallingEncoder
/* 11:   */   extends MessageToByteEncoder<Object>
/* 12:   */ {
/* 13:40 */   private static final byte[] LENGTH_PLACEHOLDER = new byte[4];
/* 14:   */   private final MarshallerProvider provider;
/* 15:   */   
/* 16:   */   public MarshallingEncoder(MarshallerProvider provider)
/* 17:   */   {
/* 18:49 */     this.provider = provider;
/* 19:   */   }
/* 20:   */   
/* 21:   */   protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out)
/* 22:   */     throws Exception
/* 23:   */   {
/* 24:54 */     Marshaller marshaller = this.provider.getMarshaller(ctx);
/* 25:55 */     int lengthPos = out.writerIndex();
/* 26:56 */     out.writeBytes(LENGTH_PLACEHOLDER);
/* 27:57 */     ChannelBufferByteOutput output = new ChannelBufferByteOutput(out);
/* 28:58 */     marshaller.start(output);
/* 29:59 */     marshaller.writeObject(msg);
/* 30:60 */     marshaller.finish();
/* 31:61 */     marshaller.close();
/* 32:   */     
/* 33:63 */     out.setInt(lengthPos, out.writerIndex() - lengthPos - 4);
/* 34:   */   }
/* 35:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.marshalling.MarshallingEncoder
 * JD-Core Version:    0.7.0.1
 */