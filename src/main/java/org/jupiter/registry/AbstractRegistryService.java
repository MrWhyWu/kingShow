package org.jupiter.registry;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.jupiter.common.concurrent.NamedThreadFactory;
import org.jupiter.common.concurrent.collection.ConcurrentSet;
import org.jupiter.common.util.Lists;
import org.jupiter.common.util.Maps;
import org.jupiter.common.util.StackTraceUtil;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;













public abstract class AbstractRegistryService
  implements RegistryService
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractRegistryService.class);
  
  private final LinkedBlockingQueue<RegisterMeta> queue = new LinkedBlockingQueue();
  private final ExecutorService registerExecutor = Executors.newSingleThreadExecutor(new NamedThreadFactory("register.executor", true));
  
  private final ScheduledExecutorService registerScheduledExecutor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("register.schedule.executor", true));
  
  private final ExecutorService localRegisterWatchExecutor = Executors.newSingleThreadExecutor(new NamedThreadFactory("local.register.watch.executor", true));
  

  private final AtomicBoolean shutdown = new AtomicBoolean(false);
  
  private final ConcurrentMap<RegisterMeta.ServiceMeta, RegisterValue> registries = Maps.newConcurrentMap();
  

  private final ConcurrentMap<RegisterMeta.ServiceMeta, CopyOnWriteArrayList<NotifyListener>> subscribeListeners = Maps.newConcurrentMap();
  
  private final ConcurrentMap<RegisterMeta.Address, CopyOnWriteArrayList<OfflineListener>> offlineListeners = Maps.newConcurrentMap();
  


  private final ConcurrentSet<RegisterMeta.ServiceMeta> subscribeSet = new ConcurrentSet();
  
  private final ConcurrentMap<RegisterMeta, RegistryService.RegisterState> registerMetaMap = Maps.newConcurrentMap();
  
  public AbstractRegistryService() {
    registerExecutor.execute(new Runnable()
    {
      public void run()
      {
        while (!shutdown.get()) {
          RegisterMeta meta = null;
          try {
            meta = (RegisterMeta)queue.take();
            registerMetaMap.put(meta, RegistryService.RegisterState.PREPARE);
            doRegister(meta);
          } catch (InterruptedException e) {
            AbstractRegistryService.logger.warn("[register.executor] interrupted.");
          } catch (Throwable t) {
            if (meta != null) {
              AbstractRegistryService.logger.error("Register [{}] fail: {}, will try again...", meta.getServiceMeta(), StackTraceUtil.stackTrace(t));
              

              final RegisterMeta finalMeta = meta;
              registerScheduledExecutor.schedule(new Runnable()
              {

                public void run() {
                  queue.add(finalMeta); } }, 1L, TimeUnit.SECONDS);
            }
            
          }
          
        }
        
      }
    });
    localRegisterWatchExecutor.execute(new Runnable()
    {
      public void run()
      {
        while (!shutdown.get()) {
          try {
            Thread.sleep(3000L);
            doCheckRegisterNodeStatus();
          } catch (InterruptedException e) {
            AbstractRegistryService.logger.warn("[local.register.watch.executor] interrupted.");
          } catch (Throwable t) {
            if (AbstractRegistryService.logger.isWarnEnabled()) {
              AbstractRegistryService.logger.warn("Check register node status fail: {}, will try again...", StackTraceUtil.stackTrace(t));
            }
          }
        }
      }
    });
  }
  
  public void register(RegisterMeta meta)
  {
    queue.add(meta);
  }
  

  public void unregister(RegisterMeta meta)
  {
    if (!queue.remove(meta)) {
      registerMetaMap.remove(meta);
      doUnregister(meta);
    }
  }
  
  public void subscribe(RegisterMeta.ServiceMeta serviceMeta, NotifyListener listener)
  {
    CopyOnWriteArrayList<NotifyListener> listeners = (CopyOnWriteArrayList)subscribeListeners.get(serviceMeta);
    if (listeners == null) {
      CopyOnWriteArrayList<NotifyListener> newListeners = new CopyOnWriteArrayList();
      listeners = (CopyOnWriteArrayList)subscribeListeners.putIfAbsent(serviceMeta, newListeners);
      if (listeners == null) {
        listeners = newListeners;
      }
    }
    listeners.add(listener);
    
    subscribeSet.add(serviceMeta);
    doSubscribe(serviceMeta);
  }
  
  public Collection<RegisterMeta> lookup(RegisterMeta.ServiceMeta serviceMeta)
  {
    RegisterValue value = (RegisterValue)registries.get(serviceMeta);
    
    if (value == null) {
      return Collections.emptyList();
    }
    
    Lock readLock = lock.readLock();
    readLock.lock();
    try {
      return Lists.newArrayList(metaSet);
    } finally {
      readLock.unlock();
    }
  }
  
  public Map<RegisterMeta.ServiceMeta, Integer> consumers()
  {
    Map<RegisterMeta.ServiceMeta, Integer> result = Maps.newHashMap();
    for (Map.Entry<RegisterMeta.ServiceMeta, RegisterValue> entry : registries.entrySet()) {
      RegisterValue value = (RegisterValue)entry.getValue();
      Lock readLock = lock.readLock();
      readLock.lock();
      try {
        result.put(entry.getKey(), Integer.valueOf(metaSet.size()));
      } finally {
        readLock.unlock();
      }
    }
    return result;
  }
  
  public Map<RegisterMeta, RegistryService.RegisterState> providers()
  {
    return new HashMap(registerMetaMap);
  }
  
  public boolean isShutdown()
  {
    return shutdown.get();
  }
  
  public void shutdownGracefully()
  {
    if (!shutdown.getAndSet(true)) {
      try {
        registerExecutor.shutdownNow();
        registerScheduledExecutor.shutdownNow();
        localRegisterWatchExecutor.shutdownNow();
      } catch (Exception e) {
        logger.error("failed to shutdown: {}.", StackTraceUtil.stackTrace(e));
      } finally {
        destroy();
      }
    }
  }
  
  public abstract void destroy();
  
  public void offlineListening(RegisterMeta.Address address, OfflineListener listener) {
    CopyOnWriteArrayList<OfflineListener> listeners = (CopyOnWriteArrayList)offlineListeners.get(address);
    if (listeners == null) {
      CopyOnWriteArrayList<OfflineListener> newListeners = new CopyOnWriteArrayList();
      listeners = (CopyOnWriteArrayList)offlineListeners.putIfAbsent(address, newListeners);
      if (listeners == null) {
        listeners = newListeners;
      }
    }
    listeners.add(listener);
  }
  
  public void offline(RegisterMeta.Address address)
  {
    CopyOnWriteArrayList<OfflineListener> listeners = (CopyOnWriteArrayList)offlineListeners.remove(address);
    if (listeners != null) {
      for (OfflineListener l : listeners) {
        l.offline();
      }
    }
  }
  


  protected void notify(RegisterMeta.ServiceMeta serviceMeta, NotifyListener.NotifyEvent event, long version, RegisterMeta... array)
  {
    if ((array == null) || (array.length == 0)) {
      return;
    }
    
    RegisterValue value = (RegisterValue)registries.get(serviceMeta);
    if (value == null) {
      RegisterValue newValue = new RegisterValue();
      value = (RegisterValue)registries.putIfAbsent(serviceMeta, newValue);
      if (value == null) {
        value = newValue;
      }
    }
    
    boolean notifyNeeded = false;
    

    Lock writeLock = lock.writeLock();
    writeLock.lock();
    try {
      long lastVersion = version;
      if ((version > lastVersion) || ((version < 0L) && (lastVersion > 0L)))
      {
        if (event == NotifyListener.NotifyEvent.CHILD_REMOVED) {
          for (RegisterMeta m : array) {
            metaSet.remove(m);
          }
        } else if (event == NotifyListener.NotifyEvent.CHILD_ADDED) {
          Collections.addAll(metaSet, array);
        }
        version = version;
        notifyNeeded = true;
      }
    } finally {
      writeLock.unlock();
    }
    
    if (notifyNeeded) {
      CopyOnWriteArrayList<NotifyListener> listeners = (CopyOnWriteArrayList)subscribeListeners.get(serviceMeta);
      if (listeners != null) {
        for (NotifyListener l : listeners) {
          for (RegisterMeta m : array) {
            l.notify(m, event);
          }
        }
      }
    }
  }
  
  protected abstract void doSubscribe(RegisterMeta.ServiceMeta paramServiceMeta);
  
  protected abstract void doRegister(RegisterMeta paramRegisterMeta);
  
  protected abstract void doUnregister(RegisterMeta paramRegisterMeta);
  
  protected abstract void doCheckRegisterNodeStatus();
  
  protected ConcurrentSet<RegisterMeta.ServiceMeta> getSubscribeSet()
  {
    return subscribeSet;
  }
  
  protected ConcurrentMap<RegisterMeta, RegistryService.RegisterState> getRegisterMetaMap() {
    return registerMetaMap;
  }
  
  protected static class RegisterValue {
    private long version = Long.MIN_VALUE;
    private final Set<RegisterMeta> metaSet = new HashSet();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    protected RegisterValue() {}
  }
}
