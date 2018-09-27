package org.jupiter.rpc.consumer.invoker;

public abstract interface GenericInvoker
{
  public abstract Object $invoke(String paramString, Object... paramVarArgs)
    throws Throwable;
}
