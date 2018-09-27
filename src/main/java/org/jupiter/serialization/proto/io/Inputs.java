package org.jupiter.serialization.proto.io;

import io.protostuff.ByteArrayInput;
import io.protostuff.Input;
import io.protostuff.ProtobufException;
import org.jupiter.serialization.io.InputBuf;






















public final class Inputs
{
  public static Input getInput(InputBuf inputBuf)
  {
    if (inputBuf.hasMemoryAddress()) {
      return new UnsafeNioBufInput(inputBuf.nioByteBuffer(), true);
    }
    return new NioBufInput(inputBuf.nioByteBuffer(), true);
  }
  
  public static Input getInput(byte[] bytes, int offset, int length) {
    return new ByteArrayInput(bytes, offset, length, true);
  }
  
  public static void checkLastTagWas(Input input, int value) throws ProtobufException {
    if ((input instanceof UnsafeNioBufInput)) {
      ((UnsafeNioBufInput)input).checkLastTagWas(value);
    } else if ((input instanceof NioBufInput)) {
      ((NioBufInput)input).checkLastTagWas(value);
    } else if ((input instanceof ByteArrayInput)) {
      ((ByteArrayInput)input).checkLastTagWas(value);
    }
  }
  
  private Inputs() {}
}
