package org.jupiter.registry;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.Timer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import org.jupiter.common.concurrent.collection.ConcurrentSet;
import org.jupiter.common.util.JConstants;
import org.jupiter.common.util.Maps;
import org.jupiter.common.util.Pair;
import org.jupiter.common.util.Preconditions;
import org.jupiter.common.util.Signal;
import org.jupiter.common.util.StackTraceUtil;
import org.jupiter.common.util.SystemClock;
import org.jupiter.common.util.SystemPropertyUtil;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;
import org.jupiter.serialization.Serializer;
import org.jupiter.serialization.SerializerFactory;
import org.jupiter.serialization.SerializerType;
import org.jupiter.transport.Acknowledge;
import org.jupiter.transport.JConfig;
import org.jupiter.transport.JConnection;
import org.jupiter.transport.JOption;
import org.jupiter.transport.JProtocolHeader;
import org.jupiter.transport.UnresolvedAddress;
import org.jupiter.transport.channel.JChannelGroup;
import org.jupiter.transport.exception.ConnectFailedException;
import org.jupiter.transport.exception.IoSignals;
import org.jupiter.transport.netty.NettyTcpConnector;
import org.jupiter.transport.netty.handler.AcknowledgeEncoder;
import org.jupiter.transport.netty.handler.IdleStateChecker;
import org.jupiter.transport.netty.handler.connector.ConnectionWatchdog;
import org.jupiter.transport.netty.handler.connector.ConnectorIdleStateTrigger;






public final class DefaultRegistry
  extends NettyTcpConnector
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultRegistry.class);
  
  private static final AttributeKey<ConcurrentSet<RegisterMeta.ServiceMeta>> C_SUBSCRIBE_KEY = AttributeKey.valueOf("client.subscribed");
  
  private static final AttributeKey<ConcurrentSet<RegisterMeta>> C_PUBLISH_KEY = AttributeKey.valueOf("client.published");
  


  private final ConcurrentMap<Long, MessageNonAck> messagesNonAck = Maps.newConcurrentMap();
  

  private final ConnectorIdleStateTrigger idleStateTrigger = new ConnectorIdleStateTrigger();
  private final MessageHandler handler = new MessageHandler();
  private final MessageEncoder encoder = new MessageEncoder();
  private final AcknowledgeEncoder ackEncoder = new AcknowledgeEncoder();
  


  private final SerializerType serializerType;
  

  private final AbstractRegistryService registryService;
  

  private volatile Channel channel;
  


  public DefaultRegistry(AbstractRegistryService registryService)
  {
    this(registryService, 1);
  }
  
  public DefaultRegistry(AbstractRegistryService registryService, int nWorkers) {
    super(nWorkers);SerializerType expected = SerializerType.parse(SystemPropertyUtil.get("jupiter.registry.default.serializer_type"));serializerType = (expected == null ? SerializerType.getDefault() : expected);
    







































































































































































































































































































































































































































































































    Thread t = new Thread(new AckTimeoutScanner(null), "ack.timeout.scanner");
    t.setDaemon(true);
    t.start();this.registryService = ((AbstractRegistryService)Preconditions.checkNotNull(registryService, "registryService"));
  }
  

  protected void doInit()
  {
    config().setOption(JOption.SO_REUSEADDR, Boolean.valueOf(true));
    config().setOption(JOption.CONNECT_TIMEOUT_MILLIS, Integer.valueOf((int)TimeUnit.SECONDS.toMillis(3L)));
    
    initChannelFactory();
  }
  



  public JConnection connect(UnresolvedAddress address, boolean async)
  {
    setOptions();
    
    Bootstrap boot = bootstrap();
    SocketAddress socketAddress = InetSocketAddress.createUnresolved(address.getHost(), address.getPort());
    

    final ConnectionWatchdog watchdog = new ConnectionWatchdog(boot, timer, socketAddress, null)
    {
      public ChannelHandler[] handlers()
      {
        return new ChannelHandler[] { this, new IdleStateChecker(timer, 0, JConstants.WRITER_IDLE_TIME_SECONDS, 0), idleStateTrigger, new DefaultRegistry.MessageDecoder(), encoder, ackEncoder, handler };
      }
    };
    



    try
    {
      ChannelFuture future;
      



      synchronized (bootstrapLock()) {
        boot.handler(new ChannelInitializer()
        {
          protected void initChannel(Channel ch) throws Exception
          {
            ch.pipeline().addLast(watchdog.handlers());
          }
          
        });
        future = boot.connect(socketAddress);
      }
      
      ChannelFuture future;
      future.sync();
      channel = future.channel();
    } catch (Throwable t) {
      throw new ConnectFailedException("connects to [" + address + "] fails", t);
    }
    
    new JConnection(address)
    {
      public void setReconnect(boolean reconnect)
      {
        if (reconnect) {
          watchdog.start();
        } else {
          watchdog.stop();
        }
      }
    };
  }
  


  public void doSubscribe(RegisterMeta.ServiceMeta serviceMeta)
  {
    Message msg = new Message(serializerType.value());
    msg.messageCode((byte)5);
    msg.data(serviceMeta);
    
    Channel ch = channel;
    
    if (attachSubscribeEventOnChannel(serviceMeta, ch)) {
      ch.writeAndFlush(msg).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
      

      MessageNonAck msgNonAck = new MessageNonAck(msg);
      messagesNonAck.put(Long.valueOf(id), msgNonAck);
    }
  }
  


  public void doRegister(RegisterMeta meta)
  {
    Message msg = new Message(serializerType.value());
    msg.messageCode((byte)3);
    msg.data(meta);
    
    Channel ch = channel;
    
    if (attachPublishEventOnChannel(meta, ch)) {
      ch.writeAndFlush(msg).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
      

      MessageNonAck msgNonAck = new MessageNonAck(msg);
      messagesNonAck.put(Long.valueOf(id), msgNonAck);
    }
  }
  


  public void doUnregister(final RegisterMeta meta)
  {
    Message msg = new Message(serializerType.value());
    msg.messageCode((byte)4);
    msg.data(meta);
    
    channel.writeAndFlush(msg).addListener(new ChannelFutureListener()
    {
      public void operationComplete(ChannelFuture future)
        throws Exception
      {
        if (!future.isSuccess()) {
          Channel ch = future.channel();
          if (ch.isActive()) {
            ch.pipeline().fireExceptionCaught(future.cause());
          }
          else if (DefaultRegistry.logger.isWarnEnabled()) {
            DefaultRegistry.logger.warn("Unregister {} fail because of channel is inactive: {}.", meta, StackTraceUtil.stackTrace(future.cause()));
          }
          
        }
        
      }
      
    });
    MessageNonAck msgNonAck = new MessageNonAck(msg);
    messagesNonAck.put(Long.valueOf(id), msgNonAck);
  }
  
  private void handleAcknowledge(Acknowledge ack) {
    messagesNonAck.remove(Long.valueOf(ack.sequence()));
  }
  
  private static boolean attachPublishEventOnChannel(RegisterMeta meta, Channel channel)
  {
    Attribute<ConcurrentSet<RegisterMeta>> attr = channel.attr(C_PUBLISH_KEY);
    ConcurrentSet<RegisterMeta> registerMetaSet = (ConcurrentSet)attr.get();
    if (registerMetaSet == null) {
      ConcurrentSet<RegisterMeta> newRegisterMetaSet = new ConcurrentSet();
      registerMetaSet = (ConcurrentSet)attr.setIfAbsent(newRegisterMetaSet);
      if (registerMetaSet == null) {
        registerMetaSet = newRegisterMetaSet;
      }
    }
    
    return registerMetaSet.add(meta);
  }
  
  private static boolean attachSubscribeEventOnChannel(RegisterMeta.ServiceMeta serviceMeta, Channel channel)
  {
    Attribute<ConcurrentSet<RegisterMeta.ServiceMeta>> attr = channel.attr(C_SUBSCRIBE_KEY);
    ConcurrentSet<RegisterMeta.ServiceMeta> serviceMetaSet = (ConcurrentSet)attr.get();
    if (serviceMetaSet == null) {
      ConcurrentSet<RegisterMeta.ServiceMeta> newServiceMetaSet = new ConcurrentSet();
      serviceMetaSet = (ConcurrentSet)attr.setIfAbsent(newServiceMetaSet);
      if (serviceMetaSet == null) {
        serviceMetaSet = newServiceMetaSet;
      }
    }
    
    return serviceMetaSet.add(serviceMeta);
  }
  
  static class MessageNonAck
  {
    private final long id;
    private final Message msg;
    private final long timestamp = SystemClock.millisClock().now();
    
    public MessageNonAck(Message msg) {
      this.msg = msg;
      id = msg.sequence();
    }
  }
  


















  static class MessageDecoder
    extends ReplayingDecoder<State>
  {
    public MessageDecoder()
    {
      super();
    }
    

    private final JProtocolHeader header = new JProtocolHeader();
    
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
      switch (DefaultRegistry.5.$SwitchMap$org$jupiter$registry$DefaultRegistry$MessageDecoder$State[((State)state()).ordinal()]) {
      case 1: 
        checkMagic(in.readShort());
        checkpoint(State.SIGN);
      case 2: 
        header.sign(in.readByte());
        checkpoint(State.STATUS);
      case 3: 
        in.readByte();
        checkpoint(State.ID);
      case 4: 
        header.id(in.readLong());
        checkpoint(State.BODY_SIZE);
      case 5: 
        header.bodySize(in.readInt());
        checkpoint(State.BODY);
      case 6: 
        byte s_code = header.serializerCode();
        
        switch (header.messageCode()) {
        case 3: 
        case 4: 
        case 6: 
          byte[] bytes = new byte[header.bodySize()];
          in.readBytes(bytes);
          
          Serializer serializer = SerializerFactory.getSerializer(s_code);
          Message msg = (Message)serializer.readObject(bytes, Message.class);
          msg.messageCode(header.messageCode());
          out.add(msg);
          
          break;
        
        case 7: 
          out.add(new Acknowledge(header.id()));
          
          break;
        case 5: default: 
          throw IoSignals.ILLEGAL_SIGN;
        }
        
        checkpoint(State.MAGIC);
      }
    }
    
    private static void checkMagic(short magic) throws Signal {
      if (magic != 47806) {
        throw IoSignals.ILLEGAL_MAGIC;
      }
    }
    
    static enum State {
      MAGIC, 
      SIGN, 
      STATUS, 
      ID, 
      BODY_SIZE, 
      BODY;
      





      private State() {}
    }
  }
  





  @ChannelHandler.Sharable
  static class MessageEncoder
    extends MessageToByteEncoder<Message>
  {
    MessageEncoder() {}
    




    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out)
      throws Exception
    {
      byte s_code = msg.serializerCode();
      byte sign = JProtocolHeader.toSign(s_code, msg.messageCode());
      Serializer serializer = SerializerFactory.getSerializer(s_code);
      byte[] bytes = serializer.writeObject(msg);
      
      out.writeShort(47806).writeByte(sign).writeByte(0).writeLong(0L).writeInt(bytes.length).writeBytes(bytes);
    }
  }
  

  @ChannelHandler.Sharable
  class MessageHandler
    extends ChannelInboundHandlerAdapter
  {
    MessageHandler() {}
    

    public void channelRead(ChannelHandlerContext ctx, Object msg)
      throws Exception
    {
      Channel ch = ctx.channel();
      
      if ((msg instanceof Message)) {
        Message obj = (Message)msg;
        
        switch (obj.messageCode()) {
        case 3: 
          Pair<RegisterMeta.ServiceMeta, ?> data = (Pair)obj.data();
          Object metaObj = data.getSecond();
          
          if ((metaObj instanceof List)) {
            List<RegisterMeta> list = (List)metaObj;
            RegisterMeta[] array = new RegisterMeta[list.size()];
            list.toArray(array);
            registryService.notify((RegisterMeta.ServiceMeta)data.getFirst(), NotifyListener.NotifyEvent.CHILD_ADDED, obj.version(), array);




          }
          else if ((metaObj instanceof RegisterMeta)) {
            registryService.notify((RegisterMeta.ServiceMeta)data.getFirst(), NotifyListener.NotifyEvent.CHILD_ADDED, obj.version(), new RegisterMeta[] { (RegisterMeta)metaObj });
          }
          





          ch.writeAndFlush(new Acknowledge(obj.sequence())).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
          

          DefaultRegistry.logger.info("Publish from RegistryServer {}, metadata: {}, version: {}.", new Object[] { data.getFirst(), metaObj, Long.valueOf(obj.version()) });
          

          break;
        
        case 4: 
          Pair<RegisterMeta.ServiceMeta, RegisterMeta> data = (Pair)obj.data();
          
          registryService.notify((RegisterMeta.ServiceMeta)data.getFirst(), NotifyListener.NotifyEvent.CHILD_REMOVED, obj.version(), new RegisterMeta[] { (RegisterMeta)data.getSecond() });
          

          ch.writeAndFlush(new Acknowledge(obj.sequence())).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
          

          DefaultRegistry.logger.info("Publish cancel from RegistryServer {}, metadata: {}, version: {}.", new Object[] { data.getFirst(), data.getSecond(), Long.valueOf(obj.version()) });
          

          break;
        
        case 6: 
          RegisterMeta.Address address = (RegisterMeta.Address)obj.data();
          
          DefaultRegistry.logger.info("Offline notice on {}.", address);
          
          registryService.offline(address);
        }
        
      }
      else if ((msg instanceof Acknowledge)) {
        DefaultRegistry.this.handleAcknowledge((Acknowledge)msg);
      } else {
        DefaultRegistry.logger.warn("Unexpected message type received: {}, channel: {}.", msg.getClass(), ch);
        
        ReferenceCountUtil.release(msg);
      }
    }
    
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
      Channel ch = channel = ctx.channel();
      

      for (RegisterMeta.ServiceMeta serviceMeta : registryService.getSubscribeSet())
      {
        if (DefaultRegistry.attachSubscribeEventOnChannel(serviceMeta, ch))
        {


          Message msg = new Message(serializerType.value());
          msg.messageCode((byte)5);
          msg.data(serviceMeta);
          
          ch.writeAndFlush(msg).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
          

          DefaultRegistry.MessageNonAck msgNonAck = new DefaultRegistry.MessageNonAck(msg);
          messagesNonAck.put(Long.valueOf(DefaultRegistry.MessageNonAck.access$500(msgNonAck)), msgNonAck);
        }
      }
      
      for (RegisterMeta meta : registryService.getRegisterMetaMap().keySet())
      {
        if (DefaultRegistry.attachPublishEventOnChannel(meta, ch))
        {


          Message msg = new Message(serializerType.value());
          msg.messageCode((byte)3);
          msg.data(meta);
          
          ch.writeAndFlush(msg).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
          

          DefaultRegistry.MessageNonAck msgNonAck = new DefaultRegistry.MessageNonAck(msg);
          messagesNonAck.put(Long.valueOf(DefaultRegistry.MessageNonAck.access$500(msgNonAck)), msgNonAck);
        }
      }
    }
    
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
      Channel ch = ctx.channel();
      
      if ((cause instanceof Signal)) {
        DefaultRegistry.logger.error("I/O signal was caught: {}, force to close channel: {}.", ((Signal)cause).name(), ch);
        
        ch.close();
      } else if ((cause instanceof IOException)) {
        DefaultRegistry.logger.error("I/O exception was caught: {}, force to close channel: {}.", StackTraceUtil.stackTrace(cause), ch);
        
        ch.close();
      } else if ((cause instanceof DecoderException)) {
        DefaultRegistry.logger.error("Decoder exception was caught: {}, force to close channel: {}.", StackTraceUtil.stackTrace(cause), ch);
        
        ch.close();
      } else {
        DefaultRegistry.logger.error("Unexpected exception was caught: {}, channel: {}.", StackTraceUtil.stackTrace(cause), ch);
      }
    }
  }
  
  private class AckTimeoutScanner implements Runnable
  {
    private AckTimeoutScanner() {}
    
    public void run() {
      try {
        for (;;) {
          for (DefaultRegistry.MessageNonAck m : messagesNonAck.values()) {
            if (SystemClock.millisClock().now() - DefaultRegistry.MessageNonAck.access$1400(m) > TimeUnit.SECONDS.toMillis(10L))
            {

              if (messagesNonAck.remove(Long.valueOf(DefaultRegistry.MessageNonAck.access$500(m))) != null)
              {


                DefaultRegistry.MessageNonAck msgNonAck = new DefaultRegistry.MessageNonAck(DefaultRegistry.MessageNonAck.access$1500(m));
                messagesNonAck.put(Long.valueOf(DefaultRegistry.MessageNonAck.access$500(msgNonAck)), msgNonAck);
                channel.writeAndFlush(DefaultRegistry.MessageNonAck.access$1500(m)).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
              }
            }
          }
          
          Thread.sleep(300L);
        }
      } catch (Throwable t) { DefaultRegistry.logger.error("An exception was caught while scanning the timeout acknowledges {}.", StackTraceUtil.stackTrace(t));
      }
    }
  }
}
