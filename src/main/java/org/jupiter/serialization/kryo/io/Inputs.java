package org.jupiter.serialization.kryo.io;

import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.FastInput;
import com.esotericsoftware.kryo.io.Input;
import java.nio.ByteBuffer;
import org.jupiter.serialization.io.InputBuf;























public final class Inputs
{
  public static Input getInput(InputBuf inputBuf)
  {
    ByteBuffer nioBuf = inputBuf.nioByteBuffer();
    ByteBufferInput input = new ByteBufferInput();
    input.setVarIntsEnabled(false);
    input.setBuffer(nioBuf, 0, nioBuf.capacity());
    return input;
  }
  
  public static Input getInput(byte[] bytes, int offset, int length) {
    return new FastInput(bytes, offset, length);
  }
  
  private Inputs() {}
}
