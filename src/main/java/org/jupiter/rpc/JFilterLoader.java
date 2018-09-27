package org.jupiter.rpc;

import java.util.List;
import org.jupiter.common.util.JServiceLoader;
import org.jupiter.common.util.StackTraceUtil;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;









public class JFilterLoader
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(JFilterLoader.class);
  
  public JFilterLoader() {}
  
  public static JFilterChain loadExtFilters(JFilterChain chain, JFilter.Type type) { try { List<JFilter> sortedList = JServiceLoader.load(JFilter.class).sort();
      

      for (int i = sortedList.size() - 1; i >= 0; i--) {
        JFilter extFilter = (JFilter)sortedList.get(i);
        JFilter.Type extType = extFilter.getType();
        if ((extType == type) || (extType == JFilter.Type.ALL)) {
          chain = new DefaultFilterChain(extFilter, chain);
        }
      }
    } catch (Throwable t) {
      logger.error("Failed to load extension filters: {}.", StackTraceUtil.stackTrace(t));
    }
    
    return chain;
  }
}
