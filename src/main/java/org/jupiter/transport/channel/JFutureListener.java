package org.jupiter.transport.channel;

import java.util.EventListener;

public abstract interface JFutureListener<C>
  extends EventListener
{
  public abstract void operationSuccess(C paramC)
    throws Exception;
  
  public abstract void operationFailure(C paramC, Throwable paramThrowable)
    throws Exception;
}
