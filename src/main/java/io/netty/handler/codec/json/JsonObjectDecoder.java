/*   1:    */ package io.netty.handler.codec.json;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufUtil;
/*   5:    */ import io.netty.channel.ChannelHandlerContext;
/*   6:    */ import io.netty.handler.codec.ByteToMessageDecoder;
/*   7:    */ import io.netty.handler.codec.CorruptedFrameException;
/*   8:    */ import io.netty.handler.codec.TooLongFrameException;
/*   9:    */ import java.util.List;
/*  10:    */ 
/*  11:    */ public class JsonObjectDecoder
/*  12:    */   extends ByteToMessageDecoder
/*  13:    */ {
/*  14:    */   private static final int ST_CORRUPTED = -1;
/*  15:    */   private static final int ST_INIT = 0;
/*  16:    */   private static final int ST_DECODING_NORMAL = 1;
/*  17:    */   private static final int ST_DECODING_ARRAY_STREAM = 2;
/*  18:    */   private int openBraces;
/*  19:    */   private int idx;
/*  20:    */   private int lastReaderIndex;
/*  21:    */   private int state;
/*  22:    */   private boolean insideString;
/*  23:    */   private final int maxObjectLength;
/*  24:    */   private final boolean streamArrayElements;
/*  25:    */   
/*  26:    */   public JsonObjectDecoder()
/*  27:    */   {
/*  28: 58 */     this(1048576);
/*  29:    */   }
/*  30:    */   
/*  31:    */   public JsonObjectDecoder(int maxObjectLength)
/*  32:    */   {
/*  33: 62 */     this(maxObjectLength, false);
/*  34:    */   }
/*  35:    */   
/*  36:    */   public JsonObjectDecoder(boolean streamArrayElements)
/*  37:    */   {
/*  38: 66 */     this(1048576, streamArrayElements);
/*  39:    */   }
/*  40:    */   
/*  41:    */   public JsonObjectDecoder(int maxObjectLength, boolean streamArrayElements)
/*  42:    */   {
/*  43: 79 */     if (maxObjectLength < 1) {
/*  44: 80 */       throw new IllegalArgumentException("maxObjectLength must be a positive int");
/*  45:    */     }
/*  46: 82 */     this.maxObjectLength = maxObjectLength;
/*  47: 83 */     this.streamArrayElements = streamArrayElements;
/*  48:    */   }
/*  49:    */   
/*  50:    */   protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/*  51:    */     throws Exception
/*  52:    */   {
/*  53: 88 */     if (this.state == -1)
/*  54:    */     {
/*  55: 89 */       in.skipBytes(in.readableBytes());
/*  56: 90 */       return;
/*  57:    */     }
/*  58: 93 */     if ((this.idx > in.readerIndex()) && (this.lastReaderIndex != in.readerIndex()))
/*  59:    */     {
/*  60: 94 */       this.idx = in.readerIndex();
/*  61: 95 */       if (this.state == 2)
/*  62:    */       {
/*  63: 96 */         this.insideString = false;
/*  64: 97 */         this.openBraces = 1;
/*  65:    */       }
/*  66:    */     }
/*  67:102 */     int idx = this.idx;
/*  68:103 */     int wrtIdx = in.writerIndex();
/*  69:105 */     if (wrtIdx > this.maxObjectLength)
/*  70:    */     {
/*  71:107 */       in.skipBytes(in.readableBytes());
/*  72:108 */       reset();
/*  73:109 */       throw new TooLongFrameException("object length exceeds " + this.maxObjectLength + ": " + wrtIdx + " bytes discarded");
/*  74:    */     }
/*  75:113 */     for (; idx < wrtIdx; idx++)
/*  76:    */     {
/*  77:114 */       byte c = in.getByte(idx);
/*  78:115 */       if (this.state == 1)
/*  79:    */       {
/*  80:116 */         decodeByte(c, in, idx);
/*  81:120 */         if (this.openBraces == 0)
/*  82:    */         {
/*  83:121 */           ByteBuf json = extractObject(ctx, in, in.readerIndex(), idx + 1 - in.readerIndex());
/*  84:122 */           if (json != null) {
/*  85:123 */             out.add(json);
/*  86:    */           }
/*  87:128 */           in.readerIndex(idx + 1);
/*  88:    */           
/*  89:    */ 
/*  90:131 */           reset();
/*  91:    */         }
/*  92:    */       }
/*  93:133 */       else if (this.state == 2)
/*  94:    */       {
/*  95:134 */         decodeByte(c, in, idx);
/*  96:136 */         if ((!this.insideString) && (((this.openBraces == 1) && (c == 44)) || ((this.openBraces == 0) && (c == 93))))
/*  97:    */         {
/*  98:139 */           for (int i = in.readerIndex(); Character.isWhitespace(in.getByte(i)); i++) {
/*  99:140 */             in.skipBytes(1);
/* 100:    */           }
/* 101:144 */           int idxNoSpaces = idx - 1;
/* 102:145 */           while ((idxNoSpaces >= in.readerIndex()) && (Character.isWhitespace(in.getByte(idxNoSpaces)))) {
/* 103:146 */             idxNoSpaces--;
/* 104:    */           }
/* 105:149 */           ByteBuf json = extractObject(ctx, in, in.readerIndex(), idxNoSpaces + 1 - in.readerIndex());
/* 106:150 */           if (json != null) {
/* 107:151 */             out.add(json);
/* 108:    */           }
/* 109:154 */           in.readerIndex(idx + 1);
/* 110:156 */           if (c == 93) {
/* 111:157 */             reset();
/* 112:    */           }
/* 113:    */         }
/* 114:    */       }
/* 115:161 */       else if ((c == 123) || (c == 91))
/* 116:    */       {
/* 117:162 */         initDecoding(c);
/* 118:164 */         if (this.state == 2) {
/* 119:166 */           in.skipBytes(1);
/* 120:    */         }
/* 121:    */       }
/* 122:169 */       else if (Character.isWhitespace(c))
/* 123:    */       {
/* 124:170 */         in.skipBytes(1);
/* 125:    */       }
/* 126:    */       else
/* 127:    */       {
/* 128:172 */         this.state = -1;
/* 129:    */         
/* 130:174 */         throw new CorruptedFrameException("invalid JSON received at byte position " + idx + ": " + ByteBufUtil.hexDump(in));
/* 131:    */       }
/* 132:    */     }
/* 133:178 */     if (in.readableBytes() == 0) {
/* 134:179 */       this.idx = 0;
/* 135:    */     } else {
/* 136:181 */       this.idx = idx;
/* 137:    */     }
/* 138:183 */     this.lastReaderIndex = in.readerIndex();
/* 139:    */   }
/* 140:    */   
/* 141:    */   protected ByteBuf extractObject(ChannelHandlerContext ctx, ByteBuf buffer, int index, int length)
/* 142:    */   {
/* 143:191 */     return buffer.retainedSlice(index, length);
/* 144:    */   }
/* 145:    */   
/* 146:    */   private void decodeByte(byte c, ByteBuf in, int idx)
/* 147:    */   {
/* 148:195 */     if (((c == 123) || (c == 91)) && (!this.insideString)) {
/* 149:196 */       this.openBraces += 1;
/* 150:197 */     } else if (((c == 125) || (c == 93)) && (!this.insideString)) {
/* 151:198 */       this.openBraces -= 1;
/* 152:199 */     } else if (c == 34) {
/* 153:202 */       if (!this.insideString)
/* 154:    */       {
/* 155:203 */         this.insideString = true;
/* 156:    */       }
/* 157:    */       else
/* 158:    */       {
/* 159:205 */         int backslashCount = 0;
/* 160:206 */         idx--;
/* 161:207 */         while ((idx >= 0) && 
/* 162:208 */           (in.getByte(idx) == 92))
/* 163:    */         {
/* 164:209 */           backslashCount++;
/* 165:210 */           idx--;
/* 166:    */         }
/* 167:216 */         if (backslashCount % 2 == 0) {
/* 168:218 */           this.insideString = false;
/* 169:    */         }
/* 170:    */       }
/* 171:    */     }
/* 172:    */   }
/* 173:    */   
/* 174:    */   private void initDecoding(byte openingBrace)
/* 175:    */   {
/* 176:225 */     this.openBraces = 1;
/* 177:226 */     if ((openingBrace == 91) && (this.streamArrayElements)) {
/* 178:227 */       this.state = 2;
/* 179:    */     } else {
/* 180:229 */       this.state = 1;
/* 181:    */     }
/* 182:    */   }
/* 183:    */   
/* 184:    */   private void reset()
/* 185:    */   {
/* 186:234 */     this.insideString = false;
/* 187:235 */     this.state = 0;
/* 188:236 */     this.openBraces = 0;
/* 189:    */   }
/* 190:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.json.JsonObjectDecoder
 * JD-Core Version:    0.7.0.1
 */