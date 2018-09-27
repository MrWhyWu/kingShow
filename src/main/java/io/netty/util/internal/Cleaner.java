package io.netty.util.internal;

import java.nio.ByteBuffer;

abstract interface Cleaner
{
  public abstract void freeDirectBuffer(ByteBuffer paramByteBuffer);
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.Cleaner
 * JD-Core Version:    0.7.0.1
 */