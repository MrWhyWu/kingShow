/*   1:    */ package io.netty.channel.oio;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.channel.Channel;
/*   6:    */ import io.netty.channel.Channel.Unsafe;
/*   7:    */ import io.netty.channel.ChannelConfig;
/*   8:    */ import io.netty.channel.ChannelFuture;
/*   9:    */ import io.netty.channel.ChannelMetadata;
/*  10:    */ import io.netty.channel.ChannelOption;
/*  11:    */ import io.netty.channel.ChannelOutboundBuffer;
/*  12:    */ import io.netty.channel.ChannelPipeline;
/*  13:    */ import io.netty.channel.FileRegion;
/*  14:    */ import io.netty.channel.RecvByteBufAllocator.Handle;
/*  15:    */ import io.netty.channel.socket.ChannelInputShutdownEvent;
/*  16:    */ import io.netty.util.internal.StringUtil;
/*  17:    */ import java.io.IOException;
/*  18:    */ 
/*  19:    */ public abstract class AbstractOioByteChannel
/*  20:    */   extends AbstractOioChannel
/*  21:    */ {
/*  22: 39 */   private static final ChannelMetadata METADATA = new ChannelMetadata(false);
/*  23: 40 */   private static final String EXPECTED_TYPES = " (expected: " + 
/*  24: 41 */     StringUtil.simpleClassName(ByteBuf.class) + ", " + 
/*  25: 42 */     StringUtil.simpleClassName(FileRegion.class) + ')';
/*  26:    */   
/*  27:    */   protected AbstractOioByteChannel(Channel parent)
/*  28:    */   {
/*  29: 48 */     super(parent);
/*  30:    */   }
/*  31:    */   
/*  32:    */   public ChannelMetadata metadata()
/*  33:    */   {
/*  34: 53 */     return METADATA;
/*  35:    */   }
/*  36:    */   
/*  37:    */   protected abstract boolean isInputShutdown();
/*  38:    */   
/*  39:    */   protected abstract ChannelFuture shutdownInput();
/*  40:    */   
/*  41:    */   private void closeOnRead(ChannelPipeline pipeline)
/*  42:    */   {
/*  43: 69 */     if (isOpen()) {
/*  44: 70 */       if (Boolean.TRUE.equals(config().getOption(ChannelOption.ALLOW_HALF_CLOSURE)))
/*  45:    */       {
/*  46: 71 */         shutdownInput();
/*  47: 72 */         pipeline.fireUserEventTriggered(ChannelInputShutdownEvent.INSTANCE);
/*  48:    */       }
/*  49:    */       else
/*  50:    */       {
/*  51: 74 */         unsafe().close(unsafe().voidPromise());
/*  52:    */       }
/*  53:    */     }
/*  54:    */   }
/*  55:    */   
/*  56:    */   private void handleReadException(ChannelPipeline pipeline, ByteBuf byteBuf, Throwable cause, boolean close, RecvByteBufAllocator.Handle allocHandle)
/*  57:    */   {
/*  58: 81 */     if (byteBuf != null) {
/*  59: 82 */       if (byteBuf.isReadable())
/*  60:    */       {
/*  61: 83 */         this.readPending = false;
/*  62: 84 */         pipeline.fireChannelRead(byteBuf);
/*  63:    */       }
/*  64:    */       else
/*  65:    */       {
/*  66: 86 */         byteBuf.release();
/*  67:    */       }
/*  68:    */     }
/*  69: 89 */     allocHandle.readComplete();
/*  70: 90 */     pipeline.fireChannelReadComplete();
/*  71: 91 */     pipeline.fireExceptionCaught(cause);
/*  72: 92 */     if ((close) || ((cause instanceof IOException))) {
/*  73: 93 */       closeOnRead(pipeline);
/*  74:    */     }
/*  75:    */   }
/*  76:    */   
/*  77:    */   protected void doRead()
/*  78:    */   {
/*  79: 99 */     ChannelConfig config = config();
/*  80:100 */     if ((isInputShutdown()) || (!this.readPending)) {
/*  81:103 */       return;
/*  82:    */     }
/*  83:107 */     this.readPending = false;
/*  84:    */     
/*  85:109 */     ChannelPipeline pipeline = pipeline();
/*  86:110 */     ByteBufAllocator allocator = config.getAllocator();
/*  87:111 */     RecvByteBufAllocator.Handle allocHandle = unsafe().recvBufAllocHandle();
/*  88:112 */     allocHandle.reset(config);
/*  89:    */     
/*  90:114 */     ByteBuf byteBuf = null;
/*  91:115 */     boolean close = false;
/*  92:116 */     boolean readData = false;
/*  93:    */     try
/*  94:    */     {
/*  95:118 */       byteBuf = allocHandle.allocate(allocator);
/*  96:    */       do
/*  97:    */       {
/*  98:120 */         allocHandle.lastBytesRead(doReadBytes(byteBuf));
/*  99:121 */         if (allocHandle.lastBytesRead() <= 0)
/* 100:    */         {
/* 101:122 */           if (byteBuf.isReadable()) {
/* 102:    */             break;
/* 103:    */           }
/* 104:123 */           byteBuf.release();
/* 105:124 */           byteBuf = null;
/* 106:125 */           close = allocHandle.lastBytesRead() < 0;
/* 107:126 */           if (!close) {
/* 108:    */             break;
/* 109:    */           }
/* 110:128 */           this.readPending = false; break;
/* 111:    */         }
/* 112:133 */         readData = true;
/* 113:    */         
/* 114:    */ 
/* 115:136 */         int available = available();
/* 116:137 */         if (available <= 0) {
/* 117:    */           break;
/* 118:    */         }
/* 119:142 */         if (!byteBuf.isWritable())
/* 120:    */         {
/* 121:143 */           int capacity = byteBuf.capacity();
/* 122:144 */           int maxCapacity = byteBuf.maxCapacity();
/* 123:145 */           if (capacity == maxCapacity)
/* 124:    */           {
/* 125:146 */             allocHandle.incMessagesRead(1);
/* 126:147 */             this.readPending = false;
/* 127:148 */             pipeline.fireChannelRead(byteBuf);
/* 128:149 */             byteBuf = allocHandle.allocate(allocator);
/* 129:    */           }
/* 130:    */           else
/* 131:    */           {
/* 132:151 */             int writerIndex = byteBuf.writerIndex();
/* 133:152 */             if (writerIndex + available > maxCapacity) {
/* 134:153 */               byteBuf.capacity(maxCapacity);
/* 135:    */             } else {
/* 136:155 */               byteBuf.ensureWritable(available);
/* 137:    */             }
/* 138:    */           }
/* 139:    */         }
/* 140:159 */       } while (allocHandle.continueReading());
/* 141:161 */       if (byteBuf != null)
/* 142:    */       {
/* 143:164 */         if (byteBuf.isReadable())
/* 144:    */         {
/* 145:165 */           this.readPending = false;
/* 146:166 */           pipeline.fireChannelRead(byteBuf);
/* 147:    */         }
/* 148:    */         else
/* 149:    */         {
/* 150:168 */           byteBuf.release();
/* 151:    */         }
/* 152:170 */         byteBuf = null;
/* 153:    */       }
/* 154:173 */       if (readData)
/* 155:    */       {
/* 156:174 */         allocHandle.readComplete();
/* 157:175 */         pipeline.fireChannelReadComplete();
/* 158:    */       }
/* 159:178 */       if (close) {
/* 160:179 */         closeOnRead(pipeline);
/* 161:    */       }
/* 162:    */     }
/* 163:    */     catch (Throwable t)
/* 164:    */     {
/* 165:182 */       handleReadException(pipeline, byteBuf, t, close, allocHandle);
/* 166:    */     }
/* 167:    */     finally
/* 168:    */     {
/* 169:184 */       if ((this.readPending) || (config.isAutoRead()) || ((!readData) && (isActive()))) {
/* 170:187 */         read();
/* 171:    */       }
/* 172:    */     }
/* 173:    */   }
/* 174:    */   
/* 175:    */   protected void doWrite(ChannelOutboundBuffer in)
/* 176:    */     throws Exception
/* 177:    */   {
/* 178:    */     for (;;)
/* 179:    */     {
/* 180:195 */       Object msg = in.current();
/* 181:196 */       if (msg == null) {
/* 182:    */         break;
/* 183:    */       }
/* 184:200 */       if ((msg instanceof ByteBuf))
/* 185:    */       {
/* 186:201 */         ByteBuf buf = (ByteBuf)msg;
/* 187:202 */         int readableBytes = buf.readableBytes();
/* 188:203 */         while (readableBytes > 0)
/* 189:    */         {
/* 190:204 */           doWriteBytes(buf);
/* 191:205 */           int newReadableBytes = buf.readableBytes();
/* 192:206 */           in.progress(readableBytes - newReadableBytes);
/* 193:207 */           readableBytes = newReadableBytes;
/* 194:    */         }
/* 195:209 */         in.remove();
/* 196:    */       }
/* 197:210 */       else if ((msg instanceof FileRegion))
/* 198:    */       {
/* 199:211 */         FileRegion region = (FileRegion)msg;
/* 200:212 */         long transferred = region.transferred();
/* 201:213 */         doWriteFileRegion(region);
/* 202:214 */         in.progress(region.transferred() - transferred);
/* 203:215 */         in.remove();
/* 204:    */       }
/* 205:    */       else
/* 206:    */       {
/* 207:217 */         in.remove(new UnsupportedOperationException("unsupported message type: " + 
/* 208:218 */           StringUtil.simpleClassName(msg)));
/* 209:    */       }
/* 210:    */     }
/* 211:    */   }
/* 212:    */   
/* 213:    */   protected final Object filterOutboundMessage(Object msg)
/* 214:    */     throws Exception
/* 215:    */   {
/* 216:225 */     if (((msg instanceof ByteBuf)) || ((msg instanceof FileRegion))) {
/* 217:226 */       return msg;
/* 218:    */     }
/* 219:230 */     throw new UnsupportedOperationException("unsupported message type: " + StringUtil.simpleClassName(msg) + EXPECTED_TYPES);
/* 220:    */   }
/* 221:    */   
/* 222:    */   protected abstract int available();
/* 223:    */   
/* 224:    */   protected abstract int doReadBytes(ByteBuf paramByteBuf)
/* 225:    */     throws Exception;
/* 226:    */   
/* 227:    */   protected abstract void doWriteBytes(ByteBuf paramByteBuf)
/* 228:    */     throws Exception;
/* 229:    */   
/* 230:    */   protected abstract void doWriteFileRegion(FileRegion paramFileRegion)
/* 231:    */     throws Exception;
/* 232:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.oio.AbstractOioByteChannel
 * JD-Core Version:    0.7.0.1
 */