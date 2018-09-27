/*  1:   */ package io.netty.handler.timeout;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.ObjectUtil;
/*  4:   */ 
/*  5:   */ public class IdleStateEvent
/*  6:   */ {
/*  7:25 */   public static final IdleStateEvent FIRST_READER_IDLE_STATE_EVENT = new IdleStateEvent(IdleState.READER_IDLE, true);
/*  8:26 */   public static final IdleStateEvent READER_IDLE_STATE_EVENT = new IdleStateEvent(IdleState.READER_IDLE, false);
/*  9:27 */   public static final IdleStateEvent FIRST_WRITER_IDLE_STATE_EVENT = new IdleStateEvent(IdleState.WRITER_IDLE, true);
/* 10:28 */   public static final IdleStateEvent WRITER_IDLE_STATE_EVENT = new IdleStateEvent(IdleState.WRITER_IDLE, false);
/* 11:29 */   public static final IdleStateEvent FIRST_ALL_IDLE_STATE_EVENT = new IdleStateEvent(IdleState.ALL_IDLE, true);
/* 12:30 */   public static final IdleStateEvent ALL_IDLE_STATE_EVENT = new IdleStateEvent(IdleState.ALL_IDLE, false);
/* 13:   */   private final IdleState state;
/* 14:   */   private final boolean first;
/* 15:   */   
/* 16:   */   protected IdleStateEvent(IdleState state, boolean first)
/* 17:   */   {
/* 18:42 */     this.state = ((IdleState)ObjectUtil.checkNotNull(state, "state"));
/* 19:43 */     this.first = first;
/* 20:   */   }
/* 21:   */   
/* 22:   */   public IdleState state()
/* 23:   */   {
/* 24:50 */     return this.state;
/* 25:   */   }
/* 26:   */   
/* 27:   */   public boolean isFirst()
/* 28:   */   {
/* 29:57 */     return this.first;
/* 30:   */   }
/* 31:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.timeout.IdleStateEvent
 * JD-Core Version:    0.7.0.1
 */