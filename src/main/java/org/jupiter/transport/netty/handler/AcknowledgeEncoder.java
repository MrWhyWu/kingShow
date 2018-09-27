package org.jupiter.transport.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.jupiter.transport.Acknowledge;






















@ChannelHandler.Sharable
public class AcknowledgeEncoder
  extends MessageToByteEncoder<Acknowledge>
{
  public AcknowledgeEncoder() {}
  
  protected void encode(ChannelHandlerContext ctx, Acknowledge ack, ByteBuf out)
    throws Exception
  {
    out.writeShort(47806).writeByte(7).writeByte(0).writeLong(ack.sequence()).writeInt(0);
  }
}
