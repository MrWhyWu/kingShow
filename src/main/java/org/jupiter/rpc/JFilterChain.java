package org.jupiter.rpc;

public abstract interface JFilterChain
{
  public abstract JFilter getFilter();
  
  public abstract JFilterChain getNext();
  
  public abstract <T extends JFilterContext> void doFilter(JRequest paramJRequest, T paramT)
    throws Throwable;
}
