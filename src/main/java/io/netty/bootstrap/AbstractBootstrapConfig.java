/*   1:    */ package io.netty.bootstrap;
/*   2:    */ 
/*   3:    */ import io.netty.channel.Channel;
/*   4:    */ import io.netty.channel.ChannelHandler;
/*   5:    */ import io.netty.channel.ChannelOption;
/*   6:    */ import io.netty.channel.EventLoopGroup;
/*   7:    */ import io.netty.util.AttributeKey;
/*   8:    */ import io.netty.util.internal.ObjectUtil;
/*   9:    */ import io.netty.util.internal.StringUtil;
/*  10:    */ import java.net.SocketAddress;
/*  11:    */ import java.util.Map;
/*  12:    */ 
/*  13:    */ public abstract class AbstractBootstrapConfig<B extends AbstractBootstrap<B, C>, C extends Channel>
/*  14:    */ {
/*  15:    */   protected final B bootstrap;
/*  16:    */   
/*  17:    */   protected AbstractBootstrapConfig(B bootstrap)
/*  18:    */   {
/*  19: 37 */     this.bootstrap = ((AbstractBootstrap)ObjectUtil.checkNotNull(bootstrap, "bootstrap"));
/*  20:    */   }
/*  21:    */   
/*  22:    */   public final SocketAddress localAddress()
/*  23:    */   {
/*  24: 44 */     return this.bootstrap.localAddress();
/*  25:    */   }
/*  26:    */   
/*  27:    */   public final ChannelFactory<? extends C> channelFactory()
/*  28:    */   {
/*  29: 52 */     return this.bootstrap.channelFactory();
/*  30:    */   }
/*  31:    */   
/*  32:    */   public final ChannelHandler handler()
/*  33:    */   {
/*  34: 59 */     return this.bootstrap.handler();
/*  35:    */   }
/*  36:    */   
/*  37:    */   public final Map<ChannelOption<?>, Object> options()
/*  38:    */   {
/*  39: 66 */     return this.bootstrap.options();
/*  40:    */   }
/*  41:    */   
/*  42:    */   public final Map<AttributeKey<?>, Object> attrs()
/*  43:    */   {
/*  44: 73 */     return this.bootstrap.attrs();
/*  45:    */   }
/*  46:    */   
/*  47:    */   public final EventLoopGroup group()
/*  48:    */   {
/*  49: 81 */     return this.bootstrap.group();
/*  50:    */   }
/*  51:    */   
/*  52:    */   public String toString()
/*  53:    */   {
/*  54: 88 */     StringBuilder buf = new StringBuilder().append(StringUtil.simpleClassName(this)).append('(');
/*  55: 89 */     EventLoopGroup group = group();
/*  56: 90 */     if (group != null) {
/*  57: 93 */       buf.append("group: ").append(StringUtil.simpleClassName(group)).append(", ");
/*  58:    */     }
/*  59: 96 */     ChannelFactory<? extends C> factory = channelFactory();
/*  60: 97 */     if (factory != null) {
/*  61:100 */       buf.append("channelFactory: ").append(factory).append(", ");
/*  62:    */     }
/*  63:102 */     SocketAddress localAddress = localAddress();
/*  64:103 */     if (localAddress != null) {
/*  65:106 */       buf.append("localAddress: ").append(localAddress).append(", ");
/*  66:    */     }
/*  67:109 */     Map<ChannelOption<?>, Object> options = options();
/*  68:110 */     if (!options.isEmpty()) {
/*  69:113 */       buf.append("options: ").append(options).append(", ");
/*  70:    */     }
/*  71:115 */     Map<AttributeKey<?>, Object> attrs = attrs();
/*  72:116 */     if (!attrs.isEmpty()) {
/*  73:119 */       buf.append("attrs: ").append(attrs).append(", ");
/*  74:    */     }
/*  75:121 */     ChannelHandler handler = handler();
/*  76:122 */     if (handler != null) {
/*  77:125 */       buf.append("handler: ").append(handler).append(", ");
/*  78:    */     }
/*  79:127 */     if (buf.charAt(buf.length() - 1) == '(')
/*  80:    */     {
/*  81:128 */       buf.append(')');
/*  82:    */     }
/*  83:    */     else
/*  84:    */     {
/*  85:130 */       buf.setCharAt(buf.length() - 2, ')');
/*  86:131 */       buf.setLength(buf.length() - 1);
/*  87:    */     }
/*  88:133 */     return buf.toString();
/*  89:    */   }
/*  90:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.bootstrap.AbstractBootstrapConfig
 * JD-Core Version:    0.7.0.1
 */