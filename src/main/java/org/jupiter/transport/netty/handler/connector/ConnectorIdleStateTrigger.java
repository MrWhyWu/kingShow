package org.jupiter.transport.netty.handler.connector;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.jupiter.transport.netty.Heartbeats;



















@ChannelHandler.Sharable
public class ConnectorIdleStateTrigger
  extends ChannelInboundHandlerAdapter
{
  public ConnectorIdleStateTrigger() {}
  
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
    throws Exception
  {
    if ((evt instanceof IdleStateEvent)) {
      IdleState state = ((IdleStateEvent)evt).state();
      if (state == IdleState.WRITER_IDLE)
      {
        ctx.writeAndFlush(Heartbeats.heartbeatContent());
      }
    } else {
      super.userEventTriggered(ctx, evt);
    }
  }
}
