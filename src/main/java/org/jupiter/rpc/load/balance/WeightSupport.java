package org.jupiter.rpc.load.balance;

import org.jupiter.common.util.SystemClock;
import org.jupiter.transport.Directory;
import org.jupiter.transport.channel.CopyOnWriteGroupList;
import org.jupiter.transport.channel.JChannelGroup;
























final class WeightSupport
{
  static int binarySearchIndex(WeightArray weightArray, int length, int value)
  {
    int low = 0;
    int high = length - 1;
    
    while (low <= high) {
      int mid = low + high >>> 1;
      long midVal = weightArray.get(mid);
      
      if (midVal < value) {
        low = mid + 1;
      } else if (midVal > value) {
        high = mid - 1;
      } else {
        return mid;
      }
    }
    
    return low;
  }
  
  static WeightArray computeWeights(CopyOnWriteGroupList groups, JChannelGroup[] elements, Directory directory) {
    int length = elements.length;
    int[] weights = new int[length];
    
    boolean allWarmUpComplete = elements[0].isWarmUpComplete();
    boolean allSameWeight = true;
    weights[0] = getWeight(elements[0], directory);
    for (int i = 1; i < length; i++) {
      allWarmUpComplete &= elements[i].isWarmUpComplete();
      weights[i] = getWeight(elements[i], directory);
      allSameWeight &= weights[(i - 1)] == weights[i];
    }
    
    if ((allWarmUpComplete) && (allSameWeight)) {
      weights = null;
    }
    
    if (weights != null) {
      for (int i = 1; i < length; i++)
      {
        weights[i] += weights[(i - 1)];
      }
    }
    
    WeightArray weightArray = new WeightArray(weights, length);
    
    if (allWarmUpComplete) {
      groups.setWeightArray(elements, directory.directoryString(), weightArray);
    }
    
    return weightArray;
  }
  
  static int getWeight(JChannelGroup group, Directory directory)
  {
    int weight = group.getWeight(directory);
    int warmUp = group.getWarmUp();
    int upTime = (int)(SystemClock.millisClock().now() - group.timestamp());
    
    if ((upTime > 0) && (upTime < warmUp))
    {
      weight = (int)(upTime / warmUp * weight);
    }
    
    return weight > 0 ? weight : 0;
  }
  
  static int n_gcd(int[] array, int n) {
    if (n == 1) {
      return array[0];
    }
    return gcd(array[(n - 1)], n_gcd(array, n - 1));
  }
  



  static int gcd(int a, int b)
  {
    if (a == b)
      return a;
    if (a == 0)
      return b;
    if (b == 0) {
      return a;
    }
    




    int aTwos = Integer.numberOfTrailingZeros(a);
    a >>= aTwos;
    int bTwos = Integer.numberOfTrailingZeros(b);
    b >>= bTwos;
    while (a != b)
    {






      int delta = a - b;
      
      int minDeltaOrZero = delta & delta >> 31;
      

      a = delta - minDeltaOrZero - minDeltaOrZero;
      

      b += minDeltaOrZero;
      a >>= Integer.numberOfTrailingZeros(a);
    }
    return a << Math.min(aTwos, bTwos);
  }
  
  private WeightSupport() {}
}
