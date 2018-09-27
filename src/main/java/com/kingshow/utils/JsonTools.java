package com.kingshow.utils;

import com.alibaba.fastjson.JSON;











public class JsonTools
{
  public JsonTools() {}
  
  public static <T> T read(String jsonString, Class<T> claz)
  {
    return JSON.parseObject(jsonString, claz);
  }
  





  public static String writeToOrg(Object obj)
  {
    return JSON.toJSONString(obj);
  }
}
