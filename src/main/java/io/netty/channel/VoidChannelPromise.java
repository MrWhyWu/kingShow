/*   1:    */ package io.netty.channel;
/*   2:    */ 
/*   3:    */ import io.netty.util.concurrent.AbstractFuture;
/*   4:    */ import io.netty.util.concurrent.Future;
/*   5:    */ import io.netty.util.concurrent.GenericFutureListener;
/*   6:    */ import java.util.concurrent.TimeUnit;
/*   7:    */ 
/*   8:    */ public final class VoidChannelPromise
/*   9:    */   extends AbstractFuture<Void>
/*  10:    */   implements ChannelPromise
/*  11:    */ {
/*  12:    */   private final Channel channel;
/*  13:    */   private final boolean fireException;
/*  14:    */   
/*  15:    */   public VoidChannelPromise(Channel channel, boolean fireException)
/*  16:    */   {
/*  17: 37 */     if (channel == null) {
/*  18: 38 */       throw new NullPointerException("channel");
/*  19:    */     }
/*  20: 40 */     this.channel = channel;
/*  21: 41 */     this.fireException = fireException;
/*  22:    */   }
/*  23:    */   
/*  24:    */   public VoidChannelPromise addListener(GenericFutureListener<? extends Future<? super Void>> listener)
/*  25:    */   {
/*  26: 46 */     fail();
/*  27: 47 */     return this;
/*  28:    */   }
/*  29:    */   
/*  30:    */   public VoidChannelPromise addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners)
/*  31:    */   {
/*  32: 52 */     fail();
/*  33: 53 */     return this;
/*  34:    */   }
/*  35:    */   
/*  36:    */   public VoidChannelPromise removeListener(GenericFutureListener<? extends Future<? super Void>> listener)
/*  37:    */   {
/*  38: 59 */     return this;
/*  39:    */   }
/*  40:    */   
/*  41:    */   public VoidChannelPromise removeListeners(GenericFutureListener<? extends Future<? super Void>>... listeners)
/*  42:    */   {
/*  43: 65 */     return this;
/*  44:    */   }
/*  45:    */   
/*  46:    */   public VoidChannelPromise await()
/*  47:    */     throws InterruptedException
/*  48:    */   {
/*  49: 70 */     if (Thread.interrupted()) {
/*  50: 71 */       throw new InterruptedException();
/*  51:    */     }
/*  52: 73 */     return this;
/*  53:    */   }
/*  54:    */   
/*  55:    */   public boolean await(long timeout, TimeUnit unit)
/*  56:    */   {
/*  57: 78 */     fail();
/*  58: 79 */     return false;
/*  59:    */   }
/*  60:    */   
/*  61:    */   public boolean await(long timeoutMillis)
/*  62:    */   {
/*  63: 84 */     fail();
/*  64: 85 */     return false;
/*  65:    */   }
/*  66:    */   
/*  67:    */   public VoidChannelPromise awaitUninterruptibly()
/*  68:    */   {
/*  69: 90 */     fail();
/*  70: 91 */     return this;
/*  71:    */   }
/*  72:    */   
/*  73:    */   public boolean awaitUninterruptibly(long timeout, TimeUnit unit)
/*  74:    */   {
/*  75: 96 */     fail();
/*  76: 97 */     return false;
/*  77:    */   }
/*  78:    */   
/*  79:    */   public boolean awaitUninterruptibly(long timeoutMillis)
/*  80:    */   {
/*  81:102 */     fail();
/*  82:103 */     return false;
/*  83:    */   }
/*  84:    */   
/*  85:    */   public Channel channel()
/*  86:    */   {
/*  87:108 */     return this.channel;
/*  88:    */   }
/*  89:    */   
/*  90:    */   public boolean isDone()
/*  91:    */   {
/*  92:113 */     return false;
/*  93:    */   }
/*  94:    */   
/*  95:    */   public boolean isSuccess()
/*  96:    */   {
/*  97:118 */     return false;
/*  98:    */   }
/*  99:    */   
/* 100:    */   public boolean setUncancellable()
/* 101:    */   {
/* 102:123 */     return true;
/* 103:    */   }
/* 104:    */   
/* 105:    */   public boolean isCancellable()
/* 106:    */   {
/* 107:128 */     return false;
/* 108:    */   }
/* 109:    */   
/* 110:    */   public boolean isCancelled()
/* 111:    */   {
/* 112:133 */     return false;
/* 113:    */   }
/* 114:    */   
/* 115:    */   public Throwable cause()
/* 116:    */   {
/* 117:138 */     return null;
/* 118:    */   }
/* 119:    */   
/* 120:    */   public VoidChannelPromise sync()
/* 121:    */   {
/* 122:143 */     fail();
/* 123:144 */     return this;
/* 124:    */   }
/* 125:    */   
/* 126:    */   public VoidChannelPromise syncUninterruptibly()
/* 127:    */   {
/* 128:149 */     fail();
/* 129:150 */     return this;
/* 130:    */   }
/* 131:    */   
/* 132:    */   public VoidChannelPromise setFailure(Throwable cause)
/* 133:    */   {
/* 134:154 */     fireException(cause);
/* 135:155 */     return this;
/* 136:    */   }
/* 137:    */   
/* 138:    */   public VoidChannelPromise setSuccess()
/* 139:    */   {
/* 140:160 */     return this;
/* 141:    */   }
/* 142:    */   
/* 143:    */   public boolean tryFailure(Throwable cause)
/* 144:    */   {
/* 145:165 */     fireException(cause);
/* 146:166 */     return false;
/* 147:    */   }
/* 148:    */   
/* 149:    */   public boolean cancel(boolean mayInterruptIfRunning)
/* 150:    */   {
/* 151:171 */     return false;
/* 152:    */   }
/* 153:    */   
/* 154:    */   public boolean trySuccess()
/* 155:    */   {
/* 156:176 */     return false;
/* 157:    */   }
/* 158:    */   
/* 159:    */   private static void fail()
/* 160:    */   {
/* 161:180 */     throw new IllegalStateException("void future");
/* 162:    */   }
/* 163:    */   
/* 164:    */   public VoidChannelPromise setSuccess(Void result)
/* 165:    */   {
/* 166:185 */     return this;
/* 167:    */   }
/* 168:    */   
/* 169:    */   public boolean trySuccess(Void result)
/* 170:    */   {
/* 171:190 */     return false;
/* 172:    */   }
/* 173:    */   
/* 174:    */   public Void getNow()
/* 175:    */   {
/* 176:195 */     return null;
/* 177:    */   }
/* 178:    */   
/* 179:    */   public ChannelPromise unvoid()
/* 180:    */   {
/* 181:200 */     ChannelPromise promise = new DefaultChannelPromise(this.channel);
/* 182:201 */     if (this.fireException) {
/* 183:202 */       promise.addListener(new ChannelFutureListener()
/* 184:    */       {
/* 185:    */         public void operationComplete(ChannelFuture future)
/* 186:    */           throws Exception
/* 187:    */         {
/* 188:205 */           if (!future.isSuccess()) {
/* 189:206 */             VoidChannelPromise.this.fireException(future.cause());
/* 190:    */           }
/* 191:    */         }
/* 192:    */       });
/* 193:    */     }
/* 194:211 */     return promise;
/* 195:    */   }
/* 196:    */   
/* 197:    */   public boolean isVoid()
/* 198:    */   {
/* 199:216 */     return true;
/* 200:    */   }
/* 201:    */   
/* 202:    */   private void fireException(Throwable cause)
/* 203:    */   {
/* 204:224 */     if ((this.fireException) && (this.channel.isRegistered())) {
/* 205:225 */       this.channel.pipeline().fireExceptionCaught(cause);
/* 206:    */     }
/* 207:    */   }
/* 208:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.VoidChannelPromise
 * JD-Core Version:    0.7.0.1
 */