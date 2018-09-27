package org.jupiter.rpc.flow.control;

public abstract interface FlowControllerHolder<T>
{
  public abstract FlowController<T> get();
}
