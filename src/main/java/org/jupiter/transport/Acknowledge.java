package org.jupiter.transport;













public class Acknowledge
{
  private final long sequence;
  











  public Acknowledge(long sequence)
  {
    this.sequence = sequence;
  }
  
  public long sequence() {
    return sequence;
  }
}
