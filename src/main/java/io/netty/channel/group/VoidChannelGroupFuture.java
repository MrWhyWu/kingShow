/*   1:    */ package io.netty.channel.group;
/*   2:    */ 
/*   3:    */ import io.netty.channel.Channel;
/*   4:    */ import io.netty.channel.ChannelFuture;
/*   5:    */ import io.netty.util.concurrent.Future;
/*   6:    */ import io.netty.util.concurrent.GenericFutureListener;
/*   7:    */ import java.util.Collections;
/*   8:    */ import java.util.Iterator;
/*   9:    */ import java.util.List;
/*  10:    */ import java.util.concurrent.TimeUnit;
/*  11:    */ 
/*  12:    */ final class VoidChannelGroupFuture
/*  13:    */   implements ChannelGroupFuture
/*  14:    */ {
/*  15: 29 */   private static final Iterator<ChannelFuture> EMPTY = Collections.emptyList().iterator();
/*  16:    */   private final ChannelGroup group;
/*  17:    */   
/*  18:    */   VoidChannelGroupFuture(ChannelGroup group)
/*  19:    */   {
/*  20: 33 */     this.group = group;
/*  21:    */   }
/*  22:    */   
/*  23:    */   public ChannelGroup group()
/*  24:    */   {
/*  25: 38 */     return this.group;
/*  26:    */   }
/*  27:    */   
/*  28:    */   public ChannelFuture find(Channel channel)
/*  29:    */   {
/*  30: 43 */     return null;
/*  31:    */   }
/*  32:    */   
/*  33:    */   public boolean isSuccess()
/*  34:    */   {
/*  35: 48 */     return false;
/*  36:    */   }
/*  37:    */   
/*  38:    */   public ChannelGroupException cause()
/*  39:    */   {
/*  40: 53 */     return null;
/*  41:    */   }
/*  42:    */   
/*  43:    */   public boolean isPartialSuccess()
/*  44:    */   {
/*  45: 58 */     return false;
/*  46:    */   }
/*  47:    */   
/*  48:    */   public boolean isPartialFailure()
/*  49:    */   {
/*  50: 63 */     return false;
/*  51:    */   }
/*  52:    */   
/*  53:    */   public ChannelGroupFuture addListener(GenericFutureListener<? extends Future<? super Void>> listener)
/*  54:    */   {
/*  55: 68 */     throw reject();
/*  56:    */   }
/*  57:    */   
/*  58:    */   public ChannelGroupFuture addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners)
/*  59:    */   {
/*  60: 73 */     throw reject();
/*  61:    */   }
/*  62:    */   
/*  63:    */   public ChannelGroupFuture removeListener(GenericFutureListener<? extends Future<? super Void>> listener)
/*  64:    */   {
/*  65: 78 */     throw reject();
/*  66:    */   }
/*  67:    */   
/*  68:    */   public ChannelGroupFuture removeListeners(GenericFutureListener<? extends Future<? super Void>>... listeners)
/*  69:    */   {
/*  70: 83 */     throw reject();
/*  71:    */   }
/*  72:    */   
/*  73:    */   public ChannelGroupFuture await()
/*  74:    */   {
/*  75: 88 */     throw reject();
/*  76:    */   }
/*  77:    */   
/*  78:    */   public ChannelGroupFuture awaitUninterruptibly()
/*  79:    */   {
/*  80: 93 */     throw reject();
/*  81:    */   }
/*  82:    */   
/*  83:    */   public ChannelGroupFuture syncUninterruptibly()
/*  84:    */   {
/*  85: 98 */     throw reject();
/*  86:    */   }
/*  87:    */   
/*  88:    */   public ChannelGroupFuture sync()
/*  89:    */   {
/*  90:103 */     throw reject();
/*  91:    */   }
/*  92:    */   
/*  93:    */   public Iterator<ChannelFuture> iterator()
/*  94:    */   {
/*  95:108 */     return EMPTY;
/*  96:    */   }
/*  97:    */   
/*  98:    */   public boolean isCancellable()
/*  99:    */   {
/* 100:113 */     return false;
/* 101:    */   }
/* 102:    */   
/* 103:    */   public boolean await(long timeout, TimeUnit unit)
/* 104:    */   {
/* 105:118 */     throw reject();
/* 106:    */   }
/* 107:    */   
/* 108:    */   public boolean await(long timeoutMillis)
/* 109:    */   {
/* 110:123 */     throw reject();
/* 111:    */   }
/* 112:    */   
/* 113:    */   public boolean awaitUninterruptibly(long timeout, TimeUnit unit)
/* 114:    */   {
/* 115:128 */     throw reject();
/* 116:    */   }
/* 117:    */   
/* 118:    */   public boolean awaitUninterruptibly(long timeoutMillis)
/* 119:    */   {
/* 120:133 */     throw reject();
/* 121:    */   }
/* 122:    */   
/* 123:    */   public Void getNow()
/* 124:    */   {
/* 125:138 */     return null;
/* 126:    */   }
/* 127:    */   
/* 128:    */   public boolean cancel(boolean mayInterruptIfRunning)
/* 129:    */   {
/* 130:143 */     return false;
/* 131:    */   }
/* 132:    */   
/* 133:    */   public boolean isCancelled()
/* 134:    */   {
/* 135:148 */     return false;
/* 136:    */   }
/* 137:    */   
/* 138:    */   public boolean isDone()
/* 139:    */   {
/* 140:153 */     return false;
/* 141:    */   }
/* 142:    */   
/* 143:    */   public Void get()
/* 144:    */   {
/* 145:158 */     throw reject();
/* 146:    */   }
/* 147:    */   
/* 148:    */   public Void get(long timeout, TimeUnit unit)
/* 149:    */   {
/* 150:163 */     throw reject();
/* 151:    */   }
/* 152:    */   
/* 153:    */   private static RuntimeException reject()
/* 154:    */   {
/* 155:167 */     return new IllegalStateException("void future");
/* 156:    */   }
/* 157:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.group.VoidChannelGroupFuture
 * JD-Core Version:    0.7.0.1
 */