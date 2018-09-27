package com.kingshow.utils;

import java.util.concurrent.ConcurrentHashMap;

public class UserCache
{
  private RSAEncryptor rsaEncryptor;
  private RSA rsa;
  private ConcurrentHashMap<String, String> uuidAndUidMap;
  private ConcurrentHashMap<io.netty.channel.ChannelId, String> userIdMap;
  private ConcurrentHashMap<String, io.netty.channel.Channel> channelIdMap;
  
  public UserCache()
  {
    userIdMap = new ConcurrentHashMap();
    channelIdMap = new ConcurrentHashMap();
    aesKeyMap = new ConcurrentHashMap();
    remoteMap = new ConcurrentHashMap();
    contextIdMap = new ConcurrentHashMap();
    fullReqIdMap = new ConcurrentHashMap();
    clientIpMap = new ConcurrentHashMap();
    teamGirlsMap = new ConcurrentHashMap();
    uuidAndUidMap = new ConcurrentHashMap();
    try {
      rsaEncryptor = new RSAEncryptor("MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAIhIs/3wz/nod7Ff/0UMzyK4gRCjPLqSfYkxxtlLn8GEn5Tg9kgKEl+CBiVad3w1afgFivaTHHI7xCC9zyulFkKQ3Q5IuouBkaY2+hKUPDzRRer3RmxUcNM4e5IUfDwG//8Hh69Q0kEHyD22lXGvo/kQnoUyhH+RjZ1UVAJAzj7lAgMBAAECgYAVh26vsggY0Yl/Asw/qztZn837w93HF3cvYiaokxLErl/LVBJz5OtsHQ09f2IaxBFedfmy5CB9R0W/aly851JxrI8WAkx2W2FNllzhha01fmlNlOSumoiRF++JcbsAjDcrcIiR8eSVNuB6ymBCrx/FqhdX3+t/VUbSAFXYT9tsgQJBALsHurnovZS1qjCTl6pkNS0V5qio88SzYP7lzgq0eYGlvfupdlLX8/MrSdi4DherMTcutUcaTzgQU20uAI0EMyECQQC6il1Kdkw8Peeb0JZMHbs+cMCsbGATiAt4pfo1b/i9/BO0QnRgDqYcjt3J9Ux22dPYbDpDtMjMRNrAKFb4BJdFAkBMrdWTZOVc88IL2mcC98SJcII5wdL3YSeyOZto7icmzUH/zLFzM5CTsLq8/HDiqVArNJ4jwZia/q6Fg6e8KO2hAkB0EK1VLF/ox7e5GkK533Hmuu8XGWN6I5bHnbYd06qYQyTbbtHMBrFSaY4UH91Qwd3u9gAWqoCZoGnfT/o03V5lAkBqq8jZd2lHifey+9cf1hsHD5WQbjJKPPIb57CK08hn7vUlX5ePJ02Q8AhdZKETaW+EsqJWpNgsu5wPqsy2UynO", "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCISLP98M/56HexX/9FDM8iuIEQozy6kn2JMcbZS5/BhJ+U4PZIChJfggYlWnd8NWn4BYr2kxxyO8Qgvc8rpRZCkN0OSLqLgZGmNvoSlDw80UXq90ZsVHDTOHuSFHw8Bv//B4evUNJBB8g9tpVxr6P5EJ6FMoR/kY2dVFQCQM4+5QIDAQAB");
      rsa = new RSA("MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBALquijOi1ZglnNDz2UVteoPBtwK0eIWxXP9V2xOOmv6vpFuC0qAG4NE/z9TNv88SlaYrW8lyfvcxVn9F2fCqIGZVC15KXYoXaKRjQA1reLJ7hDIYTsgeReQXdyKqBVB2QXLlOldBepW2PzlNfQS24tlT9onuWZ0DGB16UFfiba93AgMBAAECgYAEZ12nPi4furNqUnZLpvt+5DD3BV7aeIajF1llqrWJUIdmCT4CvGbNSN4UPQ5LSjrxgpBVvje0iRtrQTCU9s5w0TVKmXVo1pgaKPZ0X9o1dEKm0QHHfSxpPRARPZy5f+Lp2Tm8ZA5IJvJHH1Sq52oD1TbMjKv0OzrH4w7YAw/5kQJBAOs18CH4jq9UXMYk5+8YOH9oNM3Kf2l2RtGG2BQmZi6aurcUblgdKH895QY+0Lal+kL6ZaE69dOm34Zo3c0yZoUCQQDLLoyAfHJMb49NhZBnv07apJZZ+8HCZ+M9HJ5tJEc9UP0Kx+tDqjmt052IhQlD+hkmaCUNHoDnaMcWOcNJ5BTLAkEAr3U1BKJeDfxomIa/XWQ8AQ7DFSCRmyJYAcUNOzrJzsotrpTXG3Pk2cZaKiaBmz6FSaFwXq2U46X/6Ewh6QhH4QJAfbAM8EqLdico0b7G5rbcc7qO25k2cilL0/kJM+DWROL9tZaYPaJmzdXzRIlvguKKfky8ZtIw0XXqeiA/Y005YwJBAOPjPmiiBsmvIjN/3LNorW99TMkHHG/CbjNZeXVmvA7tCkUn07erI8fF9O2lVunUuI8xR3yqmDvjiSRvmIJPpiM=", "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC6roozotWYJZzQ89lFbXqDwbcCtHiFsVz/VdsTjpr+r6RbgtKgBuDRP8/Uzb/PEpWmK1vJcn73MVZ/RdnwqiBmVQteSl2KF2ikY0ANa3iye4QyGE7IHkXkF3ciqgVQdkFy5TpXQXqVtj85TX0EtuLZU/aJ7lmdAxgdelBX4m2vdwIDAQAB");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  private static class SingletonHolder
  {
    static UserCache instance = new UserCache();
    
    private SingletonHolder() {} }
  
  public static UserCache getInstance() { return SingletonHolder.instance; }
  







  private ConcurrentHashMap<String, io.netty.channel.ChannelHandlerContext> contextIdMap;
  






  private ConcurrentHashMap<String, io.netty.handler.codec.http.FullHttpRequest> fullReqIdMap;
  






  private ConcurrentHashMap<io.netty.channel.ChannelId, KeyInfo> aesKeyMap;
  






  private ConcurrentHashMap<String, Integer> remoteMap;
  






  private ConcurrentHashMap<String, String> clientIpMap;
  





  private ConcurrentHashMap<String, com.alibaba.fastjson.JSONArray> teamGirlsMap;
  





  public ConcurrentHashMap<String, com.alibaba.fastjson.JSONArray> getTeamGirlsMap()
  {
    return teamGirlsMap;
  }
  
  public void setTeamGirlsMap(ConcurrentHashMap<String, com.alibaba.fastjson.JSONArray> teamGirlsMap) { this.teamGirlsMap = teamGirlsMap; }
  
  public ConcurrentHashMap<io.netty.channel.ChannelId, String> getUserIdMap() {
    return userIdMap;
  }
  
  public ConcurrentHashMap<String, io.netty.channel.Channel> getChannelIdMap() { return channelIdMap; }
  
  public ConcurrentHashMap<io.netty.channel.ChannelId, KeyInfo> getAesKeyMap() {
    return aesKeyMap;
  }
  
  public ConcurrentHashMap<String, Integer> getRemoteMap() { return remoteMap; }
  
  public ConcurrentHashMap<String, io.netty.channel.ChannelHandlerContext> getContextIdMap() {
    return contextIdMap;
  }
  
  public ConcurrentHashMap<String, io.netty.handler.codec.http.FullHttpRequest> getFullReqIdMap() {
    return fullReqIdMap;
  }
  
  public ConcurrentHashMap<String, String> getClientIpMap() {
    return clientIpMap;
  }
  
  public ConcurrentHashMap<String, String> getUuidAndUidMap() { return uuidAndUidMap; }
  
  public void setUuidAndUidMap(ConcurrentHashMap<String, String> uuidAndUidMap) {
    this.uuidAndUidMap = uuidAndUidMap;
  }
  


  public RSAEncryptor getRSAEncryptor()
  {
    return rsaEncryptor;
  }
  


  public RSA getRSA()
  {
    return rsa;
  }
}
