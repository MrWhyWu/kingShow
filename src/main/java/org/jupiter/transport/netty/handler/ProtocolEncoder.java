package org.jupiter.transport.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.jupiter.common.util.Reflects;
import org.jupiter.transport.JProtocolHeader;
import org.jupiter.transport.payload.JRequestPayload;
import org.jupiter.transport.payload.JResponsePayload;
import org.jupiter.transport.payload.PayloadHolder;





































@ChannelHandler.Sharable
public class ProtocolEncoder
  extends MessageToByteEncoder<PayloadHolder>
{
  public ProtocolEncoder() {}
  
  protected void encode(ChannelHandlerContext ctx, PayloadHolder msg, ByteBuf out)
    throws Exception
  {
    if ((msg instanceof JRequestPayload)) {
      doEncodeRequest((JRequestPayload)msg, out);
    } else if ((msg instanceof JResponsePayload)) {
      doEncodeResponse((JResponsePayload)msg, out);
    } else {
      throw new IllegalArgumentException(Reflects.simpleClassName(msg));
    }
  }
  
  protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, PayloadHolder msg, boolean preferDirect) throws Exception
  {
    if (preferDirect) {
      return ctx.alloc().ioBuffer(16 + msg.size());
    }
    return ctx.alloc().heapBuffer(16 + msg.size());
  }
  
  private void doEncodeRequest(JRequestPayload request, ByteBuf out)
  {
    byte sign = JProtocolHeader.toSign(request.serializerCode(), (byte)1);
    long invokeId = request.invokeId();
    byte[] bytes = request.bytes();
    int length = bytes.length;
    
    out.writeShort(47806).writeByte(sign).writeByte(0).writeLong(invokeId).writeInt(length).writeBytes(bytes);
  }
  




  private void doEncodeResponse(JResponsePayload response, ByteBuf out)
  {
    byte sign = JProtocolHeader.toSign(response.serializerCode(), (byte)2);
    byte status = response.status();
    long invokeId = response.id();
    byte[] bytes = response.bytes();
    int length = bytes.length;
    
    out.writeShort(47806).writeByte(sign).writeByte(status).writeLong(invokeId).writeInt(length).writeBytes(bytes);
  }
}
