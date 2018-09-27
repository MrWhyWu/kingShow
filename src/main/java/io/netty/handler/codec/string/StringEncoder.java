/*  1:   */ package io.netty.handler.codec.string;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBufUtil;
/*  4:   */ import io.netty.channel.ChannelHandler.Sharable;
/*  5:   */ import io.netty.channel.ChannelHandlerContext;
/*  6:   */ import io.netty.handler.codec.MessageToMessageEncoder;
/*  7:   */ import java.nio.CharBuffer;
/*  8:   */ import java.nio.charset.Charset;
/*  9:   */ import java.util.List;
/* 10:   */ 
/* 11:   */ @ChannelHandler.Sharable
/* 12:   */ public class StringEncoder
/* 13:   */   extends MessageToMessageEncoder<CharSequence>
/* 14:   */ {
/* 15:   */   private final Charset charset;
/* 16:   */   
/* 17:   */   public StringEncoder()
/* 18:   */   {
/* 19:61 */     this(Charset.defaultCharset());
/* 20:   */   }
/* 21:   */   
/* 22:   */   public StringEncoder(Charset charset)
/* 23:   */   {
/* 24:68 */     if (charset == null) {
/* 25:69 */       throw new NullPointerException("charset");
/* 26:   */     }
/* 27:71 */     this.charset = charset;
/* 28:   */   }
/* 29:   */   
/* 30:   */   protected void encode(ChannelHandlerContext ctx, CharSequence msg, List<Object> out)
/* 31:   */     throws Exception
/* 32:   */   {
/* 33:76 */     if (msg.length() == 0) {
/* 34:77 */       return;
/* 35:   */     }
/* 36:80 */     out.add(ByteBufUtil.encodeString(ctx.alloc(), CharBuffer.wrap(msg), this.charset));
/* 37:   */   }
/* 38:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.string.StringEncoder
 * JD-Core Version:    0.7.0.1
 */