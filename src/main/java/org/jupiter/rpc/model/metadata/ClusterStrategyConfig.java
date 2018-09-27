package org.jupiter.rpc.model.metadata;

import java.io.Serializable;
import org.jupiter.rpc.consumer.cluster.ClusterInvoker.Strategy;





















public class ClusterStrategyConfig
  implements Serializable
{
  private static final long serialVersionUID = 8192956131353063709L;
  private ClusterInvoker.Strategy strategy;
  private int failoverRetries;
  
  public ClusterStrategyConfig() {}
  
  public static ClusterStrategyConfig of(String strategy, String failoverRetries)
  {
    int retries = 0;
    try {
      retries = Integer.parseInt(failoverRetries);
    }
    catch (Exception localException) {}
    return of(ClusterInvoker.Strategy.parse(strategy), retries);
  }
  
  public static ClusterStrategyConfig of(ClusterInvoker.Strategy strategy, int failoverRetries) {
    ClusterStrategyConfig s = new ClusterStrategyConfig();
    s.setStrategy(strategy);
    s.setFailoverRetries(failoverRetries);
    return s;
  }
  
  public ClusterInvoker.Strategy getStrategy() {
    return strategy;
  }
  
  public void setStrategy(ClusterInvoker.Strategy strategy) {
    this.strategy = strategy;
  }
  
  public int getFailoverRetries() {
    return failoverRetries;
  }
  
  public void setFailoverRetries(int failoverRetries) {
    this.failoverRetries = failoverRetries;
  }
}
