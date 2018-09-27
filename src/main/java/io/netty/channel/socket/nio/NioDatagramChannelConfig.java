/*   1:    */ package io.netty.channel.socket.nio;
/*   2:    */ 
/*   3:    */ import io.netty.channel.ChannelException;
/*   4:    */ import io.netty.channel.socket.DatagramChannelConfig;
/*   5:    */ import io.netty.channel.socket.DefaultDatagramChannelConfig;
/*   6:    */ import io.netty.util.internal.PlatformDependent;
/*   7:    */ import io.netty.util.internal.SocketUtils;
/*   8:    */ import java.lang.reflect.Field;
/*   9:    */ import java.lang.reflect.Method;
/*  10:    */ import java.net.InetAddress;
/*  11:    */ import java.net.NetworkInterface;
/*  12:    */ import java.net.SocketException;
/*  13:    */ import java.nio.channels.DatagramChannel;
/*  14:    */ import java.util.Enumeration;
/*  15:    */ 
/*  16:    */ class NioDatagramChannelConfig
/*  17:    */   extends DefaultDatagramChannelConfig
/*  18:    */ {
/*  19:    */   private static final Object IP_MULTICAST_TTL;
/*  20:    */   private static final Object IP_MULTICAST_IF;
/*  21:    */   private static final Object IP_MULTICAST_LOOP;
/*  22:    */   private static final Method GET_OPTION;
/*  23:    */   private static final Method SET_OPTION;
/*  24:    */   private final DatagramChannel javaChannel;
/*  25:    */   
/*  26:    */   static
/*  27:    */   {
/*  28: 43 */     ClassLoader classLoader = PlatformDependent.getClassLoader(DatagramChannel.class);
/*  29: 44 */     Class<?> socketOptionType = null;
/*  30:    */     try
/*  31:    */     {
/*  32: 46 */       socketOptionType = Class.forName("java.net.SocketOption", true, classLoader);
/*  33:    */     }
/*  34:    */     catch (Exception localException1) {}
/*  35: 50 */     Class<?> stdSocketOptionType = null;
/*  36:    */     try
/*  37:    */     {
/*  38: 52 */       stdSocketOptionType = Class.forName("java.net.StandardSocketOptions", true, classLoader);
/*  39:    */     }
/*  40:    */     catch (Exception localException2) {}
/*  41: 57 */     Object ipMulticastTtl = null;
/*  42: 58 */     Object ipMulticastIf = null;
/*  43: 59 */     Object ipMulticastLoop = null;
/*  44: 60 */     Method getOption = null;
/*  45: 61 */     Method setOption = null;
/*  46: 62 */     if (socketOptionType != null)
/*  47:    */     {
/*  48:    */       try
/*  49:    */       {
/*  50: 64 */         ipMulticastTtl = stdSocketOptionType.getDeclaredField("IP_MULTICAST_TTL").get(null);
/*  51:    */       }
/*  52:    */       catch (Exception e)
/*  53:    */       {
/*  54: 66 */         throw new Error("cannot locate the IP_MULTICAST_TTL field", e);
/*  55:    */       }
/*  56:    */       try
/*  57:    */       {
/*  58: 70 */         ipMulticastIf = stdSocketOptionType.getDeclaredField("IP_MULTICAST_IF").get(null);
/*  59:    */       }
/*  60:    */       catch (Exception e)
/*  61:    */       {
/*  62: 72 */         throw new Error("cannot locate the IP_MULTICAST_IF field", e);
/*  63:    */       }
/*  64:    */       try
/*  65:    */       {
/*  66: 76 */         ipMulticastLoop = stdSocketOptionType.getDeclaredField("IP_MULTICAST_LOOP").get(null);
/*  67:    */       }
/*  68:    */       catch (Exception e)
/*  69:    */       {
/*  70: 78 */         throw new Error("cannot locate the IP_MULTICAST_LOOP field", e);
/*  71:    */       }
/*  72: 81 */       Class<?> networkChannelClass = null;
/*  73:    */       try
/*  74:    */       {
/*  75: 83 */         networkChannelClass = Class.forName("java.nio.channels.NetworkChannel", true, classLoader);
/*  76:    */       }
/*  77:    */       catch (Throwable localThrowable) {}
/*  78: 88 */       if (networkChannelClass == null)
/*  79:    */       {
/*  80: 89 */         getOption = null;
/*  81: 90 */         setOption = null;
/*  82:    */       }
/*  83:    */       else
/*  84:    */       {
/*  85:    */         try
/*  86:    */         {
/*  87: 93 */           getOption = networkChannelClass.getDeclaredMethod("getOption", new Class[] { socketOptionType });
/*  88:    */         }
/*  89:    */         catch (Exception e)
/*  90:    */         {
/*  91: 95 */           throw new Error("cannot locate the getOption() method", e);
/*  92:    */         }
/*  93:    */         try
/*  94:    */         {
/*  95: 99 */           setOption = networkChannelClass.getDeclaredMethod("setOption", new Class[] { socketOptionType, Object.class });
/*  96:    */         }
/*  97:    */         catch (Exception e)
/*  98:    */         {
/*  99:101 */           throw new Error("cannot locate the setOption() method", e);
/* 100:    */         }
/* 101:    */       }
/* 102:    */     }
/* 103:105 */     IP_MULTICAST_TTL = ipMulticastTtl;
/* 104:106 */     IP_MULTICAST_IF = ipMulticastIf;
/* 105:107 */     IP_MULTICAST_LOOP = ipMulticastLoop;
/* 106:108 */     GET_OPTION = getOption;
/* 107:109 */     SET_OPTION = setOption;
/* 108:    */   }
/* 109:    */   
/* 110:    */   NioDatagramChannelConfig(NioDatagramChannel channel, DatagramChannel javaChannel)
/* 111:    */   {
/* 112:115 */     super(channel, javaChannel.socket());
/* 113:116 */     this.javaChannel = javaChannel;
/* 114:    */   }
/* 115:    */   
/* 116:    */   public int getTimeToLive()
/* 117:    */   {
/* 118:121 */     return ((Integer)getOption0(IP_MULTICAST_TTL)).intValue();
/* 119:    */   }
/* 120:    */   
/* 121:    */   public DatagramChannelConfig setTimeToLive(int ttl)
/* 122:    */   {
/* 123:126 */     setOption0(IP_MULTICAST_TTL, Integer.valueOf(ttl));
/* 124:127 */     return this;
/* 125:    */   }
/* 126:    */   
/* 127:    */   public InetAddress getInterface()
/* 128:    */   {
/* 129:132 */     NetworkInterface inf = getNetworkInterface();
/* 130:133 */     if (inf == null) {
/* 131:134 */       return null;
/* 132:    */     }
/* 133:136 */     Enumeration<InetAddress> addresses = SocketUtils.addressesFromNetworkInterface(inf);
/* 134:137 */     if (addresses.hasMoreElements()) {
/* 135:138 */       return (InetAddress)addresses.nextElement();
/* 136:    */     }
/* 137:140 */     return null;
/* 138:    */   }
/* 139:    */   
/* 140:    */   public DatagramChannelConfig setInterface(InetAddress interfaceAddress)
/* 141:    */   {
/* 142:    */     try
/* 143:    */     {
/* 144:147 */       setNetworkInterface(NetworkInterface.getByInetAddress(interfaceAddress));
/* 145:    */     }
/* 146:    */     catch (SocketException e)
/* 147:    */     {
/* 148:149 */       throw new ChannelException(e);
/* 149:    */     }
/* 150:151 */     return this;
/* 151:    */   }
/* 152:    */   
/* 153:    */   public NetworkInterface getNetworkInterface()
/* 154:    */   {
/* 155:156 */     return (NetworkInterface)getOption0(IP_MULTICAST_IF);
/* 156:    */   }
/* 157:    */   
/* 158:    */   public DatagramChannelConfig setNetworkInterface(NetworkInterface networkInterface)
/* 159:    */   {
/* 160:161 */     setOption0(IP_MULTICAST_IF, networkInterface);
/* 161:162 */     return this;
/* 162:    */   }
/* 163:    */   
/* 164:    */   public boolean isLoopbackModeDisabled()
/* 165:    */   {
/* 166:167 */     return ((Boolean)getOption0(IP_MULTICAST_LOOP)).booleanValue();
/* 167:    */   }
/* 168:    */   
/* 169:    */   public DatagramChannelConfig setLoopbackModeDisabled(boolean loopbackModeDisabled)
/* 170:    */   {
/* 171:172 */     setOption0(IP_MULTICAST_LOOP, Boolean.valueOf(loopbackModeDisabled));
/* 172:173 */     return this;
/* 173:    */   }
/* 174:    */   
/* 175:    */   public DatagramChannelConfig setAutoRead(boolean autoRead)
/* 176:    */   {
/* 177:178 */     super.setAutoRead(autoRead);
/* 178:179 */     return this;
/* 179:    */   }
/* 180:    */   
/* 181:    */   protected void autoReadCleared()
/* 182:    */   {
/* 183:184 */     ((NioDatagramChannel)this.channel).clearReadPending0();
/* 184:    */   }
/* 185:    */   
/* 186:    */   private Object getOption0(Object option)
/* 187:    */   {
/* 188:188 */     if (GET_OPTION == null) {
/* 189:189 */       throw new UnsupportedOperationException();
/* 190:    */     }
/* 191:    */     try
/* 192:    */     {
/* 193:192 */       return GET_OPTION.invoke(this.javaChannel, new Object[] { option });
/* 194:    */     }
/* 195:    */     catch (Exception e)
/* 196:    */     {
/* 197:194 */       throw new ChannelException(e);
/* 198:    */     }
/* 199:    */   }
/* 200:    */   
/* 201:    */   private void setOption0(Object option, Object value)
/* 202:    */   {
/* 203:200 */     if (SET_OPTION == null) {
/* 204:201 */       throw new UnsupportedOperationException();
/* 205:    */     }
/* 206:    */     try
/* 207:    */     {
/* 208:204 */       SET_OPTION.invoke(this.javaChannel, new Object[] { option, value });
/* 209:    */     }
/* 210:    */     catch (Exception e)
/* 211:    */     {
/* 212:206 */       throw new ChannelException(e);
/* 213:    */     }
/* 214:    */   }
/* 215:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.socket.nio.NioDatagramChannelConfig
 * JD-Core Version:    0.7.0.1
 */