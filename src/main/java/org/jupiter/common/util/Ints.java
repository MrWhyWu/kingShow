package org.jupiter.common.util;

















public final class Ints
{
  public static final int MAX_POWER_OF_TWO = 1073741824;
  
















  public static int checkedCast(long value)
  {
    int result = (int)value;
    Preconditions.checkArgument(result == value, "out of range: " + value);
    return result;
  }
  


  public static int saturatedCast(long value)
  {
    return value < -2147483648L ? Integer.MIN_VALUE : value > 2147483647L ? Integer.MAX_VALUE : (int)value;
  }
  








  public static int findNextPositivePowerOfTwo(int value)
  {
    return value >= 1073741824 ? 1073741824 : value <= 0 ? 1 : 1 << 32 - Integer.numberOfLeadingZeros(value - 1);
  }
  
  private Ints() {}
}
