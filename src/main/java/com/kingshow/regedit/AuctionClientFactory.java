package com.kingshow.regedit;

import org.jupiter.rpc.DefaultClient;
import org.jupiter.rpc.JClient;
import org.jupiter.transport.JConnector;
import org.jupiter.transport.UnresolvedAddress;
import org.jupiter.transport.netty.JNettyTcpConnector;




















public class AuctionClientFactory
{
  private static UnresolvedAddress[] addresses = null;
  

  private static JClient client;
  

  public AuctionClientFactory() {}
  

  public static synchronized void regeditClient()
  {
    String ip = "127.0.0.1";
    






    addresses = new UnresolvedAddress[] { new UnresolvedAddress(ip, 29000) };
    
    for (UnresolvedAddress address : addresses) {
      client = new DefaultClient().withConnector(new JNettyTcpConnector());
      client.connector().connect(address);
    }
  }
  
  public static UnresolvedAddress[] getAddresses() { return addresses; }
  

  public static JClient getClient()
  {
    return client;
  }
}
