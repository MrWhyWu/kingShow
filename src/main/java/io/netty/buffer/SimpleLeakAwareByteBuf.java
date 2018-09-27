/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.ResourceLeakDetector;
/*   4:    */ import io.netty.util.ResourceLeakTracker;
/*   5:    */ import io.netty.util.internal.ObjectUtil;
/*   6:    */ import java.nio.ByteOrder;
/*   7:    */ 
/*   8:    */ class SimpleLeakAwareByteBuf
/*   9:    */   extends WrappedByteBuf
/*  10:    */ {
/*  11:    */   private final ByteBuf trackedByteBuf;
/*  12:    */   final ResourceLeakTracker<ByteBuf> leak;
/*  13:    */   
/*  14:    */   SimpleLeakAwareByteBuf(ByteBuf wrapped, ByteBuf trackedByteBuf, ResourceLeakTracker<ByteBuf> leak)
/*  15:    */   {
/*  16: 36 */     super(wrapped);
/*  17: 37 */     this.trackedByteBuf = ((ByteBuf)ObjectUtil.checkNotNull(trackedByteBuf, "trackedByteBuf"));
/*  18: 38 */     this.leak = ((ResourceLeakTracker)ObjectUtil.checkNotNull(leak, "leak"));
/*  19:    */   }
/*  20:    */   
/*  21:    */   SimpleLeakAwareByteBuf(ByteBuf wrapped, ResourceLeakTracker<ByteBuf> leak)
/*  22:    */   {
/*  23: 42 */     this(wrapped, wrapped, leak);
/*  24:    */   }
/*  25:    */   
/*  26:    */   public ByteBuf slice()
/*  27:    */   {
/*  28: 47 */     return newSharedLeakAwareByteBuf(super.slice());
/*  29:    */   }
/*  30:    */   
/*  31:    */   public ByteBuf retainedSlice()
/*  32:    */   {
/*  33: 52 */     return unwrappedDerived(super.retainedSlice());
/*  34:    */   }
/*  35:    */   
/*  36:    */   public ByteBuf retainedSlice(int index, int length)
/*  37:    */   {
/*  38: 57 */     return unwrappedDerived(super.retainedSlice(index, length));
/*  39:    */   }
/*  40:    */   
/*  41:    */   public ByteBuf retainedDuplicate()
/*  42:    */   {
/*  43: 62 */     return unwrappedDerived(super.retainedDuplicate());
/*  44:    */   }
/*  45:    */   
/*  46:    */   public ByteBuf readRetainedSlice(int length)
/*  47:    */   {
/*  48: 67 */     return unwrappedDerived(super.readRetainedSlice(length));
/*  49:    */   }
/*  50:    */   
/*  51:    */   public ByteBuf slice(int index, int length)
/*  52:    */   {
/*  53: 72 */     return newSharedLeakAwareByteBuf(super.slice(index, length));
/*  54:    */   }
/*  55:    */   
/*  56:    */   public ByteBuf duplicate()
/*  57:    */   {
/*  58: 77 */     return newSharedLeakAwareByteBuf(super.duplicate());
/*  59:    */   }
/*  60:    */   
/*  61:    */   public ByteBuf readSlice(int length)
/*  62:    */   {
/*  63: 82 */     return newSharedLeakAwareByteBuf(super.readSlice(length));
/*  64:    */   }
/*  65:    */   
/*  66:    */   public ByteBuf asReadOnly()
/*  67:    */   {
/*  68: 87 */     return newSharedLeakAwareByteBuf(super.asReadOnly());
/*  69:    */   }
/*  70:    */   
/*  71:    */   public ByteBuf touch()
/*  72:    */   {
/*  73: 92 */     return this;
/*  74:    */   }
/*  75:    */   
/*  76:    */   public ByteBuf touch(Object hint)
/*  77:    */   {
/*  78: 97 */     return this;
/*  79:    */   }
/*  80:    */   
/*  81:    */   public boolean release()
/*  82:    */   {
/*  83:102 */     if (super.release())
/*  84:    */     {
/*  85:103 */       closeLeak();
/*  86:104 */       return true;
/*  87:    */     }
/*  88:106 */     return false;
/*  89:    */   }
/*  90:    */   
/*  91:    */   public boolean release(int decrement)
/*  92:    */   {
/*  93:111 */     if (super.release(decrement))
/*  94:    */     {
/*  95:112 */       closeLeak();
/*  96:113 */       return true;
/*  97:    */     }
/*  98:115 */     return false;
/*  99:    */   }
/* 100:    */   
/* 101:    */   private void closeLeak()
/* 102:    */   {
/* 103:121 */     boolean closed = this.leak.close(this.trackedByteBuf);
/* 104:122 */     assert (closed);
/* 105:    */   }
/* 106:    */   
/* 107:    */   public ByteBuf order(ByteOrder endianness)
/* 108:    */   {
/* 109:127 */     if (order() == endianness) {
/* 110:128 */       return this;
/* 111:    */     }
/* 112:130 */     return newSharedLeakAwareByteBuf(super.order(endianness));
/* 113:    */   }
/* 114:    */   
/* 115:    */   private ByteBuf unwrappedDerived(ByteBuf derived)
/* 116:    */   {
/* 117:137 */     ByteBuf unwrappedDerived = unwrapSwapped(derived);
/* 118:139 */     if ((unwrappedDerived instanceof AbstractPooledDerivedByteBuf))
/* 119:    */     {
/* 120:141 */       ((AbstractPooledDerivedByteBuf)unwrappedDerived).parent(this);
/* 121:    */       
/* 122:143 */       ResourceLeakTracker<ByteBuf> newLeak = AbstractByteBuf.leakDetector.track(derived);
/* 123:144 */       if (newLeak == null) {
/* 124:146 */         return derived;
/* 125:    */       }
/* 126:148 */       return newLeakAwareByteBuf(derived, newLeak);
/* 127:    */     }
/* 128:150 */     return newSharedLeakAwareByteBuf(derived);
/* 129:    */   }
/* 130:    */   
/* 131:    */   private static ByteBuf unwrapSwapped(ByteBuf buf)
/* 132:    */   {
/* 133:155 */     if ((buf instanceof SwappedByteBuf))
/* 134:    */     {
/* 135:    */       do
/* 136:    */       {
/* 137:157 */         buf = buf.unwrap();
/* 138:158 */       } while ((buf instanceof SwappedByteBuf));
/* 139:160 */       return buf;
/* 140:    */     }
/* 141:162 */     return buf;
/* 142:    */   }
/* 143:    */   
/* 144:    */   private SimpleLeakAwareByteBuf newSharedLeakAwareByteBuf(ByteBuf wrapped)
/* 145:    */   {
/* 146:167 */     return newLeakAwareByteBuf(wrapped, this.trackedByteBuf, this.leak);
/* 147:    */   }
/* 148:    */   
/* 149:    */   private SimpleLeakAwareByteBuf newLeakAwareByteBuf(ByteBuf wrapped, ResourceLeakTracker<ByteBuf> leakTracker)
/* 150:    */   {
/* 151:172 */     return newLeakAwareByteBuf(wrapped, wrapped, leakTracker);
/* 152:    */   }
/* 153:    */   
/* 154:    */   protected SimpleLeakAwareByteBuf newLeakAwareByteBuf(ByteBuf buf, ByteBuf trackedByteBuf, ResourceLeakTracker<ByteBuf> leakTracker)
/* 155:    */   {
/* 156:177 */     return new SimpleLeakAwareByteBuf(buf, trackedByteBuf, leakTracker);
/* 157:    */   }
/* 158:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.SimpleLeakAwareByteBuf
 * JD-Core Version:    0.7.0.1
 */