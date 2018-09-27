package org.jupiter.flight.exec;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;

























public class FlightExecClassLoader
  extends ClassLoader
{
  private static ProtectionDomain PROTECTION_DOMAIN = (ProtectionDomain)AccessController.doPrivileged(new PrivilegedAction()
  {
    public ProtectionDomain run()
    {
      return FlightExecClassLoader.class.getProtectionDomain();
    }
  });
  






  public FlightExecClassLoader()
  {
    super(Thread.currentThread().getContextClassLoader());
  }
  
  public Class<?> loadBytes(byte[] classBytes) {
    return defineClass(null, classBytes, 0, classBytes.length, PROTECTION_DOMAIN);
  }
}
