package org.jupiter.rpc.load.balance;






















public final class LoadBalancerFactory
{
  public static LoadBalancer loadBalancer(LoadBalancerType type)
  {
    if (type == LoadBalancerType.RANDOM) {
      return RandomLoadBalancer.instance();
    }
    
    if (type == LoadBalancerType.ROUND_ROBIN) {
      return RoundRobinLoadBalancer.instance();
    }
    

    return RandomLoadBalancer.instance();
  }
  
  private LoadBalancerFactory() {}
}
