/*  1:   */ package io.netty.handler.codec.marshalling;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.channel.ChannelHandler.Sharable;
/*  5:   */ import io.netty.channel.ChannelHandlerContext;
/*  6:   */ import io.netty.handler.codec.MessageToByteEncoder;
/*  7:   */ import org.jboss.marshalling.Marshaller;
/*  8:   */ 
/*  9:   */ @ChannelHandler.Sharable
/* 10:   */ public class CompatibleMarshallingEncoder
/* 11:   */   extends MessageToByteEncoder<Object>
/* 12:   */ {
/* 13:   */   private final MarshallerProvider provider;
/* 14:   */   
/* 15:   */   public CompatibleMarshallingEncoder(MarshallerProvider provider)
/* 16:   */   {
/* 17:47 */     this.provider = provider;
/* 18:   */   }
/* 19:   */   
/* 20:   */   protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out)
/* 21:   */     throws Exception
/* 22:   */   {
/* 23:52 */     Marshaller marshaller = this.provider.getMarshaller(ctx);
/* 24:53 */     marshaller.start(new ChannelBufferByteOutput(out));
/* 25:54 */     marshaller.writeObject(msg);
/* 26:55 */     marshaller.finish();
/* 27:56 */     marshaller.close();
/* 28:   */   }
/* 29:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.marshalling.CompatibleMarshallingEncoder
 * JD-Core Version:    0.7.0.1
 */