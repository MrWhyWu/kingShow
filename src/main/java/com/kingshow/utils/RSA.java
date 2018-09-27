package com.kingshow.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.crypto.Cipher;





public class RSA
{
  private static final int MAX_ENCRYPT_BLOCK = 117;
  private static final int MAX_DECRYPT_BLOCK = 128;
  private static final String KEY_ALGORITHM = "RSA";
  private RSAPrivateKey privateKey;
  private RSAPublicKey publicKey;
  
  public RSA() {}
  
  public RSA(String privateKeyStr, String publicKeyStr)
    throws Exception
  {
    loadPublicKey(publicKeyStr);
    loadPrivateKey(privateKeyStr);
  }
  



  public Map<String, String> getKeys()
    throws Exception
  {
    KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
    keyPairGen.initialize(1024);
    KeyPair keyPair = keyPairGen.generateKeyPair();
    RSAPublicKey publicKey = (RSAPublicKey)keyPair.getPublic();
    RSAPrivateKey privateKey = (RSAPrivateKey)keyPair.getPrivate();
    String publicKeyStr = getPublicKeyStr(publicKey);
    String privateKeyStr = getPrivateKeyStr(privateKey);
    Map<String, String> map = new HashMap();
    map.put("publickey", publicKeyStr);
    map.put("privatekey", privateKeyStr);
    
    return map;
  }
  







  public RSAPublicKey getPublicKey(String modulus, String exponent)
  {
    try
    {
      BigInteger b1 = new BigInteger(modulus);
      BigInteger b2 = new BigInteger(exponent);
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      RSAPublicKeySpec keySpec = new RSAPublicKeySpec(b1, b2);
      return (RSAPublicKey)keyFactory.generatePublic(keySpec);
    } catch (Exception e) {
      e.printStackTrace(); }
    return null;
  }
  








  public RSAPrivateKey getPrivateKey(String modulus, String exponent)
  {
    try
    {
      BigInteger b1 = new BigInteger(modulus);
      BigInteger b2 = new BigInteger(exponent);
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(b1, b2);
      return (RSAPrivateKey)keyFactory.generatePrivate(keySpec);
    } catch (Exception e) {
      e.printStackTrace(); }
    return null;
  }
  








  public String encryptByPublicKey(String data)
    throws Exception
  {
    String eData = URLEncoder.encode(data, "UTF-8");
    byte[] dataByte = eData.getBytes();
    

    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    cipher.init(1, publicKey);
    int inputLen = dataByte.length;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int offSet = 0;
    
    int i = 0;
    
    while (inputLen - offSet > 0) { byte[] cache;
      byte[] cache; if (inputLen - offSet > 117) {
        cache = cipher.doFinal(dataByte, offSet, 117);
      } else {
        cache = cipher.doFinal(dataByte, offSet, inputLen - offSet);
      }
      out.write(cache, 0, cache.length);
      i++;
      offSet = i * 117;
    }
    byte[] encryptedData = out.toByteArray();
    out.close();
    return Base64Utils.encode(encryptedData);
  }
  






  public String decryptByPrivateKey(String data)
    throws Exception
  {
    byte[] encryptedData = Base64Utils.decode(data);
    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    cipher.init(2, privateKey);
    int inputLen = encryptedData.length;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int offSet = 0;
    
    int i = 0;
    
    while (inputLen - offSet > 0) { byte[] cache;
      byte[] cache; if (inputLen - offSet > 128) {
        cache = 
          cipher.doFinal(encryptedData, offSet, 128);
      } else {
        cache = 
          cipher.doFinal(encryptedData, offSet, inputLen - offSet);
      }
      out.write(cache, 0, cache.length);
      i++;
      offSet = i * 128;
    }
    byte[] decryptedData = out.toByteArray();
    out.close();
    
    String dData = URLDecoder.decode(new String(decryptedData), "UTF-8");
    return dData;
  }
  





  public String decryptByPublicKey(String data, String publickey)
    throws Exception
  {
    byte[] encryptedData = Base64Utils.decode(data);
    byte[] keyBytes = Base64Utils.decode(publickey);
    X509EncodedKeySpec pkcs8KeySpec = new X509EncodedKeySpec(keyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    Key publicK = keyFactory.generatePublic(pkcs8KeySpec);
    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    cipher.init(2, publicK);
    int inputLen = encryptedData.length;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int offSet = 0;
    
    int i = 0;
    
    while (inputLen - offSet > 0) { byte[] cache;
      byte[] cache; if (inputLen - offSet > 128) {
        cache = 
          cipher.doFinal(encryptedData, offSet, 128);
      } else {
        cache = 
          cipher.doFinal(encryptedData, offSet, inputLen - offSet);
      }
      out.write(cache, 0, cache.length);
      i++;
      offSet = i * 128;
    }
    byte[] decryptedData = out.toByteArray();
    out.close();
    
    String dData = URLDecoder.decode(new String(decryptedData), "UTF-8");
    return dData;
  }
  






  public String encryptByPrivateKey(String data, String privateKey)
    throws Exception
  {
    String eData = URLEncoder.encode(data, "UTF-8");
    byte[] dataByte = eData.getBytes();
    byte[] keyBytes = Base64Utils.decode(privateKey);
    PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
    
    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    cipher.init(1, privateK);
    int inputLen = dataByte.length;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int offSet = 0;
    
    int i = 0;
    
    while (inputLen - offSet > 0) { byte[] cache;
      byte[] cache; if (inputLen - offSet > 117) {
        cache = cipher.doFinal(dataByte, offSet, 117);
      } else {
        cache = cipher.doFinal(dataByte, offSet, inputLen - offSet);
      }
      out.write(cache, 0, cache.length);
      i++;
      offSet = i * 117;
    }
    byte[] encryptedData = out.toByteArray();
    out.close();
    return Base64Utils.encode(encryptedData);
  }
  





  public Map<String, String> getModulusAndKeys()
  {
    Map<String, String> map = new HashMap();
    try
    {
      InputStream in = RSA.class
        .getResourceAsStream("/rsa.properties");
      Properties prop = new Properties();
      prop.load(in);
      
      String modulus = prop.getProperty("modulus");
      String publicKey = prop.getProperty("publicKey");
      String privateKey = prop.getProperty("privateKey");
      map.put("modulus", modulus);
      map.put("publicKey", publicKey);
      map.put("privateKey", privateKey);
      in.close();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    
    return map;
  }
  



  public void loadPublicKey(String publicKeyStr)
    throws Exception
  {
    try
    {
      byte[] buffer = Base64Utils.decode(publicKeyStr);
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
      publicKey = ((RSAPublicKey)keyFactory.generatePublic(keySpec));
    } catch (NoSuchAlgorithmException e) {
      throw new Exception("无此算法");
    } catch (InvalidKeySpecException e) {
      throw new Exception("公钥非法");
    } catch (NullPointerException e) {
      throw new Exception("公钥数据为空");
    }
  }
  






  public void loadPrivateKey(String privateKeyStr)
    throws Exception
  {
    try
    {
      byte[] buffer = Base64Utils.decode(privateKeyStr);
      
      PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      privateKey = ((RSAPrivateKey)keyFactory.generatePrivate(keySpec));
    } catch (NoSuchAlgorithmException e) {
      throw new Exception("无此算法");
    } catch (InvalidKeySpecException e) {
      throw new Exception("私钥非法");
    } catch (NullPointerException e) {
      throw new Exception("私钥数据为空");
    }
  }
  
  public String getPrivateKeyStr(PrivateKey privateKey) throws Exception
  {
    return new String(Base64Utils.encode(privateKey.getEncoded()));
  }
  
  public String getPublicKeyStr(PublicKey publicKey) throws Exception {
    return new String(Base64Utils.encode(publicKey.getEncoded()));
  }
  
  public static void main(String[] args)
    throws Exception
  {
    String mydata = "{\"advice\":\"咯小JJ\",\"advice\":\"咯小JJ\",\"advice\":\"咯小JJ\",\"advice\":\"咯小JJ\",\"advice\":\"咯小JJ\",\"advice\":\"咯小JJ\",\"type\":\"business\",\"advice\":\"咯小JJ\",\"advice\":\"咯小JJ\",\"advice\":\"咯小JJ\",\"advice\":\"咯小JJ\",\"advice\":\"咯小JJ\",\"advice\":\"咯小JJ\"}";
    RSA rsa = new RSA("MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBALquijOi1ZglnNDz2UVteoPBtwK0eIWxXP9V2xOOmv6vpFuC0qAG4NE/z9TNv88SlaYrW8lyfvcxVn9F2fCqIGZVC15KXYoXaKRjQA1reLJ7hDIYTsgeReQXdyKqBVB2QXLlOldBepW2PzlNfQS24tlT9onuWZ0DGB16UFfiba93AgMBAAECgYAEZ12nPi4furNqUnZLpvt+5DD3BV7aeIajF1llqrWJUIdmCT4CvGbNSN4UPQ5LSjrxgpBVvje0iRtrQTCU9s5w0TVKmXVo1pgaKPZ0X9o1dEKm0QHHfSxpPRARPZy5f+Lp2Tm8ZA5IJvJHH1Sq52oD1TbMjKv0OzrH4w7YAw/5kQJBAOs18CH4jq9UXMYk5+8YOH9oNM3Kf2l2RtGG2BQmZi6aurcUblgdKH895QY+0Lal+kL6ZaE69dOm34Zo3c0yZoUCQQDLLoyAfHJMb49NhZBnv07apJZZ+8HCZ+M9HJ5tJEc9UP0Kx+tDqjmt052IhQlD+hkmaCUNHoDnaMcWOcNJ5BTLAkEAr3U1BKJeDfxomIa/XWQ8AQ7DFSCRmyJYAcUNOzrJzsotrpTXG3Pk2cZaKiaBmz6FSaFwXq2U46X/6Ewh6QhH4QJAfbAM8EqLdico0b7G5rbcc7qO25k2cilL0/kJM+DWROL9tZaYPaJmzdXzRIlvguKKfky8ZtIw0XXqeiA/Y005YwJBAOPjPmiiBsmvIjN/3LNorW99TMkHHG/CbjNZeXVmvA7tCkUn07erI8fF9O2lVunUuI8xR3yqmDvjiSRvmIJPpiM=", "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC6roozotWYJZzQ89lFbXqDwbcCtHiFsVz/VdsTjpr+r6RbgtKgBuDRP8/Uzb/PEpWmK1vJcn73MVZ/RdnwqiBmVQteSl2KF2ikY0ANa3iye4QyGE7IHkXkF3ciqgVQdkFy5TpXQXqVtj85TX0EtuLZU/aJ7lmdAxgdelBX4m2vdwIDAQAB");
    String encryData = rsa.encryptByPublicKey(mydata);
    System.out.println("加密后的数据：" + encryData);
    String decryData = rsa.decryptByPrivateKey(encryData);
    System.out.println("解密后的数据：" + decryData);
    

    String data = "LUjbstZOGpmi5nXaUN7HkZHL9QvITwD6uco3fB0FhmnL+COmSFZI999nX78HcWQRpHmla7cK9eo0dMpP92XQhtBBJxyEWd7u2hg4uVangorEYEJge2lhySo3PiEjqRsNRRRSTUlu5jdxIIVEqQzWcciJU0EbZ+6rOU++t4yBmPKiOtEJ49+jN2u+o62bGbuCoGlrGiQXopLFmrK2M0xEBq5slhdnWwfAJnpIAM2AKtbd6uo0GourAKvREaGQdJ7soqXLpJaTgl0gFKorIITO5gQsle7TgHiMLP6oghKx2TNQCDLPXyBRD76EX+GuDipej18VRsp8zDtLVtl06MojQrBpFl/fmuFIM21jn1qgkke81Q3ra27065l6nB30Wu81tJYtUxooLEakPcvw2LMdUv2p+Z8/JL0wxAQPgZYS4vKgX0sCtRfWfKOgIyGVj8wgQBC+FX4i/6k9G6kUfvFgkgm2D4Vv8zvPlcE0NiqPh7oSX/vtoCb06e0TB1LmxgzWbss/PzY7D6LIplTHXCxN36BHUhb8w7u6UNciCzNC7W1zQOczLdu7j1pMiOp6q3exBNIBUVP5M2C3WDKLFx/V97/vfd+3PU1+rTyeQ33+wW4p6y4XtSAmbr8nhoDh5XXjWYpVb0bURuHHhPWRk9E0dimKiPSav37Z0g6+0ilWTQUoDQrLi8sSfxlWGY2hAGXGQhnv0GysrZEOyBU7kBA0fU8MIgxpYkm/fnuQ/SzOKn657PcsPnP5tW7xJziUVLoFnnktWKqx07x/NXDPqEN5rG512rdNUQLskaXwSWQgQKnczCKhCQJ+VdZtKlEJcUnLiuI5WmMSJoB8zEUYWEyVVA==";
    System.out.println(rsa.decryptByPrivateKey(data));
  }
}
