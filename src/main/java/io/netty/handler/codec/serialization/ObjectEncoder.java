/*  1:   */ package io.netty.handler.codec.serialization;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.buffer.ByteBufOutputStream;
/*  5:   */ import io.netty.channel.ChannelHandler.Sharable;
/*  6:   */ import io.netty.channel.ChannelHandlerContext;
/*  7:   */ import io.netty.handler.codec.MessageToByteEncoder;
/*  8:   */ import java.io.ObjectOutputStream;
/*  9:   */ import java.io.Serializable;
/* 10:   */ 
/* 11:   */ @ChannelHandler.Sharable
/* 12:   */ public class ObjectEncoder
/* 13:   */   extends MessageToByteEncoder<Serializable>
/* 14:   */ {
/* 15:38 */   private static final byte[] LENGTH_PLACEHOLDER = new byte[4];
/* 16:   */   
/* 17:   */   protected void encode(ChannelHandlerContext ctx, Serializable msg, ByteBuf out)
/* 18:   */     throws Exception
/* 19:   */   {
/* 20:42 */     int startIdx = out.writerIndex();
/* 21:   */     
/* 22:44 */     ByteBufOutputStream bout = new ByteBufOutputStream(out);
/* 23:45 */     ObjectOutputStream oout = null;
/* 24:   */     try
/* 25:   */     {
/* 26:47 */       bout.write(LENGTH_PLACEHOLDER);
/* 27:48 */       oout = new CompactObjectOutputStream(bout);
/* 28:49 */       oout.writeObject(msg);
/* 29:50 */       oout.flush();
/* 30:   */     }
/* 31:   */     finally
/* 32:   */     {
/* 33:52 */       if (oout != null) {
/* 34:53 */         oout.close();
/* 35:   */       } else {
/* 36:55 */         bout.close();
/* 37:   */       }
/* 38:   */     }
/* 39:59 */     int endIdx = out.writerIndex();
/* 40:   */     
/* 41:61 */     out.setInt(startIdx, endIdx - startIdx - 4);
/* 42:   */   }
/* 43:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.serialization.ObjectEncoder
 * JD-Core Version:    0.7.0.1
 */