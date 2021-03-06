package org.jupiter.common.util.internal.logging;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;



















































































































final class MessageFormatter
{
  private static final String DELIM_STR = "{}";
  private static final char ESCAPE_CHAR = '\\';
  
  static FormattingTuple format(String messagePattern, Object arg)
  {
    return arrayFormat(messagePattern, new Object[] { arg });
  }
  



















  static FormattingTuple format(String messagePattern, Object argA, Object argB)
  {
    return arrayFormat(messagePattern, new Object[] { argA, argB });
  }
  










  static FormattingTuple arrayFormat(String messagePattern, Object[] argArray)
  {
    if ((argArray == null) || (argArray.length == 0)) {
      return new FormattingTuple(messagePattern, null);
    }
    
    int lastArrIdx = argArray.length - 1;
    Object lastEntry = argArray[lastArrIdx];
    Throwable throwable = (lastEntry instanceof Throwable) ? (Throwable)lastEntry : null;
    
    if (messagePattern == null) {
      return new FormattingTuple(null, throwable);
    }
    
    int j = messagePattern.indexOf("{}");
    if (j == -1)
    {
      return new FormattingTuple(messagePattern, throwable);
    }
    
    StringBuilder buf = new StringBuilder(messagePattern.length() + 50);
    int i = 0;
    int L = 0;
    do {
      boolean notEscaped = (j == 0) || (messagePattern.charAt(j - 1) != '\\');
      if (notEscaped)
      {
        buf.append(messagePattern, i, j);
      } else {
        buf.append(messagePattern, i, j - 1);
        
        notEscaped = (j >= 2) && (messagePattern.charAt(j - 2) == '\\');
      }
      
      i = j + 2;
      if (notEscaped) {
        deeplyAppendParameter(buf, argArray[L], null);
        L++;
        if (L > lastArrIdx) {
          break;
        }
      } else {
        buf.append("{}");
      }
      j = messagePattern.indexOf("{}", i);
    } while (j != -1);
    

    buf.append(messagePattern, i, messagePattern.length());
    return new FormattingTuple(buf.toString(), L <= lastArrIdx ? throwable : null);
  }
  

  private static void deeplyAppendParameter(StringBuilder buf, Object o, Set<Object[]> seenSet)
  {
    if (o == null) {
      buf.append("null");
      return;
    }
    Class<?> objClass = o.getClass();
    if (!objClass.isArray()) {
      if (Number.class.isAssignableFrom(objClass))
      {
        if (objClass == Long.class) {
          buf.append(((Long)o).longValue());
        } else if ((objClass == Integer.class) || (objClass == Short.class) || (objClass == Byte.class)) {
          buf.append(((Number)o).intValue());
        } else if (objClass == Double.class) {
          buf.append(((Double)o).doubleValue());
        } else if (objClass == Float.class) {
          buf.append(((Float)o).floatValue());
        } else {
          safeObjectAppend(buf, o);
        }
      } else {
        safeObjectAppend(buf, o);
      }
    }
    else
    {
      buf.append('[');
      if (objClass == [Z.class) {
        booleanArrayAppend(buf, (boolean[])o);
      } else if (objClass == [B.class) {
        byteArrayAppend(buf, (byte[])o);
      } else if (objClass == [C.class) {
        charArrayAppend(buf, (char[])o);
      } else if (objClass == [S.class) {
        shortArrayAppend(buf, (short[])o);
      } else if (objClass == [I.class) {
        intArrayAppend(buf, (int[])o);
      } else if (objClass == [J.class) {
        longArrayAppend(buf, (long[])o);
      } else if (objClass == [F.class) {
        floatArrayAppend(buf, (float[])o);
      } else if (objClass == [D.class) {
        doubleArrayAppend(buf, (double[])o);
      } else {
        objectArrayAppend(buf, (Object[])o, seenSet);
      }
      buf.append(']');
    }
  }
  
  private static void safeObjectAppend(StringBuilder buf, Object o) {
    try {
      String oAsString = o.toString();
      buf.append(oAsString);
    } catch (Throwable t) {
      System.err.println("SLF4J: Failed toString() invocation on an object of type [" + o.getClass().getName() + ']');
      t.printStackTrace();
      buf.append("[FAILED toString()]");
    }
  }
  
  private static void objectArrayAppend(StringBuilder buf, Object[] a, Set<Object[]> seenSet) {
    if (a.length == 0) {
      return;
    }
    if (seenSet == null) {
      seenSet = new HashSet(a.length);
    }
    if (seenSet.add(a)) {
      deeplyAppendParameter(buf, a[0], seenSet);
      for (int i = 1; i < a.length; i++) {
        buf.append(", ");
        deeplyAppendParameter(buf, a[i], seenSet);
      }
      
      seenSet.remove(a);
    } else {
      buf.append("...");
    }
  }
  
  private static void booleanArrayAppend(StringBuilder buf, boolean[] a) {
    if (a.length == 0) {
      return;
    }
    buf.append(a[0]);
    for (int i = 1; i < a.length; i++) {
      buf.append(", ");
      buf.append(a[i]);
    }
  }
  
  private static void byteArrayAppend(StringBuilder buf, byte[] a) {
    if (a.length == 0) {
      return;
    }
    buf.append(a[0]);
    for (int i = 1; i < a.length; i++) {
      buf.append(", ");
      buf.append(a[i]);
    }
  }
  
  private static void charArrayAppend(StringBuilder buf, char[] a) {
    if (a.length == 0) {
      return;
    }
    buf.append(a[0]);
    for (int i = 1; i < a.length; i++) {
      buf.append(", ");
      buf.append(a[i]);
    }
  }
  
  private static void shortArrayAppend(StringBuilder buf, short[] a) {
    if (a.length == 0) {
      return;
    }
    buf.append(a[0]);
    for (int i = 1; i < a.length; i++) {
      buf.append(", ");
      buf.append(a[i]);
    }
  }
  
  private static void intArrayAppend(StringBuilder buf, int[] a) {
    if (a.length == 0) {
      return;
    }
    buf.append(a[0]);
    for (int i = 1; i < a.length; i++) {
      buf.append(", ");
      buf.append(a[i]);
    }
  }
  
  private static void longArrayAppend(StringBuilder buf, long[] a) {
    if (a.length == 0) {
      return;
    }
    buf.append(a[0]);
    for (int i = 1; i < a.length; i++) {
      buf.append(", ");
      buf.append(a[i]);
    }
  }
  
  private static void floatArrayAppend(StringBuilder buf, float[] a) {
    if (a.length == 0) {
      return;
    }
    buf.append(a[0]);
    for (int i = 1; i < a.length; i++) {
      buf.append(", ");
      buf.append(a[i]);
    }
  }
  
  private static void doubleArrayAppend(StringBuilder buf, double[] a) {
    if (a.length == 0) {
      return;
    }
    buf.append(a[0]);
    for (int i = 1; i < a.length; i++) {
      buf.append(", ");
      buf.append(a[i]);
    }
  }
  
  private MessageFormatter() {}
}
