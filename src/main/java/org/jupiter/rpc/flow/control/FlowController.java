package org.jupiter.rpc.flow.control;

public abstract interface FlowController<T>
{
  public abstract ControlResult flowControl(T paramT);
}
