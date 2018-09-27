package org.jupiter.rpc.provider;

import org.jupiter.rpc.model.metadata.ServiceWrapper;
import org.jupiter.transport.Directory;

public abstract interface LookupService
{
  public abstract ServiceWrapper lookupService(Directory paramDirectory);
}
