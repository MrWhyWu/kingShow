/*   1:    */ package io.netty.handler.codec.compression;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.buffer.ByteBufInputStream;
/*   6:    */ import io.netty.buffer.ByteBufOutputStream;
/*   7:    */ import io.netty.channel.ChannelHandlerContext;
/*   8:    */ import io.netty.handler.codec.MessageToByteEncoder;
/*   9:    */ import io.netty.util.internal.logging.InternalLogger;
/*  10:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  11:    */ import java.io.InputStream;
/*  12:    */ import lzma.sdk.lzma.Encoder;
/*  13:    */ 
/*  14:    */ public class LzmaFrameEncoder
/*  15:    */   extends MessageToByteEncoder<ByteBuf>
/*  16:    */ {
/*  17: 41 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(LzmaFrameEncoder.class);
/*  18:    */   private static final int MEDIUM_DICTIONARY_SIZE = 65536;
/*  19:    */   private static final int MIN_FAST_BYTES = 5;
/*  20:    */   private static final int MEDIUM_FAST_BYTES = 32;
/*  21:    */   private static final int MAX_FAST_BYTES = 273;
/*  22:    */   private static final int DEFAULT_MATCH_FINDER = 1;
/*  23:    */   private static final int DEFAULT_LC = 3;
/*  24:    */   private static final int DEFAULT_LP = 0;
/*  25:    */   private static final int DEFAULT_PB = 2;
/*  26:    */   private final Encoder encoder;
/*  27:    */   private final byte properties;
/*  28:    */   private final int littleEndianDictionarySize;
/*  29:    */   private static boolean warningLogged;
/*  30:    */   
/*  31:    */   public LzmaFrameEncoder()
/*  32:    */   {
/*  33: 88 */     this(65536);
/*  34:    */   }
/*  35:    */   
/*  36:    */   public LzmaFrameEncoder(int lc, int lp, int pb)
/*  37:    */   {
/*  38: 96 */     this(lc, lp, pb, 65536);
/*  39:    */   }
/*  40:    */   
/*  41:    */   public LzmaFrameEncoder(int dictionarySize)
/*  42:    */   {
/*  43:106 */     this(3, 0, 2, dictionarySize);
/*  44:    */   }
/*  45:    */   
/*  46:    */   public LzmaFrameEncoder(int lc, int lp, int pb, int dictionarySize)
/*  47:    */   {
/*  48:113 */     this(lc, lp, pb, dictionarySize, false, 32);
/*  49:    */   }
/*  50:    */   
/*  51:    */   public LzmaFrameEncoder(int lc, int lp, int pb, int dictionarySize, boolean endMarkerMode, int numFastBytes)
/*  52:    */   {
/*  53:138 */     if ((lc < 0) || (lc > 8)) {
/*  54:139 */       throw new IllegalArgumentException("lc: " + lc + " (expected: 0-8)");
/*  55:    */     }
/*  56:141 */     if ((lp < 0) || (lp > 4)) {
/*  57:142 */       throw new IllegalArgumentException("lp: " + lp + " (expected: 0-4)");
/*  58:    */     }
/*  59:144 */     if ((pb < 0) || (pb > 4)) {
/*  60:145 */       throw new IllegalArgumentException("pb: " + pb + " (expected: 0-4)");
/*  61:    */     }
/*  62:147 */     if ((lc + lp > 4) && 
/*  63:148 */       (!warningLogged))
/*  64:    */     {
/*  65:149 */       logger.warn("The latest versions of LZMA libraries (for example, XZ Utils) has an additional requirement: lc + lp <= 4. Data which don't follow this requirement cannot be decompressed with this libraries.");
/*  66:    */       
/*  67:    */ 
/*  68:152 */       warningLogged = true;
/*  69:    */     }
/*  70:155 */     if (dictionarySize < 0) {
/*  71:156 */       throw new IllegalArgumentException("dictionarySize: " + dictionarySize + " (expected: 0+)");
/*  72:    */     }
/*  73:158 */     if ((numFastBytes < 5) || (numFastBytes > 273)) {
/*  74:159 */       throw new IllegalArgumentException(String.format("numFastBytes: %d (expected: %d-%d)", new Object[] {
/*  75:160 */         Integer.valueOf(numFastBytes), Integer.valueOf(5), Integer.valueOf(273) }));
/*  76:    */     }
/*  77:164 */     this.encoder = new Encoder();
/*  78:165 */     this.encoder.setDictionarySize(dictionarySize);
/*  79:166 */     this.encoder.setEndMarkerMode(endMarkerMode);
/*  80:167 */     this.encoder.setMatchFinder(1);
/*  81:168 */     this.encoder.setNumFastBytes(numFastBytes);
/*  82:169 */     this.encoder.setLcLpPb(lc, lp, pb);
/*  83:    */     
/*  84:171 */     this.properties = ((byte)((pb * 5 + lp) * 9 + lc));
/*  85:172 */     this.littleEndianDictionarySize = Integer.reverseBytes(dictionarySize);
/*  86:    */   }
/*  87:    */   
/*  88:    */   protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out)
/*  89:    */     throws Exception
/*  90:    */   {
/*  91:177 */     int length = in.readableBytes();
/*  92:178 */     InputStream bbIn = null;
/*  93:179 */     ByteBufOutputStream bbOut = null;
/*  94:    */     try
/*  95:    */     {
/*  96:181 */       bbIn = new ByteBufInputStream(in);
/*  97:182 */       bbOut = new ByteBufOutputStream(out);
/*  98:183 */       bbOut.writeByte(this.properties);
/*  99:184 */       bbOut.writeInt(this.littleEndianDictionarySize);
/* 100:185 */       bbOut.writeLong(Long.reverseBytes(length));
/* 101:186 */       this.encoder.code(bbIn, bbOut, -1L, -1L, null);
/* 102:    */     }
/* 103:    */     finally
/* 104:    */     {
/* 105:188 */       if (bbIn != null) {
/* 106:189 */         bbIn.close();
/* 107:    */       }
/* 108:191 */       if (bbOut != null) {
/* 109:192 */         bbOut.close();
/* 110:    */       }
/* 111:    */     }
/* 112:    */   }
/* 113:    */   
/* 114:    */   protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, ByteBuf in, boolean preferDirect)
/* 115:    */     throws Exception
/* 116:    */   {
/* 117:199 */     int length = in.readableBytes();
/* 118:200 */     int maxOutputLength = maxOutputBufferLength(length);
/* 119:201 */     return ctx.alloc().ioBuffer(maxOutputLength);
/* 120:    */   }
/* 121:    */   
/* 122:    */   private static int maxOutputBufferLength(int inputLength)
/* 123:    */   {
/* 124:    */     double factor;
/* 125:    */     double factor;
/* 126:209 */     if (inputLength < 200)
/* 127:    */     {
/* 128:210 */       factor = 1.5D;
/* 129:    */     }
/* 130:    */     else
/* 131:    */     {
/* 132:    */       double factor;
/* 133:211 */       if (inputLength < 500)
/* 134:    */       {
/* 135:212 */         factor = 1.2D;
/* 136:    */       }
/* 137:    */       else
/* 138:    */       {
/* 139:    */         double factor;
/* 140:213 */         if (inputLength < 1000)
/* 141:    */         {
/* 142:214 */           factor = 1.1D;
/* 143:    */         }
/* 144:    */         else
/* 145:    */         {
/* 146:    */           double factor;
/* 147:215 */           if (inputLength < 10000) {
/* 148:216 */             factor = 1.05D;
/* 149:    */           } else {
/* 150:218 */             factor = 1.02D;
/* 151:    */           }
/* 152:    */         }
/* 153:    */       }
/* 154:    */     }
/* 155:220 */     return 13 + (int)(inputLength * factor);
/* 156:    */   }
/* 157:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.compression.LzmaFrameEncoder
 * JD-Core Version:    0.7.0.1
 */