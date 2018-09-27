/*   1:    */ package io.netty.channel.epoll;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.ChannelOutboundBuffer;
/*   5:    */ import io.netty.channel.ChannelOutboundBuffer.MessageProcessor;
/*   6:    */ import io.netty.channel.socket.DatagramPacket;
/*   7:    */ import io.netty.channel.unix.IovArray;
/*   8:    */ import io.netty.channel.unix.Limits;
/*   9:    */ import io.netty.channel.unix.NativeInetAddress;
/*  10:    */ import io.netty.util.concurrent.FastThreadLocal;
/*  11:    */ import java.net.Inet6Address;
/*  12:    */ import java.net.InetAddress;
/*  13:    */ import java.net.InetSocketAddress;
/*  14:    */ 
/*  15:    */ final class NativeDatagramPacketArray
/*  16:    */   implements ChannelOutboundBuffer.MessageProcessor
/*  17:    */ {
/*  18: 35 */   private static final FastThreadLocal<NativeDatagramPacketArray> ARRAY = new FastThreadLocal()
/*  19:    */   {
/*  20:    */     protected NativeDatagramPacketArray initialValue()
/*  21:    */       throws Exception
/*  22:    */     {
/*  23: 39 */       return new NativeDatagramPacketArray(null);
/*  24:    */     }
/*  25:    */     
/*  26:    */     protected void onRemoval(NativeDatagramPacketArray value)
/*  27:    */       throws Exception
/*  28:    */     {
/*  29: 44 */       NativeDatagramPacketArray.NativeDatagramPacket[] packetsArray = value.packets;
/*  30: 46 */       for (NativeDatagramPacketArray.NativeDatagramPacket datagramPacket : packetsArray) {
/*  31: 47 */         datagramPacket.release();
/*  32:    */       }
/*  33:    */     }
/*  34:    */   };
/*  35: 53 */   private final NativeDatagramPacket[] packets = new NativeDatagramPacket[Limits.UIO_MAX_IOV];
/*  36:    */   private int count;
/*  37:    */   
/*  38:    */   private NativeDatagramPacketArray()
/*  39:    */   {
/*  40: 57 */     for (int i = 0; i < this.packets.length; i++) {
/*  41: 58 */       this.packets[i] = new NativeDatagramPacket();
/*  42:    */     }
/*  43:    */   }
/*  44:    */   
/*  45:    */   boolean add(DatagramPacket packet)
/*  46:    */   {
/*  47: 67 */     if (this.count == this.packets.length) {
/*  48: 68 */       return false;
/*  49:    */     }
/*  50: 70 */     ByteBuf content = (ByteBuf)packet.content();
/*  51: 71 */     int len = content.readableBytes();
/*  52: 72 */     if (len == 0) {
/*  53: 73 */       return true;
/*  54:    */     }
/*  55: 75 */     NativeDatagramPacket p = this.packets[this.count];
/*  56: 76 */     InetSocketAddress recipient = (InetSocketAddress)packet.recipient();
/*  57: 77 */     if (!p.init(content, recipient)) {
/*  58: 78 */       return false;
/*  59:    */     }
/*  60: 81 */     this.count += 1;
/*  61: 82 */     return true;
/*  62:    */   }
/*  63:    */   
/*  64:    */   public boolean processMessage(Object msg)
/*  65:    */     throws Exception
/*  66:    */   {
/*  67: 87 */     return ((msg instanceof DatagramPacket)) && (add((DatagramPacket)msg));
/*  68:    */   }
/*  69:    */   
/*  70:    */   int count()
/*  71:    */   {
/*  72: 94 */     return this.count;
/*  73:    */   }
/*  74:    */   
/*  75:    */   NativeDatagramPacket[] packets()
/*  76:    */   {
/*  77:101 */     return this.packets;
/*  78:    */   }
/*  79:    */   
/*  80:    */   static NativeDatagramPacketArray getInstance(ChannelOutboundBuffer buffer)
/*  81:    */     throws Exception
/*  82:    */   {
/*  83:109 */     NativeDatagramPacketArray array = (NativeDatagramPacketArray)ARRAY.get();
/*  84:110 */     array.count = 0;
/*  85:111 */     buffer.forEachFlushedMessage(array);
/*  86:112 */     return array;
/*  87:    */   }
/*  88:    */   
/*  89:    */   static final class NativeDatagramPacket
/*  90:    */   {
/*  91:123 */     private final IovArray array = new IovArray();
/*  92:    */     private long memoryAddress;
/*  93:    */     private int count;
/*  94:    */     private byte[] addr;
/*  95:    */     private int scopeId;
/*  96:    */     private int port;
/*  97:    */     
/*  98:    */     private void release()
/*  99:    */     {
/* 100:134 */       this.array.release();
/* 101:    */     }
/* 102:    */     
/* 103:    */     private boolean init(ByteBuf buf, InetSocketAddress recipient)
/* 104:    */     {
/* 105:141 */       this.array.clear();
/* 106:142 */       if (!this.array.add(buf)) {
/* 107:143 */         return false;
/* 108:    */       }
/* 109:146 */       this.memoryAddress = this.array.memoryAddress(0);
/* 110:147 */       this.count = this.array.count();
/* 111:    */       
/* 112:149 */       InetAddress address = recipient.getAddress();
/* 113:150 */       if ((address instanceof Inet6Address))
/* 114:    */       {
/* 115:151 */         this.addr = address.getAddress();
/* 116:152 */         this.scopeId = ((Inet6Address)address).getScopeId();
/* 117:    */       }
/* 118:    */       else
/* 119:    */       {
/* 120:154 */         this.addr = NativeInetAddress.ipv4MappedIpv6Address(address.getAddress());
/* 121:155 */         this.scopeId = 0;
/* 122:    */       }
/* 123:157 */       this.port = recipient.getPort();
/* 124:158 */       return true;
/* 125:    */     }
/* 126:    */   }
/* 127:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.epoll.NativeDatagramPacketArray
 * JD-Core Version:    0.7.0.1
 */