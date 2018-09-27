/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.EmptyArrays;
/*   4:    */ import io.netty.util.internal.PlatformDependent;
/*   5:    */ import io.netty.util.internal.logging.InternalLogger;
/*   6:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   7:    */ import java.lang.reflect.Method;
/*   8:    */ import java.security.AccessController;
/*   9:    */ import java.security.PrivilegedExceptionAction;
/*  10:    */ import java.util.List;
/*  11:    */ import java.util.function.BiFunction;
/*  12:    */ import javax.net.ssl.SSLContext;
/*  13:    */ import javax.net.ssl.SSLEngine;
/*  14:    */ import javax.net.ssl.SSLParameters;
/*  15:    */ 
/*  16:    */ final class Java9SslUtils
/*  17:    */ {
/*  18: 34 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(Java9SslUtils.class);
/*  19:    */   private static final Method SET_APPLICATION_PROTOCOLS;
/*  20:    */   private static final Method GET_APPLICATION_PROTOCOL;
/*  21:    */   private static final Method GET_HANDSHAKE_APPLICATION_PROTOCOL;
/*  22:    */   private static final Method SET_HANDSHAKE_APPLICATION_PROTOCOL_SELECTOR;
/*  23:    */   private static final Method GET_HANDSHAKE_APPLICATION_PROTOCOL_SELECTOR;
/*  24:    */   
/*  25:    */   static
/*  26:    */   {
/*  27: 42 */     Method getHandshakeApplicationProtocol = null;
/*  28: 43 */     Method getApplicationProtocol = null;
/*  29: 44 */     Method setApplicationProtocols = null;
/*  30: 45 */     Method setHandshakeApplicationProtocolSelector = null;
/*  31: 46 */     Method getHandshakeApplicationProtocolSelector = null;
/*  32:    */     try
/*  33:    */     {
/*  34: 49 */       SSLContext context = SSLContext.getInstance("TLS");
/*  35: 50 */       context.init(null, null, null);
/*  36: 51 */       SSLEngine engine = context.createSSLEngine();
/*  37: 52 */       getHandshakeApplicationProtocol = (Method)AccessController.doPrivileged(new PrivilegedExceptionAction()
/*  38:    */       {
/*  39:    */         public Method run()
/*  40:    */           throws Exception
/*  41:    */         {
/*  42: 55 */           return SSLEngine.class.getMethod("getHandshakeApplicationProtocol", new Class[0]);
/*  43:    */         }
/*  44: 57 */       });
/*  45: 58 */       getHandshakeApplicationProtocol.invoke(engine, new Object[0]);
/*  46: 59 */       getApplicationProtocol = (Method)AccessController.doPrivileged(new PrivilegedExceptionAction()
/*  47:    */       {
/*  48:    */         public Method run()
/*  49:    */           throws Exception
/*  50:    */         {
/*  51: 62 */           return SSLEngine.class.getMethod("getApplicationProtocol", new Class[0]);
/*  52:    */         }
/*  53: 64 */       });
/*  54: 65 */       getApplicationProtocol.invoke(engine, new Object[0]);
/*  55:    */       
/*  56: 67 */       setApplicationProtocols = (Method)AccessController.doPrivileged(new PrivilegedExceptionAction()
/*  57:    */       {
/*  58:    */         public Method run()
/*  59:    */           throws Exception
/*  60:    */         {
/*  61: 70 */           return SSLParameters.class.getMethod("setApplicationProtocols", new Class[] { [Ljava.lang.String.class });
/*  62:    */         }
/*  63: 72 */       });
/*  64: 73 */       setApplicationProtocols.invoke(engine.getSSLParameters(), new Object[] { EmptyArrays.EMPTY_STRINGS });
/*  65:    */       
/*  66:    */ 
/*  67: 76 */       setHandshakeApplicationProtocolSelector = (Method)AccessController.doPrivileged(new PrivilegedExceptionAction()
/*  68:    */       {
/*  69:    */         public Method run()
/*  70:    */           throws Exception
/*  71:    */         {
/*  72: 79 */           return SSLEngine.class.getMethod("setHandshakeApplicationProtocolSelector", new Class[] { BiFunction.class });
/*  73:    */         }
/*  74: 81 */       });
/*  75: 82 */       setHandshakeApplicationProtocolSelector.invoke(engine, new Object[] { new BiFunction()
/*  76:    */       {
/*  77:    */         public String apply(SSLEngine sslEngine, List<String> strings)
/*  78:    */         {
/*  79: 85 */           return null;
/*  80:    */         }
/*  81: 89 */       } });
/*  82: 90 */       getHandshakeApplicationProtocolSelector = (Method)AccessController.doPrivileged(new PrivilegedExceptionAction()
/*  83:    */       {
/*  84:    */         public Method run()
/*  85:    */           throws Exception
/*  86:    */         {
/*  87: 93 */           return SSLEngine.class.getMethod("getHandshakeApplicationProtocolSelector", new Class[0]);
/*  88:    */         }
/*  89: 95 */       });
/*  90: 96 */       getHandshakeApplicationProtocolSelector.invoke(engine, new Object[0]);
/*  91:    */     }
/*  92:    */     catch (Throwable t)
/*  93:    */     {
/*  94: 98 */       logger.error("Unable to initialize Java9SslUtils, but the detected javaVersion was: {}", 
/*  95: 99 */         Integer.valueOf(PlatformDependent.javaVersion()), t);
/*  96:100 */       getHandshakeApplicationProtocol = null;
/*  97:101 */       getApplicationProtocol = null;
/*  98:102 */       setApplicationProtocols = null;
/*  99:103 */       setHandshakeApplicationProtocolSelector = null;
/* 100:104 */       getHandshakeApplicationProtocolSelector = null;
/* 101:    */     }
/* 102:106 */     GET_HANDSHAKE_APPLICATION_PROTOCOL = getHandshakeApplicationProtocol;
/* 103:107 */     GET_APPLICATION_PROTOCOL = getApplicationProtocol;
/* 104:108 */     SET_APPLICATION_PROTOCOLS = setApplicationProtocols;
/* 105:109 */     SET_HANDSHAKE_APPLICATION_PROTOCOL_SELECTOR = setHandshakeApplicationProtocolSelector;
/* 106:110 */     GET_HANDSHAKE_APPLICATION_PROTOCOL_SELECTOR = getHandshakeApplicationProtocolSelector;
/* 107:    */   }
/* 108:    */   
/* 109:    */   static boolean supportsAlpn()
/* 110:    */   {
/* 111:117 */     return GET_APPLICATION_PROTOCOL != null;
/* 112:    */   }
/* 113:    */   
/* 114:    */   static String getApplicationProtocol(SSLEngine sslEngine)
/* 115:    */   {
/* 116:    */     try
/* 117:    */     {
/* 118:122 */       return (String)GET_APPLICATION_PROTOCOL.invoke(sslEngine, new Object[0]);
/* 119:    */     }
/* 120:    */     catch (UnsupportedOperationException ex)
/* 121:    */     {
/* 122:124 */       throw ex;
/* 123:    */     }
/* 124:    */     catch (Exception ex)
/* 125:    */     {
/* 126:126 */       throw new IllegalStateException(ex);
/* 127:    */     }
/* 128:    */   }
/* 129:    */   
/* 130:    */   static String getHandshakeApplicationProtocol(SSLEngine sslEngine)
/* 131:    */   {
/* 132:    */     try
/* 133:    */     {
/* 134:132 */       return (String)GET_HANDSHAKE_APPLICATION_PROTOCOL.invoke(sslEngine, new Object[0]);
/* 135:    */     }
/* 136:    */     catch (UnsupportedOperationException ex)
/* 137:    */     {
/* 138:134 */       throw ex;
/* 139:    */     }
/* 140:    */     catch (Exception ex)
/* 141:    */     {
/* 142:136 */       throw new IllegalStateException(ex);
/* 143:    */     }
/* 144:    */   }
/* 145:    */   
/* 146:    */   static void setApplicationProtocols(SSLEngine engine, List<String> supportedProtocols)
/* 147:    */   {
/* 148:141 */     SSLParameters parameters = engine.getSSLParameters();
/* 149:    */     
/* 150:143 */     String[] protocolArray = (String[])supportedProtocols.toArray(EmptyArrays.EMPTY_STRINGS);
/* 151:    */     try
/* 152:    */     {
/* 153:145 */       SET_APPLICATION_PROTOCOLS.invoke(parameters, new Object[] { protocolArray });
/* 154:    */     }
/* 155:    */     catch (UnsupportedOperationException ex)
/* 156:    */     {
/* 157:147 */       throw ex;
/* 158:    */     }
/* 159:    */     catch (Exception ex)
/* 160:    */     {
/* 161:149 */       throw new IllegalStateException(ex);
/* 162:    */     }
/* 163:151 */     engine.setSSLParameters(parameters);
/* 164:    */   }
/* 165:    */   
/* 166:    */   static void setHandshakeApplicationProtocolSelector(SSLEngine engine, BiFunction<SSLEngine, List<String>, String> selector)
/* 167:    */   {
/* 168:    */     try
/* 169:    */     {
/* 170:157 */       SET_HANDSHAKE_APPLICATION_PROTOCOL_SELECTOR.invoke(engine, new Object[] { selector });
/* 171:    */     }
/* 172:    */     catch (UnsupportedOperationException ex)
/* 173:    */     {
/* 174:159 */       throw ex;
/* 175:    */     }
/* 176:    */     catch (Exception ex)
/* 177:    */     {
/* 178:161 */       throw new IllegalStateException(ex);
/* 179:    */     }
/* 180:    */   }
/* 181:    */   
/* 182:    */   static BiFunction<SSLEngine, List<String>, String> getHandshakeApplicationProtocolSelector(SSLEngine engine)
/* 183:    */   {
/* 184:    */     try
/* 185:    */     {
/* 186:167 */       return 
/* 187:168 */         (BiFunction)GET_HANDSHAKE_APPLICATION_PROTOCOL_SELECTOR.invoke(engine, new Object[0]);
/* 188:    */     }
/* 189:    */     catch (UnsupportedOperationException ex)
/* 190:    */     {
/* 191:170 */       throw ex;
/* 192:    */     }
/* 193:    */     catch (Exception ex)
/* 194:    */     {
/* 195:172 */       throw new IllegalStateException(ex);
/* 196:    */     }
/* 197:    */   }
/* 198:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.Java9SslUtils
 * JD-Core Version:    0.7.0.1
 */