/*   1:    */ package io.netty.bootstrap;
/*   2:    */ 
/*   3:    */ import io.netty.channel.Channel;
/*   4:    */ import io.netty.channel.ChannelFuture;
/*   5:    */ import io.netty.channel.ChannelFutureListener;
/*   6:    */ import io.netty.channel.ChannelHandler;
/*   7:    */ import io.netty.channel.ChannelOption;
/*   8:    */ import io.netty.channel.ChannelPipeline;
/*   9:    */ import io.netty.channel.ChannelPromise;
/*  10:    */ import io.netty.channel.EventLoop;
/*  11:    */ import io.netty.channel.EventLoopGroup;
/*  12:    */ import io.netty.resolver.AddressResolver;
/*  13:    */ import io.netty.resolver.AddressResolverGroup;
/*  14:    */ import io.netty.resolver.DefaultAddressResolverGroup;
/*  15:    */ import io.netty.util.Attribute;
/*  16:    */ import io.netty.util.AttributeKey;
/*  17:    */ import io.netty.util.concurrent.Future;
/*  18:    */ import io.netty.util.concurrent.FutureListener;
/*  19:    */ import io.netty.util.internal.logging.InternalLogger;
/*  20:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  21:    */ import java.net.InetAddress;
/*  22:    */ import java.net.InetSocketAddress;
/*  23:    */ import java.net.SocketAddress;
/*  24:    */ import java.util.Map;
/*  25:    */ import java.util.Map.Entry;
/*  26:    */ 
/*  27:    */ public class Bootstrap
/*  28:    */   extends AbstractBootstrap<Bootstrap, Channel>
/*  29:    */ {
/*  30: 51 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(Bootstrap.class);
/*  31: 53 */   private static final AddressResolverGroup<?> DEFAULT_RESOLVER = DefaultAddressResolverGroup.INSTANCE;
/*  32: 55 */   private final BootstrapConfig config = new BootstrapConfig(this);
/*  33: 57 */   private volatile AddressResolverGroup<SocketAddress> resolver = DEFAULT_RESOLVER;
/*  34:    */   private volatile SocketAddress remoteAddress;
/*  35:    */   
/*  36:    */   public Bootstrap() {}
/*  37:    */   
/*  38:    */   private Bootstrap(Bootstrap bootstrap)
/*  39:    */   {
/*  40: 65 */     super(bootstrap);
/*  41: 66 */     this.resolver = bootstrap.resolver;
/*  42: 67 */     this.remoteAddress = bootstrap.remoteAddress;
/*  43:    */   }
/*  44:    */   
/*  45:    */   public Bootstrap resolver(AddressResolverGroup<?> resolver)
/*  46:    */   {
/*  47: 80 */     this.resolver = (resolver == null ? DEFAULT_RESOLVER : resolver);
/*  48: 81 */     return this;
/*  49:    */   }
/*  50:    */   
/*  51:    */   public Bootstrap remoteAddress(SocketAddress remoteAddress)
/*  52:    */   {
/*  53: 89 */     this.remoteAddress = remoteAddress;
/*  54: 90 */     return this;
/*  55:    */   }
/*  56:    */   
/*  57:    */   public Bootstrap remoteAddress(String inetHost, int inetPort)
/*  58:    */   {
/*  59: 97 */     this.remoteAddress = InetSocketAddress.createUnresolved(inetHost, inetPort);
/*  60: 98 */     return this;
/*  61:    */   }
/*  62:    */   
/*  63:    */   public Bootstrap remoteAddress(InetAddress inetHost, int inetPort)
/*  64:    */   {
/*  65:105 */     this.remoteAddress = new InetSocketAddress(inetHost, inetPort);
/*  66:106 */     return this;
/*  67:    */   }
/*  68:    */   
/*  69:    */   public ChannelFuture connect()
/*  70:    */   {
/*  71:113 */     validate();
/*  72:114 */     SocketAddress remoteAddress = this.remoteAddress;
/*  73:115 */     if (remoteAddress == null) {
/*  74:116 */       throw new IllegalStateException("remoteAddress not set");
/*  75:    */     }
/*  76:119 */     return doResolveAndConnect(remoteAddress, this.config.localAddress());
/*  77:    */   }
/*  78:    */   
/*  79:    */   public ChannelFuture connect(String inetHost, int inetPort)
/*  80:    */   {
/*  81:126 */     return connect(InetSocketAddress.createUnresolved(inetHost, inetPort));
/*  82:    */   }
/*  83:    */   
/*  84:    */   public ChannelFuture connect(InetAddress inetHost, int inetPort)
/*  85:    */   {
/*  86:133 */     return connect(new InetSocketAddress(inetHost, inetPort));
/*  87:    */   }
/*  88:    */   
/*  89:    */   public ChannelFuture connect(SocketAddress remoteAddress)
/*  90:    */   {
/*  91:140 */     if (remoteAddress == null) {
/*  92:141 */       throw new NullPointerException("remoteAddress");
/*  93:    */     }
/*  94:144 */     validate();
/*  95:145 */     return doResolveAndConnect(remoteAddress, this.config.localAddress());
/*  96:    */   }
/*  97:    */   
/*  98:    */   public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress)
/*  99:    */   {
/* 100:152 */     if (remoteAddress == null) {
/* 101:153 */       throw new NullPointerException("remoteAddress");
/* 102:    */     }
/* 103:155 */     validate();
/* 104:156 */     return doResolveAndConnect(remoteAddress, localAddress);
/* 105:    */   }
/* 106:    */   
/* 107:    */   private ChannelFuture doResolveAndConnect(final SocketAddress remoteAddress, final SocketAddress localAddress)
/* 108:    */   {
/* 109:163 */     ChannelFuture regFuture = initAndRegister();
/* 110:164 */     final Channel channel = regFuture.channel();
/* 111:166 */     if (regFuture.isDone())
/* 112:    */     {
/* 113:167 */       if (!regFuture.isSuccess()) {
/* 114:168 */         return regFuture;
/* 115:    */       }
/* 116:170 */       return doResolveAndConnect0(channel, remoteAddress, localAddress, channel.newPromise());
/* 117:    */     }
/* 118:173 */     final AbstractBootstrap.PendingRegistrationPromise promise = new AbstractBootstrap.PendingRegistrationPromise(channel);
/* 119:174 */     regFuture.addListener(new ChannelFutureListener()
/* 120:    */     {
/* 121:    */       public void operationComplete(ChannelFuture future)
/* 122:    */         throws Exception
/* 123:    */       {
/* 124:179 */         Throwable cause = future.cause();
/* 125:180 */         if (cause != null)
/* 126:    */         {
/* 127:183 */           promise.setFailure(cause);
/* 128:    */         }
/* 129:    */         else
/* 130:    */         {
/* 131:187 */           promise.registered();
/* 132:188 */           Bootstrap.this.doResolveAndConnect0(channel, remoteAddress, localAddress, promise);
/* 133:    */         }
/* 134:    */       }
/* 135:191 */     });
/* 136:192 */     return promise;
/* 137:    */   }
/* 138:    */   
/* 139:    */   private ChannelFuture doResolveAndConnect0(final Channel channel, SocketAddress remoteAddress, final SocketAddress localAddress, final ChannelPromise promise)
/* 140:    */   {
/* 141:    */     try
/* 142:    */     {
/* 143:199 */       EventLoop eventLoop = channel.eventLoop();
/* 144:200 */       AddressResolver<SocketAddress> resolver = this.resolver.getResolver(eventLoop);
/* 145:202 */       if ((!resolver.isSupported(remoteAddress)) || (resolver.isResolved(remoteAddress)))
/* 146:    */       {
/* 147:204 */         doConnect(remoteAddress, localAddress, promise);
/* 148:205 */         return promise;
/* 149:    */       }
/* 150:208 */       Future<SocketAddress> resolveFuture = resolver.resolve(remoteAddress);
/* 151:210 */       if (resolveFuture.isDone())
/* 152:    */       {
/* 153:211 */         Throwable resolveFailureCause = resolveFuture.cause();
/* 154:213 */         if (resolveFailureCause != null)
/* 155:    */         {
/* 156:215 */           channel.close();
/* 157:216 */           promise.setFailure(resolveFailureCause);
/* 158:    */         }
/* 159:    */         else
/* 160:    */         {
/* 161:219 */           doConnect((SocketAddress)resolveFuture.getNow(), localAddress, promise);
/* 162:    */         }
/* 163:221 */         return promise;
/* 164:    */       }
/* 165:225 */       resolveFuture.addListener(new FutureListener()
/* 166:    */       {
/* 167:    */         public void operationComplete(Future<SocketAddress> future)
/* 168:    */           throws Exception
/* 169:    */         {
/* 170:228 */           if (future.cause() != null)
/* 171:    */           {
/* 172:229 */             channel.close();
/* 173:230 */             promise.setFailure(future.cause());
/* 174:    */           }
/* 175:    */           else
/* 176:    */           {
/* 177:232 */             Bootstrap.doConnect((SocketAddress)future.getNow(), localAddress, promise);
/* 178:    */           }
/* 179:    */         }
/* 180:    */       });
/* 181:    */     }
/* 182:    */     catch (Throwable cause)
/* 183:    */     {
/* 184:237 */       promise.tryFailure(cause);
/* 185:    */     }
/* 186:239 */     return promise;
/* 187:    */   }
/* 188:    */   
/* 189:    */   private static void doConnect(final SocketAddress remoteAddress, SocketAddress localAddress, final ChannelPromise connectPromise)
/* 190:    */   {
/* 191:247 */     final Channel channel = connectPromise.channel();
/* 192:248 */     channel.eventLoop().execute(new Runnable()
/* 193:    */     {
/* 194:    */       public void run()
/* 195:    */       {
/* 196:251 */         if (this.val$localAddress == null) {
/* 197:252 */           channel.connect(remoteAddress, connectPromise);
/* 198:    */         } else {
/* 199:254 */           channel.connect(remoteAddress, this.val$localAddress, connectPromise);
/* 200:    */         }
/* 201:256 */         connectPromise.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
/* 202:    */       }
/* 203:    */     });
/* 204:    */   }
/* 205:    */   
/* 206:    */   void init(Channel channel)
/* 207:    */     throws Exception
/* 208:    */   {
/* 209:264 */     ChannelPipeline p = channel.pipeline();
/* 210:265 */     p.addLast(new ChannelHandler[] { this.config.handler() });
/* 211:    */     
/* 212:267 */     Map<ChannelOption<?>, Object> options = options0();
/* 213:268 */     synchronized (options)
/* 214:    */     {
/* 215:269 */       setChannelOptions(channel, options, logger);
/* 216:    */     }
/* 217:272 */     Map<AttributeKey<?>, Object> attrs = attrs0();
/* 218:273 */     synchronized (attrs)
/* 219:    */     {
/* 220:274 */       for (Map.Entry<AttributeKey<?>, Object> e : attrs.entrySet()) {
/* 221:275 */         channel.attr((AttributeKey)e.getKey()).set(e.getValue());
/* 222:    */       }
/* 223:    */     }
/* 224:    */   }
/* 225:    */   
/* 226:    */   public Bootstrap validate()
/* 227:    */   {
/* 228:282 */     super.validate();
/* 229:283 */     if (this.config.handler() == null) {
/* 230:284 */       throw new IllegalStateException("handler not set");
/* 231:    */     }
/* 232:286 */     return this;
/* 233:    */   }
/* 234:    */   
/* 235:    */   public Bootstrap clone()
/* 236:    */   {
/* 237:292 */     return new Bootstrap(this);
/* 238:    */   }
/* 239:    */   
/* 240:    */   public Bootstrap clone(EventLoopGroup group)
/* 241:    */   {
/* 242:301 */     Bootstrap bs = new Bootstrap(this);
/* 243:302 */     bs.group = group;
/* 244:303 */     return bs;
/* 245:    */   }
/* 246:    */   
/* 247:    */   public final BootstrapConfig config()
/* 248:    */   {
/* 249:308 */     return this.config;
/* 250:    */   }
/* 251:    */   
/* 252:    */   final SocketAddress remoteAddress()
/* 253:    */   {
/* 254:312 */     return this.remoteAddress;
/* 255:    */   }
/* 256:    */   
/* 257:    */   final AddressResolverGroup<?> resolver()
/* 258:    */   {
/* 259:316 */     return this.resolver;
/* 260:    */   }
/* 261:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.bootstrap.Bootstrap
 * JD-Core Version:    0.7.0.1
 */