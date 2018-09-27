package org.jupiter.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.ServiceConfigurationError;
import java.util.Set;






















public final class JServiceLoader<S>
  implements Iterable<S>
{
  private static final String PREFIX = "META-INF/services/";
  private final Class<S> service;
  private final ClassLoader loader;
  private LinkedHashMap<String, S> providers = new LinkedHashMap();
  
  private JServiceLoader<S>.LazyIterator lookupIterator;
  
  public static <S> JServiceLoader<S> load(Class<S> service)
  {
    return load(service, Thread.currentThread().getContextClassLoader());
  }
  
  public static <S> JServiceLoader<S> load(Class<S> service, ClassLoader loader) {
    return new JServiceLoader(service, loader);
  }
  
  public List<S> sort() {
    List<S> sortList = Lists.newArrayList(iterator());
    
    if (sortList.size() <= 1) {
      return sortList;
    }
    
    Collections.sort(sortList, new Comparator()
    {
      public int compare(S o1, S o2)
      {
        SpiMetadata o1_spi = (SpiMetadata)o1.getClass().getAnnotation(SpiMetadata.class);
        SpiMetadata o2_spi = (SpiMetadata)o2.getClass().getAnnotation(SpiMetadata.class);
        
        int o1_priority = o1_spi == null ? 0 : o1_spi.priority();
        int o2_priority = o2_spi == null ? 0 : o2_spi.priority();
        

        return o2_priority - o1_priority;
      }
      
    });
    return sortList;
  }
  
  public S first() {
    return sort().get(0);
  }
  
  public S find(String implName) {
    for (S s : providers.values()) {
      SpiMetadata spi = (SpiMetadata)s.getClass().getAnnotation(SpiMetadata.class);
      if ((spi != null) && (spi.name().equalsIgnoreCase(implName))) {
        return s;
      }
    }
    while (lookupIterator.hasNext()) {
      Pair<String, Class<S>> e = lookupIterator.next();
      String name = (String)e.getFirst();
      Class<S> cls = (Class)e.getSecond();
      SpiMetadata spi = (SpiMetadata)cls.getAnnotation(SpiMetadata.class);
      if ((spi != null) && (spi.name().equalsIgnoreCase(implName))) {
        try {
          S provider = service.cast(cls.newInstance());
          providers.put(name, provider);
          return provider;
        } catch (Throwable x) {
          throw fail(service, "provider " + name + " could not be instantiated", x);
        }
      }
    }
    throw fail(service, "provider " + implName + " could not be found");
  }
  
  public void reload() {
    providers.clear();
    lookupIterator = new LazyIterator(service, loader, null);
  }
  
  private JServiceLoader(Class<S> service, ClassLoader loader) {
    this.service = ((Class)Preconditions.checkNotNull(service, "service interface cannot be null"));
    this.loader = (loader == null ? ClassLoader.getSystemClassLoader() : loader);
    reload();
  }
  
  private static ServiceConfigurationError fail(Class<?> service, String msg, Throwable cause) {
    return new ServiceConfigurationError(service.getName() + ": " + msg, cause);
  }
  
  private static ServiceConfigurationError fail(Class<?> service, String msg) {
    return new ServiceConfigurationError(service.getName() + ": " + msg);
  }
  
  private static ServiceConfigurationError fail(Class<?> service, URL url, int line, String msg) {
    return fail(service, url + ":" + line + ": " + msg);
  }
  


  private int parseLine(Class<?> service, URL u, BufferedReader r, int lc, List<String> names)
    throws IOException, ServiceConfigurationError
  {
    String ln = r.readLine();
    if (ln == null) {
      return -1;
    }
    int ci = ln.indexOf('#');
    if (ci >= 0) {
      ln = ln.substring(0, ci);
    }
    ln = ln.trim();
    int n = ln.length();
    if (n != 0) {
      if ((ln.indexOf(' ') >= 0) || (ln.indexOf('\t') >= 0)) {
        throw fail(service, u, lc, "illegal configuration-file syntax");
      }
      int cp = ln.codePointAt(0);
      if (!Character.isJavaIdentifierStart(cp)) {
        throw fail(service, u, lc, "illegal provider-class name: " + ln);
      }
      for (int i = Character.charCount(cp); i < n; i += Character.charCount(cp)) {
        cp = ln.codePointAt(i);
        if ((!Character.isJavaIdentifierPart(cp)) && (cp != 46)) {
          throw fail(service, u, lc, "Illegal provider-class name: " + ln);
        }
      }
      if ((!providers.containsKey(ln)) && (!names.contains(ln))) {
        names.add(ln);
      }
    }
    return lc + 1;
  }
  
  private Iterator<String> parse(Class<?> service, URL url)
  {
    InputStream in = null;
    BufferedReader r = null;
    names = Lists.newArrayList();
    try {
      in = url.openStream();
      r = new BufferedReader(new InputStreamReader(in, "utf-8"));
      int lc = 1;
      while ((lc = parseLine(service, url, r, lc, names)) >= 0) {}
      













      return names.iterator();
    }
    catch (IOException x)
    {
      throw fail(service, "error reading configuration file", x);
    } finally {
      try {
        if (r != null) {
          r.close();
        }
        if (in != null) {
          in.close();
        }
      } catch (IOException y) {
        throw fail(service, "error closing configuration file", y);
      }
    }
  }
  

  public Iterator<S> iterator()
  {
    new Iterator()
    {
      Iterator<Map.Entry<String, S>> knownProviders = providers.entrySet().iterator();
      
      public boolean hasNext()
      {
        return (knownProviders.hasNext()) || (lookupIterator.hasNext());
      }
      
      public S next()
      {
        if (knownProviders.hasNext()) {
          return ((Map.Entry)knownProviders.next()).getValue();
        }
        Pair<String, Class<S>> pair = lookupIterator.next();
        String name = (String)pair.getFirst();
        Class<S> cls = (Class)pair.getSecond();
        try {
          S provider = service.cast(cls.newInstance());
          providers.put(name, provider);
          return provider;
        } catch (Throwable x) {
          throw JServiceLoader.fail(service, "provider " + name + " could not be instantiated", x);
        }
      }
      
      public void remove()
      {
        throw new UnsupportedOperationException();
      }
    };
  }
  
  private class LazyIterator implements Iterator<Pair<String, Class<S>>> {
    Class<S> service;
    ClassLoader loader;
    Enumeration<URL> configs = null;
    Iterator<String> pending = null;
    String nextName = null;
    
    private LazyIterator(ClassLoader service) {
      this.service = service;
      this.loader = loader;
    }
    
    public boolean hasNext()
    {
      if (nextName != null) {
        return true;
      }
      if (configs == null) {
        try {
          String fullName = "META-INF/services/" + service.getName();
          if (loader == null) {
            configs = ClassLoader.getSystemResources(fullName);
          } else {
            configs = loader.getResources(fullName);
          }
        } catch (IOException x) {
          throw JServiceLoader.fail(service, "error locating configuration files", x);
        }
      }
      while ((pending == null) || (!pending.hasNext())) {
        if (!configs.hasMoreElements()) {
          return false;
        }
        pending = JServiceLoader.this.parse(service, (URL)configs.nextElement());
      }
      nextName = ((String)pending.next());
      return true;
    }
    

    public Pair<String, Class<S>> next()
    {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      String name = nextName;
      nextName = null;
      try
      {
        cls = Class.forName(name, false, loader);
      } catch (ClassNotFoundException x) { Class<?> cls;
        throw JServiceLoader.fail(service, "provider " + name + " not found"); }
      Class<?> cls;
      if (!service.isAssignableFrom(cls)) {
        throw JServiceLoader.fail(service, "provider " + name + " not a subtype");
      }
      return Pair.of(name, cls);
    }
    
    public void remove()
    {
      throw new UnsupportedOperationException();
    }
  }
  



  public String toString()
  {
    return "org.jupiter.common.util.JServiceLoader[" + service.getName() + "]";
  }
}
