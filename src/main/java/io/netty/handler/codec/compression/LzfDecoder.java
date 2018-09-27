/*   1:    */ package io.netty.handler.codec.compression;
/*   2:    */ 
/*   3:    */ import com.ning.compress.BufferRecycler;
/*   4:    */ import com.ning.compress.lzf.ChunkDecoder;
/*   5:    */ import com.ning.compress.lzf.util.ChunkDecoderFactory;
/*   6:    */ import io.netty.buffer.ByteBuf;
/*   7:    */ import io.netty.buffer.ByteBufAllocator;
/*   8:    */ import io.netty.channel.ChannelHandlerContext;
/*   9:    */ import io.netty.handler.codec.ByteToMessageDecoder;
/*  10:    */ import java.util.List;
/*  11:    */ 
/*  12:    */ public class LzfDecoder
/*  13:    */   extends ByteToMessageDecoder
/*  14:    */ {
/*  15:    */   private static enum State
/*  16:    */   {
/*  17: 44 */     INIT_BLOCK,  INIT_ORIGINAL_LENGTH,  DECOMPRESS_DATA,  CORRUPTED;
/*  18:    */     
/*  19:    */     private State() {}
/*  20:    */   }
/*  21:    */   
/*  22: 50 */   private State currentState = State.INIT_BLOCK;
/*  23:    */   private static final short MAGIC_NUMBER = 23126;
/*  24:    */   private ChunkDecoder decoder;
/*  25:    */   private BufferRecycler recycler;
/*  26:    */   private int chunkLength;
/*  27:    */   private int originalLength;
/*  28:    */   private boolean isCompressed;
/*  29:    */   
/*  30:    */   public LzfDecoder()
/*  31:    */   {
/*  32: 90 */     this(false);
/*  33:    */   }
/*  34:    */   
/*  35:    */   public LzfDecoder(boolean safeInstance)
/*  36:    */   {
/*  37:105 */     this.decoder = (safeInstance ? ChunkDecoderFactory.safeInstance() : ChunkDecoderFactory.optimalInstance());
/*  38:    */     
/*  39:107 */     this.recycler = BufferRecycler.instance();
/*  40:    */   }
/*  41:    */   
/*  42:    */   protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/*  43:    */     throws Exception
/*  44:    */   {
/*  45:    */     try
/*  46:    */     {
/*  47:113 */       switch (1.$SwitchMap$io$netty$handler$codec$compression$LzfDecoder$State[this.currentState.ordinal()])
/*  48:    */       {
/*  49:    */       case 1: 
/*  50:115 */         if (in.readableBytes() >= 5)
/*  51:    */         {
/*  52:118 */           int magic = in.readUnsignedShort();
/*  53:119 */           if (magic != 23126) {
/*  54:120 */             throw new DecompressionException("unexpected block identifier");
/*  55:    */           }
/*  56:123 */           int type = in.readByte();
/*  57:124 */           switch (type)
/*  58:    */           {
/*  59:    */           case 0: 
/*  60:126 */             this.isCompressed = false;
/*  61:127 */             this.currentState = State.DECOMPRESS_DATA;
/*  62:128 */             break;
/*  63:    */           case 1: 
/*  64:130 */             this.isCompressed = true;
/*  65:131 */             this.currentState = State.INIT_ORIGINAL_LENGTH;
/*  66:132 */             break;
/*  67:    */           default: 
/*  68:134 */             throw new DecompressionException(String.format("unknown type of chunk: %d (expected: %d or %d)", new Object[] {
/*  69:    */             
/*  70:136 */               Integer.valueOf(type), Integer.valueOf(0), Integer.valueOf(1) }));
/*  71:    */           }
/*  72:138 */           this.chunkLength = in.readUnsignedShort();
/*  73:140 */           if (type != 1) {
/*  74:    */             break;
/*  75:    */           }
/*  76:    */         }
/*  77:    */         break;
/*  78:    */       case 2: 
/*  79:145 */         if (in.readableBytes() >= 2)
/*  80:    */         {
/*  81:148 */           this.originalLength = in.readUnsignedShort();
/*  82:    */           
/*  83:150 */           this.currentState = State.DECOMPRESS_DATA;
/*  84:    */         }
/*  85:    */         break;
/*  86:    */       case 3: 
/*  87:153 */         int chunkLength = this.chunkLength;
/*  88:154 */         if (in.readableBytes() >= chunkLength)
/*  89:    */         {
/*  90:157 */           int originalLength = this.originalLength;
/*  91:159 */           if (this.isCompressed)
/*  92:    */           {
/*  93:160 */             int idx = in.readerIndex();
/*  94:    */             int inPos;
/*  95:    */             byte[] inputArray;
/*  96:    */             int inPos;
/*  97:164 */             if (in.hasArray())
/*  98:    */             {
/*  99:165 */               byte[] inputArray = in.array();
/* 100:166 */               inPos = in.arrayOffset() + idx;
/* 101:    */             }
/* 102:    */             else
/* 103:    */             {
/* 104:168 */               inputArray = this.recycler.allocInputBuffer(chunkLength);
/* 105:169 */               in.getBytes(idx, inputArray, 0, chunkLength);
/* 106:170 */               inPos = 0;
/* 107:    */             }
/* 108:173 */             ByteBuf uncompressed = ctx.alloc().heapBuffer(originalLength, originalLength);
/* 109:174 */             byte[] outputArray = uncompressed.array();
/* 110:175 */             int outPos = uncompressed.arrayOffset() + uncompressed.writerIndex();
/* 111:    */             
/* 112:177 */             boolean success = false;
/* 113:    */             try
/* 114:    */             {
/* 115:179 */               this.decoder.decodeChunk(inputArray, inPos, outputArray, outPos, outPos + originalLength);
/* 116:180 */               uncompressed.writerIndex(uncompressed.writerIndex() + originalLength);
/* 117:181 */               out.add(uncompressed);
/* 118:182 */               in.skipBytes(chunkLength);
/* 119:183 */               success = true;
/* 120:    */             }
/* 121:    */             finally
/* 122:    */             {
/* 123:185 */               if (!success) {
/* 124:186 */                 uncompressed.release();
/* 125:    */               }
/* 126:    */             }
/* 127:190 */             if (!in.hasArray()) {
/* 128:191 */               this.recycler.releaseInputBuffer(inputArray);
/* 129:    */             }
/* 130:    */           }
/* 131:193 */           else if (chunkLength > 0)
/* 132:    */           {
/* 133:194 */             out.add(in.readRetainedSlice(chunkLength));
/* 134:    */           }
/* 135:197 */           this.currentState = State.INIT_BLOCK;
/* 136:    */         }
/* 137:198 */         break;
/* 138:    */       case 4: 
/* 139:200 */         in.skipBytes(in.readableBytes());
/* 140:201 */         break;
/* 141:    */       default: 
/* 142:203 */         throw new IllegalStateException();
/* 143:    */       }
/* 144:    */     }
/* 145:    */     catch (Exception e)
/* 146:    */     {
/* 147:206 */       this.currentState = State.CORRUPTED;
/* 148:207 */       this.decoder = null;
/* 149:208 */       this.recycler = null;
/* 150:209 */       throw e;
/* 151:    */     }
/* 152:    */   }
/* 153:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.compression.LzfDecoder
 * JD-Core Version:    0.7.0.1
 */