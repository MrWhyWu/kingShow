package org.jupiter.rpc.exception;

import java.net.SocketAddress;























public class JupiterSerializationException
  extends JupiterRemoteException
{
  private static final long serialVersionUID = -5079093080483380586L;
  
  public JupiterSerializationException() {}
  
  public JupiterSerializationException(SocketAddress remoteAddress)
  {
    super(remoteAddress);
  }
  
  public JupiterSerializationException(String message) {
    super(message);
  }
  
  public JupiterSerializationException(String message, SocketAddress remoteAddress) {
    super(message, remoteAddress);
  }
  
  public JupiterSerializationException(Throwable cause) {
    super(cause);
  }
  
  public JupiterSerializationException(Throwable cause, SocketAddress remoteAddress) {
    super(cause, remoteAddress);
  }
}
