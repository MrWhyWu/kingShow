package org.jupiter.rpc;

import java.util.Collections;
import java.util.Map;
import org.jupiter.rpc.model.metadata.MessageWrapper;
import org.jupiter.rpc.tracing.TraceId;
import org.jupiter.rpc.tracing.TracingUtil;
import org.jupiter.serialization.io.OutputBuf;
import org.jupiter.transport.payload.JRequestPayload;


























public class JRequest
{
  private final JRequestPayload payload;
  private MessageWrapper message;
  
  public JRequest()
  {
    this(new JRequestPayload());
  }
  
  public JRequest(JRequestPayload payload) {
    this.payload = payload;
  }
  
  public JRequestPayload payload() {
    return payload;
  }
  
  public long invokeId() {
    return payload.invokeId();
  }
  
  public long timestamp() {
    return payload.timestamp();
  }
  
  public byte serializerCode() {
    return payload.serializerCode();
  }
  
  public void bytes(byte serializerCode, byte[] bytes) {
    payload.bytes(serializerCode, bytes);
  }
  
  public void outputBuf(byte serializerCode, OutputBuf outputBuf) {
    payload.outputBuf(serializerCode, outputBuf);
  }
  
  public MessageWrapper message() {
    return message;
  }
  
  public void message(MessageWrapper message) {
    this.message = message;
  }
  
  public String getTraceId() {
    if (message == null) {
      return null;
    }
    return TracingUtil.safeGetTraceId(message.getTraceId()).asText();
  }
  
  public Map<String, String> getAttachments() {
    Map<String, String> attachments = message != null ? message.getAttachments() : null;
    
    return attachments != null ? attachments : Collections.emptyMap();
  }
  
  public void putAttachment(String key, String value) {
    if (message != null) {
      message.putAttachment(key, value);
    }
  }
  
  public String toString()
  {
    return "JRequest{invokeId=" + invokeId() + ", timestamp=" + timestamp() + ", serializerCode=" + serializerCode() + ", message=" + message + '}';
  }
}
