package org.jupiter.common.util;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;






















public final class Lists
{
  public static <E> ArrayList<E> newArrayList()
  {
    return new ArrayList();
  }
  



  public static <E> ArrayList<E> newArrayList(E... elements)
  {
    Preconditions.checkNotNull(elements);
    
    int capacity = computeArrayListCapacity(elements.length);
    ArrayList<E> list = new ArrayList(capacity);
    Collections.addAll(list, elements);
    return list;
  }
  



  public static <E> ArrayList<E> newArrayList(Iterable<? extends E> elements)
  {
    Preconditions.checkNotNull(elements);
    return (elements instanceof Collection) ? new ArrayList((Collection)elements) : newArrayList(elements.iterator());
  }
  




  public static <E> ArrayList<E> newArrayList(Iterator<? extends E> elements)
  {
    ArrayList<E> list = newArrayList();
    while (elements.hasNext()) {
      list.add(elements.next());
    }
    return list;
  }
  



  public static <E> ArrayList<E> newArrayListWithCapacity(int initialArraySize)
  {
    Preconditions.checkArgument(initialArraySize >= 0);
    return new ArrayList(initialArraySize);
  }
  




  public static <F, T> List<T> transform(List<F> fromList, Function<? super F, ? extends T> function)
  {
    return (fromList instanceof RandomAccess) ? new TransformingRandomAccessList(fromList, function) : new TransformingSequentialList(fromList, function);
  }
  
  private static class TransformingRandomAccessList<F, T>
    extends AbstractList<T> implements RandomAccess, Serializable
  {
    private static final long serialVersionUID = 0L;
    final List<F> fromList;
    final Function<? super F, ? extends T> function;
    
    TransformingRandomAccessList(List<F> fromList, Function<? super F, ? extends T> function)
    {
      this.fromList = ((List)Preconditions.checkNotNull(fromList));
      this.function = ((Function)Preconditions.checkNotNull(function));
    }
    
    public void clear()
    {
      fromList.clear();
    }
    
    public T get(int index)
    {
      return function.apply(fromList.get(index));
    }
    
    public boolean isEmpty()
    {
      return fromList.isEmpty();
    }
    
    public T remove(int index)
    {
      return function.apply(fromList.remove(index));
    }
    
    public int size()
    {
      return fromList.size();
    }
  }
  
  private static class TransformingSequentialList<F, T> extends AbstractSequentialList<T> implements Serializable
  {
    private static final long serialVersionUID = 0L;
    final List<F> fromList;
    final Function<? super F, ? extends T> function;
    
    TransformingSequentialList(List<F> fromList, Function<? super F, ? extends T> function)
    {
      this.fromList = ((List)Preconditions.checkNotNull(fromList));
      this.function = ((Function)Preconditions.checkNotNull(function));
    }
    
    public void clear()
    {
      fromList.clear();
    }
    
    public int size()
    {
      return fromList.size();
    }
    
    public ListIterator<T> listIterator(int index)
    {
      new Lists.TransformedListIterator(fromList.listIterator(index))
      {
        T transform(F from)
        {
          return function.apply(from);
        }
      };
    }
  }
  
  static abstract class TransformedIterator<F, T> implements Iterator<T> {
    final Iterator<? extends F> backingIterator;
    
    TransformedIterator(Iterator<? extends F> backingIterator) {
      this.backingIterator = ((Iterator)Preconditions.checkNotNull(backingIterator));
    }
    
    abstract T transform(F paramF);
    
    public final boolean hasNext()
    {
      return backingIterator.hasNext();
    }
    
    public final T next()
    {
      return transform(backingIterator.next());
    }
    
    public final void remove()
    {
      backingIterator.remove();
    }
  }
  
  static abstract class TransformedListIterator<F, T> extends Lists.TransformedIterator<F, T> implements ListIterator<T>
  {
    TransformedListIterator(ListIterator<? extends F> backingIterator) {
      super();
    }
    
    private ListIterator<? extends F> backingIterator()
    {
      return (ListIterator)backingIterator;
    }
    
    public final boolean hasPrevious()
    {
      return backingIterator().hasPrevious();
    }
    
    public final T previous()
    {
      return transform(backingIterator().previous());
    }
    
    public final int nextIndex()
    {
      return backingIterator().nextIndex();
    }
    
    public final int previousIndex()
    {
      return backingIterator().previousIndex();
    }
    
    public void set(T element)
    {
      throw new UnsupportedOperationException();
    }
    
    public void add(T element)
    {
      throw new UnsupportedOperationException();
    }
  }
  
  static int computeArrayListCapacity(int arraySize) {
    Preconditions.checkArgument(arraySize >= 0);
    return Ints.saturatedCast(5L + arraySize + arraySize / 10);
  }
  
  private Lists() {}
}
