package org.jupiter.registry;













public abstract interface NotifyListener
{
  public abstract void notify(RegisterMeta paramRegisterMeta, NotifyEvent paramNotifyEvent);
  











  public static enum NotifyEvent
  {
    CHILD_ADDED, 
    CHILD_REMOVED;
    
    private NotifyEvent() {}
  }
}
