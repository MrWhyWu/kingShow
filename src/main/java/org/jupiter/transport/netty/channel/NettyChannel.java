package org.jupiter.transport.netty.channel;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoop;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import java.io.OutputStream;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import org.jupiter.serialization.io.OutputBuf;
import org.jupiter.transport.channel.JChannel;
import org.jupiter.transport.channel.JFutureListener;
import org.jupiter.transport.netty.alloc.AdaptiveOutputBufAllocator;
import org.jupiter.transport.netty.alloc.AdaptiveOutputBufAllocator.Handle;
import org.jupiter.transport.netty.handler.connector.ConnectionWatchdog;






















public class NettyChannel
  implements JChannel
{
  private static final AttributeKey<NettyChannel> NETTY_CHANNEL_KEY = AttributeKey.valueOf("netty.channel");
  
  private final Channel channel;
  
  public static NettyChannel attachChannel(Channel channel)
  {
    Attribute<NettyChannel> attr = channel.attr(NETTY_CHANNEL_KEY);
    NettyChannel nChannel = (NettyChannel)attr.get();
    if (nChannel == null) {
      NettyChannel newNChannel = new NettyChannel(channel);
      nChannel = (NettyChannel)attr.setIfAbsent(newNChannel);
      if (nChannel == null) {
        nChannel = newNChannel;
      }
    }
    return nChannel;
  }
  

  private final AdaptiveOutputBufAllocator.Handle allocHandle = AdaptiveOutputBufAllocator.DEFAULT.newHandle();
  
  private NettyChannel(Channel channel) {
    this.channel = channel;
  }
  
  public Channel channel() {
    return channel;
  }
  
  public String id()
  {
    return channel.id().asShortText();
  }
  
  public boolean isActive()
  {
    return channel.isActive();
  }
  
  public boolean inIoThread()
  {
    return channel.eventLoop().inEventLoop();
  }
  
  public SocketAddress localAddress()
  {
    return channel.localAddress();
  }
  
  public SocketAddress remoteAddress()
  {
    return channel.remoteAddress();
  }
  
  public boolean isWritable()
  {
    return channel.isWritable();
  }
  
  public boolean isMarkedReconnect()
  {
    ConnectionWatchdog watchdog = (ConnectionWatchdog)channel.pipeline().get(ConnectionWatchdog.class);
    return (watchdog != null) && (watchdog.isStarted());
  }
  
  public boolean isAutoRead()
  {
    return channel.config().isAutoRead();
  }
  
  public void setAutoRead(boolean autoRead)
  {
    channel.config().setAutoRead(autoRead);
  }
  
  public JChannel close()
  {
    channel.close();
    return this;
  }
  
  public JChannel close(final JFutureListener<JChannel> listener)
  {
    final JChannel jChannel = this;
    channel.close().addListener(new ChannelFutureListener()
    {
      public void operationComplete(ChannelFuture future) throws Exception
      {
        if (future.isSuccess()) {
          listener.operationSuccess(jChannel);
        } else {
          listener.operationFailure(jChannel, future.cause());
        }
      }
    });
    return jChannel;
  }
  
  public JChannel write(Object msg)
  {
    channel.writeAndFlush(msg, channel.voidPromise());
    return this;
  }
  
  public JChannel write(Object msg, final JFutureListener<JChannel> listener)
  {
    final JChannel jChannel = this;
    channel.writeAndFlush(msg).addListener(new ChannelFutureListener()
    {
      public void operationComplete(ChannelFuture future)
        throws Exception
      {
        if (future.isSuccess()) {
          listener.operationSuccess(jChannel);
        } else {
          listener.operationFailure(jChannel, future.cause());
        }
      }
    });
    return jChannel;
  }
  
  public OutputBuf allocOutputBuf()
  {
    return new NettyOutputBuf(allocHandle, channel.alloc());
  }
  
  public boolean equals(Object obj)
  {
    return (this == obj) || (((obj instanceof NettyChannel)) && (channel.equals(channel)));
  }
  
  public int hashCode()
  {
    return channel.hashCode();
  }
  
  public String toString()
  {
    return channel.toString();
  }
  
  static final class NettyOutputBuf implements OutputBuf
  {
    private final AdaptiveOutputBufAllocator.Handle allocHandle;
    private final ByteBuf byteBuf;
    private ByteBuffer nioByteBuffer;
    
    public NettyOutputBuf(AdaptiveOutputBufAllocator.Handle allocHandle, ByteBufAllocator alloc) {
      this.allocHandle = allocHandle;
      byteBuf = allocHandle.allocate(alloc);
      
      byteBuf.ensureWritable(16).writerIndex(byteBuf.writerIndex() + 16);
    }
    


    public OutputStream outputStream()
    {
      return new ByteBufOutputStream(byteBuf);
    }
    
    public ByteBuffer nioByteBuffer(int minWritableBytes)
    {
      if (minWritableBytes < 0) {
        minWritableBytes = byteBuf.writableBytes();
      }
      
      if (nioByteBuffer == null) {
        nioByteBuffer = newNioByteBuffer(byteBuf, minWritableBytes);
      }
      
      if (nioByteBuffer.remaining() >= minWritableBytes) {
        return nioByteBuffer;
      }
      
      int position = nioByteBuffer.position();
      nioByteBuffer = newNioByteBuffer(byteBuf, position + minWritableBytes);
      nioByteBuffer.position(position);
      return nioByteBuffer;
    }
    
    public int size()
    {
      if (nioByteBuffer == null) {
        return byteBuf.readableBytes();
      }
      return Math.max(byteBuf.readableBytes(), nioByteBuffer.position());
    }
    
    public boolean hasMemoryAddress()
    {
      return byteBuf.hasMemoryAddress();
    }
    
    public Object backingObject()
    {
      int actualWroteBytes = byteBuf.writerIndex();
      if (nioByteBuffer != null) {
        actualWroteBytes += nioByteBuffer.position();
      }
      
      allocHandle.record(actualWroteBytes);
      
      return byteBuf.writerIndex(actualWroteBytes);
    }
    
    private static ByteBuffer newNioByteBuffer(ByteBuf byteBuf, int writableBytes) {
      return byteBuf.ensureWritable(writableBytes).nioBuffer(byteBuf.writerIndex(), byteBuf.writableBytes());
    }
  }
}
