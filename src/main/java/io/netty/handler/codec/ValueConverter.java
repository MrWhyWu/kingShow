package io.netty.handler.codec;

public abstract interface ValueConverter<T>
{
  public abstract T convertObject(Object paramObject);
  
  public abstract T convertBoolean(boolean paramBoolean);
  
  public abstract boolean convertToBoolean(T paramT);
  
  public abstract T convertByte(byte paramByte);
  
  public abstract byte convertToByte(T paramT);
  
  public abstract T convertChar(char paramChar);
  
  public abstract char convertToChar(T paramT);
  
  public abstract T convertShort(short paramShort);
  
  public abstract short convertToShort(T paramT);
  
  public abstract T convertInt(int paramInt);
  
  public abstract int convertToInt(T paramT);
  
  public abstract T convertLong(long paramLong);
  
  public abstract long convertToLong(T paramT);
  
  public abstract T convertTimeMillis(long paramLong);
  
  public abstract long convertToTimeMillis(T paramT);
  
  public abstract T convertFloat(float paramFloat);
  
  public abstract float convertToFloat(T paramT);
  
  public abstract T convertDouble(double paramDouble);
  
  public abstract double convertToDouble(T paramT);
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.ValueConverter
 * JD-Core Version:    0.7.0.1
 */