package org.jupiter.transport.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.EncoderException;
import org.jupiter.common.util.Reflects;
import org.jupiter.serialization.io.OutputBuf;
import org.jupiter.transport.JProtocolHeader;
import org.jupiter.transport.payload.JRequestPayload;
import org.jupiter.transport.payload.JResponsePayload;
import org.jupiter.transport.payload.PayloadHolder;





































@ChannelHandler.Sharable
public class LowCopyProtocolEncoder
  extends ChannelOutboundHandlerAdapter
{
  public LowCopyProtocolEncoder() {}
  
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
    throws Exception
  {
    ByteBuf buf = null;
    try {
      if ((msg instanceof PayloadHolder)) {
        PayloadHolder cast = (PayloadHolder)msg;
        
        buf = encode(cast);
        
        ctx.write(buf, promise);
        
        buf = null;
      } else {
        ctx.write(msg, promise);
      }
    } catch (Throwable t) {
      throw new EncoderException(t);
    } finally {
      if (buf != null) {
        buf.release();
      }
    }
  }
  
  protected ByteBuf encode(PayloadHolder msg) throws Exception {
    if ((msg instanceof JRequestPayload))
      return doEncodeRequest((JRequestPayload)msg);
    if ((msg instanceof JResponsePayload)) {
      return doEncodeResponse((JResponsePayload)msg);
    }
    throw new IllegalArgumentException(Reflects.simpleClassName(msg));
  }
  
  private ByteBuf doEncodeRequest(JRequestPayload request)
  {
    byte sign = JProtocolHeader.toSign(request.serializerCode(), (byte)1);
    long invokeId = request.invokeId();
    ByteBuf byteBuf = (ByteBuf)request.outputBuf().backingObject();
    int length = byteBuf.readableBytes();
    
    byteBuf.markWriterIndex();
    
    byteBuf.writerIndex(byteBuf.writerIndex() - length);
    
    byteBuf.writeShort(47806).writeByte(sign).writeByte(0).writeLong(invokeId).writeInt(length - 16);
    




    byteBuf.resetWriterIndex();
    
    return byteBuf;
  }
  
  private ByteBuf doEncodeResponse(JResponsePayload response) {
    byte sign = JProtocolHeader.toSign(response.serializerCode(), (byte)2);
    byte status = response.status();
    long invokeId = response.id();
    ByteBuf byteBuf = (ByteBuf)response.outputBuf().backingObject();
    int length = byteBuf.readableBytes();
    
    byteBuf.markWriterIndex();
    
    byteBuf.writerIndex(byteBuf.writerIndex() - length);
    
    byteBuf.writeShort(47806).writeByte(sign).writeByte(status).writeLong(invokeId).writeInt(length - 16);
    




    byteBuf.resetWriterIndex();
    
    return byteBuf;
  }
}
