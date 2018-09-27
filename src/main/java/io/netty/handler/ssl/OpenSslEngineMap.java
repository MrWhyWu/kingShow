package io.netty.handler.ssl;

abstract interface OpenSslEngineMap
{
  public abstract ReferenceCountedOpenSslEngine remove(long paramLong);
  
  public abstract void add(ReferenceCountedOpenSslEngine paramReferenceCountedOpenSslEngine);
  
  public abstract ReferenceCountedOpenSslEngine get(long paramLong);
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.OpenSslEngineMap
 * JD-Core Version:    0.7.0.1
 */