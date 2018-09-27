/*   1:    */ package io.netty.channel;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.util.internal.ObjectUtil;
/*   5:    */ import java.util.IdentityHashMap;
/*   6:    */ import java.util.Map;
/*   7:    */ import java.util.Map.Entry;
/*   8:    */ import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
/*   9:    */ import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
/*  10:    */ 
/*  11:    */ public class DefaultChannelConfig
/*  12:    */   implements ChannelConfig
/*  13:    */ {
/*  14: 44 */   private static final MessageSizeEstimator DEFAULT_MSG_SIZE_ESTIMATOR = DefaultMessageSizeEstimator.DEFAULT;
/*  15:    */   private static final int DEFAULT_CONNECT_TIMEOUT = 30000;
/*  16: 49 */   private static final AtomicIntegerFieldUpdater<DefaultChannelConfig> AUTOREAD_UPDATER = AtomicIntegerFieldUpdater.newUpdater(DefaultChannelConfig.class, "autoRead");
/*  17: 51 */   private static final AtomicReferenceFieldUpdater<DefaultChannelConfig, WriteBufferWaterMark> WATERMARK_UPDATER = AtomicReferenceFieldUpdater.newUpdater(DefaultChannelConfig.class, WriteBufferWaterMark.class, "writeBufferWaterMark");
/*  18:    */   protected final Channel channel;
/*  19: 56 */   private volatile ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;
/*  20:    */   private volatile RecvByteBufAllocator rcvBufAllocator;
/*  21: 58 */   private volatile MessageSizeEstimator msgSizeEstimator = DEFAULT_MSG_SIZE_ESTIMATOR;
/*  22: 60 */   private volatile int connectTimeoutMillis = 30000;
/*  23: 61 */   private volatile int writeSpinCount = 16;
/*  24: 62 */   private volatile int autoRead = 1;
/*  25: 64 */   private volatile boolean autoClose = true;
/*  26: 65 */   private volatile WriteBufferWaterMark writeBufferWaterMark = WriteBufferWaterMark.DEFAULT;
/*  27: 66 */   private volatile boolean pinEventExecutor = true;
/*  28:    */   
/*  29:    */   public DefaultChannelConfig(Channel channel)
/*  30:    */   {
/*  31: 69 */     this(channel, new AdaptiveRecvByteBufAllocator());
/*  32:    */   }
/*  33:    */   
/*  34:    */   protected DefaultChannelConfig(Channel channel, RecvByteBufAllocator allocator)
/*  35:    */   {
/*  36: 73 */     setRecvByteBufAllocator(allocator, channel.metadata());
/*  37: 74 */     this.channel = channel;
/*  38:    */   }
/*  39:    */   
/*  40:    */   public Map<ChannelOption<?>, Object> getOptions()
/*  41:    */   {
/*  42: 80 */     return getOptions(null, new ChannelOption[] { ChannelOption.CONNECT_TIMEOUT_MILLIS, ChannelOption.MAX_MESSAGES_PER_READ, ChannelOption.WRITE_SPIN_COUNT, ChannelOption.ALLOCATOR, ChannelOption.AUTO_READ, ChannelOption.AUTO_CLOSE, ChannelOption.RCVBUF_ALLOCATOR, ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, ChannelOption.WRITE_BUFFER_WATER_MARK, ChannelOption.MESSAGE_SIZE_ESTIMATOR, ChannelOption.SINGLE_EVENTEXECUTOR_PER_GROUP });
/*  43:    */   }
/*  44:    */   
/*  45:    */   protected Map<ChannelOption<?>, Object> getOptions(Map<ChannelOption<?>, Object> result, ChannelOption<?>... options)
/*  46:    */   {
/*  47: 90 */     if (result == null) {
/*  48: 91 */       result = new IdentityHashMap();
/*  49:    */     }
/*  50: 93 */     for (ChannelOption<?> o : options) {
/*  51: 94 */       result.put(o, getOption(o));
/*  52:    */     }
/*  53: 96 */     return result;
/*  54:    */   }
/*  55:    */   
/*  56:    */   public boolean setOptions(Map<ChannelOption<?>, ?> options)
/*  57:    */   {
/*  58:102 */     if (options == null) {
/*  59:103 */       throw new NullPointerException("options");
/*  60:    */     }
/*  61:106 */     boolean setAllOptions = true;
/*  62:107 */     for (Map.Entry<ChannelOption<?>, ?> e : options.entrySet()) {
/*  63:108 */       if (!setOption((ChannelOption)e.getKey(), e.getValue())) {
/*  64:109 */         setAllOptions = false;
/*  65:    */       }
/*  66:    */     }
/*  67:113 */     return setAllOptions;
/*  68:    */   }
/*  69:    */   
/*  70:    */   public <T> T getOption(ChannelOption<T> option)
/*  71:    */   {
/*  72:119 */     if (option == null) {
/*  73:120 */       throw new NullPointerException("option");
/*  74:    */     }
/*  75:123 */     if (option == ChannelOption.CONNECT_TIMEOUT_MILLIS) {
/*  76:124 */       return Integer.valueOf(getConnectTimeoutMillis());
/*  77:    */     }
/*  78:126 */     if (option == ChannelOption.MAX_MESSAGES_PER_READ) {
/*  79:127 */       return Integer.valueOf(getMaxMessagesPerRead());
/*  80:    */     }
/*  81:129 */     if (option == ChannelOption.WRITE_SPIN_COUNT) {
/*  82:130 */       return Integer.valueOf(getWriteSpinCount());
/*  83:    */     }
/*  84:132 */     if (option == ChannelOption.ALLOCATOR) {
/*  85:133 */       return getAllocator();
/*  86:    */     }
/*  87:135 */     if (option == ChannelOption.RCVBUF_ALLOCATOR) {
/*  88:136 */       return getRecvByteBufAllocator();
/*  89:    */     }
/*  90:138 */     if (option == ChannelOption.AUTO_READ) {
/*  91:139 */       return Boolean.valueOf(isAutoRead());
/*  92:    */     }
/*  93:141 */     if (option == ChannelOption.AUTO_CLOSE) {
/*  94:142 */       return Boolean.valueOf(isAutoClose());
/*  95:    */     }
/*  96:144 */     if (option == ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK) {
/*  97:145 */       return Integer.valueOf(getWriteBufferHighWaterMark());
/*  98:    */     }
/*  99:147 */     if (option == ChannelOption.WRITE_BUFFER_LOW_WATER_MARK) {
/* 100:148 */       return Integer.valueOf(getWriteBufferLowWaterMark());
/* 101:    */     }
/* 102:150 */     if (option == ChannelOption.WRITE_BUFFER_WATER_MARK) {
/* 103:151 */       return getWriteBufferWaterMark();
/* 104:    */     }
/* 105:153 */     if (option == ChannelOption.MESSAGE_SIZE_ESTIMATOR) {
/* 106:154 */       return getMessageSizeEstimator();
/* 107:    */     }
/* 108:156 */     if (option == ChannelOption.SINGLE_EVENTEXECUTOR_PER_GROUP) {
/* 109:157 */       return Boolean.valueOf(getPinEventExecutorPerGroup());
/* 110:    */     }
/* 111:159 */     return null;
/* 112:    */   }
/* 113:    */   
/* 114:    */   public <T> boolean setOption(ChannelOption<T> option, T value)
/* 115:    */   {
/* 116:165 */     validate(option, value);
/* 117:167 */     if (option == ChannelOption.CONNECT_TIMEOUT_MILLIS) {
/* 118:168 */       setConnectTimeoutMillis(((Integer)value).intValue());
/* 119:169 */     } else if (option == ChannelOption.MAX_MESSAGES_PER_READ) {
/* 120:170 */       setMaxMessagesPerRead(((Integer)value).intValue());
/* 121:171 */     } else if (option == ChannelOption.WRITE_SPIN_COUNT) {
/* 122:172 */       setWriteSpinCount(((Integer)value).intValue());
/* 123:173 */     } else if (option == ChannelOption.ALLOCATOR) {
/* 124:174 */       setAllocator((ByteBufAllocator)value);
/* 125:175 */     } else if (option == ChannelOption.RCVBUF_ALLOCATOR) {
/* 126:176 */       setRecvByteBufAllocator((RecvByteBufAllocator)value);
/* 127:177 */     } else if (option == ChannelOption.AUTO_READ) {
/* 128:178 */       setAutoRead(((Boolean)value).booleanValue());
/* 129:179 */     } else if (option == ChannelOption.AUTO_CLOSE) {
/* 130:180 */       setAutoClose(((Boolean)value).booleanValue());
/* 131:181 */     } else if (option == ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK) {
/* 132:182 */       setWriteBufferHighWaterMark(((Integer)value).intValue());
/* 133:183 */     } else if (option == ChannelOption.WRITE_BUFFER_LOW_WATER_MARK) {
/* 134:184 */       setWriteBufferLowWaterMark(((Integer)value).intValue());
/* 135:185 */     } else if (option == ChannelOption.WRITE_BUFFER_WATER_MARK) {
/* 136:186 */       setWriteBufferWaterMark((WriteBufferWaterMark)value);
/* 137:187 */     } else if (option == ChannelOption.MESSAGE_SIZE_ESTIMATOR) {
/* 138:188 */       setMessageSizeEstimator((MessageSizeEstimator)value);
/* 139:189 */     } else if (option == ChannelOption.SINGLE_EVENTEXECUTOR_PER_GROUP) {
/* 140:190 */       setPinEventExecutorPerGroup(((Boolean)value).booleanValue());
/* 141:    */     } else {
/* 142:192 */       return false;
/* 143:    */     }
/* 144:195 */     return true;
/* 145:    */   }
/* 146:    */   
/* 147:    */   protected <T> void validate(ChannelOption<T> option, T value)
/* 148:    */   {
/* 149:199 */     if (option == null) {
/* 150:200 */       throw new NullPointerException("option");
/* 151:    */     }
/* 152:202 */     option.validate(value);
/* 153:    */   }
/* 154:    */   
/* 155:    */   public int getConnectTimeoutMillis()
/* 156:    */   {
/* 157:207 */     return this.connectTimeoutMillis;
/* 158:    */   }
/* 159:    */   
/* 160:    */   public ChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis)
/* 161:    */   {
/* 162:212 */     if (connectTimeoutMillis < 0) {
/* 163:213 */       throw new IllegalArgumentException(String.format("connectTimeoutMillis: %d (expected: >= 0)", new Object[] {
/* 164:214 */         Integer.valueOf(connectTimeoutMillis) }));
/* 165:    */     }
/* 166:216 */     this.connectTimeoutMillis = connectTimeoutMillis;
/* 167:217 */     return this;
/* 168:    */   }
/* 169:    */   
/* 170:    */   @Deprecated
/* 171:    */   public int getMaxMessagesPerRead()
/* 172:    */   {
/* 173:    */     try
/* 174:    */     {
/* 175:230 */       MaxMessagesRecvByteBufAllocator allocator = (MaxMessagesRecvByteBufAllocator)getRecvByteBufAllocator();
/* 176:231 */       return allocator.maxMessagesPerRead();
/* 177:    */     }
/* 178:    */     catch (ClassCastException e)
/* 179:    */     {
/* 180:233 */       throw new IllegalStateException("getRecvByteBufAllocator() must return an object of type MaxMessagesRecvByteBufAllocator", e);
/* 181:    */     }
/* 182:    */   }
/* 183:    */   
/* 184:    */   @Deprecated
/* 185:    */   public ChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead)
/* 186:    */   {
/* 187:    */     try
/* 188:    */     {
/* 189:248 */       MaxMessagesRecvByteBufAllocator allocator = (MaxMessagesRecvByteBufAllocator)getRecvByteBufAllocator();
/* 190:249 */       allocator.maxMessagesPerRead(maxMessagesPerRead);
/* 191:250 */       return this;
/* 192:    */     }
/* 193:    */     catch (ClassCastException e)
/* 194:    */     {
/* 195:252 */       throw new IllegalStateException("getRecvByteBufAllocator() must return an object of type MaxMessagesRecvByteBufAllocator", e);
/* 196:    */     }
/* 197:    */   }
/* 198:    */   
/* 199:    */   public int getWriteSpinCount()
/* 200:    */   {
/* 201:259 */     return this.writeSpinCount;
/* 202:    */   }
/* 203:    */   
/* 204:    */   public ChannelConfig setWriteSpinCount(int writeSpinCount)
/* 205:    */   {
/* 206:264 */     if (writeSpinCount <= 0) {
/* 207:265 */       throw new IllegalArgumentException("writeSpinCount must be a positive integer.");
/* 208:    */     }
/* 209:272 */     if (writeSpinCount == 2147483647) {
/* 210:273 */       writeSpinCount--;
/* 211:    */     }
/* 212:275 */     this.writeSpinCount = writeSpinCount;
/* 213:276 */     return this;
/* 214:    */   }
/* 215:    */   
/* 216:    */   public ByteBufAllocator getAllocator()
/* 217:    */   {
/* 218:281 */     return this.allocator;
/* 219:    */   }
/* 220:    */   
/* 221:    */   public ChannelConfig setAllocator(ByteBufAllocator allocator)
/* 222:    */   {
/* 223:286 */     if (allocator == null) {
/* 224:287 */       throw new NullPointerException("allocator");
/* 225:    */     }
/* 226:289 */     this.allocator = allocator;
/* 227:290 */     return this;
/* 228:    */   }
/* 229:    */   
/* 230:    */   public <T extends RecvByteBufAllocator> T getRecvByteBufAllocator()
/* 231:    */   {
/* 232:296 */     return this.rcvBufAllocator;
/* 233:    */   }
/* 234:    */   
/* 235:    */   public ChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator)
/* 236:    */   {
/* 237:301 */     this.rcvBufAllocator = ((RecvByteBufAllocator)ObjectUtil.checkNotNull(allocator, "allocator"));
/* 238:302 */     return this;
/* 239:    */   }
/* 240:    */   
/* 241:    */   private void setRecvByteBufAllocator(RecvByteBufAllocator allocator, ChannelMetadata metadata)
/* 242:    */   {
/* 243:312 */     if ((allocator instanceof MaxMessagesRecvByteBufAllocator)) {
/* 244:313 */       ((MaxMessagesRecvByteBufAllocator)allocator).maxMessagesPerRead(metadata.defaultMaxMessagesPerRead());
/* 245:314 */     } else if (allocator == null) {
/* 246:315 */       throw new NullPointerException("allocator");
/* 247:    */     }
/* 248:317 */     setRecvByteBufAllocator(allocator);
/* 249:    */   }
/* 250:    */   
/* 251:    */   public boolean isAutoRead()
/* 252:    */   {
/* 253:322 */     return this.autoRead == 1;
/* 254:    */   }
/* 255:    */   
/* 256:    */   public ChannelConfig setAutoRead(boolean autoRead)
/* 257:    */   {
/* 258:327 */     boolean oldAutoRead = AUTOREAD_UPDATER.getAndSet(this, autoRead ? 1 : 0) == 1;
/* 259:328 */     if ((autoRead) && (!oldAutoRead)) {
/* 260:329 */       this.channel.read();
/* 261:330 */     } else if ((!autoRead) && (oldAutoRead)) {
/* 262:331 */       autoReadCleared();
/* 263:    */     }
/* 264:333 */     return this;
/* 265:    */   }
/* 266:    */   
/* 267:    */   protected void autoReadCleared() {}
/* 268:    */   
/* 269:    */   public boolean isAutoClose()
/* 270:    */   {
/* 271:344 */     return this.autoClose;
/* 272:    */   }
/* 273:    */   
/* 274:    */   public ChannelConfig setAutoClose(boolean autoClose)
/* 275:    */   {
/* 276:349 */     this.autoClose = autoClose;
/* 277:350 */     return this;
/* 278:    */   }
/* 279:    */   
/* 280:    */   public int getWriteBufferHighWaterMark()
/* 281:    */   {
/* 282:355 */     return this.writeBufferWaterMark.high();
/* 283:    */   }
/* 284:    */   
/* 285:    */   public ChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark)
/* 286:    */   {
/* 287:360 */     if (writeBufferHighWaterMark < 0) {
/* 288:361 */       throw new IllegalArgumentException("writeBufferHighWaterMark must be >= 0");
/* 289:    */     }
/* 290:    */     for (;;)
/* 291:    */     {
/* 292:365 */       WriteBufferWaterMark waterMark = this.writeBufferWaterMark;
/* 293:366 */       if (writeBufferHighWaterMark < waterMark.low()) {
/* 294:369 */         throw new IllegalArgumentException("writeBufferHighWaterMark cannot be less than writeBufferLowWaterMark (" + waterMark.low() + "): " + writeBufferHighWaterMark);
/* 295:    */       }
/* 296:372 */       if (WATERMARK_UPDATER.compareAndSet(this, waterMark, new WriteBufferWaterMark(waterMark
/* 297:373 */         .low(), writeBufferHighWaterMark, false))) {
/* 298:374 */         return this;
/* 299:    */       }
/* 300:    */     }
/* 301:    */   }
/* 302:    */   
/* 303:    */   public int getWriteBufferLowWaterMark()
/* 304:    */   {
/* 305:381 */     return this.writeBufferWaterMark.low();
/* 306:    */   }
/* 307:    */   
/* 308:    */   public ChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark)
/* 309:    */   {
/* 310:386 */     if (writeBufferLowWaterMark < 0) {
/* 311:387 */       throw new IllegalArgumentException("writeBufferLowWaterMark must be >= 0");
/* 312:    */     }
/* 313:    */     for (;;)
/* 314:    */     {
/* 315:391 */       WriteBufferWaterMark waterMark = this.writeBufferWaterMark;
/* 316:392 */       if (writeBufferLowWaterMark > waterMark.high()) {
/* 317:395 */         throw new IllegalArgumentException("writeBufferLowWaterMark cannot be greater than writeBufferHighWaterMark (" + waterMark.high() + "): " + writeBufferLowWaterMark);
/* 318:    */       }
/* 319:398 */       if (WATERMARK_UPDATER.compareAndSet(this, waterMark, new WriteBufferWaterMark(writeBufferLowWaterMark, waterMark
/* 320:399 */         .high(), false))) {
/* 321:400 */         return this;
/* 322:    */       }
/* 323:    */     }
/* 324:    */   }
/* 325:    */   
/* 326:    */   public ChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark)
/* 327:    */   {
/* 328:407 */     this.writeBufferWaterMark = ((WriteBufferWaterMark)ObjectUtil.checkNotNull(writeBufferWaterMark, "writeBufferWaterMark"));
/* 329:408 */     return this;
/* 330:    */   }
/* 331:    */   
/* 332:    */   public WriteBufferWaterMark getWriteBufferWaterMark()
/* 333:    */   {
/* 334:413 */     return this.writeBufferWaterMark;
/* 335:    */   }
/* 336:    */   
/* 337:    */   public MessageSizeEstimator getMessageSizeEstimator()
/* 338:    */   {
/* 339:418 */     return this.msgSizeEstimator;
/* 340:    */   }
/* 341:    */   
/* 342:    */   public ChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator)
/* 343:    */   {
/* 344:423 */     if (estimator == null) {
/* 345:424 */       throw new NullPointerException("estimator");
/* 346:    */     }
/* 347:426 */     this.msgSizeEstimator = estimator;
/* 348:427 */     return this;
/* 349:    */   }
/* 350:    */   
/* 351:    */   private ChannelConfig setPinEventExecutorPerGroup(boolean pinEventExecutor)
/* 352:    */   {
/* 353:431 */     this.pinEventExecutor = pinEventExecutor;
/* 354:432 */     return this;
/* 355:    */   }
/* 356:    */   
/* 357:    */   private boolean getPinEventExecutorPerGroup()
/* 358:    */   {
/* 359:436 */     return this.pinEventExecutor;
/* 360:    */   }
/* 361:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.DefaultChannelConfig
 * JD-Core Version:    0.7.0.1
 */