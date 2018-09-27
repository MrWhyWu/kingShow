package org.jupiter.rpc;

import org.jupiter.rpc.model.metadata.ResultWrapper;
import org.jupiter.transport.Status;
import org.jupiter.transport.payload.JResponsePayload;


























public class JResponse
{
  private final JResponsePayload payload;
  private ResultWrapper result;
  
  public JResponse(long id)
  {
    payload = new JResponsePayload(id);
  }
  
  public JResponse(JResponsePayload payload) {
    this.payload = payload;
  }
  
  public JResponsePayload payload() {
    return payload;
  }
  
  public long id() {
    return payload.id();
  }
  
  public byte status() {
    return payload.status();
  }
  
  public void status(byte status) {
    payload.status(status);
  }
  
  public void status(Status status) {
    payload.status(status.value());
  }
  
  public byte serializerCode() {
    return payload.serializerCode();
  }
  
  public ResultWrapper result() {
    return result;
  }
  
  public void result(ResultWrapper result) {
    this.result = result;
  }
  
  public String toString()
  {
    return "JResponse{status=" + Status.parse(status()) + ", id=" + id() + ", result=" + result + '}';
  }
}
