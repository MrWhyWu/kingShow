package com.kingshow.netty;

import com.alibaba.fastjson.JSONObject;
import com.kingshow.utils.UserCache;
import io.netty.channel.Channel;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SO_HandlerDispatcher
  extends Thread
{
  private static final Logger log = LoggerFactory.getLogger(HandlerDispatcher.class);
  private static final long SLEEP = 1L;
  private ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue(1024);
  private Executor messageExecutor = new ThreadPoolExecutor(5, 100, Long.MAX_VALUE, TimeUnit.MILLISECONDS, queue);
  
  public SO_HandlerDispatcher() {}
  
  public void run()
  {
    try
    {
      for (;;)
      {
        if (SO_MessageQueue.getInstance().getRequestQueue().isEmpty()) {
          Thread.sleep(1L);
        } else {
          MessageWorker messageWorker = new MessageWorker(
            (JSONObject)SO_MessageQueue.getInstance().getRequestQueue().poll(), null);
          messageExecutor.execute(messageWorker);
          messageWorker = null;
        }
      }
    } catch (Exception e) {
      log.error("SO_HandlerDispatcher error ", e);
    }
  }
  
  private final class MessageWorker implements Runnable {
    private JSONObject message;
    
    private MessageWorker(JSONObject requestArg) {
      message = requestArg;
    }
    
    public void run()
    {
      if (message == null) {
        return;
      }
      try
      {
        String sendData = message.toJSONString();
        Collection<Channel> channels = UserCache.getInstance().getChannelIdMap().values();
        for (Channel channel : channels) {
          if ((channel.isActive()) && (channel.isWritable())) {
            SO_SendDealService.sendData(channel, sendData);
          }
        }
        sendData = null;
      }
      catch (Exception e) {
        SO_HandlerDispatcher.log.error("sendData error", e);
        e.printStackTrace();
      }
    }
  }
}
