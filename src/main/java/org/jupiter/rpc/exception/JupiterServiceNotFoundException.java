package org.jupiter.rpc.exception;









public class JupiterServiceNotFoundException
  extends JupiterRemoteException
{
  private static final long serialVersionUID = -2277731243490443074L;
  








  public JupiterServiceNotFoundException() {}
  








  public JupiterServiceNotFoundException(String message)
  {
    super(message);
  }
  
  public JupiterServiceNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public JupiterServiceNotFoundException(Throwable cause) {
    super(cause);
  }
  
  public synchronized Throwable fillInStackTrace()
  {
    return this;
  }
}
