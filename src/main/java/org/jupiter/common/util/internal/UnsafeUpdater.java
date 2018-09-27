package org.jupiter.common.util.internal;

import org.jupiter.common.util.ThrowUtil;

























public class UnsafeUpdater
{
  public UnsafeUpdater() {}
  
  public static <U> UnsafeIntegerFieldUpdater<U> newIntegerFieldUpdater(Class<? super U> tClass, String fieldName)
  {
    try
    {
      return new UnsafeIntegerFieldUpdater(UnsafeUtil.getUnsafe(), tClass, fieldName);
    } catch (Throwable t) {
      ThrowUtil.throwException(t);
    }
    return null;
  }
  




  public static <U> UnsafeLongFieldUpdater<U> newLongFieldUpdater(Class<? super U> tClass, String fieldName)
  {
    try
    {
      return new UnsafeLongFieldUpdater(UnsafeUtil.getUnsafe(), tClass, fieldName);
    } catch (Throwable t) {
      ThrowUtil.throwException(t);
    }
    return null;
  }
  




  public static <U, W> UnsafeReferenceFieldUpdater<U, W> newReferenceFieldUpdater(Class<? super U> tClass, String fieldName)
  {
    try
    {
      return new UnsafeReferenceFieldUpdater(UnsafeUtil.getUnsafe(), tClass, fieldName);
    } catch (Throwable t) {
      ThrowUtil.throwException(t);
    }
    return null;
  }
}
