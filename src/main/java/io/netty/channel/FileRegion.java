package io.netty.channel;

import io.netty.util.ReferenceCounted;
import java.io.IOException;
import java.nio.channels.WritableByteChannel;

public abstract interface FileRegion
  extends ReferenceCounted
{
  public abstract long position();
  
  @Deprecated
  public abstract long transfered();
  
  public abstract long transferred();
  
  public abstract long count();
  
  public abstract long transferTo(WritableByteChannel paramWritableByteChannel, long paramLong)
    throws IOException;
  
  public abstract FileRegion retain();
  
  public abstract FileRegion retain(int paramInt);
  
  public abstract FileRegion touch();
  
  public abstract FileRegion touch(Object paramObject);
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.FileRegion
 * JD-Core Version:    0.7.0.1
 */