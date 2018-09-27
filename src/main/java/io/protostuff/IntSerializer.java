/*   1:    */ package io.protostuff;
/*   2:    */ 
/*   3:    */ import java.nio.ByteBuffer;
/*   4:    */ 
/*   5:    */ public final class IntSerializer
/*   6:    */ {
/*   7:    */   public static void writeInt16(int value, byte[] buffer, int offset)
/*   8:    */   {
/*   9: 37 */     buffer[(offset++)] = ((byte)(value >>> 8 & 0xFF));
/*  10: 38 */     buffer[offset] = ((byte)value);
/*  11:    */   }
/*  12:    */   
/*  13:    */   public static void writeInt16LE(int value, byte[] buffer, int offset)
/*  14:    */   {
/*  15: 46 */     buffer[(offset++)] = ((byte)value);
/*  16: 47 */     buffer[offset] = ((byte)(value >>> 8 & 0xFF));
/*  17:    */   }
/*  18:    */   
/*  19:    */   public static void writeInt16LE(int value, ByteBuffer buffer)
/*  20:    */   {
/*  21: 52 */     buffer.put((byte)value);
/*  22: 53 */     buffer.put((byte)(value >>> 8 & 0xFF));
/*  23:    */   }
/*  24:    */   
/*  25:    */   public static void writeInt32(int value, byte[] buffer, int offset)
/*  26:    */   {
/*  27: 61 */     buffer[(offset++)] = ((byte)(value >>> 24 & 0xFF));
/*  28: 62 */     buffer[(offset++)] = ((byte)(value >>> 16 & 0xFF));
/*  29: 63 */     buffer[(offset++)] = ((byte)(value >>> 8 & 0xFF));
/*  30: 64 */     buffer[offset] = ((byte)(value >>> 0 & 0xFF));
/*  31:    */   }
/*  32:    */   
/*  33:    */   public static void writeInt32LE(int value, byte[] buffer, int offset)
/*  34:    */   {
/*  35: 72 */     buffer[(offset++)] = ((byte)(value >>> 0 & 0xFF));
/*  36: 73 */     buffer[(offset++)] = ((byte)(value >>> 8 & 0xFF));
/*  37: 74 */     buffer[(offset++)] = ((byte)(value >>> 16 & 0xFF));
/*  38: 75 */     buffer[offset] = ((byte)(value >>> 24 & 0xFF));
/*  39:    */   }
/*  40:    */   
/*  41:    */   public static void writeInt32LE(int value, ByteBuffer buffer)
/*  42:    */   {
/*  43: 80 */     buffer.put((byte)(value >>> 0 & 0xFF));
/*  44: 81 */     buffer.put((byte)(value >>> 8 & 0xFF));
/*  45: 82 */     buffer.put((byte)(value >>> 16 & 0xFF));
/*  46: 83 */     buffer.put((byte)(value >>> 24 & 0xFF));
/*  47:    */   }
/*  48:    */   
/*  49:    */   public static void writeInt64(long value, byte[] buffer, int offset)
/*  50:    */   {
/*  51: 91 */     buffer[(offset++)] = ((byte)(int)(value >>> 56));
/*  52: 92 */     buffer[(offset++)] = ((byte)(int)(value >>> 48));
/*  53: 93 */     buffer[(offset++)] = ((byte)(int)(value >>> 40));
/*  54: 94 */     buffer[(offset++)] = ((byte)(int)(value >>> 32));
/*  55: 95 */     buffer[(offset++)] = ((byte)(int)(value >>> 24));
/*  56: 96 */     buffer[(offset++)] = ((byte)(int)(value >>> 16));
/*  57: 97 */     buffer[(offset++)] = ((byte)(int)(value >>> 8));
/*  58: 98 */     buffer[offset] = ((byte)(int)(value >>> 0));
/*  59:    */   }
/*  60:    */   
/*  61:    */   public static void writeInt64LE(long value, byte[] buffer, int offset)
/*  62:    */   {
/*  63:106 */     buffer[(offset++)] = ((byte)(int)(value >>> 0));
/*  64:107 */     buffer[(offset++)] = ((byte)(int)(value >>> 8));
/*  65:108 */     buffer[(offset++)] = ((byte)(int)(value >>> 16));
/*  66:109 */     buffer[(offset++)] = ((byte)(int)(value >>> 24));
/*  67:110 */     buffer[(offset++)] = ((byte)(int)(value >>> 32));
/*  68:111 */     buffer[(offset++)] = ((byte)(int)(value >>> 40));
/*  69:112 */     buffer[(offset++)] = ((byte)(int)(value >>> 48));
/*  70:113 */     buffer[offset] = ((byte)(int)(value >>> 56));
/*  71:    */   }
/*  72:    */   
/*  73:    */   public static void writeInt64LE(long value, ByteBuffer buffer)
/*  74:    */   {
/*  75:118 */     buffer.put((byte)(int)(value >>> 0));
/*  76:119 */     buffer.put((byte)(int)(value >>> 8));
/*  77:120 */     buffer.put((byte)(int)(value >>> 16));
/*  78:121 */     buffer.put((byte)(int)(value >>> 24));
/*  79:122 */     buffer.put((byte)(int)(value >>> 32));
/*  80:123 */     buffer.put((byte)(int)(value >>> 40));
/*  81:124 */     buffer.put((byte)(int)(value >>> 48));
/*  82:125 */     buffer.put((byte)(int)(value >>> 56));
/*  83:    */   }
/*  84:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.IntSerializer
 * JD-Core Version:    0.7.0.1
 */