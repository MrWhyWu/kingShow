package org.jupiter.common.util;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import org.jupiter.common.util.internal.InternalThreadLocal;























































public class IntSequence
  extends IntRhsPadding
{
  private static final int DEFAULT_STEP = 64;
  private static final AtomicIntegerFieldUpdater<IntValue> updater = AtomicIntegerFieldUpdater.newUpdater(IntValue.class, "value");
  
  private final InternalThreadLocal<LocalSequence> localSequence = new InternalThreadLocal()
  {
    protected IntSequence.LocalSequence initialValue() throws Exception
    {
      return new IntSequence.LocalSequence(IntSequence.this, null);
    }
  };
  private final int step;
  
  public IntSequence()
  {
    this(64);
  }
  
  public IntSequence(int step) {
    this.step = step;
  }
  
  public IntSequence(int initialValue, int step) {
    updater.set(this, initialValue);
    this.step = step;
  }
  
  public int next() {
    return ((LocalSequence)localSequence.get()).next();
  }
  

  private int getNextBaseValue() { return updater.getAndAdd(this, step); }
  
  private final class LocalSequence {
    private LocalSequence() {}
    
    private int localBase = IntSequence.this.getNextBaseValue();
    private int localValue = 0;
    
    public int next() {
      int realVal = ++localValue + localBase;
      
      if (localValue == step) {
        localBase = IntSequence.this.getNextBaseValue();
        localValue = 0;
      }
      
      return realVal;
    }
  }
}
