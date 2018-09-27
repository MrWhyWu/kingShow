/*   1:    */ package io.netty.channel;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufUtil;
/*   4:    */ import io.netty.util.internal.EmptyArrays;
/*   5:    */ import io.netty.util.internal.MacAddressUtil;
/*   6:    */ import io.netty.util.internal.PlatformDependent;
/*   7:    */ import io.netty.util.internal.SystemPropertyUtil;
/*   8:    */ import io.netty.util.internal.logging.InternalLogger;
/*   9:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  10:    */ import java.lang.reflect.Method;
/*  11:    */ import java.util.Arrays;
/*  12:    */ import java.util.Random;
/*  13:    */ import java.util.concurrent.atomic.AtomicInteger;
/*  14:    */ 
/*  15:    */ public final class DefaultChannelId
/*  16:    */   implements ChannelId
/*  17:    */ {
/*  18:    */   private static final long serialVersionUID = 3884076183504074063L;
/*  19:    */   private static final InternalLogger logger;
/*  20:    */   private static final byte[] MACHINE_ID;
/*  21:    */   private static final int PROCESS_ID_LEN = 4;
/*  22:    */   private static final int PROCESS_ID;
/*  23:    */   private static final int SEQUENCE_LEN = 4;
/*  24:    */   private static final int TIMESTAMP_LEN = 8;
/*  25:    */   private static final int RANDOM_LEN = 4;
/*  26:    */   private static final AtomicInteger nextSequence;
/*  27:    */   private final byte[] data;
/*  28:    */   private final int hashCode;
/*  29:    */   private transient String shortValue;
/*  30:    */   private transient String longValue;
/*  31:    */   
/*  32:    */   public static DefaultChannelId newInstance()
/*  33:    */   {
/*  34: 55 */     return new DefaultChannelId();
/*  35:    */   }
/*  36:    */   
/*  37:    */   static
/*  38:    */   {
/*  39: 41 */     logger = InternalLoggerFactory.getInstance(DefaultChannelId.class);
/*  40:    */     
/*  41:    */ 
/*  42:    */ 
/*  43:    */ 
/*  44:    */ 
/*  45:    */ 
/*  46:    */ 
/*  47: 49 */     nextSequence = new AtomicInteger();
/*  48:    */     
/*  49:    */ 
/*  50:    */ 
/*  51:    */ 
/*  52:    */ 
/*  53:    */ 
/*  54:    */ 
/*  55:    */ 
/*  56:    */ 
/*  57: 59 */     int processId = -1;
/*  58: 60 */     String customProcessId = SystemPropertyUtil.get("io.netty.processId");
/*  59: 61 */     if (customProcessId != null)
/*  60:    */     {
/*  61:    */       try
/*  62:    */       {
/*  63: 63 */         processId = Integer.parseInt(customProcessId);
/*  64:    */       }
/*  65:    */       catch (NumberFormatException localNumberFormatException) {}
/*  66: 68 */       if (processId < 0)
/*  67:    */       {
/*  68: 69 */         processId = -1;
/*  69: 70 */         logger.warn("-Dio.netty.processId: {} (malformed)", customProcessId);
/*  70:    */       }
/*  71: 71 */       else if (logger.isDebugEnabled())
/*  72:    */       {
/*  73: 72 */         logger.debug("-Dio.netty.processId: {} (user-set)", Integer.valueOf(processId));
/*  74:    */       }
/*  75:    */     }
/*  76: 76 */     if (processId < 0)
/*  77:    */     {
/*  78: 77 */       processId = defaultProcessId();
/*  79: 78 */       if (logger.isDebugEnabled()) {
/*  80: 79 */         logger.debug("-Dio.netty.processId: {} (auto-detected)", Integer.valueOf(processId));
/*  81:    */       }
/*  82:    */     }
/*  83: 83 */     PROCESS_ID = processId;
/*  84:    */     
/*  85: 85 */     byte[] machineId = null;
/*  86: 86 */     String customMachineId = SystemPropertyUtil.get("io.netty.machineId");
/*  87: 87 */     if (customMachineId != null)
/*  88:    */     {
/*  89:    */       try
/*  90:    */       {
/*  91: 89 */         machineId = MacAddressUtil.parseMAC(customMachineId);
/*  92:    */       }
/*  93:    */       catch (Exception e)
/*  94:    */       {
/*  95: 91 */         logger.warn("-Dio.netty.machineId: {} (malformed)", customMachineId, e);
/*  96:    */       }
/*  97: 93 */       if (machineId != null) {
/*  98: 94 */         logger.debug("-Dio.netty.machineId: {} (user-set)", customMachineId);
/*  99:    */       }
/* 100:    */     }
/* 101: 98 */     if (machineId == null)
/* 102:    */     {
/* 103: 99 */       machineId = MacAddressUtil.defaultMachineId();
/* 104:100 */       if (logger.isDebugEnabled()) {
/* 105:101 */         logger.debug("-Dio.netty.machineId: {} (auto-detected)", MacAddressUtil.formatAddress(machineId));
/* 106:    */       }
/* 107:    */     }
/* 108:105 */     MACHINE_ID = machineId;
/* 109:    */   }
/* 110:    */   
/* 111:    */   private static int defaultProcessId()
/* 112:    */   {
/* 113:109 */     ClassLoader loader = null;
/* 114:    */     String value;
/* 115:    */     try
/* 116:    */     {
/* 117:112 */       loader = PlatformDependent.getClassLoader(DefaultChannelId.class);
/* 118:    */       
/* 119:114 */       Class<?> mgmtFactoryType = Class.forName("java.lang.management.ManagementFactory", true, loader);
/* 120:115 */       Class<?> runtimeMxBeanType = Class.forName("java.lang.management.RuntimeMXBean", true, loader);
/* 121:    */       
/* 122:117 */       Method getRuntimeMXBean = mgmtFactoryType.getMethod("getRuntimeMXBean", EmptyArrays.EMPTY_CLASSES);
/* 123:118 */       Object bean = getRuntimeMXBean.invoke(null, EmptyArrays.EMPTY_OBJECTS);
/* 124:119 */       Method getName = runtimeMxBeanType.getMethod("getName", EmptyArrays.EMPTY_CLASSES);
/* 125:120 */       value = (String)getName.invoke(bean, EmptyArrays.EMPTY_OBJECTS);
/* 126:    */     }
/* 127:    */     catch (Throwable t)
/* 128:    */     {
/* 129:    */       String value;
/* 130:122 */       logger.debug("Could not invoke ManagementFactory.getRuntimeMXBean().getName(); Android?", t);
/* 131:    */       try
/* 132:    */       {
/* 133:125 */         Class<?> processType = Class.forName("android.os.Process", true, loader);
/* 134:126 */         Method myPid = processType.getMethod("myPid", EmptyArrays.EMPTY_CLASSES);
/* 135:127 */         value = myPid.invoke(null, EmptyArrays.EMPTY_OBJECTS).toString();
/* 136:    */       }
/* 137:    */       catch (Throwable t2)
/* 138:    */       {
/* 139:    */         String value;
/* 140:129 */         logger.debug("Could not invoke Process.myPid(); not Android?", t2);
/* 141:130 */         value = "";
/* 142:    */       }
/* 143:    */     }
/* 144:134 */     int atIndex = value.indexOf('@');
/* 145:135 */     if (atIndex >= 0) {
/* 146:136 */       value = value.substring(0, atIndex);
/* 147:    */     }
/* 148:    */     int pid;
/* 149:    */     try
/* 150:    */     {
/* 151:141 */       pid = Integer.parseInt(value);
/* 152:    */     }
/* 153:    */     catch (NumberFormatException e)
/* 154:    */     {
/* 155:    */       int pid;
/* 156:144 */       pid = -1;
/* 157:    */     }
/* 158:147 */     if (pid < 0)
/* 159:    */     {
/* 160:148 */       pid = PlatformDependent.threadLocalRandom().nextInt();
/* 161:149 */       logger.warn("Failed to find the current process ID from '{}'; using a random value: {}", value, Integer.valueOf(pid));
/* 162:    */     }
/* 163:152 */     return pid;
/* 164:    */   }
/* 165:    */   
/* 166:    */   private DefaultChannelId()
/* 167:    */   {
/* 168:162 */     this.data = new byte[MACHINE_ID.length + 4 + 4 + 8 + 4];
/* 169:163 */     int i = 0;
/* 170:    */     
/* 171:    */ 
/* 172:166 */     System.arraycopy(MACHINE_ID, 0, this.data, i, MACHINE_ID.length);
/* 173:167 */     i += MACHINE_ID.length;
/* 174:    */     
/* 175:    */ 
/* 176:170 */     i = writeInt(i, PROCESS_ID);
/* 177:    */     
/* 178:    */ 
/* 179:173 */     i = writeInt(i, nextSequence.getAndIncrement());
/* 180:    */     
/* 181:    */ 
/* 182:176 */     i = writeLong(i, Long.reverse(System.nanoTime()) ^ System.currentTimeMillis());
/* 183:    */     
/* 184:    */ 
/* 185:179 */     int random = PlatformDependent.threadLocalRandom().nextInt();
/* 186:180 */     i = writeInt(i, random);
/* 187:181 */     assert (i == this.data.length);
/* 188:    */     
/* 189:183 */     this.hashCode = Arrays.hashCode(this.data);
/* 190:    */   }
/* 191:    */   
/* 192:    */   private int writeInt(int i, int value)
/* 193:    */   {
/* 194:187 */     this.data[(i++)] = ((byte)(value >>> 24));
/* 195:188 */     this.data[(i++)] = ((byte)(value >>> 16));
/* 196:189 */     this.data[(i++)] = ((byte)(value >>> 8));
/* 197:190 */     this.data[(i++)] = ((byte)value);
/* 198:191 */     return i;
/* 199:    */   }
/* 200:    */   
/* 201:    */   private int writeLong(int i, long value)
/* 202:    */   {
/* 203:195 */     this.data[(i++)] = ((byte)(int)(value >>> 56));
/* 204:196 */     this.data[(i++)] = ((byte)(int)(value >>> 48));
/* 205:197 */     this.data[(i++)] = ((byte)(int)(value >>> 40));
/* 206:198 */     this.data[(i++)] = ((byte)(int)(value >>> 32));
/* 207:199 */     this.data[(i++)] = ((byte)(int)(value >>> 24));
/* 208:200 */     this.data[(i++)] = ((byte)(int)(value >>> 16));
/* 209:201 */     this.data[(i++)] = ((byte)(int)(value >>> 8));
/* 210:202 */     this.data[(i++)] = ((byte)(int)value);
/* 211:203 */     return i;
/* 212:    */   }
/* 213:    */   
/* 214:    */   public String asShortText()
/* 215:    */   {
/* 216:208 */     String shortValue = this.shortValue;
/* 217:209 */     if (shortValue == null) {
/* 218:210 */       this.shortValue = (shortValue = ByteBufUtil.hexDump(this.data, this.data.length - 4, 4));
/* 219:    */     }
/* 220:212 */     return shortValue;
/* 221:    */   }
/* 222:    */   
/* 223:    */   public String asLongText()
/* 224:    */   {
/* 225:217 */     String longValue = this.longValue;
/* 226:218 */     if (longValue == null) {
/* 227:219 */       this.longValue = (longValue = newLongValue());
/* 228:    */     }
/* 229:221 */     return longValue;
/* 230:    */   }
/* 231:    */   
/* 232:    */   private String newLongValue()
/* 233:    */   {
/* 234:225 */     StringBuilder buf = new StringBuilder(2 * this.data.length + 5);
/* 235:226 */     int i = 0;
/* 236:227 */     i = appendHexDumpField(buf, i, MACHINE_ID.length);
/* 237:228 */     i = appendHexDumpField(buf, i, 4);
/* 238:229 */     i = appendHexDumpField(buf, i, 4);
/* 239:230 */     i = appendHexDumpField(buf, i, 8);
/* 240:231 */     i = appendHexDumpField(buf, i, 4);
/* 241:232 */     assert (i == this.data.length);
/* 242:233 */     return buf.substring(0, buf.length() - 1);
/* 243:    */   }
/* 244:    */   
/* 245:    */   private int appendHexDumpField(StringBuilder buf, int i, int length)
/* 246:    */   {
/* 247:237 */     buf.append(ByteBufUtil.hexDump(this.data, i, length));
/* 248:238 */     buf.append('-');
/* 249:239 */     i += length;
/* 250:240 */     return i;
/* 251:    */   }
/* 252:    */   
/* 253:    */   public int hashCode()
/* 254:    */   {
/* 255:245 */     return this.hashCode;
/* 256:    */   }
/* 257:    */   
/* 258:    */   public int compareTo(ChannelId o)
/* 259:    */   {
/* 260:250 */     if (this == o) {
/* 261:252 */       return 0;
/* 262:    */     }
/* 263:254 */     if ((o instanceof DefaultChannelId))
/* 264:    */     {
/* 265:256 */       byte[] otherData = ((DefaultChannelId)o).data;
/* 266:257 */       int len1 = this.data.length;
/* 267:258 */       int len2 = otherData.length;
/* 268:259 */       int len = Math.min(len1, len2);
/* 269:261 */       for (int k = 0; k < len; k++)
/* 270:    */       {
/* 271:262 */         byte x = this.data[k];
/* 272:263 */         byte y = otherData[k];
/* 273:264 */         if (x != y) {
/* 274:266 */           return (x & 0xFF) - (y & 0xFF);
/* 275:    */         }
/* 276:    */       }
/* 277:269 */       return len1 - len2;
/* 278:    */     }
/* 279:272 */     return asLongText().compareTo(o.asLongText());
/* 280:    */   }
/* 281:    */   
/* 282:    */   public boolean equals(Object obj)
/* 283:    */   {
/* 284:277 */     if (this == obj) {
/* 285:278 */       return true;
/* 286:    */     }
/* 287:280 */     if (!(obj instanceof DefaultChannelId)) {
/* 288:281 */       return false;
/* 289:    */     }
/* 290:283 */     DefaultChannelId other = (DefaultChannelId)obj;
/* 291:284 */     return (this.hashCode == other.hashCode) && (Arrays.equals(this.data, other.data));
/* 292:    */   }
/* 293:    */   
/* 294:    */   public String toString()
/* 295:    */   {
/* 296:289 */     return asShortText();
/* 297:    */   }
/* 298:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.DefaultChannelId
 * JD-Core Version:    0.7.0.1
 */