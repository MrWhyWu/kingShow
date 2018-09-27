/*  1:   */ package io.netty.handler.codec.serialization;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.buffer.ByteBufInputStream;
/*  5:   */ import io.netty.channel.ChannelHandlerContext;
/*  6:   */ import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
/*  7:   */ import java.io.ObjectInputStream;
/*  8:   */ 
/*  9:   */ public class ObjectDecoder
/* 10:   */   extends LengthFieldBasedFrameDecoder
/* 11:   */ {
/* 12:   */   private final ClassResolver classResolver;
/* 13:   */   
/* 14:   */   public ObjectDecoder(ClassResolver classResolver)
/* 15:   */   {
/* 16:49 */     this(1048576, classResolver);
/* 17:   */   }
/* 18:   */   
/* 19:   */   public ObjectDecoder(int maxObjectSize, ClassResolver classResolver)
/* 20:   */   {
/* 21:63 */     super(maxObjectSize, 0, 4, 0, 4);
/* 22:64 */     this.classResolver = classResolver;
/* 23:   */   }
/* 24:   */   
/* 25:   */   protected Object decode(ChannelHandlerContext ctx, ByteBuf in)
/* 26:   */     throws Exception
/* 27:   */   {
/* 28:69 */     ByteBuf frame = (ByteBuf)super.decode(ctx, in);
/* 29:70 */     if (frame == null) {
/* 30:71 */       return null;
/* 31:   */     }
/* 32:74 */     ObjectInputStream ois = new CompactObjectInputStream(new ByteBufInputStream(frame, true), this.classResolver);
/* 33:   */     try
/* 34:   */     {
/* 35:76 */       return ois.readObject();
/* 36:   */     }
/* 37:   */     finally
/* 38:   */     {
/* 39:78 */       ois.close();
/* 40:   */     }
/* 41:   */   }
/* 42:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.serialization.ObjectDecoder
 * JD-Core Version:    0.7.0.1
 */