/*   1:    */ package io.netty.util;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.ObjectUtil;
/*   4:    */ import io.netty.util.internal.StringUtil;
/*   5:    */ import java.net.IDN;
/*   6:    */ import java.util.Collections;
/*   7:    */ import java.util.LinkedHashMap;
/*   8:    */ import java.util.Locale;
/*   9:    */ import java.util.Map;
/*  10:    */ import java.util.Map.Entry;
/*  11:    */ 
/*  12:    */ public class DomainNameMapping<V>
/*  13:    */   implements Mapping<String, V>
/*  14:    */ {
/*  15:    */   final V defaultValue;
/*  16:    */   private final Map<String, V> map;
/*  17:    */   private final Map<String, V> unmodifiableMap;
/*  18:    */   
/*  19:    */   @Deprecated
/*  20:    */   public DomainNameMapping(V defaultValue)
/*  21:    */   {
/*  22: 52 */     this(4, defaultValue);
/*  23:    */   }
/*  24:    */   
/*  25:    */   @Deprecated
/*  26:    */   public DomainNameMapping(int initialCapacity, V defaultValue)
/*  27:    */   {
/*  28: 65 */     this(new LinkedHashMap(initialCapacity), defaultValue);
/*  29:    */   }
/*  30:    */   
/*  31:    */   DomainNameMapping(Map<String, V> map, V defaultValue)
/*  32:    */   {
/*  33: 69 */     this.defaultValue = ObjectUtil.checkNotNull(defaultValue, "defaultValue");
/*  34: 70 */     this.map = map;
/*  35: 71 */     this.unmodifiableMap = (map != null ? Collections.unmodifiableMap(map) : null);
/*  36:    */   }
/*  37:    */   
/*  38:    */   @Deprecated
/*  39:    */   public DomainNameMapping<V> add(String hostname, V output)
/*  40:    */   {
/*  41: 89 */     this.map.put(normalizeHostname((String)ObjectUtil.checkNotNull(hostname, "hostname")), ObjectUtil.checkNotNull(output, "output"));
/*  42: 90 */     return this;
/*  43:    */   }
/*  44:    */   
/*  45:    */   static boolean matches(String template, String hostName)
/*  46:    */   {
/*  47: 97 */     if (template.startsWith("*.")) {
/*  48: 98 */       return (template.regionMatches(2, hostName, 0, hostName.length())) || 
/*  49: 99 */         (StringUtil.commonSuffixOfLength(hostName, template, template.length() - 1));
/*  50:    */     }
/*  51:101 */     return template.equals(hostName);
/*  52:    */   }
/*  53:    */   
/*  54:    */   static String normalizeHostname(String hostname)
/*  55:    */   {
/*  56:108 */     if (needsNormalization(hostname)) {
/*  57:109 */       hostname = IDN.toASCII(hostname, 1);
/*  58:    */     }
/*  59:111 */     return hostname.toLowerCase(Locale.US);
/*  60:    */   }
/*  61:    */   
/*  62:    */   private static boolean needsNormalization(String hostname)
/*  63:    */   {
/*  64:115 */     int length = hostname.length();
/*  65:116 */     for (int i = 0; i < length; i++)
/*  66:    */     {
/*  67:117 */       int c = hostname.charAt(i);
/*  68:118 */       if (c > 127) {
/*  69:119 */         return true;
/*  70:    */       }
/*  71:    */     }
/*  72:122 */     return false;
/*  73:    */   }
/*  74:    */   
/*  75:    */   public V map(String hostname)
/*  76:    */   {
/*  77:127 */     if (hostname != null)
/*  78:    */     {
/*  79:128 */       hostname = normalizeHostname(hostname);
/*  80:130 */       for (Map.Entry<String, V> entry : this.map.entrySet()) {
/*  81:131 */         if (matches((String)entry.getKey(), hostname)) {
/*  82:132 */           return entry.getValue();
/*  83:    */         }
/*  84:    */       }
/*  85:    */     }
/*  86:136 */     return this.defaultValue;
/*  87:    */   }
/*  88:    */   
/*  89:    */   public Map<String, V> asMap()
/*  90:    */   {
/*  91:143 */     return this.unmodifiableMap;
/*  92:    */   }
/*  93:    */   
/*  94:    */   public String toString()
/*  95:    */   {
/*  96:148 */     return StringUtil.simpleClassName(this) + "(default: " + this.defaultValue + ", map: " + this.map + ')';
/*  97:    */   }
/*  98:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.DomainNameMapping
 * JD-Core Version:    0.7.0.1
 */