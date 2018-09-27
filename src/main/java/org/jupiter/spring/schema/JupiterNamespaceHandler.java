package org.jupiter.spring.schema;

import org.jupiter.spring.support.JupiterSpringClient;
import org.jupiter.spring.support.JupiterSpringConsumerBean;
import org.jupiter.spring.support.JupiterSpringProviderBean;
import org.jupiter.spring.support.JupiterSpringServer;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;




















public class JupiterNamespaceHandler
  extends NamespaceHandlerSupport
{
  public JupiterNamespaceHandler() {}
  
  public void init()
  {
    registerBeanDefinitionParser("server", new JupiterBeanDefinitionParser(JupiterSpringServer.class));
    registerBeanDefinitionParser("client", new JupiterBeanDefinitionParser(JupiterSpringClient.class));
    registerBeanDefinitionParser("provider", new JupiterBeanDefinitionParser(JupiterSpringProviderBean.class));
    registerBeanDefinitionParser("consumer", new JupiterBeanDefinitionParser(JupiterSpringConsumerBean.class));
  }
}
