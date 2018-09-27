package io.netty.handler.ssl;

import java.util.List;
import java.util.Set;

public abstract interface CipherSuiteFilter
{
  public abstract String[] filterCipherSuites(Iterable<String> paramIterable, List<String> paramList, Set<String> paramSet);
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.CipherSuiteFilter
 * JD-Core Version:    0.7.0.1
 */