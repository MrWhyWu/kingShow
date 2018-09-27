/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.ObjectUtil;
/*   4:    */ import io.netty.util.internal.PlatformDependent;
/*   5:    */ import java.util.LinkedHashSet;
/*   6:    */ import java.util.List;
/*   7:    */ import javax.net.ssl.SSLEngine;
/*   8:    */ import javax.net.ssl.SSLException;
/*   9:    */ import org.eclipse.jetty.alpn.ALPN;
/*  10:    */ import org.eclipse.jetty.alpn.ALPN.ClientProvider;
/*  11:    */ import org.eclipse.jetty.alpn.ALPN.ServerProvider;
/*  12:    */ 
/*  13:    */ abstract class JettyAlpnSslEngine
/*  14:    */   extends JdkSslEngine
/*  15:    */ {
/*  16: 34 */   private static final boolean available = ;
/*  17:    */   
/*  18:    */   static boolean isAvailable()
/*  19:    */   {
/*  20: 37 */     return available;
/*  21:    */   }
/*  22:    */   
/*  23:    */   private static boolean initAvailable()
/*  24:    */   {
/*  25: 41 */     if (PlatformDependent.javaVersion() <= 8) {
/*  26:    */       try
/*  27:    */       {
/*  28: 44 */         Class.forName("sun.security.ssl.ALPNExtension", true, null);
/*  29: 45 */         return true;
/*  30:    */       }
/*  31:    */       catch (Throwable localThrowable) {}
/*  32:    */     }
/*  33: 50 */     return false;
/*  34:    */   }
/*  35:    */   
/*  36:    */   static JettyAlpnSslEngine newClientEngine(SSLEngine engine, JdkApplicationProtocolNegotiator applicationNegotiator)
/*  37:    */   {
/*  38: 55 */     return new ClientEngine(engine, applicationNegotiator);
/*  39:    */   }
/*  40:    */   
/*  41:    */   static JettyAlpnSslEngine newServerEngine(SSLEngine engine, JdkApplicationProtocolNegotiator applicationNegotiator)
/*  42:    */   {
/*  43: 60 */     return new ServerEngine(engine, applicationNegotiator);
/*  44:    */   }
/*  45:    */   
/*  46:    */   private JettyAlpnSslEngine(SSLEngine engine)
/*  47:    */   {
/*  48: 64 */     super(engine);
/*  49:    */   }
/*  50:    */   
/*  51:    */   private static final class ClientEngine
/*  52:    */     extends JettyAlpnSslEngine
/*  53:    */   {
/*  54:    */     ClientEngine(SSLEngine engine, final JdkApplicationProtocolNegotiator applicationNegotiator)
/*  55:    */     {
/*  56: 69 */       super(null);
/*  57: 70 */       ObjectUtil.checkNotNull(applicationNegotiator, "applicationNegotiator");
/*  58: 71 */       final JdkApplicationProtocolNegotiator.ProtocolSelectionListener protocolListener = (JdkApplicationProtocolNegotiator.ProtocolSelectionListener)ObjectUtil.checkNotNull(applicationNegotiator
/*  59: 72 */         .protocolListenerFactory().newListener(this, applicationNegotiator.protocols()), "protocolListener");
/*  60:    */       
/*  61: 74 */       ALPN.put(engine, new ALPN.ClientProvider()
/*  62:    */       {
/*  63:    */         public List<String> protocols()
/*  64:    */         {
/*  65: 77 */           return applicationNegotiator.protocols();
/*  66:    */         }
/*  67:    */         
/*  68:    */         public void selected(String protocol)
/*  69:    */           throws SSLException
/*  70:    */         {
/*  71:    */           try
/*  72:    */           {
/*  73: 83 */             protocolListener.selected(protocol);
/*  74:    */           }
/*  75:    */           catch (Throwable t)
/*  76:    */           {
/*  77: 85 */             throw SslUtils.toSSLHandshakeException(t);
/*  78:    */           }
/*  79:    */         }
/*  80:    */         
/*  81:    */         public void unsupported()
/*  82:    */         {
/*  83: 91 */           protocolListener.unsupported();
/*  84:    */         }
/*  85:    */       });
/*  86:    */     }
/*  87:    */     
/*  88:    */     public void closeInbound()
/*  89:    */       throws SSLException
/*  90:    */     {
/*  91:    */       try
/*  92:    */       {
/*  93: 99 */         ALPN.remove(getWrappedEngine());
/*  94:    */         
/*  95:101 */         super.closeInbound();
/*  96:    */       }
/*  97:    */       finally
/*  98:    */       {
/*  99:101 */         super.closeInbound();
/* 100:    */       }
/* 101:    */     }
/* 102:    */     
/* 103:    */     public void closeOutbound()
/* 104:    */     {
/* 105:    */       try
/* 106:    */       {
/* 107:108 */         ALPN.remove(getWrappedEngine());
/* 108:    */         
/* 109:110 */         super.closeOutbound();
/* 110:    */       }
/* 111:    */       finally
/* 112:    */       {
/* 113:110 */         super.closeOutbound();
/* 114:    */       }
/* 115:    */     }
/* 116:    */   }
/* 117:    */   
/* 118:    */   private static final class ServerEngine
/* 119:    */     extends JettyAlpnSslEngine
/* 120:    */   {
/* 121:    */     ServerEngine(SSLEngine engine, JdkApplicationProtocolNegotiator applicationNegotiator)
/* 122:    */     {
/* 123:117 */       super(null);
/* 124:118 */       ObjectUtil.checkNotNull(applicationNegotiator, "applicationNegotiator");
/* 125:119 */       final JdkApplicationProtocolNegotiator.ProtocolSelector protocolSelector = (JdkApplicationProtocolNegotiator.ProtocolSelector)ObjectUtil.checkNotNull(applicationNegotiator.protocolSelectorFactory()
/* 126:120 */         .newSelector(this, new LinkedHashSet(applicationNegotiator.protocols())), "protocolSelector");
/* 127:    */       
/* 128:122 */       ALPN.put(engine, new ALPN.ServerProvider()
/* 129:    */       {
/* 130:    */         public String select(List<String> protocols)
/* 131:    */           throws SSLException
/* 132:    */         {
/* 133:    */           try
/* 134:    */           {
/* 135:126 */             return protocolSelector.select(protocols);
/* 136:    */           }
/* 137:    */           catch (Throwable t)
/* 138:    */           {
/* 139:128 */             throw SslUtils.toSSLHandshakeException(t);
/* 140:    */           }
/* 141:    */         }
/* 142:    */         
/* 143:    */         public void unsupported()
/* 144:    */         {
/* 145:134 */           protocolSelector.unsupported();
/* 146:    */         }
/* 147:    */       });
/* 148:    */     }
/* 149:    */     
/* 150:    */     public void closeInbound()
/* 151:    */       throws SSLException
/* 152:    */     {
/* 153:    */       try
/* 154:    */       {
/* 155:142 */         ALPN.remove(getWrappedEngine());
/* 156:    */         
/* 157:144 */         super.closeInbound();
/* 158:    */       }
/* 159:    */       finally
/* 160:    */       {
/* 161:144 */         super.closeInbound();
/* 162:    */       }
/* 163:    */     }
/* 164:    */     
/* 165:    */     public void closeOutbound()
/* 166:    */     {
/* 167:    */       try
/* 168:    */       {
/* 169:151 */         ALPN.remove(getWrappedEngine());
/* 170:    */         
/* 171:153 */         super.closeOutbound();
/* 172:    */       }
/* 173:    */       finally
/* 174:    */       {
/* 175:153 */         super.closeOutbound();
/* 176:    */       }
/* 177:    */     }
/* 178:    */   }
/* 179:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.JettyAlpnSslEngine
 * JD-Core Version:    0.7.0.1
 */