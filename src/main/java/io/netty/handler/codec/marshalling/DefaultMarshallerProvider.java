/*  1:   */ package io.netty.handler.codec.marshalling;
/*  2:   */ 
/*  3:   */ import io.netty.channel.ChannelHandlerContext;
/*  4:   */ import org.jboss.marshalling.Marshaller;
/*  5:   */ import org.jboss.marshalling.MarshallerFactory;
/*  6:   */ import org.jboss.marshalling.MarshallingConfiguration;
/*  7:   */ 
/*  8:   */ public class DefaultMarshallerProvider
/*  9:   */   implements MarshallerProvider
/* 10:   */ {
/* 11:   */   private final MarshallerFactory factory;
/* 12:   */   private final MarshallingConfiguration config;
/* 13:   */   
/* 14:   */   public DefaultMarshallerProvider(MarshallerFactory factory, MarshallingConfiguration config)
/* 15:   */   {
/* 16:40 */     this.factory = factory;
/* 17:41 */     this.config = config;
/* 18:   */   }
/* 19:   */   
/* 20:   */   public Marshaller getMarshaller(ChannelHandlerContext ctx)
/* 21:   */     throws Exception
/* 22:   */   {
/* 23:46 */     return this.factory.createMarshaller(this.config);
/* 24:   */   }
/* 25:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.marshalling.DefaultMarshallerProvider
 * JD-Core Version:    0.7.0.1
 */