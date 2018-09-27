package com.kingshow.netty;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.kingshow.auction.service.AuctionService;
import com.kingshow.auction.service.AuctionsService;
import com.kingshow.regedit.AuctionClientFactory;
import com.kingshow.utils.RSA;
import com.kingshow.utils.UserCache;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.jupiter.rpc.DispatchType;
import org.jupiter.rpc.InvokeType;
import org.jupiter.rpc.consumer.ProxyFactory;
import org.jupiter.serialization.SerializerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;







public class SO_ReceiveHandlerDispatcher
  extends Thread
{
  private static final Logger log = LoggerFactory.getLogger(HandlerDispatcher.class);
  private static final long SLEEP = 1L;
  private ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue(1024);
  private Executor messageExecutor = new ThreadPoolExecutor(5, 50, Long.MAX_VALUE, TimeUnit.MILLISECONDS, queue);
  private final String enterRoom = "20001";
  
  public SO_ReceiveHandlerDispatcher() {}
  
  public void run()
  {
    try
    {
      for (;;)
      {
        if (SO_ReceiveMessageQueue.getInstance().getRequestQueue().isEmpty()) {
          Thread.sleep(1L);
        } else {
          MessageWorker messageWorker = new MessageWorker(
            (GameRequest)SO_ReceiveMessageQueue.getInstance().getRequestQueue().poll(), null);
          messageExecutor.execute(messageWorker);
          messageWorker = null;
        }
      }
    } catch (Exception e) {
      log.error("HandlerDispatcher error ", e);
    }
  }
  
  private final class MessageWorker implements Runnable
  {
    private GameRequest gameRequest;
    
    private MessageWorker(GameRequest requestArg) {
      gameRequest = requestArg;
    }
    
    public void run()
    {
      ChannelId channelId = null;
      String reciveData = null;
      try
      {
        channelId = gameRequest.getChannelHandlerContext().channel().id();
        
        reciveData = gameRequest.getData();
        
        if (reciveData != null)
        {
          JSONObject dao = JSON.parseObject(reciveData);
          


          String data = dao.getString("data");
          




          data = UserCache.getInstance().getRSA().decryptByPrivateKey(data);
          if (data == null) {
            return;
          }
          dao = JSON.parseObject(data);
          if (dao == null) {
            return;
          }
          String key = dao.getString("method");
          if (key.equals("ws"))
          {
            String uuid = dao.getString("token");
            UserCache.getInstance().getChannelIdMap().put(uuid, gameRequest.getChannelHandlerContext().channel());
            return;
          }
          

          dao = JSON.parseObject(data);
          

          AuctionsService service = 
          




            (AuctionsService)ProxyFactory.factory(AuctionService.class).version("1.0.0.daily").client(AuctionClientFactory.getClient()).dispatchType(DispatchType.BROADCAST).invokeType(InvokeType.ASYNC).serializerType(SerializerType.KRYO).addProviderAddress(AuctionClientFactory.getAddresses()).newProxyInstance();
          if (service != null) {
            service.action(dao);
          }
          service = null;
          key = null;
          dao = null;
        }
        channelId = null;
        reciveData = null;
      } catch (Exception e) {
        e.printStackTrace();
        SO_ReceiveHandlerDispatcher.log.error("error ", e);
        if ((gameRequest.getChannelHandlerContext() != null) && 
          (gameRequest.getChannelHandlerContext().channel() != null)) {
          String remoteAdd = gameRequest.getChannelHandlerContext().channel().remoteAddress().toString();
          if (UserCache.getInstance().getRemoteMap().containsKey(remoteAdd)) {
            int num = ((Integer)UserCache.getInstance().getRemoteMap().get(remoteAdd)).intValue();
            num++;
            if (num >= 100) {
              gameRequest.getChannelHandlerContext().close();
            } else {
              UserCache.getInstance().getRemoteMap().put(remoteAdd, Integer.valueOf(num));
            }
          } else {
            UserCache.getInstance().getRemoteMap().put(remoteAdd, Integer.valueOf(1));
          }
          remoteAdd = null;
        }
        
      }
      finally
      {
        gameRequest = null;
        channelId = null;
        reciveData = null;
      }
    }
  }
}
