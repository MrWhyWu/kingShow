package io.netty.channel.kqueue;

final class KQueueStaticallyReferencedJniMethods
{
  static native short evAdd();
  
  static native short evEnable();
  
  static native short evDisable();
  
  static native short evDelete();
  
  static native short evClear();
  
  static native short evEOF();
  
  static native short evError();
  
  static native short noteReadClosed();
  
  static native short noteConnReset();
  
  static native short noteDisconnected();
  
  static native short evfiltRead();
  
  static native short evfiltWrite();
  
  static native short evfiltUser();
  
  static native short evfiltSock();
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.kqueue.KQueueStaticallyReferencedJniMethods
 * JD-Core Version:    0.7.0.1
 */