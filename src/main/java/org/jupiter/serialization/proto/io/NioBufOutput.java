package org.jupiter.serialization.proto.io;

import io.protostuff.ByteString;
import io.protostuff.IntSerializer;
import io.protostuff.Output;
import io.protostuff.ProtobufOutput;
import io.protostuff.Schema;
import io.protostuff.WireFormat;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.jupiter.common.util.internal.UnsafeReferenceFieldUpdater;
import org.jupiter.common.util.internal.UnsafeUpdater;
import org.jupiter.common.util.internal.UnsafeUtf8Util;
import org.jupiter.serialization.io.OutputBuf;

























class NioBufOutput
  implements Output
{
  private static final UnsafeReferenceFieldUpdater<ByteString, byte[]> byteStringBytesGetter = UnsafeUpdater.newReferenceFieldUpdater(ByteString.class, "bytes");
  
  protected final OutputBuf outputBuf;
  protected final int maxCapacity;
  protected ByteBuffer nioBuffer;
  protected int capacity;
  
  NioBufOutput(OutputBuf outputBuf, int minWritableBytes, int maxCapacity)
  {
    this.outputBuf = outputBuf;
    this.maxCapacity = maxCapacity;
    nioBuffer = outputBuf.nioByteBuffer(minWritableBytes);
    capacity = nioBuffer.remaining();
  }
  
  public void writeInt32(int fieldNumber, int value, boolean repeated) throws IOException
  {
    if (value < 0) {
      writeVarInt32(WireFormat.makeTag(fieldNumber, 0));
      writeVarInt64(value);
    } else {
      writeVarInt32(WireFormat.makeTag(fieldNumber, 0));
      writeVarInt32(value);
    }
  }
  
  public void writeUInt32(int fieldNumber, int value, boolean repeated) throws IOException
  {
    writeVarInt32(WireFormat.makeTag(fieldNumber, 0));
    writeVarInt32(value);
  }
  
  public void writeSInt32(int fieldNumber, int value, boolean repeated) throws IOException
  {
    writeVarInt32(WireFormat.makeTag(fieldNumber, 0));
    writeVarInt32(ProtobufOutput.encodeZigZag32(value));
  }
  
  public void writeFixed32(int fieldNumber, int value, boolean repeated) throws IOException
  {
    writeVarInt32(WireFormat.makeTag(fieldNumber, 5));
    writeInt32LE(value);
  }
  
  public void writeSFixed32(int fieldNumber, int value, boolean repeated) throws IOException
  {
    writeVarInt32(WireFormat.makeTag(fieldNumber, 5));
    writeInt32LE(value);
  }
  
  public void writeInt64(int fieldNumber, long value, boolean repeated) throws IOException
  {
    writeVarInt32(WireFormat.makeTag(fieldNumber, 0));
    writeVarInt64(value);
  }
  
  public void writeUInt64(int fieldNumber, long value, boolean repeated) throws IOException
  {
    writeVarInt32(WireFormat.makeTag(fieldNumber, 0));
    writeVarInt64(value);
  }
  
  public void writeSInt64(int fieldNumber, long value, boolean repeated) throws IOException
  {
    writeVarInt32(WireFormat.makeTag(fieldNumber, 0));
    writeVarInt64(ProtobufOutput.encodeZigZag64(value));
  }
  
  public void writeFixed64(int fieldNumber, long value, boolean repeated) throws IOException
  {
    writeVarInt32(WireFormat.makeTag(fieldNumber, 1));
    writeInt64LE(value);
  }
  
  public void writeSFixed64(int fieldNumber, long value, boolean repeated) throws IOException
  {
    writeVarInt32(WireFormat.makeTag(fieldNumber, 1));
    writeInt64LE(value);
  }
  
  public void writeFloat(int fieldNumber, float value, boolean repeated) throws IOException
  {
    writeVarInt32(WireFormat.makeTag(fieldNumber, 5));
    writeInt32LE(Float.floatToRawIntBits(value));
  }
  
  public void writeDouble(int fieldNumber, double value, boolean repeated) throws IOException
  {
    writeVarInt32(WireFormat.makeTag(fieldNumber, 1));
    writeInt64LE(Double.doubleToRawLongBits(value));
  }
  
  public void writeBool(int fieldNumber, boolean value, boolean repeated) throws IOException
  {
    writeVarInt32(WireFormat.makeTag(fieldNumber, 0));
    writeByte((byte)(value ? 1 : 0));
  }
  
  public void writeEnum(int fieldNumber, int value, boolean repeated) throws IOException
  {
    writeInt32(fieldNumber, value, repeated);
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
      
      int length;
      int length;
      if (nioBuffer.isDirect()) {
        UnsafeUtf8Util.encodeUtf8Direct(value, nioBuffer);
        
        length = nioBuffer.position() - stringStartPos;
      } else {
        int offset = nioBuffer.arrayOffset() + stringStartPos;
        int outIndex = UnsafeUtf8Util.encodeUtf8(value, nioBuffer.array(), offset, nioBuffer.remaining());
        length = outIndex - offset;
      }
      nioBuffer.position(position);
      writeVarInt32(length);
      nioBuffer.position(stringStartPos + length);
    }
    else {
      int length = UnsafeUtf8Util.encodedLength(value);
      writeVarInt32(length);
      
      ensureCapacity(length);
      
      if (nioBuffer.isDirect())
      {
        UnsafeUtf8Util.encodeUtf8Direct(value, nioBuffer);
      } else {
        int pos = nioBuffer.position();
        UnsafeUtf8Util.encodeUtf8(value, nioBuffer.array(), nioBuffer.arrayOffset() + pos, nioBuffer.remaining());
        
        nioBuffer.position(pos + length);
      }
    }
  }
  
  public void writeBytes(int fieldNumber, ByteString value, boolean repeated) throws IOException
  {
    writeByteArray(fieldNumber, (byte[])byteStringBytesGetter.get(value), repeated);
  }
  
  public void writeByteArray(int fieldNumber, byte[] value, boolean repeated) throws IOException
  {
    writeByteRange(false, fieldNumber, value, 0, value.length, repeated);
  }
  
  public void writeByteRange(boolean utf8String, int fieldNumber, byte[] value, int offset, int length, boolean repeated) throws IOException
  {
    writeVarInt32(WireFormat.makeTag(fieldNumber, 2));
    writeVarInt32(length);
    writeByteArray(value, offset, length);
  }
  
  public <T> void writeObject(int fieldNumber, T value, Schema<T> schema, boolean repeated) throws IOException
  {
    writeVarInt32(WireFormat.makeTag(fieldNumber, 3));
    schema.writeTo(this, value);
    writeVarInt32(WireFormat.makeTag(fieldNumber, 4));
  }
  
  public void writeBytes(int fieldNumber, ByteBuffer value, boolean repeated) throws IOException
  {
    writeByteRange(false, fieldNumber, value.array(), value.arrayOffset() + value.position(), value.remaining(), repeated);
  }
  
  protected void writeVarInt32(int value) throws IOException
  {
    ensureCapacity(5);
    for (;;) {
      if ((value & 0xFFFFFF80) == 0) {
        nioBuffer.put((byte)value);
        return;
      }
      nioBuffer.put((byte)(value & 0x7F | 0x80));
      value >>>= 7;
    }
  }
  
  protected void writeVarInt64(long value) throws IOException
  {
    ensureCapacity(10);
    for (;;) {
      if ((value & 0xFFFFFFFFFFFFFF80) == 0L) {
        nioBuffer.put((byte)(int)value);
        return;
      }
      nioBuffer.put((byte)((int)value & 0x7F | 0x80));
      value >>>= 7;
    }
  }
  
  protected void writeInt32LE(int value) throws IOException
  {
    ensureCapacity(4);
    IntSerializer.writeInt32LE(value, nioBuffer);
  }
  
  protected void writeInt64LE(long value) throws IOException {
    ensureCapacity(8);
    IntSerializer.writeInt64LE(value, nioBuffer);
  }
  
  protected void writeByte(byte value) throws IOException {
    ensureCapacity(1);
    nioBuffer.put(value);
  }
  
  protected void writeByteArray(byte[] value, int offset, int length) throws IOException
  {
    ensureCapacity(length);
    nioBuffer.put(value, offset, length);
  }
  
  protected void ensureCapacity(int required) throws ProtocolException {
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
    }
  }
}
