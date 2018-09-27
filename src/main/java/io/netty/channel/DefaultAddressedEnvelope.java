/*   1:    */ package io.netty.channel;
/*   2:    */ 
/*   3:    */ import io.netty.util.ReferenceCountUtil;
/*   4:    */ import io.netty.util.ReferenceCounted;
/*   5:    */ import io.netty.util.internal.StringUtil;
/*   6:    */ import java.net.SocketAddress;
/*   7:    */ 
/*   8:    */ public class DefaultAddressedEnvelope<M, A extends SocketAddress>
/*   9:    */   implements AddressedEnvelope<M, A>
/*  10:    */ {
/*  11:    */   private final M message;
/*  12:    */   private final A sender;
/*  13:    */   private final A recipient;
/*  14:    */   
/*  15:    */   public DefaultAddressedEnvelope(M message, A recipient, A sender)
/*  16:    */   {
/*  17: 42 */     if (message == null) {
/*  18: 43 */       throw new NullPointerException("message");
/*  19:    */     }
/*  20: 46 */     if ((recipient == null) && (sender == null)) {
/*  21: 47 */       throw new NullPointerException("recipient and sender");
/*  22:    */     }
/*  23: 50 */     this.message = message;
/*  24: 51 */     this.sender = sender;
/*  25: 52 */     this.recipient = recipient;
/*  26:    */   }
/*  27:    */   
/*  28:    */   public DefaultAddressedEnvelope(M message, A recipient)
/*  29:    */   {
/*  30: 60 */     this(message, recipient, null);
/*  31:    */   }
/*  32:    */   
/*  33:    */   public M content()
/*  34:    */   {
/*  35: 65 */     return this.message;
/*  36:    */   }
/*  37:    */   
/*  38:    */   public A sender()
/*  39:    */   {
/*  40: 70 */     return this.sender;
/*  41:    */   }
/*  42:    */   
/*  43:    */   public A recipient()
/*  44:    */   {
/*  45: 75 */     return this.recipient;
/*  46:    */   }
/*  47:    */   
/*  48:    */   public int refCnt()
/*  49:    */   {
/*  50: 80 */     if ((this.message instanceof ReferenceCounted)) {
/*  51: 81 */       return ((ReferenceCounted)this.message).refCnt();
/*  52:    */     }
/*  53: 83 */     return 1;
/*  54:    */   }
/*  55:    */   
/*  56:    */   public AddressedEnvelope<M, A> retain()
/*  57:    */   {
/*  58: 89 */     ReferenceCountUtil.retain(this.message);
/*  59: 90 */     return this;
/*  60:    */   }
/*  61:    */   
/*  62:    */   public AddressedEnvelope<M, A> retain(int increment)
/*  63:    */   {
/*  64: 95 */     ReferenceCountUtil.retain(this.message, increment);
/*  65: 96 */     return this;
/*  66:    */   }
/*  67:    */   
/*  68:    */   public boolean release()
/*  69:    */   {
/*  70:101 */     return ReferenceCountUtil.release(this.message);
/*  71:    */   }
/*  72:    */   
/*  73:    */   public boolean release(int decrement)
/*  74:    */   {
/*  75:106 */     return ReferenceCountUtil.release(this.message, decrement);
/*  76:    */   }
/*  77:    */   
/*  78:    */   public AddressedEnvelope<M, A> touch()
/*  79:    */   {
/*  80:111 */     ReferenceCountUtil.touch(this.message);
/*  81:112 */     return this;
/*  82:    */   }
/*  83:    */   
/*  84:    */   public AddressedEnvelope<M, A> touch(Object hint)
/*  85:    */   {
/*  86:117 */     ReferenceCountUtil.touch(this.message, hint);
/*  87:118 */     return this;
/*  88:    */   }
/*  89:    */   
/*  90:    */   public String toString()
/*  91:    */   {
/*  92:123 */     if (this.sender != null) {
/*  93:124 */       return StringUtil.simpleClassName(this) + '(' + this.sender + " => " + this.recipient + ", " + this.message + ')';
/*  94:    */     }
/*  95:127 */     return StringUtil.simpleClassName(this) + "(=> " + this.recipient + ", " + this.message + ')';
/*  96:    */   }
/*  97:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.DefaultAddressedEnvelope
 * JD-Core Version:    0.7.0.1
 */