/*  1:   */ package io.netty.handler.ssl;
/*  2:   */ 
/*  3:   */ import io.netty.internal.tcnative.SessionTicketKey;
/*  4:   */ 
/*  5:   */ public final class OpenSslSessionTicketKey
/*  6:   */ {
/*  7:   */   public static final int NAME_SIZE = 16;
/*  8:   */   public static final int HMAC_KEY_SIZE = 16;
/*  9:   */   public static final int AES_KEY_SIZE = 16;
/* 10:   */   public static final int TICKET_KEY_SIZE = 48;
/* 11:   */   final SessionTicketKey key;
/* 12:   */   
/* 13:   */   public OpenSslSessionTicketKey(byte[] name, byte[] hmacKey, byte[] aesKey)
/* 14:   */   {
/* 15:52 */     this.key = new SessionTicketKey((byte[])name.clone(), (byte[])hmacKey.clone(), (byte[])aesKey.clone());
/* 16:   */   }
/* 17:   */   
/* 18:   */   public byte[] name()
/* 19:   */   {
/* 20:60 */     return (byte[])this.key.getName().clone();
/* 21:   */   }
/* 22:   */   
/* 23:   */   public byte[] hmacKey()
/* 24:   */   {
/* 25:68 */     return (byte[])this.key.getHmacKey().clone();
/* 26:   */   }
/* 27:   */   
/* 28:   */   public byte[] aesKey()
/* 29:   */   {
/* 30:76 */     return (byte[])this.key.getAesKey().clone();
/* 31:   */   }
/* 32:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.OpenSslSessionTicketKey
 * JD-Core Version:    0.7.0.1
 */