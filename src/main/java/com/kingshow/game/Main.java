
package com.kingshow.game;

import com.kingshow.config.MainConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

public class Main
{
  private static AbstractApplicationContext ctx;
  
  public Main() {}
  
  public static void main(String[] args)
  {
    system.out.println("为什么没有智能提示？");
    ctx = new AnnotationConfigApplicationContext(new Class[] { MainConfig.class });
    
    ctx.registerShutdownHook();
    ctx.start();
  }
}
