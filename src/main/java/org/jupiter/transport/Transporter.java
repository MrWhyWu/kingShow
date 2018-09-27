package org.jupiter.transport;















public abstract interface Transporter
{
  public abstract Protocol protocol();
  














  public static enum Protocol
  {
    TCP;
    
    private Protocol() {}
  }
}
