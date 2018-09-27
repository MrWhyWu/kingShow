package org.jupiter.serialization.proto.io;

import io.protostuff.WireFormat;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.jupiter.common.util.internal.UnsafeDirectBufferUtil;
import org.jupiter.common.util.internal.UnsafeUtf8Util;
import org.jupiter.common.util.internal.UnsafeUtil;
import org.jupiter.serialization.io.OutputBuf;



























class UnsafeNioBufOutput
  extends NioBufOutput
{
  private long memoryAddress;
  
  UnsafeNioBufOutput(OutputBuf outputBuf, int minWritableBytes, int maxCapacity)
  {
    super(outputBuf, minWritableBytes, maxCapacity);
    updateBufferAddress();
  }
  
  public void writeString(int fieldNumber, CharSequence value, boolean repeated) throws IOException
  {
    writeVarInt32(WireFormat.makeTag(fieldNumber, 2));
    


    int minLength = value.length();
    int maxLength = minLength * 3;
    int minLengthVarIntSize = VarInts.computeRawVarInt32Size(minLength);
    int maxLengthVarIntSize = VarInts.computeRawVarInt32Size(maxLength);
    if (minLengthVarIntSize == maxLengthVarIntSize) {
      int position = nioBuffer.position();
      
      ensureCapacity(maxLengthVarIntSize + maxLength);
      


      int stringStartPos = position + maxLengthVarIntSize;
      nioBuffer.position(stringStartPos);
      

      UnsafeUtf8Util.encodeUtf8Direct(value, nioBuffer);
      

      int length = nioBuffer.position() - stringStartPos;
      nioBuffer.position(position);
      writeVarInt32(length);
      nioBuffer.position(stringStartPos + length);
    }
    else {
      int length = UnsafeUtf8Util.encodedLength(value);
      writeVarInt32(length);
      
      ensureCapacity(length);
      

      UnsafeUtf8Util.encodeUtf8Direct(value, nioBuffer);
    }
  }
  























  protected void writeVarInt32(int value)
    throws IOException
  {
    ensureCapacity(5);
    int position = nioBuffer.position();
    if ((value & 0xFFFFFF80) == 0)
    {
      UnsafeDirectBufferUtil.setByte(address(position++), (byte)value);
    } else if ((value & 0xC000) == 0)
    {
      UnsafeDirectBufferUtil.setShort(address(position), (value & 0x7F | 0x80) << 8 | value >>> 7);
      
      position += 2;
    } else if ((value & 0xFFE00000) == 0)
    {
      UnsafeDirectBufferUtil.setShort(address(position), (value & 0x7F | 0x80) << 8 | value >>> 7 & 0x7F | 0x80);
      
      position += 2;
      UnsafeDirectBufferUtil.setByte(address(position++), (byte)(value >>> 14));
    } else if ((value & 0xF0000000) == 0)
    {
      UnsafeDirectBufferUtil.setInt(address(position), (value & 0x7F | 0x80) << 24 | (value >>> 7 & 0x7F | 0x80) << 16 | (value >>> 14 & 0x7F | 0x80) << 8 | value >>> 21);
      



      position += 4;
    }
    else {
      UnsafeDirectBufferUtil.setInt(address(position), (value & 0x7F | 0x80) << 24 | (value >>> 7 & 0x7F | 0x80) << 16 | (value >>> 14 & 0x7F | 0x80) << 8 | value >>> 21 & 0x7F | 0x80);
      



      position += 4;
      UnsafeDirectBufferUtil.setByte(address(position++), (byte)(value >>> 28));
    }
    nioBuffer.position(position);
  }
  














  protected void writeVarInt64(long value)
    throws IOException
  {
    ensureCapacity(10);
    int position = nioBuffer.position();
    
    if ((value & 0xFFFFFFFFFFFFFF80) == 0L)
    {
      UnsafeDirectBufferUtil.setByte(address(position++), (byte)(int)value);
    } else if (value < 0L)
    {
      UnsafeDirectBufferUtil.setLong(address(position), (value & 0x7F | 0x80) << 56 | (value >>> 7 & 0x7F | 0x80) << 48 | (value >>> 14 & 0x7F | 0x80) << 40 | (value >>> 21 & 0x7F | 0x80) << 32 | (value >>> 28 & 0x7F | 0x80) << 24 | (value >>> 35 & 0x7F | 0x80) << 16 | (value >>> 42 & 0x7F | 0x80) << 8 | value >>> 49 & 0x7F | 0x80);
      







      position += 8;
      UnsafeDirectBufferUtil.setShort(address(position), ((int)(value >>> 56) & 0x7F | 0x80) << 8 | (int)(value >>> 63));
      
      position += 2;

    }
    else if ((value & 0xFFFFFFFFFFFFC000) == 0L)
    {
      UnsafeDirectBufferUtil.setShort(address(position), ((int)value & 0x7F | 0x80) << 8 | (byte)(int)(value >>> 7));
      
      position += 2;
    } else if ((value & 0xFFFFFFFFFFE00000) == 0L)
    {
      UnsafeDirectBufferUtil.setShort(address(position), ((int)value & 0x7F | 0x80) << 8 | (int)value >>> 7 & 0x7F | 0x80);
      
      position += 2;
      UnsafeDirectBufferUtil.setByte(address(position++), (byte)(int)(value >>> 14));
    } else if ((value & 0xFFFFFFFFF0000000) == 0L)
    {
      UnsafeDirectBufferUtil.setInt(address(position), ((int)value & 0x7F | 0x80) << 24 | ((int)value >>> 7 & 0x7F | 0x80) << 16 | ((int)value >>> 14 & 0x7F | 0x80) << 8 | (int)(value >>> 21));
      



      position += 4;
    } else if ((value & 0xFFFFFFF800000000) == 0L)
    {
      UnsafeDirectBufferUtil.setInt(address(position), ((int)value & 0x7F | 0x80) << 24 | ((int)value >>> 7 & 0x7F | 0x80) << 16 | ((int)value >>> 14 & 0x7F | 0x80) << 8 | (int)value >>> 21 & 0x7F | 0x80);
      



      position += 4;
      UnsafeDirectBufferUtil.setByte(address(position++), (byte)(int)(value >>> 28));
    } else if ((value & 0xFFFFFC0000000000) == 0L)
    {
      UnsafeDirectBufferUtil.setInt(address(position), ((int)value & 0x7F | 0x80) << 24 | ((int)value >>> 7 & 0x7F | 0x80) << 16 | ((int)value >>> 14 & 0x7F | 0x80) << 8 | (int)value >>> 21 & 0x7F | 0x80);
      




      position += 4;
      UnsafeDirectBufferUtil.setShort(address(position), ((int)(value >>> 28) & 0x7F | 0x80) << 8 | (int)(value >>> 35));
      
      position += 2;
    } else if ((value & 0xFFFE000000000000) == 0L)
    {
      UnsafeDirectBufferUtil.setInt(address(position), ((int)value & 0x7F | 0x80) << 24 | ((int)value >>> 7 & 0x7F | 0x80) << 16 | ((int)value >>> 14 & 0x7F | 0x80) << 8 | (int)value >>> 21 & 0x7F | 0x80);
      




      position += 4;
      UnsafeDirectBufferUtil.setShort(address(position), ((int)(value >>> 28) & 0x7F | 0x80) << 8 | (int)(value >>> 35) & 0x7F | 0x80);
      
      position += 2;
      UnsafeDirectBufferUtil.setByte(address(position++), (byte)(int)(value >>> 42));
    } else if ((value & 0xFF00000000000000) == 0L)
    {
      UnsafeDirectBufferUtil.setLong(address(position), (value & 0x7F | 0x80) << 56 | (value >>> 7 & 0x7F | 0x80) << 48 | (value >>> 14 & 0x7F | 0x80) << 40 | (value >>> 21 & 0x7F | 0x80) << 32 | (value >>> 28 & 0x7F | 0x80) << 24 | (value >>> 35 & 0x7F | 0x80) << 16 | (value >>> 42 & 0x7F | 0x80) << 8 | value >>> 49);
      







      position += 8;
    }
    else {
      UnsafeDirectBufferUtil.setLong(address(position), (value & 0x7F | 0x80) << 56 | (value >>> 7 & 0x7F | 0x80) << 48 | (value >>> 14 & 0x7F | 0x80) << 40 | (value >>> 21 & 0x7F | 0x80) << 32 | (value >>> 28 & 0x7F | 0x80) << 24 | (value >>> 35 & 0x7F | 0x80) << 16 | (value >>> 42 & 0x7F | 0x80) << 8 | value >>> 49 & 0x7F | 0x80);
      







      position += 8;
      UnsafeDirectBufferUtil.setByte(address(position++), (byte)(int)(value >>> 56));
    }
    nioBuffer.position(position);
  }
  
  protected void writeInt32LE(int value) throws IOException
  {
    ensureCapacity(4);
    int position = nioBuffer.position();
    UnsafeDirectBufferUtil.setIntLE(address(position), value);
    nioBuffer.position(position + 4);
  }
  
  protected void writeInt64LE(long value) throws IOException
  {
    ensureCapacity(8);
    int position = nioBuffer.position();
    UnsafeDirectBufferUtil.setLongLE(address(position), value);
    nioBuffer.position(position + 8);
  }
  
  protected void writeByte(byte value) throws IOException
  {
    ensureCapacity(1);
    int position = nioBuffer.position();
    UnsafeDirectBufferUtil.setByte(address(position), value);
    nioBuffer.position(position + 1);
  }
  
  protected void writeByteArray(byte[] value, int offset, int length) throws IOException
  {
    ensureCapacity(length);
    int position = nioBuffer.position();
    UnsafeDirectBufferUtil.setBytes(address(position), value, offset, length);
    nioBuffer.position(position + length);
  }
  
  protected void ensureCapacity(int required) throws ProtocolException
  {
    if (nioBuffer.remaining() < required) {
      int position = nioBuffer.position();
      
      while (capacity - position < required) {
        if (capacity == maxCapacity) {
          throw new ProtocolException("Buffer overflow. Available: " + (capacity - position) + ", required: " + required);
        }
        
        capacity = Math.min(capacity << 1, maxCapacity);
        if (capacity < 0) {
          capacity = maxCapacity;
        }
      }
      
      nioBuffer = outputBuf.nioByteBuffer(capacity - position);
      capacity = nioBuffer.limit();
      
      updateBufferAddress();
    }
  }
  
  private void updateBufferAddress() {
    memoryAddress = UnsafeUtil.addressOffset(nioBuffer);
  }
  
  private long address(int position) {
    return memoryAddress + position;
  }
}
