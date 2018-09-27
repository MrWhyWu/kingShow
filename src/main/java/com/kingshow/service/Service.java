package com.kingshow.service;

import com.alibaba.fastjson.JSONObject;
import org.jupiter.rpc.ServiceProvider;

@ServiceProvider(group="auctions")
public abstract interface Service
{
  public abstract JSONObject action(JSONObject paramJSONObject);
}
