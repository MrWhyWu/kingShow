package org.jupiter.rpc.consumer.future;

import org.jupiter.common.util.Preconditions;
import org.jupiter.rpc.JListener;





















public abstract class AbstractListenableFuture<V>
  extends AbstractFuture<V>
  implements ListenableFuture<V>
{
  private Object listeners;
  
  public AbstractListenableFuture() {}
  
  protected void done(int state, Object x)
  {
    notifyListeners(state, x);
  }
  
  public ListenableFuture<V> addListener(JListener<V> listener)
  {
    Preconditions.checkNotNull(listener, "listener");
    
    synchronized (this) {
      addListener0(listener);
    }
    
    if (isDone()) {
      notifyListeners(state(), outcome());
    }
    
    return this;
  }
  
  public ListenableFuture<V> addListeners(JListener<V>... listeners)
  {
    Preconditions.checkNotNull(listeners, "listeners");
    
    synchronized (this) {
      for (JListener<V> listener : listeners) {
        if (listener != null)
        {

          addListener0(listener);
        }
      }
    }
    if (isDone()) {
      notifyListeners(state(), outcome());
    }
    
    return this;
  }
  
  public ListenableFuture<V> removeListener(JListener<V> listener)
  {
    Preconditions.checkNotNull(listener, "listener");
    
    synchronized (this) {
      removeListener0(listener);
    }
    
    return this;
  }
  
  public ListenableFuture<V> removeListeners(JListener<V>... listeners)
  {
    Preconditions.checkNotNull(listeners, "listeners");
    
    synchronized (this) {
      for (JListener<V> listener : listeners) {
        if (listener != null)
        {

          removeListener0(listener);
        }
      }
    }
    return this;
  }
  
  protected void notifyListeners(int state, Object x)
  {
    synchronized (this)
    {
      if (this.listeners == null) {
        return;
      }
      
      Object listeners = this.listeners;
      this.listeners = null;
    }
    Object listeners;
    if ((listeners instanceof DefaultListeners)) {
      JListener<V>[] array = ((DefaultListeners)listeners).listeners();
      int size = ((DefaultListeners)listeners).size();
      
      for (int i = 0; i < size; i++) {
        notifyListener0(array[i], state, x);
      }
    } else {
      notifyListener0((JListener)listeners, state, x);
    }
  }
  
  protected abstract void notifyListener0(JListener<V> paramJListener, int paramInt, Object paramObject);
  
  private void addListener0(JListener<V> listener) {
    if (listeners == null) {
      listeners = listener;
    } else if ((listeners instanceof DefaultListeners)) {
      ((DefaultListeners)listeners).add(listener);
    } else {
      listeners = DefaultListeners.with((JListener)listeners, listener);
    }
  }
  
  private void removeListener0(JListener<V> listener) {
    if ((listeners instanceof DefaultListeners)) {
      ((DefaultListeners)listeners).remove(listener);
    } else if (listeners == listener) {
      listeners = null;
    }
  }
}
