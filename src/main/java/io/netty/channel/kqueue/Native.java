/*   1:    */ package io.netty.channel.kqueue;
/*   2:    */ 
/*   3:    */ import io.netty.channel.unix.Errors;
/*   4:    */ import io.netty.channel.unix.FileDescriptor;
/*   5:    */ import io.netty.channel.unix.Socket;
/*   6:    */ import io.netty.util.internal.NativeLibraryLoader;
/*   7:    */ import io.netty.util.internal.PlatformDependent;
/*   8:    */ import io.netty.util.internal.SystemPropertyUtil;
/*   9:    */ import io.netty.util.internal.ThrowableUtil;
/*  10:    */ import io.netty.util.internal.logging.InternalLogger;
/*  11:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  12:    */ import java.io.IOException;
/*  13:    */ import java.util.Locale;
/*  14:    */ 
/*  15:    */ final class Native
/*  16:    */ {
/*  17: 51 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(Native.class);
/*  18:    */   
/*  19:    */   static
/*  20:    */   {
/*  21:    */     try
/*  22:    */     {
/*  23: 57 */       sizeofKEvent();
/*  24:    */     }
/*  25:    */     catch (UnsatisfiedLinkError ignore)
/*  26:    */     {
/*  27: 60 */       loadNativeLibrary();
/*  28:    */     }
/*  29: 62 */     Socket.initialize();
/*  30:    */   }
/*  31:    */   
/*  32: 65 */   static final short EV_ADD = KQueueStaticallyReferencedJniMethods.evAdd();
/*  33: 66 */   static final short EV_ENABLE = KQueueStaticallyReferencedJniMethods.evEnable();
/*  34: 67 */   static final short EV_DISABLE = KQueueStaticallyReferencedJniMethods.evDisable();
/*  35: 68 */   static final short EV_DELETE = KQueueStaticallyReferencedJniMethods.evDelete();
/*  36: 69 */   static final short EV_CLEAR = KQueueStaticallyReferencedJniMethods.evClear();
/*  37: 70 */   static final short EV_ERROR = KQueueStaticallyReferencedJniMethods.evError();
/*  38: 71 */   static final short EV_EOF = KQueueStaticallyReferencedJniMethods.evEOF();
/*  39: 73 */   static final int NOTE_READCLOSED = KQueueStaticallyReferencedJniMethods.noteReadClosed();
/*  40: 74 */   static final int NOTE_CONNRESET = KQueueStaticallyReferencedJniMethods.noteConnReset();
/*  41: 75 */   static final int NOTE_DISCONNECTED = KQueueStaticallyReferencedJniMethods.noteDisconnected();
/*  42: 77 */   static final int NOTE_RDHUP = NOTE_READCLOSED | NOTE_CONNRESET | NOTE_DISCONNECTED;
/*  43: 80 */   static final short EV_ADD_CLEAR_ENABLE = (short)(EV_ADD | EV_CLEAR | EV_ENABLE);
/*  44: 81 */   static final short EV_DELETE_DISABLE = (short)(EV_DELETE | EV_DISABLE);
/*  45: 83 */   static final short EVFILT_READ = KQueueStaticallyReferencedJniMethods.evfiltRead();
/*  46: 84 */   static final short EVFILT_WRITE = KQueueStaticallyReferencedJniMethods.evfiltWrite();
/*  47: 85 */   static final short EVFILT_USER = KQueueStaticallyReferencedJniMethods.evfiltUser();
/*  48: 86 */   static final short EVFILT_SOCK = KQueueStaticallyReferencedJniMethods.evfiltSock();
/*  49:    */   
/*  50:    */   static FileDescriptor newKQueue()
/*  51:    */   {
/*  52: 89 */     return new FileDescriptor(kqueueCreate());
/*  53:    */   }
/*  54:    */   
/*  55:    */   static int keventWait(int kqueueFd, KQueueEventArray changeList, KQueueEventArray eventList, int tvSec, int tvNsec)
/*  56:    */     throws IOException
/*  57:    */   {
/*  58: 94 */     int ready = keventWait(kqueueFd, changeList.memoryAddress(), changeList.size(), eventList
/*  59: 95 */       .memoryAddress(), eventList.capacity(), tvSec, tvNsec);
/*  60: 96 */     if (ready < 0) {
/*  61: 97 */       throw Errors.newIOException("kevent", ready);
/*  62:    */     }
/*  63: 99 */     return ready;
/*  64:    */   }
/*  65:    */   
/*  66:    */   private static void loadNativeLibrary()
/*  67:    */   {
/*  68:117 */     String name = SystemPropertyUtil.get("os.name").toLowerCase(Locale.UK).trim();
/*  69:118 */     if ((!name.startsWith("mac")) && (!name.contains("bsd")) && (!name.startsWith("darwin"))) {
/*  70:119 */       throw new IllegalStateException("Only supported on BSD");
/*  71:    */     }
/*  72:121 */     String staticLibName = "netty_transport_native_kqueue";
/*  73:122 */     String sharedLibName = staticLibName + '_' + PlatformDependent.normalizedArch();
/*  74:123 */     ClassLoader cl = PlatformDependent.getClassLoader(Native.class);
/*  75:    */     try
/*  76:    */     {
/*  77:125 */       NativeLibraryLoader.load(sharedLibName, cl);
/*  78:    */     }
/*  79:    */     catch (UnsatisfiedLinkError e1)
/*  80:    */     {
/*  81:    */       try
/*  82:    */       {
/*  83:128 */         NativeLibraryLoader.load(staticLibName, cl);
/*  84:129 */         logger.debug("Failed to load {}", sharedLibName, e1);
/*  85:    */       }
/*  86:    */       catch (UnsatisfiedLinkError e2)
/*  87:    */       {
/*  88:131 */         ThrowableUtil.addSuppressed(e1, e2);
/*  89:132 */         throw e1;
/*  90:    */       }
/*  91:    */     }
/*  92:    */   }
/*  93:    */   
/*  94:    */   private static native int kqueueCreate();
/*  95:    */   
/*  96:    */   private static native int keventWait(int paramInt1, long paramLong1, int paramInt2, long paramLong2, int paramInt3, int paramInt4, int paramInt5);
/*  97:    */   
/*  98:    */   static native int keventTriggerUserEvent(int paramInt1, int paramInt2);
/*  99:    */   
/* 100:    */   static native int keventAddUserEvent(int paramInt1, int paramInt2);
/* 101:    */   
/* 102:    */   static native int sizeofKEvent();
/* 103:    */   
/* 104:    */   static native int offsetofKEventIdent();
/* 105:    */   
/* 106:    */   static native int offsetofKEventFlags();
/* 107:    */   
/* 108:    */   static native int offsetofKEventFFlags();
/* 109:    */   
/* 110:    */   static native int offsetofKEventFilter();
/* 111:    */   
/* 112:    */   static native int offsetofKeventData();
/* 113:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.kqueue.Native
 * JD-Core Version:    0.7.0.1
 */