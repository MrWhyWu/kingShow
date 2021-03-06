package org.jupiter.common.util;

































public final class Preconditions
{
  public static <T> T checkNotNull(T reference)
  {
    if (reference == null) {
      throw new NullPointerException();
    }
    return reference;
  }
  









  public static <T> T checkNotNull(T reference, Object errorMessage)
  {
    if (reference == null) {
      throw new NullPointerException(String.valueOf(errorMessage));
    }
    return reference;
  }
  






  public static void checkArgument(boolean expression)
  {
    if (!expression) {
      throw new IllegalArgumentException();
    }
  }
  








  public static void checkArgument(boolean expression, Object errorMessage)
  {
    if (!expression) {
      throw new IllegalArgumentException(String.valueOf(errorMessage));
    }
  }
  
  private Preconditions() {}
}
