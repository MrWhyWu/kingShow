package io.protostuff.runtime;

import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.Pipe;
import io.protostuff.WireFormat.FieldType;
import java.io.IOException;

public abstract interface Delegate<V>
{
  public abstract WireFormat.FieldType getFieldType();
  
  public abstract V readFrom(Input paramInput)
    throws IOException;
  
  public abstract void writeTo(Output paramOutput, int paramInt, V paramV, boolean paramBoolean)
    throws IOException;
  
  public abstract void transfer(Pipe paramPipe, Input paramInput, Output paramOutput, int paramInt, boolean paramBoolean)
    throws IOException;
  
  public abstract Class<?> typeClass();
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.Delegate
 * JD-Core Version:    0.7.0.1
 */