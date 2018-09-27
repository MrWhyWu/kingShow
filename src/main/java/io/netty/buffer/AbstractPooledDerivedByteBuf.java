/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.Recycler.Handle;
/*   4:    */ import io.netty.util.ReferenceCounted;
/*   5:    */ import java.nio.ByteBuffer;
/*   6:    */ import java.nio.ByteOrder;
/*   7:    */ 
/*   8:    */ abstract class AbstractPooledDerivedByteBuf
/*   9:    */   extends AbstractReferenceCountedByteBuf
/*  10:    */ {
/*  11:    */   private final Recycler.Handle<AbstractPooledDerivedByteBuf> recyclerHandle;
/*  12:    */   private AbstractByteBuf rootParent;
/*  13:    */   private ByteBuf parent;
/*  14:    */   
/*  15:    */   AbstractPooledDerivedByteBuf(Recycler.Handle<? extends AbstractPooledDerivedByteBuf> recyclerHandle)
/*  16:    */   {
/*  17: 42 */     super(0);
/*  18: 43 */     this.recyclerHandle = recyclerHandle;
/*  19:    */   }
/*  20:    */   
/*  21:    */   final void parent(ByteBuf newParent)
/*  22:    */   {
/*  23: 48 */     assert ((newParent instanceof SimpleLeakAwareByteBuf));
/*  24: 49 */     this.parent = newParent;
/*  25:    */   }
/*  26:    */   
/*  27:    */   public final AbstractByteBuf unwrap()
/*  28:    */   {
/*  29: 54 */     return this.rootParent;
/*  30:    */   }
/*  31:    */   
/*  32:    */   final <U extends AbstractPooledDerivedByteBuf> U init(AbstractByteBuf unwrapped, ByteBuf wrapped, int readerIndex, int writerIndex, int maxCapacity)
/*  33:    */   {
/*  34: 59 */     wrapped.retain();
/*  35: 60 */     this.parent = wrapped;
/*  36: 61 */     this.rootParent = unwrapped;
/*  37:    */     try
/*  38:    */     {
/*  39: 64 */       maxCapacity(maxCapacity);
/*  40: 65 */       setIndex0(readerIndex, writerIndex);
/*  41: 66 */       setRefCnt(1);
/*  42:    */       
/*  43:    */ 
/*  44: 69 */       U castThis = this;
/*  45: 70 */       wrapped = null;
/*  46: 71 */       return castThis;
/*  47:    */     }
/*  48:    */     finally
/*  49:    */     {
/*  50: 73 */       if (wrapped != null)
/*  51:    */       {
/*  52: 74 */         this.parent = (this.rootParent = null);
/*  53: 75 */         wrapped.release();
/*  54:    */       }
/*  55:    */     }
/*  56:    */   }
/*  57:    */   
/*  58:    */   protected final void deallocate()
/*  59:    */   {
/*  60: 85 */     ByteBuf parent = this.parent;
/*  61: 86 */     this.recyclerHandle.recycle(this);
/*  62: 87 */     parent.release();
/*  63:    */   }
/*  64:    */   
/*  65:    */   public final ByteBufAllocator alloc()
/*  66:    */   {
/*  67: 92 */     return unwrap().alloc();
/*  68:    */   }
/*  69:    */   
/*  70:    */   @Deprecated
/*  71:    */   public final ByteOrder order()
/*  72:    */   {
/*  73: 98 */     return unwrap().order();
/*  74:    */   }
/*  75:    */   
/*  76:    */   public boolean isReadOnly()
/*  77:    */   {
/*  78:103 */     return unwrap().isReadOnly();
/*  79:    */   }
/*  80:    */   
/*  81:    */   public final boolean isDirect()
/*  82:    */   {
/*  83:108 */     return unwrap().isDirect();
/*  84:    */   }
/*  85:    */   
/*  86:    */   public boolean hasArray()
/*  87:    */   {
/*  88:113 */     return unwrap().hasArray();
/*  89:    */   }
/*  90:    */   
/*  91:    */   public byte[] array()
/*  92:    */   {
/*  93:118 */     return unwrap().array();
/*  94:    */   }
/*  95:    */   
/*  96:    */   public boolean hasMemoryAddress()
/*  97:    */   {
/*  98:123 */     return unwrap().hasMemoryAddress();
/*  99:    */   }
/* 100:    */   
/* 101:    */   public final int nioBufferCount()
/* 102:    */   {
/* 103:128 */     return unwrap().nioBufferCount();
/* 104:    */   }
/* 105:    */   
/* 106:    */   public final ByteBuffer internalNioBuffer(int index, int length)
/* 107:    */   {
/* 108:133 */     return nioBuffer(index, length);
/* 109:    */   }
/* 110:    */   
/* 111:    */   public final ByteBuf retainedSlice()
/* 112:    */   {
/* 113:138 */     int index = readerIndex();
/* 114:139 */     return retainedSlice(index, writerIndex() - index);
/* 115:    */   }
/* 116:    */   
/* 117:    */   public ByteBuf slice(int index, int length)
/* 118:    */   {
/* 119:145 */     return new PooledNonRetainedSlicedByteBuf(this, unwrap(), index, length);
/* 120:    */   }
/* 121:    */   
/* 122:    */   final ByteBuf duplicate0()
/* 123:    */   {
/* 124:150 */     return new PooledNonRetainedDuplicateByteBuf(this, unwrap());
/* 125:    */   }
/* 126:    */   
/* 127:    */   private static final class PooledNonRetainedDuplicateByteBuf
/* 128:    */     extends UnpooledDuplicatedByteBuf
/* 129:    */   {
/* 130:    */     private final ReferenceCounted referenceCountDelegate;
/* 131:    */     
/* 132:    */     PooledNonRetainedDuplicateByteBuf(ReferenceCounted referenceCountDelegate, AbstractByteBuf buffer)
/* 133:    */     {
/* 134:157 */       super();
/* 135:158 */       this.referenceCountDelegate = referenceCountDelegate;
/* 136:    */     }
/* 137:    */     
/* 138:    */     int refCnt0()
/* 139:    */     {
/* 140:163 */       return this.referenceCountDelegate.refCnt();
/* 141:    */     }
/* 142:    */     
/* 143:    */     ByteBuf retain0()
/* 144:    */     {
/* 145:168 */       this.referenceCountDelegate.retain();
/* 146:169 */       return this;
/* 147:    */     }
/* 148:    */     
/* 149:    */     ByteBuf retain0(int increment)
/* 150:    */     {
/* 151:174 */       this.referenceCountDelegate.retain(increment);
/* 152:175 */       return this;
/* 153:    */     }
/* 154:    */     
/* 155:    */     ByteBuf touch0()
/* 156:    */     {
/* 157:180 */       this.referenceCountDelegate.touch();
/* 158:181 */       return this;
/* 159:    */     }
/* 160:    */     
/* 161:    */     ByteBuf touch0(Object hint)
/* 162:    */     {
/* 163:186 */       this.referenceCountDelegate.touch(hint);
/* 164:187 */       return this;
/* 165:    */     }
/* 166:    */     
/* 167:    */     boolean release0()
/* 168:    */     {
/* 169:192 */       return this.referenceCountDelegate.release();
/* 170:    */     }
/* 171:    */     
/* 172:    */     boolean release0(int decrement)
/* 173:    */     {
/* 174:197 */       return this.referenceCountDelegate.release(decrement);
/* 175:    */     }
/* 176:    */     
/* 177:    */     public ByteBuf duplicate()
/* 178:    */     {
/* 179:202 */       return new PooledNonRetainedDuplicateByteBuf(this.referenceCountDelegate, this);
/* 180:    */     }
/* 181:    */     
/* 182:    */     public ByteBuf retainedDuplicate()
/* 183:    */     {
/* 184:207 */       return PooledDuplicatedByteBuf.newInstance(unwrap(), this, readerIndex(), writerIndex());
/* 185:    */     }
/* 186:    */     
/* 187:    */     public ByteBuf slice(int index, int length)
/* 188:    */     {
/* 189:212 */       checkIndex0(index, length);
/* 190:213 */       return new AbstractPooledDerivedByteBuf.PooledNonRetainedSlicedByteBuf(this.referenceCountDelegate, unwrap(), index, length);
/* 191:    */     }
/* 192:    */     
/* 193:    */     public ByteBuf retainedSlice()
/* 194:    */     {
/* 195:219 */       return retainedSlice(readerIndex(), capacity());
/* 196:    */     }
/* 197:    */     
/* 198:    */     public ByteBuf retainedSlice(int index, int length)
/* 199:    */     {
/* 200:224 */       return PooledSlicedByteBuf.newInstance(unwrap(), this, index, length);
/* 201:    */     }
/* 202:    */   }
/* 203:    */   
/* 204:    */   private static final class PooledNonRetainedSlicedByteBuf
/* 205:    */     extends UnpooledSlicedByteBuf
/* 206:    */   {
/* 207:    */     private final ReferenceCounted referenceCountDelegate;
/* 208:    */     
/* 209:    */     PooledNonRetainedSlicedByteBuf(ReferenceCounted referenceCountDelegate, AbstractByteBuf buffer, int index, int length)
/* 210:    */     {
/* 211:233 */       super(index, length);
/* 212:234 */       this.referenceCountDelegate = referenceCountDelegate;
/* 213:    */     }
/* 214:    */     
/* 215:    */     int refCnt0()
/* 216:    */     {
/* 217:239 */       return this.referenceCountDelegate.refCnt();
/* 218:    */     }
/* 219:    */     
/* 220:    */     ByteBuf retain0()
/* 221:    */     {
/* 222:244 */       this.referenceCountDelegate.retain();
/* 223:245 */       return this;
/* 224:    */     }
/* 225:    */     
/* 226:    */     ByteBuf retain0(int increment)
/* 227:    */     {
/* 228:250 */       this.referenceCountDelegate.retain(increment);
/* 229:251 */       return this;
/* 230:    */     }
/* 231:    */     
/* 232:    */     ByteBuf touch0()
/* 233:    */     {
/* 234:256 */       this.referenceCountDelegate.touch();
/* 235:257 */       return this;
/* 236:    */     }
/* 237:    */     
/* 238:    */     ByteBuf touch0(Object hint)
/* 239:    */     {
/* 240:262 */       this.referenceCountDelegate.touch(hint);
/* 241:263 */       return this;
/* 242:    */     }
/* 243:    */     
/* 244:    */     boolean release0()
/* 245:    */     {
/* 246:268 */       return this.referenceCountDelegate.release();
/* 247:    */     }
/* 248:    */     
/* 249:    */     boolean release0(int decrement)
/* 250:    */     {
/* 251:273 */       return this.referenceCountDelegate.release(decrement);
/* 252:    */     }
/* 253:    */     
/* 254:    */     public ByteBuf duplicate()
/* 255:    */     {
/* 256:278 */       return 
/* 257:279 */         new AbstractPooledDerivedByteBuf.PooledNonRetainedDuplicateByteBuf(this.referenceCountDelegate, unwrap()).setIndex(idx(readerIndex()), idx(writerIndex()));
/* 258:    */     }
/* 259:    */     
/* 260:    */     public ByteBuf retainedDuplicate()
/* 261:    */     {
/* 262:284 */       return PooledDuplicatedByteBuf.newInstance(unwrap(), this, idx(readerIndex()), idx(writerIndex()));
/* 263:    */     }
/* 264:    */     
/* 265:    */     public ByteBuf slice(int index, int length)
/* 266:    */     {
/* 267:289 */       checkIndex0(index, length);
/* 268:290 */       return new PooledNonRetainedSlicedByteBuf(this.referenceCountDelegate, unwrap(), idx(index), length);
/* 269:    */     }
/* 270:    */     
/* 271:    */     public ByteBuf retainedSlice()
/* 272:    */     {
/* 273:296 */       return retainedSlice(0, capacity());
/* 274:    */     }
/* 275:    */     
/* 276:    */     public ByteBuf retainedSlice(int index, int length)
/* 277:    */     {
/* 278:301 */       return PooledSlicedByteBuf.newInstance(unwrap(), this, idx(index), length);
/* 279:    */     }
/* 280:    */   }
/* 281:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.AbstractPooledDerivedByteBuf
 * JD-Core Version:    0.7.0.1
 */