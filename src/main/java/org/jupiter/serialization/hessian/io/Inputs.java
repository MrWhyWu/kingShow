package org.jupiter.serialization.hessian.io;

import com.caucho.hessian.io.Hessian2Input;
import java.io.ByteArrayInputStream;
import org.jupiter.serialization.io.InputBuf;























public final class Inputs
{
  public static Hessian2Input getInput(InputBuf inputBuf)
  {
    return new Hessian2Input(inputBuf.inputStream());
  }
  
  public static Hessian2Input getInput(byte[] bytes, int offset, int length) {
    return new Hessian2Input(new ByteArrayInputStream(bytes, offset, length));
  }
  
  private Inputs() {}
}
