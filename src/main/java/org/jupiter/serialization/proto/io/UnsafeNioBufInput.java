package org.jupiter.serialization.proto.io;

import io.protostuff.ByteBufferInput;
import io.protostuff.ByteString;
import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.ProtobufException;
import io.protostuff.Schema;
import io.protostuff.UninitializedMessageException;
import io.protostuff.WireFormat;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import org.jupiter.common.util.ThrowUtil;
import org.jupiter.common.util.internal.UnsafeDirectBufferUtil;
import org.jupiter.common.util.internal.UnsafeUtf8Util;
import org.jupiter.common.util.internal.UnsafeUtil;




















class UnsafeNioBufInput
  implements Input
{
  static final int TAG_TYPE_BITS = 3;
  static final int TAG_TYPE_MASK = 7;
  static final Method byteStringWrapMethod;
  private final ByteBuffer nioBuffer;
  private int lastTag = 0;
  private int packedLimit = 0;
  




  private long memoryAddress;
  




  public final boolean decodeNestedMessageAsGroup;
  




  UnsafeNioBufInput(ByteBuffer nioBuffer, boolean protostuffMessage)
  {
    this.nioBuffer = nioBuffer;
    decodeNestedMessageAsGroup = protostuffMessage;
    updateBufferAddress();
  }
  


  public int currentOffset()
  {
    return nioBuffer.position();
  }
  


  public int currentLimit()
  {
    return nioBuffer.limit();
  }
  


  public boolean isCurrentFieldPacked()
  {
    return (packedLimit != 0) && (packedLimit != nioBuffer.position());
  }
  


  public int getLastTag()
  {
    return lastTag;
  }
  


  public int readTag()
    throws IOException
  {
    if (!nioBuffer.hasRemaining()) {
      lastTag = 0;
      return 0;
    }
    
    int tag = readRawVarInt32();
    if (tag >>> 3 == 0)
    {
      throw ProtocolException.invalidTag();
    }
    lastTag = tag;
    return tag;
  }
  




  public void checkLastTagWas(int value)
    throws ProtobufException
  {
    if (lastTag != value) {
      throw ProtocolException.invalidEndTag();
    }
  }
  




  public boolean skipField(int tag)
    throws IOException
  {
    switch (WireFormat.getTagWireType(tag)) {
    case 0: 
      readInt32();
      return true;
    case 1: 
      readRawLittleEndian64();
      return true;
    case 2: 
      int size = readRawVarInt32();
      if (size < 0) {
        throw ProtocolException.negativeSize();
      }
      nioBuffer.position(nioBuffer.position() + size);
      
      return true;
    case 3: 
      skipMessage();
      checkLastTagWas(WireFormat.makeTag(WireFormat.getTagFieldNumber(tag), 4));
      return true;
    case 4: 
      return false;
    case 5: 
      readRawLittleEndian32();
      return true;
    }
    throw ProtocolException.invalidWireType();
  }
  


  public void skipMessage()
    throws IOException
  {
    for (;;)
    {
      int tag = readTag();
      if ((tag == 0) || (!skipField(tag))) {
        return;
      }
    }
  }
  
  public <T> void handleUnknownField(int fieldNumber, Schema<T> schema) throws IOException
  {
    skipField(lastTag);
  }
  
  public <T> int readFieldNumber(Schema<T> schema) throws IOException
  {
    if (!nioBuffer.hasRemaining()) {
      lastTag = 0;
      return 0;
    }
    

    if (isCurrentFieldPacked()) {
      if (packedLimit < nioBuffer.position()) {
        throw ProtocolException.misreportedSize();
      }
      

      return lastTag >>> 3;
    }
    
    packedLimit = 0;
    int tag = readRawVarInt32();
    int fieldNumber = tag >>> 3;
    if (fieldNumber == 0) {
      if ((decodeNestedMessageAsGroup) && (7 == (tag & 0x7)))
      {


        lastTag = 0;
        return 0;
      }
      
      throw ProtocolException.invalidTag();
    }
    if ((decodeNestedMessageAsGroup) && (4 == (tag & 0x7))) {
      lastTag = 0;
      return 0;
    }
    
    lastTag = tag;
    return fieldNumber;
  }
  



  private void checkIfPackedField()
    throws IOException
  {
    if ((packedLimit == 0) && (WireFormat.getTagWireType(lastTag) == 2)) {
      int length = readRawVarInt32();
      if (length < 0) {
        throw ProtocolException.negativeSize();
      }
      
      if (nioBuffer.position() + length > nioBuffer.limit()) {
        throw ProtocolException.misreportedSize();
      }
      
      packedLimit = (nioBuffer.position() + length);
    }
  }
  


  public double readDouble()
    throws IOException
  {
    checkIfPackedField();
    return Double.longBitsToDouble(readRawLittleEndian64());
  }
  


  public float readFloat()
    throws IOException
  {
    checkIfPackedField();
    return Float.intBitsToFloat(readRawLittleEndian32());
  }
  


  public long readUInt64()
    throws IOException
  {
    checkIfPackedField();
    return readRawVarInt64();
  }
  


  public long readInt64()
    throws IOException
  {
    checkIfPackedField();
    return readRawVarInt64();
  }
  


  public int readInt32()
    throws IOException
  {
    checkIfPackedField();
    return readRawVarInt32();
  }
  


  public long readFixed64()
    throws IOException
  {
    checkIfPackedField();
    return readRawLittleEndian64();
  }
  


  public int readFixed32()
    throws IOException
  {
    checkIfPackedField();
    return readRawLittleEndian32();
  }
  


  public boolean readBool()
    throws IOException
  {
    checkIfPackedField();
    int position = nioBuffer.position();
    boolean result = UnsafeDirectBufferUtil.getByte(address(position)) != 0;
    nioBuffer.position(position + 1);
    return result;
  }
  


  public int readUInt32()
    throws IOException
  {
    checkIfPackedField();
    return readRawVarInt32();
  }
  



  public int readEnum()
    throws IOException
  {
    checkIfPackedField();
    return readRawVarInt32();
  }
  


  public int readSFixed32()
    throws IOException
  {
    checkIfPackedField();
    return readRawLittleEndian32();
  }
  


  public long readSFixed64()
    throws IOException
  {
    checkIfPackedField();
    return readRawLittleEndian64();
  }
  


  public int readSInt32()
    throws IOException
  {
    checkIfPackedField();
    int n = readRawVarInt32();
    return n >>> 1 ^ -(n & 0x1);
  }
  


  public long readSInt64()
    throws IOException
  {
    checkIfPackedField();
    long n = readRawVarInt64();
    return n >>> 1 ^ -(n & 1L);
  }
  
  public String readString() throws IOException
  {
    int length = readRawVarInt32();
    if (length < 0) {
      throw ProtocolException.negativeSize();
    }
    
    if (nioBuffer.remaining() < length) {
      throw ProtocolException.misreportedSize();
    }
    
    int position = nioBuffer.position();
    String result = UnsafeUtf8Util.decodeUtf8Direct(nioBuffer, position, length);
    nioBuffer.position(position + length);
    return result;
  }
  
  public ByteString readBytes() throws IOException
  {
    try
    {
      return (ByteString)byteStringWrapMethod.invoke(null, new Object[] { readByteArray() });
    } catch (Exception e) {
      ThrowUtil.throwException(e);
    }
    return null;
  }
  
  public void readBytes(ByteBuffer bb) throws IOException
  {
    int length = readRawVarInt32();
    if (length < 0) {
      throw ProtocolException.negativeSize();
    }
    
    if (nioBuffer.remaining() < length) {
      throw ProtocolException.misreportedSize();
    }
    
    bb.put(nioBuffer);
  }
  
  public byte[] readByteArray() throws IOException
  {
    int length = readRawVarInt32();
    if (length < 0) {
      throw ProtocolException.negativeSize();
    }
    
    if (nioBuffer.remaining() < length) {
      throw ProtocolException.misreportedSize();
    }
    
    byte[] copy = new byte[length];
    int position = nioBuffer.position();
    UnsafeDirectBufferUtil.getBytes(address(position), copy, 0, length);
    nioBuffer.position(position + length);
    return copy;
  }
  
  public <T> T mergeObject(T value, Schema<T> schema) throws IOException
  {
    if (decodeNestedMessageAsGroup) {
      return mergeObjectEncodedAsGroup(value, schema);
    }
    
    int length = readRawVarInt32();
    if (length < 0) {
      throw ProtocolException.negativeSize();
    }
    
    if (nioBuffer.remaining() < length) {
      throw ProtocolException.misreportedSize();
    }
    
    ByteBuffer dup = nioBuffer.slice();
    dup.limit(length);
    
    if (value == null) {
      value = schema.newMessage();
    }
    ByteBufferInput nestedInput = new ByteBufferInput(dup, false);
    schema.mergeFrom(nestedInput, value);
    if (!schema.isInitialized(value)) {
      throw new UninitializedMessageException(value, schema);
    }
    nestedInput.checkLastTagWas(0);
    
    nioBuffer.position(nioBuffer.position() + length);
    return value;
  }
  
  private <T> T mergeObjectEncodedAsGroup(T value, Schema<T> schema) throws IOException {
    if (value == null) {
      value = schema.newMessage();
    }
    schema.mergeFrom(this, value);
    if (!schema.isInitialized(value)) {
      throw new UninitializedMessageException(value, schema);
    }
    
    checkLastTagWas(0);
    return value;
  }
  

  public int readRawVarInt32()
    throws IOException
  {
    int position = nioBuffer.position();
    byte tmp = UnsafeDirectBufferUtil.getByte(address(position++));
    if (tmp >= 0) {
      nioBuffer.position(position);
      return tmp;
    }
    int result = tmp & 0x7F;
    if ((tmp = UnsafeDirectBufferUtil.getByte(address(position++))) >= 0) {
      result |= tmp << 7;
    } else {
      result |= (tmp & 0x7F) << 7;
      if ((tmp = UnsafeDirectBufferUtil.getByte(address(position++))) >= 0) {
        result |= tmp << 14;
      } else {
        result |= (tmp & 0x7F) << 14;
        if ((tmp = UnsafeDirectBufferUtil.getByte(address(position++))) >= 0) {
          result |= tmp << 21;
        } else {
          result |= (tmp & 0x7F) << 21;
          result |= (tmp = UnsafeDirectBufferUtil.getByte(address(position++))) << 28;
          if (tmp < 0)
          {
            for (int i = 0; i < 5; i++) {
              if (UnsafeDirectBufferUtil.getByte(address(position++)) >= 0) {
                nioBuffer.position(position);
                return result;
              }
            }
            throw ProtocolException.malformedVarInt();
          }
        }
      }
    }
    nioBuffer.position(position);
    return result;
  }
  

  public long readRawVarInt64()
    throws IOException
  {
    int shift = 0;
    long result = 0L;
    int position = nioBuffer.position();
    while (shift < 64) {
      byte b = UnsafeDirectBufferUtil.getByte(address(position++));
      result |= (b & 0x7F) << shift;
      if ((b & 0x80) == 0) {
        nioBuffer.position(position);
        return result;
      }
      shift += 7;
    }
    throw ProtocolException.malformedVarInt();
  }
  

  public int readRawLittleEndian32()
    throws IOException
  {
    int position = nioBuffer.position();
    int result = UnsafeDirectBufferUtil.getIntLE(address(position));
    nioBuffer.position(position + 4);
    return result;
  }
  

  public long readRawLittleEndian64()
    throws IOException
  {
    int position = nioBuffer.position();
    long result = UnsafeDirectBufferUtil.getLongLE(address(position));
    nioBuffer.position(position + 8);
    return result;
  }
  
  public void transferByteRangeTo(Output output, boolean utf8String, int fieldNumber, boolean repeated)
    throws IOException
  {
    int length = readRawVarInt32();
    if (length < 0) {
      throw ProtocolException.negativeSize();
    }
    
    if (utf8String)
    {

      if (nioBuffer.hasArray()) {
        output.writeByteRange(true, fieldNumber, nioBuffer.array(), nioBuffer.arrayOffset() + nioBuffer.position(), length, repeated);
        
        nioBuffer.position(nioBuffer.position() + length);
      } else {
        byte[] bytes = new byte[length];
        int position = nioBuffer.position();
        UnsafeDirectBufferUtil.getBytes(address(position), bytes, 0, length);
        nioBuffer.position(position + length);
        output.writeByteRange(true, fieldNumber, bytes, 0, bytes.length, repeated);
      }
    }
    else {
      if (nioBuffer.remaining() < length) {
        throw ProtocolException.misreportedSize();
      }
      
      ByteBuffer dup = nioBuffer.slice();
      dup.limit(length);
      
      output.writeBytes(fieldNumber, dup, repeated);
      
      nioBuffer.position(nioBuffer.position() + length);
    }
  }
  


  public ByteBuffer readByteBuffer()
    throws IOException
  {
    return ByteBuffer.wrap(readByteArray());
  }
  
  private long address(int position) {
    return memoryAddress + position;
  }
  
  private void updateBufferAddress() {
    memoryAddress = UnsafeUtil.addressOffset(nioBuffer);
  }
  
  static {
    try {
      byteStringWrapMethod = ByteString.class.getDeclaredMethod("wrap", new Class[] { [B.class });
      byteStringWrapMethod.setAccessible(true);
    } catch (NoSuchMethodException e) {
      throw new Error(e);
    }
  }
}
