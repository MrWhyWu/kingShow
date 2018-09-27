package org.jupiter.serialization.proto;

import io.protostuff.Input;
import io.protostuff.LinkedBuffer;
import io.protostuff.Output;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import java.io.IOException;
import org.jupiter.common.util.SystemPropertyUtil;
import org.jupiter.common.util.ThrowUtil;
import org.jupiter.serialization.Serializer;
import org.jupiter.serialization.SerializerType;
import org.jupiter.serialization.io.InputBuf;
import org.jupiter.serialization.io.OutputBuf;
import org.jupiter.serialization.proto.io.Inputs;
import org.jupiter.serialization.proto.io.LinkedBuffers;
import org.jupiter.serialization.proto.io.Outputs;



































public class ProtoStuffSerializer
  extends Serializer
{
  static
  {
    String always_use_sun_reflection_factory = SystemPropertyUtil.get("jupiter.serializer.protostuff.always_use_sun_reflection_factory", "true");
    
    SystemPropertyUtil.setProperty("protostuff.runtime.always_use_sun_reflection_factory", always_use_sun_reflection_factory);
    




    String allow_null_array_element = SystemPropertyUtil.get("jupiter.serializer.protostuff.allow_null_array_element", "false");
    
    SystemPropertyUtil.setProperty("protostuff.runtime.allow_null_array_element", allow_null_array_element);
  }
  

  public byte code()
  {
    return SerializerType.PROTO_STUFF.value();
  }
  

  public <T> OutputBuf writeObject(OutputBuf outputBuf, T obj)
  {
    Schema<T> schema = RuntimeSchema.getSchema(obj.getClass());
    
    Output output = Outputs.getOutput(outputBuf);
    try {
      schema.writeTo(output, obj);
    } catch (IOException e) {
      ThrowUtil.throwException(e);
    }
    
    return outputBuf;
  }
  

  public <T> byte[] writeObject(T obj)
  {
    Schema<T> schema = RuntimeSchema.getSchema(obj.getClass());
    
    LinkedBuffer buf = LinkedBuffers.getLinkedBuffer();
    Output output = Outputs.getOutput(buf);
    try {
      schema.writeTo(output, obj);
      return Outputs.toByteArray(output);
    } catch (IOException e) {
      ThrowUtil.throwException(e);
    } finally {
      LinkedBuffers.resetBuf(buf);
    }
    
    return null;
  }
  
  public <T> T readObject(InputBuf inputBuf, Class<T> clazz)
  {
    Schema<T> schema = RuntimeSchema.getSchema(clazz);
    T msg = schema.newMessage();
    
    Input input = Inputs.getInput(inputBuf);
    try {
      schema.mergeFrom(input, msg);
      Inputs.checkLastTagWas(input, 0);
    } catch (IOException e) {
      ThrowUtil.throwException(e);
    } finally {
      inputBuf.release();
    }
    
    return msg;
  }
  
  public <T> T readObject(byte[] bytes, int offset, int length, Class<T> clazz)
  {
    Schema<T> schema = RuntimeSchema.getSchema(clazz);
    T msg = schema.newMessage();
    
    Input input = Inputs.getInput(bytes, offset, length);
    try {
      schema.mergeFrom(input, msg);
      Inputs.checkLastTagWas(input, 0);
    } catch (IOException e) {
      ThrowUtil.throwException(e);
    }
    
    return msg;
  }
  
  public String toString()
  {
    return "proto_stuff:(code=" + code() + ")";
  }
  
  public ProtoStuffSerializer() {}
}
