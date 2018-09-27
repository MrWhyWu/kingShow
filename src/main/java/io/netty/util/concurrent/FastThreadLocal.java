/*   1:    */ package io.netty.util.concurrent;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.InternalThreadLocalMap;
/*   4:    */ import io.netty.util.internal.PlatformDependent;
/*   5:    */ import java.util.Collections;
/*   6:    */ import java.util.IdentityHashMap;
/*   7:    */ import java.util.Set;
/*   8:    */ 
/*   9:    */ public class FastThreadLocal<V>
/*  10:    */ {
/*  11: 46 */   private static final int variablesToRemoveIndex = ;
/*  12:    */   private final int index;
/*  13:    */   
/*  14:    */   public static void removeAll()
/*  15:    */   {
/*  16: 54 */     InternalThreadLocalMap threadLocalMap = InternalThreadLocalMap.getIfSet();
/*  17: 55 */     if (threadLocalMap == null) {
/*  18: 56 */       return;
/*  19:    */     }
/*  20:    */     try
/*  21:    */     {
/*  22: 60 */       Object v = threadLocalMap.indexedVariable(variablesToRemoveIndex);
/*  23: 61 */       if ((v != null) && (v != InternalThreadLocalMap.UNSET))
/*  24:    */       {
/*  25: 63 */         Set<FastThreadLocal<?>> variablesToRemove = (Set)v;
/*  26:    */         
/*  27: 65 */         FastThreadLocal<?>[] variablesToRemoveArray = (FastThreadLocal[])variablesToRemove.toArray(new FastThreadLocal[variablesToRemove.size()]);
/*  28: 66 */         for (FastThreadLocal<?> tlv : variablesToRemoveArray) {
/*  29: 67 */           tlv.remove(threadLocalMap);
/*  30:    */         }
/*  31:    */       }
/*  32:    */     }
/*  33:    */     finally
/*  34:    */     {
/*  35: 71 */       InternalThreadLocalMap.remove();
/*  36:    */     }
/*  37:    */   }
/*  38:    */   
/*  39:    */   public static int size()
/*  40:    */   {
/*  41: 79 */     InternalThreadLocalMap threadLocalMap = InternalThreadLocalMap.getIfSet();
/*  42: 80 */     if (threadLocalMap == null) {
/*  43: 81 */       return 0;
/*  44:    */     }
/*  45: 83 */     return threadLocalMap.size();
/*  46:    */   }
/*  47:    */   
/*  48:    */   public static void destroy() {}
/*  49:    */   
/*  50:    */   private static void addToVariablesToRemove(InternalThreadLocalMap threadLocalMap, FastThreadLocal<?> variable)
/*  51:    */   {
/*  52: 99 */     Object v = threadLocalMap.indexedVariable(variablesToRemoveIndex);
/*  53:    */     Set<FastThreadLocal<?>> variablesToRemove;
/*  54:101 */     if ((v == InternalThreadLocalMap.UNSET) || (v == null))
/*  55:    */     {
/*  56:102 */       Set<FastThreadLocal<?>> variablesToRemove = Collections.newSetFromMap(new IdentityHashMap());
/*  57:103 */       threadLocalMap.setIndexedVariable(variablesToRemoveIndex, variablesToRemove);
/*  58:    */     }
/*  59:    */     else
/*  60:    */     {
/*  61:105 */       variablesToRemove = (Set)v;
/*  62:    */     }
/*  63:108 */     variablesToRemove.add(variable);
/*  64:    */   }
/*  65:    */   
/*  66:    */   private static void removeFromVariablesToRemove(InternalThreadLocalMap threadLocalMap, FastThreadLocal<?> variable)
/*  67:    */   {
/*  68:114 */     Object v = threadLocalMap.indexedVariable(variablesToRemoveIndex);
/*  69:116 */     if ((v == InternalThreadLocalMap.UNSET) || (v == null)) {
/*  70:117 */       return;
/*  71:    */     }
/*  72:121 */     Set<FastThreadLocal<?>> variablesToRemove = (Set)v;
/*  73:122 */     variablesToRemove.remove(variable);
/*  74:    */   }
/*  75:    */   
/*  76:    */   public FastThreadLocal()
/*  77:    */   {
/*  78:128 */     this.index = InternalThreadLocalMap.nextVariableIndex();
/*  79:    */   }
/*  80:    */   
/*  81:    */   public final V get()
/*  82:    */   {
/*  83:135 */     return get(InternalThreadLocalMap.get());
/*  84:    */   }
/*  85:    */   
/*  86:    */   public final V get(InternalThreadLocalMap threadLocalMap)
/*  87:    */   {
/*  88:144 */     Object v = threadLocalMap.indexedVariable(this.index);
/*  89:145 */     if (v != InternalThreadLocalMap.UNSET) {
/*  90:146 */       return v;
/*  91:    */     }
/*  92:149 */     return initialize(threadLocalMap);
/*  93:    */   }
/*  94:    */   
/*  95:    */   private V initialize(InternalThreadLocalMap threadLocalMap)
/*  96:    */   {
/*  97:153 */     V v = null;
/*  98:    */     try
/*  99:    */     {
/* 100:155 */       v = initialValue();
/* 101:    */     }
/* 102:    */     catch (Exception e)
/* 103:    */     {
/* 104:157 */       PlatformDependent.throwException(e);
/* 105:    */     }
/* 106:160 */     threadLocalMap.setIndexedVariable(this.index, v);
/* 107:161 */     addToVariablesToRemove(threadLocalMap, this);
/* 108:162 */     return v;
/* 109:    */   }
/* 110:    */   
/* 111:    */   public final void set(V value)
/* 112:    */   {
/* 113:169 */     if (value != InternalThreadLocalMap.UNSET) {
/* 114:170 */       set(InternalThreadLocalMap.get(), value);
/* 115:    */     } else {
/* 116:172 */       remove();
/* 117:    */     }
/* 118:    */   }
/* 119:    */   
/* 120:    */   public final void set(InternalThreadLocalMap threadLocalMap, V value)
/* 121:    */   {
/* 122:180 */     if (value != InternalThreadLocalMap.UNSET)
/* 123:    */     {
/* 124:181 */       if (threadLocalMap.setIndexedVariable(this.index, value)) {
/* 125:182 */         addToVariablesToRemove(threadLocalMap, this);
/* 126:    */       }
/* 127:    */     }
/* 128:    */     else {
/* 129:185 */       remove(threadLocalMap);
/* 130:    */     }
/* 131:    */   }
/* 132:    */   
/* 133:    */   public final boolean isSet()
/* 134:    */   {
/* 135:193 */     return isSet(InternalThreadLocalMap.getIfSet());
/* 136:    */   }
/* 137:    */   
/* 138:    */   public final boolean isSet(InternalThreadLocalMap threadLocalMap)
/* 139:    */   {
/* 140:201 */     return (threadLocalMap != null) && (threadLocalMap.isIndexedVariableSet(this.index));
/* 141:    */   }
/* 142:    */   
/* 143:    */   public final void remove()
/* 144:    */   {
/* 145:207 */     remove(InternalThreadLocalMap.getIfSet());
/* 146:    */   }
/* 147:    */   
/* 148:    */   public final void remove(InternalThreadLocalMap threadLocalMap)
/* 149:    */   {
/* 150:217 */     if (threadLocalMap == null) {
/* 151:218 */       return;
/* 152:    */     }
/* 153:221 */     Object v = threadLocalMap.removeIndexedVariable(this.index);
/* 154:222 */     removeFromVariablesToRemove(threadLocalMap, this);
/* 155:224 */     if (v != InternalThreadLocalMap.UNSET) {
/* 156:    */       try
/* 157:    */       {
/* 158:226 */         onRemoval(v);
/* 159:    */       }
/* 160:    */       catch (Exception e)
/* 161:    */       {
/* 162:228 */         PlatformDependent.throwException(e);
/* 163:    */       }
/* 164:    */     }
/* 165:    */   }
/* 166:    */   
/* 167:    */   protected V initialValue()
/* 168:    */     throws Exception
/* 169:    */   {
/* 170:237 */     return null;
/* 171:    */   }
/* 172:    */   
/* 173:    */   protected void onRemoval(V value)
/* 174:    */     throws Exception
/* 175:    */   {}
/* 176:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.concurrent.FastThreadLocal
 * JD-Core Version:    0.7.0.1
 */