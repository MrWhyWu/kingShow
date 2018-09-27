package org.jupiter.rpc.consumer.processor;

import org.jupiter.rpc.JResponse;
import org.jupiter.rpc.consumer.processor.task.MessageTask;
import org.jupiter.rpc.executor.CloseableExecutor;
import org.jupiter.transport.channel.JChannel;
import org.jupiter.transport.payload.JResponsePayload;
import org.jupiter.transport.processor.ConsumerProcessor;























public class DefaultConsumerProcessor
  implements ConsumerProcessor
{
  private final CloseableExecutor executor;
  
  public DefaultConsumerProcessor()
  {
    this(ConsumerExecutors.executor());
  }
  
  public DefaultConsumerProcessor(CloseableExecutor executor) {
    this.executor = executor;
  }
  
  public void handleResponse(JChannel channel, JResponsePayload responsePayload) throws Exception
  {
    MessageTask task = new MessageTask(channel, new JResponse(responsePayload));
    if (executor == null) {
      task.run();
    } else {
      executor.execute(task);
    }
  }
  
  public void shutdown()
  {
    if (executor != null) {
      executor.shutdown();
    }
  }
}
