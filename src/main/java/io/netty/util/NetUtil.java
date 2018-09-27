/*    1:     */ package io.netty.util;
/*    2:     */ 
/*    3:     */ import io.netty.util.internal.PlatformDependent;
/*    4:     */ import io.netty.util.internal.SocketUtils;
/*    5:     */ import io.netty.util.internal.StringUtil;
/*    6:     */ import io.netty.util.internal.SystemPropertyUtil;
/*    7:     */ import io.netty.util.internal.logging.InternalLogger;
/*    8:     */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*    9:     */ import java.io.BufferedReader;
/*   10:     */ import java.io.File;
/*   11:     */ import java.io.FileReader;
/*   12:     */ import java.net.Inet4Address;
/*   13:     */ import java.net.Inet6Address;
/*   14:     */ import java.net.InetAddress;
/*   15:     */ import java.net.InetSocketAddress;
/*   16:     */ import java.net.NetworkInterface;
/*   17:     */ import java.net.SocketException;
/*   18:     */ import java.net.UnknownHostException;
/*   19:     */ import java.security.AccessController;
/*   20:     */ import java.security.PrivilegedAction;
/*   21:     */ import java.util.ArrayList;
/*   22:     */ import java.util.Enumeration;
/*   23:     */ import java.util.Iterator;
/*   24:     */ import java.util.List;
/*   25:     */ 
/*   26:     */ public final class NetUtil
/*   27:     */ {
/*   28: 123 */   private static final boolean IPV4_PREFERRED = SystemPropertyUtil.getBoolean("java.net.preferIPv4Stack", false);
/*   29: 129 */   private static final boolean IPV6_ADDRESSES_PREFERRED = SystemPropertyUtil.getBoolean("java.net.preferIPv6Addresses", false);
/*   30: 134 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(NetUtil.class);
/*   31:     */   
/*   32:     */   static
/*   33:     */   {
/*   34: 137 */     logger.debug("-Djava.net.preferIPv4Stack: {}", Boolean.valueOf(IPV4_PREFERRED));
/*   35: 138 */     logger.debug("-Djava.net.preferIPv6Addresses: {}", Boolean.valueOf(IPV6_ADDRESSES_PREFERRED));
/*   36:     */     
/*   37: 140 */     byte[] LOCALHOST4_BYTES = { 127, 0, 0, 1 };
/*   38: 141 */     byte[] LOCALHOST6_BYTES = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 };
/*   39:     */     
/*   40:     */ 
/*   41: 144 */     Inet4Address localhost4 = null;
/*   42:     */     try
/*   43:     */     {
/*   44: 146 */       localhost4 = (Inet4Address)InetAddress.getByAddress("localhost", LOCALHOST4_BYTES);
/*   45:     */     }
/*   46:     */     catch (Exception e)
/*   47:     */     {
/*   48: 149 */       PlatformDependent.throwException(e);
/*   49:     */     }
/*   50: 151 */     LOCALHOST4 = localhost4;
/*   51:     */     
/*   52:     */ 
/*   53: 154 */     Inet6Address localhost6 = null;
/*   54:     */     try
/*   55:     */     {
/*   56: 156 */       localhost6 = (Inet6Address)InetAddress.getByAddress("localhost", LOCALHOST6_BYTES);
/*   57:     */     }
/*   58:     */     catch (Exception e)
/*   59:     */     {
/*   60: 159 */       PlatformDependent.throwException(e);
/*   61:     */     }
/*   62: 161 */     LOCALHOST6 = localhost6;
/*   63:     */     
/*   64:     */ 
/*   65: 164 */     List<NetworkInterface> ifaces = new ArrayList();
/*   66:     */     try
/*   67:     */     {
/*   68: 166 */       Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
/*   69: 167 */       if (interfaces != null) {
/*   70: 168 */         while (interfaces.hasMoreElements())
/*   71:     */         {
/*   72: 169 */           NetworkInterface iface = (NetworkInterface)interfaces.nextElement();
/*   73: 171 */           if (SocketUtils.addressesFromNetworkInterface(iface).hasMoreElements()) {
/*   74: 172 */             ifaces.add(iface);
/*   75:     */           }
/*   76:     */         }
/*   77:     */       }
/*   78:     */     }
/*   79:     */     catch (SocketException e)
/*   80:     */     {
/*   81: 177 */       logger.warn("Failed to retrieve the list of available network interfaces", e);
/*   82:     */     }
/*   83: 183 */     NetworkInterface loopbackIface = null;
/*   84: 184 */     InetAddress loopbackAddr = null;
/*   85: 185 */     for (Iterator localIterator = ifaces.iterator(); localIterator.hasNext();)
/*   86:     */     {
/*   87: 185 */       iface = (NetworkInterface)localIterator.next();
/*   88: 186 */       for (i = SocketUtils.addressesFromNetworkInterface(iface); i.hasMoreElements();)
/*   89:     */       {
/*   90: 187 */         InetAddress addr = (InetAddress)i.nextElement();
/*   91: 188 */         if (addr.isLoopbackAddress())
/*   92:     */         {
/*   93: 190 */           loopbackIface = iface;
/*   94: 191 */           loopbackAddr = addr;
/*   95:     */           break label390;
/*   96:     */         }
/*   97:     */       }
/*   98:     */     }
/*   99:     */     NetworkInterface iface;
/*  100:     */     Enumeration<InetAddress> i;
/*  101:     */     label390:
/*  102: 198 */     if (loopbackIface == null) {
/*  103:     */       try
/*  104:     */       {
/*  105: 200 */         for (NetworkInterface iface : ifaces) {
/*  106: 201 */           if (iface.isLoopback())
/*  107:     */           {
/*  108: 202 */             Enumeration<InetAddress> i = SocketUtils.addressesFromNetworkInterface(iface);
/*  109: 203 */             if (i.hasMoreElements())
/*  110:     */             {
/*  111: 205 */               loopbackIface = iface;
/*  112: 206 */               loopbackAddr = (InetAddress)i.nextElement();
/*  113: 207 */               break;
/*  114:     */             }
/*  115:     */           }
/*  116:     */         }
/*  117: 212 */         if (loopbackIface == null) {
/*  118: 213 */           logger.warn("Failed to find the loopback interface");
/*  119:     */         }
/*  120:     */       }
/*  121:     */       catch (SocketException e)
/*  122:     */       {
/*  123: 216 */         logger.warn("Failed to find the loopback interface", e);
/*  124:     */       }
/*  125:     */     }
/*  126: 220 */     if (loopbackIface != null) {
/*  127: 222 */       logger.debug("Loopback interface: {} ({}, {})", new Object[] {loopbackIface
/*  128:     */       
/*  129: 224 */         .getName(), loopbackIface.getDisplayName(), loopbackAddr.getHostAddress() });
/*  130: 228 */     } else if (loopbackAddr == null) {
/*  131:     */       try
/*  132:     */       {
/*  133: 230 */         if (NetworkInterface.getByInetAddress(LOCALHOST6) != null)
/*  134:     */         {
/*  135: 231 */           logger.debug("Using hard-coded IPv6 localhost address: {}", localhost6);
/*  136: 232 */           loopbackAddr = localhost6;
/*  137:     */         }
/*  138:     */       }
/*  139:     */       catch (Exception localException2) {}finally
/*  140:     */       {
/*  141: 237 */         if (loopbackAddr == null)
/*  142:     */         {
/*  143: 238 */           logger.debug("Using hard-coded IPv4 localhost address: {}", localhost4);
/*  144: 239 */           loopbackAddr = localhost4;
/*  145:     */         }
/*  146:     */       }
/*  147:     */     }
/*  148: 245 */     LOOPBACK_IF = loopbackIface;
/*  149: 246 */     LOCALHOST = loopbackAddr;
/*  150:     */   }
/*  151:     */   
/*  152: 251 */   public static final int SOMAXCONN = ((Integer)AccessController.doPrivileged(new PrivilegedAction()
/*  153:     */   {
/*  154:     */     public Integer run()
/*  155:     */     {
/*  156: 258 */       somaxconn = PlatformDependent.isWindows() ? 200 : 128;
/*  157: 259 */       File file = new File("/proc/sys/net/core/somaxconn");
/*  158: 260 */       BufferedReader in = null;
/*  159:     */       try
/*  160:     */       {
/*  161: 265 */         if (file.exists())
/*  162:     */         {
/*  163: 266 */           in = new BufferedReader(new FileReader(file));
/*  164: 267 */           somaxconn = Integer.parseInt(in.readLine());
/*  165: 268 */           if (NetUtil.logger.isDebugEnabled()) {
/*  166: 269 */             NetUtil.logger.debug("{}: {}", file, Integer.valueOf(somaxconn));
/*  167:     */           }
/*  168:     */         }
/*  169:     */         else
/*  170:     */         {
/*  171: 273 */           Integer tmp = null;
/*  172: 274 */           if (SystemPropertyUtil.getBoolean("io.netty.net.somaxconn.trySysctl", false))
/*  173:     */           {
/*  174: 275 */             tmp = NetUtil.sysctlGetInt("kern.ipc.somaxconn");
/*  175: 276 */             if (tmp == null)
/*  176:     */             {
/*  177: 277 */               tmp = NetUtil.sysctlGetInt("kern.ipc.soacceptqueue");
/*  178: 278 */               if (tmp != null) {
/*  179: 279 */                 somaxconn = tmp.intValue();
/*  180:     */               }
/*  181:     */             }
/*  182:     */             else
/*  183:     */             {
/*  184: 282 */               somaxconn = tmp.intValue();
/*  185:     */             }
/*  186:     */           }
/*  187: 286 */           if (tmp == null) {
/*  188: 287 */             NetUtil.logger.debug("Failed to get SOMAXCONN from sysctl and file {}. Default: {}", file, 
/*  189: 288 */               Integer.valueOf(somaxconn));
/*  190:     */           }
/*  191:     */         }
/*  192: 302 */         return Integer.valueOf(somaxconn);
/*  193:     */       }
/*  194:     */       catch (Exception e)
/*  195:     */       {
/*  196: 292 */         NetUtil.logger.debug("Failed to get SOMAXCONN from sysctl and file {}. Default: {}", new Object[] { file, Integer.valueOf(somaxconn), e });
/*  197:     */       }
/*  198:     */       finally
/*  199:     */       {
/*  200: 294 */         if (in != null) {
/*  201:     */           try
/*  202:     */           {
/*  203: 296 */             in.close();
/*  204:     */           }
/*  205:     */           catch (Exception localException3) {}
/*  206:     */         }
/*  207:     */       }
/*  208:     */     }
/*  209: 251 */   })).intValue();
/*  210:     */   public static final Inet4Address LOCALHOST4;
/*  211:     */   public static final Inet6Address LOCALHOST6;
/*  212:     */   public static final InetAddress LOCALHOST;
/*  213:     */   public static final NetworkInterface LOOPBACK_IF;
/*  214:     */   private static final int IPV6_WORD_COUNT = 8;
/*  215:     */   private static final int IPV6_MAX_CHAR_COUNT = 39;
/*  216:     */   private static final int IPV6_BYTE_COUNT = 16;
/*  217:     */   private static final int IPV6_MAX_CHAR_BETWEEN_SEPARATOR = 4;
/*  218:     */   private static final int IPV6_MIN_SEPARATORS = 2;
/*  219:     */   private static final int IPV6_MAX_SEPARATORS = 8;
/*  220:     */   private static final int IPV4_MAX_CHAR_BETWEEN_SEPARATOR = 3;
/*  221:     */   private static final int IPV4_SEPARATORS = 3;
/*  222:     */   
/*  223:     */   public static boolean isIpV4StackPreferred()
/*  224:     */   {
/*  225: 347 */     return IPV4_PREFERRED;
/*  226:     */   }
/*  227:     */   
/*  228:     */   public static boolean isIpV6AddressesPreferred()
/*  229:     */   {
/*  230: 358 */     return IPV6_ADDRESSES_PREFERRED;
/*  231:     */   }
/*  232:     */   
/*  233:     */   public static byte[] createByteArrayFromIpAddressString(String ipAddressString)
/*  234:     */   {
/*  235: 366 */     if (isValidIpV4Address(ipAddressString)) {
/*  236: 367 */       return validIpV4ToBytes(ipAddressString);
/*  237:     */     }
/*  238: 370 */     if (isValidIpV6Address(ipAddressString))
/*  239:     */     {
/*  240: 371 */       if (ipAddressString.charAt(0) == '[') {
/*  241: 372 */         ipAddressString = ipAddressString.substring(1, ipAddressString.length() - 1);
/*  242:     */       }
/*  243: 375 */       int percentPos = ipAddressString.indexOf('%');
/*  244: 376 */       if (percentPos >= 0) {
/*  245: 377 */         ipAddressString = ipAddressString.substring(0, percentPos);
/*  246:     */       }
/*  247: 380 */       return getIPv6ByName(ipAddressString, true);
/*  248:     */     }
/*  249: 382 */     return null;
/*  250:     */   }
/*  251:     */   
/*  252:     */   private static int decimalDigit(String str, int pos)
/*  253:     */   {
/*  254: 386 */     return str.charAt(pos) - '0';
/*  255:     */   }
/*  256:     */   
/*  257:     */   private static byte ipv4WordToByte(String ip, int from, int toExclusive)
/*  258:     */   {
/*  259: 390 */     int ret = decimalDigit(ip, from);
/*  260: 391 */     from++;
/*  261: 392 */     if (from == toExclusive) {
/*  262: 393 */       return (byte)ret;
/*  263:     */     }
/*  264: 395 */     ret = ret * 10 + decimalDigit(ip, from);
/*  265: 396 */     from++;
/*  266: 397 */     if (from == toExclusive) {
/*  267: 398 */       return (byte)ret;
/*  268:     */     }
/*  269: 400 */     return (byte)(ret * 10 + decimalDigit(ip, from));
/*  270:     */   }
/*  271:     */   
/*  272:     */   static byte[] validIpV4ToBytes(String ip)
/*  273:     */   {
/*  274: 406 */     byte[] tmp3_1 = new byte[4]; int 
/*  275: 407 */       tmp14_11 = ip.indexOf('.', 1);int i = tmp14_11;tmp3_1[0] = ipv4WordToByte(ip, 0, tmp14_11); byte[] tmp20_3 = tmp3_1; int 
/*  276: 408 */       tmp35_32 = ip.indexOf('.', i + 2);i = tmp35_32;tmp20_3[1] = ipv4WordToByte(ip, i + 1, tmp35_32); byte[] tmp41_20 = tmp20_3; int 
/*  277: 409 */       tmp56_53 = ip.indexOf('.', i + 2);i = tmp56_53;tmp41_20[2] = ipv4WordToByte(ip, i + 1, tmp56_53); byte[] tmp62_41 = tmp41_20;tmp62_41[3] = 
/*  278: 410 */       ipv4WordToByte(ip, i + 1, ip.length());return tmp62_41;
/*  279:     */   }
/*  280:     */   
/*  281:     */   public static String intToIpAddress(int i)
/*  282:     */   {
/*  283: 418 */     StringBuilder buf = new StringBuilder(15);
/*  284: 419 */     buf.append(i >> 24 & 0xFF);
/*  285: 420 */     buf.append('.');
/*  286: 421 */     buf.append(i >> 16 & 0xFF);
/*  287: 422 */     buf.append('.');
/*  288: 423 */     buf.append(i >> 8 & 0xFF);
/*  289: 424 */     buf.append('.');
/*  290: 425 */     buf.append(i & 0xFF);
/*  291: 426 */     return buf.toString();
/*  292:     */   }
/*  293:     */   
/*  294:     */   public static String bytesToIpAddress(byte[] bytes)
/*  295:     */   {
/*  296: 436 */     return bytesToIpAddress(bytes, 0, bytes.length);
/*  297:     */   }
/*  298:     */   
/*  299:     */   public static String bytesToIpAddress(byte[] bytes, int offset, int length)
/*  300:     */   {
/*  301: 446 */     switch (length)
/*  302:     */     {
/*  303:     */     case 4: 
/*  304: 448 */       return 
/*  305:     */       
/*  306:     */ 
/*  307:     */ 
/*  308:     */ 
/*  309:     */ 
/*  310:     */ 
/*  311: 455 */         15 + (bytes[offset] & 0xFF) + '.' + (bytes[(offset + 1)] & 0xFF) + '.' + (bytes[(offset + 2)] & 0xFF) + '.' + (bytes[(offset + 3)] & 0xFF);
/*  312:     */     case 16: 
/*  313: 458 */       return toAddressString(bytes, offset, false);
/*  314:     */     }
/*  315: 460 */     throw new IllegalArgumentException("length: " + length + " (expected: 4 or 16)");
/*  316:     */   }
/*  317:     */   
/*  318:     */   public static boolean isValidIpV6Address(String ip)
/*  319:     */   {
/*  320: 465 */     int end = ip.length();
/*  321: 466 */     if (end < 2) {
/*  322: 467 */       return false;
/*  323:     */     }
/*  324: 472 */     char c = ip.charAt(0);
/*  325:     */     int start;
/*  326: 473 */     if (c == '[')
/*  327:     */     {
/*  328: 474 */       end--;
/*  329: 475 */       if (ip.charAt(end) != ']') {
/*  330: 477 */         return false;
/*  331:     */       }
/*  332: 479 */       int start = 1;
/*  333: 480 */       c = ip.charAt(1);
/*  334:     */     }
/*  335:     */     else
/*  336:     */     {
/*  337: 482 */       start = 0;
/*  338:     */     }
/*  339:     */     int colons;
/*  340:     */     int compressBegin;
/*  341: 487 */     if (c == ':')
/*  342:     */     {
/*  343: 489 */       if (ip.charAt(start + 1) != ':') {
/*  344: 490 */         return false;
/*  345:     */       }
/*  346: 492 */       int colons = 2;
/*  347: 493 */       int compressBegin = start;
/*  348: 494 */       start += 2;
/*  349:     */     }
/*  350:     */     else
/*  351:     */     {
/*  352: 496 */       colons = 0;
/*  353: 497 */       compressBegin = -1;
/*  354:     */     }
/*  355: 500 */     int wordLen = 0;
/*  356: 502 */     for (int i = start; i < end; i++)
/*  357:     */     {
/*  358: 503 */       c = ip.charAt(i);
/*  359: 504 */       if (isValidHexChar(c))
/*  360:     */       {
/*  361: 505 */         if (wordLen < 4) {
/*  362: 506 */           wordLen++;
/*  363:     */         } else {
/*  364: 509 */           return false;
/*  365:     */         }
/*  366:     */       }
/*  367:     */       else {
/*  368: 512 */         switch (c)
/*  369:     */         {
/*  370:     */         case ':': 
/*  371: 514 */           if (colons > 7) {
/*  372: 515 */             return false;
/*  373:     */           }
/*  374: 517 */           if (ip.charAt(i - 1) == ':')
/*  375:     */           {
/*  376: 518 */             if (compressBegin >= 0) {
/*  377: 519 */               return false;
/*  378:     */             }
/*  379: 521 */             compressBegin = i - 1;
/*  380:     */           }
/*  381:     */           else
/*  382:     */           {
/*  383: 523 */             wordLen = 0;
/*  384:     */           }
/*  385: 525 */           colons++;
/*  386: 526 */           break;
/*  387:     */         case '.': 
/*  388: 531 */           if (((compressBegin < 0) && (colons != 6)) || ((colons == 7) && (compressBegin >= start)) || (colons > 7)) {
/*  389: 535 */             return false;
/*  390:     */           }
/*  391: 541 */           int ipv4Start = i - wordLen;
/*  392: 542 */           int j = ipv4Start - 2;
/*  393: 543 */           if (isValidIPv4MappedChar(ip.charAt(j)))
/*  394:     */           {
/*  395: 544 */             if ((!isValidIPv4MappedChar(ip.charAt(j - 1))) || 
/*  396: 545 */               (!isValidIPv4MappedChar(ip.charAt(j - 2))) || 
/*  397: 546 */               (!isValidIPv4MappedChar(ip.charAt(j - 3)))) {
/*  398: 547 */               return false;
/*  399:     */             }
/*  400: 549 */             j -= 5;
/*  401:     */           }
/*  402: 552 */           for (; j >= start; j--)
/*  403:     */           {
/*  404: 553 */             char tmpChar = ip.charAt(j);
/*  405: 554 */             if ((tmpChar != '0') && (tmpChar != ':')) {
/*  406: 555 */               return false;
/*  407:     */             }
/*  408:     */           }
/*  409: 560 */           int ipv4End = ip.indexOf('%', ipv4Start + 7);
/*  410: 561 */           if (ipv4End < 0) {
/*  411: 562 */             ipv4End = end;
/*  412:     */           }
/*  413: 564 */           return isValidIpV4Address(ip, ipv4Start, ipv4End);
/*  414:     */         case '%': 
/*  415: 567 */           end = i;
/*  416: 568 */           break;
/*  417:     */         default: 
/*  418: 570 */           return false;
/*  419:     */         }
/*  420:     */       }
/*  421:     */     }
/*  422: 575 */     if (compressBegin < 0) {
/*  423: 576 */       return (colons == 7) && (wordLen > 0);
/*  424:     */     }
/*  425: 579 */     return (compressBegin + 2 == end) || ((wordLen > 0) && ((colons < 8) || (compressBegin <= start)));
/*  426:     */   }
/*  427:     */   
/*  428:     */   private static boolean isValidIpV4Word(CharSequence word, int from, int toExclusive)
/*  429:     */   {
/*  430: 585 */     int len = toExclusive - from;
/*  431:     */     char c0;
/*  432: 587 */     if ((len < 1) || (len > 3) || ((c0 = word.charAt(from)) < '0')) {
/*  433: 588 */       return false;
/*  434:     */     }
/*  435:     */     char c0;
/*  436: 590 */     if (len == 3)
/*  437:     */     {
/*  438:     */       char c1;
/*  439:     */       char c2;
/*  440: 591 */       return ((c1 = word.charAt(from + 1)) >= '0') && 
/*  441: 592 */         ((c2 = word.charAt(from + 2)) >= '0') && (((c0 <= '1') && (c1 <= '9') && (c2 <= '9')) || ((c0 == '2') && (c1 <= '5') && ((c2 <= '5') || ((c1 < '5') && (c2 <= '9')))));
/*  442:     */     }
/*  443: 596 */     return (c0 <= '9') && ((len == 1) || (isValidNumericChar(word.charAt(from + 1))));
/*  444:     */   }
/*  445:     */   
/*  446:     */   private static boolean isValidHexChar(char c)
/*  447:     */   {
/*  448: 600 */     return ((c >= '0') && (c <= '9')) || ((c >= 'A') && (c <= 'F')) || ((c >= 'a') && (c <= 'f'));
/*  449:     */   }
/*  450:     */   
/*  451:     */   private static boolean isValidNumericChar(char c)
/*  452:     */   {
/*  453: 604 */     return (c >= '0') && (c <= '9');
/*  454:     */   }
/*  455:     */   
/*  456:     */   private static boolean isValidIPv4MappedChar(char c)
/*  457:     */   {
/*  458: 608 */     return (c == 'f') || (c == 'F');
/*  459:     */   }
/*  460:     */   
/*  461:     */   private static boolean isValidIPv4MappedSeparators(byte b0, byte b1, boolean mustBeZero)
/*  462:     */   {
/*  463: 615 */     return (b0 == b1) && ((b0 == 0) || ((!mustBeZero) && (b1 == -1)));
/*  464:     */   }
/*  465:     */   
/*  466:     */   private static boolean isValidIPv4Mapped(byte[] bytes, int currentIndex, int compressBegin, int compressLength)
/*  467:     */   {
/*  468: 619 */     boolean mustBeZero = compressBegin + compressLength >= 14;
/*  469: 620 */     return (currentIndex <= 12) && (currentIndex >= 2) && ((!mustBeZero) || (compressBegin < 12)) && 
/*  470: 621 */       (isValidIPv4MappedSeparators(bytes[(currentIndex - 1)], bytes[(currentIndex - 2)], mustBeZero)) && 
/*  471: 622 */       (PlatformDependent.isZero(bytes, 0, currentIndex - 3));
/*  472:     */   }
/*  473:     */   
/*  474:     */   public static boolean isValidIpV4Address(String ip)
/*  475:     */   {
/*  476: 632 */     return isValidIpV4Address(ip, 0, ip.length());
/*  477:     */   }
/*  478:     */   
/*  479:     */   private static boolean isValidIpV4Address(String ip, int from, int toExcluded)
/*  480:     */   {
/*  481: 637 */     int len = toExcluded - from;
/*  482:     */     int i;
/*  483: 639 */     return (len <= 15) && (len >= 7) && 
/*  484: 640 */       ((i = ip.indexOf('.', from + 1)) > 0) && (isValidIpV4Word(ip, from, i)) && 
/*  485: 641 */       ((i = ip.indexOf('.', from = i + 2)) > 0) && (isValidIpV4Word(ip, from - 1, i)) && 
/*  486: 642 */       ((i = ip.indexOf('.', from = i + 2)) > 0) && (isValidIpV4Word(ip, from - 1, i)) && 
/*  487: 643 */       (isValidIpV4Word(ip, i + 1, toExcluded));
/*  488:     */   }
/*  489:     */   
/*  490:     */   public static Inet6Address getByName(CharSequence ip)
/*  491:     */   {
/*  492: 654 */     return getByName(ip, true);
/*  493:     */   }
/*  494:     */   
/*  495:     */   public static Inet6Address getByName(CharSequence ip, boolean ipv4Mapped)
/*  496:     */   {
/*  497: 672 */     byte[] bytes = getIPv6ByName(ip, ipv4Mapped);
/*  498: 673 */     if (bytes == null) {
/*  499: 674 */       return null;
/*  500:     */     }
/*  501:     */     try
/*  502:     */     {
/*  503: 677 */       return Inet6Address.getByAddress(null, bytes, -1);
/*  504:     */     }
/*  505:     */     catch (UnknownHostException e)
/*  506:     */     {
/*  507: 679 */       throw new RuntimeException(e);
/*  508:     */     }
/*  509:     */   }
/*  510:     */   
/*  511:     */   private static byte[] getIPv6ByName(CharSequence ip, boolean ipv4Mapped)
/*  512:     */   {
/*  513: 698 */     byte[] bytes = new byte[16];
/*  514: 699 */     int ipLength = ip.length();
/*  515: 700 */     int compressBegin = 0;
/*  516: 701 */     int compressLength = 0;
/*  517: 702 */     int currentIndex = 0;
/*  518: 703 */     int value = 0;
/*  519: 704 */     int begin = -1;
/*  520: 705 */     int i = 0;
/*  521: 706 */     int ipv6Separators = 0;
/*  522: 707 */     int ipv4Separators = 0;
/*  523:     */     
/*  524: 709 */     boolean needsShift = false;
/*  525: 710 */     for (; i < ipLength; i++)
/*  526:     */     {
/*  527: 711 */       char c = ip.charAt(i);
/*  528: 712 */       switch (c)
/*  529:     */       {
/*  530:     */       case ':': 
/*  531: 714 */         ipv6Separators++;
/*  532: 715 */         if ((i - begin > 4) || (ipv4Separators > 0) || (ipv6Separators > 8) || (currentIndex + 1 >= bytes.length)) {
/*  533: 718 */           return null;
/*  534:     */         }
/*  535: 720 */         value <<= 4 - (i - begin) << 2;
/*  536: 722 */         if (compressLength > 0) {
/*  537: 723 */           compressLength -= 2;
/*  538:     */         }
/*  539: 729 */         bytes[(currentIndex++)] = ((byte)((value & 0xF) << 4 | value >> 4 & 0xF));
/*  540: 730 */         bytes[(currentIndex++)] = ((byte)((value >> 8 & 0xF) << 4 | value >> 12 & 0xF));
/*  541: 731 */         int tmp = i + 1;
/*  542: 732 */         if ((tmp < ipLength) && (ip.charAt(tmp) == ':'))
/*  543:     */         {
/*  544: 733 */           tmp++;
/*  545: 734 */           if ((compressBegin != 0) || ((tmp < ipLength) && (ip.charAt(tmp) == ':'))) {
/*  546: 735 */             return null;
/*  547:     */           }
/*  548: 737 */           ipv6Separators++;
/*  549: 738 */           needsShift = (ipv6Separators == 2) && (value == 0);
/*  550: 739 */           compressBegin = currentIndex;
/*  551: 740 */           compressLength = bytes.length - compressBegin - 2;
/*  552: 741 */           i++;
/*  553:     */         }
/*  554: 743 */         value = 0;
/*  555: 744 */         begin = -1;
/*  556: 745 */         break;
/*  557:     */       case '.': 
/*  558: 747 */         ipv4Separators++;
/*  559: 748 */         int tmp = i - begin;
/*  560: 749 */         if ((tmp > 3) || (begin < 0) || (ipv4Separators > 3) || ((ipv6Separators > 0) && (currentIndex + compressLength < 12)) || (i + 1 >= ipLength) || (currentIndex >= bytes.length) || ((ipv4Separators == 1) && ((!ipv4Mapped) || ((currentIndex != 0) && 
/*  561:     */         
/*  562:     */ 
/*  563:     */ 
/*  564:     */ 
/*  565:     */ 
/*  566:     */ 
/*  567:     */ 
/*  568: 757 */           (!isValidIPv4Mapped(bytes, currentIndex, compressBegin, compressLength))) || ((tmp == 3) && (
/*  569:     */           
/*  570: 759 */           (!isValidNumericChar(ip.charAt(i - 1))) || 
/*  571: 760 */           (!isValidNumericChar(ip.charAt(i - 2))) || 
/*  572: 761 */           (!isValidNumericChar(ip.charAt(i - 3))))) || ((tmp == 2) && (
/*  573: 762 */           (!isValidNumericChar(ip.charAt(i - 1))) || 
/*  574: 763 */           (!isValidNumericChar(ip.charAt(i - 2))))) || ((tmp == 1) && 
/*  575: 764 */           (!isValidNumericChar(ip.charAt(i - 1))))))) {
/*  576: 765 */           return null;
/*  577:     */         }
/*  578: 767 */         value <<= 3 - tmp << 2;
/*  579:     */         
/*  580:     */ 
/*  581:     */ 
/*  582:     */ 
/*  583: 772 */         begin = (value & 0xF) * 100 + (value >> 4 & 0xF) * 10 + (value >> 8 & 0xF);
/*  584: 773 */         if ((begin < 0) || (begin > 255)) {
/*  585: 774 */           return null;
/*  586:     */         }
/*  587: 776 */         bytes[(currentIndex++)] = ((byte)begin);
/*  588: 777 */         value = 0;
/*  589: 778 */         begin = -1;
/*  590: 779 */         break;
/*  591:     */       default: 
/*  592: 781 */         if ((!isValidHexChar(c)) || ((ipv4Separators > 0) && (!isValidNumericChar(c)))) {
/*  593: 782 */           return null;
/*  594:     */         }
/*  595: 784 */         if (begin < 0) {
/*  596: 785 */           begin = i;
/*  597: 786 */         } else if (i - begin > 4) {
/*  598: 787 */           return null;
/*  599:     */         }
/*  600: 793 */         value += (StringUtil.decodeHexNibble(c) << (i - begin << 2));
/*  601:     */       }
/*  602:     */     }
/*  603: 798 */     boolean isCompressed = compressBegin > 0;
/*  604: 800 */     if (ipv4Separators > 0)
/*  605:     */     {
/*  606: 801 */       if (((begin > 0) && (i - begin > 3)) || (ipv4Separators != 3) || (currentIndex >= bytes.length)) {
/*  607: 804 */         return null;
/*  608:     */       }
/*  609: 806 */       if (ipv6Separators == 0)
/*  610:     */       {
/*  611: 807 */         compressLength = 12;
/*  612:     */       }
/*  613:     */       else
/*  614:     */       {
/*  615: 808 */         if (ipv6Separators >= 2) {
/*  616: 808 */           if ((isCompressed) || (ipv6Separators != 6) || 
/*  617: 809 */             (ip.charAt(0) == ':'))
/*  618:     */           {
/*  619: 809 */             if ((isCompressed) && (ipv6Separators < 8)) {
/*  620: 811 */               if ((ip.charAt(0) == ':') && (compressBegin > 2)) {}
/*  621:     */             }
/*  622:     */           }
/*  623:     */           else
/*  624:     */           {
/*  625: 812 */             compressLength -= 2;
/*  626:     */             break label763;
/*  627:     */           }
/*  628:     */         }
/*  629: 814 */         return null;
/*  630:     */       }
/*  631:     */       label763:
/*  632: 816 */       value <<= 3 - (i - begin) << 2;
/*  633: 821 */       begin = (value & 0xF) * 100 + (value >> 4 & 0xF) * 10 + (value >> 8 & 0xF);
/*  634: 822 */       if ((begin < 0) || (begin > 255)) {
/*  635: 823 */         return null;
/*  636:     */       }
/*  637: 825 */       bytes[(currentIndex++)] = ((byte)begin);
/*  638:     */     }
/*  639:     */     else
/*  640:     */     {
/*  641: 827 */       int tmp = ipLength - 1;
/*  642: 828 */       if (((begin > 0) && (i - begin > 4)) || (ipv6Separators < 2) || ((!isCompressed) && ((ipv6Separators + 1 != 8) || 
/*  643:     */       
/*  644:     */ 
/*  645: 831 */         (ip.charAt(0) == ':') || (ip.charAt(tmp) == ':'))) || ((isCompressed) && ((ipv6Separators > 8) || ((ipv6Separators == 8) && (((compressBegin <= 2) && 
/*  646:     */         
/*  647:     */ 
/*  648: 834 */         (ip.charAt(0) != ':')) || ((compressBegin >= 14) && 
/*  649: 835 */         (ip.charAt(tmp) != ':')))))) || (currentIndex + 1 >= bytes.length) || ((begin < 0) && 
/*  650:     */         
/*  651: 837 */         (ip.charAt(tmp - 1) != ':')) || ((compressBegin > 2) && 
/*  652: 838 */         (ip.charAt(0) == ':'))) {
/*  653: 839 */         return null;
/*  654:     */       }
/*  655: 841 */       if ((begin >= 0) && (i - begin <= 4)) {
/*  656: 842 */         value <<= 4 - (i - begin) << 2;
/*  657:     */       }
/*  658: 847 */       bytes[(currentIndex++)] = ((byte)((value & 0xF) << 4 | value >> 4 & 0xF));
/*  659: 848 */       bytes[(currentIndex++)] = ((byte)((value >> 8 & 0xF) << 4 | value >> 12 & 0xF));
/*  660:     */     }
/*  661: 851 */     i = currentIndex + compressLength;
/*  662: 852 */     if ((needsShift) || (i >= bytes.length))
/*  663:     */     {
/*  664: 854 */       if (i >= bytes.length) {
/*  665: 855 */         compressBegin++;
/*  666:     */       }
/*  667: 857 */       for (i = currentIndex; i < bytes.length;)
/*  668:     */       {
/*  669: 858 */         for (begin = bytes.length - 1; begin >= compressBegin; begin--) {
/*  670: 859 */           bytes[begin] = bytes[(begin - 1)];
/*  671:     */         }
/*  672: 861 */         bytes[begin] = 0;
/*  673: 862 */         compressBegin++;i++; continue;
/*  674: 866 */         for (i = 0; i < compressLength; i++)
/*  675:     */         {
/*  676: 867 */           begin = i + compressBegin;
/*  677: 868 */           currentIndex = begin + compressLength;
/*  678: 869 */           if (currentIndex >= bytes.length) {
/*  679:     */             break;
/*  680:     */           }
/*  681: 870 */           bytes[currentIndex] = bytes[begin];
/*  682: 871 */           bytes[begin] = 0;
/*  683:     */         }
/*  684:     */       }
/*  685:     */     }
/*  686: 878 */     if (ipv4Separators > 0)
/*  687:     */     {
/*  688: 882 */       byte tmp1228_1227 = -1;bytes[11] = tmp1228_1227;bytes[10] = tmp1228_1227;
/*  689:     */     }
/*  690: 885 */     return bytes;
/*  691:     */   }
/*  692:     */   
/*  693:     */   public static String toSocketAddressString(InetSocketAddress addr)
/*  694:     */   {
/*  695: 896 */     String port = String.valueOf(addr.getPort());
/*  696:     */     StringBuilder sb;
/*  697:     */     StringBuilder sb;
/*  698: 899 */     if (addr.isUnresolved())
/*  699:     */     {
/*  700: 900 */       String hostString = PlatformDependent.javaVersion() >= 7 ? addr.getHostString() : addr.getHostName();
/*  701: 901 */       sb = newSocketAddressStringBuilder(hostString, port, !isValidIpV6Address(hostString));
/*  702:     */     }
/*  703:     */     else
/*  704:     */     {
/*  705: 903 */       InetAddress address = addr.getAddress();
/*  706: 904 */       String hostString = toAddressString(address);
/*  707: 905 */       sb = newSocketAddressStringBuilder(hostString, port, address instanceof Inet4Address);
/*  708:     */     }
/*  709: 907 */     return ':' + port;
/*  710:     */   }
/*  711:     */   
/*  712:     */   public static String toSocketAddressString(String host, int port)
/*  713:     */   {
/*  714: 914 */     String portStr = String.valueOf(port);
/*  715: 915 */     return ':' + 
/*  716: 916 */       portStr;
/*  717:     */   }
/*  718:     */   
/*  719:     */   private static StringBuilder newSocketAddressStringBuilder(String host, String port, boolean ipv4)
/*  720:     */   {
/*  721: 920 */     int hostLen = host.length();
/*  722: 921 */     if (ipv4) {
/*  723: 923 */       return new StringBuilder(hostLen + 1 + port.length()).append(host);
/*  724:     */     }
/*  725: 926 */     StringBuilder stringBuilder = new StringBuilder(hostLen + 3 + port.length());
/*  726: 927 */     if ((hostLen > 1) && (host.charAt(0) == '[') && (host.charAt(hostLen - 1) == ']')) {
/*  727: 928 */       return stringBuilder.append(host);
/*  728:     */     }
/*  729: 930 */     return stringBuilder.append('[').append(host).append(']');
/*  730:     */   }
/*  731:     */   
/*  732:     */   public static String toAddressString(InetAddress ip)
/*  733:     */   {
/*  734: 946 */     return toAddressString(ip, false);
/*  735:     */   }
/*  736:     */   
/*  737:     */   public static String toAddressString(InetAddress ip, boolean ipv4Mapped)
/*  738:     */   {
/*  739: 974 */     if ((ip instanceof Inet4Address)) {
/*  740: 975 */       return ip.getHostAddress();
/*  741:     */     }
/*  742: 977 */     if (!(ip instanceof Inet6Address)) {
/*  743: 978 */       throw new IllegalArgumentException("Unhandled type: " + ip);
/*  744:     */     }
/*  745: 981 */     return toAddressString(ip.getAddress(), 0, ipv4Mapped);
/*  746:     */   }
/*  747:     */   
/*  748:     */   private static String toAddressString(byte[] bytes, int offset, boolean ipv4Mapped)
/*  749:     */   {
/*  750: 985 */     int[] words = new int[8];
/*  751:     */     
/*  752: 987 */     int end = offset + words.length;
/*  753: 988 */     for (int i = offset; i < end; i++) {
/*  754: 989 */       words[i] = ((bytes[(i << 1)] & 0xFF) << 8 | bytes[((i << 1) + 1)] & 0xFF);
/*  755:     */     }
/*  756: 993 */     int currentStart = -1;
/*  757:     */     
/*  758: 995 */     int shortestStart = -1;
/*  759: 996 */     int shortestLength = 0;
/*  760: 997 */     for (i = 0; i < words.length; i++) {
/*  761: 998 */       if (words[i] == 0)
/*  762:     */       {
/*  763: 999 */         if (currentStart < 0) {
/*  764:1000 */           currentStart = i;
/*  765:     */         }
/*  766:     */       }
/*  767:1002 */       else if (currentStart >= 0)
/*  768:     */       {
/*  769:1003 */         int currentLength = i - currentStart;
/*  770:1004 */         if (currentLength > shortestLength)
/*  771:     */         {
/*  772:1005 */           shortestStart = currentStart;
/*  773:1006 */           shortestLength = currentLength;
/*  774:     */         }
/*  775:1008 */         currentStart = -1;
/*  776:     */       }
/*  777:     */     }
/*  778:1012 */     if (currentStart >= 0)
/*  779:     */     {
/*  780:1013 */       int currentLength = i - currentStart;
/*  781:1014 */       if (currentLength > shortestLength)
/*  782:     */       {
/*  783:1015 */         shortestStart = currentStart;
/*  784:1016 */         shortestLength = currentLength;
/*  785:     */       }
/*  786:     */     }
/*  787:1020 */     if (shortestLength == 1)
/*  788:     */     {
/*  789:1021 */       shortestLength = 0;
/*  790:1022 */       shortestStart = -1;
/*  791:     */     }
/*  792:1026 */     int shortestEnd = shortestStart + shortestLength;
/*  793:1027 */     StringBuilder b = new StringBuilder(39);
/*  794:1028 */     if (shortestEnd < 0)
/*  795:     */     {
/*  796:1029 */       b.append(Integer.toHexString(words[0]));
/*  797:1030 */       for (i = 1; i < words.length; i++)
/*  798:     */       {
/*  799:1031 */         b.append(':');
/*  800:1032 */         b.append(Integer.toHexString(words[i]));
/*  801:     */       }
/*  802:     */     }
/*  803:     */     boolean isIpv4Mapped;
/*  804:     */     boolean isIpv4Mapped;
/*  805:1037 */     if (inRangeEndExclusive(0, shortestStart, shortestEnd))
/*  806:     */     {
/*  807:1038 */       b.append("::");
/*  808:1039 */       isIpv4Mapped = (ipv4Mapped) && (shortestEnd == 5) && (words[5] == 65535);
/*  809:     */     }
/*  810:     */     else
/*  811:     */     {
/*  812:1041 */       b.append(Integer.toHexString(words[0]));
/*  813:1042 */       isIpv4Mapped = false;
/*  814:     */     }
/*  815:1044 */     for (i = 1; i < words.length; i++) {
/*  816:1045 */       if (!inRangeEndExclusive(i, shortestStart, shortestEnd))
/*  817:     */       {
/*  818:1046 */         if (!inRangeEndExclusive(i - 1, shortestStart, shortestEnd)) {
/*  819:1048 */           if ((!isIpv4Mapped) || (i == 6)) {
/*  820:1049 */             b.append(':');
/*  821:     */           } else {
/*  822:1051 */             b.append('.');
/*  823:     */           }
/*  824:     */         }
/*  825:1054 */         if ((isIpv4Mapped) && (i > 5))
/*  826:     */         {
/*  827:1055 */           b.append(words[i] >> 8);
/*  828:1056 */           b.append('.');
/*  829:1057 */           b.append(words[i] & 0xFF);
/*  830:     */         }
/*  831:     */         else
/*  832:     */         {
/*  833:1059 */           b.append(Integer.toHexString(words[i]));
/*  834:     */         }
/*  835:     */       }
/*  836:1061 */       else if (!inRangeEndExclusive(i - 1, shortestStart, shortestEnd))
/*  837:     */       {
/*  838:1063 */         b.append("::");
/*  839:     */       }
/*  840:     */     }
/*  841:1068 */     return b.toString();
/*  842:     */   }
/*  843:     */   
/*  844:     */   private static boolean inRangeEndExclusive(int value, int start, int end)
/*  845:     */   {
/*  846:1083 */     return (value >= start) && (value < end);
/*  847:     */   }
/*  848:     */   
/*  849:     */   /* Error */
/*  850:     */   private static Integer sysctlGetInt(String sysctlKey)
/*  851:     */     throws java.io.IOException
/*  852:     */   {
/*  853:     */     // Byte code:
/*  854:     */     //   0: new 42	java/lang/ProcessBuilder
/*  855:     */     //   3: dup
/*  856:     */     //   4: iconst_2
/*  857:     */     //   5: anewarray 44	java/lang/String
/*  858:     */     //   8: dup
/*  859:     */     //   9: iconst_0
/*  860:     */     //   10: ldc 46
/*  861:     */     //   12: aastore
/*  862:     */     //   13: dup
/*  863:     */     //   14: iconst_1
/*  864:     */     //   15: aload_0
/*  865:     */     //   16: aastore
/*  866:     */     //   17: invokespecial 50	java/lang/ProcessBuilder:<init>	([Ljava/lang/String;)V
/*  867:     */     //   20: invokevirtual 54	java/lang/ProcessBuilder:start	()Ljava/lang/Process;
/*  868:     */     //   23: astore_1
/*  869:     */     //   24: aload_1
/*  870:     */     //   25: invokevirtual 60	java/lang/Process:getInputStream	()Ljava/io/InputStream;
/*  871:     */     //   28: astore_2
/*  872:     */     //   29: new 62	java/io/InputStreamReader
/*  873:     */     //   32: dup
/*  874:     */     //   33: aload_2
/*  875:     */     //   34: invokespecial 65	java/io/InputStreamReader:<init>	(Ljava/io/InputStream;)V
/*  876:     */     //   37: astore_3
/*  877:     */     //   38: new 67	java/io/BufferedReader
/*  878:     */     //   41: dup
/*  879:     */     //   42: aload_3
/*  880:     */     //   43: invokespecial 70	java/io/BufferedReader:<init>	(Ljava/io/Reader;)V
/*  881:     */     //   46: astore 4
/*  882:     */     //   48: aload 4
/*  883:     */     //   50: invokevirtual 74	java/io/BufferedReader:readLine	()Ljava/lang/String;
/*  884:     */     //   53: astore 5
/*  885:     */     //   55: aload 5
/*  886:     */     //   57: aload_0
/*  887:     */     //   58: invokevirtual 78	java/lang/String:startsWith	(Ljava/lang/String;)Z
/*  888:     */     //   61: ifeq +75 -> 136
/*  889:     */     //   64: aload 5
/*  890:     */     //   66: invokevirtual 82	java/lang/String:length	()I
/*  891:     */     //   69: iconst_1
/*  892:     */     //   70: isub
/*  893:     */     //   71: istore 6
/*  894:     */     //   73: iload 6
/*  895:     */     //   75: aload_0
/*  896:     */     //   76: invokevirtual 82	java/lang/String:length	()I
/*  897:     */     //   79: if_icmple +57 -> 136
/*  898:     */     //   82: aload 5
/*  899:     */     //   84: iload 6
/*  900:     */     //   86: invokevirtual 88	java/lang/String:charAt	(I)C
/*  901:     */     //   89: invokestatic 94	java/lang/Character:isDigit	(C)Z
/*  902:     */     //   92: ifne +38 -> 130
/*  903:     */     //   95: aload 5
/*  904:     */     //   97: iload 6
/*  905:     */     //   99: iconst_1
/*  906:     */     //   100: iadd
/*  907:     */     //   101: aload 5
/*  908:     */     //   103: invokevirtual 82	java/lang/String:length	()I
/*  909:     */     //   106: invokevirtual 98	java/lang/String:substring	(II)Ljava/lang/String;
/*  910:     */     //   109: invokestatic 103	java/lang/Integer:valueOf	(Ljava/lang/String;)Ljava/lang/Integer;
/*  911:     */     //   112: astore 7
/*  912:     */     //   114: aload 4
/*  913:     */     //   116: invokevirtual 107	java/io/BufferedReader:close	()V
/*  914:     */     //   119: aload_1
/*  915:     */     //   120: ifnull +7 -> 127
/*  916:     */     //   123: aload_1
/*  917:     */     //   124: invokevirtual 110	java/lang/Process:destroy	()V
/*  918:     */     //   127: aload 7
/*  919:     */     //   129: areturn
/*  920:     */     //   130: iinc 6 255
/*  921:     */     //   133: goto -60 -> 73
/*  922:     */     //   136: aconst_null
/*  923:     */     //   137: astore 6
/*  924:     */     //   139: aload 4
/*  925:     */     //   141: invokevirtual 107	java/io/BufferedReader:close	()V
/*  926:     */     //   144: aload_1
/*  927:     */     //   145: ifnull +7 -> 152
/*  928:     */     //   148: aload_1
/*  929:     */     //   149: invokevirtual 110	java/lang/Process:destroy	()V
/*  930:     */     //   152: aload 6
/*  931:     */     //   154: areturn
/*  932:     */     //   155: astore 8
/*  933:     */     //   157: aload 4
/*  934:     */     //   159: invokevirtual 107	java/io/BufferedReader:close	()V
/*  935:     */     //   162: aload 8
/*  936:     */     //   164: athrow
/*  937:     */     //   165: astore 9
/*  938:     */     //   167: aload_1
/*  939:     */     //   168: ifnull +7 -> 175
/*  940:     */     //   171: aload_1
/*  941:     */     //   172: invokevirtual 110	java/lang/Process:destroy	()V
/*  942:     */     //   175: aload 9
/*  943:     */     //   177: athrow
/*  944:     */     // Line number table:
/*  945:     */     //   Java source line #314	-> byte code offset #0
/*  946:     */     //   Java source line #316	-> byte code offset #24
/*  947:     */     //   Java source line #317	-> byte code offset #29
/*  948:     */     //   Java source line #318	-> byte code offset #38
/*  949:     */     //   Java source line #320	-> byte code offset #48
/*  950:     */     //   Java source line #321	-> byte code offset #55
/*  951:     */     //   Java source line #322	-> byte code offset #64
/*  952:     */     //   Java source line #323	-> byte code offset #82
/*  953:     */     //   Java source line #324	-> byte code offset #95
/*  954:     */     //   Java source line #330	-> byte code offset #114
/*  955:     */     //   Java source line #333	-> byte code offset #119
/*  956:     */     //   Java source line #334	-> byte code offset #123
/*  957:     */     //   Java source line #324	-> byte code offset #127
/*  958:     */     //   Java source line #322	-> byte code offset #130
/*  959:     */     //   Java source line #328	-> byte code offset #136
/*  960:     */     //   Java source line #330	-> byte code offset #139
/*  961:     */     //   Java source line #333	-> byte code offset #144
/*  962:     */     //   Java source line #334	-> byte code offset #148
/*  963:     */     //   Java source line #328	-> byte code offset #152
/*  964:     */     //   Java source line #330	-> byte code offset #155
/*  965:     */     //   Java source line #333	-> byte code offset #165
/*  966:     */     //   Java source line #334	-> byte code offset #171
/*  967:     */     // Local variable table:
/*  968:     */     //   start	length	slot	name	signature
/*  969:     */     //   0	178	0	sysctlKey	String
/*  970:     */     //   23	149	1	process	java.lang.Process
/*  971:     */     //   28	6	2	is	java.io.InputStream
/*  972:     */     //   37	6	3	isr	java.io.InputStreamReader
/*  973:     */     //   46	112	4	br	BufferedReader
/*  974:     */     //   53	49	5	line	String
/*  975:     */     //   71	82	6	i	int
/*  976:     */     //   112	16	7	localInteger	Integer
/*  977:     */     //   155	8	8	localObject1	Object
/*  978:     */     //   165	11	9	localObject2	Object
/*  979:     */     // Exception table:
/*  980:     */     //   from	to	target	type
/*  981:     */     //   48	114	155	finally
/*  982:     */     //   130	139	155	finally
/*  983:     */     //   155	157	155	finally
/*  984:     */     //   24	119	165	finally
/*  985:     */     //   130	144	165	finally
/*  986:     */     //   155	167	165	finally
/*  987:     */   }
/*  988:     */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.NetUtil
 * JD-Core Version:    0.7.0.1
 */