package org.jupiter.common.util;

import org.jupiter.common.util.internal.UnsafeReferenceFieldUpdater;
import org.jupiter.common.util.internal.UnsafeUpdater;
import org.jupiter.common.util.internal.UnsafeUtil;
import sun.misc.Unsafe;























public final class ThrowUtil
{
  private static final UnsafeReferenceFieldUpdater<Throwable, Throwable> causeUpdater = UnsafeUpdater.newReferenceFieldUpdater(Throwable.class, "cause");
  



  public static void throwException(Throwable t)
  {
    Unsafe unsafe = UnsafeUtil.getUnsafe();
    if (unsafe != null) {
      unsafe.throwException(t);
    } else {
      throwException0(t);
    }
  }
  













  private static <E extends Throwable> void throwException0(Throwable t)
    throws Throwable
  {
    throw t;
  }
  
  public static <T extends Throwable> T cutCause(T cause) {
    Throwable rootCause = cause;
    while (rootCause.getCause() != null) {
      rootCause = rootCause.getCause();
    }
    
    if (rootCause != cause) {
      cause.setStackTrace(rootCause.getStackTrace());
      assert (causeUpdater != null);
      causeUpdater.set(cause, cause);
    }
    return cause;
  }
  
  private ThrowUtil() {}
}
