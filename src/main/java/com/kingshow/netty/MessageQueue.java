package com.kingshow.netty;

import java.util.concurrent.LinkedBlockingQueue;

public class MessageQueue {
  private LinkedBlockingQueue<GameRequest> requestQueue = new LinkedBlockingQueue(8192);
  
  private static MessageQueue instance = new MessageQueue();
  
  public MessageQueue() {}
  
  public static MessageQueue getInstance() { return instance; }
  
  public void addRequest(GameRequest request)
  {
    requestQueue.add(request);
  }
  
  public LinkedBlockingQueue<GameRequest> getRequestQueue() {
    return requestQueue;
  }
}
