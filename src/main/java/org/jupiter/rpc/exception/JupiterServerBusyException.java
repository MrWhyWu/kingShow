package org.jupiter.rpc.exception;









public class JupiterServerBusyException
  extends JupiterRemoteException
{
  private static final long serialVersionUID = 4812626729436624336L;
  








  public JupiterServerBusyException() {}
  








  public JupiterServerBusyException(String message)
  {
    super(message);
  }
  
  public JupiterServerBusyException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public JupiterServerBusyException(Throwable cause) {
    super(cause);
  }
  
  public synchronized Throwable fillInStackTrace()
  {
    return this;
  }
}
