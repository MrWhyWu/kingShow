package io.opentracing;

public abstract interface ScopeManager
{
  public abstract Scope activate(Span paramSpan, boolean paramBoolean);
  
  public abstract Scope active();
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.opentracing.ScopeManager
 * JD-Core Version:    0.7.0.1
 */