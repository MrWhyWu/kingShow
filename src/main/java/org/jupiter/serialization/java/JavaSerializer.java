package org.jupiter.serialization.java;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.jupiter.common.util.ThrowUtil;
import org.jupiter.serialization.Serializer;
import org.jupiter.serialization.SerializerType;
import org.jupiter.serialization.io.InputBuf;
import org.jupiter.serialization.io.OutputBuf;
import org.jupiter.serialization.io.OutputStreams;
import org.jupiter.serialization.java.io.Inputs;
import org.jupiter.serialization.java.io.Outputs;























public class JavaSerializer
  extends Serializer
{
  public JavaSerializer() {}
  
  public byte code()
  {
    return SerializerType.JAVA.value();
  }
  
  public <T> OutputBuf writeObject(OutputBuf outputBuf, T obj)
  {
    ObjectOutputStream output = null;
    try {
      output = Outputs.getOutput(outputBuf);
      output.writeObject(obj);
      output.flush();
      return outputBuf;
    } catch (IOException e) {
      ThrowUtil.throwException(e);
    } finally {
      if (output != null) {
        try {
          output.close();
        } catch (IOException localIOException3) {}
      }
    }
    return null;
  }
  
  public <T> byte[] writeObject(T obj)
  {
    ByteArrayOutputStream buf = OutputStreams.getByteArrayOutputStream();
    ObjectOutputStream output = null;
    try {
      output = Outputs.getOutput(buf);
      output.writeObject(obj);
      output.flush();
      return buf.toByteArray();
    } catch (IOException e) {
      ThrowUtil.throwException(e);
    } finally {
      if (output != null) {
        try {
          output.close();
        } catch (IOException localIOException3) {}
      }
      OutputStreams.resetBuf(buf);
    }
    return null;
  }
  
  public <T> T readObject(InputBuf inputBuf, Class<T> clazz)
  {
    ObjectInputStream input = null;
    try {
      input = Inputs.getInput(inputBuf);
      Object obj = input.readObject();
      return clazz.cast(obj);
    } catch (Exception e) {
      ThrowUtil.throwException(e);
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException localIOException2) {}
      }
      inputBuf.release();
    }
    return null;
  }
  
  public <T> T readObject(byte[] bytes, int offset, int length, Class<T> clazz)
  {
    ObjectInputStream input = null;
    try {
      input = Inputs.getInput(bytes, offset, length);
      Object obj = input.readObject();
      return clazz.cast(obj);
    } catch (Exception e) {
      ThrowUtil.throwException(e);
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException localIOException2) {}
      }
    }
    return null;
  }
  
  public String toString()
  {
    return "java:(code=" + code() + ")";
  }
}
