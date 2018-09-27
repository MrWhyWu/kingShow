package org.jupiter.transport.netty.estimator;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.FileRegion;
import io.netty.channel.MessageSizeEstimator;
import io.netty.channel.MessageSizeEstimator.Handle;
import org.jupiter.transport.payload.PayloadHolder;




















public class JMessageSizeEstimator
  implements MessageSizeEstimator
{
  private static final class HandleImpl
    implements MessageSizeEstimator.Handle
  {
    private final int unknownSize;
    
    private HandleImpl(int unknownSize)
    {
      this.unknownSize = unknownSize;
    }
    
    public int size(Object msg)
    {
      if ((msg instanceof ByteBuf)) {
        return ((ByteBuf)msg).readableBytes();
      }
      
      if ((msg instanceof ByteBufHolder)) {
        return ((ByteBufHolder)msg).content().readableBytes();
      }
      
      if ((msg instanceof FileRegion)) {
        return 0;
      }
      

      if ((msg instanceof PayloadHolder)) {
        return ((PayloadHolder)msg).size();
      }
      
      return unknownSize;
    }
  }
  



  public static final MessageSizeEstimator DEFAULT = new JMessageSizeEstimator(8);
  


  private final MessageSizeEstimator.Handle handle;
  


  public JMessageSizeEstimator(int unknownSize)
  {
    if (unknownSize < 0) {
      throw new IllegalArgumentException("unknownSize: " + unknownSize + " (expected: >= 0)");
    }
    handle = new HandleImpl(unknownSize, null);
  }
  
  public MessageSizeEstimator.Handle newHandle()
  {
    return handle;
  }
}
