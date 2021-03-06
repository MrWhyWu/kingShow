package com.kingshow.auction.service;

import com.alibaba.fastjson.JSONObject;
import org.jupiter.rpc.ServiceProvider;

@ServiceProvider(group="auctions")
public abstract interface AuctionService
  extends AuctionsService
{
  public abstract void action(JSONObject paramJSONObject);
}
