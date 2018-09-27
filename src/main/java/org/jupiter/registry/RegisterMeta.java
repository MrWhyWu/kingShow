package org.jupiter.registry;





public class RegisterMeta
{
  private Address address;
  



  private ServiceMeta serviceMeta;
  



  private volatile int weight;
  



  private volatile int connCount;
  




  public RegisterMeta()
  {
    address = new Address();
    
    serviceMeta = new ServiceMeta();
  }
  


  public String getHost()
  {
    return address.getHost();
  }
  
  public void setHost(String host) {
    address.setHost(host);
  }
  
  public int getPort() {
    return address.getPort();
  }
  
  public void setPort(int port) {
    address.setPort(port);
  }
  
  public String getGroup() {
    return serviceMeta.getGroup();
  }
  
  public void setGroup(String group) {
    serviceMeta.setGroup(group);
  }
  
  public String getServiceProviderName() {
    return serviceMeta.getServiceProviderName();
  }
  
  public void setServiceProviderName(String serviceProviderName) {
    serviceMeta.setServiceProviderName(serviceProviderName);
  }
  
  public String getVersion() {
    return serviceMeta.getVersion();
  }
  
  public void setVersion(String version) {
    serviceMeta.setVersion(version);
  }
  
  public Address getAddress() {
    return address;
  }
  
  public ServiceMeta getServiceMeta() {
    return serviceMeta;
  }
  
  public int getWeight() {
    return weight;
  }
  
  public void setWeight(int weight) {
    this.weight = weight;
  }
  
  public int getConnCount() {
    return connCount;
  }
  
  public void setConnCount(int connCount) {
    this.connCount = connCount;
  }
  
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if ((o == null) || (getClass() != o.getClass())) { return false;
    }
    RegisterMeta that = (RegisterMeta)o;
    
    if (address != null ? address.equals(address) : address == null) {} return serviceMeta != null ? serviceMeta.equals(serviceMeta) : serviceMeta == null;
  }
  

  public int hashCode()
  {
    int result = address != null ? address.hashCode() : 0;
    result = 31 * result + (serviceMeta != null ? serviceMeta.hashCode() : 0);
    return result;
  }
  
  public String toString()
  {
    return "RegisterMeta{address=" + address + ", serviceMeta=" + serviceMeta + ", weight=" + weight + ", connCount=" + connCount + '}';
  }
  


  public static class Address
  {
    private String host;
    

    private int port;
    


    public Address() {}
    


    public Address(String host, int port)
    {
      this.host = host;
      this.port = port;
    }
    
    public String getHost() {
      return host;
    }
    
    public void setHost(String host) {
      this.host = host;
    }
    
    public int getPort() {
      return port;
    }
    
    public void setPort(int port) {
      this.port = port;
    }
    
    public boolean equals(Object o)
    {
      if (this == o) return true;
      if ((o == null) || (getClass() != o.getClass())) { return false;
      }
      Address address = (Address)o;
      
      return (port == port) && (host != null ? host.equals(host) : host == null);
    }
    
    public int hashCode()
    {
      int result = host != null ? host.hashCode() : 0;
      result = 31 * result + port;
      return result;
    }
    
    public String toString()
    {
      return "Address{host='" + host + '\'' + ", port=" + port + '}';
    }
  }
  


  public static class ServiceMeta
  {
    private String group;
    

    private String serviceProviderName;
    
    private String version;
    

    public ServiceMeta() {}
    

    public ServiceMeta(String group, String serviceProviderName, String version)
    {
      this.group = group;
      this.serviceProviderName = serviceProviderName;
      this.version = version;
    }
    
    public String getGroup() {
      return group;
    }
    
    public void setGroup(String group) {
      this.group = group;
    }
    
    public String getServiceProviderName() {
      return serviceProviderName;
    }
    
    public void setServiceProviderName(String serviceProviderName) {
      this.serviceProviderName = serviceProviderName;
    }
    
    public String getVersion() {
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
      ServiceMeta that = (ServiceMeta)o;
      
      if (group != null ? group.equals(group) : group == null) if (serviceProviderName != null ? !serviceProviderName.equals(serviceProviderName) : serviceProviderName != null) {} return version != null ? version.equals(version) : version == null;
    }
    


    public int hashCode()
    {
      int result = group != null ? group.hashCode() : 0;
      result = 31 * result + (serviceProviderName != null ? serviceProviderName.hashCode() : 0);
      result = 31 * result + (version != null ? version.hashCode() : 0);
      return result;
    }
    
    public String toString()
    {
      return "ServiceMeta{group='" + group + '\'' + ", serviceProviderName='" + serviceProviderName + '\'' + ", version='" + version + '\'' + '}';
    }
  }
}
