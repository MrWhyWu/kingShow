/*  1:   */ package io.netty.handler.codec.compression;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.channel.ChannelFuture;
/*  5:   */ import io.netty.channel.ChannelPromise;
/*  6:   */ import io.netty.handler.codec.MessageToByteEncoder;
/*  7:   */ 
/*  8:   */ public abstract class ZlibEncoder
/*  9:   */   extends MessageToByteEncoder<ByteBuf>
/* 10:   */ {
/* 11:   */   protected ZlibEncoder()
/* 12:   */   {
/* 13:29 */     super(false);
/* 14:   */   }
/* 15:   */   
/* 16:   */   public abstract boolean isClosed();
/* 17:   */   
/* 18:   */   public abstract ChannelFuture close();
/* 19:   */   
/* 20:   */   public abstract ChannelFuture close(ChannelPromise paramChannelPromise);
/* 21:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.compression.ZlibEncoder
 * JD-Core Version:    0.7.0.1
 */