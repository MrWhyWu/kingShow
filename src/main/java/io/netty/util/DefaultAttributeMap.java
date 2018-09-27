/*   1:    */ package io.netty.util;
/*   2:    */ 
/*   3:    */ import java.util.concurrent.atomic.AtomicReference;
/*   4:    */ import java.util.concurrent.atomic.AtomicReferenceArray;
/*   5:    */ import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
/*   6:    */ 
/*   7:    */ public class DefaultAttributeMap
/*   8:    */   implements AttributeMap
/*   9:    */ {
/*  10: 30 */   private static final AtomicReferenceFieldUpdater<DefaultAttributeMap, AtomicReferenceArray> updater = AtomicReferenceFieldUpdater.newUpdater(DefaultAttributeMap.class, AtomicReferenceArray.class, "attributes");
/*  11:    */   private static final int BUCKET_SIZE = 4;
/*  12:    */   private static final int MASK = 3;
/*  13:    */   private volatile AtomicReferenceArray<DefaultAttribute<?>> attributes;
/*  14:    */   
/*  15:    */   public <T> Attribute<T> attr(AttributeKey<T> key)
/*  16:    */   {
/*  17: 42 */     if (key == null) {
/*  18: 43 */       throw new NullPointerException("key");
/*  19:    */     }
/*  20: 45 */     AtomicReferenceArray<DefaultAttribute<?>> attributes = this.attributes;
/*  21: 46 */     if (attributes == null)
/*  22:    */     {
/*  23: 48 */       attributes = new AtomicReferenceArray(4);
/*  24: 50 */       if (!updater.compareAndSet(this, null, attributes)) {
/*  25: 51 */         attributes = this.attributes;
/*  26:    */       }
/*  27:    */     }
/*  28: 55 */     int i = index(key);
/*  29: 56 */     DefaultAttribute<?> head = (DefaultAttribute)attributes.get(i);
/*  30: 57 */     if (head == null)
/*  31:    */     {
/*  32: 60 */       head = new DefaultAttribute();
/*  33: 61 */       DefaultAttribute<T> attr = new DefaultAttribute(head, key);
/*  34: 62 */       head.next = attr;
/*  35: 63 */       attr.prev = head;
/*  36: 64 */       if (attributes.compareAndSet(i, null, head)) {
/*  37: 66 */         return attr;
/*  38:    */       }
/*  39: 68 */       head = (DefaultAttribute)attributes.get(i);
/*  40:    */     }
/*  41: 72 */     synchronized (head)
/*  42:    */     {
/*  43: 73 */       DefaultAttribute<?> curr = head;
/*  44:    */       
/*  45: 75 */       DefaultAttribute<?> next = curr.next;
/*  46: 76 */       if (next == null)
/*  47:    */       {
/*  48: 77 */         DefaultAttribute<T> attr = new DefaultAttribute(head, key);
/*  49: 78 */         curr.next = attr;
/*  50: 79 */         attr.prev = curr;
/*  51: 80 */         return attr;
/*  52:    */       }
/*  53: 83 */       if ((next.key == key) && (!next.removed)) {
/*  54: 84 */         return next;
/*  55:    */       }
/*  56: 86 */       curr = next;
/*  57:    */     }
/*  58:    */   }
/*  59:    */   
/*  60:    */   public <T> boolean hasAttr(AttributeKey<T> key)
/*  61:    */   {
/*  62: 93 */     if (key == null) {
/*  63: 94 */       throw new NullPointerException("key");
/*  64:    */     }
/*  65: 96 */     AtomicReferenceArray<DefaultAttribute<?>> attributes = this.attributes;
/*  66: 97 */     if (attributes == null) {
/*  67: 99 */       return false;
/*  68:    */     }
/*  69:102 */     int i = index(key);
/*  70:103 */     DefaultAttribute<?> head = (DefaultAttribute)attributes.get(i);
/*  71:104 */     if (head == null) {
/*  72:106 */       return false;
/*  73:    */     }
/*  74:110 */     synchronized (head)
/*  75:    */     {
/*  76:112 */       DefaultAttribute<?> curr = head.next;
/*  77:113 */       while (curr != null)
/*  78:    */       {
/*  79:114 */         if ((curr.key == key) && (!curr.removed)) {
/*  80:115 */           return true;
/*  81:    */         }
/*  82:117 */         curr = curr.next;
/*  83:    */       }
/*  84:119 */       return false;
/*  85:    */     }
/*  86:    */   }
/*  87:    */   
/*  88:    */   private static int index(AttributeKey<?> key)
/*  89:    */   {
/*  90:124 */     return key.id() & 0x3;
/*  91:    */   }
/*  92:    */   
/*  93:    */   private static final class DefaultAttribute<T>
/*  94:    */     extends AtomicReference<T>
/*  95:    */     implements Attribute<T>
/*  96:    */   {
/*  97:    */     private static final long serialVersionUID = -2661411462200283011L;
/*  98:    */     private final DefaultAttribute<?> head;
/*  99:    */     private final AttributeKey<T> key;
/* 100:    */     private DefaultAttribute<?> prev;
/* 101:    */     private DefaultAttribute<?> next;
/* 102:    */     private volatile boolean removed;
/* 103:    */     
/* 104:    */     DefaultAttribute(DefaultAttribute<?> head, AttributeKey<T> key)
/* 105:    */     {
/* 106:144 */       this.head = head;
/* 107:145 */       this.key = key;
/* 108:    */     }
/* 109:    */     
/* 110:    */     DefaultAttribute()
/* 111:    */     {
/* 112:150 */       this.head = this;
/* 113:151 */       this.key = null;
/* 114:    */     }
/* 115:    */     
/* 116:    */     public AttributeKey<T> key()
/* 117:    */     {
/* 118:156 */       return this.key;
/* 119:    */     }
/* 120:    */     
/* 121:    */     public T setIfAbsent(T value)
/* 122:    */     {
/* 123:161 */       while (!compareAndSet(null, value))
/* 124:    */       {
/* 125:162 */         T old = get();
/* 126:163 */         if (old != null) {
/* 127:164 */           return old;
/* 128:    */         }
/* 129:    */       }
/* 130:167 */       return null;
/* 131:    */     }
/* 132:    */     
/* 133:    */     public T getAndRemove()
/* 134:    */     {
/* 135:172 */       this.removed = true;
/* 136:173 */       T oldValue = getAndSet(null);
/* 137:174 */       remove0();
/* 138:175 */       return oldValue;
/* 139:    */     }
/* 140:    */     
/* 141:    */     public void remove()
/* 142:    */     {
/* 143:180 */       this.removed = true;
/* 144:181 */       set(null);
/* 145:182 */       remove0();
/* 146:    */     }
/* 147:    */     
/* 148:    */     private void remove0()
/* 149:    */     {
/* 150:186 */       synchronized (this.head)
/* 151:    */       {
/* 152:187 */         if (this.prev == null) {
/* 153:189 */           return;
/* 154:    */         }
/* 155:192 */         this.prev.next = this.next;
/* 156:194 */         if (this.next != null) {
/* 157:195 */           this.next.prev = this.prev;
/* 158:    */         }
/* 159:200 */         this.prev = null;
/* 160:201 */         this.next = null;
/* 161:    */       }
/* 162:    */     }
/* 163:    */   }
/* 164:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.DefaultAttributeMap
 * JD-Core Version:    0.7.0.1
 */