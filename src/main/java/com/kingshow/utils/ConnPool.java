package com.kingshow.utils;

import org.nutz.ssdb4j.SSDBs;
import org.nutz.ssdb4j.spi.SSDB;












public class ConnPool
{
  private SSDB ssdb;
  
  public ConnPool()
  {
    ssdb = SSDBs.pool("127.0.0.1", 9099, 2000, null);
  }
  

  private static class SingletonHolder
  {
    static ConnPool instance = new ConnPool();
    
    private SingletonHolder() {}
  }
  
  public static ConnPool getInstance() { return SingletonHolder.instance; }
  
  public SSDB getSSDB()
    throws Exception
  {
    return ssdb;
  }
}
