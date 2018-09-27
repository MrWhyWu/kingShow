package org.jupiter.registry.zookeeper;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLBackgroundPathAndBytesable;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CreateBuilder;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.DeleteBuilder;
import org.apache.curator.framework.api.ExistsBuilder;
import org.apache.curator.framework.api.GetChildrenBuilder;
import org.apache.curator.framework.api.PathAndBytesable;
import org.apache.curator.framework.api.Pathable;
import org.apache.curator.framework.api.ProtectACLCreateModePathAndBytesable;
import org.apache.curator.framework.listen.Listenable;
import org.apache.curator.framework.listen.ListenerContainer;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.Code;
import org.jupiter.common.concurrent.collection.ConcurrentSet;
import org.jupiter.common.util.Function;
import org.jupiter.common.util.Lists;
import org.jupiter.common.util.Maps;
import org.jupiter.common.util.NetUtil;
import org.jupiter.common.util.Preconditions;
import org.jupiter.common.util.SpiMetadata;
import org.jupiter.common.util.StackTraceUtil;
import org.jupiter.common.util.Strings;
import org.jupiter.common.util.SystemPropertyUtil;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;
import org.jupiter.registry.AbstractRegistryService;
import org.jupiter.registry.NotifyListener.NotifyEvent;
import org.jupiter.registry.RegisterMeta;
import org.jupiter.registry.RegisterMeta.Address;
import org.jupiter.registry.RegisterMeta.ServiceMeta;
import org.jupiter.registry.RegistryService.RegisterState;







@SpiMetadata(name="zookeeper")
public class ZookeeperRegistryService
  extends AbstractRegistryService
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(ZookeeperRegistryService.class);
  

  private static final AtomicLong sequence = new AtomicLong(0L);
  
  private final String address = SystemPropertyUtil.get("jupiter.local.address", NetUtil.getLocalAddress());
  
  private final int sessionTimeoutMs = SystemPropertyUtil.getInt("jupiter.registry.zookeeper.sessionTimeoutMs", 60000);
  private final int connectionTimeoutMs = SystemPropertyUtil.getInt("jupiter.registry.zookeeper.connectionTimeoutMs", 15000);
  
  private final ConcurrentMap<RegisterMeta.ServiceMeta, PathChildrenCache> pathChildrenCaches = Maps.newConcurrentMap();
  
  private final ConcurrentMap<RegisterMeta.Address, ConcurrentSet<RegisterMeta.ServiceMeta>> serviceMetaMap = Maps.newConcurrentMap();
  private CuratorFramework configClient;
  
  public ZookeeperRegistryService() {}
  
  public Collection<RegisterMeta> lookup(RegisterMeta.ServiceMeta serviceMeta) {
    String directory = String.format("/jupiter/provider/%s/%s/%s", new Object[] { serviceMeta.getGroup(), serviceMeta.getServiceProviderName(), serviceMeta.getVersion() });
    



    List<RegisterMeta> registerMetaList = Lists.newArrayList();
    try {
      List<String> paths = (List)configClient.getChildren().forPath(directory);
      for (String p : paths) {
        registerMetaList.add(parseRegisterMeta(String.format("%s/%s", new Object[] { directory, p })));
      }
    } catch (Exception e) {
      if (logger.isWarnEnabled()) {
        logger.warn("Lookup service meta: {} path failed, {}.", serviceMeta, StackTraceUtil.stackTrace(e));
      }
    }
    return registerMetaList;
  }
  
  protected void doSubscribe(RegisterMeta.ServiceMeta serviceMeta)
  {
    PathChildrenCache childrenCache = (PathChildrenCache)pathChildrenCaches.get(serviceMeta);
    if (childrenCache == null) {
      String directory = String.format("/jupiter/provider/%s/%s/%s", new Object[] { serviceMeta.getGroup(), serviceMeta.getServiceProviderName(), serviceMeta.getVersion() });
      



      PathChildrenCache newChildrenCache = new PathChildrenCache(configClient, directory, false);
      childrenCache = (PathChildrenCache)pathChildrenCaches.putIfAbsent(serviceMeta, newChildrenCache);
      if (childrenCache == null) {
        childrenCache = newChildrenCache;
        
        childrenCache.getListenable().addListener(new PathChildrenCacheListener()
        {
          public void childEvent(CuratorFramework client, PathChildrenCacheEvent event)
            throws Exception
          {
            ZookeeperRegistryService.logger.info("Child event: {}", event);
            
            switch (ZookeeperRegistryService.6.$SwitchMap$org$apache$curator$framework$recipes$cache$PathChildrenCacheEvent$Type[event.getType().ordinal()]) {
            case 1: 
              RegisterMeta registerMeta = ZookeeperRegistryService.this.parseRegisterMeta(event.getData().getPath());
              RegisterMeta.Address address = registerMeta.getAddress();
              RegisterMeta.ServiceMeta serviceMeta = registerMeta.getServiceMeta();
              ConcurrentSet<RegisterMeta.ServiceMeta> serviceMetaSet = ZookeeperRegistryService.this.getServiceMeta(address);
              
              serviceMetaSet.add(serviceMeta);
              ZookeeperRegistryService.this.notify(serviceMeta, NotifyListener.NotifyEvent.CHILD_ADDED, ZookeeperRegistryService.sequence.incrementAndGet(), new RegisterMeta[] { registerMeta });
              




              break;
            
            case 2: 
              RegisterMeta registerMeta = ZookeeperRegistryService.this.parseRegisterMeta(event.getData().getPath());
              RegisterMeta.Address address = registerMeta.getAddress();
              RegisterMeta.ServiceMeta serviceMeta = registerMeta.getServiceMeta();
              ConcurrentSet<RegisterMeta.ServiceMeta> serviceMetaSet = ZookeeperRegistryService.this.getServiceMeta(address);
              
              serviceMetaSet.remove(serviceMeta);
              ZookeeperRegistryService.this.notify(serviceMeta, NotifyListener.NotifyEvent.CHILD_REMOVED, ZookeeperRegistryService.sequence.incrementAndGet(), new RegisterMeta[] { registerMeta });
              




              if (serviceMetaSet.isEmpty()) {
                ZookeeperRegistryService.logger.info("Offline notify: {}.", address);
                
                ZookeeperRegistryService.this.offline(address);
              }
              break;
            }
            
          }
        });
        try
        {
          childrenCache.start();
        } catch (Exception e) {
          if (logger.isWarnEnabled()) {
            logger.warn("Subscribe {} failed, {}.", directory, StackTraceUtil.stackTrace(e));
          }
        }
      } else {
        try {
          newChildrenCache.close();
        } catch (IOException e) {
          if (logger.isWarnEnabled()) {
            logger.warn("Close [PathChildrenCache] {} failed, {}.", directory, StackTraceUtil.stackTrace(e));
          }
        }
      }
    }
  }
  
  protected void doRegister(final RegisterMeta meta)
  {
    String directory = String.format("/jupiter/provider/%s/%s/%s", new Object[] { meta.getGroup(), meta.getServiceProviderName(), meta.getVersion() });
    


    try
    {
      if (configClient.checkExists().forPath(directory) == null) {
        configClient.create().creatingParentsIfNeeded().forPath(directory);
      }
    } catch (Exception e) {
      if (logger.isWarnEnabled()) {
        logger.warn("Create parent path failed, directory: {}, {}.", directory, StackTraceUtil.stackTrace(e));
      }
    }
    try
    {
      meta.setHost(address);
      

      ((PathAndBytesable)((ACLBackgroundPathAndBytesable)configClient.create().withMode(CreateMode.EPHEMERAL)).inBackground(new BackgroundCallback()
      {
        public void processResult(CuratorFramework client, CuratorEvent event) throws Exception
        {
          if (event.getResultCode() == KeeperException.Code.OK.intValue()) {
            getRegisterMetaMap().put(meta, RegistryService.RegisterState.DONE);
          }
          
          ZookeeperRegistryService.logger.info("Register: {} - {}.", meta, event); } })).forPath(String.format("%s/%s:%s:%s:%s", new Object[] { directory, meta.getHost(), String.valueOf(meta.getPort()), String.valueOf(meta.getWeight()), String.valueOf(meta.getConnCount()) }));



    }
    catch (Exception e)
    {



      if (logger.isWarnEnabled()) {
        logger.warn("Create register meta: {} path failed, {}.", meta, StackTraceUtil.stackTrace(e));
      }
    }
  }
  

  protected void doUnregister(final RegisterMeta meta)
  {
    String directory = String.format("/jupiter/provider/%s/%s/%s", new Object[] { meta.getGroup(), meta.getServiceProviderName(), meta.getVersion() });
    


    try
    {
      if (configClient.checkExists().forPath(directory) == null) {
        return;
      }
    } catch (Exception e) {
      if (logger.isWarnEnabled()) {
        logger.warn("Check exists with parent path failed, directory: {}, {}.", directory, StackTraceUtil.stackTrace(e));
      }
    }
    try
    {
      meta.setHost(address);
      
      ((Pathable)configClient.delete().inBackground(new BackgroundCallback()
      {

        public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {
          ZookeeperRegistryService.logger.info("Unregister: {} - {}.", meta, event); } })).forPath(String.format("%s/%s:%s:%s:%s", new Object[] { directory, meta.getHost(), String.valueOf(meta.getPort()), String.valueOf(meta.getWeight()), String.valueOf(meta.getConnCount()) }));



    }
    catch (Exception e)
    {



      if (logger.isWarnEnabled()) {
        logger.warn("Delete register meta: {} path failed, {}.", meta, StackTraceUtil.stackTrace(e));
      }
    }
  }
  
  protected void doCheckRegisterNodeStatus()
  {
    for (Map.Entry<RegisterMeta, RegistryService.RegisterState> entry : getRegisterMetaMap().entrySet()) {
      if (entry.getValue() != RegistryService.RegisterState.DONE)
      {


        RegisterMeta meta = (RegisterMeta)entry.getKey();
        String directory = String.format("/jupiter/provider/%s/%s/%s", new Object[] { meta.getGroup(), meta.getServiceProviderName(), meta.getVersion() });
        



        String nodePath = String.format("%s/%s:%s:%s:%s", new Object[] { directory, meta.getHost(), String.valueOf(meta.getPort()), String.valueOf(meta.getWeight()), String.valueOf(meta.getConnCount()) });
        




        try
        {
          if (configClient.checkExists().forPath(nodePath) == null) {
            super.register(meta);
          }
        } catch (Exception e) {
          if (logger.isWarnEnabled()) {
            logger.warn("Check register status, meta: {} path failed, {}.", meta, StackTraceUtil.stackTrace(e));
          }
        }
      }
    }
  }
  
  public void connectToRegistryServer(String connectString) {
    Preconditions.checkNotNull(connectString, "connectString");
    
    configClient = CuratorFrameworkFactory.newClient(connectString, sessionTimeoutMs, connectionTimeoutMs, new ExponentialBackoffRetry(500, 20));
    

    configClient.getConnectionStateListenable().addListener(new ConnectionStateListener()
    {

      public void stateChanged(CuratorFramework client, ConnectionState newState)
      {
        ZookeeperRegistryService.logger.info("Zookeeper connection state changed {}.", newState);
        
        if (newState == ConnectionState.RECONNECTED)
        {
          ZookeeperRegistryService.logger.info("Zookeeper connection has been re-established, will re-subscribe and re-register.");
          

          for (RegisterMeta.ServiceMeta serviceMeta : getSubscribeSet()) {
            doSubscribe(serviceMeta);
          }
          

          for (RegisterMeta meta : getRegisterMetaMap().keySet()) {
            ZookeeperRegistryService.this.register(meta);
          }
          
        }
      }
    });
    configClient.start();
  }
  
  public void destroy()
  {
    for (PathChildrenCache childrenCache : pathChildrenCaches.values()) {
      try {
        childrenCache.close();
      }
      catch (IOException localIOException) {}
    }
    configClient.close();
  }
  
  public List<RegisterMeta.ServiceMeta> findServiceMetaByAddress(RegisterMeta.Address address) {
    Lists.transform(Lists.newArrayList(getServiceMeta(address)), new Function()
    {

      public RegisterMeta.ServiceMeta apply(RegisterMeta.ServiceMeta input)
      {

        RegisterMeta.ServiceMeta copy = new RegisterMeta.ServiceMeta();
        copy.setGroup(input.getGroup());
        copy.setServiceProviderName(input.getServiceProviderName());
        copy.setVersion(input.getVersion());
        return copy;
      }
    });
  }
  
  private RegisterMeta parseRegisterMeta(String data) {
    String[] array_0 = Strings.split(data, '/');
    RegisterMeta meta = new RegisterMeta();
    meta.setGroup(array_0[2]);
    meta.setServiceProviderName(array_0[3]);
    meta.setVersion(array_0[4]);
    
    String[] array_1 = Strings.split(array_0[5], ':');
    meta.setHost(array_1[0]);
    meta.setPort(Integer.parseInt(array_1[1]));
    meta.setWeight(Integer.parseInt(array_1[2]));
    meta.setConnCount(Integer.parseInt(array_1[3]));
    
    return meta;
  }
  
  private ConcurrentSet<RegisterMeta.ServiceMeta> getServiceMeta(RegisterMeta.Address address) {
    ConcurrentSet<RegisterMeta.ServiceMeta> serviceMetaSet = (ConcurrentSet)serviceMetaMap.get(address);
    if (serviceMetaSet == null) {
      ConcurrentSet<RegisterMeta.ServiceMeta> newServiceMetaSet = new ConcurrentSet();
      serviceMetaSet = (ConcurrentSet)serviceMetaMap.putIfAbsent(address, newServiceMetaSet);
      if (serviceMetaSet == null) {
        serviceMetaSet = newServiceMetaSet;
      }
    }
    return serviceMetaSet;
  }
}
