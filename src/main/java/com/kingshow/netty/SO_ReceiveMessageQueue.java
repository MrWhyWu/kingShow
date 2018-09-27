package com.kingshow.netty;

import java.util.concurrent.LinkedBlockingQueue;

public class SO_ReceiveMessageQueue {
  private LinkedBlockingQueue<GameRequest> requestQueue = new LinkedBlockingQueue(8192);
  
  private static SO_ReceiveMessageQueue instance = new SO_ReceiveMessageQueue();
  
  public SO_ReceiveMessageQueue() {}
  
  public static SO_ReceiveMessageQueue getInstance() { return instance; }
  
  public void addRequest(GameRequest request)
  {
    requestQueue.add(request);
  }
  
  public LinkedBlockingQueue<GameRequest> getRequestQueue() {
    return requestQueue;
  }
}
