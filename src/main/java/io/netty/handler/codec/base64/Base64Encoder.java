/*  1:   */ package io.netty.handler.codec.base64;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.channel.ChannelHandler.Sharable;
/*  5:   */ import io.netty.channel.ChannelHandlerContext;
/*  6:   */ import io.netty.handler.codec.MessageToMessageEncoder;
/*  7:   */ import java.util.List;
/*  8:   */ 
/*  9:   */ @ChannelHandler.Sharable
/* 10:   */ public class Base64Encoder
/* 11:   */   extends MessageToMessageEncoder<ByteBuf>
/* 12:   */ {
/* 13:   */   private final boolean breakLines;
/* 14:   */   private final Base64Dialect dialect;
/* 15:   */   
/* 16:   */   public Base64Encoder()
/* 17:   */   {
/* 18:49 */     this(true);
/* 19:   */   }
/* 20:   */   
/* 21:   */   public Base64Encoder(boolean breakLines)
/* 22:   */   {
/* 23:53 */     this(breakLines, Base64Dialect.STANDARD);
/* 24:   */   }
/* 25:   */   
/* 26:   */   public Base64Encoder(boolean breakLines, Base64Dialect dialect)
/* 27:   */   {
/* 28:57 */     if (dialect == null) {
/* 29:58 */       throw new NullPointerException("dialect");
/* 30:   */     }
/* 31:61 */     this.breakLines = breakLines;
/* 32:62 */     this.dialect = dialect;
/* 33:   */   }
/* 34:   */   
/* 35:   */   protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out)
/* 36:   */     throws Exception
/* 37:   */   {
/* 38:67 */     out.add(Base64.encode(msg, msg.readerIndex(), msg.readableBytes(), this.breakLines, this.dialect));
/* 39:   */   }
/* 40:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.base64.Base64Encoder
 * JD-Core Version:    0.7.0.1
 */