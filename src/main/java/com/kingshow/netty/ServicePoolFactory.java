package com.kingshow.netty;

import com.kingshow.regedit.ClientFactory;
import com.kingshow.service.Service;
import com.kingshow.utils.CacheTools;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.jupiter.rpc.consumer.ProxyFactory;
import org.jupiter.serialization.SerializerType;



















public class ServicePoolFactory
{
  private static GenericKeyedObjectPool<String, Service> pool;
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
  


  public ServicePoolFactory() {}
  

  public static Service getBean(String key)
    throws Exception
  {
    if (pool == null) {
      init();
    }
    return (Service)pool.borrowObject(key);
  }
  

  private static final int TOTAL_PERKEY = 100;
  
  private static final int IDLE_PERKEY = 10;
  
  public static void returnBean(String key, Service bean)
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
    extends BaseKeyedPooledObjectFactory<String, Service>
  {
    MyServicePooledFactory() {}
    



    public Service create(String key)
      throws Exception
    {
      Service service = 
      
        (Service)ProxyFactory.factory((Class)CacheTools.getInstance().getServiceMap().get(key)).version("1.0.0.daily").client(ClientFactory.getClient(key)).serializerType(SerializerType.KRYO).newProxyInstance();
      
      return service;
    }
    
    public PooledObject<Service> wrap(Service value) {
      return new DefaultPooledObject(value);
    }
    






    public boolean validateObject(String key, PooledObject<Service> p)
    {
      Service bean = (Service)p.getObject();
      if (bean == null) {
        return false;
      }
      return true;
    }
    



    public void destroyObject(String key, PooledObject<Service> p)
      throws Exception
    {}
    


    public void activateObject(String key, PooledObject<Service> p)
      throws Exception
    {
      super.activateObject(key, p);
    }
    
    public void passivateObject(String key, PooledObject<Service> p) throws Exception {
      super.passivateObject(key, p);
    }
  }
}
