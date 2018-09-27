/*   1:    */ package io.netty.handler.codec;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.ObjectUtil;
/*   4:    */ import java.util.AbstractCollection;
/*   5:    */ import java.util.AbstractList;
/*   6:    */ import java.util.Collection;
/*   7:    */ import java.util.Iterator;
/*   8:    */ import java.util.List;
/*   9:    */ import java.util.Map.Entry;
/*  10:    */ import java.util.Set;
/*  11:    */ 
/*  12:    */ public final class HeadersUtils
/*  13:    */ {
/*  14:    */   public static <K, V> List<String> getAllAsString(Headers<K, V, ?> headers, K name)
/*  15:    */   {
/*  16: 42 */     List<V> allNames = headers.getAll(name);
/*  17: 43 */     new AbstractList()
/*  18:    */     {
/*  19:    */       public String get(int index)
/*  20:    */       {
/*  21: 46 */         V value = this.val$allNames.get(index);
/*  22: 47 */         return value != null ? value.toString() : null;
/*  23:    */       }
/*  24:    */       
/*  25:    */       public int size()
/*  26:    */       {
/*  27: 52 */         return this.val$allNames.size();
/*  28:    */       }
/*  29:    */     };
/*  30:    */   }
/*  31:    */   
/*  32:    */   public static <K, V> String getAsString(Headers<K, V, ?> headers, K name)
/*  33:    */   {
/*  34: 64 */     V orig = headers.get(name);
/*  35: 65 */     return orig != null ? orig.toString() : null;
/*  36:    */   }
/*  37:    */   
/*  38:    */   public static Iterator<Map.Entry<String, String>> iteratorAsString(Iterable<Map.Entry<CharSequence, CharSequence>> headers)
/*  39:    */   {
/*  40: 73 */     return new StringEntryIterator(headers.iterator());
/*  41:    */   }
/*  42:    */   
/*  43:    */   public static <K, V> String toString(Class<?> headersClass, Iterator<Map.Entry<K, V>> headersIt, int size)
/*  44:    */   {
/*  45: 84 */     String simpleName = headersClass.getSimpleName();
/*  46: 85 */     if (size == 0) {
/*  47: 86 */       return simpleName + "[]";
/*  48:    */     }
/*  49: 91 */     StringBuilder sb = new StringBuilder(simpleName.length() + 2 + size * 20).append(simpleName).append('[');
/*  50: 92 */     while (headersIt.hasNext())
/*  51:    */     {
/*  52: 93 */       Map.Entry<?, ?> header = (Map.Entry)headersIt.next();
/*  53: 94 */       sb.append(header.getKey()).append(": ").append(header.getValue()).append(", ");
/*  54:    */     }
/*  55: 96 */     sb.setLength(sb.length() - 2);
/*  56: 97 */     return ']';
/*  57:    */   }
/*  58:    */   
/*  59:    */   public static Set<String> namesAsString(Headers<CharSequence, CharSequence, ?> headers)
/*  60:    */   {
/*  61:107 */     return new CharSequenceDelegatingStringSet(headers.names());
/*  62:    */   }
/*  63:    */   
/*  64:    */   private static final class StringEntryIterator
/*  65:    */     implements Iterator<Map.Entry<String, String>>
/*  66:    */   {
/*  67:    */     private final Iterator<Map.Entry<CharSequence, CharSequence>> iter;
/*  68:    */     
/*  69:    */     StringEntryIterator(Iterator<Map.Entry<CharSequence, CharSequence>> iter)
/*  70:    */     {
/*  71:114 */       this.iter = iter;
/*  72:    */     }
/*  73:    */     
/*  74:    */     public boolean hasNext()
/*  75:    */     {
/*  76:119 */       return this.iter.hasNext();
/*  77:    */     }
/*  78:    */     
/*  79:    */     public Map.Entry<String, String> next()
/*  80:    */     {
/*  81:124 */       return new HeadersUtils.StringEntry((Map.Entry)this.iter.next());
/*  82:    */     }
/*  83:    */     
/*  84:    */     public void remove()
/*  85:    */     {
/*  86:129 */       this.iter.remove();
/*  87:    */     }
/*  88:    */   }
/*  89:    */   
/*  90:    */   private static final class StringEntry
/*  91:    */     implements Map.Entry<String, String>
/*  92:    */   {
/*  93:    */     private final Map.Entry<CharSequence, CharSequence> entry;
/*  94:    */     private String name;
/*  95:    */     private String value;
/*  96:    */     
/*  97:    */     StringEntry(Map.Entry<CharSequence, CharSequence> entry)
/*  98:    */     {
/*  99:139 */       this.entry = entry;
/* 100:    */     }
/* 101:    */     
/* 102:    */     public String getKey()
/* 103:    */     {
/* 104:144 */       if (this.name == null) {
/* 105:145 */         this.name = ((CharSequence)this.entry.getKey()).toString();
/* 106:    */       }
/* 107:147 */       return this.name;
/* 108:    */     }
/* 109:    */     
/* 110:    */     public String getValue()
/* 111:    */     {
/* 112:152 */       if ((this.value == null) && (this.entry.getValue() != null)) {
/* 113:153 */         this.value = ((CharSequence)this.entry.getValue()).toString();
/* 114:    */       }
/* 115:155 */       return this.value;
/* 116:    */     }
/* 117:    */     
/* 118:    */     public String setValue(String value)
/* 119:    */     {
/* 120:160 */       String old = getValue();
/* 121:161 */       this.entry.setValue(value);
/* 122:162 */       return old;
/* 123:    */     }
/* 124:    */     
/* 125:    */     public String toString()
/* 126:    */     {
/* 127:167 */       return this.entry.toString();
/* 128:    */     }
/* 129:    */   }
/* 130:    */   
/* 131:    */   private static final class StringIterator<T>
/* 132:    */     implements Iterator<String>
/* 133:    */   {
/* 134:    */     private final Iterator<T> iter;
/* 135:    */     
/* 136:    */     StringIterator(Iterator<T> iter)
/* 137:    */     {
/* 138:175 */       this.iter = iter;
/* 139:    */     }
/* 140:    */     
/* 141:    */     public boolean hasNext()
/* 142:    */     {
/* 143:180 */       return this.iter.hasNext();
/* 144:    */     }
/* 145:    */     
/* 146:    */     public String next()
/* 147:    */     {
/* 148:185 */       T next = this.iter.next();
/* 149:186 */       return next != null ? next.toString() : null;
/* 150:    */     }
/* 151:    */     
/* 152:    */     public void remove()
/* 153:    */     {
/* 154:191 */       this.iter.remove();
/* 155:    */     }
/* 156:    */   }
/* 157:    */   
/* 158:    */   private static final class CharSequenceDelegatingStringSet
/* 159:    */     extends HeadersUtils.DelegatingStringSet<CharSequence>
/* 160:    */   {
/* 161:    */     CharSequenceDelegatingStringSet(Set<CharSequence> allNames)
/* 162:    */     {
/* 163:197 */       super();
/* 164:    */     }
/* 165:    */     
/* 166:    */     public boolean add(String e)
/* 167:    */     {
/* 168:202 */       return this.allNames.add(e);
/* 169:    */     }
/* 170:    */     
/* 171:    */     public boolean addAll(Collection<? extends String> c)
/* 172:    */     {
/* 173:207 */       return this.allNames.addAll(c);
/* 174:    */     }
/* 175:    */   }
/* 176:    */   
/* 177:    */   private static abstract class DelegatingStringSet<T>
/* 178:    */     extends AbstractCollection<String>
/* 179:    */     implements Set<String>
/* 180:    */   {
/* 181:    */     protected final Set<T> allNames;
/* 182:    */     
/* 183:    */     DelegatingStringSet(Set<T> allNames)
/* 184:    */     {
/* 185:215 */       this.allNames = ((Set)ObjectUtil.checkNotNull(allNames, "allNames"));
/* 186:    */     }
/* 187:    */     
/* 188:    */     public int size()
/* 189:    */     {
/* 190:220 */       return this.allNames.size();
/* 191:    */     }
/* 192:    */     
/* 193:    */     public boolean isEmpty()
/* 194:    */     {
/* 195:225 */       return this.allNames.isEmpty();
/* 196:    */     }
/* 197:    */     
/* 198:    */     public boolean contains(Object o)
/* 199:    */     {
/* 200:230 */       return this.allNames.contains(o.toString());
/* 201:    */     }
/* 202:    */     
/* 203:    */     public Iterator<String> iterator()
/* 204:    */     {
/* 205:235 */       return new HeadersUtils.StringIterator(this.allNames.iterator());
/* 206:    */     }
/* 207:    */     
/* 208:    */     public boolean remove(Object o)
/* 209:    */     {
/* 210:240 */       return this.allNames.remove(o);
/* 211:    */     }
/* 212:    */     
/* 213:    */     public void clear()
/* 214:    */     {
/* 215:245 */       this.allNames.clear();
/* 216:    */     }
/* 217:    */   }
/* 218:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.HeadersUtils
 * JD-Core Version:    0.7.0.1
 */