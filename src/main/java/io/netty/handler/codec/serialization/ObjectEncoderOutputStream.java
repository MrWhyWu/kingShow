/*   1:    */ package io.netty.handler.codec.serialization;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufOutputStream;
/*   5:    */ import io.netty.buffer.Unpooled;
/*   6:    */ import java.io.DataOutputStream;
/*   7:    */ import java.io.IOException;
/*   8:    */ import java.io.ObjectOutput;
/*   9:    */ import java.io.ObjectOutputStream;
/*  10:    */ import java.io.OutputStream;
/*  11:    */ 
/*  12:    */ public class ObjectEncoderOutputStream
/*  13:    */   extends OutputStream
/*  14:    */   implements ObjectOutput
/*  15:    */ {
/*  16:    */   private final DataOutputStream out;
/*  17:    */   private final int estimatedLength;
/*  18:    */   
/*  19:    */   public ObjectEncoderOutputStream(OutputStream out)
/*  20:    */   {
/*  21: 47 */     this(out, 512);
/*  22:    */   }
/*  23:    */   
/*  24:    */   public ObjectEncoderOutputStream(OutputStream out, int estimatedLength)
/*  25:    */   {
/*  26: 66 */     if (out == null) {
/*  27: 67 */       throw new NullPointerException("out");
/*  28:    */     }
/*  29: 69 */     if (estimatedLength < 0) {
/*  30: 70 */       throw new IllegalArgumentException("estimatedLength: " + estimatedLength);
/*  31:    */     }
/*  32: 73 */     if ((out instanceof DataOutputStream)) {
/*  33: 74 */       this.out = ((DataOutputStream)out);
/*  34:    */     } else {
/*  35: 76 */       this.out = new DataOutputStream(out);
/*  36:    */     }
/*  37: 78 */     this.estimatedLength = estimatedLength;
/*  38:    */   }
/*  39:    */   
/*  40:    */   public void writeObject(Object obj)
/*  41:    */     throws IOException
/*  42:    */   {
/*  43: 83 */     ByteBuf buf = Unpooled.buffer(this.estimatedLength);
/*  44:    */     try
/*  45:    */     {
/*  46: 85 */       ObjectOutputStream oout = new CompactObjectOutputStream(new ByteBufOutputStream(buf));
/*  47:    */       try
/*  48:    */       {
/*  49: 87 */         oout.writeObject(obj);
/*  50: 88 */         oout.flush();
/*  51:    */       }
/*  52:    */       finally
/*  53:    */       {
/*  54: 90 */         oout.close();
/*  55:    */       }
/*  56: 93 */       int objectSize = buf.readableBytes();
/*  57: 94 */       writeInt(objectSize);
/*  58: 95 */       buf.getBytes(0, this, objectSize);
/*  59:    */     }
/*  60:    */     finally
/*  61:    */     {
/*  62: 97 */       buf.release();
/*  63:    */     }
/*  64:    */   }
/*  65:    */   
/*  66:    */   public void write(int b)
/*  67:    */     throws IOException
/*  68:    */   {
/*  69:103 */     this.out.write(b);
/*  70:    */   }
/*  71:    */   
/*  72:    */   public void close()
/*  73:    */     throws IOException
/*  74:    */   {
/*  75:108 */     this.out.close();
/*  76:    */   }
/*  77:    */   
/*  78:    */   public void flush()
/*  79:    */     throws IOException
/*  80:    */   {
/*  81:113 */     this.out.flush();
/*  82:    */   }
/*  83:    */   
/*  84:    */   public final int size()
/*  85:    */   {
/*  86:117 */     return this.out.size();
/*  87:    */   }
/*  88:    */   
/*  89:    */   public void write(byte[] b, int off, int len)
/*  90:    */     throws IOException
/*  91:    */   {
/*  92:122 */     this.out.write(b, off, len);
/*  93:    */   }
/*  94:    */   
/*  95:    */   public void write(byte[] b)
/*  96:    */     throws IOException
/*  97:    */   {
/*  98:127 */     this.out.write(b);
/*  99:    */   }
/* 100:    */   
/* 101:    */   public final void writeBoolean(boolean v)
/* 102:    */     throws IOException
/* 103:    */   {
/* 104:132 */     this.out.writeBoolean(v);
/* 105:    */   }
/* 106:    */   
/* 107:    */   public final void writeByte(int v)
/* 108:    */     throws IOException
/* 109:    */   {
/* 110:137 */     this.out.writeByte(v);
/* 111:    */   }
/* 112:    */   
/* 113:    */   public final void writeBytes(String s)
/* 114:    */     throws IOException
/* 115:    */   {
/* 116:142 */     this.out.writeBytes(s);
/* 117:    */   }
/* 118:    */   
/* 119:    */   public final void writeChar(int v)
/* 120:    */     throws IOException
/* 121:    */   {
/* 122:147 */     this.out.writeChar(v);
/* 123:    */   }
/* 124:    */   
/* 125:    */   public final void writeChars(String s)
/* 126:    */     throws IOException
/* 127:    */   {
/* 128:152 */     this.out.writeChars(s);
/* 129:    */   }
/* 130:    */   
/* 131:    */   public final void writeDouble(double v)
/* 132:    */     throws IOException
/* 133:    */   {
/* 134:157 */     this.out.writeDouble(v);
/* 135:    */   }
/* 136:    */   
/* 137:    */   public final void writeFloat(float v)
/* 138:    */     throws IOException
/* 139:    */   {
/* 140:162 */     this.out.writeFloat(v);
/* 141:    */   }
/* 142:    */   
/* 143:    */   public final void writeInt(int v)
/* 144:    */     throws IOException
/* 145:    */   {
/* 146:167 */     this.out.writeInt(v);
/* 147:    */   }
/* 148:    */   
/* 149:    */   public final void writeLong(long v)
/* 150:    */     throws IOException
/* 151:    */   {
/* 152:172 */     this.out.writeLong(v);
/* 153:    */   }
/* 154:    */   
/* 155:    */   public final void writeShort(int v)
/* 156:    */     throws IOException
/* 157:    */   {
/* 158:177 */     this.out.writeShort(v);
/* 159:    */   }
/* 160:    */   
/* 161:    */   public final void writeUTF(String str)
/* 162:    */     throws IOException
/* 163:    */   {
/* 164:182 */     this.out.writeUTF(str);
/* 165:    */   }
/* 166:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.serialization.ObjectEncoderOutputStream
 * JD-Core Version:    0.7.0.1
 */