/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.util.AbstractReferenceCounted;
/*   5:    */ import io.netty.util.IllegalReferenceCountException;
/*   6:    */ import io.netty.util.internal.ObjectUtil;
/*   7:    */ 
/*   8:    */ class PemValue
/*   9:    */   extends AbstractReferenceCounted
/*  10:    */   implements PemEncoded
/*  11:    */ {
/*  12:    */   private final ByteBuf content;
/*  13:    */   private final boolean sensitive;
/*  14:    */   
/*  15:    */   public PemValue(ByteBuf content, boolean sensitive)
/*  16:    */   {
/*  17: 38 */     this.content = ((ByteBuf)ObjectUtil.checkNotNull(content, "content"));
/*  18: 39 */     this.sensitive = sensitive;
/*  19:    */   }
/*  20:    */   
/*  21:    */   public boolean isSensitive()
/*  22:    */   {
/*  23: 44 */     return this.sensitive;
/*  24:    */   }
/*  25:    */   
/*  26:    */   public ByteBuf content()
/*  27:    */   {
/*  28: 49 */     int count = refCnt();
/*  29: 50 */     if (count <= 0) {
/*  30: 51 */       throw new IllegalReferenceCountException(count);
/*  31:    */     }
/*  32: 54 */     return this.content;
/*  33:    */   }
/*  34:    */   
/*  35:    */   public PemValue copy()
/*  36:    */   {
/*  37: 59 */     return replace(this.content.copy());
/*  38:    */   }
/*  39:    */   
/*  40:    */   public PemValue duplicate()
/*  41:    */   {
/*  42: 64 */     return replace(this.content.duplicate());
/*  43:    */   }
/*  44:    */   
/*  45:    */   public PemValue retainedDuplicate()
/*  46:    */   {
/*  47: 69 */     return replace(this.content.retainedDuplicate());
/*  48:    */   }
/*  49:    */   
/*  50:    */   public PemValue replace(ByteBuf content)
/*  51:    */   {
/*  52: 74 */     return new PemValue(content, this.sensitive);
/*  53:    */   }
/*  54:    */   
/*  55:    */   public PemValue touch()
/*  56:    */   {
/*  57: 79 */     return (PemValue)super.touch();
/*  58:    */   }
/*  59:    */   
/*  60:    */   public PemValue touch(Object hint)
/*  61:    */   {
/*  62: 84 */     this.content.touch(hint);
/*  63: 85 */     return this;
/*  64:    */   }
/*  65:    */   
/*  66:    */   public PemValue retain()
/*  67:    */   {
/*  68: 90 */     return (PemValue)super.retain();
/*  69:    */   }
/*  70:    */   
/*  71:    */   public PemValue retain(int increment)
/*  72:    */   {
/*  73: 95 */     return (PemValue)super.retain(increment);
/*  74:    */   }
/*  75:    */   
/*  76:    */   protected void deallocate()
/*  77:    */   {
/*  78:100 */     if (this.sensitive) {
/*  79:101 */       SslUtils.zeroout(this.content);
/*  80:    */     }
/*  81:103 */     this.content.release();
/*  82:    */   }
/*  83:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.PemValue
 * JD-Core Version:    0.7.0.1
 */