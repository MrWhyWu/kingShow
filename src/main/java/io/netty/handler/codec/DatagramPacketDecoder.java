/*   1:    */ package io.netty.handler.codec;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.ChannelHandlerContext;
/*   5:    */ import io.netty.channel.socket.DatagramPacket;
/*   6:    */ import io.netty.util.internal.ObjectUtil;
/*   7:    */ import java.util.List;
/*   8:    */ 
/*   9:    */ public class DatagramPacketDecoder
/*  10:    */   extends MessageToMessageDecoder<DatagramPacket>
/*  11:    */ {
/*  12:    */   private final MessageToMessageDecoder<ByteBuf> decoder;
/*  13:    */   
/*  14:    */   public DatagramPacketDecoder(MessageToMessageDecoder<ByteBuf> decoder)
/*  15:    */   {
/*  16: 46 */     this.decoder = ((MessageToMessageDecoder)ObjectUtil.checkNotNull(decoder, "decoder"));
/*  17:    */   }
/*  18:    */   
/*  19:    */   public boolean acceptInboundMessage(Object msg)
/*  20:    */     throws Exception
/*  21:    */   {
/*  22: 51 */     if ((msg instanceof DatagramPacket)) {
/*  23: 52 */       return this.decoder.acceptInboundMessage(((DatagramPacket)msg).content());
/*  24:    */     }
/*  25: 54 */     return false;
/*  26:    */   }
/*  27:    */   
/*  28:    */   protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out)
/*  29:    */     throws Exception
/*  30:    */   {
/*  31: 59 */     this.decoder.decode(ctx, msg.content(), out);
/*  32:    */   }
/*  33:    */   
/*  34:    */   public void channelRegistered(ChannelHandlerContext ctx)
/*  35:    */     throws Exception
/*  36:    */   {
/*  37: 64 */     this.decoder.channelRegistered(ctx);
/*  38:    */   }
/*  39:    */   
/*  40:    */   public void channelUnregistered(ChannelHandlerContext ctx)
/*  41:    */     throws Exception
/*  42:    */   {
/*  43: 69 */     this.decoder.channelUnregistered(ctx);
/*  44:    */   }
/*  45:    */   
/*  46:    */   public void channelActive(ChannelHandlerContext ctx)
/*  47:    */     throws Exception
/*  48:    */   {
/*  49: 74 */     this.decoder.channelActive(ctx);
/*  50:    */   }
/*  51:    */   
/*  52:    */   public void channelInactive(ChannelHandlerContext ctx)
/*  53:    */     throws Exception
/*  54:    */   {
/*  55: 79 */     this.decoder.channelInactive(ctx);
/*  56:    */   }
/*  57:    */   
/*  58:    */   public void channelReadComplete(ChannelHandlerContext ctx)
/*  59:    */     throws Exception
/*  60:    */   {
/*  61: 84 */     this.decoder.channelReadComplete(ctx);
/*  62:    */   }
/*  63:    */   
/*  64:    */   public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
/*  65:    */     throws Exception
/*  66:    */   {
/*  67: 89 */     this.decoder.userEventTriggered(ctx, evt);
/*  68:    */   }
/*  69:    */   
/*  70:    */   public void channelWritabilityChanged(ChannelHandlerContext ctx)
/*  71:    */     throws Exception
/*  72:    */   {
/*  73: 94 */     this.decoder.channelWritabilityChanged(ctx);
/*  74:    */   }
/*  75:    */   
/*  76:    */   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
/*  77:    */     throws Exception
/*  78:    */   {
/*  79: 99 */     this.decoder.exceptionCaught(ctx, cause);
/*  80:    */   }
/*  81:    */   
/*  82:    */   public void handlerAdded(ChannelHandlerContext ctx)
/*  83:    */     throws Exception
/*  84:    */   {
/*  85:104 */     this.decoder.handlerAdded(ctx);
/*  86:    */   }
/*  87:    */   
/*  88:    */   public void handlerRemoved(ChannelHandlerContext ctx)
/*  89:    */     throws Exception
/*  90:    */   {
/*  91:109 */     this.decoder.handlerRemoved(ctx);
/*  92:    */   }
/*  93:    */   
/*  94:    */   public boolean isSharable()
/*  95:    */   {
/*  96:114 */     return this.decoder.isSharable();
/*  97:    */   }
/*  98:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.DatagramPacketDecoder
 * JD-Core Version:    0.7.0.1
 */