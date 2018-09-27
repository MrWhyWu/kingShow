package org.jupiter.transport.channel;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.jupiter.common.util.Maps;
import org.jupiter.transport.Directory;




















public class DirectoryJChannelGroup
{
  private final ConcurrentMap<String, CopyOnWriteGroupList> groups;
  private final GroupRefCounterMap groupRefCounter;
  
  public DirectoryJChannelGroup()
  {
    groups = Maps.newConcurrentMap();
    
    groupRefCounter = new GroupRefCounterMap();
  }
  
  public CopyOnWriteGroupList find(Directory directory) { String _directory = directory.directoryString();
    CopyOnWriteGroupList groupList = (CopyOnWriteGroupList)groups.get(_directory);
    if (groupList == null) {
      CopyOnWriteGroupList newGroupList = new CopyOnWriteGroupList(this);
      groupList = (CopyOnWriteGroupList)groups.putIfAbsent(_directory, newGroupList);
      if (groupList == null) {
        groupList = newGroupList;
      }
    }
    return groupList;
  }
  


  public int getRefCount(JChannelGroup group)
  {
    AtomicInteger counter = (AtomicInteger)groupRefCounter.get(group);
    if (counter == null) {
      return 0;
    }
    return counter.get();
  }
  


  public int incrementRefCount(JChannelGroup group)
  {
    return groupRefCounter.getOrCreate(group).incrementAndGet();
  }
  


  public int decrementRefCount(JChannelGroup group)
  {
    AtomicInteger counter = (AtomicInteger)groupRefCounter.get(group);
    if (counter == null) {
      return 0;
    }
    int count = counter.decrementAndGet();
    if (count == 0)
    {
      groupRefCounter.remove(group);
    }
    return count;
  }
  
  static class GroupRefCounterMap extends ConcurrentHashMap<JChannelGroup, AtomicInteger> {
    private static final long serialVersionUID = 6590976614405397299L;
    
    GroupRefCounterMap() {}
    
    public AtomicInteger getOrCreate(JChannelGroup key) { AtomicInteger counter = (AtomicInteger)super.get(key);
      if (counter == null) {
        AtomicInteger newCounter = new AtomicInteger(0);
        counter = (AtomicInteger)super.putIfAbsent(key, newCounter);
        if (counter == null) {
          counter = newCounter;
        }
      }
      return counter;
    }
  }
}
