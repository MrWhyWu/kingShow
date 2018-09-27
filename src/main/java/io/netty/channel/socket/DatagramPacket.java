/*  1:   */ package io.netty.channel.socket;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.buffer.ByteBufHolder;
/*  5:   */ import io.netty.channel.DefaultAddressedEnvelope;
/*  6:   */ import java.net.InetSocketAddress;
/*  7:   */ 
/*  8:   */ public final class DatagramPacket
/*  9:   */   extends DefaultAddressedEnvelope<ByteBuf, InetSocketAddress>
/* 10:   */   implements ByteBufHolder
/* 11:   */ {
/* 12:   */   public DatagramPacket(ByteBuf data, InetSocketAddress recipient)
/* 13:   */   {
/* 14:34 */     super(data, recipient);
/* 15:   */   }
/* 16:   */   
/* 17:   */   public DatagramPacket(ByteBuf data, InetSocketAddress recipient, InetSocketAddress sender)
/* 18:   */   {
/* 19:42 */     super(data, recipient, sender);
/* 20:   */   }
/* 21:   */   
/* 22:   */   public DatagramPacket copy()
/* 23:   */   {
/* 24:47 */     return replace(((ByteBuf)content()).copy());
/* 25:   */   }
/* 26:   */   
/* 27:   */   public DatagramPacket duplicate()
/* 28:   */   {
/* 29:52 */     return replace(((ByteBuf)content()).duplicate());
/* 30:   */   }
/* 31:   */   
/* 32:   */   public DatagramPacket retainedDuplicate()
/* 33:   */   {
/* 34:57 */     return replace(((ByteBuf)content()).retainedDuplicate());
/* 35:   */   }
/* 36:   */   
/* 37:   */   public DatagramPacket replace(ByteBuf content)
/* 38:   */   {
/* 39:62 */     return new DatagramPacket(content, (InetSocketAddress)recipient(), (InetSocketAddress)sender());
/* 40:   */   }
/* 41:   */   
/* 42:   */   public DatagramPacket retain()
/* 43:   */   {
/* 44:67 */     super.retain();
/* 45:68 */     return this;
/* 46:   */   }
/* 47:   */   
/* 48:   */   public DatagramPacket retain(int increment)
/* 49:   */   {
/* 50:73 */     super.retain(increment);
/* 51:74 */     return this;
/* 52:   */   }
/* 53:   */   
/* 54:   */   public DatagramPacket touch()
/* 55:   */   {
/* 56:79 */     super.touch();
/* 57:80 */     return this;
/* 58:   */   }
/* 59:   */   
/* 60:   */   public DatagramPacket touch(Object hint)
/* 61:   */   {
/* 62:85 */     super.touch(hint);
/* 63:86 */     return this;
/* 64:   */   }
/* 65:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.socket.DatagramPacket
 * JD-Core Version:    0.7.0.1
 */