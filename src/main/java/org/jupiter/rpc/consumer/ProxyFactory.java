package org.jupiter.rpc.consumer;

import java.util.Collections;
import java.util.List;
import org.jupiter.common.util.Lists;
import org.jupiter.common.util.Preconditions;
import org.jupiter.common.util.Proxies;
import org.jupiter.common.util.Strings;
import org.jupiter.rpc.DispatchType;
import org.jupiter.rpc.InvokeType;
import org.jupiter.rpc.JClient;
import org.jupiter.rpc.ServiceProvider;
import org.jupiter.rpc.consumer.cluster.ClusterInvoker.Strategy;
import org.jupiter.rpc.consumer.dispatcher.DefaultBroadcastDispatcher;
import org.jupiter.rpc.consumer.dispatcher.DefaultRoundDispatcher;
import org.jupiter.rpc.consumer.dispatcher.Dispatcher;
import org.jupiter.rpc.consumer.invoker.AsyncInvoker;
import org.jupiter.rpc.consumer.invoker.SyncInvoker;
import org.jupiter.rpc.load.balance.LoadBalancerFactory;
import org.jupiter.rpc.load.balance.LoadBalancerType;
import org.jupiter.rpc.model.metadata.ClusterStrategyConfig;
import org.jupiter.rpc.model.metadata.MethodSpecialConfig;
import org.jupiter.rpc.model.metadata.ServiceMetadata;
import org.jupiter.serialization.SerializerType;
import org.jupiter.transport.Directory;
import org.jupiter.transport.JConnection;
import org.jupiter.transport.JConnector;
import org.jupiter.transport.UnresolvedAddress;



































public class ProxyFactory<I>
{
  private final Class<I> interfaceClass;
  private String group;
  private String providerName;
  private String version;
  private JClient client;
  private SerializerType serializerType = SerializerType.getDefault();
  
  private LoadBalancerType loadBalancerType = LoadBalancerType.getDefault();
  
  private List<UnresolvedAddress> addresses;
  
  private InvokeType invokeType = InvokeType.getDefault();
  
  private DispatchType dispatchType = DispatchType.getDefault();
  
  private long timeoutMillis;
  
  private List<MethodSpecialConfig> methodSpecialConfigs;
  
  private List<ConsumerInterceptor> interceptors;
  
  private ClusterInvoker.Strategy strategy = ClusterInvoker.Strategy.getDefault();
  
  private int retries = 2;
  
  public static <I> ProxyFactory<I> factory(Class<I> interfaceClass) {
    ProxyFactory<I> factory = new ProxyFactory(interfaceClass);
    
    addresses = Lists.newArrayList();
    interceptors = Lists.newArrayList();
    methodSpecialConfigs = Lists.newArrayList();
    
    return factory;
  }
  
  private ProxyFactory(Class<I> interfaceClass) {
    this.interfaceClass = interfaceClass;
  }
  
  public Class<I> getInterfaceClass() {
    return interfaceClass;
  }
  
  public ProxyFactory<I> group(String group) {
    this.group = group;
    return this;
  }
  
  public ProxyFactory<I> providerName(String providerName) {
    this.providerName = providerName;
    return this;
  }
  
  public ProxyFactory<I> version(String version) {
    this.version = version;
    return this;
  }
  
  public ProxyFactory<I> directory(Directory directory) {
    return group(directory.getGroup()).providerName(directory.getServiceProviderName()).version(directory.getVersion());
  }
  

  public ProxyFactory<I> client(JClient client)
  {
    this.client = client;
    return this;
  }
  
  public ProxyFactory<I> serializerType(SerializerType serializerType) {
    this.serializerType = serializerType;
    return this;
  }
  
  public ProxyFactory<I> loadBalancerType(LoadBalancerType loadBalancerType) {
    this.loadBalancerType = loadBalancerType;
    return this;
  }
  
  public ProxyFactory<I> addProviderAddress(UnresolvedAddress... addresses) {
    Collections.addAll(this.addresses, addresses);
    return this;
  }
  
  public ProxyFactory<I> addProviderAddress(List<UnresolvedAddress> addresses) {
    this.addresses.addAll(addresses);
    return this;
  }
  
  public ProxyFactory<I> invokeType(InvokeType invokeType) {
    this.invokeType = ((InvokeType)Preconditions.checkNotNull(invokeType));
    return this;
  }
  
  public ProxyFactory<I> dispatchType(DispatchType dispatchType) {
    this.dispatchType = ((DispatchType)Preconditions.checkNotNull(dispatchType));
    return this;
  }
  
  public ProxyFactory<I> timeoutMillis(long timeoutMillis) {
    this.timeoutMillis = timeoutMillis;
    return this;
  }
  
  public ProxyFactory<I> addMethodSpecialConfig(MethodSpecialConfig... methodSpecialConfigs) {
    Collections.addAll(this.methodSpecialConfigs, methodSpecialConfigs);
    return this;
  }
  
  public ProxyFactory<I> addInterceptor(ConsumerInterceptor... interceptors) {
    Collections.addAll(this.interceptors, interceptors);
    return this;
  }
  
  public ProxyFactory<I> clusterStrategy(ClusterInvoker.Strategy strategy) {
    this.strategy = strategy;
    return this;
  }
  
  public ProxyFactory<I> failoverRetries(int retries) {
    this.retries = retries;
    return this;
  }
  
  public I newProxyInstance()
  {
    Preconditions.checkNotNull(interfaceClass, "interfaceClass");
    
    ServiceProvider annotation = (ServiceProvider)interfaceClass.getAnnotation(ServiceProvider.class);
    
    if (annotation != null) {
      Preconditions.checkArgument(group == null, interfaceClass.getName() + " has a @ServiceProvider annotation, can't set [group] again");
      


      Preconditions.checkArgument(providerName == null, interfaceClass.getName() + " has a @ServiceProvider annotation, can't set [providerName] again");
      



      group = annotation.group();
      String name = annotation.name();
      providerName = (Strings.isNotBlank(name) ? name : interfaceClass.getName());
    }
    
    Preconditions.checkArgument(Strings.isNotBlank(group), "group");
    Preconditions.checkArgument(Strings.isNotBlank(providerName), "providerName");
    Preconditions.checkNotNull(client, "client");
    Preconditions.checkNotNull(serializerType, "serializerType");
    
    if ((dispatchType == DispatchType.BROADCAST) && (invokeType == InvokeType.SYNC)) {
      throw reject("broadcast & sync unsupported");
    }
    

    ServiceMetadata metadata = new ServiceMetadata(group, providerName, Strings.isNotBlank(version) ? version : "1.0.0");
    




    JConnector<JConnection> connector = client.connector();
    for (UnresolvedAddress address : addresses) {
      connector.addChannelGroup(metadata, connector.group(address));
    }
    

    Dispatcher dispatcher = dispatcher().interceptors(interceptors).timeoutMillis(timeoutMillis).methodSpecialConfigs(methodSpecialConfigs);
    



    ClusterStrategyConfig strategyConfig = ClusterStrategyConfig.of(strategy, retries);
    Object handler;
    Object handler; switch (invokeType) {
    case SYNC: 
      handler = new SyncInvoker(client.appName(), metadata, dispatcher, strategyConfig, methodSpecialConfigs);
      break;
    case ASYNC: 
      handler = new AsyncInvoker(client.appName(), metadata, dispatcher, strategyConfig, methodSpecialConfigs);
      break;
    default: 
      throw reject("invokeType: " + invokeType);
    }
    Object handler;
    return Proxies.getDefault().newProxy(interfaceClass, handler);
  }
  
  protected Dispatcher dispatcher() {
    switch (dispatchType) {
    case ROUND: 
      return new DefaultRoundDispatcher(client, LoadBalancerFactory.loadBalancer(loadBalancerType), serializerType);
    
    case BROADCAST: 
      return new DefaultBroadcastDispatcher(client, serializerType);
    }
    throw reject("dispatchType: " + dispatchType);
  }
  
  private static UnsupportedOperationException reject(String message)
  {
    return new UnsupportedOperationException(message);
  }
}
