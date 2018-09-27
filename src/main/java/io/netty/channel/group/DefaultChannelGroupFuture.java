/*   1:    */ package io.netty.channel.group;
/*   2:    */ 
/*   3:    */ import io.netty.channel.Channel;
/*   4:    */ import io.netty.channel.ChannelFuture;
/*   5:    */ import io.netty.channel.ChannelFutureListener;
/*   6:    */ import io.netty.util.concurrent.BlockingOperationException;
/*   7:    */ import io.netty.util.concurrent.DefaultPromise;
/*   8:    */ import io.netty.util.concurrent.EventExecutor;
/*   9:    */ import io.netty.util.concurrent.Future;
/*  10:    */ import io.netty.util.concurrent.GenericFutureListener;
/*  11:    */ import io.netty.util.concurrent.ImmediateEventExecutor;
/*  12:    */ import java.util.ArrayList;
/*  13:    */ import java.util.Collection;
/*  14:    */ import java.util.Collections;
/*  15:    */ import java.util.Iterator;
/*  16:    */ import java.util.LinkedHashMap;
/*  17:    */ import java.util.List;
/*  18:    */ import java.util.Map;
/*  19:    */ import java.util.Map.Entry;
/*  20:    */ 
/*  21:    */ final class DefaultChannelGroupFuture
/*  22:    */   extends DefaultPromise<Void>
/*  23:    */   implements ChannelGroupFuture
/*  24:    */ {
/*  25:    */   private final ChannelGroup group;
/*  26:    */   private final Map<Channel, ChannelFuture> futures;
/*  27:    */   private int successCount;
/*  28:    */   private int failureCount;
/*  29: 47 */   private final ChannelFutureListener childListener = new ChannelFutureListener()
/*  30:    */   {
/*  31:    */     public void operationComplete(ChannelFuture future)
/*  32:    */       throws Exception
/*  33:    */     {
/*  34: 50 */       boolean success = future.isSuccess();
/*  35: 52 */       synchronized (DefaultChannelGroupFuture.this)
/*  36:    */       {
/*  37: 53 */         if (success) {
/*  38: 54 */           DefaultChannelGroupFuture.access$008(DefaultChannelGroupFuture.this);
/*  39:    */         } else {
/*  40: 56 */           DefaultChannelGroupFuture.access$108(DefaultChannelGroupFuture.this);
/*  41:    */         }
/*  42: 59 */         boolean callSetDone = DefaultChannelGroupFuture.this.successCount + DefaultChannelGroupFuture.this.failureCount == DefaultChannelGroupFuture.this.futures.size();
/*  43: 60 */         if ((!$assertionsDisabled) && (DefaultChannelGroupFuture.this.successCount + DefaultChannelGroupFuture.this.failureCount > DefaultChannelGroupFuture.this.futures.size())) {
/*  44: 60 */           throw new AssertionError();
/*  45:    */         }
/*  46:    */       }
/*  47:    */       boolean callSetDone;
/*  48: 63 */       if (callSetDone) {
/*  49: 64 */         if (DefaultChannelGroupFuture.this.failureCount > 0)
/*  50:    */         {
/*  51: 66 */           List<Map.Entry<Channel, Throwable>> failed = new ArrayList(DefaultChannelGroupFuture.this.failureCount);
/*  52: 67 */           for (ChannelFuture f : DefaultChannelGroupFuture.this.futures.values()) {
/*  53: 68 */             if (!f.isSuccess()) {
/*  54: 69 */               failed.add(new DefaultChannelGroupFuture.DefaultEntry(f.channel(), f.cause()));
/*  55:    */             }
/*  56:    */           }
/*  57: 72 */           DefaultChannelGroupFuture.this.setFailure0(new ChannelGroupException(failed));
/*  58:    */         }
/*  59:    */         else
/*  60:    */         {
/*  61: 74 */           DefaultChannelGroupFuture.this.setSuccess0();
/*  62:    */         }
/*  63:    */       }
/*  64:    */     }
/*  65:    */   };
/*  66:    */   
/*  67:    */   DefaultChannelGroupFuture(ChannelGroup group, Collection<ChannelFuture> futures, EventExecutor executor)
/*  68:    */   {
/*  69: 84 */     super(executor);
/*  70: 85 */     if (group == null) {
/*  71: 86 */       throw new NullPointerException("group");
/*  72:    */     }
/*  73: 88 */     if (futures == null) {
/*  74: 89 */       throw new NullPointerException("futures");
/*  75:    */     }
/*  76: 92 */     this.group = group;
/*  77:    */     
/*  78: 94 */     Map<Channel, ChannelFuture> futureMap = new LinkedHashMap();
/*  79: 95 */     for (ChannelFuture f : futures) {
/*  80: 96 */       futureMap.put(f.channel(), f);
/*  81:    */     }
/*  82: 99 */     this.futures = Collections.unmodifiableMap(futureMap);
/*  83:101 */     for (ChannelFuture f : this.futures.values()) {
/*  84:102 */       f.addListener(this.childListener);
/*  85:    */     }
/*  86:106 */     if (this.futures.isEmpty()) {
/*  87:107 */       setSuccess0();
/*  88:    */     }
/*  89:    */   }
/*  90:    */   
/*  91:    */   DefaultChannelGroupFuture(ChannelGroup group, Map<Channel, ChannelFuture> futures, EventExecutor executor)
/*  92:    */   {
/*  93:112 */     super(executor);
/*  94:113 */     this.group = group;
/*  95:114 */     this.futures = Collections.unmodifiableMap(futures);
/*  96:115 */     for (ChannelFuture f : this.futures.values()) {
/*  97:116 */       f.addListener(this.childListener);
/*  98:    */     }
/*  99:120 */     if (this.futures.isEmpty()) {
/* 100:121 */       setSuccess0();
/* 101:    */     }
/* 102:    */   }
/* 103:    */   
/* 104:    */   public ChannelGroup group()
/* 105:    */   {
/* 106:127 */     return this.group;
/* 107:    */   }
/* 108:    */   
/* 109:    */   public ChannelFuture find(Channel channel)
/* 110:    */   {
/* 111:132 */     return (ChannelFuture)this.futures.get(channel);
/* 112:    */   }
/* 113:    */   
/* 114:    */   public Iterator<ChannelFuture> iterator()
/* 115:    */   {
/* 116:137 */     return this.futures.values().iterator();
/* 117:    */   }
/* 118:    */   
/* 119:    */   public synchronized boolean isPartialSuccess()
/* 120:    */   {
/* 121:142 */     return (this.successCount != 0) && (this.successCount != this.futures.size());
/* 122:    */   }
/* 123:    */   
/* 124:    */   public synchronized boolean isPartialFailure()
/* 125:    */   {
/* 126:147 */     return (this.failureCount != 0) && (this.failureCount != this.futures.size());
/* 127:    */   }
/* 128:    */   
/* 129:    */   public DefaultChannelGroupFuture addListener(GenericFutureListener<? extends Future<? super Void>> listener)
/* 130:    */   {
/* 131:152 */     super.addListener(listener);
/* 132:153 */     return this;
/* 133:    */   }
/* 134:    */   
/* 135:    */   public DefaultChannelGroupFuture addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners)
/* 136:    */   {
/* 137:158 */     super.addListeners(listeners);
/* 138:159 */     return this;
/* 139:    */   }
/* 140:    */   
/* 141:    */   public DefaultChannelGroupFuture removeListener(GenericFutureListener<? extends Future<? super Void>> listener)
/* 142:    */   {
/* 143:164 */     super.removeListener(listener);
/* 144:165 */     return this;
/* 145:    */   }
/* 146:    */   
/* 147:    */   public DefaultChannelGroupFuture removeListeners(GenericFutureListener<? extends Future<? super Void>>... listeners)
/* 148:    */   {
/* 149:171 */     super.removeListeners(listeners);
/* 150:172 */     return this;
/* 151:    */   }
/* 152:    */   
/* 153:    */   public DefaultChannelGroupFuture await()
/* 154:    */     throws InterruptedException
/* 155:    */   {
/* 156:177 */     super.await();
/* 157:178 */     return this;
/* 158:    */   }
/* 159:    */   
/* 160:    */   public DefaultChannelGroupFuture awaitUninterruptibly()
/* 161:    */   {
/* 162:183 */     super.awaitUninterruptibly();
/* 163:184 */     return this;
/* 164:    */   }
/* 165:    */   
/* 166:    */   public DefaultChannelGroupFuture syncUninterruptibly()
/* 167:    */   {
/* 168:189 */     super.syncUninterruptibly();
/* 169:190 */     return this;
/* 170:    */   }
/* 171:    */   
/* 172:    */   public DefaultChannelGroupFuture sync()
/* 173:    */     throws InterruptedException
/* 174:    */   {
/* 175:195 */     super.sync();
/* 176:196 */     return this;
/* 177:    */   }
/* 178:    */   
/* 179:    */   public ChannelGroupException cause()
/* 180:    */   {
/* 181:201 */     return (ChannelGroupException)super.cause();
/* 182:    */   }
/* 183:    */   
/* 184:    */   private void setSuccess0()
/* 185:    */   {
/* 186:205 */     super.setSuccess(null);
/* 187:    */   }
/* 188:    */   
/* 189:    */   private void setFailure0(ChannelGroupException cause)
/* 190:    */   {
/* 191:209 */     super.setFailure(cause);
/* 192:    */   }
/* 193:    */   
/* 194:    */   public DefaultChannelGroupFuture setSuccess(Void result)
/* 195:    */   {
/* 196:214 */     throw new IllegalStateException();
/* 197:    */   }
/* 198:    */   
/* 199:    */   public boolean trySuccess(Void result)
/* 200:    */   {
/* 201:219 */     throw new IllegalStateException();
/* 202:    */   }
/* 203:    */   
/* 204:    */   public DefaultChannelGroupFuture setFailure(Throwable cause)
/* 205:    */   {
/* 206:224 */     throw new IllegalStateException();
/* 207:    */   }
/* 208:    */   
/* 209:    */   public boolean tryFailure(Throwable cause)
/* 210:    */   {
/* 211:229 */     throw new IllegalStateException();
/* 212:    */   }
/* 213:    */   
/* 214:    */   protected void checkDeadLock()
/* 215:    */   {
/* 216:234 */     EventExecutor e = executor();
/* 217:235 */     if ((e != null) && (e != ImmediateEventExecutor.INSTANCE) && (e.inEventLoop())) {
/* 218:236 */       throw new BlockingOperationException();
/* 219:    */     }
/* 220:    */   }
/* 221:    */   
/* 222:    */   private static final class DefaultEntry<K, V>
/* 223:    */     implements Map.Entry<K, V>
/* 224:    */   {
/* 225:    */     private final K key;
/* 226:    */     private final V value;
/* 227:    */     
/* 228:    */     DefaultEntry(K key, V value)
/* 229:    */     {
/* 230:245 */       this.key = key;
/* 231:246 */       this.value = value;
/* 232:    */     }
/* 233:    */     
/* 234:    */     public K getKey()
/* 235:    */     {
/* 236:251 */       return this.key;
/* 237:    */     }
/* 238:    */     
/* 239:    */     public V getValue()
/* 240:    */     {
/* 241:256 */       return this.value;
/* 242:    */     }
/* 243:    */     
/* 244:    */     public V setValue(V value)
/* 245:    */     {
/* 246:261 */       throw new UnsupportedOperationException("read-only");
/* 247:    */     }
/* 248:    */   }
/* 249:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.group.DefaultChannelGroupFuture
 * JD-Core Version:    0.7.0.1
 */