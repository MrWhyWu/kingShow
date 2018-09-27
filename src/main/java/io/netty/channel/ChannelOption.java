/*   1:    */ package io.netty.channel;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.util.AbstractConstant;
/*   5:    */ import io.netty.util.ConstantPool;
/*   6:    */ import java.net.InetAddress;
/*   7:    */ import java.net.NetworkInterface;
/*   8:    */ 
/*   9:    */ public class ChannelOption<T>
/*  10:    */   extends AbstractConstant<ChannelOption<T>>
/*  11:    */ {
/*  12: 35 */   private static final ConstantPool<ChannelOption<Object>> pool = new ConstantPool()
/*  13:    */   {
/*  14:    */     protected ChannelOption<Object> newConstant(int id, String name)
/*  15:    */     {
/*  16: 38 */       return new ChannelOption(id, name, null);
/*  17:    */     }
/*  18:    */   };
/*  19:    */   
/*  20:    */   public static <T> ChannelOption<T> valueOf(String name)
/*  21:    */   {
/*  22: 47 */     return (ChannelOption)pool.valueOf(name);
/*  23:    */   }
/*  24:    */   
/*  25:    */   public static <T> ChannelOption<T> valueOf(Class<?> firstNameComponent, String secondNameComponent)
/*  26:    */   {
/*  27: 55 */     return (ChannelOption)pool.valueOf(firstNameComponent, secondNameComponent);
/*  28:    */   }
/*  29:    */   
/*  30:    */   public static boolean exists(String name)
/*  31:    */   {
/*  32: 62 */     return pool.exists(name);
/*  33:    */   }
/*  34:    */   
/*  35:    */   public static <T> ChannelOption<T> newInstance(String name)
/*  36:    */   {
/*  37: 71 */     return (ChannelOption)pool.newInstance(name);
/*  38:    */   }
/*  39:    */   
/*  40: 74 */   public static final ChannelOption<ByteBufAllocator> ALLOCATOR = valueOf("ALLOCATOR");
/*  41: 75 */   public static final ChannelOption<RecvByteBufAllocator> RCVBUF_ALLOCATOR = valueOf("RCVBUF_ALLOCATOR");
/*  42: 76 */   public static final ChannelOption<MessageSizeEstimator> MESSAGE_SIZE_ESTIMATOR = valueOf("MESSAGE_SIZE_ESTIMATOR");
/*  43: 78 */   public static final ChannelOption<Integer> CONNECT_TIMEOUT_MILLIS = valueOf("CONNECT_TIMEOUT_MILLIS");
/*  44:    */   @Deprecated
/*  45: 83 */   public static final ChannelOption<Integer> MAX_MESSAGES_PER_READ = valueOf("MAX_MESSAGES_PER_READ");
/*  46: 84 */   public static final ChannelOption<Integer> WRITE_SPIN_COUNT = valueOf("WRITE_SPIN_COUNT");
/*  47:    */   @Deprecated
/*  48: 89 */   public static final ChannelOption<Integer> WRITE_BUFFER_HIGH_WATER_MARK = valueOf("WRITE_BUFFER_HIGH_WATER_MARK");
/*  49:    */   @Deprecated
/*  50: 94 */   public static final ChannelOption<Integer> WRITE_BUFFER_LOW_WATER_MARK = valueOf("WRITE_BUFFER_LOW_WATER_MARK");
/*  51: 96 */   public static final ChannelOption<WriteBufferWaterMark> WRITE_BUFFER_WATER_MARK = valueOf("WRITE_BUFFER_WATER_MARK");
/*  52: 98 */   public static final ChannelOption<Boolean> ALLOW_HALF_CLOSURE = valueOf("ALLOW_HALF_CLOSURE");
/*  53: 99 */   public static final ChannelOption<Boolean> AUTO_READ = valueOf("AUTO_READ");
/*  54:    */   @Deprecated
/*  55:108 */   public static final ChannelOption<Boolean> AUTO_CLOSE = valueOf("AUTO_CLOSE");
/*  56:110 */   public static final ChannelOption<Boolean> SO_BROADCAST = valueOf("SO_BROADCAST");
/*  57:111 */   public static final ChannelOption<Boolean> SO_KEEPALIVE = valueOf("SO_KEEPALIVE");
/*  58:112 */   public static final ChannelOption<Integer> SO_SNDBUF = valueOf("SO_SNDBUF");
/*  59:113 */   public static final ChannelOption<Integer> SO_RCVBUF = valueOf("SO_RCVBUF");
/*  60:114 */   public static final ChannelOption<Boolean> SO_REUSEADDR = valueOf("SO_REUSEADDR");
/*  61:115 */   public static final ChannelOption<Integer> SO_LINGER = valueOf("SO_LINGER");
/*  62:116 */   public static final ChannelOption<Integer> SO_BACKLOG = valueOf("SO_BACKLOG");
/*  63:117 */   public static final ChannelOption<Integer> SO_TIMEOUT = valueOf("SO_TIMEOUT");
/*  64:119 */   public static final ChannelOption<Integer> IP_TOS = valueOf("IP_TOS");
/*  65:120 */   public static final ChannelOption<InetAddress> IP_MULTICAST_ADDR = valueOf("IP_MULTICAST_ADDR");
/*  66:121 */   public static final ChannelOption<NetworkInterface> IP_MULTICAST_IF = valueOf("IP_MULTICAST_IF");
/*  67:122 */   public static final ChannelOption<Integer> IP_MULTICAST_TTL = valueOf("IP_MULTICAST_TTL");
/*  68:123 */   public static final ChannelOption<Boolean> IP_MULTICAST_LOOP_DISABLED = valueOf("IP_MULTICAST_LOOP_DISABLED");
/*  69:125 */   public static final ChannelOption<Boolean> TCP_NODELAY = valueOf("TCP_NODELAY");
/*  70:    */   @Deprecated
/*  71:129 */   public static final ChannelOption<Boolean> DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION = valueOf("DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION");
/*  72:132 */   public static final ChannelOption<Boolean> SINGLE_EVENTEXECUTOR_PER_GROUP = valueOf("SINGLE_EVENTEXECUTOR_PER_GROUP");
/*  73:    */   
/*  74:    */   private ChannelOption(int id, String name)
/*  75:    */   {
/*  76:138 */     super(id, name);
/*  77:    */   }
/*  78:    */   
/*  79:    */   @Deprecated
/*  80:    */   protected ChannelOption(String name)
/*  81:    */   {
/*  82:143 */     this(pool.nextId(), name);
/*  83:    */   }
/*  84:    */   
/*  85:    */   public void validate(T value)
/*  86:    */   {
/*  87:151 */     if (value == null) {
/*  88:152 */       throw new NullPointerException("value");
/*  89:    */     }
/*  90:    */   }
/*  91:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.ChannelOption
 * JD-Core Version:    0.7.0.1
 */