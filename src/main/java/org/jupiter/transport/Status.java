package org.jupiter.transport;

























public enum Status
{
  OK((byte)32, "OK"), 
  CLIENT_ERROR((byte)48, "CLIENT_ERROR"), 
  CLIENT_TIMEOUT((byte)49, "CLIENT_TIMEOUT"), 
  SERVER_TIMEOUT((byte)50, "SERVER_TIMEOUT"), 
  BAD_REQUEST((byte)64, "BAD_REQUEST"), 
  SERVICE_NOT_FOUND((byte)68, "SERVICE_NOT_FOUND"), 
  SERVER_ERROR((byte)80, "SERVER_ERROR"), 
  SERVER_BUSY((byte)81, "SERVER_BUSY"), 
  SERVICE_EXPECTED_ERROR((byte)82, "SERVICE_EXPECTED_ERROR"), 
  SERVICE_UNEXPECTED_ERROR((byte)83, "SERVICE_UNEXPECTED_ERROR"), 
  APP_FLOW_CONTROL((byte)84, "APP_FLOW_CONTROL"), 
  PROVIDER_FLOW_CONTROL((byte)85, "PROVIDER_FLOW_CONTROL"), 
  DESERIALIZATION_FAIL((byte)96, "DESERIALIZATION_FAIL");
  
  private Status(byte value, String description) {
    this.value = value;
    this.description = description;
  }
  
  private byte value;
  private String description;
  public static Status parse(byte value)
  {
    for (Status s : ) {
      if (value == value) {
        return s;
      }
    }
    return null;
  }
  
  public byte value() {
    return value;
  }
  
  public String description() {
    return description;
  }
  
  public String toString()
  {
    return description();
  }
}
