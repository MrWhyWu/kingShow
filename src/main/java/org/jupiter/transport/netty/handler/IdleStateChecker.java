package org.jupiter.transport.netty.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import java.util.concurrent.TimeUnit;
import org.jupiter.common.util.SystemClock;




























public class IdleStateChecker
  extends ChannelDuplexHandler
{
  private static final long MIN_TIMEOUT_MILLIS = 1L;
  private final ChannelFutureListener writeListener = new ChannelFutureListener()
  {
    public void operationComplete(ChannelFuture future) throws Exception
    {
      firstWriterIdleEvent = IdleStateChecker.access$102(IdleStateChecker.this, true);
      lastWriteTime = SystemClock.millisClock().now();
    }
  };
  
  private final HashedWheelTimer timer;
  
  private final long readerIdleTimeMillis;
  
  private final long writerIdleTimeMillis;
  
  private final long allIdleTimeMillis;
  private volatile int state;
  private volatile boolean reading;
  private volatile Timeout readerIdleTimeout;
  private volatile long lastReadTime;
  private boolean firstReaderIdleEvent = true;
  
  private volatile Timeout writerIdleTimeout;
  private volatile long lastWriteTime;
  private boolean firstWriterIdleEvent = true;
  
  private volatile Timeout allIdleTimeout;
  private boolean firstAllIdleEvent = true;
  




  public IdleStateChecker(HashedWheelTimer timer, int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds)
  {
    this(timer, readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds, TimeUnit.SECONDS);
  }
  





  public IdleStateChecker(HashedWheelTimer timer, long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit)
  {
    if (unit == null) {
      throw new NullPointerException("unit");
    }
    
    this.timer = timer;
    
    if (readerIdleTime <= 0L) {
      readerIdleTimeMillis = 0L;
    } else {
      readerIdleTimeMillis = Math.max(unit.toMillis(readerIdleTime), 1L);
    }
    if (writerIdleTime <= 0L) {
      writerIdleTimeMillis = 0L;
    } else {
      writerIdleTimeMillis = Math.max(unit.toMillis(writerIdleTime), 1L);
    }
    if (allIdleTime <= 0L) {
      allIdleTimeMillis = 0L;
    } else {
      allIdleTimeMillis = Math.max(unit.toMillis(allIdleTime), 1L);
    }
  }
  


  public long getReaderIdleTimeInMillis()
  {
    return readerIdleTimeMillis;
  }
  


  public long getWriterIdleTimeInMillis()
  {
    return writerIdleTimeMillis;
  }
  


  public long getAllIdleTimeInMillis()
  {
    return allIdleTimeMillis;
  }
  
  public void handlerAdded(ChannelHandlerContext ctx)
    throws Exception
  {
    Channel ch = ctx.channel();
    
    if ((ch.isActive()) && (ch.isRegistered()))
    {

      initialize(ctx);
    }
  }
  


  public void handlerRemoved(ChannelHandlerContext ctx)
    throws Exception
  {
    destroy();
  }
  
  public void channelRegistered(ChannelHandlerContext ctx)
    throws Exception
  {
    if (ctx.channel().isActive()) {
      initialize(ctx);
    }
    super.channelRegistered(ctx);
  }
  


  public void channelActive(ChannelHandlerContext ctx)
    throws Exception
  {
    initialize(ctx);
    super.channelActive(ctx);
  }
  
  public void channelInactive(ChannelHandlerContext ctx) throws Exception
  {
    destroy();
    super.channelInactive(ctx);
  }
  
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
  {
    if ((readerIdleTimeMillis > 0L) || (allIdleTimeMillis > 0L)) {
      firstReaderIdleEvent = (this.firstAllIdleEvent = 1);
      reading = true;
    }
    ctx.fireChannelRead(msg);
  }
  
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception
  {
    if ((readerIdleTimeMillis > 0L) || (allIdleTimeMillis > 0L)) {
      lastReadTime = SystemClock.millisClock().now();
      reading = false;
    }
    ctx.fireChannelReadComplete();
  }
  
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception
  {
    if ((writerIdleTimeMillis > 0L) || (allIdleTimeMillis > 0L)) {
      if (promise.isVoid()) {
        firstWriterIdleEvent = (this.firstAllIdleEvent = 1);
        lastWriteTime = SystemClock.millisClock().now();
      } else {
        promise.addListener(writeListener);
      }
    }
    ctx.write(msg, promise);
  }
  

  private void initialize(ChannelHandlerContext ctx)
  {
    switch (state) {
    case 1: 
    case 2: 
      return;
    }
    
    state = 1;
    
    lastReadTime = (this.lastWriteTime = SystemClock.millisClock().now());
    if (readerIdleTimeMillis > 0L) {
      readerIdleTimeout = timer.newTimeout(new ReaderIdleTimeoutTask(ctx), readerIdleTimeMillis, TimeUnit.MILLISECONDS);
    }
    

    if (writerIdleTimeMillis > 0L) {
      writerIdleTimeout = timer.newTimeout(new WriterIdleTimeoutTask(ctx), writerIdleTimeMillis, TimeUnit.MILLISECONDS);
    }
    

    if (allIdleTimeMillis > 0L) {
      allIdleTimeout = timer.newTimeout(new AllIdleTimeoutTask(ctx), allIdleTimeMillis, TimeUnit.MILLISECONDS);
    }
  }
  

  private void destroy()
  {
    state = 2;
    
    if (readerIdleTimeout != null) {
      readerIdleTimeout.cancel();
      readerIdleTimeout = null;
    }
    if (writerIdleTimeout != null) {
      writerIdleTimeout.cancel();
      writerIdleTimeout = null;
    }
    if (allIdleTimeout != null) {
      allIdleTimeout.cancel();
      allIdleTimeout = null;
    }
  }
  
  protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
    ctx.fireUserEventTriggered(evt);
  }
  
  private final class ReaderIdleTimeoutTask implements TimerTask
  {
    private final ChannelHandlerContext ctx;
    
    ReaderIdleTimeoutTask(ChannelHandlerContext ctx) {
      this.ctx = ctx;
    }
    
    public void run(Timeout timeout) throws Exception
    {
      if ((timeout.isCancelled()) || (!ctx.channel().isOpen())) {
        return;
      }
      
      long lastReadTime = IdleStateChecker.this.lastReadTime;
      long nextDelay = readerIdleTimeMillis;
      if (!reading) {
        nextDelay -= SystemClock.millisClock().now() - lastReadTime;
      }
      if (nextDelay <= 0L)
      {
        readerIdleTimeout = timer.newTimeout(this, readerIdleTimeMillis, TimeUnit.MILLISECONDS);
        try { IdleStateEvent event;
          IdleStateEvent event;
          if (firstReaderIdleEvent) {
            firstReaderIdleEvent = false;
            event = IdleStateEvent.FIRST_READER_IDLE_STATE_EVENT;
          } else {
            event = IdleStateEvent.READER_IDLE_STATE_EVENT;
          }
          channelIdle(ctx, event);
        } catch (Throwable t) {
          ctx.fireExceptionCaught(t);
        }
      }
      else {
        readerIdleTimeout = timer.newTimeout(this, nextDelay, TimeUnit.MILLISECONDS);
      }
    }
  }
  
  private final class WriterIdleTimeoutTask implements TimerTask
  {
    private final ChannelHandlerContext ctx;
    
    WriterIdleTimeoutTask(ChannelHandlerContext ctx) {
      this.ctx = ctx;
    }
    
    public void run(Timeout timeout) throws Exception
    {
      if ((timeout.isCancelled()) || (!ctx.channel().isOpen())) {
        return;
      }
      
      long lastWriteTime = IdleStateChecker.this.lastWriteTime;
      long nextDelay = writerIdleTimeMillis - (SystemClock.millisClock().now() - lastWriteTime);
      if (nextDelay <= 0L)
      {
        writerIdleTimeout = timer.newTimeout(this, writerIdleTimeMillis, TimeUnit.MILLISECONDS);
        try { IdleStateEvent event;
          IdleStateEvent event;
          if (firstWriterIdleEvent) {
            firstWriterIdleEvent = false;
            event = IdleStateEvent.FIRST_WRITER_IDLE_STATE_EVENT;
          } else {
            event = IdleStateEvent.WRITER_IDLE_STATE_EVENT;
          }
          channelIdle(ctx, event);
        } catch (Throwable t) {
          ctx.fireExceptionCaught(t);
        }
      }
      else {
        writerIdleTimeout = timer.newTimeout(this, nextDelay, TimeUnit.MILLISECONDS);
      }
    }
  }
  
  private final class AllIdleTimeoutTask implements TimerTask
  {
    private final ChannelHandlerContext ctx;
    
    AllIdleTimeoutTask(ChannelHandlerContext ctx) {
      this.ctx = ctx;
    }
    
    public void run(Timeout timeout) throws Exception
    {
      if ((timeout.isCancelled()) || (!ctx.channel().isOpen())) {
        return;
      }
      
      long nextDelay = allIdleTimeMillis;
      if (!reading) {
        long lastIoTime = Math.max(lastReadTime, lastWriteTime);
        nextDelay -= SystemClock.millisClock().now() - lastIoTime;
      }
      if (nextDelay <= 0L)
      {

        allIdleTimeout = timer.newTimeout(this, allIdleTimeMillis, TimeUnit.MILLISECONDS);
        try { IdleStateEvent event;
          IdleStateEvent event;
          if (firstAllIdleEvent) {
            firstAllIdleEvent = false;
            event = IdleStateEvent.FIRST_ALL_IDLE_STATE_EVENT;
          } else {
            event = IdleStateEvent.ALL_IDLE_STATE_EVENT;
          }
          channelIdle(ctx, event);
        } catch (Throwable t) {
          ctx.fireExceptionCaught(t);
        }
      }
      else
      {
        allIdleTimeout = timer.newTimeout(this, nextDelay, TimeUnit.MILLISECONDS);
      }
    }
  }
}
