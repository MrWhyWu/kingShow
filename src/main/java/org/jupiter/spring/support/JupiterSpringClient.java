package org.jupiter.spring.support;

import java.util.Collections;
import java.util.List;
import org.jupiter.common.util.Lists;
import org.jupiter.common.util.Pair;
import org.jupiter.common.util.Preconditions;
import org.jupiter.common.util.Strings;
import org.jupiter.common.util.SystemPropertyUtil;
import org.jupiter.common.util.ThrowUtil;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;
import org.jupiter.registry.RegistryService.RegistryType;
import org.jupiter.rpc.DefaultClient;
import org.jupiter.rpc.JClient;
import org.jupiter.rpc.consumer.ConsumerInterceptor;
import org.jupiter.transport.JConfig;
import org.jupiter.transport.JConnection;
import org.jupiter.transport.JConnectionManager;
import org.jupiter.transport.JConnector;
import org.jupiter.transport.JOption;
import org.jupiter.transport.UnresolvedAddress;
import org.springframework.beans.factory.InitializingBean;

















public class JupiterSpringClient
  implements InitializingBean
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(JupiterSpringClient.class);
  private JClient client;
  private String appName;
  private RegistryService.RegistryType registryType;
  private JConnector<JConnection> connector;
  private List<Pair<JOption<Object>, String>> childNetOptions;
  private String registryServerAddresses;
  private String providerServerAddresses;
  private List<UnresolvedAddress> providerServerUnresolvedAddresses;
  private boolean hasRegistryServer;
  private ConsumerInterceptor[] globalConsumerInterceptors;
  
  public JupiterSpringClient() {}
  
  public void afterPropertiesSet() throws Exception
  {
    init();
  }
  
  private void init() {
    client = new DefaultClient(appName, registryType);
    if (this.connector == null) {
      this.connector = createDefaultConnector();
    }
    client.withConnector(this.connector);
    
    JConfig child;
    if ((childNetOptions != null) && (!childNetOptions.isEmpty())) {
      child = this.connector.config();
      for (Pair<JOption<Object>, String> config : childNetOptions) {
        child.setOption((JOption)config.getFirst(), config.getSecond());
        logger.info("Setting child net option: {}", config);
      }
    }
    

    if (Strings.isNotBlank(registryServerAddresses)) {
      client.connectToRegistryServer(registryServerAddresses);
      hasRegistryServer = true;
    }
    
    if (!hasRegistryServer)
    {
      if (Strings.isNotBlank(providerServerAddresses)) {
        String[] array = Strings.split(providerServerAddresses, ',');
        providerServerUnresolvedAddresses = Lists.newArrayList();
        for (String s : array) {
          String[] addressStr = Strings.split(s, ':');
          String host = addressStr[0];
          int port = Integer.parseInt(addressStr[1]);
          UnresolvedAddress address = new UnresolvedAddress(host, port);
          providerServerUnresolvedAddresses.add(address);
          
          JConnector<JConnection> connector = client.connector();
          JConnection connection = (JConnection)connector.connect(address, true);
          connector.connectionManager().manage(connection);
        }
      }
    }
    
    Runtime.getRuntime().addShutdownHook(new Thread()
    {
      public void run()
      {
        client.shutdownGracefully();
      }
    });
  }
  
  public JClient getClient() {
    return client;
  }
  
  public void setClient(JClient client) {
    this.client = client;
  }
  
  public String getAppName() {
    return appName;
  }
  
  public void setAppName(String appName) {
    this.appName = appName;
  }
  
  public RegistryService.RegistryType getRegistryType() {
    return registryType;
  }
  
  public void setRegistryType(String registryType) {
    this.registryType = RegistryService.RegistryType.parse(registryType);
  }
  
  public JConnector<JConnection> getConnector() {
    return connector;
  }
  
  public void setConnector(JConnector<JConnection> connector) {
    this.connector = connector;
  }
  
  public List<Pair<JOption<Object>, String>> getChildNetOptions() {
    return childNetOptions;
  }
  
  public void setChildNetOptions(List<Pair<JOption<Object>, String>> childNetOptions) {
    this.childNetOptions = childNetOptions;
  }
  
  public String getRegistryServerAddresses() {
    return registryServerAddresses;
  }
  
  public void setRegistryServerAddresses(String registryServerAddresses) {
    this.registryServerAddresses = registryServerAddresses;
  }
  
  public String getProviderServerAddresses() {
    return providerServerAddresses;
  }
  
  public void setProviderServerAddresses(String providerServerAddresses) {
    this.providerServerAddresses = providerServerAddresses;
  }
  
  public List<UnresolvedAddress> getProviderServerUnresolvedAddresses() {
    return providerServerUnresolvedAddresses == null ? Collections.emptyList() : providerServerUnresolvedAddresses;
  }
  



  public boolean isHasRegistryServer()
  {
    return hasRegistryServer;
  }
  
  public ConsumerInterceptor[] getGlobalConsumerInterceptors() {
    return globalConsumerInterceptors;
  }
  
  public void setGlobalConsumerInterceptors(ConsumerInterceptor[] globalConsumerInterceptors) {
    this.globalConsumerInterceptors = globalConsumerInterceptors;
  }
  
  private JConnector<JConnection> createDefaultConnector()
  {
    JConnector<JConnection> defaultConnector = null;
    try {
      String className = SystemPropertyUtil.get("jupiter.io.default.connector", "org.jupiter.transport.netty.JNettyTcpConnector");
      
      Class<?> clazz = Class.forName(className);
      defaultConnector = (JConnector)clazz.newInstance();
    } catch (Exception e) {
      ThrowUtil.throwException(e);
    }
    return (JConnector)Preconditions.checkNotNull(defaultConnector, "default connector");
  }
}
