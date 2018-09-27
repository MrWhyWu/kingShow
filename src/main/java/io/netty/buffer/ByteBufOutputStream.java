/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.CharsetUtil;
/*   4:    */ import java.io.DataOutput;
/*   5:    */ import java.io.DataOutputStream;
/*   6:    */ import java.io.IOException;
/*   7:    */ import java.io.OutputStream;
/*   8:    */ 
/*   9:    */ public class ByteBufOutputStream
/*  10:    */   extends OutputStream
/*  11:    */   implements DataOutput
/*  12:    */ {
/*  13:    */   private final ByteBuf buffer;
/*  14:    */   private final int startIndex;
/*  15: 42 */   private final DataOutputStream utf8out = new DataOutputStream(this);
/*  16:    */   
/*  17:    */   public ByteBufOutputStream(ByteBuf buffer)
/*  18:    */   {
/*  19: 48 */     if (buffer == null) {
/*  20: 49 */       throw new NullPointerException("buffer");
/*  21:    */     }
/*  22: 51 */     this.buffer = buffer;
/*  23: 52 */     this.startIndex = buffer.writerIndex();
/*  24:    */   }
/*  25:    */   
/*  26:    */   public int writtenBytes()
/*  27:    */   {
/*  28: 59 */     return this.buffer.writerIndex() - this.startIndex;
/*  29:    */   }
/*  30:    */   
/*  31:    */   public void write(byte[] b, int off, int len)
/*  32:    */     throws IOException
/*  33:    */   {
/*  34: 64 */     if (len == 0) {
/*  35: 65 */       return;
/*  36:    */     }
/*  37: 68 */     this.buffer.writeBytes(b, off, len);
/*  38:    */   }
/*  39:    */   
/*  40:    */   public void write(byte[] b)
/*  41:    */     throws IOException
/*  42:    */   {
/*  43: 73 */     this.buffer.writeBytes(b);
/*  44:    */   }
/*  45:    */   
/*  46:    */   public void write(int b)
/*  47:    */     throws IOException
/*  48:    */   {
/*  49: 78 */     this.buffer.writeByte(b);
/*  50:    */   }
/*  51:    */   
/*  52:    */   public void writeBoolean(boolean v)
/*  53:    */     throws IOException
/*  54:    */   {
/*  55: 83 */     this.buffer.writeBoolean(v);
/*  56:    */   }
/*  57:    */   
/*  58:    */   public void writeByte(int v)
/*  59:    */     throws IOException
/*  60:    */   {
/*  61: 88 */     this.buffer.writeByte(v);
/*  62:    */   }
/*  63:    */   
/*  64:    */   public void writeBytes(String s)
/*  65:    */     throws IOException
/*  66:    */   {
/*  67: 93 */     this.buffer.writeCharSequence(s, CharsetUtil.US_ASCII);
/*  68:    */   }
/*  69:    */   
/*  70:    */   public void writeChar(int v)
/*  71:    */     throws IOException
/*  72:    */   {
/*  73: 98 */     this.buffer.writeChar(v);
/*  74:    */   }
/*  75:    */   
/*  76:    */   public void writeChars(String s)
/*  77:    */     throws IOException
/*  78:    */   {
/*  79:103 */     int len = s.length();
/*  80:104 */     for (int i = 0; i < len; i++) {
/*  81:105 */       this.buffer.writeChar(s.charAt(i));
/*  82:    */     }
/*  83:    */   }
/*  84:    */   
/*  85:    */   public void writeDouble(double v)
/*  86:    */     throws IOException
/*  87:    */   {
/*  88:111 */     this.buffer.writeDouble(v);
/*  89:    */   }
/*  90:    */   
/*  91:    */   public void writeFloat(float v)
/*  92:    */     throws IOException
/*  93:    */   {
/*  94:116 */     this.buffer.writeFloat(v);
/*  95:    */   }
/*  96:    */   
/*  97:    */   public void writeInt(int v)
/*  98:    */     throws IOException
/*  99:    */   {
/* 100:121 */     this.buffer.writeInt(v);
/* 101:    */   }
/* 102:    */   
/* 103:    */   public void writeLong(long v)
/* 104:    */     throws IOException
/* 105:    */   {
/* 106:126 */     this.buffer.writeLong(v);
/* 107:    */   }
/* 108:    */   
/* 109:    */   public void writeShort(int v)
/* 110:    */     throws IOException
/* 111:    */   {
/* 112:131 */     this.buffer.writeShort((short)v);
/* 113:    */   }
/* 114:    */   
/* 115:    */   public void writeUTF(String s)
/* 116:    */     throws IOException
/* 117:    */   {
/* 118:136 */     this.utf8out.writeUTF(s);
/* 119:    */   }
/* 120:    */   
/* 121:    */   public ByteBuf buffer()
/* 122:    */   {
/* 123:143 */     return this.buffer;
/* 124:    */   }
/* 125:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.ByteBufOutputStream
 * JD-Core Version:    0.7.0.1
 */