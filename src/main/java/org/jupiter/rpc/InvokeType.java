package org.jupiter.rpc;
























public enum InvokeType
{
  SYNC, 
  ASYNC;
  
  private InvokeType() {}
  public static InvokeType parse(String name) { for (InvokeType s : ) {
      if (s.name().equalsIgnoreCase(name)) {
        return s;
      }
    }
    return null;
  }
  
  public static InvokeType getDefault() {
    return SYNC;
  }
}
