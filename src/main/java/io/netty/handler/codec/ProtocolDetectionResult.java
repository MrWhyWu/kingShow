/*  1:   */ package io.netty.handler.codec;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.ObjectUtil;
/*  4:   */ 
/*  5:   */ public final class ProtocolDetectionResult<T>
/*  6:   */ {
/*  7:28 */   private static final ProtocolDetectionResult NEEDS_MORE_DATE = new ProtocolDetectionResult(ProtocolDetectionState.NEEDS_MORE_DATA, null);
/*  8:31 */   private static final ProtocolDetectionResult INVALID = new ProtocolDetectionResult(ProtocolDetectionState.INVALID, null);
/*  9:   */   private final ProtocolDetectionState state;
/* 10:   */   private final T result;
/* 11:   */   
/* 12:   */   public static <T> ProtocolDetectionResult<T> needsMoreData()
/* 13:   */   {
/* 14:42 */     return NEEDS_MORE_DATE;
/* 15:   */   }
/* 16:   */   
/* 17:   */   public static <T> ProtocolDetectionResult<T> invalid()
/* 18:   */   {
/* 19:50 */     return INVALID;
/* 20:   */   }
/* 21:   */   
/* 22:   */   public static <T> ProtocolDetectionResult<T> detected(T protocol)
/* 23:   */   {
/* 24:58 */     return new ProtocolDetectionResult(ProtocolDetectionState.DETECTED, ObjectUtil.checkNotNull(protocol, "protocol"));
/* 25:   */   }
/* 26:   */   
/* 27:   */   private ProtocolDetectionResult(ProtocolDetectionState state, T result)
/* 28:   */   {
/* 29:62 */     this.state = state;
/* 30:63 */     this.result = result;
/* 31:   */   }
/* 32:   */   
/* 33:   */   public ProtocolDetectionState state()
/* 34:   */   {
/* 35:71 */     return this.state;
/* 36:   */   }
/* 37:   */   
/* 38:   */   public T detectedProtocol()
/* 39:   */   {
/* 40:78 */     return this.result;
/* 41:   */   }
/* 42:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.ProtocolDetectionResult
 * JD-Core Version:    0.7.0.1
 */