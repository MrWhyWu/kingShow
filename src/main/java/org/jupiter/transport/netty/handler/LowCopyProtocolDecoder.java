package org.jupiter.transport.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import org.jupiter.common.util.Signal;
import org.jupiter.common.util.SystemClock;
import org.jupiter.common.util.SystemPropertyUtil;
import org.jupiter.serialization.io.InputBuf;
import org.jupiter.transport.JProtocolHeader;
import org.jupiter.transport.exception.IoSignals;
import org.jupiter.transport.payload.JRequestPayload;
import org.jupiter.transport.payload.JResponsePayload;











































public class LowCopyProtocolDecoder
  extends ReplayingDecoder<State>
{
  private static final int MAX_BODY_SIZE = SystemPropertyUtil.getInt("jupiter.io.decoder.max.body.size", 5242880);
  





  private static final boolean USE_COMPOSITE_BUF = SystemPropertyUtil.getBoolean("jupiter.io.decoder.composite.buf", false);
  
  public LowCopyProtocolDecoder() {
    super(State.MAGIC);
    if (USE_COMPOSITE_BUF) {
      setCumulator(COMPOSITE_CUMULATOR);
    }
  }
  

  private final JProtocolHeader header = new JProtocolHeader();
  
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
  {
    switch (1.$SwitchMap$org$jupiter$transport$netty$handler$LowCopyProtocolDecoder$State[((State)state()).ordinal()]) {
    case 1: 
      checkMagic(in.readShort());
      checkpoint(State.SIGN);
    case 2: 
      header.sign(in.readByte());
      checkpoint(State.STATUS);
    case 3: 
      header.status(in.readByte());
      checkpoint(State.ID);
    case 4: 
      header.id(in.readLong());
      checkpoint(State.BODY_SIZE);
    case 5: 
      header.bodySize(in.readInt());
      checkpoint(State.BODY);
    case 6: 
      switch (header.messageCode()) {
      case 15: 
        break;
      case 1: 
        int length = checkBodySize(header.bodySize());
        ByteBuf bodyByteBuf = in.readRetainedSlice(length);
        
        JRequestPayload request = new JRequestPayload(header.id());
        request.timestamp(SystemClock.millisClock().now());
        request.inputBuf(header.serializerCode(), new NettyInputBuf(bodyByteBuf));
        
        out.add(request);
        
        break;
      
      case 2: 
        int length = checkBodySize(header.bodySize());
        ByteBuf bodyByteBuf = in.readRetainedSlice(length);
        
        JResponsePayload response = new JResponsePayload(header.id());
        response.status(header.status());
        response.inputBuf(header.serializerCode(), new NettyInputBuf(bodyByteBuf));
        
        out.add(response);
        
        break;
      
      default: 
        throw IoSignals.ILLEGAL_SIGN;
      }
      checkpoint(State.MAGIC);
    }
  }
  
  private static void checkMagic(short magic) throws Signal {
    if (magic != 47806) {
      throw IoSignals.ILLEGAL_MAGIC;
    }
  }
  
  private static int checkBodySize(int size) throws Signal {
    if (size > MAX_BODY_SIZE) {
      throw IoSignals.BODY_TOO_LARGE;
    }
    return size;
  }
  
  static final class NettyInputBuf implements InputBuf
  {
    private final ByteBuf byteBuf;
    
    NettyInputBuf(ByteBuf byteBuf) {
      this.byteBuf = byteBuf;
    }
    
    public InputStream inputStream()
    {
      return new ByteBufInputStream(byteBuf);
    }
    
    public ByteBuffer nioByteBuffer()
    {
      return byteBuf.nioBuffer();
    }
    
    public int size()
    {
      return byteBuf.readableBytes();
    }
    
    public boolean hasMemoryAddress()
    {
      return byteBuf.hasMemoryAddress();
    }
    
    public boolean release()
    {
      return byteBuf.release();
    }
  }
  
  static enum State {
    MAGIC, 
    SIGN, 
    STATUS, 
    ID, 
    BODY_SIZE, 
    BODY;
    
    private State() {}
  }
}
