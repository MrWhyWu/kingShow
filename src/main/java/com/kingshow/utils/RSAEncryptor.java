package com.kingshow.utils;

import java.io.IOException;
import java.io.PrintStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;







public class RSAEncryptor
{
  private RSAPrivateKey privateKey;
  private RSAPublicKey publicKey;
  
  public RSAEncryptor(String privateKeyStr, String publicKeyStr)
    throws Exception
  {
    loadPublicKey(publicKeyStr);
    loadPrivateKey(privateKeyStr);
  }
  


  public RSAEncryptor() {}
  

  public String decryptWithBase64(String base64String)
    throws Exception
  {
    byte[] binaryData = decrypt(getPrivateKey(), new BASE64Decoder().decodeBuffer(base64String));
    String string = new String(binaryData);
    return string;
  }
  
  public String encryptWithBase64(String string)
    throws Exception
  {
    byte[] binaryData = encrypt(getPublicKey(), string.getBytes());
    String base64String = new BASE64Encoder().encodeBuffer(binaryData);
    return base64String;
  }
  




  public RSAPrivateKey getPrivateKey()
  {
    return privateKey;
  }
  



  public RSAPublicKey getPublicKey()
  {
    return publicKey;
  }
  


  public void genKeyPair()
  {
    KeyPairGenerator keyPairGen = null;
    try {
      keyPairGen = KeyPairGenerator.getInstance("RSA");
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    keyPairGen.initialize(1024, new SecureRandom());
    KeyPair keyPair = keyPairGen.generateKeyPair();
    privateKey = ((RSAPrivateKey)keyPair.getPrivate());
    publicKey = ((RSAPublicKey)keyPair.getPublic());
  }
  


  public void loadPublicKey(String publicKeyStr)
    throws Exception
  {
    try
    {
      BASE64Decoder base64Decoder = new BASE64Decoder();
      byte[] buffer = base64Decoder.decodeBuffer(publicKeyStr);
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
      publicKey = ((RSAPublicKey)keyFactory.generatePublic(keySpec));
    } catch (NoSuchAlgorithmException e) {
      throw new Exception("无此算法");
    } catch (InvalidKeySpecException e) {
      throw new Exception("公钥非法");
    } catch (IOException e) {
      throw new Exception("公钥数据内容读取错误");
    } catch (NullPointerException e) {
      throw new Exception("公钥数据为空");
    }
  }
  
  public void loadPrivateKey(String privateKeyStr) throws Exception {
    try {
      BASE64Decoder base64Decoder = new BASE64Decoder();
      byte[] buffer = base64Decoder.decodeBuffer(privateKeyStr);
      PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      privateKey = ((RSAPrivateKey)keyFactory.generatePrivate(keySpec));
    } catch (NoSuchAlgorithmException e) {
      throw new Exception("无此算法");
    } catch (InvalidKeySpecException e) {
      e.printStackTrace();
      throw new Exception("私钥非法");
    } catch (IOException e) {
      throw new Exception("私钥数据内容读取错误");
    } catch (NullPointerException e) {
      throw new Exception("私钥数据为空");
    }
  }
  





  public byte[] encrypt(RSAPublicKey publicKey, byte[] plainTextData)
    throws Exception
  {
    if (publicKey == null) {
      throw new Exception("加密公钥为空, 请设置");
    }
    Cipher cipher = null;
    try {
      cipher = Cipher.getInstance("RSA");
      cipher.init(1, publicKey);
      return cipher.doFinal(plainTextData);
    }
    catch (NoSuchAlgorithmException e) {
      throw new Exception("无此加密算法");
    } catch (NoSuchPaddingException e) {
      e.printStackTrace();
      return null;
    } catch (InvalidKeyException e) {
      throw new Exception("加密公钥非法,请检查");
    } catch (IllegalBlockSizeException e) {
      throw new Exception("明文长度非法");
    } catch (BadPaddingException e) {
      throw new Exception("明文数据已损坏");
    }
  }
  





  public byte[] decrypt(RSAPrivateKey privateKey, byte[] cipherData)
    throws Exception
  {
    if (privateKey == null) {
      throw new Exception("解密私钥为空, 请设置");
    }
    Cipher cipher = null;
    try {
      cipher = Cipher.getInstance("RSA");
      cipher.init(2, privateKey);
      return cipher.doFinal(cipherData);
    }
    catch (NoSuchAlgorithmException e) {
      throw new Exception("无此解密算法");
    } catch (NoSuchPaddingException e) {
      e.printStackTrace();
      return null;
    } catch (InvalidKeyException e) {
      throw new Exception("解密私钥非法,请检查");
    } catch (IllegalBlockSizeException e) {
      throw new Exception("密文长度非法");
    } catch (BadPaddingException e) {
      throw new Exception("密文数据已损坏");
    }
  }
  


  public static void main(String[] args)
  {
    try
    {
      RSAEncryptor rsaEncryptor = new RSAEncryptor("MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAIhIs/3wz/nod7Ff/0UMzyK4gRCjPLqSfYkxxtlLn8GEn5Tg9kgKEl+CBiVad3w1afgFivaTHHI7xCC9zyulFkKQ3Q5IuouBkaY2+hKUPDzRRer3RmxUcNM4e5IUfDwG//8Hh69Q0kEHyD22lXGvo/kQnoUyhH+RjZ1UVAJAzj7lAgMBAAECgYAVh26vsggY0Yl/Asw/qztZn837w93HF3cvYiaokxLErl/LVBJz5OtsHQ09f2IaxBFedfmy5CB9R0W/aly851JxrI8WAkx2W2FNllzhha01fmlNlOSumoiRF++JcbsAjDcrcIiR8eSVNuB6ymBCrx/FqhdX3+t/VUbSAFXYT9tsgQJBALsHurnovZS1qjCTl6pkNS0V5qio88SzYP7lzgq0eYGlvfupdlLX8/MrSdi4DherMTcutUcaTzgQU20uAI0EMyECQQC6il1Kdkw8Peeb0JZMHbs+cMCsbGATiAt4pfo1b/i9/BO0QnRgDqYcjt3J9Ux22dPYbDpDtMjMRNrAKFb4BJdFAkBMrdWTZOVc88IL2mcC98SJcII5wdL3YSeyOZto7icmzUH/zLFzM5CTsLq8/HDiqVArNJ4jwZia/q6Fg6e8KO2hAkB0EK1VLF/ox7e5GkK533Hmuu8XGWN6I5bHnbYd06qYQyTbbtHMBrFSaY4UH91Qwd3u9gAWqoCZoGnfT/o03V5lAkBqq8jZd2lHifey+9cf1hsHD5WQbjJKPPIb57CK08hn7vUlX5ePJ02Q8AhdZKETaW+EsqJWpNgsu5wPqsy2UynO", "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCISLP98M/56HexX/9FDM8iuIEQozy6kn2JMcbZS5/BhJ+U4PZIChJfggYlWnd8NWn4BYr2kxxyO8Qgvc8rpRZCkN0OSLqLgZGmNvoSlDw80UXq90ZsVHDTOHuSFHw8Bv//B4evUNJBB8g9tpVxr6P5EJ6FMoR/kY2dVFQCQM4+5QIDAQAB");
      








      String rsaBase46StringFromIOS = 
        "JF26qmQKDq1c3rjZ0ex0O3QelgvFB7t6H1fH224qOt7LwH6pEU/uRwdEYgy5SCYMQFLT+HFwSQwAVDRIGabCiZSocE9B6bdB2nIkdVeRsU/bC7hVyM2f40n4qqO1vXo87wBr58T4qA2L/oCNHJrZfWx5MADHFA3/0jr+t8k9IMc=";
      
      String decryptStringFromIOS = rsaEncryptor.decryptWithBase64(rsaBase46StringFromIOS);
      System.out.println("Decrypt result from ios client: \n" + decryptStringFromIOS);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
