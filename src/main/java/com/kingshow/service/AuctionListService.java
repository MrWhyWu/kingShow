package com.kingshow.service;

import com.alibaba.fastjson.JSONObject;
import org.jupiter.rpc.ServiceProvider;

@ServiceProvider(group="bussiness")
public abstract interface AuctionListService
  extends Service
{
  public abstract JSONObject action(JSONObject paramJSONObject);
}
