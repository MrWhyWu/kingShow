package org.jupiter.serialization.hessian.io;

import com.caucho.hessian.io.Hessian2Output;
import java.io.OutputStream;
import org.jupiter.serialization.io.OutputBuf;























public final class Outputs
{
  public static Hessian2Output getOutput(OutputBuf outputBuf)
  {
    return new Hessian2Output(outputBuf.outputStream());
  }
  
  public static Hessian2Output getOutput(OutputStream buf) {
    return new Hessian2Output(buf);
  }
  
  private Outputs() {}
}
