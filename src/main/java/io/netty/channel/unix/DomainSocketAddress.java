/*  1:   */ package io.netty.channel.unix;
/*  2:   */ 
/*  3:   */ import java.io.File;
/*  4:   */ import java.net.SocketAddress;
/*  5:   */ 
/*  6:   */ public final class DomainSocketAddress
/*  7:   */   extends SocketAddress
/*  8:   */ {
/*  9:   */   private static final long serialVersionUID = -6934618000832236893L;
/* 10:   */   private final String socketPath;
/* 11:   */   
/* 12:   */   public DomainSocketAddress(String socketPath)
/* 13:   */   {
/* 14:30 */     if (socketPath == null) {
/* 15:31 */       throw new NullPointerException("socketPath");
/* 16:   */     }
/* 17:33 */     this.socketPath = socketPath;
/* 18:   */   }
/* 19:   */   
/* 20:   */   public DomainSocketAddress(File file)
/* 21:   */   {
/* 22:37 */     this(file.getPath());
/* 23:   */   }
/* 24:   */   
/* 25:   */   public String path()
/* 26:   */   {
/* 27:44 */     return this.socketPath;
/* 28:   */   }
/* 29:   */   
/* 30:   */   public String toString()
/* 31:   */   {
/* 32:49 */     return path();
/* 33:   */   }
/* 34:   */   
/* 35:   */   public boolean equals(Object o)
/* 36:   */   {
/* 37:54 */     if (this == o) {
/* 38:55 */       return true;
/* 39:   */     }
/* 40:57 */     if (!(o instanceof DomainSocketAddress)) {
/* 41:58 */       return false;
/* 42:   */     }
/* 43:61 */     return ((DomainSocketAddress)o).socketPath.equals(this.socketPath);
/* 44:   */   }
/* 45:   */   
/* 46:   */   public int hashCode()
/* 47:   */   {
/* 48:66 */     return this.socketPath.hashCode();
/* 49:   */   }
/* 50:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.unix.DomainSocketAddress
 * JD-Core Version:    0.7.0.1
 */