package com.kingshow.ImplService;

import com.alibaba.fastjson.JSONObject;
import com.kingshow.auction.service.AuctionOutService;
import com.kingshow.netty.SO_MessageQueue;
import org.jupiter.rpc.ServiceProviderImpl;




@ServiceProviderImpl(version="1.0.0.daily")
public class AuctionOutServiceImpl
  implements AuctionOutService
{
  public AuctionOutServiceImpl() {}
  
  public void action(JSONObject reqObj)
  {
    if (reqObj != null) {
      SO_MessageQueue.getInstance().addRequest(reqObj);
    }
  }
}
