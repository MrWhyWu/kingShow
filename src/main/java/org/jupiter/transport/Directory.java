package org.jupiter.transport;

import org.jupiter.common.util.StringBuilderHelper;

























public abstract class Directory
{
  private String directoryCache;
  
  public Directory() {}
  
  public abstract String getGroup();
  
  public abstract String getServiceProviderName();
  
  public abstract String getVersion();
  
  public String directoryString()
  {
    if (directoryCache != null) {
      return directoryCache;
    }
    
    StringBuilder buf = StringBuilderHelper.get();
    buf.append(getGroup()).append('-').append(getServiceProviderName()).append('-').append(getVersion());
    




    directoryCache = buf.toString();
    
    return directoryCache;
  }
  
  public void clear() {
    directoryCache = null;
  }
}
