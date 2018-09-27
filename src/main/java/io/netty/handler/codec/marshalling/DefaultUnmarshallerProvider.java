/*  1:   */ package io.netty.handler.codec.marshalling;
/*  2:   */ 
/*  3:   */ import io.netty.channel.ChannelHandlerContext;
/*  4:   */ import org.jboss.marshalling.MarshallerFactory;
/*  5:   */ import org.jboss.marshalling.MarshallingConfiguration;
/*  6:   */ import org.jboss.marshalling.Unmarshaller;
/*  7:   */ 
/*  8:   */ public class DefaultUnmarshallerProvider
/*  9:   */   implements UnmarshallerProvider
/* 10:   */ {
/* 11:   */   private final MarshallerFactory factory;
/* 12:   */   private final MarshallingConfiguration config;
/* 13:   */   
/* 14:   */   public DefaultUnmarshallerProvider(MarshallerFactory factory, MarshallingConfiguration config)
/* 15:   */   {
/* 16:41 */     this.factory = factory;
/* 17:42 */     this.config = config;
/* 18:   */   }
/* 19:   */   
/* 20:   */   public Unmarshaller getUnmarshaller(ChannelHandlerContext ctx)
/* 21:   */     throws Exception
/* 22:   */   {
/* 23:47 */     return this.factory.createUnmarshaller(this.config);
/* 24:   */   }
/* 25:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.marshalling.DefaultUnmarshallerProvider
 * JD-Core Version:    0.7.0.1
 */