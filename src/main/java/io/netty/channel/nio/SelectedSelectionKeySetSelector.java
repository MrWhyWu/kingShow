/*  1:   */ package io.netty.channel.nio;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import java.nio.channels.SelectionKey;
/*  5:   */ import java.nio.channels.Selector;
/*  6:   */ import java.nio.channels.spi.SelectorProvider;
/*  7:   */ import java.util.Set;
/*  8:   */ 
/*  9:   */ final class SelectedSelectionKeySetSelector
/* 10:   */   extends Selector
/* 11:   */ {
/* 12:   */   private final SelectedSelectionKeySet selectionKeys;
/* 13:   */   private final Selector delegate;
/* 14:   */   
/* 15:   */   SelectedSelectionKeySetSelector(Selector delegate, SelectedSelectionKeySet selectionKeys)
/* 16:   */   {
/* 17:29 */     this.delegate = delegate;
/* 18:30 */     this.selectionKeys = selectionKeys;
/* 19:   */   }
/* 20:   */   
/* 21:   */   public boolean isOpen()
/* 22:   */   {
/* 23:35 */     return this.delegate.isOpen();
/* 24:   */   }
/* 25:   */   
/* 26:   */   public SelectorProvider provider()
/* 27:   */   {
/* 28:40 */     return this.delegate.provider();
/* 29:   */   }
/* 30:   */   
/* 31:   */   public Set<SelectionKey> keys()
/* 32:   */   {
/* 33:45 */     return this.delegate.keys();
/* 34:   */   }
/* 35:   */   
/* 36:   */   public Set<SelectionKey> selectedKeys()
/* 37:   */   {
/* 38:50 */     return this.delegate.selectedKeys();
/* 39:   */   }
/* 40:   */   
/* 41:   */   public int selectNow()
/* 42:   */     throws IOException
/* 43:   */   {
/* 44:55 */     this.selectionKeys.reset();
/* 45:56 */     return this.delegate.selectNow();
/* 46:   */   }
/* 47:   */   
/* 48:   */   public int select(long timeout)
/* 49:   */     throws IOException
/* 50:   */   {
/* 51:61 */     this.selectionKeys.reset();
/* 52:62 */     return this.delegate.select(timeout);
/* 53:   */   }
/* 54:   */   
/* 55:   */   public int select()
/* 56:   */     throws IOException
/* 57:   */   {
/* 58:67 */     this.selectionKeys.reset();
/* 59:68 */     return this.delegate.select();
/* 60:   */   }
/* 61:   */   
/* 62:   */   public Selector wakeup()
/* 63:   */   {
/* 64:73 */     return this.delegate.wakeup();
/* 65:   */   }
/* 66:   */   
/* 67:   */   public void close()
/* 68:   */     throws IOException
/* 69:   */   {
/* 70:78 */     this.delegate.close();
/* 71:   */   }
/* 72:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.nio.SelectedSelectionKeySetSelector
 * JD-Core Version:    0.7.0.1
 */