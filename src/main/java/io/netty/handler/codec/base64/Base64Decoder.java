/*  1:   */ package io.netty.handler.codec.base64;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.channel.ChannelHandler.Sharable;
/*  5:   */ import io.netty.channel.ChannelHandlerContext;
/*  6:   */ import io.netty.handler.codec.MessageToMessageDecoder;
/*  7:   */ import java.util.List;
/*  8:   */ 
/*  9:   */ @ChannelHandler.Sharable
/* 10:   */ public class Base64Decoder
/* 11:   */   extends MessageToMessageDecoder<ByteBuf>
/* 12:   */ {
/* 13:   */   private final Base64Dialect dialect;
/* 14:   */   
/* 15:   */   public Base64Decoder()
/* 16:   */   {
/* 17:52 */     this(Base64Dialect.STANDARD);
/* 18:   */   }
/* 19:   */   
/* 20:   */   public Base64Decoder(Base64Dialect dialect)
/* 21:   */   {
/* 22:56 */     if (dialect == null) {
/* 23:57 */       throw new NullPointerException("dialect");
/* 24:   */     }
/* 25:59 */     this.dialect = dialect;
/* 26:   */   }
/* 27:   */   
/* 28:   */   protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out)
/* 29:   */     throws Exception
/* 30:   */   {
/* 31:64 */     out.add(Base64.decode(msg, msg.readerIndex(), msg.readableBytes(), this.dialect));
/* 32:   */   }
/* 33:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.base64.Base64Decoder
 * JD-Core Version:    0.7.0.1
 */