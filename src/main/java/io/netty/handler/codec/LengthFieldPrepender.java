/*   1:    */ package io.netty.handler.codec;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.channel.ChannelHandler.Sharable;
/*   6:    */ import io.netty.channel.ChannelHandlerContext;
/*   7:    */ import io.netty.util.internal.ObjectUtil;
/*   8:    */ import java.nio.ByteOrder;
/*   9:    */ import java.util.List;
/*  10:    */ 
/*  11:    */ @ChannelHandler.Sharable
/*  12:    */ public class LengthFieldPrepender
/*  13:    */   extends MessageToMessageEncoder<ByteBuf>
/*  14:    */ {
/*  15:    */   private final ByteOrder byteOrder;
/*  16:    */   private final int lengthFieldLength;
/*  17:    */   private final boolean lengthIncludesLengthFieldLength;
/*  18:    */   private final int lengthAdjustment;
/*  19:    */   
/*  20:    */   public LengthFieldPrepender(int lengthFieldLength)
/*  21:    */   {
/*  22: 71 */     this(lengthFieldLength, false);
/*  23:    */   }
/*  24:    */   
/*  25:    */   public LengthFieldPrepender(int lengthFieldLength, boolean lengthIncludesLengthFieldLength)
/*  26:    */   {
/*  27: 88 */     this(lengthFieldLength, 0, lengthIncludesLengthFieldLength);
/*  28:    */   }
/*  29:    */   
/*  30:    */   public LengthFieldPrepender(int lengthFieldLength, int lengthAdjustment)
/*  31:    */   {
/*  32:103 */     this(lengthFieldLength, lengthAdjustment, false);
/*  33:    */   }
/*  34:    */   
/*  35:    */   public LengthFieldPrepender(int lengthFieldLength, int lengthAdjustment, boolean lengthIncludesLengthFieldLength)
/*  36:    */   {
/*  37:122 */     this(ByteOrder.BIG_ENDIAN, lengthFieldLength, lengthAdjustment, lengthIncludesLengthFieldLength);
/*  38:    */   }
/*  39:    */   
/*  40:    */   public LengthFieldPrepender(ByteOrder byteOrder, int lengthFieldLength, int lengthAdjustment, boolean lengthIncludesLengthFieldLength)
/*  41:    */   {
/*  42:144 */     if ((lengthFieldLength != 1) && (lengthFieldLength != 2) && (lengthFieldLength != 3) && (lengthFieldLength != 4) && (lengthFieldLength != 8)) {
/*  43:147 */       throw new IllegalArgumentException("lengthFieldLength must be either 1, 2, 3, 4, or 8: " + lengthFieldLength);
/*  44:    */     }
/*  45:151 */     ObjectUtil.checkNotNull(byteOrder, "byteOrder");
/*  46:    */     
/*  47:153 */     this.byteOrder = byteOrder;
/*  48:154 */     this.lengthFieldLength = lengthFieldLength;
/*  49:155 */     this.lengthIncludesLengthFieldLength = lengthIncludesLengthFieldLength;
/*  50:156 */     this.lengthAdjustment = lengthAdjustment;
/*  51:    */   }
/*  52:    */   
/*  53:    */   protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out)
/*  54:    */     throws Exception
/*  55:    */   {
/*  56:161 */     int length = msg.readableBytes() + this.lengthAdjustment;
/*  57:162 */     if (this.lengthIncludesLengthFieldLength) {
/*  58:163 */       length += this.lengthFieldLength;
/*  59:    */     }
/*  60:166 */     if (length < 0) {
/*  61:167 */       throw new IllegalArgumentException("Adjusted frame length (" + length + ") is less than zero");
/*  62:    */     }
/*  63:171 */     switch (this.lengthFieldLength)
/*  64:    */     {
/*  65:    */     case 1: 
/*  66:173 */       if (length >= 256) {
/*  67:174 */         throw new IllegalArgumentException("length does not fit into a byte: " + length);
/*  68:    */       }
/*  69:177 */       out.add(ctx.alloc().buffer(1).order(this.byteOrder).writeByte((byte)length));
/*  70:178 */       break;
/*  71:    */     case 2: 
/*  72:180 */       if (length >= 65536) {
/*  73:181 */         throw new IllegalArgumentException("length does not fit into a short integer: " + length);
/*  74:    */       }
/*  75:184 */       out.add(ctx.alloc().buffer(2).order(this.byteOrder).writeShort((short)length));
/*  76:185 */       break;
/*  77:    */     case 3: 
/*  78:187 */       if (length >= 16777216) {
/*  79:188 */         throw new IllegalArgumentException("length does not fit into a medium integer: " + length);
/*  80:    */       }
/*  81:191 */       out.add(ctx.alloc().buffer(3).order(this.byteOrder).writeMedium(length));
/*  82:192 */       break;
/*  83:    */     case 4: 
/*  84:194 */       out.add(ctx.alloc().buffer(4).order(this.byteOrder).writeInt(length));
/*  85:195 */       break;
/*  86:    */     case 8: 
/*  87:197 */       out.add(ctx.alloc().buffer(8).order(this.byteOrder).writeLong(length));
/*  88:198 */       break;
/*  89:    */     case 5: 
/*  90:    */     case 6: 
/*  91:    */     case 7: 
/*  92:    */     default: 
/*  93:200 */       throw new Error("should not reach here");
/*  94:    */     }
/*  95:202 */     out.add(msg.retain());
/*  96:    */   }
/*  97:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.LengthFieldPrepender
 * JD-Core Version:    0.7.0.1
 */