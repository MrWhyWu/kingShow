package org.jupiter.transport;

import org.jupiter.common.util.Preconditions;

























public class UnresolvedAddress
{
  private final String host;
  private final int port;
  
  public UnresolvedAddress(String host, int port)
  {
    Preconditions.checkNotNull(host, "host can't be null");
    Preconditions.checkArgument((port > 0) && (port < 65535), "port out of range:" + port);
    
    this.host = host;
    this.port = port;
  }
  
  public String getHost() {
    return host;
  }
  
  public int getPort() {
    return port;
  }
  
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if ((o == null) || (getClass() != o.getClass())) { return false;
    }
    UnresolvedAddress that = (UnresolvedAddress)o;
    
    return (port == port) && (host.equals(host));
  }
  
  public int hashCode()
  {
    int result = host.hashCode();
    result = 31 * result + port;
    return result;
  }
  
  public String toString()
  {
    return host + ':' + port;
  }
}
