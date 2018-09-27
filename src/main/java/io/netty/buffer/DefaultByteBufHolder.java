/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.IllegalReferenceCountException;
/*   4:    */ import io.netty.util.internal.StringUtil;
/*   5:    */ 
/*   6:    */ public class DefaultByteBufHolder
/*   7:    */   implements ByteBufHolder
/*   8:    */ {
/*   9:    */   private final ByteBuf data;
/*  10:    */   
/*  11:    */   public DefaultByteBufHolder(ByteBuf data)
/*  12:    */   {
/*  13: 30 */     if (data == null) {
/*  14: 31 */       throw new NullPointerException("data");
/*  15:    */     }
/*  16: 33 */     this.data = data;
/*  17:    */   }
/*  18:    */   
/*  19:    */   public ByteBuf content()
/*  20:    */   {
/*  21: 38 */     if (this.data.refCnt() <= 0) {
/*  22: 39 */       throw new IllegalReferenceCountException(this.data.refCnt());
/*  23:    */     }
/*  24: 41 */     return this.data;
/*  25:    */   }
/*  26:    */   
/*  27:    */   public ByteBufHolder copy()
/*  28:    */   {
/*  29: 51 */     return replace(this.data.copy());
/*  30:    */   }
/*  31:    */   
/*  32:    */   public ByteBufHolder duplicate()
/*  33:    */   {
/*  34: 61 */     return replace(this.data.duplicate());
/*  35:    */   }
/*  36:    */   
/*  37:    */   public ByteBufHolder retainedDuplicate()
/*  38:    */   {
/*  39: 71 */     return replace(this.data.retainedDuplicate());
/*  40:    */   }
/*  41:    */   
/*  42:    */   public ByteBufHolder replace(ByteBuf content)
/*  43:    */   {
/*  44: 83 */     return new DefaultByteBufHolder(content);
/*  45:    */   }
/*  46:    */   
/*  47:    */   public int refCnt()
/*  48:    */   {
/*  49: 88 */     return this.data.refCnt();
/*  50:    */   }
/*  51:    */   
/*  52:    */   public ByteBufHolder retain()
/*  53:    */   {
/*  54: 93 */     this.data.retain();
/*  55: 94 */     return this;
/*  56:    */   }
/*  57:    */   
/*  58:    */   public ByteBufHolder retain(int increment)
/*  59:    */   {
/*  60: 99 */     this.data.retain(increment);
/*  61:100 */     return this;
/*  62:    */   }
/*  63:    */   
/*  64:    */   public ByteBufHolder touch()
/*  65:    */   {
/*  66:105 */     this.data.touch();
/*  67:106 */     return this;
/*  68:    */   }
/*  69:    */   
/*  70:    */   public ByteBufHolder touch(Object hint)
/*  71:    */   {
/*  72:111 */     this.data.touch(hint);
/*  73:112 */     return this;
/*  74:    */   }
/*  75:    */   
/*  76:    */   public boolean release()
/*  77:    */   {
/*  78:117 */     return this.data.release();
/*  79:    */   }
/*  80:    */   
/*  81:    */   public boolean release(int decrement)
/*  82:    */   {
/*  83:122 */     return this.data.release(decrement);
/*  84:    */   }
/*  85:    */   
/*  86:    */   protected final String contentToString()
/*  87:    */   {
/*  88:130 */     return this.data.toString();
/*  89:    */   }
/*  90:    */   
/*  91:    */   public String toString()
/*  92:    */   {
/*  93:135 */     return StringUtil.simpleClassName(this) + '(' + contentToString() + ')';
/*  94:    */   }
/*  95:    */   
/*  96:    */   public boolean equals(Object o)
/*  97:    */   {
/*  98:140 */     if (this == o) {
/*  99:141 */       return true;
/* 100:    */     }
/* 101:143 */     if ((o instanceof ByteBufHolder)) {
/* 102:144 */       return this.data.equals(((ByteBufHolder)o).content());
/* 103:    */     }
/* 104:146 */     return false;
/* 105:    */   }
/* 106:    */   
/* 107:    */   public int hashCode()
/* 108:    */   {
/* 109:151 */     return this.data.hashCode();
/* 110:    */   }
/* 111:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.DefaultByteBufHolder
 * JD-Core Version:    0.7.0.1
 */