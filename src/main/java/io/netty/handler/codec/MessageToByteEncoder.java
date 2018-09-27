/*   1:    */ package io.netty.handler.codec;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.buffer.Unpooled;
/*   6:    */ import io.netty.channel.ChannelHandlerContext;
/*   7:    */ import io.netty.channel.ChannelOutboundHandlerAdapter;
/*   8:    */ import io.netty.channel.ChannelPromise;
/*   9:    */ import io.netty.util.ReferenceCountUtil;
/*  10:    */ import io.netty.util.internal.TypeParameterMatcher;
/*  11:    */ 
/*  12:    */ public abstract class MessageToByteEncoder<I>
/*  13:    */   extends ChannelOutboundHandlerAdapter
/*  14:    */ {
/*  15:    */   private final TypeParameterMatcher matcher;
/*  16:    */   private final boolean preferDirect;
/*  17:    */   
/*  18:    */   protected MessageToByteEncoder()
/*  19:    */   {
/*  20: 55 */     this(true);
/*  21:    */   }
/*  22:    */   
/*  23:    */   protected MessageToByteEncoder(Class<? extends I> outboundMessageType)
/*  24:    */   {
/*  25: 62 */     this(outboundMessageType, true);
/*  26:    */   }
/*  27:    */   
/*  28:    */   protected MessageToByteEncoder(boolean preferDirect)
/*  29:    */   {
/*  30: 73 */     this.matcher = TypeParameterMatcher.find(this, MessageToByteEncoder.class, "I");
/*  31: 74 */     this.preferDirect = preferDirect;
/*  32:    */   }
/*  33:    */   
/*  34:    */   protected MessageToByteEncoder(Class<? extends I> outboundMessageType, boolean preferDirect)
/*  35:    */   {
/*  36: 86 */     this.matcher = TypeParameterMatcher.get(outboundMessageType);
/*  37: 87 */     this.preferDirect = preferDirect;
/*  38:    */   }
/*  39:    */   
/*  40:    */   public boolean acceptOutboundMessage(Object msg)
/*  41:    */     throws Exception
/*  42:    */   {
/*  43: 95 */     return this.matcher.match(msg);
/*  44:    */   }
/*  45:    */   
/*  46:    */   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
/*  47:    */     throws Exception
/*  48:    */   {
/*  49:100 */     ByteBuf buf = null;
/*  50:    */     try
/*  51:    */     {
/*  52:102 */       if (acceptOutboundMessage(msg))
/*  53:    */       {
/*  54:104 */         I cast = msg;
/*  55:105 */         buf = allocateBuffer(ctx, cast, this.preferDirect);
/*  56:    */         try
/*  57:    */         {
/*  58:107 */           encode(ctx, cast, buf);
/*  59:    */         }
/*  60:    */         finally
/*  61:    */         {
/*  62:109 */           ReferenceCountUtil.release(cast);
/*  63:    */         }
/*  64:112 */         if (buf.isReadable())
/*  65:    */         {
/*  66:113 */           ctx.write(buf, promise);
/*  67:    */         }
/*  68:    */         else
/*  69:    */         {
/*  70:115 */           buf.release();
/*  71:116 */           ctx.write(Unpooled.EMPTY_BUFFER, promise);
/*  72:    */         }
/*  73:118 */         buf = null;
/*  74:    */       }
/*  75:    */       else
/*  76:    */       {
/*  77:120 */         ctx.write(msg, promise);
/*  78:    */       }
/*  79:    */     }
/*  80:    */     catch (EncoderException e)
/*  81:    */     {
/*  82:123 */       throw e;
/*  83:    */     }
/*  84:    */     catch (Throwable e)
/*  85:    */     {
/*  86:125 */       throw new EncoderException(e);
/*  87:    */     }
/*  88:    */     finally
/*  89:    */     {
/*  90:127 */       if (buf != null) {
/*  91:128 */         buf.release();
/*  92:    */       }
/*  93:    */     }
/*  94:    */   }
/*  95:    */   
/*  96:    */   protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, I msg, boolean preferDirect)
/*  97:    */     throws Exception
/*  98:    */   {
/*  99:139 */     if (preferDirect) {
/* 100:140 */       return ctx.alloc().ioBuffer();
/* 101:    */     }
/* 102:142 */     return ctx.alloc().heapBuffer();
/* 103:    */   }
/* 104:    */   
/* 105:    */   protected abstract void encode(ChannelHandlerContext paramChannelHandlerContext, I paramI, ByteBuf paramByteBuf)
/* 106:    */     throws Exception;
/* 107:    */   
/* 108:    */   protected boolean isPreferDirect()
/* 109:    */   {
/* 110:158 */     return this.preferDirect;
/* 111:    */   }
/* 112:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.MessageToByteEncoder
 * JD-Core Version:    0.7.0.1
 */