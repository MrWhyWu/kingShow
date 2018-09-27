/*  1:   */ package io.netty.util.concurrent;
/*  2:   */ 
/*  3:   */ import java.util.concurrent.Executor;
/*  4:   */ 
/*  5:   */ public final class ImmediateExecutor
/*  6:   */   implements Executor
/*  7:   */ {
/*  8:24 */   public static final ImmediateExecutor INSTANCE = new ImmediateExecutor();
/*  9:   */   
/* 10:   */   public void execute(Runnable command)
/* 11:   */   {
/* 12:32 */     if (command == null) {
/* 13:33 */       throw new NullPointerException("command");
/* 14:   */     }
/* 15:35 */     command.run();
/* 16:   */   }
/* 17:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.concurrent.ImmediateExecutor
 * JD-Core Version:    0.7.0.1
 */