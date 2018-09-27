package org.jupiter.monitor.handler;







public abstract class ChildCommandHandler<T extends CommandHandler>
  implements CommandHandler
{
  private volatile T parent;
  






  public ChildCommandHandler() {}
  






  public T getParent()
  {
    return parent;
  }
  
  public void setParent(T parent) {
    this.parent = parent;
  }
}
