/*  1:   */ package io.netty.bootstrap;
/*  2:   */ 
/*  3:   */ import io.netty.channel.Channel;
/*  4:   */ import io.netty.resolver.AddressResolverGroup;
/*  5:   */ import java.net.SocketAddress;
/*  6:   */ 
/*  7:   */ public final class BootstrapConfig
/*  8:   */   extends AbstractBootstrapConfig<Bootstrap, Channel>
/*  9:   */ {
/* 10:   */   BootstrapConfig(Bootstrap bootstrap)
/* 11:   */   {
/* 12:29 */     super(bootstrap);
/* 13:   */   }
/* 14:   */   
/* 15:   */   public SocketAddress remoteAddress()
/* 16:   */   {
/* 17:36 */     return ((Bootstrap)this.bootstrap).remoteAddress();
/* 18:   */   }
/* 19:   */   
/* 20:   */   public AddressResolverGroup<?> resolver()
/* 21:   */   {
/* 22:43 */     return ((Bootstrap)this.bootstrap).resolver();
/* 23:   */   }
/* 24:   */   
/* 25:   */   public String toString()
/* 26:   */   {
/* 27:48 */     StringBuilder buf = new StringBuilder(super.toString());
/* 28:49 */     buf.setLength(buf.length() - 1);
/* 29:50 */     buf.append(", resolver: ").append(resolver());
/* 30:51 */     SocketAddress remoteAddress = remoteAddress();
/* 31:52 */     if (remoteAddress != null) {
/* 32:54 */       buf.append(", remoteAddress: ").append(remoteAddress);
/* 33:   */     }
/* 34:56 */     return ')';
/* 35:   */   }
/* 36:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.bootstrap.BootstrapConfig
 * JD-Core Version:    0.7.0.1
 */