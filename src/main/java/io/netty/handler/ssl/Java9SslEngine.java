/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import java.nio.ByteBuffer;
/*   4:    */ import java.util.LinkedHashSet;
/*   5:    */ import java.util.List;
/*   6:    */ import java.util.function.BiFunction;
/*   7:    */ import javax.net.ssl.SSLEngine;
/*   8:    */ import javax.net.ssl.SSLEngineResult;
/*   9:    */ import javax.net.ssl.SSLEngineResult.HandshakeStatus;
/*  10:    */ import javax.net.ssl.SSLException;
/*  11:    */ 
/*  12:    */ final class Java9SslEngine
/*  13:    */   extends JdkSslEngine
/*  14:    */ {
/*  15:    */   private final JdkApplicationProtocolNegotiator.ProtocolSelectionListener selectionListener;
/*  16:    */   private final AlpnSelector alpnSelector;
/*  17:    */   
/*  18:    */   private final class AlpnSelector
/*  19:    */     implements BiFunction<SSLEngine, List<String>, String>
/*  20:    */   {
/*  21:    */     private final JdkApplicationProtocolNegotiator.ProtocolSelector selector;
/*  22:    */     private boolean called;
/*  23:    */     
/*  24:    */     AlpnSelector(JdkApplicationProtocolNegotiator.ProtocolSelector selector)
/*  25:    */     {
/*  26: 42 */       this.selector = selector;
/*  27:    */     }
/*  28:    */     
/*  29:    */     public String apply(SSLEngine sslEngine, List<String> strings)
/*  30:    */     {
/*  31: 47 */       assert (!this.called);
/*  32: 48 */       this.called = true;
/*  33:    */       try
/*  34:    */       {
/*  35: 51 */         String selected = this.selector.select(strings);
/*  36: 52 */         return selected == null ? "" : selected;
/*  37:    */       }
/*  38:    */       catch (Exception cause) {}
/*  39: 58 */       return null;
/*  40:    */     }
/*  41:    */     
/*  42:    */     void checkUnsupported()
/*  43:    */     {
/*  44: 63 */       if (this.called) {
/*  45: 68 */         return;
/*  46:    */       }
/*  47: 70 */       String protocol = Java9SslEngine.this.getApplicationProtocol();
/*  48: 71 */       assert (protocol != null);
/*  49: 73 */       if (protocol.isEmpty()) {
/*  50: 75 */         this.selector.unsupported();
/*  51:    */       }
/*  52:    */     }
/*  53:    */   }
/*  54:    */   
/*  55:    */   Java9SslEngine(SSLEngine engine, JdkApplicationProtocolNegotiator applicationNegotiator, boolean isServer)
/*  56:    */   {
/*  57: 81 */     super(engine);
/*  58: 82 */     if (isServer)
/*  59:    */     {
/*  60: 83 */       this.selectionListener = null;
/*  61:    */       
/*  62: 85 */       this.alpnSelector = new AlpnSelector(applicationNegotiator.protocolSelectorFactory().newSelector(this, new LinkedHashSet(applicationNegotiator.protocols())));
/*  63: 86 */       Java9SslUtils.setHandshakeApplicationProtocolSelector(engine, this.alpnSelector);
/*  64:    */     }
/*  65:    */     else
/*  66:    */     {
/*  67: 89 */       this.selectionListener = applicationNegotiator.protocolListenerFactory().newListener(this, applicationNegotiator.protocols());
/*  68: 90 */       this.alpnSelector = null;
/*  69: 91 */       Java9SslUtils.setApplicationProtocols(engine, applicationNegotiator.protocols());
/*  70:    */     }
/*  71:    */   }
/*  72:    */   
/*  73:    */   private SSLEngineResult verifyProtocolSelection(SSLEngineResult result)
/*  74:    */     throws SSLException
/*  75:    */   {
/*  76: 96 */     if (result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.FINISHED) {
/*  77: 97 */       if (this.alpnSelector == null)
/*  78:    */       {
/*  79:    */         try
/*  80:    */         {
/*  81:100 */           String protocol = getApplicationProtocol();
/*  82:101 */           assert (protocol != null);
/*  83:102 */           if (protocol.isEmpty()) {
/*  84:107 */             this.selectionListener.unsupported();
/*  85:    */           } else {
/*  86:109 */             this.selectionListener.selected(protocol);
/*  87:    */           }
/*  88:    */         }
/*  89:    */         catch (Throwable e)
/*  90:    */         {
/*  91:112 */           throw SslUtils.toSSLHandshakeException(e);
/*  92:    */         }
/*  93:    */       }
/*  94:    */       else
/*  95:    */       {
/*  96:115 */         assert (this.selectionListener == null);
/*  97:116 */         this.alpnSelector.checkUnsupported();
/*  98:    */       }
/*  99:    */     }
/* 100:119 */     return result;
/* 101:    */   }
/* 102:    */   
/* 103:    */   public SSLEngineResult wrap(ByteBuffer src, ByteBuffer dst)
/* 104:    */     throws SSLException
/* 105:    */   {
/* 106:124 */     return verifyProtocolSelection(super.wrap(src, dst));
/* 107:    */   }
/* 108:    */   
/* 109:    */   public SSLEngineResult wrap(ByteBuffer[] srcs, ByteBuffer dst)
/* 110:    */     throws SSLException
/* 111:    */   {
/* 112:129 */     return verifyProtocolSelection(super.wrap(srcs, dst));
/* 113:    */   }
/* 114:    */   
/* 115:    */   public SSLEngineResult wrap(ByteBuffer[] srcs, int offset, int len, ByteBuffer dst)
/* 116:    */     throws SSLException
/* 117:    */   {
/* 118:134 */     return verifyProtocolSelection(super.wrap(srcs, offset, len, dst));
/* 119:    */   }
/* 120:    */   
/* 121:    */   public SSLEngineResult unwrap(ByteBuffer src, ByteBuffer dst)
/* 122:    */     throws SSLException
/* 123:    */   {
/* 124:139 */     return verifyProtocolSelection(super.unwrap(src, dst));
/* 125:    */   }
/* 126:    */   
/* 127:    */   public SSLEngineResult unwrap(ByteBuffer src, ByteBuffer[] dsts)
/* 128:    */     throws SSLException
/* 129:    */   {
/* 130:144 */     return verifyProtocolSelection(super.unwrap(src, dsts));
/* 131:    */   }
/* 132:    */   
/* 133:    */   public SSLEngineResult unwrap(ByteBuffer src, ByteBuffer[] dst, int offset, int len)
/* 134:    */     throws SSLException
/* 135:    */   {
/* 136:149 */     return verifyProtocolSelection(super.unwrap(src, dst, offset, len));
/* 137:    */   }
/* 138:    */   
/* 139:    */   void setNegotiatedApplicationProtocol(String applicationProtocol) {}
/* 140:    */   
/* 141:    */   public String getNegotiatedApplicationProtocol()
/* 142:    */   {
/* 143:159 */     String protocol = getApplicationProtocol();
/* 144:160 */     if (protocol != null) {
/* 145:161 */       return protocol.isEmpty() ? null : protocol;
/* 146:    */     }
/* 147:163 */     return protocol;
/* 148:    */   }
/* 149:    */   
/* 150:    */   public String getApplicationProtocol()
/* 151:    */   {
/* 152:169 */     return Java9SslUtils.getApplicationProtocol(getWrappedEngine());
/* 153:    */   }
/* 154:    */   
/* 155:    */   public String getHandshakeApplicationProtocol()
/* 156:    */   {
/* 157:173 */     return Java9SslUtils.getHandshakeApplicationProtocol(getWrappedEngine());
/* 158:    */   }
/* 159:    */   
/* 160:    */   public void setHandshakeApplicationProtocolSelector(BiFunction<SSLEngine, List<String>, String> selector)
/* 161:    */   {
/* 162:177 */     Java9SslUtils.setHandshakeApplicationProtocolSelector(getWrappedEngine(), selector);
/* 163:    */   }
/* 164:    */   
/* 165:    */   public BiFunction<SSLEngine, List<String>, String> getHandshakeApplicationProtocolSelector()
/* 166:    */   {
/* 167:181 */     return Java9SslUtils.getHandshakeApplicationProtocolSelector(getWrappedEngine());
/* 168:    */   }
/* 169:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.Java9SslEngine
 * JD-Core Version:    0.7.0.1
 */