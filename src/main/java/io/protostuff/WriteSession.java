/*   1:    */ package io.protostuff;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import java.io.OutputStream;
/*   5:    */ 
/*   6:    */ public class WriteSession
/*   7:    */ {
/*   8:    */   public final LinkedBuffer head;
/*   9:    */   protected LinkedBuffer tail;
/*  10: 57 */   protected int size = 0;
/*  11:    */   public final int nextBufferSize;
/*  12:    */   public final OutputStream out;
/*  13:    */   public final FlushHandler flushHandler;
/*  14:    */   public final WriteSink sink;
/*  15:    */   
/*  16:    */   public WriteSession(LinkedBuffer head)
/*  17:    */   {
/*  18: 78 */     this(head, 512);
/*  19:    */   }
/*  20:    */   
/*  21:    */   public WriteSession(LinkedBuffer head, int nextBufferSize)
/*  22:    */   {
/*  23: 83 */     this.tail = head;
/*  24: 84 */     this.head = head;
/*  25: 85 */     this.nextBufferSize = nextBufferSize;
/*  26: 86 */     this.out = null;
/*  27: 87 */     this.flushHandler = null;
/*  28:    */     
/*  29: 89 */     this.sink = WriteSink.BUFFERED;
/*  30:    */   }
/*  31:    */   
/*  32:    */   public WriteSession(LinkedBuffer head, OutputStream out, FlushHandler flushHandler, int nextBufferSize)
/*  33:    */   {
/*  34: 95 */     this.tail = head;
/*  35: 96 */     this.head = head;
/*  36: 97 */     this.nextBufferSize = nextBufferSize;
/*  37: 98 */     this.out = out;
/*  38: 99 */     this.flushHandler = flushHandler;
/*  39:    */     
/*  40:101 */     this.sink = WriteSink.STREAMED;
/*  41:    */     
/*  42:103 */     assert (out != null);
/*  43:    */   }
/*  44:    */   
/*  45:    */   public WriteSession(LinkedBuffer head, OutputStream out)
/*  46:    */   {
/*  47:108 */     this(head, out, null, 512);
/*  48:    */   }
/*  49:    */   
/*  50:    */   public void reset() {}
/*  51:    */   
/*  52:    */   public WriteSession clear()
/*  53:    */   {
/*  54:124 */     this.tail = this.head.clear();
/*  55:125 */     this.size = 0;
/*  56:126 */     return this;
/*  57:    */   }
/*  58:    */   
/*  59:    */   public final int getSize()
/*  60:    */   {
/*  61:134 */     return this.size;
/*  62:    */   }
/*  63:    */   
/*  64:    */   public final byte[] toByteArray()
/*  65:    */   {
/*  66:142 */     LinkedBuffer node = this.head;
/*  67:143 */     int offset = 0;
/*  68:144 */     byte[] buf = new byte[this.size];
/*  69:    */     do
/*  70:    */     {
/*  71:    */       int len;
/*  72:147 */       if ((len = node.offset - node.start) > 0)
/*  73:    */       {
/*  74:149 */         System.arraycopy(node.buffer, node.start, buf, offset, len);
/*  75:150 */         offset += len;
/*  76:    */       }
/*  77:152 */     } while ((node = node.next) != null);
/*  78:154 */     return buf;
/*  79:    */   }
/*  80:    */   
/*  81:    */   protected int flush(byte[] buf, int offset, int len)
/*  82:    */     throws IOException
/*  83:    */   {
/*  84:159 */     if (this.flushHandler != null) {
/*  85:160 */       return this.flushHandler.flush(this, buf, offset, len);
/*  86:    */     }
/*  87:162 */     this.out.write(buf, offset, len);
/*  88:163 */     return offset;
/*  89:    */   }
/*  90:    */   
/*  91:    */   protected int flush(byte[] buf, int offset, int len, byte[] next, int nextoffset, int nextlen)
/*  92:    */     throws IOException
/*  93:    */   {
/*  94:169 */     if (this.flushHandler != null) {
/*  95:170 */       return this.flushHandler.flush(this, buf, offset, len, next, nextoffset, nextlen);
/*  96:    */     }
/*  97:172 */     this.out.write(buf, offset, len);
/*  98:173 */     this.out.write(next, nextoffset, nextlen);
/*  99:174 */     return offset;
/* 100:    */   }
/* 101:    */   
/* 102:    */   protected int flush(LinkedBuffer lb, byte[] buf, int offset, int len)
/* 103:    */     throws IOException
/* 104:    */   {
/* 105:180 */     if (this.flushHandler != null) {
/* 106:181 */       return this.flushHandler.flush(this, lb, buf, offset, len);
/* 107:    */     }
/* 108:183 */     this.out.write(buf, offset, len);
/* 109:184 */     return lb.start;
/* 110:    */   }
/* 111:    */   
/* 112:    */   public static abstract interface FlushHandler
/* 113:    */   {
/* 114:    */     public abstract int flush(WriteSession paramWriteSession, byte[] paramArrayOfByte, int paramInt1, int paramInt2)
/* 115:    */       throws IOException;
/* 116:    */     
/* 117:    */     public abstract int flush(WriteSession paramWriteSession, byte[] paramArrayOfByte1, int paramInt1, int paramInt2, byte[] paramArrayOfByte2, int paramInt3, int paramInt4)
/* 118:    */       throws IOException;
/* 119:    */     
/* 120:    */     public abstract int flush(WriteSession paramWriteSession, LinkedBuffer paramLinkedBuffer, byte[] paramArrayOfByte, int paramInt1, int paramInt2)
/* 121:    */       throws IOException;
/* 122:    */   }
/* 123:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.WriteSession
 * JD-Core Version:    0.7.0.1
 */