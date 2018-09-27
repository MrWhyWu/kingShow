package io.opentracing;

import java.util.Map;

public abstract interface Span
{
  public abstract SpanContext context();
  
  public abstract Span setTag(String paramString1, String paramString2);
  
  public abstract Span setTag(String paramString, boolean paramBoolean);
  
  public abstract Span setTag(String paramString, Number paramNumber);
  
  public abstract Span log(Map<String, ?> paramMap);
  
  public abstract Span log(long paramLong, Map<String, ?> paramMap);
  
  public abstract Span log(String paramString);
  
  public abstract Span log(long paramLong, String paramString);
  
  public abstract Span setBaggageItem(String paramString1, String paramString2);
  
  public abstract String getBaggageItem(String paramString);
  
  public abstract Span setOperationName(String paramString);
  
  public abstract void finish();
  
  public abstract void finish(long paramLong);
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.opentracing.Span
 * JD-Core Version:    0.7.0.1
 */