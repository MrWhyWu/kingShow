package com.kingshow.netty;

import com.kingshow.ImplService.AuctionOutServiceImpl;
import java.io.PrintStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jupiter.rpc.DefaultServer;
import org.jupiter.rpc.JServer;
import org.jupiter.rpc.JServer.ServiceRegistry;
import org.jupiter.rpc.provider.ProviderInterceptor;
import org.jupiter.transport.netty.JNettyTcpAcceptor;





public class AuctionHandlerServer
{
  private static ExecutorService executor = Executors.newFixedThreadPool(1);
  private static JServer server = null;
  
  public AuctionHandlerServer() {}
  
  public static synchronized void start() { executor.submit(new Runnable()
    {

      public void run()
      {
        if (AuctionHandlerServer.server != null) {
          return;
        }
        AuctionHandlerServer.server = new DefaultServer().withAcceptor(new JNettyTcpAcceptor(19000));
        CountDownLatch latch = new CountDownLatch(1);
        System.out.println("监听19000...............");
        
        try
        {
          AuctionHandlerServer.server.serviceRegistry().provider(new AuctionOutServiceImpl(), new ProviderInterceptor[0]).register();
          

          AuctionHandlerServer.server.start();
        }
        catch (InterruptedException e1) {
          e1.printStackTrace();
        }
        try
        {
          latch.await();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }); }
}
