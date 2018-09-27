package io.protostuff;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract interface Input
{
  public abstract <T> void handleUnknownField(int paramInt, Schema<T> paramSchema)
    throws IOException;
  
  public abstract <T> int readFieldNumber(Schema<T> paramSchema)
    throws IOException;
  
  public abstract int readInt32()
    throws IOException;
  
  public abstract int readUInt32()
    throws IOException;
  
  public abstract int readSInt32()
    throws IOException;
  
  public abstract int readFixed32()
    throws IOException;
  
  public abstract int readSFixed32()
    throws IOException;
  
  public abstract long readInt64()
    throws IOException;
  
  public abstract long readUInt64()
    throws IOException;
  
  public abstract long readSInt64()
    throws IOException;
  
  public abstract long readFixed64()
    throws IOException;
  
  public abstract long readSFixed64()
    throws IOException;
  
  public abstract float readFloat()
    throws IOException;
  
  public abstract double readDouble()
    throws IOException;
  
  public abstract boolean readBool()
    throws IOException;
  
  public abstract int readEnum()
    throws IOException;
  
  public abstract String readString()
    throws IOException;
  
  public abstract ByteString readBytes()
    throws IOException;
  
  public abstract void readBytes(ByteBuffer paramByteBuffer)
    throws IOException;
  
  public abstract byte[] readByteArray()
    throws IOException;
  
  public abstract ByteBuffer readByteBuffer()
    throws IOException;
  
  public abstract <T> T mergeObject(T paramT, Schema<T> paramSchema)
    throws IOException;
  
  public abstract void transferByteRangeTo(Output paramOutput, boolean paramBoolean1, int paramInt, boolean paramBoolean2)
    throws IOException;
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.Input
 * JD-Core Version:    0.7.0.1
 */