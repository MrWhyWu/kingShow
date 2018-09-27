/*   1:    */ package io.protostuff;
/*   2:    */ 
/*   3:    */ import java.io.DataOutput;
/*   4:    */ import java.io.IOException;
/*   5:    */ import java.io.OutputStream;
/*   6:    */ 
/*   7:    */ public final class LinkedBuffer
/*   8:    */ {
/*   9:    */   public static final int MIN_BUFFER_SIZE = 256;
/*  10:    */   public static final int DEFAULT_BUFFER_SIZE = 512;
/*  11:    */   final byte[] buffer;
/*  12:    */   final int start;
/*  13:    */   int offset;
/*  14:    */   LinkedBuffer next;
/*  15:    */   
/*  16:    */   public static LinkedBuffer allocate()
/*  17:    */   {
/*  18: 45 */     return new LinkedBuffer(512);
/*  19:    */   }
/*  20:    */   
/*  21:    */   public static LinkedBuffer allocate(int size)
/*  22:    */   {
/*  23: 53 */     if (size < 256) {
/*  24: 54 */       throw new IllegalArgumentException("256 is the minimum buffer size.");
/*  25:    */     }
/*  26: 56 */     return new LinkedBuffer(size);
/*  27:    */   }
/*  28:    */   
/*  29:    */   public static LinkedBuffer allocate(int size, LinkedBuffer previous)
/*  30:    */   {
/*  31: 64 */     if (size < 256) {
/*  32: 65 */       throw new IllegalArgumentException("256 is the minimum buffer size.");
/*  33:    */     }
/*  34: 67 */     return new LinkedBuffer(size, previous);
/*  35:    */   }
/*  36:    */   
/*  37:    */   public static LinkedBuffer wrap(byte[] array, int offset, int length)
/*  38:    */   {
/*  39: 75 */     return new LinkedBuffer(array, offset, offset + length);
/*  40:    */   }
/*  41:    */   
/*  42:    */   public static LinkedBuffer use(byte[] buffer)
/*  43:    */   {
/*  44: 83 */     return use(buffer, 0);
/*  45:    */   }
/*  46:    */   
/*  47:    */   public static LinkedBuffer use(byte[] buffer, int start)
/*  48:    */   {
/*  49: 91 */     assert (start >= 0);
/*  50: 92 */     if (buffer.length - start < 256) {
/*  51: 93 */       throw new IllegalArgumentException("256 is the minimum buffer size.");
/*  52:    */     }
/*  53: 95 */     return new LinkedBuffer(buffer, start, start);
/*  54:    */   }
/*  55:    */   
/*  56:    */   public static int writeTo(OutputStream out, LinkedBuffer node)
/*  57:    */     throws IOException
/*  58:    */   {
/*  59:105 */     int contentSize = 0;
/*  60:    */     do
/*  61:    */     {
/*  62:    */       int len;
/*  63:108 */       if ((len = node.offset - node.start) > 0)
/*  64:    */       {
/*  65:110 */         out.write(node.buffer, node.start, len);
/*  66:111 */         contentSize += len;
/*  67:    */       }
/*  68:113 */     } while ((node = node.next) != null);
/*  69:115 */     return contentSize;
/*  70:    */   }
/*  71:    */   
/*  72:    */   public static int writeTo(DataOutput out, LinkedBuffer node)
/*  73:    */     throws IOException
/*  74:    */   {
/*  75:125 */     int contentSize = 0;
/*  76:    */     do
/*  77:    */     {
/*  78:    */       int len;
/*  79:128 */       if ((len = node.offset - node.start) > 0)
/*  80:    */       {
/*  81:130 */         out.write(node.buffer, node.start, len);
/*  82:131 */         contentSize += len;
/*  83:    */       }
/*  84:133 */     } while ((node = node.next) != null);
/*  85:135 */     return contentSize;
/*  86:    */   }
/*  87:    */   
/*  88:    */   LinkedBuffer(int size)
/*  89:    */   {
/*  90:151 */     this(new byte[size], 0, 0);
/*  91:    */   }
/*  92:    */   
/*  93:    */   LinkedBuffer(int size, LinkedBuffer appendTarget)
/*  94:    */   {
/*  95:159 */     this(new byte[size], 0, 0, appendTarget);
/*  96:    */   }
/*  97:    */   
/*  98:    */   LinkedBuffer(byte[] buffer, int offset)
/*  99:    */   {
/* 100:167 */     this(buffer, offset, offset);
/* 101:    */   }
/* 102:    */   
/* 103:    */   LinkedBuffer(byte[] buffer, int start, int offset)
/* 104:    */   {
/* 105:172 */     this.buffer = buffer;
/* 106:173 */     this.start = start;
/* 107:174 */     this.offset = offset;
/* 108:    */   }
/* 109:    */   
/* 110:    */   LinkedBuffer(byte[] buffer, int offset, LinkedBuffer appendTarget)
/* 111:    */   {
/* 112:182 */     this(buffer, offset, offset);
/* 113:183 */     appendTarget.next = this;
/* 114:    */   }
/* 115:    */   
/* 116:    */   LinkedBuffer(byte[] buffer, int start, int offset, LinkedBuffer appendTarget)
/* 117:    */   {
/* 118:188 */     this(buffer, start, offset);
/* 119:189 */     appendTarget.next = this;
/* 120:    */   }
/* 121:    */   
/* 122:    */   LinkedBuffer(LinkedBuffer viewSource, LinkedBuffer appendTarget)
/* 123:    */   {
/* 124:198 */     this.buffer = viewSource.buffer;
/* 125:199 */     this.offset = (this.start = viewSource.offset);
/* 126:200 */     appendTarget.next = this;
/* 127:    */   }
/* 128:    */   
/* 129:    */   public LinkedBuffer clear()
/* 130:    */   {
/* 131:208 */     this.next = null;
/* 132:209 */     this.offset = this.start;
/* 133:210 */     return this;
/* 134:    */   }
/* 135:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.LinkedBuffer
 * JD-Core Version:    0.7.0.1
 */