/*   1:    */ package io.netty.resolver;
/*   2:    */ 
/*   3:    */ import io.netty.util.NetUtil;
/*   4:    */ import io.netty.util.internal.ObjectUtil;
/*   5:    */ import io.netty.util.internal.PlatformDependent;
/*   6:    */ import io.netty.util.internal.logging.InternalLogger;
/*   7:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   8:    */ import java.io.BufferedReader;
/*   9:    */ import java.io.File;
/*  10:    */ import java.io.FileReader;
/*  11:    */ import java.io.IOException;
/*  12:    */ import java.io.Reader;
/*  13:    */ import java.net.Inet4Address;
/*  14:    */ import java.net.Inet6Address;
/*  15:    */ import java.net.InetAddress;
/*  16:    */ import java.util.ArrayList;
/*  17:    */ import java.util.HashMap;
/*  18:    */ import java.util.List;
/*  19:    */ import java.util.Locale;
/*  20:    */ import java.util.Map;
/*  21:    */ import java.util.regex.Pattern;
/*  22:    */ 
/*  23:    */ public final class HostsFileParser
/*  24:    */ {
/*  25:    */   private static final String WINDOWS_DEFAULT_SYSTEM_ROOT = "C:\\Windows";
/*  26:    */   private static final String WINDOWS_HOSTS_FILE_RELATIVE_PATH = "\\system32\\drivers\\etc\\hosts";
/*  27:    */   private static final String X_PLATFORMS_HOSTS_FILE_PATH = "/etc/hosts";
/*  28: 51 */   private static final Pattern WHITESPACES = Pattern.compile("[ \t]+");
/*  29: 53 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(HostsFileParser.class);
/*  30:    */   
/*  31:    */   private static File locateHostsFile()
/*  32:    */   {
/*  33:    */     File hostsFile;
/*  34: 57 */     if (PlatformDependent.isWindows())
/*  35:    */     {
/*  36: 58 */       File hostsFile = new File(System.getenv("SystemRoot") + "\\system32\\drivers\\etc\\hosts");
/*  37: 59 */       if (!hostsFile.exists()) {
/*  38: 60 */         hostsFile = new File("C:\\Windows\\system32\\drivers\\etc\\hosts");
/*  39:    */       }
/*  40:    */     }
/*  41:    */     else
/*  42:    */     {
/*  43: 63 */       hostsFile = new File("/etc/hosts");
/*  44:    */     }
/*  45: 65 */     return hostsFile;
/*  46:    */   }
/*  47:    */   
/*  48:    */   public static HostsFileEntries parseSilently()
/*  49:    */   {
/*  50: 74 */     File hostsFile = locateHostsFile();
/*  51:    */     try
/*  52:    */     {
/*  53: 76 */       return parse(hostsFile);
/*  54:    */     }
/*  55:    */     catch (IOException e)
/*  56:    */     {
/*  57: 78 */       logger.warn("Failed to load and parse hosts file at " + hostsFile.getPath(), e);
/*  58:    */     }
/*  59: 79 */     return HostsFileEntries.EMPTY;
/*  60:    */   }
/*  61:    */   
/*  62:    */   public static HostsFileEntries parse()
/*  63:    */     throws IOException
/*  64:    */   {
/*  65: 90 */     return parse(locateHostsFile());
/*  66:    */   }
/*  67:    */   
/*  68:    */   public static HostsFileEntries parse(File file)
/*  69:    */     throws IOException
/*  70:    */   {
/*  71:101 */     ObjectUtil.checkNotNull(file, "file");
/*  72:102 */     if ((file.exists()) && (file.isFile())) {
/*  73:103 */       return parse(new BufferedReader(new FileReader(file)));
/*  74:    */     }
/*  75:105 */     return HostsFileEntries.EMPTY;
/*  76:    */   }
/*  77:    */   
/*  78:    */   public static HostsFileEntries parse(Reader reader)
/*  79:    */     throws IOException
/*  80:    */   {
/*  81:117 */     ObjectUtil.checkNotNull(reader, "reader");
/*  82:118 */     BufferedReader buff = new BufferedReader(reader);
/*  83:    */     try
/*  84:    */     {
/*  85:120 */       Map<String, Inet4Address> ipv4Entries = new HashMap();
/*  86:121 */       Map<String, Inet6Address> ipv6Entries = new HashMap();
/*  87:    */       String line;
/*  88:    */       int commentPosition;
/*  89:123 */       while ((line = buff.readLine()) != null)
/*  90:    */       {
/*  91:125 */         commentPosition = line.indexOf('#');
/*  92:126 */         if (commentPosition != -1) {
/*  93:127 */           line = line.substring(0, commentPosition);
/*  94:    */         }
/*  95:130 */         line = line.trim();
/*  96:131 */         if (!line.isEmpty())
/*  97:    */         {
/*  98:136 */           List<String> lineParts = new ArrayList();
/*  99:137 */           for (String s : WHITESPACES.split(line)) {
/* 100:138 */             if (!s.isEmpty()) {
/* 101:139 */               lineParts.add(s);
/* 102:    */             }
/* 103:    */           }
/* 104:144 */           if (lineParts.size() >= 2)
/* 105:    */           {
/* 106:149 */             byte[] ipBytes = NetUtil.createByteArrayFromIpAddressString((String)lineParts.get(0));
/* 107:151 */             if (ipBytes != null) {
/* 108:157 */               for (int i = 1; i < lineParts.size(); i++)
/* 109:    */               {
/* 110:158 */                 String hostname = (String)lineParts.get(i);
/* 111:159 */                 String hostnameLower = hostname.toLowerCase(Locale.ENGLISH);
/* 112:160 */                 InetAddress address = InetAddress.getByAddress(hostname, ipBytes);
/* 113:161 */                 if ((address instanceof Inet4Address))
/* 114:    */                 {
/* 115:162 */                   Inet4Address previous = (Inet4Address)ipv4Entries.put(hostnameLower, (Inet4Address)address);
/* 116:163 */                   if (previous != null) {
/* 117:165 */                     ipv4Entries.put(hostnameLower, previous);
/* 118:    */                   }
/* 119:    */                 }
/* 120:    */                 else
/* 121:    */                 {
/* 122:168 */                   Inet6Address previous = (Inet6Address)ipv6Entries.put(hostnameLower, (Inet6Address)address);
/* 123:169 */                   if (previous != null) {
/* 124:171 */                     ipv6Entries.put(hostnameLower, previous);
/* 125:    */                   }
/* 126:    */                 }
/* 127:    */               }
/* 128:    */             }
/* 129:    */           }
/* 130:    */         }
/* 131:    */       }
/* 132:176 */       return (ipv4Entries.isEmpty()) && (ipv6Entries.isEmpty()) ? HostsFileEntries.EMPTY : new HostsFileEntries(ipv4Entries, ipv6Entries);
/* 133:    */     }
/* 134:    */     finally
/* 135:    */     {
/* 136:    */       try
/* 137:    */       {
/* 138:181 */         buff.close();
/* 139:    */       }
/* 140:    */       catch (IOException e)
/* 141:    */       {
/* 142:183 */         logger.warn("Failed to close a reader", e);
/* 143:    */       }
/* 144:    */     }
/* 145:    */   }
/* 146:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.resolver.HostsFileParser
 * JD-Core Version:    0.7.0.1
 */