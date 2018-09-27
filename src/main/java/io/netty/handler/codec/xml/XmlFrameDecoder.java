/*   1:    */ package io.netty.handler.codec.xml;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.ChannelHandlerContext;
/*   5:    */ import io.netty.handler.codec.ByteToMessageDecoder;
/*   6:    */ import io.netty.handler.codec.CorruptedFrameException;
/*   7:    */ import io.netty.handler.codec.TooLongFrameException;
/*   8:    */ import java.util.List;
/*   9:    */ 
/*  10:    */ public class XmlFrameDecoder
/*  11:    */   extends ByteToMessageDecoder
/*  12:    */ {
/*  13:    */   private final int maxFrameLength;
/*  14:    */   
/*  15:    */   public XmlFrameDecoder(int maxFrameLength)
/*  16:    */   {
/*  17: 75 */     if (maxFrameLength < 1) {
/*  18: 76 */       throw new IllegalArgumentException("maxFrameLength must be a positive int");
/*  19:    */     }
/*  20: 78 */     this.maxFrameLength = maxFrameLength;
/*  21:    */   }
/*  22:    */   
/*  23:    */   protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/*  24:    */     throws Exception
/*  25:    */   {
/*  26: 83 */     boolean openingBracketFound = false;
/*  27: 84 */     boolean atLeastOneXmlElementFound = false;
/*  28: 85 */     boolean inCDATASection = false;
/*  29: 86 */     long openBracketsCount = 0L;
/*  30: 87 */     int length = 0;
/*  31: 88 */     int leadingWhiteSpaceCount = 0;
/*  32: 89 */     int bufferLength = in.writerIndex();
/*  33: 91 */     if (bufferLength > this.maxFrameLength)
/*  34:    */     {
/*  35: 93 */       in.skipBytes(in.readableBytes());
/*  36: 94 */       fail(bufferLength);
/*  37: 95 */       return;
/*  38:    */     }
/*  39: 98 */     for (int i = in.readerIndex(); i < bufferLength; i++)
/*  40:    */     {
/*  41: 99 */       byte readByte = in.getByte(i);
/*  42:100 */       if ((!openingBracketFound) && (Character.isWhitespace(readByte)))
/*  43:    */       {
/*  44:102 */         leadingWhiteSpaceCount++;
/*  45:    */       }
/*  46:    */       else
/*  47:    */       {
/*  48:103 */         if ((!openingBracketFound) && (readByte != 60))
/*  49:    */         {
/*  50:105 */           fail(ctx);
/*  51:106 */           in.skipBytes(in.readableBytes());
/*  52:107 */           return;
/*  53:    */         }
/*  54:108 */         if ((!inCDATASection) && (readByte == 60))
/*  55:    */         {
/*  56:109 */           openingBracketFound = true;
/*  57:111 */           if (i < bufferLength - 1)
/*  58:    */           {
/*  59:112 */             byte peekAheadByte = in.getByte(i + 1);
/*  60:113 */             if (peekAheadByte == 47)
/*  61:    */             {
/*  62:115 */               int peekFurtherAheadIndex = i + 2;
/*  63:116 */               while (peekFurtherAheadIndex <= bufferLength - 1)
/*  64:    */               {
/*  65:118 */                 if (in.getByte(peekFurtherAheadIndex) == 62)
/*  66:    */                 {
/*  67:119 */                   openBracketsCount -= 1L;
/*  68:120 */                   break;
/*  69:    */                 }
/*  70:122 */                 peekFurtherAheadIndex++;
/*  71:    */               }
/*  72:    */             }
/*  73:124 */             else if (isValidStartCharForXmlElement(peekAheadByte))
/*  74:    */             {
/*  75:125 */               atLeastOneXmlElementFound = true;
/*  76:    */               
/*  77:    */ 
/*  78:128 */               openBracketsCount += 1L;
/*  79:    */             }
/*  80:129 */             else if (peekAheadByte == 33)
/*  81:    */             {
/*  82:130 */               if (isCommentBlockStart(in, i))
/*  83:    */               {
/*  84:132 */                 openBracketsCount += 1L;
/*  85:    */               }
/*  86:133 */               else if (isCDATABlockStart(in, i))
/*  87:    */               {
/*  88:135 */                 openBracketsCount += 1L;
/*  89:136 */                 inCDATASection = true;
/*  90:    */               }
/*  91:    */             }
/*  92:138 */             else if (peekAheadByte == 63)
/*  93:    */             {
/*  94:140 */               openBracketsCount += 1L;
/*  95:    */             }
/*  96:    */           }
/*  97:    */         }
/*  98:143 */         else if ((!inCDATASection) && (readByte == 47))
/*  99:    */         {
/* 100:144 */           if ((i < bufferLength - 1) && (in.getByte(i + 1) == 62)) {
/* 101:146 */             openBracketsCount -= 1L;
/* 102:    */           }
/* 103:    */         }
/* 104:148 */         else if (readByte == 62)
/* 105:    */         {
/* 106:149 */           length = i + 1;
/* 107:151 */           if (i - 1 > -1)
/* 108:    */           {
/* 109:152 */             byte peekBehindByte = in.getByte(i - 1);
/* 110:154 */             if (!inCDATASection)
/* 111:    */             {
/* 112:155 */               if (peekBehindByte == 63) {
/* 113:157 */                 openBracketsCount -= 1L;
/* 114:158 */               } else if ((peekBehindByte == 45) && (i - 2 > -1) && (in.getByte(i - 2) == 45)) {
/* 115:160 */                 openBracketsCount -= 1L;
/* 116:    */               }
/* 117:    */             }
/* 118:162 */             else if ((peekBehindByte == 93) && (i - 2 > -1) && (in.getByte(i - 2) == 93))
/* 119:    */             {
/* 120:164 */               openBracketsCount -= 1L;
/* 121:165 */               inCDATASection = false;
/* 122:    */             }
/* 123:    */           }
/* 124:169 */           if ((atLeastOneXmlElementFound) && (openBracketsCount == 0L)) {
/* 125:    */             break;
/* 126:    */           }
/* 127:    */         }
/* 128:    */       }
/* 129:    */     }
/* 130:176 */     int readerIndex = in.readerIndex();
/* 131:177 */     int xmlElementLength = length - readerIndex;
/* 132:179 */     if ((openBracketsCount == 0L) && (xmlElementLength > 0))
/* 133:    */     {
/* 134:180 */       if (readerIndex + xmlElementLength >= bufferLength) {
/* 135:181 */         xmlElementLength = in.readableBytes();
/* 136:    */       }
/* 137:184 */       ByteBuf frame = extractFrame(in, readerIndex + leadingWhiteSpaceCount, xmlElementLength - leadingWhiteSpaceCount);
/* 138:185 */       in.skipBytes(xmlElementLength);
/* 139:186 */       out.add(frame);
/* 140:    */     }
/* 141:    */   }
/* 142:    */   
/* 143:    */   private void fail(long frameLength)
/* 144:    */   {
/* 145:191 */     if (frameLength > 0L) {
/* 146:192 */       throw new TooLongFrameException("frame length exceeds " + this.maxFrameLength + ": " + frameLength + " - discarded");
/* 147:    */     }
/* 148:195 */     throw new TooLongFrameException("frame length exceeds " + this.maxFrameLength + " - discarding");
/* 149:    */   }
/* 150:    */   
/* 151:    */   private static void fail(ChannelHandlerContext ctx)
/* 152:    */   {
/* 153:201 */     ctx.fireExceptionCaught(new CorruptedFrameException("frame contains content before the xml starts"));
/* 154:    */   }
/* 155:    */   
/* 156:    */   private static ByteBuf extractFrame(ByteBuf buffer, int index, int length)
/* 157:    */   {
/* 158:205 */     return buffer.copy(index, length);
/* 159:    */   }
/* 160:    */   
/* 161:    */   private static boolean isValidStartCharForXmlElement(byte b)
/* 162:    */   {
/* 163:220 */     return ((b >= 97) && (b <= 122)) || ((b >= 65) && (b <= 90)) || (b == 58) || (b == 95);
/* 164:    */   }
/* 165:    */   
/* 166:    */   private static boolean isCommentBlockStart(ByteBuf in, int i)
/* 167:    */   {
/* 168:224 */     return (i < in.writerIndex() - 3) && 
/* 169:225 */       (in.getByte(i + 2) == 45) && 
/* 170:226 */       (in.getByte(i + 3) == 45);
/* 171:    */   }
/* 172:    */   
/* 173:    */   private static boolean isCDATABlockStart(ByteBuf in, int i)
/* 174:    */   {
/* 175:230 */     return (i < in.writerIndex() - 8) && 
/* 176:231 */       (in.getByte(i + 2) == 91) && 
/* 177:232 */       (in.getByte(i + 3) == 67) && 
/* 178:233 */       (in.getByte(i + 4) == 68) && 
/* 179:234 */       (in.getByte(i + 5) == 65) && 
/* 180:235 */       (in.getByte(i + 6) == 84) && 
/* 181:236 */       (in.getByte(i + 7) == 65) && 
/* 182:237 */       (in.getByte(i + 8) == 91);
/* 183:    */   }
/* 184:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.xml.XmlFrameDecoder
 * JD-Core Version:    0.7.0.1
 */