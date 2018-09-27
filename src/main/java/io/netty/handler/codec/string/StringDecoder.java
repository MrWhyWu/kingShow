/*  1:   */ package io.netty.handler.codec.string;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.channel.ChannelHandler.Sharable;
/*  5:   */ import io.netty.channel.ChannelHandlerContext;
/*  6:   */ import io.netty.handler.codec.MessageToMessageDecoder;
/*  7:   */ import java.nio.charset.Charset;
/*  8:   */ import java.util.List;
/*  9:   */ 
/* 10:   */ @ChannelHandler.Sharable
/* 11:   */ public class StringDecoder
/* 12:   */   extends MessageToMessageDecoder<ByteBuf>
/* 13:   */ {
/* 14:   */   private final Charset charset;
/* 15:   */   
/* 16:   */   public StringDecoder()
/* 17:   */   {
/* 18:64 */     this(Charset.defaultCharset());
/* 19:   */   }
/* 20:   */   
/* 21:   */   public StringDecoder(Charset charset)
/* 22:   */   {
/* 23:71 */     if (charset == null) {
/* 24:72 */       throw new NullPointerException("charset");
/* 25:   */     }
/* 26:74 */     this.charset = charset;
/* 27:   */   }
/* 28:   */   
/* 29:   */   protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out)
/* 30:   */     throws Exception
/* 31:   */   {
/* 32:79 */     out.add(msg.toString(this.charset));
/* 33:   */   }
/* 34:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.string.StringDecoder
 * JD-Core Version:    0.7.0.1
 */