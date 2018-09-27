/*   1:    */ package io.netty.channel.unix;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.ObjectUtil;
/*   4:    */ import io.netty.util.internal.ThrowableUtil;
/*   5:    */ import java.io.File;
/*   6:    */ import java.io.IOException;
/*   7:    */ import java.nio.ByteBuffer;
/*   8:    */ import java.nio.channels.ClosedChannelException;
/*   9:    */ import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
/*  10:    */ 
/*  11:    */ public class FileDescriptor
/*  12:    */ {
/*  13: 37 */   private static final ClosedChannelException WRITE_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), FileDescriptor.class, "write(..)");
/*  14: 40 */   private static final ClosedChannelException WRITE_ADDRESS_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), FileDescriptor.class, "writeAddress(..)");
/*  15: 41 */   private static final ClosedChannelException WRITEV_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), FileDescriptor.class, "writev(..)");
/*  16: 44 */   private static final ClosedChannelException WRITEV_ADDRESSES_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), FileDescriptor.class, "writevAddresses(..)");
/*  17: 45 */   private static final ClosedChannelException READ_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), FileDescriptor.class, "read(..)");
/*  18: 47 */   private static final ClosedChannelException READ_ADDRESS_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), FileDescriptor.class, "readAddress(..)");
/*  19: 49 */   private static final Errors.NativeIoException WRITE_CONNECTION_RESET_EXCEPTION = (Errors.NativeIoException)ThrowableUtil.unknownStackTrace(
/*  20: 50 */     Errors.newConnectionResetException("syscall:write", Errors.ERRNO_EPIPE_NEGATIVE), FileDescriptor.class, "write(..)");
/*  21: 53 */   private static final Errors.NativeIoException WRITE_ADDRESS_CONNECTION_RESET_EXCEPTION = (Errors.NativeIoException)ThrowableUtil.unknownStackTrace(Errors.newConnectionResetException("syscall:write", Errors.ERRNO_EPIPE_NEGATIVE), FileDescriptor.class, "writeAddress(..)");
/*  22: 55 */   private static final Errors.NativeIoException WRITEV_CONNECTION_RESET_EXCEPTION = (Errors.NativeIoException)ThrowableUtil.unknownStackTrace(
/*  23: 56 */     Errors.newConnectionResetException("syscall:writev", Errors.ERRNO_EPIPE_NEGATIVE), FileDescriptor.class, "writev(..)");
/*  24: 59 */   private static final Errors.NativeIoException WRITEV_ADDRESSES_CONNECTION_RESET_EXCEPTION = (Errors.NativeIoException)ThrowableUtil.unknownStackTrace(Errors.newConnectionResetException("syscall:writev", Errors.ERRNO_EPIPE_NEGATIVE), FileDescriptor.class, "writeAddresses(..)");
/*  25: 61 */   private static final Errors.NativeIoException READ_CONNECTION_RESET_EXCEPTION = (Errors.NativeIoException)ThrowableUtil.unknownStackTrace(
/*  26: 62 */     Errors.newConnectionResetException("syscall:read", Errors.ERRNO_ECONNRESET_NEGATIVE), FileDescriptor.class, "read(..)");
/*  27: 65 */   private static final Errors.NativeIoException READ_ADDRESS_CONNECTION_RESET_EXCEPTION = (Errors.NativeIoException)ThrowableUtil.unknownStackTrace(Errors.newConnectionResetException("syscall:read", Errors.ERRNO_ECONNRESET_NEGATIVE), FileDescriptor.class, "readAddress(..)");
/*  28: 69 */   private static final AtomicIntegerFieldUpdater<FileDescriptor> stateUpdater = AtomicIntegerFieldUpdater.newUpdater(FileDescriptor.class, "state");
/*  29:    */   private static final int STATE_CLOSED_MASK = 1;
/*  30:    */   private static final int STATE_INPUT_SHUTDOWN_MASK = 2;
/*  31:    */   private static final int STATE_OUTPUT_SHUTDOWN_MASK = 4;
/*  32:    */   private static final int STATE_ALL_MASK = 7;
/*  33:    */   volatile int state;
/*  34:    */   final int fd;
/*  35:    */   
/*  36:    */   public FileDescriptor(int fd)
/*  37:    */   {
/*  38: 85 */     if (fd < 0) {
/*  39: 86 */       throw new IllegalArgumentException("fd must be >= 0");
/*  40:    */     }
/*  41: 88 */     this.fd = fd;
/*  42:    */   }
/*  43:    */   
/*  44:    */   public final int intValue()
/*  45:    */   {
/*  46: 95 */     return this.fd;
/*  47:    */   }
/*  48:    */   
/*  49:    */   public void close()
/*  50:    */     throws IOException
/*  51:    */   {
/*  52:    */     for (;;)
/*  53:    */     {
/*  54:103 */       int state = this.state;
/*  55:104 */       if (isClosed(state)) {
/*  56:105 */         return;
/*  57:    */       }
/*  58:108 */       if (casState(state, state | 0x7)) {
/*  59:    */         break;
/*  60:    */       }
/*  61:    */     }
/*  62:112 */     int res = close(this.fd);
/*  63:113 */     if (res < 0) {
/*  64:114 */       throw Errors.newIOException("close", res);
/*  65:    */     }
/*  66:    */   }
/*  67:    */   
/*  68:    */   public boolean isOpen()
/*  69:    */   {
/*  70:122 */     return !isClosed(this.state);
/*  71:    */   }
/*  72:    */   
/*  73:    */   public final int write(ByteBuffer buf, int pos, int limit)
/*  74:    */     throws IOException
/*  75:    */   {
/*  76:126 */     int res = write(this.fd, buf, pos, limit);
/*  77:127 */     if (res >= 0) {
/*  78:128 */       return res;
/*  79:    */     }
/*  80:130 */     return Errors.ioResult("write", res, WRITE_CONNECTION_RESET_EXCEPTION, WRITE_CLOSED_CHANNEL_EXCEPTION);
/*  81:    */   }
/*  82:    */   
/*  83:    */   public final int writeAddress(long address, int pos, int limit)
/*  84:    */     throws IOException
/*  85:    */   {
/*  86:134 */     int res = writeAddress(this.fd, address, pos, limit);
/*  87:135 */     if (res >= 0) {
/*  88:136 */       return res;
/*  89:    */     }
/*  90:138 */     return Errors.ioResult("writeAddress", res, WRITE_ADDRESS_CONNECTION_RESET_EXCEPTION, WRITE_ADDRESS_CLOSED_CHANNEL_EXCEPTION);
/*  91:    */   }
/*  92:    */   
/*  93:    */   public final long writev(ByteBuffer[] buffers, int offset, int length, long maxBytesToWrite)
/*  94:    */     throws IOException
/*  95:    */   {
/*  96:143 */     long res = writev(this.fd, buffers, offset, Math.min(Limits.IOV_MAX, length), maxBytesToWrite);
/*  97:144 */     if (res >= 0L) {
/*  98:145 */       return res;
/*  99:    */     }
/* 100:147 */     return Errors.ioResult("writev", (int)res, WRITEV_CONNECTION_RESET_EXCEPTION, WRITEV_CLOSED_CHANNEL_EXCEPTION);
/* 101:    */   }
/* 102:    */   
/* 103:    */   public final long writevAddresses(long memoryAddress, int length)
/* 104:    */     throws IOException
/* 105:    */   {
/* 106:151 */     long res = writevAddresses(this.fd, memoryAddress, length);
/* 107:152 */     if (res >= 0L) {
/* 108:153 */       return res;
/* 109:    */     }
/* 110:155 */     return Errors.ioResult("writevAddresses", (int)res, WRITEV_ADDRESSES_CONNECTION_RESET_EXCEPTION, WRITEV_ADDRESSES_CLOSED_CHANNEL_EXCEPTION);
/* 111:    */   }
/* 112:    */   
/* 113:    */   public final int read(ByteBuffer buf, int pos, int limit)
/* 114:    */     throws IOException
/* 115:    */   {
/* 116:160 */     int res = read(this.fd, buf, pos, limit);
/* 117:161 */     if (res > 0) {
/* 118:162 */       return res;
/* 119:    */     }
/* 120:164 */     if (res == 0) {
/* 121:165 */       return -1;
/* 122:    */     }
/* 123:167 */     return Errors.ioResult("read", res, READ_CONNECTION_RESET_EXCEPTION, READ_CLOSED_CHANNEL_EXCEPTION);
/* 124:    */   }
/* 125:    */   
/* 126:    */   public final int readAddress(long address, int pos, int limit)
/* 127:    */     throws IOException
/* 128:    */   {
/* 129:171 */     int res = readAddress(this.fd, address, pos, limit);
/* 130:172 */     if (res > 0) {
/* 131:173 */       return res;
/* 132:    */     }
/* 133:175 */     if (res == 0) {
/* 134:176 */       return -1;
/* 135:    */     }
/* 136:178 */     return Errors.ioResult("readAddress", res, READ_ADDRESS_CONNECTION_RESET_EXCEPTION, READ_ADDRESS_CLOSED_CHANNEL_EXCEPTION);
/* 137:    */   }
/* 138:    */   
/* 139:    */   public String toString()
/* 140:    */   {
/* 141:184 */     return "FileDescriptor{fd=" + this.fd + '}';
/* 142:    */   }
/* 143:    */   
/* 144:    */   public boolean equals(Object o)
/* 145:    */   {
/* 146:191 */     if (this == o) {
/* 147:192 */       return true;
/* 148:    */     }
/* 149:194 */     if (!(o instanceof FileDescriptor)) {
/* 150:195 */       return false;
/* 151:    */     }
/* 152:198 */     return this.fd == ((FileDescriptor)o).fd;
/* 153:    */   }
/* 154:    */   
/* 155:    */   public int hashCode()
/* 156:    */   {
/* 157:203 */     return this.fd;
/* 158:    */   }
/* 159:    */   
/* 160:    */   public static FileDescriptor from(String path)
/* 161:    */     throws IOException
/* 162:    */   {
/* 163:210 */     ObjectUtil.checkNotNull(path, "path");
/* 164:211 */     int res = open(path);
/* 165:212 */     if (res < 0) {
/* 166:213 */       throw Errors.newIOException("open", res);
/* 167:    */     }
/* 168:215 */     return new FileDescriptor(res);
/* 169:    */   }
/* 170:    */   
/* 171:    */   public static FileDescriptor from(File file)
/* 172:    */     throws IOException
/* 173:    */   {
/* 174:222 */     return from(((File)ObjectUtil.checkNotNull(file, "file")).getPath());
/* 175:    */   }
/* 176:    */   
/* 177:    */   public static FileDescriptor[] pipe()
/* 178:    */     throws IOException
/* 179:    */   {
/* 180:229 */     long res = newPipe();
/* 181:230 */     if (res < 0L) {
/* 182:231 */       throw Errors.newIOException("newPipe", (int)res);
/* 183:    */     }
/* 184:233 */     return new FileDescriptor[] { new FileDescriptor((int)(res >>> 32)), new FileDescriptor((int)res) };
/* 185:    */   }
/* 186:    */   
/* 187:    */   final boolean casState(int expected, int update)
/* 188:    */   {
/* 189:237 */     return stateUpdater.compareAndSet(this, expected, update);
/* 190:    */   }
/* 191:    */   
/* 192:    */   static boolean isClosed(int state)
/* 193:    */   {
/* 194:241 */     return (state & 0x1) != 0;
/* 195:    */   }
/* 196:    */   
/* 197:    */   static boolean isInputShutdown(int state)
/* 198:    */   {
/* 199:245 */     return (state & 0x2) != 0;
/* 200:    */   }
/* 201:    */   
/* 202:    */   static boolean isOutputShutdown(int state)
/* 203:    */   {
/* 204:249 */     return (state & 0x4) != 0;
/* 205:    */   }
/* 206:    */   
/* 207:    */   static int inputShutdown(int state)
/* 208:    */   {
/* 209:253 */     return state | 0x2;
/* 210:    */   }
/* 211:    */   
/* 212:    */   static int outputShutdown(int state)
/* 213:    */   {
/* 214:257 */     return state | 0x4;
/* 215:    */   }
/* 216:    */   
/* 217:    */   private static native int open(String paramString);
/* 218:    */   
/* 219:    */   private static native int close(int paramInt);
/* 220:    */   
/* 221:    */   private static native int write(int paramInt1, ByteBuffer paramByteBuffer, int paramInt2, int paramInt3);
/* 222:    */   
/* 223:    */   private static native int writeAddress(int paramInt1, long paramLong, int paramInt2, int paramInt3);
/* 224:    */   
/* 225:    */   private static native long writev(int paramInt1, ByteBuffer[] paramArrayOfByteBuffer, int paramInt2, int paramInt3, long paramLong);
/* 226:    */   
/* 227:    */   private static native long writevAddresses(int paramInt1, long paramLong, int paramInt2);
/* 228:    */   
/* 229:    */   private static native int read(int paramInt1, ByteBuffer paramByteBuffer, int paramInt2, int paramInt3);
/* 230:    */   
/* 231:    */   private static native int readAddress(int paramInt1, long paramLong, int paramInt2, int paramInt3);
/* 232:    */   
/* 233:    */   private static native long newPipe();
/* 234:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.unix.FileDescriptor
 * JD-Core Version:    0.7.0.1
 */