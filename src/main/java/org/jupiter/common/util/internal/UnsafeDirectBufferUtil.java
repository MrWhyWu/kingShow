package org.jupiter.common.util.internal;

import java.lang.reflect.Method;
import java.nio.ByteOrder;
import org.jupiter.common.util.StackTraceUtil;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;

























public final class UnsafeDirectBufferUtil
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(UnsafeDirectBufferUtil.class);
  
  private static final UnsafeUtil.MemoryAccessor memoryAccessor = UnsafeUtil.getMemoryAccessor();
  
  private static final long BYTE_ARRAY_BASE_OFFSET = UnsafeUtil.arrayBaseOffset([B.class);
  

  private static final long UNSAFE_COPY_THRESHOLD = 1048576L;
  

  private static final int JNI_COPY_TO_ARRAY_THRESHOLD = 6;
  

  private static final int JNI_COPY_FROM_ARRAY_THRESHOLD = 6;
  

  private static final boolean BIG_ENDIAN_NATIVE_ORDER = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
  private static final boolean UNALIGNED;
  
  static
  {
    boolean _unaligned;
    try {
      Class<?> bitsClass = Class.forName("java.nio.Bits", false, UnsafeUtil.getSystemClassLoader());
      Method unalignedMethod = bitsClass.getDeclaredMethod("unaligned", new Class[0]);
      unalignedMethod.setAccessible(true);
      _unaligned = ((Boolean)unalignedMethod.invoke(null, new Object[0])).booleanValue();
    } catch (Throwable t) { boolean _unaligned;
      if (logger.isWarnEnabled()) {
        logger.warn("java.nio.Bits: unavailable, {}.", StackTraceUtil.stackTrace(t));
      }
      
      _unaligned = false;
    }
    UNALIGNED = _unaligned;
  }
  
  public static byte getByte(long address) {
    return memoryAccessor.getByte(address);
  }
  
  public static short getShort(long address) {
    if (UNALIGNED) {
      short v = memoryAccessor.getShort(address);
      return BIG_ENDIAN_NATIVE_ORDER ? v : Short.reverseBytes(v);
    }
    return (short)(memoryAccessor.getByte(address) << 8 | memoryAccessor.getByte(address + 1L) & 0xFF);
  }
  
  public static short getShortLE(long address) {
    if (UNALIGNED) {
      short v = memoryAccessor.getShort(address);
      return BIG_ENDIAN_NATIVE_ORDER ? Short.reverseBytes(v) : v;
    }
    return (short)(memoryAccessor.getByte(address) & 0xFF | memoryAccessor.getByte(address + 1L) << 8);
  }
  
  public static int getInt(long address) {
    if (UNALIGNED) {
      int v = memoryAccessor.getInt(address);
      return BIG_ENDIAN_NATIVE_ORDER ? v : Integer.reverseBytes(v);
    }
    return memoryAccessor.getByte(address) << 24 | (memoryAccessor.getByte(address + 1L) & 0xFF) << 16 | (memoryAccessor.getByte(address + 2L) & 0xFF) << 8 | memoryAccessor.getByte(address + 3L) & 0xFF;
  }
  


  public static int getIntLE(long address)
  {
    if (UNALIGNED) {
      int v = memoryAccessor.getInt(address);
      return BIG_ENDIAN_NATIVE_ORDER ? Integer.reverseBytes(v) : v;
    }
    return memoryAccessor.getByte(address) & 0xFF | (memoryAccessor.getByte(address + 1L) & 0xFF) << 8 | (memoryAccessor.getByte(address + 2L) & 0xFF) << 16 | memoryAccessor.getByte(address + 3L) << 24;
  }
  


  public static long getLong(long address)
  {
    if (UNALIGNED) {
      long v = memoryAccessor.getLong(address);
      return BIG_ENDIAN_NATIVE_ORDER ? v : Long.reverseBytes(v);
    }
    return memoryAccessor.getByte(address) << 56 | (memoryAccessor.getByte(address + 1L) & 0xFF) << 48 | (memoryAccessor.getByte(address + 2L) & 0xFF) << 40 | (memoryAccessor.getByte(address + 3L) & 0xFF) << 32 | (memoryAccessor.getByte(address + 4L) & 0xFF) << 24 | (memoryAccessor.getByte(address + 5L) & 0xFF) << 16 | (memoryAccessor.getByte(address + 6L) & 0xFF) << 8 | memoryAccessor.getByte(address + 7L) & 0xFF;
  }
  






  public static long getLongLE(long address)
  {
    if (UNALIGNED) {
      long v = memoryAccessor.getLong(address);
      return BIG_ENDIAN_NATIVE_ORDER ? Long.reverseBytes(v) : v;
    }
    return memoryAccessor.getByte(address) & 0xFF | (memoryAccessor.getByte(address + 1L) & 0xFF) << 8 | (memoryAccessor.getByte(address + 2L) & 0xFF) << 16 | (memoryAccessor.getByte(address + 3L) & 0xFF) << 24 | (memoryAccessor.getByte(address + 4L) & 0xFF) << 32 | (memoryAccessor.getByte(address + 5L) & 0xFF) << 40 | (memoryAccessor.getByte(address + 6L) & 0xFF) << 48 | memoryAccessor.getByte(address + 7L) << 56;
  }
  






  public static void getBytes(long address, byte[] dst, int dstIndex, int length)
  {
    if (length > 6) {
      copyMemory(null, address, dst, BYTE_ARRAY_BASE_OFFSET + dstIndex, length);
    } else {
      int end = dstIndex + length;
      for (int i = dstIndex; i < end; i++) {
        dst[i] = memoryAccessor.getByte(address++);
      }
    }
  }
  
  public static void setByte(long address, int value) {
    memoryAccessor.putByte(address, (byte)value);
  }
  
  public static void setShort(long address, int value) {
    if (UNALIGNED) {
      memoryAccessor.putShort(address, BIG_ENDIAN_NATIVE_ORDER ? (short)value : Short.reverseBytes((short)value));
    }
    else {
      memoryAccessor.putByte(address, (byte)(value >>> 8));
      memoryAccessor.putByte(address + 1L, (byte)value);
    }
  }
  
  public static void setShortLE(long address, int value) {
    if (UNALIGNED) {
      memoryAccessor.putShort(address, BIG_ENDIAN_NATIVE_ORDER ? Short.reverseBytes((short)value) : (short)value);
    }
    else {
      memoryAccessor.putByte(address, (byte)value);
      memoryAccessor.putByte(address + 1L, (byte)(value >>> 8));
    }
  }
  
  public static void setInt(long address, int value) {
    if (UNALIGNED) {
      memoryAccessor.putInt(address, BIG_ENDIAN_NATIVE_ORDER ? value : Integer.reverseBytes(value));
    } else {
      memoryAccessor.putByte(address, (byte)(value >>> 24));
      memoryAccessor.putByte(address + 1L, (byte)(value >>> 16));
      memoryAccessor.putByte(address + 2L, (byte)(value >>> 8));
      memoryAccessor.putByte(address + 3L, (byte)value);
    }
  }
  
  public static void setIntLE(long address, int value) {
    if (UNALIGNED) {
      memoryAccessor.putInt(address, BIG_ENDIAN_NATIVE_ORDER ? Integer.reverseBytes(value) : value);
    } else {
      memoryAccessor.putByte(address, (byte)value);
      memoryAccessor.putByte(address + 1L, (byte)(value >>> 8));
      memoryAccessor.putByte(address + 2L, (byte)(value >>> 16));
      memoryAccessor.putByte(address + 3L, (byte)(value >>> 24));
    }
  }
  
  public static void setLong(long address, long value) {
    if (UNALIGNED) {
      memoryAccessor.putLong(address, BIG_ENDIAN_NATIVE_ORDER ? value : Long.reverseBytes(value));
    } else {
      memoryAccessor.putByte(address, (byte)(int)(value >>> 56));
      memoryAccessor.putByte(address + 1L, (byte)(int)(value >>> 48));
      memoryAccessor.putByte(address + 2L, (byte)(int)(value >>> 40));
      memoryAccessor.putByte(address + 3L, (byte)(int)(value >>> 32));
      memoryAccessor.putByte(address + 4L, (byte)(int)(value >>> 24));
      memoryAccessor.putByte(address + 5L, (byte)(int)(value >>> 16));
      memoryAccessor.putByte(address + 6L, (byte)(int)(value >>> 8));
      memoryAccessor.putByte(address + 7L, (byte)(int)value);
    }
  }
  
  public static void setLongLE(long address, long value) {
    if (UNALIGNED) {
      memoryAccessor.putLong(address, BIG_ENDIAN_NATIVE_ORDER ? Long.reverseBytes(value) : value);
    } else {
      memoryAccessor.putByte(address, (byte)(int)value);
      memoryAccessor.putByte(address + 1L, (byte)(int)(value >>> 8));
      memoryAccessor.putByte(address + 2L, (byte)(int)(value >>> 16));
      memoryAccessor.putByte(address + 3L, (byte)(int)(value >>> 24));
      memoryAccessor.putByte(address + 4L, (byte)(int)(value >>> 32));
      memoryAccessor.putByte(address + 5L, (byte)(int)(value >>> 40));
      memoryAccessor.putByte(address + 6L, (byte)(int)(value >>> 48));
      memoryAccessor.putByte(address + 7L, (byte)(int)(value >>> 56));
    }
  }
  
  public static void setBytes(long address, byte[] src, int srcIndex, int length) {
    if (length > 6) {
      copyMemory(src, BYTE_ARRAY_BASE_OFFSET + srcIndex, null, address, length);
    } else {
      int end = srcIndex + length;
      for (int i = srcIndex; i < end; i++) {
        memoryAccessor.putByte(address++, src[i]);
      }
    }
  }
  
  private static void copyMemory(Object src, long srcOffset, Object dst, long dstOffset, long length) {
    while (length > 0L) {
      long size = Math.min(length, 1048576L);
      memoryAccessor.copyMemory(src, srcOffset, dst, dstOffset, size);
      length -= size;
      srcOffset += size;
      dstOffset += size;
    }
  }
  
  private UnsafeDirectBufferUtil() {}
}
