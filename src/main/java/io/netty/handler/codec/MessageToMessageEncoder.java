/*   1:    */ package io.netty.handler.codec;
/*   2:    */ 
/*   3:    */ import io.netty.channel.ChannelHandlerContext;
/*   4:    */ import io.netty.channel.ChannelOutboundHandlerAdapter;
/*   5:    */ import io.netty.channel.ChannelPromise;
/*   6:    */ import io.netty.util.ReferenceCountUtil;
/*   7:    */ import io.netty.util.internal.StringUtil;
/*   8:    */ import io.netty.util.internal.TypeParameterMatcher;
/*   9:    */ import java.util.List;
/*  10:    */ 
/*  11:    */ public abstract class MessageToMessageEncoder<I>
/*  12:    */   extends ChannelOutboundHandlerAdapter
/*  13:    */ {
/*  14:    */   private final TypeParameterMatcher matcher;
/*  15:    */   
/*  16:    */   protected MessageToMessageEncoder()
/*  17:    */   {
/*  18: 59 */     this.matcher = TypeParameterMatcher.find(this, MessageToMessageEncoder.class, "I");
/*  19:    */   }
/*  20:    */   
/*  21:    */   protected MessageToMessageEncoder(Class<? extends I> outboundMessageType)
/*  22:    */   {
/*  23: 68 */     this.matcher = TypeParameterMatcher.get(outboundMessageType);
/*  24:    */   }
/*  25:    */   
/*  26:    */   public boolean acceptOutboundMessage(Object msg)
/*  27:    */     throws Exception
/*  28:    */   {
/*  29: 76 */     return this.matcher.match(msg);
/*  30:    */   }
/*  31:    */   
/*  32:    */   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
/*  33:    */     throws Exception
/*  34:    */   {
/*  35: 81 */     CodecOutputList out = null;
/*  36:    */     try
/*  37:    */     {
/*  38: 83 */       if (acceptOutboundMessage(msg))
/*  39:    */       {
/*  40: 84 */         out = CodecOutputList.newInstance();
/*  41:    */         
/*  42: 86 */         I cast = msg;
/*  43:    */         try
/*  44:    */         {
/*  45: 88 */           encode(ctx, cast, out);
/*  46:    */         }
/*  47:    */         finally
/*  48:    */         {
/*  49: 90 */           ReferenceCountUtil.release(cast);
/*  50:    */         }
/*  51: 93 */         if (out.isEmpty())
/*  52:    */         {
/*  53: 94 */           out.recycle();
/*  54: 95 */           out = null;
/*  55:    */           
/*  56:    */ 
/*  57: 98 */           throw new EncoderException(StringUtil.simpleClassName(this) + " must produce at least one message.");
/*  58:    */         }
/*  59:    */       }
/*  60:    */       else
/*  61:    */       {
/*  62:101 */         ctx.write(msg, promise);
/*  63:    */       }
/*  64:    */     }
/*  65:    */     catch (EncoderException e)
/*  66:    */     {
/*  67:    */       int sizeMinusOne;
/*  68:    */       ChannelPromise voidPromise;
/*  69:    */       boolean isVoidPromise;
/*  70:    */       int i;
/*  71:    */       ChannelPromise p;
/*  72:    */       ChannelPromise p;
/*  73:104 */       throw e;
/*  74:    */     }
/*  75:    */     catch (Throwable t)
/*  76:    */     {
/*  77:106 */       throw new EncoderException(t);
/*  78:    */     }
/*  79:    */     finally
/*  80:    */     {
/*  81:108 */       if (out != null)
/*  82:    */       {
/*  83:109 */         int sizeMinusOne = out.size() - 1;
/*  84:110 */         if (sizeMinusOne == 0)
/*  85:    */         {
/*  86:111 */           ctx.write(out.get(0), promise);
/*  87:    */         }
/*  88:112 */         else if (sizeMinusOne > 0)
/*  89:    */         {
/*  90:115 */           ChannelPromise voidPromise = ctx.voidPromise();
/*  91:116 */           boolean isVoidPromise = promise == voidPromise;
/*  92:117 */           for (int i = 0; i < sizeMinusOne; i++)
/*  93:    */           {
/*  94:    */             ChannelPromise p;
/*  95:    */             ChannelPromise p;
/*  96:119 */             if (isVoidPromise) {
/*  97:120 */               p = voidPromise;
/*  98:    */             } else {
/*  99:122 */               p = ctx.newPromise();
/* 100:    */             }
/* 101:124 */             ctx.write(out.getUnsafe(i), p);
/* 102:    */           }
/* 103:126 */           ctx.write(out.getUnsafe(sizeMinusOne), promise);
/* 104:    */         }
/* 105:128 */         out.recycle();
/* 106:    */       }
/* 107:    */     }
/* 108:    */   }
/* 109:    */   
/* 110:    */   protected abstract void encode(ChannelHandlerContext paramChannelHandlerContext, I paramI, List<Object> paramList)
/* 111:    */     throws Exception;
/* 112:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.MessageToMessageEncoder
 * JD-Core Version:    0.7.0.1
 */