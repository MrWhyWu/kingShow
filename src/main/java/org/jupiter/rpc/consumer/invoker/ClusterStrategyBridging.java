package org.jupiter.rpc.consumer.invoker;

import java.util.List;
import java.util.Map;
import org.jupiter.common.util.Maps;
import org.jupiter.rpc.consumer.cluster.ClusterInvoker;
import org.jupiter.rpc.consumer.cluster.ClusterInvoker.Strategy;
import org.jupiter.rpc.consumer.cluster.FailFastClusterInvoker;
import org.jupiter.rpc.consumer.cluster.FailOverClusterInvoker;
import org.jupiter.rpc.consumer.cluster.FailSafeClusterInvoker;
import org.jupiter.rpc.consumer.dispatcher.Dispatcher;
import org.jupiter.rpc.model.metadata.ClusterStrategyConfig;
import org.jupiter.rpc.model.metadata.MethodSpecialConfig;
























public class ClusterStrategyBridging
{
  private final ClusterInvoker defaultClusterInvoker;
  private final Map<String, ClusterInvoker> methodSpecialClusterInvokerMapping;
  
  public ClusterStrategyBridging(Dispatcher dispatcher, ClusterStrategyConfig defaultStrategy, List<MethodSpecialConfig> methodSpecialConfigs)
  {
    defaultClusterInvoker = createClusterInvoker(dispatcher, defaultStrategy);
    methodSpecialClusterInvokerMapping = Maps.newHashMap();
    
    for (MethodSpecialConfig config : methodSpecialConfigs) {
      ClusterStrategyConfig strategy = config.getStrategy();
      if (strategy != null) {
        methodSpecialClusterInvokerMapping.put(config.getMethodName(), createClusterInvoker(dispatcher, strategy));
      }
    }
  }
  


  public ClusterInvoker findClusterInvoker(String methodName)
  {
    ClusterInvoker invoker = (ClusterInvoker)methodSpecialClusterInvokerMapping.get(methodName);
    return invoker != null ? invoker : defaultClusterInvoker;
  }
  
  private ClusterInvoker createClusterInvoker(Dispatcher dispatcher, ClusterStrategyConfig strategy) {
    ClusterInvoker.Strategy s = strategy.getStrategy();
    switch (1.$SwitchMap$org$jupiter$rpc$consumer$cluster$ClusterInvoker$Strategy[s.ordinal()]) {
    case 1: 
      return new FailFastClusterInvoker(dispatcher);
    case 2: 
      return new FailOverClusterInvoker(dispatcher, strategy.getFailoverRetries());
    case 3: 
      return new FailSafeClusterInvoker(dispatcher);
    }
    throw new UnsupportedOperationException("Unsupported strategy: " + strategy);
  }
}
