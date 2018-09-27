package org.jupiter.common.util;

import org.jupiter.common.util.internal.InternalThreadLocalMap;


























public final class StringBuilderHelper
{
  public static StringBuilder get()
  {
    return InternalThreadLocalMap.get().stringBuilder();
  }
  
  private StringBuilderHelper() {}
}
