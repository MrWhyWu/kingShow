package org.jupiter.spring.support;

import java.util.List;
import org.jupiter.common.util.Lists;
import org.jupiter.common.util.Strings;
import org.jupiter.rpc.DispatchType;
import org.jupiter.rpc.InvokeType;
import org.jupiter.rpc.JClient;
import org.jupiter.rpc.consumer.ConsumerInterceptor;
import org.jupiter.rpc.consumer.ProxyFactory;
import org.jupiter.rpc.consumer.cluster.ClusterInvoker.Strategy;
import org.jupiter.rpc.load.balance.LoadBalancerType;
import org.jupiter.rpc.model.metadata.MethodSpecialConfig;
import org.jupiter.serialization.SerializerType;
import org.jupiter.transport.JConnector.ConnectionWatcher;
import org.jupiter.transport.UnresolvedAddress;
import org.jupiter.transport.exception.ConnectFailedException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;


























public class JupiterSpringConsumerBean<T>
  implements FactoryBean<T>, InitializingBean
{
  private JupiterSpringClient client;
  private Class<T> interfaceClass;
  private String version;
  private SerializerType serializerType;
  private LoadBalancerType loadBalancerType;
  private long waitForAvailableTimeoutMillis = -1L;
  private transient T proxy;
  private InvokeType invokeType;
  private DispatchType dispatchType;
  private long timeoutMillis;
  private List<MethodSpecialConfig> methodSpecialConfigs;
  private ConsumerInterceptor[] consumerInterceptors;
  private String providerAddresses;
  private ClusterInvoker.Strategy clusterStrategy;
  private int failoverRetries;
  
  public JupiterSpringConsumerBean() {}
  
  public T getObject() throws Exception
  {
    return proxy;
  }
  
  public Class<?> getObjectType()
  {
    return interfaceClass;
  }
  
  public boolean isSingleton()
  {
    return true;
  }
  
  public void afterPropertiesSet() throws Exception
  {
    init();
  }
  
  private void init() {
    ProxyFactory<T> factory = ProxyFactory.factory(interfaceClass).version(version);
    

    if (serializerType != null) {
      factory.serializerType(serializerType);
    }
    
    if (loadBalancerType != null) {
      factory.loadBalancerType(loadBalancerType);
    }
    
    if (client.isHasRegistryServer())
    {
      JConnector.ConnectionWatcher watcher = client.getClient().watchConnections(interfaceClass, version);
      if (waitForAvailableTimeoutMillis > 0L)
      {
        if (!watcher.waitForAvailable(waitForAvailableTimeoutMillis)) {
          throw new ConnectFailedException();
        }
      }
    } else {
      if (Strings.isBlank(providerAddresses)) {
        throw new IllegalArgumentException("Provider addresses could not be empty");
      }
      
      String[] array = Strings.split(providerAddresses, ',');
      List<UnresolvedAddress> addresses = Lists.newArrayList();
      for (String s : array) {
        String[] addressStr = Strings.split(s, ':');
        String host = addressStr[0];
        int port = Integer.parseInt(addressStr[1]);
        UnresolvedAddress address = new UnresolvedAddress(host, port);
        addresses.add(address);
      }
      factory.addProviderAddress(addresses);
    }
    
    if (invokeType != null) {
      factory.invokeType(invokeType);
    }
    
    if (dispatchType != null) {
      factory.dispatchType(dispatchType);
    }
    
    if (timeoutMillis > 0L) {
      factory.timeoutMillis(timeoutMillis);
    }
    
    if (methodSpecialConfigs != null) {
      for (MethodSpecialConfig config : methodSpecialConfigs) {
        factory.addMethodSpecialConfig(new MethodSpecialConfig[] { config });
      }
    }
    
    ConsumerInterceptor[] globalConsumerInterceptors = client.getGlobalConsumerInterceptors();
    if ((globalConsumerInterceptors != null) && (globalConsumerInterceptors.length > 0)) {
      factory.addInterceptor(globalConsumerInterceptors);
    }
    
    if ((consumerInterceptors != null) && (consumerInterceptors.length > 0)) {
      factory.addInterceptor(consumerInterceptors);
    }
    
    if (clusterStrategy != null) {
      factory.clusterStrategy(clusterStrategy);
    }
    
    if (failoverRetries > 0) {
      factory.failoverRetries(failoverRetries);
    }
    
    proxy = factory.client(client.getClient()).newProxyInstance();
  }
  

  public JupiterSpringClient getClient()
  {
    return client;
  }
  
  public void setClient(JupiterSpringClient client) {
    this.client = client;
  }
  
  public Class<T> getInterfaceClass() {
    return interfaceClass;
  }
  
  public void setInterfaceClass(Class<T> interfaceClass) {
    this.interfaceClass = interfaceClass;
  }
  
  public String getVersion() {
    return version;
  }
  
  public void setVersion(String version) {
    this.version = version;
  }
  
  public SerializerType getSerializerType() {
    return serializerType;
  }
  
  public void setSerializerType(String serializerType) {
    this.serializerType = SerializerType.parse(serializerType);
  }
  
  public LoadBalancerType getLoadBalancerType() {
    return loadBalancerType;
  }
  
  public void setLoadBalancerType(String loadBalancerType) {
    this.loadBalancerType = LoadBalancerType.parse(loadBalancerType);
    if (this.loadBalancerType == null) {
      throw new IllegalArgumentException(loadBalancerType);
    }
  }
  
  public long getWaitForAvailableTimeoutMillis() {
    return waitForAvailableTimeoutMillis;
  }
  
  public void setWaitForAvailableTimeoutMillis(long waitForAvailableTimeoutMillis) {
    this.waitForAvailableTimeoutMillis = waitForAvailableTimeoutMillis;
  }
  
  public InvokeType getInvokeType() {
    return invokeType;
  }
  
  public void setInvokeType(String invokeType) {
    this.invokeType = InvokeType.parse(invokeType);
    if (this.invokeType == null) {
      throw new IllegalArgumentException(invokeType);
    }
  }
  
  public DispatchType getDispatchType() {
    return dispatchType;
  }
  
  public void setDispatchType(String dispatchType) {
    this.dispatchType = DispatchType.parse(dispatchType);
    if (this.dispatchType == null) {
      throw new IllegalArgumentException(dispatchType);
    }
  }
  
  public long getTimeoutMillis() {
    return timeoutMillis;
  }
  
  public void setTimeoutMillis(long timeoutMillis) {
    this.timeoutMillis = timeoutMillis;
  }
  
  public List<MethodSpecialConfig> getMethodSpecialConfigs() {
    return methodSpecialConfigs;
  }
  
  public void setMethodSpecialConfigs(List<MethodSpecialConfig> methodSpecialConfigs) {
    this.methodSpecialConfigs = methodSpecialConfigs;
  }
  
  public ConsumerInterceptor[] getConsumerInterceptors() {
    return consumerInterceptors;
  }
  
  public void setConsumerInterceptors(ConsumerInterceptor[] consumerInterceptors) {
    this.consumerInterceptors = consumerInterceptors;
  }
  
  public String getProviderAddresses() {
    return providerAddresses;
  }
  
  public void setProviderAddresses(String providerAddresses) {
    this.providerAddresses = providerAddresses;
  }
  
  public ClusterInvoker.Strategy getClusterStrategy() {
    return clusterStrategy;
  }
  
  public void setClusterStrategy(String clusterStrategy) {
    this.clusterStrategy = ClusterInvoker.Strategy.parse(clusterStrategy);
    if (this.clusterStrategy == null) {
      throw new IllegalArgumentException(clusterStrategy);
    }
  }
  
  public int getFailoverRetries() {
    return failoverRetries;
  }
  
  public void setFailoverRetries(int failoverRetries) {
    this.failoverRetries = failoverRetries;
  }
}
