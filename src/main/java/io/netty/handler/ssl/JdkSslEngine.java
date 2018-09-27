/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import java.nio.ByteBuffer;
/*   4:    */ import javax.net.ssl.SSLEngine;
/*   5:    */ import javax.net.ssl.SSLEngineResult;
/*   6:    */ import javax.net.ssl.SSLEngineResult.HandshakeStatus;
/*   7:    */ import javax.net.ssl.SSLException;
/*   8:    */ import javax.net.ssl.SSLParameters;
/*   9:    */ import javax.net.ssl.SSLSession;
/*  10:    */ 
/*  11:    */ class JdkSslEngine
/*  12:    */   extends SSLEngine
/*  13:    */   implements ApplicationProtocolAccessor
/*  14:    */ {
/*  15:    */   private final SSLEngine engine;
/*  16:    */   private volatile String applicationProtocol;
/*  17:    */   
/*  18:    */   JdkSslEngine(SSLEngine engine)
/*  19:    */   {
/*  20: 32 */     this.engine = engine;
/*  21:    */   }
/*  22:    */   
/*  23:    */   public String getNegotiatedApplicationProtocol()
/*  24:    */   {
/*  25: 37 */     return this.applicationProtocol;
/*  26:    */   }
/*  27:    */   
/*  28:    */   void setNegotiatedApplicationProtocol(String applicationProtocol)
/*  29:    */   {
/*  30: 41 */     this.applicationProtocol = applicationProtocol;
/*  31:    */   }
/*  32:    */   
/*  33:    */   public SSLSession getSession()
/*  34:    */   {
/*  35: 46 */     return this.engine.getSession();
/*  36:    */   }
/*  37:    */   
/*  38:    */   public SSLEngine getWrappedEngine()
/*  39:    */   {
/*  40: 50 */     return this.engine;
/*  41:    */   }
/*  42:    */   
/*  43:    */   public void closeInbound()
/*  44:    */     throws SSLException
/*  45:    */   {
/*  46: 55 */     this.engine.closeInbound();
/*  47:    */   }
/*  48:    */   
/*  49:    */   public void closeOutbound()
/*  50:    */   {
/*  51: 60 */     this.engine.closeOutbound();
/*  52:    */   }
/*  53:    */   
/*  54:    */   public String getPeerHost()
/*  55:    */   {
/*  56: 65 */     return this.engine.getPeerHost();
/*  57:    */   }
/*  58:    */   
/*  59:    */   public int getPeerPort()
/*  60:    */   {
/*  61: 70 */     return this.engine.getPeerPort();
/*  62:    */   }
/*  63:    */   
/*  64:    */   public SSLEngineResult wrap(ByteBuffer byteBuffer, ByteBuffer byteBuffer2)
/*  65:    */     throws SSLException
/*  66:    */   {
/*  67: 75 */     return this.engine.wrap(byteBuffer, byteBuffer2);
/*  68:    */   }
/*  69:    */   
/*  70:    */   public SSLEngineResult wrap(ByteBuffer[] byteBuffers, ByteBuffer byteBuffer)
/*  71:    */     throws SSLException
/*  72:    */   {
/*  73: 80 */     return this.engine.wrap(byteBuffers, byteBuffer);
/*  74:    */   }
/*  75:    */   
/*  76:    */   public SSLEngineResult wrap(ByteBuffer[] byteBuffers, int i, int i2, ByteBuffer byteBuffer)
/*  77:    */     throws SSLException
/*  78:    */   {
/*  79: 85 */     return this.engine.wrap(byteBuffers, i, i2, byteBuffer);
/*  80:    */   }
/*  81:    */   
/*  82:    */   public SSLEngineResult unwrap(ByteBuffer byteBuffer, ByteBuffer byteBuffer2)
/*  83:    */     throws SSLException
/*  84:    */   {
/*  85: 90 */     return this.engine.unwrap(byteBuffer, byteBuffer2);
/*  86:    */   }
/*  87:    */   
/*  88:    */   public SSLEngineResult unwrap(ByteBuffer byteBuffer, ByteBuffer[] byteBuffers)
/*  89:    */     throws SSLException
/*  90:    */   {
/*  91: 95 */     return this.engine.unwrap(byteBuffer, byteBuffers);
/*  92:    */   }
/*  93:    */   
/*  94:    */   public SSLEngineResult unwrap(ByteBuffer byteBuffer, ByteBuffer[] byteBuffers, int i, int i2)
/*  95:    */     throws SSLException
/*  96:    */   {
/*  97:100 */     return this.engine.unwrap(byteBuffer, byteBuffers, i, i2);
/*  98:    */   }
/*  99:    */   
/* 100:    */   public Runnable getDelegatedTask()
/* 101:    */   {
/* 102:105 */     return this.engine.getDelegatedTask();
/* 103:    */   }
/* 104:    */   
/* 105:    */   public boolean isInboundDone()
/* 106:    */   {
/* 107:110 */     return this.engine.isInboundDone();
/* 108:    */   }
/* 109:    */   
/* 110:    */   public boolean isOutboundDone()
/* 111:    */   {
/* 112:115 */     return this.engine.isOutboundDone();
/* 113:    */   }
/* 114:    */   
/* 115:    */   public String[] getSupportedCipherSuites()
/* 116:    */   {
/* 117:120 */     return this.engine.getSupportedCipherSuites();
/* 118:    */   }
/* 119:    */   
/* 120:    */   public String[] getEnabledCipherSuites()
/* 121:    */   {
/* 122:125 */     return this.engine.getEnabledCipherSuites();
/* 123:    */   }
/* 124:    */   
/* 125:    */   public void setEnabledCipherSuites(String[] strings)
/* 126:    */   {
/* 127:130 */     this.engine.setEnabledCipherSuites(strings);
/* 128:    */   }
/* 129:    */   
/* 130:    */   public String[] getSupportedProtocols()
/* 131:    */   {
/* 132:135 */     return this.engine.getSupportedProtocols();
/* 133:    */   }
/* 134:    */   
/* 135:    */   public String[] getEnabledProtocols()
/* 136:    */   {
/* 137:140 */     return this.engine.getEnabledProtocols();
/* 138:    */   }
/* 139:    */   
/* 140:    */   public void setEnabledProtocols(String[] strings)
/* 141:    */   {
/* 142:145 */     this.engine.setEnabledProtocols(strings);
/* 143:    */   }
/* 144:    */   
/* 145:    */   public SSLSession getHandshakeSession()
/* 146:    */   {
/* 147:150 */     return this.engine.getHandshakeSession();
/* 148:    */   }
/* 149:    */   
/* 150:    */   public void beginHandshake()
/* 151:    */     throws SSLException
/* 152:    */   {
/* 153:155 */     this.engine.beginHandshake();
/* 154:    */   }
/* 155:    */   
/* 156:    */   public SSLEngineResult.HandshakeStatus getHandshakeStatus()
/* 157:    */   {
/* 158:160 */     return this.engine.getHandshakeStatus();
/* 159:    */   }
/* 160:    */   
/* 161:    */   public void setUseClientMode(boolean b)
/* 162:    */   {
/* 163:165 */     this.engine.setUseClientMode(b);
/* 164:    */   }
/* 165:    */   
/* 166:    */   public boolean getUseClientMode()
/* 167:    */   {
/* 168:170 */     return this.engine.getUseClientMode();
/* 169:    */   }
/* 170:    */   
/* 171:    */   public void setNeedClientAuth(boolean b)
/* 172:    */   {
/* 173:175 */     this.engine.setNeedClientAuth(b);
/* 174:    */   }
/* 175:    */   
/* 176:    */   public boolean getNeedClientAuth()
/* 177:    */   {
/* 178:180 */     return this.engine.getNeedClientAuth();
/* 179:    */   }
/* 180:    */   
/* 181:    */   public void setWantClientAuth(boolean b)
/* 182:    */   {
/* 183:185 */     this.engine.setWantClientAuth(b);
/* 184:    */   }
/* 185:    */   
/* 186:    */   public boolean getWantClientAuth()
/* 187:    */   {
/* 188:190 */     return this.engine.getWantClientAuth();
/* 189:    */   }
/* 190:    */   
/* 191:    */   public void setEnableSessionCreation(boolean b)
/* 192:    */   {
/* 193:195 */     this.engine.setEnableSessionCreation(b);
/* 194:    */   }
/* 195:    */   
/* 196:    */   public boolean getEnableSessionCreation()
/* 197:    */   {
/* 198:200 */     return this.engine.getEnableSessionCreation();
/* 199:    */   }
/* 200:    */   
/* 201:    */   public SSLParameters getSSLParameters()
/* 202:    */   {
/* 203:205 */     return this.engine.getSSLParameters();
/* 204:    */   }
/* 205:    */   
/* 206:    */   public void setSSLParameters(SSLParameters sslParameters)
/* 207:    */   {
/* 208:210 */     this.engine.setSSLParameters(sslParameters);
/* 209:    */   }
/* 210:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.JdkSslEngine
 * JD-Core Version:    0.7.0.1
 */