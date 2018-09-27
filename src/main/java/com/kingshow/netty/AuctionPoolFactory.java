package com.kingshow.netty;

import com.kingshow.auction.service.AuctionsService;
import com.kingshow.regedit.AuctionClientFactory;
import com.kingshow.utils.CacheTools;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.jupiter.rpc.DispatchType;
import org.jupiter.rpc.InvokeType;
import org.jupiter.rpc.consumer.ProxyFactory;
import org.jupiter.serialization.SerializerType;

public class AuctionPoolFactory
{
  private static GenericKeyedObjectPool<String, AuctionsService> pool;
  private static final GenericKeyedObjectPoolConfig config = new GenericKeyedObjectPoolConfig();
  static { config.setMaxTotalPerKey(100);
    config.setMaxIdlePerKey(10);
    config.setMaxWaitMillis(3000L);
    

    config.setJmxEnabled(true);
    config.setJmxNamePrefix("myPoolProtocol");
    
    config.setTestOnBorrow(false);
    config.setTestOnReturn(true);
    config.setTestWhileIdle(true);
  }
  


  public AuctionPoolFactory() {}
  

  public static AuctionsService getBean(String key)
    throws Exception
  {
    if (pool == null) {
      init();
    }
    return (AuctionsService)pool.borrowObject(key);
  }
  

  private static final int TOTAL_PERKEY = 100;
  
  private static final int IDLE_PERKEY = 10;
  
  public static void returnBean(String key, AuctionsService bean)
  {
    if (pool == null) {
      init();
    }
    pool.returnObject(key, bean);
  }
  


  public static synchronized void close()
  {
    if ((pool != null) && (!pool.isClosed())) {
      pool.close();
      pool = null;
    }
  }
  


  private static synchronized void init()
  {
    if (pool != null)
      return;
    pool = new GenericKeyedObjectPool(new MyServicePooledFactory(), config);
  }
  



  static class MyServicePooledFactory
    extends BaseKeyedPooledObjectFactory<String, AuctionsService>
  {
    MyServicePooledFactory() {}
    



    public AuctionsService create(String key)
      throws Exception
    {
      AuctionsService service = 
      




        (AuctionsService)ProxyFactory.factory((Class)CacheTools.getInstance().getServiceMap().get(key)).version("1.0.0.daily").client(AuctionClientFactory.getClient()).dispatchType(DispatchType.BROADCAST).invokeType(InvokeType.ASYNC).serializerType(SerializerType.KRYO).addProviderAddress(AuctionClientFactory.getAddresses()).newProxyInstance();
      return service;
    }
    
    public PooledObject<AuctionsService> wrap(AuctionsService value) {
      return new DefaultPooledObject(value);
    }
    






    public boolean validateObject(String key, PooledObject<AuctionsService> p)
    {
      AuctionsService bean = (AuctionsService)p.getObject();
      if (bean == null) {
        return false;
      }
      return true;
    }
    



    public void destroyObject(String key, PooledObject<AuctionsService> p)
      throws Exception
    {}
    


    public void activateObject(String key, PooledObject<AuctionsService> p)
      throws Exception
    {
      super.activateObject(key, p);
    }
    
    public void passivateObject(String key, PooledObject<AuctionsService> p) throws Exception {
      super.passivateObject(key, p);
    }
  }
}
