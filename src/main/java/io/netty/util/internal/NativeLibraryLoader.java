/*   1:    */ package io.netty.util.internal;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.logging.InternalLogger;
/*   4:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   5:    */ import java.io.ByteArrayOutputStream;
/*   6:    */ import java.io.Closeable;
/*   7:    */ import java.io.File;
/*   8:    */ import java.io.FileNotFoundException;
/*   9:    */ import java.io.FileOutputStream;
/*  10:    */ import java.io.IOException;
/*  11:    */ import java.io.InputStream;
/*  12:    */ import java.io.OutputStream;
/*  13:    */ import java.lang.reflect.Method;
/*  14:    */ import java.net.URL;
/*  15:    */ import java.nio.file.Files;
/*  16:    */ import java.nio.file.LinkOption;
/*  17:    */ import java.nio.file.attribute.PosixFilePermission;
/*  18:    */ import java.security.AccessController;
/*  19:    */ import java.security.PrivilegedAction;
/*  20:    */ import java.util.ArrayList;
/*  21:    */ import java.util.Arrays;
/*  22:    */ import java.util.EnumSet;
/*  23:    */ import java.util.List;
/*  24:    */ import java.util.Set;
/*  25:    */ 
/*  26:    */ public final class NativeLibraryLoader
/*  27:    */ {
/*  28:    */   private static final InternalLogger logger;
/*  29:    */   private static final String NATIVE_RESOURCE_HOME = "META-INF/native/";
/*  30:    */   private static final File WORKDIR;
/*  31:    */   
/*  32:    */   static
/*  33:    */   {
/*  34: 45 */     logger = InternalLoggerFactory.getInstance(NativeLibraryLoader.class);
/*  35:    */     
/*  36:    */ 
/*  37:    */ 
/*  38:    */ 
/*  39:    */ 
/*  40:    */ 
/*  41: 52 */     String workdir = SystemPropertyUtil.get("io.netty.native.workdir");
/*  42: 53 */     if (workdir != null)
/*  43:    */     {
/*  44: 54 */       File f = new File(workdir);
/*  45: 55 */       f.mkdirs();
/*  46:    */       try
/*  47:    */       {
/*  48: 58 */         f = f.getAbsoluteFile();
/*  49:    */       }
/*  50:    */       catch (Exception localException) {}
/*  51: 63 */       WORKDIR = f;
/*  52: 64 */       logger.debug("-Dio.netty.native.workdir: " + WORKDIR);
/*  53:    */     }
/*  54:    */     else
/*  55:    */     {
/*  56: 66 */       WORKDIR = PlatformDependent.tmpdir();
/*  57: 67 */       logger.debug("-Dio.netty.native.workdir: " + WORKDIR + " (io.netty.tmpdir)");
/*  58:    */     }
/*  59:    */   }
/*  60:    */   
/*  61: 70 */   private static final boolean DELETE_NATIVE_LIB_AFTER_LOADING = SystemPropertyUtil.getBoolean("io.netty.native.deleteLibAfterLoading", true);
/*  62:    */   
/*  63:    */   public static void loadFirstAvailable(ClassLoader loader, String... names)
/*  64:    */   {
/*  65: 82 */     List<Throwable> suppressed = new ArrayList();
/*  66: 83 */     for (String name : names) {
/*  67:    */       try
/*  68:    */       {
/*  69: 85 */         load(name, loader);
/*  70: 86 */         return;
/*  71:    */       }
/*  72:    */       catch (Throwable t)
/*  73:    */       {
/*  74: 88 */         suppressed.add(t);
/*  75: 89 */         logger.debug("Unable to load the library '{}', trying next name...", name, t);
/*  76:    */       }
/*  77:    */     }
/*  78: 93 */     IllegalArgumentException iae = new IllegalArgumentException("Failed to load any of the given libraries: " + Arrays.toString(names));
/*  79: 94 */     ThrowableUtil.addSuppressedAndClear(iae, suppressed);
/*  80: 95 */     throw iae;
/*  81:    */   }
/*  82:    */   
/*  83:    */   private static String calculatePackagePrefix()
/*  84:    */   {
/*  85:104 */     String maybeShaded = NativeLibraryLoader.class.getName();
/*  86:    */     
/*  87:106 */     String expected = "io!netty!util!internal!NativeLibraryLoader".replace('!', '.');
/*  88:107 */     if (!maybeShaded.endsWith(expected)) {
/*  89:108 */       throw new UnsatisfiedLinkError(String.format("Could not find prefix added to %s to get %s. When shading, only adding a package prefix is supported", new Object[] { expected, maybeShaded }));
/*  90:    */     }
/*  91:112 */     return maybeShaded.substring(0, maybeShaded.length() - expected.length());
/*  92:    */   }
/*  93:    */   
/*  94:    */   public static void load(String originalName, ClassLoader loader)
/*  95:    */   {
/*  96:120 */     String name = calculatePackagePrefix().replace('.', '_') + originalName;
/*  97:121 */     List<Throwable> suppressed = new ArrayList();
/*  98:    */     try
/*  99:    */     {
/* 100:124 */       loadLibrary(loader, name, false);
/* 101:125 */       return;
/* 102:    */     }
/* 103:    */     catch (Throwable ex)
/* 104:    */     {
/* 105:127 */       suppressed.add(ex);
/* 106:128 */       logger.debug("{} cannot be loaded from java.libary.path, now trying export to -Dio.netty.native.workdir: {}", new Object[] { name, WORKDIR, ex });
/* 107:    */       
/* 108:    */ 
/* 109:    */ 
/* 110:    */ 
/* 111:133 */       String libname = System.mapLibraryName(name);
/* 112:134 */       String path = "META-INF/native/" + libname;
/* 113:    */       
/* 114:136 */       InputStream in = null;
/* 115:137 */       OutputStream out = null;
/* 116:138 */       File tmpFile = null;
/* 117:    */       URL url;
/* 118:    */       URL url;
/* 119:140 */       if (loader == null) {
/* 120:141 */         url = ClassLoader.getSystemResource(path);
/* 121:    */       } else {
/* 122:143 */         url = loader.getResource(path);
/* 123:    */       }
/* 124:    */       try
/* 125:    */       {
/* 126:146 */         if (url == null) {
/* 127:147 */           if (PlatformDependent.isOsx())
/* 128:    */           {
/* 129:148 */             String fileName = "META-INF/native/lib" + name + ".jnilib";
/* 130:150 */             if (loader == null) {
/* 131:151 */               url = ClassLoader.getSystemResource(fileName);
/* 132:    */             } else {
/* 133:153 */               url = loader.getResource(fileName);
/* 134:    */             }
/* 135:155 */             if (url == null)
/* 136:    */             {
/* 137:156 */               FileNotFoundException fnf = new FileNotFoundException(fileName);
/* 138:157 */               ThrowableUtil.addSuppressedAndClear(fnf, suppressed);
/* 139:158 */               throw fnf;
/* 140:    */             }
/* 141:    */           }
/* 142:    */           else
/* 143:    */           {
/* 144:161 */             FileNotFoundException fnf = new FileNotFoundException(path);
/* 145:162 */             ThrowableUtil.addSuppressedAndClear(fnf, suppressed);
/* 146:163 */             throw fnf;
/* 147:    */           }
/* 148:    */         }
/* 149:167 */         int index = libname.lastIndexOf('.');
/* 150:168 */         String prefix = libname.substring(0, index);
/* 151:169 */         String suffix = libname.substring(index, libname.length());
/* 152:    */         
/* 153:171 */         tmpFile = File.createTempFile(prefix, suffix, WORKDIR);
/* 154:172 */         in = url.openStream();
/* 155:173 */         out = new FileOutputStream(tmpFile);
/* 156:    */         
/* 157:175 */         byte[] buffer = new byte[8192];
/* 158:    */         int length;
/* 159:177 */         while ((length = in.read(buffer)) > 0) {
/* 160:178 */           out.write(buffer, 0, length);
/* 161:    */         }
/* 162:180 */         out.flush();
/* 163:    */         
/* 164:    */ 
/* 165:    */ 
/* 166:184 */         closeQuietly(out);
/* 167:185 */         out = null;
/* 168:    */         
/* 169:187 */         loadLibrary(loader, tmpFile.getPath(), true);
/* 170:    */       }
/* 171:    */       catch (UnsatisfiedLinkError e)
/* 172:    */       {
/* 173:    */         try
/* 174:    */         {
/* 175:190 */           if ((tmpFile != null) && (tmpFile.isFile()) && (tmpFile.canRead()) && 
/* 176:191 */             (!NoexecVolumeDetector.canExecuteExecutable(tmpFile))) {
/* 177:192 */             logger.info("{} exists but cannot be executed even when execute permissions set; check volume for \"noexec\" flag; use -Dio.netty.native.workdir=[path] to set native working directory separately.", tmpFile
/* 178:    */             
/* 179:    */ 
/* 180:195 */               .getPath());
/* 181:    */           }
/* 182:    */         }
/* 183:    */         catch (Throwable t)
/* 184:    */         {
/* 185:198 */           suppressed.add(t);
/* 186:199 */           logger.debug("Error checking if {} is on a file store mounted with noexec", tmpFile, t);
/* 187:    */         }
/* 188:202 */         ThrowableUtil.addSuppressedAndClear(e, suppressed);
/* 189:203 */         throw e;
/* 190:    */       }
/* 191:    */       catch (Exception e)
/* 192:    */       {
/* 193:205 */         UnsatisfiedLinkError ule = new UnsatisfiedLinkError("could not load a native library: " + name);
/* 194:206 */         ule.initCause(e);
/* 195:207 */         ThrowableUtil.addSuppressedAndClear(ule, suppressed);
/* 196:208 */         throw ule;
/* 197:    */       }
/* 198:    */       finally
/* 199:    */       {
/* 200:210 */         closeQuietly(in);
/* 201:211 */         closeQuietly(out);
/* 202:215 */         if ((tmpFile != null) && ((!DELETE_NATIVE_LIB_AFTER_LOADING) || (!tmpFile.delete()))) {
/* 203:216 */           tmpFile.deleteOnExit();
/* 204:    */         }
/* 205:    */       }
/* 206:    */     }
/* 207:    */   }
/* 208:    */   
/* 209:    */   private static void loadLibrary(ClassLoader loader, String name, boolean absolute)
/* 210:    */   {
/* 211:228 */     Throwable suppressed = null;
/* 212:    */     try
/* 213:    */     {
/* 214:    */       try
/* 215:    */       {
/* 216:232 */         Class<?> newHelper = tryToLoadClass(loader, NativeLibraryUtil.class);
/* 217:233 */         loadLibraryByHelper(newHelper, name, absolute);
/* 218:234 */         logger.debug("Successfully loaded the library {}", name);
/* 219:235 */         return;
/* 220:    */       }
/* 221:    */       catch (UnsatisfiedLinkError e)
/* 222:    */       {
/* 223:237 */         suppressed = e;
/* 224:238 */         logger.debug("Unable to load the library '{}', trying other loading mechanism.", name, e);
/* 225:    */       }
/* 226:    */       catch (Exception e)
/* 227:    */       {
/* 228:240 */         suppressed = e;
/* 229:241 */         logger.debug("Unable to load the library '{}', trying other loading mechanism.", name, e);
/* 230:    */       }
/* 231:243 */       NativeLibraryUtil.loadLibrary(name, absolute);
/* 232:244 */       logger.debug("Successfully loaded the library {}", name);
/* 233:    */     }
/* 234:    */     catch (UnsatisfiedLinkError ule)
/* 235:    */     {
/* 236:246 */       if (suppressed != null) {
/* 237:247 */         ThrowableUtil.addSuppressed(ule, suppressed);
/* 238:    */       }
/* 239:249 */       throw ule;
/* 240:    */     }
/* 241:    */   }
/* 242:    */   
/* 243:    */   private static void loadLibraryByHelper(Class<?> helper, final String name, final boolean absolute)
/* 244:    */     throws UnsatisfiedLinkError
/* 245:    */   {
/* 246:255 */     Object ret = AccessController.doPrivileged(new PrivilegedAction()
/* 247:    */     {
/* 248:    */       public Object run()
/* 249:    */       {
/* 250:    */         try
/* 251:    */         {
/* 252:261 */           Method method = this.val$helper.getMethod("loadLibrary", new Class[] { String.class, Boolean.TYPE });
/* 253:262 */           method.setAccessible(true);
/* 254:263 */           return method.invoke(null, new Object[] { name, Boolean.valueOf(absolute) });
/* 255:    */         }
/* 256:    */         catch (Exception e)
/* 257:    */         {
/* 258:265 */           return e;
/* 259:    */         }
/* 260:    */       }
/* 261:    */     });
/* 262:269 */     if ((ret instanceof Throwable))
/* 263:    */     {
/* 264:270 */       Throwable t = (Throwable)ret;
/* 265:271 */       assert (!(t instanceof UnsatisfiedLinkError)) : (t + " should be a wrapper throwable");
/* 266:272 */       Throwable cause = t.getCause();
/* 267:273 */       if ((cause instanceof UnsatisfiedLinkError)) {
/* 268:274 */         throw ((UnsatisfiedLinkError)cause);
/* 269:    */       }
/* 270:276 */       UnsatisfiedLinkError ule = new UnsatisfiedLinkError(t.getMessage());
/* 271:277 */       ule.initCause(t);
/* 272:278 */       throw ule;
/* 273:    */     }
/* 274:    */   }
/* 275:    */   
/* 276:    */   private static Class<?> tryToLoadClass(ClassLoader loader, final Class<?> helper)
/* 277:    */     throws ClassNotFoundException
/* 278:    */   {
/* 279:    */     try
/* 280:    */     {
/* 281:292 */       return Class.forName(helper.getName(), false, loader);
/* 282:    */     }
/* 283:    */     catch (ClassNotFoundException e1)
/* 284:    */     {
/* 285:294 */       if (loader == null) {
/* 286:296 */         throw e1;
/* 287:    */       }
/* 288:    */       try
/* 289:    */       {
/* 290:300 */         final byte[] classBinary = classToByteArray(helper);
/* 291:301 */         (Class)AccessController.doPrivileged(new PrivilegedAction()
/* 292:    */         {
/* 293:    */           public Class<?> run()
/* 294:    */           {
/* 295:    */             try
/* 296:    */             {
/* 297:307 */               Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", new Class[] { String.class, [B.class, Integer.TYPE, Integer.TYPE });
/* 298:    */               
/* 299:309 */               defineClass.setAccessible(true);
/* 300:310 */               return (Class)defineClass.invoke(this.val$loader, new Object[] { helper.getName(), classBinary, Integer.valueOf(0), 
/* 301:311 */                 Integer.valueOf(classBinary.length) });
/* 302:    */             }
/* 303:    */             catch (Exception e)
/* 304:    */             {
/* 305:313 */               throw new IllegalStateException("Define class failed!", e);
/* 306:    */             }
/* 307:    */           }
/* 308:    */         });
/* 309:    */       }
/* 310:    */       catch (ClassNotFoundException e2)
/* 311:    */       {
/* 312:318 */         ThrowableUtil.addSuppressed(e2, e1);
/* 313:319 */         throw e2;
/* 314:    */       }
/* 315:    */       catch (RuntimeException e2)
/* 316:    */       {
/* 317:321 */         ThrowableUtil.addSuppressed(e2, e1);
/* 318:322 */         throw e2;
/* 319:    */       }
/* 320:    */       catch (Error e2)
/* 321:    */       {
/* 322:324 */         ThrowableUtil.addSuppressed(e2, e1);
/* 323:325 */         throw e2;
/* 324:    */       }
/* 325:    */     }
/* 326:    */   }
/* 327:    */   
/* 328:    */   private static byte[] classToByteArray(Class<?> clazz)
/* 329:    */     throws ClassNotFoundException
/* 330:    */   {
/* 331:337 */     String fileName = clazz.getName();
/* 332:338 */     int lastDot = fileName.lastIndexOf('.');
/* 333:339 */     if (lastDot > 0) {
/* 334:340 */       fileName = fileName.substring(lastDot + 1);
/* 335:    */     }
/* 336:342 */     URL classUrl = clazz.getResource(fileName + ".class");
/* 337:343 */     if (classUrl == null) {
/* 338:344 */       throw new ClassNotFoundException(clazz.getName());
/* 339:    */     }
/* 340:346 */     byte[] buf = new byte[1024];
/* 341:347 */     ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
/* 342:348 */     InputStream in = null;
/* 343:    */     try
/* 344:    */     {
/* 345:350 */       in = classUrl.openStream();
/* 346:    */       int r;
/* 347:351 */       while ((r = in.read(buf)) != -1) {
/* 348:352 */         out.write(buf, 0, r);
/* 349:    */       }
/* 350:354 */       return out.toByteArray();
/* 351:    */     }
/* 352:    */     catch (IOException ex)
/* 353:    */     {
/* 354:356 */       throw new ClassNotFoundException(clazz.getName(), ex);
/* 355:    */     }
/* 356:    */     finally
/* 357:    */     {
/* 358:358 */       closeQuietly(in);
/* 359:359 */       closeQuietly(out);
/* 360:    */     }
/* 361:    */   }
/* 362:    */   
/* 363:    */   private static void closeQuietly(Closeable c)
/* 364:    */   {
/* 365:364 */     if (c != null) {
/* 366:    */       try
/* 367:    */       {
/* 368:366 */         c.close();
/* 369:    */       }
/* 370:    */       catch (IOException localIOException) {}
/* 371:    */     }
/* 372:    */   }
/* 373:    */   
/* 374:    */   private static final class NoexecVolumeDetector
/* 375:    */   {
/* 376:    */     private static boolean canExecuteExecutable(File file)
/* 377:    */       throws IOException
/* 378:    */     {
/* 379:380 */       if (PlatformDependent.javaVersion() < 7) {
/* 380:383 */         return true;
/* 381:    */       }
/* 382:387 */       if (file.canExecute()) {
/* 383:388 */         return true;
/* 384:    */       }
/* 385:398 */       Set<PosixFilePermission> existingFilePermissions = Files.getPosixFilePermissions(file.toPath(), new LinkOption[0]);
/* 386:    */       
/* 387:400 */       Set<PosixFilePermission> executePermissions = EnumSet.of(PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.GROUP_EXECUTE, PosixFilePermission.OTHERS_EXECUTE);
/* 388:403 */       if (existingFilePermissions.containsAll(executePermissions)) {
/* 389:404 */         return false;
/* 390:    */       }
/* 391:407 */       Set<PosixFilePermission> newPermissions = EnumSet.copyOf(existingFilePermissions);
/* 392:408 */       newPermissions.addAll(executePermissions);
/* 393:409 */       Files.setPosixFilePermissions(file.toPath(), newPermissions);
/* 394:410 */       return file.canExecute();
/* 395:    */     }
/* 396:    */   }
/* 397:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.NativeLibraryLoader
 * JD-Core Version:    0.7.0.1
 */