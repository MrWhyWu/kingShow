package org.jupiter.rpc.exception;









public class JupiterBadRequestException
  extends JupiterRemoteException
{
  private static final long serialVersionUID = -6603241073638657127L;
  








  public JupiterBadRequestException() {}
  








  public JupiterBadRequestException(String message)
  {
    super(message);
  }
  
  public JupiterBadRequestException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public JupiterBadRequestException(Throwable cause) {
    super(cause);
  }
  
  public synchronized Throwable fillInStackTrace()
  {
    return this;
  }
}
