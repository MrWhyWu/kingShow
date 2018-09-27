package org.jupiter.rpc.load.balance;







final class WeightArray
{
  private final int[] array;
  




  private final int length;
  




  private int gcd;
  





  WeightArray(int[] array, int length)
  {
    this.array = array;
    this.length = (array != null ? array.length : length);
  }
  
  int get(int index) {
    if (index >= array.length) {
      throw new ArrayIndexOutOfBoundsException(index);
    }
    return array[index];
  }
  
  int length() {
    return length;
  }
  
  int gcd() {
    return gcd;
  }
  
  void gcd(int gcd) {
    this.gcd = gcd;
  }
  
  boolean isAllSameWeight() {
    return array == null;
  }
}
