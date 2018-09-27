/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import io.netty.internal.tcnative.SSL;
/*   4:    */ import io.netty.internal.tcnative.SSLContext;
/*   5:    */ import io.netty.internal.tcnative.SessionTicketKey;
/*   6:    */ import io.netty.util.internal.ObjectUtil;
/*   7:    */ import java.util.Arrays;
/*   8:    */ import java.util.Enumeration;
/*   9:    */ import java.util.NoSuchElementException;
/*  10:    */ import java.util.concurrent.locks.Lock;
/*  11:    */ import java.util.concurrent.locks.ReadWriteLock;
/*  12:    */ import javax.net.ssl.SSLSession;
/*  13:    */ import javax.net.ssl.SSLSessionContext;
/*  14:    */ 
/*  15:    */ public abstract class OpenSslSessionContext
/*  16:    */   implements SSLSessionContext
/*  17:    */ {
/*  18: 34 */   private static final Enumeration<byte[]> EMPTY = new EmptyEnumeration(null);
/*  19:    */   private final OpenSslSessionStats stats;
/*  20:    */   final ReferenceCountedOpenSslContext context;
/*  21:    */   
/*  22:    */   OpenSslSessionContext(ReferenceCountedOpenSslContext context)
/*  23:    */   {
/*  24: 44 */     this.context = context;
/*  25: 45 */     this.stats = new OpenSslSessionStats(context);
/*  26:    */   }
/*  27:    */   
/*  28:    */   public SSLSession getSession(byte[] bytes)
/*  29:    */   {
/*  30: 50 */     if (bytes == null) {
/*  31: 51 */       throw new NullPointerException("bytes");
/*  32:    */     }
/*  33: 53 */     return null;
/*  34:    */   }
/*  35:    */   
/*  36:    */   public Enumeration<byte[]> getIds()
/*  37:    */   {
/*  38: 58 */     return EMPTY;
/*  39:    */   }
/*  40:    */   
/*  41:    */   @Deprecated
/*  42:    */   public void setTicketKeys(byte[] keys)
/*  43:    */   {
/*  44: 67 */     if (keys.length % 48 != 0) {
/*  45: 68 */       throw new IllegalArgumentException("keys.length % 48 != 0");
/*  46:    */     }
/*  47: 70 */     SessionTicketKey[] tickets = new SessionTicketKey[keys.length / 48];
/*  48: 71 */     int i = 0;
/*  49: 71 */     for (int a = 0; i < tickets.length; i++)
/*  50:    */     {
/*  51: 72 */       byte[] name = Arrays.copyOfRange(keys, a, 16);
/*  52: 73 */       a += 16;
/*  53: 74 */       byte[] hmacKey = Arrays.copyOfRange(keys, a, 16);
/*  54: 75 */       i += 16;
/*  55: 76 */       byte[] aesKey = Arrays.copyOfRange(keys, a, 16);
/*  56: 77 */       a += 16;
/*  57: 78 */       tickets[i] = new SessionTicketKey(name, hmacKey, aesKey);
/*  58:    */     }
/*  59: 80 */     Lock writerLock = this.context.ctxLock.writeLock();
/*  60: 81 */     writerLock.lock();
/*  61:    */     try
/*  62:    */     {
/*  63: 83 */       SSLContext.clearOptions(this.context.ctx, SSL.SSL_OP_NO_TICKET);
/*  64: 84 */       SSLContext.setSessionTicketKeys(this.context.ctx, tickets);
/*  65:    */     }
/*  66:    */     finally
/*  67:    */     {
/*  68: 86 */       writerLock.unlock();
/*  69:    */     }
/*  70:    */   }
/*  71:    */   
/*  72:    */   public void setTicketKeys(OpenSslSessionTicketKey... keys)
/*  73:    */   {
/*  74: 94 */     ObjectUtil.checkNotNull(keys, "keys");
/*  75: 95 */     SessionTicketKey[] ticketKeys = new SessionTicketKey[keys.length];
/*  76: 96 */     for (int i = 0; i < ticketKeys.length; i++) {
/*  77: 97 */       ticketKeys[i] = keys[i].key;
/*  78:    */     }
/*  79: 99 */     Lock writerLock = this.context.ctxLock.writeLock();
/*  80:100 */     writerLock.lock();
/*  81:    */     try
/*  82:    */     {
/*  83:102 */       SSLContext.clearOptions(this.context.ctx, SSL.SSL_OP_NO_TICKET);
/*  84:103 */       SSLContext.setSessionTicketKeys(this.context.ctx, ticketKeys);
/*  85:    */     }
/*  86:    */     finally
/*  87:    */     {
/*  88:105 */       writerLock.unlock();
/*  89:    */     }
/*  90:    */   }
/*  91:    */   
/*  92:    */   public abstract void setSessionCacheEnabled(boolean paramBoolean);
/*  93:    */   
/*  94:    */   public abstract boolean isSessionCacheEnabled();
/*  95:    */   
/*  96:    */   public OpenSslSessionStats stats()
/*  97:    */   {
/*  98:123 */     return this.stats;
/*  99:    */   }
/* 100:    */   
/* 101:    */   private static final class EmptyEnumeration
/* 102:    */     implements Enumeration<byte[]>
/* 103:    */   {
/* 104:    */     public boolean hasMoreElements()
/* 105:    */     {
/* 106:129 */       return false;
/* 107:    */     }
/* 108:    */     
/* 109:    */     public byte[] nextElement()
/* 110:    */     {
/* 111:134 */       throw new NoSuchElementException();
/* 112:    */     }
/* 113:    */   }
/* 114:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.OpenSslSessionContext
 * JD-Core Version:    0.7.0.1
 */