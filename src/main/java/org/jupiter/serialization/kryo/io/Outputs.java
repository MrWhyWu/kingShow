package org.jupiter.serialization.kryo.io;

import com.esotericsoftware.kryo.io.FastOutput;
import com.esotericsoftware.kryo.io.Output;
import org.jupiter.common.util.internal.InternalThreadLocal;
import org.jupiter.serialization.io.OutputBuf;



























public final class Outputs
{
  private static final InternalThreadLocal<Output> outputBytesThreadLocal = new InternalThreadLocal()
  {
    protected Output initialValue()
    {
      return new FastOutput(512, -1);
    }
  };
  
  public static Output getOutput(OutputBuf outputBuf) {
    NioBufOutput output = new NioBufOutput(outputBuf, -1, Integer.MAX_VALUE);
    output.setVarIntsEnabled(false);
    return output;
  }
  
  public static Output getOutput() {
    return (Output)outputBytesThreadLocal.get();
  }
  
  public static void clearOutput(Output output) {
    output.clear();
    

    byte[] bytes = output.getBuffer();
    if (bytes == null) {
      return;
    }
    if (bytes.length > 262144) {
      output.setBuffer(new byte['Ȁ'], -1);
    }
  }
  
  private Outputs() {}
}
