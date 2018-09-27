/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.util.internal.ObjectUtil;
/*   6:    */ import io.netty.util.internal.SystemPropertyUtil;
/*   7:    */ import java.nio.ByteBuffer;
/*   8:    */ import java.util.Collections;
/*   9:    */ import java.util.LinkedHashSet;
/*  10:    */ import java.util.List;
/*  11:    */ import javax.net.ssl.SSLEngine;
/*  12:    */ import javax.net.ssl.SSLEngineResult;
/*  13:    */ import javax.net.ssl.SSLException;
/*  14:    */ import org.conscrypt.AllocatedBuffer;
/*  15:    */ import org.conscrypt.BufferAllocator;
/*  16:    */ import org.conscrypt.Conscrypt;
/*  17:    */ import org.conscrypt.HandshakeListener;
/*  18:    */ 
/*  19:    */ abstract class ConscryptAlpnSslEngine
/*  20:    */   extends JdkSslEngine
/*  21:    */ {
/*  22: 44 */   private static final boolean USE_BUFFER_ALLOCATOR = SystemPropertyUtil.getBoolean("io.netty.handler.ssl.conscrypt.useBufferAllocator", true);
/*  23:    */   
/*  24:    */   static ConscryptAlpnSslEngine newClientEngine(SSLEngine engine, ByteBufAllocator alloc, JdkApplicationProtocolNegotiator applicationNegotiator)
/*  25:    */   {
/*  26: 49 */     return new ClientEngine(engine, alloc, applicationNegotiator);
/*  27:    */   }
/*  28:    */   
/*  29:    */   static ConscryptAlpnSslEngine newServerEngine(SSLEngine engine, ByteBufAllocator alloc, JdkApplicationProtocolNegotiator applicationNegotiator)
/*  30:    */   {
/*  31: 54 */     return new ServerEngine(engine, alloc, applicationNegotiator);
/*  32:    */   }
/*  33:    */   
/*  34:    */   private ConscryptAlpnSslEngine(SSLEngine engine, ByteBufAllocator alloc, List<String> protocols)
/*  35:    */   {
/*  36: 58 */     super(engine);
/*  37: 69 */     if (USE_BUFFER_ALLOCATOR) {
/*  38: 70 */       Conscrypt.setBufferAllocator(engine, new BufferAllocatorAdapter(alloc));
/*  39:    */     }
/*  40: 74 */     Conscrypt.setApplicationProtocols(engine, (String[])protocols.toArray(new String[protocols.size()]));
/*  41:    */   }
/*  42:    */   
/*  43:    */   final int calculateOutNetBufSize(int plaintextBytes, int numBuffers)
/*  44:    */   {
/*  45: 87 */     long maxOverhead = Conscrypt.maxSealOverhead(getWrappedEngine()) * numBuffers;
/*  46:    */     
/*  47: 89 */     return (int)Math.min(2147483647L, plaintextBytes + maxOverhead);
/*  48:    */   }
/*  49:    */   
/*  50:    */   final SSLEngineResult unwrap(ByteBuffer[] srcs, ByteBuffer[] dests)
/*  51:    */     throws SSLException
/*  52:    */   {
/*  53: 93 */     return Conscrypt.unwrap(getWrappedEngine(), srcs, dests);
/*  54:    */   }
/*  55:    */   
/*  56:    */   private static final class ClientEngine
/*  57:    */     extends ConscryptAlpnSslEngine
/*  58:    */   {
/*  59:    */     private final JdkApplicationProtocolNegotiator.ProtocolSelectionListener protocolListener;
/*  60:    */     
/*  61:    */     ClientEngine(SSLEngine engine, ByteBufAllocator alloc, JdkApplicationProtocolNegotiator applicationNegotiator)
/*  62:    */     {
/*  63:101 */       super(alloc, applicationNegotiator.protocols(), null);
/*  64:    */       
/*  65:103 */       Conscrypt.setHandshakeListener(engine, new HandshakeListener()
/*  66:    */       {
/*  67:    */         public void onHandshakeFinished()
/*  68:    */           throws SSLException
/*  69:    */         {
/*  70:106 */           ConscryptAlpnSslEngine.ClientEngine.this.selectProtocol();
/*  71:    */         }
/*  72:109 */       });
/*  73:110 */       this.protocolListener = ((JdkApplicationProtocolNegotiator.ProtocolSelectionListener)ObjectUtil.checkNotNull(applicationNegotiator
/*  74:111 */         .protocolListenerFactory().newListener(this, applicationNegotiator.protocols()), "protocolListener"));
/*  75:    */     }
/*  76:    */     
/*  77:    */     private void selectProtocol()
/*  78:    */       throws SSLException
/*  79:    */     {
/*  80:116 */       String protocol = Conscrypt.getApplicationProtocol(getWrappedEngine());
/*  81:    */       try
/*  82:    */       {
/*  83:118 */         this.protocolListener.selected(protocol);
/*  84:    */       }
/*  85:    */       catch (Throwable e)
/*  86:    */       {
/*  87:120 */         throw SslUtils.toSSLHandshakeException(e);
/*  88:    */       }
/*  89:    */     }
/*  90:    */   }
/*  91:    */   
/*  92:    */   private static final class ServerEngine
/*  93:    */     extends ConscryptAlpnSslEngine
/*  94:    */   {
/*  95:    */     private final JdkApplicationProtocolNegotiator.ProtocolSelector protocolSelector;
/*  96:    */     
/*  97:    */     ServerEngine(SSLEngine engine, ByteBufAllocator alloc, JdkApplicationProtocolNegotiator applicationNegotiator)
/*  98:    */     {
/*  99:130 */       super(alloc, applicationNegotiator.protocols(), null);
/* 100:    */       
/* 101:    */ 
/* 102:133 */       Conscrypt.setHandshakeListener(engine, new HandshakeListener()
/* 103:    */       {
/* 104:    */         public void onHandshakeFinished()
/* 105:    */           throws SSLException
/* 106:    */         {
/* 107:136 */           ConscryptAlpnSslEngine.ServerEngine.this.selectProtocol();
/* 108:    */         }
/* 109:139 */       });
/* 110:140 */       this.protocolSelector = ((JdkApplicationProtocolNegotiator.ProtocolSelector)ObjectUtil.checkNotNull(applicationNegotiator.protocolSelectorFactory()
/* 111:141 */         .newSelector(this, new LinkedHashSet(applicationNegotiator
/* 112:142 */         .protocols())), "protocolSelector"));
/* 113:    */     }
/* 114:    */     
/* 115:    */     private void selectProtocol()
/* 116:    */       throws SSLException
/* 117:    */     {
/* 118:    */       try
/* 119:    */       {
/* 120:148 */         String protocol = Conscrypt.getApplicationProtocol(getWrappedEngine());
/* 121:149 */         this.protocolSelector.select(protocol != null ? Collections.singletonList(protocol) : 
/* 122:150 */           Collections.emptyList());
/* 123:    */       }
/* 124:    */       catch (Throwable e)
/* 125:    */       {
/* 126:152 */         throw SslUtils.toSSLHandshakeException(e);
/* 127:    */       }
/* 128:    */     }
/* 129:    */   }
/* 130:    */   
/* 131:    */   private static final class BufferAllocatorAdapter
/* 132:    */     extends BufferAllocator
/* 133:    */   {
/* 134:    */     private final ByteBufAllocator alloc;
/* 135:    */     
/* 136:    */     BufferAllocatorAdapter(ByteBufAllocator alloc)
/* 137:    */     {
/* 138:161 */       this.alloc = alloc;
/* 139:    */     }
/* 140:    */     
/* 141:    */     public AllocatedBuffer allocateDirectBuffer(int capacity)
/* 142:    */     {
/* 143:166 */       return new ConscryptAlpnSslEngine.BufferAdapter(this.alloc.directBuffer(capacity));
/* 144:    */     }
/* 145:    */   }
/* 146:    */   
/* 147:    */   private static final class BufferAdapter
/* 148:    */     extends AllocatedBuffer
/* 149:    */   {
/* 150:    */     private final ByteBuf nettyBuffer;
/* 151:    */     private final ByteBuffer buffer;
/* 152:    */     
/* 153:    */     BufferAdapter(ByteBuf nettyBuffer)
/* 154:    */     {
/* 155:175 */       this.nettyBuffer = nettyBuffer;
/* 156:176 */       this.buffer = nettyBuffer.nioBuffer(0, nettyBuffer.capacity());
/* 157:    */     }
/* 158:    */     
/* 159:    */     public ByteBuffer nioBuffer()
/* 160:    */     {
/* 161:181 */       return this.buffer;
/* 162:    */     }
/* 163:    */     
/* 164:    */     public AllocatedBuffer retain()
/* 165:    */     {
/* 166:186 */       this.nettyBuffer.retain();
/* 167:187 */       return this;
/* 168:    */     }
/* 169:    */     
/* 170:    */     public AllocatedBuffer release()
/* 171:    */     {
/* 172:192 */       this.nettyBuffer.release();
/* 173:193 */       return this;
/* 174:    */     }
/* 175:    */   }
/* 176:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.ConscryptAlpnSslEngine
 * JD-Core Version:    0.7.0.1
 */