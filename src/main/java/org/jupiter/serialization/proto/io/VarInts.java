package org.jupiter.serialization.proto.io;



























public final class VarInts
{
  public static int computeRawVarInt32Size(int value)
  {
    if ((value & 0xFFFFFF80) == 0) {
      return 1;
    }
    if ((value & 0xC000) == 0) {
      return 2;
    }
    if ((value & 0xFFE00000) == 0) {
      return 3;
    }
    if ((value & 0xF0000000) == 0) {
      return 4;
    }
    return 5;
  }
  



  public static int computeRawVarInt64Size(long value)
  {
    if ((value & 0xFFFFFFFFFFFFFF80) == 0L) {
      return 1;
    }
    if (value < 0L) {
      return 10;
    }
    
    int n = 2;
    if ((value & 0xFFFFFFF800000000) != 0L) {
      n += 4;
      value >>>= 28;
    }
    if ((value & 0xFFFFFFFFFFE00000) != 0L) {
      n += 2;
      value >>>= 14;
    }
    if ((value & 0xFFFFFFFFFFFFC000) != 0L) {
      n++;
    }
    return n;
  }
  
  private VarInts() {}
}
