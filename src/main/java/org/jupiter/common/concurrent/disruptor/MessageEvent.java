package org.jupiter.common.concurrent.disruptor;








public class MessageEvent<T>
{
  private T message;
  






  public MessageEvent() {}
  






  public T getMessage()
  {
    return message;
  }
  
  public void setMessage(T message) {
    this.message = message;
  }
}
