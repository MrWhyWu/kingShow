package org.jupiter.serialization.io;

import java.io.OutputStream;
import java.nio.ByteBuffer;

public abstract interface OutputBuf
{
  public abstract OutputStream outputStream();
  
  public abstract ByteBuffer nioByteBuffer(int paramInt);
  
  public abstract int size();
  
  public abstract boolean hasMemoryAddress();
  
  public abstract Object backingObject();
}
