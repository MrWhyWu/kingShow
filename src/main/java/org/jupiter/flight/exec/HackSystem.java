package org.jupiter.flight.exec;

import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.channels.Channel;
import java.util.Map;
import java.util.Properties;
import org.jupiter.common.util.internal.UnsafeReferenceFieldUpdater;
import org.jupiter.common.util.internal.UnsafeUpdater;



















public class HackSystem
{
  public HackSystem() {}
  
  public static final InputStream in = System.in;
  
  private static final UnsafeReferenceFieldUpdater<ByteArrayOutputStream, byte[]> bufUpdater = UnsafeUpdater.newReferenceFieldUpdater(ByteArrayOutputStream.class, "buf");
  

  private static ByteArrayOutputStream buf = new ByteArrayOutputStream(1024);
  
  public static final PrintStream out = new PrintStream(buf);
  
  public static final PrintStream err = out;
  
  public static String getBufString() {
    String value = buf.toString();
    synchronized (HackSystem.class) {
      assert (bufUpdater != null);
      if (((byte[])bufUpdater.get(buf)).length > 8192) {
        bufUpdater.set(buf, new byte['Ѐ']);
      }
    }
    return value;
  }
  
  public static void clearBuf() {
    buf.reset();
  }
  
  public static void setIn(InputStream in) {
    System.setIn(in);
  }
  
  public static void setOut(PrintStream out) {
    System.setOut(out);
  }
  
  public static void setErr(PrintStream err) {
    System.setErr(err);
  }
  
  public static Console console() {
    return System.console();
  }
  
  public static Channel inheritedChannel() throws IOException {
    return System.inheritedChannel();
  }
  
  public static void setSecurityManager(SecurityManager s) {
    System.setSecurityManager(s);
  }
  
  public static SecurityManager getSecurityManager() {
    return System.getSecurityManager();
  }
  
  public static long currentTimeMillis() {
    return System.currentTimeMillis();
  }
  
  public static long nanoTime() {
    return System.nanoTime();
  }
  
  public static void arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
  {
    System.arraycopy(src, srcPos, dest, destPos, length);
  }
  
  public static int identityHashCode(Object x) {
    return System.identityHashCode(x);
  }
  
  public static Properties getProperties() {
    return System.getProperties();
  }
  
  public static void setProperties(Properties props) {
    System.setProperties(props);
  }
  
  public static String getProperty(String key) {
    return System.getProperty(key);
  }
  
  public static String getProperty(String key, String def) {
    return System.getProperty(key, def);
  }
  
  public static String setProperty(String key, String value) {
    return System.setProperty(key, value);
  }
  
  public static String clearProperty(String key) {
    return System.clearProperty(key);
  }
  
  public static String getenv(String name) {
    return System.getenv(name);
  }
  
  public static Map<String, String> getenv() {
    return System.getenv();
  }
  
  public static void exit(int status) {
    System.exit(status);
  }
  


  public static void gc() {}
  

  public static void runFinalization() {}
  

  public static void runFinalizersOnExit(boolean value)
  {
    System.runFinalizersOnExit(value);
  }
  
  public static void load(String filename) {
    System.load(filename);
  }
  
  public static void loadLibrary(String libname) {
    System.loadLibrary(libname);
  }
  
  public static String mapLibraryName(String libname) {
    return System.mapLibraryName(libname);
  }
}
