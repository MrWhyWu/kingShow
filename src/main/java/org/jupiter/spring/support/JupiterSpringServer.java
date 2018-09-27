package org.jupiter.spring.support;

import java.util.List;
import org.jupiter.common.util.Pair;
import org.jupiter.common.util.Preconditions;
import org.jupiter.common.util.Strings;
import org.jupiter.common.util.SystemPropertyUtil;
import org.jupiter.common.util.ThrowUtil;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;
import org.jupiter.registry.RegistryService.RegistryType;
import org.jupiter.rpc.DefaultServer;
import org.jupiter.rpc.JRequest;
import org.jupiter.rpc.JServer;
import org.jupiter.rpc.flow.control.FlowController;
import org.jupiter.rpc.provider.ProviderInterceptor;
import org.jupiter.transport.JAcceptor;
import org.jupiter.transport.JConfig;
import org.jupiter.transport.JConfigGroup;
import org.jupiter.transport.JOption;
import org.springframework.beans.factory.InitializingBean;


























public class JupiterSpringServer
  implements InitializingBean
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(JupiterSpringServer.class);
  private JServer server;
  private RegistryService.RegistryType registryType;
  private JAcceptor acceptor;
  private List<Pair<JOption<Object>, String>> parentNetOptions;
  private List<Pair<JOption<Object>, String>> childNetOptions;
  private String registryServerAddresses;
  private boolean hasRegistryServer;
  private ProviderInterceptor[] globalProviderInterceptors;
  private FlowController<JRequest> globalFlowController;
  
  public JupiterSpringServer() {}
  
  public void afterPropertiesSet() throws Exception
  {
    init();
  }
  
  private void init() {
    server = new DefaultServer(registryType);
    if (acceptor == null) {
      acceptor = createDefaultAcceptor();
    }
    server.withAcceptor(acceptor);
    

    JConfigGroup configGroup = acceptor.configGroup();
    JConfig parent; if ((parentNetOptions != null) && (!parentNetOptions.isEmpty())) {
      parent = configGroup.parent();
      for (Pair<JOption<Object>, String> config : parentNetOptions) {
        parent.setOption((JOption)config.getFirst(), config.getSecond());
        logger.info("Setting parent net option: {}", config);
      } }
    JConfig child;
    if ((childNetOptions != null) && (!childNetOptions.isEmpty())) {
      child = configGroup.child();
      for (Pair<JOption<Object>, String> config : childNetOptions) {
        child.setOption((JOption)config.getFirst(), config.getSecond());
        logger.info("Setting child net option: {}", config);
      }
    }
    

    if (Strings.isNotBlank(registryServerAddresses)) {
      server.connectToRegistryServer(registryServerAddresses);
      hasRegistryServer = true;
    }
    

    if ((globalProviderInterceptors != null) && (globalProviderInterceptors.length > 0)) {
      server.withGlobalInterceptors(globalProviderInterceptors);
    }
    

    server.withGlobalFlowController(globalFlowController);
    
    Runtime.getRuntime().addShutdownHook(new Thread()
    {
      public void run()
      {
        server.shutdownGracefully();
      }
    });
    try
    {
      server.start(false);
    } catch (Exception e) {
      ThrowUtil.throwException(e);
    }
  }
  
  public JServer getServer() {
    return server;
  }
  
  public void setServer(JServer server) {
    this.server = server;
  }
  
  public RegistryService.RegistryType getRegistryType() {
    return registryType;
  }
  
  public void setRegistryType(String registryType) {
    this.registryType = RegistryService.RegistryType.parse(registryType);
  }
  
  public JAcceptor getAcceptor() {
    return acceptor;
  }
  
  public void setAcceptor(JAcceptor acceptor) {
    this.acceptor = acceptor;
  }
  
  public List<Pair<JOption<Object>, String>> getParentNetOptions() {
    return parentNetOptions;
  }
  
  public void setParentNetOptions(List<Pair<JOption<Object>, String>> parentNetOptions) {
    this.parentNetOptions = parentNetOptions;
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
  
  public boolean isHasRegistryServer() {
    return hasRegistryServer;
  }
  
  public void setHasRegistryServer(boolean hasRegistryServer) {
    this.hasRegistryServer = hasRegistryServer;
  }
  
  public ProviderInterceptor[] getGlobalProviderInterceptors() {
    return globalProviderInterceptors;
  }
  
  public void setGlobalProviderInterceptors(ProviderInterceptor[] globalProviderInterceptors) {
    this.globalProviderInterceptors = globalProviderInterceptors;
  }
  
  public FlowController<JRequest> getGlobalFlowController() {
    return globalFlowController;
  }
  
  public void setGlobalFlowController(FlowController<JRequest> globalFlowController) {
    this.globalFlowController = globalFlowController;
  }
  
  private JAcceptor createDefaultAcceptor() {
    JAcceptor defaultAcceptor = null;
    try {
      String className = SystemPropertyUtil.get("jupiter.io.default.acceptor", "org.jupiter.transport.netty.JNettyTcpAcceptor");
      
      Class<?> clazz = Class.forName(className);
      defaultAcceptor = (JAcceptor)clazz.newInstance();
    } catch (Exception e) {
      ThrowUtil.throwException(e);
    }
    return (JAcceptor)Preconditions.checkNotNull(defaultAcceptor, "default acceptor");
  }
}
