package org.jupiter.common.concurrent.disruptor;

import com.lmax.disruptor.ExceptionHandler;
import org.jupiter.common.util.StackTraceUtil;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;























public class LoggingExceptionHandler
  implements ExceptionHandler<Object>
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(LoggingExceptionHandler.class);
  
  public LoggingExceptionHandler() {}
  
  public void handleEventException(Throwable ex, long sequence, Object event) { if (logger.isWarnEnabled()) {
      logger.warn("Exception processing: {} {}, {}.", new Object[] { Long.valueOf(sequence), event, StackTraceUtil.stackTrace(ex) });
    }
  }
  
  public void handleOnStartException(Throwable ex)
  {
    if (logger.isWarnEnabled()) {
      logger.warn("Exception during onStart(), {}.", StackTraceUtil.stackTrace(ex));
    }
  }
  
  public void handleOnShutdownException(Throwable ex)
  {
    if (logger.isWarnEnabled()) {
      logger.warn("Exception during onShutdown(), {}.", StackTraceUtil.stackTrace(ex));
    }
  }
}
