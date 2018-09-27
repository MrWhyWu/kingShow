package org.jupiter.rpc.exception;









public class JupiterFlowControlException
  extends JupiterRemoteException
{
  private static final long serialVersionUID = 3478741195763320940L;
  








  public JupiterFlowControlException() {}
  








  public JupiterFlowControlException(String message)
  {
    super(message);
  }
  
  public JupiterFlowControlException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public JupiterFlowControlException(Throwable cause) {
    super(cause);
  }
  
  public synchronized Throwable fillInStackTrace()
  {
    return this;
  }
}
