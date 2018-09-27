/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import io.netty.internal.tcnative.SSLContext;
/*   4:    */ import java.util.concurrent.locks.Lock;
/*   5:    */ import java.util.concurrent.locks.ReadWriteLock;
/*   6:    */ 
/*   7:    */ public final class OpenSslSessionStats
/*   8:    */ {
/*   9:    */   private final ReferenceCountedOpenSslContext context;
/*  10:    */   
/*  11:    */   OpenSslSessionStats(ReferenceCountedOpenSslContext context)
/*  12:    */   {
/*  13: 37 */     this.context = context;
/*  14:    */   }
/*  15:    */   
/*  16:    */   public long number()
/*  17:    */   {
/*  18: 44 */     Lock readerLock = this.context.ctxLock.readLock();
/*  19: 45 */     readerLock.lock();
/*  20:    */     try
/*  21:    */     {
/*  22: 47 */       return SSLContext.sessionNumber(this.context.ctx);
/*  23:    */     }
/*  24:    */     finally
/*  25:    */     {
/*  26: 49 */       readerLock.unlock();
/*  27:    */     }
/*  28:    */   }
/*  29:    */   
/*  30:    */   public long connect()
/*  31:    */   {
/*  32: 57 */     Lock readerLock = this.context.ctxLock.readLock();
/*  33: 58 */     readerLock.lock();
/*  34:    */     try
/*  35:    */     {
/*  36: 60 */       return SSLContext.sessionConnect(this.context.ctx);
/*  37:    */     }
/*  38:    */     finally
/*  39:    */     {
/*  40: 62 */       readerLock.unlock();
/*  41:    */     }
/*  42:    */   }
/*  43:    */   
/*  44:    */   public long connectGood()
/*  45:    */   {
/*  46: 70 */     Lock readerLock = this.context.ctxLock.readLock();
/*  47: 71 */     readerLock.lock();
/*  48:    */     try
/*  49:    */     {
/*  50: 73 */       return SSLContext.sessionConnectGood(this.context.ctx);
/*  51:    */     }
/*  52:    */     finally
/*  53:    */     {
/*  54: 75 */       readerLock.unlock();
/*  55:    */     }
/*  56:    */   }
/*  57:    */   
/*  58:    */   public long connectRenegotiate()
/*  59:    */   {
/*  60: 83 */     Lock readerLock = this.context.ctxLock.readLock();
/*  61: 84 */     readerLock.lock();
/*  62:    */     try
/*  63:    */     {
/*  64: 86 */       return SSLContext.sessionConnectRenegotiate(this.context.ctx);
/*  65:    */     }
/*  66:    */     finally
/*  67:    */     {
/*  68: 88 */       readerLock.unlock();
/*  69:    */     }
/*  70:    */   }
/*  71:    */   
/*  72:    */   public long accept()
/*  73:    */   {
/*  74: 96 */     Lock readerLock = this.context.ctxLock.readLock();
/*  75: 97 */     readerLock.lock();
/*  76:    */     try
/*  77:    */     {
/*  78: 99 */       return SSLContext.sessionAccept(this.context.ctx);
/*  79:    */     }
/*  80:    */     finally
/*  81:    */     {
/*  82:101 */       readerLock.unlock();
/*  83:    */     }
/*  84:    */   }
/*  85:    */   
/*  86:    */   public long acceptGood()
/*  87:    */   {
/*  88:109 */     Lock readerLock = this.context.ctxLock.readLock();
/*  89:110 */     readerLock.lock();
/*  90:    */     try
/*  91:    */     {
/*  92:112 */       return SSLContext.sessionAcceptGood(this.context.ctx);
/*  93:    */     }
/*  94:    */     finally
/*  95:    */     {
/*  96:114 */       readerLock.unlock();
/*  97:    */     }
/*  98:    */   }
/*  99:    */   
/* 100:    */   public long acceptRenegotiate()
/* 101:    */   {
/* 102:122 */     Lock readerLock = this.context.ctxLock.readLock();
/* 103:123 */     readerLock.lock();
/* 104:    */     try
/* 105:    */     {
/* 106:125 */       return SSLContext.sessionAcceptRenegotiate(this.context.ctx);
/* 107:    */     }
/* 108:    */     finally
/* 109:    */     {
/* 110:127 */       readerLock.unlock();
/* 111:    */     }
/* 112:    */   }
/* 113:    */   
/* 114:    */   public long hits()
/* 115:    */   {
/* 116:137 */     Lock readerLock = this.context.ctxLock.readLock();
/* 117:138 */     readerLock.lock();
/* 118:    */     try
/* 119:    */     {
/* 120:140 */       return SSLContext.sessionHits(this.context.ctx);
/* 121:    */     }
/* 122:    */     finally
/* 123:    */     {
/* 124:142 */       readerLock.unlock();
/* 125:    */     }
/* 126:    */   }
/* 127:    */   
/* 128:    */   public long cbHits()
/* 129:    */   {
/* 130:150 */     Lock readerLock = this.context.ctxLock.readLock();
/* 131:151 */     readerLock.lock();
/* 132:    */     try
/* 133:    */     {
/* 134:153 */       return SSLContext.sessionCbHits(this.context.ctx);
/* 135:    */     }
/* 136:    */     finally
/* 137:    */     {
/* 138:155 */       readerLock.unlock();
/* 139:    */     }
/* 140:    */   }
/* 141:    */   
/* 142:    */   public long misses()
/* 143:    */   {
/* 144:164 */     Lock readerLock = this.context.ctxLock.readLock();
/* 145:165 */     readerLock.lock();
/* 146:    */     try
/* 147:    */     {
/* 148:167 */       return SSLContext.sessionMisses(this.context.ctx);
/* 149:    */     }
/* 150:    */     finally
/* 151:    */     {
/* 152:169 */       readerLock.unlock();
/* 153:    */     }
/* 154:    */   }
/* 155:    */   
/* 156:    */   public long timeouts()
/* 157:    */   {
/* 158:179 */     Lock readerLock = this.context.ctxLock.readLock();
/* 159:180 */     readerLock.lock();
/* 160:    */     try
/* 161:    */     {
/* 162:182 */       return SSLContext.sessionTimeouts(this.context.ctx);
/* 163:    */     }
/* 164:    */     finally
/* 165:    */     {
/* 166:184 */       readerLock.unlock();
/* 167:    */     }
/* 168:    */   }
/* 169:    */   
/* 170:    */   public long cacheFull()
/* 171:    */   {
/* 172:192 */     Lock readerLock = this.context.ctxLock.readLock();
/* 173:193 */     readerLock.lock();
/* 174:    */     try
/* 175:    */     {
/* 176:195 */       return SSLContext.sessionCacheFull(this.context.ctx);
/* 177:    */     }
/* 178:    */     finally
/* 179:    */     {
/* 180:197 */       readerLock.unlock();
/* 181:    */     }
/* 182:    */   }
/* 183:    */   
/* 184:    */   public long ticketKeyFail()
/* 185:    */   {
/* 186:205 */     Lock readerLock = this.context.ctxLock.readLock();
/* 187:206 */     readerLock.lock();
/* 188:    */     try
/* 189:    */     {
/* 190:208 */       return SSLContext.sessionTicketKeyFail(this.context.ctx);
/* 191:    */     }
/* 192:    */     finally
/* 193:    */     {
/* 194:210 */       readerLock.unlock();
/* 195:    */     }
/* 196:    */   }
/* 197:    */   
/* 198:    */   public long ticketKeyNew()
/* 199:    */   {
/* 200:218 */     Lock readerLock = this.context.ctxLock.readLock();
/* 201:219 */     readerLock.lock();
/* 202:    */     try
/* 203:    */     {
/* 204:221 */       return SSLContext.sessionTicketKeyNew(this.context.ctx);
/* 205:    */     }
/* 206:    */     finally
/* 207:    */     {
/* 208:223 */       readerLock.unlock();
/* 209:    */     }
/* 210:    */   }
/* 211:    */   
/* 212:    */   public long ticketKeyRenew()
/* 213:    */   {
/* 214:232 */     Lock readerLock = this.context.ctxLock.readLock();
/* 215:233 */     readerLock.lock();
/* 216:    */     try
/* 217:    */     {
/* 218:235 */       return SSLContext.sessionTicketKeyRenew(this.context.ctx);
/* 219:    */     }
/* 220:    */     finally
/* 221:    */     {
/* 222:237 */       readerLock.unlock();
/* 223:    */     }
/* 224:    */   }
/* 225:    */   
/* 226:    */   public long ticketKeyResume()
/* 227:    */   {
/* 228:245 */     Lock readerLock = this.context.ctxLock.readLock();
/* 229:246 */     readerLock.lock();
/* 230:    */     try
/* 231:    */     {
/* 232:248 */       return SSLContext.sessionTicketKeyResume(this.context.ctx);
/* 233:    */     }
/* 234:    */     finally
/* 235:    */     {
/* 236:250 */       readerLock.unlock();
/* 237:    */     }
/* 238:    */   }
/* 239:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.OpenSslSessionStats
 * JD-Core Version:    0.7.0.1
 */