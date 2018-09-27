package org.jupiter.transport.netty.handler.connector;

import io.netty.channel.Channel;
import io.netty.channel.Channel.Unsafe;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.handler.codec.DecoderException;
import io.netty.util.ReferenceCountUtil;
import java.io.IOException;
import org.jupiter.common.util.Signal;
import org.jupiter.common.util.StackTraceUtil;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;
import org.jupiter.transport.netty.channel.NettyChannel;
import org.jupiter.transport.payload.JResponsePayload;
import org.jupiter.transport.processor.ConsumerProcessor;


















@ChannelHandler.Sharable
public class ConnectorHandler
  extends ChannelInboundHandlerAdapter
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(ConnectorHandler.class);
  private ConsumerProcessor processor;
  
  public ConnectorHandler() {}
  
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    Channel ch = ctx.channel();
    
    if ((msg instanceof JResponsePayload)) {
      try {
        processor.handleResponse(NettyChannel.attachChannel(ch), (JResponsePayload)msg);
      } catch (Throwable t) {
        logger.error("An exception was caught: {}, on {} #channelRead().", StackTraceUtil.stackTrace(t), ch);
      }
    } else {
      logger.warn("Unexpected message type received: {}, channel: {}.", msg.getClass(), ch);
      
      ReferenceCountUtil.release(msg);
    }
  }
  
  public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception
  {
    Channel ch = ctx.channel();
    ChannelConfig config = ch.config();
    


    if (!ch.isWritable())
    {
      if (logger.isWarnEnabled()) {
        logger.warn("{} is not writable, high water mask: {}, the number of flushed entries that are not written yet: {}.", new Object[] { ch, Integer.valueOf(config.getWriteBufferHighWaterMark()), Integer.valueOf(ch.unsafe().outboundBuffer().size()) });
      }
      

      config.setAutoRead(false);
    }
    else {
      if (logger.isWarnEnabled()) {
        logger.warn("{} is writable(rehabilitate), low water mask: {}, the number of flushed entries that are not written yet: {}.", new Object[] { ch, Integer.valueOf(config.getWriteBufferLowWaterMark()), Integer.valueOf(ch.unsafe().outboundBuffer().size()) });
      }
      

      config.setAutoRead(true);
    }
  }
  
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
  {
    Channel ch = ctx.channel();
    
    if ((cause instanceof Signal)) {
      logger.error("I/O signal was caught: {}, force to close channel: {}.", ((Signal)cause).name(), ch);
      
      ch.close();
    } else if ((cause instanceof IOException)) {
      logger.error("I/O exception was caught: {}, force to close channel: {}.", StackTraceUtil.stackTrace(cause), ch);
      
      ch.close();
    } else if ((cause instanceof DecoderException)) {
      logger.error("Decoder exception was caught: {}, force to close channel: {}.", StackTraceUtil.stackTrace(cause), ch);
      
      ch.close();
    } else {
      logger.error("Unexpected exception was caught: {}, channel: {}.", StackTraceUtil.stackTrace(cause), ch);
    }
  }
  
  public ConsumerProcessor processor() {
    return processor;
  }
  
  public void processor(ConsumerProcessor processor) {
    this.processor = processor;
  }
}
