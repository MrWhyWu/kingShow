package com.kingshow.netty;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.kingshow.auction.service.AuctionHeaderService;
import com.kingshow.auction.service.AuctionUpdateMoneyService;
import com.kingshow.auction.service.AuctionsService;
import com.kingshow.auction.service.MoneyCacheService;
import com.kingshow.regedit.AuctionClientFactory;
import com.kingshow.service.Service;
import com.kingshow.utils.HttpTools;
import com.kingshow.utils.RSA;
import com.kingshow.utils.UserCache;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import java.io.PrintStream;
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









public class HandlerDispatcher
  extends Thread
{
  private static final Logger log = LoggerFactory.getLogger(HandlerDispatcher.class);
  private static final long SLEEP = 1L;
  private ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue(1024);
  private Executor messageExecutor = new ThreadPoolExecutor(5, 100, Long.MAX_VALUE, TimeUnit.MILLISECONDS, queue);
  

  public HandlerDispatcher() {}
  

  public void run()
  {
    try
    {
      for (;;)
      {
        if (MessageQueue.getInstance().getRequestQueue().isEmpty()) {
          Thread.sleep(1L);
        } else {
          MessageWorker messageWorker = new MessageWorker(
            (GameRequest)MessageQueue.getInstance().getRequestQueue().poll(), null);
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
    
    private MessageWorker(GameRequest requestArg)
    {
      gameRequest = requestArg;
    }
    
    public void run()
    {
      ChannelId channelId = null;
      try {
        channelId = gameRequest.getChannelHandlerContext().channel().id();
        
        String gameData = gameRequest.getData();
        if ((gameData == null) || (gameData.equals("heart"))) {
          return;
        }
        
        JSONObject reciveJSON = JSON.parseObject(gameData);
        gameData = null;
        
        if (reciveJSON != null)
        {






          if (reciveJSON.containsKey("data")) {
            String data = reciveJSON.getString("data");
            data = UserCache.getInstance().getRSA().decryptByPrivateKey(data);
            if (data == null) {
              reciveJSON = null;
              return;
            }
            
            reciveJSON = null;
            reciveJSON = JSON.parseObject(data);
          }
          


          String key = reciveJSON.getString("method");
          
          JSONObject result = null;
          
          if ("updateMoney".equals(key)) {
            AuctionsService aservice = 
            




              (AuctionsService)ProxyFactory.factory(AuctionUpdateMoneyService.class).version("1.0.0.daily").client(AuctionClientFactory.getClient()).dispatchType(DispatchType.BROADCAST).invokeType(InvokeType.ASYNC).serializerType(SerializerType.KRYO).addProviderAddress(AuctionClientFactory.getAddresses()).newProxyInstance();
            if (aservice != null) {
              aservice.action(reciveJSON);
            }
            result = new JSONObject();
            result.put("msg", "ok");
            HttpTools.sendCorrectResp(gameRequest.getChannelHandlerContext(), 
              gameRequest.getRequest(), result);
            return; }
          if ("updateHeader".equals(key)) {
            AuctionsService aservice = 
            




              (AuctionsService)ProxyFactory.factory(AuctionHeaderService.class).version("1.0.0.daily").client(AuctionClientFactory.getClient()).dispatchType(DispatchType.BROADCAST).invokeType(InvokeType.ASYNC).serializerType(SerializerType.KRYO).addProviderAddress(AuctionClientFactory.getAddresses()).newProxyInstance();
            if (aservice != null) {
              aservice.action(reciveJSON);
            }
            result = new JSONObject();
            result.put("msg", "ok");
            HttpTools.sendCorrectResp(gameRequest.getChannelHandlerContext(), 
              gameRequest.getRequest(), result);
            return;
          }
          Service service = ServicePoolFactory.getBean(key);
          if (service != null) {
            result = service.action(reciveJSON);
            ServicePoolFactory.returnBean(key, service);
          }
          

          if (result == null) {
            return;
          }
          if (key.equals("auctionList")) {
            System.out.println("reciveJSON==" + reciveJSON);
            AuctionsService aservice = 
            




              (AuctionsService)ProxyFactory.factory(MoneyCacheService.class).version("1.0.0.daily").client(AuctionClientFactory.getClient()).dispatchType(DispatchType.BROADCAST).invokeType(InvokeType.ASYNC).serializerType(SerializerType.KRYO).addProviderAddress(AuctionClientFactory.getAddresses()).newProxyInstance();
            if (aservice != null) {
              aservice.action(reciveJSON);
            }
            System.out.println("aservice==" + aservice);
            aservice = null;
          }
          


          HttpTools.sendCorrectResp(gameRequest.getChannelHandlerContext(), 
            gameRequest.getRequest(), result);
          key = null;
        }
        


        channelId = null;
      }
      catch (Exception e) {
        e.printStackTrace();
        HandlerDispatcher.log.error("error ", e);
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
      }
    }
  }
}
