/*  1:   */ package io.netty.channel.local;
/*  2:   */ 
/*  3:   */ import io.netty.channel.Channel;
/*  4:   */ import java.net.SocketAddress;
/*  5:   */ 
/*  6:   */ public final class LocalAddress
/*  7:   */   extends SocketAddress
/*  8:   */   implements Comparable<LocalAddress>
/*  9:   */ {
/* 10:   */   private static final long serialVersionUID = 4644331421130916435L;
/* 11:30 */   public static final LocalAddress ANY = new LocalAddress("ANY");
/* 12:   */   private final String id;
/* 13:   */   private final String strVal;
/* 14:   */   
/* 15:   */   LocalAddress(Channel channel)
/* 16:   */   {
/* 17:41 */     StringBuilder buf = new StringBuilder(16);
/* 18:42 */     buf.append("local:E");
/* 19:43 */     buf.append(Long.toHexString(channel.hashCode() & 0xFFFFFFFF | 0x0));
/* 20:44 */     buf.setCharAt(7, ':');
/* 21:45 */     this.id = buf.substring(6);
/* 22:46 */     this.strVal = buf.toString();
/* 23:   */   }
/* 24:   */   
/* 25:   */   public LocalAddress(String id)
/* 26:   */   {
/* 27:53 */     if (id == null) {
/* 28:54 */       throw new NullPointerException("id");
/* 29:   */     }
/* 30:56 */     id = id.trim().toLowerCase();
/* 31:57 */     if (id.isEmpty()) {
/* 32:58 */       throw new IllegalArgumentException("empty id");
/* 33:   */     }
/* 34:60 */     this.id = id;
/* 35:61 */     this.strVal = ("local:" + id);
/* 36:   */   }
/* 37:   */   
/* 38:   */   public String id()
/* 39:   */   {
/* 40:68 */     return this.id;
/* 41:   */   }
/* 42:   */   
/* 43:   */   public int hashCode()
/* 44:   */   {
/* 45:73 */     return this.id.hashCode();
/* 46:   */   }
/* 47:   */   
/* 48:   */   public boolean equals(Object o)
/* 49:   */   {
/* 50:78 */     if (!(o instanceof LocalAddress)) {
/* 51:79 */       return false;
/* 52:   */     }
/* 53:82 */     return this.id.equals(((LocalAddress)o).id);
/* 54:   */   }
/* 55:   */   
/* 56:   */   public int compareTo(LocalAddress o)
/* 57:   */   {
/* 58:87 */     return this.id.compareTo(o.id);
/* 59:   */   }
/* 60:   */   
/* 61:   */   public String toString()
/* 62:   */   {
/* 63:92 */     return this.strVal;
/* 64:   */   }
/* 65:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.local.LocalAddress
 * JD-Core Version:    0.7.0.1
 */