package org.jupiter.rpc.load.balance;






















public enum LoadBalancerType
{
  ROUND_ROBIN, 
  RANDOM;
  
  private LoadBalancerType() {}
  public static LoadBalancerType parse(String name) { for (LoadBalancerType s : ) {
      if (s.name().equalsIgnoreCase(name)) {
        return s;
      }
    }
    return null;
  }
  
  public static LoadBalancerType getDefault() {
    return RANDOM;
  }
}
