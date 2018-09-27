/*   1:    */ package io.netty.handler.ipfilter;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.SocketUtils;
/*   4:    */ import java.math.BigInteger;
/*   5:    */ import java.net.Inet4Address;
/*   6:    */ import java.net.Inet6Address;
/*   7:    */ import java.net.InetAddress;
/*   8:    */ import java.net.InetSocketAddress;
/*   9:    */ import java.net.UnknownHostException;
/*  10:    */ 
/*  11:    */ public final class IpSubnetFilterRule
/*  12:    */   implements IpFilterRule
/*  13:    */ {
/*  14:    */   private final IpFilterRule filterRule;
/*  15:    */   
/*  16:    */   public IpSubnetFilterRule(String ipAddress, int cidrPrefix, IpFilterRuleType ruleType)
/*  17:    */   {
/*  18:    */     try
/*  19:    */     {
/*  20: 37 */       this.filterRule = selectFilterRule(SocketUtils.addressByName(ipAddress), cidrPrefix, ruleType);
/*  21:    */     }
/*  22:    */     catch (UnknownHostException e)
/*  23:    */     {
/*  24: 39 */       throw new IllegalArgumentException("ipAddress", e);
/*  25:    */     }
/*  26:    */   }
/*  27:    */   
/*  28:    */   public IpSubnetFilterRule(InetAddress ipAddress, int cidrPrefix, IpFilterRuleType ruleType)
/*  29:    */   {
/*  30: 44 */     this.filterRule = selectFilterRule(ipAddress, cidrPrefix, ruleType);
/*  31:    */   }
/*  32:    */   
/*  33:    */   private static IpFilterRule selectFilterRule(InetAddress ipAddress, int cidrPrefix, IpFilterRuleType ruleType)
/*  34:    */   {
/*  35: 48 */     if (ipAddress == null) {
/*  36: 49 */       throw new NullPointerException("ipAddress");
/*  37:    */     }
/*  38: 52 */     if (ruleType == null) {
/*  39: 53 */       throw new NullPointerException("ruleType");
/*  40:    */     }
/*  41: 56 */     if ((ipAddress instanceof Inet4Address)) {
/*  42: 57 */       return new Ip4SubnetFilterRule((Inet4Address)ipAddress, cidrPrefix, ruleType, null);
/*  43:    */     }
/*  44: 58 */     if ((ipAddress instanceof Inet6Address)) {
/*  45: 59 */       return new Ip6SubnetFilterRule((Inet6Address)ipAddress, cidrPrefix, ruleType, null);
/*  46:    */     }
/*  47: 61 */     throw new IllegalArgumentException("Only IPv4 and IPv6 addresses are supported");
/*  48:    */   }
/*  49:    */   
/*  50:    */   public boolean matches(InetSocketAddress remoteAddress)
/*  51:    */   {
/*  52: 67 */     return this.filterRule.matches(remoteAddress);
/*  53:    */   }
/*  54:    */   
/*  55:    */   public IpFilterRuleType ruleType()
/*  56:    */   {
/*  57: 72 */     return this.filterRule.ruleType();
/*  58:    */   }
/*  59:    */   
/*  60:    */   private static final class Ip4SubnetFilterRule
/*  61:    */     implements IpFilterRule
/*  62:    */   {
/*  63:    */     private final int networkAddress;
/*  64:    */     private final int subnetMask;
/*  65:    */     private final IpFilterRuleType ruleType;
/*  66:    */     
/*  67:    */     private Ip4SubnetFilterRule(Inet4Address ipAddress, int cidrPrefix, IpFilterRuleType ruleType)
/*  68:    */     {
/*  69: 82 */       if ((cidrPrefix < 0) || (cidrPrefix > 32)) {
/*  70: 83 */         throw new IllegalArgumentException(String.format("IPv4 requires the subnet prefix to be in range of [0,32]. The prefix was: %d", new Object[] {
/*  71: 84 */           Integer.valueOf(cidrPrefix) }));
/*  72:    */       }
/*  73: 87 */       this.subnetMask = prefixToSubnetMask(cidrPrefix);
/*  74: 88 */       this.networkAddress = (ipToInt(ipAddress) & this.subnetMask);
/*  75: 89 */       this.ruleType = ruleType;
/*  76:    */     }
/*  77:    */     
/*  78:    */     public boolean matches(InetSocketAddress remoteAddress)
/*  79:    */     {
/*  80: 94 */       InetAddress inetAddress = remoteAddress.getAddress();
/*  81: 95 */       if ((inetAddress instanceof Inet4Address))
/*  82:    */       {
/*  83: 96 */         int ipAddress = ipToInt((Inet4Address)inetAddress);
/*  84: 97 */         return (ipAddress & this.subnetMask) == this.networkAddress;
/*  85:    */       }
/*  86: 99 */       return false;
/*  87:    */     }
/*  88:    */     
/*  89:    */     public IpFilterRuleType ruleType()
/*  90:    */     {
/*  91:104 */       return this.ruleType;
/*  92:    */     }
/*  93:    */     
/*  94:    */     private static int ipToInt(Inet4Address ipAddress)
/*  95:    */     {
/*  96:108 */       byte[] octets = ipAddress.getAddress();
/*  97:109 */       assert (octets.length == 4);
/*  98:    */       
/*  99:111 */       return (octets[0] & 0xFF) << 24 | (octets[1] & 0xFF) << 16 | (octets[2] & 0xFF) << 8 | octets[3] & 0xFF;
/* 100:    */     }
/* 101:    */     
/* 102:    */     private static int prefixToSubnetMask(int cidrPrefix)
/* 103:    */     {
/* 104:128 */       return (int)(-1L << 32 - cidrPrefix & 0xFFFFFFFF);
/* 105:    */     }
/* 106:    */   }
/* 107:    */   
/* 108:    */   private static final class Ip6SubnetFilterRule
/* 109:    */     implements IpFilterRule
/* 110:    */   {
/* 111:134 */     private static final BigInteger MINUS_ONE = BigInteger.valueOf(-1L);
/* 112:    */     private final BigInteger networkAddress;
/* 113:    */     private final BigInteger subnetMask;
/* 114:    */     private final IpFilterRuleType ruleType;
/* 115:    */     
/* 116:    */     private Ip6SubnetFilterRule(Inet6Address ipAddress, int cidrPrefix, IpFilterRuleType ruleType)
/* 117:    */     {
/* 118:141 */       if ((cidrPrefix < 0) || (cidrPrefix > 128)) {
/* 119:142 */         throw new IllegalArgumentException(String.format("IPv6 requires the subnet prefix to be in range of [0,128]. The prefix was: %d", new Object[] {
/* 120:143 */           Integer.valueOf(cidrPrefix) }));
/* 121:    */       }
/* 122:146 */       this.subnetMask = prefixToSubnetMask(cidrPrefix);
/* 123:147 */       this.networkAddress = ipToInt(ipAddress).and(this.subnetMask);
/* 124:148 */       this.ruleType = ruleType;
/* 125:    */     }
/* 126:    */     
/* 127:    */     public boolean matches(InetSocketAddress remoteAddress)
/* 128:    */     {
/* 129:153 */       InetAddress inetAddress = remoteAddress.getAddress();
/* 130:154 */       if ((inetAddress instanceof Inet6Address))
/* 131:    */       {
/* 132:155 */         BigInteger ipAddress = ipToInt((Inet6Address)inetAddress);
/* 133:156 */         return ipAddress.and(this.subnetMask).equals(this.networkAddress);
/* 134:    */       }
/* 135:158 */       return false;
/* 136:    */     }
/* 137:    */     
/* 138:    */     public IpFilterRuleType ruleType()
/* 139:    */     {
/* 140:163 */       return this.ruleType;
/* 141:    */     }
/* 142:    */     
/* 143:    */     private static BigInteger ipToInt(Inet6Address ipAddress)
/* 144:    */     {
/* 145:167 */       byte[] octets = ipAddress.getAddress();
/* 146:168 */       assert (octets.length == 16);
/* 147:    */       
/* 148:170 */       return new BigInteger(octets);
/* 149:    */     }
/* 150:    */     
/* 151:    */     private static BigInteger prefixToSubnetMask(int cidrPrefix)
/* 152:    */     {
/* 153:174 */       return MINUS_ONE.shiftLeft(128 - cidrPrefix);
/* 154:    */     }
/* 155:    */   }
/* 156:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ipfilter.IpSubnetFilterRule
 * JD-Core Version:    0.7.0.1
 */