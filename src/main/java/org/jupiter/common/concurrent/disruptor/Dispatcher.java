package org.jupiter.common.concurrent.disruptor;

import org.jupiter.common.util.JConstants;

























public abstract interface Dispatcher<T>
{
  public static final int BUFFER_SIZE = 32768;
  public static final int MAX_NUM_WORKERS = JConstants.AVAILABLE_PROCESSORS << 3;
  
  public abstract boolean dispatch(T paramT);
  
  public abstract void shutdown();
}
