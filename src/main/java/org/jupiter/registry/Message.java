package org.jupiter.registry;

import java.util.concurrent.atomic.AtomicLong;

























public class Message
{
  private static final AtomicLong sequenceGenerator = new AtomicLong(0L);
  private final long sequence;
  private final byte serializerCode;
  private byte messageCode;
  private long version;
  private Object data;
  
  public Message(byte serializerCode)
  {
    this(sequenceGenerator.getAndIncrement(), serializerCode);
  }
  
  public Message(long sequence, byte serializerCode) {
    this.sequence = sequence;
    this.serializerCode = serializerCode;
  }
  
  public long sequence() {
    return sequence;
  }
  
  public byte serializerCode() {
    return serializerCode;
  }
  
  public byte messageCode() {
    return messageCode;
  }
  
  public void messageCode(byte messageCode) {
    this.messageCode = messageCode;
  }
  
  public long version() {
    return version;
  }
  
  public void version(long version) {
    this.version = version;
  }
  
  public Object data() {
    return data;
  }
  
  public void data(Object data) {
    this.data = data;
  }
  
  public String toString()
  {
    return "Message{sequence=" + sequence + ", messageCode=" + messageCode + ", serializerCode=" + serializerCode + ", version=" + version + ", data=" + data + '}';
  }
}
