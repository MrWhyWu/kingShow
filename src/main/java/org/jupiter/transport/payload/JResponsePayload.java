package org.jupiter.transport.payload;









public class JResponsePayload
  extends PayloadHolder
{
  private final long id;
  







  private byte status;
  







  public JResponsePayload(long id)
  {
    this.id = id;
  }
  
  public long id() {
    return id;
  }
  
  public byte status() {
    return status;
  }
  
  public void status(byte status) {
    this.status = status;
  }
}
