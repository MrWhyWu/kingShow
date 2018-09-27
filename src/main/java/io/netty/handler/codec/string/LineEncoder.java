/*  1:   */ package io.netty.handler.codec.string;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.buffer.ByteBufUtil;
/*  5:   */ import io.netty.channel.ChannelHandler.Sharable;
/*  6:   */ import io.netty.channel.ChannelHandlerContext;
/*  7:   */ import io.netty.handler.codec.MessageToMessageEncoder;
/*  8:   */ import io.netty.util.CharsetUtil;
/*  9:   */ import io.netty.util.internal.ObjectUtil;
/* 10:   */ import java.nio.CharBuffer;
/* 11:   */ import java.nio.charset.Charset;
/* 12:   */ import java.util.List;
/* 13:   */ 
/* 14:   */ @ChannelHandler.Sharable
/* 15:   */ public class LineEncoder
/* 16:   */   extends MessageToMessageEncoder<CharSequence>
/* 17:   */ {
/* 18:   */   private final Charset charset;
/* 19:   */   private final byte[] lineSeparator;
/* 20:   */   
/* 21:   */   public LineEncoder()
/* 22:   */   {
/* 23:63 */     this(LineSeparator.DEFAULT, CharsetUtil.UTF_8);
/* 24:   */   }
/* 25:   */   
/* 26:   */   public LineEncoder(LineSeparator lineSeparator)
/* 27:   */   {
/* 28:70 */     this(lineSeparator, CharsetUtil.UTF_8);
/* 29:   */   }
/* 30:   */   
/* 31:   */   public LineEncoder(Charset charset)
/* 32:   */   {
/* 33:77 */     this(LineSeparator.DEFAULT, charset);
/* 34:   */   }
/* 35:   */   
/* 36:   */   public LineEncoder(LineSeparator lineSeparator, Charset charset)
/* 37:   */   {
/* 38:84 */     this.charset = ((Charset)ObjectUtil.checkNotNull(charset, "charset"));
/* 39:85 */     this.lineSeparator = ((LineSeparator)ObjectUtil.checkNotNull(lineSeparator, "lineSeparator")).value().getBytes(charset);
/* 40:   */   }
/* 41:   */   
/* 42:   */   protected void encode(ChannelHandlerContext ctx, CharSequence msg, List<Object> out)
/* 43:   */     throws Exception
/* 44:   */   {
/* 45:90 */     ByteBuf buffer = ByteBufUtil.encodeString(ctx.alloc(), CharBuffer.wrap(msg), this.charset, this.lineSeparator.length);
/* 46:91 */     buffer.writeBytes(this.lineSeparator);
/* 47:92 */     out.add(buffer);
/* 48:   */   }
/* 49:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.string.LineEncoder
 * JD-Core Version:    0.7.0.1
 */