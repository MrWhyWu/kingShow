/*  1:   */ package io.netty.handler.codec.serialization;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.buffer.ByteBufOutputStream;
/*  5:   */ import io.netty.channel.ChannelHandlerContext;
/*  6:   */ import io.netty.handler.codec.MessageToByteEncoder;
/*  7:   */ import java.io.ObjectOutputStream;
/*  8:   */ import java.io.OutputStream;
/*  9:   */ import java.io.Serializable;
/* 10:   */ 
/* 11:   */ public class CompatibleObjectEncoder
/* 12:   */   extends MessageToByteEncoder<Serializable>
/* 13:   */ {
/* 14:   */   private final int resetInterval;
/* 15:   */   private int writtenObjects;
/* 16:   */   
/* 17:   */   public CompatibleObjectEncoder()
/* 18:   */   {
/* 19:45 */     this(16);
/* 20:   */   }
/* 21:   */   
/* 22:   */   public CompatibleObjectEncoder(int resetInterval)
/* 23:   */   {
/* 24:58 */     if (resetInterval < 0) {
/* 25:59 */       throw new IllegalArgumentException("resetInterval: " + resetInterval);
/* 26:   */     }
/* 27:62 */     this.resetInterval = resetInterval;
/* 28:   */   }
/* 29:   */   
/* 30:   */   protected ObjectOutputStream newObjectOutputStream(OutputStream out)
/* 31:   */     throws Exception
/* 32:   */   {
/* 33:71 */     return new ObjectOutputStream(out);
/* 34:   */   }
/* 35:   */   
/* 36:   */   protected void encode(ChannelHandlerContext ctx, Serializable msg, ByteBuf out)
/* 37:   */     throws Exception
/* 38:   */   {
/* 39:76 */     ObjectOutputStream oos = newObjectOutputStream(new ByteBufOutputStream(out));
/* 40:   */     try
/* 41:   */     {
/* 42:78 */       if (this.resetInterval != 0)
/* 43:   */       {
/* 44:80 */         this.writtenObjects += 1;
/* 45:81 */         if (this.writtenObjects % this.resetInterval == 0) {
/* 46:82 */           oos.reset();
/* 47:   */         }
/* 48:   */       }
/* 49:86 */       oos.writeObject(msg);
/* 50:87 */       oos.flush();
/* 51:   */     }
/* 52:   */     finally
/* 53:   */     {
/* 54:89 */       oos.close();
/* 55:   */     }
/* 56:   */   }
/* 57:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.serialization.CompatibleObjectEncoder
 * JD-Core Version:    0.7.0.1
 */