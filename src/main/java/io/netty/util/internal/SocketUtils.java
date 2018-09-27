/*   1:    */ package io.netty.util.internal;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import java.net.InetAddress;
/*   5:    */ import java.net.InetSocketAddress;
/*   6:    */ import java.net.NetworkInterface;
/*   7:    */ import java.net.ServerSocket;
/*   8:    */ import java.net.Socket;
/*   9:    */ import java.net.SocketAddress;
/*  10:    */ import java.net.SocketException;
/*  11:    */ import java.net.UnknownHostException;
/*  12:    */ import java.nio.channels.DatagramChannel;
/*  13:    */ import java.nio.channels.ServerSocketChannel;
/*  14:    */ import java.nio.channels.SocketChannel;
/*  15:    */ import java.security.AccessController;
/*  16:    */ import java.security.PrivilegedAction;
/*  17:    */ import java.security.PrivilegedActionException;
/*  18:    */ import java.security.PrivilegedExceptionAction;
/*  19:    */ import java.util.Enumeration;
/*  20:    */ 
/*  21:    */ public final class SocketUtils
/*  22:    */ {
/*  23:    */   public static void connect(Socket socket, final SocketAddress remoteAddress, final int timeout)
/*  24:    */     throws IOException
/*  25:    */   {
/*  26:    */     try
/*  27:    */     {
/*  28: 51 */       AccessController.doPrivileged(new PrivilegedExceptionAction()
/*  29:    */       {
/*  30:    */         public Void run()
/*  31:    */           throws IOException
/*  32:    */         {
/*  33: 54 */           this.val$socket.connect(remoteAddress, timeout);
/*  34: 55 */           return null;
/*  35:    */         }
/*  36:    */       });
/*  37:    */     }
/*  38:    */     catch (PrivilegedActionException e)
/*  39:    */     {
/*  40: 59 */       throw ((IOException)e.getCause());
/*  41:    */     }
/*  42:    */   }
/*  43:    */   
/*  44:    */   public static void bind(Socket socket, final SocketAddress bindpoint)
/*  45:    */     throws IOException
/*  46:    */   {
/*  47:    */     try
/*  48:    */     {
/*  49: 65 */       AccessController.doPrivileged(new PrivilegedExceptionAction()
/*  50:    */       {
/*  51:    */         public Void run()
/*  52:    */           throws IOException
/*  53:    */         {
/*  54: 68 */           this.val$socket.bind(bindpoint);
/*  55: 69 */           return null;
/*  56:    */         }
/*  57:    */       });
/*  58:    */     }
/*  59:    */     catch (PrivilegedActionException e)
/*  60:    */     {
/*  61: 73 */       throw ((IOException)e.getCause());
/*  62:    */     }
/*  63:    */   }
/*  64:    */   
/*  65:    */   public static boolean connect(SocketChannel socketChannel, final SocketAddress remoteAddress)
/*  66:    */     throws IOException
/*  67:    */   {
/*  68:    */     try
/*  69:    */     {
/*  70: 80 */       ((Boolean)AccessController.doPrivileged(new PrivilegedExceptionAction()
/*  71:    */       {
/*  72:    */         public Boolean run()
/*  73:    */           throws IOException
/*  74:    */         {
/*  75: 83 */           return Boolean.valueOf(this.val$socketChannel.connect(remoteAddress));
/*  76:    */         }
/*  77:    */       })).booleanValue();
/*  78:    */     }
/*  79:    */     catch (PrivilegedActionException e)
/*  80:    */     {
/*  81: 87 */       throw ((IOException)e.getCause());
/*  82:    */     }
/*  83:    */   }
/*  84:    */   
/*  85:    */   public static void bind(SocketChannel socketChannel, final SocketAddress address)
/*  86:    */     throws IOException
/*  87:    */   {
/*  88:    */     try
/*  89:    */     {
/*  90: 93 */       AccessController.doPrivileged(new PrivilegedExceptionAction()
/*  91:    */       {
/*  92:    */         public Void run()
/*  93:    */           throws IOException
/*  94:    */         {
/*  95: 96 */           this.val$socketChannel.bind(address);
/*  96: 97 */           return null;
/*  97:    */         }
/*  98:    */       });
/*  99:    */     }
/* 100:    */     catch (PrivilegedActionException e)
/* 101:    */     {
/* 102:101 */       throw ((IOException)e.getCause());
/* 103:    */     }
/* 104:    */   }
/* 105:    */   
/* 106:    */   public static SocketChannel accept(ServerSocketChannel serverSocketChannel)
/* 107:    */     throws IOException
/* 108:    */   {
/* 109:    */     try
/* 110:    */     {
/* 111:107 */       (SocketChannel)AccessController.doPrivileged(new PrivilegedExceptionAction()
/* 112:    */       {
/* 113:    */         public SocketChannel run()
/* 114:    */           throws IOException
/* 115:    */         {
/* 116:110 */           return this.val$serverSocketChannel.accept();
/* 117:    */         }
/* 118:    */       });
/* 119:    */     }
/* 120:    */     catch (PrivilegedActionException e)
/* 121:    */     {
/* 122:114 */       throw ((IOException)e.getCause());
/* 123:    */     }
/* 124:    */   }
/* 125:    */   
/* 126:    */   public static void bind(DatagramChannel networkChannel, final SocketAddress address)
/* 127:    */     throws IOException
/* 128:    */   {
/* 129:    */     try
/* 130:    */     {
/* 131:120 */       AccessController.doPrivileged(new PrivilegedExceptionAction()
/* 132:    */       {
/* 133:    */         public Void run()
/* 134:    */           throws IOException
/* 135:    */         {
/* 136:123 */           this.val$networkChannel.bind(address);
/* 137:124 */           return null;
/* 138:    */         }
/* 139:    */       });
/* 140:    */     }
/* 141:    */     catch (PrivilegedActionException e)
/* 142:    */     {
/* 143:128 */       throw ((IOException)e.getCause());
/* 144:    */     }
/* 145:    */   }
/* 146:    */   
/* 147:    */   public static SocketAddress localSocketAddress(ServerSocket socket)
/* 148:    */   {
/* 149:133 */     (SocketAddress)AccessController.doPrivileged(new PrivilegedAction()
/* 150:    */     {
/* 151:    */       public SocketAddress run()
/* 152:    */       {
/* 153:136 */         return this.val$socket.getLocalSocketAddress();
/* 154:    */       }
/* 155:    */     });
/* 156:    */   }
/* 157:    */   
/* 158:    */   public static InetAddress addressByName(String hostname)
/* 159:    */     throws UnknownHostException
/* 160:    */   {
/* 161:    */     try
/* 162:    */     {
/* 163:143 */       (InetAddress)AccessController.doPrivileged(new PrivilegedExceptionAction()
/* 164:    */       {
/* 165:    */         public InetAddress run()
/* 166:    */           throws UnknownHostException
/* 167:    */         {
/* 168:146 */           return InetAddress.getByName(this.val$hostname);
/* 169:    */         }
/* 170:    */       });
/* 171:    */     }
/* 172:    */     catch (PrivilegedActionException e)
/* 173:    */     {
/* 174:150 */       throw ((UnknownHostException)e.getCause());
/* 175:    */     }
/* 176:    */   }
/* 177:    */   
/* 178:    */   public static InetAddress[] allAddressesByName(String hostname)
/* 179:    */     throws UnknownHostException
/* 180:    */   {
/* 181:    */     try
/* 182:    */     {
/* 183:156 */       (InetAddress[])AccessController.doPrivileged(new PrivilegedExceptionAction()
/* 184:    */       {
/* 185:    */         public InetAddress[] run()
/* 186:    */           throws UnknownHostException
/* 187:    */         {
/* 188:159 */           return InetAddress.getAllByName(this.val$hostname);
/* 189:    */         }
/* 190:    */       });
/* 191:    */     }
/* 192:    */     catch (PrivilegedActionException e)
/* 193:    */     {
/* 194:163 */       throw ((UnknownHostException)e.getCause());
/* 195:    */     }
/* 196:    */   }
/* 197:    */   
/* 198:    */   public static InetSocketAddress socketAddress(String hostname, final int port)
/* 199:    */   {
/* 200:168 */     (InetSocketAddress)AccessController.doPrivileged(new PrivilegedAction()
/* 201:    */     {
/* 202:    */       public InetSocketAddress run()
/* 203:    */       {
/* 204:171 */         return new InetSocketAddress(this.val$hostname, port);
/* 205:    */       }
/* 206:    */     });
/* 207:    */   }
/* 208:    */   
/* 209:    */   public static Enumeration<InetAddress> addressesFromNetworkInterface(NetworkInterface intf)
/* 210:    */   {
/* 211:177 */     (Enumeration)AccessController.doPrivileged(new PrivilegedAction()
/* 212:    */     {
/* 213:    */       public Enumeration<InetAddress> run()
/* 214:    */       {
/* 215:180 */         return this.val$intf.getInetAddresses();
/* 216:    */       }
/* 217:    */     });
/* 218:    */   }
/* 219:    */   
/* 220:    */   public static InetAddress loopbackAddress()
/* 221:    */   {
/* 222:186 */     (InetAddress)AccessController.doPrivileged(new PrivilegedAction()
/* 223:    */     {
/* 224:    */       public InetAddress run()
/* 225:    */       {
/* 226:189 */         if (PlatformDependent.javaVersion() >= 7) {
/* 227:190 */           return InetAddress.getLoopbackAddress();
/* 228:    */         }
/* 229:    */         try
/* 230:    */         {
/* 231:193 */           return InetAddress.getByName(null);
/* 232:    */         }
/* 233:    */         catch (UnknownHostException e)
/* 234:    */         {
/* 235:195 */           throw new IllegalStateException(e);
/* 236:    */         }
/* 237:    */       }
/* 238:    */     });
/* 239:    */   }
/* 240:    */   
/* 241:    */   public static byte[] hardwareAddressFromNetworkInterface(NetworkInterface intf)
/* 242:    */     throws SocketException
/* 243:    */   {
/* 244:    */     try
/* 245:    */     {
/* 246:203 */       (byte[])AccessController.doPrivileged(new PrivilegedExceptionAction()
/* 247:    */       {
/* 248:    */         public byte[] run()
/* 249:    */           throws SocketException
/* 250:    */         {
/* 251:206 */           return this.val$intf.getHardwareAddress();
/* 252:    */         }
/* 253:    */       });
/* 254:    */     }
/* 255:    */     catch (PrivilegedActionException e)
/* 256:    */     {
/* 257:210 */       throw ((SocketException)e.getCause());
/* 258:    */     }
/* 259:    */   }
/* 260:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.SocketUtils
 * JD-Core Version:    0.7.0.1
 */