/*  1:   */ package io.netty.handler.codec.marshalling;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import java.io.IOException;
/*  5:   */ import org.jboss.marshalling.ByteOutput;
/*  6:   */ 
/*  7:   */ class ChannelBufferByteOutput
/*  8:   */   implements ByteOutput
/*  9:   */ {
/* 10:   */   private final ByteBuf buffer;
/* 11:   */   
/* 12:   */   ChannelBufferByteOutput(ByteBuf buffer)
/* 13:   */   {
/* 14:36 */     this.buffer = buffer;
/* 15:   */   }
/* 16:   */   
/* 17:   */   public void close()
/* 18:   */     throws IOException
/* 19:   */   {}
/* 20:   */   
/* 21:   */   public void flush()
/* 22:   */     throws IOException
/* 23:   */   {}
/* 24:   */   
/* 25:   */   public void write(int b)
/* 26:   */     throws IOException
/* 27:   */   {
/* 28:51 */     this.buffer.writeByte(b);
/* 29:   */   }
/* 30:   */   
/* 31:   */   public void write(byte[] bytes)
/* 32:   */     throws IOException
/* 33:   */   {
/* 34:56 */     this.buffer.writeBytes(bytes);
/* 35:   */   }
/* 36:   */   
/* 37:   */   public void write(byte[] bytes, int srcIndex, int length)
/* 38:   */     throws IOException
/* 39:   */   {
/* 40:61 */     this.buffer.writeBytes(bytes, srcIndex, length);
/* 41:   */   }
/* 42:   */   
/* 43:   */   ByteBuf getBuffer()
/* 44:   */   {
/* 45:69 */     return this.buffer;
/* 46:   */   }
/* 47:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.marshalling.ChannelBufferByteOutput
 * JD-Core Version:    0.7.0.1
 */