/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.Unpooled;
/*   5:    */ import io.netty.util.AbstractReferenceCounted;
/*   6:    */ import io.netty.util.CharsetUtil;
/*   7:    */ import io.netty.util.IllegalReferenceCountException;
/*   8:    */ import io.netty.util.internal.ObjectUtil;
/*   9:    */ import java.security.PrivateKey;
/*  10:    */ 
/*  11:    */ public final class PemPrivateKey
/*  12:    */   extends AbstractReferenceCounted
/*  13:    */   implements PrivateKey, PemEncoded
/*  14:    */ {
/*  15:    */   private static final long serialVersionUID = 7978017465645018936L;
/*  16: 46 */   private static final byte[] BEGIN_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----\n".getBytes(CharsetUtil.US_ASCII);
/*  17: 47 */   private static final byte[] END_PRIVATE_KEY = "\n-----END PRIVATE KEY-----\n".getBytes(CharsetUtil.US_ASCII);
/*  18:    */   private static final String PKCS8_FORMAT = "PKCS#8";
/*  19:    */   private final ByteBuf content;
/*  20:    */   
/*  21:    */   /* Error */
/*  22:    */   static PemEncoded toPEM(io.netty.buffer.ByteBufAllocator allocator, boolean useDirect, PrivateKey key)
/*  23:    */   {
/*  24:    */     // Byte code:
/*  25:    */     //   0: aload_2
/*  26:    */     //   1: instanceof 1
/*  27:    */     //   4: ifeq +13 -> 17
/*  28:    */     //   7: aload_2
/*  29:    */     //   8: checkcast 1	io/netty/handler/ssl/PemEncoded
/*  30:    */     //   11: invokeinterface 2 1 0
/*  31:    */     //   16: areturn
/*  32:    */     //   17: aload_2
/*  33:    */     //   18: invokeinterface 3 1 0
/*  34:    */     //   23: invokestatic 4	io/netty/buffer/Unpooled:wrappedBuffer	([B)Lio/netty/buffer/ByteBuf;
/*  35:    */     //   26: astore_3
/*  36:    */     //   27: aload_0
/*  37:    */     //   28: aload_3
/*  38:    */     //   29: invokestatic 5	io/netty/handler/ssl/SslUtils:toBase64	(Lio/netty/buffer/ByteBufAllocator;Lio/netty/buffer/ByteBuf;)Lio/netty/buffer/ByteBuf;
/*  39:    */     //   32: astore 4
/*  40:    */     //   34: getstatic 6	io/netty/handler/ssl/PemPrivateKey:BEGIN_PRIVATE_KEY	[B
/*  41:    */     //   37: arraylength
/*  42:    */     //   38: aload 4
/*  43:    */     //   40: invokevirtual 7	io/netty/buffer/ByteBuf:readableBytes	()I
/*  44:    */     //   43: iadd
/*  45:    */     //   44: getstatic 8	io/netty/handler/ssl/PemPrivateKey:END_PRIVATE_KEY	[B
/*  46:    */     //   47: arraylength
/*  47:    */     //   48: iadd
/*  48:    */     //   49: istore 5
/*  49:    */     //   51: iconst_0
/*  50:    */     //   52: istore 6
/*  51:    */     //   54: iload_1
/*  52:    */     //   55: ifeq +14 -> 69
/*  53:    */     //   58: aload_0
/*  54:    */     //   59: iload 5
/*  55:    */     //   61: invokeinterface 9 2 0
/*  56:    */     //   66: goto +11 -> 77
/*  57:    */     //   69: aload_0
/*  58:    */     //   70: iload 5
/*  59:    */     //   72: invokeinterface 10 2 0
/*  60:    */     //   77: astore 7
/*  61:    */     //   79: aload 7
/*  62:    */     //   81: getstatic 6	io/netty/handler/ssl/PemPrivateKey:BEGIN_PRIVATE_KEY	[B
/*  63:    */     //   84: invokevirtual 11	io/netty/buffer/ByteBuf:writeBytes	([B)Lio/netty/buffer/ByteBuf;
/*  64:    */     //   87: pop
/*  65:    */     //   88: aload 7
/*  66:    */     //   90: aload 4
/*  67:    */     //   92: invokevirtual 12	io/netty/buffer/ByteBuf:writeBytes	(Lio/netty/buffer/ByteBuf;)Lio/netty/buffer/ByteBuf;
/*  68:    */     //   95: pop
/*  69:    */     //   96: aload 7
/*  70:    */     //   98: getstatic 8	io/netty/handler/ssl/PemPrivateKey:END_PRIVATE_KEY	[B
/*  71:    */     //   101: invokevirtual 11	io/netty/buffer/ByteBuf:writeBytes	([B)Lio/netty/buffer/ByteBuf;
/*  72:    */     //   104: pop
/*  73:    */     //   105: new 13	io/netty/handler/ssl/PemValue
/*  74:    */     //   108: dup
/*  75:    */     //   109: aload 7
/*  76:    */     //   111: iconst_1
/*  77:    */     //   112: invokespecial 14	io/netty/handler/ssl/PemValue:<init>	(Lio/netty/buffer/ByteBuf;Z)V
/*  78:    */     //   115: astore 8
/*  79:    */     //   117: iconst_1
/*  80:    */     //   118: istore 6
/*  81:    */     //   120: aload 8
/*  82:    */     //   122: astore 9
/*  83:    */     //   124: iload 6
/*  84:    */     //   126: ifne +8 -> 134
/*  85:    */     //   129: aload 7
/*  86:    */     //   131: invokestatic 15	io/netty/handler/ssl/SslUtils:zerooutAndRelease	(Lio/netty/buffer/ByteBuf;)V
/*  87:    */     //   134: aload 4
/*  88:    */     //   136: invokestatic 15	io/netty/handler/ssl/SslUtils:zerooutAndRelease	(Lio/netty/buffer/ByteBuf;)V
/*  89:    */     //   139: aload_3
/*  90:    */     //   140: invokestatic 15	io/netty/handler/ssl/SslUtils:zerooutAndRelease	(Lio/netty/buffer/ByteBuf;)V
/*  91:    */     //   143: aload 9
/*  92:    */     //   145: areturn
/*  93:    */     //   146: astore 10
/*  94:    */     //   148: iload 6
/*  95:    */     //   150: ifne +8 -> 158
/*  96:    */     //   153: aload 7
/*  97:    */     //   155: invokestatic 15	io/netty/handler/ssl/SslUtils:zerooutAndRelease	(Lio/netty/buffer/ByteBuf;)V
/*  98:    */     //   158: aload 10
/*  99:    */     //   160: athrow
/* 100:    */     //   161: astore 11
/* 101:    */     //   163: aload 4
/* 102:    */     //   165: invokestatic 15	io/netty/handler/ssl/SslUtils:zerooutAndRelease	(Lio/netty/buffer/ByteBuf;)V
/* 103:    */     //   168: aload 11
/* 104:    */     //   170: athrow
/* 105:    */     //   171: astore 12
/* 106:    */     //   173: aload_3
/* 107:    */     //   174: invokestatic 15	io/netty/handler/ssl/SslUtils:zerooutAndRelease	(Lio/netty/buffer/ByteBuf;)V
/* 108:    */     //   177: aload 12
/* 109:    */     //   179: athrow
/* 110:    */     // Line number table:
/* 111:    */     //   Java source line #59	-> byte code offset #0
/* 112:    */     //   Java source line #60	-> byte code offset #7
/* 113:    */     //   Java source line #63	-> byte code offset #17
/* 114:    */     //   Java source line #65	-> byte code offset #27
/* 115:    */     //   Java source line #67	-> byte code offset #34
/* 116:    */     //   Java source line #69	-> byte code offset #51
/* 117:    */     //   Java source line #70	-> byte code offset #54
/* 118:    */     //   Java source line #72	-> byte code offset #79
/* 119:    */     //   Java source line #73	-> byte code offset #88
/* 120:    */     //   Java source line #74	-> byte code offset #96
/* 121:    */     //   Java source line #76	-> byte code offset #105
/* 122:    */     //   Java source line #77	-> byte code offset #117
/* 123:    */     //   Java source line #78	-> byte code offset #120
/* 124:    */     //   Java source line #81	-> byte code offset #124
/* 125:    */     //   Java source line #82	-> byte code offset #129
/* 126:    */     //   Java source line #86	-> byte code offset #134
/* 127:    */     //   Java source line #89	-> byte code offset #139
/* 128:    */     //   Java source line #78	-> byte code offset #143
/* 129:    */     //   Java source line #81	-> byte code offset #146
/* 130:    */     //   Java source line #82	-> byte code offset #153
/* 131:    */     //   Java source line #86	-> byte code offset #161
/* 132:    */     //   Java source line #89	-> byte code offset #171
/* 133:    */     // Local variable table:
/* 134:    */     //   start	length	slot	name	signature
/* 135:    */     //   0	180	0	allocator	io.netty.buffer.ByteBufAllocator
/* 136:    */     //   0	180	1	useDirect	boolean
/* 137:    */     //   0	180	2	key	PrivateKey
/* 138:    */     //   26	148	3	encoded	ByteBuf
/* 139:    */     //   32	132	4	base64	ByteBuf
/* 140:    */     //   49	22	5	size	int
/* 141:    */     //   52	97	6	success	boolean
/* 142:    */     //   77	77	7	pem	ByteBuf
/* 143:    */     //   115	6	8	value	PemValue
/* 144:    */     //   122	22	9	localPemValue1	PemValue
/* 145:    */     //   146	13	10	localObject1	Object
/* 146:    */     //   161	8	11	localObject2	Object
/* 147:    */     //   171	7	12	localObject3	Object
/* 148:    */     // Exception table:
/* 149:    */     //   from	to	target	type
/* 150:    */     //   79	124	146	finally
/* 151:    */     //   146	148	146	finally
/* 152:    */     //   34	134	161	finally
/* 153:    */     //   146	163	161	finally
/* 154:    */     //   27	139	171	finally
/* 155:    */     //   146	173	171	finally
/* 156:    */   }
/* 157:    */   
/* 158:    */   public static PemPrivateKey valueOf(byte[] key)
/* 159:    */   {
/* 160:100 */     return valueOf(Unpooled.wrappedBuffer(key));
/* 161:    */   }
/* 162:    */   
/* 163:    */   public static PemPrivateKey valueOf(ByteBuf key)
/* 164:    */   {
/* 165:110 */     return new PemPrivateKey(key);
/* 166:    */   }
/* 167:    */   
/* 168:    */   private PemPrivateKey(ByteBuf content)
/* 169:    */   {
/* 170:116 */     this.content = ((ByteBuf)ObjectUtil.checkNotNull(content, "content"));
/* 171:    */   }
/* 172:    */   
/* 173:    */   public boolean isSensitive()
/* 174:    */   {
/* 175:121 */     return true;
/* 176:    */   }
/* 177:    */   
/* 178:    */   public ByteBuf content()
/* 179:    */   {
/* 180:126 */     int count = refCnt();
/* 181:127 */     if (count <= 0) {
/* 182:128 */       throw new IllegalReferenceCountException(count);
/* 183:    */     }
/* 184:131 */     return this.content;
/* 185:    */   }
/* 186:    */   
/* 187:    */   public PemPrivateKey copy()
/* 188:    */   {
/* 189:136 */     return replace(this.content.copy());
/* 190:    */   }
/* 191:    */   
/* 192:    */   public PemPrivateKey duplicate()
/* 193:    */   {
/* 194:141 */     return replace(this.content.duplicate());
/* 195:    */   }
/* 196:    */   
/* 197:    */   public PemPrivateKey retainedDuplicate()
/* 198:    */   {
/* 199:146 */     return replace(this.content.retainedDuplicate());
/* 200:    */   }
/* 201:    */   
/* 202:    */   public PemPrivateKey replace(ByteBuf content)
/* 203:    */   {
/* 204:151 */     return new PemPrivateKey(content);
/* 205:    */   }
/* 206:    */   
/* 207:    */   public PemPrivateKey touch()
/* 208:    */   {
/* 209:156 */     this.content.touch();
/* 210:157 */     return this;
/* 211:    */   }
/* 212:    */   
/* 213:    */   public PemPrivateKey touch(Object hint)
/* 214:    */   {
/* 215:162 */     this.content.touch(hint);
/* 216:163 */     return this;
/* 217:    */   }
/* 218:    */   
/* 219:    */   public PemPrivateKey retain()
/* 220:    */   {
/* 221:168 */     return (PemPrivateKey)super.retain();
/* 222:    */   }
/* 223:    */   
/* 224:    */   public PemPrivateKey retain(int increment)
/* 225:    */   {
/* 226:173 */     return (PemPrivateKey)super.retain(increment);
/* 227:    */   }
/* 228:    */   
/* 229:    */   protected void deallocate()
/* 230:    */   {
/* 231:180 */     SslUtils.zerooutAndRelease(this.content);
/* 232:    */   }
/* 233:    */   
/* 234:    */   public byte[] getEncoded()
/* 235:    */   {
/* 236:185 */     throw new UnsupportedOperationException();
/* 237:    */   }
/* 238:    */   
/* 239:    */   public String getAlgorithm()
/* 240:    */   {
/* 241:190 */     throw new UnsupportedOperationException();
/* 242:    */   }
/* 243:    */   
/* 244:    */   public String getFormat()
/* 245:    */   {
/* 246:195 */     return "PKCS#8";
/* 247:    */   }
/* 248:    */   
/* 249:    */   public void destroy()
/* 250:    */   {
/* 251:206 */     release(refCnt());
/* 252:    */   }
/* 253:    */   
/* 254:    */   public boolean isDestroyed()
/* 255:    */   {
/* 256:217 */     return refCnt() == 0;
/* 257:    */   }
/* 258:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.PemPrivateKey
 * JD-Core Version:    0.7.0.1
 */