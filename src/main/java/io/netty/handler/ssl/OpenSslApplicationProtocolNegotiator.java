package io.netty.handler.ssl;

@Deprecated
public abstract interface OpenSslApplicationProtocolNegotiator
  extends ApplicationProtocolNegotiator
{
  public abstract ApplicationProtocolConfig.Protocol protocol();
  
  public abstract ApplicationProtocolConfig.SelectorFailureBehavior selectorFailureBehavior();
  
  public abstract ApplicationProtocolConfig.SelectedListenerFailureBehavior selectedListenerFailureBehavior();
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.OpenSslApplicationProtocolNegotiator
 * JD-Core Version:    0.7.0.1
 */