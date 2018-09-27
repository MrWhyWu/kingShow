package io.netty.util;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;

public abstract interface AsyncMapping<IN, OUT>
{
  public abstract Future<OUT> map(IN paramIN, Promise<OUT> paramPromise);
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.AsyncMapping
 * JD-Core Version:    0.7.0.1
 */