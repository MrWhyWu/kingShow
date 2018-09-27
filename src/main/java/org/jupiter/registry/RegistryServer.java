package org.jupiter.registry;

import java.lang.reflect.Constructor;
import java.net.SocketAddress;
import java.util.List;
import org.jupiter.common.util.Lists;
import org.jupiter.common.util.Reflects;
import org.jupiter.common.util.SystemPropertyUtil;
import org.jupiter.common.util.ThrowUtil;

























public abstract interface RegistryServer
  extends RegistryMonitor
{
  public abstract void startRegistryServer();
  
  public static class Default
  {
    private static final Class<RegistryServer> defaultRegistryClass;
    private static final List<Class<?>[]> allConstructorsParameterTypes;
    
    static
    {
      Class<RegistryServer> cls;
      try
      {
        cls = Class.forName(SystemPropertyUtil.get("jupiter.registry.default", "org.jupiter.registry.DefaultRegistryServer"));
      } catch (ClassNotFoundException e) {
        Class<RegistryServer> cls;
        cls = null;
      }
      defaultRegistryClass = cls;
      
      if (defaultRegistryClass != null) {
        allConstructorsParameterTypes = Lists.newArrayList();
        Constructor<?>[] array = defaultRegistryClass.getDeclaredConstructors();
        for (Constructor<?> c : array) {
          allConstructorsParameterTypes.add(c.getParameterTypes());
        }
      } else {
        allConstructorsParameterTypes = null;
      }
    }
    
    public static RegistryServer createRegistryServer(int port) {
      return newInstance(new Object[] { Integer.valueOf(port) });
    }
    
    public static RegistryServer createRegistryServer(SocketAddress address) {
      return newInstance(new Object[] { address });
    }
    
    public static RegistryServer createRegistryServer(int port, int nWorkers) {
      return newInstance(new Object[] { Integer.valueOf(port), Integer.valueOf(nWorkers) });
    }
    
    public static RegistryServer createRegistryServer(SocketAddress address, int nWorkers) {
      return newInstance(new Object[] { address, Integer.valueOf(nWorkers) });
    }
    
    private static RegistryServer newInstance(Object... parameters) {
      if ((defaultRegistryClass == null) || (allConstructorsParameterTypes == null)) {
        throw new UnsupportedOperationException("Unsupported default registry");
      }
      

      Class<?>[] parameterTypes = Reflects.findMatchingParameterTypes(allConstructorsParameterTypes, parameters);
      if (parameterTypes == null) {
        throw new IllegalArgumentException("Parameter types");
      }
      
      try
      {
        Constructor<RegistryServer> c = defaultRegistryClass.getConstructor(parameterTypes);
        c.setAccessible(true);
        return (RegistryServer)c.newInstance(parameters);
      } catch (Exception e) {
        ThrowUtil.throwException(e);
      }
      return null;
    }
    
    public Default() {}
  }
}
