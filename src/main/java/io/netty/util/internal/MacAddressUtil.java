/*   1:    */ package io.netty.util.internal;
/*   2:    */ 
/*   3:    */ import io.netty.util.NetUtil;
/*   4:    */ import io.netty.util.internal.logging.InternalLogger;
/*   5:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   6:    */ import java.net.InetAddress;
/*   7:    */ import java.net.NetworkInterface;
/*   8:    */ import java.net.SocketException;
/*   9:    */ import java.util.Arrays;
/*  10:    */ import java.util.Enumeration;
/*  11:    */ import java.util.LinkedHashMap;
/*  12:    */ import java.util.Map;
/*  13:    */ import java.util.Map.Entry;
/*  14:    */ import java.util.Random;
/*  15:    */ 
/*  16:    */ public final class MacAddressUtil
/*  17:    */ {
/*  18: 35 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(MacAddressUtil.class);
/*  19:    */   private static final int EUI64_MAC_ADDRESS_LENGTH = 8;
/*  20:    */   private static final int EUI48_MAC_ADDRESS_LENGTH = 6;
/*  21:    */   
/*  22:    */   public static byte[] bestAvailableMac()
/*  23:    */   {
/*  24: 49 */     byte[] bestMacAddr = EmptyArrays.EMPTY_BYTES;
/*  25: 50 */     InetAddress bestInetAddr = NetUtil.LOCALHOST4;
/*  26:    */     
/*  27:    */ 
/*  28: 53 */     Map<NetworkInterface, InetAddress> ifaces = new LinkedHashMap();
/*  29:    */     try
/*  30:    */     {
/*  31: 55 */       Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
/*  32: 56 */       if (interfaces != null) {
/*  33: 57 */         while (interfaces.hasMoreElements())
/*  34:    */         {
/*  35: 58 */           NetworkInterface iface = (NetworkInterface)interfaces.nextElement();
/*  36:    */           
/*  37: 60 */           Enumeration<InetAddress> addrs = SocketUtils.addressesFromNetworkInterface(iface);
/*  38: 61 */           if (addrs.hasMoreElements())
/*  39:    */           {
/*  40: 62 */             InetAddress a = (InetAddress)addrs.nextElement();
/*  41: 63 */             if (!a.isLoopbackAddress()) {
/*  42: 64 */               ifaces.put(iface, a);
/*  43:    */             }
/*  44:    */           }
/*  45:    */         }
/*  46:    */       }
/*  47:    */     }
/*  48:    */     catch (SocketException e)
/*  49:    */     {
/*  50: 70 */       logger.warn("Failed to retrieve the list of available network interfaces", e);
/*  51:    */     }
/*  52: 73 */     for (Map.Entry<NetworkInterface, InetAddress> entry : ifaces.entrySet())
/*  53:    */     {
/*  54: 74 */       NetworkInterface iface = (NetworkInterface)entry.getKey();
/*  55: 75 */       InetAddress inetAddr = (InetAddress)entry.getValue();
/*  56: 76 */       if (!iface.isVirtual())
/*  57:    */       {
/*  58:    */         try
/*  59:    */         {
/*  60: 82 */           macAddr = SocketUtils.hardwareAddressFromNetworkInterface(iface);
/*  61:    */         }
/*  62:    */         catch (SocketException e)
/*  63:    */         {
/*  64:    */           byte[] macAddr;
/*  65: 84 */           logger.debug("Failed to get the hardware address of a network interface: {}", iface, e);
/*  66:    */         }
/*  67: 85 */         continue;
/*  68:    */         byte[] macAddr;
/*  69: 88 */         boolean replace = false;
/*  70: 89 */         int res = compareAddresses(bestMacAddr, macAddr);
/*  71: 90 */         if (res < 0)
/*  72:    */         {
/*  73: 92 */           replace = true;
/*  74:    */         }
/*  75: 93 */         else if (res == 0)
/*  76:    */         {
/*  77: 95 */           res = compareAddresses(bestInetAddr, inetAddr);
/*  78: 96 */           if (res < 0) {
/*  79: 98 */             replace = true;
/*  80: 99 */           } else if (res == 0) {
/*  81:101 */             if (bestMacAddr.length < macAddr.length) {
/*  82:102 */               replace = true;
/*  83:    */             }
/*  84:    */           }
/*  85:    */         }
/*  86:107 */         if (replace)
/*  87:    */         {
/*  88:108 */           bestMacAddr = macAddr;
/*  89:109 */           bestInetAddr = inetAddr;
/*  90:    */         }
/*  91:    */       }
/*  92:    */     }
/*  93:113 */     if (bestMacAddr == EmptyArrays.EMPTY_BYTES) {
/*  94:114 */       return null;
/*  95:    */     }
/*  96:117 */     switch (bestMacAddr.length)
/*  97:    */     {
/*  98:    */     case 6: 
/*  99:119 */       byte[] newAddr = new byte[8];
/* 100:120 */       System.arraycopy(bestMacAddr, 0, newAddr, 0, 3);
/* 101:121 */       newAddr[3] = -1;
/* 102:122 */       newAddr[4] = -2;
/* 103:123 */       System.arraycopy(bestMacAddr, 3, newAddr, 5, 3);
/* 104:124 */       bestMacAddr = newAddr;
/* 105:125 */       break;
/* 106:    */     default: 
/* 107:127 */       bestMacAddr = Arrays.copyOf(bestMacAddr, 8);
/* 108:    */     }
/* 109:130 */     return bestMacAddr;
/* 110:    */   }
/* 111:    */   
/* 112:    */   public static byte[] defaultMachineId()
/* 113:    */   {
/* 114:138 */     byte[] bestMacAddr = bestAvailableMac();
/* 115:139 */     if (bestMacAddr == null)
/* 116:    */     {
/* 117:140 */       bestMacAddr = new byte[8];
/* 118:141 */       PlatformDependent.threadLocalRandom().nextBytes(bestMacAddr);
/* 119:142 */       logger.warn("Failed to find a usable hardware address from the network interfaces; using random bytes: {}", 
/* 120:    */       
/* 121:144 */         formatAddress(bestMacAddr));
/* 122:    */     }
/* 123:146 */     return bestMacAddr;
/* 124:    */   }
/* 125:    */   
/* 126:    */   public static byte[] parseMAC(String value)
/* 127:    */   {
/* 128:    */     byte[] machineId;
/* 129:    */     byte[] machineId;
/* 130:157 */     switch (value.length())
/* 131:    */     {
/* 132:    */     case 17: 
/* 133:159 */       char separator = value.charAt(2);
/* 134:160 */       validateMacSeparator(separator);
/* 135:161 */       machineId = new byte[6];
/* 136:162 */       break;
/* 137:    */     case 23: 
/* 138:164 */       char separator = value.charAt(2);
/* 139:165 */       validateMacSeparator(separator);
/* 140:166 */       machineId = new byte[8];
/* 141:167 */       break;
/* 142:    */     default: 
/* 143:169 */       throw new IllegalArgumentException("value is not supported [MAC-48, EUI-48, EUI-64]");
/* 144:    */     }
/* 145:    */     char separator;
/* 146:    */     byte[] machineId;
/* 147:172 */     int end = machineId.length - 1;
/* 148:173 */     int j = 0;
/* 149:174 */     for (int i = 0; i < end; j += 3)
/* 150:    */     {
/* 151:175 */       int sIndex = j + 2;
/* 152:176 */       machineId[i] = StringUtil.decodeHexByte(value, j);
/* 153:177 */       if (value.charAt(sIndex) != separator) {
/* 154:179 */         throw new IllegalArgumentException("expected separator '" + separator + " but got '" + value.charAt(sIndex) + "' at index: " + sIndex);
/* 155:    */       }
/* 156:174 */       i++;
/* 157:    */     }
/* 158:183 */     machineId[end] = StringUtil.decodeHexByte(value, j);
/* 159:    */     
/* 160:185 */     return machineId;
/* 161:    */   }
/* 162:    */   
/* 163:    */   private static void validateMacSeparator(char separator)
/* 164:    */   {
/* 165:189 */     if ((separator != ':') && (separator != '-')) {
/* 166:190 */       throw new IllegalArgumentException("unsupported separator: " + separator + " (expected: [:-])");
/* 167:    */     }
/* 168:    */   }
/* 169:    */   
/* 170:    */   public static String formatAddress(byte[] addr)
/* 171:    */   {
/* 172:199 */     StringBuilder buf = new StringBuilder(24);
/* 173:200 */     for (byte b : addr) {
/* 174:201 */       buf.append(String.format("%02x:", new Object[] { Integer.valueOf(b & 0xFF) }));
/* 175:    */     }
/* 176:203 */     return buf.substring(0, buf.length() - 1);
/* 177:    */   }
/* 178:    */   
/* 179:    */   static int compareAddresses(byte[] current, byte[] candidate)
/* 180:    */   {
/* 181:211 */     if ((candidate == null) || (candidate.length < 6)) {
/* 182:212 */       return 1;
/* 183:    */     }
/* 184:216 */     boolean onlyZeroAndOne = true;
/* 185:217 */     for (byte b : candidate) {
/* 186:218 */       if ((b != 0) && (b != 1))
/* 187:    */       {
/* 188:219 */         onlyZeroAndOne = false;
/* 189:220 */         break;
/* 190:    */       }
/* 191:    */     }
/* 192:224 */     if (onlyZeroAndOne) {
/* 193:225 */       return 1;
/* 194:    */     }
/* 195:229 */     if ((candidate[0] & 0x1) != 0) {
/* 196:230 */       return 1;
/* 197:    */     }
/* 198:234 */     if ((candidate[0] & 0x2) == 0)
/* 199:    */     {
/* 200:235 */       if ((current.length != 0) && ((current[0] & 0x2) == 0)) {
/* 201:237 */         return 0;
/* 202:    */       }
/* 203:240 */       return -1;
/* 204:    */     }
/* 205:243 */     if ((current.length != 0) && ((current[0] & 0x2) == 0)) {
/* 206:245 */       return 1;
/* 207:    */     }
/* 208:248 */     return 0;
/* 209:    */   }
/* 210:    */   
/* 211:    */   private static int compareAddresses(InetAddress current, InetAddress candidate)
/* 212:    */   {
/* 213:257 */     return scoreAddress(current) - scoreAddress(candidate);
/* 214:    */   }
/* 215:    */   
/* 216:    */   private static int scoreAddress(InetAddress addr)
/* 217:    */   {
/* 218:261 */     if ((addr.isAnyLocalAddress()) || (addr.isLoopbackAddress())) {
/* 219:262 */       return 0;
/* 220:    */     }
/* 221:264 */     if (addr.isMulticastAddress()) {
/* 222:265 */       return 1;
/* 223:    */     }
/* 224:267 */     if (addr.isLinkLocalAddress()) {
/* 225:268 */       return 2;
/* 226:    */     }
/* 227:270 */     if (addr.isSiteLocalAddress()) {
/* 228:271 */       return 3;
/* 229:    */     }
/* 230:274 */     return 4;
/* 231:    */   }
/* 232:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.MacAddressUtil
 * JD-Core Version:    0.7.0.1
 */