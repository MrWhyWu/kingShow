/*  1:   */ package io.netty.handler.codec.marshalling;
/*  2:   */ 
/*  3:   */ import io.netty.channel.ChannelHandlerContext;
/*  4:   */ import io.netty.util.Attribute;
/*  5:   */ import io.netty.util.AttributeKey;
/*  6:   */ import org.jboss.marshalling.MarshallerFactory;
/*  7:   */ import org.jboss.marshalling.MarshallingConfiguration;
/*  8:   */ import org.jboss.marshalling.Unmarshaller;
/*  9:   */ 
/* 10:   */ public class ContextBoundUnmarshallerProvider
/* 11:   */   extends DefaultUnmarshallerProvider
/* 12:   */ {
/* 13:37 */   private static final AttributeKey<Unmarshaller> UNMARSHALLER = AttributeKey.valueOf(ContextBoundUnmarshallerProvider.class, "UNMARSHALLER");
/* 14:   */   
/* 15:   */   public ContextBoundUnmarshallerProvider(MarshallerFactory factory, MarshallingConfiguration config)
/* 16:   */   {
/* 17:41 */     super(factory, config);
/* 18:   */   }
/* 19:   */   
/* 20:   */   public Unmarshaller getUnmarshaller(ChannelHandlerContext ctx)
/* 21:   */     throws Exception
/* 22:   */   {
/* 23:46 */     Attribute<Unmarshaller> attr = ctx.attr(UNMARSHALLER);
/* 24:47 */     Unmarshaller unmarshaller = (Unmarshaller)attr.get();
/* 25:48 */     if (unmarshaller == null)
/* 26:   */     {
/* 27:49 */       unmarshaller = super.getUnmarshaller(ctx);
/* 28:50 */       attr.set(unmarshaller);
/* 29:   */     }
/* 30:52 */     return unmarshaller;
/* 31:   */   }
/* 32:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.marshalling.ContextBoundUnmarshallerProvider
 * JD-Core Version:    0.7.0.1
 */