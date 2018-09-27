package org.jupiter.common.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;






























public final class Reflects
{
  private static final Map<Class<?>, Class<?>> primitiveWrapperMap = ;
  private static final Map<Class<?>, Class<?>> wrapperPrimitiveMap;
  
  static { primitiveWrapperMap.put(Boolean.TYPE, Boolean.class);
    primitiveWrapperMap.put(Byte.TYPE, Byte.class);
    primitiveWrapperMap.put(Character.TYPE, Character.class);
    primitiveWrapperMap.put(Short.TYPE, Short.class);
    primitiveWrapperMap.put(Integer.TYPE, Integer.class);
    primitiveWrapperMap.put(Long.TYPE, Long.class);
    primitiveWrapperMap.put(Double.TYPE, Double.class);
    primitiveWrapperMap.put(Float.TYPE, Float.class);
    primitiveWrapperMap.put(Void.TYPE, Void.TYPE);
    




    wrapperPrimitiveMap = Maps.newIdentityHashMap();
    

    for (Map.Entry<Class<?>, Class<?>> entry : primitiveWrapperMap.entrySet()) {
      Class<?> wrapperClass = (Class)entry.getValue();
      Class<?> primitiveClass = (Class)entry.getKey();
      if (!primitiveClass.equals(wrapperClass)) {
        wrapperPrimitiveMap.put(wrapperClass, primitiveClass);
      }
    }
  }
  



  private static final Class<?>[] ORDERED_PRIMITIVE_TYPES = { Byte.TYPE, Short.TYPE, Character.TYPE, Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE };
  
















  public static Object fastInvoke(Object obj, String methodName, Class<?>[] parameterTypes, Object[] args)
  {
    FastMethodAccessor accessor = FastMethodAccessor.get(obj.getClass());
    return accessor.invoke(obj, methodName, parameterTypes, args);
  }
  









  public static Field getField(Class<?> clazz, String name)
    throws NoSuchFieldException
  {
    for (Class<?> cls = (Class)Preconditions.checkNotNull(clazz, "class"); cls != null; cls = cls.getSuperclass()) {
      try {
        return cls.getDeclaredField(name);
      } catch (Throwable localThrowable) {}
    }
    throw new NoSuchFieldException(clazz.getName() + "#" + name);
  }
  







  public static Object getStaticValue(Class<?> clazz, String name)
  {
    Object value = null;
    try {
      Field fd = setAccessible(getField(clazz, name));
      value = fd.get(null);
    } catch (Exception e) {
      ThrowUtil.throwException(e);
    }
    return value;
  }
  







  public static void setStaticValue(Class<?> clazz, String name, Object value)
  {
    try
    {
      Field fd = setAccessible(getField(clazz, name));
      fd.set(null, value);
    } catch (Exception e) {
      ThrowUtil.throwException(e);
    }
  }
  







  public static Object getValue(Object o, String name)
  {
    Object value = null;
    try {
      Field fd = setAccessible(getField(o.getClass(), name));
      value = fd.get(o);
    } catch (Exception e) {
      ThrowUtil.throwException(e);
    }
    return value;
  }
  






  public static void setValue(Object o, String name, Object value)
  {
    try
    {
      Field fd = setAccessible(getField(o.getClass(), name));
      fd.set(o, value);
    } catch (Exception e) {
      ThrowUtil.throwException(e);
    }
  }
  


  public static Object getTypeDefaultValue(Class<?> clazz)
  {
    Preconditions.checkNotNull(clazz, "clazz");
    
    if (clazz.isPrimitive()) {
      if (clazz == Byte.TYPE) {
        return Byte.valueOf((byte)0);
      }
      if (clazz == Short.TYPE) {
        return Short.valueOf((short)0);
      }
      if (clazz == Integer.TYPE) {
        return Integer.valueOf(0);
      }
      if (clazz == Long.TYPE) {
        return Long.valueOf(0L);
      }
      if (clazz == Float.TYPE) {
        return Float.valueOf(0.0F);
      }
      if (clazz == Double.TYPE) {
        return Double.valueOf(0.0D);
      }
      if (clazz == Character.TYPE) {
        return Character.valueOf('\000');
      }
      if (clazz == Boolean.TYPE) {
        return Boolean.valueOf(false);
      }
    }
    return null;
  }
  


  public static String simpleClassName(Object o)
  {
    return o == null ? "null_object" : simpleClassName(o.getClass());
  }
  



  public static String simpleClassName(Class<?> clazz)
  {
    if (clazz == null) {
      return "null_class";
    }
    
    Package pkg = clazz.getPackage();
    return pkg == null ? clazz.getName() : clazz.getName().substring(pkg.getName().length() + 1);
  }
  


  public static Class<?>[] findMatchingParameterTypes(List<Class<?>[]> parameterTypesList, Object[] args)
  {
    if (parameterTypesList.size() == 1) {
      return (Class[])parameterTypesList.get(0);
    }
    
    Class<?>[] parameterTypes;
    Class<?>[] parameterTypes;
    if ((args == null) || (args.length == 0)) {
      parameterTypes = new Class[0];
    } else {
      parameterTypes = new Class[args.length];
      for (int i = 0; i < args.length; i++) {
        parameterTypes[i] = args[i].getClass();
      }
    }
    
    Class<?>[] bestMatch = null;
    for (Class<?>[] pTypes : parameterTypesList) {
      if ((isAssignable(parameterTypes, pTypes, true)) && (
        (bestMatch == null) || (compareParameterTypes(pTypes, bestMatch, parameterTypes) < 0)))
      {
        bestMatch = pTypes;
      }
    }
    

    return bestMatch;
  }
  


  public static <Ext> Pair<Class<?>[], Ext> findMatchingParameterTypesExt(List<Pair<Class<?>[], Ext>> pairs, Object[] args)
  {
    if (pairs.size() == 1) {
      return (Pair)pairs.get(0);
    }
    
    Class<?>[] parameterTypes;
    Class<?>[] parameterTypes;
    if ((args == null) || (args.length == 0)) {
      parameterTypes = new Class[0];
    } else {
      parameterTypes = new Class[args.length];
      for (int i = 0; i < args.length; i++) {
        parameterTypes[i] = args[i].getClass();
      }
    }
    
    Pair<Class<?>[], Ext> bestMatch = null;
    for (Pair<Class<?>[], Ext> pair : pairs) {
      Class<?>[] pTypes = (Class[])pair.getFirst();
      if ((isAssignable(parameterTypes, pTypes, true)) && (
        (bestMatch == null) || (compareParameterTypes(pTypes, (Class[])bestMatch.getFirst(), parameterTypes) < 0)))
      {
        bestMatch = pair;
      }
    }
    

    return bestMatch;
  }
  


  public static boolean isAssignable(Class<?>[] classArray, Class<?>[] toClassArray, boolean autoboxing)
  {
    if (classArray.length != toClassArray.length) {
      return false;
    }
    
    for (int i = 0; i < classArray.length; i++) {
      if (!isAssignable(classArray[i], toClassArray[i], autoboxing)) {
        return false;
      }
    }
    
    return true;
  }
  


  public static boolean isAssignable(Class<?> cls, Class<?> toClass, boolean autoboxing)
  {
    if (toClass == null) {
      return false;
    }
    

    if (cls == null) {
      return !toClass.isPrimitive();
    }
    

    if (autoboxing) {
      if ((cls.isPrimitive()) && (!toClass.isPrimitive())) {
        cls = primitiveToWrapper(cls);
        if (cls == null) {
          return false;
        }
      }
      if ((toClass.isPrimitive()) && (!cls.isPrimitive())) {
        cls = wrapperToPrimitive(cls);
        if (cls == null) {
          return false;
        }
      }
    }
    
    if (cls.equals(toClass)) {
      return true;
    }
    

    if (cls.isPrimitive()) {
      if (!toClass.isPrimitive()) {
        return false;
      }
      if (Boolean.TYPE.equals(cls)) {
        return false;
      }
      if (Integer.TYPE.equals(cls)) {
        return (Long.TYPE.equals(toClass)) || (Float.TYPE.equals(toClass)) || (Double.TYPE.equals(toClass));
      }
      

      if (Long.TYPE.equals(cls)) {
        return (Float.TYPE.equals(toClass)) || (Double.TYPE.equals(toClass));
      }
      
      if (Float.TYPE.equals(cls)) {
        return Double.TYPE.equals(toClass);
      }
      if (Double.TYPE.equals(cls)) {
        return false;
      }
      if (Character.TYPE.equals(cls)) {
        return (Integer.TYPE.equals(toClass)) || (Long.TYPE.equals(toClass)) || (Float.TYPE.equals(toClass)) || (Double.TYPE.equals(toClass));
      }
      


      if (Short.TYPE.equals(cls)) {
        return (Integer.TYPE.equals(toClass)) || (Long.TYPE.equals(toClass)) || (Float.TYPE.equals(toClass)) || (Double.TYPE.equals(toClass));
      }
      


      if (Byte.TYPE.equals(cls)) {
        return (Short.TYPE.equals(toClass)) || (Integer.TYPE.equals(toClass)) || (Long.TYPE.equals(toClass)) || (Float.TYPE.equals(toClass)) || (Double.TYPE.equals(toClass));
      }
      




      return false;
    }
    
    return toClass.isAssignableFrom(cls);
  }
  



  public static Class<?> primitiveToWrapper(Class<?> cls)
  {
    Class<?> convertedClass = cls;
    if ((cls != null) && (cls.isPrimitive())) {
      convertedClass = (Class)primitiveWrapperMap.get(cls);
    }
    return convertedClass;
  }
  



  public static Class<?> wrapperToPrimitive(Class<?> cls)
  {
    return (Class)wrapperPrimitiveMap.get(cls);
  }
  











  private static int compareParameterTypes(Class<?>[] left, Class<?>[] right, Class<?>[] actual)
  {
    float leftCost = getTotalTransformationCost(actual, left);
    float rightCost = getTotalTransformationCost(actual, right);
    return Float.compare(leftCost, rightCost);
  }
  







  private static float getTotalTransformationCost(Class<?>[] srcArgs, Class<?>[] dstArgs)
  {
    float totalCost = 0.0F;
    for (int i = 0; i < srcArgs.length; i++)
    {
      Class<?> srcClass = srcArgs[i];
      Class<?> dstClass = dstArgs[i];
      totalCost += getObjectTransformationCost(srcClass, dstClass);
    }
    return totalCost;
  }
  








  private static float getObjectTransformationCost(Class<?> srcClass, Class<?> dstClass)
  {
    if (dstClass.isPrimitive()) {
      return getPrimitivePromotionCost(srcClass, dstClass);
    }
    float cost = 0.0F;
    while ((srcClass != null) && (!dstClass.equals(srcClass))) {
      if ((dstClass.isInterface()) && (isAssignable(srcClass, dstClass, true)))
      {




        cost += 0.25F;
        break;
      }
      cost += 1.0F;
      srcClass = srcClass.getSuperclass();
    }
    



    if (srcClass == null) {
      cost += 1.5F;
    }
    return cost;
  }
  







  private static float getPrimitivePromotionCost(Class<?> srcClass, Class<?> dstClass)
  {
    float cost = 0.0F;
    Class<?> cls = srcClass;
    if (!cls.isPrimitive())
    {
      cost += 0.1F;
      cls = wrapperToPrimitive(cls);
    }
    for (int i = 0; (cls != dstClass) && (i < ORDERED_PRIMITIVE_TYPES.length); i++) {
      if (cls == ORDERED_PRIMITIVE_TYPES[i]) {
        cost += 0.1F;
        if (i < ORDERED_PRIMITIVE_TYPES.length - 1) {
          cls = ORDERED_PRIMITIVE_TYPES[(i + 1)];
        }
      }
    }
    return cost;
  }
  





  private static Field setAccessible(Field fd)
  {
    if ((!Modifier.isPublic(fd.getModifiers())) || (!Modifier.isPublic(fd.getDeclaringClass().getModifiers()))) {
      fd.setAccessible(true);
    }
    return fd;
  }
  
  private Reflects() {}
}
