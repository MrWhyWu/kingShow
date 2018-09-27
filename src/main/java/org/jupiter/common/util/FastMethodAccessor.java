package org.jupiter.common.util;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentMap;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;























public abstract class FastMethodAccessor
{
  private static final ConcurrentMap<Class<?>, FastMethodAccessor> fastAccessorCache = ;
  


  private String[] methodNames;
  


  private Class<?>[][] parameterTypes_s;
  



  public FastMethodAccessor() {}
  



  public abstract Object invoke(Object paramObject, int paramInt, Object... paramVarArgs);
  



  public Object invoke(Object obj, String methodName, Class<?>[] parameterTypes, Object... args)
  {
    return invoke(obj, getIndex(methodName, parameterTypes), args);
  }
  
  public int getIndex(String methodName, Class<?>... parameterTypes) {
    for (int i = 0; i < methodNames.length; i++) {
      if ((methodNames[i].equals(methodName)) && (Arrays.equals(parameterTypes, parameterTypes_s[i]))) {
        return i;
      }
    }
    throw new IllegalArgumentException("Unable to find non-private method: " + methodName + " " + Arrays.toString(parameterTypes));
  }
  
  public static FastMethodAccessor get(Class<?> type)
  {
    FastMethodAccessor accessor = (FastMethodAccessor)fastAccessorCache.get(type);
    if (accessor == null)
    {
      synchronized (getClassLock(type)) {
        accessor = (FastMethodAccessor)fastAccessorCache.get(type);
        if (accessor == null) {
          accessor = create(type);
          fastAccessorCache.put(type, accessor);
        }
        return accessor;
      }
    }
    return accessor;
  }
  
  private static Class<?> getClassLock(Class<?> type) {
    return type;
  }
  
  private static FastMethodAccessor create(Class<?> type) {
    ArrayList<Method> methods = Lists.newArrayList();
    boolean isInterface = type.isInterface();
    if (!isInterface) {
      Class nextClass = type;
      while (nextClass != Object.class) {
        addDeclaredMethodsToList(nextClass, methods);
        nextClass = nextClass.getSuperclass();
      }
    } else {
      recursiveAddInterfaceMethodsToList(type, methods);
    }
    
    int n = methods.size();
    String[] methodNames = new String[n];
    Class<?>[][] parameterTypes_s = new Class[n][];
    Class<?>[] returnTypes = new Class[n];
    for (int i = 0; i < n; i++) {
      Method method = (Method)methods.get(i);
      methodNames[i] = method.getName();
      parameterTypes_s[i] = method.getParameterTypes();
      returnTypes[i] = method.getReturnType();
    }
    
    String className = type.getName();
    String accessorClassName = className + "_FastMethodAccessor";
    String accessorClassNameInternal = accessorClassName.replace('.', '/');
    String classNameInternal = className.replace('.', '/');
    String superClassNameInternal = FastMethodAccessor.class.getName().replace('.', '/');
    
    ClassWriter cw = new ClassWriter(1);
    
    cw.visit(196653, 33, accessorClassNameInternal, null, superClassNameInternal, null);
    


    MethodVisitor mv = cw.visitMethod(1, "<init>", "()V", null, null);
    mv.visitCode();
    mv.visitVarInsn(25, 0);
    mv.visitMethodInsn(183, superClassNameInternal, "<init>", "()V", false);
    mv.visitInsn(177);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
    



    mv = cw.visitMethod(129, "invoke", "(Ljava/lang/Object;I[Ljava/lang/Object;)Ljava/lang/Object;", null, null);
    mv.visitCode();
    
    if (n > 0)
    {
      mv.visitVarInsn(25, 1);
      mv.visitTypeInsn(192, classNameInternal);
      mv.visitVarInsn(58, 4);
      

      mv.visitVarInsn(21, 2);
      Label[] labels = new Label[n];
      for (int i = 0; i < n; i++) {
        labels[i] = new Label();
      }
      Label defaultLabel = new Label();
      mv.visitTableSwitchInsn(0, labels.length - 1, defaultLabel, labels);
      
      StringBuilder buf = new StringBuilder(128);
      for (int i = 0; i < n; i++) {
        mv.visitLabel(labels[i]);
        if (i == 0) {
          mv.visitFrame(1, 1, new Object[] { classNameInternal }, 0, null);
        } else {
          mv.visitFrame(3, 0, null, 0, null);
        }
        mv.visitVarInsn(25, 4);
        
        buf.setLength(0);
        buf.append('(');
        
        Class<?>[] parameterTypes = parameterTypes_s[i];
        Class<?> returnType = returnTypes[i];
        for (int p_index = 0; p_index < parameterTypes.length; p_index++) {
          mv.visitVarInsn(25, 3);
          mv.visitIntInsn(16, p_index);
          mv.visitInsn(50);
          

          Type p_type = Type.getType(parameterTypes[p_index]);
          switch (p_type.getSort()) {
          case 1: 
            mv.visitTypeInsn(192, "java/lang/Boolean");
            mv.visitMethodInsn(182, "java/lang/Boolean", "booleanValue", "()Z", false);
            break;
          case 3: 
            mv.visitTypeInsn(192, "java/lang/Byte");
            mv.visitMethodInsn(182, "java/lang/Byte", "byteValue", "()B", false);
            break;
          case 2: 
            mv.visitTypeInsn(192, "java/lang/Character");
            mv.visitMethodInsn(182, "java/lang/Character", "charValue", "()C", false);
            break;
          case 4: 
            mv.visitTypeInsn(192, "java/lang/Short");
            mv.visitMethodInsn(182, "java/lang/Short", "shortValue", "()S", false);
            break;
          case 5: 
            mv.visitTypeInsn(192, "java/lang/Integer");
            mv.visitMethodInsn(182, "java/lang/Integer", "intValue", "()I", false);
            break;
          case 6: 
            mv.visitTypeInsn(192, "java/lang/Float");
            mv.visitMethodInsn(182, "java/lang/Float", "floatValue", "()F", false);
            break;
          case 7: 
            mv.visitTypeInsn(192, "java/lang/Long");
            mv.visitMethodInsn(182, "java/lang/Long", "longValue", "()J", false);
            break;
          case 8: 
            mv.visitTypeInsn(192, "java/lang/Double");
            mv.visitMethodInsn(182, "java/lang/Double", "doubleValue", "()D", false);
            break;
          case 9: 
            mv.visitTypeInsn(192, p_type.getDescriptor());
            break;
          case 10: 
            mv.visitTypeInsn(192, p_type.getInternalName());
          }
          
          buf.append(p_type.getDescriptor());
        }
        
        buf.append(')').append(Type.getDescriptor(returnType));
        

        if (isInterface) {
          mv.visitMethodInsn(185, classNameInternal, methodNames[i], buf.toString(), true);
        } else if (Modifier.isStatic(((Method)methods.get(i)).getModifiers())) {
          mv.visitMethodInsn(184, classNameInternal, methodNames[i], buf.toString(), false);
        } else {
          mv.visitMethodInsn(182, classNameInternal, methodNames[i], buf.toString(), false);
        }
        
        Type r_type = Type.getType(returnType);
        switch (r_type.getSort()) {
        case 0: 
          mv.visitInsn(1);
          break;
        case 1: 
          mv.visitMethodInsn(184, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
          break;
        case 3: 
          mv.visitMethodInsn(184, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
          break;
        case 2: 
          mv.visitMethodInsn(184, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
          break;
        case 4: 
          mv.visitMethodInsn(184, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
          break;
        case 5: 
          mv.visitMethodInsn(184, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
          break;
        case 6: 
          mv.visitMethodInsn(184, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
          break;
        case 7: 
          mv.visitMethodInsn(184, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
          break;
        case 8: 
          mv.visitMethodInsn(184, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
        }
        
        mv.visitInsn(176);
      }
      mv.visitLabel(defaultLabel);
      mv.visitFrame(3, 0, null, 0, null);
    }
    

    mv.visitTypeInsn(187, "java/lang/IllegalArgumentException");
    mv.visitInsn(89);
    mv.visitTypeInsn(187, "java/lang/StringBuilder");
    mv.visitInsn(89);
    mv.visitLdcInsn("Method not found: ");
    mv.visitMethodInsn(183, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
    mv.visitVarInsn(21, 2);
    mv.visitMethodInsn(182, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;", false);
    mv.visitMethodInsn(182, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
    mv.visitMethodInsn(183, "java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V", false);
    mv.visitInsn(191);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
    

    cw.visitEnd();
    byte[] bytes = cw.toByteArray();
    
    AccessorClassLoader loader = AccessorClassLoader.get(type);
    Class<?> accessorClass = loader.defineClass(accessorClassName, bytes);
    try {
      FastMethodAccessor accessor = (FastMethodAccessor)accessorClass.newInstance();
      methodNames = methodNames;
      parameterTypes_s = parameterTypes_s;
      return accessor;
    } catch (Throwable t) {
      throw new RuntimeException("Error constructing method access class: " + accessorClass, t);
    }
  }
  
  private static void addDeclaredMethodsToList(Class<?> type, ArrayList<Method> methods) {
    Method[] declaredMethods = type.getDeclaredMethods();
    for (Method method : declaredMethods) {
      if (!Modifier.isPrivate(method.getModifiers())) {
        methods.add(method);
      }
    }
  }
  
  private static void recursiveAddInterfaceMethodsToList(Class<?> interfaceType, ArrayList<Method> methods) {
    addDeclaredMethodsToList(interfaceType, methods);
    for (Class nextInterface : interfaceType.getInterfaces()) {
      recursiveAddInterfaceMethodsToList(nextInterface, methods);
    }
  }
  
  static class AccessorClassLoader extends ClassLoader
  {
    private static final WeakHashMap<ClassLoader, WeakReference<AccessorClassLoader>> accessorClassLoaders = new WeakHashMap();
    
    private static final ClassLoader selfContextParentClassLoader = getParentClassLoader(AccessorClassLoader.class);
    private static volatile AccessorClassLoader selfContextAccessorClassLoader = new AccessorClassLoader(selfContextParentClassLoader);
    
    public AccessorClassLoader(ClassLoader parent) {
      super();
    }
    
    Class<?> defineClass(String name, byte[] bytes) throws ClassFormatError {
      return defineClass(name, bytes, 0, bytes.length, getClass().getProtectionDomain());
    }
    
    static AccessorClassLoader get(Class<?> type) {
      ClassLoader parent = getParentClassLoader(type);
      

      if (selfContextParentClassLoader.equals(parent)) {
        if (selfContextAccessorClassLoader == null) {
          synchronized (accessorClassLoaders) {
            if (selfContextAccessorClassLoader == null)
              selfContextAccessorClassLoader = new AccessorClassLoader(selfContextParentClassLoader);
          }
        }
        return selfContextAccessorClassLoader;
      }
      

      synchronized (accessorClassLoaders) {
        Object ref = (WeakReference)accessorClassLoaders.get(parent);
        if (ref != null) {
          AccessorClassLoader accessorClassLoader = (AccessorClassLoader)((WeakReference)ref).get();
          if (accessorClassLoader != null) {
            return accessorClassLoader;
          }
          accessorClassLoaders.remove(parent);
        }
        
        AccessorClassLoader accessorClassLoader = new AccessorClassLoader(parent);
        accessorClassLoaders.put(parent, new WeakReference(accessorClassLoader));
        return accessorClassLoader;
      }
    }
    
    private static ClassLoader getParentClassLoader(Class<?> type) {
      ClassLoader parent = type.getClassLoader();
      if (parent == null) {
        parent = ClassLoader.getSystemClassLoader();
      }
      return parent;
    }
  }
}
