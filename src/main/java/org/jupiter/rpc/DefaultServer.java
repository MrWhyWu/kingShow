package org.jupiter.rpc;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import org.jupiter.common.util.ClassUtil;
import org.jupiter.common.util.JConstants;
import org.jupiter.common.util.JServiceLoader;
import org.jupiter.common.util.Lists;
import org.jupiter.common.util.Maps;
import org.jupiter.common.util.Pair;
import org.jupiter.common.util.Preconditions;
import org.jupiter.common.util.StackTraceUtil;
import org.jupiter.common.util.Strings;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;
import org.jupiter.registry.RegisterMeta;
import org.jupiter.registry.RegistryService;
import org.jupiter.registry.RegistryService.RegistryType;
import org.jupiter.rpc.flow.control.ControlResult;
import org.jupiter.rpc.flow.control.FlowController;
import org.jupiter.rpc.model.metadata.ServiceMetadata;
import org.jupiter.rpc.model.metadata.ServiceWrapper;
import org.jupiter.rpc.provider.ProviderInterceptor;
import org.jupiter.rpc.provider.processor.DefaultProviderProcessor;
import org.jupiter.transport.Directory;
import org.jupiter.transport.JAcceptor;




















public class DefaultServer
  implements JServer
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultServer.class);
  

  static
  {
    ClassUtil.initializeClass("org.jupiter.rpc.tracing.TracingUtil", 500L);
  }
  

  private final ServiceProviderContainer providerContainer = new DefaultServiceProviderContainer(null);
  
  private final RegistryService registryService;
  
  private ProviderInterceptor[] globalInterceptors;
  
  private FlowController<JRequest> globalFlowController;
  
  private JAcceptor acceptor;
  

  public DefaultServer()
  {
    this(RegistryService.RegistryType.DEFAULT);
  }
  
  public DefaultServer(RegistryService.RegistryType registryType) {
    registryType = registryType == null ? RegistryService.RegistryType.DEFAULT : registryType;
    registryService = ((RegistryService)JServiceLoader.load(RegistryService.class).find(registryType.getValue()));
  }
  
  public JAcceptor acceptor()
  {
    return acceptor;
  }
  
  public JServer withAcceptor(JAcceptor acceptor)
  {
    if (acceptor.processor() == null) {
      acceptor.withProcessor(new DefaultProviderProcessor()
      {
        public ServiceWrapper lookupService(Directory directory)
        {
          return providerContainer.lookupService(directory.directoryString());
        }
        

        public ControlResult flowControl(JRequest request)
        {
          if (globalFlowController == null) {
            return ControlResult.ALLOWED;
          }
          return globalFlowController.flowControl(request);
        }
      });
    }
    this.acceptor = acceptor;
    return this;
  }
  
  public RegistryService registryService()
  {
    return registryService;
  }
  
  public void connectToRegistryServer(String connectString)
  {
    registryService.connectToRegistryServer(connectString);
  }
  
  public void withGlobalInterceptors(ProviderInterceptor... globalInterceptors)
  {
    this.globalInterceptors = globalInterceptors;
  }
  
  public FlowController<JRequest> globalFlowController()
  {
    return globalFlowController;
  }
  
  public void withGlobalFlowController(FlowController<JRequest> globalFlowController)
  {
    this.globalFlowController = globalFlowController;
  }
  
  public JServer.ServiceRegistry serviceRegistry()
  {
    return new DefaultServiceRegistry();
  }
  
  public ServiceWrapper lookupService(Directory directory)
  {
    return providerContainer.lookupService(directory.directoryString());
  }
  
  public ServiceWrapper removeService(Directory directory)
  {
    return providerContainer.removeService(directory.directoryString());
  }
  
  public List<ServiceWrapper> allRegisteredServices()
  {
    return providerContainer.getAllServices();
  }
  
  public void publish(ServiceWrapper serviceWrapper)
  {
    ServiceMetadata metadata = serviceWrapper.getMetadata();
    
    RegisterMeta meta = new RegisterMeta();
    meta.setPort(acceptor.boundPort());
    meta.setGroup(metadata.getGroup());
    meta.setServiceProviderName(metadata.getServiceProviderName());
    meta.setVersion(metadata.getVersion());
    meta.setWeight(serviceWrapper.getWeight());
    meta.setConnCount(JConstants.SUGGESTED_CONNECTION_COUNT);
    
    registryService.register(meta);
  }
  
  public void publish(ServiceWrapper... serviceWrappers)
  {
    for (ServiceWrapper wrapper : serviceWrappers) {
      publish(wrapper);
    }
  }
  

  public <T> void publishWithInitializer(final ServiceWrapper serviceWrapper, final JServer.ProviderInitializer<T> initializer, Executor executor)
  {
    Runnable task = new Runnable()
    {
      public void run()
      {
        try
        {
          initializer.init(serviceWrapper.getServiceProvider());
          publish(serviceWrapper);
        } catch (Exception e) {
          DefaultServer.logger.error("Error on {} #publishWithInitializer: {}.", serviceWrapper.getMetadata(), StackTraceUtil.stackTrace(e));
        }
      }
    };
    
    if (executor == null) {
      task.run();
    } else {
      executor.execute(task);
    }
  }
  
  public void publishAll()
  {
    for (ServiceWrapper wrapper : providerContainer.getAllServices()) {
      publish(wrapper);
    }
  }
  

  public void unpublish(ServiceWrapper serviceWrapper)
  {
    ServiceMetadata metadata = serviceWrapper.getMetadata();
    
    RegisterMeta meta = new RegisterMeta();
    meta.setPort(acceptor.boundPort());
    meta.setGroup(metadata.getGroup());
    meta.setVersion(metadata.getVersion());
    meta.setServiceProviderName(metadata.getServiceProviderName());
    meta.setWeight(serviceWrapper.getWeight());
    meta.setConnCount(JConstants.SUGGESTED_CONNECTION_COUNT);
    
    registryService.unregister(meta);
  }
  

  public void unpublishAll()
  {
    for (ServiceWrapper wrapper : providerContainer.getAllServices()) {
      unpublish(wrapper);
    }
  }
  
  public void start() throws InterruptedException
  {
    acceptor.start();
  }
  
  public void start(boolean sync) throws InterruptedException
  {
    acceptor.start(sync);
  }
  
  public void shutdownGracefully()
  {
    registryService.shutdownGracefully();
    acceptor.shutdownGracefully();
  }
  
  public void setAcceptor(JAcceptor acceptor) {
    withAcceptor(acceptor);
  }
  









  ServiceWrapper registerService(String group, String providerName, String version, Object serviceProvider, ProviderInterceptor[] interceptors, Map<String, List<Pair<Class<?>[], Class<?>[]>>> extensions, int weight, Executor executor, FlowController<JRequest> flowController)
  {
    ProviderInterceptor[] allInterceptors = null;
    List<ProviderInterceptor> tempList = Lists.newArrayList();
    if (globalInterceptors != null) {
      Collections.addAll(tempList, globalInterceptors);
    }
    if (interceptors != null) {
      Collections.addAll(tempList, interceptors);
    }
    if (!tempList.isEmpty()) {
      allInterceptors = (ProviderInterceptor[])tempList.toArray(new ProviderInterceptor[tempList.size()]);
    }
    
    ServiceWrapper wrapper = new ServiceWrapper(group, providerName, version, serviceProvider, allInterceptors, extensions);
    

    wrapper.setWeight(weight);
    wrapper.setExecutor(executor);
    wrapper.setFlowController(flowController);
    
    providerContainer.registerService(wrapper.getMetadata().directoryString(), wrapper);
    
    return wrapper;
  }
  
  class DefaultServiceRegistry implements JServer.ServiceRegistry {
    private Object serviceProvider;
    private ProviderInterceptor[] interceptors;
    private Class<?> interfaceClass;
    private String group;
    private String providerName;
    private String version;
    private int weight;
    private Executor executor;
    private FlowController<JRequest> flowController;
    
    DefaultServiceRegistry() {}
    
    public JServer.ServiceRegistry provider(Object serviceProvider, ProviderInterceptor... interceptors) {
      this.serviceProvider = serviceProvider;
      this.interceptors = interceptors;
      return this;
    }
    
    public JServer.ServiceRegistry interfaceClass(Class<?> interfaceClass)
    {
      this.interfaceClass = interfaceClass;
      return this;
    }
    
    public JServer.ServiceRegistry group(String group)
    {
      this.group = group;
      return this;
    }
    
    public JServer.ServiceRegistry providerName(String providerName)
    {
      this.providerName = providerName;
      return this;
    }
    
    public JServer.ServiceRegistry version(String version)
    {
      this.version = version;
      return this;
    }
    
    public JServer.ServiceRegistry weight(int weight)
    {
      this.weight = weight;
      return this;
    }
    
    public JServer.ServiceRegistry executor(Executor executor)
    {
      this.executor = executor;
      return this;
    }
    
    public JServer.ServiceRegistry flowController(FlowController<JRequest> flowController)
    {
      this.flowController = flowController;
      return this;
    }
    
    public ServiceWrapper register()
    {
      Preconditions.checkNotNull(serviceProvider, "serviceProvider");
      
      Class<?> providerClass = serviceProvider.getClass();
      
      ServiceProviderImpl implAnnotation = null;
      ServiceProvider ifAnnotation = null;
      for (Class<?> cls = providerClass; cls != Object.class; cls = cls.getSuperclass()) {
        if (implAnnotation == null) {
          implAnnotation = (ServiceProviderImpl)cls.getAnnotation(ServiceProviderImpl.class);
        }
        
        Class<?>[] interfaces = cls.getInterfaces();
        if (interfaces != null) {
          for (Class<?> i : interfaces) {
            ifAnnotation = (ServiceProvider)i.getAnnotation(ServiceProvider.class);
            if (ifAnnotation != null)
            {


              Preconditions.checkArgument(interfaceClass == null, i.getName() + " has a @ServiceProvider annotation, can't set [interfaceClass] again");
              



              interfaceClass = i;
              break;
            }
          }
        }
        if ((implAnnotation != null) && (ifAnnotation != null)) {
          break;
        }
      }
      
      if (ifAnnotation != null) {
        Preconditions.checkArgument(group == null, interfaceClass.getName() + " has a @ServiceProvider annotation, can't set [group] again");
        


        Preconditions.checkArgument(providerName == null, interfaceClass.getName() + " has a @ServiceProvider annotation, can't set [providerName] again");
        



        group = ifAnnotation.group();
        String name = ifAnnotation.name();
        providerName = (Strings.isNotBlank(name) ? name : interfaceClass.getName());
      }
      
      if (implAnnotation != null) {
        Preconditions.checkArgument(version == null, providerClass.getName() + " has a @ServiceProviderImpl annotation, can't set [version] again");
        



        version = implAnnotation.version();
      }
      
      Preconditions.checkNotNull(interfaceClass, "interfaceClass");
      Preconditions.checkArgument(Strings.isNotBlank(group), "group");
      Preconditions.checkArgument(Strings.isNotBlank(providerName), "providerName");
      Preconditions.checkArgument(Strings.isNotBlank(version), "version");
      





      Map<String, List<Pair<Class<?>[], Class<?>[]>>> extensions = Maps.newHashMap();
      for (Method method : interfaceClass.getMethods()) {
        String methodName = method.getName();
        List<Pair<Class<?>[], Class<?>[]>> list = (List)extensions.get(methodName);
        if (list == null) {
          list = Lists.newArrayList();
          extensions.put(methodName, list);
        }
        list.add(Pair.of(method.getParameterTypes(), method.getExceptionTypes()));
      }
      
      return registerService(group, providerName, version, serviceProvider, interceptors, extensions, weight, executor, flowController);
    }
  }
  






  static abstract interface ServiceProviderContainer
  {
    public abstract void registerService(String paramString, ServiceWrapper paramServiceWrapper);
    






    public abstract ServiceWrapper lookupService(String paramString);
    





    public abstract ServiceWrapper removeService(String paramString);
    





    public abstract List<ServiceWrapper> getAllServices();
  }
  





  private static final class DefaultServiceProviderContainer
    implements DefaultServer.ServiceProviderContainer
  {
    private final ConcurrentMap<String, ServiceWrapper> serviceProviders = Maps.newConcurrentMap();
    
    private DefaultServiceProviderContainer() {}
    
    public void registerService(String uniqueKey, ServiceWrapper serviceWrapper) { serviceProviders.put(uniqueKey, serviceWrapper);
      
      DefaultServer.logger.info("ServiceProvider [{}, {}] is registered.", uniqueKey, serviceWrapper);
    }
    
    public ServiceWrapper lookupService(String uniqueKey)
    {
      return (ServiceWrapper)serviceProviders.get(uniqueKey);
    }
    
    public ServiceWrapper removeService(String uniqueKey)
    {
      ServiceWrapper serviceWrapper = (ServiceWrapper)serviceProviders.remove(uniqueKey);
      if (serviceWrapper == null) {
        DefaultServer.logger.warn("ServiceProvider [{}] not found.", uniqueKey);
      } else {
        DefaultServer.logger.info("ServiceProvider [{}, {}] is removed.", uniqueKey, serviceWrapper);
      }
      return serviceWrapper;
    }
    
    public List<ServiceWrapper> getAllServices()
    {
      return Lists.newArrayList(serviceProviders.values());
    }
  }
}
