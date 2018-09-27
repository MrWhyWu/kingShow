package org.jupiter.rpc.model.metadata;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import org.jupiter.common.util.Maps;
import org.jupiter.rpc.tracing.TraceId;



























public class MessageWrapper
  implements Serializable
{
  private static final long serialVersionUID = 1009813828866652852L;
  private String appName;
  private final ServiceMetadata metadata;
  private String methodName;
  private Object[] args;
  private TraceId traceId;
  private Map<String, String> attachments;
  
  public MessageWrapper(ServiceMetadata metadata)
  {
    this.metadata = metadata;
  }
  
  public String getAppName() {
    return appName;
  }
  
  public void setAppName(String appName) {
    this.appName = appName;
  }
  
  public ServiceMetadata getMetadata() {
    return metadata;
  }
  
  public String getGroup() {
    return metadata.getGroup();
  }
  
  public String getServiceProviderName() {
    return metadata.getServiceProviderName();
  }
  
  public String getVersion() {
    return metadata.getVersion();
  }
  
  public String getMethodName() {
    return methodName;
  }
  
  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }
  
  public Object[] getArgs() {
    return args;
  }
  
  public void setArgs(Object[] args) {
    this.args = args;
  }
  
  public TraceId getTraceId() {
    return traceId;
  }
  
  public void setTraceId(TraceId traceId) {
    this.traceId = traceId;
  }
  
  public Map<String, String> getAttachments() {
    return attachments;
  }
  
  public void putAttachment(String key, String value) {
    if (attachments == null) {
      attachments = Maps.newHashMap();
    }
    attachments.put(key, value);
  }
  
  public String getOperationName() {
    return metadata.directoryString() + "." + methodName;
  }
  
  public String toString()
  {
    return "MessageWrapper{appName='" + appName + '\'' + ", metadata=" + metadata + ", methodName='" + methodName + '\'' + ", args=" + Arrays.toString(args) + ", traceId=" + traceId + ", attachments=" + attachments + '}';
  }
}
