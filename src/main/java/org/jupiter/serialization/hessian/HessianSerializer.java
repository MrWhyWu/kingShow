package org.jupiter.serialization.hessian;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.jupiter.common.util.ThrowUtil;
import org.jupiter.serialization.Serializer;
import org.jupiter.serialization.SerializerType;
import org.jupiter.serialization.hessian.io.Inputs;
import org.jupiter.serialization.hessian.io.Outputs;
import org.jupiter.serialization.io.InputBuf;
import org.jupiter.serialization.io.OutputBuf;
import org.jupiter.serialization.io.OutputStreams;























public class HessianSerializer
  extends Serializer
{
  public HessianSerializer() {}
  
  public byte code()
  {
    return SerializerType.HESSIAN.value();
  }
  
  public <T> OutputBuf writeObject(OutputBuf outputBuf, T obj)
  {
    Hessian2Output output = Outputs.getOutput(outputBuf);
    try {
      output.writeObject(obj);
      output.flush();
      return outputBuf;
    } catch (IOException e) {
      ThrowUtil.throwException(e);
    } finally {
      try {
        output.close();
      } catch (IOException localIOException3) {}
    }
    return null;
  }
  
  public <T> byte[] writeObject(T obj)
  {
    ByteArrayOutputStream buf = OutputStreams.getByteArrayOutputStream();
    Hessian2Output output = Outputs.getOutput(buf);
    try {
      output.writeObject(obj);
      output.flush();
      return buf.toByteArray();
    } catch (IOException e) {
      ThrowUtil.throwException(e);
    } finally {
      try {
        output.close();
      }
      catch (IOException localIOException3) {}
      OutputStreams.resetBuf(buf);
    }
    return null;
  }
  
  public <T> T readObject(InputBuf inputBuf, Class<T> clazz)
  {
    Hessian2Input input = Inputs.getInput(inputBuf);
    try {
      Object obj = input.readObject(clazz);
      return clazz.cast(obj);
    } catch (IOException e) {
      ThrowUtil.throwException(e);
    } finally {
      try {
        input.close();
      }
      catch (IOException localIOException3) {}
      inputBuf.release();
    }
    return null;
  }
  
  public <T> T readObject(byte[] bytes, int offset, int length, Class<T> clazz)
  {
    Hessian2Input input = Inputs.getInput(bytes, offset, length);
    try {
      Object obj = input.readObject(clazz);
      return clazz.cast(obj);
    } catch (IOException e) {
      ThrowUtil.throwException(e);
    } finally {
      try {
        input.close();
      } catch (IOException localIOException3) {}
    }
    return null;
  }
  
  public String toString()
  {
    return "hessian:(code=" + code() + ")";
  }
}
