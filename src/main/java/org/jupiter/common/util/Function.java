package org.jupiter.common.util;

public abstract interface Function<F, T>
{
  public abstract T apply(F paramF);
  
  public abstract boolean equals(Object paramObject);
}
