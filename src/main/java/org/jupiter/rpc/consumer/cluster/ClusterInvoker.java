package org.jupiter.rpc.consumer.cluster;

import org.jupiter.rpc.JRequest;
import org.jupiter.rpc.consumer.future.InvokeFuture;




















public abstract interface ClusterInvoker
{
  public abstract Strategy strategy();
  
  public abstract <T> InvokeFuture<T> invoke(JRequest paramJRequest, Class<T> paramClass)
    throws Exception;
  
  public static enum Strategy
  {
    FAIL_FAST, 
    FAIL_OVER, 
    FAIL_SAFE;
    
    private Strategy() {}
    
    public static Strategy parse(String name)
    {
      for (Strategy s : ) {
        if (s.name().equalsIgnoreCase(name)) {
          return s;
        }
      }
      return null;
    }
    
    public static Strategy getDefault() {
      return FAIL_FAST;
    }
  }
}
