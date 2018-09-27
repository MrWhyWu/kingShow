/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import io.netty.internal.tcnative.SSL;
/*   4:    */ import io.netty.internal.tcnative.SSLContext;
/*   5:    */ import java.util.concurrent.locks.Lock;
/*   6:    */ import java.util.concurrent.locks.ReadWriteLock;
/*   7:    */ 
/*   8:    */ public final class OpenSslServerSessionContext
/*   9:    */   extends OpenSslSessionContext
/*  10:    */ {
/*  11:    */   OpenSslServerSessionContext(ReferenceCountedOpenSslContext context)
/*  12:    */   {
/*  13: 29 */     super(context);
/*  14:    */   }
/*  15:    */   
/*  16:    */   public void setSessionTimeout(int seconds)
/*  17:    */   {
/*  18: 34 */     if (seconds < 0) {
/*  19: 35 */       throw new IllegalArgumentException();
/*  20:    */     }
/*  21: 37 */     Lock writerLock = this.context.ctxLock.writeLock();
/*  22: 38 */     writerLock.lock();
/*  23:    */     try
/*  24:    */     {
/*  25: 40 */       SSLContext.setSessionCacheTimeout(this.context.ctx, seconds);
/*  26:    */       
/*  27: 42 */       writerLock.unlock();
/*  28:    */     }
/*  29:    */     finally
/*  30:    */     {
/*  31: 42 */       writerLock.unlock();
/*  32:    */     }
/*  33:    */   }
/*  34:    */   
/*  35:    */   public int getSessionTimeout()
/*  36:    */   {
/*  37: 48 */     Lock readerLock = this.context.ctxLock.readLock();
/*  38: 49 */     readerLock.lock();
/*  39:    */     try
/*  40:    */     {
/*  41: 51 */       return (int)SSLContext.getSessionCacheTimeout(this.context.ctx);
/*  42:    */     }
/*  43:    */     finally
/*  44:    */     {
/*  45: 53 */       readerLock.unlock();
/*  46:    */     }
/*  47:    */   }
/*  48:    */   
/*  49:    */   public void setSessionCacheSize(int size)
/*  50:    */   {
/*  51: 59 */     if (size < 0) {
/*  52: 60 */       throw new IllegalArgumentException();
/*  53:    */     }
/*  54: 62 */     Lock writerLock = this.context.ctxLock.writeLock();
/*  55: 63 */     writerLock.lock();
/*  56:    */     try
/*  57:    */     {
/*  58: 65 */       SSLContext.setSessionCacheSize(this.context.ctx, size);
/*  59:    */       
/*  60: 67 */       writerLock.unlock();
/*  61:    */     }
/*  62:    */     finally
/*  63:    */     {
/*  64: 67 */       writerLock.unlock();
/*  65:    */     }
/*  66:    */   }
/*  67:    */   
/*  68:    */   public int getSessionCacheSize()
/*  69:    */   {
/*  70: 73 */     Lock readerLock = this.context.ctxLock.readLock();
/*  71: 74 */     readerLock.lock();
/*  72:    */     try
/*  73:    */     {
/*  74: 76 */       return (int)SSLContext.getSessionCacheSize(this.context.ctx);
/*  75:    */     }
/*  76:    */     finally
/*  77:    */     {
/*  78: 78 */       readerLock.unlock();
/*  79:    */     }
/*  80:    */   }
/*  81:    */   
/*  82:    */   public void setSessionCacheEnabled(boolean enabled)
/*  83:    */   {
/*  84: 84 */     long mode = enabled ? SSL.SSL_SESS_CACHE_SERVER : SSL.SSL_SESS_CACHE_OFF;
/*  85:    */     
/*  86: 86 */     Lock writerLock = this.context.ctxLock.writeLock();
/*  87: 87 */     writerLock.lock();
/*  88:    */     try
/*  89:    */     {
/*  90: 89 */       SSLContext.setSessionCacheMode(this.context.ctx, mode);
/*  91:    */     }
/*  92:    */     finally
/*  93:    */     {
/*  94: 91 */       writerLock.unlock();
/*  95:    */     }
/*  96:    */   }
/*  97:    */   
/*  98:    */   public boolean isSessionCacheEnabled()
/*  99:    */   {
/* 100: 97 */     Lock readerLock = this.context.ctxLock.readLock();
/* 101: 98 */     readerLock.lock();
/* 102:    */     try
/* 103:    */     {
/* 104:100 */       return SSLContext.getSessionCacheMode(this.context.ctx) == SSL.SSL_SESS_CACHE_SERVER;
/* 105:    */     }
/* 106:    */     finally
/* 107:    */     {
/* 108:102 */       readerLock.unlock();
/* 109:    */     }
/* 110:    */   }
/* 111:    */   
/* 112:    */   public boolean setSessionIdContext(byte[] sidCtx)
/* 113:    */   {
/* 114:116 */     Lock writerLock = this.context.ctxLock.writeLock();
/* 115:117 */     writerLock.lock();
/* 116:    */     try
/* 117:    */     {
/* 118:119 */       return SSLContext.setSessionIdContext(this.context.ctx, sidCtx);
/* 119:    */     }
/* 120:    */     finally
/* 121:    */     {
/* 122:121 */       writerLock.unlock();
/* 123:    */     }
/* 124:    */   }
/* 125:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.OpenSslServerSessionContext
 * JD-Core Version:    0.7.0.1
 */