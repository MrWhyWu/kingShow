/*   1:    */ package io.netty.channel.nio;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.channel.Channel;
/*   6:    */ import io.netty.channel.ChannelConfig;
/*   7:    */ import io.netty.channel.ChannelFuture;
/*   8:    */ import io.netty.channel.ChannelMetadata;
/*   9:    */ import io.netty.channel.ChannelOption;
/*  10:    */ import io.netty.channel.ChannelOutboundBuffer;
/*  11:    */ import io.netty.channel.ChannelPipeline;
/*  12:    */ import io.netty.channel.FileRegion;
/*  13:    */ import io.netty.channel.RecvByteBufAllocator.Handle;
/*  14:    */ import io.netty.channel.socket.ChannelInputShutdownEvent;
/*  15:    */ import io.netty.channel.socket.ChannelInputShutdownReadComplete;
/*  16:    */ import io.netty.util.internal.StringUtil;
/*  17:    */ import java.io.IOException;
/*  18:    */ import java.nio.channels.SelectableChannel;
/*  19:    */ import java.nio.channels.SelectionKey;
/*  20:    */ 
/*  21:    */ public abstract class AbstractNioByteChannel
/*  22:    */   extends AbstractNioChannel
/*  23:    */ {
/*  24: 44 */   private static final ChannelMetadata METADATA = new ChannelMetadata(false, 16);
/*  25: 45 */   private static final String EXPECTED_TYPES = " (expected: " + 
/*  26: 46 */     StringUtil.simpleClassName(ByteBuf.class) + ", " + 
/*  27: 47 */     StringUtil.simpleClassName(FileRegion.class) + ')';
/*  28:    */   private Runnable flushTask;
/*  29:    */   
/*  30:    */   protected AbstractNioByteChannel(Channel parent, SelectableChannel ch)
/*  31:    */   {
/*  32: 58 */     super(parent, ch, 1);
/*  33:    */   }
/*  34:    */   
/*  35:    */   protected abstract ChannelFuture shutdownInput();
/*  36:    */   
/*  37:    */   protected boolean isInputShutdown0()
/*  38:    */   {
/*  39: 67 */     return false;
/*  40:    */   }
/*  41:    */   
/*  42:    */   protected AbstractNioChannel.AbstractNioUnsafe newUnsafe()
/*  43:    */   {
/*  44: 72 */     return new NioByteUnsafe();
/*  45:    */   }
/*  46:    */   
/*  47:    */   public ChannelMetadata metadata()
/*  48:    */   {
/*  49: 77 */     return METADATA;
/*  50:    */   }
/*  51:    */   
/*  52:    */   protected class NioByteUnsafe
/*  53:    */     extends AbstractNioChannel.AbstractNioUnsafe
/*  54:    */   {
/*  55:    */     protected NioByteUnsafe()
/*  56:    */     {
/*  57: 80 */       super();
/*  58:    */     }
/*  59:    */     
/*  60:    */     private void closeOnRead(ChannelPipeline pipeline)
/*  61:    */     {
/*  62: 83 */       if (!AbstractNioByteChannel.this.isInputShutdown0())
/*  63:    */       {
/*  64: 84 */         if (Boolean.TRUE.equals(AbstractNioByteChannel.this.config().getOption(ChannelOption.ALLOW_HALF_CLOSURE)))
/*  65:    */         {
/*  66: 85 */           AbstractNioByteChannel.this.shutdownInput();
/*  67: 86 */           pipeline.fireUserEventTriggered(ChannelInputShutdownEvent.INSTANCE);
/*  68:    */         }
/*  69:    */         else
/*  70:    */         {
/*  71: 88 */           close(voidPromise());
/*  72:    */         }
/*  73:    */       }
/*  74:    */       else {
/*  75: 91 */         pipeline.fireUserEventTriggered(ChannelInputShutdownReadComplete.INSTANCE);
/*  76:    */       }
/*  77:    */     }
/*  78:    */     
/*  79:    */     private void handleReadException(ChannelPipeline pipeline, ByteBuf byteBuf, Throwable cause, boolean close, RecvByteBufAllocator.Handle allocHandle)
/*  80:    */     {
/*  81: 97 */       if (byteBuf != null) {
/*  82: 98 */         if (byteBuf.isReadable())
/*  83:    */         {
/*  84: 99 */           AbstractNioByteChannel.this.readPending = false;
/*  85:100 */           pipeline.fireChannelRead(byteBuf);
/*  86:    */         }
/*  87:    */         else
/*  88:    */         {
/*  89:102 */           byteBuf.release();
/*  90:    */         }
/*  91:    */       }
/*  92:105 */       allocHandle.readComplete();
/*  93:106 */       pipeline.fireChannelReadComplete();
/*  94:107 */       pipeline.fireExceptionCaught(cause);
/*  95:108 */       if ((close) || ((cause instanceof IOException))) {
/*  96:109 */         closeOnRead(pipeline);
/*  97:    */       }
/*  98:    */     }
/*  99:    */     
/* 100:    */     public final void read()
/* 101:    */     {
/* 102:115 */       ChannelConfig config = AbstractNioByteChannel.this.config();
/* 103:116 */       ChannelPipeline pipeline = AbstractNioByteChannel.this.pipeline();
/* 104:117 */       ByteBufAllocator allocator = config.getAllocator();
/* 105:118 */       RecvByteBufAllocator.Handle allocHandle = recvBufAllocHandle();
/* 106:119 */       allocHandle.reset(config);
/* 107:    */       
/* 108:121 */       ByteBuf byteBuf = null;
/* 109:122 */       boolean close = false;
/* 110:    */       try
/* 111:    */       {
/* 112:    */         do
/* 113:    */         {
/* 114:125 */           byteBuf = allocHandle.allocate(allocator);
/* 115:126 */           allocHandle.lastBytesRead(AbstractNioByteChannel.this.doReadBytes(byteBuf));
/* 116:127 */           if (allocHandle.lastBytesRead() <= 0)
/* 117:    */           {
/* 118:129 */             byteBuf.release();
/* 119:130 */             byteBuf = null;
/* 120:131 */             close = allocHandle.lastBytesRead() < 0;
/* 121:132 */             if (!close) {
/* 122:    */               break;
/* 123:    */             }
/* 124:134 */             AbstractNioByteChannel.this.readPending = false; break;
/* 125:    */           }
/* 126:139 */           allocHandle.incMessagesRead(1);
/* 127:140 */           AbstractNioByteChannel.this.readPending = false;
/* 128:141 */           pipeline.fireChannelRead(byteBuf);
/* 129:142 */           byteBuf = null;
/* 130:143 */         } while (allocHandle.continueReading());
/* 131:145 */         allocHandle.readComplete();
/* 132:146 */         pipeline.fireChannelReadComplete();
/* 133:148 */         if (close) {
/* 134:149 */           closeOnRead(pipeline);
/* 135:    */         }
/* 136:    */       }
/* 137:    */       catch (Throwable t)
/* 138:    */       {
/* 139:152 */         handleReadException(pipeline, byteBuf, t, close, allocHandle);
/* 140:    */       }
/* 141:    */       finally
/* 142:    */       {
/* 143:160 */         if ((!AbstractNioByteChannel.this.readPending) && (!config.isAutoRead())) {
/* 144:161 */           removeReadOp();
/* 145:    */         }
/* 146:    */       }
/* 147:    */     }
/* 148:    */   }
/* 149:    */   
/* 150:    */   protected final int doWrite0(ChannelOutboundBuffer in)
/* 151:    */     throws Exception
/* 152:    */   {
/* 153:182 */     Object msg = in.current();
/* 154:183 */     if (msg == null) {
/* 155:185 */       return 0;
/* 156:    */     }
/* 157:187 */     return doWriteInternal(in, in.current());
/* 158:    */   }
/* 159:    */   
/* 160:    */   private int doWriteInternal(ChannelOutboundBuffer in, Object msg)
/* 161:    */     throws Exception
/* 162:    */   {
/* 163:191 */     if ((msg instanceof ByteBuf))
/* 164:    */     {
/* 165:192 */       ByteBuf buf = (ByteBuf)msg;
/* 166:193 */       if (!buf.isReadable())
/* 167:    */       {
/* 168:194 */         in.remove();
/* 169:195 */         return 0;
/* 170:    */       }
/* 171:198 */       int localFlushedAmount = doWriteBytes(buf);
/* 172:199 */       if (localFlushedAmount > 0)
/* 173:    */       {
/* 174:200 */         in.progress(localFlushedAmount);
/* 175:201 */         if (!buf.isReadable()) {
/* 176:202 */           in.remove();
/* 177:    */         }
/* 178:204 */         return 1;
/* 179:    */       }
/* 180:    */     }
/* 181:206 */     else if ((msg instanceof FileRegion))
/* 182:    */     {
/* 183:207 */       FileRegion region = (FileRegion)msg;
/* 184:208 */       if (region.transferred() >= region.count())
/* 185:    */       {
/* 186:209 */         in.remove();
/* 187:210 */         return 0;
/* 188:    */       }
/* 189:213 */       long localFlushedAmount = doWriteFileRegion(region);
/* 190:214 */       if (localFlushedAmount > 0L)
/* 191:    */       {
/* 192:215 */         in.progress(localFlushedAmount);
/* 193:216 */         if (region.transferred() >= region.count()) {
/* 194:217 */           in.remove();
/* 195:    */         }
/* 196:219 */         return 1;
/* 197:    */       }
/* 198:    */     }
/* 199:    */     else
/* 200:    */     {
/* 201:223 */       throw new Error();
/* 202:    */     }
/* 203:225 */     return 2147483647;
/* 204:    */   }
/* 205:    */   
/* 206:    */   protected void doWrite(ChannelOutboundBuffer in)
/* 207:    */     throws Exception
/* 208:    */   {
/* 209:230 */     int writeSpinCount = config().getWriteSpinCount();
/* 210:    */     do
/* 211:    */     {
/* 212:232 */       Object msg = in.current();
/* 213:233 */       if (msg == null)
/* 214:    */       {
/* 215:235 */         clearOpWrite();
/* 216:    */         
/* 217:237 */         return;
/* 218:    */       }
/* 219:239 */       writeSpinCount -= doWriteInternal(in, msg);
/* 220:240 */     } while (writeSpinCount > 0);
/* 221:242 */     incompleteWrite(writeSpinCount < 0);
/* 222:    */   }
/* 223:    */   
/* 224:    */   protected final Object filterOutboundMessage(Object msg)
/* 225:    */   {
/* 226:247 */     if ((msg instanceof ByteBuf))
/* 227:    */     {
/* 228:248 */       ByteBuf buf = (ByteBuf)msg;
/* 229:249 */       if (buf.isDirect()) {
/* 230:250 */         return msg;
/* 231:    */       }
/* 232:253 */       return newDirectBuffer(buf);
/* 233:    */     }
/* 234:256 */     if ((msg instanceof FileRegion)) {
/* 235:257 */       return msg;
/* 236:    */     }
/* 237:261 */     throw new UnsupportedOperationException("unsupported message type: " + StringUtil.simpleClassName(msg) + EXPECTED_TYPES);
/* 238:    */   }
/* 239:    */   
/* 240:    */   protected final void incompleteWrite(boolean setOpWrite)
/* 241:    */   {
/* 242:266 */     if (setOpWrite)
/* 243:    */     {
/* 244:267 */       setOpWrite();
/* 245:    */     }
/* 246:    */     else
/* 247:    */     {
/* 248:270 */       Runnable flushTask = this.flushTask;
/* 249:271 */       if (flushTask == null) {
/* 250:272 */         flushTask = this.flushTask = new Runnable()
/* 251:    */         {
/* 252:    */           public void run()
/* 253:    */           {
/* 254:275 */             AbstractNioByteChannel.this.flush();
/* 255:    */           }
/* 256:    */         };
/* 257:    */       }
/* 258:279 */       eventLoop().execute(flushTask);
/* 259:    */     }
/* 260:    */   }
/* 261:    */   
/* 262:    */   protected abstract long doWriteFileRegion(FileRegion paramFileRegion)
/* 263:    */     throws Exception;
/* 264:    */   
/* 265:    */   protected abstract int doReadBytes(ByteBuf paramByteBuf)
/* 266:    */     throws Exception;
/* 267:    */   
/* 268:    */   protected abstract int doWriteBytes(ByteBuf paramByteBuf)
/* 269:    */     throws Exception;
/* 270:    */   
/* 271:    */   protected final void setOpWrite()
/* 272:    */   {
/* 273:304 */     SelectionKey key = selectionKey();
/* 274:308 */     if (!key.isValid()) {
/* 275:309 */       return;
/* 276:    */     }
/* 277:311 */     int interestOps = key.interestOps();
/* 278:312 */     if ((interestOps & 0x4) == 0) {
/* 279:313 */       key.interestOps(interestOps | 0x4);
/* 280:    */     }
/* 281:    */   }
/* 282:    */   
/* 283:    */   protected final void clearOpWrite()
/* 284:    */   {
/* 285:318 */     SelectionKey key = selectionKey();
/* 286:322 */     if (!key.isValid()) {
/* 287:323 */       return;
/* 288:    */     }
/* 289:325 */     int interestOps = key.interestOps();
/* 290:326 */     if ((interestOps & 0x4) != 0) {
/* 291:327 */       key.interestOps(interestOps & 0xFFFFFFFB);
/* 292:    */     }
/* 293:    */   }
/* 294:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.nio.AbstractNioByteChannel
 * JD-Core Version:    0.7.0.1
 */