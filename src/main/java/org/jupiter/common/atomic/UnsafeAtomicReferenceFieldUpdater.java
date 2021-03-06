package org.jupiter.common.atomic;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import sun.misc.Unsafe;





















final class UnsafeAtomicReferenceFieldUpdater<U, W>
  extends AtomicReferenceFieldUpdater<U, W>
{
  private final long offset;
  private final Unsafe unsafe;
  
  UnsafeAtomicReferenceFieldUpdater(Unsafe unsafe, Class<U> tClass, String fieldName)
    throws NoSuchFieldException
  {
    Field field = tClass.getDeclaredField(fieldName);
    if (!Modifier.isVolatile(field.getModifiers())) {
      throw new IllegalArgumentException("Field [" + fieldName + "] must be volatile");
    }
    if (unsafe == null) {
      throw new NullPointerException("unsafe");
    }
    this.unsafe = unsafe;
    offset = unsafe.objectFieldOffset(field);
  }
  
  public boolean compareAndSet(U obj, W expect, W update)
  {
    return unsafe.compareAndSwapObject(obj, offset, expect, update);
  }
  
  public boolean weakCompareAndSet(U obj, W expect, W update)
  {
    return unsafe.compareAndSwapObject(obj, offset, expect, update);
  }
  
  public void set(U obj, W newValue)
  {
    unsafe.putObjectVolatile(obj, offset, newValue);
  }
  
  public void lazySet(U obj, W newValue)
  {
    unsafe.putOrderedObject(obj, offset, newValue);
  }
  
  public W get(U obj)
  {
    return unsafe.getObjectVolatile(obj, offset);
  }
}
