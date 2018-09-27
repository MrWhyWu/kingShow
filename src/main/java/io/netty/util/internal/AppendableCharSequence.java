/*   1:    */ package io.netty.util.internal;
/*   2:    */ 
/*   3:    */ import java.util.Arrays;
/*   4:    */ 
/*   5:    */ public final class AppendableCharSequence
/*   6:    */   implements CharSequence, Appendable
/*   7:    */ {
/*   8:    */   private char[] chars;
/*   9:    */   private int pos;
/*  10:    */   
/*  11:    */   public AppendableCharSequence(int length)
/*  12:    */   {
/*  13: 26 */     if (length < 1) {
/*  14: 27 */       throw new IllegalArgumentException("length: " + length + " (length: >= 1)");
/*  15:    */     }
/*  16: 29 */     this.chars = new char[length];
/*  17:    */   }
/*  18:    */   
/*  19:    */   private AppendableCharSequence(char[] chars)
/*  20:    */   {
/*  21: 33 */     if (chars.length < 1) {
/*  22: 34 */       throw new IllegalArgumentException("length: " + chars.length + " (length: >= 1)");
/*  23:    */     }
/*  24: 36 */     this.chars = chars;
/*  25: 37 */     this.pos = chars.length;
/*  26:    */   }
/*  27:    */   
/*  28:    */   public int length()
/*  29:    */   {
/*  30: 42 */     return this.pos;
/*  31:    */   }
/*  32:    */   
/*  33:    */   public char charAt(int index)
/*  34:    */   {
/*  35: 47 */     if (index > this.pos) {
/*  36: 48 */       throw new IndexOutOfBoundsException();
/*  37:    */     }
/*  38: 50 */     return this.chars[index];
/*  39:    */   }
/*  40:    */   
/*  41:    */   public char charAtUnsafe(int index)
/*  42:    */   {
/*  43: 61 */     return this.chars[index];
/*  44:    */   }
/*  45:    */   
/*  46:    */   public AppendableCharSequence subSequence(int start, int end)
/*  47:    */   {
/*  48: 66 */     return new AppendableCharSequence(Arrays.copyOfRange(this.chars, start, end));
/*  49:    */   }
/*  50:    */   
/*  51:    */   public AppendableCharSequence append(char c)
/*  52:    */   {
/*  53: 71 */     if (this.pos == this.chars.length)
/*  54:    */     {
/*  55: 72 */       char[] old = this.chars;
/*  56: 73 */       this.chars = new char[old.length << 1];
/*  57: 74 */       System.arraycopy(old, 0, this.chars, 0, old.length);
/*  58:    */     }
/*  59: 76 */     this.chars[(this.pos++)] = c;
/*  60: 77 */     return this;
/*  61:    */   }
/*  62:    */   
/*  63:    */   public AppendableCharSequence append(CharSequence csq)
/*  64:    */   {
/*  65: 82 */     return append(csq, 0, csq.length());
/*  66:    */   }
/*  67:    */   
/*  68:    */   public AppendableCharSequence append(CharSequence csq, int start, int end)
/*  69:    */   {
/*  70: 87 */     if (csq.length() < end) {
/*  71: 88 */       throw new IndexOutOfBoundsException();
/*  72:    */     }
/*  73: 90 */     int length = end - start;
/*  74: 91 */     if (length > this.chars.length - this.pos) {
/*  75: 92 */       this.chars = expand(this.chars, this.pos + length, this.pos);
/*  76:    */     }
/*  77: 94 */     if ((csq instanceof AppendableCharSequence))
/*  78:    */     {
/*  79: 96 */       AppendableCharSequence seq = (AppendableCharSequence)csq;
/*  80: 97 */       char[] src = seq.chars;
/*  81: 98 */       System.arraycopy(src, start, this.chars, this.pos, length);
/*  82: 99 */       this.pos += length;
/*  83:100 */       return this;
/*  84:    */     }
/*  85:102 */     for (int i = start; i < end; i++) {
/*  86:103 */       this.chars[(this.pos++)] = csq.charAt(i);
/*  87:    */     }
/*  88:106 */     return this;
/*  89:    */   }
/*  90:    */   
/*  91:    */   public void reset()
/*  92:    */   {
/*  93:114 */     this.pos = 0;
/*  94:    */   }
/*  95:    */   
/*  96:    */   public String toString()
/*  97:    */   {
/*  98:119 */     return new String(this.chars, 0, this.pos);
/*  99:    */   }
/* 100:    */   
/* 101:    */   public String substring(int start, int end)
/* 102:    */   {
/* 103:126 */     int length = end - start;
/* 104:127 */     if ((start > this.pos) || (length > this.pos)) {
/* 105:128 */       throw new IndexOutOfBoundsException();
/* 106:    */     }
/* 107:130 */     return new String(this.chars, start, length);
/* 108:    */   }
/* 109:    */   
/* 110:    */   public String subStringUnsafe(int start, int end)
/* 111:    */   {
/* 112:139 */     return new String(this.chars, start, end - start);
/* 113:    */   }
/* 114:    */   
/* 115:    */   private static char[] expand(char[] array, int neededSpace, int size)
/* 116:    */   {
/* 117:143 */     int newCapacity = array.length;
/* 118:    */     do
/* 119:    */     {
/* 120:146 */       newCapacity <<= 1;
/* 121:148 */       if (newCapacity < 0) {
/* 122:149 */         throw new IllegalStateException();
/* 123:    */       }
/* 124:152 */     } while (neededSpace > newCapacity);
/* 125:154 */     char[] newArray = new char[newCapacity];
/* 126:155 */     System.arraycopy(array, 0, newArray, 0, size);
/* 127:    */     
/* 128:157 */     return newArray;
/* 129:    */   }
/* 130:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.AppendableCharSequence
 * JD-Core Version:    0.7.0.1
 */