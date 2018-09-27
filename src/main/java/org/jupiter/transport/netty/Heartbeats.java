package org.jupiter.transport.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

























public class Heartbeats
{
  private static final ByteBuf HEARTBEAT_BUF;
  
  static
  {
    ByteBuf buf = Unpooled.buffer(16);
    buf.writeShort(47806);
    buf.writeByte(15);
    buf.writeByte(0);
    buf.writeLong(0L);
    buf.writeInt(0);
    HEARTBEAT_BUF = Unpooled.unreleasableBuffer(buf).asReadOnly();
  }
  


  public static ByteBuf heartbeatContent()
  {
    return HEARTBEAT_BUF.duplicate();
  }
  
  public Heartbeats() {}
}
