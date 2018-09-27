/*   1:    */ package io.netty.channel.epoll;
/*   2:    */ 
/*   3:    */ import io.netty.channel.unix.Errors;
/*   4:    */ import io.netty.channel.unix.Errors.NativeIoException;
/*   5:    */ import io.netty.channel.unix.FileDescriptor;
/*   6:    */ import io.netty.channel.unix.Socket;
/*   7:    */ import io.netty.util.internal.NativeLibraryLoader;
/*   8:    */ import io.netty.util.internal.PlatformDependent;
/*   9:    */ import io.netty.util.internal.SystemPropertyUtil;
/*  10:    */ import io.netty.util.internal.ThrowableUtil;
/*  11:    */ import io.netty.util.internal.logging.InternalLogger;
/*  12:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  13:    */ import java.io.IOException;
/*  14:    */ import java.nio.channels.ClosedChannelException;
/*  15:    */ import java.util.Locale;
/*  16:    */ 
/*  17:    */ public final class Native
/*  18:    */ {
/*  19: 52 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(Native.class);
/*  20:    */   public static final int EPOLLIN;
/*  21:    */   public static final int EPOLLOUT;
/*  22:    */   public static final int EPOLLRDHUP;
/*  23:    */   public static final int EPOLLET;
/*  24:    */   public static final int EPOLLERR;
/*  25:    */   public static final boolean IS_SUPPORTING_SENDMMSG;
/*  26:    */   public static final boolean IS_SUPPORTING_TCP_FASTOPEN;
/*  27:    */   public static final int TCP_MD5SIG_MAXKEYLEN;
/*  28:    */   public static final String KERNEL_VERSION;
/*  29:    */   
/*  30:    */   static
/*  31:    */   {
/*  32:    */     try
/*  33:    */     {
/*  34: 58 */       offsetofEpollData();
/*  35:    */     }
/*  36:    */     catch (UnsatisfiedLinkError ignore)
/*  37:    */     {
/*  38: 61 */       loadNativeLibrary();
/*  39:    */     }
/*  40: 63 */     Socket.initialize();
/*  41:    */     
/*  42:    */ 
/*  43:    */ 
/*  44: 67 */     EPOLLIN = NativeStaticallyReferencedJniMethods.epollin();
/*  45: 68 */     EPOLLOUT = NativeStaticallyReferencedJniMethods.epollout();
/*  46: 69 */     EPOLLRDHUP = NativeStaticallyReferencedJniMethods.epollrdhup();
/*  47: 70 */     EPOLLET = NativeStaticallyReferencedJniMethods.epollet();
/*  48: 71 */     EPOLLERR = NativeStaticallyReferencedJniMethods.epollerr();
/*  49:    */     
/*  50: 73 */     IS_SUPPORTING_SENDMMSG = NativeStaticallyReferencedJniMethods.isSupportingSendmmsg();
/*  51: 74 */     IS_SUPPORTING_TCP_FASTOPEN = NativeStaticallyReferencedJniMethods.isSupportingTcpFastopen();
/*  52: 75 */     TCP_MD5SIG_MAXKEYLEN = NativeStaticallyReferencedJniMethods.tcpMd5SigMaxKeyLen();
/*  53: 76 */     KERNEL_VERSION = NativeStaticallyReferencedJniMethods.kernelVersion();
/*  54:    */     
/*  55:    */ 
/*  56:    */ 
/*  57: 80 */     SENDMMSG_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), Native.class, "sendmmsg(...)");
/*  58:    */     
/*  59: 82 */     SPLICE_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), Native.class, "splice(...)");
/*  60:    */   }
/*  61:    */   
/*  62: 86 */   private static final Errors.NativeIoException SENDMMSG_CONNECTION_RESET_EXCEPTION = Errors.newConnectionResetException("syscall:sendmmsg(...)", Errors.ERRNO_EPIPE_NEGATIVE);
/*  63: 88 */   private static final Errors.NativeIoException SPLICE_CONNECTION_RESET_EXCEPTION = Errors.newConnectionResetException("syscall:splice(...)", Errors.ERRNO_EPIPE_NEGATIVE);
/*  64:    */   private static final ClosedChannelException SENDMMSG_CLOSED_CHANNEL_EXCEPTION;
/*  65:    */   private static final ClosedChannelException SPLICE_CLOSED_CHANNEL_EXCEPTION;
/*  66:    */   
/*  67:    */   public static FileDescriptor newEventFd()
/*  68:    */   {
/*  69: 93 */     return new FileDescriptor(eventFd());
/*  70:    */   }
/*  71:    */   
/*  72:    */   public static FileDescriptor newTimerFd()
/*  73:    */   {
/*  74: 97 */     return new FileDescriptor(timerFd());
/*  75:    */   }
/*  76:    */   
/*  77:    */   public static FileDescriptor newEpollCreate()
/*  78:    */   {
/*  79:107 */     return new FileDescriptor(epollCreate());
/*  80:    */   }
/*  81:    */   
/*  82:    */   public static int epollWait(FileDescriptor epollFd, EpollEventArray events, FileDescriptor timerFd, int timeoutSec, int timeoutNs)
/*  83:    */     throws IOException
/*  84:    */   {
/*  85:114 */     int ready = epollWait0(epollFd.intValue(), events.memoryAddress(), events.length(), timerFd.intValue(), timeoutSec, timeoutNs);
/*  86:116 */     if (ready < 0) {
/*  87:117 */       throw Errors.newIOException("epoll_wait", ready);
/*  88:    */     }
/*  89:119 */     return ready;
/*  90:    */   }
/*  91:    */   
/*  92:    */   public static void epollCtlAdd(int efd, int fd, int flags)
/*  93:    */     throws IOException
/*  94:    */   {
/*  95:124 */     int res = epollCtlAdd0(efd, fd, flags);
/*  96:125 */     if (res < 0) {
/*  97:126 */       throw Errors.newIOException("epoll_ctl", res);
/*  98:    */     }
/*  99:    */   }
/* 100:    */   
/* 101:    */   public static void epollCtlMod(int efd, int fd, int flags)
/* 102:    */     throws IOException
/* 103:    */   {
/* 104:132 */     int res = epollCtlMod0(efd, fd, flags);
/* 105:133 */     if (res < 0) {
/* 106:134 */       throw Errors.newIOException("epoll_ctl", res);
/* 107:    */     }
/* 108:    */   }
/* 109:    */   
/* 110:    */   public static void epollCtlDel(int efd, int fd)
/* 111:    */     throws IOException
/* 112:    */   {
/* 113:140 */     int res = epollCtlDel0(efd, fd);
/* 114:141 */     if (res < 0) {
/* 115:142 */       throw Errors.newIOException("epoll_ctl", res);
/* 116:    */     }
/* 117:    */   }
/* 118:    */   
/* 119:    */   public static int splice(int fd, long offIn, int fdOut, long offOut, long len)
/* 120:    */     throws IOException
/* 121:    */   {
/* 122:149 */     int res = splice0(fd, offIn, fdOut, offOut, len);
/* 123:150 */     if (res >= 0) {
/* 124:151 */       return res;
/* 125:    */     }
/* 126:153 */     return Errors.ioResult("splice", res, SPLICE_CONNECTION_RESET_EXCEPTION, SPLICE_CLOSED_CHANNEL_EXCEPTION);
/* 127:    */   }
/* 128:    */   
/* 129:    */   public static int sendmmsg(int fd, NativeDatagramPacketArray.NativeDatagramPacket[] msgs, int offset, int len)
/* 130:    */     throws IOException
/* 131:    */   {
/* 132:160 */     int res = sendmmsg0(fd, msgs, offset, len);
/* 133:161 */     if (res >= 0) {
/* 134:162 */       return res;
/* 135:    */     }
/* 136:164 */     return Errors.ioResult("sendmmsg", res, SENDMMSG_CONNECTION_RESET_EXCEPTION, SENDMMSG_CLOSED_CHANNEL_EXCEPTION);
/* 137:    */   }
/* 138:    */   
/* 139:    */   private static void loadNativeLibrary()
/* 140:    */   {
/* 141:175 */     String name = SystemPropertyUtil.get("os.name").toLowerCase(Locale.UK).trim();
/* 142:176 */     if (!name.startsWith("linux")) {
/* 143:177 */       throw new IllegalStateException("Only supported on Linux");
/* 144:    */     }
/* 145:179 */     String staticLibName = "netty_transport_native_epoll";
/* 146:180 */     String sharedLibName = staticLibName + '_' + PlatformDependent.normalizedArch();
/* 147:181 */     ClassLoader cl = PlatformDependent.getClassLoader(Native.class);
/* 148:    */     try
/* 149:    */     {
/* 150:183 */       NativeLibraryLoader.load(sharedLibName, cl);
/* 151:    */     }
/* 152:    */     catch (UnsatisfiedLinkError e1)
/* 153:    */     {
/* 154:    */       try
/* 155:    */       {
/* 156:186 */         NativeLibraryLoader.load(staticLibName, cl);
/* 157:187 */         logger.debug("Failed to load {}", sharedLibName, e1);
/* 158:    */       }
/* 159:    */       catch (UnsatisfiedLinkError e2)
/* 160:    */       {
/* 161:189 */         ThrowableUtil.addSuppressed(e1, e2);
/* 162:190 */         throw e1;
/* 163:    */       }
/* 164:    */     }
/* 165:    */   }
/* 166:    */   
/* 167:    */   private static native int eventFd();
/* 168:    */   
/* 169:    */   private static native int timerFd();
/* 170:    */   
/* 171:    */   public static native void eventFdWrite(int paramInt, long paramLong);
/* 172:    */   
/* 173:    */   public static native void eventFdRead(int paramInt);
/* 174:    */   
/* 175:    */   static native void timerFdRead(int paramInt);
/* 176:    */   
/* 177:    */   private static native int epollCreate();
/* 178:    */   
/* 179:    */   private static native int epollWait0(int paramInt1, long paramLong, int paramInt2, int paramInt3, int paramInt4, int paramInt5);
/* 180:    */   
/* 181:    */   private static native int epollCtlAdd0(int paramInt1, int paramInt2, int paramInt3);
/* 182:    */   
/* 183:    */   private static native int epollCtlMod0(int paramInt1, int paramInt2, int paramInt3);
/* 184:    */   
/* 185:    */   private static native int epollCtlDel0(int paramInt1, int paramInt2);
/* 186:    */   
/* 187:    */   private static native int splice0(int paramInt1, long paramLong1, int paramInt2, long paramLong2, long paramLong3);
/* 188:    */   
/* 189:    */   private static native int sendmmsg0(int paramInt1, NativeDatagramPacketArray.NativeDatagramPacket[] paramArrayOfNativeDatagramPacket, int paramInt2, int paramInt3);
/* 190:    */   
/* 191:    */   public static native int sizeofEpollEvent();
/* 192:    */   
/* 193:    */   public static native int offsetofEpollData();
/* 194:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.epoll.Native
 * JD-Core Version:    0.7.0.1
 */