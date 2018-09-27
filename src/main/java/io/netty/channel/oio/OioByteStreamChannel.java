/*   1:    */ package io.netty.channel.oio;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.Channel;
/*   5:    */ import io.netty.channel.Channel.Unsafe;
/*   6:    */ import io.netty.channel.FileRegion;
/*   7:    */ import io.netty.channel.RecvByteBufAllocator.Handle;
/*   8:    */ import java.io.EOFException;
/*   9:    */ import java.io.IOException;
/*  10:    */ import java.io.InputStream;
/*  11:    */ import java.io.OutputStream;
/*  12:    */ import java.nio.channels.Channels;
/*  13:    */ import java.nio.channels.ClosedChannelException;
/*  14:    */ import java.nio.channels.NotYetConnectedException;
/*  15:    */ import java.nio.channels.WritableByteChannel;
/*  16:    */ 
/*  17:    */ public abstract class OioByteStreamChannel
/*  18:    */   extends AbstractOioByteChannel
/*  19:    */ {
/*  20: 37 */   private static final InputStream CLOSED_IN = new InputStream()
/*  21:    */   {
/*  22:    */     public int read()
/*  23:    */     {
/*  24: 40 */       return -1;
/*  25:    */     }
/*  26:    */   };
/*  27: 44 */   private static final OutputStream CLOSED_OUT = new OutputStream()
/*  28:    */   {
/*  29:    */     public void write(int b)
/*  30:    */       throws IOException
/*  31:    */     {
/*  32: 47 */       throw new ClosedChannelException();
/*  33:    */     }
/*  34:    */   };
/*  35:    */   private InputStream is;
/*  36:    */   private OutputStream os;
/*  37:    */   private WritableByteChannel outChannel;
/*  38:    */   
/*  39:    */   protected OioByteStreamChannel(Channel parent)
/*  40:    */   {
/*  41: 62 */     super(parent);
/*  42:    */   }
/*  43:    */   
/*  44:    */   protected final void activate(InputStream is, OutputStream os)
/*  45:    */   {
/*  46: 69 */     if (this.is != null) {
/*  47: 70 */       throw new IllegalStateException("input was set already");
/*  48:    */     }
/*  49: 72 */     if (this.os != null) {
/*  50: 73 */       throw new IllegalStateException("output was set already");
/*  51:    */     }
/*  52: 75 */     if (is == null) {
/*  53: 76 */       throw new NullPointerException("is");
/*  54:    */     }
/*  55: 78 */     if (os == null) {
/*  56: 79 */       throw new NullPointerException("os");
/*  57:    */     }
/*  58: 81 */     this.is = is;
/*  59: 82 */     this.os = os;
/*  60:    */   }
/*  61:    */   
/*  62:    */   public boolean isActive()
/*  63:    */   {
/*  64: 87 */     InputStream is = this.is;
/*  65: 88 */     if ((is == null) || (is == CLOSED_IN)) {
/*  66: 89 */       return false;
/*  67:    */     }
/*  68: 92 */     OutputStream os = this.os;
/*  69: 93 */     return (os != null) && (os != CLOSED_OUT);
/*  70:    */   }
/*  71:    */   
/*  72:    */   protected int available()
/*  73:    */   {
/*  74:    */     try
/*  75:    */     {
/*  76: 99 */       return this.is.available();
/*  77:    */     }
/*  78:    */     catch (IOException ignored) {}
/*  79:101 */     return 0;
/*  80:    */   }
/*  81:    */   
/*  82:    */   protected int doReadBytes(ByteBuf buf)
/*  83:    */     throws Exception
/*  84:    */   {
/*  85:107 */     RecvByteBufAllocator.Handle allocHandle = unsafe().recvBufAllocHandle();
/*  86:108 */     allocHandle.attemptedBytesRead(Math.max(1, Math.min(available(), buf.maxWritableBytes())));
/*  87:109 */     return buf.writeBytes(this.is, allocHandle.attemptedBytesRead());
/*  88:    */   }
/*  89:    */   
/*  90:    */   protected void doWriteBytes(ByteBuf buf)
/*  91:    */     throws Exception
/*  92:    */   {
/*  93:114 */     OutputStream os = this.os;
/*  94:115 */     if (os == null) {
/*  95:116 */       throw new NotYetConnectedException();
/*  96:    */     }
/*  97:118 */     buf.readBytes(os, buf.readableBytes());
/*  98:    */   }
/*  99:    */   
/* 100:    */   protected void doWriteFileRegion(FileRegion region)
/* 101:    */     throws Exception
/* 102:    */   {
/* 103:123 */     OutputStream os = this.os;
/* 104:124 */     if (os == null) {
/* 105:125 */       throw new NotYetConnectedException();
/* 106:    */     }
/* 107:127 */     if (this.outChannel == null) {
/* 108:128 */       this.outChannel = Channels.newChannel(os);
/* 109:    */     }
/* 110:131 */     long written = 0L;
/* 111:    */     for (;;)
/* 112:    */     {
/* 113:133 */       long localWritten = region.transferTo(this.outChannel, written);
/* 114:134 */       if (localWritten == -1L)
/* 115:    */       {
/* 116:135 */         checkEOF(region);
/* 117:136 */         return;
/* 118:    */       }
/* 119:138 */       written += localWritten;
/* 120:140 */       if (written >= region.count()) {
/* 121:141 */         return;
/* 122:    */       }
/* 123:    */     }
/* 124:    */   }
/* 125:    */   
/* 126:    */   private static void checkEOF(FileRegion region)
/* 127:    */     throws IOException
/* 128:    */   {
/* 129:147 */     if (region.transferred() < region.count()) {
/* 130:149 */       throw new EOFException("Expected to be able to write " + region.count() + " bytes, but only wrote " + region.transferred());
/* 131:    */     }
/* 132:    */   }
/* 133:    */   
/* 134:    */   protected void doClose()
/* 135:    */     throws Exception
/* 136:    */   {
/* 137:155 */     InputStream is = this.is;
/* 138:156 */     OutputStream os = this.os;
/* 139:157 */     this.is = CLOSED_IN;
/* 140:158 */     this.os = CLOSED_OUT;
/* 141:    */     try
/* 142:    */     {
/* 143:161 */       if (is != null) {
/* 144:162 */         is.close();
/* 145:    */       }
/* 146:165 */       if (os != null) {
/* 147:166 */         os.close();
/* 148:    */       }
/* 149:    */     }
/* 150:    */     finally
/* 151:    */     {
/* 152:165 */       if (os != null) {
/* 153:166 */         os.close();
/* 154:    */       }
/* 155:    */     }
/* 156:    */   }
/* 157:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.oio.OioByteStreamChannel
 * JD-Core Version:    0.7.0.1
 */