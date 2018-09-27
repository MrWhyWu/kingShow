/*  1:   */ package io.netty.channel;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.StringUtil;
/*  4:   */ import java.lang.reflect.Constructor;
/*  5:   */ 
/*  6:   */ public class ReflectiveChannelFactory<T extends Channel>
/*  7:   */   implements ChannelFactory<T>
/*  8:   */ {
/*  9:   */   private final Class<? extends T> clazz;
/* 10:   */   
/* 11:   */   public ReflectiveChannelFactory(Class<? extends T> clazz)
/* 12:   */   {
/* 13:29 */     if (clazz == null) {
/* 14:30 */       throw new NullPointerException("clazz");
/* 15:   */     }
/* 16:32 */     this.clazz = clazz;
/* 17:   */   }
/* 18:   */   
/* 19:   */   public T newChannel()
/* 20:   */   {
/* 21:   */     try
/* 22:   */     {
/* 23:38 */       return (Channel)this.clazz.getConstructor(new Class[0]).newInstance(new Object[0]);
/* 24:   */     }
/* 25:   */     catch (Throwable t)
/* 26:   */     {
/* 27:40 */       throw new ChannelException("Unable to create Channel from class " + this.clazz, t);
/* 28:   */     }
/* 29:   */   }
/* 30:   */   
/* 31:   */   public String toString()
/* 32:   */   {
/* 33:46 */     return StringUtil.simpleClassName(this.clazz) + ".class";
/* 34:   */   }
/* 35:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.ReflectiveChannelFactory
 * JD-Core Version:    0.7.0.1
 */