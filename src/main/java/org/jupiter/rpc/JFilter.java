package org.jupiter.rpc;








public abstract interface JFilter
{
  public abstract Type getType();
  







  public abstract <T extends JFilterContext> void doFilter(JRequest paramJRequest, T paramT, JFilterChain paramJFilterChain)
    throws Throwable;
  







  public static enum Type
  {
    CONSUMER, 
    PROVIDER, 
    ALL;
    
    private Type() {}
  }
}
