package org.jupiter.flight.exec;

import org.jupiter.common.util.ThrowUtil;
import org.jupiter.rpc.ServiceProviderImpl;


























@ServiceProviderImpl
public class JavaClassExecProvider
  implements JavaClassExec
{
  private static String SYSTEM_STRING = System.class.getName().replace('.', '/');
  private static String HACK_SYSTEM_STRING = HackSystem.class.getName().replace('.', '/');
  
  public JavaClassExecProvider() {}
  
  public ExecResult exec(byte[] classBytes) { ExecResult result = new ExecResult();
    UserExecInterface executor = null;
    try
    {
      ClassModifier cm = new ClassModifier(classBytes);
      classBytes = cm.modifyUTF8Constant(SYSTEM_STRING, HACK_SYSTEM_STRING);
      

      FlightExecClassLoader loader = new FlightExecClassLoader();
      Class<?> clazz = loader.loadBytes(classBytes);
      
      executor = (UserExecInterface)clazz.newInstance();
    } catch (Throwable t) {
      ThrowUtil.throwException(t);
    }
    
    synchronized (HackSystem.class) {
      HackSystem.clearBuf();
      Object value = null;
      try
      {
        if (executor != null) {
          value = executor.exec();
        }
      } catch (Throwable t) {
        t.printStackTrace(HackSystem.out);
      } finally {
        result.setDebugInfo(HackSystem.getBufString());
        result.setValue(value);
      }
    }
    return result;
  }
}
