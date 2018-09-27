package org.jupiter.transport;

import java.util.List;

public abstract interface JConfig
{
  public abstract List<JOption<?>> getOptions();
  
  public abstract <T> T getOption(JOption<T> paramJOption);
  
  public abstract <T> boolean setOption(JOption<T> paramJOption, T paramT);
}
