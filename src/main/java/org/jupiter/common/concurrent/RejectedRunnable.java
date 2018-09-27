package org.jupiter.common.concurrent;

public abstract interface RejectedRunnable
  extends Runnable
{
  public abstract void rejected();
}
