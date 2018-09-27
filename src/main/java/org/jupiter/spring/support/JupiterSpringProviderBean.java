package org.jupiter.spring.support;

import java.util.concurrent.Executor;
import org.jupiter.common.util.Preconditions;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;
import org.jupiter.rpc.JRequest;
import org.jupiter.rpc.JServer;
import org.jupiter.rpc.JServer.ProviderInitializer;
import org.jupiter.rpc.JServer.ServiceRegistry;
import org.jupiter.rpc.flow.control.FlowController;
import org.jupiter.rpc.model.metadata.ServiceWrapper;
import org.jupiter.rpc.provider.ProviderInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;


















public class JupiterSpringProviderBean
  implements InitializingBean, ApplicationContextAware
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(JupiterSpringProviderBean.class);
  private ServiceWrapper serviceWrapper;
  private JupiterSpringServer server;
  private Object providerImpl;
  private ProviderInterceptor[] providerInterceptors;
  private int weight;
  private Executor executor;
  private FlowController<JRequest> flowController;
  private JServer.ProviderInitializer<?> providerInitializer;
  private Executor providerInitializerExecutor;
  
  public JupiterSpringProviderBean() {}
  
  public void afterPropertiesSet()
    throws Exception
  {
    init();
  }
  
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
  {
    if ((applicationContext instanceof ConfigurableApplicationContext)) {
      ((ConfigurableApplicationContext)applicationContext).addApplicationListener(new JupiterApplicationListener(null));
    }
  }
  
  private void init() throws Exception {
    Preconditions.checkNotNull(server, "server");
    
    JServer.ServiceRegistry registry = server.getServer().serviceRegistry();
    
    if ((providerInterceptors != null) && (providerInterceptors.length > 0)) {
      registry.provider(providerImpl, providerInterceptors);
    } else {
      registry.provider(providerImpl, new ProviderInterceptor[0]);
    }
    
    serviceWrapper = registry.weight(weight).executor(executor).flowController(flowController).register();
  }
  



  public JupiterSpringServer getServer()
  {
    return server;
  }
  
  public void setServer(JupiterSpringServer server) {
    this.server = server;
  }
  
  public Object getProviderImpl() {
    return providerImpl;
  }
  
  public void setProviderImpl(Object providerImpl) {
    this.providerImpl = providerImpl;
  }
  
  public ProviderInterceptor[] getProviderInterceptors() {
    return providerInterceptors;
  }
  
  public void setProviderInterceptors(ProviderInterceptor[] providerInterceptors) {
    this.providerInterceptors = providerInterceptors;
  }
  
  public int getWeight() {
    return weight;
  }
  
  public void setWeight(int weight) {
    this.weight = weight;
  }
  
  public Executor getExecutor() {
    return executor;
  }
  
  public void setExecutor(Executor executor) {
    this.executor = executor;
  }
  
  public FlowController<JRequest> getFlowController() {
    return flowController;
  }
  
  public void setFlowController(FlowController<JRequest> flowController) {
    this.flowController = flowController;
  }
  
  public JServer.ProviderInitializer<?> getProviderInitializer() {
    return providerInitializer;
  }
  
  public void setProviderInitializer(JServer.ProviderInitializer<?> providerInitializer) {
    this.providerInitializer = providerInitializer;
  }
  
  public Executor getProviderInitializerExecutor() {
    return providerInitializerExecutor;
  }
  
  public void setProviderInitializerExecutor(Executor providerInitializerExecutor) {
    this.providerInitializerExecutor = providerInitializerExecutor;
  }
  
  private final class JupiterApplicationListener implements ApplicationListener {
    private JupiterApplicationListener() {}
    
    public void onApplicationEvent(ApplicationEvent event) {
      if ((server.isHasRegistryServer()) && ((event instanceof ContextRefreshedEvent)))
      {
        if (providerInitializer == null) {
          server.getServer().publish(serviceWrapper);
        } else {
          server.getServer().publishWithInitializer(serviceWrapper, providerInitializer, providerInitializerExecutor);
        }
        

        JupiterSpringProviderBean.logger.info("#publish service: {}.", serviceWrapper);
      }
    }
  }
}
