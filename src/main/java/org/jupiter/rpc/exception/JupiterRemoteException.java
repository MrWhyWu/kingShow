package org.jupiter.rpc.exception;

import java.net.SocketAddress;
























public class JupiterRemoteException
  extends RuntimeException
{
  private static final long serialVersionUID = -6516335527982400712L;
  private final SocketAddress remoteAddress;
  
  public JupiterRemoteException()
  {
    remoteAddress = null;
  }
  
  public JupiterRemoteException(SocketAddress remoteAddress) {
    this.remoteAddress = remoteAddress;
  }
  
  public JupiterRemoteException(Throwable cause) {
    super(cause);
    remoteAddress = null;
  }
  
  public JupiterRemoteException(Throwable cause, SocketAddress remoteAddress) {
    super(cause);
    this.remoteAddress = remoteAddress;
  }
  
  public JupiterRemoteException(String message) {
    super(message);
    remoteAddress = null;
  }
  
  public JupiterRemoteException(String message, SocketAddress remoteAddress) {
    super(message);
    this.remoteAddress = remoteAddress;
  }
  
  public JupiterRemoteException(String message, Throwable cause) {
    super(message, cause);
    remoteAddress = null;
  }
  
  public JupiterRemoteException(String message, Throwable cause, SocketAddress remoteAddress) {
    super(message, cause);
    this.remoteAddress = remoteAddress;
  }
  
  public SocketAddress getRemoteAddress() {
    return remoteAddress;
  }
}
