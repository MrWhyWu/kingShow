/*   1:    */ package io.netty.handler.codec.compression;
/*   2:    */ 
/*   3:    */ import com.jcraft.jzlib.Inflater;
/*   4:    */ import com.jcraft.jzlib.JZlib;
/*   5:    */ import io.netty.buffer.ByteBuf;
/*   6:    */ import io.netty.buffer.ByteBufAllocator;
/*   7:    */ import io.netty.channel.ChannelHandlerContext;
/*   8:    */ import java.util.List;
/*   9:    */ 
/*  10:    */ public class JZlibDecoder
/*  11:    */   extends ZlibDecoder
/*  12:    */ {
/*  13: 27 */   private final Inflater z = new Inflater();
/*  14:    */   private byte[] dictionary;
/*  15:    */   private volatile boolean finished;
/*  16:    */   
/*  17:    */   public JZlibDecoder()
/*  18:    */   {
/*  19: 37 */     this(ZlibWrapper.ZLIB);
/*  20:    */   }
/*  21:    */   
/*  22:    */   public JZlibDecoder(ZlibWrapper wrapper)
/*  23:    */   {
/*  24: 46 */     if (wrapper == null) {
/*  25: 47 */       throw new NullPointerException("wrapper");
/*  26:    */     }
/*  27: 50 */     int resultCode = this.z.init(ZlibUtil.convertWrapperType(wrapper));
/*  28: 51 */     if (resultCode != 0) {
/*  29: 52 */       ZlibUtil.fail(this.z, "initialization failure", resultCode);
/*  30:    */     }
/*  31:    */   }
/*  32:    */   
/*  33:    */   public JZlibDecoder(byte[] dictionary)
/*  34:    */   {
/*  35: 64 */     if (dictionary == null) {
/*  36: 65 */       throw new NullPointerException("dictionary");
/*  37:    */     }
/*  38: 67 */     this.dictionary = dictionary;
/*  39:    */     
/*  40:    */ 
/*  41: 70 */     int resultCode = this.z.inflateInit(JZlib.W_ZLIB);
/*  42: 71 */     if (resultCode != 0) {
/*  43: 72 */       ZlibUtil.fail(this.z, "initialization failure", resultCode);
/*  44:    */     }
/*  45:    */   }
/*  46:    */   
/*  47:    */   public boolean isClosed()
/*  48:    */   {
/*  49: 82 */     return this.finished;
/*  50:    */   }
/*  51:    */   
/*  52:    */   protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/*  53:    */     throws Exception
/*  54:    */   {
/*  55: 87 */     if (this.finished)
/*  56:    */     {
/*  57: 89 */       in.skipBytes(in.readableBytes());
/*  58: 90 */       return;
/*  59:    */     }
/*  60: 93 */     int inputLength = in.readableBytes();
/*  61: 94 */     if (inputLength == 0) {
/*  62: 95 */       return;
/*  63:    */     }
/*  64:    */     try
/*  65:    */     {
/*  66:100 */       this.z.avail_in = inputLength;
/*  67:101 */       if (in.hasArray())
/*  68:    */       {
/*  69:102 */         this.z.next_in = in.array();
/*  70:103 */         this.z.next_in_index = (in.arrayOffset() + in.readerIndex());
/*  71:    */       }
/*  72:    */       else
/*  73:    */       {
/*  74:105 */         byte[] array = new byte[inputLength];
/*  75:106 */         in.getBytes(in.readerIndex(), array);
/*  76:107 */         this.z.next_in = array;
/*  77:108 */         this.z.next_in_index = 0;
/*  78:    */       }
/*  79:110 */       int oldNextInIndex = this.z.next_in_index;
/*  80:    */       
/*  81:    */ 
/*  82:113 */       ByteBuf decompressed = ctx.alloc().heapBuffer(inputLength << 1);
/*  83:    */       try
/*  84:    */       {
/*  85:    */         for (;;)
/*  86:    */         {
/*  87:117 */           decompressed.ensureWritable(this.z.avail_in << 1);
/*  88:118 */           this.z.avail_out = decompressed.writableBytes();
/*  89:119 */           this.z.next_out = decompressed.array();
/*  90:120 */           this.z.next_out_index = (decompressed.arrayOffset() + decompressed.writerIndex());
/*  91:121 */           int oldNextOutIndex = this.z.next_out_index;
/*  92:    */           
/*  93:    */ 
/*  94:124 */           int resultCode = this.z.inflate(2);
/*  95:125 */           int outputLength = this.z.next_out_index - oldNextOutIndex;
/*  96:126 */           if (outputLength > 0) {
/*  97:127 */             decompressed.writerIndex(decompressed.writerIndex() + outputLength);
/*  98:    */           }
/*  99:130 */           switch (resultCode)
/* 100:    */           {
/* 101:    */           case 2: 
/* 102:132 */             if (this.dictionary == null)
/* 103:    */             {
/* 104:133 */               ZlibUtil.fail(this.z, "decompression failure", resultCode);
/* 105:    */             }
/* 106:    */             else
/* 107:    */             {
/* 108:135 */               resultCode = this.z.inflateSetDictionary(this.dictionary, this.dictionary.length);
/* 109:136 */               if (resultCode != 0) {
/* 110:137 */                 ZlibUtil.fail(this.z, "failed to set the dictionary", resultCode);
/* 111:    */               }
/* 112:    */             }
/* 113:    */             break;
/* 114:    */           case 1: 
/* 115:142 */             this.finished = true;
/* 116:143 */             this.z.inflateEnd();
/* 117:144 */             break;
/* 118:    */           case 0: 
/* 119:    */             break;
/* 120:    */           case -5: 
/* 121:148 */             if (this.z.avail_in > 0) {
/* 122:    */               break;
/* 123:    */             }
/* 124:149 */             break;
/* 125:    */           case -4: 
/* 126:    */           case -3: 
/* 127:    */           case -2: 
/* 128:    */           case -1: 
/* 129:    */           default: 
/* 130:153 */             ZlibUtil.fail(this.z, "decompression failure", resultCode);
/* 131:    */           }
/* 132:    */         }
/* 133:    */       }
/* 134:    */       finally
/* 135:    */       {
/* 136:157 */         in.skipBytes(this.z.next_in_index - oldNextInIndex);
/* 137:158 */         if (decompressed.isReadable()) {
/* 138:159 */           out.add(decompressed);
/* 139:    */         } else {
/* 140:161 */           decompressed.release();
/* 141:    */         }
/* 142:    */       }
/* 143:    */     }
/* 144:    */     finally
/* 145:    */     {
/* 146:169 */       this.z.next_in = null;
/* 147:170 */       this.z.next_out = null;
/* 148:    */     }
/* 149:    */   }
/* 150:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.compression.JZlibDecoder
 * JD-Core Version:    0.7.0.1
 */