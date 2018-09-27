/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import io.netty.channel.ChannelHandlerContext;
/*   4:    */ import io.netty.channel.ChannelPipeline;
/*   5:    */ import io.netty.handler.codec.DecoderException;
/*   6:    */ import io.netty.util.AsyncMapping;
/*   7:    */ import io.netty.util.DomainNameMapping;
/*   8:    */ import io.netty.util.Mapping;
/*   9:    */ import io.netty.util.ReferenceCountUtil;
/*  10:    */ import io.netty.util.concurrent.EventExecutor;
/*  11:    */ import io.netty.util.concurrent.Future;
/*  12:    */ import io.netty.util.concurrent.Promise;
/*  13:    */ import io.netty.util.internal.ObjectUtil;
/*  14:    */ import io.netty.util.internal.PlatformDependent;
/*  15:    */ 
/*  16:    */ public class SniHandler
/*  17:    */   extends AbstractSniHandler<SslContext>
/*  18:    */ {
/*  19: 37 */   private static final Selection EMPTY_SELECTION = new Selection(null, null);
/*  20:    */   protected final AsyncMapping<String, SslContext> mapping;
/*  21: 41 */   private volatile Selection selection = EMPTY_SELECTION;
/*  22:    */   
/*  23:    */   public SniHandler(Mapping<? super String, ? extends SslContext> mapping)
/*  24:    */   {
/*  25: 50 */     this(new AsyncMappingAdapter(mapping, null));
/*  26:    */   }
/*  27:    */   
/*  28:    */   public SniHandler(DomainNameMapping<? extends SslContext> mapping)
/*  29:    */   {
/*  30: 60 */     this(mapping);
/*  31:    */   }
/*  32:    */   
/*  33:    */   public SniHandler(AsyncMapping<? super String, ? extends SslContext> mapping)
/*  34:    */   {
/*  35: 71 */     this.mapping = ((AsyncMapping)ObjectUtil.checkNotNull(mapping, "mapping"));
/*  36:    */   }
/*  37:    */   
/*  38:    */   public String hostname()
/*  39:    */   {
/*  40: 78 */     return this.selection.hostname;
/*  41:    */   }
/*  42:    */   
/*  43:    */   public SslContext sslContext()
/*  44:    */   {
/*  45: 85 */     return this.selection.context;
/*  46:    */   }
/*  47:    */   
/*  48:    */   protected Future<SslContext> lookup(ChannelHandlerContext ctx, String hostname)
/*  49:    */     throws Exception
/*  50:    */   {
/*  51: 96 */     return this.mapping.map(hostname, ctx.executor().newPromise());
/*  52:    */   }
/*  53:    */   
/*  54:    */   protected final void onLookupComplete(ChannelHandlerContext ctx, String hostname, Future<SslContext> future)
/*  55:    */     throws Exception
/*  56:    */   {
/*  57:102 */     if (!future.isSuccess())
/*  58:    */     {
/*  59:103 */       Throwable cause = future.cause();
/*  60:104 */       if ((cause instanceof Error)) {
/*  61:105 */         throw ((Error)cause);
/*  62:    */       }
/*  63:107 */       throw new DecoderException("failed to get the SslContext for " + hostname, cause);
/*  64:    */     }
/*  65:110 */     SslContext sslContext = (SslContext)future.getNow();
/*  66:111 */     this.selection = new Selection(sslContext, hostname);
/*  67:    */     try
/*  68:    */     {
/*  69:113 */       replaceHandler(ctx, hostname, sslContext);
/*  70:    */     }
/*  71:    */     catch (Throwable cause)
/*  72:    */     {
/*  73:115 */       this.selection = EMPTY_SELECTION;
/*  74:116 */       PlatformDependent.throwException(cause);
/*  75:    */     }
/*  76:    */   }
/*  77:    */   
/*  78:    */   protected void replaceHandler(ChannelHandlerContext ctx, String hostname, SslContext sslContext)
/*  79:    */     throws Exception
/*  80:    */   {
/*  81:130 */     SslHandler sslHandler = null;
/*  82:    */     try
/*  83:    */     {
/*  84:132 */       sslHandler = sslContext.newHandler(ctx.alloc());
/*  85:133 */       ctx.pipeline().replace(this, SslHandler.class.getName(), sslHandler);
/*  86:134 */       sslHandler = null;
/*  87:    */     }
/*  88:    */     finally
/*  89:    */     {
/*  90:139 */       if (sslHandler != null) {
/*  91:140 */         ReferenceCountUtil.safeRelease(sslHandler.engine());
/*  92:    */       }
/*  93:    */     }
/*  94:    */   }
/*  95:    */   
/*  96:    */   private static final class AsyncMappingAdapter
/*  97:    */     implements AsyncMapping<String, SslContext>
/*  98:    */   {
/*  99:    */     private final Mapping<? super String, ? extends SslContext> mapping;
/* 100:    */     
/* 101:    */     private AsyncMappingAdapter(Mapping<? super String, ? extends SslContext> mapping)
/* 102:    */     {
/* 103:149 */       this.mapping = ((Mapping)ObjectUtil.checkNotNull(mapping, "mapping"));
/* 104:    */     }
/* 105:    */     
/* 106:    */     public Future<SslContext> map(String input, Promise<SslContext> promise)
/* 107:    */     {
/* 108:    */       try
/* 109:    */       {
/* 110:156 */         context = (SslContext)this.mapping.map(input);
/* 111:    */       }
/* 112:    */       catch (Throwable cause)
/* 113:    */       {
/* 114:    */         SslContext context;
/* 115:158 */         return promise.setFailure(cause);
/* 116:    */       }
/* 117:    */       SslContext context;
/* 118:160 */       return promise.setSuccess(context);
/* 119:    */     }
/* 120:    */   }
/* 121:    */   
/* 122:    */   private static final class Selection
/* 123:    */   {
/* 124:    */     final SslContext context;
/* 125:    */     final String hostname;
/* 126:    */     
/* 127:    */     Selection(SslContext context, String hostname)
/* 128:    */     {
/* 129:169 */       this.context = context;
/* 130:170 */       this.hostname = hostname;
/* 131:    */     }
/* 132:    */   }
/* 133:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.SniHandler
 * JD-Core Version:    0.7.0.1
 */