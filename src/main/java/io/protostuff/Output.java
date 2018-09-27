package io.protostuff;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract interface Output
{
  public abstract void writeInt32(int paramInt1, int paramInt2, boolean paramBoolean)
    throws IOException;
  
  public abstract void writeUInt32(int paramInt1, int paramInt2, boolean paramBoolean)
    throws IOException;
  
  public abstract void writeSInt32(int paramInt1, int paramInt2, boolean paramBoolean)
    throws IOException;
  
  public abstract void writeFixed32(int paramInt1, int paramInt2, boolean paramBoolean)
    throws IOException;
  
  public abstract void writeSFixed32(int paramInt1, int paramInt2, boolean paramBoolean)
    throws IOException;
  
  public abstract void writeInt64(int paramInt, long paramLong, boolean paramBoolean)
    throws IOException;
  
  public abstract void writeUInt64(int paramInt, long paramLong, boolean paramBoolean)
    throws IOException;
  
  public abstract void writeSInt64(int paramInt, long paramLong, boolean paramBoolean)
    throws IOException;
  
  public abstract void writeFixed64(int paramInt, long paramLong, boolean paramBoolean)
    throws IOException;
  
  public abstract void writeSFixed64(int paramInt, long paramLong, boolean paramBoolean)
    throws IOException;
  
  public abstract void writeFloat(int paramInt, float paramFloat, boolean paramBoolean)
    throws IOException;
  
  public abstract void writeDouble(int paramInt, double paramDouble, boolean paramBoolean)
    throws IOException;
  
  public abstract void writeBool(int paramInt, boolean paramBoolean1, boolean paramBoolean2)
    throws IOException;
  
  public abstract void writeEnum(int paramInt1, int paramInt2, boolean paramBoolean)
    throws IOException;
  
  public abstract void writeString(int paramInt, CharSequence paramCharSequence, boolean paramBoolean)
    throws IOException;
  
  public abstract void writeBytes(int paramInt, ByteString paramByteString, boolean paramBoolean)
    throws IOException;
  
  public abstract void writeByteArray(int paramInt, byte[] paramArrayOfByte, boolean paramBoolean)
    throws IOException;
  
  public abstract void writeByteRange(boolean paramBoolean1, int paramInt1, byte[] paramArrayOfByte, int paramInt2, int paramInt3, boolean paramBoolean2)
    throws IOException;
  
  public abstract <T> void writeObject(int paramInt, T paramT, Schema<T> paramSchema, boolean paramBoolean)
    throws IOException;
  
  public abstract void writeBytes(int paramInt, ByteBuffer paramByteBuffer, boolean paramBoolean)
    throws IOException;
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.Output
 * JD-Core Version:    0.7.0.1
 */