package org.jupiter.serialization.io;

import java.io.InputStream;
import java.nio.ByteBuffer;

public abstract interface InputBuf
{
  public abstract InputStream inputStream();
  
  public abstract ByteBuffer nioByteBuffer();
  
  public abstract int size();
  
  public abstract boolean hasMemoryAddress();
  
  public abstract boolean release();
}
