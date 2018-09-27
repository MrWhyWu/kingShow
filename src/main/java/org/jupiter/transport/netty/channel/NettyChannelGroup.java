package org.jupiter.transport.netty.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.jupiter.common.atomic.AtomicUpdater;
import org.jupiter.common.util.IntSequence;
import org.jupiter.common.util.JConstants;
import org.jupiter.common.util.Lists;
import org.jupiter.common.util.Maps;
import org.jupiter.common.util.Preconditions;
import org.jupiter.common.util.SystemClock;
import org.jupiter.common.util.SystemPropertyUtil;
import org.jupiter.common.util.ThrowUtil;
import org.jupiter.transport.Directory;
import org.jupiter.transport.UnresolvedAddress;
import org.jupiter.transport.channel.JChannel;
import org.jupiter.transport.channel.JChannelGroup;

















public class NettyChannelGroup
  implements JChannelGroup
{
  private static long LOSS_INTERVAL = SystemPropertyUtil.getLong("jupiter.io.channel.group.loss.interval.millis", TimeUnit.MINUTES.toMillis(5L));
  

  private static int DEFAULT_SEQUENCE_STEP = (JConstants.AVAILABLE_PROCESSORS << 3) + 1;
  
  private static final AtomicReferenceFieldUpdater<CopyOnWriteArrayList, Object[]> channelsUpdater = AtomicUpdater.newAtomicReferenceFieldUpdater(CopyOnWriteArrayList.class, [Ljava.lang.Object.class, "array");
  
  private static final AtomicIntegerFieldUpdater<NettyChannelGroup> signalNeededUpdater = AtomicIntegerFieldUpdater.newUpdater(NettyChannelGroup.class, "signalNeeded");
  

  private final ConcurrentLinkedQueue<Runnable> waitAvailableListeners = new ConcurrentLinkedQueue();
  
  private final UnresolvedAddress address;
  
  private final CopyOnWriteArrayList<NettyChannel> channels = new CopyOnWriteArrayList();
  

  private final ChannelFutureListener remover = new ChannelFutureListener()
  {
    public void operationComplete(ChannelFuture future) throws Exception
    {
      remove(NettyChannel.attachChannel(future.channel()));
    }
  };
  
  private final IntSequence sequence = new IntSequence(DEFAULT_SEQUENCE_STEP);
  
  private final ConcurrentMap<String, Integer> weights = Maps.newConcurrentMap();
  
  private final ReentrantLock lock = new ReentrantLock();
  private final Condition notifyCondition = lock.newCondition();
  
  private volatile int signalNeeded = 0;
  

  private volatile boolean connecting = false;
  
  private volatile int capacity = Integer.MAX_VALUE;
  private volatile int warmUp = JConstants.DEFAULT_WARM_UP;
  private volatile long timestamp = SystemClock.millisClock().now();
  private volatile long deadlineMillis = -1L;
  
  public NettyChannelGroup(UnresolvedAddress address) {
    this.address = address;
  }
  
  public UnresolvedAddress remoteAddress()
  {
    return address;
  }
  
  public JChannel next() {
    Object[] elements;
    int length;
    do {
      elements = (Object[])channelsUpdater.get(channels);
      length = elements.length;
      if (length != 0) break;
    } while (waitForAvailable(1000L));
    

    throw new IllegalStateException("No channel");
    
    if (length == 1) {
      return (JChannel)elements[0];
    }
    
    int index = sequence.next() & 0x7FFFFFFF;
    
    return (JChannel)elements[(index % length)];
  }
  

  public List<? extends JChannel> channels()
  {
    return Lists.newArrayList(channels);
  }
  
  public boolean isEmpty()
  {
    return channels.isEmpty();
  }
  
  public boolean add(JChannel channel)
  {
    boolean added = ((channel instanceof NettyChannel)) && (channels.add((NettyChannel)channel));
    if (added) {
      timestamp = SystemClock.millisClock().now();
      
      ((NettyChannel)channel).channel().closeFuture().addListener(remover);
      deadlineMillis = -1L;
      
      if (signalNeededUpdater.getAndSet(this, 0) != 0) {
        ReentrantLock _look = lock;
        _look.lock();
        try {
          notifyCondition.signalAll();
        } finally {
          _look.unlock();
        }
      }
      
      notifyListeners();
    }
    return added;
  }
  
  public boolean remove(JChannel channel)
  {
    boolean removed = ((channel instanceof NettyChannel)) && (channels.remove(channel));
    if (removed) {
      timestamp = SystemClock.millisClock().now();
      
      if (channels.isEmpty()) {
        deadlineMillis = (SystemClock.millisClock().now() + LOSS_INTERVAL);
      }
    }
    return removed;
  }
  
  public int size()
  {
    return channels.size();
  }
  
  public void setCapacity(int capacity)
  {
    this.capacity = capacity;
  }
  
  public int getCapacity()
  {
    return capacity;
  }
  
  public boolean isConnecting()
  {
    return connecting;
  }
  
  public void setConnecting(boolean connecting)
  {
    this.connecting = connecting;
  }
  
  public boolean isAvailable()
  {
    return !channels.isEmpty();
  }
  
  public boolean waitForAvailable(long timeoutMillis)
  {
    boolean available = isAvailable();
    if (available) {
      return true;
    }
    long remains = TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
    
    ReentrantLock _look = lock;
    _look.lock();
    try
    {
      while (!(available = isAvailable())) {
        signalNeeded = 1;
        if ((remains = notifyCondition.awaitNanos(remains)) <= 0L) {
          break;
        }
      }
    } catch (InterruptedException e) {
      ThrowUtil.throwException(e);
    } finally {
      _look.unlock();
    }
    
    return available;
  }
  
  public void onAvailable(Runnable listener)
  {
    waitAvailableListeners.add(listener);
    if (isAvailable()) {
      notifyListeners();
    }
  }
  
  public int getWeight(Directory directory)
  {
    Preconditions.checkNotNull(directory, "directory");
    
    Integer weight = (Integer)weights.get(directory.directoryString());
    return weight == null ? JConstants.DEFAULT_WEIGHT : weight.intValue();
  }
  
  public void putWeight(Directory directory, int weight)
  {
    Preconditions.checkNotNull(directory, "directory");
    
    if (weight == JConstants.DEFAULT_WEIGHT)
    {
      return;
    }
    weights.put(directory.directoryString(), Integer.valueOf(weight > JConstants.MAX_WEIGHT ? JConstants.MAX_WEIGHT : weight));
  }
  
  public void removeWeight(Directory directory)
  {
    Preconditions.checkNotNull(directory, "directory");
    
    weights.remove(directory.directoryString());
  }
  
  public int getWarmUp()
  {
    return warmUp > 0 ? warmUp : 0;
  }
  
  public void setWarmUp(int warmUp)
  {
    this.warmUp = warmUp;
  }
  
  public boolean isWarmUpComplete()
  {
    return SystemClock.millisClock().now() - timestamp > warmUp;
  }
  
  public long timestamp()
  {
    return timestamp;
  }
  
  public long deadlineMillis()
  {
    return deadlineMillis;
  }
  
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if ((o == null) || (getClass() != o.getClass())) { return false;
    }
    NettyChannelGroup that = (NettyChannelGroup)o;
    
    return address.equals(address);
  }
  
  public int hashCode()
  {
    return address.hashCode();
  }
  
  public String toString()
  {
    return "NettyChannelGroup{address=" + address + ", channels=" + channels + ", weights=" + weights + ", warmUp=" + warmUp + ", timestamp=" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ").format(new Date(timestamp)) + ", deadlineMillis=" + deadlineMillis + '}';
  }
  





  void notifyListeners()
  {
    for (;;)
    {
      Runnable listener = (Runnable)waitAvailableListeners.poll();
      if (listener == null) {
        break;
      }
      listener.run();
    }
  }
}
