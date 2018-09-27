package org.jupiter.transport;












public abstract class JConnection
{
  private final UnresolvedAddress address;
  










  public JConnection(UnresolvedAddress address)
  {
    this.address = address;
  }
  
  public UnresolvedAddress getAddress() {
    return address;
  }
  
  public void operationComplete(OperationListener operationListener) {}
  
  public abstract void setReconnect(boolean paramBoolean);
  
  public static abstract interface OperationListener
  {
    public abstract void complete(boolean paramBoolean);
  }
}
