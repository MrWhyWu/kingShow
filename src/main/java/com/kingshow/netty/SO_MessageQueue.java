package com.kingshow.netty;

import com.alibaba.fastjson.JSONObject;
import java.util.concurrent.LinkedBlockingQueue;






public class SO_MessageQueue
{
  private LinkedBlockingQueue<JSONObject> requestQueue = new LinkedBlockingQueue();
  
  private static SO_MessageQueue instance = new SO_MessageQueue();
  
  public SO_MessageQueue() {}
  
  public static SO_MessageQueue getInstance() { return instance; }
  
  public void addRequest(JSONObject arg)
  {
    requestQueue.add(arg);
  }
  
  public LinkedBlockingQueue<JSONObject> getRequestQueue() {
    return requestQueue;
  }
}
