package org.jupiter.rpc.consumer.future;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import org.jupiter.common.util.JConstants;
import org.jupiter.common.util.Maps;
import org.jupiter.common.util.Signal;
import org.jupiter.common.util.StackTraceUtil;
import org.jupiter.common.util.SystemPropertyUtil;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;
import org.jupiter.rpc.DispatchType;
import org.jupiter.rpc.JListener;
import org.jupiter.rpc.JResponse;
import org.jupiter.rpc.consumer.ConsumerInterceptor;
import org.jupiter.rpc.exception.JupiterBizException;
import org.jupiter.rpc.exception.JupiterRemoteException;
import org.jupiter.rpc.exception.JupiterSerializationException;
import org.jupiter.rpc.exception.JupiterTimeoutException;
import org.jupiter.rpc.model.metadata.ResultWrapper;
import org.jupiter.rpc.tracing.TraceId;
import org.jupiter.transport.Status;
import org.jupiter.transport.channel.JChannel;





















public class DefaultInvokeFuture<V>
  extends AbstractListenableFuture<V>
  implements InvokeFuture<V>
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultInvokeFuture.class);
  
  private static final long DEFAULT_TIMEOUT_NANOSECONDS = TimeUnit.MILLISECONDS.toNanos(JConstants.DEFAULT_TIMEOUT);
  
  private static final ConcurrentMap<Long, DefaultInvokeFuture<?>> roundFutures = Maps.newConcurrentMapLong();
  private static final ConcurrentMap<String, DefaultInvokeFuture<?>> broadcastFutures = Maps.newConcurrentMap();
  
  private final long invokeId;
  private final JChannel channel;
  private final Class<V> returnType;
  private final long timeout;
  private final long startTime = System.nanoTime();
  
  private volatile boolean sent = false;
  
  private ConsumerInterceptor[] interceptors;
  
  private TraceId traceId;
  
  public static <T> DefaultInvokeFuture<T> with(long invokeId, JChannel channel, long timeoutMillis, Class<T> returnType, DispatchType dispatchType)
  {
    return new DefaultInvokeFuture(invokeId, channel, timeoutMillis, returnType, dispatchType);
  }
  

  private DefaultInvokeFuture(long invokeId, JChannel channel, long timeoutMillis, Class<V> returnType, DispatchType dispatchType)
  {
    this.invokeId = invokeId;
    this.channel = channel;
    timeout = (timeoutMillis > 0L ? TimeUnit.MILLISECONDS.toNanos(timeoutMillis) : DEFAULT_TIMEOUT_NANOSECONDS);
    this.returnType = returnType;
    
    switch (1.$SwitchMap$org$jupiter$rpc$DispatchType[dispatchType.ordinal()]) {
    case 1: 
      roundFutures.put(Long.valueOf(invokeId), this);
      break;
    case 2: 
      broadcastFutures.put(subInvokeId(channel, invokeId), this);
      break;
    default: 
      throw new IllegalArgumentException("Unsupported " + dispatchType);
    }
  }
  
  public JChannel channel() {
    return channel;
  }
  
  public Class<V> returnType()
  {
    return returnType;
  }
  
  public V getResult() throws Throwable
  {
    try {
      return get(timeout, TimeUnit.NANOSECONDS);
    } catch (Signal s) {
      SocketAddress address = channel.remoteAddress();
      if (s == TIMEOUT) {
        throw new JupiterTimeoutException(address, sent ? Status.SERVER_TIMEOUT : Status.CLIENT_TIMEOUT);
      }
      throw new JupiterRemoteException(s.name(), address);
    }
  }
  

  protected void notifyListener0(JListener<V> listener, int state, Object x)
  {
    try
    {
      if (state == 2) {
        listener.complete(x);
      } else {
        listener.failure((Throwable)x);
      }
    } catch (Throwable t) {
      logger.error("An exception was thrown by {}.{}, {}.", new Object[] { listener.getClass().getName(), state == 2 ? "complete()" : "failure()", StackTraceUtil.stackTrace(t) });
    }
  }
  
  public void markSent()
  {
    sent = true;
  }
  
  public ConsumerInterceptor[] interceptors() {
    return interceptors;
  }
  
  public DefaultInvokeFuture<V> interceptors(ConsumerInterceptor[] interceptors) {
    this.interceptors = interceptors;
    return this;
  }
  
  public TraceId traceId() {
    return traceId;
  }
  
  public DefaultInvokeFuture<V> traceId(TraceId traceId) {
    this.traceId = traceId;
    return this;
  }
  
  private void doReceived(JResponse response)
  {
    byte status = response.status();
    
    if (status == Status.OK.value()) {
      ResultWrapper wrapper = response.result();
      set(wrapper.getResult());
    } else {
      setException(status, response);
    }
    
    ConsumerInterceptor[] interceptors = this.interceptors;
    if (interceptors != null) {
      for (int i = interceptors.length - 1; i >= 0; i--)
        interceptors[i].afterInvoke(traceId, response, channel);
    }
  }
  
  private void setException(byte status, JResponse response) {
    Throwable cause;
    Throwable cause;
    if (status == Status.SERVER_TIMEOUT.value()) {
      cause = new JupiterTimeoutException(channel.remoteAddress(), Status.SERVER_TIMEOUT); } else { Throwable cause;
      if (status == Status.CLIENT_TIMEOUT.value()) {
        cause = new JupiterTimeoutException(channel.remoteAddress(), Status.CLIENT_TIMEOUT); } else { Throwable cause;
        if (status == Status.DESERIALIZATION_FAIL.value()) {
          ResultWrapper wrapper = response.result();
          cause = (JupiterSerializationException)wrapper.getResult(); } else { Throwable cause;
          if (status == Status.SERVICE_EXPECTED_ERROR.value()) {
            ResultWrapper wrapper = response.result();
            cause = (Throwable)wrapper.getResult(); } else { Throwable cause;
            if (status == Status.SERVICE_UNEXPECTED_ERROR.value()) {
              ResultWrapper wrapper = response.result();
              String message = String.valueOf(wrapper.getResult());
              cause = new JupiterBizException(message, channel.remoteAddress());
            } else {
              ResultWrapper wrapper = response.result();
              Object result = wrapper.getResult();
              Throwable cause; if ((result != null) && ((result instanceof JupiterRemoteException))) {
                cause = (JupiterRemoteException)result;
              } else
                cause = new JupiterRemoteException(response.toString(), channel.remoteAddress());
            }
          } } } }
    setException(cause);
  }
  
  public static void received(JChannel channel, JResponse response) {
    long invokeId = response.id();
    
    DefaultInvokeFuture<?> future = (DefaultInvokeFuture)roundFutures.remove(Long.valueOf(invokeId));
    
    if (future == null)
    {
      future = (DefaultInvokeFuture)broadcastFutures.remove(subInvokeId(channel, invokeId));
    }
    
    if (future == null) {
      logger.warn("A timeout response [{}] finally returned on {}.", response, channel);
      return;
    }
    
    future.doReceived(response);
  }
  
  public static void fakeReceived(JChannel channel, JResponse response, DispatchType dispatchType) {
    long invokeId = response.id();
    
    DefaultInvokeFuture<?> future = null;
    
    if (dispatchType == DispatchType.ROUND) {
      future = (DefaultInvokeFuture)roundFutures.remove(Long.valueOf(invokeId));
    } else if (dispatchType == DispatchType.BROADCAST) {
      future = (DefaultInvokeFuture)broadcastFutures.remove(subInvokeId(channel, invokeId));
    }
    
    if (future == null) {
      return;
    }
    
    future.doReceived(response);
  }
  
  private static String subInvokeId(JChannel channel, long invokeId) {
    return channel.id() + invokeId;
  }
  

  private static class TimeoutScanner
    implements Runnable
  {
    private static final long TIMEOUT_SCANNER_INTERVAL_MILLIS = SystemPropertyUtil.getLong("jupiter.rpc.invoke.timeout_scanner_interval_millis", 100L);
    
    private TimeoutScanner() {}
    
    public void run() {
      for (;;) {
        try {
          Iterator i$ = DefaultInvokeFuture.roundFutures.values().iterator(); if (i$.hasNext()) { DefaultInvokeFuture<?> future = (DefaultInvokeFuture)i$.next();
            process(future, DispatchType.ROUND);
            continue;
          }
          
          Iterator i$ = DefaultInvokeFuture.broadcastFutures.values().iterator(); if (i$.hasNext()) { DefaultInvokeFuture<?> future = (DefaultInvokeFuture)i$.next();
            process(future, DispatchType.BROADCAST);
            continue;
          }
        } catch (Throwable t) { DefaultInvokeFuture.logger.error("An exception was caught while scanning the timeout futures {}.", StackTraceUtil.stackTrace(t));
        }
        try
        {
          Thread.sleep(TIMEOUT_SCANNER_INTERVAL_MILLIS);
        } catch (InterruptedException localInterruptedException) {}
      }
    }
    
    private void process(DefaultInvokeFuture<?> future, DispatchType dispatchType) {
      if ((future == null) || (future.isDone())) {
        return;
      }
      
      if (System.nanoTime() - startTime > timeout) {
        JResponse response = new JResponse(invokeId);
        response.status(sent ? Status.SERVER_TIMEOUT : Status.CLIENT_TIMEOUT);
        
        DefaultInvokeFuture.fakeReceived(channel, response, dispatchType);
      }
    }
  }
  
  static {
    Thread t = new Thread(new TimeoutScanner(null), "timeout.scanner");
    t.setDaemon(true);
    t.start();
  }
}
