/*   1:    */ package io.netty.handler.codec.protobuf;
/*   2:    */ 
/*   3:    */ import com.google.protobuf.ExtensionRegistry;
/*   4:    */ import com.google.protobuf.ExtensionRegistryLite;
/*   5:    */ import com.google.protobuf.MessageLite;
/*   6:    */ import com.google.protobuf.MessageLite.Builder;
/*   7:    */ import com.google.protobuf.Parser;
/*   8:    */ import io.netty.buffer.ByteBuf;
/*   9:    */ import io.netty.channel.ChannelHandler.Sharable;
/*  10:    */ import io.netty.channel.ChannelHandlerContext;
/*  11:    */ import io.netty.handler.codec.MessageToMessageDecoder;
/*  12:    */ import java.util.List;
/*  13:    */ 
/*  14:    */ @ChannelHandler.Sharable
/*  15:    */ public class ProtobufDecoder
/*  16:    */   extends MessageToMessageDecoder<ByteBuf>
/*  17:    */ {
/*  18:    */   private static final boolean HAS_PARSER;
/*  19:    */   private final MessageLite prototype;
/*  20:    */   private final ExtensionRegistryLite extensionRegistry;
/*  21:    */   
/*  22:    */   static
/*  23:    */   {
/*  24: 70 */     boolean hasParser = false;
/*  25:    */     try
/*  26:    */     {
/*  27: 73 */       MessageLite.class.getDeclaredMethod("getParserForType", new Class[0]);
/*  28: 74 */       hasParser = true;
/*  29:    */     }
/*  30:    */     catch (Throwable localThrowable) {}
/*  31: 79 */     HAS_PARSER = hasParser;
/*  32:    */   }
/*  33:    */   
/*  34:    */   public ProtobufDecoder(MessageLite prototype)
/*  35:    */   {
/*  36: 89 */     this(prototype, null);
/*  37:    */   }
/*  38:    */   
/*  39:    */   public ProtobufDecoder(MessageLite prototype, ExtensionRegistry extensionRegistry)
/*  40:    */   {
/*  41: 93 */     this(prototype, extensionRegistry);
/*  42:    */   }
/*  43:    */   
/*  44:    */   public ProtobufDecoder(MessageLite prototype, ExtensionRegistryLite extensionRegistry)
/*  45:    */   {
/*  46: 97 */     if (prototype == null) {
/*  47: 98 */       throw new NullPointerException("prototype");
/*  48:    */     }
/*  49:100 */     this.prototype = prototype.getDefaultInstanceForType();
/*  50:101 */     this.extensionRegistry = extensionRegistry;
/*  51:    */   }
/*  52:    */   
/*  53:    */   protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out)
/*  54:    */     throws Exception
/*  55:    */   {
/*  56:109 */     int length = msg.readableBytes();
/*  57:    */     int offset;
/*  58:    */     byte[] array;
/*  59:    */     int offset;
/*  60:110 */     if (msg.hasArray())
/*  61:    */     {
/*  62:111 */       byte[] array = msg.array();
/*  63:112 */       offset = msg.arrayOffset() + msg.readerIndex();
/*  64:    */     }
/*  65:    */     else
/*  66:    */     {
/*  67:114 */       array = new byte[length];
/*  68:115 */       msg.getBytes(msg.readerIndex(), array, 0, length);
/*  69:116 */       offset = 0;
/*  70:    */     }
/*  71:119 */     if (this.extensionRegistry == null)
/*  72:    */     {
/*  73:120 */       if (HAS_PARSER) {
/*  74:121 */         out.add(this.prototype.getParserForType().parseFrom(array, offset, length));
/*  75:    */       } else {
/*  76:123 */         out.add(this.prototype.newBuilderForType().mergeFrom(array, offset, length).build());
/*  77:    */       }
/*  78:    */     }
/*  79:126 */     else if (HAS_PARSER) {
/*  80:127 */       out.add(this.prototype.getParserForType().parseFrom(array, offset, length, this.extensionRegistry));
/*  81:    */     } else {
/*  82:130 */       out.add(this.prototype.newBuilderForType().mergeFrom(array, offset, length, this.extensionRegistry)
/*  83:131 */         .build());
/*  84:    */     }
/*  85:    */   }
/*  86:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.protobuf.ProtobufDecoder
 * JD-Core Version:    0.7.0.1
 */