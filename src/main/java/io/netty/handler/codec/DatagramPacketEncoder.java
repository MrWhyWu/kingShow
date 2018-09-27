/*   1:    */ package io.netty.handler.codec;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.AddressedEnvelope;
/*   5:    */ import io.netty.channel.ChannelHandlerContext;
/*   6:    */ import io.netty.channel.ChannelPromise;
/*   7:    */ import io.netty.channel.socket.DatagramPacket;
/*   8:    */ import io.netty.util.internal.ObjectUtil;
/*   9:    */ import io.netty.util.internal.StringUtil;
/*  10:    */ import java.net.InetSocketAddress;
/*  11:    */ import java.net.SocketAddress;
/*  12:    */ import java.util.List;
/*  13:    */ 
/*  14:    */ public class DatagramPacketEncoder<M>
/*  15:    */   extends MessageToMessageEncoder<AddressedEnvelope<M, InetSocketAddress>>
/*  16:    */ {
/*  17:    */   private final MessageToMessageEncoder<? super M> encoder;
/*  18:    */   
/*  19:    */   public DatagramPacketEncoder(MessageToMessageEncoder<? super M> encoder)
/*  20:    */   {
/*  21: 57 */     this.encoder = ((MessageToMessageEncoder)ObjectUtil.checkNotNull(encoder, "encoder"));
/*  22:    */   }
/*  23:    */   
/*  24:    */   public boolean acceptOutboundMessage(Object msg)
/*  25:    */     throws Exception
/*  26:    */   {
/*  27: 62 */     if (super.acceptOutboundMessage(msg))
/*  28:    */     {
/*  29: 64 */       AddressedEnvelope envelope = (AddressedEnvelope)msg;
/*  30: 65 */       return (this.encoder.acceptOutboundMessage(envelope.content())) && 
/*  31: 66 */         ((envelope.sender() instanceof InetSocketAddress)) && 
/*  32: 67 */         ((envelope.recipient() instanceof InetSocketAddress));
/*  33:    */     }
/*  34: 69 */     return false;
/*  35:    */   }
/*  36:    */   
/*  37:    */   protected void encode(ChannelHandlerContext ctx, AddressedEnvelope<M, InetSocketAddress> msg, List<Object> out)
/*  38:    */     throws Exception
/*  39:    */   {
/*  40: 75 */     assert (out.isEmpty());
/*  41:    */     
/*  42: 77 */     this.encoder.encode(ctx, msg.content(), out);
/*  43: 78 */     if (out.size() != 1) {
/*  44: 80 */       throw new EncoderException(StringUtil.simpleClassName(this.encoder) + " must produce only one message.");
/*  45:    */     }
/*  46: 82 */     Object content = out.get(0);
/*  47: 83 */     if ((content instanceof ByteBuf)) {
/*  48: 85 */       out.set(0, new DatagramPacket((ByteBuf)content, (InetSocketAddress)msg.recipient(), (InetSocketAddress)msg.sender()));
/*  49:    */     } else {
/*  50: 88 */       throw new EncoderException(StringUtil.simpleClassName(this.encoder) + " must produce only ByteBuf.");
/*  51:    */     }
/*  52:    */   }
/*  53:    */   
/*  54:    */   public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise)
/*  55:    */     throws Exception
/*  56:    */   {
/*  57: 94 */     this.encoder.bind(ctx, localAddress, promise);
/*  58:    */   }
/*  59:    */   
/*  60:    */   public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
/*  61:    */     throws Exception
/*  62:    */   {
/*  63:101 */     this.encoder.connect(ctx, remoteAddress, localAddress, promise);
/*  64:    */   }
/*  65:    */   
/*  66:    */   public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise)
/*  67:    */     throws Exception
/*  68:    */   {
/*  69:106 */     this.encoder.disconnect(ctx, promise);
/*  70:    */   }
/*  71:    */   
/*  72:    */   public void close(ChannelHandlerContext ctx, ChannelPromise promise)
/*  73:    */     throws Exception
/*  74:    */   {
/*  75:111 */     this.encoder.close(ctx, promise);
/*  76:    */   }
/*  77:    */   
/*  78:    */   public void deregister(ChannelHandlerContext ctx, ChannelPromise promise)
/*  79:    */     throws Exception
/*  80:    */   {
/*  81:116 */     this.encoder.deregister(ctx, promise);
/*  82:    */   }
/*  83:    */   
/*  84:    */   public void read(ChannelHandlerContext ctx)
/*  85:    */     throws Exception
/*  86:    */   {
/*  87:121 */     this.encoder.read(ctx);
/*  88:    */   }
/*  89:    */   
/*  90:    */   public void flush(ChannelHandlerContext ctx)
/*  91:    */     throws Exception
/*  92:    */   {
/*  93:126 */     this.encoder.flush(ctx);
/*  94:    */   }
/*  95:    */   
/*  96:    */   public void handlerAdded(ChannelHandlerContext ctx)
/*  97:    */     throws Exception
/*  98:    */   {
/*  99:131 */     this.encoder.handlerAdded(ctx);
/* 100:    */   }
/* 101:    */   
/* 102:    */   public void handlerRemoved(ChannelHandlerContext ctx)
/* 103:    */     throws Exception
/* 104:    */   {
/* 105:136 */     this.encoder.handlerRemoved(ctx);
/* 106:    */   }
/* 107:    */   
/* 108:    */   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
/* 109:    */     throws Exception
/* 110:    */   {
/* 111:141 */     this.encoder.exceptionCaught(ctx, cause);
/* 112:    */   }
/* 113:    */   
/* 114:    */   public boolean isSharable()
/* 115:    */   {
/* 116:146 */     return this.encoder.isSharable();
/* 117:    */   }
/* 118:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.DatagramPacketEncoder
 * JD-Core Version:    0.7.0.1
 */