package org.jupiter.rpc.model.metadata;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import org.jupiter.common.util.JConstants;
import org.jupiter.common.util.Pair;
import org.jupiter.common.util.Preconditions;
import org.jupiter.rpc.JRequest;
import org.jupiter.rpc.flow.control.FlowController;
import org.jupiter.rpc.provider.ProviderInterceptor;





































public class ServiceWrapper
  implements Serializable
{
  private static final long serialVersionUID = 6690575889849847348L;
  private final ServiceMetadata metadata;
  private final Object serviceProvider;
  private final ProviderInterceptor[] interceptors;
  private final Map<String, List<Pair<Class<?>[], Class<?>[]>>> extensions;
  private int weight = JConstants.DEFAULT_WEIGHT;
  


  private Executor executor;
  

  private FlowController<JRequest> flowController;
  


  public ServiceWrapper(String group, String providerName, String version, Object serviceProvider, ProviderInterceptor[] interceptors, Map<String, List<Pair<Class<?>[], Class<?>[]>>> extensions)
  {
    metadata = new ServiceMetadata(group, providerName, version);
    
    this.interceptors = interceptors;
    this.extensions = ((Map)Preconditions.checkNotNull(extensions, "extensions"));
    this.serviceProvider = Preconditions.checkNotNull(serviceProvider, "serviceProvider");
  }
  
  public ServiceMetadata getMetadata() {
    return metadata;
  }
  
  public Object getServiceProvider() {
    return serviceProvider;
  }
  
  public ProviderInterceptor[] getInterceptors() {
    return interceptors;
  }
  
  public int getWeight() {
    return weight;
  }
  
  public void setWeight(int weight) {
    this.weight = weight;
  }
  
  public Executor getExecutor() {
    return executor;
  }
  
  public void setExecutor(Executor executor) {
    this.executor = executor;
  }
  
  public FlowController<JRequest> getFlowController() {
    return flowController;
  }
  
  public void setFlowController(FlowController<JRequest> flowController) {
    this.flowController = flowController;
  }
  
  public List<Pair<Class<?>[], Class<?>[]>> getMethodExtension(String methodName) {
    return (List)extensions.get(methodName);
  }
  
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if ((o == null) || (getClass() != o.getClass())) { return false;
    }
    ServiceWrapper wrapper = (ServiceWrapper)o;
    
    return metadata.equals(metadata);
  }
  
  public int hashCode()
  {
    return metadata.hashCode();
  }
  
  public String toString()
  {
    return "ServiceWrapper{metadata=" + metadata + ", serviceProvider=" + serviceProvider + ", interceptors=" + Arrays.toString(interceptors) + ", extensions=" + extensions + ", weight=" + weight + ", executor=" + executor + ", flowController=" + flowController + '}';
  }
}
