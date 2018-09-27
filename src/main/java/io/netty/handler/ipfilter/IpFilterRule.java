package io.netty.handler.ipfilter;

import java.net.InetSocketAddress;

public abstract interface IpFilterRule
{
  public abstract boolean matches(InetSocketAddress paramInetSocketAddress);
  
  public abstract IpFilterRuleType ruleType();
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ipfilter.IpFilterRule
 * JD-Core Version:    0.7.0.1
 */