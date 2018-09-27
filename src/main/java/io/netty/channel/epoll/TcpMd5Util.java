/*  1:   */ package io.netty.channel.epoll;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.ObjectUtil;
/*  4:   */ import java.io.IOException;
/*  5:   */ import java.net.InetAddress;
/*  6:   */ import java.util.ArrayList;
/*  7:   */ import java.util.Collection;
/*  8:   */ import java.util.Collections;
/*  9:   */ import java.util.Iterator;
/* 10:   */ import java.util.Map;
/* 11:   */ import java.util.Map.Entry;
/* 12:   */ 
/* 13:   */ final class TcpMd5Util
/* 14:   */ {
/* 15:   */   static Collection<InetAddress> newTcpMd5Sigs(AbstractEpollChannel channel, Collection<InetAddress> current, Map<InetAddress, byte[]> newKeys)
/* 16:   */     throws IOException
/* 17:   */   {
/* 18:32 */     ObjectUtil.checkNotNull(channel, "channel");
/* 19:33 */     ObjectUtil.checkNotNull(current, "current");
/* 20:34 */     ObjectUtil.checkNotNull(newKeys, "newKeys");
/* 21:37 */     for (Map.Entry<InetAddress, byte[]> e : newKeys.entrySet())
/* 22:   */     {
/* 23:38 */       byte[] key = (byte[])e.getValue();
/* 24:39 */       if (e.getKey() == null) {
/* 25:40 */         throw new IllegalArgumentException("newKeys contains an entry with null address: " + newKeys);
/* 26:   */       }
/* 27:42 */       if (key == null) {
/* 28:43 */         throw new NullPointerException("newKeys[" + e.getKey() + ']');
/* 29:   */       }
/* 30:45 */       if (key.length == 0) {
/* 31:46 */         throw new IllegalArgumentException("newKeys[" + e.getKey() + "] has an empty key.");
/* 32:   */       }
/* 33:48 */       if (key.length > Native.TCP_MD5SIG_MAXKEYLEN) {
/* 34:49 */         throw new IllegalArgumentException("newKeys[" + e.getKey() + "] has a key with invalid length; should not exceed the maximum length (" + Native.TCP_MD5SIG_MAXKEYLEN + ')');
/* 35:   */       }
/* 36:   */     }
/* 37:56 */     for (??? = current.iterator(); ???.hasNext();)
/* 38:   */     {
/* 39:56 */       addr = (InetAddress)???.next();
/* 40:57 */       if (!newKeys.containsKey(addr)) {
/* 41:58 */         channel.socket.setTcpMd5Sig(addr, null);
/* 42:   */       }
/* 43:   */     }
/* 44:   */     InetAddress addr;
/* 45:62 */     if (newKeys.isEmpty()) {
/* 46:63 */       return Collections.emptySet();
/* 47:   */     }
/* 48:67 */     Object addresses = new ArrayList(newKeys.size());
/* 49:68 */     for (Map.Entry<InetAddress, byte[]> e : newKeys.entrySet())
/* 50:   */     {
/* 51:69 */       channel.socket.setTcpMd5Sig((InetAddress)e.getKey(), (byte[])e.getValue());
/* 52:70 */       ((Collection)addresses).add(e.getKey());
/* 53:   */     }
/* 54:73 */     return addresses;
/* 55:   */   }
/* 56:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.epoll.TcpMd5Util
 * JD-Core Version:    0.7.0.1
 */