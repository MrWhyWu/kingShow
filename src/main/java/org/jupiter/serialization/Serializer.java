package org.jupiter.serialization;

import org.jupiter.serialization.io.InputBuf;
import org.jupiter.serialization.io.OutputBuf;































public abstract class Serializer
{
  public static final int MAX_CACHED_BUF_SIZE = 262144;
  public static final int DEFAULT_BUF_SIZE = 512;
  
  public Serializer() {}
  
  public abstract byte code();
  
  public abstract <T> OutputBuf writeObject(OutputBuf paramOutputBuf, T paramT);
  
  public abstract <T> byte[] writeObject(T paramT);
  
  public abstract <T> T readObject(InputBuf paramInputBuf, Class<T> paramClass);
  
  public abstract <T> T readObject(byte[] paramArrayOfByte, int paramInt1, int paramInt2, Class<T> paramClass);
  
  public <T> T readObject(byte[] bytes, Class<T> clazz)
  {
    return readObject(bytes, 0, bytes.length, clazz);
  }
}
