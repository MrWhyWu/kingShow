package org.jupiter.rpc.tracing;

import java.io.Serializable;

























public class TraceId
  implements Serializable
{
  private static final long serialVersionUID = 2901824755629719770L;
  public static final TraceId NULL_TRACE_ID = newInstance("null");
  private final String id;
  private int node;
  
  public static TraceId newInstance(String id)
  {
    return new TraceId(id);
  }
  
  private TraceId(String id) {
    this.id = id;
    node = 0;
  }
  
  public String getId() {
    return id;
  }
  
  public int getNode() {
    return node;
  }
  
  public String asText() {
    return id + "_" + node;
  }
  
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if ((o == null) || (getClass() != o.getClass())) { return false;
    }
    TraceId traceId = (TraceId)o;
    
    return (node == node) && (id.equals(id));
  }
  
  public int hashCode()
  {
    int result = id.hashCode();
    result = 31 * result + node;
    return result;
  }
  
  public String toString()
  {
    return "TraceId{id='" + id + '\'' + ", node=" + node + '}';
  }
}
