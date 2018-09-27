package org.jupiter.rpc.exception;

import java.net.SocketAddress;
import org.jupiter.transport.Status;

























public class JupiterTimeoutException
  extends JupiterRemoteException
{
  private static final long serialVersionUID = 8768621104391094458L;
  private final Status status;
  
  public JupiterTimeoutException(SocketAddress remoteAddress, Status status)
  {
    super(remoteAddress);
    this.status = status;
  }
  
  public JupiterTimeoutException(Throwable cause, SocketAddress remoteAddress, Status status) {
    super(cause, remoteAddress);
    this.status = status;
  }
  
  public JupiterTimeoutException(String message, SocketAddress remoteAddress, Status status) {
    super(message, remoteAddress);
    this.status = status;
  }
  
  public JupiterTimeoutException(String message, Throwable cause, SocketAddress remoteAddress, Status status) {
    super(message, cause, remoteAddress);
    this.status = status;
  }
  
  public Status status() {
    return status;
  }
  
  public String toString()
  {
    return "TimeoutException{status=" + status + '}';
  }
}
