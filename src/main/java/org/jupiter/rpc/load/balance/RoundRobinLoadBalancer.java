package org.jupiter.rpc.load.balance;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import org.jupiter.transport.Directory;
import org.jupiter.transport.channel.CopyOnWriteGroupList;
import org.jupiter.transport.channel.JChannelGroup;






















































public class RoundRobinLoadBalancer
  implements LoadBalancer
{
  private static final AtomicIntegerFieldUpdater<RoundRobinLoadBalancer> indexUpdater = AtomicIntegerFieldUpdater.newUpdater(RoundRobinLoadBalancer.class, "index");
  

  private volatile int index = 0;
  
  public RoundRobinLoadBalancer() {}
  
  public static RoundRobinLoadBalancer instance() {
    return new RoundRobinLoadBalancer();
  }
  
  public JChannelGroup select(CopyOnWriteGroupList groups, Directory directory)
  {
    JChannelGroup[] elements = groups.getSnapshot();
    int length = elements.length;
    
    if (length == 0) {
      return null;
    }
    
    if (length == 1) {
      return elements[0];
    }
    
    WeightArray weightArray = (WeightArray)groups.getWeightArray(elements, directory.directoryString());
    if ((weightArray == null) || (weightArray.length() != length)) {
      weightArray = WeightSupport.computeWeights(groups, elements, directory);
    }
    
    int rrIndex = indexUpdater.getAndIncrement(this) & 0x7FFFFFFF;
    
    if (weightArray.isAllSameWeight()) {
      return elements[(rrIndex % length)];
    }
    
    int nextIndex = getNextServerIndex(weightArray, length, rrIndex);
    
    return elements[nextIndex];
  }
  
  private static int getNextServerIndex(WeightArray weightArray, int length, int rrIndex) {
    int[] weights = new int[length];
    int maxWeight = weights[0] = weightArray.get(0);
    for (int i = 1; i < length; i++) {
      weights[i] = (weightArray.get(i) - weightArray.get(i - 1));
      if (weights[i] > maxWeight) {
        maxWeight = weights[i];
      }
    }
    

    int gcd = weightArray.gcd();
    if (gcd < 1) {
      gcd = WeightSupport.n_gcd(weights, length);
      weightArray.gcd(gcd);
    }
    

    int sumWeight = weightArray.get(length - 1);
    int val = rrIndex % (sumWeight / gcd);
    for (int i = 0; i < maxWeight; i++) {
      for (int j = 0; j < length; j++) {
        if ((val == 0) && (weights[j] > 0)) {
          return j;
        }
        if (weights[j] > 0) {
          weights[j] -= gcd;
          val--;
        }
      }
    }
    
    return rrIndex % length;
  }
}
