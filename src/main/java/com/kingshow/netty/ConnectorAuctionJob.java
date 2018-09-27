package com.kingshow.netty;

import com.kingshow.regedit.AuctionClientFactory;
import java.io.PrintStream;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;


public class ConnectorAuctionJob
  implements Job
{
  public ConnectorAuctionJob() {}
  
  public void execute(JobExecutionContext arg0)
    throws JobExecutionException
  {
    AuctionClientFactory.regeditClient();
    System.out.println("链接竞技服务器。。。。。。");
  }
}
