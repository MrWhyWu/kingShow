package org.jupiter.rpc;
























public enum DispatchType
{
  ROUND, 
  BROADCAST;
  
  private DispatchType() {}
  public static DispatchType parse(String name) { for (DispatchType s : ) {
      if (s.name().equalsIgnoreCase(name)) {
        return s;
      }
    }
    return null;
  }
  
  public static DispatchType getDefault() {
    return ROUND;
  }
}
