package com.kingshow.utils;

import java.util.concurrent.atomic.AtomicLong;



public class UserIds
{
  private AtomicLong userNo;
  
  public UserIds()
  {
    long initNO = 1000L;
    















    userNo = new AtomicLong(initNO);
  }
  
  private static class SingletonHolder {
    static UserIds instance = new UserIds();
    
    private SingletonHolder() {} }
  
  public static UserIds getInstance() { return SingletonHolder.instance; }
  


  public AtomicLong getUserNo()
  {
    return userNo;
  }
}
