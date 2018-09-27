/*   1:    */ package io.opentracing.tag;
/*   2:    */ 
/*   3:    */ public final class Tags
/*   4:    */ {
/*   5:    */   public static final String SPAN_KIND_SERVER = "server";
/*   6:    */   public static final String SPAN_KIND_CLIENT = "client";
/*   7:    */   public static final String SPAN_KIND_PRODUCER = "producer";
/*   8:    */   public static final String SPAN_KIND_CONSUMER = "consumer";
/*   9: 51 */   public static final StringTag HTTP_URL = new StringTag("http.url");
/*  10: 56 */   public static final IntTag HTTP_STATUS = new IntTag("http.status_code");
/*  11: 61 */   public static final StringTag HTTP_METHOD = new StringTag("http.method");
/*  12: 66 */   public static final IntOrStringTag PEER_HOST_IPV4 = new IntOrStringTag("peer.ipv4");
/*  13: 71 */   public static final StringTag PEER_HOST_IPV6 = new StringTag("peer.ipv6");
/*  14: 76 */   public static final StringTag PEER_SERVICE = new StringTag("peer.service");
/*  15: 81 */   public static final StringTag PEER_HOSTNAME = new StringTag("peer.hostname");
/*  16: 86 */   public static final IntTag PEER_PORT = new IntTag("peer.port");
/*  17: 91 */   public static final IntTag SAMPLING_PRIORITY = new IntTag("sampling.priority");
/*  18: 96 */   public static final StringTag SPAN_KIND = new StringTag("span.kind");
/*  19:101 */   public static final StringTag COMPONENT = new StringTag("component");
/*  20:106 */   public static final BooleanTag ERROR = new BooleanTag("error");
/*  21:112 */   public static final StringTag DB_TYPE = new StringTag("db.type");
/*  22:118 */   public static final StringTag DB_INSTANCE = new StringTag("db.instance");
/*  23:123 */   public static final StringTag DB_USER = new StringTag("db.user");
/*  24:129 */   public static final StringTag DB_STATEMENT = new StringTag("db.statement");
/*  25:136 */   public static final StringTag MESSAGE_BUS_DESTINATION = new StringTag("message_bus.destination");
/*  26:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.opentracing.tag.Tags
 * JD-Core Version:    0.7.0.1
 */