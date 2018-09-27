package org.jupiter.rpc;

import java.util.List;
import java.util.concurrent.Executor;
import org.jupiter.registry.Registry;
import org.jupiter.registry.RegistryService;
import org.jupiter.rpc.flow.control.FlowController;
import org.jupiter.rpc.model.metadata.ServiceWrapper;
import org.jupiter.rpc.provider.ProviderInterceptor;
import org.jupiter.transport.Directory;
import org.jupiter.transport.JAcceptor;

public abstract interface JServer
  extends Registry
{
  public abstract JAcceptor acceptor();
  
  public abstract JServer withAcceptor(JAcceptor paramJAcceptor);
  
  public abstract RegistryService registryService();
  
  public abstract void withGlobalInterceptors(ProviderInterceptor... paramVarArgs);
  
  public abstract FlowController<JRequest> globalFlowController();
  
  public abstract void withGlobalFlowController(FlowController<JRequest> paramFlowController);
  
  public abstract ServiceRegistry serviceRegistry();
  
  public abstract ServiceWrapper lookupService(Directory paramDirectory);
  
  public abstract ServiceWrapper removeService(Directory paramDirectory);
  
  public abstract List<ServiceWrapper> allRegisteredServices();
  
  public abstract void publish(ServiceWrapper paramServiceWrapper);
  
  public abstract void publish(ServiceWrapper... paramVarArgs);
  
  public abstract <T> void publishWithInitializer(ServiceWrapper paramServiceWrapper, ProviderInitializer<T> paramProviderInitializer, Executor paramExecutor);
  
  public abstract void publishAll();
  
  public abstract void unpublish(ServiceWrapper paramServiceWrapper);
  
  public abstract void unpublishAll();
  
  public abstract void start()
    throws InterruptedException;
  
  public abstract void start(boolean paramBoolean)
    throws InterruptedException;
  
  public abstract void shutdownGracefully();
  
  public static abstract interface ProviderInitializer<T>
  {
    public abstract void init(T paramT);
  }
  
  public static abstract interface ServiceRegistry
  {
    public abstract ServiceRegistry provider(Object paramObject, ProviderInterceptor... paramVarArgs);
    
    public abstract ServiceRegistry interfaceClass(Class<?> paramClass);
    
    public abstract ServiceRegistry group(String paramString);
    
    public abstract ServiceRegistry providerName(String paramString);
    
    public abstract ServiceRegistry version(String paramString);
    
    public abstract ServiceRegistry weight(int paramInt);
    
    public abstract ServiceRegistry executor(Executor paramExecutor);
    
    public abstract ServiceRegistry flowController(FlowController<JRequest> paramFlowController);
    
    public abstract ServiceWrapper register();
  }
}
