package org.jupiter.transport.netty.handler.acceptor;

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
import java.util.concurrent.atomic.AtomicInteger;
import org.jupiter.common.util.Signal;
import org.jupiter.common.util.StackTraceUtil;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;
import org.jupiter.transport.Status;
import org.jupiter.transport.channel.JChannel;
import org.jupiter.transport.netty.channel.NettyChannel;
import org.jupiter.transport.payload.JRequestPayload;
import org.jupiter.transport.processor.ProviderProcessor;


















@ChannelHandler.Sharable
public class AcceptorHandler
  extends ChannelInboundHandlerAdapter
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(AcceptorHandler.class);
  
  private static final AtomicInteger channelCounter = new AtomicInteger(0);
  private ProviderProcessor processor;
  
  public AcceptorHandler() {}
  
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    Channel ch = ctx.channel();
    
    if ((msg instanceof JRequestPayload)) {
      JChannel jChannel = NettyChannel.attachChannel(ch);
      try {
        processor.handleRequest(jChannel, (JRequestPayload)msg);
      } catch (Throwable t) {
        processor.handleException(jChannel, (JRequestPayload)msg, Status.SERVER_ERROR, t);
      }
    } else {
      logger.warn("Unexpected message type received: {}, channel: {}.", msg.getClass(), ch);
      
      ReferenceCountUtil.release(msg);
    }
  }
  
  public void channelActive(ChannelHandlerContext ctx) throws Exception
  {
    int count = channelCounter.incrementAndGet();
    
    logger.info("Connects with {} as the {}th channel.", ctx.channel(), Integer.valueOf(count));
    
    super.channelActive(ctx);
  }
  
  public void channelInactive(ChannelHandlerContext ctx) throws Exception
  {
    int count = channelCounter.getAndDecrement();
    
    logger.warn("Disconnects with {} as the {}th channel.", ctx.channel(), Integer.valueOf(count));
    
    super.channelInactive(ctx);
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
      logger.error("An I/O exception was caught: {}, force to close channel: {}.", StackTraceUtil.stackTrace(cause), ch);
      
      ch.close();
    } else if ((cause instanceof DecoderException)) {
      logger.error("Decoder exception was caught: {}, force to close channel: {}.", StackTraceUtil.stackTrace(cause), ch);
      
      ch.close();
    } else {
      logger.error("Unexpected exception was caught: {}, channel: {}.", StackTraceUtil.stackTrace(cause), ch);
    }
  }
  
  public ProviderProcessor processor() {
    return processor;
  }
  
  public void processor(ProviderProcessor processor) {
    this.processor = processor;
  }
}
