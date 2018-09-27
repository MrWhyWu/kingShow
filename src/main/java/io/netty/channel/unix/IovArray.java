/*   1:    */ package io.netty.channel.unix;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.ChannelOutboundBuffer.MessageProcessor;
/*   5:    */ import io.netty.util.internal.ObjectUtil;
/*   6:    */ import io.netty.util.internal.PlatformDependent;
/*   7:    */ import java.nio.ByteBuffer;
/*   8:    */ 
/*   9:    */ public final class IovArray
/*  10:    */   implements ChannelOutboundBuffer.MessageProcessor
/*  11:    */ {
/*  12: 54 */   private static final int ADDRESS_SIZE = PlatformDependent.addressSize();
/*  13: 60 */   private static final int IOV_SIZE = 2 * ADDRESS_SIZE;
/*  14: 66 */   private static final int CAPACITY = Limits.IOV_MAX * IOV_SIZE;
/*  15:    */   private final long memoryAddress;
/*  16:    */   private int count;
/*  17:    */   private long size;
/*  18: 71 */   private long maxBytes = Limits.SSIZE_MAX;
/*  19:    */   
/*  20:    */   public IovArray()
/*  21:    */   {
/*  22: 74 */     this.memoryAddress = PlatformDependent.allocateMemory(CAPACITY);
/*  23:    */   }
/*  24:    */   
/*  25:    */   public void clear()
/*  26:    */   {
/*  27: 78 */     this.count = 0;
/*  28: 79 */     this.size = 0L;
/*  29:    */   }
/*  30:    */   
/*  31:    */   public boolean add(ByteBuf buf)
/*  32:    */   {
/*  33: 87 */     if (this.count == Limits.IOV_MAX) {
/*  34: 89 */       return false;
/*  35:    */     }
/*  36: 90 */     if (buf.nioBufferCount() == 1)
/*  37:    */     {
/*  38: 91 */       int len = buf.readableBytes();
/*  39: 92 */       return (len == 0) || (add(buf.memoryAddress(), buf.readerIndex(), len));
/*  40:    */     }
/*  41: 94 */     ByteBuffer[] buffers = buf.nioBuffers();
/*  42: 95 */     for (ByteBuffer nioBuffer : buffers)
/*  43:    */     {
/*  44: 96 */       int len = nioBuffer.remaining();
/*  45: 97 */       if ((len != 0) && ((!add(PlatformDependent.directBufferAddress(nioBuffer), nioBuffer.position(), len)) || (this.count == Limits.IOV_MAX))) {
/*  46:    */         break;
/*  47:    */       }
/*  48:    */     }
/*  49:101 */     return true;
/*  50:    */   }
/*  51:    */   
/*  52:    */   private boolean add(long addr, int offset, int len)
/*  53:    */   {
/*  54:106 */     if (len == 0) {
/*  55:108 */       return true;
/*  56:    */     }
/*  57:111 */     long baseOffset = memoryAddress(this.count);
/*  58:112 */     long lengthOffset = baseOffset + ADDRESS_SIZE;
/*  59:116 */     if ((this.maxBytes - len < this.size) && (this.count > 0)) {
/*  60:123 */       return false;
/*  61:    */     }
/*  62:125 */     this.size += len;
/*  63:126 */     this.count += 1;
/*  64:128 */     if (ADDRESS_SIZE == 8)
/*  65:    */     {
/*  66:130 */       PlatformDependent.putLong(baseOffset, addr + offset);
/*  67:131 */       PlatformDependent.putLong(lengthOffset, len);
/*  68:    */     }
/*  69:    */     else
/*  70:    */     {
/*  71:133 */       assert (ADDRESS_SIZE == 4);
/*  72:134 */       PlatformDependent.putInt(baseOffset, (int)addr + offset);
/*  73:135 */       PlatformDependent.putInt(lengthOffset, len);
/*  74:    */     }
/*  75:137 */     return true;
/*  76:    */   }
/*  77:    */   
/*  78:    */   public int count()
/*  79:    */   {
/*  80:144 */     return this.count;
/*  81:    */   }
/*  82:    */   
/*  83:    */   public long size()
/*  84:    */   {
/*  85:151 */     return this.size;
/*  86:    */   }
/*  87:    */   
/*  88:    */   public void maxBytes(long maxBytes)
/*  89:    */   {
/*  90:165 */     this.maxBytes = Math.min(Limits.SSIZE_MAX, ObjectUtil.checkPositive(maxBytes, "maxBytes"));
/*  91:    */   }
/*  92:    */   
/*  93:    */   public long maxBytes()
/*  94:    */   {
/*  95:173 */     return this.maxBytes;
/*  96:    */   }
/*  97:    */   
/*  98:    */   public long memoryAddress(int offset)
/*  99:    */   {
/* 100:180 */     return this.memoryAddress + IOV_SIZE * offset;
/* 101:    */   }
/* 102:    */   
/* 103:    */   public void release()
/* 104:    */   {
/* 105:187 */     PlatformDependent.freeMemory(this.memoryAddress);
/* 106:    */   }
/* 107:    */   
/* 108:    */   public boolean processMessage(Object msg)
/* 109:    */     throws Exception
/* 110:    */   {
/* 111:192 */     return ((msg instanceof ByteBuf)) && (add((ByteBuf)msg));
/* 112:    */   }
/* 113:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.unix.IovArray
 * JD-Core Version:    0.7.0.1
 */