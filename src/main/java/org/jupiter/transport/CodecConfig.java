package org.jupiter.transport;

import org.jupiter.common.util.SystemPropertyUtil;
























public final class CodecConfig
{
  private static final boolean CODEC_LOW_COPY = SystemPropertyUtil.getBoolean("jupiter.transport.codec.low_copy", true);
  
  public static boolean isCodecLowCopy()
  {
    return CODEC_LOW_COPY;
  }
  
  private CodecConfig() {}
}
