/*   1:    */ package io.netty.util;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.PlatformDependent;
/*   4:    */ import java.io.InputStream;
/*   5:    */ import java.io.PrintStream;
/*   6:    */ import java.net.URL;
/*   7:    */ import java.text.ParseException;
/*   8:    */ import java.text.SimpleDateFormat;
/*   9:    */ import java.util.Date;
/*  10:    */ import java.util.Enumeration;
/*  11:    */ import java.util.HashSet;
/*  12:    */ import java.util.Iterator;
/*  13:    */ import java.util.Map;
/*  14:    */ import java.util.Properties;
/*  15:    */ import java.util.Set;
/*  16:    */ import java.util.TreeMap;
/*  17:    */ 
/*  18:    */ public final class Version
/*  19:    */ {
/*  20:    */   private static final String PROP_VERSION = ".version";
/*  21:    */   private static final String PROP_BUILD_DATE = ".buildDate";
/*  22:    */   private static final String PROP_COMMIT_DATE = ".commitDate";
/*  23:    */   private static final String PROP_SHORT_COMMIT_HASH = ".shortCommitHash";
/*  24:    */   private static final String PROP_LONG_COMMIT_HASH = ".longCommitHash";
/*  25:    */   private static final String PROP_REPO_STATUS = ".repoStatus";
/*  26:    */   private final String artifactId;
/*  27:    */   private final String artifactVersion;
/*  28:    */   private final long buildTimeMillis;
/*  29:    */   private final long commitTimeMillis;
/*  30:    */   private final String shortCommitHash;
/*  31:    */   private final String longCommitHash;
/*  32:    */   private final String repositoryStatus;
/*  33:    */   
/*  34:    */   public static Map<String, Version> identify()
/*  35:    */   {
/*  36: 56 */     return identify(null);
/*  37:    */   }
/*  38:    */   
/*  39:    */   public static Map<String, Version> identify(ClassLoader classLoader)
/*  40:    */   {
/*  41: 65 */     if (classLoader == null) {
/*  42: 66 */       classLoader = PlatformDependent.getContextClassLoader();
/*  43:    */     }
/*  44: 70 */     Properties props = new Properties();
/*  45:    */     try
/*  46:    */     {
/*  47: 72 */       Enumeration<URL> resources = classLoader.getResources("META-INF/io.netty.versions.properties");
/*  48:    */       for (;;)
/*  49:    */       {
/*  50: 73 */         if (resources.hasMoreElements())
/*  51:    */         {
/*  52: 74 */           url = (URL)resources.nextElement();
/*  53: 75 */           InputStream in = url.openStream();
/*  54:    */           try
/*  55:    */           {
/*  56: 77 */             props.load(in);
/*  57:    */             try
/*  58:    */             {
/*  59: 80 */               in.close();
/*  60:    */             }
/*  61:    */             catch (Exception localException) {}
/*  62:    */           }
/*  63:    */           finally
/*  64:    */           {
/*  65:    */             try
/*  66:    */             {
/*  67: 80 */               in.close();
/*  68:    */             }
/*  69:    */             catch (Exception localException1) {}
/*  70:    */           }
/*  71:    */         }
/*  72:    */       }
/*  73:    */     }
/*  74:    */     catch (Exception localException2) {}
/*  75: 91 */     Set<String> artifactIds = new HashSet();
/*  76: 92 */     for (URL url = props.keySet().iterator(); url.hasNext();)
/*  77:    */     {
/*  78: 92 */       o = url.next();
/*  79: 93 */       String k = (String)o;
/*  80:    */       
/*  81: 95 */       int dotIndex = k.indexOf('.');
/*  82: 96 */       if (dotIndex > 0)
/*  83:    */       {
/*  84:100 */         String artifactId = k.substring(0, dotIndex);
/*  85:103 */         if ((props.containsKey(artifactId + ".version")) && 
/*  86:104 */           (props.containsKey(artifactId + ".buildDate")) && 
/*  87:105 */           (props.containsKey(artifactId + ".commitDate")) && 
/*  88:106 */           (props.containsKey(artifactId + ".shortCommitHash")) && 
/*  89:107 */           (props.containsKey(artifactId + ".longCommitHash")) && 
/*  90:108 */           (props.containsKey(artifactId + ".repoStatus"))) {
/*  91:112 */           artifactIds.add(artifactId);
/*  92:    */         }
/*  93:    */       }
/*  94:    */     }
/*  95:    */     Object o;
/*  96:115 */     Map<String, Version> versions = new TreeMap();
/*  97:116 */     for (String artifactId : artifactIds) {
/*  98:117 */       versions.put(artifactId, new Version(artifactId, props
/*  99:    */       
/* 100:    */ 
/* 101:    */ 
/* 102:121 */         .getProperty(artifactId + ".version"), 
/* 103:122 */         parseIso8601(props.getProperty(artifactId + ".buildDate")), 
/* 104:123 */         parseIso8601(props.getProperty(artifactId + ".commitDate")), props
/* 105:124 */         .getProperty(artifactId + ".shortCommitHash"), props
/* 106:125 */         .getProperty(artifactId + ".longCommitHash"), props
/* 107:126 */         .getProperty(artifactId + ".repoStatus")));
/* 108:    */     }
/* 109:129 */     return versions;
/* 110:    */   }
/* 111:    */   
/* 112:    */   private static long parseIso8601(String value)
/* 113:    */   {
/* 114:    */     try
/* 115:    */     {
/* 116:134 */       return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z").parse(value).getTime();
/* 117:    */     }
/* 118:    */     catch (ParseException ignored) {}
/* 119:136 */     return 0L;
/* 120:    */   }
/* 121:    */   
/* 122:    */   public static void main(String[] args)
/* 123:    */   {
/* 124:144 */     for (Version v : identify().values()) {
/* 125:145 */       System.err.println(v);
/* 126:    */     }
/* 127:    */   }
/* 128:    */   
/* 129:    */   private Version(String artifactId, String artifactVersion, long buildTimeMillis, long commitTimeMillis, String shortCommitHash, String longCommitHash, String repositoryStatus)
/* 130:    */   {
/* 131:161 */     this.artifactId = artifactId;
/* 132:162 */     this.artifactVersion = artifactVersion;
/* 133:163 */     this.buildTimeMillis = buildTimeMillis;
/* 134:164 */     this.commitTimeMillis = commitTimeMillis;
/* 135:165 */     this.shortCommitHash = shortCommitHash;
/* 136:166 */     this.longCommitHash = longCommitHash;
/* 137:167 */     this.repositoryStatus = repositoryStatus;
/* 138:    */   }
/* 139:    */   
/* 140:    */   public String artifactId()
/* 141:    */   {
/* 142:171 */     return this.artifactId;
/* 143:    */   }
/* 144:    */   
/* 145:    */   public String artifactVersion()
/* 146:    */   {
/* 147:175 */     return this.artifactVersion;
/* 148:    */   }
/* 149:    */   
/* 150:    */   public long buildTimeMillis()
/* 151:    */   {
/* 152:179 */     return this.buildTimeMillis;
/* 153:    */   }
/* 154:    */   
/* 155:    */   public long commitTimeMillis()
/* 156:    */   {
/* 157:183 */     return this.commitTimeMillis;
/* 158:    */   }
/* 159:    */   
/* 160:    */   public String shortCommitHash()
/* 161:    */   {
/* 162:187 */     return this.shortCommitHash;
/* 163:    */   }
/* 164:    */   
/* 165:    */   public String longCommitHash()
/* 166:    */   {
/* 167:191 */     return this.longCommitHash;
/* 168:    */   }
/* 169:    */   
/* 170:    */   public String repositoryStatus()
/* 171:    */   {
/* 172:195 */     return this.repositoryStatus;
/* 173:    */   }
/* 174:    */   
/* 175:    */   public String toString()
/* 176:    */   {
/* 177:200 */     return 
/* 178:201 */       this.artifactId + '-' + this.artifactVersion + '.' + this.shortCommitHash + ("clean".equals(this.repositoryStatus) ? "" : new StringBuilder().append(" (repository: ").append(this.repositoryStatus).append(')').toString());
/* 179:    */   }
/* 180:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.Version
 * JD-Core Version:    0.7.0.1
 */