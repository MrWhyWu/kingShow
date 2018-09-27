package org.jupiter.flight.exec;






public class ExecResult
{
  private String debugInfo;
  




  private Object value;
  





  public ExecResult() {}
  




  public String getDebugInfo()
  {
    return debugInfo;
  }
  
  public void setDebugInfo(String debugInfo) {
    this.debugInfo = debugInfo;
  }
  
  public Object getValue() {
    return value;
  }
  
  public void setValue(Object value) {
    this.value = value;
  }
  
  public String toString()
  {
    return "ExecResult{debugInfo='" + debugInfo + '\'' + ", value=" + value + '}';
  }
}
