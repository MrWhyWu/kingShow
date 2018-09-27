/*   1:    */ package io.netty.util;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.ObjectUtil;
/*   4:    */ import java.util.Collections;
/*   5:    */ import java.util.LinkedHashMap;
/*   6:    */ import java.util.Map;
/*   7:    */ import java.util.Map.Entry;
/*   8:    */ import java.util.Set;
/*   9:    */ 
/*  10:    */ public final class DomainNameMappingBuilder<V>
/*  11:    */ {
/*  12:    */   private final V defaultValue;
/*  13:    */   private final Map<String, V> map;
/*  14:    */   
/*  15:    */   public DomainNameMappingBuilder(V defaultValue)
/*  16:    */   {
/*  17: 43 */     this(4, defaultValue);
/*  18:    */   }
/*  19:    */   
/*  20:    */   public DomainNameMappingBuilder(int initialCapacity, V defaultValue)
/*  21:    */   {
/*  22: 54 */     this.defaultValue = ObjectUtil.checkNotNull(defaultValue, "defaultValue");
/*  23: 55 */     this.map = new LinkedHashMap(initialCapacity);
/*  24:    */   }
/*  25:    */   
/*  26:    */   public DomainNameMappingBuilder<V> add(String hostname, V output)
/*  27:    */   {
/*  28: 71 */     this.map.put(ObjectUtil.checkNotNull(hostname, "hostname"), ObjectUtil.checkNotNull(output, "output"));
/*  29: 72 */     return this;
/*  30:    */   }
/*  31:    */   
/*  32:    */   public DomainNameMapping<V> build()
/*  33:    */   {
/*  34: 82 */     return new ImmutableDomainNameMapping(this.defaultValue, this.map, null);
/*  35:    */   }
/*  36:    */   
/*  37:    */   private static final class ImmutableDomainNameMapping<V>
/*  38:    */     extends DomainNameMapping<V>
/*  39:    */   {
/*  40:    */     private static final String REPR_HEADER = "ImmutableDomainNameMapping(default: ";
/*  41:    */     private static final String REPR_MAP_OPENING = ", map: {";
/*  42:    */     private static final String REPR_MAP_CLOSING = "})";
/*  43: 95 */     private static final int REPR_CONST_PART_LENGTH = "ImmutableDomainNameMapping(default: "
/*  44: 96 */       .length() + ", map: {".length() + "})".length();
/*  45:    */     private final String[] domainNamePatterns;
/*  46:    */     private final V[] values;
/*  47:    */     private final Map<String, V> map;
/*  48:    */     
/*  49:    */     private ImmutableDomainNameMapping(V defaultValue, Map<String, V> map)
/*  50:    */     {
/*  51:104 */       super(defaultValue);
/*  52:    */       
/*  53:106 */       Set<Map.Entry<String, V>> mappings = map.entrySet();
/*  54:107 */       int numberOfMappings = mappings.size();
/*  55:108 */       this.domainNamePatterns = new String[numberOfMappings];
/*  56:109 */       this.values = ((Object[])new Object[numberOfMappings]);
/*  57:    */       
/*  58:111 */       Map<String, V> mapCopy = new LinkedHashMap(map.size());
/*  59:112 */       int index = 0;
/*  60:113 */       for (Map.Entry<String, V> mapping : mappings)
/*  61:    */       {
/*  62:114 */         String hostname = normalizeHostname((String)mapping.getKey());
/*  63:115 */         V value = mapping.getValue();
/*  64:116 */         this.domainNamePatterns[index] = hostname;
/*  65:117 */         this.values[index] = value;
/*  66:118 */         mapCopy.put(hostname, value);
/*  67:119 */         index++;
/*  68:    */       }
/*  69:122 */       this.map = Collections.unmodifiableMap(mapCopy);
/*  70:    */     }
/*  71:    */     
/*  72:    */     @Deprecated
/*  73:    */     public DomainNameMapping<V> add(String hostname, V output)
/*  74:    */     {
/*  75:128 */       throw new UnsupportedOperationException("Immutable DomainNameMapping does not support modification after initial creation");
/*  76:    */     }
/*  77:    */     
/*  78:    */     public V map(String hostname)
/*  79:    */     {
/*  80:134 */       if (hostname != null)
/*  81:    */       {
/*  82:135 */         hostname = normalizeHostname(hostname);
/*  83:    */         
/*  84:137 */         int length = this.domainNamePatterns.length;
/*  85:138 */         for (int index = 0; index < length; index++) {
/*  86:139 */           if (matches(this.domainNamePatterns[index], hostname)) {
/*  87:140 */             return this.values[index];
/*  88:    */           }
/*  89:    */         }
/*  90:    */       }
/*  91:145 */       return this.defaultValue;
/*  92:    */     }
/*  93:    */     
/*  94:    */     public Map<String, V> asMap()
/*  95:    */     {
/*  96:150 */       return this.map;
/*  97:    */     }
/*  98:    */     
/*  99:    */     public String toString()
/* 100:    */     {
/* 101:155 */       String defaultValueStr = this.defaultValue.toString();
/* 102:    */       
/* 103:157 */       int numberOfMappings = this.domainNamePatterns.length;
/* 104:158 */       if (numberOfMappings == 0) {
/* 105:159 */         return "ImmutableDomainNameMapping(default: " + defaultValueStr + ", map: {" + "})";
/* 106:    */       }
/* 107:162 */       String pattern0 = this.domainNamePatterns[0];
/* 108:163 */       String value0 = this.values[0].toString();
/* 109:164 */       int oneMappingLength = pattern0.length() + value0.length() + 3;
/* 110:165 */       int estimatedBufferSize = estimateBufferSize(defaultValueStr.length(), numberOfMappings, oneMappingLength);
/* 111:    */       
/* 112:    */ 
/* 113:168 */       StringBuilder sb = new StringBuilder(estimatedBufferSize).append("ImmutableDomainNameMapping(default: ").append(defaultValueStr).append(", map: {");
/* 114:    */       
/* 115:170 */       appendMapping(sb, pattern0, value0);
/* 116:171 */       for (int index = 1; index < numberOfMappings; index++)
/* 117:    */       {
/* 118:172 */         sb.append(", ");
/* 119:173 */         appendMapping(sb, index);
/* 120:    */       }
/* 121:176 */       return "})";
/* 122:    */     }
/* 123:    */     
/* 124:    */     private static int estimateBufferSize(int defaultValueLength, int numberOfMappings, int estimatedMappingLength)
/* 125:    */     {
/* 126:192 */       return REPR_CONST_PART_LENGTH + defaultValueLength + (int)(estimatedMappingLength * numberOfMappings * 1.1D);
/* 127:    */     }
/* 128:    */     
/* 129:    */     private StringBuilder appendMapping(StringBuilder sb, int mappingIndex)
/* 130:    */     {
/* 131:197 */       return appendMapping(sb, this.domainNamePatterns[mappingIndex], this.values[mappingIndex].toString());
/* 132:    */     }
/* 133:    */     
/* 134:    */     private static StringBuilder appendMapping(StringBuilder sb, String domainNamePattern, String value)
/* 135:    */     {
/* 136:201 */       return sb.append(domainNamePattern).append('=').append(value);
/* 137:    */     }
/* 138:    */   }
/* 139:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.DomainNameMappingBuilder
 * JD-Core Version:    0.7.0.1
 */