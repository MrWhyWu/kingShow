package org.jupiter.registry;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.Channel.Unsafe;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelMatcher;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GlobalEventExecutor;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import org.jupiter.common.concurrent.collection.ConcurrentSet;
import org.jupiter.common.util.Function;
import org.jupiter.common.util.JConstants;
import org.jupiter.common.util.Lists;
import org.jupiter.common.util.Maps;
import org.jupiter.common.util.Pair;
import org.jupiter.common.util.Signal;
import org.jupiter.common.util.StackTraceUtil;
import org.jupiter.common.util.Strings;
import org.jupiter.common.util.SystemClock;
import org.jupiter.common.util.SystemPropertyUtil;
import org.jupiter.common.util.ThrowUtil;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;
import org.jupiter.serialization.Serializer;
import org.jupiter.serialization.SerializerFactory;
import org.jupiter.serialization.SerializerType;
import org.jupiter.transport.Acknowledge;
import org.jupiter.transport.JConfig;
import org.jupiter.transport.JConfigGroup;
import org.jupiter.transport.JOption;
import org.jupiter.transport.JProtocolHeader;
import org.jupiter.transport.exception.IoSignals;
import org.jupiter.transport.netty.NettyTcpAcceptor;
import org.jupiter.transport.netty.handler.AcknowledgeEncoder;
import org.jupiter.transport.netty.handler.IdleStateChecker;
import org.jupiter.transport.netty.handler.acceptor.AcceptorIdleStateTrigger;







public final class DefaultRegistryServer
  extends NettyTcpAcceptor
  implements RegistryServer
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultRegistryServer.class);
  
  private static final AttributeKey<ConcurrentSet<RegisterMeta.ServiceMeta>> S_SUBSCRIBE_KEY = AttributeKey.valueOf("server.subscribed");
  
  private static final AttributeKey<ConcurrentSet<RegisterMeta>> S_PUBLISH_KEY = AttributeKey.valueOf("server.published");
  


  private final RegisterInfoContext registerInfoContext = new RegisterInfoContext();
  
  private final ChannelGroup subscriberChannels = new DefaultChannelGroup("subscribers", GlobalEventExecutor.INSTANCE);
  
  private final ConcurrentMap<String, MessageNonAck> messagesNonAck = Maps.newConcurrentMap();
  

  private final AcceptorIdleStateTrigger idleStateTrigger = new AcceptorIdleStateTrigger();
  private final MessageHandler handler = new MessageHandler();
  private final MessageEncoder encoder = new MessageEncoder();
  private final AcknowledgeEncoder ackEncoder = new AcknowledgeEncoder();
  











  private final SerializerType serializerType;
  











  protected void init()
  {
    super.init();
    

    JConfig parent = configGroup().parent();
    parent.setOption(JOption.SO_BACKLOG, Integer.valueOf(32768));
    parent.setOption(JOption.SO_REUSEADDR, Boolean.valueOf(true));
    

    JConfig child = configGroup().child();
    child.setOption(JOption.SO_REUSEADDR, Boolean.valueOf(true));
  }
  
  public ChannelFuture bind(SocketAddress localAddress)
  {
    ServerBootstrap boot = bootstrap();
    
    initChannelFactory();
    
    boot.childHandler(new ChannelInitializer()
    {
      protected void initChannel(Channel ch) throws Exception
      {
        ch.pipeline().addLast(new ChannelHandler[] { new IdleStateChecker(timer, JConstants.READER_IDLE_TIME_SECONDS, 0, 0), idleStateTrigger, new DefaultRegistryServer.MessageDecoder(), encoder, ackEncoder, handler });



      }
      



    });
    setOptions();
    
    return boot.bind(localAddress);
  }
  
  public List<String> listPublisherHosts()
  {
    List<RegisterMeta.Address> fromList = registerInfoContext.listPublisherHosts();
    
    Lists.transform(fromList, new Function()
    {
      public String apply(RegisterMeta.Address input)
      {
        return input.getHost();
      }
    });
  }
  
  public List<String> listSubscriberAddresses()
  {
    List<String> hosts = Lists.newArrayList();
    for (Channel ch : subscriberChannels) {
      SocketAddress address = ch.remoteAddress();
      if ((address instanceof InetSocketAddress)) {
        String host = ((InetSocketAddress)address).getAddress().getHostAddress();
        int port = ((InetSocketAddress)address).getPort();
        hosts.add(new RegisterMeta.Address(host, port).toString());
      }
    }
    return hosts;
  }
  
  public List<String> listAddressesByService(String group, String serviceProviderName, String version)
  {
    RegisterMeta.ServiceMeta serviceMeta = new RegisterMeta.ServiceMeta(group, serviceProviderName, version);
    List<RegisterMeta.Address> fromList = registerInfoContext.listAddressesByService(serviceMeta);
    
    Lists.transform(fromList, new Function()
    {
      public String apply(RegisterMeta.Address input)
      {
        return input.toString();
      }
    });
  }
  
  public List<String> listServicesByAddress(String host, int port)
  {
    RegisterMeta.Address address = new RegisterMeta.Address(host, port);
    List<RegisterMeta.ServiceMeta> fromList = registerInfoContext.listServicesByAddress(address);
    
    Lists.transform(fromList, new Function()
    {
      public String apply(RegisterMeta.ServiceMeta input)
      {
        return input.toString();
      }
    });
  }
  
  public void startRegistryServer()
  {
    try {
      start();
    } catch (InterruptedException e) {
      ThrowUtil.throwException(e);
    }
  }
  

  private void handlePublish(RegisterMeta meta, Channel channel)
  {
    logger.info("Publish {} on channel{}.", meta, channel);
    
    attachPublishEventOnChannel(meta, channel);
    
    final RegisterMeta.ServiceMeta serviceMeta = meta.getServiceMeta();
    ConfigWithVersion<ConcurrentMap<RegisterMeta.Address, RegisterMeta>> config = registerInfoContext.getRegisterMeta(serviceMeta);
    

    synchronized (registerInfoContext.publishLock(config))
    {
      if (((ConcurrentMap)config.getConfig()).putIfAbsent(meta.getAddress(), meta) == null) {
        registerInfoContext.getServiceMeta(meta.getAddress()).add(serviceMeta);
        
        final Message msg = new Message(serializerType.value());
        msg.messageCode((byte)3);
        msg.version(config.newVersion());
        msg.data(Pair.of(serviceMeta, meta));
        
        subscriberChannels.writeAndFlush(msg, new ChannelMatcher()
        {
          public boolean matches(Channel channel)
          {
            boolean doSend = DefaultRegistryServer.isChannelSubscribeOnServiceMeta(serviceMeta, channel);
            if (doSend) {
              DefaultRegistryServer.MessageNonAck msgNonAck = new DefaultRegistryServer.MessageNonAck(serviceMeta, msg, channel);
              
              messagesNonAck.put(id, msgNonAck);
            }
            return doSend;
          }
        });
      }
    }
  }
  

  private void handlePublishCancel(RegisterMeta meta, Channel channel)
  {
    logger.info("Cancel publish {} on channel{}.", meta, channel);
    
    attachPublishCancelEventOnChannel(meta, channel);
    
    final RegisterMeta.ServiceMeta serviceMeta = meta.getServiceMeta();
    ConfigWithVersion<ConcurrentMap<RegisterMeta.Address, RegisterMeta>> config = registerInfoContext.getRegisterMeta(serviceMeta);
    
    if (((ConcurrentMap)config.getConfig()).isEmpty()) {
      return;
    }
    
    synchronized (registerInfoContext.publishLock(config))
    {
      RegisterMeta.Address address = meta.getAddress();
      RegisterMeta data = (RegisterMeta)((ConcurrentMap)config.getConfig()).remove(address);
      if (data != null) {
        registerInfoContext.getServiceMeta(address).remove(serviceMeta);
        
        final Message msg = new Message(serializerType.value());
        msg.messageCode((byte)4);
        msg.version(config.newVersion());
        msg.data(Pair.of(serviceMeta, data));
        
        subscriberChannels.writeAndFlush(msg, new ChannelMatcher()
        {
          public boolean matches(Channel channel)
          {
            boolean doSend = DefaultRegistryServer.isChannelSubscribeOnServiceMeta(serviceMeta, channel);
            if (doSend) {
              DefaultRegistryServer.MessageNonAck msgNonAck = new DefaultRegistryServer.MessageNonAck(serviceMeta, msg, channel);
              
              messagesNonAck.put(id, msgNonAck);
            }
            return doSend;
          }
        });
      }
    }
  }
  

  private void handleSubscribe(RegisterMeta.ServiceMeta serviceMeta, Channel channel)
  {
    logger.info("Subscribe {} on channel{}.", serviceMeta, channel);
    
    attachSubscribeEventOnChannel(serviceMeta, channel);
    
    subscriberChannels.add(channel);
    
    ConfigWithVersion<ConcurrentMap<RegisterMeta.Address, RegisterMeta>> config = registerInfoContext.getRegisterMeta(serviceMeta);
    
    if (((ConcurrentMap)config.getConfig()).isEmpty()) {
      return;
    }
    
    Message msg = new Message(serializerType.value());
    msg.messageCode((byte)3);
    msg.version(config.getVersion());
    List<RegisterMeta> registerMetaList = Lists.newArrayList(((ConcurrentMap)config.getConfig()).values());
    
    msg.data(Pair.of(serviceMeta, registerMetaList));
    
    MessageNonAck msgNonAck = new MessageNonAck(serviceMeta, msg, channel);
    
    messagesNonAck.put(id, msgNonAck);
    channel.writeAndFlush(msg);
  }
  
  private void handleAcknowledge(Acknowledge ack, Channel channel)
  {
    messagesNonAck.remove(key(ack.sequence(), channel));
  }
  

  private void handleOfflineNotice(RegisterMeta.Address address)
  {
    logger.info("OfflineNotice on {}.", address);
    
    Message msg = new Message(serializerType.value());
    msg.messageCode((byte)6);
    msg.data(address);
    subscriberChannels.writeAndFlush(msg);
  }
  
  private static String key(long sequence, Channel channel) {
    return String.valueOf(sequence) + '-' + channel.id().asShortText();
  }
  
  private static boolean attachPublishEventOnChannel(RegisterMeta meta, Channel channel)
  {
    Attribute<ConcurrentSet<RegisterMeta>> attr = channel.attr(S_PUBLISH_KEY);
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
  
  private static boolean attachPublishCancelEventOnChannel(RegisterMeta meta, Channel channel)
  {
    Attribute<ConcurrentSet<RegisterMeta>> attr = channel.attr(S_PUBLISH_KEY);
    ConcurrentSet<RegisterMeta> registerMetaSet = (ConcurrentSet)attr.get();
    if (registerMetaSet == null) {
      ConcurrentSet<RegisterMeta> newRegisterMetaSet = new ConcurrentSet();
      registerMetaSet = (ConcurrentSet)attr.setIfAbsent(newRegisterMetaSet);
      if (registerMetaSet == null) {
        registerMetaSet = newRegisterMetaSet;
      }
    }
    
    return registerMetaSet.remove(meta);
  }
  
  private static boolean attachSubscribeEventOnChannel(RegisterMeta.ServiceMeta serviceMeta, Channel channel)
  {
    Attribute<ConcurrentSet<RegisterMeta.ServiceMeta>> attr = channel.attr(S_SUBSCRIBE_KEY);
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
  
  private static boolean isChannelSubscribeOnServiceMeta(RegisterMeta.ServiceMeta serviceMeta, Channel channel)
  {
    ConcurrentSet<RegisterMeta.ServiceMeta> serviceMetaSet = (ConcurrentSet)channel.attr(S_SUBSCRIBE_KEY).get();
    return (serviceMetaSet != null) && (serviceMetaSet.contains(serviceMeta));
  }
  

  static class MessageNonAck
  {
    private final String id;
    
    private final RegisterMeta.ServiceMeta serviceMeta;
    
    private final Message msg;
    private final Channel channel;
    private final long version;
    private final long timestamp = SystemClock.millisClock().now();
    
    public MessageNonAck(RegisterMeta.ServiceMeta serviceMeta, Message msg, Channel channel) {
      this.serviceMeta = serviceMeta;
      this.msg = msg;
      this.channel = channel;
      version = msg.version();
      
      id = DefaultRegistryServer.key(msg.sequence(), channel);
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
      switch (DefaultRegistryServer.7.$SwitchMap$org$jupiter$registry$DefaultRegistryServer$MessageDecoder$State[((State)state()).ordinal()]) {
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
        case 15: 
          break;
        case 3: 
        case 4: 
        case 5: 
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
        case 8: case 9: case 10: case 11: case 12: case 13: case 14: default: 
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
        case 4: 
          RegisterMeta meta = (RegisterMeta)obj.data();
          if (Strings.isNullOrEmpty(meta.getHost())) {
            SocketAddress address = ch.remoteAddress();
            if ((address instanceof InetSocketAddress)) {
              meta.setHost(((InetSocketAddress)address).getAddress().getHostAddress());
            } else {
              DefaultRegistryServer.logger.warn("Could not get remote host: {}, info: {}", ch, meta);
              
              return;
            }
          }
          
          if (obj.messageCode() == 3) {
            DefaultRegistryServer.this.handlePublish(meta, ch);
          } else if (obj.messageCode() == 4) {
            DefaultRegistryServer.this.handlePublishCancel(meta, ch);
          }
          ch.writeAndFlush(new Acknowledge(obj.sequence())).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
          

          break;
        case 5: 
          DefaultRegistryServer.this.handleSubscribe((RegisterMeta.ServiceMeta)obj.data(), ch);
          ch.writeAndFlush(new Acknowledge(obj.sequence())).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
          

          break;
        case 6: 
          DefaultRegistryServer.this.handleOfflineNotice((RegisterMeta.Address)obj.data());
        }
        
      }
      else if ((msg instanceof Acknowledge)) {
        DefaultRegistryServer.this.handleAcknowledge((Acknowledge)msg, ch);
      } else {
        DefaultRegistryServer.logger.warn("Unexpected message type received: {}, channel: {}.", msg.getClass(), ch);
        
        ReferenceCountUtil.release(msg);
      }
    }
    
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
      Channel ch = ctx.channel();
      

      ConcurrentSet<RegisterMeta> registerMetaSet = (ConcurrentSet)ch.attr(DefaultRegistryServer.S_PUBLISH_KEY).get();
      
      if ((registerMetaSet == null) || (registerMetaSet.isEmpty())) {
        return;
      }
      
      RegisterMeta.Address address = null;
      for (RegisterMeta meta : registerMetaSet) {
        if (address == null) {
          address = meta.getAddress();
        }
        DefaultRegistryServer.this.handlePublishCancel(meta, ch);
      }
      
      if (address != null)
      {
        DefaultRegistryServer.this.handleOfflineNotice(address);
      }
    }
    
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception
    {
      Channel ch = ctx.channel();
      ChannelConfig config = ch.config();
      


      if (!ch.isWritable())
      {
        if (DefaultRegistryServer.logger.isWarnEnabled()) {
          DefaultRegistryServer.logger.warn("{} is not writable, high water mask: {}, the number of flushed entries that are not written yet: {}.", new Object[] { ch, Integer.valueOf(config.getWriteBufferHighWaterMark()), Integer.valueOf(ch.unsafe().outboundBuffer().size()) });
        }
        

        config.setAutoRead(false);
      }
      else {
        if (DefaultRegistryServer.logger.isWarnEnabled()) {
          DefaultRegistryServer.logger.warn("{} is writable(rehabilitate), low water mask: {}, the number of flushed entries that are not written yet: {}.", new Object[] { ch, Integer.valueOf(config.getWriteBufferLowWaterMark()), Integer.valueOf(ch.unsafe().outboundBuffer().size()) });
        }
        

        config.setAutoRead(true);
      }
    }
    
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
      Channel ch = ctx.channel();
      
      if ((cause instanceof Signal)) {
        DefaultRegistryServer.logger.error("I/O signal was caught: {}, force to close channel: {}.", ((Signal)cause).name(), ch);
        
        ch.close();
      } else if ((cause instanceof IOException)) {
        DefaultRegistryServer.logger.error("I/O exception was caught: {}, force to close channel: {}.", StackTraceUtil.stackTrace(cause), cause);
        
        ch.close();
      } else if ((cause instanceof DecoderException)) {
        DefaultRegistryServer.logger.error("Decoder exception was caught: {}, force to close channel: {}.", StackTraceUtil.stackTrace(cause), ch);
        
        ch.close();
      } else {
        DefaultRegistryServer.logger.error("Unexpected exception was caught: {}, channel: {}.", StackTraceUtil.stackTrace(cause), ch);
      }
    }
  }
  
  private class AckTimeoutScanner implements Runnable
  {
    private AckTimeoutScanner() {}
    
    public void run() {
      for (;;) {
        try {
          Iterator i$ = messagesNonAck.values().iterator(); if (i$.hasNext()) { DefaultRegistryServer.MessageNonAck m = (DefaultRegistryServer.MessageNonAck)i$.next();
            if (SystemClock.millisClock().now() - DefaultRegistryServer.MessageNonAck.access$1600(m) > TimeUnit.SECONDS.toMillis(10L))
            {

              if ((messagesNonAck.remove(DefaultRegistryServer.MessageNonAck.access$600(m)) == null) || 
              


                (registerInfoContext.getRegisterMeta(DefaultRegistryServer.MessageNonAck.access$1700(m)).getVersion() > DefaultRegistryServer.MessageNonAck.access$1900(m))) {
                continue;
              }
              

              if (DefaultRegistryServer.MessageNonAck.access$2000(m).isActive()) {
                DefaultRegistryServer.MessageNonAck msgNonAck = new DefaultRegistryServer.MessageNonAck(DefaultRegistryServer.MessageNonAck.access$1700(m), DefaultRegistryServer.MessageNonAck.access$2100(m), DefaultRegistryServer.MessageNonAck.access$2000(m));
                messagesNonAck.put(DefaultRegistryServer.MessageNonAck.access$600(msgNonAck), msgNonAck);
                DefaultRegistryServer.MessageNonAck.access$2000(m).writeAndFlush(DefaultRegistryServer.MessageNonAck.access$2100(m)).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
              }
            }
            
            continue;
          }
        } catch (Throwable t) { DefaultRegistryServer.logger.error("An exception was caught while scanning the timeout acknowledges {}.", StackTraceUtil.stackTrace(t));
        }
        try
        {
          Thread.sleep(300L);
        }
        catch (InterruptedException localInterruptedException) {}
      }
    }
  }
  
  public DefaultRegistryServer(int port)
  {
    super(port);SerializerType expected = SerializerType.parse(SystemPropertyUtil.get("jupiter.registry.default.serializer_type"));serializerType = (expected == null ? SerializerType.getDefault() : expected);
    










































































































































































































































































































































































































































































































































































































































    Thread t = new Thread(new AckTimeoutScanner(null), "ack.timeout.scanner");
    t.setDaemon(true);
    t.start();
  }
  
  public DefaultRegistryServer(SocketAddress address)
  {
    super(address);SerializerType expected = SerializerType.parse(SystemPropertyUtil.get("jupiter.registry.default.serializer_type"));serializerType = (expected == null ? SerializerType.getDefault() : expected);
    






































































































































































































































































































































































































































































































































































































































    Thread t = new Thread(new AckTimeoutScanner(null), "ack.timeout.scanner");
    t.setDaemon(true);
    t.start();
  }
  
  public DefaultRegistryServer(int port, int nWorkers)
  {
    super(port, nWorkers);SerializerType expected = SerializerType.parse(SystemPropertyUtil.get("jupiter.registry.default.serializer_type"));serializerType = (expected == null ? SerializerType.getDefault() : expected);
    


































































































































































































































































































































































































































































































































































































































    Thread t = new Thread(new AckTimeoutScanner(null), "ack.timeout.scanner");
    t.setDaemon(true);
    t.start();
  }
  
  public DefaultRegistryServer(SocketAddress address, int nWorkers)
  {
    super(address, nWorkers);SerializerType expected = SerializerType.parse(SystemPropertyUtil.get("jupiter.registry.default.serializer_type"));serializerType = (expected == null ? SerializerType.getDefault() : expected);
    






























































































































































































































































































































































































































































































































































































































    Thread t = new Thread(new AckTimeoutScanner(null), "ack.timeout.scanner");
    t.setDaemon(true);
    t.start();
  }
}
