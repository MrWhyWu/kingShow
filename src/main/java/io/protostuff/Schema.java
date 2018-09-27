package io.protostuff;

import java.io.IOException;

public abstract interface Schema<T>
{
  public abstract String getFieldName(int paramInt);
  
  public abstract int getFieldNumber(String paramString);
  
  public abstract boolean isInitialized(T paramT);
  
  public abstract T newMessage();
  
  public abstract String messageName();
  
  public abstract String messageFullName();
  
  public abstract Class<? super T> typeClass();
  
  public abstract void mergeFrom(Input paramInput, T paramT)
    throws IOException;
  
  public abstract void writeTo(Output paramOutput, T paramT)
    throws IOException;
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.Schema
 * JD-Core Version:    0.7.0.1
 */