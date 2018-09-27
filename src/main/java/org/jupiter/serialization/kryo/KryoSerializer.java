package org.jupiter.serialization.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import org.jupiter.common.concurrent.collection.ConcurrentSet;
import org.jupiter.common.util.internal.InternalThreadLocal;
import org.jupiter.serialization.Serializer;
import org.jupiter.serialization.SerializerType;
import org.jupiter.serialization.io.InputBuf;
import org.jupiter.serialization.io.OutputBuf;
import org.jupiter.serialization.kryo.io.Inputs;
import org.jupiter.serialization.kryo.io.Outputs;
import org.objenesis.strategy.StdInstantiatorStrategy;




























public class KryoSerializer
  extends Serializer
{
  private static ConcurrentSet<Class<?>> useJavaSerializerTypes = new ConcurrentSet();
  
  static {
    useJavaSerializerTypes.add(Throwable.class);
  }
  
  private static final InternalThreadLocal<Kryo> kryoThreadLocal = new InternalThreadLocal()
  {
    protected Kryo initialValue() throws Exception
    {
      Kryo kryo = new Kryo();
      for (Class<?> type : KryoSerializer.useJavaSerializerTypes) {
        kryo.addDefaultSerializer(type, JavaSerializer.class);
      }
      kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
      kryo.setRegistrationRequired(false);
      kryo.setReferences(false);
      return kryo;
    }
  };
  



  public static void setJavaSerializer(Class<?> type)
  {
    useJavaSerializerTypes.add(type);
  }
  
  public byte code()
  {
    return SerializerType.KRYO.value();
  }
  
  public <T> OutputBuf writeObject(OutputBuf outputBuf, T obj)
  {
    Output output = Outputs.getOutput(outputBuf);
    Kryo kryo = (Kryo)kryoThreadLocal.get();
    kryo.writeObject(output, obj);
    return outputBuf;
  }
  
  public <T> byte[] writeObject(T obj)
  {
    Output output = Outputs.getOutput();
    Kryo kryo = (Kryo)kryoThreadLocal.get();
    try {
      kryo.writeObject(output, obj);
      return output.toBytes();
    } finally {
      Outputs.clearOutput(output);
    }
  }
  
  public <T> T readObject(InputBuf inputBuf, Class<T> clazz)
  {
    Input input = Inputs.getInput(inputBuf);
    Kryo kryo = (Kryo)kryoThreadLocal.get();
    try {
      return kryo.readObject(input, clazz);
    } finally {
      inputBuf.release();
    }
  }
  
  public <T> T readObject(byte[] bytes, int offset, int length, Class<T> clazz)
  {
    Input input = Inputs.getInput(bytes, offset, length);
    Kryo kryo = (Kryo)kryoThreadLocal.get();
    return kryo.readObject(input, clazz);
  }
  
  public String toString()
  {
    return "kryo:(code=" + code() + ")";
  }
  
  public KryoSerializer() {}
}
