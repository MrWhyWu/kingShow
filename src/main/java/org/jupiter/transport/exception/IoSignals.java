package org.jupiter.transport.exception;

import org.jupiter.common.util.Signal;





























public class IoSignals
{
  public static final Signal ILLEGAL_MAGIC = Signal.valueOf(IoSignals.class, "ILLEGAL_MAGIC");
  
  public static final Signal ILLEGAL_SIGN = Signal.valueOf(IoSignals.class, "ILLEGAL_SIGN");
  
  public static final Signal READER_IDLE = Signal.valueOf(IoSignals.class, "READER_IDLE");
  
  public static final Signal BODY_TOO_LARGE = Signal.valueOf(IoSignals.class, "BODY_TOO_LARGE");
  
  public IoSignals() {}
}
