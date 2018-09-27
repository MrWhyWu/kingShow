/*  1:   */ package io.netty.channel.nio;
/*  2:   */ 
/*  3:   */ import java.nio.channels.SelectionKey;
/*  4:   */ import java.util.AbstractSet;
/*  5:   */ import java.util.Arrays;
/*  6:   */ import java.util.Iterator;
/*  7:   */ 
/*  8:   */ final class SelectedSelectionKeySet
/*  9:   */   extends AbstractSet<SelectionKey>
/* 10:   */ {
/* 11:   */   SelectionKey[] keys;
/* 12:   */   int size;
/* 13:   */   
/* 14:   */   SelectedSelectionKeySet()
/* 15:   */   {
/* 16:29 */     this.keys = new SelectionKey[1024];
/* 17:   */   }
/* 18:   */   
/* 19:   */   public boolean add(SelectionKey o)
/* 20:   */   {
/* 21:34 */     if (o == null) {
/* 22:35 */       return false;
/* 23:   */     }
/* 24:38 */     this.keys[(this.size++)] = o;
/* 25:39 */     if (this.size == this.keys.length) {
/* 26:40 */       increaseCapacity();
/* 27:   */     }
/* 28:43 */     return true;
/* 29:   */   }
/* 30:   */   
/* 31:   */   public int size()
/* 32:   */   {
/* 33:48 */     return this.size;
/* 34:   */   }
/* 35:   */   
/* 36:   */   public boolean remove(Object o)
/* 37:   */   {
/* 38:53 */     return false;
/* 39:   */   }
/* 40:   */   
/* 41:   */   public boolean contains(Object o)
/* 42:   */   {
/* 43:58 */     return false;
/* 44:   */   }
/* 45:   */   
/* 46:   */   public Iterator<SelectionKey> iterator()
/* 47:   */   {
/* 48:63 */     throw new UnsupportedOperationException();
/* 49:   */   }
/* 50:   */   
/* 51:   */   void reset()
/* 52:   */   {
/* 53:67 */     reset(0);
/* 54:   */   }
/* 55:   */   
/* 56:   */   void reset(int start)
/* 57:   */   {
/* 58:71 */     Arrays.fill(this.keys, start, this.size, null);
/* 59:72 */     this.size = 0;
/* 60:   */   }
/* 61:   */   
/* 62:   */   private void increaseCapacity()
/* 63:   */   {
/* 64:76 */     SelectionKey[] newKeys = new SelectionKey[this.keys.length << 1];
/* 65:77 */     System.arraycopy(this.keys, 0, newKeys, 0, this.size);
/* 66:78 */     this.keys = newKeys;
/* 67:   */   }
/* 68:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.nio.SelectedSelectionKeySet
 * JD-Core Version:    0.7.0.1
 */