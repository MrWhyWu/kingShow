/*   1:    */ package io.netty.bootstrap;
/*   2:    */ 
/*   3:    */ import io.netty.channel.ChannelHandler;
/*   4:    */ import io.netty.channel.ChannelOption;
/*   5:    */ import io.netty.channel.EventLoopGroup;
/*   6:    */ import io.netty.channel.ServerChannel;
/*   7:    */ import io.netty.util.AttributeKey;
/*   8:    */ import io.netty.util.internal.StringUtil;
/*   9:    */ import java.util.Map;
/*  10:    */ 
/*  11:    */ public final class ServerBootstrapConfig
/*  12:    */   extends AbstractBootstrapConfig<ServerBootstrap, ServerChannel>
/*  13:    */ {
/*  14:    */   ServerBootstrapConfig(ServerBootstrap bootstrap)
/*  15:    */   {
/*  16: 33 */     super(bootstrap);
/*  17:    */   }
/*  18:    */   
/*  19:    */   public EventLoopGroup childGroup()
/*  20:    */   {
/*  21: 42 */     return ((ServerBootstrap)this.bootstrap).childGroup();
/*  22:    */   }
/*  23:    */   
/*  24:    */   public ChannelHandler childHandler()
/*  25:    */   {
/*  26: 50 */     return ((ServerBootstrap)this.bootstrap).childHandler();
/*  27:    */   }
/*  28:    */   
/*  29:    */   public Map<ChannelOption<?>, Object> childOptions()
/*  30:    */   {
/*  31: 57 */     return ((ServerBootstrap)this.bootstrap).childOptions();
/*  32:    */   }
/*  33:    */   
/*  34:    */   public Map<AttributeKey<?>, Object> childAttrs()
/*  35:    */   {
/*  36: 64 */     return ((ServerBootstrap)this.bootstrap).childAttrs();
/*  37:    */   }
/*  38:    */   
/*  39:    */   public String toString()
/*  40:    */   {
/*  41: 69 */     StringBuilder buf = new StringBuilder(super.toString());
/*  42: 70 */     buf.setLength(buf.length() - 1);
/*  43: 71 */     buf.append(", ");
/*  44: 72 */     EventLoopGroup childGroup = childGroup();
/*  45: 73 */     if (childGroup != null)
/*  46:    */     {
/*  47: 74 */       buf.append("childGroup: ");
/*  48: 75 */       buf.append(StringUtil.simpleClassName(childGroup));
/*  49: 76 */       buf.append(", ");
/*  50:    */     }
/*  51: 78 */     Map<ChannelOption<?>, Object> childOptions = childOptions();
/*  52: 79 */     if (!childOptions.isEmpty())
/*  53:    */     {
/*  54: 80 */       buf.append("childOptions: ");
/*  55: 81 */       buf.append(childOptions);
/*  56: 82 */       buf.append(", ");
/*  57:    */     }
/*  58: 84 */     Map<AttributeKey<?>, Object> childAttrs = childAttrs();
/*  59: 85 */     if (!childAttrs.isEmpty())
/*  60:    */     {
/*  61: 86 */       buf.append("childAttrs: ");
/*  62: 87 */       buf.append(childAttrs);
/*  63: 88 */       buf.append(", ");
/*  64:    */     }
/*  65: 90 */     ChannelHandler childHandler = childHandler();
/*  66: 91 */     if (childHandler != null)
/*  67:    */     {
/*  68: 92 */       buf.append("childHandler: ");
/*  69: 93 */       buf.append(childHandler);
/*  70: 94 */       buf.append(", ");
/*  71:    */     }
/*  72: 96 */     if (buf.charAt(buf.length() - 1) == '(')
/*  73:    */     {
/*  74: 97 */       buf.append(')');
/*  75:    */     }
/*  76:    */     else
/*  77:    */     {
/*  78: 99 */       buf.setCharAt(buf.length() - 2, ')');
/*  79:100 */       buf.setLength(buf.length() - 1);
/*  80:    */     }
/*  81:103 */     return buf.toString();
/*  82:    */   }
/*  83:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.bootstrap.ServerBootstrapConfig
 * JD-Core Version:    0.7.0.1
 */