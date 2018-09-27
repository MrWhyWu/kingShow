/*   1:    */ package io.netty.handler.codec.compression;
/*   2:    */ 
/*   3:    */ import com.ning.compress.BufferRecycler;
/*   4:    */ import com.ning.compress.lzf.ChunkEncoder;
/*   5:    */ import com.ning.compress.lzf.LZFEncoder;
/*   6:    */ import com.ning.compress.lzf.util.ChunkEncoderFactory;
/*   7:    */ import io.netty.buffer.ByteBuf;
/*   8:    */ import io.netty.channel.ChannelHandlerContext;
/*   9:    */ import io.netty.handler.codec.MessageToByteEncoder;
/*  10:    */ 
/*  11:    */ public class LzfEncoder
/*  12:    */   extends MessageToByteEncoder<ByteBuf>
/*  13:    */ {
/*  14:    */   private static final int MIN_BLOCK_TO_COMPRESS = 16;
/*  15:    */   private final ChunkEncoder encoder;
/*  16:    */   private final BufferRecycler recycler;
/*  17:    */   
/*  18:    */   public LzfEncoder()
/*  19:    */   {
/*  20: 58 */     this(false, 65535);
/*  21:    */   }
/*  22:    */   
/*  23:    */   public LzfEncoder(boolean safeInstance)
/*  24:    */   {
/*  25: 71 */     this(safeInstance, 65535);
/*  26:    */   }
/*  27:    */   
/*  28:    */   public LzfEncoder(int totalLength)
/*  29:    */   {
/*  30: 83 */     this(false, totalLength);
/*  31:    */   }
/*  32:    */   
/*  33:    */   public LzfEncoder(boolean safeInstance, int totalLength)
/*  34:    */   {
/*  35: 99 */     super(false);
/*  36:100 */     if ((totalLength < 16) || (totalLength > 65535)) {
/*  37:101 */       throw new IllegalArgumentException("totalLength: " + totalLength + " (expected: " + 16 + '-' + 65535 + ')');
/*  38:    */     }
/*  39:107 */     this.encoder = (safeInstance ? ChunkEncoderFactory.safeNonAllocatingInstance(totalLength) : ChunkEncoderFactory.optimalNonAllocatingInstance(totalLength));
/*  40:    */     
/*  41:109 */     this.recycler = BufferRecycler.instance();
/*  42:    */   }
/*  43:    */   
/*  44:    */   protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out)
/*  45:    */     throws Exception
/*  46:    */   {
/*  47:114 */     int length = in.readableBytes();
/*  48:115 */     int idx = in.readerIndex();
/*  49:    */     int inputPtr;
/*  50:    */     byte[] input;
/*  51:    */     int inputPtr;
/*  52:118 */     if (in.hasArray())
/*  53:    */     {
/*  54:119 */       byte[] input = in.array();
/*  55:120 */       inputPtr = in.arrayOffset() + idx;
/*  56:    */     }
/*  57:    */     else
/*  58:    */     {
/*  59:122 */       input = this.recycler.allocInputBuffer(length);
/*  60:123 */       in.getBytes(idx, input, 0, length);
/*  61:124 */       inputPtr = 0;
/*  62:    */     }
/*  63:127 */     int maxOutputLength = LZFEncoder.estimateMaxWorkspaceSize(length);
/*  64:128 */     out.ensureWritable(maxOutputLength);
/*  65:129 */     byte[] output = out.array();
/*  66:130 */     int outputPtr = out.arrayOffset() + out.writerIndex();
/*  67:131 */     int outputLength = LZFEncoder.appendEncoded(this.encoder, input, inputPtr, length, output, outputPtr) - outputPtr;
/*  68:    */     
/*  69:133 */     out.writerIndex(out.writerIndex() + outputLength);
/*  70:134 */     in.skipBytes(length);
/*  71:136 */     if (!in.hasArray()) {
/*  72:137 */       this.recycler.releaseInputBuffer(input);
/*  73:    */     }
/*  74:    */   }
/*  75:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.compression.LzfEncoder
 * JD-Core Version:    0.7.0.1
 */