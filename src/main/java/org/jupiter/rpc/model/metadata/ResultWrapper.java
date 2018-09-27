package org.jupiter.rpc.model.metadata;

import java.io.Serializable;
























public class ResultWrapper
  implements Serializable
{
  private static final long serialVersionUID = -1126932930252953428L;
  private Object result;
  
  public ResultWrapper() {}
  
  public Object getResult()
  {
    return result;
  }
  
  public void setResult(Object result) {
    this.result = result;
  }
  
  public void setError(Throwable cause) {
    result = cause;
  }
  
  public String toString()
  {
    return "ResultWrapper{result=" + result + '}';
  }
}
