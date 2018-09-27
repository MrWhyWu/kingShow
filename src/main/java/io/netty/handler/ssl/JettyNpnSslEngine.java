/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.ObjectUtil;
/*   4:    */ import io.netty.util.internal.PlatformDependent;
/*   5:    */ import java.util.LinkedHashSet;
/*   6:    */ import java.util.List;
/*   7:    */ import javax.net.ssl.SSLEngine;
/*   8:    */ import javax.net.ssl.SSLException;
/*   9:    */ import org.eclipse.jetty.npn.NextProtoNego;
/*  10:    */ import org.eclipse.jetty.npn.NextProtoNego.ClientProvider;
/*  11:    */ import org.eclipse.jetty.npn.NextProtoNego.ServerProvider;
/*  12:    */ 
/*  13:    */ final class JettyNpnSslEngine
/*  14:    */   extends JdkSslEngine
/*  15:    */ {
/*  16:    */   private static boolean available;
/*  17:    */   
/*  18:    */   static boolean isAvailable()
/*  19:    */   {
/*  20: 38 */     updateAvailability();
/*  21: 39 */     return available;
/*  22:    */   }
/*  23:    */   
/*  24:    */   private static void updateAvailability()
/*  25:    */   {
/*  26: 43 */     if (available) {
/*  27: 44 */       return;
/*  28:    */     }
/*  29:    */     try
/*  30:    */     {
/*  31: 48 */       Class.forName("sun.security.ssl.NextProtoNegoExtension", true, null);
/*  32: 49 */       available = true;
/*  33:    */     }
/*  34:    */     catch (Exception localException) {}
/*  35:    */   }
/*  36:    */   
/*  37:    */   JettyNpnSslEngine(SSLEngine engine, final JdkApplicationProtocolNegotiator applicationNegotiator, boolean server)
/*  38:    */   {
/*  39: 56 */     super(engine);
/*  40: 57 */     ObjectUtil.checkNotNull(applicationNegotiator, "applicationNegotiator");
/*  41: 59 */     if (server)
/*  42:    */     {
/*  43: 60 */       final JdkApplicationProtocolNegotiator.ProtocolSelectionListener protocolListener = (JdkApplicationProtocolNegotiator.ProtocolSelectionListener)ObjectUtil.checkNotNull(applicationNegotiator
/*  44: 61 */         .protocolListenerFactory().newListener(this, applicationNegotiator.protocols()), "protocolListener");
/*  45:    */       
/*  46: 63 */       NextProtoNego.put(engine, new NextProtoNego.ServerProvider()
/*  47:    */       {
/*  48:    */         public void unsupported()
/*  49:    */         {
/*  50: 66 */           protocolListener.unsupported();
/*  51:    */         }
/*  52:    */         
/*  53:    */         public List<String> protocols()
/*  54:    */         {
/*  55: 71 */           return applicationNegotiator.protocols();
/*  56:    */         }
/*  57:    */         
/*  58:    */         public void protocolSelected(String protocol)
/*  59:    */         {
/*  60:    */           try
/*  61:    */           {
/*  62: 77 */             protocolListener.selected(protocol);
/*  63:    */           }
/*  64:    */           catch (Throwable t)
/*  65:    */           {
/*  66: 79 */             PlatformDependent.throwException(t);
/*  67:    */           }
/*  68:    */         }
/*  69:    */       });
/*  70:    */     }
/*  71:    */     else
/*  72:    */     {
/*  73: 84 */       final JdkApplicationProtocolNegotiator.ProtocolSelector protocolSelector = (JdkApplicationProtocolNegotiator.ProtocolSelector)ObjectUtil.checkNotNull(applicationNegotiator.protocolSelectorFactory()
/*  74: 85 */         .newSelector(this, new LinkedHashSet(applicationNegotiator.protocols())), "protocolSelector");
/*  75:    */       
/*  76: 87 */       NextProtoNego.put(engine, new NextProtoNego.ClientProvider()
/*  77:    */       {
/*  78:    */         public boolean supports()
/*  79:    */         {
/*  80: 90 */           return true;
/*  81:    */         }
/*  82:    */         
/*  83:    */         public void unsupported()
/*  84:    */         {
/*  85: 95 */           protocolSelector.unsupported();
/*  86:    */         }
/*  87:    */         
/*  88:    */         public String selectProtocol(List<String> protocols)
/*  89:    */         {
/*  90:    */           try
/*  91:    */           {
/*  92:101 */             return protocolSelector.select(protocols);
/*  93:    */           }
/*  94:    */           catch (Throwable t)
/*  95:    */           {
/*  96:103 */             PlatformDependent.throwException(t);
/*  97:    */           }
/*  98:104 */           return null;
/*  99:    */         }
/* 100:    */       });
/* 101:    */     }
/* 102:    */   }
/* 103:    */   
/* 104:    */   public void closeInbound()
/* 105:    */     throws SSLException
/* 106:    */   {
/* 107:113 */     NextProtoNego.remove(getWrappedEngine());
/* 108:114 */     super.closeInbound();
/* 109:    */   }
/* 110:    */   
/* 111:    */   public void closeOutbound()
/* 112:    */   {
/* 113:119 */     NextProtoNego.remove(getWrappedEngine());
/* 114:120 */     super.closeOutbound();
/* 115:    */   }
/* 116:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.JettyNpnSslEngine
 * JD-Core Version:    0.7.0.1
 */