package org.jupiter.transport.payload;

import org.jupiter.common.util.LongSequence;































public class JRequestPayload
  extends PayloadHolder
{
  private static final LongSequence sequence = new LongSequence();
  
  private final long invokeId;
  
  private transient long timestamp;
  
  public JRequestPayload()
  {
    this(sequence.next());
  }
  
  public JRequestPayload(long invokeId) {
    this.invokeId = invokeId;
  }
  
  public long invokeId() {
    return invokeId;
  }
  
  public long timestamp() {
    return timestamp;
  }
  
  public void timestamp(long timestamp) {
    this.timestamp = timestamp;
  }
}
