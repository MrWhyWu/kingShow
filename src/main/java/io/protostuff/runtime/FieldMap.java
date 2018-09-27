package io.protostuff.runtime;

import java.util.List;

abstract interface FieldMap<T>
{
  public abstract Field<T> getFieldByNumber(int paramInt);
  
  public abstract Field<T> getFieldByName(String paramString);
  
  public abstract int getFieldCount();
  
  public abstract List<Field<T>> getFields();
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.FieldMap
 * JD-Core Version:    0.7.0.1
 */