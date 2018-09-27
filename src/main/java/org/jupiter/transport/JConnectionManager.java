package org.jupiter.transport;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jupiter.common.util.Maps;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;


























public class JConnectionManager
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(JConnectionManager.class);
  
  private final ConcurrentMap<UnresolvedAddress, CopyOnWriteArrayList<JConnection>> connections = Maps.newConcurrentMap();
  
  public JConnectionManager() {}
  
  public void manage(JConnection connection)
  {
    UnresolvedAddress address = connection.getAddress();
    CopyOnWriteArrayList<JConnection> list = (CopyOnWriteArrayList)connections.get(address);
    if (list == null) {
      CopyOnWriteArrayList<JConnection> newList = new CopyOnWriteArrayList();
      list = (CopyOnWriteArrayList)connections.putIfAbsent(address, newList);
      if (list == null) {
        list = newList;
      }
    }
    list.add(connection);
  }
  


  public void cancelAutoReconnect(UnresolvedAddress address)
  {
    CopyOnWriteArrayList<JConnection> list = (CopyOnWriteArrayList)connections.remove(address);
    if (list != null) {
      for (JConnection c : list) {
        c.setReconnect(false);
      }
      logger.warn("Cancel reconnect to: {}.", address);
    }
  }
  


  public void cancelAllAutoReconnect()
  {
    for (UnresolvedAddress address : connections.keySet()) {
      cancelAutoReconnect(address);
    }
  }
}
