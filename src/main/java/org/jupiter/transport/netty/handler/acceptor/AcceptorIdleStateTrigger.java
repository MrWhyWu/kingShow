package org.jupiter.transport.netty.handler.acceptor;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.jupiter.transport.exception.IoSignals;



















@ChannelHandler.Sharable
public class AcceptorIdleStateTrigger
  extends ChannelInboundHandlerAdapter
{
  public AcceptorIdleStateTrigger() {}
  
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
    throws Exception
  {
    if ((evt instanceof IdleStateEvent)) {
      IdleState state = ((IdleStateEvent)evt).state();
      if (state == IdleState.READER_IDLE) {
        throw IoSignals.READER_IDLE;
      }
    } else {
      super.userEventTriggered(ctx, evt);
    }
  }
}
