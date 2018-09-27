package org.jupiter.serialization.proto.io;

import io.protostuff.LinkedBuffer;
import org.jupiter.common.util.internal.InternalThreadLocal;


























public final class LinkedBuffers
{
  private static final InternalThreadLocal<LinkedBuffer> bufThreadLocal = new InternalThreadLocal()
  {
    protected LinkedBuffer initialValue()
    {
      return LinkedBuffer.allocate(512);
    }
  };
  
  public static LinkedBuffer getLinkedBuffer() {
    return (LinkedBuffer)bufThreadLocal.get();
  }
  
  public static void resetBuf(LinkedBuffer buf) {
    buf.clear();
  }
  
  private LinkedBuffers() {}
}
