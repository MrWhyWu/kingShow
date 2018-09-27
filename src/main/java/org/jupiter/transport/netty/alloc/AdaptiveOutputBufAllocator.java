package org.jupiter.transport.netty.alloc;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import java.util.List;
import org.jupiter.common.util.Lists;

























public class AdaptiveOutputBufAllocator
{
  private static final int DEFAULT_MINIMUM = 64;
  private static final int DEFAULT_INITIAL = 512;
  private static final int DEFAULT_MAXIMUM = 524288;
  private static final int INDEX_INCREMENT = 4;
  private static final int INDEX_DECREMENT = 1;
  private static final int[] SIZE_TABLE;
  
  static
  {
    List<Integer> sizeTable = Lists.newArrayList();
    for (int i = 16; i < 512; i += 16) {
      sizeTable.add(Integer.valueOf(i));
    }
    
    for (int i = 512; i > 0; i <<= 1) {
      sizeTable.add(Integer.valueOf(i));
    }
    
    SIZE_TABLE = new int[sizeTable.size()];
    for (int i = 0; i < SIZE_TABLE.length; i++) {
      SIZE_TABLE[i] = ((Integer)sizeTable.get(i)).intValue();
    }
  }
  
  public static final AdaptiveOutputBufAllocator DEFAULT = new AdaptiveOutputBufAllocator();
  private final int minIndex;
  
  private static int getSizeTableIndex(int size) { int low = 0;int high = SIZE_TABLE.length - 1;
    for (;;) { if (high < low) {
        return low;
      }
      if (high == low) {
        return high;
      }
      
      int mid = low + high >>> 1;
      int a = SIZE_TABLE[mid];
      int b = SIZE_TABLE[(mid + 1)];
      if (size > b) {
        low = mid + 1;
      } else if (size < a) {
        high = mid - 1;
      } else { if (size == a) {
          return mid;
        }
        return mid + 1;
      }
    }
  }
  



  private final int maxIndex;
  


  private final int initial;
  


  private static final class HandleImpl
    implements AdaptiveOutputBufAllocator.Handle
  {
    private final int minIndex;
    

    private final int maxIndex;
    

    private int index;
    

    private volatile int nextAllocateBufSize;
    

    private boolean decreaseNow;
    


    HandleImpl(int minIndex, int maxIndex, int initial)
    {
      this.minIndex = minIndex;
      this.maxIndex = maxIndex;
      
      index = AdaptiveOutputBufAllocator.getSizeTableIndex(initial);
      nextAllocateBufSize = AdaptiveOutputBufAllocator.SIZE_TABLE[index];
    }
    
    public ByteBuf allocate(ByteBufAllocator alloc)
    {
      return alloc.buffer(guess());
    }
    
    public int guess()
    {
      return nextAllocateBufSize;
    }
    
    public void record(int actualWroteBytes)
    {
      if (actualWroteBytes <= AdaptiveOutputBufAllocator.SIZE_TABLE[Math.max(0, index - 1 - 1)]) {
        if (decreaseNow) {
          index = Math.max(index - 1, minIndex);
          nextAllocateBufSize = AdaptiveOutputBufAllocator.SIZE_TABLE[index];
          decreaseNow = false;
        } else {
          decreaseNow = true;
        }
      } else if (actualWroteBytes >= nextAllocateBufSize) {
        index = Math.min(index + 4, maxIndex);
        nextAllocateBufSize = AdaptiveOutputBufAllocator.SIZE_TABLE[index];
        decreaseNow = false;
      }
    }
  }
  








  private AdaptiveOutputBufAllocator()
  {
    this(64, 512, 524288);
  }
  






  public AdaptiveOutputBufAllocator(int minimum, int initial, int maximum)
  {
    if (minimum <= 0) {
      throw new IllegalArgumentException("minimum: " + minimum);
    }
    if (initial < minimum) {
      throw new IllegalArgumentException("initial: " + initial);
    }
    if (maximum < initial) {
      throw new IllegalArgumentException("maximum: " + maximum);
    }
    
    int minIndex = getSizeTableIndex(minimum);
    if (SIZE_TABLE[minIndex] < minimum) {
      this.minIndex = (minIndex + 1);
    } else {
      this.minIndex = minIndex;
    }
    
    int maxIndex = getSizeTableIndex(maximum);
    if (SIZE_TABLE[maxIndex] > maximum) {
      this.maxIndex = (maxIndex - 1);
    } else {
      this.maxIndex = maxIndex;
    }
    
    this.initial = initial;
  }
  
  public Handle newHandle() {
    return new HandleImpl(minIndex, maxIndex, initial);
  }
  
  public static abstract interface Handle
  {
    public abstract ByteBuf allocate(ByteBufAllocator paramByteBufAllocator);
    
    public abstract int guess();
    
    public abstract void record(int paramInt);
  }
}
