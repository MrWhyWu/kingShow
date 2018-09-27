package org.jupiter.common.util;































public final class Pow2
{
  public static int roundToPowerOfTwo(int value)
  {
    return 1 << 32 - Integer.numberOfLeadingZeros(value - 1);
  }
  





  public static boolean isPowerOfTwo(int value)
  {
    return (value & value - 1) == 0;
  }
  
  private Pow2() {}
}
