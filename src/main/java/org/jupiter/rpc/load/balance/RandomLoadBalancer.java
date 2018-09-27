package org.jupiter.rpc.load.balance;

import java.util.concurrent.ThreadLocalRandom;
import org.jupiter.transport.Directory;
import org.jupiter.transport.channel.CopyOnWriteGroupList;
import org.jupiter.transport.channel.JChannelGroup;





































public class RandomLoadBalancer
  implements LoadBalancer
{
  public RandomLoadBalancer() {}
  
  private static final RandomLoadBalancer instance = new RandomLoadBalancer();
  
  public static RandomLoadBalancer instance() {
    return instance;
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
    
    ThreadLocalRandom random = ThreadLocalRandom.current();
    
    if (weightArray.isAllSameWeight()) {
      return elements[random.nextInt(length)];
    }
    
    int nextIndex = getNextServerIndex(weightArray, length, random);
    
    return elements[nextIndex];
  }
  
  private static int getNextServerIndex(WeightArray weightArray, int length, ThreadLocalRandom random) {
    int sumWeight = weightArray.get(length - 1);
    int val = random.nextInt(sumWeight + 1);
    return WeightSupport.binarySearchIndex(weightArray, length, val);
  }
}
