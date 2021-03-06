package org.jupiter.common.util;





























public final class Bytes
{
  public static int bytes2Int(byte[] bytes, int start, int length)
  {
    Preconditions.checkArgument((length > 0) && (length <= 4), "invalid length: " + length);
    int sum = 0;
    int end = start + length;
    for (int i = start; i < end; i++) {
      int n = bytes[i] & 0xFF;
      n <<= --length * 8;
      sum |= n;
    }
    return sum;
  }
  



  public static byte[] int2Bytes(int value, int length)
  {
    Preconditions.checkArgument((length > 0) && (length <= 4), "invalid length: " + length);
    byte[] bytes = new byte[length];
    for (int i = 0; i < length; i++) {
      bytes[(length - i - 1)] = ((byte)(value >> 8 * i & 0xFF));
    }
    return bytes;
  }
  



  public static String bytes2String(byte[] bytes, int start, int length)
  {
    return new String(bytes, start, length);
  }
  



  public static byte[] string2Bytes(String str)
  {
    return str.getBytes();
  }
  


  public static byte[] replace(byte[] originalBytes, int offset, int length, byte[] replaceBytes)
  {
    byte[] newBytes = new byte[originalBytes.length + (replaceBytes.length - length)];
    
    System.arraycopy(originalBytes, 0, newBytes, 0, offset);
    System.arraycopy(replaceBytes, 0, newBytes, offset, replaceBytes.length);
    System.arraycopy(originalBytes, offset + length, newBytes, offset + replaceBytes.length, originalBytes.length - offset - length);
    
    return newBytes;
  }
  



  public static boolean contains(byte[] array, byte target)
  {
    for (byte value : array) {
      if (value == target) {
        return true;
      }
    }
    return false;
  }
  
  private Bytes() {}
}
