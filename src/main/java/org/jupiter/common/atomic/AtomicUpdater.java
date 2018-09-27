package org.jupiter.common.atomic;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.jupiter.common.util.internal.UnsafeUtil;
































public final class AtomicUpdater
{
  public static <U, W> AtomicReferenceFieldUpdater<U, W> newAtomicReferenceFieldUpdater(Class<U> tClass, Class<W> vClass, String fieldName)
  {
    try
    {
      return new UnsafeAtomicReferenceFieldUpdater(UnsafeUtil.getUnsafe(), tClass, fieldName);
    } catch (Throwable t) {}
    return AtomicReferenceFieldUpdater.newUpdater(tClass, vClass, fieldName);
  }
  
  private AtomicUpdater() {}
}
