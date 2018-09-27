/*  1:   */ package io.netty.channel.unix;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.EmptyArrays;
/*  4:   */ 
/*  5:   */ public final class PeerCredentials
/*  6:   */ {
/*  7:   */   private final int pid;
/*  8:   */   private final int uid;
/*  9:   */   private final int[] gids;
/* 10:   */   
/* 11:   */   PeerCredentials(int p, int u, int... gids)
/* 12:   */   {
/* 13:38 */     this.pid = p;
/* 14:39 */     this.uid = u;
/* 15:40 */     this.gids = (gids == null ? EmptyArrays.EMPTY_INTS : gids);
/* 16:   */   }
/* 17:   */   
/* 18:   */   public int pid()
/* 19:   */   {
/* 20:50 */     return this.pid;
/* 21:   */   }
/* 22:   */   
/* 23:   */   public int uid()
/* 24:   */   {
/* 25:54 */     return this.uid;
/* 26:   */   }
/* 27:   */   
/* 28:   */   public int[] gids()
/* 29:   */   {
/* 30:58 */     return (int[])this.gids.clone();
/* 31:   */   }
/* 32:   */   
/* 33:   */   public String toString()
/* 34:   */   {
/* 35:63 */     StringBuilder sb = new StringBuilder(128);
/* 36:64 */     sb.append("UserCredentials[pid=").append(this.pid).append("; uid=").append(this.uid).append("; gids=[");
/* 37:65 */     if (this.gids.length > 0)
/* 38:   */     {
/* 39:66 */       sb.append(this.gids[0]);
/* 40:67 */       for (int i = 1; i < this.gids.length; i++) {
/* 41:68 */         sb.append(", ").append(this.gids[i]);
/* 42:   */       }
/* 43:   */     }
/* 44:71 */     sb.append(']');
/* 45:72 */     return sb.toString();
/* 46:   */   }
/* 47:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.unix.PeerCredentials
 * JD-Core Version:    0.7.0.1
 */