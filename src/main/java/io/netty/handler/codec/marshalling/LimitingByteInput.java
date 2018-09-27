/*  1:   */ package io.netty.handler.codec.marshalling;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.jboss.marshalling.ByteInput;
/*  5:   */ 
/*  6:   */ class LimitingByteInput
/*  7:   */   implements ByteInput
/*  8:   */ {
/*  9:29 */   private static final TooBigObjectException EXCEPTION = new TooBigObjectException();
/* 10:   */   private final ByteInput input;
/* 11:   */   private final long limit;
/* 12:   */   private long read;
/* 13:   */   
/* 14:   */   LimitingByteInput(ByteInput input, long limit)
/* 15:   */   {
/* 16:36 */     if (limit <= 0L) {
/* 17:37 */       throw new IllegalArgumentException("The limit MUST be > 0");
/* 18:   */     }
/* 19:39 */     this.input = input;
/* 20:40 */     this.limit = limit;
/* 21:   */   }
/* 22:   */   
/* 23:   */   public void close()
/* 24:   */     throws IOException
/* 25:   */   {}
/* 26:   */   
/* 27:   */   public int available()
/* 28:   */     throws IOException
/* 29:   */   {
/* 30:50 */     return readable(this.input.available());
/* 31:   */   }
/* 32:   */   
/* 33:   */   public int read()
/* 34:   */     throws IOException
/* 35:   */   {
/* 36:55 */     int readable = readable(1);
/* 37:56 */     if (readable > 0)
/* 38:   */     {
/* 39:57 */       int b = this.input.read();
/* 40:58 */       this.read += 1L;
/* 41:59 */       return b;
/* 42:   */     }
/* 43:61 */     throw EXCEPTION;
/* 44:   */   }
/* 45:   */   
/* 46:   */   public int read(byte[] array)
/* 47:   */     throws IOException
/* 48:   */   {
/* 49:67 */     return read(array, 0, array.length);
/* 50:   */   }
/* 51:   */   
/* 52:   */   public int read(byte[] array, int offset, int length)
/* 53:   */     throws IOException
/* 54:   */   {
/* 55:72 */     int readable = readable(length);
/* 56:73 */     if (readable > 0)
/* 57:   */     {
/* 58:74 */       int i = this.input.read(array, offset, readable);
/* 59:75 */       this.read += i;
/* 60:76 */       return i;
/* 61:   */     }
/* 62:78 */     throw EXCEPTION;
/* 63:   */   }
/* 64:   */   
/* 65:   */   public long skip(long bytes)
/* 66:   */     throws IOException
/* 67:   */   {
/* 68:84 */     int readable = readable((int)bytes);
/* 69:85 */     if (readable > 0)
/* 70:   */     {
/* 71:86 */       long i = this.input.skip(readable);
/* 72:87 */       this.read += i;
/* 73:88 */       return i;
/* 74:   */     }
/* 75:90 */     throw EXCEPTION;
/* 76:   */   }
/* 77:   */   
/* 78:   */   private int readable(int length)
/* 79:   */   {
/* 80:95 */     return (int)Math.min(length, this.limit - this.read);
/* 81:   */   }
/* 82:   */   
/* 83:   */   static final class TooBigObjectException
/* 84:   */     extends IOException
/* 85:   */   {
/* 86:   */     private static final long serialVersionUID = 1L;
/* 87:   */   }
/* 88:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.marshalling.LimitingByteInput
 * JD-Core Version:    0.7.0.1
 */