package org.jupiter.common.util;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;









































public abstract class ConstantPool<T extends Constant<T>>
{
  private final ConcurrentMap<String, T> constants = Maps.newConcurrentMap();
  
  private final AtomicInteger nextId = new AtomicInteger(1);
  
  public ConstantPool() {}
  
  public T valueOf(Class<?> firstNameComponent, String secondNameComponent)
  {
    Preconditions.checkNotNull(firstNameComponent, "firstNameComponent");
    Preconditions.checkNotNull(secondNameComponent, "secondNameComponent");
    
    return valueOf(firstNameComponent.getName() + '#' + secondNameComponent);
  }
  







  public T valueOf(String name)
  {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "empty name");
    return getOrCreate(name);
  }
  




  private T getOrCreate(String name)
  {
    T constant = (Constant)constants.get(name);
    if (constant == null) {
      T newConstant = newConstant(nextId.getAndIncrement(), name);
      constant = (Constant)constants.putIfAbsent(name, newConstant);
      if (constant == null) {
        constant = newConstant;
      }
    }
    return constant;
  }
  


  public boolean exists(String name)
  {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "empty name");
    return constants.containsKey(name);
  }
  



  public T newInstance(String name)
  {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "empty name");
    return createOrThrow(name);
  }
  




  private T createOrThrow(String name)
  {
    T constant = (Constant)constants.get(name);
    if (constant == null) {
      T newConstant = newConstant(nextId.getAndIncrement(), name);
      constant = (Constant)constants.putIfAbsent(name, newConstant);
      if (constant == null) {
        return newConstant;
      }
    }
    
    throw new IllegalArgumentException(String.format("'%s' is already in use", new Object[] { name }));
  }
  
  protected abstract T newConstant(int paramInt, String paramString);
}
