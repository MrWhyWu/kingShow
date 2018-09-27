/*   1:    */ package io.netty.bootstrap;
/*   2:    */ 
/*   3:    */ import io.netty.channel.Channel;
/*   4:    */ import io.netty.channel.Channel.Unsafe;
/*   5:    */ import io.netty.channel.ChannelConfig;
/*   6:    */ import io.netty.channel.ChannelFuture;
/*   7:    */ import io.netty.channel.ChannelFutureListener;
/*   8:    */ import io.netty.channel.ChannelHandler;
/*   9:    */ import io.netty.channel.ChannelHandlerContext;
/*  10:    */ import io.netty.channel.ChannelInboundHandlerAdapter;
/*  11:    */ import io.netty.channel.ChannelInitializer;
/*  12:    */ import io.netty.channel.ChannelOption;
/*  13:    */ import io.netty.channel.ChannelPipeline;
/*  14:    */ import io.netty.channel.EventLoop;
/*  15:    */ import io.netty.channel.EventLoopGroup;
/*  16:    */ import io.netty.channel.ServerChannel;
/*  17:    */ import io.netty.util.Attribute;
/*  18:    */ import io.netty.util.AttributeKey;
/*  19:    */ import io.netty.util.internal.logging.InternalLogger;
/*  20:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  21:    */ import java.util.LinkedHashMap;
/*  22:    */ import java.util.Map;
/*  23:    */ import java.util.Map.Entry;
/*  24:    */ import java.util.Set;
/*  25:    */ import java.util.concurrent.TimeUnit;
/*  26:    */ 
/*  27:    */ public class ServerBootstrap
/*  28:    */   extends AbstractBootstrap<ServerBootstrap, ServerChannel>
/*  29:    */ {
/*  30: 45 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(ServerBootstrap.class);
/*  31: 47 */   private final Map<ChannelOption<?>, Object> childOptions = new LinkedHashMap();
/*  32: 48 */   private final Map<AttributeKey<?>, Object> childAttrs = new LinkedHashMap();
/*  33: 49 */   private final ServerBootstrapConfig config = new ServerBootstrapConfig(this);
/*  34:    */   private volatile EventLoopGroup childGroup;
/*  35:    */   private volatile ChannelHandler childHandler;
/*  36:    */   
/*  37:    */   public ServerBootstrap() {}
/*  38:    */   
/*  39:    */   private ServerBootstrap(ServerBootstrap bootstrap)
/*  40:    */   {
/*  41: 56 */     super(bootstrap);
/*  42: 57 */     this.childGroup = bootstrap.childGroup;
/*  43: 58 */     this.childHandler = bootstrap.childHandler;
/*  44: 59 */     synchronized (bootstrap.childOptions)
/*  45:    */     {
/*  46: 60 */       this.childOptions.putAll(bootstrap.childOptions);
/*  47:    */     }
/*  48: 62 */     synchronized (bootstrap.childAttrs)
/*  49:    */     {
/*  50: 63 */       this.childAttrs.putAll(bootstrap.childAttrs);
/*  51:    */     }
/*  52:    */   }
/*  53:    */   
/*  54:    */   public ServerBootstrap group(EventLoopGroup group)
/*  55:    */   {
/*  56: 72 */     return group(group, group);
/*  57:    */   }
/*  58:    */   
/*  59:    */   public ServerBootstrap group(EventLoopGroup parentGroup, EventLoopGroup childGroup)
/*  60:    */   {
/*  61: 81 */     super.group(parentGroup);
/*  62: 82 */     if (childGroup == null) {
/*  63: 83 */       throw new NullPointerException("childGroup");
/*  64:    */     }
/*  65: 85 */     if (this.childGroup != null) {
/*  66: 86 */       throw new IllegalStateException("childGroup set already");
/*  67:    */     }
/*  68: 88 */     this.childGroup = childGroup;
/*  69: 89 */     return this;
/*  70:    */   }
/*  71:    */   
/*  72:    */   public <T> ServerBootstrap childOption(ChannelOption<T> childOption, T value)
/*  73:    */   {
/*  74: 98 */     if (childOption == null) {
/*  75: 99 */       throw new NullPointerException("childOption");
/*  76:    */     }
/*  77:101 */     if (value == null) {
/*  78:102 */       synchronized (this.childOptions)
/*  79:    */       {
/*  80:103 */         this.childOptions.remove(childOption);
/*  81:    */       }
/*  82:    */     } else {
/*  83:106 */       synchronized (this.childOptions)
/*  84:    */       {
/*  85:107 */         this.childOptions.put(childOption, value);
/*  86:    */       }
/*  87:    */     }
/*  88:110 */     return this;
/*  89:    */   }
/*  90:    */   
/*  91:    */   public <T> ServerBootstrap childAttr(AttributeKey<T> childKey, T value)
/*  92:    */   {
/*  93:118 */     if (childKey == null) {
/*  94:119 */       throw new NullPointerException("childKey");
/*  95:    */     }
/*  96:121 */     if (value == null) {
/*  97:122 */       this.childAttrs.remove(childKey);
/*  98:    */     } else {
/*  99:124 */       this.childAttrs.put(childKey, value);
/* 100:    */     }
/* 101:126 */     return this;
/* 102:    */   }
/* 103:    */   
/* 104:    */   public ServerBootstrap childHandler(ChannelHandler childHandler)
/* 105:    */   {
/* 106:133 */     if (childHandler == null) {
/* 107:134 */       throw new NullPointerException("childHandler");
/* 108:    */     }
/* 109:136 */     this.childHandler = childHandler;
/* 110:137 */     return this;
/* 111:    */   }
/* 112:    */   
/* 113:    */   void init(Channel channel)
/* 114:    */     throws Exception
/* 115:    */   {
/* 116:142 */     Map<ChannelOption<?>, Object> options = options0();
/* 117:143 */     synchronized (options)
/* 118:    */     {
/* 119:144 */       setChannelOptions(channel, options, logger);
/* 120:    */     }
/* 121:147 */     Map<AttributeKey<?>, Object> attrs = attrs0();
/* 122:148 */     synchronized (attrs)
/* 123:    */     {
/* 124:149 */       for (Map.Entry<AttributeKey<?>, Object> e : attrs.entrySet())
/* 125:    */       {
/* 126:151 */         AttributeKey<Object> key = (AttributeKey)e.getKey();
/* 127:152 */         channel.attr(key).set(e.getValue());
/* 128:    */       }
/* 129:    */     }
/* 130:156 */     ChannelPipeline p = channel.pipeline();
/* 131:    */     
/* 132:158 */     final EventLoopGroup currentChildGroup = this.childGroup;
/* 133:159 */     final ChannelHandler currentChildHandler = this.childHandler;
/* 134:    */     Map.Entry<ChannelOption<?>, Object>[] currentChildOptions;
/* 135:162 */     synchronized (this.childOptions)
/* 136:    */     {
/* 137:163 */       currentChildOptions = (Map.Entry[])this.childOptions.entrySet().toArray(newOptionArray(this.childOptions.size()));
/* 138:    */     }
/* 139:    */     final Map.Entry<ChannelOption<?>, Object>[] currentChildOptions;
/* 140:    */     Object currentChildAttrs;
/* 141:165 */     synchronized (this.childAttrs)
/* 142:    */     {
/* 143:166 */       currentChildAttrs = (Map.Entry[])this.childAttrs.entrySet().toArray(newAttrArray(this.childAttrs.size()));
/* 144:    */     }
/* 145:    */     final Map.Entry<AttributeKey<?>, Object>[] currentChildAttrs;
/* 146:169 */     p.addLast(new ChannelHandler[] { new ChannelInitializer()
/* 147:    */     {
/* 148:    */       public void initChannel(final Channel ch)
/* 149:    */         throws Exception
/* 150:    */       {
/* 151:172 */         final ChannelPipeline pipeline = ch.pipeline();
/* 152:173 */         ChannelHandler handler = ServerBootstrap.this.config.handler();
/* 153:174 */         if (handler != null) {
/* 154:175 */           pipeline.addLast(new ChannelHandler[] { handler });
/* 155:    */         }
/* 156:178 */         ch.eventLoop().execute(new Runnable()
/* 157:    */         {
/* 158:    */           public void run()
/* 159:    */           {
/* 160:181 */             pipeline.addLast(new ChannelHandler[] { new ServerBootstrap.ServerBootstrapAcceptor(ch, ServerBootstrap.1.this.val$currentChildGroup, ServerBootstrap.1.this.val$currentChildHandler, ServerBootstrap.1.this.val$currentChildOptions, ServerBootstrap.1.this.val$currentChildAttrs) });
/* 161:    */           }
/* 162:    */         });
/* 163:    */       }
/* 164:    */     } });
/* 165:    */   }
/* 166:    */   
/* 167:    */   public ServerBootstrap validate()
/* 168:    */   {
/* 169:191 */     super.validate();
/* 170:192 */     if (this.childHandler == null) {
/* 171:193 */       throw new IllegalStateException("childHandler not set");
/* 172:    */     }
/* 173:195 */     if (this.childGroup == null)
/* 174:    */     {
/* 175:196 */       logger.warn("childGroup is not set. Using parentGroup instead.");
/* 176:197 */       this.childGroup = this.config.group();
/* 177:    */     }
/* 178:199 */     return this;
/* 179:    */   }
/* 180:    */   
/* 181:    */   private static Map.Entry<AttributeKey<?>, Object>[] newAttrArray(int size)
/* 182:    */   {
/* 183:204 */     return new Map.Entry[size];
/* 184:    */   }
/* 185:    */   
/* 186:    */   private static Map.Entry<ChannelOption<?>, Object>[] newOptionArray(int size)
/* 187:    */   {
/* 188:209 */     return new Map.Entry[size];
/* 189:    */   }
/* 190:    */   
/* 191:    */   private static class ServerBootstrapAcceptor
/* 192:    */     extends ChannelInboundHandlerAdapter
/* 193:    */   {
/* 194:    */     private final EventLoopGroup childGroup;
/* 195:    */     private final ChannelHandler childHandler;
/* 196:    */     private final Map.Entry<ChannelOption<?>, Object>[] childOptions;
/* 197:    */     private final Map.Entry<AttributeKey<?>, Object>[] childAttrs;
/* 198:    */     private final Runnable enableAutoReadTask;
/* 199:    */     
/* 200:    */     ServerBootstrapAcceptor(final Channel channel, EventLoopGroup childGroup, ChannelHandler childHandler, Map.Entry<ChannelOption<?>, Object>[] childOptions, Map.Entry<AttributeKey<?>, Object>[] childAttrs)
/* 201:    */     {
/* 202:223 */       this.childGroup = childGroup;
/* 203:224 */       this.childHandler = childHandler;
/* 204:225 */       this.childOptions = childOptions;
/* 205:226 */       this.childAttrs = childAttrs;
/* 206:    */       
/* 207:    */ 
/* 208:    */ 
/* 209:    */ 
/* 210:    */ 
/* 211:    */ 
/* 212:233 */       this.enableAutoReadTask = new Runnable()
/* 213:    */       {
/* 214:    */         public void run()
/* 215:    */         {
/* 216:236 */           channel.config().setAutoRead(true);
/* 217:    */         }
/* 218:    */       };
/* 219:    */     }
/* 220:    */     
/* 221:    */     public void channelRead(ChannelHandlerContext ctx, Object msg)
/* 222:    */     {
/* 223:244 */       final Channel child = (Channel)msg;
/* 224:    */       
/* 225:246 */       child.pipeline().addLast(new ChannelHandler[] { this.childHandler });
/* 226:    */       
/* 227:248 */       AbstractBootstrap.setChannelOptions(child, this.childOptions, ServerBootstrap.logger);
/* 228:250 */       for (Map.Entry<AttributeKey<?>, Object> e : this.childAttrs) {
/* 229:251 */         child.attr((AttributeKey)e.getKey()).set(e.getValue());
/* 230:    */       }
/* 231:    */       try
/* 232:    */       {
/* 233:255 */         this.childGroup.register(child).addListener(new ChannelFutureListener()
/* 234:    */         {
/* 235:    */           public void operationComplete(ChannelFuture future)
/* 236:    */             throws Exception
/* 237:    */           {
/* 238:258 */             if (!future.isSuccess()) {
/* 239:259 */               ServerBootstrap.ServerBootstrapAcceptor.forceClose(child, future.cause());
/* 240:    */             }
/* 241:    */           }
/* 242:    */         });
/* 243:    */       }
/* 244:    */       catch (Throwable t)
/* 245:    */       {
/* 246:264 */         forceClose(child, t);
/* 247:    */       }
/* 248:    */     }
/* 249:    */     
/* 250:    */     private static void forceClose(Channel child, Throwable t)
/* 251:    */     {
/* 252:269 */       child.unsafe().closeForcibly();
/* 253:270 */       ServerBootstrap.logger.warn("Failed to register an accepted channel: {}", child, t);
/* 254:    */     }
/* 255:    */     
/* 256:    */     public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
/* 257:    */       throws Exception
/* 258:    */     {
/* 259:275 */       ChannelConfig config = ctx.channel().config();
/* 260:276 */       if (config.isAutoRead())
/* 261:    */       {
/* 262:279 */         config.setAutoRead(false);
/* 263:280 */         ctx.channel().eventLoop().schedule(this.enableAutoReadTask, 1L, TimeUnit.SECONDS);
/* 264:    */       }
/* 265:284 */       ctx.fireExceptionCaught(cause);
/* 266:    */     }
/* 267:    */   }
/* 268:    */   
/* 269:    */   public ServerBootstrap clone()
/* 270:    */   {
/* 271:291 */     return new ServerBootstrap(this);
/* 272:    */   }
/* 273:    */   
/* 274:    */   @Deprecated
/* 275:    */   public EventLoopGroup childGroup()
/* 276:    */   {
/* 277:302 */     return this.childGroup;
/* 278:    */   }
/* 279:    */   
/* 280:    */   final ChannelHandler childHandler()
/* 281:    */   {
/* 282:306 */     return this.childHandler;
/* 283:    */   }
/* 284:    */   
/* 285:    */   final Map<ChannelOption<?>, Object> childOptions()
/* 286:    */   {
/* 287:310 */     return copiedMap(this.childOptions);
/* 288:    */   }
/* 289:    */   
/* 290:    */   final Map<AttributeKey<?>, Object> childAttrs()
/* 291:    */   {
/* 292:314 */     return copiedMap(this.childAttrs);
/* 293:    */   }
/* 294:    */   
/* 295:    */   public final ServerBootstrapConfig config()
/* 296:    */   {
/* 297:319 */     return this.config;
/* 298:    */   }
/* 299:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.bootstrap.ServerBootstrap
 * JD-Core Version:    0.7.0.1
 */