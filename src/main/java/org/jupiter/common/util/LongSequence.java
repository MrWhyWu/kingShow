package org.jupiter.common.util;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import org.jupiter.common.util.internal.InternalThreadLocal;























































public class LongSequence
  extends LongRhsPadding
{
  private static final int DEFAULT_STEP = 128;
  private static final AtomicLongFieldUpdater<LongValue> updater = AtomicLongFieldUpdater.newUpdater(LongValue.class, "value");
  
  private final InternalThreadLocal<LocalSequence> localSequence = new InternalThreadLocal()
  {
    protected LongSequence.LocalSequence initialValue() throws Exception
    {
      return new LongSequence.LocalSequence(LongSequence.this, null);
    }
  };
  private final int step;
  
  public LongSequence()
  {
    this(128);
  }
  
  public LongSequence(int step) {
    this.step = step;
  }
  
  public LongSequence(long initialValue, int step) {
    updater.set(this, initialValue);
    this.step = step;
  }
  
  public long next() {
    return ((LocalSequence)localSequence.get()).next();
  }
  

  private long getNextBaseValue() { return updater.getAndAdd(this, step); }
  
  private final class LocalSequence {
    private LocalSequence() {}
    
    private long localBase = LongSequence.this.getNextBaseValue();
    private long localValue = 0L;
    
    public long next() {
      long realVal = ++localValue + localBase;
      
      if (localValue == step) {
        localBase = LongSequence.this.getNextBaseValue();
        localValue = 0L;
      }
      
      return realVal;
    }
  }
}
