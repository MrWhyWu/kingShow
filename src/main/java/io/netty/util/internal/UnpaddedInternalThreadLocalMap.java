/*  1:   */ package io.netty.util.internal;
/*  2:   */ 
/*  3:   */ import java.nio.charset.Charset;
/*  4:   */ import java.nio.charset.CharsetDecoder;
/*  5:   */ import java.nio.charset.CharsetEncoder;
/*  6:   */ import java.util.ArrayList;
/*  7:   */ import java.util.Map;
/*  8:   */ import java.util.concurrent.atomic.AtomicInteger;
/*  9:   */ 
/* 10:   */ class UnpaddedInternalThreadLocalMap
/* 11:   */ {
/* 12:35 */   static final ThreadLocal<InternalThreadLocalMap> slowThreadLocalMap = new ThreadLocal();
/* 13:36 */   static final AtomicInteger nextIndex = new AtomicInteger();
/* 14:   */   Object[] indexedVariables;
/* 15:   */   int futureListenerStackDepth;
/* 16:   */   int localChannelReaderStackDepth;
/* 17:   */   Map<Class<?>, Boolean> handlerSharableCache;
/* 18:   */   IntegerHolder counterHashCode;
/* 19:   */   ThreadLocalRandom random;
/* 20:   */   Map<Class<?>, TypeParameterMatcher> typeParameterMatcherGetCache;
/* 21:   */   Map<Class<?>, Map<String, TypeParameterMatcher>> typeParameterMatcherFindCache;
/* 22:   */   StringBuilder stringBuilder;
/* 23:   */   Map<Charset, CharsetEncoder> charsetEncoderCache;
/* 24:   */   Map<Charset, CharsetDecoder> charsetDecoderCache;
/* 25:   */   ArrayList<Object> arrayList;
/* 26:   */   
/* 27:   */   UnpaddedInternalThreadLocalMap(Object[] indexedVariables)
/* 28:   */   {
/* 29:59 */     this.indexedVariables = indexedVariables;
/* 30:   */   }
/* 31:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.UnpaddedInternalThreadLocalMap
 * JD-Core Version:    0.7.0.1
 */