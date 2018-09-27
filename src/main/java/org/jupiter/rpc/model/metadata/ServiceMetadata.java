package org.jupiter.rpc.model.metadata;

import java.io.Serializable;
import org.jupiter.common.util.Preconditions;
import org.jupiter.transport.Directory;



























public class ServiceMetadata
  extends Directory
  implements Serializable
{
  private static final long serialVersionUID = -8908295634641380163L;
  private String group;
  private String serviceProviderName;
  private String version;
  
  public ServiceMetadata() {}
  
  public ServiceMetadata(String group, String serviceProviderName, String version)
  {
    this.group = ((String)Preconditions.checkNotNull(group, "group"));
    this.serviceProviderName = ((String)Preconditions.checkNotNull(serviceProviderName, "serviceProviderName"));
    this.version = ((String)Preconditions.checkNotNull(version, "version"));
  }
  
  public String getGroup()
  {
    return group;
  }
  
  public void setGroup(String group) {
    this.group = group;
  }
  
  public String getServiceProviderName()
  {
    return serviceProviderName;
  }
  
  public void setServiceProviderName(String serviceProviderName) {
    this.serviceProviderName = serviceProviderName;
  }
  
  public String getVersion()
  {
    return version;
  }
  
  public void setVersion(String version) {
    this.version = version;
  }
  
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if ((o == null) || (getClass() != o.getClass())) { return false;
    }
    ServiceMetadata metadata = (ServiceMetadata)o;
    
    return (group.equals(group)) && (serviceProviderName.equals(serviceProviderName)) && (version.equals(version));
  }
  


  public int hashCode()
  {
    int result = group.hashCode();
    result = 31 * result + serviceProviderName.hashCode();
    result = 31 * result + version.hashCode();
    return result;
  }
  
  public String toString()
  {
    return "ServiceMetadata{group='" + group + '\'' + ", serviceProviderName='" + serviceProviderName + '\'' + ", version='" + version + '\'' + '}';
  }
}
