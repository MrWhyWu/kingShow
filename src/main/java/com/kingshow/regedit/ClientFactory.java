package com.kingshow.regedit;

import com.kingshow.service.AuctionListService;
import com.kingshow.service.AuctionRecordService;
import com.kingshow.service.AuctionedYetInfoService;
import com.kingshow.utils.CacheTools;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.jupiter.common.util.SystemPropertyUtil;
import org.jupiter.registry.RegistryService;
import org.jupiter.registry.RegistryService.RegistryType;
import org.jupiter.rpc.DefaultClient;
import org.jupiter.rpc.JClient;
import org.jupiter.transport.JConnector;
import org.jupiter.transport.JConnector.ConnectionWatcher;
import org.jupiter.transport.exception.ConnectFailedException;
import org.jupiter.transport.netty.JNettyTcpConnector;






public class ClientFactory
{
  private static AtomicInteger clientIndex;
  private static final int maxClient = 10;
  private static JClient[] clients;
  private static ConcurrentHashMap<Integer, CopyOnWriteArrayList<String>> clientMap;
  
  public ClientFactory() {}
  
  public static synchronized void regeditClient()
  {
    if (clients != null)
      return;
    SystemPropertyUtil.setProperty("jupiter.tracing.needed", "false");
    clients = new JClient[10];
    clientIndex = new AtomicInteger(0);
    clientMap = new ConcurrentHashMap();
    

    ArrayList<Class> clazzlist = new ArrayList();
    
    clazzlist.add(AuctionedYetInfoService.class);
    clazzlist.add(AuctionListService.class);
    clazzlist.add(AuctionRecordService.class);
    

    CacheTools.getInstance().getServiceMap().put("lastAuction", AuctionedYetInfoService.class);
    CacheTools.getInstance().getServiceMap().put("auctionList", AuctionListService.class);
    CacheTools.getInstance().getServiceMap().put("auctionRecord", AuctionRecordService.class);
    


    JConnector.ConnectionWatcher watcherLogin = null;
    for (int i = 0; 
        i < 10; i++)
    {


      JClient client = new DefaultClient(RegistryService.RegistryType.ZOOKEEPER)
        .withConnector(new JNettyTcpConnector());
      
      client.connectToRegistryServer("172.31.172.246:2181,172.31.172.246:2182,172.31.172.246:2183");
      
      clients[i] = client;
      
      for (Class clazz : clazzlist)
      {
        watcherLogin = client.watchConnections(clazz, "1.0.0.daily");
        

        if (!watcherLogin.waitForAvailable(3000L)) {
          throw new ConnectFailedException();
        }
      }
      clientMap.put(Integer.valueOf(i), new CopyOnWriteArrayList());
    }
    clazzlist = null;
  }
  

  public static JClient getClient(String key)
  {
    int index = -1;
    CopyOnWriteArrayList<String> clientEventList = null;
    Set<Integer> keys = clientMap.keySet();
    for (Integer ckey : keys) {
      clientEventList = (CopyOnWriteArrayList)clientMap.get(ckey);
      if (!clientEventList.contains(key)) {
        clientEventList.add(key);
        index = ckey.intValue();
        break;
      }
      clientEventList = null;
    }
    keys = null;
    clientEventList = null;
    if (index == -1) {
      index = new Random(System.currentTimeMillis()).nextInt(10);
    }
    
    return clients[index];
  }
}
