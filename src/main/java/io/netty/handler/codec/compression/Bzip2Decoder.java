/*   1:    */ package io.netty.handler.codec.compression;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.channel.ChannelHandlerContext;
/*   6:    */ import io.netty.handler.codec.ByteToMessageDecoder;
/*   7:    */ import java.util.List;
/*   8:    */ 
/*   9:    */ public class Bzip2Decoder
/*  10:    */   extends ByteToMessageDecoder
/*  11:    */ {
/*  12:    */   private static enum State
/*  13:    */   {
/*  14: 36 */     INIT,  INIT_BLOCK,  INIT_BLOCK_PARAMS,  RECEIVE_HUFFMAN_USED_MAP,  RECEIVE_HUFFMAN_USED_BITMAPS,  RECEIVE_SELECTORS_NUMBER,  RECEIVE_SELECTORS,  RECEIVE_HUFFMAN_LENGTH,  DECODE_HUFFMAN_DATA,  EOF;
/*  15:    */     
/*  16:    */     private State() {}
/*  17:    */   }
/*  18:    */   
/*  19: 47 */   private State currentState = State.INIT;
/*  20: 52 */   private final Bzip2BitReader reader = new Bzip2BitReader();
/*  21:    */   private Bzip2BlockDecompressor blockDecompressor;
/*  22:    */   private Bzip2HuffmanStageDecoder huffmanStageDecoder;
/*  23:    */   private int blockSize;
/*  24:    */   private int blockCRC;
/*  25:    */   private int streamCRC;
/*  26:    */   
/*  27:    */   protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/*  28:    */     throws Exception
/*  29:    */   {
/*  30: 81 */     if (!in.isReadable()) {
/*  31: 82 */       return;
/*  32:    */     }
/*  33: 85 */     Bzip2BitReader reader = this.reader;
/*  34: 86 */     reader.setByteBuf(in);
/*  35:    */     for (;;)
/*  36:    */     {
/*  37: 89 */       switch (1.$SwitchMap$io$netty$handler$codec$compression$Bzip2Decoder$State[this.currentState.ordinal()])
/*  38:    */       {
/*  39:    */       case 1: 
/*  40: 91 */         if (in.readableBytes() < 4) {
/*  41: 92 */           return;
/*  42:    */         }
/*  43: 94 */         int magicNumber = in.readUnsignedMedium();
/*  44: 95 */         if (magicNumber != 4348520) {
/*  45: 96 */           throw new DecompressionException("Unexpected stream identifier contents. Mismatched bzip2 protocol version?");
/*  46:    */         }
/*  47: 99 */         int blockSize = in.readByte() - 48;
/*  48:100 */         if ((blockSize < 1) || (blockSize > 9)) {
/*  49:101 */           throw new DecompressionException("block size is invalid");
/*  50:    */         }
/*  51:103 */         this.blockSize = (blockSize * 100000);
/*  52:    */         
/*  53:105 */         this.streamCRC = 0;
/*  54:106 */         this.currentState = State.INIT_BLOCK;
/*  55:    */       case 2: 
/*  56:109 */         if (!reader.hasReadableBytes(10)) {
/*  57:110 */           return;
/*  58:    */         }
/*  59:113 */         int magic1 = reader.readBits(24);
/*  60:114 */         int magic2 = reader.readBits(24);
/*  61:115 */         if ((magic1 == 1536581) && (magic2 == 3690640))
/*  62:    */         {
/*  63:117 */           int storedCombinedCRC = reader.readInt();
/*  64:118 */           if (storedCombinedCRC != this.streamCRC) {
/*  65:119 */             throw new DecompressionException("stream CRC error");
/*  66:    */           }
/*  67:121 */           this.currentState = State.EOF;
/*  68:    */         }
/*  69:    */         else
/*  70:    */         {
/*  71:124 */           if ((magic1 != 3227993) || (magic2 != 2511705)) {
/*  72:125 */             throw new DecompressionException("bad block header");
/*  73:    */           }
/*  74:127 */           this.blockCRC = reader.readInt();
/*  75:128 */           this.currentState = State.INIT_BLOCK_PARAMS;
/*  76:    */         }
/*  77:    */         break;
/*  78:    */       case 3: 
/*  79:131 */         if (!reader.hasReadableBits(25)) {
/*  80:132 */           return;
/*  81:    */         }
/*  82:134 */         boolean blockRandomised = reader.readBoolean();
/*  83:135 */         int bwtStartPointer = reader.readBits(24);
/*  84:    */         
/*  85:137 */         this.blockDecompressor = new Bzip2BlockDecompressor(this.blockSize, this.blockCRC, blockRandomised, bwtStartPointer, reader);
/*  86:    */         
/*  87:139 */         this.currentState = State.RECEIVE_HUFFMAN_USED_MAP;
/*  88:    */       case 4: 
/*  89:142 */         if (!reader.hasReadableBits(16)) {
/*  90:143 */           return;
/*  91:    */         }
/*  92:145 */         this.blockDecompressor.huffmanInUse16 = reader.readBits(16);
/*  93:146 */         this.currentState = State.RECEIVE_HUFFMAN_USED_BITMAPS;
/*  94:    */       case 5: 
/*  95:149 */         Bzip2BlockDecompressor blockDecompressor = this.blockDecompressor;
/*  96:150 */         int inUse16 = blockDecompressor.huffmanInUse16;
/*  97:151 */         int bitNumber = Integer.bitCount(inUse16);
/*  98:152 */         byte[] huffmanSymbolMap = blockDecompressor.huffmanSymbolMap;
/*  99:154 */         if (!reader.hasReadableBits(bitNumber * 16 + 3)) {
/* 100:155 */           return;
/* 101:    */         }
/* 102:158 */         int huffmanSymbolCount = 0;
/* 103:159 */         if (bitNumber > 0) {
/* 104:160 */           for (int i = 0; i < 16; i++) {
/* 105:161 */             if ((inUse16 & 32768 >>> i) != 0)
/* 106:    */             {
/* 107:162 */               int j = 0;
/* 108:162 */               for (int k = i << 4; j < 16; k++)
/* 109:    */               {
/* 110:163 */                 if (reader.readBoolean()) {
/* 111:164 */                   huffmanSymbolMap[(huffmanSymbolCount++)] = ((byte)k);
/* 112:    */                 }
/* 113:162 */                 j++;
/* 114:    */               }
/* 115:    */             }
/* 116:    */           }
/* 117:    */         }
/* 118:170 */         blockDecompressor.huffmanEndOfBlockSymbol = (huffmanSymbolCount + 1);
/* 119:    */         
/* 120:172 */         int totalTables = reader.readBits(3);
/* 121:173 */         if ((totalTables < 2) || (totalTables > 6)) {
/* 122:174 */           throw new DecompressionException("incorrect huffman groups number");
/* 123:    */         }
/* 124:176 */         int alphaSize = huffmanSymbolCount + 2;
/* 125:177 */         if (alphaSize > 258) {
/* 126:178 */           throw new DecompressionException("incorrect alphabet size");
/* 127:    */         }
/* 128:180 */         this.huffmanStageDecoder = new Bzip2HuffmanStageDecoder(reader, totalTables, alphaSize);
/* 129:181 */         this.currentState = State.RECEIVE_SELECTORS_NUMBER;
/* 130:    */       case 6: 
/* 131:184 */         if (!reader.hasReadableBits(15)) {
/* 132:185 */           return;
/* 133:    */         }
/* 134:187 */         int totalSelectors = reader.readBits(15);
/* 135:188 */         if ((totalSelectors < 1) || (totalSelectors > 18002)) {
/* 136:189 */           throw new DecompressionException("incorrect selectors number");
/* 137:    */         }
/* 138:191 */         this.huffmanStageDecoder.selectors = new byte[totalSelectors];
/* 139:    */         
/* 140:193 */         this.currentState = State.RECEIVE_SELECTORS;
/* 141:    */       case 7: 
/* 142:196 */         Bzip2HuffmanStageDecoder huffmanStageDecoder = this.huffmanStageDecoder;
/* 143:197 */         byte[] selectors = huffmanStageDecoder.selectors;
/* 144:198 */         int totalSelectors = selectors.length;
/* 145:199 */         Bzip2MoveToFrontTable tableMtf = huffmanStageDecoder.tableMTF;
/* 146:203 */         for (int currSelector = huffmanStageDecoder.currentSelector; currSelector < totalSelectors; currSelector++)
/* 147:    */         {
/* 148:205 */           if (!reader.hasReadableBits(6))
/* 149:    */           {
/* 150:207 */             huffmanStageDecoder.currentSelector = currSelector;
/* 151:208 */             return;
/* 152:    */           }
/* 153:210 */           int index = 0;
/* 154:211 */           while (reader.readBoolean()) {
/* 155:212 */             index++;
/* 156:    */           }
/* 157:214 */           selectors[currSelector] = tableMtf.indexToFront(index);
/* 158:    */         }
/* 159:217 */         this.currentState = State.RECEIVE_HUFFMAN_LENGTH;
/* 160:    */       case 8: 
/* 161:220 */         Bzip2HuffmanStageDecoder huffmanStageDecoder = this.huffmanStageDecoder;
/* 162:221 */         int totalTables = huffmanStageDecoder.totalTables;
/* 163:222 */         byte[][] codeLength = huffmanStageDecoder.tableCodeLengths;
/* 164:223 */         int alphaSize = huffmanStageDecoder.alphabetSize;
/* 165:    */         
/* 166:    */ 
/* 167:    */ 
/* 168:227 */         int currLength = huffmanStageDecoder.currentLength;
/* 169:228 */         int currAlpha = 0;
/* 170:229 */         boolean modifyLength = huffmanStageDecoder.modifyLength;
/* 171:230 */         boolean saveStateAndReturn = false;
/* 172:231 */         for (int currGroup = huffmanStageDecoder.currentGroup; currGroup < totalTables; currGroup++)
/* 173:    */         {
/* 174:233 */           if (!reader.hasReadableBits(5))
/* 175:    */           {
/* 176:234 */             saveStateAndReturn = true;
/* 177:235 */             break;
/* 178:    */           }
/* 179:237 */           if (currLength < 0) {
/* 180:238 */             currLength = reader.readBits(5);
/* 181:    */           }
/* 182:240 */           for (currAlpha = huffmanStageDecoder.currentAlpha; currAlpha < alphaSize; currAlpha++)
/* 183:    */           {
/* 184:242 */             if (!reader.isReadable())
/* 185:    */             {
/* 186:243 */               saveStateAndReturn = true;
/* 187:    */               break label970;
/* 188:    */             }
/* 189:246 */             while ((modifyLength) || (reader.readBoolean()))
/* 190:    */             {
/* 191:247 */               if (!reader.isReadable())
/* 192:    */               {
/* 193:248 */                 modifyLength = true;
/* 194:249 */                 saveStateAndReturn = true;
/* 195:    */                 break label970;
/* 196:    */               }
/* 197:253 */               currLength += (reader.readBoolean() ? -1 : 1);
/* 198:254 */               modifyLength = false;
/* 199:255 */               if (!reader.isReadable())
/* 200:    */               {
/* 201:256 */                 saveStateAndReturn = true;
/* 202:    */                 break label970;
/* 203:    */               }
/* 204:    */             }
/* 205:260 */             codeLength[currGroup][currAlpha] = ((byte)currLength);
/* 206:    */           }
/* 207:262 */           currLength = -1;
/* 208:263 */           currAlpha = huffmanStageDecoder.currentAlpha = 0;
/* 209:264 */           modifyLength = false;
/* 210:    */         }
/* 211:266 */         if (saveStateAndReturn)
/* 212:    */         {
/* 213:268 */           huffmanStageDecoder.currentGroup = currGroup;
/* 214:269 */           huffmanStageDecoder.currentLength = currLength;
/* 215:270 */           huffmanStageDecoder.currentAlpha = currAlpha;
/* 216:271 */           huffmanStageDecoder.modifyLength = modifyLength;
/* 217:272 */           return;
/* 218:    */         }
/* 219:276 */         huffmanStageDecoder.createHuffmanDecodingTables();
/* 220:277 */         this.currentState = State.DECODE_HUFFMAN_DATA;
/* 221:    */       case 9: 
/* 222:    */         label970:
/* 223:280 */         Bzip2BlockDecompressor blockDecompressor = this.blockDecompressor;
/* 224:281 */         int oldReaderIndex = in.readerIndex();
/* 225:282 */         boolean decoded = blockDecompressor.decodeHuffmanData(this.huffmanStageDecoder);
/* 226:283 */         if (!decoded) {
/* 227:284 */           return;
/* 228:    */         }
/* 229:289 */         if ((in.readerIndex() == oldReaderIndex) && (in.isReadable())) {
/* 230:290 */           reader.refill();
/* 231:    */         }
/* 232:293 */         int blockLength = blockDecompressor.blockLength();
/* 233:294 */         ByteBuf uncompressed = ctx.alloc().buffer(blockLength);
/* 234:295 */         boolean success = false;
/* 235:    */         try
/* 236:    */         {
/* 237:    */           int uncByte;
/* 238:298 */           while ((uncByte = blockDecompressor.read()) >= 0) {
/* 239:299 */             uncompressed.writeByte(uncByte);
/* 240:    */           }
/* 241:302 */           int currentBlockCRC = blockDecompressor.checkCRC();
/* 242:303 */           this.streamCRC = ((this.streamCRC << 1 | this.streamCRC >>> 31) ^ currentBlockCRC);
/* 243:    */           
/* 244:305 */           out.add(uncompressed);
/* 245:306 */           success = true;
/* 246:    */         }
/* 247:    */         finally
/* 248:    */         {
/* 249:308 */           if (!success) {
/* 250:309 */             uncompressed.release();
/* 251:    */           }
/* 252:    */         }
/* 253:312 */         this.currentState = State.INIT_BLOCK;
/* 254:    */       }
/* 255:    */     }
/* 256:315 */     in.skipBytes(in.readableBytes());
/* 257:316 */     return;
/* 258:    */     
/* 259:318 */     throw new IllegalStateException();
/* 260:    */   }
/* 261:    */   
/* 262:    */   public boolean isClosed()
/* 263:    */   {
/* 264:328 */     return this.currentState == State.EOF;
/* 265:    */   }
/* 266:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.compression.Bzip2Decoder
 * JD-Core Version:    0.7.0.1
 */