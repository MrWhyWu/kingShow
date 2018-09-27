/*   1:    */ package io.netty.util;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.ObjectUtil;
/*   4:    */ import io.netty.util.internal.PlatformDependent;
/*   5:    */ import io.netty.util.internal.SystemPropertyUtil;
/*   6:    */ import io.netty.util.internal.logging.InternalLogger;
/*   7:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   8:    */ import java.lang.reflect.Constructor;
/*   9:    */ import java.security.AccessController;
/*  10:    */ import java.security.PrivilegedAction;
/*  11:    */ 
/*  12:    */ public abstract class ResourceLeakDetectorFactory
/*  13:    */ {
/*  14: 33 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(ResourceLeakDetectorFactory.class);
/*  15: 35 */   private static volatile ResourceLeakDetectorFactory factoryInstance = new DefaultResourceLeakDetectorFactory();
/*  16:    */   
/*  17:    */   public static ResourceLeakDetectorFactory instance()
/*  18:    */   {
/*  19: 43 */     return factoryInstance;
/*  20:    */   }
/*  21:    */   
/*  22:    */   public static void setResourceLeakDetectorFactory(ResourceLeakDetectorFactory factory)
/*  23:    */   {
/*  24: 54 */     factoryInstance = (ResourceLeakDetectorFactory)ObjectUtil.checkNotNull(factory, "factory");
/*  25:    */   }
/*  26:    */   
/*  27:    */   public final <T> ResourceLeakDetector<T> newResourceLeakDetector(Class<T> resource)
/*  28:    */   {
/*  29: 65 */     return newResourceLeakDetector(resource, 128);
/*  30:    */   }
/*  31:    */   
/*  32:    */   @Deprecated
/*  33:    */   public abstract <T> ResourceLeakDetector<T> newResourceLeakDetector(Class<T> paramClass, int paramInt, long paramLong);
/*  34:    */   
/*  35:    */   public <T> ResourceLeakDetector<T> newResourceLeakDetector(Class<T> resource, int samplingInterval)
/*  36:    */   {
/*  37: 93 */     return newResourceLeakDetector(resource, 128, 9223372036854775807L);
/*  38:    */   }
/*  39:    */   
/*  40:    */   private static final class DefaultResourceLeakDetectorFactory
/*  41:    */     extends ResourceLeakDetectorFactory
/*  42:    */   {
/*  43:    */     private final Constructor<?> obsoleteCustomClassConstructor;
/*  44:    */     private final Constructor<?> customClassConstructor;
/*  45:    */     
/*  46:    */     DefaultResourceLeakDetectorFactory()
/*  47:    */     {
/*  48:    */       String customLeakDetector;
/*  49:    */       try
/*  50:    */       {
/*  51:106 */         customLeakDetector = (String)AccessController.doPrivileged(new PrivilegedAction()
/*  52:    */         {
/*  53:    */           public String run()
/*  54:    */           {
/*  55:109 */             return SystemPropertyUtil.get("io.netty.customResourceLeakDetector");
/*  56:    */           }
/*  57:    */         });
/*  58:    */       }
/*  59:    */       catch (Throwable cause)
/*  60:    */       {
/*  61:    */         String customLeakDetector;
/*  62:113 */         ResourceLeakDetectorFactory.logger.error("Could not access System property: io.netty.customResourceLeakDetector", cause);
/*  63:114 */         customLeakDetector = null;
/*  64:    */       }
/*  65:116 */       if (customLeakDetector == null)
/*  66:    */       {
/*  67:117 */         this.obsoleteCustomClassConstructor = (this.customClassConstructor = null);
/*  68:    */       }
/*  69:    */       else
/*  70:    */       {
/*  71:119 */         this.obsoleteCustomClassConstructor = obsoleteCustomClassConstructor(customLeakDetector);
/*  72:120 */         this.customClassConstructor = customClassConstructor(customLeakDetector);
/*  73:    */       }
/*  74:    */     }
/*  75:    */     
/*  76:    */     private static Constructor<?> obsoleteCustomClassConstructor(String customLeakDetector)
/*  77:    */     {
/*  78:    */       try
/*  79:    */       {
/*  80:126 */         Class<?> detectorClass = Class.forName(customLeakDetector, true, 
/*  81:127 */           PlatformDependent.getSystemClassLoader());
/*  82:129 */         if (ResourceLeakDetector.class.isAssignableFrom(detectorClass)) {
/*  83:130 */           return detectorClass.getConstructor(new Class[] { Class.class, Integer.TYPE, Long.TYPE });
/*  84:    */         }
/*  85:132 */         ResourceLeakDetectorFactory.logger.error("Class {} does not inherit from ResourceLeakDetector.", customLeakDetector);
/*  86:    */       }
/*  87:    */       catch (Throwable t)
/*  88:    */       {
/*  89:135 */         ResourceLeakDetectorFactory.logger.error("Could not load custom resource leak detector class provided: {}", customLeakDetector, t);
/*  90:    */       }
/*  91:138 */       return null;
/*  92:    */     }
/*  93:    */     
/*  94:    */     private static Constructor<?> customClassConstructor(String customLeakDetector)
/*  95:    */     {
/*  96:    */       try
/*  97:    */       {
/*  98:143 */         Class<?> detectorClass = Class.forName(customLeakDetector, true, 
/*  99:144 */           PlatformDependent.getSystemClassLoader());
/* 100:146 */         if (ResourceLeakDetector.class.isAssignableFrom(detectorClass)) {
/* 101:147 */           return detectorClass.getConstructor(new Class[] { Class.class, Integer.TYPE });
/* 102:    */         }
/* 103:149 */         ResourceLeakDetectorFactory.logger.error("Class {} does not inherit from ResourceLeakDetector.", customLeakDetector);
/* 104:    */       }
/* 105:    */       catch (Throwable t)
/* 106:    */       {
/* 107:152 */         ResourceLeakDetectorFactory.logger.error("Could not load custom resource leak detector class provided: {}", customLeakDetector, t);
/* 108:    */       }
/* 109:155 */       return null;
/* 110:    */     }
/* 111:    */     
/* 112:    */     public <T> ResourceLeakDetector<T> newResourceLeakDetector(Class<T> resource, int samplingInterval, long maxActive)
/* 113:    */     {
/* 114:162 */       if (this.obsoleteCustomClassConstructor != null) {
/* 115:    */         try
/* 116:    */         {
/* 117:166 */           ResourceLeakDetector<T> leakDetector = (ResourceLeakDetector)this.obsoleteCustomClassConstructor.newInstance(new Object[] { resource, 
/* 118:167 */             Integer.valueOf(samplingInterval), Long.valueOf(maxActive) });
/* 119:168 */           ResourceLeakDetectorFactory.logger.debug("Loaded custom ResourceLeakDetector: {}", this.obsoleteCustomClassConstructor
/* 120:169 */             .getDeclaringClass().getName());
/* 121:170 */           return leakDetector;
/* 122:    */         }
/* 123:    */         catch (Throwable t)
/* 124:    */         {
/* 125:172 */           ResourceLeakDetectorFactory.logger.error("Could not load custom resource leak detector provided: {} with the given resource: {}", new Object[] {this.obsoleteCustomClassConstructor
/* 126:    */           
/* 127:174 */             .getDeclaringClass().getName(), resource, t });
/* 128:    */         }
/* 129:    */       }
/* 130:178 */       ResourceLeakDetector<T> resourceLeakDetector = new ResourceLeakDetector(resource, samplingInterval, maxActive);
/* 131:    */       
/* 132:180 */       ResourceLeakDetectorFactory.logger.debug("Loaded default ResourceLeakDetector: {}", resourceLeakDetector);
/* 133:181 */       return resourceLeakDetector;
/* 134:    */     }
/* 135:    */     
/* 136:    */     public <T> ResourceLeakDetector<T> newResourceLeakDetector(Class<T> resource, int samplingInterval)
/* 137:    */     {
/* 138:186 */       if (this.customClassConstructor != null) {
/* 139:    */         try
/* 140:    */         {
/* 141:190 */           ResourceLeakDetector<T> leakDetector = (ResourceLeakDetector)this.customClassConstructor.newInstance(new Object[] { resource, Integer.valueOf(samplingInterval) });
/* 142:191 */           ResourceLeakDetectorFactory.logger.debug("Loaded custom ResourceLeakDetector: {}", this.customClassConstructor
/* 143:192 */             .getDeclaringClass().getName());
/* 144:193 */           return leakDetector;
/* 145:    */         }
/* 146:    */         catch (Throwable t)
/* 147:    */         {
/* 148:195 */           ResourceLeakDetectorFactory.logger.error("Could not load custom resource leak detector provided: {} with the given resource: {}", new Object[] {this.customClassConstructor
/* 149:    */           
/* 150:197 */             .getDeclaringClass().getName(), resource, t });
/* 151:    */         }
/* 152:    */       }
/* 153:201 */       ResourceLeakDetector<T> resourceLeakDetector = new ResourceLeakDetector(resource, samplingInterval);
/* 154:202 */       ResourceLeakDetectorFactory.logger.debug("Loaded default ResourceLeakDetector: {}", resourceLeakDetector);
/* 155:203 */       return resourceLeakDetector;
/* 156:    */     }
/* 157:    */   }
/* 158:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.ResourceLeakDetectorFactory
 * JD-Core Version:    0.7.0.1
 */