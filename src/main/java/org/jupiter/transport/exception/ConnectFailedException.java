package org.jupiter.transport.exception;









public class ConnectFailedException
  extends RuntimeException
{
  private static final long serialVersionUID = -2890742743547564900L;
  







  public ConnectFailedException() {}
  







  public ConnectFailedException(String message)
  {
    super(message);
  }
  
  public ConnectFailedException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public ConnectFailedException(Throwable cause) {
    super(cause);
  }
}
