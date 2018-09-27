package org.jupiter.transport;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.jupiter.common.util.AbstractConstant;
import org.jupiter.common.util.ConstantPool;




























public final class JOption<T>
  extends AbstractConstant<JOption<T>>
{
  private static final ConstantPool<JOption<Object>> pool = new ConstantPool()
  {
    protected JOption<Object> newConstant(int id, String name)
    {
      return new JOption(id, name, null);
    }
  };
  


  public static <T> JOption<T> valueOf(String name)
  {
    return (JOption)pool.valueOf(name);
  }
  


  public static <T> JOption<T> valueOf(Class<?> firstNameComponent, String secondNameComponent)
  {
    return (JOption)pool.valueOf(firstNameComponent, secondNameComponent);
  }
  



  public static <T> JOption<T> newInstance(String name)
  {
    return (JOption)pool.newInstance(name);
  }
  


  public static boolean exists(String name)
  {
    return pool.exists(name);
  }
  



  public static final JOption<Boolean> TCP_NODELAY = valueOf("TCP_NODELAY");
  















  public static final JOption<Boolean> KEEP_ALIVE = valueOf("KEEP_ALIVE");
  










  public static final JOption<Boolean> SO_REUSEADDR = valueOf("SO_REUSEADDR");
  




  public static final JOption<Integer> SO_SNDBUF = valueOf("SO_SNDBUF");
  




  public static final JOption<Integer> SO_RCVBUF = valueOf("SO_RCVBUF");
  
  public static final JOption<Integer> SO_LINGER = valueOf("SO_LINGER");
  






























  public static final JOption<Integer> SO_BACKLOG = valueOf("SO_BACKLOG");
  
  public static final JOption<Integer> IP_TOS = valueOf("IP_TOS");
  
  public static final JOption<Boolean> ALLOW_HALF_CLOSURE = valueOf("ALLOW_HALF_CLOSURE");
  



  public static final JOption<Integer> WRITE_BUFFER_HIGH_WATER_MARK = valueOf("WRITE_BUFFER_HIGH_WATER_MARK");
  



  public static final JOption<Integer> WRITE_BUFFER_LOW_WATER_MARK = valueOf("WRITE_BUFFER_LOW_WATER_MARK");
  





  public static final JOption<Integer> IO_RATIO = valueOf("IO_RATIO");
  
  public static final JOption<Integer> CONNECT_TIMEOUT_MILLIS = valueOf("CONNECT_TIMEOUT_MILLIS");
  public static final Set<JOption<?>> ALL_OPTIONS;
  
  static
  {
    Set<JOption<?>> options = new HashSet();
    
    options.add(TCP_NODELAY);
    options.add(KEEP_ALIVE);
    options.add(SO_REUSEADDR);
    options.add(SO_SNDBUF);
    options.add(SO_RCVBUF);
    options.add(SO_LINGER);
    options.add(SO_BACKLOG);
    options.add(IP_TOS);
    options.add(ALLOW_HALF_CLOSURE);
    options.add(WRITE_BUFFER_HIGH_WATER_MARK);
    options.add(WRITE_BUFFER_LOW_WATER_MARK);
    options.add(IO_RATIO);
    options.add(CONNECT_TIMEOUT_MILLIS);
    
    ALL_OPTIONS = Collections.unmodifiableSet(options);
  }
  
  private JOption(int id, String name) {
    super(id, name);
  }
}
