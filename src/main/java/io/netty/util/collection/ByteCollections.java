/*   1:    */ package io.netty.util.collection;
/*   2:    */ 
/*   3:    */ import java.util.Collection;
/*   4:    */ import java.util.Collections;
/*   5:    */ import java.util.Iterator;
/*   6:    */ import java.util.Map;
/*   7:    */ import java.util.Map.Entry;
/*   8:    */ import java.util.NoSuchElementException;
/*   9:    */ import java.util.Set;
/*  10:    */ 
/*  11:    */ public final class ByteCollections
/*  12:    */ {
/*  13: 29 */   private static final ByteObjectMap<Object> EMPTY_MAP = new EmptyMap(null);
/*  14:    */   
/*  15:    */   public static <V> ByteObjectMap<V> emptyMap()
/*  16:    */   {
/*  17: 39 */     return EMPTY_MAP;
/*  18:    */   }
/*  19:    */   
/*  20:    */   public static <V> ByteObjectMap<V> unmodifiableMap(ByteObjectMap<V> map)
/*  21:    */   {
/*  22: 46 */     return new UnmodifiableMap(map);
/*  23:    */   }
/*  24:    */   
/*  25:    */   private static final class EmptyMap
/*  26:    */     implements ByteObjectMap<Object>
/*  27:    */   {
/*  28:    */     public Object get(byte key)
/*  29:    */     {
/*  30: 55 */       return null;
/*  31:    */     }
/*  32:    */     
/*  33:    */     public Object put(byte key, Object value)
/*  34:    */     {
/*  35: 60 */       throw new UnsupportedOperationException("put");
/*  36:    */     }
/*  37:    */     
/*  38:    */     public Object remove(byte key)
/*  39:    */     {
/*  40: 65 */       return null;
/*  41:    */     }
/*  42:    */     
/*  43:    */     public int size()
/*  44:    */     {
/*  45: 70 */       return 0;
/*  46:    */     }
/*  47:    */     
/*  48:    */     public boolean isEmpty()
/*  49:    */     {
/*  50: 75 */       return true;
/*  51:    */     }
/*  52:    */     
/*  53:    */     public boolean containsKey(Object key)
/*  54:    */     {
/*  55: 80 */       return false;
/*  56:    */     }
/*  57:    */     
/*  58:    */     public void clear() {}
/*  59:    */     
/*  60:    */     public Set<Byte> keySet()
/*  61:    */     {
/*  62: 90 */       return Collections.emptySet();
/*  63:    */     }
/*  64:    */     
/*  65:    */     public boolean containsKey(byte key)
/*  66:    */     {
/*  67: 95 */       return false;
/*  68:    */     }
/*  69:    */     
/*  70:    */     public boolean containsValue(Object value)
/*  71:    */     {
/*  72:100 */       return false;
/*  73:    */     }
/*  74:    */     
/*  75:    */     public Iterable<ByteObjectMap.PrimitiveEntry<Object>> entries()
/*  76:    */     {
/*  77:105 */       return Collections.emptySet();
/*  78:    */     }
/*  79:    */     
/*  80:    */     public Object get(Object key)
/*  81:    */     {
/*  82:110 */       return null;
/*  83:    */     }
/*  84:    */     
/*  85:    */     public Object put(Byte key, Object value)
/*  86:    */     {
/*  87:115 */       throw new UnsupportedOperationException();
/*  88:    */     }
/*  89:    */     
/*  90:    */     public Object remove(Object key)
/*  91:    */     {
/*  92:120 */       return null;
/*  93:    */     }
/*  94:    */     
/*  95:    */     public void putAll(Map<? extends Byte, ?> m)
/*  96:    */     {
/*  97:125 */       throw new UnsupportedOperationException();
/*  98:    */     }
/*  99:    */     
/* 100:    */     public Collection<Object> values()
/* 101:    */     {
/* 102:130 */       return Collections.emptyList();
/* 103:    */     }
/* 104:    */     
/* 105:    */     public Set<Map.Entry<Byte, Object>> entrySet()
/* 106:    */     {
/* 107:135 */       return Collections.emptySet();
/* 108:    */     }
/* 109:    */   }
/* 110:    */   
/* 111:    */   private static final class UnmodifiableMap<V>
/* 112:    */     implements ByteObjectMap<V>
/* 113:    */   {
/* 114:    */     private final ByteObjectMap<V> map;
/* 115:    */     private Set<Byte> keySet;
/* 116:    */     private Set<Map.Entry<Byte, V>> entrySet;
/* 117:    */     private Collection<V> values;
/* 118:    */     private Iterable<ByteObjectMap.PrimitiveEntry<V>> entries;
/* 119:    */     
/* 120:    */     UnmodifiableMap(ByteObjectMap<V> map)
/* 121:    */     {
/* 122:152 */       this.map = map;
/* 123:    */     }
/* 124:    */     
/* 125:    */     public V get(byte key)
/* 126:    */     {
/* 127:157 */       return this.map.get(key);
/* 128:    */     }
/* 129:    */     
/* 130:    */     public V put(byte key, V value)
/* 131:    */     {
/* 132:162 */       throw new UnsupportedOperationException("put");
/* 133:    */     }
/* 134:    */     
/* 135:    */     public V remove(byte key)
/* 136:    */     {
/* 137:167 */       throw new UnsupportedOperationException("remove");
/* 138:    */     }
/* 139:    */     
/* 140:    */     public int size()
/* 141:    */     {
/* 142:172 */       return this.map.size();
/* 143:    */     }
/* 144:    */     
/* 145:    */     public boolean isEmpty()
/* 146:    */     {
/* 147:177 */       return this.map.isEmpty();
/* 148:    */     }
/* 149:    */     
/* 150:    */     public void clear()
/* 151:    */     {
/* 152:182 */       throw new UnsupportedOperationException("clear");
/* 153:    */     }
/* 154:    */     
/* 155:    */     public boolean containsKey(byte key)
/* 156:    */     {
/* 157:187 */       return this.map.containsKey(key);
/* 158:    */     }
/* 159:    */     
/* 160:    */     public boolean containsValue(Object value)
/* 161:    */     {
/* 162:192 */       return this.map.containsValue(value);
/* 163:    */     }
/* 164:    */     
/* 165:    */     public boolean containsKey(Object key)
/* 166:    */     {
/* 167:197 */       return this.map.containsKey(key);
/* 168:    */     }
/* 169:    */     
/* 170:    */     public V get(Object key)
/* 171:    */     {
/* 172:202 */       return this.map.get(key);
/* 173:    */     }
/* 174:    */     
/* 175:    */     public V put(Byte key, V value)
/* 176:    */     {
/* 177:207 */       throw new UnsupportedOperationException("put");
/* 178:    */     }
/* 179:    */     
/* 180:    */     public V remove(Object key)
/* 181:    */     {
/* 182:212 */       throw new UnsupportedOperationException("remove");
/* 183:    */     }
/* 184:    */     
/* 185:    */     public void putAll(Map<? extends Byte, ? extends V> m)
/* 186:    */     {
/* 187:217 */       throw new UnsupportedOperationException("putAll");
/* 188:    */     }
/* 189:    */     
/* 190:    */     public Iterable<ByteObjectMap.PrimitiveEntry<V>> entries()
/* 191:    */     {
/* 192:222 */       if (this.entries == null) {
/* 193:223 */         this.entries = new Iterable()
/* 194:    */         {
/* 195:    */           public Iterator<ByteObjectMap.PrimitiveEntry<V>> iterator()
/* 196:    */           {
/* 197:226 */             return new ByteCollections.UnmodifiableMap.IteratorImpl(ByteCollections.UnmodifiableMap.this, ByteCollections.UnmodifiableMap.this.map.entries().iterator());
/* 198:    */           }
/* 199:    */         };
/* 200:    */       }
/* 201:231 */       return this.entries;
/* 202:    */     }
/* 203:    */     
/* 204:    */     public Set<Byte> keySet()
/* 205:    */     {
/* 206:236 */       if (this.keySet == null) {
/* 207:237 */         this.keySet = Collections.unmodifiableSet(this.map.keySet());
/* 208:    */       }
/* 209:239 */       return this.keySet;
/* 210:    */     }
/* 211:    */     
/* 212:    */     public Set<Map.Entry<Byte, V>> entrySet()
/* 213:    */     {
/* 214:244 */       if (this.entrySet == null) {
/* 215:245 */         this.entrySet = Collections.unmodifiableSet(this.map.entrySet());
/* 216:    */       }
/* 217:247 */       return this.entrySet;
/* 218:    */     }
/* 219:    */     
/* 220:    */     public Collection<V> values()
/* 221:    */     {
/* 222:252 */       if (this.values == null) {
/* 223:253 */         this.values = Collections.unmodifiableCollection(this.map.values());
/* 224:    */       }
/* 225:255 */       return this.values;
/* 226:    */     }
/* 227:    */     
/* 228:    */     private class IteratorImpl
/* 229:    */       implements Iterator<ByteObjectMap.PrimitiveEntry<V>>
/* 230:    */     {
/* 231:    */       final Iterator<ByteObjectMap.PrimitiveEntry<V>> iter;
/* 232:    */       
/* 233:    */       IteratorImpl()
/* 234:    */       {
/* 235:265 */         this.iter = iter;
/* 236:    */       }
/* 237:    */       
/* 238:    */       public boolean hasNext()
/* 239:    */       {
/* 240:270 */         return this.iter.hasNext();
/* 241:    */       }
/* 242:    */       
/* 243:    */       public ByteObjectMap.PrimitiveEntry<V> next()
/* 244:    */       {
/* 245:275 */         if (!hasNext()) {
/* 246:276 */           throw new NoSuchElementException();
/* 247:    */         }
/* 248:278 */         return new ByteCollections.UnmodifiableMap.EntryImpl(ByteCollections.UnmodifiableMap.this, (ByteObjectMap.PrimitiveEntry)this.iter.next());
/* 249:    */       }
/* 250:    */       
/* 251:    */       public void remove()
/* 252:    */       {
/* 253:283 */         throw new UnsupportedOperationException("remove");
/* 254:    */       }
/* 255:    */     }
/* 256:    */     
/* 257:    */     private class EntryImpl
/* 258:    */       implements ByteObjectMap.PrimitiveEntry<V>
/* 259:    */     {
/* 260:    */       private final ByteObjectMap.PrimitiveEntry<V> entry;
/* 261:    */       
/* 262:    */       EntryImpl()
/* 263:    */       {
/* 264:294 */         this.entry = entry;
/* 265:    */       }
/* 266:    */       
/* 267:    */       public byte key()
/* 268:    */       {
/* 269:299 */         return this.entry.key();
/* 270:    */       }
/* 271:    */       
/* 272:    */       public V value()
/* 273:    */       {
/* 274:304 */         return this.entry.value();
/* 275:    */       }
/* 276:    */       
/* 277:    */       public void setValue(V value)
/* 278:    */       {
/* 279:309 */         throw new UnsupportedOperationException("setValue");
/* 280:    */       }
/* 281:    */     }
/* 282:    */   }
/* 283:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.collection.ByteCollections
 * JD-Core Version:    0.7.0.1
 */