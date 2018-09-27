package org.jupiter.rpc;

import org.jupiter.common.util.Preconditions;






public class DefaultFilterChain
  implements JFilterChain
{
  private final JFilter filter;
  private final JFilterChain next;
  
  public DefaultFilterChain(JFilter filter, JFilterChain next)
  {
    this.filter = ((JFilter)Preconditions.checkNotNull(filter, "filter"));
    this.next = next;
  }
  
  public JFilter getFilter()
  {
    return filter;
  }
  
  public JFilterChain getNext()
  {
    return next;
  }
  
  public <T extends JFilterContext> void doFilter(JRequest request, T filterCtx) throws Throwable
  {
    filter.doFilter(request, filterCtx, next);
  }
}
