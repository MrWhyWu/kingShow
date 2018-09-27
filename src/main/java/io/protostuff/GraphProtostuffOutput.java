/*   1:    */ package io.protostuff;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import java.util.Map.Entry;
/*   5:    */ 
/*   6:    */ public final class GraphProtostuffOutput
/*   7:    */   extends FilterOutput<ProtostuffOutput>
/*   8:    */ {
/*   9:    */   private final IdentityMap references;
/*  10: 35 */   private int refCount = 0;
/*  11:    */   
/*  12:    */   public GraphProtostuffOutput(ProtostuffOutput output)
/*  13:    */   {
/*  14: 39 */     super(output);
/*  15: 40 */     this.references = new IdentityMap();
/*  16:    */   }
/*  17:    */   
/*  18:    */   public GraphProtostuffOutput(ProtostuffOutput output, int initialCapacity)
/*  19:    */   {
/*  20: 45 */     super(output);
/*  21: 46 */     this.references = new IdentityMap(initialCapacity);
/*  22:    */   }
/*  23:    */   
/*  24:    */   public <T> void writeObject(int fieldNumber, T value, Schema<T> schema, boolean repeated)
/*  25:    */     throws IOException
/*  26:    */   {
/*  27: 53 */     ProtostuffOutput output = (ProtostuffOutput)this.output;
/*  28: 55 */     if (this.references.shouldIncrement(this.refCount, value, output, fieldNumber))
/*  29:    */     {
/*  30: 57 */       this.refCount += 1;
/*  31:    */       
/*  32: 59 */       output.tail = output.sink.writeVarInt32(
/*  33: 60 */         WireFormat.makeTag(fieldNumber, 3), output, output.tail);
/*  34:    */       
/*  35:    */ 
/*  36:    */ 
/*  37: 64 */       schema.writeTo(this, value);
/*  38:    */       
/*  39: 66 */       output.tail = output.sink.writeVarInt32(
/*  40: 67 */         WireFormat.makeTag(fieldNumber, 4), output, output.tail);
/*  41:    */     }
/*  42:    */   }
/*  43:    */   
/*  44:    */   private static final class IdentityMap
/*  45:    */   {
/*  46:    */     private static final int DEFAULT_CAPACITY = 32;
/*  47:    */     private static final int MINIMUM_CAPACITY = 4;
/*  48:    */     private static final int MAXIMUM_CAPACITY = 536870912;
/*  49:    */     private transient Object[] table;
/*  50:    */     private int size;
/*  51:    */     private transient int threshold;
/*  52:    */     
/*  53:    */     public IdentityMap()
/*  54:    */     {
/*  55:125 */       init(32);
/*  56:    */     }
/*  57:    */     
/*  58:    */     public IdentityMap(int expectedMaxSize)
/*  59:    */     {
/*  60:140 */       if (expectedMaxSize < 0) {
/*  61:141 */         throw new IllegalArgumentException("expectedMaxSize is negative: " + expectedMaxSize);
/*  62:    */       }
/*  63:142 */       init(capacity(expectedMaxSize));
/*  64:    */     }
/*  65:    */     
/*  66:    */     private int capacity(int expectedMaxSize)
/*  67:    */     {
/*  68:154 */       int minCapacity = 3 * expectedMaxSize / 2;
/*  69:    */       int result;
/*  70:    */       int result;
/*  71:158 */       if ((minCapacity > 536870912) || (minCapacity < 0))
/*  72:    */       {
/*  73:160 */         result = 536870912;
/*  74:    */       }
/*  75:    */       else
/*  76:    */       {
/*  77:164 */         result = 4;
/*  78:165 */         while (result < minCapacity) {
/*  79:166 */           result <<= 1;
/*  80:    */         }
/*  81:    */       }
/*  82:168 */       return result;
/*  83:    */     }
/*  84:    */     
/*  85:    */     private void init(int initCapacity)
/*  86:    */     {
/*  87:181 */       this.threshold = (initCapacity * 2 / 3);
/*  88:182 */       this.table = new Object[2 * initCapacity];
/*  89:    */     }
/*  90:    */     
/*  91:    */     private static int hash(Object x, int length)
/*  92:    */     {
/*  93:190 */       int h = System.identityHashCode(x);
/*  94:    */       
/*  95:192 */       return (h << 1) - (h << 8) & length - 1;
/*  96:    */     }
/*  97:    */     
/*  98:    */     private static int nextKeyIndex(int i, int len)
/*  99:    */     {
/* 100:200 */       return i + 2 < len ? i + 2 : 0;
/* 101:    */     }
/* 102:    */     
/* 103:    */     public boolean shouldIncrement(int value, Object k, WriteSession output, int fieldNumber)
/* 104:    */       throws IOException
/* 105:    */     {
/* 106:254 */       Object[] tab = this.table;
/* 107:255 */       int len = tab.length;
/* 108:256 */       int i = hash(k, len);
/* 109:    */       Object item;
/* 110:259 */       while ((item = tab[i]) != null)
/* 111:    */       {
/* 112:261 */         if (item == k)
/* 113:    */         {
/* 114:263 */           if ((k instanceof Map.Entry)) {
/* 115:265 */             if (k.getClass().getName().startsWith("java.util")) {
/* 116:269 */               return true;
/* 117:    */             }
/* 118:    */           }
/* 119:272 */           output.tail = output.sink.writeVarInt32(((Integer)tab[(i + 1)])
/* 120:273 */             .intValue(), output, output.sink
/* 121:    */             
/* 122:275 */             .writeVarInt32(
/* 123:276 */             WireFormat.makeTag(fieldNumber, 6), output, output.tail));
/* 124:    */           
/* 125:    */ 
/* 126:    */ 
/* 127:280 */           return false;
/* 128:    */         }
/* 129:282 */         i = nextKeyIndex(i, len);
/* 130:    */       }
/* 131:286 */       tab[i] = k;
/* 132:287 */       tab[(i + 1)] = Integer.valueOf(value);
/* 133:288 */       if (++this.size >= this.threshold) {
/* 134:289 */         resize(len);
/* 135:    */       }
/* 136:290 */       return true;
/* 137:    */     }
/* 138:    */     
/* 139:    */     private void resize(int newCapacity)
/* 140:    */     {
/* 141:302 */       int newLength = newCapacity * 2;
/* 142:    */       
/* 143:304 */       Object[] oldTable = this.table;
/* 144:305 */       int oldLength = oldTable.length;
/* 145:306 */       if (oldLength == 1073741824)
/* 146:    */       {
/* 147:308 */         if (this.threshold == 536870911) {
/* 148:309 */           throw new IllegalStateException("Capacity exhausted.");
/* 149:    */         }
/* 150:310 */         this.threshold = 536870911;
/* 151:311 */         return;
/* 152:    */       }
/* 153:313 */       if (oldLength >= newLength) {
/* 154:314 */         return;
/* 155:    */       }
/* 156:316 */       Object[] newTable = new Object[newLength];
/* 157:317 */       this.threshold = (newLength / 3);
/* 158:319 */       for (int j = 0; j < oldLength; j += 2)
/* 159:    */       {
/* 160:321 */         Object key = oldTable[j];
/* 161:322 */         if (key != null)
/* 162:    */         {
/* 163:324 */           Object value = oldTable[(j + 1)];
/* 164:325 */           oldTable[j] = null;
/* 165:326 */           oldTable[(j + 1)] = null;
/* 166:327 */           int i = hash(key, newLength);
/* 167:328 */           while (newTable[i] != null) {
/* 168:329 */             i = nextKeyIndex(i, newLength);
/* 169:    */           }
/* 170:330 */           newTable[i] = key;
/* 171:331 */           newTable[(i + 1)] = value;
/* 172:    */         }
/* 173:    */       }
/* 174:334 */       this.table = newTable;
/* 175:    */     }
/* 176:    */   }
/* 177:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.GraphProtostuffOutput
 * JD-Core Version:    0.7.0.1
 */