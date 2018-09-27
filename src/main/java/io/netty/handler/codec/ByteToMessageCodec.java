/*   1:    */ package io.netty.handler.codec;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.ChannelDuplexHandler;
/*   5:    */ import io.netty.channel.ChannelHandlerContext;
/*   6:    */ import io.netty.channel.ChannelPromise;
/*   7:    */ import io.netty.util.internal.TypeParameterMatcher;
/*   8:    */ import java.util.List;
/*   9:    */ 
/*  10:    */ public abstract class ByteToMessageCodec<I>
/*  11:    */   extends ChannelDuplexHandler
/*  12:    */ {
/*  13:    */   private final TypeParameterMatcher outboundMsgMatcher;
/*  14:    */   private final MessageToByteEncoder<I> encoder;
/*  15: 39 */   private final ByteToMessageDecoder decoder = new ByteToMessageDecoder()
/*  16:    */   {
/*  17:    */     public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/*  18:    */       throws Exception
/*  19:    */     {
/*  20: 42 */       ByteToMessageCodec.this.decode(ctx, in, out);
/*  21:    */     }
/*  22:    */     
/*  23:    */     protected void decodeLast(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/*  24:    */       throws Exception
/*  25:    */     {
/*  26: 47 */       ByteToMessageCodec.this.decodeLast(ctx, in, out);
/*  27:    */     }
/*  28:    */   };
/*  29:    */   
/*  30:    */   protected ByteToMessageCodec()
/*  31:    */   {
/*  32: 55 */     this(true);
/*  33:    */   }
/*  34:    */   
/*  35:    */   protected ByteToMessageCodec(Class<? extends I> outboundMessageType)
/*  36:    */   {
/*  37: 62 */     this(outboundMessageType, true);
/*  38:    */   }
/*  39:    */   
/*  40:    */   protected ByteToMessageCodec(boolean preferDirect)
/*  41:    */   {
/*  42: 73 */     ensureNotSharable();
/*  43: 74 */     this.outboundMsgMatcher = TypeParameterMatcher.find(this, ByteToMessageCodec.class, "I");
/*  44: 75 */     this.encoder = new Encoder(preferDirect);
/*  45:    */   }
/*  46:    */   
/*  47:    */   protected ByteToMessageCodec(Class<? extends I> outboundMessageType, boolean preferDirect)
/*  48:    */   {
/*  49: 87 */     ensureNotSharable();
/*  50: 88 */     this.outboundMsgMatcher = TypeParameterMatcher.get(outboundMessageType);
/*  51: 89 */     this.encoder = new Encoder(preferDirect);
/*  52:    */   }
/*  53:    */   
/*  54:    */   public boolean acceptOutboundMessage(Object msg)
/*  55:    */     throws Exception
/*  56:    */   {
/*  57: 98 */     return this.outboundMsgMatcher.match(msg);
/*  58:    */   }
/*  59:    */   
/*  60:    */   public void channelRead(ChannelHandlerContext ctx, Object msg)
/*  61:    */     throws Exception
/*  62:    */   {
/*  63:103 */     this.decoder.channelRead(ctx, msg);
/*  64:    */   }
/*  65:    */   
/*  66:    */   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
/*  67:    */     throws Exception
/*  68:    */   {
/*  69:108 */     this.encoder.write(ctx, msg, promise);
/*  70:    */   }
/*  71:    */   
/*  72:    */   public void channelReadComplete(ChannelHandlerContext ctx)
/*  73:    */     throws Exception
/*  74:    */   {
/*  75:113 */     this.decoder.channelReadComplete(ctx);
/*  76:    */   }
/*  77:    */   
/*  78:    */   public void channelInactive(ChannelHandlerContext ctx)
/*  79:    */     throws Exception
/*  80:    */   {
/*  81:118 */     this.decoder.channelInactive(ctx);
/*  82:    */   }
/*  83:    */   
/*  84:    */   public void handlerAdded(ChannelHandlerContext ctx)
/*  85:    */     throws Exception
/*  86:    */   {
/*  87:    */     try
/*  88:    */     {
/*  89:124 */       this.decoder.handlerAdded(ctx);
/*  90:    */       
/*  91:126 */       this.encoder.handlerAdded(ctx);
/*  92:    */     }
/*  93:    */     finally
/*  94:    */     {
/*  95:126 */       this.encoder.handlerAdded(ctx);
/*  96:    */     }
/*  97:    */   }
/*  98:    */   
/*  99:    */   public void handlerRemoved(ChannelHandlerContext ctx)
/* 100:    */     throws Exception
/* 101:    */   {
/* 102:    */     try
/* 103:    */     {
/* 104:133 */       this.decoder.handlerRemoved(ctx);
/* 105:    */       
/* 106:135 */       this.encoder.handlerRemoved(ctx);
/* 107:    */     }
/* 108:    */     finally
/* 109:    */     {
/* 110:135 */       this.encoder.handlerRemoved(ctx);
/* 111:    */     }
/* 112:    */   }
/* 113:    */   
/* 114:    */   protected abstract void encode(ChannelHandlerContext paramChannelHandlerContext, I paramI, ByteBuf paramByteBuf)
/* 115:    */     throws Exception;
/* 116:    */   
/* 117:    */   protected abstract void decode(ChannelHandlerContext paramChannelHandlerContext, ByteBuf paramByteBuf, List<Object> paramList)
/* 118:    */     throws Exception;
/* 119:    */   
/* 120:    */   protected void decodeLast(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/* 121:    */     throws Exception
/* 122:    */   {
/* 123:153 */     if (in.isReadable()) {
/* 124:156 */       decode(ctx, in, out);
/* 125:    */     }
/* 126:    */   }
/* 127:    */   
/* 128:    */   private final class Encoder
/* 129:    */     extends MessageToByteEncoder<I>
/* 130:    */   {
/* 131:    */     Encoder(boolean preferDirect)
/* 132:    */     {
/* 133:162 */       super();
/* 134:    */     }
/* 135:    */     
/* 136:    */     public boolean acceptOutboundMessage(Object msg)
/* 137:    */       throws Exception
/* 138:    */     {
/* 139:167 */       return ByteToMessageCodec.this.acceptOutboundMessage(msg);
/* 140:    */     }
/* 141:    */     
/* 142:    */     protected void encode(ChannelHandlerContext ctx, I msg, ByteBuf out)
/* 143:    */       throws Exception
/* 144:    */     {
/* 145:172 */       ByteToMessageCodec.this.encode(ctx, msg, out);
/* 146:    */     }
/* 147:    */   }
/* 148:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.ByteToMessageCodec
 * JD-Core Version:    0.7.0.1
 */