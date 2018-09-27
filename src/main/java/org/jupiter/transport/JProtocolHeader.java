package org.jupiter.transport;





public class JProtocolHeader
{
  public static final int HEADER_SIZE = 16;
  



  public static final short MAGIC = -17730;
  


  public static final byte REQUEST = 1;
  


  public static final byte RESPONSE = 2;
  


  public static final byte PUBLISH_SERVICE = 3;
  


  public static final byte PUBLISH_CANCEL_SERVICE = 4;
  


  public static final byte SUBSCRIBE_SERVICE = 5;
  


  public static final byte OFFLINE_NOTICE = 6;
  


  public static final byte ACK = 7;
  


  public static final byte HEARTBEAT = 15;
  


  private byte messageCode;
  


  private byte serializerCode;
  


  private byte status;
  


  private long id;
  


  private int bodySize;
  



  public JProtocolHeader() {}
  



  public static byte toSign(byte serializerCode, byte messageCode)
  {
    return (byte)(serializerCode << 4 | messageCode & 0xF);
  }
  
  public void sign(byte sign)
  {
    messageCode = ((byte)(sign & 0xF));
    
    serializerCode = ((byte)((sign & 0xFF) >> 4));
  }
  
  public byte messageCode() {
    return messageCode;
  }
  
  public byte serializerCode() {
    return serializerCode;
  }
  
  public byte status() {
    return status;
  }
  
  public void status(byte status) {
    this.status = status;
  }
  
  public long id() {
    return id;
  }
  
  public void id(long id) {
    this.id = id;
  }
  
  public int bodySize() {
    return bodySize;
  }
  
  public void bodySize(int bodyLength) {
    bodySize = bodyLength;
  }
  
  public String toString()
  {
    return "JProtocolHeader{messageCode=" + messageCode + ", serializerCode=" + serializerCode + ", status=" + status + ", id=" + id + ", bodySize=" + bodySize + '}';
  }
}
