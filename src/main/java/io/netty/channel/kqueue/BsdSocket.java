/*   1:    */ package io.netty.channel.kqueue;
/*   2:    */ 
/*   3:    */ import io.netty.channel.DefaultFileRegion;
/*   4:    */ import io.netty.channel.unix.Errors;
/*   5:    */ import io.netty.channel.unix.Errors.NativeIoException;
/*   6:    */ import io.netty.channel.unix.PeerCredentials;
/*   7:    */ import io.netty.channel.unix.Socket;
/*   8:    */ import io.netty.util.internal.ThrowableUtil;
/*   9:    */ import java.io.IOException;
/*  10:    */ import java.nio.channels.ClosedChannelException;
/*  11:    */ 
/*  12:    */ final class BsdSocket
/*  13:    */   extends Socket
/*  14:    */ {
/*  15: 37 */   private static final ClosedChannelException SENDFILE_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), Native.class, "sendfile(..)");
/*  16: 44 */   static final int BSD_SND_LOW_AT_MAX = Math.min(131072, 32768);
/*  17: 47 */   private static final Errors.NativeIoException SENDFILE_CONNECTION_RESET_EXCEPTION = Errors.newConnectionResetException("syscall:sendfile", Errors.ERRNO_EPIPE_NEGATIVE);
/*  18:    */   private static final int APPLE_SND_LOW_AT_MAX = 131072;
/*  19:    */   private static final int FREEBSD_SND_LOW_AT_MAX = 32768;
/*  20:    */   
/*  21:    */   BsdSocket(int fd)
/*  22:    */   {
/*  23: 52 */     super(fd);
/*  24:    */   }
/*  25:    */   
/*  26:    */   void setAcceptFilter(AcceptFilter acceptFilter)
/*  27:    */     throws IOException
/*  28:    */   {
/*  29: 56 */     setAcceptFilter(intValue(), acceptFilter.filterName(), acceptFilter.filterArgs());
/*  30:    */   }
/*  31:    */   
/*  32:    */   void setTcpNoPush(boolean tcpNoPush)
/*  33:    */     throws IOException
/*  34:    */   {
/*  35: 60 */     setTcpNoPush(intValue(), tcpNoPush ? 1 : 0);
/*  36:    */   }
/*  37:    */   
/*  38:    */   void setSndLowAt(int lowAt)
/*  39:    */     throws IOException
/*  40:    */   {
/*  41: 64 */     setSndLowAt(intValue(), lowAt);
/*  42:    */   }
/*  43:    */   
/*  44:    */   boolean isTcpNoPush()
/*  45:    */     throws IOException
/*  46:    */   {
/*  47: 68 */     return getTcpNoPush(intValue()) != 0;
/*  48:    */   }
/*  49:    */   
/*  50:    */   int getSndLowAt()
/*  51:    */     throws IOException
/*  52:    */   {
/*  53: 72 */     return getSndLowAt(intValue());
/*  54:    */   }
/*  55:    */   
/*  56:    */   AcceptFilter getAcceptFilter()
/*  57:    */     throws IOException
/*  58:    */   {
/*  59: 76 */     String[] result = getAcceptFilter(intValue());
/*  60: 77 */     return result == null ? AcceptFilter.PLATFORM_UNSUPPORTED : new AcceptFilter(result[0], result[1]);
/*  61:    */   }
/*  62:    */   
/*  63:    */   PeerCredentials getPeerCredentials()
/*  64:    */     throws IOException
/*  65:    */   {
/*  66: 81 */     return getPeerCredentials(intValue());
/*  67:    */   }
/*  68:    */   
/*  69:    */   long sendFile(DefaultFileRegion src, long baseOffset, long offset, long length)
/*  70:    */     throws IOException
/*  71:    */   {
/*  72: 87 */     src.open();
/*  73:    */     
/*  74: 89 */     long res = sendFile(intValue(), src, baseOffset, offset, length);
/*  75: 90 */     if (res >= 0L) {
/*  76: 91 */       return res;
/*  77:    */     }
/*  78: 93 */     return Errors.ioResult("sendfile", (int)res, SENDFILE_CONNECTION_RESET_EXCEPTION, SENDFILE_CLOSED_CHANNEL_EXCEPTION);
/*  79:    */   }
/*  80:    */   
/*  81:    */   public static BsdSocket newSocketStream()
/*  82:    */   {
/*  83: 97 */     return new BsdSocket(newSocketStream0());
/*  84:    */   }
/*  85:    */   
/*  86:    */   public static BsdSocket newSocketDgram()
/*  87:    */   {
/*  88:101 */     return new BsdSocket(newSocketDgram0());
/*  89:    */   }
/*  90:    */   
/*  91:    */   public static BsdSocket newSocketDomain()
/*  92:    */   {
/*  93:105 */     return new BsdSocket(newSocketDomain0());
/*  94:    */   }
/*  95:    */   
/*  96:    */   private static native long sendFile(int paramInt, DefaultFileRegion paramDefaultFileRegion, long paramLong1, long paramLong2, long paramLong3)
/*  97:    */     throws IOException;
/*  98:    */   
/*  99:    */   private static native String[] getAcceptFilter(int paramInt)
/* 100:    */     throws IOException;
/* 101:    */   
/* 102:    */   private static native int getTcpNoPush(int paramInt)
/* 103:    */     throws IOException;
/* 104:    */   
/* 105:    */   private static native int getSndLowAt(int paramInt)
/* 106:    */     throws IOException;
/* 107:    */   
/* 108:    */   private static native PeerCredentials getPeerCredentials(int paramInt)
/* 109:    */     throws IOException;
/* 110:    */   
/* 111:    */   private static native void setAcceptFilter(int paramInt, String paramString1, String paramString2)
/* 112:    */     throws IOException;
/* 113:    */   
/* 114:    */   private static native void setTcpNoPush(int paramInt1, int paramInt2)
/* 115:    */     throws IOException;
/* 116:    */   
/* 117:    */   private static native void setSndLowAt(int paramInt1, int paramInt2)
/* 118:    */     throws IOException;
/* 119:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.kqueue.BsdSocket
 * JD-Core Version:    0.7.0.1
 */