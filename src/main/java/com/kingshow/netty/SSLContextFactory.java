package com.kingshow.netty;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;

public class SSLContextFactory
{
  public SSLContextFactory() {}
  
  public static SslContext getSslContext() throws Exception
  {
    char[] passArray = "ds3iu32ndf".toCharArray();
    



    String os = System.getProperty("os.name");
    String path = null;
    if (os.toLowerCase().startsWith("win"))
    {
      path = "e:\\ssl\\pay.xiinz.com.jks";
    }
    else {
      String separator = File.separator;
      path = separator + "home" + separator + "lbs.n4321.com.jks";
    }
    






    KeyManagerFactory keyManagerFactory = null;
    KeyStore keyStore = KeyStore.getInstance("JKS");
    keyStore.load(new FileInputStream(path), passArray);
    keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
    keyManagerFactory.init(keyStore, passArray);
    SslContext sslContext = SslContextBuilder.forServer(keyManagerFactory).sslProvider(SslProvider.OPENSSL).build();
    
    return sslContext;
  }
}
