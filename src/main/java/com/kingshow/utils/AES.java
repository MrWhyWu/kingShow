package com.kingshow.utils;

import com.kingshow.netty.HandlerDispatcher;
import io.netty.util.CharsetUtil;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




public class AES
{
  private static final Logger log = LoggerFactory.getLogger(HandlerDispatcher.class);
  private static final KeyPair keyPair = initKey();
  
  public AES() {}
  
  public static KeyPair initKey() {
    try {
      SecureRandom random = new SecureRandom();
      KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");
      generator.initialize(1024, random);
      return generator.generateKeyPair();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  




  public static String decryptBase64(String string)
  {
    return new String(decrypt(Base64.decodeBase64(string)));
  }
  
  private static byte[] decrypt(byte[] string)
  {
    try {
      Cipher cipher = Cipher.getInstance("RSA/None/PKCS1Padding", "BC");
      RSAPrivateKey pbk = (RSAPrivateKey)keyPair.getPrivate();
      cipher.init(2, pbk);
      return cipher.doFinal(string);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  




  public static String generateBase64PublicKey()
  {
    RSAPublicKey key = (RSAPublicKey)keyPair.getPublic();
    return new String(Base64.encodeBase64(key.getEncoded()));
  }
  
  public static String Encrypt(String sSrc, String sKey, String iV) throws Exception {
    if (sKey == null) {
      log.info("Key为空null");
      return null;
    }
    
    if (sKey.length() != 16) {
      log.info("Key长度不是16位");
      return null;
    }
    byte[] raw = sKey.getBytes();
    SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    IvParameterSpec iv = new IvParameterSpec(iV.getBytes());
    cipher.init(1, skeySpec, iv);
    byte[] encrypted = cipher.doFinal(sSrc.getBytes());
    
    return Base64.encodeBase64String(encrypted);
  }
  
  public static String Decrypt(String sSrc, String sKey, String iV) throws Exception
  {
    try
    {
      if (sKey == null) {
        log.info("Key为空null");
        return null;
      }
      
      if (sKey.length() != 16) {
        log.info("Key长度不是16位");
        return null;
      }
      byte[] raw = sKey.getBytes(CharsetUtil.UTF_8);
      SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      IvParameterSpec iv = new IvParameterSpec(iV.getBytes());
      cipher.init(2, skeySpec, iv);
      byte[] encrypted1 = Base64.decodeBase64(sSrc);
      try {
        byte[] original = cipher.doFinal(encrypted1);
        return new String(original);
      }
      catch (Exception e) {
        log.info("", e);
        return null;
      }
      

      return null;
    }
    catch (Exception ex)
    {
      log.info("", ex);
    }
  }
}
