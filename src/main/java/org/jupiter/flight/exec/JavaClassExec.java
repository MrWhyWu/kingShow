package org.jupiter.flight.exec;

import org.jupiter.rpc.ServiceProvider;

@ServiceProvider(name="javaClassExec", group="exec")
public abstract interface JavaClassExec
{
  public abstract ExecResult exec(byte[] paramArrayOfByte);
}
