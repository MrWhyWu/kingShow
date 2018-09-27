package com.kingshow.utils;

import java.io.Serializable;

public class KeyInfo implements Serializable { private static final long serialVersionUID = -2518960488230243259L;
  
  public KeyInfo() {}
  
  private String IV = null;
  private String KEY = null;
  private String uuid = null;
  
  public String getIV() {
    return IV;
  }
  
  public void setIV(String iV) { IV = iV; }
  
  public String getKEY() {
    return KEY;
  }
  
  public void setKEY(String kEY) { KEY = kEY; }
  
  public String getUuid() {
    return uuid;
  }
  
  public void setUuid(String uuid) { this.uuid = uuid; }
}
