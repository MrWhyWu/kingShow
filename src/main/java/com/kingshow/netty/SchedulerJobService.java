package com.kingshow.netty;

import java.io.PrintStream;
import java.util.Date;
import org.quartz.DateBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;




public class SchedulerJobService
  extends Thread
{
  public SchedulerJobService() {}
  
  public void run()
  {
    AuctionHandlerServer.start();
    
    SchedulerFactory sf = new StdSchedulerFactory();
    

    try
    {
      Scheduler sched = sf.getScheduler();
      
      Date startTime = DateBuilder.nextGivenMinuteDate(null, 1);
      
      JobDetail job = JobBuilder.newJob(ConnectorAuctionJob.class).withIdentity("job1", "group1").build();
      SimpleTrigger trigger = 
        (SimpleTrigger)TriggerBuilder.newTrigger().withIdentity("trigger1", "group1").startAt(startTime).withSchedule(SimpleScheduleBuilder.simpleSchedule()).build();
      sched.scheduleJob(job, trigger);
      
      sched.start();
      

      Thread.sleep(65000L);
      

      sched.shutdown(true);
      System.out.println("调度器停止");
    }
    catch (SchedulerException e1) {
      e1.printStackTrace();
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
