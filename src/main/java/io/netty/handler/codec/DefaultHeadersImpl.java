/*  1:   */ package io.netty.handler.codec;
/*  2:   */ 
/*  3:   */ import io.netty.util.HashingStrategy;
/*  4:   */ 
/*  5:   */ public final class DefaultHeadersImpl<K, V>
/*  6:   */   extends DefaultHeaders<K, V, DefaultHeadersImpl<K, V>>
/*  7:   */ {
/*  8:   */   public DefaultHeadersImpl(HashingStrategy<K> nameHashingStrategy, ValueConverter<V> valueConverter, DefaultHeaders.NameValidator<K> nameValidator)
/*  9:   */   {
/* 10:27 */     super(nameHashingStrategy, valueConverter, nameValidator);
/* 11:   */   }
/* 12:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.DefaultHeadersImpl
 * JD-Core Version:    0.7.0.1
 */