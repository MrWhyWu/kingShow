/*  1:   */ package io.netty.util;
/*  2:   */ 
/*  3:   */ @Deprecated
/*  4:   */ public final class DomainMappingBuilder<V>
/*  5:   */ {
/*  6:   */   private final DomainNameMappingBuilder<V> builder;
/*  7:   */   
/*  8:   */   public DomainMappingBuilder(V defaultValue)
/*  9:   */   {
/* 10:37 */     this.builder = new DomainNameMappingBuilder(defaultValue);
/* 11:   */   }
/* 12:   */   
/* 13:   */   public DomainMappingBuilder(int initialCapacity, V defaultValue)
/* 14:   */   {
/* 15:48 */     this.builder = new DomainNameMappingBuilder(initialCapacity, defaultValue);
/* 16:   */   }
/* 17:   */   
/* 18:   */   public DomainMappingBuilder<V> add(String hostname, V output)
/* 19:   */   {
/* 20:64 */     this.builder.add(hostname, output);
/* 21:65 */     return this;
/* 22:   */   }
/* 23:   */   
/* 24:   */   public DomainNameMapping<V> build()
/* 25:   */   {
/* 26:75 */     return this.builder.build();
/* 27:   */   }
/* 28:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.DomainMappingBuilder
 * JD-Core Version:    0.7.0.1
 */