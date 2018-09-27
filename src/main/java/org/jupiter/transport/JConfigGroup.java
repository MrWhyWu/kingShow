package org.jupiter.transport;

public abstract interface JConfigGroup
{
  public abstract JConfig parent();
  
  public abstract JConfig child();
}
