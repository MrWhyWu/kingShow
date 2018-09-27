package org.jupiter.serialization.proto.io;

import io.protostuff.LinkedBuffer;
import io.protostuff.Output;
import io.protostuff.ProtostuffOutput;
import io.protostuff.WriteSession;
import org.jupiter.common.util.Reflects;
import org.jupiter.serialization.io.OutputBuf;






















public final class Outputs
{
  public static Output getOutput(OutputBuf outputBuf)
  {
    if (outputBuf.hasMemoryAddress()) {
      return new UnsafeNioBufOutput(outputBuf, -1, Integer.MAX_VALUE);
    }
    return new NioBufOutput(outputBuf, -1, Integer.MAX_VALUE);
  }
  
  public static Output getOutput(LinkedBuffer buf) {
    return new ProtostuffOutput(buf);
  }
  
  public static byte[] toByteArray(Output output) {
    if ((output instanceof WriteSession)) {
      return ((WriteSession)output).toByteArray();
    }
    throw new IllegalArgumentException("Output [" + Reflects.simpleClassName(output) + "] must be a WriteSession's implementation");
  }
  
  private Outputs() {}
}
