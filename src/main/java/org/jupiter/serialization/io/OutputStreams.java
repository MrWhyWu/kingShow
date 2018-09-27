package org.jupiter.serialization.io;

import java.io.ByteArrayOutputStream;
import org.jupiter.common.util.internal.InternalThreadLocal;
import org.jupiter.common.util.internal.UnsafeReferenceFieldUpdater;
import org.jupiter.common.util.internal.UnsafeUpdater;



























public final class OutputStreams
{
  private static final UnsafeReferenceFieldUpdater<ByteArrayOutputStream, byte[]> bufUpdater = UnsafeUpdater.newReferenceFieldUpdater(ByteArrayOutputStream.class, "buf");
  


  private static final InternalThreadLocal<ByteArrayOutputStream> bufThreadLocal = new InternalThreadLocal()
  {
    protected ByteArrayOutputStream initialValue()
    {
      return new ByteArrayOutputStream(512);
    }
  };
  
  public static ByteArrayOutputStream getByteArrayOutputStream() {
    return (ByteArrayOutputStream)bufThreadLocal.get();
  }
  
  public static void resetBuf(ByteArrayOutputStream buf) {
    buf.reset();
    

    assert (bufUpdater != null);
    if (((byte[])bufUpdater.get(buf)).length > 262144) {
      bufUpdater.set(buf, new byte['Ȁ']);
    }
  }
  
  private OutputStreams() {}
}
