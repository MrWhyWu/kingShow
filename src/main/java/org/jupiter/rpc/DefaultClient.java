package org.jupiter.rpc;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.jupiter.common.util.ClassUtil;
import org.jupiter.common.util.JServiceLoader;
import org.jupiter.common.util.Preconditions;
import org.jupiter.common.util.Strings;
import org.jupiter.common.util.ThrowUtil;
import org.jupiter.registry.AbstractRegistryService;
import org.jupiter.registry.NotifyListener;
import org.jupiter.registry.NotifyListener.NotifyEvent;
import org.jupiter.registry.OfflineListener;
import org.jupiter.registry.RegisterMeta;
import org.jupiter.registry.RegisterMeta.Address;
import org.jupiter.registry.RegisterMeta.ServiceMeta;
import org.jupiter.registry.RegistryService;
import org.jupiter.registry.RegistryService.RegistryType;
import org.jupiter.rpc.consumer.processor.DefaultConsumerProcessor;
import org.jupiter.rpc.model.metadata.ServiceMetadata;
import org.jupiter.transport.Directory;
import org.jupiter.transport.JConnection;
import org.jupiter.transport.JConnection.OperationListener;
import org.jupiter.transport.JConnectionManager;
import org.jupiter.transport.JConnector;
import org.jupiter.transport.JConnector.ConnectionWatcher;
import org.jupiter.transport.UnresolvedAddress;
import org.jupiter.transport.channel.DirectoryJChannelGroup;
import org.jupiter.transport.channel.JChannelGroup;





public class DefaultClient
  implements JClient
{
  private final RegistryService registryService;
  private final String appName;
  private JConnector<JConnection> connector;
  
  static
  {
    ClassUtil.initializeClass("org.jupiter.rpc.tracing.TracingUtil", 500L);
  }
  





  public DefaultClient()
  {
    this("UNKNOWN", RegistryService.RegistryType.DEFAULT);
  }
  
  public DefaultClient(RegistryService.RegistryType registryType) {
    this("UNKNOWN", registryType);
  }
  
  public DefaultClient(String appName) {
    this(appName, RegistryService.RegistryType.DEFAULT);
  }
  
  public DefaultClient(String appName, RegistryService.RegistryType registryType) {
    this.appName = (Strings.isBlank(appName) ? "UNKNOWN" : appName);
    registryType = registryType == null ? RegistryService.RegistryType.DEFAULT : registryType;
    registryService = ((RegistryService)JServiceLoader.load(RegistryService.class).find(registryType.getValue()));
  }
  
  public String appName()
  {
    return appName;
  }
  
  public JConnector<JConnection> connector()
  {
    return connector;
  }
  
  public JClient withConnector(JConnector<JConnection> connector)
  {
    if (connector.processor() == null) {
      connector.withProcessor(new DefaultConsumerProcessor());
    }
    this.connector = connector;
    return this;
  }
  
  public RegistryService registryService()
  {
    return registryService;
  }
  
  public Collection<RegisterMeta> lookup(Directory directory)
  {
    RegisterMeta.ServiceMeta serviceMeta = toServiceMeta(directory);
    return registryService.lookup(serviceMeta);
  }
  
  public JConnector.ConnectionWatcher watchConnections(Class<?> interfaceClass)
  {
    return watchConnections(interfaceClass, "1.0.0");
  }
  
  public JConnector.ConnectionWatcher watchConnections(Class<?> interfaceClass, String version)
  {
    Preconditions.checkNotNull(interfaceClass, "interfaceClass");
    ServiceProvider annotation = (ServiceProvider)interfaceClass.getAnnotation(ServiceProvider.class);
    Preconditions.checkNotNull(annotation, interfaceClass + " is not a ServiceProvider interface");
    String providerName = annotation.name();
    providerName = Strings.isNotBlank(providerName) ? providerName : interfaceClass.getName();
    version = Strings.isNotBlank(version) ? version : "1.0.0";
    
    return watchConnections(new ServiceMetadata(annotation.group(), providerName, version));
  }
  
  public JConnector.ConnectionWatcher watchConnections(final Directory directory)
  {
    JConnector.ConnectionWatcher manager = new JConnector.ConnectionWatcher()
    {
      private final JConnectionManager connectionManager = connector.connectionManager();
      
      private final ReentrantLock lock = new ReentrantLock();
      private final Condition notifyCondition = lock.newCondition();
      
      private final AtomicBoolean signalNeeded = new AtomicBoolean(false);
      
      public void start()
      {
        subscribe(directory, new NotifyListener()
        {
          public void notify(RegisterMeta registerMeta, NotifyListener.NotifyEvent event)
          {
            UnresolvedAddress address = new UnresolvedAddress(registerMeta.getHost(), registerMeta.getPort());
            final JChannelGroup group = connector.group(address);
            if (event == NotifyListener.NotifyEvent.CHILD_ADDED) {
              if (group.isAvailable()) {
                onSucceed(group, signalNeeded.getAndSet(false));
              }
              else if (group.isConnecting()) {
                group.onAvailable(new Runnable()
                {
                  public void run()
                  {
                    DefaultClient.1.1.this.onSucceed(group, signalNeeded.getAndSet(false));
                  }
                });
              } else {
                group.setConnecting(true);
                JConnection[] connections = connectTo(address, group, registerMeta, true);
                final AtomicInteger countdown = new AtomicInteger(connections.length);
                for (JConnection c : connections) {
                  c.operationComplete(new JConnection.OperationListener()
                  {
                    public void complete(boolean isSuccess)
                    {
                      if (isSuccess) {
                        DefaultClient.1.1.this.onSucceed(group, signalNeeded.getAndSet(false));
                      }
                      if (countdown.decrementAndGet() <= 0) {
                        group.setConnecting(false);
                      }
                    }
                  });
                }
              }
              
              group.putWeight(val$directory, registerMeta.getWeight());
            } else if (event == NotifyListener.NotifyEvent.CHILD_REMOVED) {
              connector.removeChannelGroup(val$directory, group);
              group.removeWeight(val$directory);
              if (connector.directoryGroup().getRefCount(group) <= 0) {
                connectionManager.cancelAutoReconnect(address);
              }
            }
          }
          
          private JConnection[] connectTo(final UnresolvedAddress address, final JChannelGroup group, RegisterMeta registerMeta, boolean async) {
            int connCount = registerMeta.getConnCount();
            connCount = connCount < 1 ? 1 : connCount;
            
            JConnection[] connections = new JConnection[connCount];
            group.setCapacity(connCount);
            for (int i = 0; i < connCount; i++) {
              JConnection connection = (JConnection)connector.connect(address, async);
              connections[i] = connection;
              connectionManager.manage(connection);
            }
            
            offlineListening(address, new OfflineListener()
            {
              public void offline()
              {
                connectionManager.cancelAutoReconnect(address);
                if (!group.isAvailable()) {
                  connector.removeChannelGroup(val$directory, group);
                }
                
              }
            });
            return connections;
          }
          
          private void onSucceed(JChannelGroup group, boolean doSignal) {
            connector.addChannelGroup(val$directory, group);
            
            if (doSignal) {
              ReentrantLock _look = lock;
              _look.lock();
              try {
                notifyCondition.signalAll();
              } finally {
                _look.unlock();
              }
            }
          }
        });
      }
      
      public boolean waitForAvailable(long timeoutMillis)
      {
        if (connector.isDirectoryAvailable(directory)) {
          return true;
        }
        
        long remains = TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
        
        boolean available = false;
        ReentrantLock _look = lock;
        _look.lock();
        try {
          signalNeeded.set(true);
          
          while (!(available = connector.isDirectoryAvailable(directory))) {
            if ((remains = notifyCondition.awaitNanos(remains)) <= 0L) {
              break;
            }
          }
        } catch (InterruptedException e) {
          ThrowUtil.throwException(e);
        } finally {
          _look.unlock();
        }
        
        return (available) || (connector.isDirectoryAvailable(directory));
      }
      
    };
    manager.start();
    
    return manager;
  }
  
  public boolean awaitConnections(Class<?> interfaceClass, long timeoutMillis)
  {
    return awaitConnections(interfaceClass, "1.0.0", timeoutMillis);
  }
  
  public boolean awaitConnections(Class<?> interfaceClass, String version, long timeoutMillis)
  {
    JConnector.ConnectionWatcher watcher = watchConnections(interfaceClass, version);
    return watcher.waitForAvailable(timeoutMillis);
  }
  
  public boolean awaitConnections(Directory directory, long timeoutMillis)
  {
    JConnector.ConnectionWatcher watcher = watchConnections(directory);
    return watcher.waitForAvailable(timeoutMillis);
  }
  
  public void subscribe(Directory directory, NotifyListener listener)
  {
    registryService.subscribe(toServiceMeta(directory), listener);
  }
  
  public void offlineListening(UnresolvedAddress address, OfflineListener listener)
  {
    if ((registryService instanceof AbstractRegistryService)) {
      ((AbstractRegistryService)registryService).offlineListening(toAddress(address), listener);
    } else {
      throw new UnsupportedOperationException();
    }
  }
  
  public void shutdownGracefully()
  {
    registryService.shutdownGracefully();
    connector.shutdownGracefully();
  }
  
  public void connectToRegistryServer(String connectString)
  {
    registryService.connectToRegistryServer(connectString);
  }
  
  public void setConnector(JConnector<JConnection> connector)
  {
    withConnector(connector);
  }
  
  private static RegisterMeta.ServiceMeta toServiceMeta(Directory directory) {
    RegisterMeta.ServiceMeta serviceMeta = new RegisterMeta.ServiceMeta();
    serviceMeta.setGroup((String)Preconditions.checkNotNull(directory.getGroup(), "group"));
    serviceMeta.setServiceProviderName((String)Preconditions.checkNotNull(directory.getServiceProviderName(), "serviceProviderName"));
    serviceMeta.setVersion((String)Preconditions.checkNotNull(directory.getVersion(), "version"));
    return serviceMeta;
  }
  
  private static RegisterMeta.Address toAddress(UnresolvedAddress address) {
    return new RegisterMeta.Address(address.getHost(), address.getPort());
  }
}
