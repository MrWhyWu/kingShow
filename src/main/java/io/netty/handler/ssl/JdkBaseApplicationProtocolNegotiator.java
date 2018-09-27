/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.ObjectUtil;
/*   4:    */ import java.util.Collections;
/*   5:    */ import java.util.List;
/*   6:    */ import java.util.Set;
/*   7:    */ import javax.net.ssl.SSLEngine;
/*   8:    */ import javax.net.ssl.SSLHandshakeException;
/*   9:    */ 
/*  10:    */ class JdkBaseApplicationProtocolNegotiator
/*  11:    */   implements JdkApplicationProtocolNegotiator
/*  12:    */ {
/*  13:    */   private final List<String> protocols;
/*  14:    */   private final JdkApplicationProtocolNegotiator.ProtocolSelectorFactory selectorFactory;
/*  15:    */   private final JdkApplicationProtocolNegotiator.ProtocolSelectionListenerFactory listenerFactory;
/*  16:    */   private final JdkApplicationProtocolNegotiator.SslEngineWrapperFactory wrapperFactory;
/*  17:    */   
/*  18:    */   JdkBaseApplicationProtocolNegotiator(JdkApplicationProtocolNegotiator.SslEngineWrapperFactory wrapperFactory, JdkApplicationProtocolNegotiator.ProtocolSelectorFactory selectorFactory, JdkApplicationProtocolNegotiator.ProtocolSelectionListenerFactory listenerFactory, Iterable<String> protocols)
/*  19:    */   {
/*  20: 47 */     this(wrapperFactory, selectorFactory, listenerFactory, ApplicationProtocolUtil.toList(protocols));
/*  21:    */   }
/*  22:    */   
/*  23:    */   JdkBaseApplicationProtocolNegotiator(JdkApplicationProtocolNegotiator.SslEngineWrapperFactory wrapperFactory, JdkApplicationProtocolNegotiator.ProtocolSelectorFactory selectorFactory, JdkApplicationProtocolNegotiator.ProtocolSelectionListenerFactory listenerFactory, String... protocols)
/*  24:    */   {
/*  25: 60 */     this(wrapperFactory, selectorFactory, listenerFactory, ApplicationProtocolUtil.toList(protocols));
/*  26:    */   }
/*  27:    */   
/*  28:    */   private JdkBaseApplicationProtocolNegotiator(JdkApplicationProtocolNegotiator.SslEngineWrapperFactory wrapperFactory, JdkApplicationProtocolNegotiator.ProtocolSelectorFactory selectorFactory, JdkApplicationProtocolNegotiator.ProtocolSelectionListenerFactory listenerFactory, List<String> protocols)
/*  29:    */   {
/*  30: 73 */     this.wrapperFactory = ((JdkApplicationProtocolNegotiator.SslEngineWrapperFactory)ObjectUtil.checkNotNull(wrapperFactory, "wrapperFactory"));
/*  31: 74 */     this.selectorFactory = ((JdkApplicationProtocolNegotiator.ProtocolSelectorFactory)ObjectUtil.checkNotNull(selectorFactory, "selectorFactory"));
/*  32: 75 */     this.listenerFactory = ((JdkApplicationProtocolNegotiator.ProtocolSelectionListenerFactory)ObjectUtil.checkNotNull(listenerFactory, "listenerFactory"));
/*  33: 76 */     this.protocols = Collections.unmodifiableList((List)ObjectUtil.checkNotNull(protocols, "protocols"));
/*  34:    */   }
/*  35:    */   
/*  36:    */   public List<String> protocols()
/*  37:    */   {
/*  38: 81 */     return this.protocols;
/*  39:    */   }
/*  40:    */   
/*  41:    */   public JdkApplicationProtocolNegotiator.ProtocolSelectorFactory protocolSelectorFactory()
/*  42:    */   {
/*  43: 86 */     return this.selectorFactory;
/*  44:    */   }
/*  45:    */   
/*  46:    */   public JdkApplicationProtocolNegotiator.ProtocolSelectionListenerFactory protocolListenerFactory()
/*  47:    */   {
/*  48: 91 */     return this.listenerFactory;
/*  49:    */   }
/*  50:    */   
/*  51:    */   public JdkApplicationProtocolNegotiator.SslEngineWrapperFactory wrapperFactory()
/*  52:    */   {
/*  53: 96 */     return this.wrapperFactory;
/*  54:    */   }
/*  55:    */   
/*  56: 99 */   static final JdkApplicationProtocolNegotiator.ProtocolSelectorFactory FAIL_SELECTOR_FACTORY = new JdkApplicationProtocolNegotiator.ProtocolSelectorFactory()
/*  57:    */   {
/*  58:    */     public JdkApplicationProtocolNegotiator.ProtocolSelector newSelector(SSLEngine engine, Set<String> supportedProtocols)
/*  59:    */     {
/*  60:102 */       return new JdkBaseApplicationProtocolNegotiator.FailProtocolSelector((JdkSslEngine)engine, supportedProtocols);
/*  61:    */     }
/*  62:    */   };
/*  63:106 */   static final JdkApplicationProtocolNegotiator.ProtocolSelectorFactory NO_FAIL_SELECTOR_FACTORY = new JdkApplicationProtocolNegotiator.ProtocolSelectorFactory()
/*  64:    */   {
/*  65:    */     public JdkApplicationProtocolNegotiator.ProtocolSelector newSelector(SSLEngine engine, Set<String> supportedProtocols)
/*  66:    */     {
/*  67:109 */       return new JdkBaseApplicationProtocolNegotiator.NoFailProtocolSelector((JdkSslEngine)engine, supportedProtocols);
/*  68:    */     }
/*  69:    */   };
/*  70:113 */   static final JdkApplicationProtocolNegotiator.ProtocolSelectionListenerFactory FAIL_SELECTION_LISTENER_FACTORY = new JdkApplicationProtocolNegotiator.ProtocolSelectionListenerFactory()
/*  71:    */   {
/*  72:    */     public JdkApplicationProtocolNegotiator.ProtocolSelectionListener newListener(SSLEngine engine, List<String> supportedProtocols)
/*  73:    */     {
/*  74:117 */       return new JdkBaseApplicationProtocolNegotiator.FailProtocolSelectionListener((JdkSslEngine)engine, supportedProtocols);
/*  75:    */     }
/*  76:    */   };
/*  77:121 */   static final JdkApplicationProtocolNegotiator.ProtocolSelectionListenerFactory NO_FAIL_SELECTION_LISTENER_FACTORY = new JdkApplicationProtocolNegotiator.ProtocolSelectionListenerFactory()
/*  78:    */   {
/*  79:    */     public JdkApplicationProtocolNegotiator.ProtocolSelectionListener newListener(SSLEngine engine, List<String> supportedProtocols)
/*  80:    */     {
/*  81:125 */       return new JdkBaseApplicationProtocolNegotiator.NoFailProtocolSelectionListener((JdkSslEngine)engine, supportedProtocols);
/*  82:    */     }
/*  83:    */   };
/*  84:    */   
/*  85:    */   static class NoFailProtocolSelector
/*  86:    */     implements JdkApplicationProtocolNegotiator.ProtocolSelector
/*  87:    */   {
/*  88:    */     private final JdkSslEngine engineWrapper;
/*  89:    */     private final Set<String> supportedProtocols;
/*  90:    */     
/*  91:    */     NoFailProtocolSelector(JdkSslEngine engineWrapper, Set<String> supportedProtocols)
/*  92:    */     {
/*  93:134 */       this.engineWrapper = engineWrapper;
/*  94:135 */       this.supportedProtocols = supportedProtocols;
/*  95:    */     }
/*  96:    */     
/*  97:    */     public void unsupported()
/*  98:    */     {
/*  99:140 */       this.engineWrapper.setNegotiatedApplicationProtocol(null);
/* 100:    */     }
/* 101:    */     
/* 102:    */     public String select(List<String> protocols)
/* 103:    */       throws Exception
/* 104:    */     {
/* 105:145 */       for (String p : this.supportedProtocols) {
/* 106:146 */         if (protocols.contains(p))
/* 107:    */         {
/* 108:147 */           this.engineWrapper.setNegotiatedApplicationProtocol(p);
/* 109:148 */           return p;
/* 110:    */         }
/* 111:    */       }
/* 112:151 */       return noSelectMatchFound();
/* 113:    */     }
/* 114:    */     
/* 115:    */     public String noSelectMatchFound()
/* 116:    */       throws Exception
/* 117:    */     {
/* 118:155 */       this.engineWrapper.setNegotiatedApplicationProtocol(null);
/* 119:156 */       return null;
/* 120:    */     }
/* 121:    */   }
/* 122:    */   
/* 123:    */   private static final class FailProtocolSelector
/* 124:    */     extends JdkBaseApplicationProtocolNegotiator.NoFailProtocolSelector
/* 125:    */   {
/* 126:    */     FailProtocolSelector(JdkSslEngine engineWrapper, Set<String> supportedProtocols)
/* 127:    */     {
/* 128:162 */       super(supportedProtocols);
/* 129:    */     }
/* 130:    */     
/* 131:    */     public String noSelectMatchFound()
/* 132:    */       throws Exception
/* 133:    */     {
/* 134:167 */       throw new SSLHandshakeException("Selected protocol is not supported");
/* 135:    */     }
/* 136:    */   }
/* 137:    */   
/* 138:    */   private static class NoFailProtocolSelectionListener
/* 139:    */     implements JdkApplicationProtocolNegotiator.ProtocolSelectionListener
/* 140:    */   {
/* 141:    */     private final JdkSslEngine engineWrapper;
/* 142:    */     private final List<String> supportedProtocols;
/* 143:    */     
/* 144:    */     NoFailProtocolSelectionListener(JdkSslEngine engineWrapper, List<String> supportedProtocols)
/* 145:    */     {
/* 146:176 */       this.engineWrapper = engineWrapper;
/* 147:177 */       this.supportedProtocols = supportedProtocols;
/* 148:    */     }
/* 149:    */     
/* 150:    */     public void unsupported()
/* 151:    */     {
/* 152:182 */       this.engineWrapper.setNegotiatedApplicationProtocol(null);
/* 153:    */     }
/* 154:    */     
/* 155:    */     public void selected(String protocol)
/* 156:    */       throws Exception
/* 157:    */     {
/* 158:187 */       if (this.supportedProtocols.contains(protocol)) {
/* 159:188 */         this.engineWrapper.setNegotiatedApplicationProtocol(protocol);
/* 160:    */       } else {
/* 161:190 */         noSelectedMatchFound(protocol);
/* 162:    */       }
/* 163:    */     }
/* 164:    */     
/* 165:    */     protected void noSelectedMatchFound(String protocol)
/* 166:    */       throws Exception
/* 167:    */     {}
/* 168:    */   }
/* 169:    */   
/* 170:    */   private static final class FailProtocolSelectionListener
/* 171:    */     extends JdkBaseApplicationProtocolNegotiator.NoFailProtocolSelectionListener
/* 172:    */   {
/* 173:    */     FailProtocolSelectionListener(JdkSslEngine engineWrapper, List<String> supportedProtocols)
/* 174:    */     {
/* 175:201 */       super(supportedProtocols);
/* 176:    */     }
/* 177:    */     
/* 178:    */     protected void noSelectedMatchFound(String protocol)
/* 179:    */       throws Exception
/* 180:    */     {
/* 181:206 */       throw new SSLHandshakeException("No compatible protocols found");
/* 182:    */     }
/* 183:    */   }
/* 184:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.JdkBaseApplicationProtocolNegotiator
 * JD-Core Version:    0.7.0.1
 */