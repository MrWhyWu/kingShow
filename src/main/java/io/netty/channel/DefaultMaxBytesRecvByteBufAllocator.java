/*   1:    */ package io.netty.channel;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.util.UncheckedBooleanSupplier;
/*   6:    */ import java.util.AbstractMap.SimpleEntry;
/*   7:    */ import java.util.Map.Entry;
/*   8:    */ 
/*   9:    */ public class DefaultMaxBytesRecvByteBufAllocator
/*  10:    */   implements MaxBytesRecvByteBufAllocator
/*  11:    */ {
/*  12:    */   private volatile int maxBytesPerRead;
/*  13:    */   private volatile int maxBytesPerIndividualRead;
/*  14:    */   
/*  15:    */   private final class HandleImpl
/*  16:    */     implements RecvByteBufAllocator.ExtendedHandle
/*  17:    */   {
/*  18:    */     private int individualReadMax;
/*  19:    */     private int bytesToRead;
/*  20:    */     private int lastBytesRead;
/*  21:    */     private int attemptBytesRead;
/*  22: 38 */     private final UncheckedBooleanSupplier defaultMaybeMoreSupplier = new UncheckedBooleanSupplier()
/*  23:    */     {
/*  24:    */       public boolean get()
/*  25:    */       {
/*  26: 41 */         return DefaultMaxBytesRecvByteBufAllocator.HandleImpl.this.attemptBytesRead == DefaultMaxBytesRecvByteBufAllocator.HandleImpl.this.lastBytesRead;
/*  27:    */       }
/*  28:    */     };
/*  29:    */     
/*  30:    */     private HandleImpl() {}
/*  31:    */     
/*  32:    */     public ByteBuf allocate(ByteBufAllocator alloc)
/*  33:    */     {
/*  34: 47 */       return alloc.ioBuffer(guess());
/*  35:    */     }
/*  36:    */     
/*  37:    */     public int guess()
/*  38:    */     {
/*  39: 52 */       return Math.min(this.individualReadMax, this.bytesToRead);
/*  40:    */     }
/*  41:    */     
/*  42:    */     public void reset(ChannelConfig config)
/*  43:    */     {
/*  44: 57 */       this.bytesToRead = DefaultMaxBytesRecvByteBufAllocator.this.maxBytesPerRead();
/*  45: 58 */       this.individualReadMax = DefaultMaxBytesRecvByteBufAllocator.this.maxBytesPerIndividualRead();
/*  46:    */     }
/*  47:    */     
/*  48:    */     public void incMessagesRead(int amt) {}
/*  49:    */     
/*  50:    */     public void lastBytesRead(int bytes)
/*  51:    */     {
/*  52: 67 */       this.lastBytesRead = bytes;
/*  53:    */       
/*  54:    */ 
/*  55: 70 */       this.bytesToRead -= bytes;
/*  56:    */     }
/*  57:    */     
/*  58:    */     public int lastBytesRead()
/*  59:    */     {
/*  60: 75 */       return this.lastBytesRead;
/*  61:    */     }
/*  62:    */     
/*  63:    */     public boolean continueReading()
/*  64:    */     {
/*  65: 80 */       return continueReading(this.defaultMaybeMoreSupplier);
/*  66:    */     }
/*  67:    */     
/*  68:    */     public boolean continueReading(UncheckedBooleanSupplier maybeMoreDataSupplier)
/*  69:    */     {
/*  70: 86 */       return (this.bytesToRead > 0) && (maybeMoreDataSupplier.get());
/*  71:    */     }
/*  72:    */     
/*  73:    */     public void readComplete() {}
/*  74:    */     
/*  75:    */     public void attemptedBytesRead(int bytes)
/*  76:    */     {
/*  77: 95 */       this.attemptBytesRead = bytes;
/*  78:    */     }
/*  79:    */     
/*  80:    */     public int attemptedBytesRead()
/*  81:    */     {
/*  82:100 */       return this.attemptBytesRead;
/*  83:    */     }
/*  84:    */   }
/*  85:    */   
/*  86:    */   public DefaultMaxBytesRecvByteBufAllocator()
/*  87:    */   {
/*  88:105 */     this(65536, 65536);
/*  89:    */   }
/*  90:    */   
/*  91:    */   public DefaultMaxBytesRecvByteBufAllocator(int maxBytesPerRead, int maxBytesPerIndividualRead)
/*  92:    */   {
/*  93:109 */     checkMaxBytesPerReadPair(maxBytesPerRead, maxBytesPerIndividualRead);
/*  94:110 */     this.maxBytesPerRead = maxBytesPerRead;
/*  95:111 */     this.maxBytesPerIndividualRead = maxBytesPerIndividualRead;
/*  96:    */   }
/*  97:    */   
/*  98:    */   public RecvByteBufAllocator.Handle newHandle()
/*  99:    */   {
/* 100:117 */     return new HandleImpl(null);
/* 101:    */   }
/* 102:    */   
/* 103:    */   public int maxBytesPerRead()
/* 104:    */   {
/* 105:122 */     return this.maxBytesPerRead;
/* 106:    */   }
/* 107:    */   
/* 108:    */   public DefaultMaxBytesRecvByteBufAllocator maxBytesPerRead(int maxBytesPerRead)
/* 109:    */   {
/* 110:127 */     if (maxBytesPerRead <= 0) {
/* 111:128 */       throw new IllegalArgumentException("maxBytesPerRead: " + maxBytesPerRead + " (expected: > 0)");
/* 112:    */     }
/* 113:132 */     synchronized (this)
/* 114:    */     {
/* 115:133 */       int maxBytesPerIndividualRead = maxBytesPerIndividualRead();
/* 116:134 */       if (maxBytesPerRead < maxBytesPerIndividualRead) {
/* 117:135 */         throw new IllegalArgumentException("maxBytesPerRead cannot be less than maxBytesPerIndividualRead (" + maxBytesPerIndividualRead + "): " + maxBytesPerRead);
/* 118:    */       }
/* 119:140 */       this.maxBytesPerRead = maxBytesPerRead;
/* 120:    */     }
/* 121:142 */     return this;
/* 122:    */   }
/* 123:    */   
/* 124:    */   public int maxBytesPerIndividualRead()
/* 125:    */   {
/* 126:147 */     return this.maxBytesPerIndividualRead;
/* 127:    */   }
/* 128:    */   
/* 129:    */   public DefaultMaxBytesRecvByteBufAllocator maxBytesPerIndividualRead(int maxBytesPerIndividualRead)
/* 130:    */   {
/* 131:152 */     if (maxBytesPerIndividualRead <= 0) {
/* 132:153 */       throw new IllegalArgumentException("maxBytesPerIndividualRead: " + maxBytesPerIndividualRead + " (expected: > 0)");
/* 133:    */     }
/* 134:158 */     synchronized (this)
/* 135:    */     {
/* 136:159 */       int maxBytesPerRead = maxBytesPerRead();
/* 137:160 */       if (maxBytesPerIndividualRead > maxBytesPerRead) {
/* 138:161 */         throw new IllegalArgumentException("maxBytesPerIndividualRead cannot be greater than maxBytesPerRead (" + maxBytesPerRead + "): " + maxBytesPerIndividualRead);
/* 139:    */       }
/* 140:166 */       this.maxBytesPerIndividualRead = maxBytesPerIndividualRead;
/* 141:    */     }
/* 142:168 */     return this;
/* 143:    */   }
/* 144:    */   
/* 145:    */   public synchronized Map.Entry<Integer, Integer> maxBytesPerReadPair()
/* 146:    */   {
/* 147:173 */     return new AbstractMap.SimpleEntry(Integer.valueOf(this.maxBytesPerRead), Integer.valueOf(this.maxBytesPerIndividualRead));
/* 148:    */   }
/* 149:    */   
/* 150:    */   private static void checkMaxBytesPerReadPair(int maxBytesPerRead, int maxBytesPerIndividualRead)
/* 151:    */   {
/* 152:177 */     if (maxBytesPerRead <= 0) {
/* 153:178 */       throw new IllegalArgumentException("maxBytesPerRead: " + maxBytesPerRead + " (expected: > 0)");
/* 154:    */     }
/* 155:180 */     if (maxBytesPerIndividualRead <= 0) {
/* 156:181 */       throw new IllegalArgumentException("maxBytesPerIndividualRead: " + maxBytesPerIndividualRead + " (expected: > 0)");
/* 157:    */     }
/* 158:184 */     if (maxBytesPerRead < maxBytesPerIndividualRead) {
/* 159:185 */       throw new IllegalArgumentException("maxBytesPerRead cannot be less than maxBytesPerIndividualRead (" + maxBytesPerIndividualRead + "): " + maxBytesPerRead);
/* 160:    */     }
/* 161:    */   }
/* 162:    */   
/* 163:    */   public DefaultMaxBytesRecvByteBufAllocator maxBytesPerReadPair(int maxBytesPerRead, int maxBytesPerIndividualRead)
/* 164:    */   {
/* 165:194 */     checkMaxBytesPerReadPair(maxBytesPerRead, maxBytesPerIndividualRead);
/* 166:197 */     synchronized (this)
/* 167:    */     {
/* 168:198 */       this.maxBytesPerRead = maxBytesPerRead;
/* 169:199 */       this.maxBytesPerIndividualRead = maxBytesPerIndividualRead;
/* 170:    */     }
/* 171:201 */     return this;
/* 172:    */   }
/* 173:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.DefaultMaxBytesRecvByteBufAllocator
 * JD-Core Version:    0.7.0.1
 */