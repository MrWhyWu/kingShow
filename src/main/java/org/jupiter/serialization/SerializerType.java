package org.jupiter.serialization;

























public enum SerializerType
{
  PROTO_STUFF((byte)1), 
  HESSIAN((byte)2), 
  KRYO((byte)3), 
  JAVA((byte)4);
  
  private final byte value;
  
  private SerializerType(byte value) {
    if ((0 < value) && (value < 16)) {
      this.value = value;
    } else {
      throw new IllegalArgumentException("Out of range(0x01 ~ 0x0f): " + value);
    }
  }
  

  public byte value()
  {
    return value;
  }
  
  public static SerializerType parse(String name) {
    for (SerializerType s : ) {
      if (s.name().equalsIgnoreCase(name)) {
        return s;
      }
    }
    return null;
  }
  
  public static SerializerType parse(byte value) {
    for (SerializerType s : ) {
      if (s.value() == value) {
        return s;
      }
    }
    return null;
  }
  
  public static SerializerType getDefault() {
    return PROTO_STUFF;
  }
}
