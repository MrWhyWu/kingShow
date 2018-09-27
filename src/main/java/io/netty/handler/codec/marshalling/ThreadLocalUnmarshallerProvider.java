/*  1:   */ package io.netty.handler.codec.marshalling;
/*  2:   */ 
/*  3:   */ import io.netty.channel.ChannelHandlerContext;
/*  4:   */ import io.netty.util.concurrent.FastThreadLocal;
/*  5:   */ import org.jboss.marshalling.MarshallerFactory;
/*  6:   */ import org.jboss.marshalling.MarshallingConfiguration;
/*  7:   */ import org.jboss.marshalling.Unmarshaller;
/*  8:   */ 
/*  9:   */ public class ThreadLocalUnmarshallerProvider
/* 10:   */   implements UnmarshallerProvider
/* 11:   */ {
/* 12:31 */   private final FastThreadLocal<Unmarshaller> unmarshallers = new FastThreadLocal();
/* 13:   */   private final MarshallerFactory factory;
/* 14:   */   private final MarshallingConfiguration config;
/* 15:   */   
/* 16:   */   public ThreadLocalUnmarshallerProvider(MarshallerFactory factory, MarshallingConfiguration config)
/* 17:   */   {
/* 18:43 */     this.factory = factory;
/* 19:44 */     this.config = config;
/* 20:   */   }
/* 21:   */   
/* 22:   */   public Unmarshaller getUnmarshaller(ChannelHandlerContext ctx)
/* 23:   */     throws Exception
/* 24:   */   {
/* 25:49 */     Unmarshaller unmarshaller = (Unmarshaller)this.unmarshallers.get();
/* 26:50 */     if (unmarshaller == null)
/* 27:   */     {
/* 28:51 */       unmarshaller = this.factory.createUnmarshaller(this.config);
/* 29:52 */       this.unmarshallers.set(unmarshaller);
/* 30:   */     }
/* 31:54 */     return unmarshaller;
/* 32:   */   }
/* 33:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.marshalling.ThreadLocalUnmarshallerProvider
 * JD-Core Version:    0.7.0.1
 */