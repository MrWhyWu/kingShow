/*   1:    */ package io.netty.channel.epoll;
/*   2:    */ 
/*   3:    */ import io.netty.channel.DefaultFileRegion;
/*   4:    */ import io.netty.channel.unix.Errors;
/*   5:    */ import io.netty.channel.unix.Errors.NativeIoException;
/*   6:    */ import io.netty.channel.unix.NativeInetAddress;
/*   7:    */ import io.netty.channel.unix.PeerCredentials;
/*   8:    */ import io.netty.channel.unix.Socket;
/*   9:    */ import io.netty.util.internal.ThrowableUtil;
/*  10:    */ import java.io.IOException;
/*  11:    */ import java.net.InetAddress;
/*  12:    */ import java.nio.channels.ClosedChannelException;
/*  13:    */ 
/*  14:    */ final class LinuxSocket
/*  15:    */   extends Socket
/*  16:    */ {
/*  17:    */   private static final long MAX_UINT32_T = 4294967295L;
/*  18: 39 */   private static final Errors.NativeIoException SENDFILE_CONNECTION_RESET_EXCEPTION = Errors.newConnectionResetException("syscall:sendfile(...)", Errors.ERRNO_EPIPE_NEGATIVE);
/*  19: 40 */   private static final ClosedChannelException SENDFILE_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), Native.class, "sendfile(...)");
/*  20:    */   
/*  21:    */   public LinuxSocket(int fd)
/*  22:    */   {
/*  23: 44 */     super(fd);
/*  24:    */   }
/*  25:    */   
/*  26:    */   void setTcpDeferAccept(int deferAccept)
/*  27:    */     throws IOException
/*  28:    */   {
/*  29: 48 */     setTcpDeferAccept(intValue(), deferAccept);
/*  30:    */   }
/*  31:    */   
/*  32:    */   void setTcpQuickAck(boolean quickAck)
/*  33:    */     throws IOException
/*  34:    */   {
/*  35: 52 */     setTcpQuickAck(intValue(), quickAck ? 1 : 0);
/*  36:    */   }
/*  37:    */   
/*  38:    */   void setTcpCork(boolean tcpCork)
/*  39:    */     throws IOException
/*  40:    */   {
/*  41: 56 */     setTcpCork(intValue(), tcpCork ? 1 : 0);
/*  42:    */   }
/*  43:    */   
/*  44:    */   void setTcpNotSentLowAt(long tcpNotSentLowAt)
/*  45:    */     throws IOException
/*  46:    */   {
/*  47: 60 */     if ((tcpNotSentLowAt < 0L) || (tcpNotSentLowAt > 4294967295L)) {
/*  48: 61 */       throw new IllegalArgumentException("tcpNotSentLowAt must be a uint32_t");
/*  49:    */     }
/*  50: 63 */     setTcpNotSentLowAt(intValue(), (int)tcpNotSentLowAt);
/*  51:    */   }
/*  52:    */   
/*  53:    */   void setTcpFastOpen(int tcpFastopenBacklog)
/*  54:    */     throws IOException
/*  55:    */   {
/*  56: 67 */     setTcpFastOpen(intValue(), tcpFastopenBacklog);
/*  57:    */   }
/*  58:    */   
/*  59:    */   void setTcpFastOpenConnect(boolean tcpFastOpenConnect)
/*  60:    */     throws IOException
/*  61:    */   {
/*  62: 71 */     setTcpFastOpenConnect(intValue(), tcpFastOpenConnect ? 1 : 0);
/*  63:    */   }
/*  64:    */   
/*  65:    */   boolean isTcpFastOpenConnect()
/*  66:    */     throws IOException
/*  67:    */   {
/*  68: 75 */     return isTcpFastOpenConnect(intValue()) != 0;
/*  69:    */   }
/*  70:    */   
/*  71:    */   void setTcpKeepIdle(int seconds)
/*  72:    */     throws IOException
/*  73:    */   {
/*  74: 79 */     setTcpKeepIdle(intValue(), seconds);
/*  75:    */   }
/*  76:    */   
/*  77:    */   void setTcpKeepIntvl(int seconds)
/*  78:    */     throws IOException
/*  79:    */   {
/*  80: 83 */     setTcpKeepIntvl(intValue(), seconds);
/*  81:    */   }
/*  82:    */   
/*  83:    */   void setTcpKeepCnt(int probes)
/*  84:    */     throws IOException
/*  85:    */   {
/*  86: 87 */     setTcpKeepCnt(intValue(), probes);
/*  87:    */   }
/*  88:    */   
/*  89:    */   void setTcpUserTimeout(int milliseconds)
/*  90:    */     throws IOException
/*  91:    */   {
/*  92: 91 */     setTcpUserTimeout(intValue(), milliseconds);
/*  93:    */   }
/*  94:    */   
/*  95:    */   void setIpFreeBind(boolean enabled)
/*  96:    */     throws IOException
/*  97:    */   {
/*  98: 95 */     setIpFreeBind(intValue(), enabled ? 1 : 0);
/*  99:    */   }
/* 100:    */   
/* 101:    */   void setIpTransparent(boolean enabled)
/* 102:    */     throws IOException
/* 103:    */   {
/* 104: 99 */     setIpTransparent(intValue(), enabled ? 1 : 0);
/* 105:    */   }
/* 106:    */   
/* 107:    */   void getTcpInfo(EpollTcpInfo info)
/* 108:    */     throws IOException
/* 109:    */   {
/* 110:103 */     getTcpInfo(intValue(), info.info);
/* 111:    */   }
/* 112:    */   
/* 113:    */   void setTcpMd5Sig(InetAddress address, byte[] key)
/* 114:    */     throws IOException
/* 115:    */   {
/* 116:107 */     NativeInetAddress a = NativeInetAddress.newInstance(address);
/* 117:108 */     setTcpMd5Sig(intValue(), a.address(), a.scopeId(), key);
/* 118:    */   }
/* 119:    */   
/* 120:    */   boolean isTcpCork()
/* 121:    */     throws IOException
/* 122:    */   {
/* 123:112 */     return isTcpCork(intValue()) != 0;
/* 124:    */   }
/* 125:    */   
/* 126:    */   int getTcpDeferAccept()
/* 127:    */     throws IOException
/* 128:    */   {
/* 129:116 */     return getTcpDeferAccept(intValue());
/* 130:    */   }
/* 131:    */   
/* 132:    */   boolean isTcpQuickAck()
/* 133:    */     throws IOException
/* 134:    */   {
/* 135:120 */     return isTcpQuickAck(intValue()) != 0;
/* 136:    */   }
/* 137:    */   
/* 138:    */   long getTcpNotSentLowAt()
/* 139:    */     throws IOException
/* 140:    */   {
/* 141:124 */     return getTcpNotSentLowAt(intValue()) & 0xFFFFFFFF;
/* 142:    */   }
/* 143:    */   
/* 144:    */   int getTcpKeepIdle()
/* 145:    */     throws IOException
/* 146:    */   {
/* 147:128 */     return getTcpKeepIdle(intValue());
/* 148:    */   }
/* 149:    */   
/* 150:    */   int getTcpKeepIntvl()
/* 151:    */     throws IOException
/* 152:    */   {
/* 153:132 */     return getTcpKeepIntvl(intValue());
/* 154:    */   }
/* 155:    */   
/* 156:    */   int getTcpKeepCnt()
/* 157:    */     throws IOException
/* 158:    */   {
/* 159:136 */     return getTcpKeepCnt(intValue());
/* 160:    */   }
/* 161:    */   
/* 162:    */   int getTcpUserTimeout()
/* 163:    */     throws IOException
/* 164:    */   {
/* 165:140 */     return getTcpUserTimeout(intValue());
/* 166:    */   }
/* 167:    */   
/* 168:    */   boolean isIpFreeBind()
/* 169:    */     throws IOException
/* 170:    */   {
/* 171:144 */     return isIpFreeBind(intValue()) != 0;
/* 172:    */   }
/* 173:    */   
/* 174:    */   boolean isIpTransparent()
/* 175:    */     throws IOException
/* 176:    */   {
/* 177:148 */     return isIpTransparent(intValue()) != 0;
/* 178:    */   }
/* 179:    */   
/* 180:    */   PeerCredentials getPeerCredentials()
/* 181:    */     throws IOException
/* 182:    */   {
/* 183:152 */     return getPeerCredentials(intValue());
/* 184:    */   }
/* 185:    */   
/* 186:    */   long sendFile(DefaultFileRegion src, long baseOffset, long offset, long length)
/* 187:    */     throws IOException
/* 188:    */   {
/* 189:158 */     src.open();
/* 190:    */     
/* 191:160 */     long res = sendFile(intValue(), src, baseOffset, offset, length);
/* 192:161 */     if (res >= 0L) {
/* 193:162 */       return res;
/* 194:    */     }
/* 195:164 */     return Errors.ioResult("sendfile", (int)res, SENDFILE_CONNECTION_RESET_EXCEPTION, SENDFILE_CLOSED_CHANNEL_EXCEPTION);
/* 196:    */   }
/* 197:    */   
/* 198:    */   public static LinuxSocket newSocketStream()
/* 199:    */   {
/* 200:168 */     return new LinuxSocket(newSocketStream0());
/* 201:    */   }
/* 202:    */   
/* 203:    */   public static LinuxSocket newSocketDgram()
/* 204:    */   {
/* 205:172 */     return new LinuxSocket(newSocketDgram0());
/* 206:    */   }
/* 207:    */   
/* 208:    */   public static LinuxSocket newSocketDomain()
/* 209:    */   {
/* 210:176 */     return new LinuxSocket(newSocketDomain0());
/* 211:    */   }
/* 212:    */   
/* 213:    */   private static native long sendFile(int paramInt, DefaultFileRegion paramDefaultFileRegion, long paramLong1, long paramLong2, long paramLong3)
/* 214:    */     throws IOException;
/* 215:    */   
/* 216:    */   private static native int getTcpDeferAccept(int paramInt)
/* 217:    */     throws IOException;
/* 218:    */   
/* 219:    */   private static native int isTcpQuickAck(int paramInt)
/* 220:    */     throws IOException;
/* 221:    */   
/* 222:    */   private static native int isTcpCork(int paramInt)
/* 223:    */     throws IOException;
/* 224:    */   
/* 225:    */   private static native int getTcpNotSentLowAt(int paramInt)
/* 226:    */     throws IOException;
/* 227:    */   
/* 228:    */   private static native int getTcpKeepIdle(int paramInt)
/* 229:    */     throws IOException;
/* 230:    */   
/* 231:    */   private static native int getTcpKeepIntvl(int paramInt)
/* 232:    */     throws IOException;
/* 233:    */   
/* 234:    */   private static native int getTcpKeepCnt(int paramInt)
/* 235:    */     throws IOException;
/* 236:    */   
/* 237:    */   private static native int getTcpUserTimeout(int paramInt)
/* 238:    */     throws IOException;
/* 239:    */   
/* 240:    */   private static native int isIpFreeBind(int paramInt)
/* 241:    */     throws IOException;
/* 242:    */   
/* 243:    */   private static native int isIpTransparent(int paramInt)
/* 244:    */     throws IOException;
/* 245:    */   
/* 246:    */   private static native void getTcpInfo(int paramInt, long[] paramArrayOfLong)
/* 247:    */     throws IOException;
/* 248:    */   
/* 249:    */   private static native PeerCredentials getPeerCredentials(int paramInt)
/* 250:    */     throws IOException;
/* 251:    */   
/* 252:    */   private static native int isTcpFastOpenConnect(int paramInt)
/* 253:    */     throws IOException;
/* 254:    */   
/* 255:    */   private static native void setTcpDeferAccept(int paramInt1, int paramInt2)
/* 256:    */     throws IOException;
/* 257:    */   
/* 258:    */   private static native void setTcpQuickAck(int paramInt1, int paramInt2)
/* 259:    */     throws IOException;
/* 260:    */   
/* 261:    */   private static native void setTcpCork(int paramInt1, int paramInt2)
/* 262:    */     throws IOException;
/* 263:    */   
/* 264:    */   private static native void setTcpNotSentLowAt(int paramInt1, int paramInt2)
/* 265:    */     throws IOException;
/* 266:    */   
/* 267:    */   private static native void setTcpFastOpen(int paramInt1, int paramInt2)
/* 268:    */     throws IOException;
/* 269:    */   
/* 270:    */   private static native void setTcpFastOpenConnect(int paramInt1, int paramInt2)
/* 271:    */     throws IOException;
/* 272:    */   
/* 273:    */   private static native void setTcpKeepIdle(int paramInt1, int paramInt2)
/* 274:    */     throws IOException;
/* 275:    */   
/* 276:    */   private static native void setTcpKeepIntvl(int paramInt1, int paramInt2)
/* 277:    */     throws IOException;
/* 278:    */   
/* 279:    */   private static native void setTcpKeepCnt(int paramInt1, int paramInt2)
/* 280:    */     throws IOException;
/* 281:    */   
/* 282:    */   private static native void setTcpUserTimeout(int paramInt1, int paramInt2)
/* 283:    */     throws IOException;
/* 284:    */   
/* 285:    */   private static native void setIpFreeBind(int paramInt1, int paramInt2)
/* 286:    */     throws IOException;
/* 287:    */   
/* 288:    */   private static native void setIpTransparent(int paramInt1, int paramInt2)
/* 289:    */     throws IOException;
/* 290:    */   
/* 291:    */   private static native void setTcpMd5Sig(int paramInt1, byte[] paramArrayOfByte1, int paramInt2, byte[] paramArrayOfByte2)
/* 292:    */     throws IOException;
/* 293:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.epoll.LinuxSocket
 * JD-Core Version:    0.7.0.1
 */