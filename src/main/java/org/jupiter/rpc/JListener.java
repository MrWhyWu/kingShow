package org.jupiter.rpc;

import java.util.EventListener;

public abstract interface JListener<V>
  extends EventListener
{
  public abstract void complete(V paramV);
  
  public abstract void failure(Throwable paramThrowable);
}
