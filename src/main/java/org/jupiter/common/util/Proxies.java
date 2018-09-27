package org.jupiter.common.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.DynamicType.Builder.MethodDefinition.ImplementationDefinition;
import net.bytebuddy.dynamic.DynamicType.Builder.MethodDefinition.ReceiverTypeDefinition;
import net.bytebuddy.dynamic.DynamicType.Loaded;
import net.bytebuddy.dynamic.DynamicType.Unloaded;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy.Default;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;




















public enum Proxies
{
  JDK_PROXY(new ProxyDelegate()), 
  










  BYTE_BUDDY(new ProxyDelegate());
  









  private final ProxyDelegate delegate;
  









  private Proxies(ProxyDelegate delegate)
  {
    this.delegate = delegate;
  }
  
  public static Proxies getDefault() {
    return BYTE_BUDDY;
  }
  
  public <T> T newProxy(Class<T> interfaceType, Object handler) {
    return delegate.newProxy(interfaceType, handler);
  }
  
  static abstract interface ProxyDelegate
  {
    public abstract <T> T newProxy(Class<T> paramClass, Object paramObject);
  }
}
