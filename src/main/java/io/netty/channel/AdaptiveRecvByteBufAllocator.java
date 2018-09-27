/*   1:    */ package io.netty.channel;
/*   2:    */ 
/*   3:    */ import java.util.ArrayList;
/*   4:    */ import java.util.List;
/*   5:    */ 
/*   6:    */ public class AdaptiveRecvByteBufAllocator
/*   7:    */   extends DefaultMaxMessagesRecvByteBufAllocator
/*   8:    */ {
/*   9:    */   static final int DEFAULT_MINIMUM = 64;
/*  10:    */   static final int DEFAULT_INITIAL = 1024;
/*  11:    */   static final int DEFAULT_MAXIMUM = 65536;
/*  12:    */   private static final int INDEX_INCREMENT = 4;
/*  13:    */   private static final int INDEX_DECREMENT = 1;
/*  14:    */   private static final int[] SIZE_TABLE;
/*  15:    */   
/*  16:    */   static
/*  17:    */   {
/*  18: 46 */     List<Integer> sizeTable = new ArrayList();
/*  19: 47 */     for (int i = 16; i < 512; i += 16) {
/*  20: 48 */       sizeTable.add(Integer.valueOf(i));
/*  21:    */     }
/*  22: 51 */     for (int i = 512; i > 0; i <<= 1) {
/*  23: 52 */       sizeTable.add(Integer.valueOf(i));
/*  24:    */     }
/*  25: 55 */     SIZE_TABLE = new int[sizeTable.size()];
/*  26: 56 */     for (int i = 0; i < SIZE_TABLE.length; i++) {
/*  27: 57 */       SIZE_TABLE[i] = ((Integer)sizeTable.get(i)).intValue();
/*  28:    */     }
/*  29:    */   }
/*  30:    */   
/*  31:    */   @Deprecated
/*  32: 65 */   public static final AdaptiveRecvByteBufAllocator DEFAULT = new AdaptiveRecvByteBufAllocator();
/*  33:    */   private final int minIndex;
/*  34:    */   private final int maxIndex;
/*  35:    */   private final int initial;
/*  36:    */   
/*  37:    */   private static int getSizeTableIndex(int size)
/*  38:    */   {
/*  39: 68 */     int low = 0;int high = SIZE_TABLE.length - 1;
/*  40:    */     for (;;)
/*  41:    */     {
/*  42: 69 */       if (high < low) {
/*  43: 70 */         return low;
/*  44:    */       }
/*  45: 72 */       if (high == low) {
/*  46: 73 */         return high;
/*  47:    */       }
/*  48: 76 */       int mid = low + high >>> 1;
/*  49: 77 */       int a = SIZE_TABLE[mid];
/*  50: 78 */       int b = SIZE_TABLE[(mid + 1)];
/*  51: 79 */       if (size > b)
/*  52:    */       {
/*  53: 80 */         low = mid + 1;
/*  54:    */       }
/*  55: 81 */       else if (size < a)
/*  56:    */       {
/*  57: 82 */         high = mid - 1;
/*  58:    */       }
/*  59:    */       else
/*  60:    */       {
/*  61: 83 */         if (size == a) {
/*  62: 84 */           return mid;
/*  63:    */         }
/*  64: 86 */         return mid + 1;
/*  65:    */       }
/*  66:    */     }
/*  67:    */   }
/*  68:    */   
/*  69:    */   private final class HandleImpl
/*  70:    */     extends DefaultMaxMessagesRecvByteBufAllocator.MaxMessageHandle
/*  71:    */   {
/*  72:    */     private final int minIndex;
/*  73:    */     private final int maxIndex;
/*  74:    */     private int index;
/*  75:    */     private int nextReceiveBufferSize;
/*  76:    */     private boolean decreaseNow;
/*  77:    */     
/*  78:    */     public HandleImpl(int minIndex, int maxIndex, int initial)
/*  79:    */     {
/*  80: 98 */       super();
/*  81: 99 */       this.minIndex = minIndex;
/*  82:100 */       this.maxIndex = maxIndex;
/*  83:    */       
/*  84:102 */       this.index = AdaptiveRecvByteBufAllocator.getSizeTableIndex(initial);
/*  85:103 */       this.nextReceiveBufferSize = AdaptiveRecvByteBufAllocator.SIZE_TABLE[this.index];
/*  86:    */     }
/*  87:    */     
/*  88:    */     public void lastBytesRead(int bytes)
/*  89:    */     {
/*  90:112 */       if (bytes == attemptedBytesRead()) {
/*  91:113 */         record(bytes);
/*  92:    */       }
/*  93:115 */       super.lastBytesRead(bytes);
/*  94:    */     }
/*  95:    */     
/*  96:    */     public int guess()
/*  97:    */     {
/*  98:120 */       return this.nextReceiveBufferSize;
/*  99:    */     }
/* 100:    */     
/* 101:    */     private void record(int actualReadBytes)
/* 102:    */     {
/* 103:124 */       if (actualReadBytes <= AdaptiveRecvByteBufAllocator.SIZE_TABLE[Math.max(0, this.index - 1 - 1)])
/* 104:    */       {
/* 105:125 */         if (this.decreaseNow)
/* 106:    */         {
/* 107:126 */           this.index = Math.max(this.index - 1, this.minIndex);
/* 108:127 */           this.nextReceiveBufferSize = AdaptiveRecvByteBufAllocator.SIZE_TABLE[this.index];
/* 109:128 */           this.decreaseNow = false;
/* 110:    */         }
/* 111:    */         else
/* 112:    */         {
/* 113:130 */           this.decreaseNow = true;
/* 114:    */         }
/* 115:    */       }
/* 116:132 */       else if (actualReadBytes >= this.nextReceiveBufferSize)
/* 117:    */       {
/* 118:133 */         this.index = Math.min(this.index + 4, this.maxIndex);
/* 119:134 */         this.nextReceiveBufferSize = AdaptiveRecvByteBufAllocator.SIZE_TABLE[this.index];
/* 120:135 */         this.decreaseNow = false;
/* 121:    */       }
/* 122:    */     }
/* 123:    */     
/* 124:    */     public void readComplete()
/* 125:    */     {
/* 126:141 */       record(totalBytesRead());
/* 127:    */     }
/* 128:    */   }
/* 129:    */   
/* 130:    */   public AdaptiveRecvByteBufAllocator()
/* 131:    */   {
/* 132:155 */     this(64, 1024, 65536);
/* 133:    */   }
/* 134:    */   
/* 135:    */   public AdaptiveRecvByteBufAllocator(int minimum, int initial, int maximum)
/* 136:    */   {
/* 137:166 */     if (minimum <= 0) {
/* 138:167 */       throw new IllegalArgumentException("minimum: " + minimum);
/* 139:    */     }
/* 140:169 */     if (initial < minimum) {
/* 141:170 */       throw new IllegalArgumentException("initial: " + initial);
/* 142:    */     }
/* 143:172 */     if (maximum < initial) {
/* 144:173 */       throw new IllegalArgumentException("maximum: " + maximum);
/* 145:    */     }
/* 146:176 */     int minIndex = getSizeTableIndex(minimum);
/* 147:177 */     if (SIZE_TABLE[minIndex] < minimum) {
/* 148:178 */       this.minIndex = (minIndex + 1);
/* 149:    */     } else {
/* 150:180 */       this.minIndex = minIndex;
/* 151:    */     }
/* 152:183 */     int maxIndex = getSizeTableIndex(maximum);
/* 153:184 */     if (SIZE_TABLE[maxIndex] > maximum) {
/* 154:185 */       this.maxIndex = (maxIndex - 1);
/* 155:    */     } else {
/* 156:187 */       this.maxIndex = maxIndex;
/* 157:    */     }
/* 158:190 */     this.initial = initial;
/* 159:    */   }
/* 160:    */   
/* 161:    */   public RecvByteBufAllocator.Handle newHandle()
/* 162:    */   {
/* 163:196 */     return new HandleImpl(this.minIndex, this.maxIndex, this.initial);
/* 164:    */   }
/* 165:    */   
/* 166:    */   public AdaptiveRecvByteBufAllocator respectMaybeMoreData(boolean respectMaybeMoreData)
/* 167:    */   {
/* 168:201 */     super.respectMaybeMoreData(respectMaybeMoreData);
/* 169:202 */     return this;
/* 170:    */   }
/* 171:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.AdaptiveRecvByteBufAllocator
 * JD-Core Version:    0.7.0.1
 */